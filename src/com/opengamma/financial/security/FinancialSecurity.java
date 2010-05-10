/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import com.opengamma.engine.security.DefaultSecurity;

/**
 * An implementation of {@code Security} that implements the visitor pattern.
 */
public abstract class FinancialSecurity extends DefaultSecurity {

  /**
   * Creates a new security.
   * @param securityType  the type, not null
   */
  public FinancialSecurity(String securityType) {
    super(securityType);
  }

  /**
   * Accepts and processes the visitor.
   * 
   * @param <T>  the visitor result type
   * @param visitor  the visitor, not null
   * @return the result
   */
  public abstract <T> T accept(FinancialSecurityVisitor<T> visitor);

}
