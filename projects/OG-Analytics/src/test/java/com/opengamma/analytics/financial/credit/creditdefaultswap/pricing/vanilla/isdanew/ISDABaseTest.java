package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew;

import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.opengamma.analytics.financial.credit.StubType;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDAInstrumentTypes;
import com.opengamma.analytics.financial.schedule.NoHolidayCalendar;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.ArgumentChecker;

public class ISDABaseTest {
  protected static final AnalyticCDSPricer PRICER = new AnalyticCDSPricer();
  protected static final AnalyticCDSPricer PRICER_CORRECT = new AnalyticCDSPricer(true);
  protected static final ISDACompliantCreditCurveBuilder CREDIT_CURVE_BUILDER = new FastCreditCurveBuilder();
  protected static final SpreadSensitivityCalculator CS01_CAL = new SpreadSensitivityCalculator();

  protected static final double ONE_PC = 1e-2;
  protected static final double ONE_BP = 1e-4;
  protected static final double ONE_HUNDRED = 100.;
  protected static final double TEN_THOUSAND = 10000.;

  protected static final Calendar DEFAULT_CALENDAR = new MondayToFridayCalendar("Weekend_Only");
  protected static final Calendar NO_HOLIDAY_CALENDAR = new NoHolidayCalendar();
  protected static final DayCount ACT365 = DayCountFactory.INSTANCE.getDayCount("ACT/365");
  protected static final DayCount ACT360 = DayCountFactory.INSTANCE.getDayCount("ACT/360");
  protected static final DayCount D30360 = DayCountFactory.INSTANCE.getDayCount("30/360");
  protected static final DayCount ACT_ACT_ISDA = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");

  protected static final BusinessDayConvention FOLLOWING = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  protected static final BusinessDayConvention MOD_FOLLOWING = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");

  //standard CDS settings 
  protected static final boolean PAY_ACC_ON_DEFAULT = true;
  protected static final Period PAYMENT_INTERVAL = Period.ofMonths(3);
  protected static final StubType STUB = StubType.FRONTSHORT;
  protected static final boolean PROCTECTION_START = true;
  protected static final double RECOVERY_RATE = 0.4;

  protected static ISDACompliantYieldCurve makeYieldCurve(final LocalDate today, final LocalDate spotDate, final String[] maturities, final String[] type, final double[] rates,
      final DayCount moneyMarketDCC, final DayCount swapDCC, final Period swapInterval) {
    final DayCount curveDCC = ACT365;
    final int nInstruments = maturities.length;
    ArgumentChecker.isTrue(nInstruments == type.length, "type length {} does not match maturities length {}", type.length, nInstruments);
    ArgumentChecker.isTrue(nInstruments == rates.length, "rates length {} does not match  maturities length {}", rates.length, nInstruments);
    final Period[] tenors = new Period[nInstruments];
    final ISDAInstrumentTypes[] types = new ISDAInstrumentTypes[nInstruments];
    for (int i = 0; i < nInstruments; i++) {
      String temp = maturities[i];
      if (temp.endsWith("M")) {
        temp = temp.split("M")[0];
        tenors[i] = Period.ofMonths(Integer.valueOf(temp));
      } else if (temp.endsWith("Y")) {
        temp = temp.split("Y")[0];
        tenors[i] = Period.ofYears(Integer.valueOf(temp));
      } else {
        throw new IllegalArgumentException("cannot parse " + temp);
      }

      temp = type[i];
      if (temp.equalsIgnoreCase("M")) {
        types[i] = ISDAInstrumentTypes.MoneyMarket;
      } else if (temp.equalsIgnoreCase("S")) {
        types[i] = ISDAInstrumentTypes.Swap;
      } else {
        throw new IllegalArgumentException("cannot parse " + temp);
      }
    }
    return ISDACompliantYieldCurveBuild.build(today, spotDate, types, tenors, rates, moneyMarketDCC, swapDCC, swapInterval, curveDCC, MOD_FOLLOWING);

  }

  protected static ISDACompliantYieldCurve makeYieldCurve(final LocalDate today, final LocalDate spotDate, final String[] maturities, final String[] type, final double[] rates) {
    final DayCount moneyMarketDCC = ACT360;
    final DayCount swapDCC = D30360;
    final Period swapInterval = Period.ofMonths(6);
    return makeYieldCurve(today, spotDate, maturities, type, rates, moneyMarketDCC, swapDCC, swapInterval);
  }

  protected static LocalDate parseDateString(final String ddmmyyyy) {
    ArgumentChecker.notNull(ddmmyyyy, "ddmmyyyy");
    final String[] temp = ddmmyyyy.split("/");
    ArgumentChecker.isTrue(temp.length == 3, "date formatt wrong: length");
    final int day = Integer.valueOf(temp[0]);
    final int month = Integer.valueOf(temp[1]);
    final int year = Integer.valueOf(temp[2]);
    ArgumentChecker.isTrue(year > 1900 && year < 2500, "date formatt wrong: year out of range - {}", year);
    return LocalDate.of(year, month, day);
  }

  protected static LocalDate[] parseDateStrings(final String[] ddmmyyyy) {
    ArgumentChecker.notNull(ddmmyyyy, "ddmmyyyy");
    final int n = ddmmyyyy.length;
    final LocalDate[] res = new LocalDate[n];
    for (int i = 0; i < n; i++) {
      res[i] = parseDateString(ddmmyyyy[i]);
    }
    return res;
  }
}
