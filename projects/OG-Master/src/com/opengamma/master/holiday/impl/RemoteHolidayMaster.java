/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.holiday.impl;

import java.net.URI;
import java.util.List;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.HolidayHistoryRequest;
import com.opengamma.master.holiday.HolidayHistoryResult;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.master.holiday.HolidayMetaDataRequest;
import com.opengamma.master.holiday.HolidayMetaDataResult;
import com.opengamma.master.holiday.HolidaySearchRequest;
import com.opengamma.master.holiday.HolidaySearchResult;
import com.opengamma.master.impl.AbstractRemoteDocumentMaster;
import com.opengamma.util.ArgumentChecker;
import com.sun.jersey.api.client.GenericType;

/**
 * Provides access to a remote {@link HolidayMaster}.
 */
public class RemoteHolidayMaster
    extends AbstractRemoteDocumentMaster<HolidayDocument>
    implements HolidayMaster {

  /**
   * Creates an instance.
   *
   * @param baseUri  the base target URI for all RESTful web services, not null
   */
  public RemoteHolidayMaster(final URI baseUri) {
    super(baseUri);
  }

  /**
   * Creates an instance.
   *
   * @param baseUri  the base target URI for all RESTful web services, not null
   * @param changeManager  the change manager, not null
   */
  public RemoteHolidayMaster(final URI baseUri, ChangeManager changeManager) {
    super(baseUri, changeManager);
  }

  //-------------------------------------------------------------------------
  @Override
  public HolidayMetaDataResult metaData(HolidayMetaDataRequest request) {
    ArgumentChecker.notNull(request, "request");

    URI uri = DataHolidayMasterResource.uriMetaData(getBaseUri(), request);
    return accessRemote(uri).get(HolidayMetaDataResult.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public HolidaySearchResult search(final HolidaySearchRequest request) {
    ArgumentChecker.notNull(request, "request");

    URI uri = DataHolidayMasterResource.uriSearch(getBaseUri());
    return accessRemote(uri).post(HolidaySearchResult.class, request);
  }

  //-------------------------------------------------------------------------
  @Override
  public HolidayDocument get(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");

    if (uniqueId.isVersioned()) {
      URI uri = (new DataHolidayResource()).uriVersion(getBaseUri(), uniqueId);
      return accessRemote(uri).get(HolidayDocument.class);
    } else {
      return get(uniqueId, VersionCorrection.LATEST);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public HolidayDocument get(final ObjectIdentifiable objectId, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");

    URI uri = (new DataHolidayResource()).uri(getBaseUri(), objectId, versionCorrection);
    return accessRemote(uri).get(HolidayDocument.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public HolidayDocument add(final HolidayDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getHoliday(), "document.holiday");

    URI uri = DataHolidayMasterResource.uriAdd(getBaseUri());
    return accessRemote(uri).post(HolidayDocument.class, document);
  }

  //-------------------------------------------------------------------------
  @Override
  public HolidayDocument update(final HolidayDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getHoliday(), "document.holiday");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");

    URI uri = (new DataHolidayResource()).uri(getBaseUri(), document.getUniqueId(), null);
    return accessRemote(uri).post(HolidayDocument.class, document);
  }

  //-------------------------------------------------------------------------
  @Override
  public void remove(final ObjectIdentifiable objectIdentifiable) {
    ArgumentChecker.notNull(objectIdentifiable, "objectIdentifiable");

    URI uri = (new DataHolidayResource()).uri(getBaseUri(), objectIdentifiable, null);
    accessRemote(uri).delete();
  }

  //-------------------------------------------------------------------------
  @Override
  public HolidayHistoryResult history(final HolidayHistoryRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getObjectId(), "request.objectId");

    URI uri = (new DataHolidayResource()).uriVersions(getBaseUri(), request.getObjectId(), request);
    return accessRemote(uri).get(HolidayHistoryResult.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public HolidayDocument correct(final HolidayDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getHoliday(), "document.holiday");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");

    URI uri = (new DataHolidayResource()).uriVersion(getBaseUri(), document.getUniqueId());
    return accessRemote(uri).post(HolidayDocument.class, document);
  }

  @Override
  public List<UniqueId> replaceVersion(UniqueId uniqueId, List<HolidayDocument> replacementDocuments) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    for (HolidayDocument replacementDocument : replacementDocuments) {
      ArgumentChecker.notNull(replacementDocument, "documentToAdd");
      ArgumentChecker.notNull(replacementDocument.getName(), "document.name");
      ArgumentChecker.notNull(replacementDocument.getHoliday(), "document.holiday");
    }
    URI uri = (new DataHolidayResource()).uriVersion(getBaseUri(), uniqueId);
    return accessRemote(uri).put(new GenericType<List<UniqueId>>() {
    }, replacementDocuments);
  }

  @Override
  public List<UniqueId> replaceAllVersions(ObjectIdentifiable objectId, List<HolidayDocument> replacementDocuments) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    for (HolidayDocument replacementDocument : replacementDocuments) {
      ArgumentChecker.notNull(replacementDocument, "documentToAdd");
      ArgumentChecker.notNull(replacementDocument.getName(), "document.name");
      ArgumentChecker.notNull(replacementDocument.getHoliday(), "document.holiday");
    }
    URI uri = (new DataHolidayResource()).uriAll(getBaseUri(), objectId, null);
    return accessRemote(uri).put(new GenericType<List<UniqueId>>() {
    }, replacementDocuments);
  }

  @Override
  public List<UniqueId> replaceVersions(ObjectIdentifiable objectId, List<HolidayDocument> replacementDocuments) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    for (HolidayDocument replacementDocument : replacementDocuments) {
      ArgumentChecker.notNull(replacementDocument, "documentToAdd");
      ArgumentChecker.notNull(replacementDocument.getName(), "document.name");
      ArgumentChecker.notNull(replacementDocument.getHoliday(), "document.holiday");
    }
    URI uri = (new DataHolidayResource()).uri(getBaseUri(), objectId, null);
    return accessRemote(uri).put(new GenericType<List<UniqueId>>() {
    }, replacementDocuments);
  }

}
