/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.util.ArgumentChecker;

/**
 * Generator (or template) for a swap described by its two legs generators.
 */
public class GeneratorSwapCrossCurrency extends GeneratorInstrument<GeneratorAttributeFX> {

  /** The first leg generator. The market quote will be applied on this leg. */
  private final GeneratorLeg _leg1;
  /** The second leg generator. */
  private final GeneratorLeg _leg2;

  /**
   * Constructor.
   * @param name The generator name.
   * @param leg1 The first leg generator. The market quote will be applied on this leg.
   * @param leg2 The second leg generator.
   */
  public GeneratorSwapCrossCurrency(String name, GeneratorLeg leg1, GeneratorLeg leg2) {
    super(name);
    ArgumentChecker.notNull(leg1, "first leg");
    ArgumentChecker.notNull(leg2, "second leg");
    _leg1 = leg1;
    _leg2 = leg2;
  }

  /**
   * Gets the legOnAa.
   * @return the legOnAa
   */
  public GeneratorLeg getLeg1() {
    return _leg1;
  }

  /**
   * Gets the legIbor.
   * @return the legIbor
   */
  public GeneratorLeg getLeg2() {
    return _leg2;
  }

  @Override
  public SwapDefinition generateInstrument(ZonedDateTime date, double marketQuote, double notional, GeneratorAttributeFX attribute) {
    GeneratorAttributeIR attributeIr = new GeneratorAttributeIR(attribute.getStartPeriod(), attribute.getEndPeriod());
    AnnuityDefinition<?> leg1 = _leg1.generateInstrument(date, marketQuote, notional, attributeIr);
    final double fx = attribute.getFXMatrix().getFxRate(_leg1.getCcy(), _leg2.getCcy());
    AnnuityDefinition<?> leg2 = _leg1.generateInstrument(date, 0.0, -fx * notional, attributeIr);
    return new SwapDefinition(leg1, leg2);
  }

}
