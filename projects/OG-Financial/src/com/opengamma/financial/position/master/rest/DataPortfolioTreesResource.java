/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.master.rest;

import java.net.URI;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Providers;

import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.position.PortfolioTreeDocument;
import com.opengamma.master.position.PortfolioTreeSearchRequest;
import com.opengamma.master.position.PortfolioTreeSearchResult;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.transport.jaxrs.FudgeRest;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for all portfolio trees.
 * <p>
 * The portfolios resource represents the whole of a position master.
 */
@Path("/data/portfoliotrees")
public class DataPortfolioTreesResource extends AbstractDataResource {

  /**
   * The injected position master.
   */
  private final PositionMaster _posMaster;

  /**
   * Creates the resource.
   * @param posMaster  the position master, not null
   */
  public DataPortfolioTreesResource(final PositionMaster posMaster) {
    ArgumentChecker.notNull(posMaster, "PositionMaster");
    _posMaster = posMaster;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the position master.
   * @return the position master, not null
   */
  public PositionMaster getPositionMaster() {
    return _posMaster;
  }

  //-------------------------------------------------------------------------
  @GET
  public Response search(@Context Providers providers, @QueryParam("msg") String msgBase64) {
    PortfolioTreeSearchRequest request = decodeBean(PortfolioTreeSearchRequest.class, providers, msgBase64);
    PortfolioTreeSearchResult result = getPositionMaster().searchPortfolioTrees(request);
    return Response.ok(result).build();
  }

  @POST
  @Consumes(FudgeRest.MEDIA)
  public Response add(@Context UriInfo uriInfo, PortfolioTreeDocument request) {
    PortfolioTreeDocument result = getPositionMaster().addPortfolioTree(request);
    return Response.created(DataPortfolioTreeResource.uri(uriInfo.getBaseUri(), result.getPortfolioId())).entity(result).build();
  }

  //-------------------------------------------------------------------------
  @Path("{portfolioId}")
  public DataPortfolioTreeResource findPortfolio(@PathParam("portfolioId") String idStr) {
    UniqueIdentifier id = UniqueIdentifier.parse(idStr);
    return new DataPortfolioTreeResource(this, id);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for all portfolios.
   * @param baseUri  the base URI, not null
   * @param searchMsg  the search message, may be null
   * @return the URI, not null
   */
  public static URI uri(URI baseUri, String searchMsg) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/portfoliotrees");
    if (searchMsg != null) {
      bld.queryParam("msg", searchMsg);
    }
    return bld.build();
  }

}
