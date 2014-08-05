/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;

/**
 * Generator (or template) for a swap overnight arithmetic average (plus spread) vs Ibor.
 */
public class GeneratorSwapONAAIbor extends GeneratorInstrument<GeneratorAttributeIR> {

  /** The overnight arithmetic average leg. */
  private final GeneratorLegONArithmeticAverage _legOnAa;
  /** The Ibor leg generator. */
  private final GeneratorLegIbor _legIbor;

  /**
   * Constructor.
   * @param name The generator name.
   * @param legOnAa The overnight arithmetic average leg.
   * @param legIbor The Ibor leg generator.
   */
  public GeneratorSwapONAAIbor(String name, GeneratorLegONArithmeticAverage legOnAa, GeneratorLegIbor legIbor) {
    super(name);
    _legOnAa = legOnAa;
    _legIbor = legIbor;
  }

  /**
   * Gets the legOnAa.
   * @return the legOnAa
   */
  public GeneratorLegONArithmeticAverage getLegOnAa() {
    return _legOnAa;
  }

  /**
   * Gets the legIbor.
   * @return the legIbor
   */
  public GeneratorLegIbor getLegIbor() {
    return _legIbor;
  }

  @Override
  public SwapDefinition generateInstrument(ZonedDateTime date, double marketQuote, double notional, GeneratorAttributeIR attribute) {
    return new SwapDefinition(_legOnAa.generateInstrument(date, marketQuote, notional, attribute), _legIbor.generateInstrument(date, 0.0, -notional, attribute));
  }

}
