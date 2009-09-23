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
 * A chooser option gives the holder the right to choose whether the option is
 * to be a standard call or put after a certain time. The exercise style of the
 * option, once the choice has been made, is European.
 * <p>
 * The payoff from the option with time to maturity <i>T</i>, strike <i>K</i>,
 * spot <i>S</i> is <i>max(c<sub>BSM</i>(S, K, T), p<sub>BSM</sub>(S, K, T)</i>,
 * where <i>C<sub>BSM</sub></i> is the Black-Scholes-Merton call price and
 * <i>P<sub>BSM</sub></i> is the Black-Scholes-Merton put price.
 * 
 * @author emcleod
 */

public class ChooserOptionDefinition extends OptionDefinition<StandardOptionDataBundle> {
  private InstantProvider _chooseDate;
  protected EuropeanVanillaOptionDefinition _callDefinition;
  protected EuropeanVanillaOptionDefinition _putDefinition;
  protected final AnalyticOptionModel<EuropeanVanillaOptionDefinition, StandardOptionDataBundle> BSM = new BlackScholesMertonModel();
  protected final Greek PRICE = new Price();
  protected final List<Greek> GREEKS = Arrays.asList(new Greek[] { PRICE });

  /**
   * 
   * @param strike
   * @param expiry
   * @param chooseDate
   * @param vars
   */
  public ChooserOptionDefinition(double strike, Expiry expiry, InstantProvider chooseDate, StandardOptionDataBundle vars) {
    super(strike, expiry, null);
    if (chooseDate.toInstant().isAfter(expiry.toInstant()))
      throw new IllegalArgumentException("Option expiry must be after the choice date");
    _chooseDate = chooseDate;
    _callDefinition = new EuropeanVanillaOptionDefinition(strike, expiry, true);
    _putDefinition = new EuropeanVanillaOptionDefinition(strike, expiry, false);
  }

  @Override
  protected void initPayoffAndExerciseFunctions() {
    _payoffFunction = new Function1D<OptionDataBundleWithPrice<StandardOptionDataBundle>, Double>() {

      @Override
      public Double evaluate(OptionDataBundleWithPrice<StandardOptionDataBundle> data) {
        double callPrice = BSM.getGreeks(_callDefinition, data.getDataBundle(), GREEKS).get(PRICE).values().iterator().next();
        double putPrice = BSM.getGreeks(_putDefinition, data.getDataBundle(), GREEKS).get(PRICE).values().iterator().next();
        return Math.max(callPrice, putPrice);
      }

    };

    _exerciseFunction = new Function1D<OptionDataBundleWithPrice<StandardOptionDataBundle>, Boolean>() {

      @Override
      public Boolean evaluate(OptionDataBundleWithPrice<StandardOptionDataBundle> x) {
        return false;
      }

    };
  }

  /**
   * 
   * @return The date upon which the choice is made.
   */
  public InstantProvider getChooseDate() {
    return _chooseDate;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((BSM == null) ? 0 : BSM.hashCode());
    result = prime * result + ((GREEKS == null) ? 0 : GREEKS.hashCode());
    result = prime * result + ((PRICE == null) ? 0 : PRICE.hashCode());
    result = prime * result + ((_callDefinition == null) ? 0 : _callDefinition.hashCode());
    result = prime * result + ((_chooseDate == null) ? 0 : _chooseDate.hashCode());
    result = prime * result + ((_putDefinition == null) ? 0 : _putDefinition.hashCode());
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
    ChooserOptionDefinition other = (ChooserOptionDefinition) obj;
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
    return true;
  }
}
