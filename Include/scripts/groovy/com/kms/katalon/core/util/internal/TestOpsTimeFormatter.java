package com.kms.katalon.core.util.internal;

import java.text.MessageFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class TestOpsTimeFormatter {

    public static final String FORMAT_LAST_YEAR = "MMM dd, yyyy";

    public static final String FORMAT_LAST_MONTH = "MMM dd";

    public static final String FORMAT_OVER_A_DAY = "MMM dd, HH:mm";

    public static final String FORMAT_DATE = "MM/dd/yyyy HH:mm:ss";

    public static final ZoneId DEFAULT_ZONE = ZoneId.of("Z");

    private DateTimeLanguageBundle languageBundle;

    public TestOpsTimeFormatter() {
        this.languageBundle = DateTimeLanguageBundle.DEFAULT;
    }

    public TestOpsTimeFormatter(DateTimeLanguageBundle languageBundle) {
        this.languageBundle = languageBundle == null ? DateTimeLanguageBundle.DEFAULT : languageBundle;
    }

    public String formatDetail(Date dateTime) {
        return formatDetail(dateTime, ZoneId.systemDefault());
    }

    public String formatDetail(Date dateTime, ZoneId formatZone) {
        ZoneId zoneId = formatZone == null ? DEFAULT_ZONE : formatZone;
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(dateTime.toInstant(), zoneId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(FORMAT_DATE);
        return formatter.format(zonedDateTime);
    }

    public String formatEvent(Date dateTime, ZoneId formatZone) {
        return formatEvent(dateTime, null, formatZone);
    }

    public String formatEvent(Date dateTime) {
        return formatEvent(dateTime, null, null);
    }

    public String formatEvent(Date dateTime, Date comparedDate) {
        return formatEvent(dateTime, comparedDate, null);
    }

    public String formatEvent(Date dateTime, Date comparedDate, ZoneId zoneId) {
        Objects.requireNonNull(dateTime);
        ZoneId timeZone = zoneId == null ? DEFAULT_ZONE : zoneId;
        ZonedDateTime dateTimeInSystemZone = ZonedDateTime.ofInstant(dateTime.toInstant(), timeZone)
                .withZoneSameInstant(ZoneId.systemDefault());
        ZonedDateTime comparedTimeInSystemZone = (comparedDate == null) ? ZonedDateTime.now()
                : ZonedDateTime.ofInstant(comparedDate.toInstant(), timeZone);
        return formatEvent(dateTimeInSystemZone, comparedTimeInSystemZone);
    }

    public String formatEvent(ZonedDateTime dateTime, ZonedDateTime comparedDate) {
        if (Math.abs(dateTime.getYear() - comparedDate.getYear()) != 0) {
            return formatLastYear(dateTime);
        }
        if (Math.abs(dateTime.getMonthValue() - comparedDate.getMonthValue()) != 0) {
            return formatLastMonth(dateTime);
        }
        if (Math.abs(dateTime.getDayOfMonth() - comparedDate.getDayOfMonth()) != 0) {
            return formatOverADay(dateTime);
        }
        return format(dateTime, comparedDate);
    }

    private String format(ZonedDateTime dateTime, ZonedDateTime now) {
        long timeElapsed = now.toInstant().toEpochMilli() - dateTime.toInstant().toEpochMilli();
        boolean isPast = timeElapsed >= 0;
        timeElapsed = Math.abs(timeElapsed);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(timeElapsed);
        long minutes = TimeUnit.SECONDS.toMinutes(seconds);
        long hours = TimeUnit.MINUTES.toHours(minutes);
        StringBuilder formatter = new StringBuilder();
        // add prefix
        if (isPast) {
            formatter.append(languageBundle.getProperty(DateTimeLanguageBundle.PROP_PAST_PREFIX));
        } else {
            formatter.append(languageBundle.getProperty(DateTimeLanguageBundle.PROP_FUTURE_PREFIX));
        }

        // handle time format
        formatter.append(" ");
        formatter.append(formatTime(hours, minutes, seconds, timeElapsed));

        formatter.append(" ");
        // add suffix
        if (isPast) {
            formatter.append(languageBundle.getProperty(DateTimeLanguageBundle.PROP_PAST_SUFFIX));
        } else {
            formatter.append(languageBundle.getProperty(DateTimeLanguageBundle.PROP_FUTURE_SUFFIX));
        }
        return formatter.toString().trim();
    }

    private String formatTime(long hours, long minutes, long seconds, long timeElapsed) {
        if (timeElapsed < 1000) {
            return languageBundle.getProperty(DateTimeLanguageBundle.PROP_TINY_TIME);
        }
        if ((timeElapsed % 1000) >= 500) {
            ++seconds;
        }
        if ((seconds % 60) >= 30) {
            ++minutes;
        }
        if ((minutes % 60) >= 30) {
            ++hours;
        }
        if (hours > 0) {
            String format = (hours > 1) ? languageBundle.getProperty(DateTimeLanguageBundle.PROP_HOURS)
                    : languageBundle.getProperty(DateTimeLanguageBundle.PROP_HOUR);
            return MessageFormat.format(format, String.valueOf(hours));
        }
        if (minutes > 0) {
            String format = (minutes > 1) ? languageBundle.getProperty(DateTimeLanguageBundle.PROP_MINUTES)
                    : languageBundle.getProperty(DateTimeLanguageBundle.PROP_MINUTE);
            return MessageFormat.format(format, String.valueOf(minutes));
        }
        if (seconds > 0) {
            String format = (seconds > 1) ? languageBundle.getProperty(DateTimeLanguageBundle.PROP_SECONDS)
                    : languageBundle.getProperty(DateTimeLanguageBundle.PROP_SECOND);
            return MessageFormat.format(format, String.valueOf(seconds));
        }
        throw new UnsupportedOperationException("Unsupported operation for given parameters");
    }

    private String formatLastYear(ZonedDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(FORMAT_LAST_YEAR);
        return formatter.format(dateTime);
    }

    private String formatLastMonth(ZonedDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(FORMAT_LAST_MONTH);
        return formatter.format(dateTime);
    }

    private String formatOverADay(ZonedDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(FORMAT_OVER_A_DAY);
        return formatter.format(dateTime);
    }

    public String formatDuration(long elapsedTime) {
        return formatDuration(elapsedTime, false);
    }

    public String formatDuration(long elapsedTime, boolean showZeroValue) {
        boolean isMillisOnly = elapsedTime < 1000;
        long seconds = elapsedTime / 1000;
        elapsedTime %= 1000;
        long minutes = seconds / 60;
        seconds %= 60;
        long hours = minutes / 60;
        minutes %= 60;
        long days = hours / 24;
        hours %= 24;
        if (!isMillisOnly && elapsedTime >= 500) {
            ++seconds;
            elapsedTime = 0;
        }
        if (seconds >= 60) {
            ++minutes;
            seconds = 0;
        }
        if (minutes >= 60) {
            ++hours;
            minutes = 0;
        }
        if (hours >= 24) {
            ++days;
            hours = 0;
        }
        StringBuilder builder = new StringBuilder();
        if (days != 0 || showZeroValue) {
            builder.append(MessageFormat.format(languageBundle.getProperty(DateTimeLanguageBundle.PROP_DAY_SHORT),
                    String.valueOf(days)));
        }
        if (hours != 0 || showZeroValue) {
            builder.append(" ");
            builder.append(MessageFormat.format(languageBundle.getProperty(DateTimeLanguageBundle.PROP_HOUR_SHORT),
                    String.valueOf(hours)));
        }
        if (minutes != 0 || showZeroValue) {
            builder.append(" ");
            builder.append(MessageFormat.format(languageBundle.getProperty(DateTimeLanguageBundle.PROP_MINUTE_SHORT),
                    String.valueOf(minutes)));
        }
        if (hours == 0 && (seconds != 0 || showZeroValue)) {
            builder.append(" ");
            builder.append(MessageFormat.format(languageBundle.getProperty(DateTimeLanguageBundle.PROP_SECOND_SHORT),
                    String.valueOf(seconds)));
        }
        if (builder.length() == 0) {
            builder.append(MessageFormat.format(languageBundle.getProperty(DateTimeLanguageBundle.PROP_MILLI),
                    String.valueOf(elapsedTime)));
        }
        return builder.toString().trim();
    }

}
