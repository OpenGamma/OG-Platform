/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.security.impl;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.opengamma.DataNotFoundException;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.AbstractRemoteSource;
import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.security.AbstractSecuritySource;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.FudgeListWrapper;
import com.opengamma.util.rest.UniformInterfaceException404NotFound;

/**
 * Provides remote access to an {@link SecuritySource}.
 */
public class RemoteSecuritySource extends AbstractRemoteSource<Security> implements SecuritySource {

  /**
   * The change manager.
   */
  private final ChangeManager _changeManager;

  /**
   * Creates an instance.
   * 
   * @param baseUri the base target URI for all RESTful web services, not null
   */
  public RemoteSecuritySource(final URI baseUri) {
    this(baseUri, new BasicChangeManager());
  }

  /**
   * Creates an instance.
   * 
   * @param baseUri the base target URI for all RESTful web services, not null
   * @param changeManager the change manager, not null
   */
  public RemoteSecuritySource(final URI baseUri, final ChangeManager changeManager) {
    super(baseUri);
    ArgumentChecker.notNull(changeManager, "changeManager");
    _changeManager = changeManager;
  }

  //-------------------------------------------------------------------------
  @Override
  public Security get(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");

    URI uri = DataSecuritySourceResource.uriGet(getBaseUri(), uniqueId);
    return accessRemote(uri).get(Security.class);
  }

  @Override
  public Security get(final ObjectId objectId, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");

    URI uri = DataSecuritySourceResource.uriGet(getBaseUri(), objectId, versionCorrection);
    return accessRemote(uri).get(Security.class);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Collection<Security> get(final ExternalIdBundle bundle, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(bundle, "bundle");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");

    URI uri = DataSecuritySourceResource.uriSearch(getBaseUri(), versionCorrection, bundle);
    return accessRemote(uri).get(FudgeListWrapper.class).getList();
  }

  @Override
  public Map<ExternalIdBundle, Collection<Security>> getAll(final Collection<ExternalIdBundle> bundles, final VersionCorrection versionCorrection) {
    // TODO: Implement this properly as a REST call
    return AbstractSecuritySource.getAll(this, bundles, versionCorrection);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Map<UniqueId, Security> get(final Collection<UniqueId> uniqueIds) {
    ArgumentChecker.notNull(uniqueIds, "uniqueIds");

    URI uri = DataSecuritySourceResource.uriBulk(getBaseUri(), uniqueIds);
    List<Security> list = accessRemote(uri).get(FudgeListWrapper.class).getList();
    Map<UniqueId, Security> result = Maps.newHashMap();
    for (Security security : list) {
      result.put(security.getUniqueId(), security);
    }
    return result;
  }

  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }

  //-------------------------------------------------------------------------
  @SuppressWarnings("unchecked")
  @Override
  public Collection<Security> get(final ExternalIdBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");

    URI uri = DataSecuritySourceResource.uriSearchList(getBaseUri(), bundle);
    return accessRemote(uri).get(FudgeListWrapper.class).getList();
  }

  @Override
  public Security getSingle(final ExternalIdBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");

    try {
      URI uri = DataSecuritySourceResource.uriSearchSingle(getBaseUri(), bundle, null);
      return accessRemote(uri).get(Security.class);
    } catch (DataNotFoundException ex) {
      return null;
    } catch (UniformInterfaceException404NotFound ex) {
      return null;
    }
  }

  @Override
  public Security getSingle(final ExternalIdBundle bundle, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(bundle, "bundle");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");

    try {
      URI uri = DataSecuritySourceResource.uriSearchSingle(getBaseUri(), bundle, versionCorrection);
      return accessRemote(uri).get(Security.class);
    } catch (UniformInterfaceException404NotFound ex) {
      throw new OpenGammaRuntimeException("Unable to access remote source ", ex);
    }
  }

  @Override
  public Map<ExternalIdBundle, Security> getSingle(final Collection<ExternalIdBundle> bundles, final VersionCorrection versionCorrection) {
    // TODO: Implement this properly as a REST call
    return AbstractSecuritySource.getSingle(this, bundles, versionCorrection);
  }

}
