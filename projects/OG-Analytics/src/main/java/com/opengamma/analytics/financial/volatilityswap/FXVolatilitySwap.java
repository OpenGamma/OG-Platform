/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.volatilityswap;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * An FX volatility swap is a forward contract on the realised volatility an FX rate.
 */
public class FXVolatilitySwap extends VolatilitySwap {

  /**
   * The base currency.
   */
  private final Currency _baseCurrency;

  /**
   * The counter currency.
   */
  private final Currency _counterCurrency;

  /**
   * @param timeToObservationStart Time to first observation. Negative if observations have begun.
   * @param timeToObservationEnd Time to final observation. Negative if observations have finished.
   * @param observationFrequency The observation frequency, not null
   * @param timeToSettlement Time of cash settlement. If negative, the swap has expired.
   * @param volStrike Fair value of Variance struck at trade date
   * @param volNotional Trade pays the difference between realized and strike variance multiplied by this
   * @param currency Currency of cash settlement
   * @param baseCurrency The base currency, not null
   * @param counterCurrency The counter currency, not null
   * @param annualizationFactor Number of business days per year
   */
  public FXVolatilitySwap(final double timeToObservationStart, final double timeToObservationEnd, final PeriodFrequency observationFrequency,
      final double timeToSettlement, final double volStrike, final double volNotional, final Currency currency, final Currency baseCurrency,
      final Currency counterCurrency, final double annualizationFactor) {
    super(timeToObservationStart, timeToObservationEnd, observationFrequency, timeToSettlement, volStrike, volNotional, currency, annualizationFactor);
    ArgumentChecker.notNull(baseCurrency, "baseCurrency");
    ArgumentChecker.notNull(counterCurrency, "counterCurrency");
    _baseCurrency = baseCurrency;
    _counterCurrency = counterCurrency;
  }

  /**
   * Gets the base currency.
   * @return the base currency
   */
  public Currency getBaseCurrency() {
    return _baseCurrency;
  }
  /**
   * Gets the counter currency.
   * @return the counter currency
   */
  public Currency getCounterCurrency() {
    return _counterCurrency;
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitFXVolatilitySwap(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitFXVolatilitySwap(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _baseCurrency.hashCode();
    result = prime * result + _counterCurrency.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (!(obj instanceof FXVolatilitySwap)) {
      return false;
    }
    final FXVolatilitySwap other = (FXVolatilitySwap) obj;
    if (!ObjectUtils.equals(_baseCurrency, other._baseCurrency)) {
      return false;
    }
    if (!ObjectUtils.equals(_counterCurrency, other._counterCurrency)) {
      return false;
    }
    return true;
  }

}
