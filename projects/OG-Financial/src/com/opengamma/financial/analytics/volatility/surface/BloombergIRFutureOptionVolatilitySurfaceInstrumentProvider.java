/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import java.text.DecimalFormat;

import javax.time.calendar.Clock;
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
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;

/**
 * 
 */
public class BloombergIRFutureOptionVolatilitySurfaceInstrumentProvider implements SurfaceInstrumentProvider<Number, Double> {
  private static BiMap<MonthOfYear, Character> s_monthCode;
  private static final DateAdjuster NEXT_EXPIRY_ADJUSTER = new NextExpiryAdjuster();
  private static final ExternalScheme SCHEME = SecurityUtils.BLOOMBERG_TICKER_WEAK;
  private static final DecimalFormat FORMATTER = new DecimalFormat("##.###");

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
    FORMATTER.setMinimumFractionDigits(3);
  }

  private final String _futureOptionPrefix;
  private final String _postfix;
  private final String _dataFieldName; // expecting MarketDataRequirementNames.IMPLIED_VOLATILITY or OPT_IMPLIED_VOLATILITY_MID
  private final Double _useCallAboveStrike;
  
  /**
   * @param futureOptionPrefix the prefix to the resulting code
   * @param postfix the postfix to the resulting code
   * @param dataFieldName the name of the data field e.g. PX_LAST
   * @param useCallAboveStrike the strike above which to use calls rather than puts
   */
  public BloombergIRFutureOptionVolatilitySurfaceInstrumentProvider(final String futureOptionPrefix, final String postfix, final String dataFieldName, final Double useCallAboveStrike) {
    Validate.notNull(futureOptionPrefix, "future option prefix");
    Validate.notNull(postfix, "postfix");
    Validate.notNull(dataFieldName, "data field name");
    Validate.notNull(useCallAboveStrike, "use call above this strike");
    _futureOptionPrefix = futureOptionPrefix;
    _postfix = postfix;
    _dataFieldName = dataFieldName;
    _useCallAboveStrike = useCallAboveStrike;
  }

  @Override
  public ExternalId getInstrument(final Number futureOptionNumber, final Double strike) {
    throw new OpenGammaRuntimeException("Need a surface date to create an interest rate future option surface");
  }

  @Override
  public ExternalId getInstrument(final Number futureOptionNumber, final Double strike, final LocalDate surfaceDate) {
    final StringBuffer ticker = new StringBuffer();
    ticker.append(_futureOptionPrefix);
    ticker.append(createQuarterlyFutureOptions(futureOptionNumber.intValue(), strike, surfaceDate));
    ticker.append(_postfix);
    return ExternalId.of(SCHEME, ticker.toString());
  }

  private String createQuarterlyFutureOptions(final int futureOptionNumber, final Double strike, final LocalDate surfaceDate) {
    LocalDate futureOptionExpiry = surfaceDate.with(NEXT_EXPIRY_ADJUSTER);
    final StringBuilder futureOptionCode = new StringBuilder();
    for (int i = 1; i < futureOptionNumber; i++) {
      futureOptionExpiry = (futureOptionExpiry.plusDays(7)).with(NEXT_EXPIRY_ADJUSTER);
    }
    futureOptionCode.append(s_monthCode.get(futureOptionExpiry.getMonthOfYear()));
    // TODO: TIMEZONE
    
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
    final String typeString = _useCallAboveStrike < strike ? "C " : "P ";
    futureOptionCode.append(typeString);
    futureOptionCode.append(FORMATTER.format(strike));
    futureOptionCode.append(" ");
    return futureOptionCode.toString();
  }

  public String getFutureOptionPrefix() {
    return _futureOptionPrefix;
  }

  public String getPostfix() {
    return _postfix;
  }

  public Double useCallAboveStrike() {
    return _useCallAboveStrike;
  }

  @Override
  public String getDataFieldName() {
    return _dataFieldName;
  }

  @Override
  public int hashCode() {
    return getFutureOptionPrefix().hashCode() + getPostfix().hashCode() + getDataFieldName().hashCode() + useCallAboveStrike().hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof BloombergIRFutureOptionVolatilitySurfaceInstrumentProvider)) {
      return false;
    }
    final BloombergIRFutureOptionVolatilitySurfaceInstrumentProvider other = (BloombergIRFutureOptionVolatilitySurfaceInstrumentProvider) obj;
    return getFutureOptionPrefix().equals(other.getFutureOptionPrefix()) &&
           getPostfix().equals(other.getPostfix()) &&
           useCallAboveStrike().equals(other.useCallAboveStrike()) &&
           getDataFieldName().equals(other.getDataFieldName());
  }
}
