/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.deposit;

/**
 * Visitor for the {@code PeriodicZeroDepositSecurity} type
 * 
 * @param <T> visitor method return type
 */
public interface PeriodicZeroDepositSecurityVisitor<T> {

  T visitPeriodicZeroDepositSecurity(PeriodicZeroDepositSecurity security);
}
