package com.apipas.easyflow.android;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import androidx.annotation.NonNull;

@SuppressWarnings("unused")
public class FlowContext implements Parcelable {
    private final String id;
    private State state;
    private String stateId;
    private final AtomicBoolean terminated = new AtomicBoolean(false);
    private final CountDownLatch completionLatch = new CountDownLatch(1);

    public FlowContext() {
        id = UUID.randomUUID() + ":" + getClass().getSimpleName();
    }

    public FlowContext(@NonNull String aId) {
        id = aId + ":" + getClass().getSimpleName();
    }

    protected FlowContext(Parcel in) {
        id = in.readString();
        stateId = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(stateId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<FlowContext> CREATOR = new Creator<FlowContext>() {
        @Override
        public FlowContext createFromParcel(Parcel in) {
            return new FlowContext(in);
        }

        @Override
        public FlowContext[] newArray(int size) {
            return new FlowContext[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setState(State state) {
        this.state = state;
        this.stateId = state.getId();
    }

    public State getState() {
        return state;
    }

    public String getStateId() {
        return stateId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        FlowContext that = (FlowContext) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public boolean isTerminated() {
        return terminated.get();
    }

    public boolean isRunning() {
        return isStarted() && !terminated.get();
    }

    public boolean isStarted() {
        return state != null;
    }

    protected void setTerminated() {
        this.terminated.set(true);
        this.completionLatch.countDown();
    }

    /**
     * Block current thread until Context terminated
     */
    protected void awaitTermination() throws InterruptedException {
        this.completionLatch.await();
    }

    @Override
    public String toString() {
        return id;
    }

    public static <T extends FlowContext> T cast(FlowContext c, Class<T> k) {
        return k.cast(c);
    }
}
