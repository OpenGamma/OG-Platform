/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.var;

/**
 * @param <S> The type of the parameters
 * @param <T> The type of the data
 */
public interface VaRCalculator<S, T> {

  @SuppressWarnings("unchecked")
  VaRCalculationResult evaluate(final S parameters, final T... data);
}
