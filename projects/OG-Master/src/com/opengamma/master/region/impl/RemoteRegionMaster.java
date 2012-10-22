/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.region.impl;

import java.net.URI;
import java.util.List;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.impl.AbstractRemoteDocumentMaster;
import com.opengamma.master.region.RegionDocument;
import com.opengamma.master.region.RegionHistoryRequest;
import com.opengamma.master.region.RegionHistoryResult;
import com.opengamma.master.region.RegionMaster;
import com.opengamma.master.region.RegionSearchRequest;
import com.opengamma.master.region.RegionSearchResult;
import com.opengamma.util.ArgumentChecker;
import com.sun.jersey.api.client.GenericType;

/**
 * Provides access to a remote {@link RegionMaster}.
 */
public class RemoteRegionMaster
    extends AbstractRemoteDocumentMaster<RegionDocument>
    implements RegionMaster {

  /**
   * Creates an instance.
   *
   * @param baseUri  the base target URI for all RESTful web services, not null
   */
  public RemoteRegionMaster(final URI baseUri) {
    super(baseUri);
  }

  /**
   * Creates an instance.
   *
   * @param baseUri  the base target URI for all RESTful web services, not null
   * @param changeManager  the change manager, not null
   */
  public RemoteRegionMaster(final URI baseUri, ChangeManager changeManager) {
    super(baseUri, changeManager);
  }

  //-------------------------------------------------------------------------
  @Override
  public RegionSearchResult search(final RegionSearchRequest request) {
    ArgumentChecker.notNull(request, "request");

    URI uri = DataRegionMasterResource.uriSearch(getBaseUri());
    return accessRemote(uri).post(RegionSearchResult.class, request);
  }

  //-------------------------------------------------------------------------
  @Override
  public RegionDocument get(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");

    if (uniqueId.isVersioned()) {
      URI uri = (new DataRegionResource()).uriVersion(getBaseUri(), uniqueId);
      return accessRemote(uri).get(RegionDocument.class);
    } else {
      return get(uniqueId, VersionCorrection.LATEST);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public RegionDocument get(final ObjectIdentifiable objectId, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");

    URI uri = (new DataRegionResource()).uri(getBaseUri(), objectId, versionCorrection);
    return accessRemote(uri).get(RegionDocument.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public RegionDocument add(final RegionDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getRegion(), "document.region");

    URI uri = DataRegionMasterResource.uriAdd(getBaseUri());
    return accessRemote(uri).post(RegionDocument.class, document);
  }

  //-------------------------------------------------------------------------
  @Override
  public RegionDocument update(final RegionDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getRegion(), "document.region");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");

    URI uri = (new DataRegionResource()).uri(getBaseUri(), document.getUniqueId(), null);
    return accessRemote(uri).post(RegionDocument.class, document);
  }

  //-------------------------------------------------------------------------
  @Override
  public void remove(final ObjectIdentifiable objectIdentifiable) {
    ArgumentChecker.notNull(objectIdentifiable, "objectIdentifiable");

    URI uri = (new DataRegionResource()).uri(getBaseUri(), objectIdentifiable.getObjectId(), null);
    accessRemote(uri).delete();
  }

  //-------------------------------------------------------------------------
  @Override
  public RegionHistoryResult history(final RegionHistoryRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getObjectId(), "request.objectId");

    URI uri = (new DataRegionResource()).uriVersions(getBaseUri(), request.getObjectId(), request);
    return accessRemote(uri).get(RegionHistoryResult.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public RegionDocument correct(final RegionDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getRegion(), "document.region");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");

    URI uri = (new DataRegionResource()).uriVersion(getBaseUri(), document.getUniqueId());
    return accessRemote(uri).post(RegionDocument.class, document);
  }

  @Override
  public List<UniqueId> replaceVersion(UniqueId uniqueId, List<RegionDocument> replacementDocuments) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    for (RegionDocument replacementDocument : replacementDocuments) {
      ArgumentChecker.notNull(replacementDocument, "documentToAdd");
      ArgumentChecker.notNull(replacementDocument.getRegion(), "document.region");
    }
    URI uri = (new DataRegionResource()).uriVersion(getBaseUri(), uniqueId);
    return accessRemote(uri).put(new GenericType<List<UniqueId>>() {
    }, replacementDocuments);
  }

  @Override
  public List<UniqueId> replaceAllVersions(ObjectIdentifiable objectId, List<RegionDocument> replacementDocuments) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    for (RegionDocument replacementDocument : replacementDocuments) {
      ArgumentChecker.notNull(replacementDocument, "documentToAdd");
      ArgumentChecker.notNull(replacementDocument.getRegion(), "document.region");
    }
    URI uri = (new DataRegionResource()).uriAll(getBaseUri(), objectId, null);
    return accessRemote(uri).put(new GenericType<List<UniqueId>>() {
    }, replacementDocuments);
  }


  @Override
  public List<UniqueId> replaceVersions(ObjectIdentifiable objectId, List<RegionDocument> replacementDocuments) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    for (RegionDocument replacementDocument : replacementDocuments) {
      ArgumentChecker.notNull(replacementDocument, "documentToAdd");
      ArgumentChecker.notNull(replacementDocument.getRegion(), "document.region");
    }
    URI uri = (new DataRegionResource()).uri(getBaseUri(), objectId, null);
    return accessRemote(uri).put(new GenericType<List<UniqueId>>() {
    }, replacementDocuments);
  }

}
