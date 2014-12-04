/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata;

import java.util.Objects;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.util.money.Currency;

/**
 * Market data ID identifying an FX matrix for a particular set of currencies.
 */
public final class FxMatrixId implements MarketDataId<FXMatrix> {

  private final Set<Currency> _currencies;

  private FxMatrixId(Set<Currency> currencies) {
    _currencies = ImmutableSet.copyOf(currencies);
  }

  /**
   * Returns an FX matrix for the specified set of currencies.
   *
   * @param currencies the currencies in the matrix
   * @return an FX matrix for the specified set of currencies
   */
  public static FxMatrixId of(Set<Currency> currencies) {
    return new FxMatrixId(currencies);
  }

  /**
   * Returns an FX matrix for the specified currencies.
   *
   * @param currencies the currencies in the matrix
   * @return an FX matrix for the specified currencies
   */
  public static FxMatrixId of(Currency... currencies) {
    return new FxMatrixId(ImmutableSet.copyOf(currencies));
  }

  /**
   * @return the currencies in the FX matrix
   */
  Set<Currency> getCurrencies() {
    return _currencies;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_currencies);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final FxMatrixId other = (FxMatrixId) obj;
    return Objects.equals(this._currencies, other._currencies);
  }

  @Override
  public String toString() {
    return "FxMatrixMarketDataId{_currencies=" + _currencies + '}';
  }

  @Override
  public Class<FXMatrix> getMarketDataType() {
    return FXMatrix.class;
  }
}
