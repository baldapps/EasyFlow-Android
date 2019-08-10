package com.apipas.easyflow.android;

import com.apipas.easyflow.android.err.DefinitionError;

import java.util.HashSet;
import java.util.Set;

public class FlowBuilder<C extends FlowContext> {
	private State<C> startState;
	private static Set<String> states = new HashSet<>();
	
	protected FlowBuilder(State<C> startState) {
		this.startState = startState;
	}
	
	public static <C extends FlowContext> FlowBuilder<C> from(State<C> startState) {
		return new FlowBuilder<>(startState);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public EasyFlow<C> transit(TransitionBuilder... transitions) {
        for (TransitionBuilder<C> transition : transitions) {
            transition.getEvent().addTransition(startState, transition.getStateTo());
        }

		return new EasyFlow<C>(startState, transitions);
	}
	
	public static <C extends FlowContext> Event<C> event(String name) {
		return new Event<>(name);
	}
	
	public static <C extends FlowContext> Event<C> event() {
		return new Event<>();
	}
	
	public static <C extends FlowContext> State<C> state(String name) throws DefinitionError {
		if (states.contains(name))
			throw new DefinitionError("state name must be unique");
		states.add(name);
		return new State<>(name);
	}
}
