package com.apipas.easyflow.android;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;

import androidx.annotation.NonNull;

@SuppressWarnings("unused")
public class UiThreadExecutor implements Executor {
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public void execute(@NonNull Runnable command) {
        handler.post(command);
    }
}