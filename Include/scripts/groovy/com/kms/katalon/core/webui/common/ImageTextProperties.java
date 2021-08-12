package com.kms.katalon.core.webui.common;

import java.awt.Color;

import org.apache.commons.lang.StringUtils;

import com.kms.katalon.core.webui.constants.CoreWebuiMessageConstants;

public class ImageTextProperties {
    private String text = StringUtils.EMPTY;

    private int x = 0;

    private int y = 0;

    private String font = "Arial";

    private int fontSize = 12;

    private Color fontColor = Color.BLACK;

    private FontStyle fontStyle = FontStyle.PLAIN;

    public ImageTextProperties() {
        super();
    }

    public ImageTextProperties(String text, int x, int y, String font, int fontSize, Color fontColor,
            FontStyle fontStyle) {
        this.setText(text);
        this.setX(x);
        this.setY(y);
        this.setFont(font);
        this.setFontSize(fontSize);
        this.setFontColor(fontColor);
        this.setFontStyle(fontStyle);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public String getFont() {
        return font;
    }

    public void setFont(String font) {
        if (font == null) {
            return;
        }
        this.font = font;
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        if (fontSize <= 0) {
            throw new IllegalArgumentException(CoreWebuiMessageConstants.MSG_ERR_PROPERTY_MUST_BE_A_POSITIVE_INT);
        }
        this.fontSize = fontSize;
    }

    public Color getFontColor() {
        return this.fontColor;
    }

    public void setFontColor(Color fontColor) {
        if (fontColor == null) {
            return;
        }
        this.fontColor = fontColor;
    }

    public int getFontStyle() {
        return fontStyle.get();
    }

    public void setFontStyle(FontStyle fontStyle) {
        if (fontStyle == null) {
            return;
        }
        this.fontStyle = fontStyle;
    }
}
