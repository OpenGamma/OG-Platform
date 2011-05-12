/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.portfolio;

import java.net.URI;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.ObjectIdentifier;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioHistoryRequest;
import com.opengamma.master.portfolio.PortfolioHistoryResult;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.util.db.PagingRequest;
import com.opengamma.web.WebPaging;

/**
 * RESTful resource for all portfolios.
 * <p>
 * The portfolios resource represents the whole of a portfolio master.
 */
@Path("/portfolios")
public class WebPortfoliosResource extends AbstractWebPortfolioResource {

  /**
   * Creates the resource.
   * @param portfolioMaster  the portfolio master, not null
   * @param positionMaster  the position master, not null
   */
  protected WebPortfoliosResource(final PortfolioMaster portfolioMaster, final PositionMaster positionMaster) {
    super(portfolioMaster, positionMaster);
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String getHTML(
      @QueryParam("page") int page,
      @QueryParam("pageSize") int pageSize,
      @QueryParam("name") String name,
      @QueryParam("depth") String depthStr,
      @QueryParam("portfolioId") List<String> portfolioIdStrs,
      @QueryParam("nodeId") List<String> nodeIdStrs) {
    FlexiBean out = createSearchResultData(page, pageSize, name, depthStr, portfolioIdStrs, nodeIdStrs);
    return getFreemarker().build("portfolios/portfolios.ftl", out);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public String getJSON(
      @QueryParam("page") int page,
      @QueryParam("pageSize") int pageSize,
      @QueryParam("name") String name,
      @QueryParam("depth") String depthStr,
      @QueryParam("portfolioId") List<String> portfolioIdStrs,
      @QueryParam("nodeId") List<String> nodeIdStrs) {
    FlexiBean out = createSearchResultData(page, pageSize, name, depthStr, portfolioIdStrs, nodeIdStrs);
    return getFreemarker().build("portfolios/jsonportfolios.ftl", out);
  }

  private FlexiBean createSearchResultData(int page, int pageSize, String name, String depthStr,
      List<String> portfolioIdStrs, List<String> nodeIdStrs) {
    FlexiBean out = createRootData();
    
    PortfolioSearchRequest searchRequest = new PortfolioSearchRequest();
    searchRequest.setPagingRequest(PagingRequest.of(page, pageSize));
    searchRequest.setName(StringUtils.trimToNull(name));
    searchRequest.setDepth(NumberUtils.toInt(depthStr, -1));
    for (String portfolioIdStr : portfolioIdStrs) {
      searchRequest.addPortfolioId(ObjectIdentifier.parse(portfolioIdStr));
    }
    for (String nodeIdStr : nodeIdStrs) {
      searchRequest.addNodeId(ObjectIdentifier.parse(nodeIdStr));
    }
    out.put("searchRequest", searchRequest);
    
    if (data().getUriInfo().getQueryParameters().size() > 0) {
      PortfolioSearchResult searchResult = data().getPortfolioMaster().search(searchRequest);
      out.put("searchResult", searchResult);
      out.put("paging", new WebPaging(searchResult.getPaging(), data().getUriInfo()));
    }
    return out;
  }

  //-------------------------------------------------------------------------
  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.TEXT_HTML)
  public Response postHTML(@FormParam("name") String name) {
    name = StringUtils.trimToNull(name);
    if (name == null) {
      FlexiBean out = createRootData();
      out.put("err_nameMissing", true);
      String html = getFreemarker().build("portfolios/portfolios-add.ftl", out);
      return Response.ok(html).build();
    }
    URI uri = createPortfolio(name);
    return Response.seeOther(uri).build();
  }

  private URI createPortfolio(String name) {
    ManageablePortfolio portfolio = new ManageablePortfolio(name);
    PortfolioDocument doc = new PortfolioDocument(portfolio);
    PortfolioDocument added = data().getPortfolioMaster().add(doc);
    URI uri = data().getUriInfo().getAbsolutePathBuilder().path(added.getUniqueId().toLatest().toString()).build();
    return uri;
  }

  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.APPLICATION_JSON)
  public Response postJSON(@FormParam("name") String name) {
    name = StringUtils.trimToNull(name);
    URI uri = createPortfolio(name);
    return Response.created(uri).build();
  }

  //-------------------------------------------------------------------------
  @Path("{portfolioId}")
  public WebPortfolioResource findPortfolio(@PathParam("portfolioId") String idStr) {
    data().setUriPortfolioId(idStr);
    UniqueIdentifier oid = UniqueIdentifier.parse(idStr);
    try {
      PortfolioDocument doc = data().getPortfolioMaster().get(oid);
      data().setPortfolio(doc);
      data().setNode(doc.getPortfolio().getRootNode());
    } catch (DataNotFoundException ex) {
      PortfolioHistoryRequest historyRequest = new PortfolioHistoryRequest(oid);
      historyRequest.setPagingRequest(PagingRequest.ONE);
      PortfolioHistoryResult historyResult = data().getPortfolioMaster().history(historyRequest);
      if (historyResult.getDocuments().size() == 0) {
        return null;
      }
      data().setPortfolio(historyResult.getFirstDocument());
      data().setNode(historyResult.getFirstDocument().getPortfolio().getRootNode());
    }
    return new WebPortfolioResource(this);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  protected FlexiBean createRootData() {
    FlexiBean out = super.createRootData();
    PortfolioSearchRequest searchRequest = new PortfolioSearchRequest();
    out.put("searchRequest", searchRequest);
    return out;
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for portfolios.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(WebPortfoliosData data) {
    return data.getUriInfo().getBaseUriBuilder().path(WebPortfoliosResource.class).build();
  }

}
