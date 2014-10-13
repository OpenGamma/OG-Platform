/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel.demo;

import static com.opengamma.analytics.financial.credit.isdastandardmodel.IMMDateLogic.getIMMDateSet;
import static com.opengamma.analytics.financial.credit.isdastandardmodel.IMMDateLogic.getNextIMMDate;
import static com.opengamma.analytics.financial.credit.isdastandardmodel.IMMDateLogic.getPrevIMMDate;
import static com.opengamma.financial.convention.businessday.BusinessDayDateUtils.addWorkDays;
import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Month;
import org.threeten.bp.Period;

import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalytic;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalyticFactory;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSRiskFactors;
import com.opengamma.analytics.financial.credit.isdastandardmodel.FastCreditCurveBuilder;
import com.opengamma.analytics.financial.credit.isdastandardmodel.FiniteDifferenceSpreadSensitivityCalculator;
import com.opengamma.analytics.financial.credit.isdastandardmodel.HedgeRatioCalculator;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDABaseTest;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCreditCurveBuilder;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCreditCurveBuilder.ArbitrageHandling;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.PriceType;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.test.TestGroup;

/**
 * End-to-end test for single name CDSs
 */
@Test(groups = TestGroup.UNIT)
public class SingleNameCDSE2ETest extends ISDABaseTest {

  //Calculators
  private static final FiniteDifferenceSpreadSensitivityCalculator FD_SPREAD_SENSE_CAL = new FiniteDifferenceSpreadSensitivityCalculator(
      OG_FIX);
  private static final CDSRiskFactors RISK_CAL = new CDSRiskFactors(OG_FIX);
  private static final HedgeRatioCalculator HEDGE_CAL = new HedgeRatioCalculator(OG_FIX);

  //Trade
  private static final CDSAnalyticFactory CDS_FACTORY = new CDSAnalyticFactory(0.4);
  private static final double NOTIONAL = 1e6;
  private static final double COUPON = 0.01;
  private static final LocalDate TRADE_DATE = LocalDate.of(2011, Month.JUNE, 13);
  private static final LocalDate NEXT_IMM = getNextIMMDate(TRADE_DATE);
  private static final LocalDate STEPIN = TRADE_DATE.plusDays(1);
  private static final LocalDate CASH_SETTLE_DATE = addWorkDays(TRADE_DATE, 3, DEFAULT_CALENDAR); // AKA valuation date
  private static final LocalDate STARTDATE = getPrevIMMDate(TRADE_DATE);

  //Yield curve
  private static final LocalDate SPOT_DATE = LocalDate.of(2011, Month.JUNE, 15);
  private static final String[] YIELD_CURVE_POINTS = new String[] {"1M", "2M", "3M", "6M", "9M", "1Y", "2Y", "3Y",
      "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "11Y", "12Y", "15Y", "20Y", "25Y", "30Y" };
  private static final String[] YIELD_CURVE_INSTRUMENTS = new String[] {"M", "M", "M", "M", "M", "M", "S", "S", "S",
      "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S" };
  private static final double[] YIELD_CURVE_RATES = new double[] {0.00445, 0.009488, 0.012337, 0.017762, 0.01935,
      0.020838, 0.01652, 0.02018, 0.023033, 0.02525, 0.02696, 0.02825, 0.02931, 0.03017,
      0.03092, 0.0316, 0.03231, 0.03367, 0.03419, 0.03411, 0.03412 };
  private static final ISDACompliantYieldCurve YIELD_CURVE = makeYieldCurve(TRADE_DATE, SPOT_DATE, YIELD_CURVE_POINTS,
      YIELD_CURVE_INSTRUMENTS, YIELD_CURVE_RATES, ACT360, D30360, Period.ofYears(1));

  //Credit curve form pillar CDSs
  private static final Period[] TENORS = new Period[] {Period.ofMonths(6), Period.ofYears(1), Period.ofYears(3),
      Period.ofYears(5), Period.ofYears(7), Period.ofYears(10) };
  private static final LocalDate[] PILLAR_DATES = getIMMDateSet(NEXT_IMM, TENORS);
  private static final LocalDate[] IMM_DATES = getIMMDateSet(NEXT_IMM, 41);
  private static final LocalDate[] MATURITIES_6M_STEP;
  private static final LocalDate[] MATURITIES_1Y_STEP;
  private static final double[] SPREADS = new double[] {0.007926718, 0.007926718, 0.012239372, 0.016978579,
      0.019270856, 0.02086048 };
  private static final CDSAnalytic[] PILLAR_CDSS;
  private static final ISDACompliantCreditCurve CREDIT_CURVE;
  static {
    final ISDACompliantCreditCurveBuilder curveBuilder = new FastCreditCurveBuilder(OG_FIX,
        ArbitrageHandling.ZeroHazardRate);

    final int nPillars = PILLAR_DATES.length;
    PILLAR_CDSS = new CDSAnalytic[nPillars];
    for (int i = 0; i < nPillars; i++) {
      PILLAR_CDSS[i] = new CDSAnalytic(TRADE_DATE, STEPIN, CASH_SETTLE_DATE, STARTDATE, PILLAR_DATES[i],
          PAY_ACC_ON_DEFAULT, PAYMENT_INTERVAL, STUB, PROCTECTION_START, RECOVERY_RATE);
    }

    CREDIT_CURVE = curveBuilder.calibrateCreditCurve(PILLAR_CDSS, SPREADS, YIELD_CURVE);

    final int n = IMM_DATES.length;
    final LocalDate[] temp = new LocalDate[n];
    int count = 0;
    for (int i = 0; i < n; i = i + 2) {
      temp[count++] = IMM_DATES[i];
    }
    MATURITIES_6M_STEP = new LocalDate[count];
    System.arraycopy(temp, 0, MATURITIES_6M_STEP, 0, count);

    count = 0;
    for (int i = 0; i < n; i = i + 4) {
      temp[count++] = IMM_DATES[i];
    }
    MATURITIES_1Y_STEP = new LocalDate[count];
    System.arraycopy(temp, 0, MATURITIES_1Y_STEP, 0, count);
  }

  //Bucket CDSs
  private static final Period[] BUCKETS = new Period[] {Period.ofMonths(6), Period.ofYears(1), Period.ofYears(2),
      Period.ofYears(3), Period.ofYears(4), Period.ofYears(5), Period.ofYears(6), Period.ofYears(7), Period.ofYears(8),
      Period.ofYears(9), Period.ofYears(10), Period.ofYears(12), Period.ofYears(15), Period.ofYears(20),
      Period.ofYears(25), Period.ofYears(30) };
  private static final LocalDate[] BUCKET_DATES = getIMMDateSet(NEXT_IMM, BUCKETS);
  private static final CDSAnalytic[] BUCKET_CDSS = CDS_FACTORY.makeCDS(TRADE_DATE, STARTDATE, BUCKET_DATES);

  //Hedge CDS
  private static final Period[] HEDGES = new Period[] {Period.of(1, 6, 0), Period.of(2, 0, 0), Period.of(6, 0, 0),
      Period.of(9, 0, 0), Period.of(20, 0, 0) };
  private static final LocalDate[] HEDGE_DATES = getIMMDateSet(NEXT_IMM, HEDGES);
  private static final CDSAnalytic[] HEDGE_CDSS = CDS_FACTORY.makeCDS(TRADE_DATE, STARTDATE, HEDGE_DATES);
  private static final double[] HEDGE_COUPON = new double[HEDGES.length];
  static {
    Arrays.fill(HEDGE_COUPON, COUPON);
  }

  private static final double TOL = 1.0e-10;
  
  /**
   * Standard CDS with short maturity
   */
  @Test
  public void IMMCDSTest1() {
    // Build pricing CDS
    Period tenor = Period.of(2, 3, 0);
    LocalDate maturityDate = NEXT_IMM.plus(tenor); // 2013-09-20
    CDSAnalytic pricingCDS = new CDSAnalytic(TRADE_DATE, STEPIN, CASH_SETTLE_DATE, STARTDATE, maturityDate,
        PAY_ACC_ON_DEFAULT, PAYMENT_INTERVAL, STUB, PROCTECTION_START, RECOVERY_RATE);

    int accrualDays = pricingCDS.getAccuredDays();
    double accruedPermium = pricingCDS.getAccruedPremium(COUPON) * NOTIONAL;
    double cleanPV = PRICER_OG_FIX.pv(pricingCDS, YIELD_CURVE, CREDIT_CURVE, COUPON) * NOTIONAL;
    double dirtyPV = PRICER_OG_FIX.pv(pricingCDS, YIELD_CURVE, CREDIT_CURVE, COUPON, PriceType.DIRTY) * NOTIONAL;
    ISDACompliantYieldCurve constantCurve = new ISDACompliantYieldCurve(1.0, 0.0);
    double expectedLoss = PRICER_OG_FIX.protectionLeg(pricingCDS, constantCurve, CREDIT_CURVE) * NOTIONAL;
    double cleanRPV01 = PRICER_OG_FIX.annuity(pricingCDS, YIELD_CURVE, CREDIT_CURVE) * NOTIONAL * ONE_BP;
    double dirtyRPV01 = PRICER_OG_FIX.annuity(pricingCDS, YIELD_CURVE, CREDIT_CURVE, PriceType.DIRTY) * NOTIONAL *
        ONE_BP;
    double parSpread = PRICER_OG_FIX.parSpread(pricingCDS, YIELD_CURVE, CREDIT_CURVE) * TEN_THOUSAND; // BPS
    double parallelCS01 = FD_SPREAD_SENSE_CAL.parallelCS01FromCreditCurve(pricingCDS, COUPON, BUCKET_CDSS, YIELD_CURVE,
        CREDIT_CURVE, ONE_BP) * ONE_BP * NOTIONAL;
    double[] bucketedCS01 = FD_SPREAD_SENSE_CAL.bucketedCS01FromCreditCurve(pricingCDS, COUPON, BUCKET_CDSS,
        YIELD_CURVE, CREDIT_CURVE, ONE_BP);
    for (int i = 0; i < bucketedCS01.length; ++i) {
      bucketedCS01[i] *= (NOTIONAL * ONE_BP);
    }
    double valueOnDefault = RISK_CAL.valueOnDefault(pricingCDS, YIELD_CURVE, CREDIT_CURVE, COUPON) * NOTIONAL;
    double recovery01 = RISK_CAL.recoveryRateSensitivity(pricingCDS, YIELD_CURVE, CREDIT_CURVE) * NOTIONAL;
    double[] bucketCDSCoupons = new double[PILLAR_CDSS.length];
    Arrays.fill(bucketCDSCoupons, COUPON);
    DoubleMatrix1D hedgeRatio = HEDGE_CAL.getHedgeRatios(pricingCDS, COUPON, HEDGE_CDSS, HEDGE_COUPON,
        CREDIT_CURVE, YIELD_CURVE);

    double[] expectedBCS01 = new double[] {-0.059816728292805266, -0.1756625232531006, 146.41928967059485,
        74.44722664445152, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
    double[] expectedRatio = new double[] {-0.4961598054357507, 1.4961464596451923, 1.4642425207782464E-10,
        -1.7783577172741516E-11, 1.6745736381180108E-11 };

    assertEquals("accrual days", 86, accrualDays);
    assertEqualsRelativeTol("accrued permium", 2388.888888888889, accruedPermium, TOL);
    assertEqualsRelativeTol("clean PV", 3353.352757004241, cleanPV, TOL);
    assertEqualsRelativeTol("dirty PV", 964.4638681153561, dirtyPV, TOL);
    assertEqualsRelativeTol("expected loss", 26064.180466391186, expectedLoss, TOL);
    assertEqualsRelativeTol("clean RPV01", 221.26105981697694, cleanRPV01, TOL);
    assertEqualsRelativeTol("dirty RPV01", 245.14994870586577, dirtyRPV01, TOL);
    assertEqualsRelativeTol("par spread (BPS)", 115.15563904366216, parSpread, TOL);
    assertEqualsRelativeTol("parallel CS01", 220.58629230464064, parallelCS01, TOL);
    assertDoubleArray("bucketed CS01", expectedBCS01, bucketedCS01, TOL);
    assertEqualsRelativeTol("value on default", 596646.6472429958, valueOnDefault, TOL);
    assertEqualsRelativeTol("recovery01", -42465.764564503224, recovery01, TOL);
    assertDoubleArray("hedge ratio", expectedRatio, hedgeRatio.getData(), TOL);
  }

  /**
   * Standard CDS with longer maturity
   */
  @Test
  public void IMMCDSTest2() {
    // Build pricing CDS
    Period tenor = Period.of(17, 0, 0);
    LocalDate maturityDate = NEXT_IMM.plus(tenor); // 2028-06-20
    CDSAnalytic pricingCDS = new CDSAnalytic(TRADE_DATE, STEPIN, CASH_SETTLE_DATE, STARTDATE, maturityDate,
        PAY_ACC_ON_DEFAULT, PAYMENT_INTERVAL, STUB, PROCTECTION_START, RECOVERY_RATE);

    int accrualDays = pricingCDS.getAccuredDays();
    double accruedPermium = pricingCDS.getAccruedPremium(COUPON) * NOTIONAL;
    double cleanPV = PRICER_OG_FIX.pv(pricingCDS, YIELD_CURVE, CREDIT_CURVE, COUPON) * NOTIONAL;
    double dirtyPV = PRICER_OG_FIX.pv(pricingCDS, YIELD_CURVE, CREDIT_CURVE, COUPON, PriceType.DIRTY) * NOTIONAL;
    ISDACompliantYieldCurve constantCurve = new ISDACompliantYieldCurve(1.0, 0.0);
    double expectedLoss = PRICER_OG_FIX.protectionLeg(pricingCDS, constantCurve, CREDIT_CURVE) * NOTIONAL;
    double cleanRPV01 = PRICER_OG_FIX.annuity(pricingCDS, YIELD_CURVE, CREDIT_CURVE) * NOTIONAL * ONE_BP;
    double dirtyRPV01 = PRICER_OG_FIX.annuity(pricingCDS, YIELD_CURVE, CREDIT_CURVE, PriceType.DIRTY) * NOTIONAL *
        ONE_BP;
    double parSpread = PRICER_OG_FIX.parSpread(pricingCDS, YIELD_CURVE, CREDIT_CURVE) * TEN_THOUSAND; // BPS
    double parallelCS01 = FD_SPREAD_SENSE_CAL.parallelCS01FromCreditCurve(pricingCDS, COUPON, BUCKET_CDSS, YIELD_CURVE,
        CREDIT_CURVE, ONE_BP) * ONE_BP * NOTIONAL;
    double[] bucketedCS01 = FD_SPREAD_SENSE_CAL.bucketedCS01FromCreditCurve(pricingCDS, COUPON, BUCKET_CDSS,
        YIELD_CURVE, CREDIT_CURVE, ONE_BP);
    for (int i = 0; i < bucketedCS01.length; ++i) {
      bucketedCS01[i] *= (NOTIONAL * ONE_BP);
    }
    double valueOnDefault = RISK_CAL.valueOnDefault(pricingCDS, YIELD_CURVE, CREDIT_CURVE, COUPON) * NOTIONAL;
    double recovery01 = RISK_CAL.recoveryRateSensitivity(pricingCDS, YIELD_CURVE, CREDIT_CURVE) * NOTIONAL;
    double[] bucketCDSCoupons = new double[PILLAR_CDSS.length];
    Arrays.fill(bucketCDSCoupons, COUPON);
    DoubleMatrix1D hedgeRatio = HEDGE_CAL.getHedgeRatios(pricingCDS, COUPON, HEDGE_CDSS, HEDGE_COUPON,
        CREDIT_CURVE, YIELD_CURVE);

    double[] expectedBCS01 = new double[] {-0.24881405158438952, -0.8250965690681511, -2.065006424661897,
        -2.593485656315897, -3.3336057889687964, -4.131193830109181, -5.022055333397946, -6.00285454821492,
        -7.065252961069302, -8.198950442794839, -14.137560442079034, -30.46976382278799, 482.0743103690905,
        496.1001262353548, 0.0, 0.0 };
    double[] expectedRatio = new double[] {3.732938159080875E-4, -9.054326559634073E-4, -9.917078640324583E-4,
        0.16704887600018242, 0.8382683669540425 };

    assertEquals("accrual days", 86, accrualDays);
    assertEqualsRelativeTol("accrued permium", 2388.888888888889, accruedPermium, TOL);
    assertEqualsRelativeTol("clean PV", 127830.95471515371, cleanPV, TOL);
    assertEqualsRelativeTol("dirty PV", 125442.06582626482, dirtyPV, TOL);
    assertEqualsRelativeTol("expected loss", 296176.8396749446, expectedLoss, TOL);
    assertEqualsRelativeTol("clean RPV01", 1041.0542259606402, cleanRPV01, TOL);
    assertEqualsRelativeTol("dirty RPV01", 1064.9431148495291, dirtyRPV01, TOL);
    assertEqualsRelativeTol("par spread (BPS)", 222.7899100041564, parSpread, TOL);
    assertEqualsRelativeTol("parallel CS01", 891.7343555394641, parallelCS01, TOL);
    assertDoubleArray("bucketed CS01", expectedBCS01, bucketedCS01, TOL);
    assertEqualsRelativeTol("value on default", 472169.0452848463, valueOnDefault, TOL);
    assertEqualsRelativeTol("recovery01", -386560.6288520296, recovery01, TOL);
    assertDoubleArray("hedge ratio", expectedRatio, hedgeRatio.getData(), TOL);
  }

  /**
   * Example of legacy CDS
   */
  @Test
  public void LegacyCDSTest() {
    Period tenor = Period.of(10, 0, 0);
    LocalDate startDate = STEPIN; // T+1 start
    LocalDate maturityDate = STEPIN.plus(tenor); // counted from effective date
    CDSAnalytic pricingCDS = new CDSAnalytic(TRADE_DATE, STEPIN, CASH_SETTLE_DATE, startDate, maturityDate,
        PAY_ACC_ON_DEFAULT, PAYMENT_INTERVAL, STUB, PROCTECTION_START, RECOVERY_RATE);

    double coupon = PRICER_OG_FIX.parSpread(pricingCDS, YIELD_CURVE, CREDIT_CURVE); // coupon s.t. zero upfront value
    int accrualDays = pricingCDS.getAccuredDays();
    double accruedPermium = pricingCDS.getAccruedPremium(COUPON) * NOTIONAL;
    double cleanPV = PRICER_OG_FIX.pv(pricingCDS, YIELD_CURVE, CREDIT_CURVE, coupon) * NOTIONAL;
    double dirtyPV = PRICER_OG_FIX.pv(pricingCDS, YIELD_CURVE, CREDIT_CURVE, coupon, PriceType.DIRTY) * NOTIONAL;
    ISDACompliantYieldCurve constantCurve = new ISDACompliantYieldCurve(1.0, 0.0);
    double expectedLoss = PRICER_OG_FIX.protectionLeg(pricingCDS, constantCurve, CREDIT_CURVE) * NOTIONAL;
    double cleanRPV01 = PRICER_OG_FIX.annuity(pricingCDS, YIELD_CURVE, CREDIT_CURVE) * NOTIONAL * ONE_BP;
    double dirtyRPV01 = PRICER_OG_FIX.annuity(pricingCDS, YIELD_CURVE, CREDIT_CURVE, PriceType.DIRTY) * NOTIONAL *
        ONE_BP;
    double parSpread = coupon * TEN_THOUSAND; // BPS
    double parallelCS01 = FD_SPREAD_SENSE_CAL.parallelCS01FromCreditCurve(pricingCDS, coupon, BUCKET_CDSS, YIELD_CURVE,
        CREDIT_CURVE, ONE_BP) * ONE_BP * NOTIONAL;
    double[] bucketedCS01 = FD_SPREAD_SENSE_CAL.bucketedCS01FromCreditCurve(pricingCDS, coupon, BUCKET_CDSS,
        YIELD_CURVE, CREDIT_CURVE, ONE_BP);
    for (int i = 0; i < bucketedCS01.length; ++i) {
      bucketedCS01[i] *= (NOTIONAL * ONE_BP);
    }
    double valueOnDefault = RISK_CAL.valueOnDefault(pricingCDS, YIELD_CURVE, CREDIT_CURVE, coupon) * NOTIONAL;
    double recovery01 = RISK_CAL.recoveryRateSensitivity(pricingCDS, YIELD_CURVE, CREDIT_CURVE) * NOTIONAL;
    double[] bucketCDSCoupons = new double[PILLAR_CDSS.length];
    Arrays.fill(bucketCDSCoupons, COUPON);
    DoubleMatrix1D hedgeRatio = HEDGE_CAL.getHedgeRatios(pricingCDS, coupon, HEDGE_CDSS, HEDGE_COUPON,
        CREDIT_CURVE, YIELD_CURVE);

    double[] expectedBCS01 = new double[] {2.7708391137082344E-5, -3.7192471324942744E-6, 6.128070273447861E-5,
        3.3650313091548867E-4, 6.236303418116762E-4, 2.3971802320943425E-4, 5.053599205773196E-4, 6.192200641130796E-4,
        2.2392077081434536E-4, 11.28357139537628, 758.7513550775249, 0.0, 0.0, 0.0, 0.0, 0.0 };
    double[] expectedRatio = new double[] {-0.01656596110844081, 0.06035538569241238, 0.059185025018514595,
        0.887776791735758, 0.14988720733714989 };

    assertEquals("accrual days", 0, accrualDays);
    assertEqualsRelativeTol("accrued permium", 0.0, accruedPermium, TOL);
    assertEqualsRelativeTol("clean PV", 0.0, cleanPV, TOL);
    assertEqualsRelativeTol("dirty PV", 0.0, dirtyPV, TOL); // no accrued for legacy CDS
    assertEqualsRelativeTol("expected loss", 185414.3066614696, expectedLoss, TOL);
    assertEqualsRelativeTol("clean RPV01", 770.0746171228492, cleanRPV01, TOL);
    assertEqualsRelativeTol("dirty RPV01", 770.0746171228492, dirtyRPV01, TOL); // no accrued for legacy CDS
    assertEqualsRelativeTol("par spread (BPS)", 208.5446941701706, parSpread, TOL);
    assertEqualsRelativeTol("parallel CS01", 769.4893870984209, parallelCS01, TOL);
    assertDoubleArray("bucketed CS01", expectedBCS01, bucketedCS01, TOL);
    assertEqualsRelativeTol("value on default", 600000.0, valueOnDefault, TOL);
    assertEqualsRelativeTol("recovery01", -267658.2925268264, recovery01, TOL);
    assertDoubleArray("hedge ratio", expectedRatio, hedgeRatio.getData(), TOL);
  }

  private void assertEqualsRelativeTol(String message, double expected, double result, double relTol) {
    double ref = Math.abs(expected);
    double tol = ref < 1.0e-10 ? relTol : Math.abs(expected) * relTol;
    assertEquals(message, expected, result, tol);
  }

  private void assertDoubleArray(String message, double[] expected, double[] result, double relTol) {
    int nValues = expected.length;
    assertEquals(nValues, result.length);
    for (int i = 0; i < nValues; ++i) {
      assertEqualsRelativeTol(message + "(" + i + "-th element)", expected[i], result[i], Math.abs(expected[i]) *
          relTol);
    }

  }
}
