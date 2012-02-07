/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.batch;

import java.net.URI;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractRemoteClient;

/**
 * Provides remote access to an {@link AdHocBatchDbManager}.
 */
public class RemoteAdHocBatchDbManager extends AbstractRemoteClient implements AdHocBatchDbManager {

  /**
   * Creates an instance.
   * 
   * @param baseUri  the base target URI for all RESTful web services, not null
   */
  public RemoteAdHocBatchDbManager(final URI baseUri) {
    super(baseUri);
  }

  //-------------------------------------------------------------------------
  @Override
  public void write(AdHocBatchResult batch) {
    ArgumentChecker.notNull(batch, "batch");
    
    URI uri = DataAdHocBatchDbManagerResource.uriWrite(getBaseUri());
    accessRemote(uri).post(batch);
  }

}
