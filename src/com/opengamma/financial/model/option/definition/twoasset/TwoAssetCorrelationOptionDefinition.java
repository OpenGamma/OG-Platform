/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition.twoasset;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.option.definition.EuropeanExerciseFunction;
import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.OptionExerciseFunction;
import com.opengamma.financial.model.option.definition.OptionPayoffFunction;
import com.opengamma.util.time.Expiry;

/**
 * 
 */
public class TwoAssetCorrelationOptionDefinition extends OptionDefinition {
  private final OptionExerciseFunction<StandardTwoAssetOptionDataBundle> _exerciseFunction = new EuropeanExerciseFunction<StandardTwoAssetOptionDataBundle>();
  private final OptionPayoffFunction<StandardTwoAssetOptionDataBundle> _payoffFunction = new OptionPayoffFunction<StandardTwoAssetOptionDataBundle>() {

    @Override
    public double getPayoff(final StandardTwoAssetOptionDataBundle data, final Double optionPrice) {
      Validate.notNull(data, "data");
      final double s1 = data.getFirstSpot();
      final double s2 = data.getSecondSpot();
      final double k = getStrike();
      final double p = getPayout();
      if (isCall()) {
        return s1 > k ? Math.max(s2 - p, 0) : 0;
      }
      return s1 < k ? Math.max(p - s2, 0) : 0;
    }
  };
  private final double _payout;

  public TwoAssetCorrelationOptionDefinition(final double strike, final Expiry expiry, final boolean isCall, final double payout) {
    super(strike, expiry, isCall);
    _payout = payout;
  }

  @SuppressWarnings("unchecked")
  @Override
  public OptionExerciseFunction<StandardTwoAssetOptionDataBundle> getExerciseFunction() {
    return _exerciseFunction;
  }

  @SuppressWarnings("unchecked")
  @Override
  public OptionPayoffFunction<StandardTwoAssetOptionDataBundle> getPayoffFunction() {
    return _payoffFunction;
  }

  public double getPayout() {
    return _payout;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_payout);
    result = prime * result + (int) (temp ^ (temp >>> 32));
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
    if (getClass() != obj.getClass()) {
      return false;
    }
    final TwoAssetCorrelationOptionDefinition other = (TwoAssetCorrelationOptionDefinition) obj;
    if (Double.doubleToLongBits(_payout) != Double.doubleToLongBits(other._payout)) {
      return false;
    }
    return true;
  }

}
