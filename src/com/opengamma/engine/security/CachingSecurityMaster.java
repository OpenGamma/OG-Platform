/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.security;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;

/**
 * A security master implementation that caches another.
 */
public class CachingSecurityMaster implements SecurityMaster {

  /**
   * The underlying security master.
   */
  private final SecurityMaster _underlying;
  /**
   * The cache.
   */
  private final Map<UniqueIdentifier, Security> _uidCache = new HashMap<UniqueIdentifier, Security>();
  /**
   * The cache.
   */
  private final Map<IdentifierBundle, Collection<Security>> _bundleCache = new HashMap<IdentifierBundle, Collection<Security>>();

  /**
   * Creates a security master.
   * @param underlying  the underlying master, not null
   */
  public CachingSecurityMaster(SecurityMaster underlying) {
    ArgumentChecker.notNull(underlying, "underlying");
    _underlying = underlying;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the underlying security master.
   * @return the underlying security master, not null
   */
  public SecurityMaster getUnderlying() {
    return _underlying;
  }

  //-------------------------------------------------------------------------
  @Override
  public synchronized Security getSecurity(UniqueIdentifier uid) {
    if (_uidCache.containsKey(uid)) {
      return _uidCache.get(uid);
    }
    Security security = getUnderlying().getSecurity(uid);
    _uidCache.put(uid, security);
    return security;
  }

  @Override
  public synchronized Collection<Security> getSecurities(IdentifierBundle secKey) {
    if (_bundleCache.containsKey(secKey)) {
      return _bundleCache.get(secKey);
    }
    Collection<Security> result = getUnderlying().getSecurities(secKey);
    _bundleCache.put(secKey, result);
    return result;
  }

  @Override
  public synchronized Security getSecurity(IdentifierBundle secKey) {
    Collection<Security> matched = getSecurities(secKey);
    if (matched.isEmpty()) {
      return null;
    }
    return matched.iterator().next();
  }

  @Override
  public Set<String> getAllSecurityTypes() {
    return getUnderlying().getAllSecurityTypes();
  }

}
