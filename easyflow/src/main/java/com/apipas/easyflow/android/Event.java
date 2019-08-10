package com.apipas.easyflow.android;

import android.util.Log;

import com.apipas.easyflow.android.call.EventHandler;
import com.apipas.easyflow.android.err.DefinitionError;
import com.apipas.easyflow.android.err.ExecutionError;
import com.apipas.easyflow.android.err.LogicViolationError;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class Event {
    private static final String TAG = Event.class.getSimpleName();
    private static long idCounter = 1;

    private String id;
    private Map<State, State> transitions = new HashMap<>();
    private EasyFlow runner;

    private EventHandler onTriggeredHandler;

    public Event() {
        this.id = "Event_" + (idCounter++);
    }

    public Event(String id) {
        this.id = id;
    }

    public TransitionBuilder to(State stateTo) {
        return new TransitionBuilder(this, stateTo);
    }

    public TransitionBuilder finish(State stateTo) {
        stateTo.setFinal(true);
        return new TransitionBuilder(this, stateTo);
    }

    public Event whenTriggered(EventHandler onTriggered) {
        onTriggeredHandler = onTriggered;
        return this;
    }

    public void trigger(final FlowContext context) {

        try {
            if (null == runner) {
                throw new LogicViolationError("Invalid Event: " + Event.this.toString() +
                        " triggered while in State: " + context.getState() + " for " + context);
            }
        } catch (LogicViolationError logicViolationError) {
            Log.e(TAG, String.format("Event has no State to map to. You must define event %s transition in map Flow!", this), logicViolationError);
            return;
        }


        if (BuildConfig.DEBUG)
            Log.d(TAG, String.format("trigger %s for %s", this, context));

        if (context.isTerminated()) {
            return;
        }

        runner.execute(() -> {
            State stateFrom = context.getState();
            State stateTo = transitions.get(stateFrom);

            try {
                if (stateTo == null) {
                    throw new LogicViolationError("Invalid Event: " + Event.this.toString() +
                            " triggered while in State: " + context.getState() + " for " + context);
                } else {
                    callOnTriggered(context, stateFrom, stateTo);
                    runner.callOnEventTriggered(Event.this, stateFrom, stateTo, context);
                    runner.setCurrentState(stateTo, context);
                }
            } catch (Exception e) {
                runner.callOnError(new ExecutionError(stateFrom, Event.this, e,
                        "Execution Error in [Event.trigger]", context));
            }
        });
    }

    protected void addTransition(State from, State to) {
        State existingTransitionState = transitions.get(from);
        if (existingTransitionState != null) {
            if (existingTransitionState == to) {
                throw new DefinitionError("Duplicate transition[" + this + "] from " + from + " to " + to);
            } else {
                throw new DefinitionError("Ambiguous transition[" + this + "] from " + from + " to " + to + " and " +
                        existingTransitionState);

            }
        }

        transitions.put(from, to);
    }

    protected void setFlowRunner(EasyFlow runner) {
        this.runner = runner;
    }

    private void callOnTriggered(FlowContext context, State from, State to) throws Exception {
        if (onTriggeredHandler != null) {
            onTriggeredHandler.call(Event.this, from, to, context);
        }
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Event other = (Event) obj;
        if (id == null) {
            return other.id == null;
        } else
            return id.equals(other.id);
    }
}
