package com.apipas.easyflow.android.err;

import com.apipas.easyflow.android.Event;
import com.apipas.easyflow.android.State;
import com.apipas.easyflow.android.StatefulContext;

@SuppressWarnings("rawtypes")
public class ExecutionError extends Exception {
	private static final long serialVersionUID = 4362053831847081229L;
	private State state;
	private Event event;
	private StatefulContext context;
	
	public ExecutionError(State state, Event event, Exception error, String message, StatefulContext context) {
		super(message, error);
		
		this.state = state;
		this.event = event;
		this.context = context;
	}

	public State getState() {
		return state;
	}

	public Event getEvent() {
		return event;
	}

	public StatefulContext getContext() {
		return context;
	}
}
