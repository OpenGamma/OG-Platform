/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.core.security.impl;

import java.util.Collection;
import java.util.Map;

import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.id.UniqueIdentifierSchemeDelegator;
import com.opengamma.util.ArgumentChecker;

/**
 * A source of securities that uses the scheme of the unique identifier to determine which
 * underlying source should handle the request.
 * <p>
 * If no scheme-specific handler has been registered, a default is used.
 */
public class DelegatingSecuritySource extends UniqueIdentifierSchemeDelegator<SecuritySource> implements SecuritySource {

  /**
   * Creates a new instance with a default source of securities.
   * @param defaultSource  the default source to fall back to, not null
   */
  public DelegatingSecuritySource(SecuritySource defaultSource) {
    super(defaultSource);
  }

  public DelegatingSecuritySource(SecuritySource defaultSource, Map<String, SecuritySource> delegates) {
    super(defaultSource, delegates);
  }

  //-------------------------------------------------------------------------
  @Override
  public Security getSecurity(UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    return chooseDelegate(uid).getSecurity(uid);
  }

  @Override
  public Collection<Security> getSecurities(IdentifierBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");
    // best implementation is to return first matching result
    for (SecuritySource delegateSource : getDelegates().values()) {
      Collection<Security> result = delegateSource.getSecurities(bundle);
      if (!result.isEmpty()) {
        return result;
      }
    }
    return getDefaultDelegate().getSecurities(bundle);
  }

  @Override
  public Security getSecurity(IdentifierBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");
    // best implementation is to return first matching result
    for (SecuritySource delegateSource : getDelegates().values()) {
      Security result = delegateSource.getSecurity(bundle);
      if (result != null) {
        return result;
      }
    }
    return getDefaultDelegate().getSecurity(bundle);
  }

}
