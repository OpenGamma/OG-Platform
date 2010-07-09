/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.interestrate.definition.HoLeeDataBundle;
import com.opengamma.math.function.Function1D;
import com.opengamma.util.time.DateUtil;

/**
 * 
 */
public class HoLeeInterestRateModel {

  public Function1D<HoLeeDataBundle, Double> getInterestRateFunction(final ZonedDateTime time, final ZonedDateTime maturity) {
    Validate.notNull(time);
    Validate.notNull(maturity);
    return new Function1D<HoLeeDataBundle, Double>() {

      @Override
      public Double evaluate(final HoLeeDataBundle data) {
        Validate.notNull(data);
        final double t = DateUtil.getDifferenceInYears(data.getDate(), time);
        final double s = DateUtil.getDifferenceInYears(data.getDate(), maturity);
        final double b = s - t;
        final double sigma = data.getVolatility(t);
        final double rT = data.getInterestRate(t);
        final double rS = data.getInterestRate(s);
        final double pT = Math.exp(-rT * t);
        final double pS = Math.exp(-rS * s);
        final double dlnPdt = -rT;
        final double lnA = Math.log(pS / pT) - b * dlnPdt - 0.5 * sigma * sigma * b * b;
        return Math.exp(lnA - b * rT);
      }
    };
  }
}
