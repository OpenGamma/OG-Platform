/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.helper;

import org.threeten.bp.Instant;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.position.Portfolio;
import com.opengamma.id.UniqueId;

/**
 * Provides access to details of the available outputs from an input set (e.g. a portfolio or view definition).
 */
public interface AvailableOutputsProvider {

  /**
   * Gets details of the available outputs for a portfolio, by passing the portfolio to be analysed.
   * <p>
   * This allows the available outputs to be retrieved for portfolios which cannot be referenced by unique identifier.
   * 
   * @param portfolio  the portfolio, not null
   * @param instant  the instant for which to analyse the available outputs, null for now
   * @return the available outputs, not null
   */
  AvailableOutputs getPortfolioOutputs(Portfolio portfolio, Instant instant);
  
  /**
   * Gets details of the available outputs for a portfolio, by passing the portfolio to be analysed.
   * <p>
   * This allows the available outputs to be retrieved for portfolios which cannot be referenced by unique identifier.
   * <p>
   * The portfolio may be truncated by limiting the number of positions and/or the number of nodes at each level. These
   * limits can be used to avoid processing every item when the portfolio is known to contain duplicate security types.
   * 
   * @param portfolio  the portfolio, not null
   * @param instant  the instant for which to analyse the available outputs, null for now
   * @param maxPositions  the maximum number of positions at each level, not negative, null for unlimited
   * @param maxNodes  the maximum number of nodes at each level, not negative, null for unlimited
   * @return the available outputs, not null
   * @throws DataNotFoundException  if the portfolio identifier is invalid or cannot be resolved to a portfolio
   */
  AvailableOutputs getPortfolioOutputs(Portfolio portfolio, Instant instant, Integer maxNodes, Integer maxPositions);
  
  /**
   * Gets details of the available outputs for a portfolio, by the portfolio identifier.
   * 
   * @param portfolioId  the unique identifier of the portfolio, not null
   * @param instant  the instant for which to analyse the available outputs, null for now
   * @return the available outputs, not null
   * @throws DataNotFoundException  if the portfolio identifier is invalid or cannot be resolved to a portfolio
   */
  AvailableOutputs getPortfolioOutputs(UniqueId portfolioId, Instant instant);
  
  /**
   * Gets details of the available outputs for a portfolio, by the portfolio identifier.
   * <p>
   * The portfolio may be truncated by limiting the number of positions and/or the number of nodes at each level. These
   * limits can be used to avoid processing every item when the portfolio is known to contain duplicate security types.
   * 
   * @param portfolioId  the unique identifier of the portfolio, not null
   * @param instant  the instant for which to analyse the available outputs, null for now
   * @param maxPositions  the maximum number of positions at each level, not negative, null for unlimited
   * @param maxNodes  the maximum number of nodes at each level, not negative, null for unlimited
   * @return the available outputs, not null
   * @throws DataNotFoundException  if the portfolio identifier is invalid or cannot be resolved to a portfolio
   */
  AvailableOutputs getPortfolioOutputs(UniqueId portfolioId, Instant instant, Integer maxNodes, Integer maxPositions);
  
}
