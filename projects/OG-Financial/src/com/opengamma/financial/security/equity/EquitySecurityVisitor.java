/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.financial.security.equity;

/**
 * Visitor for the {@code EquitySecurity} subclasses.
 * 
 * @param <T> visitor method return type
 */
public interface EquitySecurityVisitor<T> {

  T visitEquitySecurity(EquitySecurity security);

}
