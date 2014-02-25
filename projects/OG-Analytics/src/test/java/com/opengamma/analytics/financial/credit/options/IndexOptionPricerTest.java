/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.options;

import static com.opengamma.analytics.financial.credit.isdastandardmodel.IMMDateLogic.getNextIMMDate;
import static com.opengamma.analytics.financial.credit.options.YieldCurveProvider.ISDA_EUR_20140206;
import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.analytics.financial.credit.index.CDSIndexCalculator;
import com.opengamma.analytics.financial.credit.index.PortfolioSwapAdjustment;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalytic;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalyticFactory;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDABaseTest;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.MarketQuoteConverter;
import com.opengamma.analytics.financial.credit.isdastandardmodel.PriceType;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;

/**
 * 
 */
public class IndexOptionPricerTest extends ISDABaseTest {
  protected static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister.DEFAULT_SEED);
  protected static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1, RANDOM);
  private static final CDSAnalyticFactory FACTORY = new CDSAnalyticFactory();

  private static final int INDEX_SIZE = 125;
  private static final double INDEX_COUPON = 0.01;
  private static final ISDACompliantCreditCurve[] CREDIT_CURVES;
  private static final ISDACompliantYieldCurve YIELD_CURVE = ISDA_EUR_20140206;
  private static final double[] RECOVERY_RATES;
  private static final LocalDate TRADE_DATE = LocalDate.of(2014, 02, 06);

  private static final PortfolioSwapAdjustment PSA = new PortfolioSwapAdjustment();
  private static final CDSIndexCalculator INDEX_CAL = new CDSIndexCalculator();

  private static final boolean PRINT = false;

  static {

    final double[] ccNodes = new double[] {0.5, 1, 3, 5, 7, 10 };
    CREDIT_CURVES = new ISDACompliantCreditCurve[INDEX_SIZE];
    RECOVERY_RATES = new double[INDEX_SIZE];
    final int nKnots = ccNodes.length;
    final double sigma1 = 0.7;
    final double sigma2 = 0.3;
    for (int i = 0; i < INDEX_SIZE; i++) {
      final double u = RANDOM.nextDouble();
      final double z = NORMAL.getInverseCDF(u);
      final double mean = 0.03 * Math.exp(sigma1 * z - sigma1 * sigma1 / 2);
      RECOVERY_RATES[i] = 0.9 - 0.7 * u;
      final double[] fwd = new double[nKnots];
      for (int jj = 0; jj < nKnots; jj++) {
        fwd[jj] = mean * Math.exp(sigma2 * NORMAL.nextRandom() - sigma2 * sigma2 / 2);
      }
      CREDIT_CURVES[i] = ISDACompliantCreditCurve.makeFromForwardRates(ccNodes, fwd);
    }

    if (PRINT) {
      System.out.println("IndexOptionPricerTest - set PRINT to false before push");
    }
  }

  /**
   * This is a pure regression test of the intrinsic value of the index 
   */
  @Test
  public void priceIndexTest() {
    final CDSAnalytic cds = FACTORY.makeIMMCDS(TRADE_DATE, Period.ofYears(5));
    final double price = INDEX_CAL.indexPV(cds, INDEX_COUPON, YIELD_CURVE, CREDIT_CURVES, RECOVERY_RATES, PriceType.CLEAN);
    final double expected = 0.02361802336968797; //clean index value (on cash settlement date)

    if (PRINT) {
      System.out.println(price);
    }
    assertEquals(expected, price, 1e-15);
  }

  @Test
  public void indexAdjTest() {

    final CDSAnalytic cds = FACTORY.makeIMMCDS(TRADE_DATE, Period.ofYears(5));
    final double indexPrice = 0.021;
    final ISDACompliantCreditCurve[] adjCurves = PSA.adjustCurves(cds, INDEX_COUPON, YIELD_CURVE, CREDIT_CURVES, RECOVERY_RATES, indexPrice);
    final double price = INDEX_CAL.indexPV(cds, INDEX_COUPON, YIELD_CURVE, adjCurves, RECOVERY_RATES, PriceType.CLEAN);
    assertEquals(indexPrice, price, 1e-15);
  }

  /**
   * Price an index option with homogeneous 'flat' credit curves for the individual CDS, and perform a regression test on the results. 
   */
  @Test
  public void indexOptionTest() {
    final double[] expOTMPrices = new double[] {4.55610170987198E-05, 0.000288207369087854, 0.000975586885207958, 0.00228489082751991, 0.00375653610502007, 0.00240528780926365, 0.00150669764092969,
      0.000929227360082338, 0.000567074849635796, 0.000343780572859934, 0.00020766068544309, 0.000125274546229225, 0.000075609018024543, 4.57162384920173E-05, 2.77202155938946E-05,
      1.68688672109296E-05, 1.03083567586636E-05, 6.32836586904037E-06, 3.90414765678112E-06, 2.42095954024808E-06 };

    final double lambda = 0.01;
    final ISDACompliantCreditCurve creditCurve = new ISDACompliantCreditCurve(1.0, lambda);
    final ISDACompliantCreditCurve[] creditCurves = new ISDACompliantCreditCurve[INDEX_SIZE];
    final double[] recoeryRates = new double[INDEX_SIZE];
    Arrays.fill(creditCurves, creditCurve);
    Arrays.fill(recoeryRates, RECOVERY_RATE);

    final double f = 1.0; //the index factor
    //start by adjusting the curves 

    final LocalDate optionExpiry = getNextIMMDate(TRADE_DATE).minusDays(1); //make expiry the next IMM - 1 (19/12/2013)
    final CDSAnalytic fwdCDS = FACTORY.makeIMMCDS(TRADE_DATE, Period.ofYears(5));
    final CDSAnalytic fwdStartingCDS = FACTORY.makeForwardStartingIMMCDS(TRADE_DATE, optionExpiry, Period.ofYears(5));

    final double expiry = ACT365F.getDayCountFraction(TRADE_DATE, optionExpiry);
    final double atmFwd = INDEX_CAL.defaultAdjustedForwardIndexValue(fwdStartingCDS, expiry, YIELD_CURVE, INDEX_COUPON, creditCurve);

    final IndexOptionPricer pricer = new IndexOptionPricer(fwdStartingCDS, expiry, YIELD_CURVE, INDEX_COUPON);
    final double vol = 1;
    final double atmPayer = pricer.getOptionPriceForPriceQuotedIndex(atmFwd, vol, atmFwd, true);
    final double atmRec = pricer.getOptionPriceForPriceQuotedIndex(atmFwd, vol, atmFwd, false);
    assertEquals(atmPayer, atmRec);
    final double atmK = (new MarketQuoteConverter()).pufToQuotedSpread(fwdCDS, INDEX_COUPON, YIELD_CURVE.withOffset(expiry), atmFwd);

    for (int i = 0; i < 20; i++) {
      final double k = 0.003 + 0.015 * i / 19.;
      final boolean isPayer = k >= atmK;
      final double price = pricer.getOptionPriceForSpreadQuotedIndex(atmFwd, vol, k, isPayer);

      if (PRINT) {
        System.out.println(price);
      }
      assertEquals(expOTMPrices[i], price, 1e-15);
    }
  }

  /**
   * This test the time to calculate an option price using two different methods to compute the annuity from a (flat/quoted) spread. 
   * This was run on R White's Mac Pro on 14/01/2014 using 200 warmups and 1000 hotspots 
   * Time using ISDA for annuity: 11.694420000000001ms
   * Time using approximation for annuity: 1.7096709999999997ms
   */
  @Test
  public void speedTest() {
    //this is repeated from above 
    final CDSAnalytic spotCDS = FACTORY.makeIMMCDS(TRADE_DATE, Period.ofYears(5));
    final double indexPrice = 0.021;
    final double f = 1.0; //the index factor
    //start by adjusting the curves to a single index value 
    final ISDACompliantCreditCurve[] adjCurves = PSA.adjustCurves(spotCDS, INDEX_COUPON, YIELD_CURVE, CREDIT_CURVES, RECOVERY_RATES, indexPrice);

    final LocalDate optionExpiry = TRADE_DATE.plusMonths(1); //option expiry/(effective) start of protection is in 1 month
    final double timeToExpiry = ACT365F.getDayCountFraction(TRADE_DATE, optionExpiry);
    final CDSAnalytic fwdStartCDS = FACTORY.makeForwardStartingIMMCDS(TRADE_DATE, optionExpiry, Period.ofYears(5));
    final CDSAnalytic fwdCDS = FACTORY.makeIMMCDS(TRADE_DATE, Period.ofYears(5));
    final IndexOptionPricer pricerISDA = new IndexOptionPricer(fwdCDS, timeToExpiry, YIELD_CURVE, INDEX_COUPON, true);//this uses to ISDA model to compute annuity 
    final IndexOptionPricer pricerApprox = new IndexOptionPricer(fwdCDS, timeToExpiry, YIELD_CURVE, INDEX_COUPON); //this uses the credit triangle to compute annuity 

    final double atmFwdVal = INDEX_CAL.defaultAdjustedForwardIndexValue(fwdStartCDS, timeToExpiry, INDEX_SIZE, YIELD_CURVE, INDEX_COUPON, adjCurves, RECOVERY_RATES);

    if (PRINT) {
      final int warmups = 200;
      final int hotspots = 1000;

      for (int i = 0; i < warmups; i++) {
        final double vol = RANDOM.nextDouble();
        final double k = 0.005 + 0.02 * RANDOM.nextDouble();
        final double p = pricerISDA.getOptionPriceForSpreadQuotedIndex(atmFwdVal, vol, k, true);
      }

      long t0 = System.nanoTime();
      for (int i = 0; i < hotspots; i++) {
        final double vol = RANDOM.nextDouble();
        final double k = 0.005 + 0.02 * RANDOM.nextDouble();
        final double p = pricerISDA.getOptionPriceForSpreadQuotedIndex(atmFwdVal, vol, k, true);
      }
      long t1 = System.nanoTime();
      System.out.println("Time using ISDA for annuity: " + (t1 - t0) * 1e-6 / hotspots + "ms");

      for (int i = 0; i < warmups; i++) {
        final double vol = RANDOM.nextDouble();
        final double k = 0.005 + 0.02 * RANDOM.nextDouble();
        final double p = pricerApprox.getOptionPriceForSpreadQuotedIndex(atmFwdVal, vol, k, true);
      }

      t0 = System.nanoTime();
      for (int i = 0; i < hotspots; i++) {
        final double vol = RANDOM.nextDouble();
        final double k = 0.005 + 0.02 * RANDOM.nextDouble();
        final double p = pricerApprox.getOptionPriceForSpreadQuotedIndex(atmFwdVal, vol, k, true);
      }
      t1 = System.nanoTime();
      System.out.println("Time using approximation for annuity: " + (t1 - t0) * 1e-6 / hotspots + "ms");
    }
  }

  /**
   * Check options priced with the annuity approximation are close to those using the ISDA model
   */
  @Test
  public void isdaVApproxTest() {
    final double indexPrice = 0.021;
    final CDSAnalytic spotCDS = FACTORY.makeIMMCDS(TRADE_DATE, Period.ofYears(5));
    //start by adjusting the curves to a single index value 
    final ISDACompliantCreditCurve[] adjCurves = PSA.adjustCurves(spotCDS, INDEX_COUPON, YIELD_CURVE, CREDIT_CURVES, RECOVERY_RATES, indexPrice);

    final LocalDate optionExpiry = TRADE_DATE.plusMonths(1); //option expiry/(effective) start of protection is in 1 month
    final double timeToExpiry = ACT365F.getDayCountFraction(TRADE_DATE, optionExpiry);
    final CDSAnalytic fwdStartCDS = FACTORY.makeForwardStartingIMMCDS(TRADE_DATE, optionExpiry, Period.ofYears(5));
    final CDSAnalytic fwdCDS = FACTORY.makeIMMCDS(TRADE_DATE, Period.ofYears(5));
    final IndexOptionPricer pricerISDA = new IndexOptionPricer(fwdCDS, timeToExpiry, YIELD_CURVE, INDEX_COUPON, true);//this uses to ISDA model to compute annuity 
    final IndexOptionPricer pricerApprox = new IndexOptionPricer(fwdCDS, timeToExpiry, YIELD_CURVE, INDEX_COUPON); //this uses the credit triangle to compute annuity 

    final double atmFwdVal = INDEX_CAL.defaultAdjustedForwardIndexValue(fwdStartCDS, timeToExpiry, INDEX_SIZE, YIELD_CURVE, INDEX_COUPON, adjCurves, RECOVERY_RATES);

    final double vol = 0.4;
    for (int i = 0; i < 100; i++) {
      final double k = 0.002 + 0.02 * i / 100.;
      final double payer1 = pricerISDA.getOptionPriceForSpreadQuotedIndex(atmFwdVal, vol, k, true);
      final double payer2 = pricerApprox.getOptionPriceForSpreadQuotedIndex(atmFwdVal, vol, k, true);
      final double rec1 = pricerISDA.getOptionPriceForSpreadQuotedIndex(atmFwdVal, vol, k, false);
      final double rec2 = pricerApprox.getOptionPriceForSpreadQuotedIndex(atmFwdVal, vol, k, false);
      if (PRINT) {
        System.out.println(k + "\t" + payer1 + "\t" + payer2);
      }
      assertEquals(payer1, payer2, 1e-6 + payer1 * 1e-4);
      assertEquals(rec1, rec2, 1e-6 + rec1 * 1e-4);
    }
  }

  @Test
  public void defaultedNamesTest() {
    //this is repeated from above 
    final double indexPrice = 0.021;
    final CDSAnalytic spotCDS = FACTORY.makeIMMCDS(TRADE_DATE, Period.ofYears(5));
    //start by adjusting the curves to a single index value 
    final ISDACompliantCreditCurve[] adjCurves = PSA.adjustCurves(spotCDS, INDEX_COUPON, YIELD_CURVE, CREDIT_CURVES, RECOVERY_RATES, indexPrice);

    final LocalDate optionExpiry = TRADE_DATE.plusMonths(1); //option expiry/(effective) start of protection is in 1 month
    final double timeToExpiry = ACT365F.getDayCountFraction(TRADE_DATE, optionExpiry);
    final CDSAnalytic fwdStartCDS = FACTORY.makeForwardStartingIMMCDS(TRADE_DATE, optionExpiry, Period.ofYears(5));
    final CDSAnalytic fwdCDS = FACTORY.makeIMMCDS(TRADE_DATE, Period.ofYears(5));
    final IndexOptionPricer pricerISDA = new IndexOptionPricer(fwdCDS, timeToExpiry, YIELD_CURVE, INDEX_COUPON, true);//this uses to ISDA model to compute annuity 
    final IndexOptionPricer pricerApprox = new IndexOptionPricer(fwdCDS, timeToExpiry, YIELD_CURVE, INDEX_COUPON); //this uses the credit triangle to compute annuity 

    //treat the last 3 names as defaulted. 
    final int nDefaulted = 3;
    double defaultedValue = 0.0;
    for (int i = INDEX_SIZE - 1; i > INDEX_SIZE - 1 - nDefaulted; i--) {
      defaultedValue += (1 - RECOVERY_RATES[i]);
    }
    defaultedValue /= INDEX_SIZE;
    final ISDACompliantCreditCurve[] nonDefaultedAdjCurves = new ISDACompliantCreditCurve[INDEX_SIZE - nDefaulted];
    final double[] nonDefaultedRR = new double[INDEX_SIZE - nDefaulted];
    System.arraycopy(adjCurves, 0, nonDefaultedAdjCurves, 0, INDEX_SIZE - nDefaulted);
    System.arraycopy(RECOVERY_RATES, 0, nonDefaultedRR, 0, INDEX_SIZE - nDefaulted);

    final double atmFwdVal = INDEX_CAL.defaultAdjustedForwardIndexValue(fwdStartCDS, timeToExpiry, INDEX_SIZE, YIELD_CURVE, INDEX_COUPON, nonDefaultedAdjCurves, nonDefaultedRR, defaultedValue);

    final double vol = 0.5;
    for (int i = 0; i < 100; i++) {
      final double k = 0.002 + 0.02 * i / 100.;
      final double payer1 = pricerISDA.getOptionPriceForSpreadQuotedIndex(atmFwdVal, vol, k, true);
      final double payer2 = pricerApprox.getOptionPriceForSpreadQuotedIndex(atmFwdVal, vol, k, true);
      final double rec1 = pricerISDA.getOptionPriceForSpreadQuotedIndex(atmFwdVal, vol, k, false);
      final double rec2 = pricerApprox.getOptionPriceForSpreadQuotedIndex(atmFwdVal, vol, k, false);
      if (PRINT) {
        System.out.println(k + "\t" + payer1 + "\t" + payer2);
      }
      assertEquals(payer1, payer2, 1e-6 + payer1 * 1e-4);
      assertEquals(rec1, rec2, 1e-6 + rec1 * 1e-4);
    }

  }

}
