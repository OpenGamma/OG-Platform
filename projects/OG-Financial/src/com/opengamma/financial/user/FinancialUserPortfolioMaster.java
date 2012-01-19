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
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioHistoryRequest;
import com.opengamma.master.portfolio.PortfolioHistoryResult;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;

/**
 * Wraps a portfolio master to trap calls to record user based information to allow clean up and
 * hooks for access control logics if needed.
 */
public class FinancialUserPortfolioMaster extends AbstractFinancialUserService implements PortfolioMaster {

  /**
   * The underlying master.
   */
  private final PortfolioMaster _underlying;

  /**
   * Creates an instance.
   * 
   * @param userName  the user name, not null
   * @param clientName  the client name, not null
   * @param tracker  the tracker, not null
   * @param underlying  the underlying master, not null
   */
  public FinancialUserPortfolioMaster(String userName, String clientName, FinancialUserDataTracker tracker, PortfolioMaster underlying) {
    super(userName, clientName, tracker, FinancialUserDataType.PORTFOLIO);
    _underlying = underlying;
  }

  /**
   * Creates an instance.
   * 
   * @param client  the client, not null
   * @param underlying  the underlying master, not null
   */
  public FinancialUserPortfolioMaster(FinancialClient client, PortfolioMaster underlying) {
    super(client, FinancialUserDataType.PORTFOLIO);
    _underlying = underlying;
  }

  //-------------------------------------------------------------------------
  @Override
  public PortfolioSearchResult search(PortfolioSearchRequest request) {
    return _underlying.search(request);
  }

  @Override
  public PortfolioDocument get(UniqueId uniqueId) {
    return _underlying.get(uniqueId);
  }

  @Override
  public PortfolioDocument get(ObjectIdentifiable objectId, VersionCorrection versionCorrection) {
    return _underlying.get(objectId, versionCorrection);
  }

  @Override
  public PortfolioDocument add(PortfolioDocument document) {
    document = _underlying.add(document);
    if (document.getUniqueId() != null) {
      created(document.getUniqueId());
    }
    return document;
  }

  @Override
  public PortfolioDocument update(PortfolioDocument document) {
    return _underlying.update(document);
  }

  @Override
  public void remove(UniqueId uniqueId) {
    _underlying.remove(uniqueId);
    deleted(uniqueId);
  }

  @Override
  public PortfolioHistoryResult history(PortfolioHistoryRequest request) {
    return _underlying.history(request);
  }

  @Override
  public PortfolioDocument correct(PortfolioDocument document) {
    return _underlying.correct(document);
  }

  @Override
  public ManageablePortfolioNode getNode(UniqueId nodeId) {
    return _underlying.getNode(nodeId);
  }

  //-------------------------------------------------------------------------
  @Override
  public ChangeManager changeManager() {
    return _underlying.changeManager();
  }

}
