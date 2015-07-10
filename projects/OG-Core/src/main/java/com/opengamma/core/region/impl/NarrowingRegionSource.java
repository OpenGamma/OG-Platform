/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.region.impl;

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Ordering;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.region.Region;
import com.opengamma.core.region.RegionSource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * A region source that delegates to an another source, but which ensures that
 * it only calls the get methods on the delegate. This is intended to allow
 * the use of proxy classes as the delegates which allows different
 * behaviours e.g. capturing the data returned from sources.
 */
public class NarrowingRegionSource implements RegionSource {

  private final RegionSource _delegate;

  /**
   * Create a narrowing source, wrapping the provided source.
   *
   * @param delegate the source to delegate to, not null
   */
  public NarrowingRegionSource(RegionSource delegate) {
    _delegate = ArgumentChecker.notNull(delegate, "delegate");
  }

  @Override
  public Region getHighestLevelRegion(ExternalId externalId) {
    return getHighestLevelRegion(externalId.toBundle());
  }

  @Override
  public Region getHighestLevelRegion(ExternalIdBundle bundle) {
    Collection<Region> regions = get(bundle, VersionCorrection.LATEST);
    return regions.isEmpty() ? null : Ordering.from(RegionComparator.ASC).min(regions);
  }

  @Override
  public Collection<Region> get(ExternalIdBundle bundle, VersionCorrection versionCorrection) {
    return _delegate.get(bundle, versionCorrection);
  }

  @Override
  public Map<ExternalIdBundle, Collection<Region>> getAll(Collection<ExternalIdBundle> bundles,
                                                          VersionCorrection versionCorrection) {

    ImmutableMap.Builder<ExternalIdBundle, Collection<Region>> builder = ImmutableMap.builder();
    for (ExternalIdBundle bundle : bundles) {
      Collection<Region> regions = get(bundle, versionCorrection);
      if (!regions.isEmpty()) {
        builder.put(bundle, regions);
      }
    }
    return builder.build();
  }

  @Override
  public Collection<Region> get(ExternalIdBundle bundle) {
    return _delegate.get(bundle);
  }

  @Override
  public Region getSingle(ExternalIdBundle bundle) {
    return getSingle(bundle, VersionCorrection.LATEST);
  }

  @Override
  public Region getSingle(ExternalIdBundle bundle, VersionCorrection versionCorrection) {
    Collection<Region> regions = get(bundle, versionCorrection);
    return regions.isEmpty() ? null : regions.iterator().next();
  }

  @Override
  public Map<ExternalIdBundle, Region> getSingle(Collection<ExternalIdBundle> bundles,
                                                 VersionCorrection versionCorrection) {

    ImmutableMap.Builder<ExternalIdBundle, Region> builder = ImmutableMap.builder();
    for (ExternalIdBundle bundle : bundles) {
      Region region = getSingle(bundle, versionCorrection);
      if (region != null) {
        builder.put(bundle, region);
      }
    }
    return builder.build();
  }

  @Override
  public ChangeManager changeManager() {
    return _delegate.changeManager();
  }

  @Override
  public Region get(UniqueId uniqueId) {
    return _delegate.get(uniqueId);
  }

  @Override
  public Region get(ObjectId objectId, VersionCorrection versionCorrection) {
    return _delegate.get(objectId, versionCorrection);
  }

  @Override
  public Map<UniqueId, Region> get(Collection<UniqueId> uniqueIds) {
    return _delegate.get(uniqueIds);
  }

  @Override
  public Map<ObjectId, Region> get(Collection<ObjectId> objectIds, VersionCorrection versionCorrection) {
    return _delegate.get(objectIds, versionCorrection);
  }
}
