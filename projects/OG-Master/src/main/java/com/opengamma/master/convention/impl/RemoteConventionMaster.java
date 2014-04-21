/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.convention.impl;

import java.net.URI;
import java.util.List;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.convention.ConventionDocument;
import com.opengamma.master.convention.ConventionHistoryRequest;
import com.opengamma.master.convention.ConventionHistoryResult;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.master.convention.ConventionMetaDataRequest;
import com.opengamma.master.convention.ConventionMetaDataResult;
import com.opengamma.master.convention.ConventionSearchRequest;
import com.opengamma.master.convention.ConventionSearchResult;
import com.opengamma.master.impl.AbstractRemoteDocumentMaster;
import com.opengamma.util.ArgumentChecker;
import com.sun.jersey.api.client.GenericType;

/**
 * Provides access to a remote {@link ConventionMaster}.
 */
public class RemoteConventionMaster
    extends AbstractRemoteDocumentMaster<ConventionDocument>
    implements ConventionMaster {

  /**
   * Creates an instance.
   *
   * @param baseUri  the base target URI for all RESTful web services, not null
   */
  public RemoteConventionMaster(final URI baseUri) {
    super(baseUri);
  }

  /**
   * Creates an instance.
   *
   * @param baseUri  the base target URI for all RESTful web services, not null
   * @param changeManager  the change manager, not null
   */
  public RemoteConventionMaster(final URI baseUri, ChangeManager changeManager) {
    super(baseUri, changeManager);
  }

  //-------------------------------------------------------------------------
  @Override
  public ConventionMetaDataResult metaData(ConventionMetaDataRequest request) {
    ArgumentChecker.notNull(request, "request");

    URI uri = DataConventionMasterResource.uriMetaData(getBaseUri(), request);
    return accessRemote(uri).get(ConventionMetaDataResult.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public ConventionSearchResult search(final ConventionSearchRequest request) {
    ArgumentChecker.notNull(request, "request");

    URI uri = DataConventionMasterResource.uriSearch(getBaseUri());
    return accessRemote(uri).post(ConventionSearchResult.class, request);
  }

  //-------------------------------------------------------------------------
  @Override
  public ConventionDocument get(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");

    if (uniqueId.isVersioned()) {
      URI uri = (new DataConventionResource()).uriVersion(getBaseUri(), uniqueId);
      return accessRemote(uri).get(ConventionDocument.class);
    } else {
      return get(uniqueId, VersionCorrection.LATEST);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public ConventionDocument get(final ObjectIdentifiable objectId, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");

    URI uri = (new DataConventionResource()).uri(getBaseUri(), objectId, versionCorrection);
    return accessRemote(uri).get(ConventionDocument.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public ConventionDocument add(final ConventionDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getConvention(), "document.convention");

    URI uri = DataConventionMasterResource.uriAdd(getBaseUri());
    return accessRemote(uri).post(ConventionDocument.class, document);
  }

  //-------------------------------------------------------------------------
  @Override
  public ConventionDocument update(final ConventionDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getConvention(), "document.convention");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");

    URI uri = (new DataConventionResource()).uri(getBaseUri(), document.getUniqueId(), null);
    return accessRemote(uri).post(ConventionDocument.class, document);
  }

  //-------------------------------------------------------------------------
  @Override
  public void remove(final ObjectIdentifiable objectIdentifiable) {
    ArgumentChecker.notNull(objectIdentifiable, "objectIdentifiable");

    URI uri = (new DataConventionResource()).uri(getBaseUri(), objectIdentifiable, null);
    accessRemote(uri).delete();
  }

  //-------------------------------------------------------------------------
  @Override
  public ConventionHistoryResult history(final ConventionHistoryRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getObjectId(), "request.objectId");

    URI uri = (new DataConventionResource()).uriVersions(getBaseUri(), request.getObjectId(), request);
    return accessRemote(uri).get(ConventionHistoryResult.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public ConventionDocument correct(final ConventionDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getConvention(), "document.convention");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");

    URI uri = (new DataConventionResource()).uriVersion(getBaseUri(), document.getUniqueId());
    return accessRemote(uri).post(ConventionDocument.class, document);
  }

  @Override
  public List<UniqueId> replaceVersion(UniqueId uniqueId, List<ConventionDocument> replacementDocuments) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    for (ConventionDocument replacementDocument : replacementDocuments) {
      ArgumentChecker.notNull(replacementDocument, "replacementDocument");
      ArgumentChecker.notNull(replacementDocument.getConvention(), "replacementDocument.convention");
    }

    URI uri = (new DataConventionResource()).uriVersion(getBaseUri(), uniqueId);
    return accessRemote(uri).put(new GenericType<List<UniqueId>>() {
    }, replacementDocuments);
  }

  @Override
  public List<UniqueId> replaceAllVersions(ObjectIdentifiable objectId, List<ConventionDocument> replacementDocuments) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    for (ConventionDocument replacementDocument : replacementDocuments) {
      ArgumentChecker.notNull(replacementDocument, "replacementDocument");
      ArgumentChecker.notNull(replacementDocument.getConvention(), "replacementDocument.convention");
    }
    URI uri = (new DataConventionResource()).uriAll(getBaseUri(), objectId, null);
    return accessRemote(uri).put(new GenericType<List<UniqueId>>() {
    }, replacementDocuments);
  }

  @Override
  public List<UniqueId> replaceVersions(ObjectIdentifiable objectId, List<ConventionDocument> replacementDocuments) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    for (ConventionDocument replacementDocument : replacementDocuments) {
      ArgumentChecker.notNull(replacementDocument, "replacementDocument");
      ArgumentChecker.notNull(replacementDocument.getConvention(), "replacementDocument.convention");
    }
    URI uri = (new DataConventionResource()).uri(getBaseUri(), objectId, null);
    return accessRemote(uri).put(new GenericType<List<UniqueId>>() {
    }, replacementDocuments);
  }

}
