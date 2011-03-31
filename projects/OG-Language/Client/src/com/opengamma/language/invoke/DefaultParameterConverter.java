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
    for (i = 0; i < parameters.length; i++) {
      parameters[i] = valueConverter.convertValue(sessionContext, clientParameters.get(i), targetParameters.get(i)
          .getJavaTypeInfo());
    }
    return parameters;
  }

}
