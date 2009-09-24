/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition;

import java.util.Arrays;
import java.util.List;

import javax.time.InstantProvider;

import com.opengamma.financial.greeks.Greek;
import com.opengamma.financial.greeks.Price;
import com.opengamma.financial.model.option.pricing.analytic.AnalyticOptionModel;
import com.opengamma.financial.model.option.pricing.analytic.BlackScholesMertonModel;
import com.opengamma.math.function.Function1D;
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
 * @author emcleod
 */
public class ComplexChooserOptionDefinition extends OptionDefinition<StandardOptionDataBundle> {
  private final double _callStrike;
  private final double _putStrike;
  private final InstantProvider _chooseDate;
  private final Expiry _callExpiry;
  private final Expiry _putExpiry;
  protected final EuropeanVanillaOptionDefinition _callDefinition;
  protected final EuropeanVanillaOptionDefinition _putDefinition;
  protected final AnalyticOptionModel<EuropeanVanillaOptionDefinition, StandardOptionDataBundle> BSM = new BlackScholesMertonModel();
  protected final Greek PRICE = new Price();
  protected final List<Greek> GREEKS = Arrays.asList(new Greek[] { PRICE });

  /**
   * 
   * @param callStrike
   * @param putStrike
   * @param expiry
   * @param chooseDate
   * @param callExpiry
   * @param putExpiry
   */
  public ComplexChooserOptionDefinition(double callStrike, double putStrike, Expiry expiry, InstantProvider chooseDate, Expiry callExpiry, Expiry putExpiry) {
    super(null, expiry, null);
    if (chooseDate.toInstant().isAfter(expiry.toInstant()))
      throw new IllegalArgumentException("Option expiry must be after the choice date");
    _chooseDate = chooseDate;
    _callStrike = callStrike;
    _putStrike = putStrike;
    _callExpiry = callExpiry;
    _putExpiry = putExpiry;
    _callDefinition = new EuropeanVanillaOptionDefinition(callStrike, callExpiry, true);
    _putDefinition = new EuropeanVanillaOptionDefinition(putStrike, putExpiry, false);
  }

  @Override
  protected void initPayoffAndExerciseFunctions() {
    _payoffFunction = new Function1D<StandardOptionDataBundle, Double>() {

      @Override
      public Double evaluate(StandardOptionDataBundle data) {
        final double callPrice = BSM.getGreeks(_callDefinition, data, GREEKS).get(PRICE).values().iterator().next();
        final double putPrice = BSM.getGreeks(_putDefinition, data, GREEKS).get(PRICE).values().iterator().next();
        return Math.max(callPrice, putPrice);
      }

    };

    _exerciseFunction = new Function1D<StandardOptionDataBundle, Boolean>() {

      @Override
      public Boolean evaluate(StandardOptionDataBundle data) {
        return false;
      }

    };
  }

  public double getCallStrike() {
    return _callStrike;
  }

  public double getPutStrike() {
    return _putStrike;
  }

  public InstantProvider getChooseDate() {
    return _chooseDate;
  }

  public Expiry getCallExpiry() {
    return _callExpiry;
  }

  public Expiry getPutExpiry() {
    return _putExpiry;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((BSM == null) ? 0 : BSM.hashCode());
    result = prime * result + ((GREEKS == null) ? 0 : GREEKS.hashCode());
    result = prime * result + ((PRICE == null) ? 0 : PRICE.hashCode());
    result = prime * result + ((_callDefinition == null) ? 0 : _callDefinition.hashCode());
    result = prime * result + ((_callExpiry == null) ? 0 : _callExpiry.hashCode());
    long temp;
    temp = Double.doubleToLongBits(_callStrike);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + ((_chooseDate == null) ? 0 : _chooseDate.hashCode());
    result = prime * result + ((_putDefinition == null) ? 0 : _putDefinition.hashCode());
    result = prime * result + ((_putExpiry == null) ? 0 : _putExpiry.hashCode());
    temp = Double.doubleToLongBits(_putStrike);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    ComplexChooserOptionDefinition other = (ComplexChooserOptionDefinition) obj;
    if (BSM == null) {
      if (other.BSM != null)
        return false;
    } else if (!BSM.equals(other.BSM))
      return false;
    if (GREEKS == null) {
      if (other.GREEKS != null)
        return false;
    } else if (!GREEKS.equals(other.GREEKS))
      return false;
    if (PRICE == null) {
      if (other.PRICE != null)
        return false;
    } else if (!PRICE.equals(other.PRICE))
      return false;
    if (_callDefinition == null) {
      if (other._callDefinition != null)
        return false;
    } else if (!_callDefinition.equals(other._callDefinition))
      return false;
    if (_callExpiry == null) {
      if (other._callExpiry != null)
        return false;
    } else if (!_callExpiry.equals(other._callExpiry))
      return false;
    if (Double.doubleToLongBits(_callStrike) != Double.doubleToLongBits(other._callStrike))
      return false;
    if (_chooseDate == null) {
      if (other._chooseDate != null)
        return false;
    } else if (!_chooseDate.equals(other._chooseDate))
      return false;
    if (_putDefinition == null) {
      if (other._putDefinition != null)
        return false;
    } else if (!_putDefinition.equals(other._putDefinition))
      return false;
    if (_putExpiry == null) {
      if (other._putExpiry != null)
        return false;
    } else if (!_putExpiry.equals(other._putExpiry))
      return false;
    if (Double.doubleToLongBits(_putStrike) != Double.doubleToLongBits(other._putStrike))
      return false;
    return true;
  }

}
