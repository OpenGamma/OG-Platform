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

  PortfolioSearchResult search(PortfolioCapability portfolioCapability, PortfolioSearchRequest request);

  void remove(PortfolioCapability portfolioCapability, ObjectIdentifiable oid);

  PortfolioDocument get(PortfolioCapability portfolioCapability, ObjectIdentifiable objectId, VersionCorrection versionCorrection);

  UniqueId addVersion(PortfolioCapability portfolioCapability, ObjectIdentifiable objectId, PortfolioDocument documentToAdd);

  PortfolioDocument get(PortfolioCapability portfolioCapability, UniqueId uniqueId);

  List<UniqueId> replaceAllVersions(PortfolioCapability portfolioCapability, ObjectIdentifiable objectId, List<PortfolioDocument> replacementDocuments);

  PortfolioDocument update(PortfolioCapability portfolioCapability, PortfolioDocument document);

  PortfolioHistoryResult history(PortfolioCapability portfolioCapability, PortfolioHistoryRequest request);

  PortfolioDocument add(PortfolioCapability portfolioCapability, PortfolioDocument document);

  List<UniqueId> replaceVersions(PortfolioCapability portfolioCapability, ObjectIdentifiable objectId, List<PortfolioDocument> replacementDocuments);

  ManageablePortfolioNode getNode(PortfolioCapability portfolioCapability, UniqueId nodeId);

  Map<UniqueId, PortfolioDocument> get(PortfolioCapability portfolioCapability, Collection<UniqueId> uniqueIds);

  PortfolioDocument correct(PortfolioCapability portfolioCapability, PortfolioDocument document);

  void removeVersion(PortfolioCapability portfolioCapability, UniqueId uniqueId);

  UniqueId replaceVersion(PortfolioCapability portfolioCapability, PortfolioDocument replacementDocument);

  List<UniqueId> replaceVersion(PortfolioCapability portfolioCapability, UniqueId uniqueId, List<PortfolioDocument> replacementDocuments);

}
