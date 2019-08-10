package com.apipas.easyflow.android;



import com.apipas.easyflow.android.err.DefinitionError;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LogicValidator {
	private State startState;
	private Set<State> states;
	
	public LogicValidator(State startState) {
		this.startState = startState;
		states = new HashSet<>();
	}
	
	public void validate() throws DefinitionError {
		validate(startState);
	}
	
	private void validate(State state) throws DefinitionError {
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
			
			for (Map.Entry<Event, State> e : state.getTransitions().entrySet()) {
				State stateTo = e.getValue();
				if (state.equals(stateTo)) {
					throw new DefinitionError("Circular Event usage: " + e.getKey());
				}
				validate(stateTo);
			}
		}
	}
}
