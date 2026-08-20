package dev.portero.atlas.event;

public interface Cancellable {

    boolean cancelled();

    void cancelled(boolean cancelled);

    default void cancel() {
        this.cancelled(true);
    }
}
