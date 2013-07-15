/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb.orgs;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.master.orgs.impl.RemoteOrganizationMaster;
import com.opengamma.masterdb.TimeOverrideRequest;

import java.net.URI;

import org.threeten.bp.Instant;

/**
 * Provides remote access to a {@link com.opengamma.masterdb.orgs.DbOrganizationMaster}.
 */
public class RemoteDbOrganizationMaster extends RemoteOrganizationMaster {

  /**
   * Creates an instance.
   * 
   * @param baseUri  the base target URI for all RESTful web services, not null
   */
  public RemoteDbOrganizationMaster(final URI baseUri) {
    super(baseUri);
  }

  /**
   * Creates an instance.
   * 
   * @param baseUri  the base target URI for all RESTful web services, not null
   * @param changeManager  the change manager, not null
   */

  public RemoteDbOrganizationMaster(final URI baseUri, ChangeManager changeManager) {
    super(baseUri, changeManager);
  }

  //-------------------------------------------------------------------------
  /**
   * Overrides the current time seen by the remote organization master to a fixed instant.
   * 
   * @param instant  the instant, or null to remove an existing override.
   */
  public void setTimeOverride(final Instant instant) {
    URI uri = DataDbOrganizationMasterResource.uriTimeOverride(getBaseUri());
    TimeOverrideRequest request = new TimeOverrideRequest();
    request.setTimeOverride(instant);
    accessRemote(uri).put(request);
  }
  
}
