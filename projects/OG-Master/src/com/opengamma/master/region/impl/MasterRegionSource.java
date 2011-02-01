/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.region.impl;

import com.opengamma.core.region.RegionSource;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.AbstractMasterSource;
import com.opengamma.master.region.ManageableRegion;
import com.opengamma.master.region.RegionDocument;
import com.opengamma.master.region.RegionMaster;
import com.opengamma.master.region.RegionSearchRequest;
import com.opengamma.util.db.PagingRequest;

/**
 * A {@code RegionSource} implemented using an underlying {@code RegionMaster}.
 * <p>
 * The {@link RegionSource} interface provides regions to the application via a narrow API.
 * This class provides the source on top of a standard {@link RegionMaster}.
 */
public class MasterRegionSource extends AbstractMasterSource<RegionDocument, RegionMaster> implements RegionSource {

  /**
   * Creates an instance with an underlying master which does not override versions.
   * 
   * @param master  the master, not null
   */
  public MasterRegionSource(final RegionMaster master) {
    super(master);
  }

  /**
   * Creates an instance with an underlying master optionally overriding the requested version.
   * 
   * @param master  the master, not null
   * @param versionCorrection  the version-correction locator to search at, null to not override versions
   */
  public MasterRegionSource(final RegionMaster master, VersionCorrection versionCorrection) {
    super(master, versionCorrection);
  }

  //-------------------------------------------------------------------------
  @Override
  public ManageableRegion getRegion(UniqueIdentifier uniqueId) {
    RegionDocument doc = getDocument(uniqueId);
    return (doc != null ? doc.getRegion() : null);
  }

  @Override
  public ManageableRegion getHighestLevelRegion(Identifier regionId) {
    RegionSearchRequest request = new RegionSearchRequest(regionId);
    request.setPagingRequest(PagingRequest.ONE);
    request.setVersionCorrection(getVersionCorrection());
    return getMaster().search(request).getFirstRegion();
  }

  public ManageableRegion getHighestLevelRegion(IdentifierBundle regionIds) {
    RegionSearchRequest request = new RegionSearchRequest(regionIds);
    request.setPagingRequest(PagingRequest.ONE);
    request.setVersionCorrection(getVersionCorrection());
    return getMaster().search(request).getFirstRegion();
  }

}
