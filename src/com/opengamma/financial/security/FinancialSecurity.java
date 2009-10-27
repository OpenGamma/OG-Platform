/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import com.opengamma.engine.security.DefaultSecurity;

/**
 * 
 *
 * @author jim
 */
public abstract class FinancialSecurity extends DefaultSecurity {
  public abstract <T> T accept(FinancialSecurityVisitor<T> visitor);
}
