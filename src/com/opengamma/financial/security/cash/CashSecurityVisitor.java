/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */

package com.opengamma.financial.security.cash;

public interface CashSecurityVisitor<T> {

  T visitCashSecurity(CashSecurity security);

}
