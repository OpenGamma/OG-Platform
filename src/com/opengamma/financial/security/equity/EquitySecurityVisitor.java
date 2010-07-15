/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */

package com.opengamma.financial.security.equity;

public interface EquitySecurityVisitor<T> {

  T visitEquitySecurity(EquitySecurity security);

}
