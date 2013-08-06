/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.swap;

/**
 * Visitor for the {@code Notional} subclasses.
 * 
 * @param <T> visitor method return type
 */
public interface NotionalVisitor<T> {

  T visitCommodityNotional(CommodityNotional notional);

  T visitInterestRateNotional(InterestRateNotional notional);

  T visitSecurityNotional(SecurityNotional notional);

  T visitVarianceSwapNotional(VarianceSwapNotional notional);
}
