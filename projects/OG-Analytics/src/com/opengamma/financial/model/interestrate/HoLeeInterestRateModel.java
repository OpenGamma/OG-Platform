/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.interestrate.definition.StandardDiscountBondModelDataBundle;
import com.opengamma.math.function.Function1D;
import com.opengamma.util.time.DateUtil;

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
        final double t = DateUtil.getDifferenceInYears(data.getDate(), time);
        final double s = DateUtil.getDifferenceInYears(data.getDate(), maturity);
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
