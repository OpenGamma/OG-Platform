/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.examples.bloomberg.component;

import java.util.LinkedHashMap;

import org.springframework.context.support.GenericApplicationContext;

import com.opengamma.component.ComponentRepository;
import com.opengamma.component.factory.AbstractSpringComponentFactory;

/**
 * Spring-based data server.
 */
public class ExampleBloombergSpringDataServerComponentFactory extends AbstractSpringComponentFactory {

  @Override
  public void init(ComponentRepository repo, LinkedHashMap<String, String> configuration) throws Exception {
    GenericApplicationContext appContext = createApplicationContext(repo);
    repo.registerLifecycle(appContext);
  }

}
