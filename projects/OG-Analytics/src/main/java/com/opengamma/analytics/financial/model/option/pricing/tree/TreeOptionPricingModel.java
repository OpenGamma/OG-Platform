/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.tree;

import com.opengamma.analytics.financial.greeks.GreekResultCollection;
import com.opengamma.analytics.financial.model.option.definition.StandardOptionDataBundle;

/**
 * 
 */
public abstract class TreeOptionPricingModel {

  /**
   * Compute option price on one asset with constant volatility, interest rate and dividend
   * @param lattice {@link LatticeSpecification}
   * @param function {@link OptionFunctionProvider1D}
   * @param spot Spot price of underlying
   * @param volatility Volatility 
   * @param interestRate Interest rate
   * @param dividend Dividend
   * @return Option price
   */
  public abstract double getPrice(final LatticeSpecification lattice, final OptionFunctionProvider1D function, final double spot, final double volatility, final double interestRate,
      final double dividend);

  /**
   * Compute option price on one asset with time-varying volatility, interest rate and dividend
   * The condition (number of steps) = (volatility length) = (interest rate length) = (dividend length) Should hold
   * @param function {@link OptionFunctionProvider1D}
   * @param spot Spot price of underlying
   * @param volatility Volatility 
   * @param interestRate Interest rate
   * @param dividend Dividend
   * @return Option price
   */
  public abstract double getPrice(final OptionFunctionProvider1D function, final double spot, final double[] volatility, final double[] interestRate, final double[] dividend);

  /**
   * Compute option price on one asset with volatility, interest rate and discrete dividends 
   * @param lattice {@link LatticeSpecification}
   * @param function {@link OptionFunctionProvider1D}
   * @param spot Spot price of underlying
   * @param volatility Volatility 
   * @param interestRate Interest rate
   * @param dividend {@link DividendFunctionProvider}
   * @return Option price
   */
  public abstract double getPrice(final LatticeSpecification lattice, final OptionFunctionProvider1D function, final double spot, final double volatility, final double interestRate,
      final DividendFunctionProvider dividend);

  /**
   * Compute option price on two assets with constant volatilities, correlation, interest rate and dividends
   * @param function {@link OptionFunctionProvider2D}
   * @param spot1 Spot price of asset 1
   * @param spot2 Spot price of asset 2
   * @param volatility1 Volatility of asset 1
   * @param volatility2 Volatility of asset 2
   * @param correlation Correlation between asset 1 and asset 2
   * @param interestRate Interest rate
   * @param dividend1 Dividend of asset 1 
   * @param dividend2 Dividend of asset 2
   * @return Option price
   */
  public abstract double getPrice(final OptionFunctionProvider2D function, final double spot1, final double spot2, final double volatility1, final double volatility2, final double correlation,
      final double interestRate, final double dividend1, final double dividend2);

  /**
   * Compute option Greeks on one asset with constant volatility, interest rate and dividend
   * @param lattice {@link LatticeSpecification}
   * @param function {@link OptionFunctionProvider1D}
   * @param spot Spot price of underlying
   * @param volatility Volatility 
   * @param interestRate Interest rate
   * @param dividend Dividend
   * @return Option Greeks as {@link GreekResultCollection}
   */
  public abstract GreekResultCollection getGreeks(final LatticeSpecification lattice, final OptionFunctionProvider1D function, final double spot, final double volatility, final double interestRate,
      final double dividend);

  /**
   * Compute option Greeks on one asset with time-varying volatility, interest rate and dividend
   * The condition (number of steps) = (volatility length) = (interest rate length) = (dividend length) Should hold
   * @param function {@link OptionFunctionProvider1D}
   * @param spot Spot price of underlying
   * @param volatility Volatility 
   * @param interestRate Interest rate
   * @param dividend Dividend
   * @return Option Greeks as {@link GreekResultCollection}
   */
  public abstract GreekResultCollection getGreeks(final OptionFunctionProvider1D function, final double spot, final double[] volatility, final double[] interestRate, final double[] dividend);

  /**
   * Compute option Greeks on one asset with volatility, interest rate and discrete dividends 
   * @param lattice {@link LatticeSpecification}
   * @param function {@link OptionFunctionProvider1D}
   * @param spot Spot price of underlying
   * @param volatility Volatility 
   * @param interestRate Interest rate
   * @param dividend {@link DividendFunctionProvider}
   * @return Option Greeks as {@link GreekResultCollection}
   */
  public abstract GreekResultCollection getGreeks(final LatticeSpecification lattice, final OptionFunctionProvider1D function, final double spot, final double volatility, final double interestRate,
      final DividendFunctionProvider dividend);

  /**
   * Compute option Greeks on two assets with constant volatilities, correlation, interest rate and dividends
   * @param function {@link OptionFunctionProvider2D}
   * @param spot1 Spot price of asset 1
   * @param spot2 Spot price of asset 2
   * @param volatility1 Volatility of asset 1
   * @param volatility2 Volatility of asset 2
   * @param correlation Correlation between asset 1 and asset 2
   * @param interestRate Interest rate
   * @param dividend1 Dividend of asset 1 
   * @param dividend2 Dividend of asset 2
   * @return Option Greeks as an array {option price, delta for asset 1, delta for asset 2, theta, gamma for asset 1, gamma for asset 2, cross gamma}
   */
  public abstract double[] getGreeks(final OptionFunctionProvider2D function, final double spot1, final double spot2, final double volatility1, final double volatility2, final double correlation,
      final double interestRate, final double dividend1, final double dividend2);

  /**
   * Compute option price by using implied tree
   * @param function {@link OptionFunctionProvider1D}
   * @param data Market data
   * @return Option price
   */
  public abstract double getPrice(final OptionFunctionProvider1D function, final StandardOptionDataBundle data);
}
