/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.security;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.opengamma.id.DelegateByScheme;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;

/**
 * A security master that uses the scheme of the unique identifier to determine which
 * underlying security master should handle the request.
 * <p>
 * If no scheme-specific handler has been registered, a default is used.
 */
public class DelegatingSecurityMaster extends DelegateByScheme<SecurityMaster> implements SecurityMaster {

  /**
   * Constructs a new security master.
   * 
   * @param defaultMaster  the default master to fall back to, not null
   */
  public DelegatingSecurityMaster(SecurityMaster defaultMaster) {
    super(defaultMaster);
  }

  //-------------------------------------------------------------------------
  @Override
  public Security getSecurity(UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    return chooseDelegate(uid).getSecurity(uid);
  }

  @Override
  public Collection<Security> getSecurities(IdentifierBundle secKey) {
    // TODO: this implementation is poor, but API limits us
    Collection<Security> result = getDefaultDelegate().getSecurities(secKey);
    if (result != null) {
      return result;
    }
    for (SecurityMaster delegateMaster : getDelegates()) {
      result = delegateMaster.getSecurities(secKey);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  @Override
  public Security getSecurity(IdentifierBundle secKey) {
    // TODO: this implementation is poor, but API limits us
    Security result = getDefaultDelegate().getSecurity(secKey);
    if (result != null) {
      return result;
    }
    for (SecurityMaster delegateMaster : getDelegates()) {
      result = delegateMaster.getSecurity(secKey);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  @Override
  public Set<String> getAllSecurityTypes() {
    Set<String> result = new HashSet<String>(getDefaultDelegate().getAllSecurityTypes());
    for (SecurityMaster delegateMaster : getDelegates()) {
      result.addAll(delegateMaster.getAllSecurityTypes());
    }
    return Collections.unmodifiableSet(result);
  }

}
