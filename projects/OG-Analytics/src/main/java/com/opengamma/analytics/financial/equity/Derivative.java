/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity;

/**
 * 
 */
public interface Derivative {

  <S, T> T accept(DerivativeVisitor<S, T> visitor, S data);

  <T> T accept(DerivativeVisitor<?, T> visitor);

  /**
   * @return 'Analytics Time' (years in ACT_ACT) between valuation and settlement times 
   */
  double getTimeToSettlement();
}
