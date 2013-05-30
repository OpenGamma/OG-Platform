/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.PriceType;
import com.opengamma.analytics.financial.credit.creditdefaultswap.CreditDefaultSwapDefinitionDataSets;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyVanillaCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.PresentValueCreditDefaultSwap;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.util.time.DateUtils;

/**
 * 
 */
public class PresentValueCreditDefaultSwapTest {
  private static final PresentValueCreditDefaultSwap CALCULATOR = new PresentValueCreditDefaultSwap();
  private static final ZonedDateTime VALUATION_DATE = DateUtils.getUTCDate(2013, 3, 4);
  private static final ZonedDateTime[] MARKET_TENORS = new ZonedDateTime[] {DateUtils.getUTCDate(2013, 3, 20), DateUtils.getUTCDate(2013, 6, 19), DateUtils.getUTCDate(2013, 9, 18),
      DateUtils.getUTCDate(2013, 12, 18), DateUtils.getUTCDate(2014, 3, 19), DateUtils.getUTCDate(2015, 3, 18), DateUtils.getUTCDate(2016, 3, 16), DateUtils.getUTCDate(2018, 3, 15),
      DateUtils.getUTCDate(2023, 3, 15) };
  private static final double[] MARKET_SPREADS = new double[] {300, 315, 350, 390, 400, 420, 410, 404, 402 };
  private static final ZonedDateTime[] YIELD_TENORS = new ZonedDateTime[] {DateUtils.getUTCDate(2013, 4, 1), DateUtils.getUTCDate(2013, 5, 1), DateUtils.getUTCDate(2013, 6, 1),
      DateUtils.getUTCDate(2013, 12, 1), DateUtils.getUTCDate(2014, 3, 1), DateUtils.getUTCDate(2015, 3, 1), DateUtils.getUTCDate(2016, 3, 1), DateUtils.getUTCDate(2018, 3, 1),
      DateUtils.getUTCDate(2023, 3, 1) };
  private static final double[] YIELD_TIMES = new double[YIELD_TENORS.length];
  private static final double[] YIELDS = new double[] {0.005, 0.006, 0.008, 0.009, 0.01, 0.012, 0.015, 0.02, 0.03 };
  private static final double OFFSET = 0;
  private static final ISDADateCurve YIELD_CURVE;
  private static final double BP = 10000;
  private static final double EPS = 1e-15;

  static {
    for (int i = 0; i < YIELD_TENORS.length; i++) {
      YIELD_TIMES[i] = TimeCalculator.getTimeBetween(VALUATION_DATE, YIELD_TENORS[i]);
    }
    YIELD_CURVE = new ISDADateCurve("ISDA", YIELD_TENORS, YIELD_TIMES, YIELDS, OFFSET);
  }

  
  @Test(enabled = false)
  public void testAccruedInterest() {
    final LegacyVanillaCreditDefaultSwapDefinition cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaDefinition().withMaturityDate(VALUATION_DATE.plusYears(10));
  //  final double accruedInterest = CALCULATOR.calculateAccruedInterest(VALUATION_DATE, cds);
    final double cleanPrice = CALCULATOR.calibrateAndGetPresentValue(VALUATION_DATE, cds, MARKET_TENORS, MARKET_SPREADS, YIELD_CURVE, PriceType.CLEAN);
    final double dirtyPrice = CALCULATOR.calibrateAndGetPresentValue(VALUATION_DATE, cds, MARKET_TENORS, MARKET_SPREADS, YIELD_CURVE, PriceType.DIRTY);
    
    System.out.println(cleanPrice +" "+dirtyPrice);
    //assertEquals(cleanPrice, dirtyPrice + accruedInterest, EPS);
  }
  

  @Test(enabled = false)
  public void testRecoveryRateEqualsOne() {
    /*
    final LegacyVanillaCreditDefaultSwapDefinition cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaDefinitionWithRecoveryRate(1).withMaturityDate(
        VALUATION_DATE.plusYears(10));
    final HazardRateCurve hazardRateCurve = CALCULATOR.calibrateHazardRateCurve(VALUATION_DATE, cds, MARKET_TENORS, MARKET_SPREADS, YIELD_CURVE);
    final double contingentLeg = CALCULATOR.calculateContingentLeg(VALUATION_DATE, cds, YIELD_CURVE, hazardRateCurve);
    final double cleanPremiumLeg = CALCULATOR.calculatePremiumLeg(VALUATION_DATE, cds, YIELD_CURVE, hazardRateCurve, PriceType.CLEAN);
    final double dirtyPremiumLeg = -CALCULATOR.calculatePremiumLeg(VALUATION_DATE, cds, YIELD_CURVE, hazardRateCurve, PriceType.DIRTY) * cds.getParSpread() / BP;
    final double cleanPrice = CALCULATOR.calibrateAndGetPresentValue(VALUATION_DATE, cds, MARKET_TENORS, MARKET_SPREADS, YIELD_CURVE, PriceType.CLEAN);
    final double dirtyPrice = CALCULATOR.calibrateAndGetPresentValue(VALUATION_DATE, cds, MARKET_TENORS, MARKET_SPREADS, YIELD_CURVE, PriceType.DIRTY);
    assertEquals(0, contingentLeg, EPS);
    assertEquals(-cleanPremiumLeg * cds.getParSpread() / BP, cleanPrice, EPS);
    assertEquals(dirtyPremiumLeg, dirtyPrice, 1e-9); //TODO accuracy
    */
  }

  @Test(enabled = false)
  public void testParSpreadEqualsZero() {
    /*
    final LegacyVanillaCreditDefaultSwapDefinition cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaDefinitionWithParSpread(0).withMaturityDate(VALUATION_DATE.plusYears(10));
    final HazardRateCurve hazardRateCurve = CALCULATOR.calibrateHazardRateCurve(VALUATION_DATE, cds, MARKET_TENORS, MARKET_SPREADS, YIELD_CURVE);
    final double contingentLeg = CALCULATOR.calculateContingentLeg(VALUATION_DATE, cds, YIELD_CURVE, hazardRateCurve);
    final double cleanPrice = CALCULATOR.calibrateAndGetPresentValue(VALUATION_DATE, cds, MARKET_TENORS, MARKET_SPREADS, YIELD_CURVE, PriceType.CLEAN);
    final double dirtyPrice = CALCULATOR.calibrateAndGetPresentValue(VALUATION_DATE, cds, MARKET_TENORS, MARKET_SPREADS, YIELD_CURVE, PriceType.DIRTY);
    assertEquals(contingentLeg, cleanPrice, EPS);
    assertEquals(contingentLeg, dirtyPrice, EPS);
    */
  }

  @Test(enabled = false)
  public void testCreditSpreadsEqualZero() {
    /*
    final int n = MARKET_TENORS.length;
    final double[] marketSpreads = new double[n];
    Arrays.fill(marketSpreads, EPS);
    final LegacyVanillaCreditDefaultSwapDefinition cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaDefinition().withMaturityDate(VALUATION_DATE.plusYears(10));
    final HazardRateCurve hazardRateCurve = CALCULATOR.calibrateHazardRateCurve(VALUATION_DATE, cds, MARKET_TENORS, marketSpreads, YIELD_CURVE);
    final double contingentLeg = CALCULATOR.calculateContingentLeg(VALUATION_DATE, cds, YIELD_CURVE, hazardRateCurve);
    final double cleanPremiumLeg = CALCULATOR.calculatePremiumLeg(VALUATION_DATE, cds, YIELD_CURVE, hazardRateCurve, PriceType.CLEAN);
    final double dirtyPremiumLeg = CALCULATOR.calculatePremiumLeg(VALUATION_DATE, cds, YIELD_CURVE, hazardRateCurve, PriceType.DIRTY);
    final double cleanPrice = CALCULATOR.calibrateAndGetPresentValue(VALUATION_DATE, cds, MARKET_TENORS, marketSpreads, YIELD_CURVE, PriceType.CLEAN);
    final double dirtyPrice = CALCULATOR.calibrateAndGetPresentValue(VALUATION_DATE, cds, MARKET_TENORS, marketSpreads, YIELD_CURVE, PriceType.DIRTY);
    assertEquals(0, contingentLeg, EPS);
    assertEquals(-cleanPremiumLeg * cds.getParSpread() / BP, cleanPrice, EPS);
    assertEquals(-dirtyPremiumLeg * cds.getParSpread() / BP, dirtyPrice, 1e-9); //TODO accuracy
    */
  }

}
