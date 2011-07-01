/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.forex.method;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.forex.calculator.ForexDerivative;
import com.opengamma.financial.forex.definition.ForexDefinition;
import com.opengamma.financial.forex.definition.ForexOptionVanillaDefinition;
import com.opengamma.financial.forex.derivative.ForexOptionSingleBarrier;
import com.opengamma.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.option.definition.Barrier;
import com.opengamma.financial.model.option.definition.Barrier.BarrierType;
import com.opengamma.financial.model.option.definition.Barrier.KnockType;
import com.opengamma.financial.model.option.definition.Barrier.ObservationType;
import com.opengamma.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.financial.model.option.definition.SmileDeltaTermStructureParameter;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackBarrierPriceFunction;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.tuple.Triple;

/**
 * Tests related to the Black world pricing method for single barrier Forex option.
 */
public class ForexOptionSingleBarrierBlackMethodTest {
  // General
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final int SETTLEMENT_DAYS = 2;
  private static final DayCount ACT_ACT = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
  // Smile data
  private static final Currency CUR_1 = Currency.EUR;
  private static final Currency CUR_2 = Currency.USD;
  private static final Period[] EXPIRY_PERIOD = new Period[] {Period.ofMonths(3), Period.ofMonths(6), Period.ofYears(1), Period.ofYears(2)};
  private static final int NB_EXP = EXPIRY_PERIOD.length;
  private static final ZonedDateTime REFERENCE_DATE = DateUtil.getUTCDate(2011, 6, 13);
  private static final ZonedDateTime REFERENCE_SPOT = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, CALENDAR, SETTLEMENT_DAYS);
  private static final ZonedDateTime[] PAY_DATE = new ZonedDateTime[NB_EXP];
  private static final ZonedDateTime[] EXPIRY_DATE = new ZonedDateTime[NB_EXP];
  private static final double[] TIME_TO_EXPIRY = new double[NB_EXP + 1];
  static {
    TIME_TO_EXPIRY[0] = 0.0;
    for (int loopexp = 0; loopexp < NB_EXP; loopexp++) {
      PAY_DATE[loopexp] = ScheduleCalculator.getAdjustedDate(REFERENCE_SPOT, BUSINESS_DAY, CALENDAR, EXPIRY_PERIOD[loopexp]);
      EXPIRY_DATE[loopexp] = ScheduleCalculator.getAdjustedDate(PAY_DATE[loopexp], CALENDAR, -SETTLEMENT_DAYS);
      TIME_TO_EXPIRY[loopexp + 1] = ACT_ACT.getDayCountFraction(REFERENCE_DATE, EXPIRY_DATE[loopexp]);
    }
  }
  private static final double SPOT = 1.40;
  private static final double[] ATM = {0.175, 0.185, 0.18, 0.17, 0.16};
  private static final double[] DELTA = new double[] {0.10, 0.25};
  private static final double[][] RISK_REVERSAL = new double[][] { {-0.010, -0.0050}, {-0.011, -0.0060}, {-0.012, -0.0070}, {-0.013, -0.0080}, {-0.014, -0.0090}};
  private static final double[][] STRANGLE = new double[][] { {0.0300, 0.0100}, {0.0310, 0.0110}, {0.0320, 0.0120}, {0.0330, 0.0130}, {0.0340, 0.0140}};
  private static final SmileDeltaTermStructureParameter SMILE_TERM = new SmileDeltaTermStructureParameter(TIME_TO_EXPIRY, DELTA, ATM, RISK_REVERSAL, STRANGLE);
  // Methods and curves
  private static final YieldCurveBundle CURVES = ForexTestsDataSets.createCurvesForex();
  private static final String[] CURVES_NAME = CURVES.getAllNames().toArray(new String[0]);
  private static final SmileDeltaTermStructureDataBundle SMILE_BUNDLE = new SmileDeltaTermStructureDataBundle(SMILE_TERM, SPOT, CURVES);
  private static final ForexOptionVanillaMethod METHOD_VANILLA = new ForexOptionVanillaMethod();
  private static final ForexOptionSingleBarrierBlackMethod METHOD_BARRIER = new ForexOptionSingleBarrierBlackMethod();
  private static final BlackBarrierPriceFunction BLACK_BARRIER_FUNCTION = new BlackBarrierPriceFunction();
  // Option
  private static final double STRIKE = 1.45;
  private static final boolean IS_CALL = true;
  private static final boolean IS_LONG = true;
  private static final double NOTIONAL = 100000000;
  private static final Barrier BARRIER = new Barrier(KnockType.IN, BarrierType.DOWN, ObservationType.CLOSE, 1.35);
  private static final double REBATE = 50000;
  private static final ZonedDateTime OPTION_PAY_DATE = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, BUSINESS_DAY, CALENDAR, Period.ofMonths(9));
  private static final ZonedDateTime OPTION_EXPIRY_DATE = ScheduleCalculator.getAdjustedDate(OPTION_PAY_DATE, CALENDAR, -SETTLEMENT_DAYS);
  private static final ForexDefinition FOREX_DEFINITION = new ForexDefinition(CUR_1, CUR_2, OPTION_PAY_DATE, NOTIONAL, STRIKE);
  private static final ForexOptionVanillaDefinition VANILLA_OPTION_DEFINITION = new ForexOptionVanillaDefinition(FOREX_DEFINITION, OPTION_EXPIRY_DATE, IS_CALL, IS_LONG);
  private static final ForexOptionVanilla OPTION_VANILLA = VANILLA_OPTION_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final ForexOptionSingleBarrier OPTION_BARRIER = new ForexOptionSingleBarrier(OPTION_VANILLA, BARRIER, REBATE);

  @Test
  /**
   * Comparison with the underlying vanilla option (the vanilla option is more expensive).
   */
  public void comparisonVanilla() {
    MultipleCurrencyAmount priceVanilla = METHOD_VANILLA.presentValue(OPTION_VANILLA, SMILE_BUNDLE);
    MultipleCurrencyAmount priceBarrier = METHOD_BARRIER.presentValue(OPTION_BARRIER, SMILE_BUNDLE);
    assertTrue("Barriers are cheaper than vanilla", priceVanilla.getAmount(CUR_2) > priceBarrier.getAmount(CUR_2));
  }

  @Test
  /**
   * Tests present value with a direct computation.
   */
  public void presentValue() {
    MultipleCurrencyAmount priceMethod = METHOD_BARRIER.presentValue(OPTION_BARRIER, SMILE_BUNDLE);
    double payTime = OPTION_VANILLA.getUnderlyingForex().getPaymentTime();
    double rateDomestic = CURVES.getCurve(CURVES_NAME[1]).getInterestRate(payTime);
    double rateForeign = CURVES.getCurve(CURVES_NAME[0]).getInterestRate(payTime);
    double forward = SPOT * Math.exp(-rateForeign * payTime) / Math.exp(-rateDomestic * payTime);
    double volatility = SMILE_TERM.getVolatility(new Triple<Double, Double, Double>(OPTION_VANILLA.getTimeToExpiry(), STRIKE, forward));
    double priceComputed = BLACK_BARRIER_FUNCTION.getPrice(OPTION_VANILLA, BARRIER, REBATE / NOTIONAL, SPOT, rateForeign, rateDomestic, volatility) * NOTIONAL;
    assertEquals("Barriers present value", priceComputed, priceMethod.getAmount(CUR_2), 1.0E-2);
  }

  @Test
  /**
   * Test the price scaling and the long/short parity.
   */
  public void scaleLongShortParity() {
    MultipleCurrencyAmount priceBarrier = METHOD_BARRIER.presentValue(OPTION_BARRIER, SMILE_BUNDLE);
    double scale = 10;
    final ForexDefinition fxDefinitionScale = new ForexDefinition(CUR_1, CUR_2, OPTION_PAY_DATE, NOTIONAL * scale, STRIKE);
    final ForexOptionVanillaDefinition optionDefinitionScale = new ForexOptionVanillaDefinition(fxDefinitionScale, OPTION_EXPIRY_DATE, IS_CALL, IS_LONG);
    final ForexOptionVanilla optionScale = optionDefinitionScale.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final ForexOptionSingleBarrier optionBarrierScale = new ForexOptionSingleBarrier(optionScale, BARRIER, scale * REBATE);
    MultipleCurrencyAmount priceBarrierScale = METHOD_BARRIER.presentValue(optionBarrierScale, SMILE_BUNDLE);
    assertEquals("Barriers are cheaper than vanilla", priceBarrier.getAmount(CUR_2) * scale, priceBarrierScale.getAmount(CUR_2), 1.0E-2);
    final ForexOptionVanillaDefinition optionShortDefinition = new ForexOptionVanillaDefinition(FOREX_DEFINITION, OPTION_EXPIRY_DATE, IS_CALL, !IS_LONG);
    final ForexOptionVanilla optionShort = optionShortDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final ForexOptionSingleBarrier barrierShort = new ForexOptionSingleBarrier(optionShort, BARRIER, REBATE);
    MultipleCurrencyAmount priceBarrierShort = METHOD_BARRIER.presentValue(barrierShort, SMILE_BUNDLE);
    assertEquals("Barriers are cheaper than vanilla", -priceBarrier.getAmount(CUR_2), priceBarrierShort.getAmount(CUR_2), 1.0E-2);
  }

  @Test
  /**
   * Tests that the price given by the method for a generic ForexDerivatrive is the same as for a specific ForexOptionSingleBarrier.
   */
  public void methodForexBarrier() {
    ForexDerivative fx = OPTION_BARRIER;
    YieldCurveBundle curves = SMILE_BUNDLE;
    MultipleCurrencyAmount priceGeneric = METHOD_BARRIER.presentValue(fx, curves);
    MultipleCurrencyAmount priceSpecific = METHOD_BARRIER.presentValue(OPTION_BARRIER, SMILE_BUNDLE);
    assertEquals("Barrier price: generic vs specific", priceSpecific, priceGeneric);
  }

}
