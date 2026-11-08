package com.careeragent.ingestion;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;

/**
 * Real page downloader. Only http and https are allowed, local and private network addresses are
 * refused, requests time out, and the body is capped at 1 MB.
 */
public class HttpPageFetcher implements PageFetcher {
    static final int MAX_BYTES = 1_000_000;

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    @Override
    public String fetch(String url) {
        URI uri = checkUrl(url);
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(15))
                .header("User-Agent", "Mozilla/5.0 (compatible; CareerAgent/0.1)")
                .GET()
                .build();
        try {
            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() >= 400) {
                response.body().close();
                throw new JobIngestException("The site returned HTTP " + response.statusCode()
                        + ". Paste the job description text instead.");
            }
            try (InputStream in = response.body()) {
                return new String(in.readNBytes(MAX_BYTES), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new JobIngestException("Could not download that page: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new JobIngestException("The download was interrupted.", e);
        }
    }

    /** Validates the URL and returns it as a URI, or throws JobIngestException. */
    static URI checkUrl(String url) {
        URI uri;
        try {
            uri = new URI(url.trim());
        } catch (URISyntaxException e) {
            throw new JobIngestException("That is not a valid URL.");
        }
        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
        if (!scheme.equals("http") && !scheme.equals("https")) {
            throw new JobIngestException("Only http and https links are supported.");
        }
        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new JobIngestException("That URL has no website name.");
        }
        try {
            for (InetAddress address : InetAddress.getAllByName(host)) {
                if (address.isLoopbackAddress() || address.isSiteLocalAddress()
                        || address.isLinkLocalAddress() || address.isAnyLocalAddress()) {
                    throw new JobIngestException("Links to local or private network addresses are not allowed.");
                }
            }
        } catch (UnknownHostException e) {
            throw new JobIngestException("Could not find that website.");
        }
        return uri;
    }
}