/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.options;

import static com.opengamma.analytics.financial.credit.isdastandardmodel.IMMDateLogic.getNextIMMDate;
import static com.opengamma.analytics.financial.credit.isdastandardmodel.IMMDateLogic.getPrevIMMDate;
import static com.opengamma.financial.convention.businessday.BusinessDayDateUtils.addWorkDays;
import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.analytics.financial.credit.index.PortfolioSwapAdjustment;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalytic;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalyticFactory;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDABaseTest;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;
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
  private static final ISDACompliantYieldCurve YIELD_CURVE;
  private static final double[] RECOVERY_RATES;
  private static final LocalDate TRADE_DATE = LocalDate.of(2013, 11, 29);

  static {
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
  }

  /**
   * This is a pure regression test of the intrinsic value of the index 
   */
  @Test
  public void priceIndexTest() {
    final PortfolioSwapAdjustment psa = new PortfolioSwapAdjustment();
    final CDSAnalytic cds = FACTORY.makeIMMCDS(TRADE_DATE, Period.ofYears(5));
    final double price = psa.indexPV(cds, INDEX_COUPON, INDEX_SIZE, YIELD_CURVE, CREDIT_CURVES, RECOVERY_RATES, PriceType.CLEAN);
    final double expected = 0.022730962800242956;
    assertEquals(expected, price);
    //System.out.println(price);
  }

  @Test
  public void indexAdjTest() {
    final PortfolioSwapAdjustment psa = new PortfolioSwapAdjustment();
    final CDSAnalytic cds = FACTORY.makeIMMCDS(TRADE_DATE, Period.ofYears(5));
    final double indexPrice = 0.021;
    final ISDACompliantCreditCurve[] adjCurves = psa.adjustCurves(cds, INDEX_COUPON, INDEX_SIZE, YIELD_CURVE, CREDIT_CURVES, RECOVERY_RATES, indexPrice);
    final double price = psa.indexPV(cds, INDEX_COUPON, INDEX_SIZE, YIELD_CURVE, adjCurves, RECOVERY_RATES, PriceType.CLEAN);
    assertEquals(indexPrice, price);
  }

  @Test
  public void indexOptionTest() {
    final DefaultSwaption defSwapPricer = new DefaultSwaption();
    final CDSAnalytic spotCDS = FACTORY.makeIMMCDS(TRADE_DATE, Period.ofYears(5));
    final double indexPrice = 0.021;
    //start by adjusting the curves 
    final PortfolioSwapAdjustment psa = new PortfolioSwapAdjustment();
    final ISDACompliantCreditCurve[] adjCurves = psa.adjustCurves(spotCDS, INDEX_COUPON, INDEX_SIZE, YIELD_CURVE, CREDIT_CURVES, RECOVERY_RATES, indexPrice);

    final LocalDate optionExpiry = TRADE_DATE.plusMonths(1); //option expiry/(effective) start of protection is in 1 month
    final LocalDate proStart = optionExpiry.plusDays(1);
    //cash-settlement for a normal (i.e. spot) CDS is 3 working days after trade date
    final LocalDate csDate = addWorkDays(optionExpiry, 3, DEFAULT_CALENDAR);
    final LocalDate accStart = FOLLOWING.adjustDate(DEFAULT_CALENDAR, getPrevIMMDate(optionExpiry));
    final LocalDate mat = getNextIMMDate(TRADE_DATE).plusYears(5); //Note: at option expiry (29/12/2013) this is still on-the-run

    final CDSAnalytic fwdCDS = FACTORY.makeCDS(TRADE_DATE, proStart, csDate, accStart, mat);
    
    
    //debug

    final IndexOptionPricer pricer = new IndexOptionPricer(fwdCDS, YIELD_CURVE, INDEX_COUPON);
    final double vol = 1;
    for (int i = 0; i < 100; i++) {
      final double k = 0.005 + 0.02 * i / 100.;
      final double price = pricer.price(INDEX_SIZE, adjCurves, RECOVERY_RATES, 0.0, k, vol);
      defSwapPricer.price(fwdCDS, YIELD_CURVE, null, k, i, vol, PROCTECTION_START, PAY_ACC_ON_DEFAULT)
      System.out.println(k + "\t" + price);
    }
  }
}
