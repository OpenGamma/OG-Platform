/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.config;

import static com.opengamma.web.json.AbstractJSONBuilder.fudgeToJson;

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
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.web.json.JSONBuilder;

/**
 * RESTful resource for a configuration document.
 * 
 */
@Path("/configs/{configId}")
public class WebConfigResource extends AbstractWebConfigResource {
    
  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  public WebConfigResource(final AbstractWebConfigResource parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String getHTML() {
    FlexiBean out = createRootData();
    ConfigDocument<?> doc = data().getConfig();
    out.put("configXml", createXML(doc));
    return getFreemarker().build("configs/config.ftl", out);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getJSON(@Context Request request) {
    EntityTag etag = new EntityTag(data().getConfig().getUniqueId().toString());
    ResponseBuilder builder = request.evaluatePreconditions(etag);
    if (builder != null) {
      return builder.build();
    }
    FlexiBean out = createRootData();
    ConfigDocument<?> doc = data().getConfig();
    String jsonConfig = StringUtils.stripToNull(toJSON(doc.getValue(), doc.getType()));
    if (jsonConfig != null) {
      out.put("configJSON", jsonConfig);
    } else {
      out.put("configXML", StringEscapeUtils.escapeJavaScript(createXML(doc)));
    }
    out.put("type", doc.getType().getName());
    String json = getFreemarker().build("configs/jsonconfig.ftl", out);
    return Response.ok(json).tag(etag).build();
  }
  
  @SuppressWarnings("unchecked")
  private <T> String toJSON(final Object configObj, final Class<T> configType) {
    JSONBuilder<T> jsonBuilder = (JSONBuilder<T>) data().getJsonBuilderMap().get(configType);
    String result = null;
    if (jsonBuilder != null) {
      result = jsonBuilder.toJSON((T) configObj);
    } else {
      result = fudgeToJson(configObj);
    }
    return result;
  }
  
  //-------------------------------------------------------------------------
  @PUT
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.TEXT_HTML)
  public Response putHTML(
      @FormParam("name") String name,
      @FormParam("configxml") String xml) {
    if (data().getConfig().isLatest() == false) {
      return Response.status(Status.FORBIDDEN).entity(getHTML()).build();
    }
    
    name = StringUtils.trimToNull(name);
    xml = StringUtils.trimToNull(xml);
    if (name == null || xml == null) {
      FlexiBean out = createRootData();
      if (name == null) {
        out.put("err_nameMissing", true);
      }
      if (xml == null) {
        out.put("err_xmlMissing", true);
      }
      String html = getFreemarker().build("configs/config-update.ftl", out);
      return Response.ok(html).build();
    }
    
    URI uri = updateConfig(name, parseXML(xml));
    return Response.seeOther(uri).build();
  }

  @PUT
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.APPLICATION_JSON)
  public Response putJSON(
      @FormParam("name") String name,
      @FormParam("configJSON") String json,
      @FormParam("configXML") String xml) {
    if (data().getConfig().isLatest() == false) {
      return Response.status(Status.FORBIDDEN).entity(getHTML()).build();
    }
    
    name = StringUtils.trimToNull(name);
    json = StringUtils.trimToNull(json);
    xml = StringUtils.trimToNull(xml);
    // JSON allows a null config to just change the name
    if (name == null) {
      return Response.status(Status.BAD_REQUEST).build();
    }
    Object configValue = null;
    if (json != null) {
      configValue = parseJSON(json);
    } else if (xml != null) {
      configValue = parseXML(xml);
    }
    updateConfig(name, configValue);
    return Response.ok().build();
  }

  @SuppressWarnings({"unchecked", "rawtypes" })
  private URI updateConfig(String name, Object newConfigValue) {
    ConfigDocument<?> oldDoc = data().getConfig();
    ConfigDocument doc = new ConfigDocument(oldDoc.getType());
    doc.setUniqueId(oldDoc.getUniqueId());
    doc.setName(name);
    if (newConfigValue != null) {  // null means only update the name
      doc.setValue(newConfigValue);
    }
    doc = data().getConfigMaster().update(doc);
    data().setConfig(doc);
    URI uri = WebConfigResource.uri(data());
    return uri;
  }

  //-------------------------------------------------------------------------
  @DELETE
  @Produces(MediaType.TEXT_HTML)
  public Response deleteHTML() {
    ConfigDocument<?> doc = data().getConfig();
    if (doc.isLatest() == false) {
      return Response.status(Status.FORBIDDEN).entity(getHTML()).build();
    }
    data().getConfigMaster().remove(doc.getUniqueId());
    URI uri = WebConfigsResource.uri(data());
    return Response.seeOther(uri).build();
  }

  @DELETE
  @Produces(MediaType.APPLICATION_JSON)
  public Response deleteJSON() {
    ConfigDocument<?> doc = data().getConfig();
    if (doc.isLatest()) {
      data().getConfigMaster().remove(doc.getUniqueId());
    }
    return Response.ok().build();
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  protected FlexiBean createRootData() {
    FlexiBean out = super.createRootData();
    ConfigDocument<?> doc = data().getConfig();
    out.put("configDoc", doc);
    out.put("config", doc.getValue());
    out.put("deleted", !doc.isLatest());
    return out;
  }

  //-------------------------------------------------------------------------
  @Path("versions")
  public WebConfigVersionsResource findVersions() {
    return new WebConfigVersionsResource(this);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(final WebConfigData data) {
    return uri(data, null);
  }

  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @param overrideConfigId  the override config id, null uses information from data
   * @return the URI, not null
   */
  public static URI uri(final WebConfigData data, final UniqueId overrideConfigId) {
    String configId = data.getBestConfigUriId(overrideConfigId);
    return data.getUriInfo().getBaseUriBuilder().path(WebConfigResource.class).build(configId);
  }

}
