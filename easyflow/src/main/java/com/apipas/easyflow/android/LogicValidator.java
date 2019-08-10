package com.apipas.easyflow.android;



import com.apipas.easyflow.android.err.DefinitionError;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LogicValidator <C extends FlowContext> {
	private State<C> startState;
	private Set<State<C>> states;
	
	public LogicValidator(State<C> startState) {
		this.startState = startState;
		states = new HashSet<>();
	}
	
	public void validate() throws DefinitionError {
		validate(startState);
	}
	
	private void validate(State<C> state) throws DefinitionError {
		if (!states.contains(state)) {
			// haven't started with this state yet
			states.add(state);
			
			if (state.isFinal()) {
				if (!state.getTransitions().isEmpty()) {
					throw new DefinitionError("Some events defined for final State: " + state);
				}
			} else {
				if (state.getTransitions().isEmpty()) {
					throw new DefinitionError("No events defined for non-final State: " + state);
				}
			}
			
			for (Map.Entry<Event<C>, State<C>> e : state.getTransitions().entrySet()) {
				State<C> stateTo = e.getValue();
				if (state.equals(stateTo)) {
					throw new DefinitionError("Circular Event usage: " + e.getKey());
				}
				validate(stateTo);
			}
		}
	}
}
