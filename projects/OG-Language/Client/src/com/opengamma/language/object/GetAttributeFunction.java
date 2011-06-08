/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.object;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

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

  private final Method _read;
  private final MetaFunction _definition;

  public GetAttributeFunction(final String name, final String description, final Method read, final MetaParameter object) {
    _read = read;
    final List<MetaParameter> args = Collections.singletonList(object);
    _definition = new MetaFunction(name, args, new AbstractFunctionInvoker(args) {
      @Override
      protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
        try {
          return _read.invoke(parameters[0]);
        } catch (Exception e) {
          throw new OpenGammaRuntimeException(e.getMessage(), e);
        }
      }
    });
    _definition.setDescription(description);
  }

  @Override
  public MetaFunction getMetaFunction() {
    return _definition;
  }

}
