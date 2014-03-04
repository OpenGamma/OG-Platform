/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.position;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.id.UniqueId;
import com.opengamma.master.position.PositionDocument;

/**
 * RESTful resource for a version of a position.
 */
@Path("/positions/{positionId}/versions/{versionId}")
@Produces(MediaType.TEXT_HTML)
public class WebPositionVersionResource extends AbstractWebPositionResource {

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  public WebPositionVersionResource(final AbstractWebPositionResource parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
  @GET
  public String getHTML() {
    FlexiBean out = createRootData();
    return getFreemarker().build(HTML_DIR + "positionversion.ftl", out);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getJSON(@Context Request request) {
    EntityTag etag = new EntityTag(data().getVersioned().getUniqueId().toString());
    ResponseBuilder builder = request.evaluatePreconditions(etag);
    if (builder != null) {
      return builder.build();
    }
    data().getVersioned().getPosition().getSecurityLink().resolveQuiet(data().getSecuritySource());
    FlexiBean out = createRootData();
    String json = getFreemarker().build(JSON_DIR + "position.ftl", out);
    return Response.ok(json).tag(etag).build();
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  protected FlexiBean createRootData() {
    FlexiBean out = super.createRootData();
    PositionDocument latestPositionDoc = data().getPosition();
    PositionDocument versionedPosition = (PositionDocument) data().getVersioned();
    out.put("latestPositionDoc", latestPositionDoc);
    out.put("latestPosition", latestPositionDoc.getPosition());
    out.put("positionDoc", versionedPosition);
    out.put("position", versionedPosition.getPosition());
    out.put("security", versionedPosition.getPosition().getSecurity());
    out.put("deleted", !latestPositionDoc.isLatest());
    
    out.put("tradeAttrModel", getTradeAttributesModel());
    out.put(POSITION_XML, StringEscapeUtils.escapeJavaScript(getPositionXml(versionedPosition.getPosition())));
    return out;
  }

  private TradeAttributesModel getTradeAttributesModel() {
    PositionDocument doc = data().getVersioned();
    TradeAttributesModel getTradeAttributesModel = new TradeAttributesModel(doc.getPosition());
    return getTradeAttributesModel;
  }
  
  //-------------------------------------------------------------------------
  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(final WebPositionsData data) {
    return uri(data, null);
  }

  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @param overrideVersionId  the override version id, null uses information from data
   * @return the URI, not null
   */
  public static URI uri(final WebPositionsData data, final UniqueId overrideVersionId) {
    String positionId = data.getBestPositionUriId(null);
    String versionId = StringUtils.defaultString(overrideVersionId != null ? overrideVersionId.getVersion() : data.getUriVersionId());
    return data.getUriInfo().getBaseUriBuilder().path(WebPositionVersionResource.class).build(positionId, versionId);
  }

}
