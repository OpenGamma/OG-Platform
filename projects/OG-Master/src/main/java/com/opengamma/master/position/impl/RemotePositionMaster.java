/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.position.impl;

import java.net.URI;
import java.util.List;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.impl.AbstractRemoteDocumentMaster;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionHistoryRequest;
import com.opengamma.master.position.PositionHistoryResult;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.position.PositionSearchRequest;
import com.opengamma.master.position.PositionSearchResult;
import com.opengamma.util.ArgumentChecker;
import com.sun.jersey.api.client.GenericType;

/**
 * Provides access to a remote {@link PortfolioMaster}.
 */
public class RemotePositionMaster
    extends AbstractRemoteDocumentMaster<PositionDocument>
    implements PositionMaster {

  /**
   * Creates an instance.
   *
   * @param baseUri  the base target URI for all RESTful web services, not null
   */
  public RemotePositionMaster(final URI baseUri) {
    super(baseUri);
  }

  /**
   * Creates an instance.
   *
   * @param baseUri  the base target URI for all RESTful web services, not null
   * @param changeManager  the change manager, not null
   */
  public RemotePositionMaster(final URI baseUri, ChangeManager changeManager) {
    super(baseUri, changeManager);
  }

  //-------------------------------------------------------------------------
  @Override
  public PositionSearchResult search(final PositionSearchRequest request) {
    ArgumentChecker.notNull(request, "request");

    URI uri = DataPositionMasterResource.uriSearch(getBaseUri());
    return accessRemote(uri).post(PositionSearchResult.class, request);
  }

  //-------------------------------------------------------------------------
  @Override
  public PositionDocument get(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");

    if (uniqueId.isVersioned()) {
      URI uri = (new DataPositionResource()).uriVersion(getBaseUri(), uniqueId);
      return accessRemote(uri).get(PositionDocument.class);
    } else {
      return get(uniqueId, VersionCorrection.LATEST);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public PositionDocument get(final ObjectIdentifiable objectId, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");

    URI uri = (new DataPositionResource()).uri(getBaseUri(), objectId, versionCorrection);
    return accessRemote(uri).get(PositionDocument.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public PositionDocument add(final PositionDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getPosition(), "document.position");

    URI uri = DataPositionMasterResource.uriAdd(getBaseUri());
    return accessRemote(uri).post(PositionDocument.class, document);
  }

  //-------------------------------------------------------------------------
  @Override
  public PositionDocument update(final PositionDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getPosition(), "document.position");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");

    URI uri = (new DataPositionResource()).uri(getBaseUri(), document.getUniqueId(), null);
    return accessRemote(uri).post(PositionDocument.class, document);
  }

  //-------------------------------------------------------------------------
  @Override
  public void remove(final ObjectIdentifiable objectIdentifiable) {
    ArgumentChecker.notNull(objectIdentifiable, "objectIdentifiable");

    URI uri = (new DataPositionResource()).uri(getBaseUri(), objectIdentifiable, null);
    accessRemote(uri).delete();
  }

  //-------------------------------------------------------------------------
  @Override
  public PositionHistoryResult history(final PositionHistoryRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getObjectId(), "request.objectId");

    URI uri = (new DataPositionResource()).uriVersions(getBaseUri(), request.getObjectId(), request);
    return accessRemote(uri).get(PositionHistoryResult.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public PositionDocument correct(final PositionDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getPosition(), "document.position");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");

    URI uri = (new DataPositionResource()).uriVersion(getBaseUri(), document.getUniqueId());
    return accessRemote(uri).post(PositionDocument.class, document);
  }

  //-------------------------------------------------------------------------
  @Override
  public ManageableTrade getTrade(final UniqueId tradeId) {
    ArgumentChecker.notNull(tradeId, "tradeId");

    URI uri = DataTradeResource.uriVersion(getBaseUri(), tradeId);
    return accessRemote(uri).get(ManageableTrade.class);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @Override
  public List<UniqueId> replaceVersion(UniqueId uniqueId, List<PositionDocument> replacementDocuments) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    for (PositionDocument replacementDocument : replacementDocuments) {
      ArgumentChecker.notNull(replacementDocument, "documentToAdd");
      ArgumentChecker.notNull(replacementDocument.getPosition(), "document.position");
    }
    URI uri = (new DataPositionResource()).uriVersion(getBaseUri(), uniqueId);
    return accessRemote(uri).put(new GenericType<List<UniqueId>>() {
    }, replacementDocuments);
  }

  @Override
  public List<UniqueId> replaceAllVersions(ObjectIdentifiable objectId, List<PositionDocument> replacementDocuments) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    for (PositionDocument replacementDocument : replacementDocuments) {
      ArgumentChecker.notNull(replacementDocument, "documentToAdd");
      ArgumentChecker.notNull(replacementDocument.getPosition(), "document.position");
    }
    URI uri = (new DataPositionResource()).uri(getBaseUri(), objectId, null);
    return accessRemote(uri).put(new GenericType<List<UniqueId>>() {
    }, replacementDocuments);
  }

  @Override
  public List<UniqueId> replaceVersions(ObjectIdentifiable objectId, List<PositionDocument> replacementDocuments) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    for (PositionDocument replacementDocument : replacementDocuments) {
      ArgumentChecker.notNull(replacementDocument, "documentToAdd");
      ArgumentChecker.notNull(replacementDocument.getPosition(), "document.position");
    }
    URI uri = (new DataPositionResource()).uri(getBaseUri(), objectId, null);
    return accessRemote(uri).put(new GenericType<List<UniqueId>>() {
    }, replacementDocuments);
  }
}
