/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.object;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import org.joda.beans.Bean;
import org.joda.beans.MetaProperty;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;

/**
 * A function which can retrieve an attribute value from a target object.
 */
public class GetAttributeFunction implements PublishedFunction {

  private final MetaFunction _definition;

  public GetAttributeFunction(final String category, final String name, final String description, final Method read, final MetaParameter object) {
    final List<MetaParameter> args = Collections.singletonList(object);
    _definition = new MetaFunction(category, name, args, new AbstractFunctionInvoker(args) {
      @Override
      protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
        try {
          return read.invoke(parameters[0]);
        } catch (Exception e) {
          throw new OpenGammaRuntimeException(e.getMessage(), e);
        }
      }
    });
    _definition.setDescription(description);
  }

  public GetAttributeFunction(final String category, final String name, final String description, final MetaProperty<?> metaProperty, final MetaParameter object) {
    final List<MetaParameter> args = Collections.singletonList(object);
    _definition = new MetaFunction(category, name, args, new AbstractFunctionInvoker(args) {
      @Override
      protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
        return metaProperty.get((Bean) parameters[0]);
      }
    });
    _definition.setDescription(description);
  }

  @Override
  public MetaFunction getMetaFunction() {
    return _definition;
  }

}
