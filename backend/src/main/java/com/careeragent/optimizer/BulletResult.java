package com.careeragent.optimizer;

import java.util.List;

public record BulletResult(String original, List<BulletVersion> versions) {
    public String describe() {
        StringBuilder sb = new StringBuilder("Original: ").append(original);
        int i = 1;
        for (BulletVersion v : versions) {
            sb.append("\n  ").append(i++).append(". ").append(v.text());
            if (!v.unsupportedFigures().isEmpty()) {
                sb.append("\n     ! Figures not in the original (verify): ")
                        .append(String.join(", ", v.unsupportedFigures()));
            }
        }
        return sb.toString();
    }
}