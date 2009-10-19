/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import java.util.Collections;

import com.opengamma.engine.security.DefaultSecurity;
import com.opengamma.engine.security.Security;
import com.opengamma.id.DomainSpecificIdentifier;
import com.opengamma.id.IdentificationDomain;

/**
 * A concrete, JavaBean-based implementation of {@link Security}. 
 *
 * @author kirk
 */
public class EquitySecurity extends DefaultSecurity {
  public EquitySecurity(String ticker, String domain) {
    setSecurityType("EQUITY_OPTION");
    setIdentifiers(Collections.singleton(new DomainSpecificIdentifier(new IdentificationDomain(domain), ticker)));
  }
}
