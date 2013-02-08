/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb.position;

import java.net.URI;

import org.threeten.bp.Instant;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.master.position.impl.RemotePositionMaster;
import com.opengamma.masterdb.TimeOverrideRequest;

/**
 * Provides remote access to a {@link DbPositionMaster}.
 */
public class RemoteDbPositionMaster extends RemotePositionMaster {

  /**
   * Creates an instance.
   * 
   * @param baseUri  the base target URI for all RESTful web services, not null
   */
  public RemoteDbPositionMaster(final URI baseUri) {
    super(baseUri);
  }

  /**
   * Creates an instance.
   * 
   * @param baseUri  the base target URI for all RESTful web services, not null
   * @param changeManager  the change manager, not null
   */
  public RemoteDbPositionMaster(final URI baseUri, ChangeManager changeManager) {
    super(baseUri, changeManager);
  }

  //-------------------------------------------------------------------------
  /**
   * Overrides the current time seen by the remote position master to a fixed instant.
   * 
   * @param instant  the instant, or null to remove an existing override.
   */
  public void setTimeOverride(final Instant instant) {
    URI uri = DataDbPositionMasterResource.uriTimeOverride(getBaseUri());
    TimeOverrideRequest request = new TimeOverrideRequest();
    request.setTimeOverride(instant);
    accessRemote(uri).put(request);
  }
  
}
