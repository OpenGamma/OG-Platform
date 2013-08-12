/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isda;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.ISDAYieldCurveAndHazardRateCurveProvider;
import com.opengamma.analytics.financial.credit.PriceType;
import com.opengamma.analytics.financial.credit.creditdefaultswap.CreditDefaultSwapDefinitionDataSets;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.vanilla.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.PresentValueCreditDefaultSwap;
import com.opengamma.analytics.financial.credit.hazardratecurve.HazardRateCurve;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;
import com.opengamma.analytics.financial.credit.schedulegeneration.GenerateCreditDefaultSwapPremiumLegSchedule;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.time.DateUtils;

/**
 *
 */
public class ISDACompliantPremiumLegCalculatorAgaintISDATest {

  private static final GenerateCreditDefaultSwapPremiumLegSchedule PREMIUM_LEG_SCHEDULE_BUILDER = new GenerateCreditDefaultSwapPremiumLegSchedule();
  private static final PresentValueCreditDefaultSwap DEPRICATED_CALCULATOR = new PresentValueCreditDefaultSwap();
  private static final ZonedDateTime VALUATION_DATE = DateUtils.getUTCDate(2013, 5, 30);

  private static final ZonedDateTime BASE_DATE = DateUtils.getUTCDate(2013, 5, 27);

  // points related to the credit curve
  private static final ZonedDateTime[] HR_DATES = new ZonedDateTime[] {DateUtils.getUTCDate(2014, 6, 20), DateUtils.getUTCDate(2015, 6, 20), DateUtils.getUTCDate(2018, 6, 20),
      DateUtils.getUTCDate(2020, 6, 20), DateUtils.getUTCDate(2023, 6, 20)};
  private static final double[] SURVIVAL_PROB = new double[] {0.998465169025505, 0.990295118948977, 0.950609390326734, 0.908711160461782, 0.831568020877281};
  private static final double[] HR_TIMES;
  private static final double[] HR_RATES;
  private static final HazardRateCurve HAZARD_RATE_CURVE;

  // points related to the yield curve (note: here we use yield curve points directly rather than fit from IR instruments)
  private static final ZonedDateTime[] YC_DATES = new ZonedDateTime[] {DateUtils.getUTCDate(2013, 6, 27), DateUtils.getUTCDate(2013, 8, 27), DateUtils.getUTCDate(2013, 11, 27),
      DateUtils.getUTCDate(2014, 5, 27), DateUtils.getUTCDate(2015, 5, 27), DateUtils.getUTCDate(2016, 5, 27), DateUtils.getUTCDate(2018, 5, 27), DateUtils.getUTCDate(2020, 5, 27),
      DateUtils.getUTCDate(2023, 5, 27), DateUtils.getUTCDate(2028, 5, 27), DateUtils.getUTCDate(2033, 5, 27), DateUtils.getUTCDate(2043, 5, 27)};
  private static final double[] YC_RATES = new double[] {0.001928, 0.002295, 0.002728, 0.004153, 0.005468, 0.00684, 0.004495, 0.006555, 0.00919, 0.01212, 0.014905, 0.017475};
  private static final double[] DISCOUNT_FACT = new double[] {0.999836423085376, 0.999422363918169, 0.998627604022246, 0.995864176076753, 0.989153047561162, 0.979739248277308, 0.977812910043371,
      0.955260628324658, 0.912533560879657, 0.834569599165928, 0.743710886947677, 0.59448827669764};
  private static final double[] YC_TIMES;
  private static final ISDADateCurve YIELD_CURVE;

  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("ACT/365");
  // TODO check the purpose of this offset
  private static final double OFFSET = 0.0;// 1. / 365;
  private static final ISDAYieldCurveAndHazardRateCurveProvider CURVES;
  // The CDS
  private static final CreditDefaultSwapDefinition CDS;

  private static final ZonedDateTime[] FEE_LEG_PAYMENT_DATES = new ZonedDateTime[] {DateUtils.getUTCDate(2013, 6, 20), DateUtils.getUTCDate(2013, 9, 20), DateUtils.getUTCDate(2013, 12, 20),
      DateUtils.getUTCDate(2014, 3, 20), DateUtils.getUTCDate(2014, 6, 20), DateUtils.getUTCDate(2014, 9, 22), DateUtils.getUTCDate(2014, 12, 22), DateUtils.getUTCDate(2015, 3, 20),
      DateUtils.getUTCDate(2015, 6, 22), DateUtils.getUTCDate(2015, 9, 21), DateUtils.getUTCDate(2015, 12, 21), DateUtils.getUTCDate(2016, 3, 21), DateUtils.getUTCDate(2016, 6, 20),
      DateUtils.getUTCDate(2016, 9, 20), DateUtils.getUTCDate(2016, 12, 20), DateUtils.getUTCDate(2017, 3, 20), DateUtils.getUTCDate(2017, 6, 20), DateUtils.getUTCDate(2017, 9, 20),
      DateUtils.getUTCDate(2017, 12, 20), DateUtils.getUTCDate(2018, 3, 20), DateUtils.getUTCDate(2018, 6, 20)};

  static {
    final int ccPoints = HR_DATES.length;
    HR_TIMES = new double[ccPoints];
    HR_RATES = new double[ccPoints];
    for (int i = 0; i < ccPoints; i++) {
      HR_TIMES[i] = DAY_COUNT.getDayCountFraction(BASE_DATE, HR_DATES[i]);
      // TODO these should be fitted to the par-spreads then compared to the numbers from ISDA - here we take directly from the ISDA fit
      HR_RATES[i] = -Math.log(SURVIVAL_PROB[i]) / HR_TIMES[i];
    }
    HAZARD_RATE_CURVE = new HazardRateCurve(HR_DATES, HR_TIMES, HR_RATES, OFFSET);

    final int ycPoints = YC_DATES.length;
    YC_TIMES = new double[ycPoints];
    for (int i = 0; i < ycPoints; i++) {
      YC_TIMES[i] = DAY_COUNT.getDayCountFraction(BASE_DATE, YC_DATES[i]);
    }
    YIELD_CURVE = new ISDADateCurve("ISDA", BASE_DATE, YC_DATES, YC_RATES, OFFSET); // Remake: this constructor assumes ACT/365
    CURVES = new ISDAYieldCurveAndHazardRateCurveProvider(YIELD_CURVE, HAZARD_RATE_CURVE);

    CDS = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaDefinition().withEffectiveDate(DateUtils.getUTCDate(2013, 3, 20)).withStartDate(DateUtils.getUTCDate(2013, 3, 20))
        .withMaturityDate(DateUtils.getUTCDate(2018, 6, 20)).withRecoveryRate(0.4).withSpread(50);
  }

  @Test
  public void yieldCurveTest() {
    final int n = YC_TIMES.length;
    for (int i = 0; i < n; i++) {
      final double t = YC_TIMES[i];
      final double df = YIELD_CURVE.getDiscountFactor(t);
      assertEquals(DISCOUNT_FACT[i], df, 1e-10);
    }
  }

  @Test
  public void hazardCurveTest() {
    final int n = HR_TIMES.length;
    for (int i = 0; i < n; i++) {
      final double t = HR_TIMES[i];
      final double q = HAZARD_RATE_CURVE.getSurvivalProbability(t);
      assertEquals(SURVIVAL_PROB[i], q, 1e-10);
    }
  }

  /**
   * Check the future payment dates (i.e. after valuation date) on the payment leg agree with ISDA/Excel
   * For now we do not check the other information (accrual start and end)
   */
  @Test
  public void feeLegPaymentTimesTest() {
    final ZonedDateTime[][] premiumLegSchedule = PREMIUM_LEG_SCHEDULE_BUILDER.constructISDACompliantCreditDefaultSwapPremiumLegSchedule(CDS);
    final int n = premiumLegSchedule.length;
    int jj = 0;
    for (int i = 0; i < n; i++) {
      final ZonedDateTime temp = premiumLegSchedule[i][0];
      if (temp.isAfter(VALUATION_DATE)) {
        assertTrue(temp.equals(FEE_LEG_PAYMENT_DATES[jj++]));
      }
    }
  }

  @Test
  public void pvTest() {
    final double result = DEPRICATED_CALCULATOR.calculatePremiumLeg(VALUATION_DATE, CDS, YIELD_CURVE, HAZARD_RATE_CURVE, PriceType.CLEAN);

    //System.out.println(result);
  }

}
