/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.web.position;

import java.math.BigDecimal;
import java.net.URI;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.joda.beans.impl.flexi.FlexiBean;

import com.google.common.base.Objects;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.PositionDocument;

/**
 * RESTful resource for a position.
 */
@Path("/positions/{positionId}")
public class WebPositionResource extends AbstractWebPositionResource {

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  public WebPositionResource(final AbstractWebPositionResource parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String get() {
    FlexiBean out = createRootData();
    return getFreemarker().build("positions/position.ftl", out);
  }

  @PUT
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public Response put(
      @FormParam("quantity") String quantityStr) {
    quantityStr = StringUtils.replace(StringUtils.trimToNull(quantityStr), ",", "");
    BigDecimal quantity = quantityStr != null && NumberUtils.isNumber(quantityStr) ? new BigDecimal(quantityStr) : null;
    if (quantityStr == null) {
      FlexiBean out = createRootData();
      if (quantityStr == null) {
        out.put("err_quantityMissing", true);
      }
      if (quantity == null) {
        out.put("err_quantityNotNumeric", true);
      }
      String html = getFreemarker().build("positions/position-update.ftl", out);
      return Response.ok(html).build();
    }
    PositionDocument doc = data().getPosition();
    ManageablePosition position = doc.getPosition();
    if (Objects.equal(position.getQuantity(), quantity) == false) {
      position.setQuantity(quantity);
      doc = data().getPositionMaster().update(doc);
      data().setPosition(doc);
    }
    URI uri = WebPositionResource.uri(data());
    return Response.seeOther(uri).build();
  }

  @DELETE
  public Response delete() {
    PositionDocument doc = data().getPosition();
    data().getPositionMaster().remove(doc.getUniqueId());
    URI uri = WebPositionsResource.uri(data());
    return Response.seeOther(uri).build();
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  protected FlexiBean createRootData() {
    FlexiBean out = super.createRootData();
    PositionDocument doc = data().getPosition();
    out.put("positionDoc", doc);
    out.put("position", doc.getPosition());
    return out;
  }

  //-------------------------------------------------------------------------
  @Path("versions")
  public WebPositionVersionsResource findVersions() {
    return new WebPositionVersionsResource(this);
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
   * @param overridePositionId  the override position id, null uses information from data
   * @return the URI, not null
   */
  public static URI uri(final WebPositionsData data, final UniqueIdentifier overridePositionId) {
    String positionId = data.getBestPositionUriId(overridePositionId);
    return data.getUriInfo().getBaseUriBuilder().path(WebPositionResource.class).build(positionId);
  }

}
