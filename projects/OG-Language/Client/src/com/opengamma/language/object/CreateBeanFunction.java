/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.object;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;

import com.opengamma.language.Data;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;
import com.opengamma.util.ArgumentChecker;

/**
 * A function that can create instances of a Joda {@link Bean} class.  The function's parameters are derived from
 * the bean's writable properties in the order they appear in its {@link Bean#metaBean() meta} class.
 */
public class CreateBeanFunction implements PublishedFunction {

  private final MetaBean _metaBean;
  private final MetaFunction _metaFunction;
  private final String _paramDescription;

  /**
   * @param functionName The name of the function
   * @param beanClass The class of object that the function will create
   * @param <T> Class of object to be created
   */
  public <T extends Bean> CreateBeanFunction(String functionName, Class<T> beanClass) {
    _metaBean = JodaBeanUtils.metaBean(beanClass);
    final List<MetaParameter> metaParameters = new ArrayList<MetaParameter>();
    List<String> paramDescriptions = new ArrayList<String>();
    for (MetaProperty<?> metaProperty : BeanUtils.writableMetaProperties(_metaBean)) {
      Class<?> propertyType = metaProperty.propertyType();
      paramDescriptions.add(propertyType.getSimpleName() + " " + metaProperty.name());
      // TODO get the PropertyDefinition annotation from the property and check whether its validate property = notNull
      JavaTypeInfo<?> typeInfo = JavaTypeInfo.builder(propertyType).allowNull().get();
      // TODO parameter descriptions
      metaParameters.add(new MetaParameter(metaProperty.name(), typeInfo));
    }
    _paramDescription = StringUtils.join(paramDescriptions, ", ");
    _metaFunction = new MetaFunction(null, functionName, metaParameters, new Invoker(metaParameters));
  }

  @Override
  public MetaFunction getMetaFunction() {
    return _metaFunction;
  }

  /**
   * Package scoped for easier testing - the test can use {@link #invokeImpl(SessionContext, Object[])} instead
   * of {@link #invoke(SessionContext, List)} and doesn't have to convert the parameters to {@link Data}.
   */
  /* package */class Invoker extends AbstractFunctionInvoker {

    private final List<MetaParameter> _metaParameters;

    public Invoker(List<MetaParameter> metaParameters) {
      super(metaParameters);
      _metaParameters = metaParameters;
    }

    @Override
    protected Object invokeImpl(SessionContext sessionContext, Object[] parameters) {
      ArgumentChecker.notNull(parameters, "parameters");
      // TODO is this check necessary?  will the language plumbing take care of counting arguments? I would hope so
      if (parameters.length > _metaParameters.size()) {
        throw new IllegalArgumentException("Too many parameters received: " + parameters.length + ", expected: " +
                                               _metaParameters.size());
      }
      BeanBuilder<? extends Bean> builder = _metaBean.builder();
      int i = 0;
      for (Object parameter : parameters) {
        MetaParameter metaParameter = _metaParameters.get(i++);
        builder.set(metaParameter.getName(), parameter);
      }
      return builder.build();
    }
  }

  @Override
  public String toString() {
    return "CreateBeanFunction{" + _metaFunction.getName() + "(" + _paramDescription + ")}";
  }
}
