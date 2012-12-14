/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

/**
 * Interface for parameter provider with multi-curves provider.
 */
public interface ParameterProviderInterface {

  /**
   * Returns the multicurve provider.
   * @return The multicurve provider
   */
  MulticurveProviderInterface getMulticurveProvider();

}
