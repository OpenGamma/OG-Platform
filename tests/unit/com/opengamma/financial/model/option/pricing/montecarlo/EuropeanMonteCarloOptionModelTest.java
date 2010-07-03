/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.montecarlo;

import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.financial.model.interestrate.curve.ConstantYieldCurve;
import com.opengamma.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.stochastic.BlackScholesGeometricBrownianMotionProcess;
import com.opengamma.financial.model.volatility.surface.ConstantVolatilitySurface;
import com.opengamma.math.random.NormalRandomNumberGenerator;
import com.opengamma.math.random.RandomNumberGenerator;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Expiry;

/**
 * 
 */
public class EuropeanMonteCarloOptionModelTest {
  private static final RandomNumberGenerator GENERATOR = new NormalRandomNumberGenerator(0, 1);
  private static final ZonedDateTime DATE = DateUtil.getUTCDate(2009, 1, 1);
  private static final Expiry EXPIRY = new Expiry(DateUtil.getDateOffsetWithYearFraction(DATE, 1));
  @SuppressWarnings("unused")
  private static final StandardOptionDataBundle DATA = new StandardOptionDataBundle(new ConstantYieldCurve(0.06), 0.06, new ConstantVolatilitySurface(0.2), 100., DATE);
  @SuppressWarnings("unused")
  private static final OptionDefinition CALL = new EuropeanVanillaOptionDefinition(110, EXPIRY, true);

  @Test
  public void test() {
    final EuropeanMonteCarloOptionModel model = new EuropeanMonteCarloOptionModel(200000, 100, new BlackScholesGeometricBrownianMotionProcess<OptionDefinition, StandardOptionDataBundle>(), GENERATOR);
    final EuropeanMonteCarloOptionModel model1 = new EuropeanMonteCarloOptionModel(200000, 100, new BlackScholesGeometricBrownianMotionProcess<OptionDefinition, StandardOptionDataBundle>(),
        GENERATOR, new AntitheticVariate<OptionDefinition, StandardOptionDataBundle>());
  }
}
