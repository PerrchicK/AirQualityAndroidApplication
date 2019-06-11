package com.perrchick.airqualityapplication.util;

import android.os.Handler;
import android.os.HandlerThread;

/**
 * Created by perrchick on 12/06/2017.
 */
public abstract class BRZTimer {
    private static final String TAG = BRZTimer.class.getSimpleName();
    private static Handler _sharedHandler;
    private final String key;
    private boolean shouldRepeat = false;

    private Runnable runnableTask;
    private final int afterDelayMillis;
    private boolean canceled;
    private boolean isStarted;
    private boolean isExecuted;

    protected BRZTimer(int afterDelayMillis, String key, boolean shouldRepeat) {
        this(afterDelayMillis, key);
        this.shouldRepeat = shouldRepeat;
    }

    protected BRZTimer(final int afterDelayMillis, final String key) {
        this.afterDelayMillis = afterDelayMillis;
        this.key = key;
        isStarted = false;
        isExecuted = false;
        canceled = false;

        runnableTask = () -> {
            if (!canceled || runnableTask != null) {
                if (!shouldRepeat) {
                    isExecuted = true;
                }

//                Repeater repeater;
//                repeater.setRepeaterDelay();
                onFinish(BRZTimer.this);

                if (shouldRepeat) {
                    BRZTimer.newTimer(afterDelayMillis, key, runnableTask).start();
                }
            } else {
                AppLogger.log(TAG, "canceled timer, key: " + getKey());
            }

            if (!shouldRepeat || canceled) {
                runnableTask = null;
            }
        };
    }

    public BRZTimer start() {
        if (isStarted) return this;

        isStarted = true;
        getSharedHandler().postDelayed(runnableTask, afterDelayMillis);
        //ScenesApplication.getHandler().postDelayed(runnableTask, afterDelayMillis);
        return this;
    }

    private static Handler getSharedHandler() {
        synchronized (BRZTimer.class) {
            if (_sharedHandler == null) {
                HandlerThread papaTimerThread = new HandlerThread(TAG);
                papaTimerThread.start();
                _sharedHandler = new Handler(papaTimerThread.getLooper());
            }
        }

        return _sharedHandler;
    }

    protected abstract void onFinish(BRZTimer timer);

    public void cancel() {
        getSharedHandler().removeCallbacks(runnableTask);

        canceled = true;
        runnableTask = null;
        //Log.v(TAG, "canceled timer with key: " + getKey());
    }

    public String getKey() {
        return key;
    }

    public boolean isExecuted() {
        return isExecuted;
    }

    public static BRZTimer newTimer(int afterDelayMillis, String key, Runnable runnable) {
        if (runnable == null) return null;

        return new BRZTimer(afterDelayMillis, key) {
            @Override
            protected void onFinish(BRZTimer timer) {
                runnable.run();
            }
        };
    }

    public boolean isCanceled() {
        return canceled;
    }
}
