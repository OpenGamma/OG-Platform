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
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.engine.view.server.EngineFudgeContextConfiguration;
import com.opengamma.financial.fudgemsg.FinancialFudgeContextConfiguration;
import com.opengamma.financial.position.AddPortfolioRequest;
import com.opengamma.financial.position.ManagablePositionMaster;
import com.opengamma.financial.position.PortfolioSummary;
import com.opengamma.financial.position.SearchPortfoliosRequest;
import com.opengamma.financial.position.SearchPortfoliosResult;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.transport.jaxrs.FudgeRest;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.PagingRequest;

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
  private final ManagablePositionMaster _posMaster;
  /**
   * The fudge context to use when deserializing requests 
   */
  private final FudgeDeserializationContext _fudgeDeserializationContext;
  /**
   * The fudge context to use when serializing responses 
   */
  private final FudgeSerializationContext _fudgeSerializationContext;
  /**
   * Information about the URI injected by JSR-311.
   */
  @Context
  private UriInfo _uriInfo;

  /**
   * Creates the resource.
   * @param posMaster  the position master, not null
   */
  public PortfoliosResource(final ManagablePositionMaster posMaster) {
    ArgumentChecker.notNull(posMaster, "PositionMaster");
    _posMaster = posMaster;
    
    FudgeContext fudgeContext = new FudgeContext();
    EngineFudgeContextConfiguration.INSTANCE.configureFudgeContext(fudgeContext);
    FinancialFudgeContextConfiguration.INSTANCE.configureFudgeContext(fudgeContext);
    _fudgeDeserializationContext = new FudgeDeserializationContext(fudgeContext);
    _fudgeSerializationContext = new FudgeSerializationContext(fudgeContext);
  }

  /**
   * Creates the resource.
   * @param uriInfo  the URI information, not null
   * @param posMaster  the position master, not null
   */
  public PortfoliosResource(UriInfo uriInfo, final ManagablePositionMaster posMaster) {
    this(posMaster);
    ArgumentChecker.notNull(uriInfo, "uriInfo");
    _uriInfo = uriInfo;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the position master.
   * @return the position master, not null
   */
  public ManagablePositionMaster getPositionMaster() {
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
  public SearchPortfoliosResult getAsFudge(
      @QueryParam("page") int page,
      @QueryParam("pageSize") int pageSize,
      @QueryParam("name") String name,
      @QueryParam("deleted") boolean deleted) {
    PagingRequest paging = PagingRequest.of(page, pageSize);
    SearchPortfoliosRequest request = new SearchPortfoliosRequest(paging);
    request.setName(StringUtils.trimToNull(name));
    request.setIncludeDeleted(deleted);
    return getPositionMaster().searchPortfolios(request);
  }

  @GET
  @Produces(MediaType.TEXT_HTML)
  public String getAsHtml(
      @QueryParam("page") int page,
      @QueryParam("pageSize") int pageSize,
      @QueryParam("name") String name,
      @QueryParam("deleted") boolean deleted) {
    String html = "<html>\n" +
      "<head><title>Portfolios</title></head>\n" +
      "<body>\n" +
      "<h2>Portfolio search</h2>\n" +
      "<form method=\"GET\" action=\"" + getUriInfo().getAbsolutePath() + "\">" +
      "Name: <input type=\"text\" size=\"30\" name=\"name\" /><br />" +
      "Deleted: <label><input type=\"checkbox\" name=\"deleted\" value=\"true\" /> Include deleted portfolios</label><br />" +
      "<input type=\"submit\" value=\"Search\" />" +
      "</form>\n";
    
    if (getUriInfo().getQueryParameters().size() > 0) {
      PagingRequest paging = PagingRequest.of(page, pageSize);
      SearchPortfoliosRequest request = new SearchPortfoliosRequest(paging);
      request.setName(StringUtils.trimToNull(name));
      request.setIncludeDeleted(deleted);
      SearchPortfoliosResult result = getPositionMaster().searchPortfolios(request);
      
      html += "<h2>Portfolio results</h2>\n" +
        "<p><table border=\"1\">" +
        "<tr><th>Name</th><th>Positions</th><th>Last updated</th><th>Status</th><th>Actions</th></tr>\n";
      for (PortfolioSummary summary : result.getPortfolioSummaries()) {
        URI uri = getUriInfo().getBaseUriBuilder().path(PortfolioResource.class).build(summary.getUniqueIdentifier().toLatest());
        html += "<tr>";
        if (summary.isActive()) {
          html += "<td><a href=\"" + uri + "\">" + summary.getName() + "</a></td>";
        } else {
          html += "<td>" + summary.getName() + "</td>";
        }
        DateTimeFormatter pattern = DateTimeFormatters.pattern("dd MMM yyyy, HH:mm:ss.SSS");
        html +=
          "<td>" + summary.getTotalPositions() + "</td>" +
          "<td>" + pattern.print(OffsetDateTime.ofInstant(summary.getStartInstant(), ZoneOffset.UTC)) + "</td>" +
          "<td>" + (summary.isActive() ? "Active" : "Deleted") + "</td>";
        if (summary.isActive()) {
          html += "<td><a href=\"" + uri + "\">View</a></td>";
        } else {
          html += "<td>" +
            "<form method=\"POST\" action=\"" + uri + "\">" +
            "<input type=\"hidden\" name=\"method\" value=\"PUT\" />" +
            "<input type=\"hidden\" name=\"status\" value=\"A\" />" +
            "<input type=\"submit\" value=\"Reinstate\" />" +
            "</form>" +
            "</td>";
        }
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

  @POST
  public FudgeMsgEnvelope postFudge(FudgeMsgEnvelope addPortfolioRequestMsg) {
    AddPortfolioRequest request = _fudgeDeserializationContext.fudgeMsgToObject(AddPortfolioRequest.class, addPortfolioRequestMsg.getMessage());
    request.checkValid();
    
    UniqueIdentifier uid = getPositionMaster().addPortfolio(request);
    MutableFudgeFieldContainer responseMsg = _fudgeSerializationContext.objectToFudgeMsg(uid);
    return new FudgeMsgEnvelope(responseMsg);
  }

  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public Response postForm(@FormParam("name") String name) {
    AddPortfolioRequest request = new AddPortfolioRequest();
    request.setName(name);
    try {
      request.checkValid();  // TODO: proper validation
    } catch (IllegalArgumentException ex) {
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
    UniqueIdentifier uid = getPositionMaster().addPortfolio(request);
    URI uri = getUriInfo().getAbsolutePathBuilder().path(uid.toString()).build();
    return Response.seeOther(uri).build();
  }

  //-------------------------------------------------------------------------
  @Path("{portfolioUid}")
  public PortfolioResource findPortfolio(@PathParam("portfolioUid") String uidStr) {
    UniqueIdentifier uid = UniqueIdentifier.parse(uidStr);
    return new PortfolioResource(this, uid);
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
