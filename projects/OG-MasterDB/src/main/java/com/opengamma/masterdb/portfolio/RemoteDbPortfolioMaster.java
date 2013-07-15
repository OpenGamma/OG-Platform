/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb.portfolio;

import java.net.URI;

import org.threeten.bp.Instant;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.master.portfolio.impl.RemotePortfolioMaster;
import com.opengamma.masterdb.TimeOverrideRequest;

/**
 * Provides remote access to a {@link DbPortfolioMaster}.
 */
public class RemoteDbPortfolioMaster extends RemotePortfolioMaster {

  /**
   * Creates an instance.
   * 
   * @param baseUri  the base target URI for all RESTful web services, not null
   */
  public RemoteDbPortfolioMaster(final URI baseUri) {
    super(baseUri);
  }

  /**
   * Creates an instance.
   * 
   * @param baseUri  the base target URI for all RESTful web services, not null
   * @param changeManager  the change manager, not null
   */
  public RemoteDbPortfolioMaster(final URI baseUri, ChangeManager changeManager) {
    super(baseUri, changeManager);
  }
  
  //-------------------------------------------------------------------------
  /**
   * Overrides the current time seen by the remote portfolio master to a fixed instant.
   * 
   * @param instant  the instant, or null to remove an existing override.
   */
  public void setTimeOverride(final Instant instant) {
    URI uri = DataDbPortfolioMasterResource.uriTimeOverride(getBaseUri());
    TimeOverrideRequest request = new TimeOverrideRequest();
    request.setTimeOverride(instant);
    accessRemote(uri).put(request);
  }
  
}
