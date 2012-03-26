/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.bloombergexample.component;

import java.util.LinkedHashMap;

import org.springframework.context.support.GenericApplicationContext;

import com.opengamma.component.ComponentRepository;
import com.opengamma.component.factory.AbstractSpringComponentFactory;

/**
 * 
 */
public class ExampleBloombergDataServerComponentFactory extends AbstractSpringComponentFactory {
  
  @Override
  public void init(ComponentRepository repo, LinkedHashMap<String, String> configuration) throws Exception {
    GenericApplicationContext appContext = createApplicationContext(repo);
    repo.registerLifecycle(appContext);
  }

}
