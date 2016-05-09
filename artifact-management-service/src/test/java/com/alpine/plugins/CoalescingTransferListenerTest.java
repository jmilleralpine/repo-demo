package com.alpine.plugins;

import org.eclipse.aether.transfer.TransferCancelledException;
import org.eclipse.aether.transfer.TransferEvent;
import org.eclipse.aether.transfer.TransferListener;
import org.junit.Test;
import org.objenesis.ObjenesisStd;

import java.util.HashSet;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class CoalescingTransferListenerTest {

    // helpful!
    private TransferEvent testEvent =
        new ObjenesisStd().getInstantiatorOf(TransferEvent.class).newInstance();

    private HashSet<String> called = new HashSet<>();

    private TransferListener prover = new TransferListener() {
        @Override
        public void transferInitiated(TransferEvent event) throws TransferCancelledException {
            assert(event == testEvent);
            called.add("transferInitiated");
        }

        @Override
        public void transferStarted(TransferEvent event) throws TransferCancelledException {
            assert(event == testEvent);
            called.add("transferStarted");
        }

        @Override
        public void transferProgressed(TransferEvent event) throws TransferCancelledException {
            assert(event == testEvent);
            called.add("transferProgressed");
        }

        @Override
        public void transferCorrupted(TransferEvent event) throws TransferCancelledException {
            assert(event == testEvent);
            called.add("transferCorrupted");
        }

        @Override
        public void transferSucceeded(TransferEvent event) {
            assert(event == testEvent);
            called.add("transferSucceeded");
        }

        @Override
        public void transferFailed(TransferEvent event) {
            assert(event == testEvent);
            called.add("transferFailed");
        }
    };

    private CoalescingTransferListener l = new CoalescingTransferListener();

    private static final int expectedCount = 6;

    private void doAllCalls() throws Exception {

        l.transferInitiated(testEvent);
        l.transferCorrupted(testEvent);
        l.transferFailed(testEvent);
        l.transferProgressed(testEvent);
        l.transferStarted(testEvent);
        l.transferSucceeded(testEvent);
    }

    @Test
    public void test() throws Exception {

        l.add(prover);

        doAllCalls();

        assertThat(called, hasSize(expectedCount));

        l.remove(prover);
        called.clear();

        doAllCalls();

        assertThat(called, is(empty()));

        l.add(prover);

        doAllCalls();

        assertThat(called, hasSize(expectedCount));
    }
}