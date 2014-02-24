/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.options;

import static com.opengamma.financial.convention.businessday.BusinessDayDateUtils.addWorkDays;

import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDABaseTest;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.TYOCalendar;
import com.opengamma.financial.convention.daycount.DayCount;

/**
 * This holds yield curves use in tests
 */
public class YieldCurveProvider extends ISDABaseTest {

  //USD conventions
  final static String[] USD_PILLARS = new String[] {"1M", "2M", "3M", "6M", "1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "12Y", "15Y", "20Y", "25Y", "30Y" };
  final static String[] USD_INSTR = new String[] {"M", "M", "M", "M", "M", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S" };
  final static DayCount USD_MM_DCC = ACT360;
  final static DayCount USD_SWAP_DCC = D30360;
  final static Period USD_SWAP_INTERVAL = Period.ofMonths(6);

  //EUR conventions 
  final static String[] EUR_PILLARS = new String[] {"1M", "2M", "3M", "6M", "9M", "1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "12Y", "15Y", "20Y", "30Y" };
  final static String[] EUR_INSTR = new String[] {"M", "M", "M", "M", "M", "M", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S" };
  final static DayCount EUR_MM_DCC = ACT360;
  final static DayCount EUR_SWAP_DCC = D30360;
  final static Period EUR_SWAP_INTERVAL = Period.ofYears(1);

  //GBP conventions 
  final static String[] GBP_PILLARS = new String[] {"1M", "2M", "3M", "6M", "1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "12Y", "15Y", "20Y", "25Y", "30Y" };
  final static String[] GBP_INSTR = new String[] {"M", "M", "M", "M", "M", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S" };
  final static DayCount GBP_MM_DCC = ACT365F;
  final static DayCount GBP_SWAP_DCC = ACT365F;
  final static Period GBP_SWAP_INTERVAL = Period.ofMonths(6);

  //JPY Conventions
  final static TYOCalendar TYO_CAL = new TYOCalendar("TYO");
  final static String[] JPY_PILLARS = new String[] {"1M", "2M", "3M", "6M", "1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "12Y", "15Y", "20Y", "30Y" };
  final static String[] JPY_INSTR = new String[] {"M", "M", "M", "M", "M", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S" };
  final static DayCount JPY_MM_DCC = ACT360;
  final static DayCount JPY_SWAP_DCC = ACT365F;
  final static Period JPY_SWAP_INTERVAL = Period.ofMonths(6);

  public static ISDACompliantYieldCurve ISDA_USD_20140213;
  public static ISDACompliantYieldCurve ISDA_EUR_20140206;

  static {

    final double[] rates_USD_20140213 = new double[] {0.001575, 0.002, 0.002365, 0.003333, 0.005617, 0.004425, 0.00783, 0.01191, 0.015775, 0.01915, 0.021935, 0.024205, 0.026055, 0.02764, 0.030115,
      0.032515, 0.03456, 0.035465, 0.03592 };
    ISDA_USD_20140213 = makeUSDCurve(LocalDate.of(2014, 2, 13), rates_USD_20140213);

    final double[] rates_EUR_20140206 = new double[] {0.00223, 0.00252, 0.00287, 0.00386, 0.0047, 0.00548, 0.00436, 0.0058, 0.00783, 0.00997, 0.01207, 0.014, 0.01572, 0.01728, 0.01866, 0.02087,
      0.02307, 0.02456, 0.02498 };
    ISDA_EUR_20140206 = makeEURCurve(LocalDate.of(2014, 2, 6), rates_EUR_20140206);
  }

  public static ISDACompliantYieldCurve makeUSDCurve(final LocalDate tradeDate, final double[] rates) {
    final LocalDate spotDate = addWorkDays(tradeDate.minusDays(1), 3, DEFAULT_CALENDAR);
    return makeYieldCurve(tradeDate, spotDate, USD_PILLARS, USD_INSTR, rates, USD_MM_DCC, USD_SWAP_DCC, USD_SWAP_INTERVAL);
  }

  public static ISDACompliantYieldCurve makeEURCurve(final LocalDate tradeDate, final double[] rates) {
    final LocalDate spotDate = addWorkDays(tradeDate.minusDays(1), 3, DEFAULT_CALENDAR);
    return makeYieldCurve(tradeDate, spotDate, EUR_PILLARS, EUR_INSTR, rates, EUR_MM_DCC, EUR_SWAP_DCC, EUR_SWAP_INTERVAL);
  }

  public static ISDACompliantYieldCurve makeGBPCurve(final LocalDate tradeDate, final double[] rates) {
    final LocalDate spotDate = addWorkDays(tradeDate.minusDays(1), 3, DEFAULT_CALENDAR);
    return makeYieldCurve(tradeDate, spotDate, GBP_PILLARS, GBP_INSTR, rates, GBP_MM_DCC, GBP_SWAP_DCC, GBP_SWAP_INTERVAL);
  }

  public static ISDACompliantYieldCurve makeJPYCurve(final LocalDate tradeDate, final double[] rates) {
    final LocalDate spotDate = addWorkDays(tradeDate.minusDays(1), 3, DEFAULT_CALENDAR);
    return makeYieldCurve(tradeDate, spotDate, JPY_PILLARS, JPY_INSTR, rates, JPY_MM_DCC, JPY_SWAP_DCC, JPY_SWAP_INTERVAL, TYO_CAL);
  }

}
