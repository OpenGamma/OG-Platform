/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.auth.master.portfolio.rest;

import com.opengamma.auth.master.portfolio.PortfolioCapability;
import com.opengamma.auth.master.portfolio.SecurePortfolioMaster;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.impl.AbstractRemoteMaster;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioHistoryRequest;
import com.opengamma.master.portfolio.PortfolioHistoryResult;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.FudgeRestClient;
import com.opengamma.util.rest.RestUtils;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterface;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

/**
 * Provides access to a remote {@link com.opengamma.master.portfolio.PortfolioMaster}.
 */
public class RemoteSecurePortfolioMaster extends AbstractRemoteMaster implements SecurePortfolioMaster {

  /**
   * Creates an instance.
   *
   * @param baseUri the base target URI for all RESTful web services, not null
   */
  public RemoteSecurePortfolioMaster(final URI baseUri) {
    super(baseUri);
  }

  /**
   * Creates an instance.
   *
   * @param baseUri the base target URI for all RESTful web services, not null
   */
  public RemoteSecurePortfolioMaster(final URI baseUri, final FudgeRestClient client) {
    super(baseUri, client);
  }

  /**
   * Creates an instance.
   *
   * @param baseUri       the base target URI for all RESTful web services, not null
   * @param changeManager the change manager, not null
   */
  public RemoteSecurePortfolioMaster(final URI baseUri, ChangeManager changeManager) {
    super(baseUri, changeManager);
  }

  /**
   * Accesses the remote master.
   *
   * @param uri the URI to call, not null
   * @return the resource, suitable for calling get/post/put/delete on, not null
   */
  protected UniformInterface accessRemote(PortfolioCapability portfolioCapability, URI uri) {
    FudgeRestClient client = getRestClient();
    return client.accessFudge(uri).header("Capability", RestUtils.encodeBase64(portfolioCapability));
  }


  @Override
  public PortfolioSearchResult search(PortfolioCapability portfolioCapability, PortfolioSearchRequest request) {
    ArgumentChecker.notNull(request, "request");

    URI uri = DataSecurePortfolioMasterResource.uriSearch(getBaseUri());
    return accessRemote(portfolioCapability, uri).post(PortfolioSearchResult.class, request);
  }

  @Override
  public void remove(PortfolioCapability portfolioCapability, ObjectIdentifiable objectIdentifiable) {
    ArgumentChecker.notNull(objectIdentifiable, "objectIdentifiable");

    URI uri = (new DataSecurePortfolioResource()).uri(getBaseUri(), objectIdentifiable, null);
    accessRemote(portfolioCapability, uri).delete();
  }

  @Override
  public PortfolioDocument get(PortfolioCapability portfolioCapability, ObjectIdentifiable objectId, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");

    URI uri = (new DataSecurePortfolioResource()).uri(getBaseUri(), objectId, versionCorrection);
    return accessRemote(portfolioCapability, uri).get(PortfolioDocument.class);
  }

  @Override
  public UniqueId addVersion(PortfolioCapability portfolioCapability, ObjectIdentifiable objectId, PortfolioDocument documentToAdd) {
    List<UniqueId> result = replaceVersions(portfolioCapability, objectId, Collections.singletonList(documentToAdd));
    if (result.isEmpty()) {
      return null;
    } else {
      return result.get(0);
    }
  }

  @Override
  public PortfolioDocument get(PortfolioCapability portfolioCapability, UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");

    if (uniqueId.isVersioned()) {
      URI uri = (new DataSecurePortfolioResource()).uriVersion(getBaseUri(), uniqueId);
      return accessRemote(portfolioCapability, uri).get(PortfolioDocument.class);
    } else {
      return get(portfolioCapability, uniqueId, VersionCorrection.LATEST);
    }
  }

  @Override
  public List<UniqueId> replaceVersion(PortfolioCapability portfolioCapability, UniqueId uniqueId, List<PortfolioDocument> replacementDocuments) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    for (PortfolioDocument replacementDocument : replacementDocuments) {
      ArgumentChecker.notNull(replacementDocument, "documentToAdd");
      ArgumentChecker.notNull(replacementDocument.getPortfolio(), "document.portfolio");
      ArgumentChecker.notNull(replacementDocument.getPortfolio().getRootNode(), "document.portfolio.rootNode");
    }
    URI uri = (new DataSecurePortfolioResource()).uriVersion(getBaseUri(), uniqueId);
    return accessRemote(portfolioCapability, uri).put(new GenericType<List<UniqueId>>() {
    }, replacementDocuments);
  }

  @Override
  public List<UniqueId> replaceAllVersions(PortfolioCapability portfolioCapability, ObjectIdentifiable objectId, List<PortfolioDocument> replacementDocuments) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    for (PortfolioDocument replacementDocument : replacementDocuments) {
      ArgumentChecker.notNull(replacementDocument, "documentToAdd");
      ArgumentChecker.notNull(replacementDocument.getPortfolio(), "document.portfolio");
      ArgumentChecker.notNull(replacementDocument.getPortfolio().getRootNode(), "document.portfolio.rootNode");
    }
    URI uri = (new DataSecurePortfolioResource()).uriAll(getBaseUri(), objectId, null);
    return accessRemote(portfolioCapability, uri).put(new GenericType<List<UniqueId>>() {
    }, replacementDocuments);
  }

  @Override
  public List<UniqueId> replaceVersions(PortfolioCapability portfolioCapability, ObjectIdentifiable objectId, List<PortfolioDocument> replacementDocuments) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    for (PortfolioDocument replacementDocument : replacementDocuments) {
      ArgumentChecker.notNull(replacementDocument, "documentToAdd");
      ArgumentChecker.notNull(replacementDocument.getPortfolio(), "document.portfolio");
      ArgumentChecker.notNull(replacementDocument.getPortfolio().getRootNode(), "document.portfolio.rootNode");
    }
    URI uri = (new DataSecurePortfolioResource()).uri(getBaseUri(), objectId, null);
    return accessRemote(portfolioCapability, uri).put(new GenericType<List<UniqueId>>() {
    }, replacementDocuments);
  }


  @Override
  public PortfolioDocument update(PortfolioCapability portfolioCapability, PortfolioDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getPortfolio(), "document.portfolio");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");

    URI uri = (new DataSecurePortfolioResource()).uri(getBaseUri(), document.getUniqueId(), null);
    return accessRemote(portfolioCapability, uri).post(PortfolioDocument.class, document);
  }

  @Override
  public PortfolioHistoryResult history(PortfolioCapability portfolioCapability, PortfolioHistoryRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getObjectId(), "request.objectId");

    URI uri = (new DataSecurePortfolioResource()).uriVersions(getBaseUri(), request.getObjectId(), request);
    return accessRemote(portfolioCapability, uri).get(PortfolioHistoryResult.class);
  }

  @Override
  public PortfolioDocument add(PortfolioCapability portfolioCapability, PortfolioDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getPortfolio(), "document.portfolio");
    ArgumentChecker.notNull(document.getPortfolio().getRootNode(), "document.portfolio.rootNode");

    URI uri = DataSecurePortfolioMasterResource.uriAdd(getBaseUri());
    return accessRemote(portfolioCapability, uri).post(PortfolioDocument.class, document);
  }


  @Override
  public ManageablePortfolioNode getNode(PortfolioCapability portfolioCapability, UniqueId nodeId) {
    ArgumentChecker.notNull(nodeId, "nodeId");

    URI uri = DataSecurePortfolioNodeResource.uri(getBaseUri(), nodeId);
    return accessRemote(portfolioCapability, uri).get(ManageablePortfolioNode.class);
  }

  @Override
  public Map<UniqueId, PortfolioDocument> get(PortfolioCapability portfolioCapability, Collection<UniqueId> uniqueIds) {
    Map<UniqueId, PortfolioDocument> resultMap = newHashMap();
    for (UniqueId uniqueId : uniqueIds) {
      PortfolioDocument doc = get(portfolioCapability, uniqueId);
      resultMap.put(uniqueId, doc);
    }
    return resultMap;
  }

  @Override
  public PortfolioDocument correct(PortfolioCapability portfolioCapability, PortfolioDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getPortfolio(), "document.portfolio");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");

    URI uri = (new DataSecurePortfolioResource()).uriVersion(getBaseUri(), document.getUniqueId());
    return accessRemote(portfolioCapability, uri).post(PortfolioDocument.class, document);
  }

  @Override
  public void removeVersion(PortfolioCapability portfolioCapability, UniqueId uniqueId) {
    replaceVersion(portfolioCapability, uniqueId, Collections.<PortfolioDocument>emptyList());
  }

  @Override
  public UniqueId replaceVersion(PortfolioCapability portfolioCapability, PortfolioDocument replacementDocument) {
    List<UniqueId> result = replaceVersion(portfolioCapability, replacementDocument.getUniqueId(), Collections.singletonList(replacementDocument));
    if (result.isEmpty()) {
      return null;
    } else {
      return result.get(0);
    }
  }

}
