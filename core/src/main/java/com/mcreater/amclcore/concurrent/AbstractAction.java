package com.mcreater.amclcore.concurrent;

public abstract class AbstractAction extends AbstractTask<Void> {
    protected abstract void execute() throws Exception;

    protected final Void call() throws Exception {
        execute();
        return null;
    }
}
