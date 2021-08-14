package com.kms.katalon.core.webui.util;

import java.awt.Color;
import java.text.MessageFormat;

import org.apache.commons.lang3.StringUtils;

import com.kms.katalon.core.webui.common.FontStyle;
import com.kms.katalon.core.webui.constants.CoreWebuiMessageConstants;

public class ParseUtil {

    public static int parseInt(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        throw new ClassCastException(
                MessageFormat.format(CoreWebuiMessageConstants.MSG_ERR_CANNOT_PARSE_TO_CLASS, value, Integer.class));
    }

    public static FontStyle parseFontStyle(Object value) {
        if (value == null) {
            return FontStyle.PLAIN;
        }
        if (value instanceof FontStyle) {
            return (FontStyle) value;
        }
        if (value instanceof String) {
            String text = (String) value;
            return FontStyle.valueOf(text.toUpperCase());
        }
        throw new ClassCastException(
                MessageFormat.format(CoreWebuiMessageConstants.MSG_ERR_CANNOT_PARSE_TO_CLASS, value, FontStyle.class));

    }

    public static Color parseColor(Object value) {
        if (value == null) {
            return Color.BLACK;
        }
        if (value instanceof Color) {
            return (Color) value;
        }
        if (value instanceof String) {
            try {
                return Color.decode((String) value);
            } catch (Exception exception) {
                throw new IllegalArgumentException("'fontColor' must be a hex. Example: '#000000'");
            }
        }
        throw new ClassCastException(
                MessageFormat.format(CoreWebuiMessageConstants.MSG_ERR_CANNOT_PARSE_TO_CLASS, value, Color.class));
    }

    public static String parseString(Object value) {
        if (value == null) {
            return StringUtils.EMPTY;
        }
        if (value instanceof String) {
            return (String) value;
        }
        return value.toString();
    }
}
