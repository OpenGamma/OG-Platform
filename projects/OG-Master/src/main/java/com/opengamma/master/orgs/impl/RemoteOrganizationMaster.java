/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.orgs.impl;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.impl.AbstractRemoteDocumentMaster;
import com.opengamma.master.orgs.ManageableOrganization;
import com.opengamma.master.orgs.OrganizationDocument;
import com.opengamma.master.orgs.OrganizationHistoryRequest;
import com.opengamma.master.orgs.OrganizationHistoryResult;
import com.opengamma.master.orgs.OrganizationMaster;
import com.opengamma.master.orgs.OrganizationSearchRequest;
import com.opengamma.master.orgs.OrganizationSearchResult;
import com.opengamma.util.ArgumentChecker;
import com.sun.jersey.api.client.GenericType;

import java.net.URI;
import java.util.List;

/**
 * Provides access to a remote {@link com.opengamma.master.orgs.OrganizationMaster}.
 */
public class RemoteOrganizationMaster
    extends AbstractRemoteDocumentMaster<OrganizationDocument>
    implements OrganizationMaster {

  /**
   * Creates an instance.
   *
   * @param baseUri  the base target URI for all RESTful web services, not null
   */
  public RemoteOrganizationMaster(final URI baseUri) {
    super(baseUri);
  }

  /**
   * Creates an instance.
   *
   * @param baseUri  the base target URI for all RESTful web services, not null
   * @param changeManager  the change manager, not null
   */
  public RemoteOrganizationMaster(final URI baseUri, ChangeManager changeManager) {
    super(baseUri, changeManager);
  }

  //-------------------------------------------------------------------------
  @Override
  public OrganizationSearchResult search(final OrganizationSearchRequest request) {
    ArgumentChecker.notNull(request, "request");

    URI uri = DataOrganizationMasterResource.uriSearch(getBaseUri());
    return accessRemote(uri).post(OrganizationSearchResult.class, request);
  }

  //-------------------------------------------------------------------------
  @Override
  public OrganizationDocument get(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");

    if (uniqueId.isVersioned()) {
      URI uri = (new DataOrganizationResource()).uriVersion(getBaseUri(), uniqueId);
      return accessRemote(uri).get(OrganizationDocument.class);
    } else {
      return get(uniqueId, VersionCorrection.LATEST);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public OrganizationDocument get(final ObjectIdentifiable objectId, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");

    URI uri = (new DataOrganizationResource()).uri(getBaseUri(), objectId, versionCorrection);
    return accessRemote(uri).get(OrganizationDocument.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public OrganizationDocument add(final OrganizationDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getOrganization(), "document.organization");

    URI uri = DataOrganizationMasterResource.uriAdd(getBaseUri());
    return accessRemote(uri).post(OrganizationDocument.class, document);
  }

  //-------------------------------------------------------------------------
  @Override
  public OrganizationDocument update(final OrganizationDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getOrganization(), "document.organization");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");

    URI uri = (new DataOrganizationResource()).uri(getBaseUri(), document.getUniqueId(), null);
    return accessRemote(uri).post(OrganizationDocument.class, document);
  }

  //-------------------------------------------------------------------------
  @Override
  public void remove(final ObjectIdentifiable objectIdentifiable) {
    ArgumentChecker.notNull(objectIdentifiable, "objectIdentifiable");

    URI uri = (new DataOrganizationResource()).uri(getBaseUri(), objectIdentifiable, null);
    accessRemote(uri).delete();
  }

  //-------------------------------------------------------------------------
  @Override
  public OrganizationHistoryResult history(final OrganizationHistoryRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getObjectId(), "request.objectId");

    URI uri = (new DataOrganizationResource()).uriVersions(getBaseUri(), request.getObjectId(), request);
    return accessRemote(uri).get(OrganizationHistoryResult.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public OrganizationDocument correct(final OrganizationDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getOrganization(), "document.organization");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");

    URI uri = (new DataOrganizationResource()).uriVersion(getBaseUri(), document.getUniqueId());
    return accessRemote(uri).post(OrganizationDocument.class, document);
  }

  //-------------------------------------------------------------------------
  @Override
  public ManageableOrganization getOrganization(final UniqueId uid) {
    return get(uid).getOrganization();
  }

  @Override
  public List<UniqueId> replaceVersion(UniqueId uniqueId, List<OrganizationDocument> replacementDocuments) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    for (OrganizationDocument replacementDocument : replacementDocuments) {
      ArgumentChecker.notNull(replacementDocument, "documentToAdd");
      ArgumentChecker.notNull(replacementDocument.getOrganization(), "document.organization");
    }
    URI uri = (new DataOrganizationResource()).uriVersion(getBaseUri(), uniqueId);
    return accessRemote(uri).put(new GenericType<List<UniqueId>>() {
    }, replacementDocuments);
  }

  @Override
  public List<UniqueId> replaceAllVersions(ObjectIdentifiable objectId, List<OrganizationDocument> replacementDocuments) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    for (OrganizationDocument replacementDocument : replacementDocuments) {
      ArgumentChecker.notNull(replacementDocument, "documentToAdd");
      ArgumentChecker.notNull(replacementDocument.getOrganization(), "document.organization");
    }
    URI uri = (new DataOrganizationResource()).uriAll(getBaseUri(), objectId, null);
    return accessRemote(uri).put(new GenericType<List<UniqueId>>() {
    }, replacementDocuments);
  }

  @Override
  public List<UniqueId> replaceVersions(ObjectIdentifiable objectId, List<OrganizationDocument> replacementDocuments) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    for (OrganizationDocument replacementDocument : replacementDocuments) {
      ArgumentChecker.notNull(replacementDocument, "documentToAdd");
      ArgumentChecker.notNull(replacementDocument.getOrganization(), "document.organization");
    }
    URI uri = (new DataOrganizationResource()).uri(getBaseUri(), objectId, null);
    return accessRemote(uri).put(new GenericType<List<UniqueId>>() {
    }, replacementDocuments);
  }
}
