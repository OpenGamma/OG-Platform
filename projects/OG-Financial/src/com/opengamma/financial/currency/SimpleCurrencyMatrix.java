/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.currency;

import com.opengamma.core.common.Currency;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;

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
        // This definately shouldn't happen
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
   * @param source  the source currency
   * @param target  the target currency
   * @param uniqueId  the unique identifier of the external data
   */
  public void setLiveData(final Currency source, final Currency target, final UniqueIdentifier uniqueId) {
    ArgumentChecker.notNull(source, "source");
    ArgumentChecker.notNull(target, "target");
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    if (source.equals(target)) {
      throw new IllegalArgumentException("target");
    }
    addConversion(source, target, CurrencyMatrixValue.of(uniqueId));
  }

}
