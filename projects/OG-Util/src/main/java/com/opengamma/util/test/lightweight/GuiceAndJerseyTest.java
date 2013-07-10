package com.opengamma.util.test.lightweight;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.LowLevelAppDescriptor;

public abstract class GuiceAndJerseyTest extends JerseyTest {
  @Override
  protected AppDescriptor configure() {
    Injector injector = Guice.createInjector(getModule());
    injector.injectMembers(this);

    setTestContainerFactory(new GuiceInMemoryTestContainerFactory(injector));

    return new LowLevelAppDescriptor.Builder("ignore").contextPath("rest").build();
  }

  abstract protected AbstractModule getModule();
}
