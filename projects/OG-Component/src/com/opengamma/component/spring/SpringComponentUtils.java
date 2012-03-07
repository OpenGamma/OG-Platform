/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component.spring;

import java.util.Map.Entry;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import com.opengamma.component.ComponentKey;
import com.opengamma.component.ComponentRepository;

/**
 * Utilities integrating Spring and OpenGamma components.
 */
public class SpringComponentUtils {

  /**
   * Restricted constructor.
   */
  public SpringComponentUtils() {
  }

  //-------------------------------------------------------------------------
  /**
   * Publish the component repository to Spring.
   * 
   * @param repo  the repository, not null
   * @param beanFactory  the bean factory, not null
   */
  public static void publishComponentRepository(ComponentRepository repo, ConfigurableListableBeanFactory beanFactory) {
    for (Entry<ComponentKey, Object> entry : repo.getInstanceMap().entrySet()) {
      ComponentKey key = entry.getKey();
      beanFactory.registerSingleton(toSpringName(key), entry.getValue());
      
      // handle names that came from Spring and don't match standard pattern
      if (beanFactory.containsBean(key.getClassifier()) == false) {
        beanFactory.registerSingleton(key.getClassifier(), entry.getValue());
      }
    }
  }

  /**
   * Gets the equivalent name in Spring.
   * 
   * @param key  the component key, not null
   * @return the name in Spring, not null
   */
  public static String toSpringName(ComponentKey key) {
    return key.getClassifier() + key.getType().getSimpleName();
  }

}
