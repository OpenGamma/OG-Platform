/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

import java.util.List;
import java.util.Set;

import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.util.tuple.DoublesPair;

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

  // TODO: Maybe some of the methods below should be in an implementation class.
  // REVIEW emcleod 2013-9-16 Yes, they should be moved - these classes do far too much and there's
  // quite a lot of code repeated between various providers.
  /**
   * Gets the sensitivities to the curve parameters in the MulticurveProvider.
   * @param name The curve name
   * @param pointSensitivity The point sensitivities
   * @return The sensitivities to the parameters
   */
  double[] parameterSensitivity(String name, List<DoublesPair> pointSensitivity);

  /**
   * Gets the forward sensitivities to the curve parameters.
   * @param name The curve name
   * @param pointSensitivity The point sensitivities
   * @return The forward sensitivities to the parameters
   */
  double[] parameterForwardSensitivity(String name, List<ForwardSensitivity> pointSensitivity);

  /**
   * Returns an unmodifiable sorted set of all curve names. If there are no curves in the provider,
   * returns an empty set.
   * @return An unmodifiable set of all curve names.
   */
  Set<String> getAllCurveNames();
}
