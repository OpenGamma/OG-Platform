/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.riskfactors;

import com.opengamma.engine.value.ValueProperties;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public interface RiskFactorsConfigurationProvider {

  /**
   * Provides the opportunity to process the generated constraints.
   * 
   * @param constraints  the constraints, not null
   * @return the processed constraints, not null
   */
  ValueProperties processConstraints(ValueProperties constraints);
  
  /**
   * Gets the name of the funding curve to use where required.
   * 
   * @return the name of the funding curve, not null
   */
  String getFundingCurve();
  
  /**
   * Gets the name of the forward curve to use where required.
   * 
   * @param currency  the currency for which the forward curve is required, not null
   * @return the name of the forward curve, not null
   */
  String getForwardCurve(Currency currency);
  
}
