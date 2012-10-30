/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import com.opengamma.component.ComponentRepository;

/**
 * Spring bean factory post processor that exposes the component repository.
 * <p>
 * To use this class, simply declare it as a bean in a Spring XML file.
 * It will be run before resolving other Spring bean references, pulling the
 * components into the Spring context.
 * Components are exposed using the name {@code classifierType}, where the
 * type is the simple name of the registered type.
 */
public class ComponentRepositoryBeanPostProcessor implements BeanFactoryPostProcessor {

  //-------------------------------------------------------------------------
  @Override
  public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    ComponentRepository repo = ComponentRepository.getThreadLocal();
    SpringComponentUtils.publishComponentRepository(repo, beanFactory);
  }

}
