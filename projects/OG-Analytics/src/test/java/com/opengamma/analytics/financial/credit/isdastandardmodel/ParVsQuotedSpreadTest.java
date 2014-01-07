/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel;

import static com.opengamma.analytics.financial.credit.isdastandardmodel.IMMDateLogic.getIMMDateSet;
import static com.opengamma.analytics.financial.credit.isdastandardmodel.IMMDateLogic.getNextIMMDate;
import static com.opengamma.analytics.financial.credit.isdastandardmodel.IMMDateLogic.isIMMDate;
import static com.opengamma.financial.convention.businessday.BusinessDayDateUtils.addWorkDays;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Month;
import org.threeten.bp.Period;

import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalytic;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSQuoteConvention;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.MarketQuoteConverter;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ParSpread;
import com.opengamma.analytics.financial.credit.isdastandardmodel.QuotedSpread;
import com.opengamma.analytics.financial.model.BumpType;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class ParVsQuotedSpreadTest extends ISDABaseTest {
  private static final MarketQuoteConverter PUF_CONVERTER = new MarketQuoteConverter();
  protected static final double NOTIONAL = 1e6;
  private static final LocalDate TRADE_DATE = LocalDate.of(2013, Month.FEBRUARY, 27); //Today 
  private static final LocalDate EFFECTIVE_DATE = TRADE_DATE.plusDays(1); // AKA stepin date
  private static final LocalDate CASH_SETTLE_DATE = addWorkDays(TRADE_DATE, 3, DEFAULT_CALENDAR); // AKA valuation date
  private static final LocalDate STARTDATE = LocalDate.of(2012, Month.DECEMBER, 20);//last IMM date before TRADE_DATE;

  private static final double COUPON = 100;
  private static final double RECOVERY = 0.25;
  private static final Period NON_IMM_TENOR = Period.ofMonths(6);

  //yield curve
  private static final LocalDate SPOT_DATE = LocalDate.of(2013, Month.MARCH, 1);
  private static final String[] YIELD_CURVE_POINTS = new String[] {"1M", "2M", "3M", "6M", "9M", "1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "12Y", "15Y", "20Y", "25Y", "30Y" };
  private static final String[] YIELD_CURVE_INSTRUMENTS = new String[] {"M", "M", "M", "M", "M", "M", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S" };
  private static final double[] YIELD_CURVE_RATES = new double[] {0.002037, 0.00243, 0.002866, 0.004569, 0.00612, 0.007525, 0.00398, 0.005075, 0.00697, 0.00933, 0.01182, 0.01413, 0.016225, 0.01809,
    0.01973, 0.022455, 0.02519, 0.02753, 0.02867, 0.029325 };
  private static final ISDACompliantYieldCurve YIELD_CURVE = makeYieldCurve(TRADE_DATE, SPOT_DATE, YIELD_CURVE_POINTS, YIELD_CURVE_INSTRUMENTS, YIELD_CURVE_RATES);

  private static final String[] MATURITY_DATE_STRINGS = new String[] {"20/03/2013", "20/04/2013", "20/05/2013", "20/06/2013", "20/07/2013", "20/08/2013", "20/09/2013", "20/10/2013", "20/11/2013",
    "20/12/2013", "20/01/2014", "20/02/2014", "20/03/2014", "20/04/2014", "20/05/2014", "20/06/2014", "20/07/2014", "20/08/2014", "20/09/2014", "20/10/2014", "20/11/2014", "20/12/2014", "20/01/2015",
    "20/02/2015", "20/03/2015", "20/04/2015", "20/05/2015", "20/06/2015", "20/07/2015", "20/08/2015", "20/09/2015", "20/10/2015", "20/11/2015", "20/12/2015", "20/01/2016", "20/02/2016", "20/03/2016",
    "20/04/2016", "20/05/2016", "20/06/2016", "20/07/2016", "20/08/2016", "20/09/2016", "20/10/2016", "20/11/2016", "20/12/2016", "20/01/2017", "20/02/2017", "20/03/2017", "20/04/2017", "20/05/2017",
    "20/06/2017", "20/07/2017", "20/08/2017", "20/09/2017", "20/10/2017", "20/11/2017", "20/12/2017", "20/01/2018", "20/02/2018", "20/03/2018", "20/04/2018", "20/05/2018", "20/06/2018", "20/07/2018",
    "20/08/2018", "20/09/2018", "20/10/2018", "20/11/2018", "20/12/2018", "20/01/2019", "20/02/2019", "20/03/2019", "20/04/2019", "20/05/2019", "20/06/2019", "20/07/2019", "20/08/2019", "20/09/2019",
    "20/10/2019", "20/11/2019", "20/12/2019", "20/01/2020", "20/02/2020", "20/03/2020", "20/04/2020", "20/05/2020", "20/06/2020", "20/07/2020", "20/08/2020", "20/09/2020", "20/10/2020", "20/11/2020",
    "20/12/2020", "20/01/2021", "20/02/2021", "20/03/2021", "20/04/2021", "20/05/2021", "20/06/2021", "20/07/2021", "20/08/2021", "20/09/2021", "20/10/2021", "20/11/2021", "20/12/2021", "20/01/2022",
    "20/02/2022", "20/03/2022", "20/04/2022", "20/05/2022", "20/06/2022", "20/07/2022", "20/08/2022", "20/09/2022", "20/10/2022", "20/11/2022", "20/12/2022", "20/01/2023", "20/02/2023", "20/03/2023" };

  private static final LocalDate[] MATURITY_DATES = parseDateStrings(MATURITY_DATE_STRINGS);
  private static final LocalDate[] IMM_DATES;
  private static final LocalDate[] NON_IMM_DATES;

  //these are a mix of quoted spreads (for IMM dates) and par spreads (for non IMM dates)
  private static final double[] SPREADS_BPS_ALL = new double[] {60.04, 58.48, 58.68, 60.39, 61.22, 63.02, 64.85, 68.32, 71.47, 74.93, 76.28, 78.24, 81.63, 84.76, 88.94, 94.66, 96.45, 99.81, 104.32,
    105.65, 108.27, 111.37, 112.99, 115.26, 117.63, 120.8, 124.09, 127.81, 130.38, 133.34, 136.82, 138.77, 141.3, 144.53, 146.03, 148.33, 151.36, 154.12, 157.58, 162.05, 164.28, 167.56, 171.71,
    173.55, 176.45, 180.1, 181.9, 184.58, 187.87, 189.95, 192.85, 196.78, 198.42, 201.2, 205.21, 206.35, 208.85, 212.71, 213.4, 215.48, 219.05, 219.11, 220.81, 224.53, 224.07, 225.66, 229.56, 228.61,
    230, 234.04, 232.6, 233.87, 237.97, 236.37, 237.64, 241.91, 240.19, 241.42, 245.56, 243.79, 244.94, 248.97, 247.07, 248.09, 252.11, 249.88, 250.7, 254.82, 252.31, 253.09, 257.4, 254.54, 255.22,
    259.64, 256.52, 257.17, 261.7, 258.42, 259.07, 263.84, 260.35, 260.98, 265.81, 262.14, 262.71, 267.6, 263.78, 264.32, 269.28, 265.33, 265.85, 270.98, 266.89, 267.42, 272.63, 268.42, 268.94,
    274.2, 269.85, 270.28, 275.49 };

  //treat spreads on IMM dates as quoted (aka flat) spreads, and those on non-IMM dates as par spreads;
  private static final List<CDSQuoteConvention> IMM_QUOTES;
  private static final List<CDSQuoteConvention> NON_IMM_QUOTES;

  //the buckets
  private static final Period[] TENORS = new Period[] {Period.ofMonths(6), Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5), Period.ofYears(6),
    Period.ofYears(7), Period.ofYears(8), Period.ofYears(9), Period.ofYears(10) };

  //the bucket (or pillar) dates 
  private static final LocalDate NEXT_IMM = getNextIMMDate(TRADE_DATE);
  private static final LocalDate[] PILLAR_DATES = getIMMDateSet(NEXT_IMM, TENORS);

  //the spreads at the buckets (pillars) - it is not clear whether these are par or quoted spreads 
  private static final double[] PILLAR_SPREADS;

  static {
    final double coupon = COUPON * ONE_BP;
    final int n = SPREADS_BPS_ALL.length;
    final List<LocalDate> immDates = new ArrayList<>(n / 4 + 2);
    final List<LocalDate> nonImmDates = new ArrayList<>(3 * n / 4 + 2);

    IMM_QUOTES = new ArrayList<>(n / 4 + 2);
    NON_IMM_QUOTES = new ArrayList<>(3 * n / 4 + 2);
    for (int i = 0; i < n; i++) {
      if (isIMMDate(MATURITY_DATES[i])) {
        IMM_QUOTES.add(new QuotedSpread(coupon, SPREADS_BPS_ALL[i] * ONE_BP));
        immDates.add(MATURITY_DATES[i]);
      } else {
        NON_IMM_QUOTES.add(new ParSpread(SPREADS_BPS_ALL[i] * ONE_BP));
        nonImmDates.add(MATURITY_DATES[i]);
      }
    }
    IMM_DATES = new LocalDate[immDates.size()];
    NON_IMM_DATES = new LocalDate[nonImmDates.size()];
    immDates.toArray(IMM_DATES);
    nonImmDates.toArray(NON_IMM_DATES);

    final int nPillars = PILLAR_DATES.length;
    PILLAR_SPREADS = new double[nPillars]; //do not distinguish between par and quoted spreads 
    for (int i = 0; i < nPillars; i++) {
      final int index = Arrays.binarySearch(MATURITY_DATES, PILLAR_DATES[i]);
      if (index < 0) {
        final int insPoint = -(index + 1);
        PILLAR_SPREADS[i] = SPREADS_BPS_ALL[insPoint - 1];
      } else {
        PILLAR_SPREADS[i] = SPREADS_BPS_ALL[index];
      }
    }
  }

  @Test(enabled = false)
  public void buildCurveFromPillarsTest() {

    final int nPillars = PILLAR_DATES.length;

    final CDSAnalytic[] pillarCDSs = new CDSAnalytic[nPillars];
    final CDSQuoteConvention[] quotes = new CDSQuoteConvention[nPillars];
    final double[] spreads = new double[nPillars];
    for (int i = 0; i < nPillars; i++) {
      pillarCDSs[i] = new CDSAnalytic(TRADE_DATE, SPOT_DATE, EFFECTIVE_DATE, STARTDATE, PILLAR_DATES[i], PAY_ACC_ON_DEFAULT, PAYMENT_INTERVAL, STUB, PROCTECTION_START, RECOVERY);
      spreads[i] = PILLAR_SPREADS[i] * ONE_BP;
      quotes[i] = new QuotedSpread(COUPON * ONE_BP, spreads[i]);
      System.out.println(TENORS[i] + "\t" + PILLAR_DATES[i]);
    }

    //build curve from pillar dates
    final ISDACompliantCreditCurve creditCurve = CREDIT_CURVE_BUILDER.calibrateCreditCurve(pillarCDSs, quotes, YIELD_CURVE);

    for (int i = 0; i < SPREADS_BPS_ALL.length; i++) {
      final LocalDate mat = MATURITY_DATES[i];
      if (isIMMDate(mat)) {
        final Period couponInterval = PAYMENT_INTERVAL;
        final CDSAnalytic cds = new CDSAnalytic(TRADE_DATE, SPOT_DATE, EFFECTIVE_DATE, STARTDATE, mat, PAY_ACC_ON_DEFAULT, couponInterval, STUB, PROCTECTION_START, RECOVERY);
        final double puf = PRICER.pv(cds, YIELD_CURVE, creditCurve, COUPON * ONE_BP);
        final double qSpread = PUF_CONVERTER.pufToQuotedSpread(cds, COUPON * ONE_BP, YIELD_CURVE, puf);
        System.out.println(mat + "\tIMM\t" + qSpread * TEN_THOUSAND);
      } else {
        final Period couponInterval = NON_IMM_TENOR;
        final CDSAnalytic cds = new CDSAnalytic(TRADE_DATE, SPOT_DATE, EFFECTIVE_DATE, STARTDATE, mat, PAY_ACC_ON_DEFAULT, couponInterval, STUB, PROCTECTION_START, RECOVERY);
        final double parSpread = PRICER.parSpread(cds, YIELD_CURVE, creditCurve);
        System.out.println(mat + "\tnon-IMM\t" + parSpread * TEN_THOUSAND);
      }
    }
  }

  @Test(enabled = false)
  public void bucketCS01test() {
    final double scale = ONE_BP * NOTIONAL;
    final double imm_coupon = COUPON * ONE_BP;

    final int nPillars = PILLAR_DATES.length;
    final CDSAnalytic[] pillarCDSs_IMM = new CDSAnalytic[nPillars];
    final CDSAnalytic[] pillarCDSs_nonIMM = new CDSAnalytic[nPillars];
    final CDSQuoteConvention[] pillar_quotes = new CDSQuoteConvention[nPillars];
    final double[] pillar_qSpreads = new double[nPillars];
    for (int i = 0; i < nPillars; i++) {
      pillarCDSs_IMM[i] = new CDSAnalytic(TRADE_DATE, EFFECTIVE_DATE, CASH_SETTLE_DATE, STARTDATE, PILLAR_DATES[i], PAY_ACC_ON_DEFAULT, PAYMENT_INTERVAL, STUB, PROCTECTION_START, RECOVERY);
      pillarCDSs_nonIMM[i] = new CDSAnalytic(TRADE_DATE, EFFECTIVE_DATE, CASH_SETTLE_DATE, STARTDATE, PILLAR_DATES[i], PAY_ACC_ON_DEFAULT, NON_IMM_TENOR, STUB, PROCTECTION_START, RECOVERY);
      pillar_qSpreads[i] = PILLAR_SPREADS[i] * ONE_BP;
      if (isIMMDate(PILLAR_DATES[i])) {
        pillar_quotes[i] = new QuotedSpread(imm_coupon, pillar_qSpreads[i]);
      } else {
        pillar_quotes[i] = new ParSpread(pillar_qSpreads[i]);
      }
      System.out.println(PILLAR_DATES[i] + "\t" + PILLAR_SPREADS[i]);
    }
    System.out.print("\n");

    //The spreads at the pillar date (which are IMM dates) are quoted spreads - convert to equivalent par spread 
    final double[] pillar_parSpreads = PUF_CONVERTER.quotedSpreadToParSpreads(pillarCDSs_IMM, imm_coupon, YIELD_CURVE, pillar_qSpreads);

    final int nIMMDates = IMM_DATES.length;
    final int nNonIMMDates = NON_IMM_DATES.length;

    final double[] parellelCS01_IMM = new double[nIMMDates];
    final double[][] bucketedCS01_IMM_qSpread = new double[nIMMDates][];
    final double[][] bucketedCS01_IMM_pSpread = new double[nIMMDates][];
    final double[][] bucketedCS01_IMM_flatSpread = new double[nIMMDates][];

    for (int i = 0; i < nIMMDates; i++) {
      final CDSAnalytic pricingCDS = new CDSAnalytic(TRADE_DATE, EFFECTIVE_DATE, CASH_SETTLE_DATE, STARTDATE, IMM_DATES[i], PAY_ACC_ON_DEFAULT, PAYMENT_INTERVAL, STUB, PROCTECTION_START, RECOVERY);
      final double[] flatSpreadTS = new double[nPillars];
      Arrays.fill(flatSpreadTS, ((QuotedSpread) IMM_QUOTES.get(i)).getQuotedSpread());

      parellelCS01_IMM[i] = CS01_CAL.parallelCS01(pricingCDS, IMM_QUOTES.get(i), YIELD_CURVE, ONE_BP);
      bucketedCS01_IMM_qSpread[i] = CS01_CAL.bucketedCS01FromPillarQuotes(pricingCDS, imm_coupon, YIELD_CURVE, pillarCDSs_IMM, pillar_quotes, ONE_BP);
      bucketedCS01_IMM_pSpread[i] = CS01_CAL.bucketedCS01FromParSpreads(pricingCDS, imm_coupon, YIELD_CURVE, pillarCDSs_IMM, pillar_parSpreads, ONE_BP, BumpType.ADDITIVE);
      bucketedCS01_IMM_flatSpread[i] = CS01_CAL.bucketedCS01FromParSpreads(pricingCDS, imm_coupon, YIELD_CURVE, pillarCDSs_IMM, flatSpreadTS, ONE_BP, BumpType.ADDITIVE);
    }

    final double[] parellelCS01_nonIMM_pSpread = new double[nNonIMMDates];
    final double[] parellelCS01_nonIMM_qSpread = new double[nNonIMMDates];
    final double[] parellelCS01_nonIMM_qSpread_SA = new double[nNonIMMDates];
    final double[] parellelCS01_nonIMM_qSpread_bumped = new double[nNonIMMDates];
    final double[][] bucketedCS01_nonIMM_pSpread = new double[nNonIMMDates][];
    final double[][] bucketedCS01_nonIMM_qSpread = new double[nNonIMMDates][];
    final double[][] bucketedCS01_nonIMM_qSpread_SA = new double[nNonIMMDates][];
    final double[][] bucketedCS01_nonIMM_qSpread_bumped = new double[nNonIMMDates][];

    for (int i = 0; i < nNonIMMDates; i++) {
      final CDSAnalytic pricingCDS = new CDSAnalytic(TRADE_DATE, EFFECTIVE_DATE, CASH_SETTLE_DATE, STARTDATE, NON_IMM_DATES[i], PAY_ACC_ON_DEFAULT, NON_IMM_TENOR, STUB, PROCTECTION_START, RECOVERY);
      final double coupon = NON_IMM_QUOTES.get(i).getCoupon();

      //convert pillar spreads to par spreads first 
      parellelCS01_nonIMM_pSpread[i] = CS01_CAL.parallelCS01FromParSpreads(pricingCDS, coupon, YIELD_CURVE, pillarCDSs_IMM, pillar_parSpreads, ONE_BP, BumpType.ADDITIVE);
      bucketedCS01_nonIMM_pSpread[i] = CS01_CAL.bucketedCS01FromParSpreads(pricingCDS, coupon, YIELD_CURVE, pillarCDSs_IMM, pillar_parSpreads, ONE_BP, BumpType.ADDITIVE);

      //treat the pillar quotes (which are quoted spreads) as par spreads 
      parellelCS01_nonIMM_qSpread[i] = CS01_CAL.parallelCS01FromParSpreads(pricingCDS, coupon, YIELD_CURVE, pillarCDSs_IMM, pillar_qSpreads, ONE_BP, BumpType.ADDITIVE);
      bucketedCS01_nonIMM_qSpread[i] = CS01_CAL.bucketedCS01FromParSpreads(pricingCDS, coupon, YIELD_CURVE, pillarCDSs_IMM, pillar_qSpreads, ONE_BP, BumpType.ADDITIVE);

      //treat the pillar quotes (which are quoted spreads) as par spreads  with SA coupon interval
      parellelCS01_nonIMM_qSpread_SA[i] = CS01_CAL.parallelCS01FromParSpreads(pricingCDS, coupon, YIELD_CURVE, pillarCDSs_nonIMM, pillar_qSpreads, ONE_BP, BumpType.ADDITIVE);
      bucketedCS01_nonIMM_qSpread_SA[i] = CS01_CAL.bucketedCS01FromParSpreads(pricingCDS, coupon, YIELD_CURVE, pillarCDSs_nonIMM, pillar_qSpreads, ONE_BP, BumpType.ADDITIVE);

      parellelCS01_nonIMM_qSpread_bumped[i] = CS01_CAL.parallelCS01FromPillarQuotes(pricingCDS, coupon, YIELD_CURVE, pillarCDSs_IMM, pillar_quotes, ONE_BP);
      bucketedCS01_nonIMM_qSpread_bumped[i] = CS01_CAL.bucketedCS01FromPillarQuotes(pricingCDS, coupon, YIELD_CURVE, pillarCDSs_IMM, pillar_quotes, ONE_BP);

      //debug

    }

    output("CS01 IMM quoted spreads", IMM_DATES, TENORS, bucketedCS01_IMM_qSpread, parellelCS01_IMM, scale);
    output("CS01 IMM par spreads", IMM_DATES, TENORS, bucketedCS01_IMM_pSpread, parellelCS01_IMM, scale);
    output("CS01 IMM flat spread term structure", IMM_DATES, TENORS, bucketedCS01_IMM_flatSpread, parellelCS01_IMM, scale);

    output("CS01 non-IMM par spreads", NON_IMM_DATES, TENORS, bucketedCS01_nonIMM_pSpread, parellelCS01_nonIMM_pSpread, scale);
    output("CS01 non-IMM quoted spreads", NON_IMM_DATES, TENORS, bucketedCS01_nonIMM_qSpread, parellelCS01_nonIMM_qSpread, scale);
    output("CS01 non-IMM quoted spreads (SA)", NON_IMM_DATES, TENORS, bucketedCS01_nonIMM_qSpread_SA, parellelCS01_nonIMM_qSpread_SA, scale);
    output("CS01 non-IMM quoted spreads bumped", NON_IMM_DATES, TENORS, bucketedCS01_nonIMM_qSpread_bumped, parellelCS01_nonIMM_qSpread_bumped, scale);
  }

  private void output(final String name, final LocalDate[] maturities, final Period[] pillars, final double[][] bCS01, final double[] pCS01, final double scale) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.noNulls(maturities, "maturities");
    ArgumentChecker.noNulls(pillars, "pillars");
    ArgumentChecker.noNulls(bCS01, "bCS01");
    ArgumentChecker.notEmpty(pCS01, "pCS01");
    final int rows = maturities.length;
    ArgumentChecker.isTrue(rows == bCS01.length, "bCS01 length wrong");
    ArgumentChecker.isTrue(rows == pCS01.length, "pCS01 length wrong");
    final int columns = pillars.length;
    ArgumentChecker.isTrue(columns == bCS01[0].length, "bCS01 width wrong");

    System.out.println(name);
    System.out.print("Maturity");
    for (int j = 0; j < columns; j++) {
      System.out.print("\t" + pillars[j]);
    }
    System.out.print("\t\tSum\tParallel\n");

    for (int i = 0; i < rows; i++) {
      System.out.print(maturities[i]);
      double sum = 0.0;
      for (int j = 0; j < columns; j++) {
        final double temp = bCS01[i][j] * scale;
        sum += temp;
        System.out.print("\t" + temp);
      }
      System.out.print("\t\t" + sum + "\t" + scale * pCS01[i] + "\n");
    }
    System.out.print("\n");
  }

}
