/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.holiday.impl;

import java.net.URI;

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
import com.opengamma.master.impl.AbstractRemoteMaster;
import com.opengamma.util.ArgumentChecker;

/**
 * Provides access to a remote {@link HolidayMaster}.
 */
public class RemoteHolidayMaster extends AbstractRemoteMaster implements HolidayMaster {

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
    
    String msgBase64 = getRestClient().encodeBean(request);
    URI uri = DataHolidaysResource.uriMetaData(getBaseUri(), msgBase64);
    return accessRemote(uri).get(HolidayMetaDataResult.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public HolidaySearchResult search(final HolidaySearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    
    String msgBase64 = getRestClient().encodeBean(request);
    URI uri = DataHolidaysResource.uri(getBaseUri(), msgBase64);
    return accessRemote(uri).get(HolidaySearchResult.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public HolidayDocument get(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    
    if (uniqueId.isVersioned()) {
      URI uri = DataHolidayResource.uriVersion(getBaseUri(), uniqueId);
      return accessRemote(uri).get(HolidayDocument.class);
    } else {
      return get(uniqueId, VersionCorrection.LATEST);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public HolidayDocument get(final ObjectIdentifiable objectId, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    
    URI uri = DataHolidayResource.uri(getBaseUri(), objectId, versionCorrection);
    return accessRemote(uri).get(HolidayDocument.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public HolidayDocument add(final HolidayDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getHoliday(), "document.holiday");
    
    URI uri = DataHolidaysResource.uri(getBaseUri(), null);
    return accessRemote(uri).post(HolidayDocument.class, document);
  }

  //-------------------------------------------------------------------------
  @Override
  public HolidayDocument update(final HolidayDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getHoliday(), "document.holiday");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");
    
    URI uri = DataHolidayResource.uri(getBaseUri(), document.getUniqueId(), VersionCorrection.LATEST);
    return accessRemote(uri).put(HolidayDocument.class, document);
  }

  //-------------------------------------------------------------------------
  @Override
  public void remove(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    
    URI uri = DataHolidayResource.uri(getBaseUri(), uniqueId, VersionCorrection.LATEST);
    accessRemote(uri).delete();
  }

  //-------------------------------------------------------------------------
  @Override
  public HolidayHistoryResult history(final HolidayHistoryRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getObjectId(), "request.objectId");
    
    String msgBase64 = getRestClient().encodeBean(request);
    URI uri = DataHolidayResource.uriVersions(getBaseUri(), request.getObjectId(), msgBase64);
    return accessRemote(uri).get(HolidayHistoryResult.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public HolidayDocument correct(final HolidayDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getHoliday(), "document.holiday");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");
    
    URI uri = DataHolidayResource.uriVersion(getBaseUri(), document.getUniqueId());
    return accessRemote(uri).get(HolidayDocument.class);
  }

}
