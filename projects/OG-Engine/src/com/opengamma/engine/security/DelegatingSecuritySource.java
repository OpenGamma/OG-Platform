/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.security;

import java.util.Collection;

import com.opengamma.id.UniqueIdentifierSchemeDelegator;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
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

  //-------------------------------------------------------------------------
  @Override
  public Security getSecurity(UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    return chooseDelegate(uid).getSecurity(uid);
  }

  @Override
  public Collection<Security> getSecurities(IdentifierBundle securityKey) {
    ArgumentChecker.notNull(securityKey, "securityKey");
    // TODO: this implementation is poor, but API limits us
    Collection<Security> result; 
    for (SecuritySource delegateSource : getDelegates().values()) {
      result = delegateSource.getSecurities(securityKey);
      if (result != null) {
        return result;
      }
    }
    return getDefaultDelegate().getSecurities(securityKey);
  }

  @Override
  public Security getSecurity(IdentifierBundle securityKey) {
    ArgumentChecker.notNull(securityKey, "securityKey");
    // TODO: this implementation is poor, but API limits us
    Security result;
    for (SecuritySource delegateMaster : getDelegates().values()) {
      result = delegateMaster.getSecurity(securityKey);
      if (result != null) {
        return result;
      }
    }
    return getDefaultDelegate().getSecurity(securityKey);
  }

}
