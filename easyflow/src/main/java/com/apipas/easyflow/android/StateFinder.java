package com.apipas.easyflow.android;


import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class StateFinder<C extends FlowContext> {
    private String id;
    private State<C> startState;
    private Set<State<C>> states;

    public StateFinder(State<C> startState, String id) {
        this.startState = startState;
        states = new HashSet<>();
        this.id = id;
    }

    public State<C> find() {
        return find(startState);
    }

    private State<C> find(State<C> state) {
        if (!states.contains(state)) {
            // haven't started with this state yet
            states.add(state);

            for (Map.Entry<Event<C>, State<C>> e : state.getTransitions().entrySet()) {
                State<C> stateTo = e.getValue();
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
