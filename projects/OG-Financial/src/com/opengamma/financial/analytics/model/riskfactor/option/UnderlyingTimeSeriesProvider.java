/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.riskfactor.option;

import javax.time.calendar.LocalDate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.greeks.AbstractGreekVisitor;
import com.opengamma.analytics.financial.greeks.Greek;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * 
 */
public class UnderlyingTimeSeriesProvider {
  private static final FieldGreekVisitor FIELD_VISITOR = new FieldGreekVisitor();
  private final HistoricalTimeSeriesSource _timeSeriesSource;
  private final String _resolutionKey;
  private final UnderlyingFinancialSecurityVisitor _securityVisitor;

  public UnderlyingTimeSeriesProvider(final HistoricalTimeSeriesSource timeSeriesSource, final String resolutionKey, final SecuritySource securitySource) {
    ArgumentChecker.notNull(timeSeriesSource, "time series source");
    ArgumentChecker.notNull(resolutionKey, "resolution key");
    ArgumentChecker.notNull(securitySource, "security source");
    _timeSeriesSource = timeSeriesSource;
    _resolutionKey = resolutionKey;
    _securityVisitor = new UnderlyingFinancialSecurityVisitor(securitySource);
  }

  public LocalDateDoubleTimeSeries getSeries(final Greek greek, final FinancialSecurity security) {
    return getSeries(greek, security, null, null);
  }

  public LocalDateDoubleTimeSeries getSeries(final Greek greek, final FinancialSecurity security, final LocalDate startDate, final LocalDate endDate) {
    final String fieldName = greek.accept(FIELD_VISITOR);
    final ExternalIdBundle underlyingId = security.accept(_securityVisitor);
    final HistoricalTimeSeries hts = _timeSeriesSource.getHistoricalTimeSeries(fieldName, underlyingId, _resolutionKey, startDate, true, endDate, true);
    if (hts == null) {
      throw new OpenGammaRuntimeException("Could not get time series pair for " + underlyingId + " for security " + security + " for " + _resolutionKey + "/" + fieldName);
    }
    return hts.getTimeSeries();
  }

  private static class FieldGreekVisitor extends AbstractGreekVisitor<String> {

    public FieldGreekVisitor() {
    }

    @Override
    public String visitDelta() {
      return MarketDataRequirementNames.MARKET_VALUE;
    }

    @Override
    public String visitGamma() {
      return MarketDataRequirementNames.MARKET_VALUE;
    }
  }

  private class UnderlyingFinancialSecurityVisitor extends FinancialSecurityVisitorAdapter<ExternalIdBundle> {
    private final SecuritySource _securitySource;

    public UnderlyingFinancialSecurityVisitor(final SecuritySource securitySource) {
      _securitySource = securitySource;
    }

    @Override
    public ExternalIdBundle visitEquityIndexOptionSecurity(final EquityIndexOptionSecurity security) {
      return _securitySource.getSecurity(ExternalIdBundle.of(security.getUnderlyingId())).getExternalIdBundle();
    }

    @Override
    public ExternalIdBundle visitEquityOptionSecurity(final EquityOptionSecurity security) {
      return _securitySource.getSecurity(ExternalIdBundle.of(security.getUnderlyingId())).getExternalIdBundle();
    }

  }
}
