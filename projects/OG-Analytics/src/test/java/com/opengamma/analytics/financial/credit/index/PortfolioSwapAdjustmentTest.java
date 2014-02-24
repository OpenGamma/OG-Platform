/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.index;

import static com.opengamma.analytics.financial.credit.options.CDSIndexPrvider.CDX_NA_HY_20140213_PRICES;
import static com.opengamma.analytics.financial.credit.options.CDSIndexPrvider.CDX_NA_HY_20140213_RECOVERY_RATES;
import static com.opengamma.analytics.financial.credit.options.CDSIndexPrvider.CDX_NA_HY_21_COUPON;
import static com.opengamma.analytics.financial.credit.options.CDSIndexPrvider.CDX_NA_HY_21_RECOVERY_RATE;
import static com.opengamma.analytics.financial.credit.options.CDSIndexPrvider.INDEX_TENORS;
import static com.opengamma.analytics.financial.credit.options.CDSIndexPrvider.getCDX_NA_HY_20140213_CreditCurves;
import static com.opengamma.analytics.financial.credit.options.YieldCurveProvider.ISDA_USD_20140213;
import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalytic;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalyticFactory;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDABaseTest;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;

/**
 * 
 */
public class PortfolioSwapAdjustmentTest extends ISDABaseTest {

  private static final LocalDate TRADE_DATE = LocalDate.of(2014, 2, 13);
  private static final Period[] INDEX_PILLARS = INDEX_TENORS;
  private static final double INDEX_COUPON = CDX_NA_HY_21_COUPON;
  private static final double INDEX_RR = CDX_NA_HY_21_RECOVERY_RATE;
  private static final ISDACompliantCreditCurve[] CREDIT_CURVES = getCDX_NA_HY_20140213_CreditCurves();
  private static final double[] RECOVERY_RATES = CDX_NA_HY_20140213_RECOVERY_RATES;
  private static ISDACompliantYieldCurve YIELD_CURVE = ISDA_USD_20140213;
  private static double[] PRICES = CDX_NA_HY_20140213_PRICES;

  private static PortfolioSwapAdjustment PSA = new PortfolioSwapAdjustment();
  private static CDSIndexCalculator INDEX_CAL = new CDSIndexCalculator();
  private static CDSAnalyticFactory FACTORY = new CDSAnalyticFactory(INDEX_RR);

  @Test
  public void singleTermAdjustmentTest() {
    final CDSAnalytic[] cdx = FACTORY.makeCDX(TRADE_DATE, INDEX_PILLARS);
    final int n = cdx.length;

    for (int i = 0; i < n; i++) {
      final double puf = 1 - PRICES[i];
      final ISDACompliantCreditCurve[] adjCurves = PSA.adjustCurves(cdx[i], INDEX_COUPON, YIELD_CURVE, CREDIT_CURVES, RECOVERY_RATES, puf);
      final double puf2 = INDEX_CAL.indexPV(cdx[i], INDEX_COUPON, YIELD_CURVE, adjCurves, RECOVERY_RATES);
      assertEquals(puf, puf2, 1e-14);
    }
  }

  @Test
  public void multiTermAdjustmentTest() {
    final CDSAnalytic[] cdx = FACTORY.makeCDX(TRADE_DATE, INDEX_PILLARS);
    final int n = cdx.length;

    final double[] puf = new double[n];
    for (int i = 0; i < n; i++) {
      puf[i] = 1 - PRICES[i];
    }
    final ISDACompliantCreditCurve[] adjCurves = PSA.adjustCurves(cdx, INDEX_COUPON, YIELD_CURVE, CREDIT_CURVES, RECOVERY_RATES, puf);
    for (int i = 0; i < n; i++) {
      final double puf2 = INDEX_CAL.indexPV(cdx[i], INDEX_COUPON, YIELD_CURVE, adjCurves, RECOVERY_RATES);
      assertEquals(puf[i], puf2, 1e-14);
    }
  }

}
