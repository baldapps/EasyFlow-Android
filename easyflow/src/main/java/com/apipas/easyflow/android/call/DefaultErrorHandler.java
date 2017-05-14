package com.apipas.easyflow.android.call;


import android.util.Log;

import com.apipas.easyflow.android.err.ExecutionError;

public class DefaultErrorHandler implements ExecutionErrorHandler {

    private static final String TAG = DefaultErrorHandler.class.getSimpleName();

    @Override
    public void call(ExecutionError error) {
        String msg = "Execution Error in State [" + error.getState() + "] ";
        if (error.getEvent() != null) {
            msg += "on Event [" + error.getEvent() + "] ";
        }
        msg += "with Context [" + error.getContext() + "] ";

        Exception e = new Exception(msg, error);
        Log.e(TAG, "Error", e);
    }
}
