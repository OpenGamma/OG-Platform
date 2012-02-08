/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.user;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionHistoryRequest;
import com.opengamma.master.position.PositionHistoryResult;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.position.PositionSearchRequest;
import com.opengamma.master.position.PositionSearchResult;

/**
 * Wraps a position master to trap calls to record user based information to allow clean up and
 * hooks for access control logics if needed.
 */
public class FinancialUserPositionMaster extends AbstractFinancialUserService implements PositionMaster {

  /**
   * The underlying master.
   */
  private final PositionMaster _underlying;

  /**
   * Creates an instance.
   * 
   * @param client  the client, not null
   * @param underlying  the underlying master, not null
   */
  public FinancialUserPositionMaster(FinancialClient client, PositionMaster underlying) {
    super(client, FinancialUserDataType.POSITION);
    _underlying = underlying;
  }

  //-------------------------------------------------------------------------
  @Override
  public PositionSearchResult search(PositionSearchRequest request) {
    return _underlying.search(request);
  }

  @Override
  public PositionDocument get(UniqueId uniqueId) {
    return _underlying.get(uniqueId);
  }

  @Override
  public PositionDocument get(ObjectIdentifiable objectId, VersionCorrection versionCorrection) {
    return _underlying.get(objectId, versionCorrection);
  }

  @Override
  public PositionDocument add(PositionDocument document) {
    document = _underlying.add(document);
    if (document.getUniqueId() != null) {
      created(document.getUniqueId());
    }
    return document;
  }

  @Override
  public PositionDocument update(PositionDocument document) {
    return _underlying.update(document);
  }

  @Override
  public void remove(UniqueId uniqueId) {
    _underlying.remove(uniqueId);
    deleted(uniqueId);
  }

  @Override
  public PositionHistoryResult history(PositionHistoryRequest request) {
    return _underlying.history(request);
  }

  @Override
  public PositionDocument correct(PositionDocument document) {
    return _underlying.correct(document);
  }

  @Override
  public ManageableTrade getTrade(UniqueId uniqueId) {
    return _underlying.getTrade(uniqueId);
  }

  //-------------------------------------------------------------------------
  @Override
  public ChangeManager changeManager() {
    return _underlying.changeManager();
  }

}
