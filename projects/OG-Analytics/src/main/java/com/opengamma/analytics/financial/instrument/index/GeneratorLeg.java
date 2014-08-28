/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Generator (or template) for single currency leg. 
 * Abstract class.
 */
public abstract class GeneratorLeg extends GeneratorInstrument<GeneratorAttributeIR> {
  
  /** The leg generator currency. */
  private final Currency _ccy;
  
  /** 
   * Constructor.
   * @param name The generator name.
   * @param ccy The leg generator currency.
   */
  public GeneratorLeg(String name, Currency ccy) {
    super(name);
    ArgumentChecker.notNull(ccy, "currency");
    _ccy = ccy;
  }
  
  /**
   * Returns the leg generator currency.
   * @return The currency.
   */
  public Currency getCcy() {
    return _ccy;
  }
  
  @Override
  /**
   * The leg generated is a receiver leg. To obtain a payer leg, use a negative notional.
   */
  public abstract  AnnuityDefinition<?> generateInstrument(final ZonedDateTime date, final double marketQuote, 
      final double notional, final GeneratorAttributeIR attribute);

}
