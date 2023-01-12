package com.udnahc.locationmanager.executors;

public class PriorityRunnable implements Runnable {

    private final Priority priority;

    public PriorityRunnable(Priority priority) {
        this.priority = priority;
    }

    public PriorityRunnable() {
        this.priority = Priority.MEDIUM;
    }

    @Override
    public void run() {
        // nothing to do here.
    }

    public Priority getPriority() {
        return priority;
    }
}
