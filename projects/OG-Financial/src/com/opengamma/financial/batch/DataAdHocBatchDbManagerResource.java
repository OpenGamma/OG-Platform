/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.batch;

import java.net.URI;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.UriBuilder;

/**
 * RESTful resource for the batch manager.
 * <p>
 * This resource receives and processes RESTful calls.
 */
@Path("adHocBatchDbManager")
public class DataAdHocBatchDbManagerResource {

  /**
   * The underlying manager.
   */
  private final AdHocBatchDbManager _underlying;

  /**
   * Creates an instance.
   * 
   * @param underlying  the underlying manager, not null
   */
  public DataAdHocBatchDbManagerResource(AdHocBatchDbManager underlying) {
    _underlying = underlying;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the underlying master.
   * 
   * @return the underlying master, not null
   */
  protected AdHocBatchDbManager getUnderlying() {
    return _underlying;
  }

  //-------------------------------------------------------------------------
  @POST
  public void post(final AdHocBatchResult batch) {
    getUnderlying().write(batch);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI.
   * 
   * @param baseUri  the base URI, not null
   * @return the URI, not null
   */
  public static URI uriWrite(URI baseUri) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("adHocBatchDbManager");
    return bld.build();
  }

}
