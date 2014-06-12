/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel;

import static com.opengamma.analytics.financial.credit.isdastandardmodel.IMMDateLogic.getIMMDateSet;
import static com.opengamma.analytics.financial.credit.isdastandardmodel.IMMDateLogic.getNextIMMDate;
import static com.opengamma.analytics.financial.credit.isdastandardmodel.IMMDateLogic.getNextIndexRollDate;
import static com.opengamma.analytics.financial.credit.isdastandardmodel.IMMDateLogic.getPrevIMMDate;
import static com.opengamma.financial.convention.businessday.BusinessDayDateUtils.addWorkDays;
import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Month;
import org.threeten.bp.Period;

import com.opengamma.analytics.financial.model.BumpType;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class IndexCDSTest extends ISDABaseTest {

  private static final MarketQuoteConverter PUF_CONVERTER = new MarketQuoteConverter();

  //the buckets
  private static final Period[] TENORS = new Period[] {Period.ofMonths(6), Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5), Period.ofYears(7),
    Period.ofYears(10), Period.ofYears(15), Period.ofYears(20), Period.ofYears(30) };

  private static final double COUPON = 0.01;
  private static final double NOTIONAL = 1e16; //ten quadrillion notional (to get more dp from Markit)

  /**
   * Test of CDX.NA.IG.20-v1 5Y from Markit website
   */
  @Test
  public void test() {
    //numbers from https://www.markit.com
    //expected values (user)
    final double mCleanPrice = 100.3;
    final double mCashSettle = -43750455922031.0;
    final int mAccDays = 49;
    final double mAccAmt = 13611111111111.11;
    final double mCreditDV01 = 4647028138242.0;
    //transformed 
    final double mCashSettleTransformed = -43757966062423.0;
    final double mCreditDV01Transformed = 4646838148143.0;

    final double tradeLevel = 0.00935;
    final LocalDate tradeDate = LocalDate.of(2013, Month.AUGUST, 7);
    final LocalDate stepinDate = tradeDate.plusDays(1); // AKA stepin date
    final LocalDate cashSettleDate = addWorkDays(tradeDate, 3, DEFAULT_CALENDAR); // AKA valuation date
    final LocalDate startDate = getPrevIMMDate(tradeDate);
    final LocalDate maturity = LocalDate.of(2018, Month.JUNE, 20);

    //yield curve
    final LocalDate spotDate = addWorkDays(tradeDate.minusDays(1), 3, DEFAULT_CALENDAR);
    final String[] yieldCurvePoints = new String[] {"1M", "2M", "3M", "6M", "1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "12Y", "15Y", "20Y", "25Y", "30Y" };
    final String[] yieldCurveInstruments = new String[] {"M", "M", "M", "M", "M", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S" };
    final double[] rates = new double[] {0.00185, 0.00227, 0.002664, 0.003955, 0.006654, 0.004845, 0.00784, 0.011725, 0.0157, 0.01919, 0.02219, 0.024565, 0.02657, 0.02825, 0.03095, 0.033495,
      0.035505, 0.036425, 0.036915 };
    final ISDACompliantYieldCurve yieldCurve = makeYieldCurve(tradeDate, spotDate, yieldCurvePoints, yieldCurveInstruments, rates, ACT360, D30360, Period.ofMonths(6));

    final LocalDate nextIMM = getNextIMMDate(tradeDate);
    final LocalDate[] pillarDates = getIMMDateSet(nextIMM, TENORS);
    final int nPillars = pillarDates.length;
    final double[] flatSpreads = new double[nPillars];
    Arrays.fill(flatSpreads, tradeLevel);
    final CDSAnalytic[] calibrationCDS = new CDSAnalytic[nPillars];
    for (int i = 0; i < nPillars; i++) {
      calibrationCDS[i] = new CDSAnalytic(tradeDate, stepinDate, cashSettleDate, startDate, pillarDates[i], PAY_ACC_ON_DEFAULT, PAYMENT_INTERVAL, STUB, PROCTECTION_START, RECOVERY_RATE);
    }

    final CDSAnalytic pointCDS = new CDSAnalytic(tradeDate, stepinDate, cashSettleDate, startDate, maturity, PAY_ACC_ON_DEFAULT, PAYMENT_INTERVAL, STUB, PROCTECTION_START, RECOVERY_RATE);
    final QuotedSpread qSpread = new QuotedSpread(COUPON, tradeLevel);
    final double puf = PUF_CONVERTER.convert(pointCDS, qSpread, yieldCurve).getPointsUpFront();
    final double price = (1 - puf) * 100;
    final double accAmt = NOTIONAL * pointCDS.getAccruedPremium(COUPON);
    final double cashSettle = puf * NOTIONAL - accAmt;
    final double cs01 = NOTIONAL * ONE_BP * CS01_CAL.parallelCS01(pointCDS, qSpread, yieldCurve, ONE_BP);

    //    System.out.println("price: " + price + "%");
    //    System.out.println("Accured Days: " + pointCDS.getAccuredDays());
    //    System.out.println("Accured Amt: " + accAmt);
    //    System.out.println("Cash Settlement: " + cashSettle);
    //    System.out.println("Credit DV01: " + cs01);

    assertEquals("price", mCleanPrice, price, 1e-1); //only 1dp of percentage given
    assertEquals("Cash Settlement", mCashSettle, cashSettle, 1e-15 * NOTIONAL);
    assertEquals("Accured Days", mAccDays, pointCDS.getAccuredDays());
    assertEquals("Accured Amt", mAccAmt, accAmt, 1e-18 * NOTIONAL);
    assertEquals("Credit DV01", mCreditDV01, cs01, 1e-15 * NOTIONAL);

    //flat spread term structure (transformed)
    final ISDACompliantCreditCurve creditCurve = CREDIT_CURVE_BUILDER.calibrateCreditCurve(calibrationCDS, flatSpreads, yieldCurve);
    final double cashSettleTrans = NOTIONAL * PRICER.pv(pointCDS, yieldCurve, creditCurve, COUPON, PriceType.DIRTY);
    final double cs01Trans = NOTIONAL * ONE_BP * CS01_CAL.parallelCS01FromParSpreads(pointCDS, COUPON, yieldCurve, calibrationCDS, flatSpreads, ONE_BP, BumpType.ADDITIVE);
    //    System.out.println("Cash Settlement (trans): " + cashSettleTrans);
    //    System.out.println("Credit DV01 (trans): " + cs01Trans);
    assertEquals("Cash Settlement (trans)", mCashSettleTransformed, cashSettleTrans, 1e-15 * NOTIONAL);
    assertEquals("Credit DV01 (Trans)", mCreditDV01Transformed, cs01Trans, 1e-15 * NOTIONAL);
  }

  /**
   *  iTraxx Europe Series 20 Version 1 5Y
   */
  @Test
  public void test2() {
    final double coupon = 0.01;
    final CDSAnalyticFactory factory = new CDSAnalyticFactory();
    final LocalDate tradeDate = LocalDate.of(2014, 1, 15);
    final LocalDate ycSpotDate = LocalDate.of(2014, 1, 17);
    final String[] yieldCurvePoints = new String[] {"1M", "2M", "3M", "6M", "9M", "1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "12Y", "15Y", "20Y", "30Y" };
    final String[] yieldCurveInstruments = new String[] {"M", "M", "M", "M", "M", "M", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S" };
    final double[] rates = new double[] {0.00208, 0.00247, 0.00282, 0.0039, 0.00482, 0.00557, 0.00522, 0.00705, 0.00942, 0.01182, 0.01405, 0.01607, 0.01788, 0.01948, 0.02088, 0.02315, 0.02537,
      0.02686, 0.02724 };
    final ISDACompliantYieldCurve yieldCurve = makeYieldCurve(tradeDate, ycSpotDate, yieldCurvePoints, yieldCurveInstruments, rates, ACT360, D30360, Period.ofYears(1));

    //TODO have explicit index methods in CDSAnalyticFactory [PLAT-5564]
    final CDSAnalytic cds = factory.makeIMMCDS(tradeDate, Period.of(4, 9, 0)); //Index maturity is 20-Dec-2018

    final int mAccDays = 27;
    final double mAccAmount = 7.5e-4;
    assertEquals(mAccDays, cds.getAccuredDays());
    assertEquals(mAccAmount, cds.getAccruedPremium(coupon));

    final double df = yieldCurve.getDiscountFactor(cds.getCashSettleTime());

    //these are Markit 'user' values (calculated from constant hazard rate) 
    ISDACompliantCreditCurve cc = CREDIT_CURVE_BUILDER.calibrateCreditCurve(cds, 0.012, yieldCurve);
    double marketValue = PRICER.pv(cds, yieldCurve, cc, coupon, PriceType.DIRTY, 0.0);
    double cashSettlement = marketValue / df;
    double mMarketValue = 0.008568684437956;
    double mCashSettlement = 0.008568931959138;
    assertEquals(mMarketValue, marketValue, 1e-15);
    assertEquals(mCashSettlement, cashSettlement, 1e-15);

    //now use clear price of 99.07% (note index quoted as 1-puf)
    final double puf = 0.0093;
    cc = CREDIT_CURVE_BUILDER.calibrateCreditCurve(cds, coupon, yieldCurve, puf);
    marketValue = PRICER.pv(cds, yieldCurve, cc, coupon, PriceType.DIRTY, 0.0);
    cashSettlement = marketValue / df;
    mMarketValue = 0.008549753025685;
    mCashSettlement = 0.00855;
    assertEquals(mMarketValue, marketValue, 1e-15);
    assertEquals(mCashSettlement, cashSettlement, 1e-15);

  }

  @Test(enabled = false)
  public void rollingTest() {

    final MarketQuoteConverter pufConverter = new MarketQuoteConverter();
    final FastCreditCurveBuilder builder = new FastCreditCurveBuilder();

    final double notional = 1e12;
    final LocalDate today = LocalDate.of(2011, Month.JUNE, 13);
    final Period tenor = Period.ofYears(3);
    final double tradeLevel = 99.785 * ONE_BP;
    //final double tradeLevel = 99.78471 * ONE_BP;

    final LocalDate tradeDate = today;
    final LocalDate stepinDate = tradeDate.plusDays(1); // AKA stepin date
    final LocalDate cashSettleDate = addWorkDays(tradeDate, 3, DEFAULT_CALENDAR); // AKA valuation date
    final LocalDate startDate = getPrevIMMDate(tradeDate).plusDays(1);
    final LocalDate nextRolldate = getNextIndexRollDate(today);
    final LocalDate maturity = nextRolldate.plus(tenor).minusMonths(3);

    //yield curve
    final LocalDate spotDate = addWorkDays(today.minusDays(1), 3, DEFAULT_CALENDAR);
    final String[] yieldCurvePoints = new String[] {"1M", "2M", "3M", "6M", "9M", "1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "12Y", "15Y", "20Y", "30Y" };
    final String[] yieldCurveInstruments = new String[] {"M", "M", "M", "M", "M", "M", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S" };
    final double[] rates = new double[] {0.01262, 0.01344, 0.01469, 0.01739, 0.01947, 0.02145, 0.02114, 0.02308, 0.02511, 0.02695, 0.02857, 0.02989, 0.03104, 0.03204, 0.03292, 0.0345, 0.03619,
      0.03712, 0.03602 };
    final ISDACompliantYieldCurve yieldCurve = makeYieldCurve(tradeDate, spotDate, yieldCurvePoints, yieldCurveInstruments, rates, ACT360, D30360, Period.ofYears(1));

    final CDSAnalytic pointCDS = new CDSAnalytic(tradeDate, stepinDate, cashSettleDate, startDate, maturity, PAY_ACC_ON_DEFAULT, PAYMENT_INTERVAL, STUB, PROCTECTION_START, RECOVERY_RATE);
    final QuotedSpread qSpread = new QuotedSpread(COUPON, tradeLevel);
    final double puf = pufConverter.convert(pointCDS, qSpread, yieldCurve).getPointsUpFront();
    final double accAmt = notional * pointCDS.getAccruedPremium(COUPON);
    final double cashAmount = notional * puf - accAmt;
    System.out.println(startDate + "\t" + maturity + "\t" + puf + "\t" + (1 - puf) * 100 + "%\t" + cashAmount + "\t" + pointCDS.getAccuredDays() + "\t" + accAmt);

    final double impSpread = pufConverter.pufToQuotedSpread(pointCDS, COUPON, yieldCurve, puf);
    System.out.println("imp Spread: " + impSpread);

    //flat spread calculations 
    final Period[] standardTenors = new Period[] {Period.ofMonths(6), Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5), Period.ofYears(7),
      Period.ofYears(10) };
    final int nMat = standardTenors.length;
    final LocalDate[] maturities = new LocalDate[nMat];
    final CDSAnalytic[] pillarCDS = new CDSAnalytic[nMat];
    for (int i = 0; i < nMat; i++) {
      maturities[i] = nextRolldate.plus(standardTenors[i]).minusMonths(3);
      pillarCDS[i] = new CDSAnalytic(tradeDate, stepinDate, cashSettleDate, startDate, maturities[i], PAY_ACC_ON_DEFAULT, PAYMENT_INTERVAL, STUB, PROCTECTION_START, RECOVERY_RATE);
    }
    final double[] flatSpreads = new double[nMat];
    Arrays.fill(flatSpreads, tradeLevel);
    final ISDACompliantCreditCurve creditCurve = builder.calibrateCreditCurve(pillarCDS, flatSpreads, yieldCurve);
    final double pufTrans = PRICER_MARKIT_FIX.pv(pointCDS, yieldCurve, creditCurve, COUPON);
    final double cashAmountTrans = notional * pufTrans - accAmt;
    System.out.println(pufTrans + "\t" + cashAmountTrans);

  }
}
