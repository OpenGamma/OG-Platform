/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.user;

import java.util.List;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.portfolio.*;

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

  @Override
  public UniqueId addVersion(ObjectIdentifiable objectId, PortfolioDocument documentToAdd) {
    documentToAdd = _underlying.add(documentToAdd);
    if (documentToAdd.getUniqueId() != null) {
      created(documentToAdd.getUniqueId());
    }
    return documentToAdd.getUniqueId();
  }

  @Override
  public List<UniqueId> replaceVersion(UniqueId uniqueId, List<PortfolioDocument> replacementDocuments) {
    return _underlying.replaceVersion(uniqueId, replacementDocuments);
  }

  @Override
  public List<UniqueId> replaceAllVersions(ObjectIdentifiable objectId, List<PortfolioDocument> replacementDocuments) {
    return _underlying.replaceAllVersions(objectId, replacementDocuments);
  }

  @Override
  public List<UniqueId> replaceVersions(ObjectIdentifiable objectId, List<PortfolioDocument> replacementDocuments) {
    return _underlying.replaceVersions(objectId, replacementDocuments);
  }

  @Override
  public UniqueId replaceVersion(PortfolioDocument replacementDocument) {
    return _underlying.replaceVersion(replacementDocument);
  }

  @Override
  public void removeVersion(UniqueId uniqueId) {
    _underlying.removeVersion(uniqueId);
    deleted(uniqueId);
  }
}
