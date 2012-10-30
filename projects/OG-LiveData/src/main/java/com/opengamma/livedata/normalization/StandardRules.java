/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;

import java.util.Collections;

import com.opengamma.core.value.MarketDataRequirementNames;

/**
 * Normalization rules that are known to be part of the OpenGamma standard package.
 */
public class StandardRules {
  
  private static final NormalizationRuleSet NO_NORMALIZATION = 
    new NormalizationRuleSet("No Normalization", 
        "Raw",
        Collections.<NormalizationRule>emptyList());
  
  /**
   * Gets the ID of the standard OpenGamma normalization rule set. 
   * <p>
   * Market data messages normalized with the standard OpenGamma 
   * scheme will include:
   * <ul>
   * <li>{@link MarketDataRequirementNames#MARKET_VALUE}
   * <li>{@link MarketDataRequirementNames#VOLUME} (if available)
   * <li>{@link MarketDataRequirementNames#IMPLIED_VOLATILITY} (if available)
   * <li>{@link MarketDataRequirementNames#YIELD_CONVENTION_MID} (if available)
   * <li>{@link MarketDataRequirementNames#YIELD_YIELD_TO_MATURITY_MID} (if available)
   * <li>{@link MarketDataRequirementNames#DIRTY_PRICE_MID} (if available)
   * </ul>
   * 
   * @return the ID of the standard OpenGamma normalization rule set. 
   */
  public static String getOpenGammaRuleSetId() {
    return "OpenGamma";
  }
  
  /**
   * Gets the ID of the standard "no-normalization" rule set.
   * <p>
   * This normalization rule will pass messages from the
   * underlying market data API to the client unmodified.
   *  
   * @return the ID of the standard "no-normalization" rule set.
   */
  public static NormalizationRuleSet getNoNormalization() {
    return NO_NORMALIZATION;
  }
  
}
