/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.region.impl;

import javax.time.Instant;
import javax.time.InstantProvider;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.region.RegionSource;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.region.ManageableRegion;
import com.opengamma.master.region.RegionMaster;
import com.opengamma.master.region.RegionSearchRequest;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.PagingRequest;

/**
 * A {@code RegionSource} implemented using an underlying {@code RegionMaster}.
 * <p>
 * The {@link RegionSource} interface provides regions to the application via a narrow API.
 * This class provides the source on top of a standard {@link RegionMaster}.
 */
public class MasterRegionSource implements RegionSource {

  /**
   * The underlying master.
   */
  private final RegionMaster _regionMaster;
  /**
   * The instant to search for a version at.
   * Null is treated as the latest version.
   */
  private final Instant _versionAsOfInstant;
  /**
   * The instant to search for corrections for.
   * Null is treated as the latest correction.
   */
  private final Instant _correctedToInstant;

  /**
   * Creates an instance with an underlying region master.
   * 
   * @param regionMaster  the region master, not null
   */
  public MasterRegionSource(final RegionMaster regionMaster) {
    this(regionMaster, null, null);
  }

  /**
   * Creates an instance with an underlying region master viewing the version
   * that existed on the specified instant.
   * 
   * @param regionMaster  the region master, not null
   * @param versionAsOfInstantProvider  the version instant to retrieve, null for latest version
   */
  public MasterRegionSource(final RegionMaster regionMaster, final InstantProvider versionAsOfInstantProvider) {
    this(regionMaster, versionAsOfInstantProvider, null);
  }

  /**
   * Creates an instance with an underlying region master viewing the version
   * that existed on the specified instant as corrected to the correction instant.
   * 
   * @param regionMaster  the region master, not null
   * @param versionAsOfInstantProvider  the version instant to retrieve, null for latest version
   * @param correctedToInstantProvider  the instant that the data should be corrected to, null for latest correction
   */
  public MasterRegionSource(final RegionMaster regionMaster, final InstantProvider versionAsOfInstantProvider, final InstantProvider correctedToInstantProvider) {
    ArgumentChecker.notNull(regionMaster, "regionMaster");
    _regionMaster = regionMaster;
    if (versionAsOfInstantProvider != null) {
      _versionAsOfInstant = Instant.of(versionAsOfInstantProvider);
    } else {
      _versionAsOfInstant = null;
    }
    if (correctedToInstantProvider != null) {
      _correctedToInstant = Instant.of(correctedToInstantProvider);
    } else {
      _correctedToInstant = null;
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the underlying region master.
   * 
   * @return the region master, not null
   */
  public RegionMaster getRegionMaster() {
    return _regionMaster;
  }

  /**
   * Gets the version instant to retrieve.
   * 
   * @return the version instant to retrieve, null for latest version
   */
  public Instant getVersionAsOfInstant() {
    return _versionAsOfInstant;
  }

  /**
   * Gets the instant that the data should be corrected to.
   * 
   * @return the instant that the data should be corrected to, null for latest correction
   */
  public Instant getCorrectedToInstant() {
    return _correctedToInstant;
  }

  //-------------------------------------------------------------------------
  @Override
  public ManageableRegion getRegion(UniqueIdentifier uid) {
    try {
      return getRegionMaster().get(uid).getRegion();
    } catch (DataNotFoundException ex) {
      return null;
    }
  }

  @Override
  public ManageableRegion getHighestLevelRegion(Identifier regionId) {
    RegionSearchRequest request = new RegionSearchRequest(regionId);
    request.setPagingRequest(PagingRequest.ONE);
    request.setVersionAsOfInstant(getVersionAsOfInstant());
    request.setCorrectedToInstant(getCorrectedToInstant());
    return getRegionMaster().search(request).getFirstRegion();
  }

  public ManageableRegion getHighestLevelRegion(IdentifierBundle regionIds) {
    RegionSearchRequest request = new RegionSearchRequest(regionIds);
    request.setPagingRequest(PagingRequest.ONE);
    request.setVersionAsOfInstant(getVersionAsOfInstant());
    request.setCorrectedToInstant(getCorrectedToInstant());
    return getRegionMaster().search(request).getFirstRegion();
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    String str = "MasterRegionSource[" + getRegionMaster();
    if (_versionAsOfInstant != null) {
      str += ",versionAsOf=" + _versionAsOfInstant;
    }
    if (_versionAsOfInstant != null) {
      str += ",correctedTo=" + _correctedToInstant;
    }
    return str + "]";
  }

}
