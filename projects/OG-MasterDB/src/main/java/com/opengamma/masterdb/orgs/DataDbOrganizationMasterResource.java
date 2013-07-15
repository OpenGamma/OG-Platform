/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb.orgs;

import com.opengamma.master.orgs.impl.DataOrganizationMasterResource;
import com.opengamma.masterdb.TimeOverrideRequest;
import com.opengamma.util.ArgumentChecker;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

import org.threeten.bp.Clock;
import org.threeten.bp.ZoneId;

/**
 * RESTful resource for organizations.
 * <p>
 * The organizations resource receives and processes RESTful calls to a database organization master.
 */
@Path("organizationMaster")
public class DataDbOrganizationMasterResource extends DataOrganizationMasterResource {

  /**
   * The organization master.
   */
  private final DbOrganizationMaster _dbOrganizationMaster;
  
  /**
   * Creates the resource, exposing the underlying master over REST.
   * 
   * @param dbOrganizationMaster  the underlying database organization master, not null
   */
  public DataDbOrganizationMasterResource(final DbOrganizationMaster dbOrganizationMaster) {
    super(dbOrganizationMaster);
    _dbOrganizationMaster = dbOrganizationMaster;
  }
  
  //-------------------------------------------------------------------------
  public DbOrganizationMaster getDbOrganizationMaster() {
    return _dbOrganizationMaster;
  }
  
  //-------------------------------------------------------------------------
  @PUT
  @Path("timeOverride")
  public Response setTimeOverride(final TimeOverrideRequest doc) {
    ArgumentChecker.notNull(doc, "doc");
    if (doc.getTimeOverride() == null) {
      getDbOrganizationMaster().resetClock();
    } else {
      getDbOrganizationMaster().setClock(Clock.fixed(doc.getTimeOverride(), ZoneId.of("UTC")));
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
