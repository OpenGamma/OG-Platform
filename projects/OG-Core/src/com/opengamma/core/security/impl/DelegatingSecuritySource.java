/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.security.impl;

import java.util.Collection;
import java.util.Map;

import com.opengamma.core.change.AggregatingChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdSchemeDelegator;
import com.opengamma.util.ArgumentChecker;

/**
 * A source of securities that uses the scheme of the unique identifier to determine which
 * underlying source should handle the request.
 * <p>
 * If no scheme-specific handler has been registered, a default is used.
 */
public class DelegatingSecuritySource extends UniqueIdSchemeDelegator<SecuritySource> implements SecuritySource {

  /**
   * The change manager
   */
  private final ChangeManager _changeManager;
  
  /**
   * Creates an instance specifying the default delegate.
   * 
   * @param defaultSource  the source to use when no scheme matches, not null
   */
  public DelegatingSecuritySource(SecuritySource defaultSource) {
    super(defaultSource);
    _changeManager = defaultSource.changeManager();
  }

  /**
   * Creates an instance specifying the default delegate.
   * 
   * @param defaultSource  the source to use when no scheme matches, not null
   * @param schemePrefixToSourceMap  the map of sources by scheme to switch on, not null
   */
  public DelegatingSecuritySource(SecuritySource defaultSource, Map<String, SecuritySource> schemePrefixToSourceMap) {
    super(defaultSource, schemePrefixToSourceMap);
    
    // REVIEW jonathan 2011-08-03 -- this assumes that the delegating source lasts for the lifetime of the engine as we
    // never detach from the underlying change managers.
    AggregatingChangeManager changeManager = new AggregatingChangeManager();
    changeManager.addChangeManager(defaultSource.changeManager());
    for (SecuritySource source : schemePrefixToSourceMap.values()) {
      changeManager.addChangeManager(source.changeManager());
    }
    _changeManager = changeManager;
  }

  //-------------------------------------------------------------------------
  @Override
  public Security getSecurity(UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    return chooseDelegate(uniqueId).getSecurity(uniqueId);
  }

  @Override
  public Collection<Security> getSecurities(ExternalIdBundle bundle) {
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
  public Security getSecurity(ExternalIdBundle bundle) {
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

  //-------------------------------------------------------------------------
  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }

}
