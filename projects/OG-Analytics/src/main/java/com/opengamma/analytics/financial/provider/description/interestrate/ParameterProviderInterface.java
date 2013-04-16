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
   * Create a new copy of the provider. For the curves related data, new maps are created. The parameter related data, the same objects are used.
   * @return The new provider.
   */
  ParameterProviderInterface copy();

  /**
   * Returns the multicurve provider.
   * @return The multicurve provider
   */
  MulticurveProviderInterface getMulticurveProvider();

}
