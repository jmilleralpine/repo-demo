package com.alpine.plugins;

import org.eclipse.aether.transfer.TransferCancelledException;
import org.eclipse.aether.transfer.TransferEvent;
import org.eclipse.aether.transfer.TransferListener;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


class CoalescingTransferListener implements TransferListener {

    private final Set<TransferListener> listeners =
        Collections.newSetFromMap(new ConcurrentHashMap<TransferListener, Boolean>());

    void add(TransferListener listener) {
        listeners.add(listener);
    }

    void remove(TransferListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void transferInitiated(TransferEvent event) throws TransferCancelledException {
        for (TransferListener listener : listeners) {
            listener.transferInitiated(event);
        }
    }

    @Override
    public void transferStarted(TransferEvent event) throws TransferCancelledException {
        for (TransferListener listener : listeners) {
            listener.transferStarted(event);
        }
    }

    @Override
    public void transferProgressed(TransferEvent event) throws TransferCancelledException {
        for (TransferListener listener : listeners) {
            listener.transferProgressed(event);
        }
    }

    @Override
    public void transferCorrupted(TransferEvent event) throws TransferCancelledException {
        for (TransferListener listener : listeners) {
            listener.transferCorrupted(event);
        }
    }

    @Override
    public void transferSucceeded(TransferEvent event) {
        for (TransferListener listener : listeners) {
            listener.transferSucceeded(event);
        }
    }

    @Override
    public void transferFailed(TransferEvent event) {
        for (TransferListener listener : listeners) {
            listener.transferFailed(event);
        }
    }
}
