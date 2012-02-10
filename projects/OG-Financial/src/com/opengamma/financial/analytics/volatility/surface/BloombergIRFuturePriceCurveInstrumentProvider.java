/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import java.text.DecimalFormat;

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
import com.opengamma.util.OpenGammaClock;

/**
 * 
 */
public class BloombergIRFuturePriceCurveInstrumentProvider implements FuturePriceCurveInstrumentProvider<Number> {
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

  private final String _futurePrefix;
  private final String _postfix;
  private final String _dataFieldName; // expecting MarketDataRequirementNames.MARKET_PRICE

  public BloombergIRFuturePriceCurveInstrumentProvider(final String futurePrefix, final String postfix, final String dataFieldName) {
    Validate.notNull(futurePrefix, "future option prefix");
    Validate.notNull(postfix, "postfix");
    Validate.notNull(dataFieldName, "data field name");
    _futurePrefix = futurePrefix;
    _postfix = postfix;
    _dataFieldName = dataFieldName;
  }

  @Override
  public ExternalId getInstrument(final Number futureNumber) {
    throw new OpenGammaRuntimeException("Need a surface date to create an interest rate future option surface");
  }

  @Override
  public ExternalId getInstrument(final Number futureNumber, final LocalDate surfaceDate) {
    final StringBuffer ticker = new StringBuffer();
    ticker.append(_futurePrefix);
    ticker.append(createQuarterlyFutures(futureNumber.intValue(), surfaceDate));
    ticker.append(_postfix);
    return ExternalId.of(SCHEME, ticker.toString());
  }

  private String createQuarterlyFutures(final int futureNumber, final LocalDate surfaceDate) {
    LocalDate futureExpiry = surfaceDate.with(NEXT_EXPIRY_ADJUSTER);
    final StringBuilder futureCode = new StringBuilder();
    for (int i = 1; i < futureNumber; i++) {
      futureExpiry = (futureExpiry.plusDays(7)).with(NEXT_EXPIRY_ADJUSTER);
    }
    futureCode.append(s_monthCode.get(futureExpiry.getMonthOfYear()));
    final LocalDate today = LocalDate.now(OpenGammaClock.getInstance());
    if (futureExpiry.isBefore(today.minus(Period.ofMonths(3)))) {
      final int yearsNum = futureExpiry.getYear() % 100;
      if (yearsNum < 10) {
        futureCode.append("0"); // so we get '09' rather than '9'  
      }
      futureCode.append(Integer.toString(yearsNum));
    } else {
      futureCode.append(Integer.toString(futureExpiry.getYear() % 10));
    }
    futureCode.append(" ");
    return futureCode.toString();
  }

  public String getFuturePrefix() {
    return _futurePrefix;
  }

  public String getPostfix() {
    return _postfix;
  }

  @Override
  public String getDataFieldName() {
    return _dataFieldName;
  }

  @Override
  public int hashCode() {
    return getFuturePrefix().hashCode() + getPostfix().hashCode() + getDataFieldName().hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof BloombergIRFuturePriceCurveInstrumentProvider)) {
      return false;
    }
    final BloombergIRFuturePriceCurveInstrumentProvider other = (BloombergIRFuturePriceCurveInstrumentProvider) obj;
    return getFuturePrefix().equals(other.getFuturePrefix()) &&
           getPostfix().equals(other.getPostfix()) &&
           getDataFieldName().equals(other.getDataFieldName());
  }
}
