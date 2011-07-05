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
  private static final Barrier BARRIER_DOWN_IN = new Barrier(KnockType.IN, BarrierType.DOWN, ObservationType.CONTINUOUS, 90);
  private static final Barrier BARRIER_DOWN_OUT = new Barrier(KnockType.OUT, BarrierType.DOWN, ObservationType.CONTINUOUS, 90);
  private static final Barrier BARRIER_UP_IN = new Barrier(KnockType.IN, BarrierType.UP, ObservationType.CONTINUOUS, 110);
  private static final Barrier BARRIER_UP_OUT = new Barrier(KnockType.OUT, BarrierType.UP, ObservationType.CONTINUOUS, 110);
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
    final AnalyticOptionModel<EuropeanStandardBarrierOptionDefinition, StandardOptionDataBundle> model = new EuropeanStandardBarrierOptionModel();
    final StandardOptionDataBundle data = new StandardOptionDataBundle(new YieldCurve(ConstantDoublesCurve.from(RATE)), COST_OF_CARRY, new VolatilitySurface(ConstantDoublesSurface.from(VOLATILITY)),
        SPOT, REFERENCE_DATE);
    Expiry expiry = new Expiry(EXPIRY_DATE);

    double priceDI1 = BARRIER_FUNCTION.getPrice(OPTION_VANILLA, BARRIER_DOWN_IN, REBATE, SPOT, COST_OF_CARRY, RATE, VOLATILITY);
    EuropeanStandardBarrierOptionDefinition optionBarrierDI = new EuropeanStandardBarrierOptionDefinition(STRIKE, expiry, IS_CALL, BARRIER_DOWN_IN, REBATE);
    double priceDI2 = model.getPricingFunction(optionBarrierDI).evaluate(data);
    assertEquals("Comparison Down In", priceDI2, priceDI1, 1.0E-10);

    double priceDO1 = BARRIER_FUNCTION.getPrice(OPTION_VANILLA, BARRIER_DOWN_OUT, REBATE, SPOT, COST_OF_CARRY, RATE, VOLATILITY);
    EuropeanStandardBarrierOptionDefinition optionBarrierDO = new EuropeanStandardBarrierOptionDefinition(STRIKE, expiry, IS_CALL, BARRIER_DOWN_OUT, REBATE);
    double priceDO2 = model.getPricingFunction(optionBarrierDO).evaluate(data);
    assertEquals("Comparison Down Out", priceDO2, priceDO1, 1.0E-10);

    double priceUI1 = BARRIER_FUNCTION.getPrice(OPTION_VANILLA, BARRIER_UP_IN, REBATE, SPOT, COST_OF_CARRY, RATE, VOLATILITY);
    EuropeanStandardBarrierOptionDefinition optionBarrierUI = new EuropeanStandardBarrierOptionDefinition(STRIKE, expiry, IS_CALL, BARRIER_UP_IN, REBATE);
    double priceUI2 = model.getPricingFunction(optionBarrierUI).evaluate(data);
    assertEquals("Comparison Up In", priceUI2, priceUI1, 1.0E-10);

    double priceUO1 = BARRIER_FUNCTION.getPrice(OPTION_VANILLA, BARRIER_UP_OUT, REBATE, SPOT, COST_OF_CARRY, RATE, VOLATILITY);
    EuropeanStandardBarrierOptionDefinition optionBarrierUO = new EuropeanStandardBarrierOptionDefinition(STRIKE, expiry, IS_CALL, BARRIER_UP_OUT, REBATE);
    double priceUO2 = model.getPricingFunction(optionBarrierUO).evaluate(data);
    assertEquals("Comparison Up Out", priceUO2, priceUO1, 1.0E-10);

    double vol0 = 0.0;
    double priceVol01 = BARRIER_FUNCTION.getPrice(OPTION_VANILLA, BARRIER_DOWN_IN, REBATE, SPOT, COST_OF_CARRY, RATE, vol0);
    final StandardOptionDataBundle data0 = new StandardOptionDataBundle(new YieldCurve(ConstantDoublesCurve.from(RATE)), COST_OF_CARRY, new VolatilitySurface(ConstantDoublesSurface.from(vol0)), SPOT,
        REFERENCE_DATE);
    double priceVol02 = model.getPricingFunction(optionBarrierDI).evaluate(data0);
    assertEquals(priceVol02, priceVol01, 1.0E-10);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void exceptionDown() {
    BARRIER_FUNCTION.getPrice(OPTION_VANILLA, BARRIER_DOWN_IN, REBATE, 85.0, COST_OF_CARRY, RATE, VOLATILITY);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void exceptionUp() {
    Barrier barrierUp = new Barrier(KnockType.IN, BarrierType.UP, ObservationType.CONTINUOUS, 90);
    BARRIER_FUNCTION.getPrice(OPTION_VANILLA, barrierUp, REBATE, SPOT, COST_OF_CARRY, RATE, VOLATILITY);
  }

  @Test
  /**
   * Tests the adjoint implementation (with computation of the derivatives).
   */
  public void adjointPrice() {
    double[] derivatives = new double[5];
    double priceDI = BARRIER_FUNCTION.getPrice(OPTION_VANILLA, BARRIER_DOWN_IN, REBATE, SPOT, COST_OF_CARRY, RATE, VOLATILITY);
    double priceDIAdjoint = BARRIER_FUNCTION.getPriceAdjoint(OPTION_VANILLA, BARRIER_DOWN_IN, REBATE, SPOT, COST_OF_CARRY, RATE, VOLATILITY, derivatives);
    assertEquals("Black single barrier: Adjoint price Down In", priceDI, priceDIAdjoint, 1.0E-10);
    double priceDO = BARRIER_FUNCTION.getPrice(OPTION_VANILLA, BARRIER_DOWN_OUT, REBATE, SPOT, COST_OF_CARRY, RATE, VOLATILITY);
    double priceDOAdjoint = BARRIER_FUNCTION.getPriceAdjoint(OPTION_VANILLA, BARRIER_DOWN_OUT, REBATE, SPOT, COST_OF_CARRY, RATE, VOLATILITY, derivatives);
    assertEquals("Black single barrier: Adjoint price Down Out", priceDO, priceDOAdjoint, 1.0E-10);
    double priceUI = BARRIER_FUNCTION.getPrice(OPTION_VANILLA, BARRIER_UP_IN, REBATE, SPOT, COST_OF_CARRY, RATE, VOLATILITY);
    double priceUIAdjoint = BARRIER_FUNCTION.getPriceAdjoint(OPTION_VANILLA, BARRIER_UP_IN, REBATE, SPOT, COST_OF_CARRY, RATE, VOLATILITY, derivatives);
    assertEquals("Black single barrier: Adjoint price Up In", priceUI, priceUIAdjoint, 1.0E-10);
    double priceUO = BARRIER_FUNCTION.getPrice(OPTION_VANILLA, BARRIER_UP_OUT, REBATE, SPOT, COST_OF_CARRY, RATE, VOLATILITY);
    double priceUOAdjoint = BARRIER_FUNCTION.getPriceAdjoint(OPTION_VANILLA, BARRIER_UP_OUT, REBATE, SPOT, COST_OF_CARRY, RATE, VOLATILITY, derivatives);
    assertEquals("Black single barrier: Adjoint price Up Out", priceUO, priceUOAdjoint, 1.0E-10);
  }

  @Test
  /**
   * Tests the adjoint implementation (with computation of the derivatives).
   */
  public void adjointDerivatives() {
    double shiftSpot = 0.01;
    double shiftRate = 1.0E-6;
    double shiftCoC = 1.0E-6;
    double shiftVol = 1.0E-6;
    double[] derivatives = new double[5];
    // DOWN-IN
    double priceDI = BARRIER_FUNCTION.getPriceAdjoint(OPTION_VANILLA, BARRIER_DOWN_IN, REBATE, SPOT, COST_OF_CARRY, RATE, VOLATILITY, derivatives);
    double priceDISpot = BARRIER_FUNCTION.getPrice(OPTION_VANILLA, BARRIER_DOWN_IN, REBATE, SPOT + shiftSpot, COST_OF_CARRY, RATE, VOLATILITY);
    assertEquals("Black single barrier: Adjoint spot derivative - Down In", (priceDISpot - priceDI) / shiftSpot, derivatives[0], 1.0E-5);
    double priceDIRate = BARRIER_FUNCTION.getPrice(OPTION_VANILLA, BARRIER_DOWN_IN, REBATE, SPOT, COST_OF_CARRY, RATE + shiftRate, VOLATILITY);
    assertEquals("Black single barrier: Adjoint rate derivative - Down In", (priceDIRate - priceDI) / shiftRate, derivatives[2], 1.0E-5);
    double priceDICoC = BARRIER_FUNCTION.getPrice(OPTION_VANILLA, BARRIER_DOWN_IN, REBATE, SPOT, COST_OF_CARRY + shiftCoC, RATE, VOLATILITY);
    assertEquals("Black single barrier: Adjoint cost-of-carry derivative - Down In", (priceDICoC - priceDI) / shiftCoC, derivatives[3], 1.0E-5);
    double priceDIVol = BARRIER_FUNCTION.getPrice(OPTION_VANILLA, BARRIER_DOWN_IN, REBATE, SPOT, COST_OF_CARRY, RATE, VOLATILITY + shiftVol);
    assertEquals("Black single barrier: Adjoint cost-of-carry derivative - Down In", (priceDIVol - priceDI) / shiftVol, derivatives[4], 1.0E-4);
    // DOWN-OUT
    double priceDO = BARRIER_FUNCTION.getPriceAdjoint(OPTION_VANILLA, BARRIER_DOWN_OUT, REBATE, SPOT, COST_OF_CARRY, RATE, VOLATILITY, derivatives);
    double priceDOSpot = BARRIER_FUNCTION.getPrice(OPTION_VANILLA, BARRIER_DOWN_OUT, REBATE, SPOT + shiftSpot, COST_OF_CARRY, RATE, VOLATILITY);
    assertEquals("Black single barrier: Adjoint spot derivative - Down Out", (priceDOSpot - priceDO) / shiftSpot, derivatives[0], 2.0E-4);
    double priceDORate = BARRIER_FUNCTION.getPrice(OPTION_VANILLA, BARRIER_DOWN_OUT, REBATE, SPOT, COST_OF_CARRY, RATE + shiftRate, VOLATILITY);
    assertEquals("Black single barrier: Adjoint rate derivative - Down Out", (priceDORate - priceDO) / shiftRate, derivatives[2], 1.0E-5);
    double priceDOCoC = BARRIER_FUNCTION.getPrice(OPTION_VANILLA, BARRIER_DOWN_OUT, REBATE, SPOT, COST_OF_CARRY + shiftCoC, RATE, VOLATILITY);
    assertEquals("Black single barrier: Adjoint cost-of-carry derivative - Down Out", (priceDOCoC - priceDO) / shiftCoC, derivatives[3], 1.0E-4);
    double priceDOVol = BARRIER_FUNCTION.getPrice(OPTION_VANILLA, BARRIER_DOWN_OUT, REBATE, SPOT, COST_OF_CARRY, RATE, VOLATILITY + shiftVol);
    assertEquals("Black single barrier: Adjoint cost-of-carry derivative - Down Out", (priceDOVol - priceDO) / shiftVol, derivatives[4], 1.0E-4);
    // UP-IN
    double priceUI = BARRIER_FUNCTION.getPriceAdjoint(OPTION_VANILLA, BARRIER_UP_IN, REBATE, SPOT, COST_OF_CARRY, RATE, VOLATILITY, derivatives);
    double priceUISpot = BARRIER_FUNCTION.getPrice(OPTION_VANILLA, BARRIER_UP_IN, REBATE, SPOT + shiftSpot, COST_OF_CARRY, RATE, VOLATILITY);
    assertEquals("Black single barrier: Adjoint spot derivative - Up In", (priceUISpot - priceUI) / shiftSpot, derivatives[0], 2.0E-4);
    double priceUIRate = BARRIER_FUNCTION.getPrice(OPTION_VANILLA, BARRIER_UP_IN, REBATE, SPOT, COST_OF_CARRY, RATE + shiftRate, VOLATILITY);
    assertEquals("Black single barrier: Adjoint rate derivative - Up In", (priceUIRate - priceUI) / shiftRate, derivatives[2], 1.0E-5);
    double priceUICoC = BARRIER_FUNCTION.getPrice(OPTION_VANILLA, BARRIER_UP_IN, REBATE, SPOT, COST_OF_CARRY + shiftCoC, RATE, VOLATILITY);
    assertEquals("Black single barrier: Adjoint cost-of-carry derivative - Up In", (priceUICoC - priceUI) / shiftCoC, derivatives[3], 1.0E-4);
    double priceUIVol = BARRIER_FUNCTION.getPrice(OPTION_VANILLA, BARRIER_UP_IN, REBATE, SPOT, COST_OF_CARRY, RATE, VOLATILITY + shiftVol);
    assertEquals("Black single barrier: Adjoint cost-of-carry derivative - Up In", (priceUIVol - priceUI) / shiftVol, derivatives[4], 1.0E-5);
    // UP-OUT
    double priceUO = BARRIER_FUNCTION.getPriceAdjoint(OPTION_VANILLA, BARRIER_UP_OUT, REBATE, SPOT, COST_OF_CARRY, RATE, VOLATILITY, derivatives);
    double priceUOSpot = BARRIER_FUNCTION.getPrice(OPTION_VANILLA, BARRIER_UP_OUT, REBATE, SPOT + shiftSpot, COST_OF_CARRY, RATE, VOLATILITY);
    assertEquals("Black single barrier: Adjoint spot derivative - Up Out", (priceUOSpot - priceUO) / shiftSpot, derivatives[0], 1.0E-4);
    double priceUORate = BARRIER_FUNCTION.getPrice(OPTION_VANILLA, BARRIER_UP_OUT, REBATE, SPOT, COST_OF_CARRY, RATE + shiftRate, VOLATILITY);
    assertEquals("Black single barrier: Adjoint rate derivative - Up Out", (priceUORate - priceUO) / shiftRate, derivatives[2], 1.0E-5);
    double priceUOCoC = BARRIER_FUNCTION.getPrice(OPTION_VANILLA, BARRIER_UP_OUT, REBATE, SPOT, COST_OF_CARRY + shiftCoC, RATE, VOLATILITY);
    assertEquals("Black single barrier: Adjoint cost-of-carry derivative - Up Out", (priceUOCoC - priceUO) / shiftCoC, derivatives[3], 1.0E-5);
    double priceUOVol = BARRIER_FUNCTION.getPrice(OPTION_VANILLA, BARRIER_UP_OUT, REBATE, SPOT, COST_OF_CARRY, RATE, VOLATILITY + shiftVol);
    assertEquals("Black single barrier: Adjoint cost-of-carry derivative - Up Out", (priceUOVol - priceUO) / shiftVol, derivatives[4], 2.0E-5);
  }

}
