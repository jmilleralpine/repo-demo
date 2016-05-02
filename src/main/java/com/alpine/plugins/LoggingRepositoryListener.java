package com.alpine.plugins;

import org.eclipse.aether.AbstractRepositoryListener;
import org.eclipse.aether.RepositoryEvent;

/**
 * Created by jasonmiller on 4/29/16.
 */
class LoggingRepositoryListener extends AbstractRepositoryListener {


    @Override
    public void metadataResolving(RepositoryEvent event) {
        System.out.println(event);
    }

    @Override
    public void metadataResolved(RepositoryEvent event) {
        System.out.println(event);
    }

    @Override
    public void artifactResolved(RepositoryEvent event) {
        System.out.println(event);
    }

    @Override
    public void artifactResolving(RepositoryEvent event) {
        System.out.println(event);
    }
}
