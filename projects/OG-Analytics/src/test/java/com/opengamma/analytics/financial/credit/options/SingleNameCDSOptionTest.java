/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.options;

import static com.opengamma.analytics.financial.credit.options.YieldCurveProvider.ISDA_USD_20140205;
import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalytic;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalyticFactory;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDABaseTest;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.MarketQuoteConverter;
import com.opengamma.analytics.financial.credit.isdastandardmodel.PriceType;
import com.opengamma.analytics.financial.credit.isdastandardmodel.QuotedSpread;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;

/**
 * These test look at options to enter a CDS on
 * Republic of Italy 
 * RED Pair code 4AB951AB1
 * Trade date 5-Feb-2014
 * 
 * The options on single name CDS on BBG CDSO screen appears to be an option to enter a bespoke CDS with a running coupon equal to the strike and accrual from T+1 
 * (i.e. a pre-`Big Bang' type CDS), so these can be priced using the annuity as the numeraire, thus can be priced with the Black formula.
 * However the test below show that BBG is using some other model. It is likely  the BBG is using its own proprietary model which would be impossible to replicate 
 * without more details.  
 */
public class SingleNameCDSOptionTest extends ISDABaseTest {
  private static final double NOTIONAL = 1e7;
  private static final LocalDate TRADE_DATE = LocalDate.of(2014, 2, 5);
  private static final LocalDate CASH_SETTLEMENT_DATE = LocalDate.of(2014, 2, 10);
  private static final LocalDate EXPIRY = LocalDate.of(2014, 3, 20);
  private static final LocalDate MATURITY = LocalDate.of(2019, 6, 20);
  private static final double TRADE_SPREAD = 173.66 * ONE_BP;
  private static final double COUPON = 100 * ONE_BP;

  private static final double EXP_FWD_ANNUITY = 4.83644025;
  private static final double FWD_SPREAD = 182.7666347 * ONE_BP;

  private static final Period TENOR = Period.ofYears(5);
  private static final Period[] PILLAR_TENORS = new Period[] {Period.ofMonths(6), Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5), Period.ofYears(7),
    Period.ofYears(10) };
  private static final double[] PILLAR_PAR_SPREADS;
  private static final QuotedSpread[] PILLAR_QUOTED_SPREADS;

  private static final MarketQuoteConverter CONVERTER = new MarketQuoteConverter();
  private static final CDSAnalyticFactory FACTORY = new CDSAnalyticFactory();
  private static final CDSAnalytic SPOT_CDS = FACTORY.makeIMMCDS(TRADE_DATE, TENOR);
  private static final CDSAnalytic[] PILLAR_CDS = FACTORY.makeIMMCDS(TRADE_DATE, PILLAR_TENORS);
  // private static final CDSAnalytic[] PILLAR_CDS = FACTORY.makeCDS(EXPIRY, EXPIRY.plusDays(1), getIMMDateSet(getNextIMMDate(TRADE_DATE), PILLAR_TENORS));

  private static ISDACompliantYieldCurve YIELD_CURVE = ISDA_USD_20140205;

  private static final double[] STRIKES = new double[] {100, 140, 150, 160, 170, 180, 182.767, 190, 200, 210, 220, 230, 250, 300 };
  private static final double[] MTM = new double[] {400295.95, 207773.36, 161867.8, 119561.3, 83049.12, 53969.18, 47325.4, 32746.86, 18566.8, 9863.46, 4929, 2327.63, 446.51, 3.6693 };

  private static boolean PRINT = false;

  static {
    final double[] spreads = new double[] {57.43, 74.97, 111.32, 139.32, 157.64, 173.66, 209.28, 228.35 };
    final int n = spreads.length;
    PILLAR_PAR_SPREADS = new double[n];
    PILLAR_QUOTED_SPREADS = new QuotedSpread[n];
    for (int i = 0; i < n; i++) {
      PILLAR_PAR_SPREADS[i] = spreads[i] * ONE_BP;
      PILLAR_QUOTED_SPREADS[i] = new QuotedSpread(COUPON, PILLAR_PAR_SPREADS[i]);
    }

    if (PRINT) {
      System.out.println("SingleNameCDSOptionTest - set PRINT to false before push");
    }
  }

  @Test
  public void upfrontModelTest() {
    final double puf = CONVERTER.quotedSpreadToPUF(SPOT_CDS, COUPON, YIELD_CURVE, TRADE_SPREAD);
    assertEquals(3.45676772, puf * ONE_HUNDRED, 1e-8);

    final double cs01 = NOTIONAL * ONE_BP * CS01_CAL.parallelCS01(SPOT_CDS, new QuotedSpread(COUPON, TRADE_SPREAD), YIELD_CURVE, ONE_BP);
    assertEquals(4547.5729663, cs01, 1e-7);
  }

  /**
   * BBG treats the pillar spreads (which are quoted spreads) as par spreads to build the credit curve   
   */
  @Test
  public void isdaFairValueTest() {
    final ISDACompliantCreditCurve cc = CREDIT_CURVE_BUILDER.calibrateCreditCurve(PILLAR_CDS, PILLAR_PAR_SPREADS, YIELD_CURVE);
    final double puf = PRICER.pv(SPOT_CDS, YIELD_CURVE, cc, COUPON);
    assertEquals(3.50365852774, puf * ONE_HUNDRED, 1e-11);
    final double cs01 = NOTIONAL * ONE_BP * CS01_CAL.parallelCS01FromCreditCurve(SPOT_CDS, COUPON, PILLAR_CDS, YIELD_CURVE, cc, ONE_BP);

    if (PRINT) {
      System.out.println("CS01:\t" + cs01);
    }
    //TODO clearly BBG do not calculate CS01 exactly the same way as us - hence this discrepancy 
    assertEquals(4607.92895, cs01, 2e-1); //0.2 out on notional of 10MM
  }

  @Test
  public void forwardPriceTest() {

    final double tE = ACT365F.getDayCountFraction(TRADE_DATE, EXPIRY);
    final double tCS = ACT365F.getDayCountFraction(TRADE_DATE, CASH_SETTLEMENT_DATE);
    final double dealSpread = 175 * ONE_BP;

    //The CDS 'seen' at expiry 
    final CDSAnalytic fwdStartingCDS = FACTORY.makeForwardStartingCDS(TRADE_DATE, EXPIRY, EXPIRY.plusDays(1), MATURITY);

    //fair value curve form par spreads
    final ISDACompliantCreditCurve cc = CREDIT_CURVE_BUILDER.calibrateCreditCurve(PILLAR_CDS, PILLAR_PAR_SPREADS, YIELD_CURVE);

    final double expFwdProt = PRICER.protectionLeg(fwdStartingCDS, YIELD_CURVE, cc, 0.0);
    final double expFwdAnnuity = PRICER.annuity(fwdStartingCDS, YIELD_CURVE, cc, PriceType.CLEAN, 0.0);
    final double fwdSpread = PRICER.parSpread(fwdStartingCDS, YIELD_CURVE, cc);
    assertEquals(fwdSpread, expFwdProt / expFwdAnnuity, 1e-15);

    if (PRINT) {
      System.out.println("Expected fwd annuity: " + expFwdAnnuity);
      System.out.println("Forward Par spread: " + fwdSpread * TEN_THOUSAND);
    }
    assertEquals("Expected fwd annuity", EXP_FWD_ANNUITY, expFwdAnnuity, 2e-3); //expressed per unit notional and unit coupon 
    assertEquals("Forward spread", FWD_SPREAD * TEN_THOUSAND, fwdSpread * TEN_THOUSAND, 4e-3); //it is not clear what BBG means exactly by ATM Fwd , but this is close; 4-1000th of a bps

    final double dfCS = YIELD_CURVE.getDiscountFactor(tCS);
    //this is the expected value of a CDS with a coupon of dealSpread on cash settlement date 
    final double cashAmt = NOTIONAL * (fwdSpread - dealSpread) * expFwdAnnuity / dfCS;
    if (PRINT) {
      System.out.println("cashAmt: " + cashAmt);
    }
    assertEquals("cashAmt", 37563.68, cashAmt, 21); //$21 out on notional of 10MM

    //price option with Black
    final double vol = 0.4;
    final double oPrice = NOTIONAL * expFwdAnnuity * BlackFormulaRepository.price(fwdSpread, dealSpread, tE, vol, true);
    if (PRINT) {
      System.out.println("option price: " + oPrice);
    }
    assertEquals(67521.44, oPrice, 1000); //option price out by $1000 when using Black formula with fwd spread and annuity we calculated 
  }

  /**
   * in this test we used the annuity and forward derived from the put-call parity of BBG option prices in the Black formula, and imply the volatility 
   * corresponding to the BBG price. We find a large discrepancy, which suggests BBG does not use the standard Black framework for these options
   */
  @Test(enabled = false)
  public void blackOptionTest() {
    final double tEAlt = ACT_ACT_ISDA.getDayCountFraction(TRADE_DATE, EXPIRY);
    System.out.println("time-to-expiry: " + tEAlt);
    final double vol = 0.4;
    final int n = STRIKES.length;
    for (int i = 0; i < n; i++) {
      final double p = NOTIONAL * EXP_FWD_ANNUITY * BlackFormulaRepository.price(FWD_SPREAD, STRIKES[i] * ONE_BP, tEAlt, vol, true);
      final double impVol = BlackFormulaRepository.impliedVolatility(MTM[i] / NOTIONAL / EXP_FWD_ANNUITY, FWD_SPREAD, STRIKES[i] * ONE_BP, tEAlt, true);
      // final double impVol2 = BlackFormulaRepository.impliedVolatility(p / NOTIONAL / EXP_FWD_ANNUITY, FWD_SPREAD, STRIKES[i] * ONE_BP, tEAlt, true);
      System.out.println(STRIKES[i] + "\t" + p + "\t" + impVol + "\t" + (p / MTM[i]));
      // assertEquals(vol, impVol, 2e-2);
    }

  }

  /**
   * In this test we used our own calculations for the forward and annuity in the Black formula. The same comments as above apply 
   */
  @Test
  public void blackOptionTest2() {
    final double tEAlt = ACT_ACT_ISDA.getDayCountFraction(TRADE_DATE, EXPIRY);
    //The CDS 'seen' at expiry 
    final CDSAnalytic fwdCDS = FACTORY.makeCDS(TRADE_DATE, EXPIRY.plusDays(1), EXPIRY.plusDays(1), EXPIRY.plusDays(1), MATURITY);

    //fair value curve form par spreads
    final ISDACompliantCreditCurve cc = CREDIT_CURVE_BUILDER.calibrateCreditCurve(PILLAR_CDS, PILLAR_PAR_SPREADS, YIELD_CURVE);

    final double expFwdProt = PRICER.protectionLeg(fwdCDS, YIELD_CURVE, cc, 0.0);
    final double expFwdAnnuity = PRICER.annuity(fwdCDS, YIELD_CURVE, cc, PriceType.CLEAN, 0.0);
    final double fwdSpread = expFwdProt / expFwdAnnuity;

    final double vol = 0.4;
    final int n = STRIKES.length;
    for (int i = 0; i < n; i++) {
      final double p = NOTIONAL * expFwdAnnuity * BlackFormulaRepository.price(fwdSpread, STRIKES[i] * ONE_BP, tEAlt, vol, true);
      final double p2 = NOTIONAL * EXP_FWD_ANNUITY * BlackFormulaRepository.price(FWD_SPREAD, STRIKES[i] * ONE_BP, tEAlt, vol, true);
      final double impVol = BlackFormulaRepository.impliedVolatility(MTM[i] / NOTIONAL / expFwdAnnuity, fwdSpread, STRIKES[i] * ONE_BP, tEAlt, true);
      final double impVol2 = BlackFormulaRepository.impliedVolatility(MTM[i] / NOTIONAL / EXP_FWD_ANNUITY, FWD_SPREAD, STRIKES[i] * ONE_BP, tEAlt, true);
      if (PRINT) {
        System.out.println(STRIKES[i] + "\t" + p + "\t" + p2 + "\t" + impVol + "\t" + impVol2);
      }
      if (i > 0) {
        assertEquals(vol, impVol, 2e-2);
      }
    }
  }
}
