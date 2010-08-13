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
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.Expiry;

/**
 * 
 */
public class EuropeanExchangeAssetOptionDefinition extends OptionDefinition {
  private final OptionExerciseFunction<StandardTwoAssetOptionDataBundle> _exerciseFunction = new EuropeanExerciseFunction<StandardTwoAssetOptionDataBundle>();
  private final OptionPayoffFunction<StandardTwoAssetOptionDataBundle> _payoffFunction = new OptionPayoffFunction<StandardTwoAssetOptionDataBundle>() {

    @SuppressWarnings("synthetic-access")
    @Override
    public double getPayoff(StandardTwoAssetOptionDataBundle data, Double optionPrice) {
      Validate.notNull(data, "data");
      double s1 = data.getFirstSpot();
      double s2 = data.getSecondSpot();
      return Math.max(_firstQuantity * s1 - _secondQuantity * s2, 0);
    }

  };
  private final double _firstQuantity;
  private final double _secondQuantity;

  public EuropeanExchangeAssetOptionDefinition(Expiry expiry, double firstQuantity, double secondQuantity) {
    super(null, expiry, null);
    ArgumentChecker.notNegativeOrZero(firstQuantity, "quantity 1");
    ArgumentChecker.notNegativeOrZero(secondQuantity, "quantity 2");
    _firstQuantity = firstQuantity;
    _secondQuantity = secondQuantity;
  }

  public double getFirstQuantity() {
    return _firstQuantity;
  }

  public double getSecondQuantity() {
    return _secondQuantity;
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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_firstQuantity);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_secondQuantity);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    EuropeanExchangeAssetOptionDefinition other = (EuropeanExchangeAssetOptionDefinition) obj;
    if (Double.doubleToLongBits(_firstQuantity) != Double.doubleToLongBits(other._firstQuantity)) {
      return false;
    }
    if (Double.doubleToLongBits(_secondQuantity) != Double.doubleToLongBits(other._secondQuantity)) {
      return false;
    }
    return true;
  }

}
