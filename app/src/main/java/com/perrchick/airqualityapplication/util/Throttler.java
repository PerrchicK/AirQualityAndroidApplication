package com.perrchick.airqualityapplication.util;

import java.util.HashMap;

import androidx.annotation.Nullable;
import com.perrchick.airqualityapplication.AirQualityApplication;

/**
 * Created by perrchick on 31/10/2017.
 * Prevents from running redundant calls.
 * Translation: https://translate.google.com/#en/iw/throttle
 * Explanation: http://danielemargutti.com/2017/10/19/throttle-in-swift/
 */
public class Throttler {
    private static final HashMap<String, Throttler> throttles;
    private static final String TAG = Throttler.class.getSimpleName();

    static {
        throttles = new HashMap<>();
    }

    private final String throttleKey;
    private final int throttleDelay;
    private Runnable runnableToThrottle;
    @Nullable
    private BRZTimer timer;

    /**
     * Prevents from the runnable to run redundant times.
     * Extremely important to keep in mind that every call with parameters may be lost, the best usage should use a non-parameters methods, or use thius parameter as a key.
     * @param throttleKey      The key to identify the throttled task
     * @param runnable         The throttled task
     * @param throttleDelay    How long (in that case - int) should it wait?
     */
    public static void throttle(String throttleKey, Runnable runnable, int throttleDelay) {
        if (runnable == null) return;

        Throttler throttler = throttles.get(throttleKey);
        if (throttler == null) {
            throttler = new Throttler(throttleKey, throttleDelay);
            throttles.put(throttleKey, throttler);
         } else {
            throttler.cancel();
        }

        throttler.throttle(() -> {
            runnable.run();
            throttles.remove(throttleKey);
        });
    }

    private void cancel() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        runnableToThrottle = null;
    }

    private Throttler(String throttleKey, int throttleDelay) {
        this.throttleDelay = throttleDelay;
        this.throttleKey = throttleKey;
    }

    private void throttle(Runnable runnable) {
        if (timer != null) {
            timer.cancel();
        }
        runnableToThrottle = runnable;
        timer = BRZTimer.newTimer(throttleDelay, this.toString(), () -> {
            if (runnableToThrottle == null) return;
            AppLogger.log(TAG, "Executing throttled task with key: " + throttleKey);
            AirQualityApplication.sharedInstance().runOnUiThread(runnableToThrottle);
            runnableToThrottle = null;
        }).start();
    }
}
