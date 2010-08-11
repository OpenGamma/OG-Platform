/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.rest;

import java.net.URI;

import javax.time.calendar.OffsetDateTime;
import javax.time.calendar.ZoneOffset;
import javax.time.calendar.format.DateTimeFormatter;
import javax.time.calendar.format.DateTimeFormatters;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.FudgeDeserializationContext;

import com.opengamma.engine.view.server.EngineFudgeContextConfiguration;
import com.opengamma.financial.fudgemsg.FinancialFudgeContextConfiguration;
import com.opengamma.financial.position.master.ManageablePortfolio;
import com.opengamma.financial.position.master.PortfolioTreeDocument;
import com.opengamma.financial.position.master.PortfolioTreeSearchRequest;
import com.opengamma.financial.position.master.PortfolioTreeSearchResult;
import com.opengamma.financial.position.master.PositionMaster;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.transport.jaxrs.FudgeRest;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.PagingRequest;
import com.opengamma.util.fudge.UtilFudgeContextConfiguration;

/**
 * RESTful resource for all portfolios.
 * <p>
 * The portfolios resource represents the whole of a position master.
 */
@Path("/portfolios")
public class PortfoliosResource {

  /**
   * The injected position master.
   */
  private final PositionMaster _posMaster;
  /**
   * The fudge context to use when deserializing requests 
   */
  private final FudgeDeserializationContext _fudgeDeserializationContext;
  /**
   * Information about the URI injected by JSR-311.
   */
  @Context
  private UriInfo _uriInfo;

  /**
   * Creates the resource.
   * @param posMaster  the position master, not null
   */
  public PortfoliosResource(final PositionMaster posMaster) {
    ArgumentChecker.notNull(posMaster, "PositionMaster");
    _posMaster = posMaster;
    
    FudgeContext fudgeContext = new FudgeContext();
    UtilFudgeContextConfiguration.INSTANCE.configureFudgeContext(fudgeContext);
    EngineFudgeContextConfiguration.INSTANCE.configureFudgeContext(fudgeContext);
    FinancialFudgeContextConfiguration.INSTANCE.configureFudgeContext(fudgeContext);
    _fudgeDeserializationContext = new FudgeDeserializationContext(fudgeContext);
  }

  /**
   * Creates the resource.
   * @param uriInfo  the URI information, not null
   * @param posMaster  the position master, not null
   */
  public PortfoliosResource(UriInfo uriInfo, final PositionMaster posMaster) {
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
  @Produces(FudgeRest.MEDIA)
  public PortfolioTreeSearchResult getAsFudge(
      @QueryParam("page") int page,
      @QueryParam("pageSize") int pageSize,
      @QueryParam("name") String name) {
    final PortfolioTreeSearchRequest request = new PortfolioTreeSearchRequest();
    request.setPagingRequest(PagingRequest.of(page, pageSize));
    request.setName(StringUtils.trimToNull(name));
    return getPositionMaster().searchPortfolioTrees(request);
  }

  @GET
  @Produces(MediaType.TEXT_HTML)
  public String getAsHtml(
      @QueryParam("page") int page,
      @QueryParam("pageSize") int pageSize,
      @QueryParam("name") String name) {
    String html = "<html>\n" +
      "<head><title>Portfolios</title></head>\n" +
      "<body>\n" +
      "<h2>Portfolio search</h2>\n" +
      "<form method=\"GET\" action=\"" + getUriInfo().getAbsolutePath() + "\">" +
      "Name: <input type=\"text\" size=\"30\" name=\"name\" /><br />" +
      "<input type=\"submit\" value=\"Search\" />" +
      "</form>\n";
    
    if (getUriInfo().getQueryParameters().size() > 0) {
      final PortfolioTreeSearchRequest request = new PortfolioTreeSearchRequest();
      request.setPagingRequest(PagingRequest.of(page, pageSize));
      request.setName(StringUtils.trimToNull(name));
      PortfolioTreeSearchResult result = getPositionMaster().searchPortfolioTrees(request);
      
      html += "<h2>Portfolio results</h2>\n" +
        "<p><table border=\"1\">" +
        "<tr><th>Name</th><th>Version valid from</th><th>Actions</th></tr>\n";
      for (PortfolioTreeDocument doc : result.getDocuments()) {
        URI uri = getUriInfo().getBaseUriBuilder().path(PortfolioResource.class).build(doc.getPortfolioId().toLatest());
        html += "<tr>";
        html += "<td><a href=\"" + uri + "\">" + doc.getPortfolio().getName() + "</a></td>";
        DateTimeFormatter pattern = DateTimeFormatters.pattern("dd MMM yyyy, HH:mm:ss.SSS");
        html +=
          "<td>" + pattern.print(OffsetDateTime.ofInstant(doc.getVersionFromInstant(), ZoneOffset.UTC)) + "</td>";
        html += "<td><a href=\"" + uri + "\">View</a></td>";
        html += "</tr>\n";
      }
      html += "</table></p>\n";
    }
    html += "<h2>Add portfolio</h2>\n" +
      "<form method=\"POST\" action=\"" + getUriInfo().getAbsolutePath() + "\">" +
      "Name: <input type=\"text\" size=\"30\" name=\"name\" /><br />" +
      "<input type=\"submit\" value=\"Add\" />" +
      "</form>\n";
    html += "</body>\n</html>";
    return html;
  }

  //-------------------------------------------------------------------------
  @POST
  @Produces(FudgeRest.MEDIA)
  public PortfolioTreeDocument postFudge(FudgeMsgEnvelope addPortfolioRequestMsg) {
    PortfolioTreeDocument request = _fudgeDeserializationContext.fudgeMsgToObject(PortfolioTreeDocument.class, addPortfolioRequestMsg.getMessage());
    return getPositionMaster().addPortfolioTree(request);
  }

  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public Response postForm(@FormParam("name") String name) {
    if (StringUtils.isEmpty(name)) {
      String html = "<html>\n" +
        "<head><title>Portfolios</title></head>\n" +
        "<body>\n" +
        "<h2>Add portfolio</h2>\n" +
        "<p>The name must be entered!</p>\n" +
        "<form method=\"POST\" action=\"" + getUriInfo().getAbsolutePath() + "\"><br />" +
        "Name: <input type=\"text\" size=\"30\" name=\"name\" /><br />" +
        "<input type=\"submit\" value=\"Add\" /><br />" +
        "</form>\n" +
        "</body>\n</html>";
      return Response.ok(html).build();
    }
    ManageablePortfolio portfolio = new ManageablePortfolio(name);
    PortfolioTreeDocument doc = new PortfolioTreeDocument(portfolio);
    PortfolioTreeDocument added = getPositionMaster().addPortfolioTree(doc);
    URI uri = getUriInfo().getAbsolutePathBuilder().path(added.getPortfolioId().toString()).build();
    return Response.seeOther(uri).build();
  }

  //-------------------------------------------------------------------------
  @Path("{portfolioUid}")
  public PortfolioResource findPortfolio(@PathParam("portfolioUid") String uidStr) {
    UniqueIdentifier uid = UniqueIdentifier.parse(uidStr);
    return new PortfolioResource(this, uid);
  }
  
  //-------------------------------------------------------------------------
  @Path(PositionMasterResourceNames.POSITION_MASTER_POSITIONS)
  public AllPositionsResource getPortfolios() {
    return new AllPositionsResource(this, _fudgeDeserializationContext);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for portfolios.
   * @param uriInfo  the URI information, not null
   * @return the URI, not null
   */
  public static URI uri(UriInfo uriInfo) {
    return uriInfo.getBaseUriBuilder().path(PortfoliosResource.class).build();
  }

}
