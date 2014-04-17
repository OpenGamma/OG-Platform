/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.curve;

import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.financial.analytics.fxforwardcurve.FXForwardCurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.IndexType;
import com.opengamma.financial.analytics.ircurve.strips.DataFieldType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.Tenor;

/**
 * Generates synthetic tickers for use in FX forward curves.
 */
public class ExampleFXForwardCurveInstrumentProvider implements FXForwardCurveInstrumentProvider {
  /** This provider uses market data directly */
  private static final boolean USE_SPOT_RATE_FROM_GRAPH = false;
  /** The data field type, not used */
  private static final DataFieldType FIELD_TYPE = DataFieldType.OUTRIGHT;
  /** The data field, not used */
  private static final String DATA_FIELD = MarketDataRequirementNames.MARKET_VALUE;
  /** The ticker scheme */
  private static final ExternalScheme SCHEME = ExternalSchemes.OG_SYNTHETIC_TICKER;
  /** The ticker prefix */
  private final String _prefix;
  /** The ticker postfix */
  private final String _postfix;
  /** The spot prefix */
  private final String _spotPrefix;
  /** The data field name */
  private final String _dataFieldName;
  /** The spot name */
  private final String _spotName;
  /** The spot external id */
  private final ExternalId _spotId;

  /**
   * @param prefix The FX forward prefix, not null
   * @param postfix The postfix, not null
   * @param spotPrefix The FX spot prefix, not null
   * @param dataFieldName The data field name, not null
   */
  public ExampleFXForwardCurveInstrumentProvider(final String prefix, final String postfix, final String spotPrefix,
      final String dataFieldName) {
    ArgumentChecker.notNull(prefix, "prefix");
    ArgumentChecker.notNull(postfix, "postfix");
    ArgumentChecker.notNull(spotPrefix, "spot prefix");
    ArgumentChecker.notNull(dataFieldName, "data field name");
    _prefix = prefix;
    _postfix = postfix;
    _spotPrefix = spotPrefix;
    _dataFieldName = dataFieldName;
    _spotName = spotPrefix;
    _spotId = ExternalId.of(SCHEME, _spotName);
  }

  /**
   * Gets the prefix.
   * @return The prefix
   */
  public String getPrefix() {
    return _prefix;
  }

  /**
   * Gets the postfix.
   * @return The postfix
   */
  public String getPostfix() {
    return _postfix;
  }

  /**
   * Gets the spot prefix.
   * @return The spot prefix
   */
  public String getSpotPrefix() {
    return _spotPrefix;
  }

  @Override
  public String getDataFieldName() {
    return _dataFieldName;
  }

  /**
   * Gets the spot name.
   * @return The spot name
   */
  public String getSpotName() {
    return _spotName;
  }

  @Override
  public ExternalId getSpotInstrument() {
    return _spotId;
  }

  @Override
  public String getMarketDataField() {
    return DATA_FIELD;
  }

  @Override
  public DataFieldType getDataFieldType() {
    return FIELD_TYPE;
  }

  @Override
  public boolean useSpotRateFromGraph() {
    return USE_SPOT_RATE_FROM_GRAPH;
  }

  @Override
  public ExternalId getInstrument(final LocalDate curveDate, final Tenor tenor) {
    final StringBuffer ticker = new StringBuffer();
    ticker.append(_prefix);
    final Period period = tenor.getPeriod();
    if (period.getYears() != 0) {
      ticker.append(period.getYears() + "Y");
    } else if (period.getMonths() != 0) {
      ticker.append(period.getMonths() + "M");
    } else {
      final int days = period.getDays();
      if (days != 0) {
        if (days % 7 == 0) {
          ticker.append(days / 7 + "W");
        } else if (days == 1) {
          ticker.append("ON");
        } else if (days == 2) {
          ticker.append("TN");
        } else if (days == 3) {
          ticker.append("SN");
        } else {
          throw new OpenGammaRuntimeException("Cannot handle period of " + days + " days");
        }
      } else {
        throw new OpenGammaRuntimeException("Can only handle periods of year, month, week and day");
      }
    }
    ticker.append(_postfix);
    return ExternalId.of(SCHEME, ticker.toString());
  }

  @Override
  public ExternalId getInstrument(final LocalDate curveDate, final Tenor tenor, final int numQuarterlyFuturesFromTenor) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ExternalId getInstrument(final LocalDate curveDate, final Tenor tenor, final int periodsPerYear, final boolean isPeriodicZeroDeposit) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ExternalId getInstrument(final LocalDate curveDate, final Tenor tenor, final Tenor payTenor, final Tenor receiveTenor, final IndexType payIndexType,
      final IndexType receiveIndexType) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ExternalId getInstrument(final LocalDate curveDate, final Tenor tenor, final Tenor resetTenor, final IndexType indexType) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ExternalId getInstrument(final LocalDate curveDate, final Tenor startTenor, final Tenor futureTenor, final int numFutureFromTenor) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ExternalId getInstrument(final LocalDate curveDate, final Tenor startTenor, final int startIMMPeriods, final int endIMMPeriods) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int hashCode() {
    return getPrefix().hashCode() + getPostfix().hashCode() + getDataFieldName().hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof ExampleFXForwardCurveInstrumentProvider)) {
      return false;
    }
    final ExampleFXForwardCurveInstrumentProvider other = (ExampleFXForwardCurveInstrumentProvider) obj;
    return getPrefix().equals(other.getPrefix()) &&
        getPostfix().equals(other.getPostfix()) &&
        getSpotPrefix().equals(other.getSpotPrefix()) &&
        getDataFieldName().equals(other.getDataFieldName());
  }

}
