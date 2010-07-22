package com.opengamma.financial.security.swap;

/**
 * Visitor for the Notional subclasses.
 * 
 * @param <T> visitor method return type
 */
public interface NotionalVisitor<T> {

  T visitCommodityNotional(CommodityNotional notional);

  T visitInterestRateNotional(InterestRateNotional notional);

  T visitSecurityNotional(SecurityNotional notional);

}
