package com.apipas.easyflow.android;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;

public class AsyncExecutor implements Executor {
	private Executor executor = Executors.newSingleThreadExecutor();

	@Override
	public void execute(@NonNull Runnable task) {
		executor.execute(task);
	}
}
