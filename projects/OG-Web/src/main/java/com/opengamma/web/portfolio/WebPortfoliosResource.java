/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.portfolio;

import java.net.URI;
import java.util.List;
import java.util.concurrent.ExecutorService;

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

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.DocumentVisibility;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioHistoryRequest;
import com.opengamma.master.portfolio.PortfolioHistoryResult;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;
import com.opengamma.master.portfolio.PortfolioSearchSortOrder;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.web.WebPaging;
import com.opengamma.web.analytics.rest.MasterType;
import com.opengamma.web.analytics.rest.Subscribe;
import com.opengamma.web.analytics.rest.SubscribeMaster;

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
   * @param securitySource  the security source, not null
   * @param executor  the executor service, not null
   */
  public WebPortfoliosResource(final PortfolioMaster portfolioMaster, final PositionMaster positionMaster, final SecuritySource securitySource, final ExecutorService executor) {
    super(portfolioMaster, positionMaster, securitySource, executor);
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  @SubscribeMaster(MasterType.PORTFOLIO)
  public String getHTML(
      @QueryParam("pgIdx") Integer pgIdx,
      @QueryParam("pgNum") Integer pgNum,
      @QueryParam("pgSze") Integer pgSze,
      @QueryParam("sort") String sort,
      @QueryParam("name") String name,
      @QueryParam("portfolioId") List<String> portfolioIdStrs,
      @QueryParam("nodeId") List<String> nodeIdStrs,
      @QueryParam("includeHidden") Boolean includeHidden) {
    PagingRequest pr = buildPagingRequest(pgIdx, pgNum, pgSze);
    PortfolioSearchSortOrder so = buildSortOrder(sort, PortfolioSearchSortOrder.NAME_ASC);
    FlexiBean out = createSearchResultData(pr, so, name, portfolioIdStrs, nodeIdStrs, includeHidden);
    return getFreemarker().build(HTML_DIR + "portfolios.ftl", out);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @SubscribeMaster(MasterType.PORTFOLIO)
  public String getJSON(
      @QueryParam("pgIdx") Integer pgIdx,
      @QueryParam("pgNum") Integer pgNum,
      @QueryParam("pgSze") Integer pgSze,
      @QueryParam("sort") String sort,
      @QueryParam("name") String name,
      @QueryParam("portfolioId") List<String> portfolioIdStrs,
      @QueryParam("nodeId") List<String> nodeIdStrs,
      @QueryParam("includeHidden") Boolean includeHidden) {
    PagingRequest pr = buildPagingRequest(pgIdx, pgNum, pgSze);
    PortfolioSearchSortOrder so = buildSortOrder(sort, PortfolioSearchSortOrder.NAME_ASC);
    FlexiBean out = createSearchResultData(pr, so, name, portfolioIdStrs, nodeIdStrs, includeHidden);
    return getFreemarker().build(JSON_DIR + "portfolios.ftl", out);
  }

  private FlexiBean createSearchResultData(PagingRequest pr, PortfolioSearchSortOrder sort, String name, 
      List<String> portfolioIdStrs, List<String> nodeIdStrs, Boolean includeHidden) {
    FlexiBean out = createRootData();
    
    PortfolioSearchRequest searchRequest = new PortfolioSearchRequest();
    searchRequest.setPagingRequest(pr);
    searchRequest.setSortOrder(sort);
    searchRequest.setName(StringUtils.trimToNull(name));
    searchRequest.setDepth(1);  // see PLAT-1733, also, depth is set to 1 for knowing # of childNodes for UI tree
    searchRequest.setIncludePositions(true);  // initially false because of PLAT-2012, now true for portfolio tree
    if (BooleanUtils.isTrue(includeHidden)) {
      searchRequest.setVisibility(DocumentVisibility.HIDDEN);
    }
    for (String portfolioIdStr : portfolioIdStrs) {
      searchRequest.addPortfolioObjectId(ObjectId.parse(portfolioIdStr));
    }
    for (String nodeIdStr : nodeIdStrs) {
      searchRequest.addNodeObjectId(ObjectId.parse(nodeIdStr));
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
      String html = getFreemarker().build(HTML_DIR + "portfolios-add.ftl", out);
      return Response.ok(html).build();
    }
    URI uri = createPortfolio(name);
    return Response.seeOther(uri).build();
  }

  private URI createPortfolio(String name) {
    ManageablePortfolio portfolio = new ManageablePortfolio(name);
    PortfolioDocument doc = new PortfolioDocument(portfolio);
    PortfolioDocument added = data().getPortfolioMaster().add(doc);
    return data().getUriInfo().getAbsolutePathBuilder().path(added.getUniqueId().toLatest().toString()).build();
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
  public WebPortfolioResource findPortfolio(@Subscribe @PathParam("portfolioId") String idStr) {
    data().setUriPortfolioId(idStr);
    UniqueId oid = UniqueId.parse(idStr);
    try {
      PortfolioDocument doc = data().getPortfolioMaster().get(oid);
      data().setPortfolio(doc);
      data().setNode(doc.getPortfolio().getRootNode());
    } catch (DataNotFoundException ex) {
      PortfolioHistoryRequest historyRequest = new PortfolioHistoryRequest(oid);
      historyRequest.setPagingRequest(PagingRequest.ONE);
      PortfolioHistoryResult historyResult = data().getPortfolioMaster().history(historyRequest);
      if (historyResult.getDocuments().size() == 0) {
        throw ex;
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
