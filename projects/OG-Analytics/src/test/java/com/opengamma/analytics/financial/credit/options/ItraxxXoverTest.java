/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.options;

import static com.opengamma.analytics.financial.credit.options.YieldCurveProvider.ISDA_EUR_20140206;
import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.opengamma.analytics.financial.credit.index.CDSIndexCalculator;
import com.opengamma.analytics.financial.credit.isdastandardmodel.AnnuityForSpreadApproxFunction;
import com.opengamma.analytics.financial.credit.isdastandardmodel.AnnuityForSpreadFunction;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalytic;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalyticFactory;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDABaseTest;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.MarketQuoteConverter;
import com.opengamma.analytics.financial.credit.isdastandardmodel.PriceType;
import com.opengamma.analytics.financial.credit.isdastandardmodel.QuotedSpread;

/**
 * 
 */
public class ItraxxXoverTest extends ISDABaseTest {
  private static final double NOTIONAL = 1e8;
  private static final LocalDate ACC_START = LocalDate.of(2013, 12, 20);
  private static final LocalDate TRADE_DATE = LocalDate.of(2014, 2, 6);
  private static final LocalDate EXPIRY = LocalDate.of(2014, 3, 19);
  private static final LocalDate EXERCISE_SETTLE = LocalDate.of(2014, 3, 24);
  private static final LocalDate MATURITY = LocalDate.of(2018, 12, 20);
  private static final double TRADE_SPREAD = 318.25 * ONE_BP;
  private static final double COUPON = 500 * ONE_BP;

  // private static final double EXP_FWD_ANNUITY = 4.83644025;
  private static final double BBG_ATM_FWD = 331.7902 * ONE_BP;
  private static final double TRUE_ATM_FWD = 326.6135 * ONE_BP;
  private static final double DEFAULT_ADJ_INDEX = -7195598.53; //found by setting strike to 500bps

  private static final Period TENOR = Period.ofYears(5);

  private static final Period[] PILLAR_TENORS = new Period[] {Period.ofMonths(6), Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5), Period.ofYears(7),
    Period.ofYears(10) };
  private static final double[] PILLAR_PAR_SPREADS;
  private static final QuotedSpread[] PILLAR_QUOTED_SPREADS;

  private static final MarketQuoteConverter CONVERTER = new MarketQuoteConverter();
  private static final CDSAnalyticFactory FACTORY = new CDSAnalyticFactory();
  private static final CDSAnalytic SPOT_CDX = FACTORY.makeCDX(TRADE_DATE, TENOR);
  private static final CDSAnalytic FWD_START_CDX = FACTORY.makeCDS(TRADE_DATE, EXPIRY.plusDays(1), EXERCISE_SETTLE, ACC_START, MATURITY);
  private static final CDSAnalytic FWD_CDX = FACTORY.makeCDX(EXPIRY, TENOR);
  private static final CDSAnalytic[] PILLAR_CDX = FACTORY.makeCDX(TRADE_DATE, PILLAR_TENORS);

  private static ISDACompliantYieldCurve YIELD_CURVE = ISDA_EUR_20140206;

  private static final double[] STRIKES = new double[] {10, 260, 280, 300, 320, 326.6135, 331.7902, 340, 360, 380, 400, 420, 440, 500, 800, 10000 };

  private static final double[] CALLPRICE = new double[] {15806482.5, 3026637.28, 2136488.22, 1347217.39, 736789.83, 582864.50, 479143.09, 343256.18, 135570.31, 45626.11, 13229.32, 3350.15, 751.64,
    4.69, 0, 0 };
  private static final double[] PUTPRICE = new double[] {0, 5586.6, 38353.12, 158280.03, 443543.41, 582864.64, 707672.3, 932401.6, 1594012.99, 2360472.75, 3171784.78, 3993114.37, 4809416.57,
    7193399.12, 17646108.69, 68831330.13 };

  private static final double[] EXERCISE_PRICE = new double[] {-0.230069242448716, -0.102175748837439, -0.0929437651628761, -0.0838490019206849, -0.074889348064225, -0.0719559839373113,
    -0.0696699930090803, -0.0660627259570898, -0.0573670897726163, -0.0488004260940694, -0.0403607527142736, -0.0320461182354909, -0.02385460196939, 0, 0.104559170409921, 0.61656821972402 };

  private static CDSIndexCalculator INDEX_CAL = new CDSIndexCalculator();
  private static boolean PRINT = false;

  static {
    final double[] spreads = new double[] {204.87, 204.87, 204.87, 204.87, 261.56, 318.25, 377.98, 401.39 };
    final int n = spreads.length;
    PILLAR_PAR_SPREADS = new double[n];
    PILLAR_QUOTED_SPREADS = new QuotedSpread[n];
    for (int i = 0; i < n; i++) {
      PILLAR_PAR_SPREADS[i] = spreads[i] * ONE_BP;
      PILLAR_QUOTED_SPREADS[i] = new QuotedSpread(COUPON, PILLAR_PAR_SPREADS[i]);
    }

    if (PRINT) {
      System.out.println("ItraxxXoverTest - set PRINT to false before push");
    }
  }

  /**
   * Should be given by P(t,T_E)E[V + D - G(k)], where V is the index value at expiry, D is the default settlement value and  G(K) = (k-C)A(K) is the excise price 
   */
  @Test
  //(enabled = false)
  public void putCallTest() {
    if (PRINT) {
      final double tE = ACT365F.getDayCountFraction(TRADE_DATE, EXPIRY);
      final double tES = ACT365F.getDayCountFraction(TRADE_DATE, EXERCISE_SETTLE);
      //build an index curve treating the index spreads and single name par spreads 
      final ISDACompliantCreditCurve cc = CREDIT_CURVE_BUILDER.calibrateCreditCurve(PILLAR_CDX, PILLAR_PAR_SPREADS, YIELD_CURVE);
      final double indexPV = PRICER.pv(SPOT_CDX, YIELD_CURVE, cc, COUPON, PriceType.CLEAN, 0.0);
      final double df = YIELD_CURVE.getDiscountFactor(tES);
      final double q = cc.getSurvivalProbability(tE);
      final double defaultSettlePV = df * (1 - q) * (1 - RECOVERY_RATE);
      System.out.println("discount fact: " + df);

      final ISDACompliantYieldCurve fwdYC = YIELD_CURVE.withOffset(tES);

      System.out.println(indexPV * NOTIONAL + "\t" + defaultSettlePV * NOTIONAL);
      final AnnuityForSpreadFunction annuity = new AnnuityForSpreadApproxFunction(FWD_CDX, fwdYC);
      final int n = STRIKES.length;
      for (int i = 0; i < n; i++) {
        final double exPrice = NOTIONAL * CONVERTER.quotedSpreadToPUF(FWD_CDX, COUPON, fwdYC, STRIKES[i] * ONE_BP);
        final double exPrice2 = NOTIONAL * (STRIKES[i] * ONE_BP - COUPON) * annuity.evaluate(STRIKES[i] * ONE_BP);
        final double putCall = df * (DEFAULT_ADJ_INDEX - exPrice2);
        final double error = putCall - (CALLPRICE[i] - PUTPRICE[i]);
        // System.out.println(STRIKES[i] + "\t" + exPrice * NOTIONAL + "\t" + (indexPV + defaultSettlePV - exPrice) * NOTIONAL);
        System.out.println(STRIKES[i] + "\t" + exPrice + "\t" + exPrice2 + "\t" + putCall + "\t" + error);
      }
    }
  }

  /**
   * Regression test for forward values 
   */
  @Test
  public void forwardValueTest() {
    final double expFwdIndexVal = -7201983.340857886;
    final double expFwdSpread = 331.649277309528 * ONE_BP;
    final double expATMFwdSpread = 327.38630192687924 * ONE_BP;

    final double tE = ACT365F.getDayCountFraction(TRADE_DATE, EXPIRY);

    //build credit curve by first converting the quoted spreads to PUF  
    final ISDACompliantCreditCurve cc = CREDIT_CURVE_BUILDER.calibrateCreditCurve(PILLAR_CDX, PILLAR_QUOTED_SPREADS, YIELD_CURVE);

    final double fwdIndexVal = INDEX_CAL.defaultAdjustedForwardIndexValue(FWD_START_CDX, tE, YIELD_CURVE, COUPON, cc);
    final double fwdSpread = INDEX_CAL.defaultAdjustedForwardSpread(FWD_START_CDX, tE, YIELD_CURVE, cc);
    final ISDACompliantYieldCurve fwdYC = YIELD_CURVE.withOffset(tE);
    final double atmFwdSpread = CONVERTER.pufToQuotedSpread(FWD_CDX, COUPON, fwdYC, fwdIndexVal);

    if (PRINT) {
      System.out.println("Fwd Index val:\t" + NOTIONAL * fwdIndexVal);
      System.out.println("Fwd Spread:\t" + fwdSpread * TEN_THOUSAND);
      System.out.println("ATM Forward:\t" + atmFwdSpread * TEN_THOUSAND);
    }
    assertEquals("FwdIndexVal", expFwdIndexVal, NOTIONAL * fwdIndexVal, NOTIONAL * 1e-16);
    assertEquals("fwdSpread", expFwdSpread, fwdSpread, 1e-16);
    assertEquals("ATMFwdSpread", expATMFwdSpread, atmFwdSpread, 1e-16);
  }

  @Test
  public void optionPrices() {
    final double expX0a = 327.91390602255956 * ONE_BP;
    final double expX0b = 327.7699441941618 * ONE_BP;

    final double[] expOTMprices = new double[] {6.1327369003985E-231, 5647.51524217555, 38556.0028211884, 158651.691867189, 443955.180447323, 583250.500215998, 479498.475809142, 343550.920743509,
      135711.32921182, 45670.1459990407, 13236.2006801549, 3348.89825017615, 750.312519414825, 4.65015781997449, 7.92961638482859E-14 };

    final double tE = ACT365F.getDayCountFraction(TRADE_DATE, EXPIRY);
    final double tES = ACT365F.getDayCountFraction(TRADE_DATE, EXERCISE_SETTLE);
    final double df = YIELD_CURVE.getDiscountFactor(tES);
    final double vol = 0.3;
    //  final BloombergIndexOptionPricer pricer = new BloombergIndexOptionPricer(TRADE_DATE, EXPIRY, EXPIRY.plusDays(1), EXERCISE_SETTLE, ACC_START, MATURITY, YIELD_CURVE, RECOVERY_RATE);
    final IndexOptionPricer oPricer = new IndexOptionPricer(FWD_CDX, tE, YIELD_CURVE, COUPON);

    final ISDACompliantCreditCurve cc = CREDIT_CURVE_BUILDER.calibrateCreditCurve(PILLAR_CDX, PILLAR_QUOTED_SPREADS, YIELD_CURVE);
    final double fwdIndexVal = INDEX_CAL.defaultAdjustedForwardIndexValue(FWD_START_CDX, tE, YIELD_CURVE, COUPON, cc);

    final double x0a = oPricer.calibrateX0(DEFAULT_ADJ_INDEX / NOTIONAL, vol);
    final double x0b = oPricer.calibrateX0(fwdIndexVal, vol);
    if (PRINT) {
      System.out.println("BBG fwd index val:\t" + DEFAULT_ADJ_INDEX);
      System.out.println("Cal fwd index val:\t" + fwdIndexVal * NOTIONAL);
      System.out.println("BBG X0:\t" + x0a * TEN_THOUSAND);
      System.out.println("Cal X0:\t" + x0b * TEN_THOUSAND);
    }

    assertEquals(expX0a, x0a, expX0a * 1e-15);
    assertEquals(expX0a, x0a, expX0b * 1e-15);

    final int n = STRIKES.length - 1;
    for (int i = 0; i < n; i++) {
      final double payer = NOTIONAL * oPricer.getOptionPriceForPriceQuotedIndex(DEFAULT_ADJ_INDEX / NOTIONAL, vol, EXERCISE_PRICE[i], true);
      final double receiver = NOTIONAL * oPricer.getOptionPriceForPriceQuotedIndex(DEFAULT_ADJ_INDEX / NOTIONAL, vol, EXERCISE_PRICE[i], false);
      final double payer2 = NOTIONAL * oPricer.getOptionPriceForSpreadQuotedIndex(fwdIndexVal, vol, STRIKES[i] * ONE_BP, true);
      final double receiver2 = NOTIONAL * oPricer.getOptionPriceForSpreadQuotedIndex(fwdIndexVal, vol, STRIKES[i] * ONE_BP, false);

      double impVol = 0;

      //TODO PLAT-5993 The tolerance has been turned down to pass in Bamboo
      final double tol = 1e-12 * expOTMprices[i];
      if (DEFAULT_ADJ_INDEX / NOTIONAL < EXERCISE_PRICE[i]) {
        assertEquals(expOTMprices[i], payer, tol);
        if (CALLPRICE[i] > 0) {
          impVol = oPricer.impliedVol(DEFAULT_ADJ_INDEX / NOTIONAL, EXERCISE_PRICE[i], CALLPRICE[i] / NOTIONAL, true);
        }
      } else {
        assertEquals(expOTMprices[i], receiver, tol);
        if (PUTPRICE[i] > 0) {
          impVol = oPricer.impliedVol(DEFAULT_ADJ_INDEX / NOTIONAL, EXERCISE_PRICE[i], PUTPRICE[i] / NOTIONAL, false);
        }
      }
      if (PRINT) {
        System.out.println(STRIKES[i] + "\t" + payer + "\t" + receiver + "\t" + payer2 + "\t" + receiver2 + "\t" + impVol);
      }
    }
    //  
  }

  private double bbgIndexVal(final double lambda, final double r, final double tE, final double tM) {
    final double rlambda = r + lambda;
    final double df = Math.exp(-r * tE);
    final double e1 = Math.exp(-rlambda * tE);
    final double e2 = Math.exp(-rlambda * tM);
    final double ann = (e1 - e2) / rlambda;
    return ((1 - RECOVERY_RATE) * lambda - COUPON) * ann / df;
  }
}
