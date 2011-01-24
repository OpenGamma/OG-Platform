/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.future.definition;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

/**
 * 
 */
public class BondFutureDeliverableBasketDataBundle {
  private final double[] _cleanPrices;
  private final double[] _repoRates;
  private final int _n;

  public BondFutureDeliverableBasketDataBundle(final double[] cleanPrices, final double[] repoRates) {
    Validate.notNull(cleanPrices, "clean prices");
    Validate.notNull(repoRates, "repoRates");
    _n = cleanPrices.length;
    Validate.isTrue(_n == repoRates.length, "must have a clean price and repo rate for each bond");
    _cleanPrices = cleanPrices;
    _repoRates = repoRates;
  }

  public double[] getCleanPrices() {
    return _cleanPrices;
  }

  public double[] getRepoRates() {
    return _repoRates;
  }

  public int getBasketSize() {
    return _n;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(_cleanPrices);
    result = prime * result + Arrays.hashCode(_repoRates);
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final BondFutureDeliverableBasketDataBundle other = (BondFutureDeliverableBasketDataBundle) obj;
    if (!Arrays.equals(_cleanPrices, other._cleanPrices)) {
      return false;
    }
    return Arrays.equals(_repoRates, other._repoRates);
  }

}
