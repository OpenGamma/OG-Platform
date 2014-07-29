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
import com.opengamma.analytics.financial.credit.index.IntrinsicIndexDataBundle;
import com.opengamma.analytics.financial.credit.index.PortfolioSwapAdjustment;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalytic;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalyticFactory;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDABaseTest;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.MarketQuoteConverter;
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
  private static final IntrinsicIndexDataBundle INTRINSIC_DATA;
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
    INTRINSIC_DATA = new IntrinsicIndexDataBundle(CREDIT_CURVES, RECOVERY_RATES);
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
    final double price = INDEX_CAL.indexPV(cds, INDEX_COUPON, YIELD_CURVE, INTRINSIC_DATA);
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
    final IntrinsicIndexDataBundle adjCurves = PSA.adjustCurves(indexPrice, cds, INDEX_COUPON, YIELD_CURVE, INTRINSIC_DATA);
    final double price = INDEX_CAL.indexPUF(cds, INDEX_COUPON, YIELD_CURVE, adjCurves);
    assertEquals(indexPrice, price, 1e-15);
  }

  /**
   * Price an index option with homogeneous 'flat' credit curves for the individual CDS, and perform a regression test on the results. 
   */
  @Test
  public void indexOptionTest() {
    final double[] expOTMPrices = new double[] {4.1631007571372696E-5, 2.6973611070343375E-4, 9.295502243146243E-4, 0.0022070362751108927, 0.004085595156615987, 0.002640736254260279,
      0.0016694319443711508, 0.0010387325595910084, 6.392918916192971E-4, 3.907074536166385E-4, 2.3783431950800257E-4, 1.4453687319597615E-4, 8.784959620724592E-5, 5.347507099057006E-5,
      3.263367733083826E-5, 1.9981456126459862E-5, 1.2282675436274992E-5, 7.583278420259651E-6, 4.703919588472932E-6, 2.932259282757757E-6 };

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

    final IndexOptionPricer pricer = new IndexOptionPricer(fwdCDS, expiry, YIELD_CURVE, INDEX_COUPON);
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
        System.out.println(price + ",");
      }
      assertEquals(expOTMPrices[i], price, 1e-15);
    }
  }

  @Test
  public void putCallTest() {

    final LocalDate optionExpiry = TRADE_DATE.plusMonths(1);
    final CDSAnalytic fwdCDS = FACTORY.makeIMMCDS(TRADE_DATE, Period.ofYears(5));
    final CDSAnalytic fwdStartingCDS = FACTORY.makeForwardStartingIMMCDS(TRADE_DATE, optionExpiry, Period.ofYears(5));

    final double expiry = ACT365F.getDayCountFraction(TRADE_DATE, optionExpiry);
    final double atmFwd = -0.02;

    final IndexOptionPricer pricer = new IndexOptionPricer(fwdCDS, expiry, YIELD_CURVE, INDEX_COUPON);
    final double vol = 0.34;
    final double df = YIELD_CURVE.getDiscountFactor(fwdStartingCDS.getCashSettleTime());

    for (int i = 0; i < 20; i++) {
      final double exercisePrice = -0.04 + 0.1 * i / 19.;
      final double putCall = df * (atmFwd - exercisePrice);
      final double payer = pricer.getOptionPremium(atmFwd, vol, new ExerciseAmount(exercisePrice), true);
      final double reciver = pricer.getOptionPremium(atmFwd, vol, new ExerciseAmount(exercisePrice), false);
      //    System.out.println(exercisePrice + "\t" + payer + "\t" + reciver);
      assertEquals(payer - reciver, putCall, 1e-12);
    }

  }

  /**
   * This test the time to calculate an option price using two different methods to compute the annuity from a (flat/quoted) spread. 
   * This was run on R White's Mac Pro on 14/01/2014 using 200 warmups and 1000 hotspots 
   * Time using ISDA for annuity: 5.814947999999999ms
   * Time using approximation for annuity: 1.43962ms
   */
  @Test
  public void speedTest() {
    //this is repeated from above 
    final CDSAnalytic spotCDS = FACTORY.makeIMMCDS(TRADE_DATE, Period.ofYears(5));
    final double indexPrice = 0.021;
    final double f = 1.0; //the index factor
    //start by adjusting the curves to a single index value 
    final IntrinsicIndexDataBundle adjCurves = PSA.adjustCurves(indexPrice, spotCDS, INDEX_COUPON, YIELD_CURVE, INTRINSIC_DATA);

    final LocalDate optionExpiry = TRADE_DATE.plusMonths(1); //option expiry/(effective) start of protection is in 1 month
    final double timeToExpiry = ACT365F.getDayCountFraction(TRADE_DATE, optionExpiry);
    final CDSAnalytic fwdStartCDS = FACTORY.makeForwardStartingIMMCDS(TRADE_DATE, optionExpiry, Period.ofYears(5));
    final CDSAnalytic fwdCDS = FACTORY.makeIMMCDS(TRADE_DATE, Period.ofYears(5));
    final IndexOptionPricer pricerISDA = new IndexOptionPricer(fwdCDS, timeToExpiry, YIELD_CURVE, INDEX_COUPON, true);//this uses to ISDA model to compute annuity 
    final IndexOptionPricer pricerApprox = new IndexOptionPricer(fwdCDS, timeToExpiry, YIELD_CURVE, INDEX_COUPON); //this uses the credit triangle to compute annuity 

    final double atmFwdVal = INDEX_CAL.defaultAdjustedForwardIndexValue(fwdStartCDS, timeToExpiry, YIELD_CURVE, INDEX_COUPON, adjCurves);

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
    final IntrinsicIndexDataBundle adjCurves = PSA.adjustCurves(indexPrice, spotCDS, INDEX_COUPON, YIELD_CURVE, INTRINSIC_DATA);

    final LocalDate optionExpiry = TRADE_DATE.plusMonths(1); //option expiry/(effective) start of protection is in 1 month
    final double timeToExpiry = ACT365F.getDayCountFraction(TRADE_DATE, optionExpiry);
    final CDSAnalytic fwdStartCDS = FACTORY.makeForwardStartingIMMCDS(TRADE_DATE, optionExpiry, Period.ofYears(5));
    final CDSAnalytic fwdCDS = FACTORY.makeIMMCDS(TRADE_DATE, Period.ofYears(5));
    final IndexOptionPricer pricerISDA = new IndexOptionPricer(fwdCDS, timeToExpiry, YIELD_CURVE, INDEX_COUPON, true);//this uses to ISDA model to compute annuity 
    final IndexOptionPricer pricerApprox = new IndexOptionPricer(fwdCDS, timeToExpiry, YIELD_CURVE, INDEX_COUPON); //this uses the credit triangle to compute annuity 

    final double atmFwdVal = INDEX_CAL.defaultAdjustedForwardIndexValue(fwdStartCDS, timeToExpiry, YIELD_CURVE, INDEX_COUPON, adjCurves);

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
    final double[] expOTM = new double[] {9.076800764955738E-58, 3.4226674270476496E-53, 3.421305456107505E-49, 1.1578431585829338E-45, 1.5971443685030326E-42, 1.0368924201927302E-39,
      3.5488934506809077E-37, 7.013017170443694E-35, 8.614686159820895E-33, 6.989713086397245E-31, 3.9397404998361574E-29, 1.6092967109704648E-27, 4.937503232924944E-26, 1.1730934910278846E-24,
      2.2155898144875945E-23, 3.4025886679708334E-22, 4.333505166418396E-21, 4.6563803518661164E-20, 4.2854645659737556E-19, 3.4235577931791882E-18, 2.402256063322698E-17, 1.4961984880648425E-16,
      8.349609439174132E-16, 4.210233474792479E-15, 1.9328458757406006E-14, 8.134023739062972E-14, 3.15729001261722E-13, 1.1367368714819529E-12, 3.815542543731805E-12, 1.1995608173294043E-11,
      3.547357283025416E-11, 9.905947198985513E-11, 2.62149200845133E-10, 6.59616065568106E-10, 1.582854456644385E-9, 3.63256974036388E-9, 7.993468395411526E-9, 1.6906724004197517E-8,
      3.444534406470765E-8, 6.774232294873387E-8, 1.2885050063100898E-7, 2.374612436930876E-7, 4.2472177780647457E-7, 7.384252217101014E-7, 1.2497928944235257E-6, 2.0620462115765177E-6,
      3.3208584405433833E-6, 5.22664910774526E-6, 8.048489915315826E-6, 1.2139283310038822E-5, 1.7951546138525213E-5, 2.6053032280397427E-5, 3.7141442120650334E-5, 5.2056601696062696E-5,
      7.179001552326956E-5, 9.748982249311326E-5, 1.3046086022755916E-4, 1.7215897710912286E-4, 2.2417926032948268E-4, 2.882381110269799E-4, 3.6614946597432273E-4, 4.5979578000882737E-4,
      5.710947311635238E-4, 7.019627942388466E-4, 8.542770685297848E-4, 0.0010298368141732719, 0.001230326139299173, 0.0014572792580807967, 0.0017120495336512811, 0.0019957833508774444,
      0.0023093995935475906, 0.0026535752237636084, 0.0030287371954614726, 0.0034350606417693345, 0.003872473042891695, 0.004273779463476312, 0.003854085486068055, 0.0034654428042082794,
      0.0031069260932821844, 0.0027774504027998047, 0.0024757964184719925, 0.0022006362856210955, 0.0019505592571228772, 0.0017240964970243375, 0.0015197445557299247, 0.001335986936817134,
      0.0011713136864685823, 0.0010242385494673638, 8.93313737411912E-4, 7.771422330342129E-4, 6.743876948715313E-4, 5.837820728043969E-4, 5.041310934645091E-4, 4.343178207319807E-4,
      3.733044819095235E-4, 3.2013281811392165E-4, 2.7392309091215267E-4, 2.338722206547276E-4, 1.9925083793707264E-4, 1.693998770868527E-4 };
    final CDSAnalytic spotCDS = FACTORY.makeIMMCDS(TRADE_DATE, Period.ofYears(5));
    //start by adjusting the curves to a single index value 
    final IntrinsicIndexDataBundle adjCurves = PSA.adjustCurves(indexPrice, spotCDS, INDEX_COUPON, YIELD_CURVE, INTRINSIC_DATA);

    final LocalDate optionExpiry = TRADE_DATE.plusMonths(1); //option expiry/(effective) start of protection is in 1 month
    final double timeToExpiry = ACT365F.getDayCountFraction(TRADE_DATE, optionExpiry);
    final CDSAnalytic fwdStartCDS = FACTORY.makeForwardStartingIMMCDS(TRADE_DATE, optionExpiry, Period.ofYears(5));
    final CDSAnalytic fwdCDS = FACTORY.makeIMMCDS(TRADE_DATE, Period.ofYears(5));
    final IndexOptionPricer pricerISDA = new IndexOptionPricer(fwdCDS, timeToExpiry, YIELD_CURVE, INDEX_COUPON, true);//this uses to ISDA model to compute annuity 
    final IndexOptionPricer pricerApprox = new IndexOptionPricer(fwdCDS, timeToExpiry, YIELD_CURVE, INDEX_COUPON); //this uses the credit triangle to compute annuity 

    //default three random names 
    final IntrinsicIndexDataBundle adjCurvesWithDefaults = adjCurves.withDefault(0, 34, 111);
    final double atmFwdVal = INDEX_CAL.defaultAdjustedForwardIndexValue(fwdStartCDS, timeToExpiry, YIELD_CURVE, INDEX_COUPON, adjCurvesWithDefaults);
    final double atmK = (new MarketQuoteConverter()).pufToQuotedSpread(fwdCDS, INDEX_COUPON, YIELD_CURVE.withOffset(timeToExpiry), atmFwdVal);

    final double vol = 0.5;
    for (int i = 0; i < 100; i++) {
      final double k = 0.002 + 0.02 * i / 100.;
      final double payer1 = pricerISDA.getOptionPriceForSpreadQuotedIndex(atmFwdVal, vol, k, true);
      final double payer2 = pricerApprox.getOptionPriceForSpreadQuotedIndex(atmFwdVal, vol, k, true);
      final double rec1 = pricerISDA.getOptionPriceForSpreadQuotedIndex(atmFwdVal, vol, k, false);
      final double rec2 = pricerApprox.getOptionPriceForSpreadQuotedIndex(atmFwdVal, vol, k, false);
      final double otm = pricerISDA.getOptionPriceForSpreadQuotedIndex(atmFwdVal, vol, k, k >= atmK);
      if (PRINT) {
        System.out.println(k + "\t" + payer1 + "\t" + payer2);
        //  System.out.println(otm + ",");
      }
      assertEquals(payer1, payer2, 1e-6 + payer1 * 1e-4);
      assertEquals(rec1, rec2, 1e-6 + rec1 * 1e-4);
      assertEquals(expOTM[i], otm, expOTM[i] * 1e-12);
    }

  }

}
