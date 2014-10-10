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

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Month;
import org.threeten.bp.Period;
import org.threeten.bp.format.DateTimeFormatter;

import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalytic;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalyticFactory;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSRiskFactors;
import com.opengamma.analytics.financial.credit.isdastandardmodel.FastCreditCurveBuilder;
import com.opengamma.analytics.financial.credit.isdastandardmodel.FiniteDifferenceSpreadSensitivityCalculator;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDABaseTest;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCreditCurveBuilder;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCreditCurveBuilder.ArbitrageHandling;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.MarketQuoteConverter;
import com.opengamma.analytics.financial.credit.isdastandardmodel.PriceType;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.MatrixAlgebra;
import com.opengamma.analytics.math.matrix.OGMatrixAlgebra;
import com.opengamma.util.test.TestGroup;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class SingleNameCDSE2ETest extends ISDABaseTest {
  private static final MatrixAlgebra MA = new OGMatrixAlgebra();
  private static final DateTimeFormatter DATE_FORMATT = DateTimeFormatter.ofPattern("dd-MMM-yy");

  //Calculators
  private static final MarketQuoteConverter PUF_CONVERTER = new MarketQuoteConverter(OG_FIX);
  private static final FiniteDifferenceSpreadSensitivityCalculator FD_SPREAD_SENSE_CAL = new FiniteDifferenceSpreadSensitivityCalculator(
      OG_FIX);
  private static final CDSRiskFactors RISK_CAL = new CDSRiskFactors(OG_FIX);

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
      Period.ofYears(3), Period.ofYears(4), Period.ofYears(5), Period.ofYears(6),
      Period.ofYears(7), Period.ofYears(8), Period.ofYears(9), Period.ofYears(10), Period.ofYears(12),
      Period.ofYears(15), Period.ofYears(20), Period.ofYears(25), Period.ofYears(30) };
  private static final LocalDate[] BUCKET_DATES = getIMMDateSet(NEXT_IMM, BUCKETS);
  private static final CDSAnalytic[] BUCKET_CDSS = CDS_FACTORY.makeCDS(TRADE_DATE, STARTDATE, BUCKET_DATES);

  @Test(enabled = false)
  public void IMMCDSTest1() {
    // Build pricing CDS
    Period tenor = Period.of(2, 3, 0);
    LocalDate maturityDate = NEXT_IMM.plus(tenor); // 2013-09-20
    CDSAnalytic pricingCDS = new CDSAnalytic(TRADE_DATE, STEPIN, CASH_SETTLE_DATE, STARTDATE, maturityDate,
        PAY_ACC_ON_DEFAULT, PAYMENT_INTERVAL, STUB, PROCTECTION_START, RECOVERY_RATE);

    int accrualDays = pricingCDS.getAccuredDays();
    double accruedPermium = pricingCDS.getAccruedPremium(COUPON) * NOTIONAL;
    System.out.println(accrualDays);
    System.out.println(accruedPermium);

    double cleanPV = PRICER_OG_FIX.pv(pricingCDS, YIELD_CURVE, CREDIT_CURVE, COUPON) * NOTIONAL;
    double dirtyPV = PRICER_OG_FIX.pv(pricingCDS, YIELD_CURVE, CREDIT_CURVE, COUPON, PriceType.DIRTY) * NOTIONAL;
    System.out.println(cleanPV);
    System.out.println(dirtyPV);

    ISDACompliantYieldCurve constantCurve = new ISDACompliantYieldCurve(1.0, 0.0);
    double expectedLoss = PRICER_OG_FIX.protectionLeg(pricingCDS, constantCurve, CREDIT_CURVE) * NOTIONAL;
    System.out.println(expectedLoss);

    double cleanRPV01 = PRICER_OG_FIX.annuity(pricingCDS, YIELD_CURVE, CREDIT_CURVE) * NOTIONAL * ONE_BP;
    double dirtyRPV01 = PRICER_OG_FIX.annuity(pricingCDS, YIELD_CURVE, CREDIT_CURVE, PriceType.DIRTY) * NOTIONAL *
        ONE_BP;
    System.out.println(cleanRPV01);
    System.out.println(dirtyRPV01);

    double parSpread = PRICER_OG_FIX.parSpread(pricingCDS, YIELD_CURVE, CREDIT_CURVE) * TEN_THOUSAND; // BPS
    System.out.println(parSpread);

    double parallelCS01 = FD_SPREAD_SENSE_CAL.parallelCS01FromCreditCurve(pricingCDS, COUPON, BUCKET_CDSS, YIELD_CURVE,
        CREDIT_CURVE, ONE_BP) * ONE_BP * NOTIONAL;
    System.out.println(parallelCS01);

    double[] bucketedCS01 = FD_SPREAD_SENSE_CAL.bucketedCS01FromCreditCurve(pricingCDS, COUPON, BUCKET_CDSS,
        YIELD_CURVE,
        CREDIT_CURVE, ONE_BP);
    for (int i = 0; i < bucketedCS01.length; ++i) {
      bucketedCS01[i] *= (NOTIONAL * ONE_BP);
    }
    System.out.println(new DoubleMatrix1D(bucketedCS01));
    
    double valueOnDefault = RISK_CAL.valueOnDefault(pricingCDS, YIELD_CURVE, CREDIT_CURVE, COUPON) * NOTIONAL;
    System.out.println(valueOnDefault);
    double recovery01 = RISK_CAL.recoveryRateSensitivity(pricingCDS, YIELD_CURVE, CREDIT_CURVE) * NOTIONAL;
    System.out.println(recovery01);
  }

  @Test(enabled = false)
  public void IMMCDSTest2() {
    // Build pricing CDS
    Period tenor = Period.of(18, 0, 0);
    LocalDate maturityDate = NEXT_IMM.plus(tenor); // 2029-06-20
    CDSAnalytic pricingCDS = new CDSAnalytic(TRADE_DATE, STEPIN, CASH_SETTLE_DATE, STARTDATE, maturityDate,
        PAY_ACC_ON_DEFAULT, PAYMENT_INTERVAL, STUB, PROCTECTION_START, RECOVERY_RATE);

    int accrualDays = pricingCDS.getAccuredDays();
    double accruedPermium = pricingCDS.getAccruedPremium(COUPON) * NOTIONAL;
    System.out.println(accrualDays);
    System.out.println(accruedPermium);

    double cleanPV = PRICER_OG_FIX.pv(pricingCDS, YIELD_CURVE, CREDIT_CURVE, COUPON) * NOTIONAL;
    double dirtyPV = PRICER_OG_FIX.pv(pricingCDS, YIELD_CURVE, CREDIT_CURVE, COUPON, PriceType.DIRTY) * NOTIONAL;
    System.out.println(cleanPV);
    System.out.println(dirtyPV);

    ISDACompliantYieldCurve constantCurve = new ISDACompliantYieldCurve(1.0, 0.0);
    double expectedLoss = PRICER_OG_FIX.protectionLeg(pricingCDS, constantCurve, CREDIT_CURVE) * NOTIONAL;
    System.out.println(expectedLoss);

    double cleanRPV01 = PRICER_OG_FIX.annuity(pricingCDS, YIELD_CURVE, CREDIT_CURVE) * NOTIONAL * ONE_BP;
    double dirtyRPV01 = PRICER_OG_FIX.annuity(pricingCDS, YIELD_CURVE, CREDIT_CURVE, PriceType.DIRTY) * NOTIONAL *
        ONE_BP;
    System.out.println(cleanRPV01);
    System.out.println(dirtyRPV01);

    double parSpread = PRICER_OG_FIX.parSpread(pricingCDS, YIELD_CURVE, CREDIT_CURVE) * TEN_THOUSAND; // BPS
    System.out.println(parSpread);

    double parallelCS01 = FD_SPREAD_SENSE_CAL.parallelCS01FromCreditCurve(pricingCDS, COUPON, BUCKET_CDSS, YIELD_CURVE,
        CREDIT_CURVE, ONE_BP) * ONE_BP * NOTIONAL;
    System.out.println(parallelCS01);

    double[] bucketedCS01 = FD_SPREAD_SENSE_CAL.bucketedCS01FromCreditCurve(pricingCDS, COUPON, BUCKET_CDSS,
        YIELD_CURVE,
        CREDIT_CURVE, ONE_BP);
    for (int i = 0; i < bucketedCS01.length; ++i) {
      bucketedCS01[i] *= (NOTIONAL * ONE_BP);
    }
    System.out.println(new DoubleMatrix1D(bucketedCS01));

    double valueOnDefault = RISK_CAL.valueOnDefault(pricingCDS, YIELD_CURVE, CREDIT_CURVE, COUPON) * NOTIONAL;
    System.out.println(valueOnDefault);
    double recovery01 = RISK_CAL.recoveryRateSensitivity(pricingCDS, YIELD_CURVE, CREDIT_CURVE) * NOTIONAL;
    System.out.println(recovery01);
  }

  private void assertDoubleArray(double[] expected, double[] result, double relTol) {
    int nValues = expected.length;
    assertEquals(nValues, result.length);
    for (int i = 0; i < nValues; ++i) {
      assertEquals(expected[i], result[i], Math.abs(expected[i]) * relTol);
    }

  }
}
