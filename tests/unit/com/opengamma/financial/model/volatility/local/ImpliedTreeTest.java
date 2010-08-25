/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.local;

import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.financial.model.interestrate.curve.ConstantYieldCurve;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.volatility.surface.FunctionalVolatilitySurface;
import com.opengamma.math.function.Function1D;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class ImpliedTreeTest {
  private static final double SPOT = 100;
  private static final YieldAndDiscountCurve R = new ConstantYieldCurve(0.05);
  private static final double B = 0.05;
  private static final double GLOBAL_VOL = 0.15;
  private static final ZonedDateTime DATE = DateUtil.getUTCDate(2010, 7, 1);
  private static final OptionDefinition OPTION = new EuropeanVanillaOptionDefinition(SPOT, new Expiry(DateUtil.getDateOffsetWithYearFraction(DATE, 5)), true);
  private static final Function1D<DoublesPair, Double> SMILE = new Function1D<DoublesPair, Double>() {

    @Override
    public Double evaluate(final DoublesPair pair) {
      final double k = pair.second;
      return GLOBAL_VOL - (k - SPOT) / 10 * 0.005;
    }

  };
  private static final StandardOptionDataBundle DATA = new StandardOptionDataBundle(R, B, new FunctionalVolatilitySurface(SMILE), SPOT, DATE);

  @Test
  public void test() {
    DermanKaniImpliedTreeModel.getTrees(OPTION, DATA);
  }
}
