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
import com.opengamma.core.security.AbstractSecuritySource;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdSchemeDelegator;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * A source of securities that uses the scheme of the unique identifier to determine which underlying source should handle the request.
 * <p>
 * If no scheme-specific handler has been registered, a default is used.
 */
public class DelegatingSecuritySource extends AbstractSecuritySource implements SecuritySource {

  /**
   * The change manager
   */
  private final ChangeManager _changeManager;

  /**
   * The uniqueId scheme delegator.
   */
  private final UniqueIdSchemeDelegator<SecuritySource> _delegator;

  /**
   * Creates an instance specifying the default delegate.
   * 
   * @param defaultSource the source to use when no scheme matches, not null
   */
  public DelegatingSecuritySource(SecuritySource defaultSource) {
    _delegator = new UniqueIdSchemeDelegator<SecuritySource>(defaultSource);
    _changeManager = defaultSource.changeManager();
  }

  /**
   * Creates an instance specifying the default delegate.
   * 
   * @param defaultSource the source to use when no scheme matches, not null
   * @param schemePrefixToSourceMap the map of sources by scheme to switch on, not null
   */
  public DelegatingSecuritySource(SecuritySource defaultSource, Map<String, SecuritySource> schemePrefixToSourceMap) {
    _delegator = new UniqueIdSchemeDelegator<SecuritySource>(defaultSource, schemePrefixToSourceMap);
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
  public Security get(UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    return _delegator.chooseDelegate(uniqueId.getScheme()).get(uniqueId);
  }

  @Override
  public Security get(ObjectId objectId, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    return _delegator.chooseDelegate(objectId.getScheme()).get(objectId, versionCorrection);
  }

  @Override
  public Collection<Security> get(ExternalIdBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");
    // best implementation is to return first matching result
    for (SecuritySource delegateSource : _delegator.getDelegates().values()) {
      Collection<Security> result = delegateSource.get(bundle);
      if (!result.isEmpty()) {
        return result;
      }
    }
    return _delegator.getDefaultDelegate().get(bundle);
  }

  @Override
  public Collection<Security> get(ExternalIdBundle bundle, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(bundle, "bundle");
    // best implementation is to return first matching result
    for (SecuritySource delegateSource : _delegator.getDelegates().values()) {
      Collection<Security> result = delegateSource.get(bundle, versionCorrection);
      if (!result.isEmpty()) {
        return result;
      }
    }
    return _delegator.getDefaultDelegate().get(bundle, versionCorrection);
  }

  @Override
  public Security getSingle(ExternalIdBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");
    // best implementation is to return first matching result
    for (SecuritySource delegateSource : _delegator.getDelegates().values()) {
      Security result = delegateSource.getSingle(bundle);
      if (result != null) {
        return result;
      }
    }
    return _delegator.getDefaultDelegate().getSingle(bundle);
  }

  @Override
  public Security getSingle(ExternalIdBundle bundle, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(bundle, "bundle");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    for (SecuritySource delegateSource : _delegator.getDelegates().values()) {
      Security result = delegateSource.getSingle(bundle, versionCorrection);
      if (result != null) {
        return result;
      }
    }
    return _delegator.getDefaultDelegate().getSingle(bundle, versionCorrection);
  }

  //-------------------------------------------------------------------------
  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }

  /**
   * Gets the delegator.
   * 
   * @return the delegator
   */
  public UniqueIdSchemeDelegator<SecuritySource> getDelegator() {
    return _delegator;
  }

}
