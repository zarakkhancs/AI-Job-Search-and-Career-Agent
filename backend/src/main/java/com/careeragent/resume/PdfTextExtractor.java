package com.careeragent.resume;

import java.io.IOException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

/** Reads the selectable text out of a PDF file (Apache PDFBox). */
public class PdfTextExtractor {
    static final int MAX_BYTES = 5 * 1024 * 1024;

    public String extract(byte[] pdf) {
        if (pdf == null || pdf.length == 0) {
            throw new ResumeException("The uploaded file is empty.");
        }
        if (pdf.length > MAX_BYTES) {
            throw new ResumeException("The PDF is larger than 5 MB.");
        }
        String text;
        try (PDDocument document = Loader.loadPDF(pdf)) {
            text = new PDFTextStripper().getText(document);
        } catch (IOException e) {
            throw new ResumeException("Could not read the PDF. Is it a valid, unencrypted PDF file?", e);
        }
        String cleaned = clean(text);
        if (cleaned.isEmpty()) {
            throw new ResumeException(
                    "No selectable text was found in the PDF (is it a scanned image?). Paste the text instead.");
        }
        return cleaned;
    }

    static String clean(String text) {
        StringBuilder sb = new StringBuilder();
        boolean lastBlank = true;
        for (String line : text.replace("\r", "").split("\n")) {
            String t = line.replaceAll("[ \\t\\u00a0]+", " ").trim();
            if (t.isEmpty()) {
                if (!lastBlank) {
                    sb.append("\n");
                    lastBlank = true;
                }
            } else {
                sb.append(t).append("\n");
                lastBlank = false;
            }
        }
        return sb.toString().trim();
    }
}