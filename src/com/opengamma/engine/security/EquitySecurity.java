/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.security;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.opengamma.IdentificationDomain;
import com.opengamma.DomainSpecificIdentifier;

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
