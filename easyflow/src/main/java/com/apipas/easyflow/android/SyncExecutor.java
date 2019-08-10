package com.apipas.easyflow.android;

import java.util.ArrayList;
import java.util.concurrent.Executor;

import androidx.annotation.NonNull;

@SuppressWarnings("unused")
public class SyncExecutor implements Executor {
    private ArrayList<Runnable> queue = new ArrayList<Runnable>();
    private boolean running = false;

	@Override
	public void execute(@NonNull Runnable task) {
        queue.add(task);

        if (!running) {
            while (!queue.isEmpty()) {
                Runnable nextTask = queue.remove(queue.size() - 1);
                running = true;
                nextTask.run();
                running = false;
            }
        }
    }
}
