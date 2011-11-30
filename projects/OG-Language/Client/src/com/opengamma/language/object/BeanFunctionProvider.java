/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.object;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.joda.beans.Bean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.language.function.AbstractFunctionProvider;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.util.ArgumentChecker;

/**
 * Provider of functions that create instances of Joda {@link Bean} classes
 * @see CreateBeanFunction
 */
public class BeanFunctionProvider extends AbstractFunctionProvider {

  private static final Logger s_logger = LoggerFactory.getLogger(BeanFunctionProvider.class);

  private final List<CreateBeanFunction> _functions;

  /**
   * Creates a provider that exports a function for each class named in {@code beanClassNames}.  The classes must
   * implement the Joda {@link Bean} interface.
   * @param beanClassNames Fully-qualified class names of Joda {@link Bean} classes for which builder functions
   * will be exported
   */
  @SuppressWarnings("unchecked")
  public BeanFunctionProvider(List<String> beanClassNames) {
    ArgumentChecker.notNull(beanClassNames, "beanClassNames");
    _functions = new ArrayList<CreateBeanFunction>(beanClassNames.size());
    for (String beanClassName : beanClassNames) {
      Class<? extends Bean> beanClass;
      try {
        Class<?> aClass = Class.forName(beanClassName);
        if (!Bean.class.isAssignableFrom(aClass)) {
          s_logger.warn("Class " + aClass.getName() + " doesn't extend Bean, can't create a function to build instances");
          continue;
        }
        beanClass = (Class<? extends Bean>) aClass;
      } catch (ClassNotFoundException e) {
        throw new OpenGammaRuntimeException("Unable to load bean class " + beanClassName, e);
      }
      CreateBeanFunction function = new CreateBeanFunction(beanClass.getSimpleName(), beanClass);
      s_logger.debug("Created function definition: " + function);
      _functions.add(function);
    }
  }

  @Override
  protected void loadDefinitions(Collection<MetaFunction> definitions) {
    for (CreateBeanFunction function : _functions) {
      s_logger.debug("Adding function definition: " + function);
      definitions.add(function.getMetaFunction());
    }
  }
}
