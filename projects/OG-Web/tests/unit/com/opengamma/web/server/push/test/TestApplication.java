/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.test;

import com.opengamma.web.server.push.subscription.ViewportDefinitionReader;
import com.opengamma.web.server.push.web.ViewportsResource;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

/**
 *
 */
public class TestApplication extends Application {

  @Override
  public Set<Class<?>> getClasses() {
    HashSet<Class<?>> classes = new HashSet<Class<?>>();
    classes.add(TestResource.class);
    classes.add(TestSubResources.class);
    //classes.add(ViewportsResource.class);
    classes.add(ViewportDefinitionReader.class);
    return classes;
  }
}
