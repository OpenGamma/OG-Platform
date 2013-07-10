package com.opengamma.util.test.lightweight;

import java.net.URI;
import java.util.Map;

import com.google.inject.Injector;
import com.google.inject.Scope;
import com.google.inject.servlet.ServletScopes;
import com.opengamma.transport.jaxrs.FudgeObjectBinaryConsumer;
import com.opengamma.transport.jaxrs.FudgeObjectBinaryProducer;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.guice.spi.container.GuiceComponentProviderFactory;
import com.sun.jersey.spi.container.WebApplication;
import com.sun.jersey.spi.container.WebApplicationFactory;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.LowLevelAppDescriptor;
import com.sun.jersey.test.framework.impl.container.inmemory.TestResourceClientHandler;
import com.sun.jersey.test.framework.spi.container.TestContainer;
import com.sun.jersey.test.framework.spi.container.TestContainerFactory;

/**
 * @author Rune Peter Bjrønstad <runepeter@gmail.com>
 * @see https://github.com/runepeter/jersey-guice-test-framework#readme
 */
public class GuiceInMemoryTestContainerFactory implements TestContainerFactory {
  private final Injector injector;

  public GuiceInMemoryTestContainerFactory(final Injector injector) {
    this.injector = injector;
  }

  public Class<LowLevelAppDescriptor> supports() {
    return LowLevelAppDescriptor.class;
  }

  public TestContainer create(URI uri, AppDescriptor descriptor) throws IllegalArgumentException {
    return new GuiceInMemoryTestContainer(uri, (LowLevelAppDescriptor) descriptor, injector);
  }

  private static class GuiceInMemoryTestContainer implements TestContainer {

    private final URI baseUri;
    private final ResourceConfig config;
    private final WebApplication application;
    private final Injector injector;

    public GuiceInMemoryTestContainer(final URI baseUri, final LowLevelAppDescriptor descriptor, final Injector injector) {
      this.baseUri = baseUri;
      this.config = descriptor.getResourceConfig();
      this.application = WebApplicationFactory.createWebApplication();
      this.injector = injector;
    }

    public Client getClient() {
      //return new Client(new TestResourceClientHandler(baseUri, application));
      ClientConfig config = new DefaultClientConfig(FudgeObjectBinaryConsumer.class, FudgeObjectBinaryProducer.class);
      Client client = new Client(new TestResourceClientHandler(baseUri, application), config);
      return client;
    }

    public URI getBaseUri() {
      return baseUri;
    }

    public void start() {
      if (!application.isInitiated()) {
        application.initiate(config, new GuiceComponentProviderFactory(config, injector) {
          @Override
          public Map<Scope, ComponentScope> createScopeMap() {
            Map<Scope, ComponentScope> m = super.createScopeMap();

            m.put(ServletScopes.REQUEST, ComponentScope.PerRequest);
            return m;
          }
        });
      }
    }

    public void stop() {
      if (application.isInitiated()) {
        application.destroy();
      }
    }

  }

}
