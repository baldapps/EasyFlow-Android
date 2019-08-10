package com.apipas.easyflow.android;


import android.os.Bundle;
import android.util.Log;

import com.apipas.easyflow.android.call.ContextHandler;
import com.apipas.easyflow.android.call.DefaultErrorHandler;
import com.apipas.easyflow.android.call.EventHandler;
import com.apipas.easyflow.android.call.ExecutionErrorHandler;
import com.apipas.easyflow.android.call.StateHandler;
import com.apipas.easyflow.android.err.DefinitionError;
import com.apipas.easyflow.android.err.ExecutionError;

import java.util.concurrent.Executor;

import androidx.annotation.NonNull;


@SuppressWarnings({"UnusedReturnValue", "unused"})
public class EasyFlow {
    private static final String TAG = EasyFlow.class.getSimpleName();
    protected State startState;
    private FlowContext context;
    private Executor executor;
    private boolean validated;

    private StateHandler onStateEnterHandler;
    private StateHandler onStateLeaveHandler;
    private StateHandler onFinalStateHandler;
    private EventHandler onEventTriggeredHandler;
    private ContextHandler onTerminateHandler;
    private ExecutionErrorHandler onError;
    private static final String KEY_CONTEXT = "easyflow.context";

    protected EasyFlow(State startState, TransitionBuilder... transitions) {
        this.startState = startState;
        this.validated = false;
        for (TransitionBuilder transition : transitions) {
            startState.addEvent(transition.getEvent(), transition.getStateTo());
        }
    }

    private void prepare() {
        startState.setFlowRunner(this);
        if (executor == null) {
            executor = new UiThreadExecutor();
        }

        if (onError == null) {
            onError = new DefaultErrorHandler();
        }
    }

    public EasyFlow validate() throws DefinitionError {
        if (!validated) {
            prepare();

            LogicValidator validator = new LogicValidator(startState);
            validator.validate();
            validated = true;
        }

        return this;
    }

    public void start() throws DefinitionError {
        validate();
        if (this.context == null)
            this.context = new FlowContext();

        if (context.getState() == null) {
            setCurrentState(startState, context);
        }
    }

    public void start(final FlowContext context) throws DefinitionError {
        validate();
        this.context = context;

        if (context.getState() == null) {
            setCurrentState(startState, context);
        }
    }

    protected void setCurrentState(final State state, final FlowContext context) {
        execute(() -> {
            if (BuildConfig.DEBUG)
                Log.d(TAG, String.format("setting current state to %s for %s <<<", state, context));

            State prevState = context.getState();
            if (prevState != null) {
                prevState.leave(context);
            }

            context.setState(state);
            context.getState().enter(context);

            if (BuildConfig.DEBUG)
                Log.d(TAG, String.format("setting current state to %s for %s >>>", state, context));
        });
    }

    public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        context = savedInstanceState.getParcelable(KEY_CONTEXT);
        if (context != null) {
            StateFinder stateFinder = new StateFinder(startState, context.getStateId());
            State s = stateFinder.find();
            context.setState(s);
        }
    }

    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelable(KEY_CONTEXT, context);
    }

    protected void execute(Runnable task) {
        executor.execute(task);
    }

    public FlowContext getContext() {
        return context;
    }

    public EasyFlow whenEventTriggered(EventHandler onEventTriggered) {
        this.onEventTriggeredHandler = onEventTriggered;
        return this;
    }

    protected void callOnEventTriggered(Event event, State from, State to, FlowContext context) {
        if (onEventTriggeredHandler != null) {
            try {
                if (BuildConfig.DEBUG)
                    Log.d(TAG, String.format("when triggered %s in %s for %s <<<", event, from, context));

                onEventTriggeredHandler.call(event, from, to, context);

                if (BuildConfig.DEBUG)
                    Log.d(TAG, String.format("when triggered %s in %s for %s >>>", event, from, context));
            } catch (Exception e) {
                callOnError(new ExecutionError(from, event, e, "Execution Error in [EasyFlow.whenEventTriggered] " +
                        "handler", context));
            }
        }
    }

    public EasyFlow whenStateEnter(StateHandler onStateEnter) {
        this.onStateEnterHandler = onStateEnter;
        return this;
    }

    protected void callOnStateEnter(final State state, final FlowContext context) {
        if (onStateEnterHandler != null) {
            try {
                if (BuildConfig.DEBUG)
                    Log.d(TAG, String.format("when enter state %s for %s <<<", state, context));

                onStateEnterHandler.call(state, context);

                if (BuildConfig.DEBUG)
                    Log.d(TAG, String.format("when enter state %s for %s >>>", state, context));
            } catch (Exception e) {
                callOnError(new ExecutionError(state, null, e, "Execution Error in [EasyFlow.whenStateEnter] handler"
                        , context));
            }
        }
    }

    public EasyFlow whenStateLeave(StateHandler onStateLeave) {
        this.onStateLeaveHandler = onStateLeave;
        return this;
    }

    protected void callOnStateLeave(final State state, final FlowContext context) {
        if (onStateLeaveHandler != null) {
            try {
                if (BuildConfig.DEBUG)
                    Log.d(TAG, String.format("when leave state %s for %s <<<", state, context));

                onStateLeaveHandler.call(state, context);

                if (BuildConfig.DEBUG)
                    Log.d(TAG, String.format("when leave state %s for %s >>>", state, context));
            } catch (Exception e) {
                callOnError(new ExecutionError(state, null, e, "Execution Error in [EasyFlow.whenStateLeave] handler"
                        , context));
            }
        }
    }

    public EasyFlow whenFinalState(StateHandler onFinalState) {
        this.onFinalStateHandler = onFinalState;
        return this;
    }

    protected void callOnFinalState(final State state, final FlowContext context) {
        try {
            if (onFinalStateHandler != null) {
                if (BuildConfig.DEBUG)
                    Log.d(TAG, String.format("when final state %s for %s <<<", state, context));

                onFinalStateHandler.call(state, context);

                if (BuildConfig.DEBUG)
                    Log.d(TAG, String.format("when final state %s for %s >>>", state, context));
            }

            callOnTerminate(context);
        } catch (Exception e) {
            callOnError(new ExecutionError(state, null, e, "Execution Error in [EasyFlow.whenFinalState] handler",
                    context));
        }
    }

    public EasyFlow whenError(ExecutionErrorHandler onError) {
        this.onError = onError;
        return this;
    }

    public EasyFlow whenTerminate(ContextHandler onTerminateHandler) {
        this.onTerminateHandler = onTerminateHandler;
        return this;
    }

    public void waitForCompletion() throws InterruptedException {
        context.awaitTermination();
    }

    public EasyFlow executor(Executor executor) {
        this.executor = executor;
        return this;
    }

    protected void callOnError(final ExecutionError error) {
        if (onError != null) {
            onError.call(error);
        }
        callOnTerminate(error.getContext());
    }

    protected void callOnTerminate(final FlowContext context) {
        if (!context.isTerminated()) {
            try {
                if (BuildConfig.DEBUG)
                    Log.d(TAG, String.format("terminating context %s", context));

                context.setTerminated();
                if (onTerminateHandler != null) {
                    if (BuildConfig.DEBUG)
                        Log.d(TAG, String.format("when terminate for %s <<<", context));

                    onTerminateHandler.call(context);

                    if (BuildConfig.DEBUG)
                        Log.d(TAG, String.format("when terminate for %s >>>", context));
                }
            } catch (Exception e) {
                Log.e(TAG, "Execution Error in [EasyFlow.whenTerminate] handler", e);
            }
        }
    }
}
