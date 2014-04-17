/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.options;

import static com.opengamma.analytics.financial.credit.options.CDSIndexPrvider.CDX_NA_HY_20140213_PRICES;
import static com.opengamma.analytics.financial.credit.options.CDSIndexPrvider.CDX_NA_HY_20140213_RECOVERY_RATES;
import static com.opengamma.analytics.financial.credit.options.CDSIndexPrvider.CDX_NA_HY_21_COUPON;
import static com.opengamma.analytics.financial.credit.options.CDSIndexPrvider.CDX_NA_HY_21_NAMES;
import static com.opengamma.analytics.financial.credit.options.CDSIndexPrvider.CDX_NA_HY_21_RECOVERY_RATE;
import static com.opengamma.analytics.financial.credit.options.CDSIndexPrvider.INDEX_TENORS;
import static com.opengamma.analytics.financial.credit.options.CDSIndexPrvider.getCDX_NA_HY_20140213_CreditCurves;
import static com.opengamma.analytics.financial.credit.options.YieldCurveProvider.ISDA_USD_20140213;
import static com.opengamma.analytics.financial.credit.options.YieldCurveProvider.ISDA_USD_20140213_RATES;
import static com.opengamma.analytics.financial.credit.options.YieldCurveProvider.makeUSDBuilder;
import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.opengamma.analytics.financial.credit.index.CDSIndexCalculator;
import com.opengamma.analytics.financial.credit.index.IntrinsicIndexDataBundle;
import com.opengamma.analytics.financial.credit.index.PortfolioSwapAdjustment;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalytic;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalyticFactory;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDABaseTest;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurveBuild;
import com.opengamma.analytics.financial.credit.isdastandardmodel.PointsUpFront;
import com.opengamma.analytics.financial.credit.isdastandardmodel.PriceType;
import com.opengamma.analytics.math.MathException;
import com.opengamma.analytics.math.differentiation.FiniteDifferenceType;

/**
 * 
 */
public class CDXNAHYTest extends ISDABaseTest {
  private static final double NOTIONAL = 1e8;

  private static final LocalDate TRADE_DATE = LocalDate.of(2014, 2, 13);

  private static final LocalDate EXPIRY = LocalDate.of(2014, 3, 19);
  private static final LocalDate EXERCISE_SETTLE = LocalDate.of(2014, 3, 24);
  private static final LocalDate MATURITY = LocalDate.of(2018, 12, 20);
  private static final double PRICE = 107.62 * ONE_PC;

  private static final double INDEX_COUPON = CDX_NA_HY_21_COUPON;
  private static final double INDEX_RECOVERY = CDX_NA_HY_21_RECOVERY_RATE;

  // private static final double EXP_FWD_ANNUITY = 4.83644025;
  private static final double BBG_ATM_FWD = 107.1444434 * ONE_PC;
  private static final Period TENOR = Period.ofYears(5);

  private static final ISDACompliantCreditCurve[] CREDIT_CURVES = getCDX_NA_HY_20140213_CreditCurves();
  private static final double[] RECOVERY_RATES = CDX_NA_HY_20140213_RECOVERY_RATES;
  private static IntrinsicIndexDataBundle INTRINSIC_DATA = new IntrinsicIndexDataBundle(CREDIT_CURVES, RECOVERY_RATES);
  private static ISDACompliantYieldCurve YIELD_CURVE = ISDA_USD_20140213;
  private static double[] PRICES = CDX_NA_HY_20140213_PRICES;
  private static int INDEX_SIZE = RECOVERY_RATES.length;

  private static final PointsUpFront[] PILLAR_PUF;
  private static final CDSAnalyticFactory FACTORY = new CDSAnalyticFactory(INDEX_RECOVERY);
  private static final CDSAnalytic FWD_START_CDX = FACTORY.makeForwardStartingCDS(TRADE_DATE, EXPIRY, MATURITY);
  private static final CDSAnalytic FWD_CDX = FACTORY.makeCDX(EXPIRY, TENOR);
  private static final CDSAnalytic[] PILLAR_CDX = FACTORY.makeCDX(TRADE_DATE, INDEX_TENORS);

  private static final double[] STRIKES = new double[] {103, 104, 105, 106, 107.1444434, 108, 109, 110, 111 };
  private static final double[] CALLPRICE = new double[] {1050.57, 7281.74, 38630.22, 154451.94, 529900.91, 1053705.97, 1894442.5, 2859098.57, 3855162.16 };
  private static final double[] PUTPRICE = new double[] {4144889.03, 3151266.17, 2182760.61, 1298728.3, 529900.91, 198274.25, 39156.75, 3958.78, 168.34 };

  private static final PortfolioSwapAdjustment PSA = new PortfolioSwapAdjustment();
  private static final CDSIndexCalculator INDEX_CAL = new CDSIndexCalculator();

  private static final boolean PRINT = false;

  static {
    final int n = PRICES.length;
    PILLAR_PUF = new PointsUpFront[n];
    for (int i = 0; i < n; i++) {
      PILLAR_PUF[i] = new PointsUpFront(INDEX_COUPON, 1 - PRICES[i]);
    }

    if (PRINT) {
      System.out.println("CDXNAHYTest - set PRINT to false before push");
    }
  }

  /**
   * This computes the (default adjusted) forward spread and index value using a pseudo index credit curve and adjusted intrinsic curves 
   */
  @Test
  public void forwardValueTest() {
    final double tE = ACT365F.getDayCountFraction(TRADE_DATE, EXPIRY);
    final double tES = ACT365F.getDayCountFraction(TRADE_DATE, EXERCISE_SETTLE);
    final double expFwdPrice1 = -7149840.399010934 / NOTIONAL;
    final double expFwdPrice2 = -7121885.71427097 / NOTIONAL;
    final double expFwdPrice3 = -7150407.604015437 / NOTIONAL;
    final double expFwdSpread1 = 333.22161805754155 * ONE_BP;
    final double expFwdSpread2 = 333.5071399772611 * ONE_BP;
    final double expFwdSpread3 = 333.06693990756 * ONE_BP;

    //build (pseudo) index credit curve from PUF
    final ISDACompliantCreditCurve cc = CREDIT_CURVE_BUILDER.calibrateCreditCurve(PILLAR_CDX, PILLAR_PUF, YIELD_CURVE);

    final double q = cc.getSurvivalProbability(tE);
    final double df = YIELD_CURVE.getDiscountFactor(tES);

    final double fwdProt = PRICER.protectionLeg(FWD_START_CDX, YIELD_CURVE, cc, 0) + df * (1 - INDEX_RECOVERY) * (1 - q);
    final double fwdAnn = PRICER.annuity(FWD_START_CDX, YIELD_CURVE, cc, PriceType.CLEAN, 0);
    final double fwdSpread = fwdProt / fwdAnn;

    final double fwdSpread2 = INDEX_CAL.defaultAdjustedForwardSpread(FWD_START_CDX, tE, YIELD_CURVE, cc);
    assertEquals(fwdSpread, fwdSpread2, 1e-15);

    final double fwdIndexVal = ((fwdProt - INDEX_COUPON * fwdAnn) / df);
    final double fwdIndexVal2 = INDEX_CAL.defaultAdjustedForwardIndexValue(FWD_START_CDX, tE, YIELD_CURVE, INDEX_COUPON, cc);
    assertEquals(fwdIndexVal, fwdIndexVal2, 1e-15);

    if (PRINT) {
      System.out.println("df: " + df);
      System.out.println("Fwd Spread: " + fwdSpread * TEN_THOUSAND);
      System.out.println("Fwd Index val: " + NOTIONAL * fwdIndexVal);
    }

    final double fwdSpreadUnAdj = INDEX_CAL.defaultAdjustedForwardSpread(FWD_START_CDX, tE, YIELD_CURVE, INTRINSIC_DATA);
    final double fwdIndexValUnAdj = INDEX_CAL.defaultAdjustedForwardIndexValue(FWD_START_CDX, tE, YIELD_CURVE, INDEX_COUPON, INTRINSIC_DATA);

    final int n = PILLAR_PUF.length;
    final double[] indexPUF = new double[n];
    for (int i = 0; i < n; i++) {
      indexPUF[i] = PILLAR_PUF[i].getPointsUpFront();
    }
    final IntrinsicIndexDataBundle adjCurves = PSA.adjustCurves(indexPUF, PILLAR_CDX, INDEX_COUPON, YIELD_CURVE, INTRINSIC_DATA);
    final double fwdSpreadAdj = INDEX_CAL.defaultAdjustedForwardSpread(FWD_START_CDX, tE, YIELD_CURVE, adjCurves);
    final double fwdIndexValAdj = INDEX_CAL.defaultAdjustedForwardIndexValue(FWD_START_CDX, tE, YIELD_CURVE, INDEX_COUPON, adjCurves);

    if (PRINT) {//intrinsic 
      System.out.println("Intrinsic fwd spread before and after adjustment:\t" + fwdSpreadUnAdj * TEN_THOUSAND + "\t" + fwdSpreadAdj * TEN_THOUSAND);
      System.out.println("Intrinsic fwd value before and after adjustment:\t" + fwdIndexValUnAdj * NOTIONAL + "\t" + fwdIndexValAdj * NOTIONAL);
    }

    assertEquals("Regression test for index curve ATM Fwd", expFwdPrice1, fwdIndexVal2, 1e-15);
    assertEquals("Regression test for intrinic ATM Fwd", expFwdPrice2, fwdIndexValUnAdj, 1e-15);
    assertEquals("Regression test for adjusted intrinic ATM Fwd", expFwdPrice3, fwdIndexValAdj, 1e-15);
    assertEquals("Regression test for index curve ATM Fwd spread", expFwdSpread1, fwdSpread2, 1e-15);
    assertEquals("Regression test for intrinic ATM Fwd spread", expFwdSpread2, fwdSpreadUnAdj, 1e-15);
    assertEquals("Regression test for adjusted intrinic ATM Fwd spread", expFwdSpread3, fwdSpreadAdj, 1e-15);
  }

  @Test
  void forwardValueWithAccrualTest() {
    final LocalDate expiry = LocalDate.of(2014, 3, 18);
    final CDSAnalytic fwdStartCDS = FACTORY.makeForwardStartingCDS(TRADE_DATE, expiry, MATURITY);
    //  assertEquals(89, fwdStartCDS.getAccuredDays());

    final ISDACompliantCreditCurve cc = CREDIT_CURVE_BUILDER.calibrateCreditCurve(PILLAR_CDX, PILLAR_PUF, YIELD_CURVE);
    final double tE = ACT365F.getDayCountFraction(TRADE_DATE, expiry);
    final double fwdIndexVal = INDEX_CAL.defaultAdjustedForwardIndexValue(fwdStartCDS, tE, YIELD_CURVE, INDEX_COUPON, cc);

    final double defSet = INDEX_CAL.expectedDefaultSettlementValue(tE, cc, fwdStartCDS.getLGD());
    final double fwdIndexVal2 = defSet + PRICER.pv(fwdStartCDS, YIELD_CURVE, cc, INDEX_COUPON);

    if (PRINT) {
      System.out.println(fwdIndexVal * NOTIONAL);
      System.out.println(fwdIndexVal2 * NOTIONAL);
    }
    assertEquals(fwdIndexVal, fwdIndexVal2, NOTIONAL * 1e-15);
  }

  @Test
  void optionPriceTest() {
    final double expFwdVal = -0.07149840399010934;
    final double[] expPayer = new double[] {1166.48464873934, 7845.0135391454, 40556.7856883846, 158851.690745577, 536239.999079741, 1058884.2933964, 1896630.82108264, 2859437.33558545,
      3855042.43135933 };
    final double[] expRec = new double[] {4144837.70571472, 3151702.55128495, 2184600.64011402, 1303081.86185103, 536239.999079741, 203487.097861513, 41419.9422275726, 4412.77341021166,
      204.185863913313 };
    final double[] expVol = new double[] {0.296998752854268, 0.296883528058332, 0.296757503699748, 0.296619609888718, 0.296447951763999, 0.296292852663868, 0.296096569049928, 0.295877172385047,
      0.295628805272022 };

    final double tE = ACT365F.getDayCountFraction(TRADE_DATE, EXPIRY);
    final double tES = ACT365F.getDayCountFraction(TRADE_DATE, EXERCISE_SETTLE);

    final IndexOptionPricer oPricer = new IndexOptionPricer(FWD_CDX, tE, YIELD_CURVE, INDEX_COUPON);
    //build credit curve assuming par spreads
    final ISDACompliantCreditCurve cc = CREDIT_CURVE_BUILDER.calibrateCreditCurve(PILLAR_CDX, PILLAR_PUF, YIELD_CURVE);

    final double fwdIndexVal = INDEX_CAL.defaultAdjustedForwardIndexValue(FWD_START_CDX, tE, YIELD_CURVE, INDEX_COUPON, cc);
    assertEquals(expFwdVal, fwdIndexVal, 1e-15);
    final double vol = 0.3;

    if (PRINT) {
      final double df = YIELD_CURVE.getDiscountFactor(tES);
      final double x0 = oPricer.calibrateX0(fwdIndexVal, vol);
      System.out.println("df: " + df);
      System.out.println("Default-adj fwd index value: " + fwdIndexVal);
      System.out.println("X0: " + x0 * TEN_THOUSAND);
    }

    final int n = STRIKES.length;
    for (int i = 0; i < n; i++) {
      final double g = 1 - STRIKES[i] * ONE_PC;
      final double payer1 = oPricer.getOptionPriceForPriceQuotedIndex(1 - BBG_ATM_FWD, vol, g, true);
      final double recevier1 = oPricer.getOptionPriceForPriceQuotedIndex(1 - BBG_ATM_FWD, vol, g, false);
      final double payer2 = oPricer.getOptionPriceForPriceQuotedIndex(fwdIndexVal, vol, g, true);
      final double recevier2 = oPricer.getOptionPriceForPriceQuotedIndex(fwdIndexVal, vol, g, false);

      double vol1 = 0;
      double vol2 = 0;
      if (1 - BBG_ATM_FWD < g) {
        vol1 = oPricer.impliedVol(1 - BBG_ATM_FWD, g, CALLPRICE[i] / NOTIONAL, true);
        vol2 = oPricer.impliedVol(fwdIndexVal, g, CALLPRICE[i] / NOTIONAL, true);
      } else {
        vol1 = oPricer.impliedVol(1 - BBG_ATM_FWD, g, PUTPRICE[i] / NOTIONAL, false);
        vol2 = oPricer.impliedVol(fwdIndexVal, g, PUTPRICE[i] / NOTIONAL, false);
      }
      if (PRINT) {
        System.out.println(STRIKES[i] + "\t" + payer1 * NOTIONAL + "\t" + recevier1 * NOTIONAL + "\t" + payer2 * NOTIONAL + "\t" + recevier2 * NOTIONAL + "\t" + vol1 + "\t" + vol2);
      }
      assertEquals("Regression test for payer option", expPayer[i] / NOTIONAL, payer1, 1e-12);
      assertEquals("Regression test for reciver option", expRec[i] / NOTIONAL, recevier1, 1e-12);
      assertEquals("Regression test for implied vol", expVol[i], vol1, 1e-12);
    }
  }

  @Test
  public void cs01FlatTest() {
    final double[] expCS01 = new double[] {138.946475346697, 781.47013737074, 3269.03852433866, 9939.58490066654, 23642.6842859673, 34507.4999277713, 42497.1607278321, 45100.0176303277,
      45494.3878513171 };

    final double tE = ACT365F.getDayCountFraction(TRADE_DATE, EXPIRY);
    final CS01OptionCalculator cs01Cal = new CS01OptionCalculator();

    final double vol = 0.3;
    final CDSAnalytic cds = PILLAR_CDX[1]; //5Y cds 
    final PointsUpFront puf = PILLAR_PUF[1]; //the 5Y quote 

    for (int i = 0; i < STRIKES.length; i++) {
      final IndexOptionStrike strike = new ExerciseAmount(1 - STRIKES[i] * ONE_PC);
      final double cs01Payer = NOTIONAL * ONE_BP * cs01Cal.indexCurveApprox(FWD_CDX, tE, cds, puf, INDEX_COUPON, YIELD_CURVE, strike, vol, true, ONE_BP);
      final double cs01Rec = NOTIONAL * ONE_BP * cs01Cal.indexCurveApprox(FWD_CDX, tE, cds, puf, INDEX_COUPON, YIELD_CURVE, strike, vol, false, ONE_BP);
      if (PRINT) {
        System.out.println(STRIKES[i] + "\t" + cs01Payer + "\t" + cs01Rec);
      }
      assertEquals(expCS01[i], cs01Payer, NOTIONAL * 1e-15);
    }
  }

  @Test
  public void cs01Test() {
    final double[] expCS01 = new double[] {139.045084643814, 781.921221873803, 3270.49124500207, 9942.71206365506, 23646.9093111734, 34510.8082974714, 42498.5229611971, 45100.2751552152,
      45494.4025851437 };
    final double tE = ACT365F.getDayCountFraction(TRADE_DATE, EXPIRY);
    final CS01OptionCalculator cs01Cal = new CS01OptionCalculator();

    final double vol = 0.3;

    for (int i = 0; i < STRIKES.length; i++) {
      final IndexOptionStrike strike = new ExerciseAmount(1 - STRIKES[i] * ONE_PC);
      final double cs01Payer = NOTIONAL * ONE_BP * cs01Cal.indexCurveApprox(FWD_CDX, tE, PILLAR_CDX, PILLAR_PUF, INDEX_COUPON, YIELD_CURVE, strike, vol, true, ONE_BP);
      final double cs01Rec = NOTIONAL * ONE_BP * cs01Cal.indexCurveApprox(FWD_CDX, tE, PILLAR_CDX, PILLAR_PUF, INDEX_COUPON, YIELD_CURVE, strike, vol, false, ONE_BP);
      if (PRINT) {
        System.out.println(STRIKES[i] + "\t" + cs01Payer + "\t" + cs01Rec);
      }
      assertEquals(expCS01[i], cs01Payer, NOTIONAL * 1e-15);
    }
  }

  @Test
  public void cs01FullTest() {
    final double[] expCS01 = new double[] {138.863619033975, 781.087728296301, 3267.78959129945, 9936.82959737445, 23638.7271990648, 34504.0347198934, 42495.0858808288, 45098.7764730417,
      45493.3340286387 };
    final double tE = ACT365F.getDayCountFraction(TRADE_DATE, EXPIRY);
    final CS01OptionCalculator cs01Cal = new CS01OptionCalculator();

    final int n = PILLAR_PUF.length;
    final double[] indexPUF = new double[n];

    for (int i = 0; i < n; i++) {
      indexPUF[i] = PILLAR_PUF[i].getPointsUpFront();
    }
    final IntrinsicIndexDataBundle adjCurves = PSA.adjustCurves(indexPUF, PILLAR_CDX, INDEX_COUPON, YIELD_CURVE, INTRINSIC_DATA);

    final double vol = 0.3;
    final CDSAnalytic cdx = PILLAR_CDX[1]; //5Y 

    for (int i = 0; i < STRIKES.length; i++) {
      final IndexOptionStrike strike = new ExerciseAmount(1 - STRIKES[i] * ONE_PC);
      final double cs01Payer = NOTIONAL * ONE_BP * cs01Cal.fullCal(FWD_CDX, tE, cdx, INDEX_COUPON, YIELD_CURVE, adjCurves, strike, vol, true, ONE_BP);
      final double cs01Rec = NOTIONAL * ONE_BP * cs01Cal.fullCal(FWD_CDX, tE, cdx, INDEX_COUPON, YIELD_CURVE, adjCurves, strike, vol, false, ONE_BP);
      if (PRINT) {
        System.out.println(STRIKES[i] + "\t" + cs01Payer + "\t" + cs01Rec);
      }
      assertEquals(expCS01[i], cs01Payer, NOTIONAL * 1e-15);
    }
  }

  @Test
  public void deltaTest() {
    final double[] expDelta = new double[] {0.00289580626005673, 0.0164481880047495, 0.0694879134935995, 0.213319724045625, 0.512531360909954, 0.752647383327198, 0.931288304651875, 0.990256794878088,
      0.999316059617505 };

    final double tE = ACT365F.getDayCountFraction(TRADE_DATE, EXPIRY);
    final FiniteDifferenceGreekCalculator greekCal = new FiniteDifferenceGreekCalculator();

    final double eps = 1e-6;

    final int n = PILLAR_PUF.length;
    final double[] indexPUF = new double[n];
    for (int i = 0; i < n; i++) {
      indexPUF[i] = PILLAR_PUF[i].getPointsUpFront();
    }
    final IntrinsicIndexDataBundle adjCurves = PSA.adjustCurves(indexPUF, PILLAR_CDX, INDEX_COUPON, YIELD_CURVE, INTRINSIC_DATA);

    final double vol = 0.3;
    final CDSAnalytic cdx = PILLAR_CDX[1]; //5Y 
    for (int i = 0; i < STRIKES.length; i++) {
      final IndexOptionStrike strike = new ExerciseAmount(1 - STRIKES[i] * ONE_PC);
      final double deltaPayer = greekCal.delta(FWD_CDX, tE, cdx, INDEX_COUPON, YIELD_CURVE, adjCurves, strike, vol, true, eps, FiniteDifferenceType.CENTRAL);
      final double deltaRec = greekCal.delta(FWD_CDX, tE, cdx, INDEX_COUPON, YIELD_CURVE, adjCurves, strike, vol, false, eps, FiniteDifferenceType.CENTRAL);

      if (PRINT) {
        System.out.println(STRIKES[i] + "\t" + deltaPayer + "\t" + deltaRec);
      }
      //TODO [PLAT-5993] The accuracy has been turned down because different platforms produce different results. However this is a very high tolerance for this type of test.
      assertEquals(expDelta[i], deltaPayer, 1e-10);
    }
  }

  @Test
  public void deltaPrintTest() {
    if (PRINT) {

      final double tE = ACT365F.getDayCountFraction(TRADE_DATE, EXPIRY);
      final FiniteDifferenceGreekCalculator greekCal = new FiniteDifferenceGreekCalculator();

      final double eps = 1e-6;

      final int n = PILLAR_PUF.length;
      final double[] indexPUF = new double[n];
      for (int i = 0; i < n; i++) {
        indexPUF[i] = PILLAR_PUF[i].getPointsUpFront();
      }
      final IntrinsicIndexDataBundle adjCurves = PSA.adjustCurves(indexPUF, PILLAR_CDX, INDEX_COUPON, YIELD_CURVE, INTRINSIC_DATA);

      final double vol = 0.3;
      final CDSAnalytic cdx = PILLAR_CDX[1]; //5Y 
      for (int i = 0; i < 100; i++) {
        final double strikePrice = 102. + 10 * i / 99.;
        final IndexOptionStrike strike = new ExerciseAmount(1 - strikePrice * ONE_PC);
        final double deltaPayer = greekCal.delta(FWD_CDX, tE, cdx, INDEX_COUPON, YIELD_CURVE, adjCurves, strike, vol, true, eps, FiniteDifferenceType.CENTRAL);
        System.out.println(strikePrice + "\t" + deltaPayer);
      }
    }
  }

  @Test
  public void deltaByCS01Test() {
    final double[] expDelta = new double[] {0.00305222696988043, 0.0171664968361922, 0.0718107280135607, 0.218342127985126, 0.519357100911974, 0.7580237043069, 0.933531993475759, 0.99070875896559,
      0.999371860506079 };
    final double tE = ACT365F.getDayCountFraction(TRADE_DATE, EXPIRY);
    final FiniteDifferenceGreekCalculator greekCal = new FiniteDifferenceGreekCalculator();

    final double vol = 0.3;
    final CDSAnalytic cds = PILLAR_CDX[1]; //5Y cds 
    final PointsUpFront puf = PILLAR_PUF[1]; //the 5Y quote 

    for (int i = 0; i < STRIKES.length; i++) {
      final IndexOptionStrike strike = new ExerciseAmount(1 - STRIKES[i] * ONE_PC);
      final double deltaPayer = greekCal.deltaByCS01(FWD_CDX, tE, cds, puf, INDEX_COUPON, YIELD_CURVE, strike, vol, true, ONE_BP);
      final double deltaRec = greekCal.deltaByCS01(FWD_CDX, tE, cds, puf, INDEX_COUPON, YIELD_CURVE, strike, vol, false, ONE_BP);
      if (PRINT) {
        System.out.println(STRIKES[i] + "\t" + deltaPayer + "\t" + deltaRec);
      }
      assertEquals(expDelta[i], deltaPayer, 1e-12);
    }
  }

  @Test(enabled = false)
  public void gammaTest() {
    final double[] expGamma = new double[] {0.65886335151805, 3.04714161346829, 9.92606062448497, 21.6242635776853, 29.6437457913967, 23.4868756388006, 9.82867259802233, 1.91733434684593,
      0.149075196631542 };

    final double tE = ACT365F.getDayCountFraction(TRADE_DATE, EXPIRY);
    final FiniteDifferenceGreekCalculator greekCal = new FiniteDifferenceGreekCalculator();

    final double eps = 1e-6;

    final int n = PILLAR_PUF.length;
    final double[] indexPUF = new double[n];
    for (int i = 0; i < n; i++) {
      indexPUF[i] = PILLAR_PUF[i].getPointsUpFront();
    }
    final IntrinsicIndexDataBundle adjCurves = PSA.adjustCurves(indexPUF, PILLAR_CDX, INDEX_COUPON, YIELD_CURVE, INTRINSIC_DATA);

    final double vol = 0.3;
    final CDSAnalytic cdx = PILLAR_CDX[1]; //5Y 
    for (int i = 0; i < STRIKES.length; i++) {
      final IndexOptionStrike strike = new ExerciseAmount(1 - STRIKES[i] * ONE_PC);
      final double gammaPayer = greekCal.gamma(FWD_CDX, tE, cdx, INDEX_COUPON, YIELD_CURVE, adjCurves, strike, vol, true, eps);
      final double gammaRec = greekCal.gamma(FWD_CDX, tE, cdx, INDEX_COUPON, YIELD_CURVE, adjCurves, strike, vol, false, eps);
      if (PRINT) {
        System.out.println(STRIKES[i] + "\t" + gammaPayer + "\t" + gammaRec);
      }
      //TODO [PLAT-5993] The accuracy has been turned down because different platforms produce different results. However this is a very high tolerance for this type of test.
      assertEquals(expGamma[i], gammaPayer, 1e-3 * expGamma[i]);
    }
  }

  @Test
  public void gammaByCS01Test() {
    if (PRINT) {
      final double tE = ACT365F.getDayCountFraction(TRADE_DATE, EXPIRY);
      final FiniteDifferenceGreekCalculator greekCal = new FiniteDifferenceGreekCalculator();

      final double vol = 0.3;
      final CDSAnalytic cds = PILLAR_CDX[1]; //5Y cds 
      final PointsUpFront puf = PILLAR_PUF[1]; //the 5Y quote 

      for (final double element : STRIKES) {
        final IndexOptionStrike strike = new ExerciseAmount(1 - element * ONE_PC);
        final double gammaPayer = greekCal.gammaByCS01(FWD_CDX, tE, cds, puf, INDEX_COUPON, YIELD_CURVE, strike, vol, true);
        final double gammaRec = greekCal.gammaByCS01(FWD_CDX, tE, cds, puf, INDEX_COUPON, YIELD_CURVE, strike, vol, false);
        System.out.println(element + "\t" + gammaPayer + "\t" + gammaRec);
      }
    }
  }

  @Test
  public void gammaPrintTest() {
    if (PRINT) {
      final double tE = ACT365F.getDayCountFraction(TRADE_DATE, EXPIRY);
      final FiniteDifferenceGreekCalculator greekCal = new FiniteDifferenceGreekCalculator();

      final double eps = 1e-4;
      final CDSAnalytic cds = PILLAR_CDX[1]; //5Y cds 
      final PointsUpFront puf = PILLAR_PUF[1]; //the 5Y quote 

      final int n = PILLAR_PUF.length;
      final double[] indexPUF = new double[n];
      for (int i = 0; i < n; i++) {
        indexPUF[i] = PILLAR_PUF[i].getPointsUpFront();
      }
      final IntrinsicIndexDataBundle adjCurves = PSA.adjustCurves(indexPUF, PILLAR_CDX, INDEX_COUPON, YIELD_CURVE, INTRINSIC_DATA);

      final double vol = 0.3;
      final CDSAnalytic cdx = PILLAR_CDX[1]; //5Y 
      for (int i = 0; i < 100; i++) {

        // final double strikePrice = 110. + 1 * i / 99.;
        final double strikePrice = 102. + 10 * i / 99.;
        final IndexOptionStrike strike = new ExerciseAmount(1 - strikePrice * ONE_PC);
        final double gammaPayer = greekCal.gamma(FWD_CDX, tE, cdx, INDEX_COUPON, YIELD_CURVE, adjCurves, strike, vol, true, eps);
        final double gammaPayerCS01 = greekCal.gammaByCS01(FWD_CDX, tE, cds, puf, INDEX_COUPON, YIELD_CURVE, strike, vol, true, ONE_BP, 10 * ONE_BP);
        System.out.println(strikePrice + "\t" + gammaPayer + "\t" + gammaPayerCS01);
      }
    }
  }

  @Test
  public void vegaTest() {
    final double[] expVega = new double[] {396.574716989006, 1834.09584242344, 5974.56411533317, 13015.7901309657, 17842.7602031284, 14136.901958007, 5915.92153748407, 1154.027149417,
      89.7029953619661 };

    final double tE = ACT365F.getDayCountFraction(TRADE_DATE, EXPIRY);
    final FiniteDifferenceGreekCalculator greekCal = new FiniteDifferenceGreekCalculator();

    final double vol = 0.3;

    final int n = PILLAR_PUF.length;
    final double[] indexPUF = new double[n];
    for (int i = 0; i < n; i++) {
      indexPUF[i] = PILLAR_PUF[i].getPointsUpFront();
    }
    final IntrinsicIndexDataBundle adjCurves = PSA.adjustCurves(indexPUF, PILLAR_CDX, INDEX_COUPON, YIELD_CURVE, INTRINSIC_DATA);
    final double atmFwd = INDEX_CAL.defaultAdjustedForwardIndexValue(FWD_START_CDX, tE, YIELD_CURVE, INDEX_COUPON, adjCurves);
    for (int i = 0; i < STRIKES.length; i++) {
      final IndexOptionStrike strike = new ExerciseAmount(1 - STRIKES[i] * ONE_PC);
      final double vega1PC = NOTIONAL * ONE_PC * greekCal.vega(atmFwd, FWD_CDX, tE, INDEX_COUPON, YIELD_CURVE, strike, vol, true, ONE_PC, FiniteDifferenceType.FORWARD);
      final double vegaFD = NOTIONAL * ONE_HUNDRED * ONE_BP * greekCal.vega(atmFwd, FWD_CDX, tE, INDEX_COUPON, YIELD_CURVE, strike, vol, false, ONE_BP, FiniteDifferenceType.CENTRAL);
      if (PRINT) {
        System.out.println(STRIKES[i] + "\t" + vega1PC + "\t" + vegaFD);
      }
      assertEquals(expVega[i], vegaFD, 1e-15 * NOTIONAL);
    }
  }

  public void vegaPrintTest() {
    if (PRINT) {
      final double tE = ACT365F.getDayCountFraction(TRADE_DATE, EXPIRY);
      final FiniteDifferenceGreekCalculator greekCal = new FiniteDifferenceGreekCalculator();

      final double vol = 0.3;

      final int n = PILLAR_PUF.length;
      final double[] indexPUF = new double[n];
      for (int i = 0; i < n; i++) {
        indexPUF[i] = PILLAR_PUF[i].getPointsUpFront();
      }
      final IntrinsicIndexDataBundle adjCurves = PSA.adjustCurves(indexPUF, PILLAR_CDX, INDEX_COUPON, YIELD_CURVE, INTRINSIC_DATA);
      final double atmFwd = INDEX_CAL.defaultAdjustedForwardIndexValue(FWD_START_CDX, tE, YIELD_CURVE, INDEX_COUPON, adjCurves);
      for (int i = 0; i < 100; i++) {
        final double strikePrice = 102. + 10 * i / 99.;
        final IndexOptionStrike strike = new ExerciseAmount(1 - strikePrice * ONE_PC);
        final double vegaFD = NOTIONAL * ONE_HUNDRED * ONE_BP * greekCal.vega(atmFwd, FWD_CDX, tE, INDEX_COUPON, YIELD_CURVE, strike, vol, false, ONE_BP, FiniteDifferenceType.CENTRAL);
        System.out.println(strikePrice + "\t" + vegaFD);
      }
    }
  }

  @Test
  public void thetaTest() {
    final double[] expTheta = new double[] {-166.324597258476, -787.075048874065, -2610.1855614945, -5754.7887100487, -7928.53905375824, -6259.3422579046, -2580.19372964511, -480.551350985972,
      -20.0888668935861 };
    final double tE = ACT365F.getDayCountFraction(TRADE_DATE, EXPIRY);
    final FiniteDifferenceGreekCalculator greekCal = new FiniteDifferenceGreekCalculator();

    final double vol = 0.3;
    final double oneDay = 1 / 365.;

    final int n = PILLAR_PUF.length;
    final double[] indexPUF = new double[n];
    for (int i = 0; i < n; i++) {
      indexPUF[i] = PILLAR_PUF[i].getPointsUpFront();
    }
    final IntrinsicIndexDataBundle adjCurves = PSA.adjustCurves(indexPUF, PILLAR_CDX, INDEX_COUPON, YIELD_CURVE, INTRINSIC_DATA);
    final double atmFwd = INDEX_CAL.defaultAdjustedForwardIndexValue(FWD_START_CDX, tE, YIELD_CURVE, INDEX_COUPON, adjCurves);
    for (int i = 0; i < STRIKES.length; i++) {
      final IndexOptionStrike strike = new ExerciseAmount(1 - STRIKES[i] * ONE_PC);
      final double thetaPayer = NOTIONAL * oneDay * greekCal.thetaWithDefault(atmFwd, FWD_CDX, tE, INDEX_COUPON, YIELD_CURVE, strike, vol, true, oneDay);
      final double thetaRec = NOTIONAL * oneDay * greekCal.thetaWithDefault(atmFwd, FWD_CDX, tE, INDEX_COUPON, YIELD_CURVE, strike, vol, false, oneDay);
      if (PRINT) {
        System.out.println(STRIKES[i] + "\t" + thetaPayer + "\t" + thetaRec);
      }
      assertEquals(expTheta[i], thetaPayer, 1e-15 * NOTIONAL);
    }
  }

  @Test
  public void thetaWithoutDefaultTest() {
    final double[] expTheta = new double[] {-171.779302723284, -819.593563605345, -2752.95177577143, -6205.34070023028, -9030.51153313318, -7885.03115126178, -4590.44271716863, -2613.77608014867,
      -2171.22546743581 };

    final double tE = ACT365F.getDayCountFraction(TRADE_DATE, EXPIRY);
    final FiniteDifferenceGreekCalculator greekCal = new FiniteDifferenceGreekCalculator();

    final double oneDay = 1 / 365.;

    final int n = PILLAR_PUF.length;
    final double[] indexPUF = new double[n];
    for (int i = 0; i < n; i++) {
      indexPUF[i] = PILLAR_PUF[i].getPointsUpFront();
    }
    final IntrinsicIndexDataBundle adjCurves = PSA.adjustCurves(indexPUF, PILLAR_CDX, INDEX_COUPON, YIELD_CURVE, INTRINSIC_DATA);
    final double atmFwd = INDEX_CAL.defaultAdjustedForwardIndexValue(FWD_START_CDX, tE, YIELD_CURVE, INDEX_COUPON, adjCurves);
    //System.out.println("ATM FWD\t" + atmFwd);

    final double vol = 0.3;
    for (int i = 0; i < STRIKES.length; i++) {
      final IndexOptionStrike strike = new ExerciseAmount(1 - STRIKES[i] * ONE_PC);
      final double thetaPayer = NOTIONAL * oneDay * greekCal.thetaWithoutDefault(FWD_CDX, tE, INDEX_COUPON, YIELD_CURVE, adjCurves, strike, vol, true, oneDay);
      final double thetaRec = NOTIONAL * oneDay * greekCal.thetaWithoutDefault(FWD_CDX, tE, INDEX_COUPON, YIELD_CURVE, adjCurves, strike, vol, false, oneDay);
      if (PRINT) {
        System.out.println(STRIKES[i] + "\t" + thetaPayer + "\t" + thetaRec);
      }
      assertEquals(expTheta[i], thetaPayer, 1e-15 * NOTIONAL);
    }
  }

  @Test
  public void irDV01Test() {
    final double[] expIR01 = new double[] {1.53050161177664, 12.0242139330876, 65.5557026436111, 247.553870009976, 716.909943688972, 1166.39438932867, 1553.4092320784, 1696.56799845659,
      1713.3387819529 };

    final double tE = ACT365F.getDayCountFraction(TRADE_DATE, EXPIRY);
    final FiniteDifferenceGreekCalculator greekCal = new FiniteDifferenceGreekCalculator();

    final double[] rates = ISDA_USD_20140213_RATES;
    final ISDACompliantYieldCurveBuild ycBuilder = makeUSDBuilder(TRADE_DATE);

    final double vol = 0.3;
    final CDSAnalytic cdx = PILLAR_CDX[1]; //5Y 
    final double indexPUF = PILLAR_PUF[1].getPointsUpFront();
    for (int i = 0; i < STRIKES.length; i++) {
      final IndexOptionStrike strike = new ExerciseAmount(1 - STRIKES[i] * ONE_PC);
      final double ir01Payer = NOTIONAL * ONE_BP * greekCal.irDV01(FWD_CDX, tE, cdx, INDEX_COUPON, indexPUF, ycBuilder, rates, strike, vol, true, ONE_BP);
      final double ir01Rec = NOTIONAL * ONE_BP * greekCal.irDV01(FWD_CDX, tE, cdx, INDEX_COUPON, indexPUF, ycBuilder, rates, strike, vol, false, ONE_BP);
      if (PRINT) {
        System.out.println(STRIKES[i] + "\t" + ir01Payer + "\t" + ir01Rec);
      }
      assertEquals(expIR01[i], ir01Payer, 1e-15 * NOTIONAL);
    }

  }

  @Test
  public void irDV01FullTest() {
    final double[] expIR01 = new double[] {1.52646334094705, 12.0006324213253, 65.4562450567238, 247.258505507242, 716.252880789743, 1165.50127736789, 1552.39497727707, 1695.54046945928,
      1712.31479267827 };
    final double tE = ACT365F.getDayCountFraction(TRADE_DATE, EXPIRY);
    final FiniteDifferenceGreekCalculator greekCal = new FiniteDifferenceGreekCalculator();

    final double[] rates = ISDA_USD_20140213_RATES;
    final ISDACompliantYieldCurveBuild ycBuilder = makeUSDBuilder(TRADE_DATE);

    final int n = PILLAR_PUF.length;
    final double[] indexPUF = new double[n];
    for (int i = 0; i < n; i++) {
      indexPUF[i] = PILLAR_PUF[i].getPointsUpFront();
    }
    final IntrinsicIndexDataBundle adjCurves = PSA.adjustCurves(indexPUF, PILLAR_CDX, INDEX_COUPON, YIELD_CURVE, INTRINSIC_DATA);

    final double vol = 0.3;
    final CDSAnalytic cdx = PILLAR_CDX[1]; //5Y 
    for (int i = 0; i < STRIKES.length; i++) {
      final IndexOptionStrike strike = new ExerciseAmount(1 - STRIKES[i] * ONE_PC);
      final double ir01Payer = NOTIONAL * ONE_BP * greekCal.irDV01(FWD_CDX, tE, cdx, INDEX_COUPON, ycBuilder, rates, adjCurves, strike, vol, true, ONE_BP);
      final double ir01Rec = NOTIONAL * ONE_BP * greekCal.irDV01(FWD_CDX, tE, cdx, INDEX_COUPON, ycBuilder, rates, adjCurves, strike, vol, false, ONE_BP);
      if (PRINT) {
        System.out.println(STRIKES[i] + "\t" + ir01Payer + "\t" + ir01Rec);
      }
      assertEquals(expIR01[i], ir01Payer, 1e-15 * NOTIONAL);
    }
  }

  @Test
  public void defaultSenseTest() {
    if (PRINT) {
      final double tE = ACT365F.getDayCountFraction(TRADE_DATE, EXPIRY);
      final FiniteDifferenceGreekCalculator greekCal = new FiniteDifferenceGreekCalculator();
      final String[] names = CDX_NA_HY_21_NAMES;
      final double vol = 0.3;

      final int n = PILLAR_PUF.length;
      final double[] indexPUF = new double[n];
      for (int i = 0; i < n; i++) {
        indexPUF[i] = PILLAR_PUF[i].getPointsUpFront();
      }
      final IntrinsicIndexDataBundle adjCurves = PSA.adjustCurves(indexPUF, PILLAR_CDX, INDEX_COUPON, YIELD_CURVE, INTRINSIC_DATA);
      final double atmFwd = INDEX_CAL.defaultAdjustedForwardIndexValue(FWD_START_CDX, tE, YIELD_CURVE, INDEX_COUPON, adjCurves);

      final String[] exampleNames = new String[] {"The AES Corp", "BOMBARDIER INC.", "J C Penney Co Inc", "TX Competitive Elec Hldgs Co LLC" };
      final int nNames = exampleNames.length;
      final double[] atmFwdWithDefaults = new double[nNames];
      final List<String> listNames = Arrays.asList(names);
      int index = 0;
      System.out.print("Strike price");
      for (final String name : exampleNames) {
        System.out.print("\t" + name);
        final int pos = listNames.indexOf(name);
        if (pos < 0) {
          throw new MathException("cannot find " + name);
        }
        final IntrinsicIndexDataBundle withDefault = adjCurves.withDefault(pos);
        atmFwdWithDefaults[index++] = INDEX_CAL.defaultAdjustedForwardIndexValue(FWD_START_CDX, tE, YIELD_CURVE, INDEX_COUPON, withDefault);
      }
      System.out.print("\n");

      final IndexOptionPricer pricer = new IndexOptionPricer(FWD_CDX, tE, YIELD_CURVE, INDEX_COUPON);

      for (int i = 0; i < 100; i++) {
        final double strikePrice = 102. + 10 * i / 99.;
        System.out.print(strikePrice);
        final IndexOptionStrike strike = new ExerciseAmount(1 - strikePrice * ONE_PC);
        final double payer = pricer.getOptionPremium(atmFwd, vol, strike, true);
        for (int jj = 0; jj < nNames; jj++) {
          final double payerWithDefault = pricer.getOptionPremium(atmFwdWithDefaults[jj], vol, strike, true);
          final double diffPayer = payerWithDefault - payer;
          System.out.print("\t" + diffPayer);
        }
        System.out.print("\n");
      }
    }
  }

  @Test
  public void defaultSense2Test() {
    if (PRINT) {
      final double tE = ACT365F.getDayCountFraction(TRADE_DATE, EXPIRY);

      final double vol = 0.3;

      final int n = PILLAR_PUF.length;
      final double[] indexPUF = new double[n];
      for (int i = 0; i < n; i++) {
        indexPUF[i] = PILLAR_PUF[i].getPointsUpFront();
      }
      final ISDACompliantCreditCurve indexCurve = CREDIT_CURVE_BUILDER.calibrateCreditCurve(PILLAR_CDX, PILLAR_PUF, YIELD_CURVE);
      final double defSettle = (1 - INDEX_RECOVERY) / INDEX_SIZE;

      final double atmFwd = INDEX_CAL.defaultAdjustedForwardIndexValue(FWD_START_CDX, tE, YIELD_CURVE, INDEX_COUPON, indexCurve);
      final double atmFwdWithDefault = INDEX_CAL.defaultAdjustedForwardIndexValue(FWD_START_CDX, tE, INDEX_SIZE, YIELD_CURVE, INDEX_COUPON, indexCurve, defSettle, 1);
      final IndexOptionPricer pricer = new IndexOptionPricer(FWD_CDX, tE, YIELD_CURVE, INDEX_COUPON);

      for (int i = 0; i < 100; i++) {
        final double strikePrice = 102. + 10 * i / 99.;
        System.out.print(strikePrice);
        final IndexOptionStrike strike = new ExerciseAmount(1 - strikePrice * ONE_PC);
        final double payer = pricer.getOptionPremium(atmFwd, vol, strike, true);

        final double payerWithDefault = pricer.getOptionPremium(atmFwdWithDefault, vol, strike, true);
        final double diffPayer = payerWithDefault - payer;
        System.out.print("\t" + diffPayer);
        System.out.print("\n");
      }
    }
  }

}
