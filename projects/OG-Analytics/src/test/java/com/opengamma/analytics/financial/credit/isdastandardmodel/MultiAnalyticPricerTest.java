/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel;

import static com.opengamma.analytics.financial.credit.isdastandardmodel.IMMDateLogic.getIMMDateSet;
import static com.opengamma.analytics.financial.credit.isdastandardmodel.IMMDateLogic.getNextIMMDate;
import static com.opengamma.analytics.financial.credit.isdastandardmodel.IMMDateLogic.getPrevIMMDate;
import static com.opengamma.analytics.financial.credit.isdastandardmodel.IMMDateLogic.isIMMDate;
import static com.opengamma.financial.convention.businessday.BusinessDayDateUtils.addWorkDays;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Month;
import org.threeten.bp.Period;

import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class MultiAnalyticPricerTest extends ISDABaseTest {

  private static final MultiAnalyticCDSPricer MULTI_PRICER_ISDA = new MultiAnalyticCDSPricer();
  private static final MultiAnalyticCDSPricer MULTI_PRICER_MARKIT_FIX = new MultiAnalyticCDSPricer(MARKIT_FIX);
  private static final CDSAnalyticFactory FACTORY = new CDSAnalyticFactory();

  private static final ISDACompliantYieldCurve YIELD_CURVE;
  private static final ISDACompliantCreditCurve CREDIT_CURVE;

  static {

    final double[] yieldCurveNodes = new double[] {1 / 365., 1 / 52., 1 / 12., 1 / 4., 1 / 2., 1., 2., 3., 4., 5., 7., 10, 15, 20, 30 };
    final double[] zeroRates = new double[] {0.01, 0.011, 0.013, 0.015, 0.02, 0.03, 0.035, 0.04, 0.04, 0.06, 0.06, 0.057, 0.055, 0.05, 0.05 };
    YIELD_CURVE = new ISDACompliantYieldCurve(yieldCurveNodes, zeroRates);
    final double[] creditCurveNodes = new double[] {1 / 2., 1, 2, 3, 5, 7, 10 };
    final double[] zeroHazardRates = new double[] {0.0015, 0.002, 0.0023, 0.0025, 0.0024, 0.0023, 0.002 };
    CREDIT_CURVE = new ISDACompliantCreditCurve(creditCurveNodes, zeroHazardRates);
  }

  @Test
  public void singleCDSTest() {
    final LocalDate tradeDate = LocalDate.of(2013, Month.AUGUST, 30);
    final LocalDate effectiveDate = FOLLOWING.adjustDate(DEFAULT_CALENDAR, getPrevIMMDate(tradeDate));
    final LocalDate stepinDate = tradeDate.plusDays(1);
    final LocalDate valueDate = addWorkDays(tradeDate, 3, DEFAULT_CALENDAR);
    final LocalDate nextIMM = getNextIMMDate(tradeDate);
    final int matIndex = 4;
    LocalDate temp = nextIMM;
    for (int i = 0; i < matIndex; i++) {
      temp = temp.plus(PAYMENT_INTERVAL);
    }
    final LocalDate maturity = temp;
    final Tenor paymentInt = Tenor.of(PAYMENT_INTERVAL);

    final CDSAnalytic cdsS = FACTORY.makeCDS(tradeDate, effectiveDate, maturity);
    final MultiCDSAnalytic cdsM = new MultiCDSAnalytic(tradeDate, stepinDate, valueDate, effectiveDate, nextIMM, new int[] {matIndex }, PAY_ACC_ON_DEFAULT, paymentInt, STUB, PROCTECTION_START,
        RECOVERY_RATE, FOLLOWING, DEFAULT_CALENDAR, ACT360, ACT365F);

    final double proLegS = PRICER.protectionLeg(cdsS, YIELD_CURVE, CREDIT_CURVE);
    final double proLegM = MULTI_PRICER_ISDA.protectionLeg(cdsM, YIELD_CURVE, CREDIT_CURVE)[0];
    //    System.out.println("pro leg: " + proLegS + "\t" + proLegM);
    final double rpv01S = PRICER.annuity(cdsS, YIELD_CURVE, CREDIT_CURVE, PriceType.CLEAN);
    final double rpv01M = MULTI_PRICER_ISDA.pvPremiumLegPerUnitSpread(cdsM, YIELD_CURVE, CREDIT_CURVE, PriceType.CLEAN)[0];
    //  System.out.println("rpv01: " + rpv01S + "\t" + rpv01M);

    //These are identical calculations, so the match should be exact 
    assertEquals("proLeg", proLegS, proLegM, 0);
    assertEquals("RPV01", rpv01S, rpv01M, 0);
  }

  @Test
  public void multiCDSTest() {
    final LocalDate tradeDate = LocalDate.of(2013, Month.AUGUST, 30);
    final LocalDate effectiveDate = FOLLOWING.adjustDate(DEFAULT_CALENDAR, getPrevIMMDate(tradeDate));
    final LocalDate stepinDate = tradeDate.plusDays(1);
    final LocalDate valueDate = addWorkDays(tradeDate, 3, DEFAULT_CALENDAR);
    final LocalDate nextIMM = getNextIMMDate(tradeDate);
    final int[] matIndex = new int[] {1, 2, 4, 8, 12, 20, 28 };
    final int nMat = matIndex.length;

    final LocalDate[] maturities = new LocalDate[nMat];

    LocalDate tMat = nextIMM;
    for (int i = 0; i < nMat; i++) {
      final int steps = i == 0 ? matIndex[0] : matIndex[i] - matIndex[i - 1];
      for (int j = 0; j < steps; j++) {
        tMat = tMat.plus(PAYMENT_INTERVAL);
      }
      maturities[i] = tMat;
    }

    final double[] coupons = new double[] {0.0075, 0.008, 0.01, 0.01, 0.011, 0.01, 0.009 };
    final Tenor paymentInt = Tenor.of(PAYMENT_INTERVAL);

    final CDSAnalytic[] cdsS = FACTORY.makeCDS(tradeDate, effectiveDate, maturities);
    final MultiCDSAnalytic cdsM = new MultiCDSAnalytic(tradeDate, stepinDate, valueDate, effectiveDate, nextIMM, matIndex, PAY_ACC_ON_DEFAULT, paymentInt, STUB, PROCTECTION_START, RECOVERY_RATE,
        FOLLOWING, DEFAULT_CALENDAR, ACT360, ACT365F);

    final double[] pvM = MULTI_PRICER_ISDA.pv(cdsM, YIELD_CURVE, CREDIT_CURVE, coupons);
    //    final double[] proLegM = MULTI_PRICER.protectionLeg(cdsM, YIELD_CURVE, CREDIT_CURVE);
    //    final double[] rpv01M = MULTI_PRICER.pvPremiumLegPerUnitSpread(cdsM, YIELD_CURVE, CREDIT_CURVE, PriceType.CLEAN);

    final double[] pvS = new double[nMat];
    for (int i = 0; i < nMat; i++) {
      pvS[i] = PRICER.pv(cdsS[i], YIELD_CURVE, CREDIT_CURVE, coupons[i]);
    }

    //These take different paths, so the match will not be exact 
    for (int i = 0; i < nMat; i++) {
      assertEquals("pv " + i, pvS[i], pvM[i], 1e-16);
    }

    //check to correct integral prices 
    final double[] pvMC = MULTI_PRICER_MARKIT_FIX.pv(cdsM, YIELD_CURVE, CREDIT_CURVE, coupons);
    final double[] pvSC = new double[nMat];
    for (int i = 0; i < nMat; i++) {
      pvSC[i] = PRICER_MARKIT_FIX.pv(cdsS[i], YIELD_CURVE, CREDIT_CURVE, coupons[i]);
    }

    //These take different paths, so the match will not be exact 
    for (int i = 0; i < nMat; i++) {
      assertEquals("pv (fixed)" + i, pvSC[i], pvMC[i], 1e-16);
    }
  }

  @Test
  public void multiCDSTest3() {
    final int nValDates = 41;
    final LocalDate tradeDate = LocalDate.of(2011, 6, 13);
    final LocalDate nextIMM = getNextIMMDate(tradeDate);
    final LocalDate accStartDate = FOLLOWING.adjustDate(DEFAULT_CALENDAR, getPrevIMMDate(tradeDate));
    final LocalDate[] maturityDates = getIMMDateSet(nextIMM, nValDates); //maturity dates don't change (it is the same CDSs on each day of the scenario) 

    final double[] coupons = new double[nValDates];
    Arrays.fill(coupons, ONE_PC);
    final CDSAnalytic[] cds = FACTORY.makeCDS(tradeDate, accStartDate, maturityDates);
    final MultiCDSAnalytic multiCDS = FACTORY.makeMultiIMMCDS(tradeDate, 0, 40);
    final MultiAnalyticCDSPricer mPricer = new MultiAnalyticCDSPricer(MARKIT_FIX);
    final double[] mRPV01 = mPricer.pvPremiumLegPerUnitSpread(multiCDS, YIELD_CURVE, CREDIT_CURVE, PriceType.CLEAN);
    for (int i = 0; i < nValDates; i++) {
      final double rpv01 = PRICER_MARKIT_FIX.annuity(cds[i], YIELD_CURVE, CREDIT_CURVE, PriceType.CLEAN);
      assertEquals(rpv01, mRPV01[i], 1e-15 * rpv01);
    }
  }

  @Test
  public void multiCDSTest2() {

    //numbers from ISDA model excel (with fix)
    final double[] expectedProtLeg = new double[] {2.45394099651068E-05, 0.00227491490938686, 0.00448142313178339, 0.00666745010334752, 0.00885603585574933, 0.012691978781156, 0.0164500855081327,
        0.0201318759141635, 0.0238598226827485, 0.0275443376519455, 0.0311399510498073, 0.0346489017007175, 0.0381880070431471, 0.0434702839464633, 0.0486021197153017, 0.0535881757494423,
        0.0585947781482861, 0.0635100334228483, 0.0682822757875156, 0.0729670602447138, 0.0776160534847412, 0.0818931345586326, 0.0860467169145431, 0.0900806892461579, 0.0941296532623397,
        0.0981042087895352, 0.101963461190518, 0.105711060988118, 0.109472064842192, 0.113288927289599, 0.116993122936688, 0.120588291169969, 0.124194442553579, 0.127732056036848, 0.131164913796168,
        0.13453307768769, 0.137873733063254, 0.141150384026368, 0.144329371544781, 0.147413885672373, 0.150506909958133 };
    final double[] expectedRPV01 = new double[] {0.255551477261065, 0.256632130692319, 0.505623806773112, 0.75227939608251, 0.999196556679805, 1.24414240013293, 1.48412997820174, 1.71924769187078,
        1.95722450070843, 2.19205173432063, 2.42120515404789, 2.64484382580866, 2.87031426005254, 3.0921437216807, 3.30771077822955, 3.51719921532105, 3.7273400042775, 3.93373063819273,
        4.13412825531985, 4.33085331038569, 4.52597943738742, 4.71755423579183, 4.9036063521181, 5.08430937903227, 5.26557601138535, 5.44360502471132, 5.61646068630761, 5.78432376573634,
        5.95268616377267, 6.11796359393172, 6.27837179689681, 6.43406633865226, 6.59017164733675, 6.74336671903601, 6.89201730114158, 7.03786649101056, 7.18241173055505, 7.3242766448501,
        7.46192022533206, 7.5954663571564, 7.72932665473152 };

    final LocalDate spotDate = LocalDate.of(2011, Month.JUNE, 15);
    final String[] yieldCurvePoints = new String[] {"1M", "2M", "3M", "6M", "9M", "1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "11Y", "12Y", "15Y", "20Y", "25Y", "30Y" };
    final String[] yieldCurveInstruments = new String[] {"M", "M", "M", "M", "M", "M", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S" };
    //final String[] yieldCurveInstruments = new String[] {"M", "M", "M", "M", "M", "M", "M", "M", "M", "M", "M", "M", "M", "M", "M", "M", "M", "M", "M" };
    final double[] rates = new double[] {0.00445, 0.009488, 0.012337, 0.017762, 0.01935, 0.020838, 0.01652, 0.02018, 0.023033, 0.02525, 0.02696, 0.02825, 0.02931, 0.03017, 0.03092, 0.0316, 0.03231,
        0.03367, 0.03419, 0.03411, 0.03412 };

    final LocalDate tradeDate = LocalDate.of(2011, Month.JUNE, 19);
    final ISDACompliantYieldCurve yieldCurve = makeYieldCurve(tradeDate, spotDate, yieldCurvePoints, yieldCurveInstruments, rates, ACT360, D30360, Period.ofYears(1));

    final FastCreditCurveBuilder ccBuilder = new FastCreditCurveBuilder(MARKIT_FIX);
    final double[] spreads = new double[] {0.00886315689995649, 0.00886315689995649, 0.0133044689825873, 0.0171490070952563, 0.0183903639181293, 0.0194721890639724 };
    final Period[] tenors = new Period[] {Period.ofMonths(6), Period.ofYears(1), Period.ofYears(3), Period.ofYears(5), Period.ofYears(7), Period.ofYears(10) };

    final CDSAnalytic[] pillarCDS = FACTORY.makeIMMCDS(tradeDate, tenors);
    final ISDACompliantCreditCurve creditCurve = ccBuilder.calibrateCreditCurve(pillarCDS, spreads, yieldCurve);

    final int nVals = 41;

    final LocalDate accStart = FOLLOWING.adjustDate(DEFAULT_CALENDAR, getPrevIMMDate(tradeDate));
    final LocalDate nextIMM = isIMMDate(tradeDate) ? tradeDate : getNextIMMDate(tradeDate);
    final LocalDate[] maturities = getIMMDateSet(nextIMM, nVals);
    final CDSAnalytic[] cdsArray = FACTORY.makeCDS(tradeDate, accStart, maturities);
    final MultiCDSAnalytic multiCDS = FACTORY.makeMultiIMMCDS(tradeDate, 0, nVals - 1);

    final double[] proLeg1 = new double[nVals];
    final double[] premLeg1 = new double[nVals];
    final double[] proLeg2 = MULTI_PRICER_MARKIT_FIX.protectionLeg(multiCDS, yieldCurve, creditCurve);
    final double[] premLeg2 = MULTI_PRICER_MARKIT_FIX.pvPremiumLegPerUnitSpread(multiCDS, yieldCurve, creditCurve, PriceType.DIRTY);
    for (int i = 0; i < nVals; i++) {
      proLeg1[i] = PRICER_MARKIT_FIX.protectionLeg(cdsArray[i], yieldCurve, creditCurve);
      premLeg1[i] = PRICER_MARKIT_FIX.annuity(cdsArray[i], yieldCurve, creditCurve, PriceType.DIRTY);
      //   System.out.println(proLeg1[i] + "\t" + proLeg2[i] + "\t" + premLeg1[i] + "\t" + premLeg2[i]);
      assertEquals(expectedProtLeg[i], proLeg1[i], 1e-15);
      assertEquals(expectedRPV01[i], premLeg1[i], 1e-14);
      assertEquals(proLeg1[i], proLeg2[i], 1e-15);
      assertEquals(premLeg1[i], premLeg2[i], 1e-14);
    }

  }

  /**
   * 
   */
  @Test
  public void pvTest() {
    /*
     * This is not needed if we follow exactly the same procedure.
     */
    final double tol = 1.e-13;

    final AccrualOnDefaultFormulae form = AccrualOnDefaultFormulae.OrignalISDA;
    final MultiAnalyticCDSPricer mPricer = new MultiAnalyticCDSPricer(form);
    final AnalyticCDSPricer sPricer = new AnalyticCDSPricer(form);

    final double premium = 250. * 1.e-4;

    final LocalDate tradeDate = LocalDate.of(2013, Month.AUGUST, 30);
    final LocalDate effectiveDate = FOLLOWING.adjustDate(DEFAULT_CALENDAR, getPrevIMMDate(tradeDate));
    final LocalDate stepinDate = tradeDate.plusDays(1);
    final LocalDate valueDate = addWorkDays(tradeDate, 3, DEFAULT_CALENDAR);
    final LocalDate nextIMM = getNextIMMDate(tradeDate);
    final int matIndex0 = 4;
    final int matIndex1 = 8;
    LocalDate temp = nextIMM;
    for (int i = 0; i < matIndex0; i++) {
      temp = temp.plus(PAYMENT_INTERVAL);
    }
    final LocalDate maturity0 = temp;
    temp = nextIMM;
    for (int i = 0; i < matIndex1; i++) {
      temp = temp.plus(PAYMENT_INTERVAL);
    }
    final LocalDate maturity1 = temp;
    final Tenor paymentInt = Tenor.of(PAYMENT_INTERVAL);

    final boolean accOnDefault = false;

    final MultiCDSAnalytic cdsM = new MultiCDSAnalytic(tradeDate, stepinDate, valueDate, effectiveDate, nextIMM, new int[] {matIndex0, matIndex1 }, accOnDefault, paymentInt, STUB,
        PROCTECTION_START, RECOVERY_RATE, FOLLOWING, DEFAULT_CALENDAR, ACT360, ACT365F);
    final CDSAnalytic cds0 = new CDSAnalytic(tradeDate, stepinDate, valueDate, effectiveDate, maturity0, accOnDefault, PAYMENT_INTERVAL, STUB,
        PROCTECTION_START, RECOVERY_RATE, FOLLOWING, DEFAULT_CALENDAR, ACT360, ACT365F);
    final CDSAnalytic cds1 = new CDSAnalytic(tradeDate, stepinDate, valueDate, effectiveDate, maturity1, accOnDefault, PAYMENT_INTERVAL, STUB,
        PROCTECTION_START, RECOVERY_RATE, FOLLOWING, DEFAULT_CALENDAR, ACT360, ACT365F);

    final double[] prices = mPricer.pv(cdsM, YIELD_CURVE, CREDIT_CURVE, premium, PriceType.CLEAN);
    final double price0 = sPricer.pv(cds0, YIELD_CURVE, CREDIT_CURVE, premium, PriceType.CLEAN);
    final double price1 = sPricer.pv(cds1, YIELD_CURVE, CREDIT_CURVE, premium, PriceType.CLEAN);
    assertEquals(price0, prices[0], tol);
    assertEquals(price1, prices[1], tol);

    final double[] pricesClean = mPricer.pv(cdsM, YIELD_CURVE, CREDIT_CURVE, premium);
    assertEquals(pricesClean[0], prices[0], tol);
    assertEquals(pricesClean[1], prices[1], tol);

    /*
     * Error tested
     */
    try {
      mPricer.pv(cdsM, YIELD_CURVE, CREDIT_CURVE, new double[] {premium }, PriceType.CLEAN);
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }
  }

  /**
   * 
   */
  @Test
  public void flatFactorTest() {
    /*
     * This is not needed if we follow exactly the same procedure.
     */
    final double tol = 1.e-13;

    final AccrualOnDefaultFormulae form1 = AccrualOnDefaultFormulae.OrignalISDA;
    final AccrualOnDefaultFormulae form2 = AccrualOnDefaultFormulae.MarkitFix;
    final MultiAnalyticCDSPricer mPricerOrig = new MultiAnalyticCDSPricer(form1);
    final MultiAnalyticCDSPricer mPricerFix = new MultiAnalyticCDSPricer(form2);
    final AnalyticCDSPricer sPricerOrig = new AnalyticCDSPricer(form1);
    final AnalyticCDSPricer sPricerFix = new AnalyticCDSPricer(form2);

    final LocalDate tradeDate = LocalDate.of(2013, Month.JANUARY, 31);
    final LocalDate effectiveDate = FOLLOWING.adjustDate(DEFAULT_CALENDAR, getPrevIMMDate(tradeDate));
    final LocalDate stepinDate = tradeDate.plusDays(1);
    final LocalDate valueDate = addWorkDays(tradeDate, 3, DEFAULT_CALENDAR);
    final LocalDate nextIMM = getNextIMMDate(tradeDate);
    final int matIndex0 = 4;
    final int matIndex1 = 6;
    LocalDate temp = nextIMM;
    for (int i = 0; i < matIndex0; i++) {
      temp = temp.plus(PAYMENT_INTERVAL);
    }
    final LocalDate maturity0 = temp;
    temp = nextIMM;
    for (int i = 0; i < matIndex1; i++) {
      temp = temp.plus(PAYMENT_INTERVAL);
    }
    final LocalDate maturity1 = temp;
    final Tenor paymentInt = Tenor.of(PAYMENT_INTERVAL);

    final MultiCDSAnalytic cdsM = new MultiCDSAnalytic(tradeDate, stepinDate, valueDate, effectiveDate, nextIMM, new int[] {matIndex0, matIndex1 }, PAY_ACC_ON_DEFAULT, paymentInt, STUB,
        PROCTECTION_START, RECOVERY_RATE, FOLLOWING, DEFAULT_CALENDAR, ACT360, ACT365F);
    final CDSAnalytic cds0 = new CDSAnalytic(tradeDate, stepinDate, valueDate, effectiveDate, maturity0, PAY_ACC_ON_DEFAULT, PAYMENT_INTERVAL, STUB,
        PROCTECTION_START, RECOVERY_RATE, FOLLOWING, DEFAULT_CALENDAR, ACT360, ACT365F);
    final CDSAnalytic cds1 = new CDSAnalytic(tradeDate, stepinDate, valueDate, effectiveDate, maturity1, PAY_ACC_ON_DEFAULT, PAYMENT_INTERVAL, STUB,
        PROCTECTION_START, RECOVERY_RATE, FOLLOWING, DEFAULT_CALENDAR, ACT360, ACT365F);

    final double[] yieldCurveNodes = new double[] {1 / 365., 1 / 52., 1 / 12., 1 / 4., 1 / 2., 1., 2., 3., 4., 5., 7., 10, 15, 20, 30 };
    final double[] zeroRates = new double[] {0.01, 0.011, 0.013, 0.015, 0.02, 0.072, 0.036, 0.024, 0.018, 0.06, 0.06, 0.057, 0.055, 0.05, 0.05 };
    final ISDACompliantYieldCurve yCurve = new ISDACompliantYieldCurve(yieldCurveNodes, zeroRates);
    final double[] creditCurveNodes = new double[] {1 / 2., 1, 2, 3, 5, 7, 10 };
    final double[] zeroHazardRates = new double[] {0.0015, 0.0048, 0.0024, 0.0016, 0.00096, 0.0023, 0.002 };
    final ISDACompliantCreditCurve cCurve = new ISDACompliantCreditCurve(creditCurveNodes, zeroHazardRates);

    final double[] spreads = mPricerOrig.parSpread(cdsM, yCurve, cCurve);
    final double spread0 = sPricerOrig.parSpread(cds0, yCurve, cCurve);
    final double spread1 = sPricerOrig.parSpread(cds1, yCurve, cCurve);
    assertEquals(spread0, spreads[0], tol);
    assertEquals(spread1, spreads[1], tol);

    final double[] spreadsFix = mPricerFix.parSpread(cdsM, yCurve, cCurve);
    final double spread0Fix = sPricerFix.parSpread(cds0, yCurve, cCurve);
    final double spread1Fix = sPricerFix.parSpread(cds1, yCurve, cCurve);
    assertEquals(spread0Fix, spreadsFix[0], tol);
    assertEquals(spread1Fix, spreadsFix[1], tol);
  }

  @Test(enabled = false)
  public void speedTest() {
    final int warmups = 200;
    final int hotspot = 1000;

    final LocalDate tradeDate = LocalDate.of(2013, Month.AUGUST, 30);
    final LocalDate effectiveDate = FOLLOWING.adjustDate(DEFAULT_CALENDAR, getPrevIMMDate(tradeDate));
    final LocalDate stepinDate = tradeDate.plusDays(1);
    final LocalDate valueDate = addWorkDays(tradeDate, 3, DEFAULT_CALENDAR);
    final LocalDate nextIMM = getNextIMMDate(tradeDate);
    final int[] matIndex = new int[41];
    final int nMat = matIndex.length;
    final double[] coupons = new double[nMat];
    for (int i = 0; i < nMat; i++) {
      matIndex[i] = i;
      coupons[i] = 0.001 + i / 4000.;
    }

    final LocalDate[] maturities = new LocalDate[nMat];

    LocalDate tMat = nextIMM;
    for (int i = 0; i < nMat; i++) {
      final int steps = i == 0 ? matIndex[0] : matIndex[i] - matIndex[i - 1];
      for (int j = 0; j < steps; j++) {
        tMat = tMat.plus(PAYMENT_INTERVAL);
      }
      maturities[i] = tMat;
    }
    final Tenor paymentInt = Tenor.of(PAYMENT_INTERVAL);

    final CDSAnalytic[] cdsS = FACTORY.makeCDS(tradeDate, effectiveDate, maturities);
    final MultiCDSAnalytic cdsM = new MultiCDSAnalytic(tradeDate, stepinDate, valueDate, effectiveDate, nextIMM, matIndex, PAY_ACC_ON_DEFAULT, paymentInt, STUB, PROCTECTION_START, RECOVERY_RATE,
        FOLLOWING, DEFAULT_CALENDAR, ACT360, ACT365F);

    final double[] pvSC = new double[nMat];
    double[] pvMC = null;

    for (int w = 0; w < warmups; w++) {
      for (int i = 0; i < nMat; i++) {
        pvSC[i] = PRICER_MARKIT_FIX.pv(cdsS[i], YIELD_CURVE, CREDIT_CURVE, coupons[i]);
      }
      pvMC = MULTI_PRICER_MARKIT_FIX.pv(cdsM, YIELD_CURVE, CREDIT_CURVE, coupons);
    }

    //These take different paths, so the match will not be exact 
    for (int i = 0; i < nMat; i++) {
      assertEquals("pv " + i, pvSC[i], pvMC[i], 1e-16);
    }

    long time = System.nanoTime();
    for (int h = 0; h < hotspot; h++) {
      for (int i = 0; i < nMat; i++) {
        pvSC[i] = PRICER_MARKIT_FIX.pv(cdsS[i], YIELD_CURVE, CREDIT_CURVE, coupons[i]);
      }
    }
    long nextTime = System.nanoTime();
    System.out.println("Time for " + hotspot + " single CDS prices " + (nextTime - time) / 1e6 + "ms");
    time = nextTime;
    for (int h = 0; h < hotspot; h++) {
      pvMC = MULTI_PRICER_MARKIT_FIX.pv(cdsM, YIELD_CURVE, CREDIT_CURVE, coupons);
    }
    nextTime = System.nanoTime();
    System.out.println("Time for " + hotspot + " multi CDS prices " + (nextTime - time) / 1e6 + "ms");
  }

}
