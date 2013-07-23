/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew;

import static com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.IMMDateLogic.getIMMDateSet;
import static com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.IMMDateLogic.isIMMDate;
import static com.opengamma.financial.convention.businessday.BusinessDayDateUtils.addWorkDays;

import java.util.Arrays;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Month;
import org.threeten.bp.Period;

import com.opengamma.analytics.financial.model.BumpType;

/**
 * 
 */
public class ParVsQuotedSpreadTest extends ISDABaseTest {
  private static final PointsUpFrontConverter PUF_CONVERTER = new PointsUpFrontConverter();
  protected static final double NOTIONAL = 1e6;
  private static final LocalDate TRADE_DATE = LocalDate.of(2013, Month.FEBRUARY, 27); //Today 
  private static final LocalDate EFFECTIVE_DATE = TRADE_DATE.plusDays(1); // AKA stepin date
  private static final LocalDate CASH_SETTLE_DATE = addWorkDays(TRADE_DATE, 3, DEFAULT_CALENDAR); // AKA valuation date
  private static final LocalDate STARTDATE = LocalDate.of(2012, Month.DECEMBER, 20);//last IMM date before TRADE_DATE;

  private static final double COUPON = 100;
  private static final double RECOVERY = 0.25;

  //yield curve
  private static final LocalDate SPOT_DATE = LocalDate.of(2013, Month.MARCH, 1);
  private static final String[] YIELD_CURVE_POINTS = new String[] {"1M", "2M", "3M", "6M", "9M", "1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "12Y", "15Y", "20Y", "25Y", "30Y" };
  private static final String[] YIELD_CURVE_INSTRUMENTS = new String[] {"M", "M", "M", "M", "M", "M", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S" };
  private static final double[] YIELD_CURVE_RATES = new double[] {0.002037, 0.00243, 0.002866, 0.004569, 0.00612, 0.007525, 0.00398, 0.005075, 0.00697, 0.00933, 0.01182, 0.01413, 0.016225, 0.01809,
    0.01973, 0.022455, 0.02519, 0.02753, 0.02867, 0.029325 };
  private static final ISDACompliantYieldCurve YIELD_CURVE = makeYieldCurve(TRADE_DATE, SPOT_DATE, YIELD_CURVE_POINTS, YIELD_CURVE_INSTRUMENTS, YIELD_CURVE_RATES);

  private static final String[] CURVE_DATES_STRINGS = new String[] {"20/03/2013", "20/04/2013", "20/05/2013", "20/06/2013", "20/07/2013", "20/08/2013", "20/09/2013", "20/10/2013", "20/11/2013",
    "20/12/2013", "20/01/2014", "20/02/2014", "20/03/2014", "20/04/2014", "20/05/2014", "20/06/2014", "20/07/2014", "20/08/2014", "20/09/2014", "20/10/2014", "20/11/2014", "20/12/2014", "20/01/2015",
    "20/02/2015", "20/03/2015", "20/04/2015", "20/05/2015", "20/06/2015", "20/07/2015", "20/08/2015", "20/09/2015", "20/10/2015", "20/11/2015", "20/12/2015", "20/01/2016", "20/02/2016", "20/03/2016",
    "20/04/2016", "20/05/2016", "20/06/2016", "20/07/2016", "20/08/2016", "20/09/2016", "20/10/2016", "20/11/2016", "20/12/2016", "20/01/2017", "20/02/2017", "20/03/2017", "20/04/2017", "20/05/2017",
    "20/06/2017", "20/07/2017", "20/08/2017", "20/09/2017", "20/10/2017", "20/11/2017", "20/12/2017", "20/01/2018", "20/02/2018", "20/03/2018", "20/04/2018", "20/05/2018", "20/06/2018", "20/07/2018",
    "20/08/2018", "20/09/2018", "20/10/2018", "20/11/2018", "20/12/2018", "20/01/2019", "20/02/2019", "20/03/2019", "20/04/2019", "20/05/2019", "20/06/2019", "20/07/2019", "20/08/2019", "20/09/2019",
    "20/10/2019", "20/11/2019", "20/12/2019", "20/01/2020", "20/02/2020", "20/03/2020", "20/04/2020", "20/05/2020", "20/06/2020", "20/07/2020", "20/08/2020", "20/09/2020", "20/10/2020", "20/11/2020",
    "20/12/2020", "20/01/2021", "20/02/2021", "20/03/2021", "20/04/2021", "20/05/2021", "20/06/2021", "20/07/2021", "20/08/2021", "20/09/2021", "20/10/2021", "20/11/2021", "20/12/2021", "20/01/2022",
    "20/02/2022", "20/03/2022", "20/04/2022", "20/05/2022", "20/06/2022", "20/07/2022", "20/08/2022", "20/09/2022", "20/10/2022", "20/11/2022", "20/12/2022", "20/01/2023", "20/02/2023", "20/03/2023" };

  private static final LocalDate[] CURVE_DATES = parseDateStrings(CURVE_DATES_STRINGS);

  private static final double[] CURVE_SPREADS = new double[] {60.04, 58.48, 58.68, 60.39, 61.22, 63.02, 64.85, 68.32, 71.47, 74.93, 76.28, 78.24, 81.63, 84.76, 88.94, 94.66, 96.45, 99.81, 104.32,
    105.65, 108.27, 111.37, 112.99, 115.26, 117.63, 120.8, 124.09, 127.81, 130.38, 133.34, 136.82, 138.77, 141.3, 144.53, 146.03, 148.33, 151.36, 154.12, 157.58, 162.05, 164.28, 167.56, 171.71,
    173.55, 176.45, 180.1, 181.9, 184.58, 187.87, 189.95, 192.85, 196.78, 198.42, 201.2, 205.21, 206.35, 208.85, 212.71, 213.4, 215.48, 219.05, 219.11, 220.81, 224.53, 224.07, 225.66, 229.56, 228.61,
    230, 234.04, 232.6, 233.87, 237.97, 236.37, 237.64, 241.91, 240.19, 241.42, 245.56, 243.79, 244.94, 248.97, 247.07, 248.09, 252.11, 249.88, 250.7, 254.82, 252.31, 253.09, 257.4, 254.54, 255.22,
    259.64, 256.52, 257.17, 261.7, 258.42, 259.07, 263.84, 260.35, 260.98, 265.81, 262.14, 262.71, 267.6, 263.78, 264.32, 269.28, 265.33, 265.85, 270.98, 266.89, 267.42, 272.63, 268.42, 268.94,
    274.2, 269.85, 270.28, 275.49 };

  private static final LocalDate[] FULL_IMM_DATES;
  private static final double[] QUOTED_SPREADS;

  private static final Period[] TENORS = new Period[] {Period.ofMonths(6), Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5), Period.ofYears(6),
    Period.ofYears(7), Period.ofYears(8), Period.ofYears(9), Period.ofYears(10) };

  private static final LocalDate[] PILLAR_IMM_DATES = getIMMDateSet(EFFECTIVE_DATE, TENORS);
  private static final LocalDate[] PILLAR_IMM_DATES_ADJ;
  private static final double[] PILLAR_SPREADS;

  static {
    final int n = CURVE_SPREADS.length;
    final double[] tSpreads = new double[n];
    final LocalDate[] tDates = new LocalDate[n];
    int count = 0;
    for (int i = 0; i < n; i++) {
      if (isIMMDate(CURVE_DATES[i])) {
        tSpreads[count] = CURVE_SPREADS[i];
        tDates[count] = CURVE_DATES[i];
        count++;
      }
    }
    FULL_IMM_DATES = new LocalDate[count];
    QUOTED_SPREADS = new double[count];
    System.arraycopy(tDates, 0, FULL_IMM_DATES, 0, count);
    System.arraycopy(tSpreads, 0, QUOTED_SPREADS, 0, count);

    final int nTenors = PILLAR_IMM_DATES.length;
    PILLAR_SPREADS = new double[nTenors];
    PILLAR_IMM_DATES_ADJ = new LocalDate[nTenors];
    for (int i = 0; i < nTenors; i++) {
      final int index = Arrays.binarySearch(CURVE_DATES, PILLAR_IMM_DATES[i]);
      if (index < 0) {
        final int insPoint = -(index + 1);
        PILLAR_IMM_DATES_ADJ[i] = CURVE_DATES[insPoint - 1];
        PILLAR_SPREADS[i] = CURVE_SPREADS[insPoint - 1];
      } else {
        PILLAR_IMM_DATES_ADJ[i] = PILLAR_IMM_DATES[i];
        PILLAR_SPREADS[i] = CURVE_SPREADS[index];
      }
    }
  }

  @Test(enabled = false)
  public void buildCurveFromPillarsTest() {

    final int nPillars = PILLAR_IMM_DATES.length;

    final CDSAnalytic[] pillarCDSs = new CDSAnalytic[nPillars];
    final CDSQuoteConvention[] quotes = new CDSQuoteConvention[nPillars];
    for (int i = 0; i < nPillars; i++) {
      pillarCDSs[i] = new CDSAnalytic(TRADE_DATE, SPOT_DATE, EFFECTIVE_DATE, STARTDATE, PILLAR_IMM_DATES[i], PAY_ACC_ON_DEFAULT, TENOR, STUB, PROCTECTION_START, RECOVERY);
      quotes[i] = new QuotedSpread(COUPON * ONE_BP, PILLAR_SPREADS[i] * ONE_BP);
      System.out.println(PILLAR_IMM_DATES[i]);
    }

    //build curve from pillar dates
    final ISDACompliantCreditCurve creditCurve = CREDIT_CURVE_BUILDER.calibrateCreditCurve(pillarCDSs, quotes, YIELD_CURVE);

    for (int i = 0; i < CURVE_SPREADS.length; i++) {
      final LocalDate mat = CURVE_DATES[i];
      final CDSAnalytic cds = new CDSAnalytic(TRADE_DATE, SPOT_DATE, EFFECTIVE_DATE, STARTDATE, mat, PAY_ACC_ON_DEFAULT, TENOR, STUB, PROCTECTION_START, RECOVERY);

      final double puf = PRICER.pv(cds, YIELD_CURVE, creditCurve, COUPON * ONE_BP);
      final double qSpread = PUF_CONVERTER.pufToQuotedSpread(cds, COUPON * ONE_BP, YIELD_CURVE, puf);
      final double parSpread = PRICER.parSpread(cds, YIELD_CURVE, creditCurve);
      System.out.println(mat + "\t" + parSpread * TEN_THOUSAND + "\t" + qSpread * TEN_THOUSAND);
    }
  }

  @Test(enabled = false)
  public void test() {
    final int nPillars = FULL_IMM_DATES.length;
    final CDSAnalytic[] immCDSs = new CDSAnalytic[nPillars];
    final CDSQuoteConvention[] quotes = new CDSQuoteConvention[nPillars];
    for (int i = 0; i < nPillars; i++) {
      immCDSs[i] = new CDSAnalytic(TRADE_DATE, SPOT_DATE, EFFECTIVE_DATE, STARTDATE, FULL_IMM_DATES[i], PAY_ACC_ON_DEFAULT, TENOR, STUB, PROCTECTION_START, RECOVERY);
      quotes[i] = new QuotedSpread(COUPON * ONE_BP, QUOTED_SPREADS[i] * ONE_BP);
    }

    //build curve from IMM dates
    final ISDACompliantCreditCurve creditCurveIMM = CREDIT_CURVE_BUILDER.calibrateCreditCurve(immCDSs, quotes, YIELD_CURVE);

    final int nNonIMM = CURVE_SPREADS.length - nPillars;
    int index = 0;
    for (int i = 0; i < CURVE_SPREADS.length; i++) {
      final LocalDate mat = CURVE_DATES[i];
      final CDSAnalytic cds = new CDSAnalytic(TRADE_DATE, SPOT_DATE, EFFECTIVE_DATE, STARTDATE, mat, PAY_ACC_ON_DEFAULT, TENOR, STUB, PROCTECTION_START, RECOVERY);

      final double puf = PRICER.pv(cds, YIELD_CURVE, creditCurveIMM, COUPON * ONE_BP);
      final double qSpread = PUF_CONVERTER.pufToQuotedSpread(cds, COUPON * ONE_BP, YIELD_CURVE, puf);
      final double parSpread = PRICER.parSpread(cds, YIELD_CURVE, creditCurveIMM);
      System.out.println(mat + "\t" + parSpread * TEN_THOUSAND + "\t" + qSpread * TEN_THOUSAND);
      index++;
    }

  }

  @Test(enabled = false)
  public void bucketCS01test() {
    final double scale = ONE_BP * NOTIONAL;

    final int nPillars = PILLAR_IMM_DATES.length;
    final CDSAnalytic[] pillarCDSs = new CDSAnalytic[nPillars];
    final CDSQuoteConvention[] quotes = new CDSQuoteConvention[nPillars];
    final double[] qSpreads = new double[nPillars];
    for (int i = 0; i < nPillars; i++) {
      pillarCDSs[i] = new CDSAnalytic(TRADE_DATE, EFFECTIVE_DATE, CASH_SETTLE_DATE, STARTDATE, PILLAR_IMM_DATES[i], PAY_ACC_ON_DEFAULT, TENOR, STUB, PROCTECTION_START, RECOVERY);
      qSpreads[i] = PILLAR_SPREADS[i] * ONE_BP;
      quotes[i] = new QuotedSpread(COUPON * ONE_BP, qSpreads[i]);
      System.out.println(PILLAR_IMM_DATES[i] + "\t" + PILLAR_SPREADS[i]);
    }
    System.out.print("\n");

    //The spreads at the pillar date (which are IMM dates) are quoted spreads - convert to equivalent par spread 
    final double[] parSpreads = PUF_CONVERTER.quotedSpreadToParSpreads(pillarCDSs, COUPON * ONE_BP, YIELD_CURVE, qSpreads);

    final int n = CURVE_DATES.length;
    final CDSAnalytic[] pricingCDSs = new CDSAnalytic[n];
    final double[][] bucketedCS01_fromPSpread = new double[n][];
    final double[][] bucketedCS01_fromQSpread = new double[n][];
    final double[] parellelCS01_fromPSpread = new double[n];
    final double[] parellelCS01_fromQSpread = new double[n];
    for (int i = 0; i < n; i++) {
      if (isIMMDate(CURVE_DATES[i])) {
        pricingCDSs[i] = new CDSAnalytic(TRADE_DATE, EFFECTIVE_DATE, CASH_SETTLE_DATE, STARTDATE, CURVE_DATES[i], PAY_ACC_ON_DEFAULT, TENOR, STUB, PROCTECTION_START, RECOVERY);
      } else {
        pricingCDSs[i] = new CDSAnalytic(TRADE_DATE, EFFECTIVE_DATE, CASH_SETTLE_DATE, STARTDATE, CURVE_DATES[i], PAY_ACC_ON_DEFAULT, Period.ofMonths(6), STUB, PROCTECTION_START, RECOVERY);
      }

      parellelCS01_fromPSpread[i] = CS01_CAL.parallelCS01FromParSpreads(pricingCDSs[i], CURVE_SPREADS[i] * ONE_BP, YIELD_CURVE, pillarCDSs, parSpreads, ONE_BP, BumpType.ADDITIVE);
      parellelCS01_fromQSpread[i] = CS01_CAL.parallelCS01FromQuotedSpread(pricingCDSs[i], COUPON * ONE_BP, YIELD_CURVE, CURVE_SPREADS[i] * ONE_BP, ONE_BP, BumpType.ADDITIVE);
      bucketedCS01_fromPSpread[i] = CS01_CAL.bucketedCS01FromParSpreads(pricingCDSs[i], CURVE_SPREADS[i] * ONE_BP, YIELD_CURVE, pillarCDSs, parSpreads, ONE_BP, BumpType.ADDITIVE);
      bucketedCS01_fromQSpread[i] = CS01_CAL.bucketedCS01FromQuotedSpreads(pricingCDSs[i], COUPON * ONE_BP, YIELD_CURVE, pillarCDSs, qSpreads, ONE_BP, BumpType.ADDITIVE);
    }

    //   final double[][] bucketedCS01 = CS01_CAL.bucketedCS01FromParSpreads(pricingCDSs, COUPON * ONE_BP, YIELD_CURVE, pillarCDSs, qSpreads, ONE_BP, BumpType.ADDITIVE);
    System.out.println("bucked CS01 from par spread");
    for (int i = 0; i < n; i++) {
      System.out.print(CURVE_DATES[i]);
      double sum = 0.0;
      for (int j = 0; j < nPillars; j++) {
        sum += bucketedCS01_fromPSpread[i][j];
        System.out.print("\t" + scale * bucketedCS01_fromPSpread[i][j]);
      }
      System.out.print("\t" + sum * scale + "\t\t" + scale * parellelCS01_fromPSpread[i]);
      System.out.print("\n");
    }
    System.out.print("\n");

    System.out.println("bucked CS01 from quoted spread");
    for (int i = 0; i < n; i++) {
      System.out.print(CURVE_DATES[i]);
      double sum = 0.0;
      for (int j = 0; j < nPillars; j++) {
        sum += bucketedCS01_fromQSpread[i][j];
        System.out.print("\t" + scale * bucketedCS01_fromQSpread[i][j]);
      }
      System.out.print("\t" + sum * scale + "\t\t" + scale * parellelCS01_fromQSpread[i]);
      System.out.print("\n");
    }
    System.out.print("\n");

    System.out.println("bucked CS01 Craig");
    for (int i = 0; i < n; i++) {
      System.out.print(CURVE_DATES[i]);
      final double[] flatSpreadTS = new double[nPillars];
      Arrays.fill(flatSpreadTS, CURVE_SPREADS[i] * ONE_BP);
      final ISDACompliantCreditCurve cc = CREDIT_CURVE_BUILDER.calibrateCreditCurve(pillarCDSs, flatSpreadTS, YIELD_CURVE);
      final double basePrice = PRICER.pv(pricingCDSs[i], YIELD_CURVE, cc, COUPON * ONE_BP);
      for (int j = 0; j < nPillars; j++) {
        final double[] bumpedSpreads = new double[nPillars];
        System.arraycopy(flatSpreadTS, 0, bumpedSpreads, 0, nPillars);
        bumpedSpreads[j] += ONE_BP;
        final ISDACompliantCreditCurve ccBumped = CREDIT_CURVE_BUILDER.calibrateCreditCurve(pillarCDSs, bumpedSpreads, YIELD_CURVE);
        final double bumpedPrice = PRICER.pv(pricingCDSs[i], YIELD_CURVE, ccBumped, COUPON * ONE_BP);
        final double cs01 = (bumpedPrice - basePrice) * NOTIONAL;
        System.out.print("\t" + cs01);
      }
      System.out.print("\n");
    }
  }

}
