/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import javax.time.calendar.DateAdjuster;
import javax.time.calendar.LocalDate;
import javax.time.calendar.MonthOfYear;
import javax.time.calendar.Period;

import org.apache.commons.lang.Validate;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.financial.analytics.ircurve.NextExpiryAdjuster;
import com.opengamma.id.IdentificationScheme;
import com.opengamma.id.Identifier;

/**
 * 
 */
public class BloombergInterestRateFutureOptionVolatilitySurfaceInstrumentProvider implements SurfaceInstrumentProvider<Integer, Double> {
  private static BiMap<MonthOfYear, Character> s_monthCode;
  private static final DateAdjuster NEXT_EXPIRY_ADJUSTER = new NextExpiryAdjuster();
  private static final IdentificationScheme SCHEME = SecurityUtils.BLOOMBERG_TICKER;

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

  private final String _futureOptionPrefix;
  private final String _postfix;

  public BloombergInterestRateFutureOptionVolatilitySurfaceInstrumentProvider(final String futureOptionPrefix, final String postfix) {
    Validate.notNull(futureOptionPrefix, "future option prefix");
    Validate.notNull(postfix, "postfix");
    _futureOptionPrefix = futureOptionPrefix;
    _postfix = postfix;
  }

  @Override
  public Identifier getInstrument(final Integer futureOptionNumber, final Double strike) {
    throw new OpenGammaRuntimeException("Need a surface date to create an interest rate future option surface");
  }

  @Override
  public Identifier getInstrument(final Integer futureOptionNumber, final Double strike, final LocalDate surfaceDate) {
    final StringBuffer ticker = new StringBuffer();
    ticker.append(_futureOptionPrefix);
    ticker.append(createQuarterlyFutureOptions(futureOptionNumber, strike, surfaceDate));
    ticker.append(_postfix);
    return Identifier.of(SCHEME, ticker.toString());
  }

  private String createQuarterlyFutureOptions(final Integer futureOptionNumber, final Double strike, final LocalDate surfaceDate) {
    LocalDate futureOptionExpiry = surfaceDate.with(NEXT_EXPIRY_ADJUSTER);
    final StringBuilder futureOptionCode = new StringBuilder();
    for (int i = 1; i < futureOptionNumber; i++) {
      futureOptionExpiry = (futureOptionExpiry.plusDays(7)).with(NEXT_EXPIRY_ADJUSTER);
    }
    futureOptionCode.append(s_monthCode.get(futureOptionExpiry.getMonthOfYear()));
    final LocalDate today = LocalDate.now();
    if (futureOptionExpiry.isBefore(today.minus(Period.ofMonths(3)))) {
      final int yearsNum = futureOptionExpiry.getYear() % 100;
      if (yearsNum < 10) {
        futureOptionCode.append("0"); // so we get '09' rather than '9'  
      }
      futureOptionCode.append(Integer.toString(yearsNum));
    } else {
      futureOptionCode.append(Integer.toString(futureOptionExpiry.getYear() % 10));
    }
    futureOptionCode.append(" ");
    futureOptionCode.append(strike); //TODO this isn't right - will need to be formatted
    //TODO put/call?
    futureOptionCode.append(" ");
    return futureOptionCode.toString();
  }

  public String getFutureOptionPrefix() {
    return _futureOptionPrefix;
  }

  public String getPostfix() {
    return _postfix;
  }

  @Override
  public int hashCode() {
    return getFutureOptionPrefix().hashCode() + getPostfix().hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof BloombergInterestRateFutureOptionVolatilitySurfaceInstrumentProvider)) {
      return false;
    }
    final BloombergInterestRateFutureOptionVolatilitySurfaceInstrumentProvider other = (BloombergInterestRateFutureOptionVolatilitySurfaceInstrumentProvider) obj;
    return getFutureOptionPrefix().equals(other.getFutureOptionPrefix()) &&
           getPostfix().equals(other.getPostfix());
  }
}
