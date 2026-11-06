package com.careeragent.optimizer;

import java.util.List;

/** One rewritten bullet plus any figures in it that the original bullet does not contain. */
public record BulletVersion(String text, List<String> unsupportedFigures) {}