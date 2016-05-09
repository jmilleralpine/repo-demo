package com.alpine.plugins;

import org.eclipse.aether.RepositoryEvent;
import org.eclipse.aether.RepositoryListener;
import org.junit.Test;
import org.objenesis.ObjenesisStd;

import java.util.HashSet;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class CoalescingRepositoryListenerTest {

    // helpful!
    private RepositoryEvent testEvent =
        new ObjenesisStd().getInstantiatorOf(RepositoryEvent.class).newInstance();

    private HashSet<String> called = new HashSet<>();

    private RepositoryListener prover = new RepositoryListener() {

        @Override
        public void artifactDescriptorInvalid(RepositoryEvent event) {
            assertTrue(event == testEvent);
            called.add("artifactDescriptorInvalid");
        }

        @Override
        public void artifactDescriptorMissing(RepositoryEvent event) {
            assertTrue(event == testEvent);
            called.add("artifactDescriptorMissing");
        }

        @Override
        public void artifactDeploying(RepositoryEvent event) {
            assertTrue(event == testEvent);
            called.add("artifactDeploying");
        }

        @Override
        public void artifactDeployed(RepositoryEvent event) {
            assertTrue(event == testEvent);
            called.add("artifactDeployed");
        }

        @Override
        public void artifactDownloading(RepositoryEvent event) {
            assertTrue(event == testEvent);
            called.add("artifactDownloading");
        }

        @Override
        public void artifactDownloaded(RepositoryEvent event) {
            assertTrue(event == testEvent);
            called.add("artifactDownloaded");
        }

        @Override
        public void artifactInstalling(RepositoryEvent event) {
            assertTrue(event == testEvent);
            called.add("artifactInstalling");
        }

        @Override
        public void artifactInstalled(RepositoryEvent event) {
            assertTrue(event == testEvent);
            called.add("artifactInstalled");
        }

        @Override
        public void artifactResolving(RepositoryEvent event) {
            assertTrue(event == testEvent);
            called.add("artifactResolving");
        }

        @Override
        public void artifactResolved(RepositoryEvent event) {
            assertTrue(event == testEvent);
            called.add("artifactResolved");
        }

        @Override
        public void metadataDeploying(RepositoryEvent event) {
            assertTrue(event == testEvent);
            called.add("metadataDeploying");
        }

        @Override
        public void metadataDeployed(RepositoryEvent event) {
            assertTrue(event == testEvent);
            called.add("metadataDeployed");
        }

        @Override
        public void metadataDownloading(RepositoryEvent event) {
            assertTrue(event == testEvent);
            called.add("metadataDownloading");
        }

        @Override
        public void metadataDownloaded(RepositoryEvent event) {
            assertTrue(event == testEvent);
            called.add("metadataDownloaded");
        }

        @Override
        public void metadataInstalling(RepositoryEvent event) {
            assertTrue(event == testEvent);
            called.add("metadataInstalling");
        }

        @Override
        public void metadataInstalled(RepositoryEvent event) {
            assertTrue(event == testEvent);
            called.add("metadataInstalled");
        }

        @Override
        public void metadataInvalid(RepositoryEvent event) {
            assertTrue(event == testEvent);
            called.add("metadataInvalid");
        }

        @Override
        public void metadataResolving(RepositoryEvent event) {
            assertTrue(event == testEvent);
            called.add("metadataResolving");
        }

        @Override
        public void metadataResolved(RepositoryEvent event) {
            assertTrue(event == testEvent);
            called.add("metadataResolved");
        }
    };

    private CoalescingRepositoryListener l = new CoalescingRepositoryListener();

    private static final int expectedCount = 19;

    private void doAllCalls() {

        l.artifactDescriptorInvalid(testEvent);
        l.artifactDescriptorMissing(testEvent);
        l.artifactDeployed(testEvent);
        l.artifactDeploying(testEvent);
        l.artifactDownloaded(testEvent);
        l.artifactDownloading(testEvent);
        l.artifactInstalled(testEvent);
        l.artifactInstalling(testEvent);
        l.artifactResolved(testEvent);
        l.artifactResolving(testEvent);

        l.metadataDeployed(testEvent);
        l.metadataDeploying(testEvent);
        l.metadataDownloaded(testEvent);
        l.metadataDownloading(testEvent);
        l.metadataInstalled(testEvent);
        l.metadataInstalling(testEvent);
        l.metadataInvalid(testEvent);
        l.metadataResolved(testEvent);
        l.metadataResolving(testEvent);
    }

    @Test
    public void test() {

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