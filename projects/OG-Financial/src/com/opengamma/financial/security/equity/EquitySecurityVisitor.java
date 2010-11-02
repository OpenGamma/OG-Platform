/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
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
