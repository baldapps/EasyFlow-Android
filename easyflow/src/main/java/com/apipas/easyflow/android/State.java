package com.apipas.easyflow.android;

import android.util.Log;

import com.apipas.easyflow.android.call.StateHandler;
import com.apipas.easyflow.android.err.ExecutionError;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"UnusedReturnValue", "unused"})
public class State {
    private static final String TAG = State.class.getSimpleName();

    private boolean isFinal = false;
    private EasyFlow runner;
    private String id;
    private Map<Event, State> transitions = new HashMap<>();
    private StateHandler onEnterHandler;
    private StateHandler onLeaveHandler;

    State(String id) {
        this.id = id;
    }

    public State whenEnter(StateHandler onEnterListener) {
        this.onEnterHandler = onEnterListener;
        return this;
    }

    public String getId() {
        return id;
    }

    public State whenLeave(StateHandler onLeaveListener) {
        this.onLeaveHandler = onLeaveListener;
        return this;
    }

    public boolean hasEvent(Event event) {
        return transitions.containsKey(event);
    }

    public boolean hasTransitionTo(State state) {
        return transitions.containsValue(state);
    }

    @Override
    public String toString() {
        return id;
    }

    protected void addEvent(Event event, State stateTo) {
        if (BuildConfig.DEBUG)
            Log.d("Tag", "add transition: {" + id + "} --- {" + event + "} --> {" + stateTo + "}");
        transitions.put(event, stateTo);
    }

    protected void enter(final FlowContext context) {
        if (context.isTerminated()) {
            return;
        }

        try {
            // first enter state
            runner.callOnStateEnter(State.this, context);

            if (onEnterHandler != null) {
                if (BuildConfig.DEBUG)
                    Log.d(TAG, String.format("when enter %s for %s <<<", State.this, context));

                onEnterHandler.call(State.this, context);

                if (BuildConfig.DEBUG)
                    Log.d(TAG, String.format("when enter %s for %s >>>", State.this, context));
            }

            if (isFinal) {
                runner.callOnFinalState(State.this, context);
            }
        } catch (Exception e) {
            runner.callOnError(new ExecutionError(State.this, null, e, "Execution Error in [State.whenEnter] handler"
                    , context));
        }
    }

    protected void leave(final FlowContext context) {
        if (context.isTerminated()) {
            return;
        }

        // then leave the state
        if (onLeaveHandler != null) {
            try {
                if (BuildConfig.DEBUG)
                    Log.d(TAG, String.format("when leave %s for %s <<<", State.this, context));

                onLeaveHandler.call(State.this, context);

                if (BuildConfig.DEBUG)
                    Log.d(TAG, String.format("when leave %s for %s >>>", State.this, context));
            } catch (Exception e) {
                runner.callOnError(new ExecutionError(State.this, null, e, "Execution Error in [State.whenEnter] " +
                        "handler", context));
            }
        }
        runner.callOnStateLeave(this, context);
    }

    protected Map<Event, State> getTransitions() {
        return transitions;
    }

    public boolean isFinal() {
        return isFinal;
    }

    protected void setFinal(boolean isFinal) {
        this.isFinal = isFinal;
    }

    protected void setFlowRunner(EasyFlow runner) {
        this.runner = runner;

        for (Event event : transitions.keySet()) {
            event.setFlowRunner(runner);
        }

        for (State nextState : transitions.values()) {
            if (nextState.runner == null) {
                nextState.setFlowRunner(runner);
            }
        }
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
        State other = (State) obj;
        if (id == null) {
            return other.id == null;
        } else
            return id.equals(other.id);
    }
}
