/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.util.ArgumentChecker;

/**
 * Generator (or template) for a swap described by its two legs generators. Both legs are in the same currency.
 */
public class GeneratorSwapSingleCurrency extends GeneratorInstrument<GeneratorAttributeIR> {

  /** The first leg generator. The market quote will be applied on this leg. */
  private final GeneratorLeg _leg1;
  /** The second leg generator. */
  private final GeneratorLeg _leg2;

  /**
   * Constructor.
   * @param name The generator name.
   * @param leg1 The first leg generator. The market quote will be applied on this leg.
   * @param leg2 The second leg generator. Both leg should be in the same currency.
   */
  public GeneratorSwapSingleCurrency(String name, GeneratorLeg leg1, GeneratorLeg leg2) {
    super(name);
    ArgumentChecker.notNull(leg1, "first leg");
    ArgumentChecker.notNull(leg2, "second leg");
    ArgumentChecker.isTrue(leg1.getCurrency().equals(leg2.getCurrency()), "Both legs should be in the same currency");
    _leg1 = leg1;
    _leg2 = leg2;
  }

  /**
   * Returns the first leg generator.
   * @return The generator.
   */
  public GeneratorLeg getLeg1() {
    return _leg1;
  }

  /**
   * Return the second leg generator.
   * @return The generator.
   */
  public GeneratorLeg getLeg2() {
    return _leg2;
  }

  @Override
  public SwapDefinition generateInstrument(ZonedDateTime date, double marketQuote, double notional, 
      GeneratorAttributeIR attribute) {
    return new SwapDefinition(_leg1.generateInstrument(date, marketQuote, notional, attribute), 
        _leg2.generateInstrument(date, 0.0, -notional, attribute));
  }

}
