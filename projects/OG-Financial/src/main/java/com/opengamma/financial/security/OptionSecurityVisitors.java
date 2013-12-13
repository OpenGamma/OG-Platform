/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import com.opengamma.financial.security.option.BondFutureOptionSecurity;
import com.opengamma.financial.security.option.CommodityFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.option.OptionType;
import com.opengamma.util.time.Expiry;

/**
 * Utility class contain a number of visitors that get fields typical of Option Securities
 */
public class OptionSecurityVisitors {

  private static FinancialSecurityVisitorAdapter<Double> s_strikeVisitor = new StrikeVisitor();
  private static FinancialSecurityVisitorAdapter<Expiry> s_expiryVisitor = new ExpiryVisitor();
  private static FinancialSecurityVisitorAdapter<String> s_exchangeVisitor = new ExchangeVisitor();
  private static FinancialSecurityVisitorAdapter<OptionType> s_optionTypeVisitor = new OptionTypeVisitor();
  
  /**
   * @return Instance of {@link FinancialSecurityVisitorAdapter} that provides strike of option securities
   */
  public static FinancialSecurityVisitorAdapter<Double> getStrikeVisitor() {
    return s_strikeVisitor;
  }
  /**
   * @return Instance of {@link FinancialSecurityVisitorAdapter}  that provides Expiry of option securities
   */
  public static FinancialSecurityVisitorAdapter<Expiry> getExpiryVisitor() {
    return s_expiryVisitor;
  }
  /**
   * @return Instance of {@link FinancialSecurityVisitorAdapter}  that provides Exchange Code of option securities
   */
  public static FinancialSecurityVisitorAdapter<String> getExchangeVisitor() {
    return s_exchangeVisitor;
  }
  /**
   * @return Instance of FinancialSecurityVisitorAdapter that provides {@link OptionType} of option securities
   */
  public static FinancialSecurityVisitorAdapter<OptionType> getOptionTypeVisitor() {
    return s_optionTypeVisitor;
  }
  
  /**
   * Get strike for security
   */
  public static class StrikeVisitor extends FinancialSecurityVisitorAdapter<Double> {
    
    @Override
    public Double visitEquityIndexOptionSecurity(final EquityIndexOptionSecurity security) {
      return Double.valueOf(security.getStrike());
    }

    @Override
    public Double visitEquityOptionSecurity(final EquityOptionSecurity security) {
      return Double.valueOf(security.getStrike());
    }

    @Override
    public Double visitEquityIndexFutureOptionSecurity(final EquityIndexFutureOptionSecurity security) {
      return Double.valueOf(security.getStrike());
    }

    @Override
    public Double visitBondFutureOptionSecurity(final BondFutureOptionSecurity security) {
      return Double.valueOf(security.getStrike());
    }

    @Override
    public Double visitCommodityFutureOptionSecurity(final CommodityFutureOptionSecurity security) {
      return Double.valueOf(security.getStrike());
    }
    
    @Override
    public Double visitIRFutureOptionSecurity(final IRFutureOptionSecurity security) {
      return Double.valueOf(security.getStrike());
    }
  }
  
  /**
   * Get Expiry for security
   */
  public static class ExpiryVisitor extends FinancialSecurityVisitorAdapter<Expiry> {
    
    @Override
    public Expiry visitEquityIndexOptionSecurity(final EquityIndexOptionSecurity security) {
      return security.getExpiry();
    }

    @Override
    public Expiry visitEquityOptionSecurity(final EquityOptionSecurity security) {
      return security.getExpiry();
    }

    @Override
    public Expiry visitEquityIndexFutureOptionSecurity(final EquityIndexFutureOptionSecurity security) {
      return security.getExpiry();
    }

    @Override
    public Expiry visitBondFutureOptionSecurity(final BondFutureOptionSecurity security) {
      return security.getExpiry();
    }

    @Override
    public Expiry visitCommodityFutureOptionSecurity(final CommodityFutureOptionSecurity security) {
      return security.getExpiry();
    }
    
    @Override
    public Expiry visitIRFutureOptionSecurity(final IRFutureOptionSecurity security) {
      return security.getExpiry();
    }
  }
  
  /**
   * Get Exchange for security. <p>
   * NOte: This defaults to Settlement Exchange when both Settlement and Trading Exchanges are available for the SecurityType.
   */
  public static class ExchangeVisitor extends FinancialSecurityVisitorAdapter<String> {
    
    @Override
    public String visitEquityIndexOptionSecurity(final EquityIndexOptionSecurity security) {
      return security.getExchange();
    }

    @Override
    public String visitEquityOptionSecurity(final EquityOptionSecurity security) {
      return security.getExchange();
    }

    @Override
    public String visitEquityIndexFutureOptionSecurity(final EquityIndexFutureOptionSecurity security) {
      return security.getExchange();
    }

    @Override
    public String visitBondFutureOptionSecurity(final BondFutureOptionSecurity security) {
      return security.getSettlementExchange();
    }

    @Override
    public String visitCommodityFutureOptionSecurity(final CommodityFutureOptionSecurity security) {
      return security.getSettlementExchange();
    }
    
    @Override
    public String visitIRFutureOptionSecurity(final IRFutureOptionSecurity security) {
      return security.getExchange();
    }
  }
  
  /**
   * Get {@link OptionType}, CALL or PUT, for security. <p>
   */
  public static class OptionTypeVisitor extends FinancialSecurityVisitorAdapter<OptionType> {
    
    @Override
    public OptionType visitEquityIndexOptionSecurity(final EquityIndexOptionSecurity security) {
      return security.getOptionType();
    }

    @Override
    public OptionType visitEquityOptionSecurity(final EquityOptionSecurity security) {
      return security.getOptionType();
    }

    @Override
    public OptionType visitEquityIndexFutureOptionSecurity(final EquityIndexFutureOptionSecurity security) {
      return security.getOptionType();
    }

    @Override
    public OptionType visitBondFutureOptionSecurity(final BondFutureOptionSecurity security) {
      return security.getOptionType();
    }

    @Override
    public OptionType visitCommodityFutureOptionSecurity(final CommodityFutureOptionSecurity security) {
      return security.getOptionType();
    }
    
    @Override
    public OptionType visitIRFutureOptionSecurity(final IRFutureOptionSecurity security) {
      return security.getOptionType();
    }
  }
  
}
