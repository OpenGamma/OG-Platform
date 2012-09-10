/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.currency;

import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * A simple, mutable, {@link CurrencyMatrix}. When conversion entries are added, the reciprocal
 * value is also added.
 */
public class SimpleCurrencyMatrix extends AbstractCurrencyMatrix {

  @Override
  protected void addConversion(final Currency source, final Currency target, final CurrencyMatrixValue rate) {
    super.addConversion(source, target, rate);
    super.addConversion(target, source, rate.getReciprocal());
  }

  /**
   * Sets the conversion rate in the matrix as the number of units of the source currency per unit of the
   * target currency. The supplied value and reciprocal are added.
   * 
   * @param source the source currency
   * @param target the target currency
   * @param rate the number of units of source currency per unit of target currency
   */
  public void setFixedConversion(final Currency source, final Currency target, final double rate) {
    ArgumentChecker.notNull(source, "source");
    ArgumentChecker.notNull(target, "target");
    ArgumentChecker.notZero(0, 0, "rate");
    if (source.equals(target)) {
      // This shouldn't happen in sensible code
      if (rate != 1.0) {
        // This definitely shouldn't happen
        throw new IllegalArgumentException("rate");
      }
      return;
    }
    addConversion(source, target, CurrencyMatrixValue.of(rate));
  }

  /**
   * Sets the matrix to convert the two currencies using an intermediate rate. source:cross and cross:target
   * must have already been added to the matrix for this to succeed.
   * 
   * @param source the source currency
   * @param target the target currency
   * @param cross the intermediate currency
   */
  public void setCrossConversion(final Currency source, final Currency target, final Currency cross) {
    ArgumentChecker.notNull(source, "source");
    ArgumentChecker.notNull(target, "target");
    ArgumentChecker.notNull(target, "cross");
    if (source.equals(target)) {
      throw new IllegalArgumentException("target");
    }
    if (source.equals(cross) || target.equals(cross)) {
      throw new IllegalArgumentException("cross");
    }
    if (getConversion(source, cross) == null) {
      throw new IllegalArgumentException("cross");
    }
    if (getConversion(cross, target) == null) {
      throw new IllegalArgumentException("cross");
    }
    addConversion(source, target, CurrencyMatrixValue.of(cross));
  }

  /**
   * Sets the matrix to convert two currencies using market data supplied externally, for example a live data provider.
   * 
   * @param source the source currency, not null
   * @param target the target currency, not null
   * @param valueRequirement the market data requirement, not null
   */
  public void setLiveData(final Currency source, final Currency target, final ValueRequirement valueRequirement) {
    ArgumentChecker.notNull(source, "source");
    ArgumentChecker.notNull(target, "target");
    ArgumentChecker.notNull(valueRequirement, "valueRequirement");
    if (source.equals(target)) {
      throw new IllegalArgumentException("target");
    }
    addConversion(source, target, CurrencyMatrixValue.of(valueRequirement));
  }

}
