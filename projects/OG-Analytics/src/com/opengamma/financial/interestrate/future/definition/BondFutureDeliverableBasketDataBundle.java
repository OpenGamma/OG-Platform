/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.future.definition;

import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

/**
 * 
 */
public class BondFutureDeliverableBasketDataBundle {
  private final List<Double> _deliveryDates;
  private final List<Double> _cleanPrices;
  private final List<Double> _accruedInterest;
  private final List<Double> _repoRates;
  private final int _n;

  //TODO is the list of delivery dates strictly necessary?
  public BondFutureDeliverableBasketDataBundle(final List<Double> deliveryDates, final List<Double> cleanPrices, final List<Double> accruedInterest, final List<Double> repoRates) {
    Validate.noNullElements(deliveryDates, "delivery dates");
    Validate.noNullElements(cleanPrices, "clean prices");
    Validate.noNullElements(accruedInterest, "accrued interest");
    Validate.noNullElements(repoRates, "repoRates");
    _n = deliveryDates.size();
    Validate.isTrue(cleanPrices.size() == _n, "must have a clean price for bond");
    Validate.isTrue(accruedInterest.size() == _n, "must have accrued interest for each bond");
    Validate.isTrue(repoRates.size() == _n, "must have repo rate for each bond");
    _deliveryDates = deliveryDates;
    _cleanPrices = cleanPrices;
    _accruedInterest = accruedInterest;
    _repoRates = repoRates;
  }

  public List<Double> getDeliveryDates() {
    return _deliveryDates;
  }

  public List<Double> getCleanPrices() {
    return _cleanPrices;
  }

  public List<Double> getAccruedInterest() {
    return _accruedInterest;
  }

  public List<Double> getRepoRates() {
    return _repoRates;
  }

  public int getSize() {
    return _n;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _accruedInterest.hashCode();
    result = prime * result + _cleanPrices.hashCode();
    result = prime * result + _deliveryDates.hashCode();
    result = prime * result + _repoRates.hashCode();
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
    if (!ObjectUtils.equals(_accruedInterest, other._accruedInterest)) {
      return false;
    }
    if (!ObjectUtils.equals(_cleanPrices, other._cleanPrices)) {
      return false;
    }
    if (!ObjectUtils.equals(_deliveryDates, other._deliveryDates)) {
      return false;
    }
    return ObjectUtils.equals(_repoRates, other._repoRates);
  }

}
