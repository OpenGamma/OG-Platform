/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.test.lightweight;

import org.testng.annotations.BeforeMethod;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.opengamma.transport.jaxrs.FudgeObjectBinaryConsumer;
import com.opengamma.transport.jaxrs.FudgeObjectBinaryProducer;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.LowLevelAppDescriptor;

/**
 * Framework for testing RESTful resources
 */
public abstract class OpenGammaRestResourceTest extends GuiceAndJerseyTest {

  @Override
  protected AppDescriptor configure() {
    Injector injector = Guice.createInjector(getModule());
    injector.injectMembers(this);
    setTestContainerFactory(new GuiceInMemoryTestContainerFactory(injector));
    LowLevelAppDescriptor.Builder builder = new LowLevelAppDescriptor.Builder(FudgeObjectBinaryConsumer.class, FudgeObjectBinaryProducer.class).contextPath("rest");
    return builder.build();
  }

  @Override
  protected final AbstractModule getModule() {
    return defineProviders();
  }

  protected abstract ProviderModule defineProviders();

  @BeforeMethod
  public void setUp() throws Exception {
    super.setUp();
  }

}
