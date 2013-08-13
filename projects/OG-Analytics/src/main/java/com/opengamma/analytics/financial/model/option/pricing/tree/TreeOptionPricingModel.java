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

  public abstract double getPrice(final LatticeSpecification lattice, final OptionFunctionProvider function, final double spot, final double timeToExpiry, final double volatility,
      final double interestRate, final double dividend);

  public abstract GreekResultCollection getGreeks(final LatticeSpecification lattice, final OptionFunctionProvider function, final double spot, final double timeToExpiry, final double volatility,
      final double interestRate, final double dividend);

  /*
   * TODO Following options are NOT yet supported
   * ComplexChooserOptionDefinition
   * EuropeanOptionOnEuropeanVanillaOptionDefinition
   * EuropeanStandardBarrierOptionDefinition
   * ExtremeSpreadOptionDefinition
   * FadeInOptionDefinition
   * FixedStrikeLookbackOptionDefinition
   * FloatingStrikeLookbackOptionDefinition
   * ForwardStartOptionDefinition
   * SimpleChooserOptionDefinition
   */

  //  protected abstract double getPrice(final LatticeSpecification lattice, final OptionPayoffFunction<StandardOptionDataBundle> payoffFunction, 
  //  final StandardOptionDataBundle data, final double strike,      final double timeToExpiry, final int nSteps,
  //      final boolean isCall);
  //
  //  protected abstract double getPrice(final LatticeSpecification lattice, final StandardOptionDataBundle data, final double strike, final double timeToExpiry, final int nSteps,
  //      final boolean isCall);

}
