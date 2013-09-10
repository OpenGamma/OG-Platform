package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew;

import static com.opengamma.financial.convention.businessday.BusinessDayDateUtils.addWorkDays;
import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Month;

public class CS01FromPUFTest extends ISDABaseTest {

  protected static final double NOTIONAL = 1e6;
  private static final LocalDate TRADE_DATE = LocalDate.of(2013, Month.APRIL, 10); //Today 
  private static final LocalDate EFFECTIVE_DATE = TRADE_DATE.plusDays(1); // AKA stepin date
  private static final LocalDate CASH_SETTLE_DATE = addWorkDays(TRADE_DATE, 3, DEFAULT_CALENDAR); // AKA valuation date
  private static final LocalDate STARTDATE = LocalDate.of(2013, Month.MARCH, 20);//last IMM date before TRADE_DATE;

  private static final double COUPON = 500;
  private static final double[] PUF = new double[] {0.32, 0.69, 1.32, 1.79, 2.36, 3.01, 3.7, 4.39, 5.02, 5.93, 6.85, 7.76, 8.67, 9.6, 10.53, 11.45, 12.33, 13.29, 14.26, 15.2, 16.11, 16.62, 17.12,
    17.62, 18.09, 18.55, 19, 19.44, 19.87, 20.33, 20.79, 21.24, 21.67, 22.04, 22.41, 22.77, 23.12, 23.46, 23.8, 24.14, 24.46 };
  //from Excel sheet
  private static final double[] EXPECTED_PCS01 = new double[] {19.4476717214585, 43.8661130664243, 66.8471984124203, 88.7553472912889, 110.06998773129, 130.241766300415, 149.078259562216,
    166.69903416236, 183.957182839617, 199.138069115451, 212.919225708888, 225.611666159392, 237.356489788121, 247.869659508626, 257.114141612835, 265.199358214979, 272.786968453925,
    278.794138474647, 283.547449071608, 287.3896580744, 290.717945552049, 296.390819613274, 301.375408610055, 305.626841744427, 309.714345009954, 313.29322325846, 316.312476703606, 318.886594326717,
    321.108562383404, 322.498334553417, 323.314906431477, 323.700137078414, 323.96484289246, 324.483246674001, 324.554665413312, 324.325609046883, 323.949329444989, 323.352508975527,
    322.393347729988, 321.094035337033, 319.829180765091 };
  private static final String[] MATURITY_STRINGS = new String[] {"20/06/2013", "20/09/2013", "20/12/2013", "20/03/2014", "20/06/2014", "20/09/2014", "20/12/2014", "20/03/2015", "20/06/2015",
    "20/09/2015", "20/12/2015", "20/03/2016", "20/06/2016", "20/09/2016", "20/12/2016", "20/03/2017", "20/06/2017", "20/09/2017", "20/12/2017", "20/03/2018", "20/06/2018", "20/09/2018", "20/12/2018",
    "20/03/2019", "20/06/2019", "20/09/2019", "20/12/2019", "20/03/2020", "20/06/2020", "20/09/2020", "20/12/2020", "20/03/2021", "20/06/2021", "20/09/2021", "20/12/2021", "20/03/2022", "20/06/2022",
    "20/09/2022", "20/12/2022", "20/03/2023", "20/06/2023" };
  private static final LocalDate[] MATURITIES = parseDateStrings(MATURITY_STRINGS);

  //yield curve
  private static final LocalDate SPOT_DATE = LocalDate.of(2013, Month.APRIL, 12);
  private static final String[] YIELD_CURVE_POINTS = new String[] {"1M", "2M", "3M", "6M", "9M", "1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "12Y", "15Y", "20Y", "25Y", "30Y" };
  private static final String[] YIELD_CURVE_INSTRUMENTS = new String[] {"M", "M", "M", "M", "M", "M", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S" };
  private static final double[] YIELD_CURVE_RATES = new double[] {0.001993, 0.002403, 0.002781, 0.004419, 0.005782, 0.00721, 0.003795, 0.00483, 0.00658, 0.008815, 0.01127, 0.01362, 0.01573, 0.017605,
    0.019215, 0.02195, 0.02468, 0.026975, 0.0281, 0.02874 };
  private static final ISDACompliantYieldCurve YIELD_CURVE = makeYieldCurve(TRADE_DATE, SPOT_DATE, YIELD_CURVE_POINTS, YIELD_CURVE_INSTRUMENTS, YIELD_CURVE_RATES);

  @Test
  public void parellelCS01Test() {
    final double notional = 1e6;
    final double coupon = COUPON * ONE_BP;
    final double scale = notional * ONE_BP;
    final int n = MATURITIES.length;

    for (int i = 0; i < n; i++) {
      final CDSAnalytic cds = new CDSAnalytic(TRADE_DATE, EFFECTIVE_DATE, CASH_SETTLE_DATE, STARTDATE, MATURITIES[i], PROCTECTION_START, PAYMENT_INTERVAL, STUB, PAY_ACC_ON_DEFAULT, RECOVERY_RATE);
      final double puf = PUF[i] * ONE_PC;
      final double cs01 = scale * CS01_CAL.parallelCS01FromPUF(cds, coupon, YIELD_CURVE, puf, ONE_BP);
      // System.out.println(EXPECTED_PCS01[i] + "\t" + cs01);
      assertEquals(MATURITIES[i].toString(), EXPECTED_PCS01[i], cs01, 1e-14 * notional);
    }
  }

}
