/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic.formula;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.financial.model.option.definition.Barrier;
import com.opengamma.financial.model.option.definition.Barrier.BarrierType;
import com.opengamma.financial.model.option.definition.Barrier.KnockType;
import com.opengamma.financial.model.option.definition.Barrier.ObservationType;
import com.opengamma.financial.model.option.definition.EuropeanStandardBarrierOptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.option.pricing.analytic.AnalyticOptionModel;
import com.opengamma.financial.model.option.pricing.analytic.EuropeanStandardBarrierOptionModel;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.math.curve.ConstantDoublesCurve;
import com.opengamma.math.surface.ConstantDoublesSurface;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Expiry;

public class BlackBarrierPriceFunctionTest {
  private static final ZonedDateTime REFERENCE_DATE = DateUtil.getUTCDate(2011, 7, 1);
  private static final ZonedDateTime EXPIRY_DATE = DateUtil.getUTCDate(2012, 1, 2);
  private static final double EXPIRY_TIME = DateUtil.getDifferenceInYears(REFERENCE_DATE, EXPIRY_DATE);
  private static final double STRIKE = 100;
  private static final boolean IS_CALL = true;
  private static final EuropeanVanillaOption OPTION_VANILLA = new EuropeanVanillaOption(STRIKE, EXPIRY_TIME, IS_CALL);
  private static final Barrier BARRIER = new Barrier(KnockType.IN, BarrierType.DOWN, ObservationType.CONTINUOUS, 90);
  private static final double REBATE = 2;
  private static final double SPOT = 105;
  private static final double RATE = 0.05;
  private static final double COST_OF_CARRY = 0.03;
  private static final double VOLATILITY = 0.20;
  private static final BlackBarrierPriceFunction BARRIER_FUNCTION = new BlackBarrierPriceFunction();

  @Test
  /**
   * Tests the comparison with the other implementation. This test may be removed when only one version remains.
   */
  public void comparison() {
    double price1 = BARRIER_FUNCTION.getPrice(OPTION_VANILLA, BARRIER, REBATE, SPOT, COST_OF_CARRY, RATE, VOLATILITY);
    final StandardOptionDataBundle data = new StandardOptionDataBundle(new YieldCurve(ConstantDoublesCurve.from(RATE)), COST_OF_CARRY, new VolatilitySurface(ConstantDoublesSurface.from(VOLATILITY)),
        SPOT, REFERENCE_DATE);
    Expiry expiry = new Expiry(EXPIRY_DATE);
    EuropeanStandardBarrierOptionDefinition optionBarrier = new EuropeanStandardBarrierOptionDefinition(STRIKE, expiry, IS_CALL, BARRIER, REBATE);
    final AnalyticOptionModel<EuropeanStandardBarrierOptionDefinition, StandardOptionDataBundle> MODEL = new EuropeanStandardBarrierOptionModel();
    double price2 = MODEL.getPricingFunction(optionBarrier).evaluate(data);
    assertEquals(price2, price1, 1.0E-10);
    double vol0 = 0.0;
    double priceVol01 = BARRIER_FUNCTION.getPrice(OPTION_VANILLA, BARRIER, REBATE, SPOT, COST_OF_CARRY, RATE, vol0);
    final StandardOptionDataBundle data0 = new StandardOptionDataBundle(new YieldCurve(ConstantDoublesCurve.from(RATE)), COST_OF_CARRY, new VolatilitySurface(ConstantDoublesSurface.from(vol0)), SPOT,
        REFERENCE_DATE);
    double priceVol02 = MODEL.getPricingFunction(optionBarrier).evaluate(data0);
    assertEquals(priceVol02, priceVol01, 1.0E-10);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void exceptionDown() {
    BARRIER_FUNCTION.getPrice(OPTION_VANILLA, BARRIER, REBATE, 85.0, COST_OF_CARRY, RATE, VOLATILITY);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void exceptionUp() {
    Barrier barrierUp = new Barrier(KnockType.IN, BarrierType.UP, ObservationType.CONTINUOUS, 90);
    BARRIER_FUNCTION.getPrice(OPTION_VANILLA, barrierUp, REBATE, SPOT, COST_OF_CARRY, RATE, VOLATILITY);
  }

}
