/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.options;

import static com.opengamma.analytics.financial.credit.isdastandardmodel.IMMDateLogic.getNextIMMDate;
import static com.opengamma.analytics.financial.credit.options.CDSIndexPrvider.INDEX_TENORS;
import static com.opengamma.analytics.financial.credit.options.YieldCurveProvider.ISDA_EUR_20140206;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

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
import com.opengamma.analytics.financial.credit.isdastandardmodel.MarketQuoteConverter;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.util.test.TestGroup;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class BlackIndexOptionPricerTest extends ISDABaseTest {
  private static final CDSAnalyticFactory FACTORY = new CDSAnalyticFactory();
  private static final MarketQuoteConverter CONVERTER = new MarketQuoteConverter();

  private static final int INDEX_SIZE = 125;
  private static final double INDEX_COUPON = 0.01;
  private static final ISDACompliantCreditCurve[] CREDIT_CURVES;
  private static final ISDACompliantYieldCurve YIELD_CURVE = ISDA_EUR_20140206;
  private static final double[] RECOVERY_RATES;
  private static final IntrinsicIndexDataBundle INTRINSIC_DATA;
  private static final LocalDate TRADE_DATE = LocalDate.of(2014, 02, 06);
  private static final Period[] INDEX_PILLARS = INDEX_TENORS;

  private static final PortfolioSwapAdjustment PSA = new PortfolioSwapAdjustment();
  private static final CDSIndexCalculator INDEX_CAL = new CDSIndexCalculator();

  static {
    final double[] ccNodes = new double[] {0.5, 1.0, 3.0, 5.0, 7.0, 10.0 };
    final double[] fwdBase = new double[] {0.12, 0.1, 0.03, 0.02, 0.04, 0.06, };
    final int nNodes = ccNodes.length;
    CREDIT_CURVES = new ISDACompliantCreditCurve[INDEX_SIZE];
    RECOVERY_RATES = new double[INDEX_SIZE];
    for (int i = 0; i < INDEX_SIZE; i++) {
      RECOVERY_RATES[i] = 0.5 - 0.3 * Math.cos(i);
      final double[] fwd = new double[nNodes];
      for (int k = 0; k < nNodes; ++k) {
        fwd[k] = fwdBase[k] * (1. + 0.3 * Math.sin(i * k));
      }
      CREDIT_CURVES[i] = ISDACompliantCreditCurve.makeFromForwardRates(ccNodes, fwd);
    }
    INTRINSIC_DATA = new IntrinsicIndexDataBundle(CREDIT_CURVES, RECOVERY_RATES);
  }

  /**
   * 
   */
  @Test
  public void consistencyTest() {
    final double tol = 1.e-12;

    final LocalDate optionExpiry = getNextIMMDate(TRADE_DATE).minusDays(1);
    final double timeToExpiry = ACT365F.getDayCountFraction(TRADE_DATE, optionExpiry);
    final CDSAnalytic fwdCDX = FACTORY.makeCDX(optionExpiry, Period.ofYears(5));
    final CDSAnalytic fwdStartingCDX = fwdCDX.withOffset(timeToExpiry);
    //  final CDSAnalytic fwdStartingCDS = FACTORY.makeForwardStartingIMMCDS(TRADE_DATE, optionExpiry, Period.ofYears(5));

    final double[] indexPUF = new double[] {0.0556, 0.0582, 0.0771, 0.0652 };
    final CDSAnalytic[] indexCDS = FACTORY.makeCDX(TRADE_DATE, INDEX_PILLARS);

    final IntrinsicIndexDataBundle adjCurves = PSA.adjustCurves(indexPUF, indexCDS, INDEX_COUPON, YIELD_CURVE, INTRINSIC_DATA);
    final double fwdSpread = INDEX_CAL.defaultAdjustedForwardSpread(fwdStartingCDX, timeToExpiry, YIELD_CURVE, adjCurves);
    final double fwdAnnuity = INDEX_CAL.indexAnnuity(fwdStartingCDX, YIELD_CURVE, adjCurves);
    final BlackIndexOptionPricer pricerWithFwd = new BlackIndexOptionPricer(fwdCDX, timeToExpiry, YIELD_CURVE, INDEX_COUPON, fwdSpread, fwdAnnuity);
    final BlackIndexOptionPricer pricerNoFwd = new BlackIndexOptionPricer(fwdCDX, timeToExpiry, YIELD_CURVE, INDEX_COUPON, adjCurves);

    final boolean[] payer = new boolean[] {true, false };
    final double vol = 0.4;
    final double strikeSpread = 0.015;
    final ISDACompliantYieldCurve fwdYC = YIELD_CURVE.withOffset(timeToExpiry);
    final double strikePrice = CONVERTER.quotedSpreadToPUF(fwdCDX, INDEX_COUPON, fwdYC, strikeSpread);
    final IndexOptionStrike exPriceAmount = new ExerciseAmount(strikePrice);
    final IndexOptionStrike exSpreadAmount = new SpreadBasedStrike(strikeSpread);

    for (int i = 0; i < 2; ++i) {
      /**
       * Consistency between option pricing methods
       */
      final double premFromPrice = pricerWithFwd.getOptionPremium(exPriceAmount, vol, payer[i]);
      final double premFromSpread = pricerWithFwd.getOptionPremium(exSpreadAmount, vol, payer[i]);
      final double premFromPriceBare = pricerWithFwd.getOptionPriceForPriceQuotedIndex(strikePrice, vol, payer[i]);
      final double premFromSpreadBare = pricerWithFwd.getOptionPriceForSpreadQuotedIndex(strikeSpread, vol, payer[i]);
      assertEquals(premFromPrice, premFromSpread, tol);
      assertEquals(premFromSpread, premFromPriceBare, tol);
      assertEquals(premFromPriceBare, premFromSpreadBare, tol);

      /**
       * Consistency between constructors 
       */
      final double PremFromPriceNoFwd = pricerNoFwd.getOptionPremium(exPriceAmount, vol, payer[i]);
      assertEquals(premFromPrice, PremFromPriceNoFwd, tol);

      /**
       * Consistency with implied vol
       */
      final double vol1 = pricerWithFwd.getImpliedVolatility(exPriceAmount, premFromPrice, payer[i]);
      final double vol2 = pricerWithFwd.getImpliedVolatility(exSpreadAmount, premFromSpread, payer[i]);
      final double vol3 = pricerWithFwd.getImpliedVolForExercisePrice(strikePrice, premFromPriceBare, payer[i]);
      final double vol4 = pricerWithFwd.getImpliedVolForSpreadStrike(strikeSpread, premFromSpreadBare, payer[i]);
      assertEquals(vol, vol1, tol);
      assertEquals(vol, vol2, tol);
      assertEquals(vol, vol3, tol);
      assertEquals(vol, vol4, tol);
    }
  }

  /**
   * 
   */
  @SuppressWarnings("unused")
  @Test
  public void limitTest() {
    final LocalDate optionExpiry = getNextIMMDate(TRADE_DATE).minusDays(1);
    final double timeToExpiry = ACT365F.getDayCountFraction(TRADE_DATE, optionExpiry);
    final CDSAnalytic fwdCDX = FACTORY.makeCDX(optionExpiry, Period.ofYears(5));
    final CDSAnalytic fwdStartingCDX = fwdCDX.withOffset(timeToExpiry);

    final double[] indexPUF = new double[] {0.0556, 0.0582, 0.0771, 0.0652 };
    final CDSAnalytic[] indexCDS = FACTORY.makeCDX(TRADE_DATE, INDEX_PILLARS);

    final IntrinsicIndexDataBundle adjCurves = PSA.adjustCurves(indexPUF, indexCDS, INDEX_COUPON, YIELD_CURVE, INTRINSIC_DATA);
    final double fwdSpread = INDEX_CAL.defaultAdjustedForwardSpread(fwdStartingCDX, timeToExpiry, YIELD_CURVE, adjCurves);
    final double fwdAnnuity = INDEX_CAL.indexAnnuity(fwdStartingCDX, YIELD_CURVE, adjCurves);

    final BlackIndexOptionPricer pricerWithFwd = new BlackIndexOptionPricer(fwdCDX, timeToExpiry, YIELD_CURVE, INDEX_COUPON, fwdSpread, fwdAnnuity);
    final double modStrikeLimit = INDEX_COUPON + fwdCDX.getLGD() / fwdAnnuity;
    final double vol = 0.4;
    final double payLargeSpLimit = fwdAnnuity * BlackFormulaRepository.price(fwdSpread, modStrikeLimit, timeToExpiry, vol, true);
    final double recLargeSpLimit = fwdAnnuity * BlackFormulaRepository.price(fwdSpread, modStrikeLimit, timeToExpiry, vol, false);

    final double largeStrikeSp = 75.0;
    final double payLargeStrikeSp = pricerWithFwd.getOptionPriceForSpreadQuotedIndex(largeStrikeSp, vol, true);
    final double recLargeStrikeSp = pricerWithFwd.getOptionPriceForSpreadQuotedIndex(largeStrikeSp, vol, false);
    assertEquals(payLargeSpLimit, payLargeStrikeSp, 1.e-12);
    assertEquals(recLargeSpLimit, recLargeStrikeSp, 1.e-3);//larger strike spread ends up with failure in root-finding

    /**
     * Exception expected
     */
    final double negativeTime = -0.5;
    try {
      new BlackIndexOptionPricer(fwdCDX, negativeTime, YIELD_CURVE, INDEX_COUPON, fwdSpread, fwdAnnuity);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("timeToExpiry must be positive. Value given " + negativeTime, e.getMessage());
    }
    try {
      new BlackIndexOptionPricer(fwdStartingCDX, timeToExpiry, YIELD_CURVE, INDEX_COUPON, fwdSpread, fwdAnnuity);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("fwdCDS should be a Forward CDS", e.getMessage());
    }
    final double negativeCoupon = -150.0 * 1.0e-4;
    try {
      new BlackIndexOptionPricer(fwdCDX, timeToExpiry, YIELD_CURVE, negativeCoupon, fwdSpread, fwdAnnuity);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("indexCoupon must be positive", e.getMessage());
    }
    final double negativeFwdSp = -0.5;
    try {
      new BlackIndexOptionPricer(fwdCDX, timeToExpiry, YIELD_CURVE, INDEX_COUPON, negativeFwdSp, fwdAnnuity);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("defaultAdjustedFwdSpread must be positive", e.getMessage());
    }
    final double negativeAnn = -0.3;
    try {
      new BlackIndexOptionPricer(fwdCDX, timeToExpiry, YIELD_CURVE, INDEX_COUPON, fwdSpread, negativeAnn);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("pvFwdAnnuity must be positive", e.getMessage());
    }
    final double largeAnn = fwdCDX.getProtectionEnd() * 2.0;
    try {
      new BlackIndexOptionPricer(fwdCDX, timeToExpiry, YIELD_CURVE, INDEX_COUPON, fwdSpread, largeAnn);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("Value of annuity of " + largeAnn + " is greater than length (in years) of forward CDS. Annuity should be given for unit notional", e.getMessage());
    }

    final double smallStrike = -0.9;
    try {
      pricerWithFwd.getOptionPriceForPriceQuotedIndex(smallStrike, vol, true);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }
    final double largeStrike = 0.9;
    try {
      pricerWithFwd.getOptionPriceForPriceQuotedIndex(largeStrike, vol, true);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }
    try {
      pricerWithFwd.getOptionPriceForSpreadQuotedIndex(smallStrike, vol, true);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }
  }
}
