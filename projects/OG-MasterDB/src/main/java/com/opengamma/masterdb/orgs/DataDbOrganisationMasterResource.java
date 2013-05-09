/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb.orgs;

import com.opengamma.master.orgs.impl.DataOrganisationMasterResource;
import com.opengamma.masterdb.TimeOverrideRequest;
import com.opengamma.util.ArgumentChecker;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

import org.threeten.bp.Clock;
import org.threeten.bp.ZoneOffset;

/**
 * RESTful resource for organisations.
 * <p>
 * The organisations resource receives and processes RESTful calls to a database organisation master.
 */
@Path("organisationMaster")
public class DataDbOrganisationMasterResource extends DataOrganisationMasterResource {

  /**
   * The organisation master.
   */
  private final DbOrganisationMaster _dbOrganisationMaster;
  
  /**
   * Creates the resource, exposing the underlying master over REST.
   * 
   * @param dbOrganisationMaster  the underlying database organisation master, not null
   */
  public DataDbOrganisationMasterResource(final DbOrganisationMaster dbOrganisationMaster) {
    super(dbOrganisationMaster);
    _dbOrganisationMaster = dbOrganisationMaster;
  }
  
  //-------------------------------------------------------------------------
  public DbOrganisationMaster getDbOrganisationMaster() {
    return _dbOrganisationMaster;
  }
  
  //-------------------------------------------------------------------------
  @PUT
  @Path("timeOverride")
  public Response setTimeOverride(final TimeOverrideRequest doc) {
    ArgumentChecker.notNull(doc, "doc");
    if (doc.getTimeOverride() == null) {
      getDbOrganisationMaster().resetClock();
    } else {
      getDbOrganisationMaster().setClock(Clock.fixed(doc.getTimeOverride(), ZoneOffset.UTC));
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
