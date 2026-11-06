package com.careeragent.prep;

import java.util.List;

public record PrepSheet(List<String> technical, List<String> behavioral) {
    public String describe() {
        StringBuilder sb = new StringBuilder("Technical questions:");
        int i = 1;
        for (String q : technical) {
            sb.append("\n  ").append(i++).append(". ").append(q);
        }
        sb.append("\nBehavioral questions:");
        i = 1;
        for (String q : behavioral) {
            sb.append("\n  ").append(i++).append(". ").append(q);
        }
        return sb.toString();
    }
}