/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.riskfactor.option;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.greeks.AbstractGreekVisitor;
import com.opengamma.analytics.financial.greeks.Greek;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.analytics.timeseries.DateConstraint;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolutionResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class UnderlyingTimeSeriesProvider {

  private static final FieldGreekVisitor FIELD_VISITOR = new FieldGreekVisitor();

  private final HistoricalTimeSeriesResolver _timeSeriesResolver;
  private final String _resolutionKey;
  private final UnderlyingFinancialSecurityVisitor _securityVisitor;

  public UnderlyingTimeSeriesProvider(final HistoricalTimeSeriesResolver timeSeriesResolver, final String resolutionKey, final SecuritySource securitySource) {
    ArgumentChecker.notNull(timeSeriesResolver, "timeSeriesResolver");
    ArgumentChecker.notNull(resolutionKey, "resolutionLey");
    ArgumentChecker.notNull(securitySource, "securitySource");
    _timeSeriesResolver = timeSeriesResolver;
    _resolutionKey = resolutionKey;
    _securityVisitor = new UnderlyingFinancialSecurityVisitor(securitySource);
  }

  private HistoricalTimeSeriesResolver getTimeSeriesResolver() {
    return _timeSeriesResolver;
  }

  private String getResolutionKey() {
    return _resolutionKey;
  }

  private UnderlyingFinancialSecurityVisitor getSecurityVisitor() {
    return _securityVisitor;
  }

  public ValueRequirement getSeriesRequirement(final Greek greek, final FinancialSecurity security) {
    return getSeriesRequirement(greek, security, DateConstraint.NULL, DateConstraint.VALUATION_TIME);
  }

  public ValueRequirement getSeriesRequirement(final Greek greek, final FinancialSecurity security, final DateConstraint startDate, final DateConstraint endDate) {
    final String fieldName = greek.accept(FIELD_VISITOR);
    final ExternalIdBundle underlyingId = security.accept(getSecurityVisitor());
    final HistoricalTimeSeriesResolutionResult timeSeries = getTimeSeriesResolver().resolve(underlyingId, null, null, null, fieldName, getResolutionKey());
    if (timeSeries == null) {
      throw new OpenGammaRuntimeException("Could not resolve time series for " + underlyingId + " for security " + security + " for " + getResolutionKey() + "/" + fieldName);
    }
    return HistoricalTimeSeriesFunctionUtils.createHTSRequirement(timeSeries, fieldName, startDate, true, endDate, true);
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
      return _securitySource.getSingle(ExternalIdBundle.of(security.getUnderlyingId())).getExternalIdBundle();
    }

    @Override
    public ExternalIdBundle visitEquityOptionSecurity(final EquityOptionSecurity security) {
      Security underlyingSecurity = _securitySource.getSingle(ExternalIdBundle.of(security.getUnderlyingId()));
      if (underlyingSecurity == null) {
        throw new NullPointerException("Unable to obtain underlying security for " + security.getUnderlyingId());
      }
      return underlyingSecurity.getExternalIdBundle();
    }

  }
}
