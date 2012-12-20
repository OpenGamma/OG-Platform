/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.object;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;

/**
 * A function which can all attribute values from a target object.
 */
public class ObjectValuesFunction implements PublishedFunction {

  private final Map<String, Method> _readers;
  private final MetaFunction _definition;

  public ObjectValuesFunction(final String category, final String name, final String description, final Map<String, Method> readers, final MetaParameter object) {
    _readers = readers;
    final List<MetaParameter> args = Collections.singletonList(object);
    _definition = new MetaFunction(category, name, args, new AbstractFunctionInvoker(args) {
      @Override
      protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
        try {
          final Map<String, Object> result = new HashMap<String, Object>();
          for (Map.Entry<String, Method> reader : _readers.entrySet()) {
            result.put(reader.getKey(), reader.getValue().invoke(parameters[0]));
          }
          return result;
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
