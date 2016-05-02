package com.alpine.plugins;

import org.eclipse.aether.transfer.TransferCancelledException;
import org.eclipse.aether.transfer.TransferEvent;
import org.eclipse.aether.transfer.TransferListener;

/**
 * ehhhhhh
 */
class LoggingTransferListener implements TransferListener {
    @Override
    public void transferInitiated(TransferEvent event) throws TransferCancelledException {
        System.out.println(event);
    }

    @Override
    public void transferStarted(TransferEvent event) throws TransferCancelledException {
        System.out.println(event);
    }

    @Override
    public void transferProgressed(TransferEvent event) throws TransferCancelledException {
        System.out.println(event);
        System.out.println(event.getTransferredBytes());
    }

    @Override
    public void transferCorrupted(TransferEvent event) throws TransferCancelledException {
        System.out.println(event);
    }

    @Override
    public void transferSucceeded(TransferEvent event) {
        System.out.println(event);
    }

    @Override
    public void transferFailed(TransferEvent event) {
        System.out.println(event);
    }
}