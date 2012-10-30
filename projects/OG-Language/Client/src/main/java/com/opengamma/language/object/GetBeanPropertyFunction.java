/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.language.object;

import java.util.List;

import org.joda.beans.Bean;
import org.joda.beans.MetaProperty;

import com.google.common.collect.ImmutableList;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * A function which can retrieve a property value from a Joda {@link Bean} instance.
 */
public class GetBeanPropertyFunction implements PublishedFunction {

  private final MetaFunction _definition;
  
  public GetBeanPropertyFunction(final String functionName, final MetaProperty<?> property, final MetaParameter beanParameter) {
    List<MetaParameter> parameters = ImmutableList.of(beanParameter);
    _definition = new MetaFunction(null, functionName, parameters, new AbstractFunctionInvoker(parameters) {

      @Override
      protected Object invokeImpl(SessionContext sessionContext, Object[] parameters) throws AsynchronousExecution {
        return property.get((Bean) parameters[0]);
      }
      
    });
    _definition.setDescription("Gets the " + property.name() + " field from a " + property.declaringType().getSimpleName());
  }
  
  @Override
  public MetaFunction getMetaFunction() {
    return _definition;
  }

}
