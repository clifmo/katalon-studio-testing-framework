package com.kms.katalon.core.util.internal;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DateTimeLanguageBundle {
    private Locale locale;

    private Map<String, String> properties;

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public DateTimeLanguageBundle(Locale locale, Map<String, String> properties) {
        this.locale = locale;
        this.properties = properties;
    }

    public String getProperty(String key) {
        return properties.get(key);
    }

    // static
    public static final DateTimeLanguageBundle DEFAULT;

    public static final String PROP_MILLI = "PROP_MILLI";

    public static final String PROP_SECOND = "PROP_SECOND";

    public static final String PROP_SECONDS = "PROP_SECONDS";

    public static final String PROP_SECOND_SHORT = "PROP_SECOND_SHORT";

    public static final String PROP_MINUTE = "PROP_MINUTE";

    public static final String PROP_MINUTES = "PROP_MINUTES";

    public static final String PROP_MINUTE_SHORT = "PROP_MINUTE_SHORT";

    public static final String PROP_HOUR = "PROP_HOUR";

    public static final String PROP_HOURS = "PROP_HOURS";

    public static final String PROP_HOUR_SHORT = "PROP_HOUR_SHORT";

    public static final String PROP_DAY = "PROP_DAY";

    public static final String PROP_DAYS = "PROP_DAYS";

    public static final String PROP_DAY_SHORT = "PROP_DAY_SHORT";

    public static final String PROP_PAST_PREFIX = "PROP_PAST_PREFIX";

    public static final String PROP_PAST_SUFFIX = "PROP_PAST_SUFFIX";

    public static final String PROP_FUTURE_PREFIX = "PROP_FUTURE_PREFIX";

    public static final String PROP_FUTURE_SUFFIX = "PROP_FUTURE_SUFFIX";

    public static final String PROP_TINY_TIME = "PROP_TINY_TIME";

    static {
        Locale locale = Locale.US;
        Map<String, String> props = new HashMap<>();
        props.put(PROP_MILLI, "{0}ms");
        props.put(PROP_SECOND, "{0} second");
        props.put(PROP_SECONDS, "{0} seconds");
        props.put(PROP_SECOND_SHORT, "{0}s");
        props.put(PROP_MINUTE, "{0} minute");
        props.put(PROP_MINUTES, "{0} minutes");
        props.put(PROP_MINUTE_SHORT, "{0}m");
        props.put(PROP_HOUR, "{0} hour");
        props.put(PROP_HOURS, "{0} hours");
        props.put(PROP_HOUR_SHORT, "{0}h");
        props.put(PROP_DAY, "{0} day");
        props.put(PROP_DAYS, "{0} days");
        props.put(PROP_DAY_SHORT, "{0}d");
        props.put(PROP_FUTURE_PREFIX, "in");
        props.put(PROP_FUTURE_SUFFIX, "");
        props.put(PROP_PAST_PREFIX, "");
        props.put(PROP_PAST_SUFFIX, "ago");
        props.put(PROP_TINY_TIME, "a few moment");
        DEFAULT = new DateTimeLanguageBundle(locale, props);
    }

}
