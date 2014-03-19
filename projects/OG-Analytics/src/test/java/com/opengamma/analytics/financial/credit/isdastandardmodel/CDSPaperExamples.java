/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel;

import static com.opengamma.analytics.financial.credit.isdastandardmodel.IMMDateLogic.getIMMDateSet;
import static com.opengamma.analytics.financial.credit.isdastandardmodel.IMMDateLogic.getNextIMMDate;
import static com.opengamma.financial.convention.businessday.BusinessDayDateUtils.addWorkDays;

import java.util.Arrays;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Month;
import org.threeten.bp.Period;
import org.threeten.bp.format.DateTimeFormatter;

import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCreditCurveBuilder.ArbitrageHandling;
import com.opengamma.analytics.financial.model.BumpType;
import com.opengamma.analytics.math.linearalgebra.LUDecompositionCommons;
import com.opengamma.analytics.math.linearalgebra.LUDecompositionResult;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.MatrixAlgebra;
import com.opengamma.analytics.math.matrix.OGMatrixAlgebra;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class CDSPaperExamples extends ISDABaseTest {
  private static final MatrixAlgebra MA = new OGMatrixAlgebra();
  private static final DateTimeFormatter DATE_FORMATT = DateTimeFormatter.ofPattern("dd-MMM-yy");

  private static final MarketQuoteConverter PUF_CONVERTER = new MarketQuoteConverter(MARKIT_FIX);
  private static final FiniteDifferenceSpreadSensitivityCalculator FD_SPREAD_SENSE_CAL = new FiniteDifferenceSpreadSensitivityCalculator(MARKIT_FIX);
  private static final AnalyticSpreadSensitivityCalculator ANAL_SPREAD_SENSE_CAL = new AnalyticSpreadSensitivityCalculator(MARKIT_FIX);
  private static final CDSAnalyticFactory CDS_FACTORY = new CDSAnalyticFactory(0.4);

  private static final LocalDate TODAY = LocalDate.of(2011, Month.JUNE, 13);
  private static final LocalDate NEXT_IMM = getNextIMMDate(TODAY);

  private static final LocalDate TRADE_DATE = LocalDate.of(2011, Month.JUNE, 13);
  private static final LocalDate STEPIN = TRADE_DATE.plusDays(1);
  private static final LocalDate CASH_SETTLE_DATE = addWorkDays(TRADE_DATE, 3, DEFAULT_CALENDAR); // AKA valuation date
  private static final LocalDate STARTDATE = LocalDate.of(2011, Month.MARCH, 20);

  private static final Period[] TENORS = new Period[] {Period.ofMonths(6), Period.ofYears(1), Period.ofYears(3), Period.ofYears(5), Period.ofYears(7), Period.ofYears(10) };
  //  private static final LocalDate NEXT_IMM = getNextIMMDate(EFFECTIVE_DATE);
  private static final LocalDate[] PILLAR_DATES = getIMMDateSet(NEXT_IMM, TENORS);
  private static final LocalDate[] IMM_DATES = getIMMDateSet(NEXT_IMM, 41);
  private static final LocalDate[] MATURITIES_6M_STEP;
  private static final LocalDate[] MATURITIES_1Y_STEP;

  //yield curve
  private static final LocalDate SPOT_DATE = LocalDate.of(2011, Month.JUNE, 15);
  private static final String[] YIELD_CURVE_POINTS = new String[] {"1M", "2M", "3M", "6M", "9M", "1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "11Y", "12Y", "15Y", "20Y", "25Y", "30Y" };
  private static final String[] YIELD_CURVE_INSTRUMENTS = new String[] {"M", "M", "M", "M", "M", "M", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S" };
  private static final double[] YIELD_CURVE_RATES = new double[] {0.00445, 0.009488, 0.012337, 0.017762, 0.01935, 0.020838, 0.01652, 0.02018, 0.023033, 0.02525, 0.02696, 0.02825, 0.02931, 0.03017,
    0.03092, 0.0316, 0.03231, 0.03367, 0.03419, 0.03411, 0.03412 };
  private static final ISDACompliantYieldCurve YIELD_CURVE = makeYieldCurve(TRADE_DATE, SPOT_DATE, YIELD_CURVE_POINTS, YIELD_CURVE_INSTRUMENTS, YIELD_CURVE_RATES, ACT360, D30360, Period.ofYears(1));

  private static final double COUPON = 0.01;
  private static final double[] SPREADS = new double[] {0.007926718, 0.007926718, 0.012239372, 0.016978579, 0.019270856, 0.02086048 };
  private static final CDSAnalytic[] PILLAR_CDSS;
  private static final ISDACompliantCreditCurve CREDIT_CURVE;

  static {
    final ISDACompliantCreditCurveBuilder curveBuilder = new FastCreditCurveBuilder(MARKIT_FIX, ArbitrageHandling.ZeroHazardRate);

    final int nPillars = PILLAR_DATES.length;
    PILLAR_CDSS = new CDSAnalytic[nPillars];
    for (int i = 0; i < nPillars; i++) {
      PILLAR_CDSS[i] = new CDSAnalytic(TRADE_DATE, STEPIN, CASH_SETTLE_DATE, STARTDATE, PILLAR_DATES[i], PAY_ACC_ON_DEFAULT, PAYMENT_INTERVAL, STUB, PROCTECTION_START, RECOVERY_RATE);
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

  @Test(enabled = false)
  public void yieldCurveDump() {
    final int n = YIELD_CURVE.getNumberOfKnots();
    for (int i = 0; i < n; i++) {
      System.out.println(YIELD_CURVE.getTimeAtIndex(i) + "\t" + YIELD_CURVE.getZeroRateAtIndex(i));
    }
  }

  @Test(enabled = false)
  public void creditCurveDump() {
    System.out.println(PILLAR_CDSS[0].getAccuredDays());
    final int n = PILLAR_DATES.length;
    for (int i = 0; i < n; i++) {
      final double t = CREDIT_CURVE.getTimeAtIndex(i);
      System.out.println(PILLAR_DATES[i].toString(DATE_FORMATT) + "\t" + SPREADS[i] * TEN_THOUSAND + "\t" + t + "\t" + CREDIT_CURVE.getSurvivalProbability(t));
    }
  }

  @Test(enabled = false)
  public void threeWayPriceTest() {
    final double notional = 1e7;
    final Period[] tenors = new Period[] {Period.ofMonths(3), Period.ofMonths(6), Period.ofYears(1), Period.ofYears(5), Period.ofYears(10) };
    final CDSAnalytic[] cds = CDS_FACTORY.makeIMMCDS(TRADE_DATE, tenors);
    final int n = tenors.length;
    final double[][] res = new double[3][n];
    for (int i = 0; i < n; i++) {
      res[0][i] = notional * PRICER_OG_FIX.pv(cds[i], YIELD_CURVE, CREDIT_CURVE, 0.01);
      res[1][i] = notional * PRICER.pv(cds[i], YIELD_CURVE, CREDIT_CURVE, 0.01);
      res[2][i] = notional * PRICER_MARKIT_FIX.pv(cds[i], YIELD_CURVE, CREDIT_CURVE, 0.01);
    }

    System.out.println(new DoubleMatrix2D(res));
  }

  @Test(enabled = false)
  public void test() {

    final int nMat = IMM_DATES.length;
    for (int i = 0; i < nMat; i++) {
      final CDSAnalytic cds = new CDSAnalytic(TRADE_DATE, STEPIN, CASH_SETTLE_DATE, STARTDATE, IMM_DATES[i], PAY_ACC_ON_DEFAULT, PAYMENT_INTERVAL, STUB, PROCTECTION_START, RECOVERY_RATE);
      final double dPV = PRICER_MARKIT_FIX.pv(cds, YIELD_CURVE, CREDIT_CURVE, COUPON, PriceType.DIRTY);
      final double proLeg = PRICER_MARKIT_FIX.protectionLeg(cds, YIELD_CURVE, CREDIT_CURVE);

      System.out.println(IMM_DATES[i] + "\t" + dPV + "\t" + proLeg);
      //   assertEquals(MATURITIES[i].toString(), EXPECTED_UPFRONT_CHARGE[i], dPV, 1e-15);
    }

  }

  /**
   * Plots price against hazard rate 
   */
  @Test(enabled = false)
  public void funcTest() {
    final CDSAnalyticFactory factory = new CDSAnalyticFactory(1.0);
    final CDSAnalytic cds = factory.makeIMMCDS(TRADE_DATE, Period.ofYears(5));
    for (int i = 0; i < 100; i++) {
      final double lambda = 0.8 * i / 100.;
      final ISDACompliantCreditCurve cc = new ISDACompliantCreditCurve(5.0, lambda);
      final double price = PRICER_MARKIT_FIX.pv(cds, YIELD_CURVE, cc, 0.05);
      System.out.println(lambda + "\t" + price);
    }
  }

  @Test(enabled = false)
  public void spreadVsPUF() {
    final CDSAnalyticFactory factory = new CDSAnalyticFactory(0.4);
    final CDSAnalytic cds = factory.makeIMMCDS(TRADE_DATE, Period.ofYears(10));

    for (int i = 0; i < 100; i++) {
      final double qs = i * 10;
      final double puf0 = PUF_CONVERTER.convert(cds, new QuotedSpread(0.0, qs * ONE_BP), YIELD_CURVE).getPointsUpFront();
      final double puf100 = PUF_CONVERTER.convert(cds, new QuotedSpread(0.01, qs * ONE_BP), YIELD_CURVE).getPointsUpFront();
      final double puf500 = PUF_CONVERTER.convert(cds, new QuotedSpread(0.05, qs * ONE_BP), YIELD_CURVE).getPointsUpFront();
      System.out.println(qs + "\t" + puf0 + "\t" + puf100 + "\t" + puf500);
    }

  }

  @Test(enabled = false)
  public void parRateSensitivityTest() {
    final DateTimeFormatter formatt = DateTimeFormatter.ofPattern("dd-MMM-yy");
    final StringBuilder out = new StringBuilder();
    final AnalyticCDSPricer pricer = new AnalyticCDSPricer(MARKIT_FIX);
    final CDSAnalyticFactory factory = new CDSAnalyticFactory(0.4);
    final int nPillars = PILLAR_DATES.length;
    out.append("\\begin{tabular}{");
    for (int i = 0; i < nPillars + 1; i++) {
      out.append("c|");
    }
    out.append("}\n");
    out.append("\\cline{2-" + (nPillars + 1) + "}\n");
    out.append("& \\multicolumn{" + nPillars + "}{c|}{Curve Nodes}\\\\\n");
    out.append("\\hline\n");
    out.append("\\multicolumn{1}{|c|}{CDS Maturity}");
    for (int i = 0; i < nPillars; i++) {
      out.append("& " + TENORS[i].toString());
    }
    out.append("\\\\\n");
    out.append("\\hline\n");

    for (int i = 0; i < IMM_DATES.length; i = i + 2) {
      final LocalDate mat = IMM_DATES[i];
      out.append("\\multicolumn{1}{|c|}{" + mat.toString(formatt) + "}");
      final CDSAnalytic cds = factory.makeCDS(TRADE_DATE, STARTDATE, mat);
      for (int j = 0; j < nPillars; j++) {
        final double sense = pricer.parSpreadCreditSensitivity(cds, YIELD_CURVE, CREDIT_CURVE, j);
        out.append(String.format("& %.4f", sense));

      }
      out.append("\\\\\n");
    }
    out.append("\\hline\n");
    out.append("\\end{tabular}\n");

    System.out.print(out);
  }

  @Test(enabled = false)
  public void pvSensitivityTest() {
    final double coupon = 0.01;
    final DateTimeFormatter formatt = DateTimeFormatter.ofPattern("dd-MMM-yy");
    final StringBuilder out = new StringBuilder();
    final AnalyticCDSPricer pricer = new AnalyticCDSPricer(MARKIT_FIX);
    final CDSAnalyticFactory factory = new CDSAnalyticFactory(0.4);
    final int nPillars = PILLAR_DATES.length;
    out.append("\\begin{tabular}{");
    for (int i = 0; i < nPillars + 1; i++) {
      out.append("c|");
    }
    out.append("}\n");
    out.append("\\cline{2-" + (nPillars + 1) + "}\n");
    out.append("& \\multicolumn{" + nPillars + "}{c|}{Curve Nodes}\\\\\n");
    out.append("\\hline\n");
    out.append("\\multicolumn{1}{|c|}{CDS Maturity}");
    for (int i = 0; i < nPillars; i++) {
      out.append("& " + TENORS[i].toString());
    }
    out.append("\\\\\n");
    out.append("\\hline\n");

    for (int i = 0; i < nPillars; i++) {
      final LocalDate mat = PILLAR_DATES[i];
      out.append("\\multicolumn{1}{|c|}{" + mat.toString(formatt) + "}");
      final CDSAnalytic cds = factory.makeCDS(TRADE_DATE, STARTDATE, mat);
      for (int j = 0; j < nPillars; j++) {
        final double sense = pricer.pvCreditSensitivity(cds, YIELD_CURVE, CREDIT_CURVE, coupon, j);
        out.append(String.format("& %.5f", sense));

      }
      out.append("\\\\\n");
    }
    out.append("\\hline\n");
    out.append("\\end{tabular}\n");

    System.out.print(out);
  }

  @Test(enabled = false)
  public void hedgingTest1() {
    final double coupon = 0.01;
    final DateTimeFormatter formatt = DateTimeFormatter.ofPattern("dd-MMM-yy");
    final StringBuilder out = new StringBuilder();

    final CDSAnalyticFactory factory = new CDSAnalyticFactory(0.4);
    final int nPillars = PILLAR_DATES.length;
    final double[][] res = new double[nPillars][nPillars];
    for (int i = 0; i < nPillars; i++) {
      final LocalDate mat = PILLAR_DATES[i];
      final CDSAnalytic cds = factory.makeCDS(TRADE_DATE, STARTDATE, mat);
      for (int j = 0; j < nPillars; j++) {
        final double sense = PRICER.pvCreditSensitivity(cds, YIELD_CURVE, CREDIT_CURVE, coupon, j);
        res[j][i] = sense;
      }
    }
    final DoubleMatrix2D jacT = new DoubleMatrix2D(res);
    //  System.out.println(jacT.toString());
    final LUDecompositionCommons decomp = new LUDecompositionCommons();
    final LUDecompositionResult luRes = decomp.evaluate(jacT);

    out.append("\\begin{tabular}{");
    for (int i = 0; i < nPillars + 1; i++) {
      out.append("c|");
    }
    out.append("}\n");
    out.append("\\cline{2-" + (nPillars + 1) + "}\n");
    out.append("& \\multicolumn{" + nPillars + "}{c|}{Hedge Instrument Maturity}\\\\\n");
    out.append("\\hline\n");
    out.append("\\multicolumn{1}{|c|}{CDS Maturity}");
    for (int i = 0; i < nPillars; i++) {
      out.append("& " + TENORS[i].toString());
    }
    out.append("\\\\\n");
    out.append("\\hline\n");

    for (int i = 0; i < IMM_DATES.length; i = i + 2) {
      final LocalDate mat = IMM_DATES[i];
      out.append("\\multicolumn{1}{|c|}{" + mat.toString(formatt) + "}");
      final CDSAnalytic cds = factory.makeCDS(TRADE_DATE, STARTDATE, mat);

      final double[] temp = new double[nPillars];
      for (int j = 0; j < nPillars; j++) {
        final double sense = PRICER.pvCreditSensitivity(cds, YIELD_CURVE, CREDIT_CURVE, coupon, j);
        temp[j] = sense;
      }
      final DoubleMatrix1D vLambda = new DoubleMatrix1D(temp);
      //     System.out.println(vLambda);

      final DoubleMatrix1D w = luRes.solve(vLambda);
      for (int j = 0; j < nPillars; j++) {
        out.append(String.format("& %.5f", w.getEntry(j)));
      }

      out.append("\\\\\n");
    }
    out.append("\\hline\n");
    out.append("\\end{tabular}\n");
    System.out.print(out);
  }

  @SuppressWarnings("unused")
  @Test(enabled = false)
  public void hedgingTest2() {

    final double coupon = 0.01;
    final AnalyticCDSPricer pricer = new AnalyticCDSPricer(MARKIT_FIX);
    final CDSAnalyticFactory factory = new CDSAnalyticFactory(0.4);
    final LocalDate mat = LocalDate.of(2015, Month.JUNE, 20);
    final LocalDate mat1 = LocalDate.of(2014, Month.JUNE, 20);
    final LocalDate mat2 = LocalDate.of(2016, Month.JUNE, 20);
    final double[] notional = new double[] {1.0, -0.47556, -0.52474 };

    final CDSAnalytic[] cdsPort = new CDSAnalytic[3];
    cdsPort[0] = factory.makeCDS(TRADE_DATE, STARTDATE, mat);
    cdsPort[1] = factory.makeCDS(TRADE_DATE, STARTDATE, mat1);
    cdsPort[2] = factory.makeCDS(TRADE_DATE, STARTDATE, mat2);
    final double basePV = pricer.pv(cdsPort[0], YIELD_CURVE, CREDIT_CURVE, coupon);
    double basePVH = 0;
    for (int i = 0; i < 3; i++) {
      basePVH += notional[i] * pricer.pv(cdsPort[i], YIELD_CURVE, CREDIT_CURVE, coupon);
    }

    for (int k = 0; k < 101; k++) {
      final double bump = -100 + 2.0 * k;
      final ISDACompliantCreditCurve cc = bumpCurve(CREDIT_CURVE, bump * ONE_BP);
      final ISDACompliantCreditCurve ccTilt = tiltCurve(CREDIT_CURVE, bump * ONE_BP);
      final double pv = pricer.pv(cdsPort[0], YIELD_CURVE, cc, coupon);
      double pvH = 0;
      double pvTilt = 0;
      for (int i = 0; i < 3; i++) {
        pvH += notional[i] * pricer.pv(cdsPort[i], YIELD_CURVE, cc, coupon);
        pvTilt += notional[i] * pricer.pv(cdsPort[i], YIELD_CURVE, ccTilt, coupon);
      }
      System.out.println(bump + "\t" + (pvH - basePVH) * 1e7 + "\t" + (pvTilt - basePVH) * 1e7);
    }
  }

  /**
   * The sensitivity of the PV of a set of CDSs to the par spreads of the CDSs used to construct the credit curve.
   *  The last column shows the sensitivity of all the spreads moving in parallel. The (priced) CDSs all have a coupon of 100bps.
   *  All CDSs have a recovery rate of 40\% and the Trade date is 13-Jun-2011.
   */
  @Test(enabled = false)
  public void analCS01test() {

    final int nMat = MATURITIES_6M_STEP.length;
    final double[] coupons = new double[nMat];
    Arrays.fill(coupons, COUPON);
    final CDSAnalytic[] cds = CDS_FACTORY.makeCDS(TRADE_DATE, STARTDATE, MATURITIES_6M_STEP);
    final double[][] analCS01 = ANAL_SPREAD_SENSE_CAL.bucketedCS01FromCreditCurve(cds, coupons, PILLAR_CDSS, YIELD_CURVE, CREDIT_CURVE);

    final int nPillars = PILLAR_DATES.length;
    final String[] columnHeadings = new String[nPillars + 1];
    for (int i = 0; i < nPillars; i++) {
      columnHeadings[i] = TENORS[i].toString();
    }
    columnHeadings[nPillars] = "Total";

    final String[] rowHeadings = new String[nMat];
    final double[][] data = new double[nMat][nPillars + 1];
    for (int i = 0; i < nMat; i++) {
      rowHeadings[i] = MATURITIES_6M_STEP[i].toString(DATE_FORMATT);
      System.arraycopy(analCS01[i], 0, data[i], 0, nPillars);
      double sum = 0;
      for (int j = 0; j < nPillars; j++) {
        sum += analCS01[i][j];
      }
      data[i][nPillars] = sum;
    }

    System.out.println(dumpLatexTable("Tenors", "CDS Maturities", columnHeadings, rowHeadings, data, 4));
  }

  @Test(enabled = false)
  void analVFDCS01Test() {
    final LocalDate mat = LocalDate.of(2019, Month.JUNE, 20);
    final CDSAnalytic cds = CDS_FACTORY.makeCDS(TRADE_DATE, STARTDATE, mat);
    final double[] analCS01 = ANAL_SPREAD_SENSE_CAL.bucketedCS01FromCreditCurve(cds, COUPON, PILLAR_CDSS, YIELD_CURVE, CREDIT_CURVE);
    final double pCS01 = FD_SPREAD_SENSE_CAL.parallelCS01FromParSpreads(cds, COUPON, YIELD_CURVE, PILLAR_CDSS, SPREADS, ONE_BP, BumpType.ADDITIVE);
    final double[] bCS01 = FD_SPREAD_SENSE_CAL.bucketedCS01FromCreditCurve(cds, COUPON, PILLAR_CDSS, YIELD_CURVE, CREDIT_CURVE, ONE_BP);

    final int nPillars = PILLAR_DATES.length;
    final String[] columnHeadings = new String[nPillars + 2];

    final double[][] data = new double[2][nPillars + 2];
    double sumA = 0;
    double sumFD = 0;
    for (int i = 0; i < nPillars; i++) {
      columnHeadings[i] = TENORS[i].toString();
      data[0][i] = analCS01[i];
      sumA += analCS01[i];
      data[1][i] = bCS01[i];
      sumFD += bCS01[i];
    }
    data[0][nPillars] = sumA;
    data[0][nPillars + 1] = sumA;
    data[1][nPillars] = sumFD;
    data[1][nPillars + 1] = pCS01;
    columnHeadings[nPillars] = "Sum";
    columnHeadings[nPillars + 1] = "Parallel";
    final String[] rowHeadings = new String[] {"Analytic", "Forward FD" };
    System.out.println(dumpLatexTable("Tenors", "Calculation Method", columnHeadings, rowHeadings, data, 5));
  }

  @Test(enabled = false)
  public void spreadHedgeTest() {
    final LUDecompositionCommons decomp = new LUDecompositionCommons();
    final int nPillars = PILLAR_CDSS.length;
    final double[] coupons = new double[nPillars];
    Arrays.fill(coupons, COUPON);
    final double[][] temp = ANAL_SPREAD_SENSE_CAL.bucketedCS01FromCreditCurve(PILLAR_CDSS, coupons, PILLAR_CDSS, YIELD_CURVE, CREDIT_CURVE);
    final DoubleMatrix2D jacT = MA.getTranspose(new DoubleMatrix2D(temp));
    //System.out.println(jac);
    final LUDecompositionResult decRes = decomp.evaluate(jacT);

    final int nMat = MATURITIES_6M_STEP.length;

    final double[][] res = new double[nMat][];
    final CDSAnalytic[] cds = CDS_FACTORY.makeCDS(TRADE_DATE, STARTDATE, MATURITIES_6M_STEP);
    for (int i = 0; i < nMat; i++) {
      final double[] vs = ANAL_SPREAD_SENSE_CAL.bucketedCS01FromCreditCurve(cds[i], COUPON, PILLAR_CDSS, YIELD_CURVE, CREDIT_CURVE);
      res[i] = decRes.solve(vs);
    }
    final DoubleMatrix2D hedge = new DoubleMatrix2D(res);
    System.out.println(hedge);
  }

  @Test(enabled = false)
  public void parallelCS01Test() {
    final MarketQuoteConverter puf_con = new MarketQuoteConverter(MARKIT_FIX);
    final ISDACompliantCreditCurveBuilder curveBuilder = new FastCreditCurveBuilder(MARKIT_FIX, ArbitrageHandling.ZeroHazardRate);
    final LocalDate mat = LocalDate.of(2019, Month.JUNE, 20);
    final CDSAnalytic cds = CDS_FACTORY.makeCDS(TRADE_DATE, STARTDATE, mat);

    final QuotedSpread quote = new QuotedSpread(COUPON, 135 * ONE_BP);
    final double cs01FD = FD_SPREAD_SENSE_CAL.parallelCS01(cds, quote, YIELD_CURVE, COUPON);
    final ISDACompliantCreditCurve cc = curveBuilder.calibrateCreditCurve(cds, quote, YIELD_CURVE);
    final PointsUpFront puf = puf_con.convert(cds, quote, YIELD_CURVE);
    System.out.println(puf.getPointsUpFront());
    final ISDACompliantCreditCurve cc2 = curveBuilder.calibrateCreditCurve(cds, COUPON, YIELD_CURVE, puf.getPointsUpFront());

    final double cs01Anal = PRICER_MARKIT_FIX.pvCreditSensitivity(cds, YIELD_CURVE, cc, COUPON, 0) / PRICER_MARKIT_FIX.parSpreadCreditSensitivity(cds, YIELD_CURVE, cc, 0);
    final double cs01Anal2 = PRICER_MARKIT_FIX.pvCreditSensitivity(cds, YIELD_CURVE, cc2, COUPON, 0) / PRICER_MARKIT_FIX.parSpreadCreditSensitivity(cds, YIELD_CURVE, cc2, 0);
    System.out.println(cs01FD + "\t" + cs01Anal + "\t" + cs01Anal2);

    final double eps = 1e-5;
    final PointsUpFront pufUp = puf_con.convert(cds, new QuotedSpread(COUPON, quote.getQuotedSpread() + eps), YIELD_CURVE);
    final PointsUpFront pufDown = puf_con.convert(cds, new QuotedSpread(COUPON, quote.getQuotedSpread() - eps), YIELD_CURVE);
    final double res = (pufUp.getPointsUpFront() - pufDown.getPointsUpFront()) / 2 / eps;
    System.out.println(res);
  }

  @Test(enabled = false)
  public void flatHazardTest() {
    final ISDACompliantCreditCurve flat = new ISDACompliantCreditCurve(1.0, 0.01);
    final int nMat = IMM_DATES.length;
    final CDSAnalytic[] cds = CDS_FACTORY.makeCDS(TRADE_DATE, STARTDATE, IMM_DATES);
    for (int i = 0; i < nMat; i++) {
      final double t = ACT365F.getDayCountFraction(TRADE_DATE, IMM_DATES[i]);
      final double s = PRICER_OG_FIX.parSpread(cds[i], YIELD_CURVE, flat);
      System.out.println(t + "\t" + s * TEN_THOUSAND);
    }
  }

  @Test(enabled = false)
  public void bucketedCS01() {
    final MarketQuoteConverter puf_con = new MarketQuoteConverter(MARKIT_FIX);
    final int nMat = MATURITIES_1Y_STEP.length;
    final int nPillars = PILLAR_CDSS.length;
    final CDSAnalytic[] cds = CDS_FACTORY.makeCDS(TRADE_DATE, STARTDATE, MATURITIES_1Y_STEP);
    for (int j = 0; j < nPillars; j++) {
      System.out.print("\t" + TENORS[j]);
    }
    System.out.print("\n");
    for (int i = 0; i < nMat; i++) {
      final double puf = PRICER_MARKIT_FIX.pv(cds[i], YIELD_CURVE, CREDIT_CURVE, COUPON);
      final double qs = puf_con.pufToQuotedSpread(cds[i], COUPON, YIELD_CURVE, puf);
      final double[] spreads = new double[nPillars];
      Arrays.fill(spreads, qs);
      final double[] bCS01 = FD_SPREAD_SENSE_CAL.bucketedCS01FromParSpreads(cds[i], COUPON, YIELD_CURVE, PILLAR_CDSS, spreads, ONE_BP, BumpType.ADDITIVE);
      final double pCS01 = FD_SPREAD_SENSE_CAL.parallelCS01FromParSpreads(cds[i], COUPON, YIELD_CURVE, PILLAR_CDSS, spreads, ONE_BP, BumpType.ADDITIVE);
      //      final double[] bCS01 = FD_SPREAD_SENSE_CAL.bucketedCS01FromQuotedSpreads(cds[i], COUPON, YIELD_CURVE, PILLAR_CDSS, SPREADS, ONE_BP, BumpType.ADDITIVE);
      //      final double pCS01 = FD_SPREAD_SENSE_CAL.parallelCS01FromQuotedSpread(cds[i], COUPON, YIELD_CURVE, cds[i], qs, ONE_BP, BumpType.ADDITIVE);
      System.out.print(MATURITIES_1Y_STEP[i]);
      double sum = 0;
      for (int j = 0; j < nPillars; j++) {
        sum += bCS01[j];
        System.out.print("\t" + bCS01[j]);
      }
      System.out.print("\t" + sum + "\t" + pCS01);
      System.out.print("\n");
    }
  }

  @Test(enabled = false)
  public void yieldSenseTest() {
    final String[] ycPoints = new String[] {"1M", "3M", "6M", "1Y", "3Y", "5Y", "7Y", "10Y", "11Y", "12Y", "15Y", "20Y", "25Y", "30Y" };
    final String[] instuments = new String[] {"M", "M", "M", "M", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S" };
    final double[] rates = new double[] {0.00445, 0.009488, 0.012337, 0.017762, 0.01935, 0.020838, 0.01652, 0.02018, 0.023033, 0.02525, 0.02696, 0.02825, 0.02931, 0.03017 };
    final ISDACompliantYieldCurve yc = makeYieldCurve(TRADE_DATE, SPOT_DATE, ycPoints, instuments, rates, ACT360, D30360, Period.ofYears(1));

    final int nMat = MATURITIES_1Y_STEP.length;
    final int nYCPoints = ycPoints.length;
    final CDSAnalytic[] cds = CDS_FACTORY.makeCDS(TRADE_DATE, STARTDATE, MATURITIES_1Y_STEP);
    for (int j = 0; j < nYCPoints; j++) {
      System.out.print("\t" + ycPoints[j]);
    }
    System.out.print("\n");
    for (int i = 0; i < nMat; i++) {
      System.out.print(MATURITIES_1Y_STEP[i].toString(DATE_FORMATT));
      for (int j = 0; j < nYCPoints; j++) {
        final double sense = PRICER_MARKIT_FIX.pvYieldSensitivity(cds[i], yc, CREDIT_CURVE, COUPON, j);
        System.out.print("\t" + sense);
      }
      System.out.print("\n");
    }
  }

  @Test(enabled = false)
  public void annuityTest() {
    final LocalDate mat = getNextIMMDate(TRADE_DATE).plus(Period.ofYears(10));
    final CDSAnalytic cds = CDS_FACTORY.makeIMMCDS(TRADE_DATE, Period.ofYears(10));
    final double expiry = ACT365F.getDayCountFraction(TRADE_DATE, mat);
    final double adj = 365. / 360.;
    final AnnuityForSpreadFunction isdaFunc = new AnnuityForSpreadISDAFunction(cds, YIELD_CURVE);
    final AnnuityForSpreadFunction approxFunc = new AnnuityForSpreadContPemiumApproxFunction(cds, YIELD_CURVE);

    for (int i = 0; i < 100; i++) {
      final double s = (1 + 25 * i) * ONE_BP;
      final double a = isdaFunc.evaluate(s);
      final double a2 = approxFunc.evaluate(s);

      final double lambda = adj * s / (1 - RECOVERY_RATE);
      final double a3 = adj * annuity(0.01, lambda, expiry);
      System.out.println(s * TEN_THOUSAND + "\t" + a + "\t" + a2);
    }

  }

  private double annuity(final double r, final double hazardRate, final double expiry) {
    return (1 - Math.exp(-expiry * (r + hazardRate))) / (r + hazardRate);
  }

  private String dumpLatexTable(final String heading1, final String heading2, final String[] columnHeadings, final String[] rowHeadings, final double[][] data, final int dp) {

    ArgumentChecker.noNulls(columnHeadings, "columnHeadings");
    ArgumentChecker.noNulls(rowHeadings, "rowHeadings");
    ArgumentChecker.noNulls(data, "data");
    final int nColumns = columnHeadings.length;
    final int nRows = rowHeadings.length;
    ArgumentChecker.isTrue(nColumns == data[0].length, nColumns + "column headings, but data has " + data[0].length + " columns");
    ArgumentChecker.isTrue(nRows == data.length, nRows + "row headings, but data has " + data.length + " rows");

    final String format = "& %." + dp + "f";
    final StringBuilder out = new StringBuilder();

    out.append("\\begin{tabular}{");
    for (int i = 0; i < nColumns + 1; i++) {
      out.append("c|");
    }
    out.append("}\n");
    out.append("\\cline{2-" + (nColumns + 1) + "}\n");
    out.append("& \\multicolumn{" + nColumns + "}{c|}{" + heading1 + "}\\\\\n");
    out.append("\\hline\n");
    out.append("\\multicolumn{1}{|c|}{" + heading2 + "}");
    for (int i = 0; i < nColumns; i++) {
      out.append("& " + columnHeadings[i]);
    }
    out.append("\\\\\n");
    out.append("\\hline\n");

    for (int i = 0; i < nRows; i++) {
      out.append("\\multicolumn{1}{|c|}{" + rowHeadings[i] + "}");
      for (int j = 0; j < nColumns; j++) {
        out.append(String.format(format, data[i][j]));
      }
      out.append("\\\\\n");
    }
    out.append("\\hline\n");
    out.append("\\end{tabular}\n");

    return out.toString();
  }

  private ISDACompliantCreditCurve bumpCurve(final ISDACompliantCreditCurve curve, final double amount) {
    final double[] r = curve.getKnotZeroRates();
    final int n = r.length;
    for (int i = 0; i < n; i++) {
      r[i] += amount;
    }
    return curve.withRates(r);
  }

  private ISDACompliantCreditCurve tiltCurve(final ISDACompliantCreditCurve curve, final double amount) {
    final double[] r = curve.getKnotZeroRates();
    final int n = r.length;
    for (int i = 0; i < n; i++) {
      r[i] += +(amount / (n / 2)) * (i - n / 2);
    }
    return curve.withRates(r);
  }
}
