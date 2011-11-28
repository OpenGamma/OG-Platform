/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import javax.time.calendar.LocalDate;
import javax.time.calendar.format.DateTimeFormatter;
import javax.time.calendar.format.DateTimeFormatters;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;

/**
 * Generates equity option Bloomberg ticker codes from ATM strike (set via ini
 * t()), tenor, double and date.
 */
public class BloombergEquityOptionVolatilitySurfaceInstrumentProvider implements SurfaceInstrumentProvider<LocalDate, Double> {
  private static final Logger s_logger = LoggerFactory.getLogger(BloombergEquityOptionVolatilitySurfaceInstrumentProvider.class);
  private static final ExternalScheme SCHEME = SecurityUtils.BLOOMBERG_TICKER_WEAK;

  private final String _underlyingPrefix; //expecting something like DJX
  private final String _postfix; //expecting Index or Equity
  private final String _dataFieldName; //expecting MarketDataRequirementNames.MARKET_VALUE
  private static final DateTimeFormatter s_dateFormatter = DateTimeFormatters.pattern("MM/dd/yy");

  private Boolean _generatePuts;

  public BloombergEquityOptionVolatilitySurfaceInstrumentProvider(final String underlyingPrefix, final String postfix, final String dataFieldName) {
    Validate.notNull(underlyingPrefix, "underlying prefix");
    Validate.notNull(postfix, "postfix");
    Validate.notNull(dataFieldName, "data field name");
    _underlyingPrefix = underlyingPrefix;
    _postfix = postfix;
    _dataFieldName = dataFieldName;
  }
  
  public void init(boolean generatePuts) {
    _generatePuts = generatePuts;
  }

  public String getUnderlyingPrefix() {
    return _underlyingPrefix;
  }

  public String getPostfix() {
    return _postfix;
  }

  @Override
  public String getDataFieldName() {
    return _dataFieldName;
  }

  @Override
  public ExternalId getInstrument(final LocalDate expiry, final Double strike) {
    throw new OpenGammaRuntimeException("Need surface date to calculate expiry");
  }

  @Override
  public ExternalId getInstrument(final LocalDate expiry, final Double strike, final LocalDate surfaceDate) {
    return createEquityOptionVolatilityCode(expiry, strike);
  }

  private ExternalId createEquityOptionVolatilityCode(final LocalDate expiry, final Double strike) {
    if (_generatePuts == null) {
      s_logger.error("Cannot create option volatility code until atm strike is set (use init method)");
    }
    final StringBuffer ticker = new StringBuffer();
    ticker.append(_underlyingPrefix);
    ticker.append(" ");
    String formattedDate = s_dateFormatter.print(expiry);
    ticker.append(formattedDate);
    ticker.append(" ");
    // TODO: check this logic
    if (_generatePuts) {
      ticker.append("P");
    } else {
      ticker.append("C");
    }
    ticker.append(strike);
    ticker.append(" ");
    ticker.append(_postfix);
    return ExternalId.of(SCHEME, ticker.toString());
  }

  @Override
  public int hashCode() {
    return getUnderlyingPrefix().hashCode() + getPostfix().hashCode() + getDataFieldName().hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof BloombergEquityOptionVolatilitySurfaceInstrumentProvider)) {
      return false;
    }
    final BloombergEquityOptionVolatilitySurfaceInstrumentProvider other = (BloombergEquityOptionVolatilitySurfaceInstrumentProvider) obj;
    return getUnderlyingPrefix().equals(other.getUnderlyingPrefix()) &&
           getPostfix().equals(other.getPostfix()) &&
           getDataFieldName().equals(other.getDataFieldName());
  }
}
