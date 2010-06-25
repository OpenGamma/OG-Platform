/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.financial.model.interestrate.definition.HoLeeDataBundle;
import com.opengamma.math.function.Function1D;
import com.opengamma.util.time.DateUtil;

/**
 * 
 */
public class HoLeeInterestRateModel {
  private final double _delta = 0.1;

  public Function1D<HoLeeDataBundle, Double> getInterestRateFunction(final ZonedDateTime time,
      final ZonedDateTime maturity) {
    if (time == null) {
      throw new IllegalArgumentException("Time was null");
    }
    if (maturity == null) {
      throw new IllegalArgumentException("Maturity was null");
    }
    return new Function1D<HoLeeDataBundle, Double>() {

      @Override
      public Double evaluate(final HoLeeDataBundle data) {
        if (data == null) {
          throw new IllegalArgumentException("Data bundle was null");
        }
        final double t = DateUtil.getDifferenceInYears(data.getDate(), time);
        final double s = DateUtil.getDifferenceInYears(data.getDate(), maturity);
        final double b = s - t;
        final double sigma = data.getVolatility(t);
        final double rT = data.getInterestRate(t);
        final double rs = data.getInterestRate(s);
        final double pT = Math.exp(-rT * t);
        final double ps = Math.exp(-rs * s);
        final double upT = t + _delta;
        final double downT = t - _delta;
        final double dlnPdt = (-data.getInterestRate(upT) * upT + data.getInterestRate(downT) * downT) / (2 * _delta);
        final double lnA = Math.log(ps / pT) - b * dlnPdt - 0.5 * sigma * sigma * b * b;
        return Math.exp(lnA - b * rT);
      }
    };
  }
}
