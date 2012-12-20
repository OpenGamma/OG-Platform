/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.user;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.AbstractChangeProvidingMaster;
import com.opengamma.master.ChangeProvidingDecorator;
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
public class FinancialUserPortfolioMaster extends AbstractFinancialUserMaster<PortfolioDocument> implements PortfolioMaster {

  /**
   * The underlying master.
   */
  private final PortfolioMaster _underlying;
  private final AbstractChangeProvidingMaster<PortfolioDocument> _changeProvidingMaster;

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
    _changeProvidingMaster = ChangeProvidingDecorator.wrap(underlying);
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
    _changeProvidingMaster = ChangeProvidingDecorator.wrap(underlying);
    init();
  }

  public PortfolioDocument add(PortfolioDocument document) {
    return _changeProvidingMaster.add(document);
  }

  public UniqueId addVersion(ObjectIdentifiable objectId, PortfolioDocument documentToAdd) {
    return _changeProvidingMaster.addVersion(objectId, documentToAdd);
  }

  public PortfolioDocument correct(PortfolioDocument document) {
    return _changeProvidingMaster.correct(document);
  }

  @Override
  public PortfolioDocument get(ObjectIdentifiable objectId, VersionCorrection versionCorrection) {
    return _changeProvidingMaster.get(objectId, versionCorrection);
  }

  @Override
  public PortfolioDocument get(UniqueId uniqueId) {
    return _changeProvidingMaster.get(uniqueId);
  }

  public Map<UniqueId, PortfolioDocument> get(Collection<UniqueId> uniqueIds) {
    return _changeProvidingMaster.get(uniqueIds);
  }

  @Override
  public void remove(ObjectIdentifiable objectIdentifiable) {
    _changeProvidingMaster.remove(objectIdentifiable);
  }

  @Override
  public void removeVersion(UniqueId uniqueId) {
    _changeProvidingMaster.removeVersion(uniqueId);
  }

  public List<UniqueId> replaceAllVersions(ObjectIdentifiable objectId, List<PortfolioDocument> replacementDocuments) {
    return _changeProvidingMaster.replaceAllVersions(objectId, replacementDocuments);
  }

  public UniqueId replaceVersion(PortfolioDocument replacementDocument) {
    return _changeProvidingMaster.replaceVersion(replacementDocument);
  }

  public List<UniqueId> replaceVersion(UniqueId uniqueId, List<PortfolioDocument> replacementDocuments) {
    return _changeProvidingMaster.replaceVersion(uniqueId, replacementDocuments);
  }

  public List<UniqueId> replaceVersions(ObjectIdentifiable objectId, List<PortfolioDocument> replacementDocuments) {
    return _changeProvidingMaster.replaceVersions(objectId, replacementDocuments);
  }

  public PortfolioDocument update(PortfolioDocument document) {
    return _changeProvidingMaster.update(document);
  }

  @Override
  public ChangeManager changeManager() {
    return _changeProvidingMaster.changeManager();
  }

  @Override
  public ManageablePortfolioNode getNode(UniqueId nodeId) {
    return _underlying.getNode(nodeId);
  }

  @Override
  public PortfolioHistoryResult history(PortfolioHistoryRequest request) {
    return _underlying.history(request);
  }

  @Override
  public PortfolioSearchResult search(PortfolioSearchRequest request) {
    return _underlying.search(request);
  }
}
