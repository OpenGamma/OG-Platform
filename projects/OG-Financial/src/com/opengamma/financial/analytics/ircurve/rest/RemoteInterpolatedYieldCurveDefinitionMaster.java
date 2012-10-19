/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve.rest;

import java.net.URI;
import java.util.List;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveDefinitionMaster;
import com.opengamma.financial.analytics.ircurve.YieldCurveDefinitionDocument;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.impl.AbstractRemoteDocumentMaster;
import com.opengamma.util.ArgumentChecker;
import com.sun.jersey.api.client.GenericType;

/**
 * Provides access to a remote {@link InterpolatedYieldCurveDefinitionMaster}.
 */
public class RemoteInterpolatedYieldCurveDefinitionMaster extends AbstractRemoteDocumentMaster<YieldCurveDefinitionDocument> implements InterpolatedYieldCurveDefinitionMaster {

  /**
   * Creates an instance.
   *
   * @param baseUri  the base target URI for all RESTful web services, not null
   */
  public RemoteInterpolatedYieldCurveDefinitionMaster(final URI baseUri) {
    super(baseUri);
  }

  /**
   * Creates an instance.
   *
   * @param baseUri  the base target URI for all RESTful web services, not null
   * @param changeManager  the change manager, not null
   */
  public RemoteInterpolatedYieldCurveDefinitionMaster(final URI baseUri, ChangeManager changeManager) {
    super(baseUri, changeManager);
  }

  //-------------------------------------------------------------------------
  @Override
  public YieldCurveDefinitionDocument get(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");

    if (uniqueId.isVersioned()) {
      URI uri = (new DataInterpolatedYieldCurveDefinitionResource()).uriVersion(getBaseUri(), uniqueId);
      return accessRemote(uri).get(YieldCurveDefinitionDocument.class);
    } else {
      return get(uniqueId, VersionCorrection.LATEST);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public YieldCurveDefinitionDocument get(final ObjectIdentifiable objectId, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");

    URI uri = (new DataInterpolatedYieldCurveDefinitionResource()).uri(getBaseUri(), objectId, versionCorrection);
    return accessRemote(uri).get(YieldCurveDefinitionDocument.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public YieldCurveDefinitionDocument add(final YieldCurveDefinitionDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getYieldCurveDefinition(), "document.definition");

    URI uri = DataInterpolatedYieldCurveDefinitionMasterResource.uri(getBaseUri());
    return accessRemote(uri).post(YieldCurveDefinitionDocument.class, document);
  }

  //-------------------------------------------------------------------------
  @Override
  public YieldCurveDefinitionDocument addOrUpdate(final YieldCurveDefinitionDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getYieldCurveDefinition(), "document.definition");

    URI uri = DataInterpolatedYieldCurveDefinitionMasterResource.uri(getBaseUri());
    return accessRemote(uri).post(YieldCurveDefinitionDocument.class, document);
  }

  //-------------------------------------------------------------------------
  @Override
  public YieldCurveDefinitionDocument update(final YieldCurveDefinitionDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getYieldCurveDefinition(), "document.definition");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");

    URI uri = (new DataInterpolatedYieldCurveDefinitionResource()).uri(getBaseUri(), document.getUniqueId(), null);
    return accessRemote(uri).post(YieldCurveDefinitionDocument.class, document);
  }

  //-------------------------------------------------------------------------
  @Override
  public void remove(final ObjectIdentifiable objectIdentifiable) {
    ArgumentChecker.notNull(objectIdentifiable, "objectIdentifiable");

    URI uri = (new DataInterpolatedYieldCurveDefinitionResource()).uri(getBaseUri(), objectIdentifiable, null);
    accessRemote(uri).delete();
  }

  //-------------------------------------------------------------------------
  @Override
  public YieldCurveDefinitionDocument correct(final YieldCurveDefinitionDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getYieldCurveDefinition(), "document.definition");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");

    URI uri = (new DataInterpolatedYieldCurveDefinitionResource()).uriVersion(getBaseUri(), document.getUniqueId());
    return accessRemote(uri).post(YieldCurveDefinitionDocument.class, document);
  }

  @Override
  public List<UniqueId> replaceVersion(UniqueId uniqueId, List<YieldCurveDefinitionDocument> replacementDocuments) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    for (YieldCurveDefinitionDocument replacementDocument : replacementDocuments) {
      ArgumentChecker.notNull(replacementDocument, "documentToAdd");
      ArgumentChecker.notNull(replacementDocument.getYieldCurveDefinition(), "document.definition");
    }
    URI uri = (new DataInterpolatedYieldCurveDefinitionResource()).uriVersion(getBaseUri(), uniqueId);
    return accessRemote(uri).put(new GenericType<List<UniqueId>>() {
    }, replacementDocuments);
  }

  @Override
  public List<UniqueId> replaceAllVersions(ObjectIdentifiable objectId, List<YieldCurveDefinitionDocument> replacementDocuments) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    for (YieldCurveDefinitionDocument replacementDocument : replacementDocuments) {
      ArgumentChecker.notNull(replacementDocument, "documentToAdd");
      ArgumentChecker.notNull(replacementDocument.getYieldCurveDefinition(), "document.definition");
    }
    URI uri = (new DataInterpolatedYieldCurveDefinitionResource()).uriAll(getBaseUri(), objectId, null);
    return accessRemote(uri).put(new GenericType<List<UniqueId>>() {
    }, replacementDocuments);
  }

  @Override
  public List<UniqueId> replaceVersions(ObjectIdentifiable objectId, List<YieldCurveDefinitionDocument> replacementDocuments) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    for (YieldCurveDefinitionDocument replacementDocument : replacementDocuments) {
      ArgumentChecker.notNull(replacementDocument, "documentToAdd");
      ArgumentChecker.notNull(replacementDocument.getYieldCurveDefinition(), "document.definition");
    }
    URI uri = (new DataInterpolatedYieldCurveDefinitionResource()).uri(getBaseUri(), objectId, null);
    return accessRemote(uri).put(new GenericType<List<UniqueId>>() {
    }, replacementDocuments);
  }
}
