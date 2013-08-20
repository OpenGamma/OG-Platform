/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.tree;

import com.opengamma.analytics.financial.greeks.GreekResultCollection;

/**
 * 
 */
public abstract class TreeOptionPricingModel {

  /*
   * TODO Two dimensional case must be implemented (by using StandardTwoAssetOptionDataBundle class)
   */
  //  public double getPrice(final LatticeSpecification lattice, final OptionDefinition definition, final StandardOptionDataBundle data, final int steps) {
  //    final double strike = definition.getStrike();
  //    final double timeToExpiry = definition.getTimeToExpiry(data.getDate());
  //    final boolean isCall = definition.isCall();
  //
  //    if (definition instanceof AmericanVanillaOptionDefinition) {
  //      return this.getPrice(lattice, data, strike, timeToExpiry, steps, isCall);
  //    } else {
  //      return this.getPrice(lattice, definition.getPayoffFunction(), data, strike, timeToExpiry, steps, isCall);
  //    }
  //  }

  /**
   * Compute option price with constant volatility, interest rate and dividend
   * @param lattice {@link LatticeSpecification}
   * @param function [@link OptionFunctionProvider1D}
   * @param spot Spot price of underlying
   * @param timeToExpiry Time to expiry
   * @param volatility Volatility 
   * @param interestRate Interest rate
   * @param dividend Dividend
   * @return Option price
   */
  public abstract double getPrice(final LatticeSpecification lattice, final OptionFunctionProvider1D function, final double spot, final double timeToExpiry, final double volatility,
      final double interestRate, final double dividend);

  /**
   * Compute option price with time-varying volatility, interest rate and dividend
   * The condtion (number of steps) = (volatility length) = (interest rate length) = (dividend length) Should hold
   * @param function [@link OptionFunctionProvider1D}
   * @param spot Spot price of underlying
   * @param timeToExpiry Time to expiry
   * @param volatility Volatility 
   * @param interestRate Interest rate
   * @param dividend Dividend
   * @return Option price
   */
  public abstract double getPrice(final OptionFunctionProvider1D function, final double spot, final double timeToExpiry, final double[] volatility, final double[] interestRate,
      final double[] dividend);

  /**
   * Compute option Greeks with constant volatility, interest rate and dividend
   * @param lattice {@link LatticeSpecification}
   * @param function [@link OptionFunctionProvider1D}
   * @param spot Spot price of underlying
   * @param timeToExpiry Time to expiry
   * @param volatility Volatility 
   * @param interestRate Interest rate
   * @param dividend Dividend
   * @return Option Greeks as {@link GreekResultCollection}
   */
  public abstract GreekResultCollection getGreeks(final LatticeSpecification lattice, final OptionFunctionProvider1D function, final double spot, final double timeToExpiry, final double volatility,
      final double interestRate, final double dividend);

  /**
   * Compute option Greeks with time-varying volatility, interest rate and dividend
   * The condtion (number of steps) = (volatility length) = (interest rate length) = (dividend length) Should hold
   * @param function [@link OptionFunctionProvider1D}
   * @param spot Spot price of underlying
   * @param timeToExpiry Time to expiry
   * @param volatility Volatility 
   * @param interestRate Interest rate
   * @param dividend Dividend
   * @return Option Greeks as {@link GreekResultCollection}
   */
  public abstract GreekResultCollection getGreeks(final OptionFunctionProvider1D function, final double spot, final double timeToExpiry, final double[] volatility, final double[] interestRate,
      final double[] dividend);

  public abstract double getPriceWithDiscreteDividends(final LatticeSpecification lattice, final OptionFunctionProvider1D function, final double spot, final double timeToExpiry,
      final double volatility,
      final double interestRate, final DividendFunctionProvider dividend);

  public abstract GreekResultCollection getGreeksWithDiscreteDividends(final LatticeSpecification lattice, final OptionFunctionProvider1D function, final double spot, final double timeToExpiry,
      final double volatility,
      final double interestRate, final DividendFunctionProvider dividend);

}
