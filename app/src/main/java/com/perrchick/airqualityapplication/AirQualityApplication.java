package com.perrchick.airqualityapplication;

import android.app.Application;
import android.os.Handler;
import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;

public class AirQualityApplication extends Application {
    private static WeakReference<AirQualityApplication> shared;
    private Handler mainThreadHandler;

    @androidx.annotation.Nullable
    public static AirQualityApplication shared() {
        return shared.get();
    }

    @Nullable
    public static AirQualityApplication instance() {
        return shared();
    }

    @Nullable
    public static AirQualityApplication getInstance() {
        return shared();
    }

    public static AirQualityApplication sharedInstance() {
        return shared();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        shared = new WeakReference<>(this);
        mainThreadHandler = new Handler();
    }

    public void runOnUiThread(Runnable runnable) {
        mainThreadHandler.post(runnable);
    }

    public void removeFromUiThread(Runnable runnable) {
        mainThreadHandler.removeCallbacks(runnable);
    }
}
