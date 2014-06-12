/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.region.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.opengamma.core.AbstractSourceWithExternalBundle;
import com.opengamma.core.region.Region;
import com.opengamma.core.region.RegionSource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.AbstractMasterSource;
import com.opengamma.master.region.ManageableRegion;
import com.opengamma.master.region.RegionDocument;
import com.opengamma.master.region.RegionMaster;
import com.opengamma.master.region.RegionSearchRequest;
import com.opengamma.util.PublicSPI;
import com.opengamma.util.paging.PagingRequest;

/**
 * A {@code RegionSource} implemented using an underlying {@code RegionMaster}.
 * <p>
 * The {@link RegionSource} interface provides regions to the application via a narrow API. This class provides the source on top of a standard {@link RegionMaster}.
 */
@PublicSPI
public class MasterRegionSource extends AbstractMasterSource<Region, RegionDocument, RegionMaster> implements RegionSource {

  /**
   * Creates an instance with an underlying master.
   * 
   * @param master the master, not null
   */
  public MasterRegionSource(final RegionMaster master) {
    super(master);
  }

  //-------------------------------------------------------------------------
  @SuppressWarnings({"unchecked", "rawtypes" })
  @Override
  public Collection<Region> get(ExternalIdBundle bundle, VersionCorrection versionCorrection) {
    RegionSearchRequest request = new RegionSearchRequest(bundle);
    request.setVersionCorrection(versionCorrection);
    return (List) getMaster().search(request).getRegions();
  }

  @Override
  public ManageableRegion getHighestLevelRegion(ExternalId regionId) {
    RegionSearchRequest request = new RegionSearchRequest(regionId);
    request.setPagingRequest(PagingRequest.ONE);
    return getMaster().search(request).getFirstRegion();
  }

  @Override
  public ManageableRegion getHighestLevelRegion(ExternalIdBundle regionIds) {
    RegionSearchRequest request = new RegionSearchRequest(regionIds);
    request.setPagingRequest(PagingRequest.ONE);
    return getMaster().search(request).getFirstRegion();
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

}
