/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.web;

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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;

import com.opengamma.financial.position.master.ManageablePortfolio;
import com.opengamma.financial.position.master.PortfolioTreeDocument;
import com.opengamma.financial.position.master.PortfolioTreeSearchRequest;
import com.opengamma.financial.position.master.PortfolioTreeSearchResult;
import com.opengamma.financial.position.master.PositionMaster;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.db.PagingRequest;

/**
 * RESTful resource for all portfolios.
 * <p>
 * The portfolios resource represents the whole of a position master.
 */
@Path("/portfolios")
public class WebPortfoliosResource extends AbstractWebPortfolioResource {

  /**
   * Creates the resource.
   * @param positionMaster  the position master, not null
   */
  public WebPortfoliosResource(final PositionMaster positionMaster) {
    super(positionMaster);
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String get(
      @QueryParam("page") int page,
      @QueryParam("pageSize") int pageSize,
      @QueryParam("name") String name) {
    String html = "<html>\n" +
      "<head><title>Portfolios</title></head>\n" +
      "<body>\n" +
      "<h2>Portfolio search</h2>\n" +
      "<form method=\"GET\" action=\"" + data().getUriInfo().getAbsolutePath() + "\">" +
      "Name: <input type=\"text\" size=\"30\" name=\"name\" /><br />" +
      "<input type=\"submit\" value=\"Search\" />" +
      "</form>\n";
    
    if (data().getUriInfo().getQueryParameters().size() > 0) {
      final PortfolioTreeSearchRequest request = new PortfolioTreeSearchRequest();
      request.setPagingRequest(PagingRequest.of(page, pageSize));
      request.setName(StringUtils.trimToNull(name));
      PortfolioTreeSearchResult result = data().getPositionMaster().searchPortfolioTrees(request);
      
      html += "<h2>Portfolio results</h2>\n" +
        "<p><table border=\"1\">" +
        "<tr><th>Name</th><th>Version valid from</th><th>Actions</th></tr>\n";
      for (PortfolioTreeDocument doc : result.getDocuments()) {
        URI uri = data().getUriInfo().getBaseUriBuilder().path(WebPortfolioResource.class).build(doc.getPortfolioId().toLatest());
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
      "<form method=\"POST\" action=\"" + data().getUriInfo().getAbsolutePath() + "\">" +
      "Name: <input type=\"text\" size=\"30\" name=\"name\" /><br />" +
      "<input type=\"submit\" value=\"Add\" />" +
      "</form>\n";
    html += "</body>\n</html>";
    return html;
  }

  //-------------------------------------------------------------------------
  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public Response post(@FormParam("name") String name) {
    if (StringUtils.isEmpty(name)) {
      String html = "<html>\n" +
        "<head><title>Portfolios</title></head>\n" +
        "<body>\n" +
        "<h2>Add portfolio</h2>\n" +
        "<p>The name must be entered!</p>\n" +
        "<form method=\"POST\" action=\"" + data().getUriInfo().getAbsolutePath() + "\"><br />" +
        "Name: <input type=\"text\" size=\"30\" name=\"name\" /><br />" +
        "<input type=\"submit\" value=\"Add\" /><br />" +
        "</form>\n" +
        "</body>\n</html>";
      return Response.ok(html).build();
    }
    ManageablePortfolio portfolio = new ManageablePortfolio(name);
    PortfolioTreeDocument doc = new PortfolioTreeDocument(portfolio);
    PortfolioTreeDocument added = data().getPositionMaster().addPortfolioTree(doc);
    URI uri = data().getUriInfo().getAbsolutePathBuilder().path(added.getPortfolioId().toLatest().toString()).build();
    return Response.seeOther(uri).build();
  }

  //-------------------------------------------------------------------------
  @Path("{portfolioId}")
  public WebPortfolioResource findPortfolio(@PathParam("portfolioId") String idStr) {
    data().setUriPortfolioId(idStr);
    PortfolioTreeDocument portfolio = data().getPositionMaster().getPortfolioTree(UniqueIdentifier.parse(idStr));
    data().setPortfolio(portfolio);
    return new WebPortfolioResource(this);
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
