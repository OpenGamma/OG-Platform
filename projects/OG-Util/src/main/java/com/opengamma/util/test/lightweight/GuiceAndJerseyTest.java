/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.test.lightweight;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.LowLevelAppDescriptor;

/**
 * Framework for testing RESTful resources
 */
public abstract class GuiceAndJerseyTest extends JerseyTest {
  @Override
  protected AppDescriptor configure() {
    Injector injector = Guice.createInjector(getModule());
    injector.injectMembers(this);

    setTestContainerFactory(new GuiceInMemoryTestContainerFactory(injector));

    return new LowLevelAppDescriptor.Builder("ignore").contextPath("rest").build();
  }

  protected abstract AbstractModule getModule();
}
