/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.push;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

/**
 *
 */
public class TestApplication extends Application {

  @Override
  public Set<Class<?>> getClasses() {
    HashSet<Class<?>> classes = new HashSet<Class<?>>();
    classes.add(TestResource.class);
    classes.add(TestSubResources.class);
    return classes;
  }
}
