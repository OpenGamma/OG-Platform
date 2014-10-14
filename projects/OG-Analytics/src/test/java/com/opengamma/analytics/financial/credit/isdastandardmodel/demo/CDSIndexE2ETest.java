/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel.demo;

import static com.opengamma.analytics.financial.credit.options.CDSIndexPrvider.CDS_TENORS;
import static com.opengamma.analytics.financial.credit.options.CDSIndexPrvider.CDX_NA_HY_20140213_PAR_SPREADS;
import static com.opengamma.analytics.financial.credit.options.CDSIndexPrvider.CDX_NA_HY_20140213_PRICES;
import static com.opengamma.analytics.financial.credit.options.CDSIndexPrvider.CDX_NA_HY_20140213_RECOVERY_RATES;
import static com.opengamma.analytics.financial.credit.options.CDSIndexPrvider.CDX_NA_HY_21_COUPON;
import static com.opengamma.analytics.financial.credit.options.CDSIndexPrvider.CDX_NA_HY_21_RECOVERY_RATE;
import static com.opengamma.analytics.financial.credit.options.CDSIndexPrvider.INDEX_TENORS;
import static com.opengamma.analytics.financial.credit.options.CDSIndexPrvider.buildCreditCurves;
import static com.opengamma.analytics.financial.credit.options.YieldCurveProvider.ISDA_USD_20140213;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.analytics.financial.credit.index.CDSIndexCalculator;
import com.opengamma.analytics.financial.credit.index.IntrinsicIndexDataBundle;
import com.opengamma.analytics.financial.credit.index.PortfolioSwapAdjustment;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalytic;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalyticFactory;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDABaseTest;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.InterestRateSensitivityCalculator;
import com.opengamma.analytics.financial.credit.isdastandardmodel.PointsUpFront;
import com.opengamma.analytics.financial.credit.isdastandardmodel.PriceType;

/**
 * 
 */
public class CDSIndexE2ETest extends ISDABaseTest {
  private static final double NOTIONAL = 1e8;

  private static final LocalDate TRADE_DATE = LocalDate.of(2014, 2, 13);

  private static final LocalDate EXPIRY = LocalDate.of(2014, 3, 19);
  private static final LocalDate EXERCISE_SETTLE = LocalDate.of(2014, 3, 24);
  private static final LocalDate MATURITY = LocalDate.of(2018, 12, 20);

  private static final double INDEX_COUPON = CDX_NA_HY_21_COUPON;
  private static final double INDEX_RECOVERY = CDX_NA_HY_21_RECOVERY_RATE;

  // yield curve
  private static ISDACompliantYieldCurve YIELD_CURVE = ISDA_USD_20140213;

  // credit curves for single names
  private static final double[] RECOVERY_RATES = CDX_NA_HY_20140213_RECOVERY_RATES;
  private static final ISDACompliantCreditCurve[] CREDIT_CURVES = buildCreditCurves(TRADE_DATE,
      CDX_NA_HY_20140213_PAR_SPREADS, RECOVERY_RATES, CDS_TENORS, ISDA_USD_20140213);
  private static IntrinsicIndexDataBundle INTRINSIC_DATA = new IntrinsicIndexDataBundle(CREDIT_CURVES, RECOVERY_RATES);

  // index market data
  private static double[] PRICES = CDX_NA_HY_20140213_PRICES;
  private static int INDEX_SIZE = RECOVERY_RATES.length;

  private static final PointsUpFront[] PILLAR_PUF;
  private static final CDSAnalyticFactory FACTORY = new CDSAnalyticFactory(INDEX_RECOVERY);
  private static final CDSAnalytic[] CDX = FACTORY.makeCDX(TRADE_DATE, INDEX_TENORS);

  private static final PortfolioSwapAdjustment PSA = new PortfolioSwapAdjustment();
  private static final CDSIndexCalculator INDEX_CAL = new CDSIndexCalculator();
  private static final InterestRateSensitivityCalculator IR_CAL = new InterestRateSensitivityCalculator();

  //  private static final boolean PRINT = false;

  static {
    final int n = PRICES.length;
    PILLAR_PUF = new PointsUpFront[n];
    for (int i = 0; i < n; i++) {
      PILLAR_PUF[i] = new PointsUpFront(INDEX_COUPON, 1 - PRICES[i]);
    }

    //    if (PRINT) {
    //      System.out.println("CDXNAHYTest - set PRINT to false before push");
    //    }
  }

  @Test(enabled = false)
  public void indexTest() {
    int pos = 1;
    final CDSAnalytic targentCDX = CDX[pos];

    final int n = PILLAR_PUF.length;
    final double[] indexPUF = new double[n];
    for (int i = 0; i < n; i++) {
      indexPUF[i] = PILLAR_PUF[i].getPointsUpFront();
    }
    final IntrinsicIndexDataBundle adjCurves = PSA.adjustCurves(indexPUF[pos], CDX[pos], INDEX_COUPON, YIELD_CURVE,
        INTRINSIC_DATA); // use single node curve
    double cleanPrice = INDEX_CAL.indexPV(targentCDX, INDEX_COUPON, YIELD_CURVE, adjCurves) * NOTIONAL;
    double dirtyPrice = INDEX_CAL.indexPV(targentCDX, INDEX_COUPON, YIELD_CURVE, adjCurves, PriceType.DIRTY) * NOTIONAL;

    System.out.println(cleanPrice);
    System.out.println(dirtyPrice);  // agree with 1 - PRICES[pos]

    //    ISDACompliantCreditCurve creditCurve = (new FastCreditCurveBuilder(OG_FIX)).calibrateCreditCurve(targentCDX,
    //        INDEX_COUPON, YIELD_CURVE, indexPUF[pos]);
    //    double pv = PRICER_OG_FIX.pv(targentCDX, YIELD_CURVE, creditCurve, INDEX_COUPON) * NOTIONAL;
    //    double pvDirty = PRICER_OG_FIX.pv(targentCDX, YIELD_CURVE, creditCurve, INDEX_COUPON, PriceType.DIRTY) * NOTIONAL;
    //    System.out.println(pv);
    //    System.out.println(pvDirty);

    int accruedDays = targentCDX.getAccuredDays();
    double accruedPremium = targentCDX.getAccruedPremium(INDEX_COUPON);
    System.out.println(accruedDays);
    System.out.println(accruedPremium);

//    ISDACompliantYieldCurve ycUP = bumpYieldCurve(YIELD_CURVE, ONE_BP);
//    double cleanPriceUp = INDEX_CAL.indexPV(targentCDX, INDEX_COUPON, ycUP, adjCurves) * NOTIONAL;
//    System.out.println((cleanPriceUp - cleanPrice) * ONE_BP);
//    double IR01FromSingleCurve = IR_CAL.parallelIR01(targentCDX, INDEX_COUPON, creditCurve, YIELD_CURVE) * ONE_BP *
//        NOTIONAL;
//    System.out.println(IR01FromSingleCurve);
    
    double weightedCleanRPV01 = INDEX_CAL.indexAnnuity(targentCDX, YIELD_CURVE, adjCurves);
    System.out.println(weightedCleanRPV01);
    double weightedDirtyRPV01 = INDEX_CAL.indexAnnuity(targentCDX, YIELD_CURVE, adjCurves, PriceType.DIRTY);
    System.out.println(weightedDirtyRPV01);
    double durationWeightedAverageSpread = INDEX_CAL.intrinsicIndexSpread(targentCDX, YIELD_CURVE, adjCurves) *
        TEN_THOUSAND;
    System.out.println(durationWeightedAverageSpread);

    double expectedLoss = INDEX_CAL.expectedDefaultSettlementValue(targentCDX.getProtectionEnd(), adjCurves) * NOTIONAL;
    System.out.println(expectedLoss);

    //    adjCurves.withDefault(n)
  }

  //  JTD - per-name report for instantaneous default for index trades (decomposed)
  //  Recovery01 - should apply to curve, trade or curve + trade simultaneously.

  //

  //  private ISDACompliantYieldCurve bumpYieldCurve(final ISDACompliantYieldCurve curve, final double bumpAmount) {
  //    final int n = curve.getNumberOfKnots();
  //    final double[] bumped = curve.getKnotZeroRates();
  //    for (int i = 0; i < n; i++) {
  //      bumped[i] += bumpAmount;
  //    }
  //    return curve.withRates(bumped);
  //  }
}
