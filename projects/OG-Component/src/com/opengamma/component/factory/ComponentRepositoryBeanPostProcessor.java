/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component.factory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import com.opengamma.component.ComponentRepository;

/**
 * Spring bean factory post processor that exposes the component repository.
 */
public class ComponentRepositoryBeanPostProcessor implements BeanFactoryPostProcessor {

  //-------------------------------------------------------------------------
  @Override
  public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    ComponentRepository repo = ComponentRepository.getThreadLocal();
    SpringComponentUtils.publishComponentRepository(repo, beanFactory);
  }

}
