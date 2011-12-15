/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.riskfactor.option;

import javax.time.calendar.LocalDate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesFields;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.greeks.AbstractGreekVisitor;
import com.opengamma.financial.greeks.Greek;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.FXSecurity;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.financial.security.option.EquityBarrierOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexDividendFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXOptionSecurity;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;
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
  
  public UnderlyingTimeSeriesProvider(final HistoricalTimeSeriesSource timeSeriesSource, final String resolutionKey, 
      final SecuritySource securitySource) {
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
    
    public String visitDelta() {
      return HistoricalTimeSeriesFields.LAST_PRICE;
    }
    
    public String visitGamma() {
      return HistoricalTimeSeriesFields.LAST_PRICE;
    }
  }

  private class UnderlyingFinancialSecurityVisitor implements FinancialSecurityVisitor<ExternalIdBundle> {
    private final SecuritySource _securitySource;
    
    public UnderlyingFinancialSecurityVisitor(final SecuritySource securitySource) {
      _securitySource = securitySource;
    }
    
    @Override
    public ExternalIdBundle visitBondSecurity(final BondSecurity security) {
      throw new UnsupportedOperationException("This visitor does not support BondSecurity");
    }

    @Override
    public ExternalIdBundle visitCashSecurity(final CashSecurity security) {
      throw new UnsupportedOperationException("This visitor does not support CashSecurity");
    }

    @Override
    public ExternalIdBundle visitEquitySecurity(final EquitySecurity security) {
      throw new UnsupportedOperationException("This visitor does not support EquitySecurity");
    }

    @Override
    public ExternalIdBundle visitFRASecurity(final FRASecurity security) {
      throw new UnsupportedOperationException("This visitor does not support FRASecurity");
    }

    @Override
    public ExternalIdBundle visitFutureSecurity(final FutureSecurity security) {
      throw new UnsupportedOperationException("This visitor does not support FutureSecurity");
    }

    @Override
    public ExternalIdBundle visitSwapSecurity(final SwapSecurity security) {
      throw new UnsupportedOperationException("This visitor does not support SwapSecurity");
    }

    @Override
    public ExternalIdBundle visitEquityIndexOptionSecurity(final EquityIndexOptionSecurity security) {
      return _securitySource.getSecurity(ExternalIdBundle.of(security.getUnderlyingId())).getExternalIdBundle();
    }

    @Override
    public ExternalIdBundle visitEquityOptionSecurity(final EquityOptionSecurity security) {
      return _securitySource.getSecurity(ExternalIdBundle.of(security.getUnderlyingId())).getExternalIdBundle();
    }

    @Override
    public ExternalIdBundle visitEquityBarrierOptionSecurity(final EquityBarrierOptionSecurity security) {
      throw new UnsupportedOperationException("This visitor does not support EquityBarrierOptionSecurity");
    }

    @Override
    public ExternalIdBundle visitFXOptionSecurity(final FXOptionSecurity security) {
      throw new UnsupportedOperationException("This visitor does not support FXOptionSecurity");
    }

    @Override
    public ExternalIdBundle visitNonDeliverableFXOptionSecurity(final NonDeliverableFXOptionSecurity security) {
      throw new UnsupportedOperationException("This visitor does not support NonDeliverableFXOptionSecurity");
    }

    @Override
    public ExternalIdBundle visitSwaptionSecurity(final SwaptionSecurity security) {
      throw new UnsupportedOperationException("This visitor does not support SwaptionSecurity");
    }

    @Override
    public ExternalIdBundle visitIRFutureOptionSecurity(final IRFutureOptionSecurity security) {
      throw new UnsupportedOperationException("This visitor does not support IRFutureOptionSecurity");
    }

    @Override
    public ExternalIdBundle visitEquityIndexDividendFutureOptionSecurity(final EquityIndexDividendFutureOptionSecurity security) {
      throw new UnsupportedOperationException("This visitor does not support EquityIndexDividendFutureOptionSecurity");
    }

    @Override
    public ExternalIdBundle visitFXBarrierOptionSecurity(final FXBarrierOptionSecurity security) {
      throw new UnsupportedOperationException("This visitor does not support FXBarrierOptionSecurity");
    }

    @Override
    public ExternalIdBundle visitFXSecurity(final FXSecurity security) {
      throw new UnsupportedOperationException("This visitor does not support FXSecurity");
    }

    @Override
    public ExternalIdBundle visitFXForwardSecurity(final FXForwardSecurity security) {
      throw new UnsupportedOperationException("This visitor does not support FXForwardSecurity");
    }

    @Override
    public ExternalIdBundle visitNonDeliverableFXForwardSecurity(final NonDeliverableFXForwardSecurity security) {
      throw new UnsupportedOperationException("This visitor does not support NonDeliverableFXForwardSecurity");
    }

    @Override
    public ExternalIdBundle visitCapFloorSecurity(final CapFloorSecurity security) {
      throw new UnsupportedOperationException("This visitor does not support CapFloorSecurity");
    }

    @Override
    public ExternalIdBundle visitCapFloorCMSSpreadSecurity(final CapFloorCMSSpreadSecurity security) {
      throw new UnsupportedOperationException("This visitor does not support CapFloorCMSSpreadSecurity");
    }

    @Override
    public ExternalIdBundle visitEquityVarianceSwapSecurity(final EquityVarianceSwapSecurity security) {
      throw new UnsupportedOperationException("This visitor does not support EquityVarianceSwapSecurity");
    }
    
  }
}
