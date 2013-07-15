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
import com.opengamma.master.orgs.ManageableOrganisation;
import com.opengamma.master.orgs.OrganisationDocument;
import com.opengamma.master.orgs.OrganisationHistoryRequest;
import com.opengamma.master.orgs.OrganisationHistoryResult;
import com.opengamma.master.orgs.OrganisationMaster;
import com.opengamma.master.orgs.OrganisationSearchRequest;
import com.opengamma.master.orgs.OrganisationSearchResult;
import com.opengamma.util.ArgumentChecker;
import com.sun.jersey.api.client.GenericType;

import java.net.URI;
import java.util.List;

/**
 * Provides access to a remote {@link com.opengamma.master.organisation.OrganisationMaster}.
 */
public class RemoteOrganisationMaster
    extends AbstractRemoteDocumentMaster<OrganisationDocument>
    implements OrganisationMaster {

  /**
   * Creates an instance.
   *
   * @param baseUri  the base target URI for all RESTful web services, not null
   */
  public RemoteOrganisationMaster(final URI baseUri) {
    super(baseUri);
  }

  /**
   * Creates an instance.
   *
   * @param baseUri  the base target URI for all RESTful web services, not null
   * @param changeManager  the change manager, not null
   */
  public RemoteOrganisationMaster(final URI baseUri, ChangeManager changeManager) {
    super(baseUri, changeManager);
  }

  //-------------------------------------------------------------------------
  @Override
  public OrganisationSearchResult search(final OrganisationSearchRequest request) {
    ArgumentChecker.notNull(request, "request");

    URI uri = DataOrganisationMasterResource.uriSearch(getBaseUri());
    return accessRemote(uri).post(OrganisationSearchResult.class, request);
  }

  //-------------------------------------------------------------------------
  @Override
  public OrganisationDocument get(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");

    if (uniqueId.isVersioned()) {
      URI uri = (new DataOrganisationResource()).uriVersion(getBaseUri(), uniqueId);
      return accessRemote(uri).get(OrganisationDocument.class);
    } else {
      return get(uniqueId, VersionCorrection.LATEST);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public OrganisationDocument get(final ObjectIdentifiable objectId, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");

    URI uri = (new DataOrganisationResource()).uri(getBaseUri(), objectId, versionCorrection);
    return accessRemote(uri).get(OrganisationDocument.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public OrganisationDocument add(final OrganisationDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getOrganisation(), "document.organisation");

    URI uri = DataOrganisationMasterResource.uriAdd(getBaseUri());
    return accessRemote(uri).post(OrganisationDocument.class, document);
  }

  //-------------------------------------------------------------------------
  @Override
  public OrganisationDocument update(final OrganisationDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getOrganisation(), "document.organisation");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");

    URI uri = (new DataOrganisationResource()).uri(getBaseUri(), document.getUniqueId(), null);
    return accessRemote(uri).post(OrganisationDocument.class, document);
  }

  //-------------------------------------------------------------------------
  @Override
  public void remove(final ObjectIdentifiable objectIdentifiable) {
    ArgumentChecker.notNull(objectIdentifiable, "objectIdentifiable");

    URI uri = (new DataOrganisationResource()).uri(getBaseUri(), objectIdentifiable, null);
    accessRemote(uri).delete();
  }

  //-------------------------------------------------------------------------
  @Override
  public OrganisationHistoryResult history(final OrganisationHistoryRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getObjectId(), "request.objectId");

    URI uri = (new DataOrganisationResource()).uriVersions(getBaseUri(), request.getObjectId(), request);
    return accessRemote(uri).get(OrganisationHistoryResult.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public OrganisationDocument correct(final OrganisationDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getOrganisation(), "document.organisation");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");

    URI uri = (new DataOrganisationResource()).uriVersion(getBaseUri(), document.getUniqueId());
    return accessRemote(uri).post(OrganisationDocument.class, document);
  }

  //-------------------------------------------------------------------------
  @Override
  public ManageableOrganisation getOrganisation(final UniqueId uid) {
    return get(uid).getOrganisation();
  }

  @Override
  public List<UniqueId> replaceVersion(UniqueId uniqueId, List<OrganisationDocument> replacementDocuments) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    for (OrganisationDocument replacementDocument : replacementDocuments) {
      ArgumentChecker.notNull(replacementDocument, "documentToAdd");
      ArgumentChecker.notNull(replacementDocument.getOrganisation(), "document.organisation");
    }
    URI uri = (new DataOrganisationResource()).uriVersion(getBaseUri(), uniqueId);
    return accessRemote(uri).put(new GenericType<List<UniqueId>>() {
    }, replacementDocuments);
  }

  @Override
  public List<UniqueId> replaceAllVersions(ObjectIdentifiable objectId, List<OrganisationDocument> replacementDocuments) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    for (OrganisationDocument replacementDocument : replacementDocuments) {
      ArgumentChecker.notNull(replacementDocument, "documentToAdd");
      ArgumentChecker.notNull(replacementDocument.getOrganisation(), "document.organisation");
    }
    URI uri = (new DataOrganisationResource()).uriAll(getBaseUri(), objectId, null);
    return accessRemote(uri).put(new GenericType<List<UniqueId>>() {
    }, replacementDocuments);
  }

  @Override
  public List<UniqueId> replaceVersions(ObjectIdentifiable objectId, List<OrganisationDocument> replacementDocuments) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    for (OrganisationDocument replacementDocument : replacementDocuments) {
      ArgumentChecker.notNull(replacementDocument, "documentToAdd");
      ArgumentChecker.notNull(replacementDocument.getOrganisation(), "document.organisation");
    }
    URI uri = (new DataOrganisationResource()).uri(getBaseUri(), objectId, null);
    return accessRemote(uri).put(new GenericType<List<UniqueId>>() {
    }, replacementDocuments);
  }
}
