package com.careeragent.prompts;

/** Product of the Factory Method: a ready-to-send prompt for one kind of AI feature. */
public interface Prompt {
    String name();

    String text();
}