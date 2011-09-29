package com.opengamma.language.object;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.language.function.AbstractFunctionProvider;
import com.opengamma.language.function.MetaFunction;
import org.joda.beans.Bean;

import java.util.Collection;

/**
 * TODO provide multiple functions for different bean classes, read properties file(?) from classpath with list of classes
 * that would allow functions to be added for classes that aren't part of the core projects (e.g. client-specific classes)
 */
public class BeanFunctionProvider extends AbstractFunctionProvider {

  private CreateBeanFunction _function;

  // TODO get rid of beanClassName, read class names from resource (or something like that). maybe resourceName param
  @SuppressWarnings({"unchecked"})
  public BeanFunctionProvider(String functionName, String beanClassName) {
    Class<? extends Bean> beanClass;
    try {
      Class<?> aClass = Class.forName(beanClassName);
      // TODO check it's a bean class
      beanClass = (Class<? extends Bean>) aClass;
    } catch (ClassNotFoundException e) {
      throw new OpenGammaRuntimeException("", e);
    }
    _function = new CreateBeanFunction(functionName, beanClass);
  }

  @Override
  protected void loadDefinitions(Collection<MetaFunction> definitions) {
    definitions.add(_function.getMetaFunction());
  }
}
