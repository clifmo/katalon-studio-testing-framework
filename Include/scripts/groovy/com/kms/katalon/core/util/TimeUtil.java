package com.kms.katalon.core.util;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class TimeUtil {
    public static final long INFINITE_TIMEOUT = 0;

    public interface IntervalCallback {
        void call(Timer timer);
    }

    public interface TimeoutCallback {
        void call();
    }

    public static Timer setInterval(IntervalCallback callback, long interval) {
        return setInterval(callback, interval, INFINITE_TIMEOUT);
    }

    public static Timer setInterval(IntervalCallback callback, long interval, long duration) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                callback.call(timer);
            }
        }, interval, interval);

        if (duration > 0) {
            setTimeout(() -> {
                timer.cancel();
            }, duration);
        }

        return timer;
    }

    public static Timer setTimeout(TimeoutCallback callback, long timeout) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                callback.call();
            }
        }, timeout);
        return timer;
    }

    public static boolean isCanceled(Timer timer) {
        if (timer == null) {
            return true;
        }
        return ((int) ObjectUtil.getField(ObjectUtil.getField(timer, "queue"), "size")) <= 0;
    }

    /**
     * @return true if the timer is running and successfully canceled, false if the timer is already canceled or the timer is null
     */
    public static boolean cancel(Timer timer) {
        if (!TimeUtil.isCanceled(timer)) {
            timer.cancel();
            return true;
        }
        return false;
    }

    /**
     * Get a diff between two dates
     * 
     * @param date1 the oldest date
     * @param date2 the newest date
     * @param timeUnit the unit in which you want the diff
     * @return the diff value, in the provided unit
     */
    public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }
}
