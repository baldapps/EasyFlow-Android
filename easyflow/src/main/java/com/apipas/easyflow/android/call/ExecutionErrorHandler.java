package com.apipas.easyflow.android.call;


import com.apipas.easyflow.android.err.ExecutionError;

public interface ExecutionErrorHandler {
	void call(ExecutionError error);
}
