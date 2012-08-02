/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.position.impl;

import java.util.List;
import java.util.Map;

import com.opengamma.core.change.AggregatingChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.id.*;
import com.opengamma.master.position.*;

/**
 * A master of positions that uses the scheme of the unique identifier to determine which
 * underlying master should handle the request.
 * <p>
 * If no scheme-specific handler has been registered, a default is used.
 * <p>
 * Change events are aggregated from the different masters and presented through a single change manager.
 */
public class DelegatingPositionMaster extends UniqueIdSchemeDelegator<PositionMaster> implements PositionMaster {

  /**
   * The change manager.
   */
  private final ChangeManager _changeManager;

  /**
   * Creates an instance specifying the default delegate.
   *
   * @param defaultMaster the master to use when no scheme matches, not null
   */
  public DelegatingPositionMaster(PositionMaster defaultMaster) {
    super(defaultMaster);
    _changeManager = defaultMaster.changeManager();
  }

  /**
   * Creates an instance specifying the default delegate.
   *
   * @param defaultMaster the master to use when no scheme matches, not null
   * @param schemePrefixToMasterMap  the map of masters by scheme to switch on, not null
   */
  public DelegatingPositionMaster(PositionMaster defaultMaster, Map<String, PositionMaster> schemePrefixToMasterMap) {
    super(defaultMaster, schemePrefixToMasterMap);
    AggregatingChangeManager changeManager = new AggregatingChangeManager();

    // REVIEW jonathan 2011-08-03 -- this assumes that the delegating master lasts for the lifetime of the engine as we
    // never detach from the underlying change managers.
    changeManager.addChangeManager(defaultMaster.changeManager());
    for (PositionMaster master : schemePrefixToMasterMap.values()) {
      changeManager.addChangeManager(master.changeManager());
    }
    _changeManager = changeManager;
  }

  @Override
  public PositionHistoryResult history(PositionHistoryRequest request) {
    return chooseDelegate(request.getObjectId().getScheme()).history(request);
  }

  @Override
  public PositionSearchResult search(PositionSearchRequest request) {
    List<ObjectId> ids = request.getPositionObjectIds();
    if (ids.size() > 0) {
      return chooseDelegate(ids.get(0).getScheme()).search(request);
    } else {
      return new PositionSearchResult();
    }
  }

  @Override
  public PositionDocument get(UniqueId uniqueId) {
    return chooseDelegate(uniqueId.getScheme()).get(uniqueId);
  }

  @Override
  public PositionDocument get(ObjectIdentifiable objectId, VersionCorrection versionCorrection) {
    return chooseDelegate(objectId.getObjectId().getScheme()).get(objectId, versionCorrection);
  }

  @Override
  public PositionDocument add(PositionDocument document) {
    throw new UnsupportedOperationException("The delegationg position master is read only");
  }

  @Override
  public PositionDocument update(PositionDocument document) {
    throw new UnsupportedOperationException("The delegationg position master is read only");
  }

  @Override
  public void remove(UniqueId uniqueId) {
    throw new UnsupportedOperationException("The delegationg position master is read only");
  }

  @Override
  public PositionDocument correct(PositionDocument document) {
    throw new UnsupportedOperationException("The delegationg position master is read only");
  }

  @Override
  public ManageableTrade getTrade(UniqueId uniqueId) {
    return chooseDelegate(uniqueId.getScheme()).getTrade(uniqueId);
  }

  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }

}
