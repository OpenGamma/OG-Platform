/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.invoke;

import java.util.List;

import com.opengamma.language.Data;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.error.InvokeParameterConversionException;

/**
 * Default implementation of {@link ParameterConverter}.
 */
public class DefaultParameterConverter implements ParameterConverter {

  @Override
  public Object[] convertParameters(final SessionContext sessionContext, final List<Data> clientParameters,
      final List<MetaParameter> targetParameters) {
    final ValueConverter valueConverter = sessionContext.getGlobalContext().getValueConverter();
    final Object[] parameters = new Object[clientParameters.size()];
    int i = 0;
    try {
      while (i < parameters.length) {
        final Object converted = valueConverter.convertValue(sessionContext, clientParameters.get(i), targetParameters.get(i).getJavaTypeInfo());
        parameters[i++] = converted;
      }
    } catch (InvalidConversionException e) {
      throw new InvokeParameterConversionException(i, e.getClientMessage());
    }
    return parameters;
  }

}
