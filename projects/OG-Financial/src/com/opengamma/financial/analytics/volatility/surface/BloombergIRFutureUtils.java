/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import javax.time.calendar.DateAdjuster;
import javax.time.calendar.LocalDate;
import javax.time.calendar.MonthOfYear;
import javax.time.calendar.Period;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.opengamma.financial.analytics.ircurve.NextExpiryAdjuster;
import com.opengamma.util.OpenGammaClock;

/**
 * Utility methods for building Bloomberg Tickers on IR Futures and IR Future Options (Refer to Bloomberg page: WIR)
 */
public class BloombergIRFutureUtils {
  
  
  /**
   *  Bloomberg's two character prefixes for Interest Rate Futures
   *  Be careful: Some of Bloomberg's code consist of 1 digit only with a trailing space! eg "L " for Sterling 
   *  Do NOT use name() to get string values of this enum as in this case, the trailing string will not be present
   *  Instead, use toString, which can be, and has, been overridden.    
   */
  public enum IRFuturePrefix {
    /** USD, Eurodollar, 3-month, CME */
    ED,
    /** EUR, Euro Euribor,  3-month, LIF */
    ER,
    /** AUD, 90 day Bankers' Acceptance, 3-month, SFE */
    IR,
    /** GBP, Short Sterling, 3-month, LIF */
    L {
      @Override
      public String toString() {
        return "L ";
      }
    }
  }
  private static BiMap<MonthOfYear, Character> s_monthCode;
  private static final DateAdjuster NEXT_EXPIRY_ADJUSTER = new NextExpiryAdjuster();
  static {
    s_monthCode = HashBiMap.create();
    s_monthCode.put(MonthOfYear.JANUARY, 'F');
    s_monthCode.put(MonthOfYear.FEBRUARY, 'G');
    s_monthCode.put(MonthOfYear.MARCH, 'H');
    s_monthCode.put(MonthOfYear.APRIL, 'J');
    s_monthCode.put(MonthOfYear.MAY, 'K');
    s_monthCode.put(MonthOfYear.JUNE, 'M');
    s_monthCode.put(MonthOfYear.JULY, 'N');
    s_monthCode.put(MonthOfYear.AUGUST, 'Q');
    s_monthCode.put(MonthOfYear.SEPTEMBER, 'U');
    s_monthCode.put(MonthOfYear.OCTOBER, 'V');
    s_monthCode.put(MonthOfYear.NOVEMBER, 'X');
    s_monthCode.put(MonthOfYear.DECEMBER, 'Z');
  }
  
  /**
   * Produces the month-year string required to build ExternalId for Bloomberg tickers of IRFutureSecurity and IRFutureOptionSecurity
   * @param nthFuture The n'th future following valuation date
   * @param valuationDate valuation date
   * @param twoDigitYearDate Expired futures will, before this date, be referenced by a 2-digit year (eg 12 for 2012) as opposed to trading futures (eg 2 for 2012) 
   * @return e.g. M10 (for June 2010) or Z3 (for December 2013), both valid as of valuationDate 2012/04/10
   */
  public static final String getQuarterlyExpiryMonthYearCode(final int nthFuture, final LocalDate valuationDate, final LocalDate twoDigitYearDate) {
    LocalDate futureExpiry = valuationDate.with(NEXT_EXPIRY_ADJUSTER);
    final StringBuilder futureCode = new StringBuilder();
    for (int i = 1; i < nthFuture; i++) {
      futureExpiry = (futureExpiry.plusDays(7)).with(NEXT_EXPIRY_ADJUSTER);
    }
    futureCode.append(s_monthCode.get(futureExpiry.getMonthOfYear()));
    
    if (futureExpiry.isBefore(twoDigitYearDate)) {

      final int yearsNum = futureExpiry.getYear() % 100;
      if (yearsNum < 10) {
        futureCode.append("0"); // so we get '09' rather than '9'  
      }
      futureCode.append(Integer.toString(yearsNum));
    } else {
      futureCode.append(Integer.toString(futureExpiry.getYear() % 10));
    }
    return futureCode.toString();
  }
  /**
   * Produces the month-year string required to build ExternalId for Bloomberg ticker of IRFutureSecurity
   * @param futurePrefix 2 character String of Future (eg ED, ER, IR)
   * @param nthFuture The n'th future following valuation date
   * @param curveDate Date curve is valid; valuation date
   * @return e.g. M10 (for June 2010) or Z3 (for December 2013), both valid as of valuationDate 2012/04/10
   */
  public static final String getQuarterlyExpiryCodeForFutures(final String futurePrefix, final int nthFuture, final LocalDate curveDate) {
    //Year convention for historical data is specific to the futurePrefix 
    LocalDate twoDigitYearSwitch; 
    final LocalDate today = LocalDate.now(OpenGammaClock.getInstance());
    switch(IRFuturePrefix.valueOf(futurePrefix.trim())) {
      case ED:
        // Quarterly Eurodollar expiries trade for a decade. They begin just after the previous one expires
        // To accomodate 1-digit years for active contracts, the expired one changes immediately
        twoDigitYearSwitch = today.minus(Period.ofDays(2)); 
        break;
      default:
        twoDigitYearSwitch = today.minus(Period.ofMonths(11));
        break;
    } 
    return getQuarterlyExpiryMonthYearCode(nthFuture, curveDate, twoDigitYearSwitch);
  }
  
  /**
   * Produces the month-year string required to build ExternalId for Bloomberg ticker of IRFutureSecurity
   * NOTE: Eurodollar FutureOptions do not share the same naming convention for past expiries as their underlying futures!
   * @param futurePrefix 2 character String of Future (eg ED, ER, IR)
   * @param nthFuture The n'th future following valuation date
   * @param curveDate Date curve is valid; valuation date
   * @return e.g. M10 (for June 2010) or Z3 (for December 2013), both valid as of valuationDate 2012/04/10
   */
  public static final String getQuarterlyExpiryCodeForFutureOptions(final String futurePrefix, final int nthFuture, final LocalDate curveDate) {
    final LocalDate today = LocalDate.now(OpenGammaClock.getInstance());
    LocalDate twoDigitYearSwitch = today.minus(Period.ofMonths(11));
    return getQuarterlyExpiryMonthYearCode(nthFuture, curveDate, twoDigitYearSwitch);
  }
  
}
