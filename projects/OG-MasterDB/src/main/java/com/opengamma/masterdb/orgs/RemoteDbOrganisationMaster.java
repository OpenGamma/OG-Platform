/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb.orgs;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.master.orgs.impl.RemoteOrganisationMaster;
import com.opengamma.masterdb.TimeOverrideRequest;

import java.net.URI;

import org.threeten.bp.Instant;

/**
 * Provides remote access to a {@link com.opengamma.masterdb.orgs.DbOrganisationMaster}.
 */
public class RemoteDbOrganisationMaster extends RemoteOrganisationMaster {

  /**
   * Creates an instance.
   * 
   * @param baseUri  the base target URI for all RESTful web services, not null
   */
  public RemoteDbOrganisationMaster(final URI baseUri) {
    super(baseUri);
  }

  /**
   * Creates an instance.
   * 
   * @param baseUri  the base target URI for all RESTful web services, not null
   * @param changeManager  the change manager, not null
   */
  public RemoteDbOrganisationMaster(final URI baseUri, ChangeManager changeManager) {
    super(baseUri, changeManager);
  }

  //-------------------------------------------------------------------------
  /**
   * Overrides the current time seen by the remote organisation master to a fixed instant.
   * 
   * @param instant  the instant, or null to remove an existing override.
   */
  public void setTimeOverride(final Instant instant) {
    URI uri = DataDbOrganisationMasterResource.uriTimeOverride(getBaseUri());
    TimeOverrideRequest request = new TimeOverrideRequest();
    request.setTimeOverride(instant);
    accessRemote(uri).put(request);
  }
  
}
