/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.config;

/**
 * Visitor for processing {@link ConfigurationItem} instances.
 * 
 * @param <T> return type of the accept method
 */
public interface ConfigurationItemVisitor<T> {

  /**
   * Apply to an {@link EnableCycleAccess} instance.
   * 
   * @param enableCycleAccess the instance
   * @return the application result
   */
  T visitEnableCycleAccess(EnableCycleAccess enableCycleAccess);

  /**
   * Apply to a {@link MarketDataOverride} instance.
   * 
   * @param marketDataOverride the instance
   * @return the application result
   */
  T visitMarketDataOverride(MarketDataOverride marketDataOverride);

  /**
   * Apply to a {@link ValueProperty} instance.
   * 
   * @param valueProperty the instance
   * @return the application result
   */
  T visitValueProperty(ValueProperty valueProperty);

  /**
   * Apply to a {@link ViewCalculationRate} instance.
   * 
   * @param viewCalculationRate the instance
   * @return the application result
   */
  T visitViewCalculationRate(ViewCalculationRate viewCalculationRate);

}
