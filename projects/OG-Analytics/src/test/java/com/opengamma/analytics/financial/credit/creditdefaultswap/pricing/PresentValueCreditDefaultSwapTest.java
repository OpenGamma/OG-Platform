/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.credit.PriceType;
import com.opengamma.analytics.financial.credit.creditdefaultswap.CreditDefaultSwapDefinitionDataSets;
import com.opengamma.analytics.financial.credit.creditdefaultswap.calibration.CalibrateHazardRateTermStructureISDAMethod;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyVanillaCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.legacy.PresentValueLegacyCreditDefaultSwap;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.PresentValueCreditDefaultSwap;
import com.opengamma.analytics.financial.credit.hazardratecurve.HazardRateCurve;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;
import com.opengamma.analytics.financial.credit.schedulegeneration.GenerateCreditDefaultSwapPremiumLegSchedule;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.time.DateUtils;

/**
 * Tests of the original, now deprecated, CDS PresentValue Calculator: PresentValueCreditDefaultSwap.
 */
public class PresentValueCreditDefaultSwapTest {

  // Calculators -----------------------
  private static final PresentValueCreditDefaultSwap CALCULATOR = new PresentValueCreditDefaultSwap();
  private static final PresentValueLegacyCreditDefaultSwap LEGACY_CALCULATOR = new PresentValueLegacyCreditDefaultSwap();
  private static final CalibrateHazardRateTermStructureISDAMethod CALIBRATOR = new CalibrateHazardRateTermStructureISDAMethod();

  // Market Data -----------------------
  private static final ZonedDateTime VALUATION_DATE = DateUtils.getUTCDate(2013, 3, 4);
  private static final ZonedDateTime[] MARKET_TENORS = new ZonedDateTime[] {DateUtils.getUTCDate(2013, 3, 20), DateUtils.getUTCDate(2013, 6, 19), DateUtils.getUTCDate(2013, 9, 18),
      DateUtils.getUTCDate(2013, 12, 18), DateUtils.getUTCDate(2014, 3, 19), DateUtils.getUTCDate(2015, 3, 18), DateUtils.getUTCDate(2016, 3, 16), DateUtils.getUTCDate(2018, 3, 21),
      DateUtils.getUTCDate(2023, 3, 15) };
  private static final double[] MARKET_TIMES = new double[MARKET_TENORS.length + 1];
  private static final double[] MARKET_SPREADS = new double[] {300, 315, 350, 390, 400, 420, 410, 404, 402 };
  private static final double[] ZERO_SPREADS = new double[MARKET_TENORS.length];
  private static final double[] ZERO_HAZARD_RATES = new double[MARKET_TENORS.length + 1];

  private static final ZonedDateTime[] YIELD_TENORS = new ZonedDateTime[] {DateUtils.getUTCDate(2013, 4, 1), DateUtils.getUTCDate(2013, 5, 1), DateUtils.getUTCDate(2013, 6, 1),
      DateUtils.getUTCDate(2013, 12, 1), DateUtils.getUTCDate(2014, 3, 1), DateUtils.getUTCDate(2015, 3, 1), DateUtils.getUTCDate(2016, 3, 1), DateUtils.getUTCDate(2018, 3, 1),
      DateUtils.getUTCDate(2023, 3, 1) };
  private static final double[] YIELD_TIMES = new double[YIELD_TENORS.length];
  private static final double[] YIELDS = new double[] {0.005, 0.006, 0.008, 0.009, 0.01, 0.012, 0.015, 0.02, 0.03 };
  private static final double OFFSET = 0;
  private static final ISDADateCurve YIELD_CURVE;
  private static final double BP = 10000;
  private static final double EPS = 1e-15;
  private static final DayCount ACT_365 = DayCountFactory.INSTANCE.getDayCount("ACT/365");

  static {
    Arrays.fill(ZERO_SPREADS, 0.);
    Arrays.fill(ZERO_HAZARD_RATES, 0.0);

    MARKET_TIMES[0] = 0.0;
    for (int i = 0; i < MARKET_TENORS.length; i++) {
      MARKET_TIMES[i + 1] = ACT_365.getDayCountFraction(VALUATION_DATE, MARKET_TENORS[i]);
    }
    for (int i = 0; i < YIELD_TENORS.length; i++) {
      YIELD_TIMES[i] = TimeCalculator.getTimeBetween(VALUATION_DATE, YIELD_TENORS[i]);
    }
    YIELD_CURVE = new ISDADateCurve("ISDA", YIELD_TENORS, YIELD_TIMES, YIELDS, OFFSET);
  }
  private static final double[] HAZARD_RATES = new double[] {0.07499999990686775, 0.07499999990686775, 0.07874999990221114, 0.08749999994567285,
      0.09749999993946404, 0.09999999993791184, 0.10499999993480742, 0.10249999993635962, 0.10099999993729095, 0.1004999999376014 };
  private static final HazardRateCurve HAZARD_CURVE = new HazardRateCurve(MARKET_TENORS, MARKET_TIMES, HAZARD_RATES, 0);
  final HazardRateCurve ZERO_HAZARD_CURVE = new HazardRateCurve(MARKET_TENORS, MARKET_TIMES, ZERO_HAZARD_RATES, 0);

  // Tests -----------------------

  /**
   * Tests relationship between valuation of clean and dirty prices matches accrued interest.
   * <p>
   * This test works on trivial case of zero spreads.
   * <p>
   * It calls PresentValueCreditDefaultSwap.calibrateAndGetPresentValue.
   */
  @Test
  public void testAccruedInterestZeroSpread() {
    final LegacyVanillaCreditDefaultSwapDefinition cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaDefinition().withMaturityDate(VALUATION_DATE.plusYears(10));
    GenerateCreditDefaultSwapPremiumLegSchedule premiumLegScheduleBuilder = new GenerateCreditDefaultSwapPremiumLegSchedule();
    final ZonedDateTime[][] premiumLegSchedule = premiumLegScheduleBuilder.constructISDACompliantCreditDefaultSwapPremiumLegSchedule(cds);
    final ZonedDateTime stepinDate = VALUATION_DATE.plusDays(1);
    final double accruedInterest = (cds.getNotional() * cds.getParSpread() / 10000.0) * CALCULATOR.calculateAccruedInterest(cds, premiumLegSchedule, stepinDate);
    final double cleanPrice = CALCULATOR.calibrateAndGetPresentValue(VALUATION_DATE, cds, MARKET_TENORS, ZERO_SPREADS, YIELD_CURVE, PriceType.CLEAN);
    final double dirtyPrice = CALCULATOR.calibrateAndGetPresentValue(VALUATION_DATE, cds, MARKET_TENORS, ZERO_SPREADS, YIELD_CURVE, PriceType.DIRTY);
    assertEquals(cleanPrice, dirtyPrice + accruedInterest, EPS);
  }

  /**
   * Tests relationship between valuation of clean and dirty prices matches accrued interest.
   * <p>
   * This test works on provided hazard rates, hence no curve bootstrapping is performed.
   * <p>
   * It calls PresentValueCreditDefaultSwap.calculatePremiumLeg on PriceType.Clean and PriceType.Dirty
   */
  @Test
  public void testAccruedInterest() {
    final LegacyVanillaCreditDefaultSwapDefinition cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaDefinition().withMaturityDate(VALUATION_DATE.plusYears(10));
    GenerateCreditDefaultSwapPremiumLegSchedule premiumLegScheduleBuilder = new GenerateCreditDefaultSwapPremiumLegSchedule();
    final ZonedDateTime[][] premiumLegSchedule = premiumLegScheduleBuilder.constructISDACompliantCreditDefaultSwapPremiumLegSchedule(cds);
    final ZonedDateTime stepinDate = VALUATION_DATE.plusDays(1);
    final double accruedInterest = (cds.getNotional() * cds.getParSpread() / 10000.0) * CALCULATOR.calculateAccruedInterest(cds, premiumLegSchedule, stepinDate);
    final double dirtyPremiumLeg = -CALCULATOR.calculatePremiumLeg(VALUATION_DATE, cds, YIELD_CURVE, HAZARD_CURVE, PriceType.DIRTY) * cds.getParSpread() / BP;
    final double riskyAnnuity = CALCULATOR.calculatePremiumLeg(VALUATION_DATE, cds, YIELD_CURVE, HAZARD_CURVE, PriceType.CLEAN);
    final double cleanPremiumLeg = -cds.getParSpread() / BP * riskyAnnuity;
    assertEquals(cleanPremiumLeg / (dirtyPremiumLeg + accruedInterest), 1.0, EPS);
  }

  /**
   * Regression Tests.
   * <p>
   * This test works on provided hazard rates, hence no curve bootstrapping is performed.
   * <p>
   * It calls PresentValueCreditDefaultSwap.calculatePremiumLeg on PriceType.Clean and PriceType.Dirty
   */
  @Test
  public void regressionTest() {
    final LegacyVanillaCreditDefaultSwapDefinition cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaDefinition().withMaturityDate(VALUATION_DATE.plusYears(10));
    GenerateCreditDefaultSwapPremiumLegSchedule premiumLegScheduleBuilder = new GenerateCreditDefaultSwapPremiumLegSchedule();
    final ZonedDateTime[][] premiumLegSchedule = premiumLegScheduleBuilder.constructISDACompliantCreditDefaultSwapPremiumLegSchedule(cds);
    final ZonedDateTime stepinDate = VALUATION_DATE.plusDays(1);
    final double accruedInterest = (cds.getNotional() * cds.getParSpread() / 10000.0) * CALCULATOR.calculateAccruedInterest(cds, premiumLegSchedule, stepinDate);
    final double dirtyPremiumLeg = -CALCULATOR.calculatePremiumLeg(VALUATION_DATE, cds, YIELD_CURVE, HAZARD_CURVE, PriceType.DIRTY) * cds.getParSpread() / BP;
    final double riskyAnnuity = CALCULATOR.calculatePremiumLeg(VALUATION_DATE, cds, YIELD_CURVE, HAZARD_CURVE, PriceType.CLEAN);
    final double cleanPremiumLeg = -cds.getParSpread() / BP * riskyAnnuity;
    assertEquals(accruedInterest / 341.6666666666667, 1.0, EPS);
    assertEquals(dirtyPremiumLeg / -1551069.4610753073, 1.0, EPS);
    assertEquals(cleanPremiumLeg / -1550727.7944086404, 1.0, EPS);
  }

  /**
   * Test of calculator when full recovery is expected on default, a strange case where the contingent leg should have zero value.
   * <p>
   * In this test, a hazard curve is provided. No calibration to market spreads is performed.
   */
  @Test
  public void testRecoveryRateEqualsOneWithHazardCurveProvided() {
    final LegacyVanillaCreditDefaultSwapDefinition cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaDefinitionWithRecoveryRate(1).withMaturityDate(
        VALUATION_DATE.plusYears(10));
    final double contingentLeg = CALCULATOR.calculateContingentLeg(VALUATION_DATE, cds, YIELD_CURVE, HAZARD_CURVE);
    final double dirtyPremiumLeg = -CALCULATOR.calculatePremiumLeg(VALUATION_DATE, cds, YIELD_CURVE, HAZARD_CURVE, PriceType.DIRTY) * cds.getParSpread() / BP;
    final double riskyAnnuity = CALCULATOR.calculatePremiumLeg(VALUATION_DATE, cds, YIELD_CURVE, HAZARD_CURVE, PriceType.CLEAN);
    final double cleanPremiumLeg = -riskyAnnuity * cds.getParSpread() / BP;
    assertEquals(0, contingentLeg, EPS);
    assertEquals(cleanPremiumLeg, -1550727.7944086404, 1e-9);
    assertEquals(dirtyPremiumLeg, -1551069.4610753073, 1e-9);
  }

  /**
   * Test of calculator when full recovery is expected on default, a strange case where the contingent leg should have zero value.
   * <p>
   * In this test, market par cds spreads are provided. Calibration to market spreads is performed.
   * <p>
   * Note: It is this calibration that is causing the exception.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testRecoveryRateEqualsOneWithMarketSpreadsProvided() {
    final LegacyVanillaCreditDefaultSwapDefinition cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaDefinitionWithRecoveryRate(1).withMaturityDate(
        VALUATION_DATE.plusYears(10));
    final double cleanPremiumLeg = CALCULATOR.calculatePremiumLeg(VALUATION_DATE, cds, YIELD_CURVE, HAZARD_CURVE, PriceType.CLEAN);
    final double dirtyPremiumLeg = -CALCULATOR.calculatePremiumLeg(VALUATION_DATE, cds, YIELD_CURVE, HAZARD_CURVE, PriceType.DIRTY) * cds.getParSpread() / BP;
    final double dirtyPrice = CALCULATOR.calibrateAndGetPresentValue(VALUATION_DATE, cds, MARKET_TENORS, MARKET_SPREADS, YIELD_CURVE, PriceType.DIRTY);
    final double cleanPrice = CALCULATOR.calibrateAndGetPresentValue(VALUATION_DATE, cds, MARKET_TENORS, MARKET_SPREADS, YIELD_CURVE, PriceType.CLEAN);
    assertEquals(-cleanPremiumLeg * cds.getParSpread() / BP, cleanPrice, EPS);
    assertEquals(dirtyPremiumLeg, dirtyPrice, 1e-9); //TODO accuracy
  }

  /**
   * Test of calculator when full recovery is expected on default, of which there is no implied risk and spreads and hazard rates are zero.
   * <p>
   * In this test, market par cds spreads are provided. Calibration to market spreads is performed.
   * <p>
   * Note: It is this calibration that is causing the exception.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testRecoveryRateEqualsOneWithZeroSpreadsProvided() {
    final LegacyVanillaCreditDefaultSwapDefinition cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaDefinitionWithRecoveryRate(1).withMaturityDate(
        VALUATION_DATE.plusYears(10));
    final double riskyAnnuity = CALCULATOR.calculatePremiumLeg(VALUATION_DATE, cds, YIELD_CURVE, HAZARD_CURVE, PriceType.CLEAN);
    final double cleanPremiumLeg = -riskyAnnuity * cds.getParSpread() / BP;
    final double dirtyPremiumLeg = -CALCULATOR.calculatePremiumLeg(VALUATION_DATE, cds, YIELD_CURVE, HAZARD_CURVE, PriceType.DIRTY) * cds.getParSpread() / BP;
    final double cleanPrice = CALCULATOR.calibrateAndGetPresentValue(VALUATION_DATE, cds, MARKET_TENORS, ZERO_SPREADS, YIELD_CURVE, PriceType.CLEAN);
    final double dirtyPrice = CALCULATOR.calibrateAndGetPresentValue(VALUATION_DATE, cds, MARKET_TENORS, ZERO_SPREADS, YIELD_CURVE, PriceType.DIRTY);
    assertEquals(cleanPremiumLeg / cleanPrice, 1.0, EPS);
    assertEquals(dirtyPremiumLeg / dirtyPrice, 1.0, EPS);
  }

  /**
   * Test that the entire swap value is equal to the value of the contingent leg when the par spread on the premium leg is zero.
   */
  @Test
  public void testParSpreadEqualsZero() {
    final LegacyVanillaCreditDefaultSwapDefinition cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaDefinitionWithParSpread(0).withMaturityDate(VALUATION_DATE.plusYears(10));
    final double contingentLeg = CALCULATOR.calculateContingentLeg(VALUATION_DATE, cds, YIELD_CURVE, HAZARD_CURVE);
    final double cleanPrice = LEGACY_CALCULATOR.getPresentValueLegacyCreditDefaultSwap(VALUATION_DATE, cds, YIELD_CURVE, HAZARD_CURVE, PriceType.CLEAN);
    final double dirtyPrice = LEGACY_CALCULATOR.getPresentValueLegacyCreditDefaultSwap(VALUATION_DATE, cds, YIELD_CURVE, HAZARD_CURVE, PriceType.DIRTY);
    assertEquals(contingentLeg, cleanPrice, EPS);
    assertEquals(contingentLeg, dirtyPrice, EPS);
  }

  /**
   * Test trivial case with zero par spreads, zero hazard rates. This is a test that the methods in {@link PresentValueCreditDefaultSwap} are in sync with {@link PresentValueLegacyCreditDefaultSwap}
   */
  @Test
  public void testCreditSpreadsEqualZero() {
    final LegacyVanillaCreditDefaultSwapDefinition cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaDefinition().withMaturityDate(VALUATION_DATE.plusYears(10));
    final double contingentLeg = CALCULATOR.calculateContingentLeg(VALUATION_DATE, cds, YIELD_CURVE, ZERO_HAZARD_CURVE);
    final double cleanRiskyAnnuity = CALCULATOR.calculatePremiumLeg(VALUATION_DATE, cds, YIELD_CURVE, ZERO_HAZARD_CURVE, PriceType.CLEAN);
    final double dirtyRiskyAnnuity = CALCULATOR.calculatePremiumLeg(VALUATION_DATE, cds, YIELD_CURVE, ZERO_HAZARD_CURVE, PriceType.DIRTY);
    final double cleanPremiumLeg = -cleanRiskyAnnuity * cds.getParSpread() / BP;
    final double dirtyPremiumLeg = -dirtyRiskyAnnuity * cds.getParSpread() / BP;
    final double cleanPrice = CALCULATOR.calibrateAndGetPresentValue(VALUATION_DATE, cds, MARKET_TENORS, ZERO_SPREADS, YIELD_CURVE, PriceType.CLEAN);
    final double dirtyPrice = CALCULATOR.calibrateAndGetPresentValue(VALUATION_DATE, cds, MARKET_TENORS, ZERO_SPREADS, YIELD_CURVE, PriceType.DIRTY);
    assertEquals(0, contingentLeg, EPS);
    assertEquals(cleanPremiumLeg / cleanPrice - 1.0, 0.0, EPS);
    assertEquals(dirtyPremiumLeg / dirtyPrice - 1.0, 0.0, EPS);
  }

}
