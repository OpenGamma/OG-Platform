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

import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;

/**
 * 
 *
 * @author kirk
 */
public class CachingSecurityMaster implements SecurityMaster {
  private final SecurityMaster _underlying;
  private final Map<IdentifierBundle, Collection<Security>> _identifierBundle2SecurityCollectionCache = new HashMap<IdentifierBundle, Collection<Security>>();
  private final Map<IdentifierBundle, Security> _identifierBundle2SecurityCache = new HashMap<IdentifierBundle, Security>();
  private final Map<Identifier, Security> _identityKey2SecurityCache = new HashMap<Identifier, Security>();
  
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
  public Collection<Security> getSecurities(IdentifierBundle secKey) {
    if(_identifierBundle2SecurityCollectionCache.containsKey(secKey)) {
      return _identifierBundle2SecurityCollectionCache.get(secKey);
    }
    Collection<Security> result = getUnderlying().getSecurities(secKey);
    _identifierBundle2SecurityCollectionCache.put(secKey, result);
    return result;
  }

  @Override
  public synchronized Security getSecurity(IdentifierBundle secKey) {
    if(_identifierBundle2SecurityCache.containsKey(secKey)) {
      return _identifierBundle2SecurityCache.get(secKey);
    }
    Security result = getUnderlying().getSecurity(secKey);
    _identifierBundle2SecurityCache.put(secKey, result);
    return result;
  }

  @Override
  public synchronized Security getSecurity(Identifier identityKey) {
    if(_identityKey2SecurityCache.containsKey(identityKey)) {
      return _identityKey2SecurityCache.get(identityKey);
    }
    Security security = getUnderlying().getSecurity(identityKey);
    _identityKey2SecurityCache.put(identityKey, security);
    return security;
  }

}
