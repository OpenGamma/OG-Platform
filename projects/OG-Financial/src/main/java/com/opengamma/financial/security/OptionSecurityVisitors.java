/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import com.opengamma.core.security.Security;
import com.opengamma.financial.security.option.BondFutureOptionSecurity;
import com.opengamma.financial.security.option.CommodityFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.option.OptionType;
import com.opengamma.util.time.Expiry;

/**
 * Utility class containing a number of visitors that get fields typical of option securities.
 */
public class OptionSecurityVisitors {
  /** Strike visitor */
  private static FinancialSecurityVisitorAdapter<Double> s_strikeVisitor = new StrikeVisitor();
  /** Expiry visitor */
  private static FinancialSecurityVisitorAdapter<Expiry> s_expiryVisitor = new ExpiryVisitor();
  /** Exchange visitor */
  private static FinancialSecurityVisitorAdapter<String> s_exchangeVisitor = new ExchangeVisitor();
  /** Option type visitor */
  private static FinancialSecurityVisitorAdapter<OptionType> s_optionTypeVisitor = new OptionTypeVisitor();

  /**
   * Gets a visitor that provides the strike of options.
   * @return Instance of {@link FinancialSecurityVisitorAdapter} that provides the strike of option securities
   */
  public static FinancialSecurityVisitorAdapter<Double> getStrikeVisitor() {
    return s_strikeVisitor;
  }

  /**
   * Gets the strike of a security, if applicable.
   * @param security The security
   * @return The strike
   * @throws UnsupportedOperationException if the security is null or is not one of the types handled.
   */
  public static Double getStrike(final Security security) {
    if (security instanceof FinancialSecurity) {
      return ((FinancialSecurity) security).accept(s_strikeVisitor);
    }
    throw new UnsupportedOperationException("Cannot get strike for security " + security);
  }

  /**
   * Gets a visitor that provides the expiry of an option.
   * @return Instance of {@link FinancialSecurityVisitorAdapter} that provides the expiry of option securities
   */
  public static FinancialSecurityVisitorAdapter<Expiry> getExpiryVisitor() {
    return s_expiryVisitor;
  }

  /**
   * Gets the expiry of a security, if applicable.
   * @param security The security
   * @return The expiry
   * @throws UnsupportedOperationException if the security is null or is not one of the types handled.
   */
  public static Expiry getExpiry(final Security security) {
    if (security instanceof FinancialSecurity) {
      return ((FinancialSecurity) security).accept(s_expiryVisitor);
    }
    throw new UnsupportedOperationException("Cannot get expiry for security " + security);
  }


  /**
   * Gets a visitor that provides the exchange code for an option.
   * @return Instance of {@link FinancialSecurityVisitorAdapter} that provides exchange code of option securities
   */
  public static FinancialSecurityVisitorAdapter<String> getExchangeVisitor() {
    return s_exchangeVisitor;
  }

  /**
   * Gets the exchange of a security, if applicable. If both settlement and trading exchanges are available,
   * returns the settlement exchange.
   * @param security The security
   * @return The strike
   * @throws UnsupportedOperationException if the security is null or is not one of the types handled.
   */
  public static String getExchange(final Security security) {
    if (security instanceof FinancialSecurity) {
      return ((FinancialSecurity) security).accept(s_exchangeVisitor);
    }
    throw new UnsupportedOperationException("Cannot get exchange for security " + security);
  }

  /**
   * Gets a visitor that provides the option type.
   * @return Instance of FinancialSecurityVisitorAdapter that provides {@link OptionType} of option securities
   */
  public static FinancialSecurityVisitorAdapter<OptionType> getOptionTypeVisitor() {
    return s_optionTypeVisitor;
  }

  /**
   * Gets the option type of a security, if applicable.
   * @param security The security
   * @return The option type
   * @throws UnsupportedOperationException if the security is null or is not one of the types handled.
   */
  public static OptionType getOptionType(final Security security) {
    if (security instanceof FinancialSecurity) {
      return ((FinancialSecurity) security).accept(s_optionTypeVisitor);
    }
    throw new UnsupportedOperationException("Cannot get option type for security " + security);
  }

  /**
   * Gets the strike for a security.
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
   * Get the expiry for a security.
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
   * Note: This defaults to Settlement Exchange when both Settlement and Trading Exchanges are available for the SecurityType.
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
   * Get {@link OptionType}, CALL or PUT, for security.
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
