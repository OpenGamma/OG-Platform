/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.exchange.impl;

import java.net.URI;
import java.util.List;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.exchange.ExchangeDocument;
import com.opengamma.master.exchange.ExchangeHistoryRequest;
import com.opengamma.master.exchange.ExchangeHistoryResult;
import com.opengamma.master.exchange.ExchangeMaster;
import com.opengamma.master.exchange.ExchangeSearchRequest;
import com.opengamma.master.exchange.ExchangeSearchResult;
import com.opengamma.master.impl.AbstractRemoteDocumentMaster;
import com.opengamma.util.ArgumentChecker;
import com.sun.jersey.api.client.GenericType;

/**
 * Provides access to a remote {@link ExchangeMaster}.
 */
public class RemoteExchangeMaster
    extends AbstractRemoteDocumentMaster<ExchangeDocument>
    implements ExchangeMaster {

  /**
   * Creates an instance.
   *
   * @param baseUri  the base target URI for all RESTful web services, not null
   */
  public RemoteExchangeMaster(final URI baseUri) {
    super(baseUri);
  }

  /**
   * Creates an instance.
   *
   * @param baseUri  the base target URI for all RESTful web services, not null
   * @param changeManager  the change manager, not null
   */
  public RemoteExchangeMaster(final URI baseUri, ChangeManager changeManager) {
    super(baseUri, changeManager);
  }

  //-------------------------------------------------------------------------
  @Override
  public ExchangeSearchResult search(final ExchangeSearchRequest request) {
    ArgumentChecker.notNull(request, "request");

    URI uri = DataExchangeMasterResource.uriSearch(getBaseUri());
    return accessRemote(uri).post(ExchangeSearchResult.class, request);
  }

  //-------------------------------------------------------------------------
  @Override
  public ExchangeDocument get(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");

    if (uniqueId.isVersioned()) {
      URI uri = (new DataExchangeResource()).uriVersion(getBaseUri(), uniqueId);
      return accessRemote(uri).get(ExchangeDocument.class);
    } else {
      return get(uniqueId, VersionCorrection.LATEST);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public ExchangeDocument get(final ObjectIdentifiable objectId, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");

    URI uri = (new DataExchangeResource()).uri(getBaseUri(), objectId, versionCorrection);
    return accessRemote(uri).get(ExchangeDocument.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public ExchangeDocument add(final ExchangeDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getExchange(), "document.exchange");

    URI uri = DataExchangeMasterResource.uriAdd(getBaseUri());
    return accessRemote(uri).post(ExchangeDocument.class, document);
  }

  //-------------------------------------------------------------------------
  @Override
  public ExchangeDocument update(final ExchangeDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getExchange(), "document.exchange");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");

    URI uri = (new DataExchangeResource()).uri(getBaseUri(), document.getUniqueId(), null);
    return accessRemote(uri).post(ExchangeDocument.class, document);
  }

  //-------------------------------------------------------------------------
  @Override
  public void remove(final ObjectIdentifiable objectIdentifiable) {
    ArgumentChecker.notNull(objectIdentifiable, "objectIdentifiable");

    URI uri = (new DataExchangeResource()).uri(getBaseUri(), objectIdentifiable, null);
    accessRemote(uri).delete();
  }

  //-------------------------------------------------------------------------
  @Override
  public ExchangeHistoryResult history(final ExchangeHistoryRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getObjectId(), "request.objectId");

    URI uri = (new DataExchangeResource()).uriVersions(getBaseUri(), request.getObjectId(), request);
    return accessRemote(uri).get(ExchangeHistoryResult.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public ExchangeDocument correct(final ExchangeDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getExchange(), "document.exchange");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");

    URI uri = (new DataExchangeResource()).uriVersion(getBaseUri(), document.getUniqueId());
    return accessRemote(uri).post(ExchangeDocument.class, document);
  }

  @Override
  public List<UniqueId> replaceVersion(UniqueId uniqueId, List<ExchangeDocument> replacementDocuments) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    for (ExchangeDocument replacementDocument : replacementDocuments) {
      ArgumentChecker.notNull(replacementDocument, "replacementDocument");
      ArgumentChecker.notNull(replacementDocument.getExchange(), "replacementDocument.exchange");
    }

    URI uri = (new DataExchangeResource()).uriVersion(getBaseUri(), uniqueId);
    return accessRemote(uri).put(new GenericType<List<UniqueId>>() {
    }, replacementDocuments);
  }

  @Override
  public List<UniqueId> replaceAllVersions(ObjectIdentifiable objectId, List<ExchangeDocument> replacementDocuments) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    for (ExchangeDocument replacementDocument : replacementDocuments) {
      ArgumentChecker.notNull(replacementDocument, "replacementDocument");
      ArgumentChecker.notNull(replacementDocument.getExchange(), "replacementDocument.exchange");
    }
    URI uri = (new DataExchangeResource()).uriAll(getBaseUri(), objectId, null);
    return accessRemote(uri).put(new GenericType<List<UniqueId>>() {
    }, replacementDocuments);
  }

  @Override
  public List<UniqueId> replaceVersions(ObjectIdentifiable objectId, List<ExchangeDocument> replacementDocuments) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    for (ExchangeDocument replacementDocument : replacementDocuments) {
      ArgumentChecker.notNull(replacementDocument, "replacementDocument");
      ArgumentChecker.notNull(replacementDocument.getExchange(), "replacementDocument.exchange");
    }
    URI uri = (new DataExchangeResource()).uri(getBaseUri(), objectId, null);
    return accessRemote(uri).put(new GenericType<List<UniqueId>>() {
    }, replacementDocuments);
  }
}
