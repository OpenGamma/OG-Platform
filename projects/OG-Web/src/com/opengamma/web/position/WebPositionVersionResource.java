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

import org.apache.commons.lang.StringUtils;
import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
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
    return getFreemarker().build("positions/positionversion.ftl", out);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getJSON(@Context Request request) {
    EntityTag etag = new EntityTag(data().getVersioned().getUniqueId().toString());
    ResponseBuilder builder = request.evaluatePreconditions(etag);
    if (builder != null) {
      return builder.build();
    }
    FlexiBean out = createRootData();
    createSecurityData(out, data().getVersioned());
    String json = getFreemarker().build("positions/jsonposition.ftl", out);
    return Response.ok(json).tag(etag).build();
  }

  private void createSecurityData(FlexiBean out, PositionDocument doc) {
    IdentifierBundle securityKey = doc.getPosition().getSecurityKey();
    SecuritySource securitySource = data().getSecuritySource();
    Security security = securitySource.getSecurity(securityKey);
    out.put("security", security);
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
    out.put("deleted", !latestPositionDoc.isLatest());
    return out;
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
  public static URI uri(final WebPositionsData data, final UniqueIdentifier overrideVersionId) {
    String positionId = data.getBestPositionUriId(null);
    String versionId = StringUtils.defaultString(overrideVersionId != null ? overrideVersionId.getVersion() : data.getUriVersionId());
    return data.getUriInfo().getBaseUriBuilder().path(WebPositionVersionResource.class).build(positionId, versionId);
  }

}
