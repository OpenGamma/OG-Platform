/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import java.text.DecimalFormat;
import java.util.HashMap;

import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;

import com.opengamma.financial.analytics.ircurve.NextExpiryAdjuster;
import com.opengamma.financial.analytics.model.FutureOptionExpiries;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;

/**
 * Provides ExternalIds for equity future options used to build a volatility surface
 */
public class BloombergEquityIndexFutureOptionVolatilitySurfaceInstrumentProvider implements SurfaceInstrumentProvider<Pair<Integer, Tenor>, Double> {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(BloombergEquityIndexFutureOptionVolatilitySurfaceInstrumentProvider.class);
  /** The strike formatter */
  private static final DecimalFormat FORMATTER = new DecimalFormat("##.##");
  /** The expiry rules */
  private static final HashMap<String, FutureOptionExpiries> EXPIRY_RULES;
  static {
    EXPIRY_RULES = new HashMap<>();
    EXPIRY_RULES.put("DEFAULT", FutureOptionExpiries.of(new NextExpiryAdjuster(3, DayOfWeek.FRIDAY, 1))); //TODO
  }
  private final String _futureOptionPrefix;
  private final String _postfix;
  private final String _dataFieldName;
  private final Double _useCallAboveStrike;
  private final String _exchangeIdName;
  private final String _tickerSchemeName;

  /**
   * @param futureOptionPrefix the prefix to the resulting code (e.g. SP), not null
   * @param postfix the postfix to the resulting code (e.g. Index), not null
   * @param dataFieldName the name of the data field, not null.
   * @param useCallAboveStrike the strike above which to use calls rather than puts, not null
   * @param exchangeIdName the exchange id, not null
   * @param tickerSchemeName the ticker scheme name, not null
   */
  public BloombergEquityIndexFutureOptionVolatilitySurfaceInstrumentProvider(final String futureOptionPrefix, final String postfix, final String dataFieldName,
      final Double useCallAboveStrike, final String exchangeIdName, final String tickerSchemeName) {
    _futureOptionPrefix = futureOptionPrefix;
    _postfix = postfix;
    _dataFieldName = dataFieldName;
    _useCallAboveStrike = useCallAboveStrike;
    _exchangeIdName = exchangeIdName;
    _tickerSchemeName = tickerSchemeName;
  }

  /**
   * Provides an ExternalID for Bloomberg ticker,
   * given a reference date and an integer offset, the n'th subsequent option <p>
   * The format is prefix + month + year + callPutFlag + strike + postfix <p>
   * e.g. SPH3C 1000.0 Index
   * <p>
   * @param nthOfPeriod n'th future following curve date, not null
   * @param strike option's strike, expressed as price in %, e.g. 98.750, not null
   * @param surfaceDate date of curve validity; valuation date, not null
   * @return the id of the Bloomberg ticker
   */
  @Override
  public ExternalId getInstrument(final Pair<Integer, Tenor> nthOfPeriod, final Double strike, final LocalDate surfaceDate) {
    ArgumentChecker.notNull(nthOfPeriod, "futureOptionNumber");
    ArgumentChecker.notNull(strike, "strike");
    ArgumentChecker.notNull(surfaceDate, "surface date");
    final String prefix = getFutureOptionPrefix();
    final StringBuffer ticker = new StringBuffer();
    ticker.append(prefix);
    FutureOptionExpiries expiryRule = EXPIRY_RULES.get(prefix); // TODO: Review whether we can hoist from loop in RawVolatilitySurfaceDataFunction.buildDataRequirements
    if (expiryRule == null) {
      s_logger.info("No expiry rule has been setup for " + prefix + ". Using Default of 3rd Friday.");
      expiryRule = EXPIRY_RULES.get("DEFAULT");
    }
    final int nthExpiry = nthOfPeriod.getFirst();
    final Tenor period = nthOfPeriod.getSecond();
    final LocalDate expiry = expiryRule.getExpiry(nthExpiry, surfaceDate, period);
    final String expiryCode = BloombergFutureUtils.getShortExpiryCode(expiry);
    ticker.append(expiryCode);
    ticker.append(strike > useCallAboveStrike() ? "C" : "P");
    ticker.append(" ");
    ticker.append(FORMATTER.format(strike));
    ticker.append(" ");
    ticker.append(getPostfix());
    return ExternalId.of(getTickerSchemeName(), ticker.toString());
  }

  /**
   * Gets the expiryRules.
   * @return the expiryRules
   */
  public static HashMap<String, FutureOptionExpiries> getExpiryRules() {
    return EXPIRY_RULES;
  }

  @Override
  public ExternalId getInstrument(final Pair<Integer, Tenor> xAxis, final Double yAxis) {
    throw new UnsupportedOperationException("Must supply a surface date");
  }

  @Override
  public String getDataFieldName() {
    return _dataFieldName;
  }

  public Double useCallAboveStrike() {
    return _useCallAboveStrike;
  }

  /**
   * Gets the future option prefix.
   * @return The future option prefix
   */
  public String getFutureOptionPrefix() {
    return _futureOptionPrefix;
  }

  /**
   * Gets the postfix.
   * @return The postfix
   */
  public String getPostfix() {
    return _postfix;
  }

  /**
   * Gets the exchange id.
   * @return The exchange id
   */
  public String getExchangeIdName() {
    return _exchangeIdName;
  }

  /**
   * Gets the ticker scheme name.
   * @return The ticker scheme name
   */
  public String getTickerSchemeName() {
    return _tickerSchemeName;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _dataFieldName.hashCode();
    result = prime * result + _exchangeIdName.hashCode();
    result = prime * result + _futureOptionPrefix.hashCode();
    result = prime * result + _postfix.hashCode();
    result = prime * result + _tickerSchemeName.hashCode();
    result = prime * result + _useCallAboveStrike.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof BloombergEquityIndexFutureOptionVolatilitySurfaceInstrumentProvider)) {
      return false;
    }
    final BloombergEquityIndexFutureOptionVolatilitySurfaceInstrumentProvider other = (BloombergEquityIndexFutureOptionVolatilitySurfaceInstrumentProvider) obj;
    if (Double.compare(_useCallAboveStrike, other._useCallAboveStrike) != 0) {
      return false;
    }
    if (!ObjectUtils.equals(_futureOptionPrefix, other._futureOptionPrefix)) {
      return false;
    }
    if (!ObjectUtils.equals(_exchangeIdName, other._exchangeIdName)) {
      return false;
    }
    if (!ObjectUtils.equals(_tickerSchemeName, other._tickerSchemeName)) {
      return false;
    }
    if (!ObjectUtils.equals(_dataFieldName, other._dataFieldName)) {
      return false;
    }
    if (!ObjectUtils.equals(_postfix, other._postfix)) {
      return false;
    }
    return true;
  }


}
