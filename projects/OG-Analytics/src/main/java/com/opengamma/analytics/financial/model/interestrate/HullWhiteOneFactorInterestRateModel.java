/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate;

import org.apache.commons.lang.Validate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorDataBundle;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.util.time.DateUtils;

/**
 * 
 */
public class HullWhiteOneFactorInterestRateModel implements DiscountBondModel<HullWhiteOneFactorDataBundle> {
  private final double _delta = 0.1;

  @Override
  public Function1D<HullWhiteOneFactorDataBundle, Double> getDiscountBondFunction(final ZonedDateTime time, final ZonedDateTime maturity) {
    Validate.notNull(time);
    Validate.notNull(maturity);
    return new Function1D<HullWhiteOneFactorDataBundle, Double>() {

      @Override
      public Double evaluate(final HullWhiteOneFactorDataBundle data) {
        Validate.notNull(data);
        final double t = DateUtils.getDifferenceInYears(data.getDate(), time);
        final double s = DateUtils.getDifferenceInYears(data.getDate(), maturity);
        final double rT = data.getShortRate(t);
        final double rs = data.getShortRate(s);
        final double pT = Math.exp(-rT * t);
        final double ps = Math.exp(-rs * s);
        final Double sigma = data.getShortRateVolatility(t);
        final double dt = s - t;
        final double speed = data.getReversionSpeed();
        final double b = (1 - Math.exp(-speed * dt)) / speed;
        final double upT = t + _delta;
        final double downT = t - _delta;
        final double dlnPdt = (-data.getShortRate(upT) * upT + data.getShortRate(downT) * downT) / (2 * _delta);
        final double lnA = Math.log(ps / pT) - b * dlnPdt - sigma * sigma * Math.pow(Math.exp(-speed * s) - Math.exp(-speed * t), 2) * (Math.exp(2 * speed * t) - 1) / (4 * speed * speed * speed);
        return Math.exp(lnA - b * rT);
      }

    };
  }
}
