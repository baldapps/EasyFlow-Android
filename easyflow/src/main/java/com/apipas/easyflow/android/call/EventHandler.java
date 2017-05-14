package com.apipas.easyflow.android.call;


import com.apipas.easyflow.android.Event;
import com.apipas.easyflow.android.State;
import com.apipas.easyflow.android.StatefulContext;

public interface EventHandler<C extends StatefulContext> {
	void call(Event<C> event, State<C> from, State<C> to, C context) throws Exception;
}
