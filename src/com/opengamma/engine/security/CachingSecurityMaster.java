/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.security;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.opengamma.id.DomainSpecificIdentifier;
import com.opengamma.id.DomainSpecificIdentifiers;

/**
 * 
 *
 * @author kirk
 */
public class CachingSecurityMaster implements SecurityMaster {
  private final SecurityMaster _underlying;
  private final Map<DomainSpecificIdentifiers, Security> _cache = new HashMap<DomainSpecificIdentifiers, Security>();
  private final Map<DomainSpecificIdentifier, Security> _cacheByIdentityKey = new HashMap<DomainSpecificIdentifier, Security>();
  
  public CachingSecurityMaster(SecurityMaster underlying) {
    assert underlying != null;
    _underlying = underlying;
  }

  /**
   * @return the underlying
   */
  public SecurityMaster getUnderlying() {
    return _underlying;
  }

  @Override
  public Set<String> getAllSecurityTypes() {
    return getUnderlying().getAllSecurityTypes();
  }

  @Override
  public Collection<Security> getSecurities(DomainSpecificIdentifiers secKey) {
    return getUnderlying().getSecurities(secKey);
  }

  @Override
  public synchronized Security getSecurity(DomainSpecificIdentifiers secKey) {
    if(_cache.containsKey(secKey)) {
      return _cache.get(secKey);
    }
    Security result = getUnderlying().getSecurity(secKey);
    _cache.put(secKey, result);
    return result;
  }

  @Override
  public synchronized Security getSecurity(DomainSpecificIdentifier identityKey) {
    if(_cacheByIdentityKey.containsKey(identityKey)) {
      return _cacheByIdentityKey.get(identityKey);
    }
    Security security = getUnderlying().getSecurity(identityKey);
    _cacheByIdentityKey.put(identityKey, security);
    return security;
  }

}
