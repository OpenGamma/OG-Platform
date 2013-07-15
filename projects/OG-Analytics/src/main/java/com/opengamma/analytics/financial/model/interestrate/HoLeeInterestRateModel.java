/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate;

import org.apache.commons.lang.Validate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.model.interestrate.definition.StandardDiscountBondModelDataBundle;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.util.time.DateUtils;

/**
 * 
 */
public class HoLeeInterestRateModel implements DiscountBondModel<StandardDiscountBondModelDataBundle> {

  @Override
  public Function1D<StandardDiscountBondModelDataBundle, Double> getDiscountBondFunction(final ZonedDateTime time, final ZonedDateTime maturity) {
    Validate.notNull(time);
    Validate.notNull(maturity);
    return new Function1D<StandardDiscountBondModelDataBundle, Double>() {

      @Override
      public Double evaluate(final StandardDiscountBondModelDataBundle data) {
        Validate.notNull(data);
        final double t = DateUtils.getDifferenceInYears(data.getDate(), time);
        final double s = DateUtils.getDifferenceInYears(data.getDate(), maturity);
        final double b = s - t;
        final double sigma = data.getShortRateVolatility(t);
        final double rT = data.getShortRate(t);
        final double rS = data.getShortRate(s);
        final double pT = Math.exp(-rT * t);
        final double pS = Math.exp(-rS * s);
        final double dlnPdt = -rT;
        final double lnA = Math.log(pS / pT) - b * dlnPdt - 0.5 * sigma * sigma * b * b;
        return Math.exp(lnA - b * rT);
      }
    };
  }
}
