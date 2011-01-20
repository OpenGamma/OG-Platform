/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.financial.security.cash;

/**
 * Visitor for the {@code CashSecurity} subclasses.
 * 
 * @param <T> visitor method return type
 */
public interface CashSecurityVisitor<T> {

  T visitCashSecurity(CashSecurity security);

}
