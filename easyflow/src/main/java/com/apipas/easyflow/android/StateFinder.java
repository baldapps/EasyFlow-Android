package com.apipas.easyflow.android;


import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class StateFinder {
    private String id;
    private State startState;
    private Set<State> states;

    public StateFinder(State startState, String id) {
        this.startState = startState;
        states = new HashSet<>();
        this.id = id;
    }

    public State find() {
        return find(startState);
    }

    private State find(State state) {
        if (!states.contains(state)) {
            // haven't started with this state yet
            states.add(state);

            if (state.getId().equals(id))
                return state;

            for (Map.Entry<Event, State> e : state.getTransitions().entrySet()) {
                State stateTo = e.getValue();
                if (stateTo.getId().equals(id))
                    return stateTo;
                stateTo = find(stateTo);
                if (stateTo != null)
                    return stateTo;
            }
        }
        return null;
    }
}
