/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition;

import java.util.Collections;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.greeks.Greek;
import com.opengamma.analytics.financial.model.option.pricing.analytic.AnalyticOptionModel;
import com.opengamma.analytics.financial.model.option.pricing.analytic.BlackScholesMertonModel;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;

/**
 * A complex chooser option gives the holder the right to choose whether the
 * option is to be a standard call or put after a certain time. The exercise
 * style of the option, once the choice has been made, is European.
 * <p>
 * The payoff from the option with strike <i>K</i> and spot <i>S</i> is
 * <i>max(c<sub>BSM</i>(S, K, T<sub>C</sub>), p<sub>BSM</sub>(S, K,
 * T<sub>P</sub>)</i>, where <i>C<sub>BSM</sub></i> is the Black-Scholes-Merton
 * call price, <i>P<sub>BSM</sub></i> is the Black-Scholes-Merton put price,
 * <i>T<sub>C</sub></i> is the time to maturity of the call and
 * <i>T<sub>P</sub></i> is the time to maturity of the put.
 * 
 */
public class ComplexChooserOptionDefinition extends OptionDefinition {
  private final OptionPayoffFunction<StandardOptionDataBundle> _payoffFunction = new OptionPayoffFunction<StandardOptionDataBundle>() {

    @SuppressWarnings("synthetic-access")
    @Override
    public double getPayoff(final StandardOptionDataBundle data, final Double optionPrice) {
      final double callPrice = BSM.getGreeks(getCallDefinition(), data, GREEKS).get(Greek.FAIR_PRICE);
      final double putPrice = BSM.getGreeks(getPutDefinition(), data, GREEKS).get(Greek.FAIR_PRICE);
      return Math.max(callPrice, putPrice);
    }
  };
  private final OptionExerciseFunction<StandardOptionDataBundle> _exerciseFunction = new EuropeanExerciseFunction<>();
  private final double _callStrike;
  private final double _putStrike;
  private final Expiry _callExpiry;
  private final Expiry _putExpiry;
  private final OptionDefinition _callDefinition;
  private final OptionDefinition _putDefinition;
  private static final AnalyticOptionModel<OptionDefinition, StandardOptionDataBundle> BSM = new BlackScholesMertonModel();
  private static final Set<Greek> GREEKS = Collections.singleton(Greek.FAIR_PRICE);

  /**
   * 
   * @param callStrike The strike of the potential call option
   * @param putStrike The strike of the potential put option
   * @param chooseDate The choice date (expiry) of the chooser option
   * @param callExpiry The expiry date of the potential call option
   * @param putExpiry The expiry date of the potential put option
   */
  public ComplexChooserOptionDefinition(final Expiry chooseDate, final double callStrike, final Expiry callExpiry, final double putStrike, final Expiry putExpiry) {
    super(null, chooseDate, null);
    Validate.notNull(callExpiry);
    Validate.notNull(putExpiry);
    ArgumentChecker.notNegative(callStrike, "call strike");
    ArgumentChecker.notNegative(putStrike, "put strike");
    if (callExpiry.getExpiry().isBefore(chooseDate.getExpiry())) {
      throw new IllegalArgumentException("Call expiry must be after the choose date");
    }
    if (putExpiry.getExpiry().isBefore(chooseDate.getExpiry())) {
      throw new IllegalArgumentException("Put expiry must be after the choose date");
    }
    _callStrike = callStrike;
    _putStrike = putStrike;
    _callExpiry = callExpiry;
    _putExpiry = putExpiry;
    _callDefinition = new EuropeanVanillaOptionDefinition(callStrike, callExpiry, true);
    _putDefinition = new EuropeanVanillaOptionDefinition(putStrike, putExpiry, false);
  }

  public double getCallStrike() {
    return _callStrike;
  }

  public double getPutStrike() {
    return _putStrike;
  }

  public Expiry getCallExpiry() {
    return _callExpiry;
  }

  public Expiry getPutExpiry() {
    return _putExpiry;
  }

  public double getTimeToCallExpiry(final ZonedDateTime date) {
    if (date.isAfter(getCallExpiry().getExpiry())) {
      throw new IllegalArgumentException("Date " + date + " is after call expiry " + getCallExpiry());
    }
    return DateUtils.getDifferenceInYears(date, getCallExpiry().getExpiry());
  }

  public double getTimeToPutExpiry(final ZonedDateTime date) {
    if (date.isAfter(getPutExpiry().getExpiry())) {
      throw new IllegalArgumentException("Date " + date + " is after put expiry " + getPutExpiry());
    }
    return DateUtils.getDifferenceInYears(date, getPutExpiry().getExpiry());
  }

  public OptionDefinition getCallDefinition() {
    return _callDefinition;
  }

  public OptionDefinition getPutDefinition() {
    return _putDefinition;
  }

  @Override
  public OptionExerciseFunction<StandardOptionDataBundle> getExerciseFunction() {
    return _exerciseFunction;
  }

  @Override
  public OptionPayoffFunction<StandardOptionDataBundle> getPayoffFunction() {
    return _payoffFunction;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((_callExpiry == null) ? 0 : _callExpiry.hashCode());
    long temp;
    temp = Double.doubleToLongBits(_callStrike);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + ((_putExpiry == null) ? 0 : _putExpiry.hashCode());
    temp = Double.doubleToLongBits(_putStrike);
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
    final ComplexChooserOptionDefinition other = (ComplexChooserOptionDefinition) obj;
    if (Double.doubleToLongBits(_callStrike) != Double.doubleToLongBits(other._callStrike)) {
      return false;
    }
    if (Double.doubleToLongBits(_putStrike) != Double.doubleToLongBits(other._putStrike)) {
      return false;
    }
    return ObjectUtils.equals(_callExpiry, other._callExpiry) && ObjectUtils.equals(_putExpiry, other._putExpiry);
  }

}
