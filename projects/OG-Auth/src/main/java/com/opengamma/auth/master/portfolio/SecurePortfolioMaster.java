/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * <p/>
 * Please see distribution for license.
 */
package com.opengamma.auth.master.portfolio;

import com.opengamma.core.change.ChangeProvider;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioHistoryRequest;
import com.opengamma.master.portfolio.PortfolioHistoryResult;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;

import java.util.Collection;
import java.util.List;
import java.util.Map;


public interface SecurePortfolioMaster extends ChangeProvider {

  public PortfolioSearchResult search(PortfolioCapability PortfolioCapability, PortfolioSearchRequest request);

  public void remove(PortfolioCapability PortfolioCapability, ObjectIdentifiable oid);

  public PortfolioDocument get(PortfolioCapability PortfolioCapability, ObjectIdentifiable objectId, VersionCorrection versionCorrection);

  public UniqueId addVersion(PortfolioCapability PortfolioCapability, ObjectIdentifiable objectId, PortfolioDocument documentToAdd);

  public PortfolioDocument get(PortfolioCapability PortfolioCapability, UniqueId uniqueId);

  public List<UniqueId> replaceAllVersions(PortfolioCapability PortfolioCapability, ObjectIdentifiable objectId, List<PortfolioDocument> replacementDocuments);

  public PortfolioDocument update(PortfolioCapability PortfolioCapability, PortfolioDocument document);

  public PortfolioHistoryResult history(PortfolioCapability PortfolioCapability, PortfolioHistoryRequest request);

  public PortfolioDocument add(PortfolioCapability PortfolioCapability, PortfolioDocument document);

  public List<UniqueId> replaceVersions(PortfolioCapability PortfolioCapability, ObjectIdentifiable objectId, List<PortfolioDocument> replacementDocuments);

  public ManageablePortfolioNode getNode(PortfolioCapability PortfolioCapability, UniqueId nodeId);

  public Map<UniqueId, PortfolioDocument> get(PortfolioCapability PortfolioCapability, Collection<UniqueId> uniqueIds);

  public PortfolioDocument correct(PortfolioCapability PortfolioCapability, PortfolioDocument document);

  public void removeVersion(PortfolioCapability PortfolioCapability, UniqueId uniqueId);

  public UniqueId replaceVersion(PortfolioCapability PortfolioCapability, PortfolioDocument replacementDocument);

  public List<UniqueId> replaceVersion(PortfolioCapability PortfolioCapability, UniqueId uniqueId, List<PortfolioDocument> replacementDocuments);

}
