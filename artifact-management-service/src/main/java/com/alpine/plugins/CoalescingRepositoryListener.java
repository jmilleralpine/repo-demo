package com.alpine.plugins;

import org.eclipse.aether.RepositoryEvent;
import org.eclipse.aether.RepositoryListener;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

class CoalescingRepositoryListener implements RepositoryListener {

    private final Set<RepositoryListener> listeners =
        Collections.newSetFromMap(new ConcurrentHashMap<RepositoryListener, Boolean>());

    void add(RepositoryListener listener) {
        listeners.add(listener);
    }

    void remove(RepositoryListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void artifactDescriptorInvalid(RepositoryEvent event) {
        for (RepositoryListener listener : listeners) {
            listener.artifactDescriptorInvalid(event);
        }
    }

    @Override
    public void artifactDescriptorMissing(RepositoryEvent event) {
        for (RepositoryListener listener : listeners) {
            listener.artifactDescriptorMissing(event);
        }
    }

    @Override
    public void metadataInvalid(RepositoryEvent event) {
        for (RepositoryListener listener : listeners) {
            listener.metadataInvalid(event);
        }
    }

    @Override
    public void artifactResolving(RepositoryEvent event) {
        for (RepositoryListener listener : listeners) {
            listener.artifactResolving(event);
        }
    }

    @Override
    public void artifactResolved(RepositoryEvent event) {
        for (RepositoryListener listener : listeners) {
            listener.artifactResolved(event);
        }
    }

    @Override
    public void metadataResolving(RepositoryEvent event) {
        for (RepositoryListener listener : listeners) {
            listener.metadataResolving(event);
        }
    }

    @Override
    public void metadataResolved(RepositoryEvent event) {
        for (RepositoryListener listener : listeners) {
            listener.metadataResolved(event);
        }
    }

    @Override
    public void artifactDownloading(RepositoryEvent event) {
        for (RepositoryListener listener : listeners) {
            listener.artifactDownloading(event);
        }
    }

    @Override
    public void artifactDownloaded(RepositoryEvent event) {
        for (RepositoryListener listener : listeners) {
            listener.artifactDownloaded(event);
        }
    }

    @Override
    public void metadataDownloading(RepositoryEvent event) {
        for (RepositoryListener listener : listeners) {
            listener.metadataDownloading(event);
        }
    }

    @Override
    public void metadataDownloaded(RepositoryEvent event) {
        for (RepositoryListener listener : listeners) {
            listener.metadataDownloaded(event);
        }
    }

    @Override
    public void artifactInstalling(RepositoryEvent event) {
        for (RepositoryListener listener : listeners) {
            listener.artifactInstalling(event);
        }
    }

    @Override
    public void artifactInstalled(RepositoryEvent event) {
        for (RepositoryListener listener : listeners) {
            listener.artifactInstalled(event);
        }
    }

    @Override
    public void metadataInstalling(RepositoryEvent event) {
        for (RepositoryListener listener : listeners) {
            listener.metadataInstalling(event);
        }
    }

    @Override
    public void metadataInstalled(RepositoryEvent event) {
        for (RepositoryListener listener : listeners) {
            listener.metadataInstalled(event);
        }
    }

    @Override
    public void artifactDeploying(RepositoryEvent event) {
        for (RepositoryListener listener : listeners) {
            listener.artifactDeploying(event);
        }
    }

    @Override
    public void artifactDeployed(RepositoryEvent event) {
        for (RepositoryListener listener : listeners) {
            listener.artifactDeployed(event);
        }
    }

    @Override
    public void metadataDeploying(RepositoryEvent event) {
        for (RepositoryListener listener : listeners) {
            listener.metadataDeploying(event);
        }
    }

    @Override
    public void metadataDeployed(RepositoryEvent event) {
        for (RepositoryListener listener : listeners) {
            listener.metadataDeployed(event);
        }
    }
}
