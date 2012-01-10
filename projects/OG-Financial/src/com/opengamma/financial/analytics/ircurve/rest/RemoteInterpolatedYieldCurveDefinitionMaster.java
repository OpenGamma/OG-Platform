/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve.rest;

import java.net.URI;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveDefinitionMaster;
import com.opengamma.financial.analytics.ircurve.YieldCurveDefinitionDocument;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.impl.AbstractRemoteMaster;
import com.opengamma.util.ArgumentChecker;

/**
 * Provides access to a remote {@link InterpolatedYieldCurveDefinitionMaster}.
 */
public class RemoteInterpolatedYieldCurveDefinitionMaster extends AbstractRemoteMaster implements InterpolatedYieldCurveDefinitionMaster {

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
      URI uri = DataInterpolatedYieldCurveDefinitionResource.uriVersion(getBaseUri(), uniqueId);
      return accessRemote(uri).get(YieldCurveDefinitionDocument.class);
    } else {
      return get(uniqueId, VersionCorrection.LATEST);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public YieldCurveDefinitionDocument get(final ObjectIdentifiable objectId, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    
    URI uri = DataInterpolatedYieldCurveDefinitionResource.uri(getBaseUri(), objectId, versionCorrection);
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
    
    URI uri = DataInterpolatedYieldCurveDefinitionResource.uri(getBaseUri(), document.getUniqueId(), null);
    return accessRemote(uri).post(YieldCurveDefinitionDocument.class, document);
  }

  //-------------------------------------------------------------------------
  @Override
  public void remove(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    
    URI uri = DataInterpolatedYieldCurveDefinitionResource.uri(getBaseUri(), uniqueId, null);
    accessRemote(uri).delete();
  }

  //-------------------------------------------------------------------------
  @Override
  public YieldCurveDefinitionDocument correct(final YieldCurveDefinitionDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getYieldCurveDefinition(), "document.definition");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");
    
    URI uri = DataInterpolatedYieldCurveDefinitionResource.uriVersion(getBaseUri(), document.getUniqueId());
    return accessRemote(uri).post(YieldCurveDefinitionDocument.class, document);
  }

}
