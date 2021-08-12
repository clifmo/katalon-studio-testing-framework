package com.kms.katalon.core.webui.common;

public enum FontStyle {
    PLAIN(0), 
    BOLD(1), 
    ITALIC(2);

    private final int value;

    private FontStyle(int value) {
        this.value = value;
    }

    public int get() {
        return this.value;
    }
}
