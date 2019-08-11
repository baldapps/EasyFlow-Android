package com.apipas.easyflow.android;

import com.apipas.easyflow.android.err.DefinitionError;

import java.util.HashSet;
import java.util.Set;

import androidx.annotation.NonNull;

@SuppressWarnings("unused")
public class FlowBuilder {
    private State startState;
    private EasyFlow result;
    private String tag;
    private static Set<String> states = new HashSet<>();

    protected FlowBuilder(State startState, @NonNull String tag) {
        this.startState = startState;
        this.tag = tag;
    }

    public static FlowBuilder from(State startState, @NonNull String tag) {
        return new FlowBuilder(startState, tag);
    }

    public FlowBuilder transit(TransitionBuilder... transitions) {
        for (TransitionBuilder transition : transitions) {
            transition.getEvent().addTransition(startState, transition.getStateTo());
        }

        result = new EasyFlow(startState, transitions);
        return this;
    }

    public EasyFlow build() {
        states.clear();
        result.setTag(tag);
        return result;
    }

    public static Event event(String name) {
        return new Event(name);
    }

    public static Event event() {
        return new Event();
    }

    public static State state(String name) throws DefinitionError {
        if (states.contains(name))
            throw new DefinitionError("state name must be unique");
        states.add(name);
        return new State(name);
    }
}
