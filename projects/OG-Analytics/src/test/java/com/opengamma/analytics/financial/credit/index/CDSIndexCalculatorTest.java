/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.index;

import static com.opengamma.analytics.financial.credit.options.CDSIndexPrvider.CDX_NA_HY_20140213_RECOVERY_RATES;
import static com.opengamma.analytics.financial.credit.options.CDSIndexPrvider.CDX_NA_HY_21_RECOVERY_RATE;
import static com.opengamma.analytics.financial.credit.options.CDSIndexPrvider.INDEX_TENORS;
import static com.opengamma.analytics.financial.credit.options.CDSIndexPrvider.getCDX_NA_HY_20140213_CreditCurves;
import static com.opengamma.analytics.financial.credit.options.YieldCurveProvider.ISDA_USD_20140213;
import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.opengamma.analytics.financial.credit.isdastandardmodel.AnalyticCDSPricer;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalytic;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalyticFactory;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDABaseTest;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.PriceType;
import com.opengamma.financial.convention.businessday.BusinessDayDateUtils;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.test.TestGroup;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class CDSIndexCalculatorTest extends ISDABaseTest {

  private static final LocalDate TRADE_DATE = LocalDate.of(2014, 2, 13);
  private static final Period[] INDEX_PILLARS = INDEX_TENORS;
  private static final double INDEX_RR = CDX_NA_HY_21_RECOVERY_RATE;
  private static final ISDACompliantCreditCurve[] CREDIT_CURVES = getCDX_NA_HY_20140213_CreditCurves();
  private static final double[] RECOVERY_RATES = CDX_NA_HY_20140213_RECOVERY_RATES;
  private static final IntrinsicIndexDataBundle INTRINSIC_DATA = new IntrinsicIndexDataBundle(CREDIT_CURVES, RECOVERY_RATES);
  private static ISDACompliantYieldCurve YIELD_CURVE = ISDA_USD_20140213;

  private static CDSIndexCalculator INDEX_CAL = new CDSIndexCalculator();
  private static CDSAnalyticFactory FACTORY = new CDSAnalyticFactory(INDEX_RR);

  /**
   * 
   */
  @Test
  public void pvAndPufTest() {
    final double tol = 1.e-12;
    final CDSAnalytic cdx = FACTORY.makeCDX(TRADE_DATE, Period.ofYears(3).plusMonths(5));
    final double indexCoupon = 300 * 1.e-4;
    final CDSAnalytic[] pillarCdx = FACTORY.makeCDX(TRADE_DATE, INDEX_PILLARS);

    /*
     * No defaulted names
     */
    final double res = INDEX_CAL.indexPUF(cdx, indexCoupon, YIELD_CURVE, INTRINSIC_DATA);
    final double resDirty = INDEX_CAL.indexPV(cdx, indexCoupon, YIELD_CURVE, INTRINSIC_DATA, PriceType.DIRTY);
    final double sp = INDEX_CAL.intrinsicIndexSpread(cdx, YIELD_CURVE, INTRINSIC_DATA);
    final double resSpread = INDEX_CAL.indexPUF(cdx, sp, YIELD_CURVE, INTRINSIC_DATA);
    assertEquals(0., resSpread, tol);
    final double resAvgSpread = INDEX_CAL.averageSpread(cdx, YIELD_CURVE, INTRINSIC_DATA);
    final double resAnnuity = INDEX_CAL.indexAnnuity(cdx, YIELD_CURVE, INTRINSIC_DATA, cdx.getCashSettleTime() + 0.01);

    final int indexSize = CREDIT_CURVES.length;
    double ref = 0.;
    double refDirty = 0.;
    double refAvgSpread = 0.;
    double refAnnuity = 0.;
    final AnalyticCDSPricer cdsPricer = new AnalyticCDSPricer();
    for (int i = 0; i < indexSize; ++i) {
      final CDSAnalytic cds = cdx.withRecoveryRate(1. - INTRINSIC_DATA.getLGD(i));
      ref += INTRINSIC_DATA.getWeight(i) * cdsPricer.pv(cds, YIELD_CURVE, CREDIT_CURVES[i], indexCoupon);
      refDirty += INTRINSIC_DATA.getWeight(i) * cdsPricer.pv(cds, YIELD_CURVE, CREDIT_CURVES[i], indexCoupon, PriceType.DIRTY);
      refAvgSpread += INTRINSIC_DATA.getWeight(i) * cdsPricer.parSpread(cds, YIELD_CURVE, CREDIT_CURVES[i]);
      refAnnuity += INTRINSIC_DATA.getWeight(i) * cdsPricer.annuity(cds, YIELD_CURVE, CREDIT_CURVES[i], PriceType.CLEAN, cdx.getCashSettleTime() + 0.01);
    }
    ref /= INTRINSIC_DATA.getIndexFactor();

    assertEquals(ref, res, tol);
    assertEquals(refDirty, resDirty, tol);
    assertEquals(refAvgSpread, resAvgSpread, tol);
    assertEquals(refAnnuity, resAnnuity, tol);

    final ISDACompliantCreditCurve impliedCurve = INDEX_CAL.impliedIndexCurve(pillarCdx, indexCoupon, YIELD_CURVE, INTRINSIC_DATA);
    final int nPillars = INDEX_PILLARS.length;
    for (int i = 0; i < nPillars; ++i) {
      final double puf = cdsPricer.pv(pillarCdx[i], YIELD_CURVE, impliedCurve, indexCoupon);
      final double pufFullCurve = INDEX_CAL.indexPV(pillarCdx[i], indexCoupon, YIELD_CURVE, INTRINSIC_DATA);
      assertEquals(pufFullCurve, puf, tol);
    }

    /*
     * Defaulted names
     */
    final int[] defaultedNames = new int[] {2, 15, 37, 51 };
    final IntrinsicIndexDataBundle intrinsicDataWithDefaulted = INTRINSIC_DATA.withDefault(defaultedNames);
    final double resDefaulted = INDEX_CAL.indexPUF(cdx, indexCoupon, YIELD_CURVE, intrinsicDataWithDefaulted);
    final double resDefaultedDirty = INDEX_CAL.indexPV(cdx, indexCoupon, YIELD_CURVE, intrinsicDataWithDefaulted, PriceType.DIRTY);
    final double resDefaultedAvgSpread = INDEX_CAL.averageSpread(cdx, YIELD_CURVE, intrinsicDataWithDefaulted);
    ref = 0.;
    refDirty = 0.;
    refAvgSpread = 0.;
    for (int i = 0; i < indexSize; ++i) {
      if (!intrinsicDataWithDefaulted.isDefaulted(i)) {
        final CDSAnalytic cds = cdx.withRecoveryRate(1. - intrinsicDataWithDefaulted.getLGD(i));
        ref += intrinsicDataWithDefaulted.getWeight(i) * cdsPricer.pv(cds, YIELD_CURVE, CREDIT_CURVES[i], indexCoupon);
        refDirty += intrinsicDataWithDefaulted.getWeight(i) * cdsPricer.pv(cds, YIELD_CURVE, CREDIT_CURVES[i], indexCoupon, PriceType.DIRTY);
        refAvgSpread += intrinsicDataWithDefaulted.getWeight(i) * cdsPricer.parSpread(cds, YIELD_CURVE, CREDIT_CURVES[i]);
      }
    }
    ref /= intrinsicDataWithDefaulted.getIndexFactor();
    assertEquals(ref, resDefaulted, tol);
    assertEquals(refDirty, resDefaultedDirty, tol);
    refAvgSpread /= intrinsicDataWithDefaulted.getIndexFactor();
    assertEquals(refAvgSpread, resDefaultedAvgSpread, tol);

    final double valTime = (DayCounts.ACT_365).getDayCountFraction(TRADE_DATE, BusinessDayDateUtils.addWorkDays(TRADE_DATE, 3, DEFAULT_CALENDAR));
    final double resWithValTime = INDEX_CAL.indexPV(cdx, indexCoupon, YIELD_CURVE, INTRINSIC_DATA, PriceType.CLEAN, valTime);
    assertEquals(res * INTRINSIC_DATA.getIndexFactor(), resWithValTime, tol);

    final ISDACompliantCreditCurve impliedCurveDefaulted = INDEX_CAL.impliedIndexCurve(pillarCdx, indexCoupon, YIELD_CURVE, intrinsicDataWithDefaulted);
    for (int i = 0; i < nPillars; ++i) {
      final double puf = cdsPricer.pv(pillarCdx[i], YIELD_CURVE, impliedCurveDefaulted, indexCoupon);
      final double pufFullCurve = INDEX_CAL.indexPV(pillarCdx[i], indexCoupon, YIELD_CURVE, intrinsicDataWithDefaulted) / intrinsicDataWithDefaulted.getIndexFactor();
      assertEquals(pufFullCurve, puf, tol);
    }

    /*
     * IllegalArgument exception thrown
     */
    final int[] defInd = new int[indexSize];
    for (int i = 0; i < indexSize; ++i) {
      defInd[i] = i;
    }
    final IntrinsicIndexDataBundle allDefaulted = INTRINSIC_DATA.withDefault(defInd);
    try {
      INDEX_CAL.indexPUF(cdx, indexCoupon, YIELD_CURVE, allDefaulted);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("Index completely defaulted - not possible to rescale for PUF", e.getMessage());
    }
    try {
      INDEX_CAL.intrinsicIndexSpread(cdx, YIELD_CURVE, allDefaulted);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("Every name in the index is defaulted - cannot calculate a spread", e.getMessage());
    }
    try {
      INDEX_CAL.averageSpread(cdx, YIELD_CURVE, allDefaulted);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("Every name in the index is defaulted - cannot calculate a spread", e.getMessage());
    }
    try {
      INDEX_CAL.impliedIndexCurve(pillarCdx, indexCoupon, YIELD_CURVE, allDefaulted);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("Every name in the index is defaulted - cannot calculate implied index curve", e.getMessage());
    }
  }
}
