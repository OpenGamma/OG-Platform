/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.object;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;

/**
 * A function which can set an attribute value on a target object.
 */
public class SetAttributeFunction implements PublishedFunction {

  private final Method _write;
  private final MetaFunction _definition;

  public SetAttributeFunction(final String name, final String description, final Method write, final MetaParameter object, final MetaParameter value) {
    _write = write;
    final List<MetaParameter> args = Arrays.asList(object, value);
    _definition = new MetaFunction(name, args, new AbstractFunctionInvoker(args) {
      @Override
      protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
        try {
          _write.invoke(parameters[0], parameters[1]);
          return parameters[0];
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
