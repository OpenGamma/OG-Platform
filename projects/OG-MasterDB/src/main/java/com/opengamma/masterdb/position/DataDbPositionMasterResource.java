/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb.position;

import java.net.URI;

import javax.time.TimeSource;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import com.opengamma.master.position.impl.DataPositionMasterResource;
import com.opengamma.masterdb.TimeOverrideRequest;
import com.opengamma.util.ArgumentChecker;

/**
 * RESTful resource for positions.
 * <p>
 * The positions resource receives and processes RESTful calls to a database position master.
 */
@Path("positionMaster")
public class DataDbPositionMasterResource extends DataPositionMasterResource {

  /**
   * The position master.
   */
  private final DbPositionMaster _dbPositionMaster;
  
  /**
   * Creates the resource, exposing the underlying master over REST.
   * 
   * @param dbPositionMaster  the underlying database position master, not null
   */
  public DataDbPositionMasterResource(final DbPositionMaster dbPositionMaster) {
    super(dbPositionMaster);
    _dbPositionMaster = dbPositionMaster;
  }
  
  //-------------------------------------------------------------------------
  public DbPositionMaster getDbPositionMaster() {
    return _dbPositionMaster;
  }
  
  //-------------------------------------------------------------------------
  @PUT
  @Path("timeOverride")
  public Response setTimeOverride(final TimeOverrideRequest doc) {
    ArgumentChecker.notNull(doc, "doc");
    if (doc.getTimeOverride() == null) {
      getDbPositionMaster().resetTimeSource();
    } else {
      getDbPositionMaster().setTimeSource(TimeSource.fixed(doc.getTimeOverride()));
    }
    return responseOk();
  }
  
  //-------------------------------------------------------------------------
  /**
   * Builds a URI.
   * 
   * @param baseUri  the base URI, not null
   * @return the URI, not null
   */
  public static URI uriTimeOverride(URI baseUri) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("timeOverride");
    return bld.build();
  }
  
}
