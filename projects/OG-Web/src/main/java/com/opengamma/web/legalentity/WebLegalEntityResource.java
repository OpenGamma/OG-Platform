/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.legalentity;

import java.net.URI;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.id.UniqueId;
import com.opengamma.master.legalentity.LegalEntityDocument;
import com.opengamma.master.legalentity.ManageableLegalEntity;

/** RESTful resource for a legalEntity document. */
@Path("/legalEntities/{legalEntityId}")
public class WebLegalEntityResource extends AbstractWebLegalEntityResource {

  /**
   * Creates the resource.
   *
   * @param parent the parent resource, not null
   */
  public WebLegalEntityResource(final AbstractWebLegalEntityResource parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String getHTML() {
    final FlexiBean out = createRootData();
    final LegalEntityDocument doc = data().getLegalEntity();
    out.put("legalEntityXML", createBeanXML(doc.getLegalEntity()));
    return getFreemarker().build(HTML_DIR + "legalentity.ftl", out);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getJSON(@Context final Request request) {
    final EntityTag etag = new EntityTag(data().getLegalEntity().getUniqueId().toString());
    final ResponseBuilder builder = request.evaluatePreconditions(etag);
    if (builder != null) {
      return builder.build();
    }
    final FlexiBean out = createRootData();
    final LegalEntityDocument doc = data().getLegalEntity();
    out.put("legalEntityXML", StringEscapeUtils.escapeJava(createBeanXML(doc.getLegalEntity())));
    out.put("type", data().getTypeMap().inverse().get(doc.getLegalEntity().getClass()));
    final String json = getFreemarker().build(JSON_DIR + "legalentity.ftl", out);
    return Response.ok(json).tag(etag).build();
  }

  //-------------------------------------------------------------------------
  @PUT
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.TEXT_HTML)
  public Response putHTML(
      @FormParam("name") String name,
      @FormParam("legalEntityXML") String xml) {
    if (data().getLegalEntity().isLatest() == false) {
      return Response.status(Status.FORBIDDEN).entity(getHTML()).build();
    }

    name = StringUtils.trimToNull(name);
    xml = StringUtils.trimToNull(xml);
    if (name == null || xml == null) {
      final FlexiBean out = createRootData();
      if (name == null) {
        out.put("err_nameMissing", true);
      }
      if (xml == null) {
        out.put("err_xmlMissing", true);
      }
      final String html = getFreemarker().build(HTML_DIR + "legalentity-update.ftl", out);
      return Response.ok(html).build();
    }

    ManageableLegalEntity legalEntity = parseXML(xml, data().getLegalEntity().getLegalEntity().getClass());
    final URI uri = updateLegalEntity(name, legalEntity);
    return Response.seeOther(uri).build();
  }

  @PUT
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.APPLICATION_JSON)
  public Response putJSON(
      @FormParam("name") String name,
      @FormParam("legalEntityJSON") String json,
      @FormParam("legalEntityXML") String xml) {
    if (data().getLegalEntity().isLatest() == false) {
      return Response.status(Status.FORBIDDEN).entity(getHTML()).build();
    }

    name = StringUtils.trimToNull(name);
    json = StringUtils.trimToNull(json);
    xml = StringUtils.trimToNull(xml);
    // JSON allows a null legalEntity to just change the name
    if (name == null) {
      return Response.status(Status.BAD_REQUEST).build();
    }
    ManageableLegalEntity legalEntityValue = null;
    if (json != null) {
      legalEntityValue = (ManageableLegalEntity) parseJSON(json);
    } else if (xml != null) {
      legalEntityValue = parseXML(xml, ManageableLegalEntity.class);
    }
    updateLegalEntity(name, legalEntityValue);
    return Response.ok().build();
  }

  private URI updateLegalEntity(final String name, final ManageableLegalEntity snapshot) {
    final LegalEntityDocument oldDoc = data().getLegalEntity();
    LegalEntityDocument doc = new LegalEntityDocument(snapshot);
    snapshot.setName(name);
    doc.setUniqueId(oldDoc.getUniqueId());
    doc = data().getLegalEntityMaster().update(doc);
    data().setLegalEntity(doc);
    return WebLegalEntityResource.uri(data());
  }

  //-------------------------------------------------------------------------
  @DELETE
  @Produces(MediaType.TEXT_HTML)
  public Response deleteHTML() {
    final LegalEntityDocument doc = data().getLegalEntity();
    if (doc.isLatest() == false) {
      return Response.status(Status.FORBIDDEN).entity(getHTML()).build();
    }
    data().getLegalEntityMaster().remove(doc.getUniqueId());
    final URI uri = WebLegalEntitiesResource.uri(data());
    return Response.seeOther(uri).build();
  }

  @DELETE
  @Produces(MediaType.APPLICATION_JSON)
  public Response deleteJSON() {
    final LegalEntityDocument doc = data().getLegalEntity();
    if (doc.isLatest()) {
      data().getLegalEntityMaster().remove(doc.getUniqueId());
    }
    return Response.ok().build();
  }

  //-------------------------------------------------------------------------

  /**
   * Creates the output root data.
   *
   * @return the output root data, not null
   */
  @Override
  protected FlexiBean createRootData() {
    final FlexiBean out = super.createRootData();
    final LegalEntityDocument doc = data().getLegalEntity();
    out.put("legalEntityDoc", doc);
    out.put("legalEntity", doc.getLegalEntity());
    out.put("deleted", !doc.isLatest());
    return out;
  }

  //-------------------------------------------------------------------------
  @Path("versions")
  public WebLegalEntityVersionsResource findVersions() {
    return new WebLegalEntityVersionsResource(this);
  }

  //-------------------------------------------------------------------------

  /**
   * Builds a URI for this resource.
   *
   * @param data the data, not null
   * @return the URI, not null
   */
  public static URI uri(final WebLegalEntityData data) {
    return uri(data, null);
  }

  /**
   * Builds a URI for this resource.
   *
   * @param data the data, not null
   * @param overrideLegalEntityId the override legalEntity id, null uses information from data
   * @return the URI, not null
   */
  public static URI uri(final WebLegalEntityData data, final UniqueId overrideLegalEntityId) {
    final String legalEntityId = data.getBestLegalEntityUriId(overrideLegalEntityId);
    return data.getUriInfo().getBaseUriBuilder().path(WebLegalEntityResource.class).build(legalEntityId);
  }

}
