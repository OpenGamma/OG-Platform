/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.security.impl;

import java.net.URI;
import java.util.List;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.impl.AbstractRemoteDocumentMaster;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityHistoryRequest;
import com.opengamma.master.security.SecurityHistoryResult;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecurityMetaDataRequest;
import com.opengamma.master.security.SecurityMetaDataResult;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.util.ArgumentChecker;
import com.sun.jersey.api.client.GenericType;

/**
 * Provides access to a remote {@link SecurityMaster}.
 */
public class RemoteSecurityMaster
    extends AbstractRemoteDocumentMaster<SecurityDocument>
    implements SecurityMaster {

  /**
   * Creates an instance.
   *
   * @param baseUri  the base target URI for all RESTful web services, not null
   */
  public RemoteSecurityMaster(final URI baseUri) {
    super(baseUri);
  }

  /**
   * Creates an instance.
   *
   * @param baseUri  the base target URI for all RESTful web services, not null
   * @param changeManager  the change manager, not null
   */
  public RemoteSecurityMaster(final URI baseUri, ChangeManager changeManager) {
    super(baseUri, changeManager);
  }

  //-------------------------------------------------------------------------
  @Override
  public SecurityMetaDataResult metaData(SecurityMetaDataRequest request) {
    ArgumentChecker.notNull(request, "request");

    URI uri = DataSecurityMasterResource.uriMetaData(getBaseUri(), request);
    return accessRemote(uri).get(SecurityMetaDataResult.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public SecuritySearchResult search(final SecuritySearchRequest request) {
    ArgumentChecker.notNull(request, "request");

    URI uri = DataSecurityMasterResource.uriSearch(getBaseUri());
    return accessRemote(uri).post(SecuritySearchResult.class, request);
  }

  //-------------------------------------------------------------------------
  @Override
  public SecurityDocument get(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");

    if (uniqueId.isVersioned()) {
      URI uri = (new DataSecurityResource()).uriVersion(getBaseUri(), uniqueId);
      return accessRemote(uri).get(SecurityDocument.class);
    } else {
      return get(uniqueId, VersionCorrection.LATEST);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public SecurityDocument get(final ObjectIdentifiable objectId, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");

    URI uri = (new DataSecurityResource()).uri(getBaseUri(), objectId, versionCorrection);
    return accessRemote(uri).get(SecurityDocument.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public SecurityDocument add(final SecurityDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getSecurity(), "document.security");

    URI uri = DataSecurityMasterResource.uriAdd(getBaseUri());
    return accessRemote(uri).post(SecurityDocument.class, document);
  }

  //-------------------------------------------------------------------------
  @Override
  public SecurityDocument update(final SecurityDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getSecurity(), "document.security");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");

    URI uri = (new DataSecurityResource()).uri(getBaseUri(), document.getUniqueId(), null);
    return accessRemote(uri).post(SecurityDocument.class, document);
  }

  //-------------------------------------------------------------------------
  @Override
  public void remove(final ObjectIdentifiable objectIdentifiable) {
    ArgumentChecker.notNull(objectIdentifiable, "objectIdentifiable");

    URI uri = (new DataSecurityResource()).uri(getBaseUri(), objectIdentifiable, null);
    accessRemote(uri).delete();
  }

  //-------------------------------------------------------------------------
  @Override
  public SecurityHistoryResult history(final SecurityHistoryRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getObjectId(), "request.objectId");

    URI uri = (new DataSecurityResource()).uriVersions(getBaseUri(), request.getObjectId(), request);
    return accessRemote(uri).get(SecurityHistoryResult.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public SecurityDocument correct(final SecurityDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getSecurity(), "document.security");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");

    URI uri = (new DataSecurityResource()).uriVersion(getBaseUri(), document.getUniqueId());
    return accessRemote(uri).post(SecurityDocument.class, document);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @Override
  public List<UniqueId> replaceVersion(UniqueId uniqueId, List<SecurityDocument> replacementDocuments) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    for (SecurityDocument replacementDocument : replacementDocuments) {
      ArgumentChecker.notNull(replacementDocument, "documentToAdd");
      ArgumentChecker.notNull(replacementDocument.getSecurity(), "document.security");
    }
    URI uri = (new DataSecurityResource()).uriVersion(getBaseUri(), uniqueId);
    return accessRemote(uri).put(new GenericType<List<UniqueId>>() {
    }, replacementDocuments);
  }

  @Override
  public List<UniqueId> replaceAllVersions(ObjectIdentifiable objectId, List<SecurityDocument> replacementDocuments) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    for (SecurityDocument replacementDocument : replacementDocuments) {
      ArgumentChecker.notNull(replacementDocument, "documentToAdd");
      ArgumentChecker.notNull(replacementDocument.getSecurity(), "document.security");
    }
    URI uri = (new DataSecurityResource()).uriAll(getBaseUri(), objectId, null);
    return accessRemote(uri).put(new GenericType<List<UniqueId>>() {
    }, replacementDocuments);
  }

  @Override
  public List<UniqueId> replaceVersions(ObjectIdentifiable objectId, List<SecurityDocument> replacementDocuments) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    for (SecurityDocument replacementDocument : replacementDocuments) {
      ArgumentChecker.notNull(replacementDocument, "documentToAdd");
      ArgumentChecker.notNull(replacementDocument.getSecurity(), "document.security");
    }
    URI uri = (new DataSecurityResource()).uri(getBaseUri(), objectId, null);
    return accessRemote(uri).put(new GenericType<List<UniqueId>>() {
    }, replacementDocuments);
  }
}
