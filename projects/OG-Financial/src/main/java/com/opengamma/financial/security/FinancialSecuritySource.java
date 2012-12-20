/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import java.util.Collection;

import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;

/**
 * A source of security information able to access standard implementations.
 * <p>
 * This interface provides a simple view of securities as needed by the financial project.
 * This may be backed by a full-featured security master, or by a much simpler data structure.
 */
public interface FinancialSecuritySource extends SecuritySource {

  /**
   * Finds the all the available bonds with an issuer type of the provided string.  
   * This would be used, for example, to construct a bond curve.
   * 
   * @param issuerName  the issuer name, wildcards allowed, may be null
   * @return a collection of bond securities with the issuerType specified, not null
   */
  Collection<Security> getBondsWithIssuerName(String issuerName);

}
