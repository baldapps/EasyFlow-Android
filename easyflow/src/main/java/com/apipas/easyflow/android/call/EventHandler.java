package com.apipas.easyflow.android.call;


import com.apipas.easyflow.android.Event;
import com.apipas.easyflow.android.State;
import com.apipas.easyflow.android.FlowContext;

public interface EventHandler {
	void call(Event event, State from, State to, FlowContext context) throws Exception;
}
