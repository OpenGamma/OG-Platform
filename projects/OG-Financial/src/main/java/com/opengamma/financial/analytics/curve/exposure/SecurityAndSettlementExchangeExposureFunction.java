/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve.exposure;

import java.util.Arrays;
import java.util.List;

import com.opengamma.core.position.Trade;
import com.opengamma.core.security.Security;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitorSameValueAdapter;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.future.AgricultureFutureSecurity;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.financial.security.future.DeliverableSwapFutureSecurity;
import com.opengamma.financial.security.future.EnergyFutureSecurity;
import com.opengamma.financial.security.future.EquityFutureSecurity;
import com.opengamma.financial.security.future.EquityIndexDividendFutureSecurity;
import com.opengamma.financial.security.future.FXFutureSecurity;
import com.opengamma.financial.security.future.FederalFundsFutureSecurity;
import com.opengamma.financial.security.future.IndexFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.future.MetalFutureSecurity;
import com.opengamma.financial.security.future.StockFutureSecurity;
import com.opengamma.financial.security.option.BondFutureOptionSecurity;
import com.opengamma.financial.security.option.CommodityFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityBarrierOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexDividendFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.FxFutureOptionSecurity;
import com.opengamma.id.ExternalId;

/**
 * Exposure function that returns the security type and settlement exchange for a given trade. If the trade does not have
 * a settlement exchange, then null is returned.
 */
public class SecurityAndSettlementExchangeExposureFunction implements ExposureFunction {

  /**
   * The name of the exposure function.
   */
  public static final String NAME = "Security / Settlement Exchange";
  
  @Override
  public String getName() {
    return NAME;
  }
  
  @Override
  public List<ExternalId> getIds(Trade trade) {
    Security security = trade.getSecurity();
    if (security instanceof FinancialSecurity) {
      return ((FinancialSecurity) security).accept(SecurityAndSettlementExchangeVisitor.getInstance());
    }
    return null;
  }
  
  /**
   * Visitor that returns the security and settlement exchange for a given trade.
   */
  private static final class SecurityAndSettlementExchangeVisitor extends FinancialSecurityVisitorSameValueAdapter<List<ExternalId>> {
    
    /**
     * The singleton instance.
     */
    private static final SecurityAndSettlementExchangeVisitor INSTANCE = new SecurityAndSettlementExchangeVisitor();
    
    /**
     * Returns the singleton.
     * @return the singleton.
     */
    public static SecurityAndSettlementExchangeVisitor getInstance() {
      return INSTANCE;
    }
    
    /**
     * Constructor that sets the default returned value to null.
     */
    public SecurityAndSettlementExchangeVisitor() {
      super(null);
    }

    @Override
    public List<ExternalId> visitAgricultureFutureSecurity(final AgricultureFutureSecurity security) {
      final String exchange = security.getSettlementExchange();
      final String securityType = security.getSecurityType();
      return Arrays.asList(ExternalId.of(SECURITY_IDENTIFIER, securityType + SEPARATOR + exchange));
    }
  
    @Override
    public List<ExternalId> visitBondFutureSecurity(final BondFutureSecurity security) {
      final String exchange = security.getSettlementExchange();
      final String securityType = security.getSecurityType();
      return Arrays.asList(ExternalId.of(SECURITY_IDENTIFIER, securityType + SEPARATOR + exchange));
    }
  
    @Override
    public List<ExternalId> visitEquityIndexDividendFutureSecurity(final EquityIndexDividendFutureSecurity security) {
      final String exchange = security.getSettlementExchange();
      final String securityType = security.getSecurityType();
      return Arrays.asList(ExternalId.of(SECURITY_IDENTIFIER, securityType + SEPARATOR + exchange));
    }
  
    @Override
    public List<ExternalId> visitFXFutureSecurity(final FXFutureSecurity security) {
      final String exchange = security.getSettlementExchange();
      final String securityType = security.getSecurityType();
      return Arrays.asList(ExternalId.of(SECURITY_IDENTIFIER, securityType + SEPARATOR + exchange));
    }
  
    @Override
    public List<ExternalId> visitStockFutureSecurity(final StockFutureSecurity security) {
      final String exchange = security.getSettlementExchange();
      final String securityType = security.getSecurityType();
      return Arrays.asList(ExternalId.of(SECURITY_IDENTIFIER, securityType + SEPARATOR + exchange));
    }
  
    @Override
    public List<ExternalId> visitEquityFutureSecurity(final EquityFutureSecurity security) {
      final String exchange = security.getSettlementExchange();
      final String securityType = security.getSecurityType();
      return Arrays.asList(ExternalId.of(SECURITY_IDENTIFIER, securityType + SEPARATOR + exchange));
    }
  
    @Override
    public List<ExternalId> visitEnergyFutureSecurity(final EnergyFutureSecurity security) {
      final String exchange = security.getSettlementExchange();
      final String securityType = security.getSecurityType();
      return Arrays.asList(ExternalId.of(SECURITY_IDENTIFIER, securityType + SEPARATOR + exchange));
    }
  
    @Override
    public List<ExternalId> visitIndexFutureSecurity(final IndexFutureSecurity security) {
      final String exchange = security.getSettlementExchange();
      final String securityType = security.getSecurityType();
      return Arrays.asList(ExternalId.of(SECURITY_IDENTIFIER, securityType + SEPARATOR + exchange));
    }
  
    @Override
    public List<ExternalId> visitInterestRateFutureSecurity(final InterestRateFutureSecurity security) {
      final String exchange = security.getSettlementExchange();
      final String securityType = security.getSecurityType();
      return Arrays.asList(ExternalId.of(SECURITY_IDENTIFIER, securityType + SEPARATOR + exchange));
    }
  
    @Override
    public List<ExternalId> visitFederalFundsFutureSecurity(final FederalFundsFutureSecurity security) {
      final String exchange = security.getSettlementExchange();
      final String securityType = security.getSecurityType();
      return Arrays.asList(ExternalId.of(SECURITY_IDENTIFIER, securityType + SEPARATOR + exchange));
    }
  
    @Override
    public List<ExternalId> visitMetalFutureSecurity(final MetalFutureSecurity security) {
      final String exchange = security.getSettlementExchange();
      final String securityType = security.getSecurityType();
      return Arrays.asList(ExternalId.of(SECURITY_IDENTIFIER, securityType + SEPARATOR + exchange));
    }
  
    @Override
    public List<ExternalId> visitCommodityFutureOptionSecurity(final CommodityFutureOptionSecurity security) {
      final String exchange = security.getSettlementExchange();
      final String securityType = security.getSecurityType();
      return Arrays.asList(ExternalId.of(SECURITY_IDENTIFIER, securityType + SEPARATOR + exchange));
    }
  
    @Override
    public List<ExternalId> visitFxFutureOptionSecurity(final FxFutureOptionSecurity security) {
      final String exchange = security.getSettlementExchange();
      final String securityType = security.getSecurityType();
      return Arrays.asList(ExternalId.of(SECURITY_IDENTIFIER, securityType + SEPARATOR + exchange));
    }
  
    @Override
    public List<ExternalId> visitBondFutureOptionSecurity(final BondFutureOptionSecurity security) {
      final String exchange = security.getSettlementExchange();
      final String securityType = security.getSecurityType();
      return Arrays.asList(ExternalId.of(SECURITY_IDENTIFIER, securityType + SEPARATOR + exchange));
    }
  
    @Override
    public List<ExternalId> visitEquityBarrierOptionSecurity(final EquityBarrierOptionSecurity security) {
      final String exchange = security.getExchange();
      final String securityType = security.getSecurityType();
      return Arrays.asList(ExternalId.of(SECURITY_IDENTIFIER, securityType + SEPARATOR + exchange));
    }
  
    @Override
    public List<ExternalId> visitEquityIndexDividendFutureOptionSecurity(final EquityIndexDividendFutureOptionSecurity security) {
      final String exchange = security.getExchange();
      final String securityType = security.getSecurityType();
      return Arrays.asList(ExternalId.of(SECURITY_IDENTIFIER, securityType + SEPARATOR + exchange));
    }
  
    @Override
    public List<ExternalId> visitEquityIndexFutureOptionSecurity(final EquityIndexFutureOptionSecurity security) {
      final String exchange = security.getExchange();
      final String securityType = security.getSecurityType();
      return Arrays.asList(ExternalId.of(SECURITY_IDENTIFIER, securityType + SEPARATOR + exchange));
    }
  
    @Override
    public List<ExternalId> visitEquityIndexOptionSecurity(final EquityIndexOptionSecurity security) {
      final String exchange = security.getExchange();
      final String securityType = security.getSecurityType();
      return Arrays.asList(ExternalId.of(SECURITY_IDENTIFIER, securityType + SEPARATOR + exchange));
    }
  
    @Override
    public List<ExternalId> visitEquityOptionSecurity(final EquityOptionSecurity security) {
      final String exchange = security.getExchange();
      final String securityType = security.getSecurityType();
      return Arrays.asList(ExternalId.of(SECURITY_IDENTIFIER, securityType + SEPARATOR + exchange));
    }
  
    @Override
    public List<ExternalId> visitEquitySecurity(final EquitySecurity security) {
      final String exchange = security.getExchange();
      final String securityType = security.getSecurityType();
      return Arrays.asList(ExternalId.of(SECURITY_IDENTIFIER, securityType + SEPARATOR + exchange));
    }
  
    @Override
    public List<ExternalId> visitDeliverableSwapFutureSecurity(final DeliverableSwapFutureSecurity security) {
      final String exchange = security.getSettlementExchange();
      final String securityType = security.getSecurityType();
      return Arrays.asList(ExternalId.of(SECURITY_IDENTIFIER, securityType + SEPARATOR + exchange));
    }
  }
}
