/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.analytics;

import com.opengamma.engine.security.Security;

/**
 * A system through which individual {@link AnalyticFunctionDefinition} values
 * can be obtained, ultimately from one or more {@link AnalyticFunctionRepository} instances.
 *
 * @author kirk
 */
public interface AnalyticFunctionResolver {
  
  AnalyticFunctionDefinition resolve(
      AnalyticValueDefinition<?> requiredValue,
      Security security);

}
