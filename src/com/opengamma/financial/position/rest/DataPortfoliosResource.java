/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.rest;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import com.opengamma.financial.position.master.PortfolioTreeDocument;
import com.opengamma.financial.position.master.PortfolioTreeSearchRequest;
import com.opengamma.financial.position.master.PortfolioTreeSearchResult;
import com.opengamma.financial.position.master.PositionMaster;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.transport.jaxrs.FudgeRest;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.PagingRequest;

/**
 * RESTful resource for all portfolios.
 * <p>
 * The portfolios resource represents the whole of a position master.
 */
@Path("/data/portfolios")
@Produces(FudgeRest.MEDIA)
public class DataPortfoliosResource {

  /**
   * The injected position master.
   */
  private final PositionMaster _posMaster;
  /**
   * Information about the URI injected by JSR-311.
   */
  @Context
  private UriInfo _uriInfo;

  /**
   * Creates the resource.
   * @param posMaster  the position master, not null
   */
  public DataPortfoliosResource(final PositionMaster posMaster) {
    ArgumentChecker.notNull(posMaster, "PositionMaster");
    _posMaster = posMaster;
  }

  /**
   * Creates the resource.
   * @param uriInfo  the URI information, not null
   * @param posMaster  the position master, not null
   */
  public DataPortfoliosResource(UriInfo uriInfo, final PositionMaster posMaster) {
    this(posMaster);
    ArgumentChecker.notNull(uriInfo, "uriInfo");
    _uriInfo = uriInfo;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the position master.
   * @return the position master, not null
   */
  public PositionMaster getPositionMaster() {
    return _posMaster;
  }

  /**
   * Gets the URI info.
   * @return the uri info, not null
   */
  public UriInfo getUriInfo() {
    return _uriInfo;
  }

  //-------------------------------------------------------------------------
  @GET
  public PortfolioTreeSearchResult get(
      @QueryParam("page") int page,
      @QueryParam("pageSize") int pageSize,
      @QueryParam("name") String name,
      @QueryParam("depth") int depth) {
    final PortfolioTreeSearchRequest request = new PortfolioTreeSearchRequest();
    request.setPagingRequest(PagingRequest.of(page, pageSize));
    request.setName(name);
    request.setDepth(depth);
    return getPositionMaster().searchPortfolioTrees(request);
  }

  @POST
  public PortfolioTreeDocument post(PortfolioTreeDocument request) {
    return getPositionMaster().addPortfolioTree(request);
  }

  //-------------------------------------------------------------------------
  @Path("{portfolioId}")
  public DataPortfolioResource findPortfolio(@PathParam("portfolioId") String idStr) {
    UniqueIdentifier id = UniqueIdentifier.parse(idStr);
    return new DataPortfolioResource(this, id);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for all portfolios.
   * @param uriInfo  the URI information, not null
   * @return the URI, not null
   */
  public static URI uri(UriInfo uriInfo) {
    return uriInfo.getBaseUriBuilder().path(DataPortfoliosResource.class).build();
  }

}
