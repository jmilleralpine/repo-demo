package com.alpine.plugins;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;

import javax.inject.Singleton;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Main {

    private static final Path LOCAL_REPO = Paths.get(System.getProperty("user.home")).resolve("ALPINE_TEST_REPO").toAbsolutePath();

    private static final RepositorySystemConfiguration configuration =
        new RepositorySystemConfiguration(
            LOCAL_REPO,
            false,
            Collections.<Repo>emptyList(),
            new RepositorySystemConfiguration.Timeout(5, TimeUnit.SECONDS),
            new RepositorySystemConfiguration.Timeout(10, TimeUnit.SECONDS)
        );

    public static void main(String[] args) throws Exception {

        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(
                URI.create("http://localhost:9090/"),
                new ResourceConfig()
                    // resources
                    .register(RepositoryResource.class)
                    // injections
                    .register(new AbstractBinder() {
                        @Override
                        protected void configure() {

                            bind(PathParser.class).to(PathParser.class)
                                .in(Singleton.class);

                            bind(RepositorySystemArtifactManagementService.class).to(ArtifactManagementService.class)
                                .in(Singleton.class);

                            bind(configuration).to(RepositorySystemConfiguration.class);
                        }
                    })
                    // logging errors
                    .register(new ApplicationEventListener() {
                        @Override
                        public void onEvent(ApplicationEvent event) {
                            // ehhhhh
                        }

                        @Override
                        public RequestEventListener onRequest(RequestEvent requestEvent) {
                            return new RequestEventListener() {
                                @Override
                                public void onEvent(RequestEvent event) {
                                    switch(event.getType()) {
                                        case ON_EXCEPTION:
                                            event.getException().printStackTrace();
                                            break;

                                        case RESOURCE_METHOD_START:
                                            // logging it all!
                                            System.out.println(event.getContainerRequest().getMethod() + ": " + event.getContainerRequest().getPath(true));
                                            for (Map.Entry<String, List<String>> headers :
                                                event.getContainerRequest().getHeaders().entrySet()) {

                                                for (String value : headers.getValue()) {
                                                    System.out.println(headers.getKey() + " = " + value);
                                                }
                                            }
                                            break;

                                        case FINISHED:
                                            System.out.println(event.getContainerResponse().getStatusInfo());
                                            System.out.println();
                                            break;
                                    }
                                }
                            };
                        }
                    })
        );

        System.out.println("Press any key to quit");
        System.in.read();
        server.stop();
    }
}
