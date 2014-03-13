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
import static com.opengamma.financial.convention.businessday.BusinessDayDateUtils.addWorkDays;
import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.analytics.financial.credit.index.CDSIndexCalculator;
import com.opengamma.analytics.financial.credit.index.IntrinsicIndexDataBundle;
import com.opengamma.analytics.financial.credit.index.PortfolioSwapAdjustment;
import com.opengamma.analytics.financial.credit.isdastandardmodel.AnnuityForSpreadApproxFunction;
import com.opengamma.analytics.financial.credit.isdastandardmodel.AnnuityForSpreadContPemiumApproxFunction;
import com.opengamma.analytics.financial.credit.isdastandardmodel.AnnuityForSpreadFunction;
import com.opengamma.analytics.financial.credit.isdastandardmodel.AnnuityForSpreadISDAFunction;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalytic;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalyticFactory;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDABaseTest;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.MarketQuoteConverter;
import com.opengamma.analytics.math.MathException;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;

/**
 * These 'tests' produce the data for the figures in the paper "Forward CDS, Indices and Options"
 */
public class IndexOptionsPaperExamples extends ISDABaseTest {
  private static final MarketQuoteConverter CONVERTER = new MarketQuoteConverter();
  private static final CDSAnalyticFactory FACTORY = new CDSAnalyticFactory();
  protected static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister.DEFAULT_SEED);
  protected static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1, RANDOM);
  private static final int INDEX_SIZE = 125;
  private static final double INDEX_COUPON = 0.01;
  private static final ISDACompliantCreditCurve[] CREDIT_CURVES;
  private static final IntrinsicIndexDataBundle INTRINSIC_DATA;
  private static final ISDACompliantYieldCurve YIELD_CURVE;
  private static final double[] RECOVERY_RATES;
  private static final LocalDate TRADE_DATE = LocalDate.of(2014, 1, 14);
  private static final CDSAnalytic CDS_6M;
  private static final CDSAnalytic CDS_1Y;
  private static final CDSAnalytic CDS_3Y;
  private static final CDSAnalytic CDS_5Y;
  private static final CDSAnalytic CDS_7Y;
  private static final CDSAnalytic CDS_10Y;
  //  private static final CDSAnalytic CDS_1M_5Y; //forward starting CDS 1M into 5Y

  private static final PortfolioSwapAdjustment PSA = new PortfolioSwapAdjustment();
  private static final CDSIndexCalculator INDEX_CAL = new CDSIndexCalculator();

  static {
    CDS_6M = FACTORY.makeIMMCDS(TRADE_DATE, Period.ofMonths(6));
    CDS_1Y = FACTORY.makeIMMCDS(TRADE_DATE, Period.ofYears(1));
    CDS_3Y = FACTORY.makeIMMCDS(TRADE_DATE, Period.ofYears(3));
    CDS_5Y = FACTORY.makeIMMCDS(TRADE_DATE, Period.ofYears(5));
    CDS_7Y = FACTORY.makeIMMCDS(TRADE_DATE, Period.ofYears(7));
    CDS_10Y = FACTORY.makeIMMCDS(TRADE_DATE, Period.ofYears(10));

    final LocalDate spotDate = addWorkDays(TRADE_DATE.minusDays(1), 3, DEFAULT_CALENDAR);
    final String[] yieldCurvePoints = new String[] {"1M", "2M", "3M", "6M", "9M", "1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "11Y", "12Y", "15Y", "20Y", "25Y", "30Y" };
    final String[] yieldCurveInstruments = new String[] {"M", "M", "M", "M", "M", "M", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S" };
    final double[] rates = new double[] {0.00445, 0.009488, 0.012337, 0.017762, 0.01935, 0.020838, 0.01652, 0.02018, 0.023033, 0.02525, 0.02696, 0.02825, 0.02931, 0.03017, 0.03092, 0.0316, 0.03231,
      0.03367, 0.03419, 0.03411, 0.03412 };
    YIELD_CURVE = makeYieldCurve(TRADE_DATE, spotDate, yieldCurvePoints, yieldCurveInstruments, rates, ACT360, D30360, Period.ofYears(1));
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
  }

  /**
   * Risky Annuity Vs. Spread for 1Y and 10Y CDS
   */
  @Test(enabled = false)
  public void annuityForSpreadTest() {
    System.out.println("IndexOptionsPaperExamples.annuityForSpreadTest\n\n");

    final AnnuityForSpreadFunction func1 = new AnnuityForSpreadISDAFunction(CDS_1Y, YIELD_CURVE);
    final AnnuityForSpreadFunction func2 = new AnnuityForSpreadISDAFunction(CDS_10Y, YIELD_CURVE);
    System.out.println("Spread(bps)\t1Y\t10Y");
    for (int i = 0; i < 200; i++) {
      final double s = 0 + 3000. * i / 200;
      final double a1 = func1.evaluate(s * ONE_BP);
      final double a2 = func2.evaluate(s * ONE_BP);
      System.out.println(s + "\t" + a1 + "\t" + a2);
    }
  }

  /**
   * Risky Annuity Vs. Spread calculated using ISDA model and continuous premiums approximation 
   */
  @Test(enabled = false)
  public void annuityApproxTest() {
    System.out.println("IndexOptionsPaperExamples.annuityApproxTest\n\n");

    final AnnuityForSpreadFunction func1 = new AnnuityForSpreadISDAFunction(CDS_10Y, YIELD_CURVE);
    final AnnuityForSpreadFunction func2 = new AnnuityForSpreadContPemiumApproxFunction(CDS_10Y, YIELD_CURVE);
    final AnnuityForSpreadFunction func3 = new AnnuityForSpreadApproxFunction(CDS_10Y, YIELD_CURVE);
    System.out.println("Spread(bps)\tISDA\tApproximation1\tApproximation2");
    for (int i = 0; i < 200; i++) {
      final double s = 0 + 3000. * i / 200;
      final double a1 = func1.evaluate(s * ONE_BP);
      final double a2 = func2.evaluate(s * ONE_BP);
      final double a3 = func3.evaluate(s * ONE_BP);
      System.out.println(s + "\t" + a1 + "\t" + a2 + "\t" + a3);
    }
  }

  /**
   * payoff Vs index spread for 1M option to enter the 5Y index
   */
  @Test(enabled = false)
  public void payoffVsIndexSpread() {
    System.out.println("IndexOptionsPaperExamples.payoffVsIndexSpread\n\n");

    final double weight = 0.01; //index size 100, equal weighting 
    final double k = 0.012; //strike of 120bps
    final LocalDate optionExpiry = TRADE_DATE.plusMonths(1);
    final double time2Expiry = ACT365F.getDayCountFraction(TRADE_DATE, optionExpiry);
    final ISDACompliantYieldCurve fwdYC = YIELD_CURVE.withOffset(time2Expiry);
    //this is the 5Y cds on the option expiry date (14/2/2014) - since we havn't crossed a IMM date, this is the same CDS as 
    //the 5Y CDS today (14/1/2014)
    final CDSAnalytic fwdCDS = FACTORY.makeIMMCDS(optionExpiry, Period.ofYears(5));
    final double df = fwdYC.getDiscountFactor(fwdCDS.getCashSettleTime());
    final double coupon = 0.01;

    final AnnuityForSpreadFunction annFunc = new AnnuityForSpreadISDAFunction(fwdCDS, fwdYC);
    final double excisePrice = (k - coupon) * annFunc.evaluate(k);
    final int[] defaults = new int[] {0, 1, 2, 5, 10 };

    //payer
    for (int i = 0; i < 200; i++) {
      final double s = 0.00 + 0.02 * i / 200.;
      final double annuity = annFunc.evaluate(s);
      final double puf = (s - coupon) * annuity;
      System.out.print(s * TEN_THOUSAND);
      for (int j = 0; j < 5; j++) {
        final double f = 1 - defaults[j] * weight;
        final double defaultedValue = (1 - RECOVERY_RATE) * defaults[j] * weight;
        //discount the payoff from the exercise settlement date to the exercise date
        final double payoff = df * Math.max(0, defaultedValue + f * puf - excisePrice);
        System.out.print("\t" + payoff);
      }
      System.out.print("\n");
    }
    System.out.print("\n\n");

    //Receiver
    for (int i = 0; i < 200; i++) {
      final double s = 0.00 + 0.02 * i / 200.;
      final double annuity = annFunc.evaluate(s);
      final double puf = (s - coupon) * annuity;
      System.out.print(s * TEN_THOUSAND);
      for (int j = 0; j < 5; j++) {
        final double f = 1 - defaults[j] * weight;
        final double defaultedValue = (1 - RECOVERY_RATE) * defaults[j] * weight;
        final double payoff = df * Math.max(0, excisePrice - defaultedValue - f * puf);
        System.out.print("\t" + payoff);
      }
      System.out.print("\n");
    }
  }

  /**
   * The option price vs strike for the ISDA and approx way of calculating annuity 
   */
  @Test(enabled = false)
  public void isdaVApproxTest() {
    System.out.println("IndexOptionsPaperExamples.isdaVApproxTest\n\n");

    //this is repeated from above 
    final CDSAnalytic spotCDS = FACTORY.makeIMMCDS(TRADE_DATE, Period.ofYears(5));
    final double indexPrice = 0.021;

    //start by adjusting the curves 

    final IntrinsicIndexDataBundle adjCurves = PSA.adjustCurves(indexPrice, spotCDS, INDEX_COUPON, YIELD_CURVE, INTRINSIC_DATA);

    final LocalDate optionExpiry = TRADE_DATE.plusMonths(1); //option expiry/(effective) start of protection is in 1 month

    final double timeToExpiry = ACT365F.getDayCountFraction(TRADE_DATE, optionExpiry);
    final CDSAnalytic fwdStartingCDS = FACTORY.makeForwardStartingCDX(TRADE_DATE, optionExpiry, Period.ofYears(5));
    final CDSAnalytic fwdCDS = FACTORY.makeCDX(optionExpiry, Period.ofYears(5));
    final IndexOptionPricer pricerISDA = new IndexOptionPricer(fwdCDS, timeToExpiry, YIELD_CURVE, INDEX_COUPON, true);//this uses to ISDA model to compute annuity
    final IndexOptionPricer pricerApprox = new IndexOptionPricer(fwdCDS, timeToExpiry, YIELD_CURVE, INDEX_COUPON, false); //this uses to ISDA model to compute annuity 

    final double atmFwdPrice = INDEX_CAL.defaultAdjustedForwardIndexValue(fwdStartingCDS, timeToExpiry, YIELD_CURVE, INDEX_COUPON, adjCurves);
    final MarketQuoteConverter converter = new MarketQuoteConverter();
    final double atmFwdSpread = converter.pufToQuotedSpread(fwdCDS, INDEX_COUPON, YIELD_CURVE.withOffset(timeToExpiry), atmFwdPrice);
    final double vol = 0.4;
    System.out.println("atmFwd: " + atmFwdSpread * TEN_THOUSAND);
    System.out.println("Strike (bps)\tPrice (ISDA)\tPrice(approx)");
    for (int i = 0; i < 200; i++) {
      final double k = 0.009 + 0.014 * i / 199.;
      final boolean isPayer = k >= atmFwdSpread;
      final double price1 = pricerISDA.getOptionPriceForSpreadQuotedIndex(atmFwdPrice, vol, k, isPayer);
      final double price2 = pricerApprox.getOptionPriceForSpreadQuotedIndex(atmFwdPrice, vol, k, isPayer);
      System.out.println(k * TEN_THOUSAND + "\t" + price1 + "\t" + price2);
    }
  }

  /**
   * The option price vs strike when a certain number of names (0-3) have already defaulted 
   */
  @Test(enabled = false)
  public void defaultedNamesTest() {
    System.out.println("IndexOptionsPaperExamples.defaultedNamesTest\n");

    //this is repeated from above 
    final double indexPrice = 0.021;
    //start by adjusting the curves to match the 5Y index price 
    final IntrinsicIndexDataBundle adjCurves = PSA.adjustCurves(indexPrice, CDS_5Y, INDEX_COUPON, YIELD_CURVE, INTRINSIC_DATA);

    final LocalDate optionExpiry = TRADE_DATE.plusMonths(1); //option expiry/(effective) start of protection is in 1 month
    final double timeToExpiry = ACT365F.getDayCountFraction(TRADE_DATE, optionExpiry);
    final CDSAnalytic fwdCDS = FACTORY.makeCDX(optionExpiry, Period.ofYears(5));
    final CDSAnalytic fwdStartingCDS = FACTORY.makeForwardStartingCDX(TRADE_DATE, optionExpiry, Period.ofYears(5));
    final IndexOptionPricer pricerISDA = new IndexOptionPricer(fwdCDS, timeToExpiry, YIELD_CURVE, INDEX_COUPON, true);//this uses to ISDA model to compute annuity

    final int maxDefaults = 4;
    final double vol = 0.4;
    System.out.print("Strike (bps)");
    for (int nDefaulted = 0; nDefaulted < maxDefaults; nDefaulted++) {
      System.out.print("\t" + nDefaulted + " defaults");
    }
    System.out.print("\n");

    for (int i = 0; i < 100; i++) {
      final double k = 0.005 + 0.02 * i / 100.;
      System.out.print(k * TEN_THOUSAND);
      IntrinsicIndexDataBundle curvesWithDefaults = null;
      for (int nDefaulted = 0; nDefaulted < maxDefaults; nDefaulted++) {
        if (nDefaulted == 0) {
          curvesWithDefaults = adjCurves;
        } else {
          curvesWithDefaults = curvesWithDefaults.withDefault(INDEX_SIZE - nDefaulted); //take defaults from end of list 
        }

        final double fwdVal = INDEX_CAL.defaultAdjustedForwardIndexValue(fwdStartingCDS, timeToExpiry, YIELD_CURVE, INDEX_COUPON, curvesWithDefaults);
        final double price = pricerISDA.getOptionPriceForSpreadQuotedIndex(fwdVal, vol, k, true);
        System.out.print("\t" + price);
      }
      System.out.print("\n");
    }

  }

  /**
   * Price an index option using Black formula
   */
  @Test(enabled = false)
  public void blackTest() {
    final DefaultSwaption ds = new DefaultSwaption();

    //this is repeated from above 
    final double indexPrice = 0.021;
    //start by adjusting the curves 
    final IntrinsicIndexDataBundle adjCurves = PSA.adjustCurves(indexPrice, CDS_5Y, INDEX_COUPON, YIELD_CURVE, INTRINSIC_DATA);

    final CDSAnalytic[] pillarCDS = new CDSAnalytic[] {CDS_6M, CDS_1Y, CDS_3Y, CDS_5Y, CDS_7Y };
    final ISDACompliantCreditCurve indexCreditCurve = INDEX_CAL.impliedIndexCurve(pillarCDS, INDEX_COUPON, YIELD_CURVE, adjCurves);
    final double price = PRICER.pv(CDS_5Y, YIELD_CURVE, indexCreditCurve, INDEX_COUPON);
    //System.out.println(price);
    assertEquals(indexPrice, price, 1e-16);

    final LocalDate optionExpiry = TRADE_DATE.plusMonths(1);
    final CDSAnalytic fwdCDS = FACTORY.makeCDX(optionExpiry, Period.ofYears(5));
    final CDSAnalytic fwdStartingCDS = FACTORY.makeForwardStartingCDX(TRADE_DATE, optionExpiry, Period.ofYears(5));
    final double timeToExpiry = ACT365F.getDayCountFraction(TRADE_DATE, optionExpiry);

    final double df = YIELD_CURVE.getDiscountFactor(fwdStartingCDS.getCashSettleTime());
    final IndexOptionPricer iop = new IndexOptionPricer(fwdCDS, timeToExpiry, YIELD_CURVE, INDEX_COUPON);
    final double fwdAnnuity = df * INDEX_CAL.indexAnnuity(fwdStartingCDS, YIELD_CURVE, adjCurves);
    final double fwdSpread = INDEX_CAL.defaultAdjustedForwardSpread(fwdStartingCDS, timeToExpiry, YIELD_CURVE, adjCurves);
    final double dav = INDEX_CAL.defaultAdjustedForwardIndexValue(fwdStartingCDS, timeToExpiry, YIELD_CURVE, INDEX_COUPON, adjCurves);

    final double davCheck = (fwdSpread - INDEX_COUPON) * fwdAnnuity / df;
    assertEquals("default-adjusted fwd", dav, davCheck, 1e-12);
    final ISDACompliantYieldCurve fwdYC = YIELD_CURVE.withOffset(timeToExpiry);
    final double atmFwdSpread = CONVERTER.pufToQuotedSpread(fwdCDS, INDEX_COUPON, fwdYC, dav);
    System.out.println("Default-adjusted forward price\t" + dav);
    System.out.println("Default-adjusted forward  spread\t" + fwdSpread * TEN_THOUSAND);
    System.out.println("ATM Fwd Spread\t" + atmFwdSpread * TEN_THOUSAND);

    final BlackIndexOptionPricer blackPricer = new BlackIndexOptionPricer(fwdCDS, timeToExpiry, YIELD_CURVE, INDEX_COUPON, fwdSpread, fwdAnnuity);

    final double vol = 0.4;
    for (int i = 0; i < 100; i++) {
      final double k = 0.01 + 0.01 * i / 99.;
      final double exercisePrice = CONVERTER.quotedSpreadToPUF(fwdCDS, INDEX_COUPON, fwdYC, k);
      final double putCall = df * (dav - exercisePrice);
      final IndexOptionStrike strike = new ExerciseAmount(exercisePrice);
      final double pedersenPayer = iop.getOptionPremium(dav, vol, strike, true);
      final double pedersenRec = iop.getOptionPremium(dav, vol, strike, false);

      final double modBlackPayer = blackPricer.getOptionPremium(strike, vol, true);
      final double modBlackRec = blackPricer.getOptionPremium(strike, vol, false);
      final double impFwd = (modBlackPayer - modBlackRec) / df + exercisePrice;
      assertEquals("put-call parity", dav, impFwd, 1e-12);
      double iv;
      double ivCall = 0;
      double ivPut = 0;
      try {
        final boolean isPayer = k > fwdSpread;
        final double otmPrice = isPayer ? pedersenPayer : pedersenRec;
        ivCall = blackPricer.getImpliedVolatility(strike, pedersenPayer, true);
        ivPut = blackPricer.getImpliedVolatility(strike, pedersenRec, false);
        iv = blackPricer.getImpliedVolatility(strike, otmPrice, isPayer);
      } catch (final IllegalArgumentException e) {
        iv = 0;
      }
      System.out.println(k * TEN_THOUSAND + "\t" + putCall + "\t" + pedersenPayer + "\t" + pedersenRec + "\t" + modBlackPayer + "\t" + modBlackRec + "\t" + iv + "\t" + ivCall + "\t" + ivPut);
    }

  }

  /**
   * Test for CDX.NA.HY on 13-Feb-2014
   * Show market and intrinsic prices (both as PUF and quoted spread), then show the effect of the adjustment on par spreads 
   */
  @Test(enabled = false)
  public void portfolioAdjustmentTest() {
    System.out.print("IndexOptionsPaperExamples.portfolioAdjustmentTest\n\n");

    final LocalDate tradeDate = LocalDate.of(2014, 2, 13);
    final double indexRR = CDX_NA_HY_21_RECOVERY_RATE;
    final double indexCoupon = CDX_NA_HY_21_COUPON;
    final double[] indexPrices = CDX_NA_HY_20140213_PRICES;
    final ISDACompliantYieldCurve yieldCurve = ISDA_USD_20140213;
    final ISDACompliantCreditCurve[] intrCreditCurves = getCDX_NA_HY_20140213_CreditCurves();
    final double[] intrRR = CDX_NA_HY_20140213_RECOVERY_RATES;
    final String[] names = CDX_NA_HY_21_NAMES;
    final IntrinsicIndexDataBundle intrinsicData = new IntrinsicIndexDataBundle(intrCreditCurves, intrRR);

    final CDSAnalyticFactory factory = new CDSAnalyticFactory(indexRR);
    final CDSAnalytic[] pillarCDX = factory.makeCDX(tradeDate, INDEX_TENORS);

    final int nIndexTerms = INDEX_TENORS.length;
    final double[] puf = new double[nIndexTerms];
    for (int i = 0; i < nIndexTerms; i++) {
      puf[i] = 1 - indexPrices[i];
    }

    //Show market and intrinsic prices (both as PUF and quoted spread)
    System.out.println("Market PUF\tIntrinsic PUF\tMarket Quoted Spread\tIntrinsic Quoted Spread");
    for (int i = 0; i < nIndexTerms; i++) {
      final double intrPUF = INDEX_CAL.indexPV(pillarCDX[i], indexCoupon, yieldCurve, intrinsicData);
      final double intrQS = CONVERTER.pufToQuotedSpread(pillarCDX[i], indexCoupon, yieldCurve, intrPUF) * TEN_THOUSAND;
      final double mrkQS = CONVERTER.pufToQuotedSpread(pillarCDX[i], indexCoupon, yieldCurve, puf[i]) * TEN_THOUSAND;
      System.out.println(INDEX_TENORS[i].toString() + "\t" + puf[i] * ONE_HUNDRED + "\t" + intrPUF * ONE_HUNDRED + "\t" + mrkQS + "\t" + intrQS);
    }

    final IntrinsicIndexDataBundle adjCreditCurves = PSA.adjustCurves(puf, pillarCDX, indexCoupon, yieldCurve, intrinsicData);
    //check the adjustment worked 
    for (int i = 0; i < nIndexTerms; i++) {
      final double adjPUF = INDEX_CAL.indexPV(pillarCDX[i], indexCoupon, yieldCurve, adjCreditCurves);
      assertEquals("Recovery of Price", puf[i], adjPUF, 1e-9);
    }

    //show original and adjusted par spreads 
    final Period[] tenors = new Period[] {Period.ofMonths(6), Period.ofYears(5), Period.ofYears(10) };
    final CDSAnalytic[] exampleCDS = FACTORY.makeIMMCDS(tradeDate, tenors);
    final String[] exampleNames = new String[] {"The AES Corp", "BOMBARDIER INC.", "J C Penney Co Inc", "TX Competitive Elec Hldgs Co LLC" };
    final List<String> listNames = Arrays.asList(names);
    System.out.print("\n\tOriginal Par Spreads\t\tAdjusted Par Spreads\n");
    System.out.print("\t6M\t5Y\t10Y\t6M\t5Y\t10Y\n");
    for (final String name : exampleNames) {
      final int pos = listNames.indexOf(name);
      if (pos < 0) {
        throw new MathException("cannot find " + name);
      }
      final double rr = intrRR[pos];
      System.out.print(name);
      for (final CDSAnalytic cds : exampleCDS) {
        final double parSpread = PRICER.parSpread(cds.withRecoveryRate(rr), yieldCurve, intrCreditCurves[pos]);
        System.out.print("\t" + parSpread * TEN_THOUSAND);
      }
      for (final CDSAnalytic cds : exampleCDS) {
        final double parSpread = PRICER.parSpread(cds.withRecoveryRate(rr), yieldCurve, adjCreditCurves.getCreditCurve(pos));
        System.out.print("\t" + parSpread * TEN_THOUSAND);
      }
      System.out.print("\n");
    }

    //one year survival probability for TX Competitive Elec Hldgs Co LLC
    final int index = listNames.indexOf("TX Competitive Elec Hldgs Co LLC");
    System.out.println("\nOne year survival probability for " + names[index] + ":\t" + intrCreditCurves[index].getSurvivalProbability(1.0) * ONE_HUNDRED + "%");
  }

}
