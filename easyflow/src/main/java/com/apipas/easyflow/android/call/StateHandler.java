package com.apipas.easyflow.android.call;


import com.apipas.easyflow.android.State;
import com.apipas.easyflow.android.StatefulContext;

public interface StateHandler<C extends StatefulContext> {
	void call(State<C> state, C context) throws Exception;
}
