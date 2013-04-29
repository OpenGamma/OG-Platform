/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.region.impl;

import java.net.URI;
import java.util.Collection;
import java.util.Map;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.AbstractRemoteSource;
import com.opengamma.core.AbstractSourceWithExternalBundle;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.DummyChangeManager;
import com.opengamma.core.region.Region;
import com.opengamma.core.region.RegionSource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.FudgeListWrapper;
import com.opengamma.util.rest.UniformInterfaceException404NotFound;

/**
 * Provides remote access to an {@link RegionSource}.
 */
public class RemoteRegionSource extends AbstractRemoteSource<Region> implements RegionSource {

  private final ChangeManager _changeManager;

  /**
   * Creates an instance.
   * 
   * @param baseUri the base target URI for all RESTful web services, not null
   */
  public RemoteRegionSource(final URI baseUri) {
    this(baseUri, DummyChangeManager.INSTANCE);
  }

  public RemoteRegionSource(final URI baseUri, final ChangeManager changeManager) {
    super(baseUri);
    ArgumentChecker.notNull(changeManager, "changeManager");
    _changeManager = changeManager;
  }

  //-------------------------------------------------------------------------
  @Override
  public Region get(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");

    URI uri = DataRegionSourceResource.uriGet(getBaseUri(), uniqueId);
    return accessRemote(uri).get(Region.class);
  }

  @Override
  public Region get(final ObjectId objectId, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");

    URI uri = DataRegionSourceResource.uriGet(getBaseUri(), objectId, versionCorrection);
    return accessRemote(uri).get(Region.class);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Collection<Region> get(final ExternalIdBundle bundle, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(bundle, "bundle");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");

    URI uri = DataRegionSourceResource.uriSearch(getBaseUri(), versionCorrection, bundle);
    return accessRemote(uri).get(FudgeListWrapper.class).getList();
  }

  //-------------------------------------------------------------------------
  @Override
  public Region getHighestLevelRegion(ExternalId externalId) {
    try {
      return getHighestLevelRegion(ExternalIdBundle.of(externalId));
    } catch (DataNotFoundException ex) {
      return null;
    } catch (UniformInterfaceException404NotFound ex) {
      return null;
    }
  }

  @Override
  public Region getHighestLevelRegion(ExternalIdBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");

    try {
      URI uri = DataRegionSourceResource.uriSearchHighest(getBaseUri(), bundle);
      return accessRemote(uri).get(Region.class);
    } catch (DataNotFoundException ex) {
      return null;
    } catch (UniformInterfaceException404NotFound ex) {
      return null;
    }
  }

  @Override
  public Map<ExternalIdBundle, Collection<Region>> getAll(Collection<ExternalIdBundle> bundles, VersionCorrection versionCorrection) {
    return AbstractSourceWithExternalBundle.getAll(this, bundles, versionCorrection);
  }

  @Override
  public Collection<Region> get(ExternalIdBundle bundle) {
    return AbstractSourceWithExternalBundle.get(this, bundle);
  }

  @Override
  public Region getSingle(ExternalIdBundle bundle) {
    return AbstractSourceWithExternalBundle.getSingle(this, bundle);
  }

  @Override
  public Region getSingle(ExternalIdBundle bundle, VersionCorrection versionCorrection) {
    return AbstractSourceWithExternalBundle.getSingle(this, bundle, versionCorrection);
  }

  @Override
  public Map<ExternalIdBundle, Region> getSingle(Collection<ExternalIdBundle> bundles, VersionCorrection versionCorrection) {
    return AbstractSourceWithExternalBundle.getSingle(this, bundles, versionCorrection);
  }

  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }

}
