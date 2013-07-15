/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition.twoasset;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.model.option.definition.EuropeanExerciseFunction;
import com.opengamma.analytics.financial.model.option.definition.OptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.OptionExerciseFunction;
import com.opengamma.analytics.financial.model.option.definition.OptionPayoffFunction;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.Expiry;

/**
 * Defines a European-style exchange-one-asset-for-another option. The holder
 * can exchange an amount $Q_1$ of the first underlying for an amount $Q_2$ of
 * the second underlying.
 * <p>
 * The payoff of this option is:
 * $$
 * \begin{eqnarray*}
 * max\left(Q_1S_1 - Q_2S_2, 0\right)
 * \end{eqnarray*}
 * $$
 * where $Q_1$ is the quantity of the first asset, $S_1$ is the spot price of
 * the first underlying, $Q_2$ is the quantity of the second asset and $S_2$ is
 * the spot price of the second underlying.
 */
public class EuropeanExchangeAssetOptionDefinition extends OptionDefinition {
  private final OptionExerciseFunction<StandardTwoAssetOptionDataBundle> _exerciseFunction = new EuropeanExerciseFunction<>();
  private final OptionPayoffFunction<StandardTwoAssetOptionDataBundle> _payoffFunction = new OptionPayoffFunction<StandardTwoAssetOptionDataBundle>() {

    @SuppressWarnings("synthetic-access")
    @Override
    public double getPayoff(final StandardTwoAssetOptionDataBundle data, final Double optionPrice) {
      Validate.notNull(data, "data");
      final double s1 = data.getFirstSpot();
      final double s2 = data.getSecondSpot();
      return Math.max(_firstQuantity * s1 - _secondQuantity * s2, 0);
    }

  };
  private final double _firstQuantity;
  private final double _secondQuantity;

  /**
   * 
   * @param expiry The expiry
   * @param firstQuantity The quantity of the first asset
   * @param secondQuantity The quantity of the second asset
   */
  public EuropeanExchangeAssetOptionDefinition(final Expiry expiry, final double firstQuantity, final double secondQuantity) {
    super(null, expiry, null);
    ArgumentChecker.notNegativeOrZero(firstQuantity, "quantity 1");
    ArgumentChecker.notNegativeOrZero(secondQuantity, "quantity 2");
    _firstQuantity = firstQuantity;
    _secondQuantity = secondQuantity;
  }

  /**
   * 
   * @return The quantity of the first asset
   */
  public double getFirstQuantity() {
    return _firstQuantity;
  }

  /**
   * 
   * @return The quantity of the second asset
   */
  public double getSecondQuantity() {
    return _secondQuantity;
  }

  /**
   * The exercise function of this option is European (see {@link EuropeanExerciseFunction})
   * @return The exercise function
   */
  @SuppressWarnings("unchecked")
  @Override
  public OptionExerciseFunction<StandardTwoAssetOptionDataBundle> getExerciseFunction() {
    return _exerciseFunction;
  }

  /**
   * @return The payoff function
   */
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
    final EuropeanExchangeAssetOptionDefinition other = (EuropeanExchangeAssetOptionDefinition) obj;
    if (Double.doubleToLongBits(_firstQuantity) != Double.doubleToLongBits(other._firstQuantity)) {
      return false;
    }
    if (Double.doubleToLongBits(_secondQuantity) != Double.doubleToLongBits(other._secondQuantity)) {
      return false;
    }
    return true;
  }

}
