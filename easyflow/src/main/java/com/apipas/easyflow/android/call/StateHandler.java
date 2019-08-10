package com.apipas.easyflow.android.call;


import com.apipas.easyflow.android.State;
import com.apipas.easyflow.android.FlowContext;

public interface StateHandler {
	void call(State state, FlowContext context) throws Exception;
}
