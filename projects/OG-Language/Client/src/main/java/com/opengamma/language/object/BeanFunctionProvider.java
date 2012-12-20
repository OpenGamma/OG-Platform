/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.object;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.joda.beans.Bean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.function.AbstractFunctionProvider;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;
import com.opengamma.util.ArgumentChecker;

/**
 * Provider of constructor and property accessor/mutator functions for Joda {@link Bean} classes.
 * 
 * @see CreateBeanFunction
 * @see GetBeanPropertyFunction
 */
public class BeanFunctionProvider extends AbstractFunctionProvider {

  private static final Logger s_logger = LoggerFactory.getLogger(BeanFunctionProvider.class);

  private final List<PublishedFunction> _functions;

  /**
   * Creates a provider that exports a function for each class named in {@code beanClassNames}.  The classes must
   * implement the Joda {@link Bean} interface.
   * @param beanClassNames Fully-qualified class names of Joda {@link Bean} classes for which builder functions
   * will be exported
   */

  @SuppressWarnings("unchecked")
  public BeanFunctionProvider(List<String> beanClassNames) {
    ArgumentChecker.notNull(beanClassNames, "beanClassNames");
    _functions = new ArrayList<PublishedFunction>();
    for (String beanClassName : beanClassNames) {
      try {
        Class<?> clazz = Class.forName(beanClassName);
        if (!Bean.class.isAssignableFrom(clazz)) {
          s_logger.warn("Class " + clazz.getName() + " doesn't extend Bean. Cannot create a function to build instances of this class.");
          continue;
        }
        Class<? extends Bean> beanClass = (Class<? extends Bean>) clazz;
        _functions.addAll(createBeanFunctions(beanClass));
      } catch (ClassNotFoundException e) {
        throw new OpenGammaRuntimeException("Unable to create functions for class " + beanClassName, e);
      }
    }
  }
  
  private <T extends Bean> Collection<PublishedFunction> createBeanFunctions(Class<T> beanClass) {
    final Collection<PublishedFunction> functions = new ArrayList<PublishedFunction>();
    final String simpleBeanName = beanClass.getSimpleName();
    functions.add(new CreateBeanFunction(simpleBeanName, beanClass));
    final MetaBean metaBean = JodaBeanUtils.metaBean(beanClass);
    final MetaParameter beanParameter = new MetaParameter(StringUtils.uncapitalize(simpleBeanName), JavaTypeInfo.builder(beanClass).get());
    beanParameter.setDescription("is the " + simpleBeanName + " to query");
    for (MetaProperty<?> property : metaBean.metaPropertyIterable()) {
      final String accessorFunctionName = "Get" + simpleBeanName + StringUtils.capitalize(property.name());
      functions.add(new GetBeanPropertyFunction(accessorFunctionName, property, beanParameter));
    }
    s_logger.debug("Created function definitions: " + functions);
    return functions;
  }

  @Override
  protected void loadDefinitions(Collection<MetaFunction> definitions) {
    for (PublishedFunction function : _functions) {
      s_logger.debug("Adding function definition: " + function);
      definitions.add(function.getMetaFunction());
    }
  }
}
