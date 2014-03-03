/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.legalentity.impl;

import java.net.URI;
import java.util.List;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.impl.AbstractRemoteDocumentMaster;
import com.opengamma.master.legalentity.LegalEntityDocument;
import com.opengamma.master.legalentity.LegalEntityHistoryRequest;
import com.opengamma.master.legalentity.LegalEntityHistoryResult;
import com.opengamma.master.legalentity.LegalEntityMaster;
import com.opengamma.master.legalentity.LegalEntityMetaDataRequest;
import com.opengamma.master.legalentity.LegalEntityMetaDataResult;
import com.opengamma.master.legalentity.LegalEntitySearchRequest;
import com.opengamma.master.legalentity.LegalEntitySearchResult;
import com.opengamma.util.ArgumentChecker;
import com.sun.jersey.api.client.GenericType;

/**
 * Provides access to a remote {@link LegalEntityMaster}.
 */
public class RemoteLegalEntityMaster
    extends AbstractRemoteDocumentMaster<LegalEntityDocument>
    implements LegalEntityMaster {

  /**
   * Creates an instance.
   *
   * @param baseUri the base target URI for all RESTful web services, not null
   */
  public RemoteLegalEntityMaster(final URI baseUri) {
    super(baseUri);
  }

  /**
   * Creates an instance.
   *
   * @param baseUri       the base target URI for all RESTful web services, not null
   * @param changeManager the change manager, not null
   */
  public RemoteLegalEntityMaster(final URI baseUri, ChangeManager changeManager) {
    super(baseUri, changeManager);
  }

  //-------------------------------------------------------------------------
  @Override
  public LegalEntityMetaDataResult metaData(LegalEntityMetaDataRequest request) {
    ArgumentChecker.notNull(request, "request");

    URI uri = DataLegalEntityMasterResource.uriMetaData(getBaseUri(), request);
    return accessRemote(uri).get(LegalEntityMetaDataResult.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public LegalEntitySearchResult search(final LegalEntitySearchRequest request) {
    ArgumentChecker.notNull(request, "request");

    URI uri = DataLegalEntityMasterResource.uriSearch(getBaseUri());
    return accessRemote(uri).post(LegalEntitySearchResult.class, request);
  }

  //-------------------------------------------------------------------------
  @Override
  public LegalEntityDocument get(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");

    if (uniqueId.isVersioned()) {
      URI uri = (new com.opengamma.master.legalentity.impl.DataLegalEntityResource()).uriVersion(getBaseUri(), uniqueId);
      return accessRemote(uri).get(LegalEntityDocument.class);
    } else {
      return get(uniqueId, VersionCorrection.LATEST);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public LegalEntityDocument get(final ObjectIdentifiable objectId, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");

    URI uri = (new com.opengamma.master.legalentity.impl.DataLegalEntityResource()).uri(getBaseUri(), objectId, versionCorrection);
    return accessRemote(uri).get(LegalEntityDocument.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public LegalEntityDocument add(final LegalEntityDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getLegalEntity(), "document.legalentity");

    URI uri = DataLegalEntityMasterResource.uriAdd(getBaseUri());
    return accessRemote(uri).post(LegalEntityDocument.class, document);
  }

  //-------------------------------------------------------------------------
  @Override
  public LegalEntityDocument update(final LegalEntityDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getLegalEntity(), "document.legalentity");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");

    URI uri = (new com.opengamma.master.legalentity.impl.DataLegalEntityResource()).uri(getBaseUri(), document.getUniqueId(), null);
    return accessRemote(uri).post(LegalEntityDocument.class, document);
  }

  //-------------------------------------------------------------------------
  @Override
  public void remove(final ObjectIdentifiable objectIdentifiable) {
    ArgumentChecker.notNull(objectIdentifiable, "objectIdentifiable");

    URI uri = (new com.opengamma.master.legalentity.impl.DataLegalEntityResource()).uri(getBaseUri(), objectIdentifiable, null);
    accessRemote(uri).delete();
  }

  //-------------------------------------------------------------------------
  @Override
  public LegalEntityHistoryResult history(final LegalEntityHistoryRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getObjectId(), "request.objectId");

    URI uri = (new com.opengamma.master.legalentity.impl.DataLegalEntityResource()).uriVersions(getBaseUri(), request.getObjectId(), request);
    return accessRemote(uri).get(LegalEntityHistoryResult.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public LegalEntityDocument correct(final LegalEntityDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getLegalEntity(), "document.legalentity");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");

    URI uri = (new com.opengamma.master.legalentity.impl.DataLegalEntityResource()).uriVersion(getBaseUri(), document.getUniqueId());
    return accessRemote(uri).post(LegalEntityDocument.class, document);
  }

  @Override
  public List<UniqueId> replaceVersion(UniqueId uniqueId, List<LegalEntityDocument> replacementDocuments) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    for (LegalEntityDocument replacementDocument : replacementDocuments) {
      ArgumentChecker.notNull(replacementDocument, "replacementDocument");
      ArgumentChecker.notNull(replacementDocument.getLegalEntity(), "replacementDocument.legalentity");
    }

    URI uri = (new com.opengamma.master.legalentity.impl.DataLegalEntityResource()).uriVersion(getBaseUri(), uniqueId);
    return accessRemote(uri).put(new GenericType<List<UniqueId>>() {
    }, replacementDocuments);
  }

  @Override
  public List<UniqueId> replaceAllVersions(ObjectIdentifiable objectId, List<LegalEntityDocument> replacementDocuments) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    for (LegalEntityDocument replacementDocument : replacementDocuments) {
      ArgumentChecker.notNull(replacementDocument, "replacementDocument");
      ArgumentChecker.notNull(replacementDocument.getLegalEntity(), "replacementDocument.legalentity");
    }
    URI uri = (new com.opengamma.master.legalentity.impl.DataLegalEntityResource()).uriAll(getBaseUri(), objectId, null);
    return accessRemote(uri).put(new GenericType<List<UniqueId>>() {
    }, replacementDocuments);
  }

  @Override
  public List<UniqueId> replaceVersions(ObjectIdentifiable objectId, List<LegalEntityDocument> replacementDocuments) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    for (LegalEntityDocument replacementDocument : replacementDocuments) {
      ArgumentChecker.notNull(replacementDocument, "replacementDocument");
      ArgumentChecker.notNull(replacementDocument.getLegalEntity(), "replacementDocument.legalentity");
    }
    URI uri = (new DataLegalEntityResource()).uri(getBaseUri(), objectId, null);
    return accessRemote(uri).put(new GenericType<List<UniqueId>>() {
    }, replacementDocuments);
  }

}
