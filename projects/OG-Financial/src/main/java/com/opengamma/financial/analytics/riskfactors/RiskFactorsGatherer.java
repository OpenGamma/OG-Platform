/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.riskfactors;

import java.util.Set;

import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.Position;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.view.ViewCalculationConfiguration;

/**
 * Gathers the risk factors available from a function library for elements of a portfolio, producing a
 * {@link ValueRequirement} for each risk factor that the function library can produce.
 */
public interface RiskFactorsGatherer {

  /**
   * Gets the risk factors for a single position.
   * 
   * @param position  the position, not null
   * @return the risk factors, not null
   */
  Set<ValueRequirement> getPositionRiskFactors(Position position);
  
  /**
   * Gets the risk factors for every position in a portfolio.
   * 
   * @param portfolio  the portfolio, not null
   * @return the risk factors, not null
   */
  Set<ValueRequirement> getPositionRiskFactors(Portfolio portfolio);
  
  /**
   * Adds the risk factors as portfolio requirements to a view calculation configuration.
   * 
   * @param portfolio  the portfolio, not null
   * @param calcConfig  the view calculation configuration, not null
   */
  void addPortfolioRiskFactors(Portfolio portfolio, ViewCalculationConfiguration calcConfig);
  
}
