/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.financial.model.interestrate.definition.VasicekDataBundle;
import com.opengamma.math.function.Function1D;
import com.opengamma.util.time.DateUtil;

/**
 * 
 * @author emcleod
 */
public class VasicekInterestRateModel {

  public Function1D<VasicekDataBundle, Double> getInterestRateFunction(final ZonedDateTime time, final ZonedDateTime maturity) {
    if (time == null)
      throw new IllegalArgumentException("Time was null");
    if (maturity == null)
      throw new IllegalArgumentException("Maturity was null");
    return new Function1D<VasicekDataBundle, Double>() {

      @Override
      public Double evaluate(final VasicekDataBundle data) {
        if (data == null)
          throw new IllegalArgumentException("Data bundle was null");
        final double r = data.getShortRate();
        final double lt = data.getLongTermInterestRate();
        final double speed = data.getReversionSpeed();
        final double sigma = data.getShortRateVolatility();
        final double dt = DateUtil.getDifferenceInYears(time, maturity);
        final double sigmaSq = sigma * sigma;
        final double speedSq = speed * speed;
        final double rInfinity = lt - 0.5 * sigmaSq / speedSq;
        final double factor = 1 - Math.exp(-speed * dt);
        final double a = rInfinity * (factor / speed - dt) - sigmaSq * factor * factor / (4 * speedSq * speed);
        final double b = factor / speed;
        return Math.exp(a - r * b);
      }

    };
  }
}
