package com.apipas.easyflow.android;


import android.util.Log;

import com.apipas.easyflow.android.call.ContextHandler;
import com.apipas.easyflow.android.call.DefaultErrorHandler;
import com.apipas.easyflow.android.call.EventHandler;
import com.apipas.easyflow.android.call.ExecutionErrorHandler;
import com.apipas.easyflow.android.call.StateHandler;
import com.apipas.easyflow.android.err.ExecutionError;

import java.util.concurrent.Executor;


public class EasyFlow<C extends StatefulContext> {
    private static final String TAG = EasyFlow.class.getSimpleName();
    protected State<C> startState;
    private C context;
    private Executor executor;
    private boolean validated;

    private StateHandler<C> onStateEnterHandler;
    private StateHandler<C> onStateLeaveHandler;
    private StateHandler<C> onFinalStateHandler;
    private EventHandler<C> onEventTriggeredHandler;
    private ContextHandler<C> onTerminateHandler;
    private ExecutionErrorHandler onError;
    private boolean trace = false;

    protected EasyFlow(State<C> startState, TransitionBuilder<C>... transitions) {
        this.startState = startState;
        this.validated = false;
        for (TransitionBuilder<C> transition : transitions) {
            startState.addEvent(transition.getEvent(), transition.getStateTo());
        }
    }

    private void prepare() {
        startState.setFlowRunner(this);
        if (executor == null) {
            executor = new AsyncExecutor();
        }

        if (onError == null) {
            onError = new DefaultErrorHandler();
        }
    }

    public EasyFlow<C> validate() {
        if (!validated) {
            prepare();

            LogicValidator<C> validator = new LogicValidator<C>(startState);
            validator.validate();
            validated = true;
        }

        return this;
    }

    public void start(final C context) {
        validate();
        this.context = context;

        if (context.getState() == null) {
            setCurrentState(startState, context);
        }
    }

    protected void setCurrentState(final State<C> state, final C context) {
        execute(new Runnable() {
            @Override
            public void run() {
                if (isTrace())
                    Log.d(TAG, String.format("setting current state to %s for %s <<<", state, context));

                State<C> prevState = context.getState();
                if (prevState != null) {
                    prevState.leave(context);
                }

                context.setState(state);
                context.getState().enter(context);

                if (isTrace())
                    Log.d(TAG, String.format("setting current state to %s for %s >>>", state, context));
            }
        });
    }

    protected void execute(Runnable task) {
        executor.execute(task);
    }

    public C getContext() {
        return context;
    }

    public EasyFlow<C> whenEventTriggered(EventHandler<C> onEventTriggered) {
        this.onEventTriggeredHandler = onEventTriggered;
        return this;
    }

    protected void callOnEventTriggered(Event<C> event, State<C> from, State<C> to, C context) throws Exception {
        if (onEventTriggeredHandler != null) {
            try {
                if (isTrace())
                    Log.d(TAG, String.format("when triggered %s in %s for %s <<<", event, from, context));

                onEventTriggeredHandler.call(event, from, to, context);

                if (isTrace())
                    Log.d(TAG, String.format("when triggered %s in %s for %s >>>", event, from, context));
            } catch (Exception e) {
                callOnError(new ExecutionError(from, event, e,
                        "Execution Error in [EasyFlow.whenEventTriggered] handler", context));
            }
        }
    }

    public EasyFlow<C> whenStateEnter(StateHandler<C> onStateEnter) {
        this.onStateEnterHandler = onStateEnter;
        return this;
    }

    protected void callOnStateEnter(final State<C> state, final C context) {
        if (onStateEnterHandler != null) {
            try {
                if (isTrace())
                    Log.d(TAG, String.format("when enter state %s for %s <<<", state, context));

                onStateEnterHandler.call(state, context);

                if (isTrace())
                    Log.d(TAG, String.format("when enter state %s for %s >>>", state, context));
            } catch (Exception e) {
                callOnError(new ExecutionError(state, null, e,
                        "Execution Error in [EasyFlow.whenStateEnter] handler", context));
            }
        }
    }

    public EasyFlow<C> whenStateLeave(StateHandler<C> onStateLeave) {
        this.onStateLeaveHandler = onStateLeave;
        return this;
    }

    protected void callOnStateLeave(final State<C> state, final C context) {
        if (onStateLeaveHandler != null) {
            try {
                if (isTrace())
                    Log.d(TAG, String.format("when leave state %s for %s <<<", state, context));

                onStateLeaveHandler.call(state, context);

                if (isTrace())
                    Log.d(TAG, String.format("when leave state %s for %s >>>", state, context));
            } catch (Exception e) {
                callOnError(new ExecutionError(state, null, e,
                        "Execution Error in [EasyFlow.whenStateLeave] handler", context));
            }
        }
    }

    public EasyFlow<C> whenFinalState(StateHandler<C> onFinalState) {
        this.onFinalStateHandler = onFinalState;
        return this;
    }

    protected void callOnFinalState(final State<C> state, final C context) {
        try {
            if (onFinalStateHandler != null) {
                if (isTrace())
                    Log.d(TAG, String.format("when final state %s for %s <<<", state, context));

                onFinalStateHandler.call(state, context);

                if (isTrace())
                    Log.d(TAG, String.format("when final state %s for %s >>>", state, context));
            }

            callOnTerminate(context);
        } catch (Exception e) {
            callOnError(new ExecutionError(state, null, e,
                    "Execution Error in [EasyFlow.whenFinalState] handler", context));
        }
    }

    public EasyFlow<C> whenError(ExecutionErrorHandler onError) {
        this.onError = onError;
        return this;
    }

    public EasyFlow<C> whenTerminate(ContextHandler onTerminateHandler) {
        this.onTerminateHandler = onTerminateHandler;
        return this;
    }

    public void waitForCompletion() {
        waitForCompletion(context);
    }

    public void waitForCompletion(C context) {
        context.awaitTermination();
    }

    public EasyFlow<C> executor(Executor executor) {
        this.executor = executor;
        return this;
    }

    public EasyFlow<C> trace() {
        trace = true;
        return this;
    }

    protected boolean isTrace() {
        return trace;
    }

    protected void callOnError(final ExecutionError error) {
        if (onError != null) {
            onError.call(error);
        }
        callOnTerminate((C) error.getContext());
    }

    protected void callOnTerminate(final C context) {
        if (!context.isTerminated()) {
            try {
                if (isTrace())
                    Log.d(TAG, String.format("terminating context %s", context));

                context.setTerminated();
                if (onTerminateHandler != null) {
                    if (isTrace())
                        Log.d(TAG, String.format("when terminate for %s <<<", context));

                    onTerminateHandler.call(context);

                    if (isTrace())
                        Log.d(TAG, String.format("when terminate for %s >>>", context));
                }
            } catch (Exception e) {
                Log.e(TAG, "Execution Error in [EasyFlow.whenTerminate] handler", e);
            }
        }
    }
}
