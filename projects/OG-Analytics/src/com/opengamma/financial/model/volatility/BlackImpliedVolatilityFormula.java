/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;

/**
 * 
 */
public class BlackImpliedVolatilityFormula {

  public double getImpliedVolatility(final BlackFunctionData data, final EuropeanVanillaOption option, final double optionPrice) {
    Validate.notNull(data, "null data");
    Validate.notNull(option, "null option");

    final double discountFactor = data.getDiscountFactor();
    final boolean isCall = option.isCall();
    final double f = data.getForward();
    final double k = option.getStrike();
    final double t = option.getTimeToExpiry();
    final double fwdPrice = optionPrice / discountFactor;

    return BlackFormulaRepository.impliedVolatility(fwdPrice, f, k, t, isCall);

  }

}
