/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.analytics;

import java.util.Collection;

import com.opengamma.engine.security.Security;

/**
 * 
 *
 * @author kirk
 */
public interface SecurityAnalyticFunctionDefinition
extends AnalyticFunctionDefinition {

  /**
   * Determine whether this function is applicable to the specified security type
   * in general.
   * 
   * @param securityType The name of the security type to check.
   * @return {@code true} iff this function is potentially applicable to a position
   *         in a security with the specified type.
   */
  boolean isApplicableTo(String securityType);

  Collection<AnalyticValueDefinition<?>> getPossibleResults(Security security);
  
  Collection<AnalyticValueDefinition<?>> getInputs(Security security);
  
}
