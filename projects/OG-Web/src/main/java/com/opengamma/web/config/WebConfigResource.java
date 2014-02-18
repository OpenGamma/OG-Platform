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

import com.opengamma.core.config.impl.ConfigItem;
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
    final FlexiBean out = createRootData();
    final ConfigDocument doc = data().getConfig();
    out.put(CONFIG_XML, StringEscapeUtils.escapeJava(createBeanXML(doc.getConfig().getValue())));
    return getFreemarker().build(HTML_DIR + "config.ftl", out);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getJSON(@Context final Request request) {
    final EntityTag etag = new EntityTag(data().getConfig().getUniqueId().toString());
    final ResponseBuilder builder = request.evaluatePreconditions(etag);
    if (builder != null) {
      return builder.build();
    }
    final FlexiBean out = createRootData();
    final ConfigDocument doc = data().getConfig();
    final String jsonConfig = StringUtils.stripToNull(toJSON(doc.getConfig().getValue(), doc.getType()));
    if (jsonConfig != null) {
      out.put("configJSON", jsonConfig);
    }
    out.put(CONFIG_XML, StringEscapeUtils.escapeJava(createBeanXML(doc.getConfig().getValue())));
    out.put("type", doc.getType().getSimpleName());
    final String json = getFreemarker().build(JSON_DIR + "config.ftl", out);
    return Response.ok(json).tag(etag).build();
  }

  @SuppressWarnings("unchecked")
  private <T> String toJSON(final Object configObj, final Class<T> configType) {
    final JSONBuilder<T> jsonBuilder = (JSONBuilder<T>) data().getJsonBuilderMap().get(configType);
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
      @FormParam(CONFIG_XML) String configXml) {
    if (data().getConfig().isLatest() == false) {
      return Response.status(Status.FORBIDDEN).entity(getHTML()).build();
    }

    name = StringUtils.trimToNull(name);
    configXml = StringUtils.trimToNull(configXml);
    if (name == null || configXml == null) {
      final FlexiBean out = createRootData();
      out.put(CONFIG_XML, StringEscapeUtils.escapeJavaScript(StringUtils.defaultString(configXml)));
      if (name == null) {
        out.put("err_nameMissing", true);
      }
      if (configXml == null) {
        out.put("err_xmlMissing", true);
      }
      final String html = getFreemarker().build(HTML_DIR + "config-update.ftl", out);
      return Response.ok(html).build();
    }

    Object parsed = parseXML(configXml, data().getConfig().getConfig().getType());
    final URI uri = updateConfig(name, parsed);
    return Response.seeOther(uri).build();
  }

  @PUT
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.APPLICATION_JSON)
  public Response putJSON(
      @FormParam("name") String name,
      @FormParam("configJSON") String json,
      @FormParam(CONFIG_XML) String configXml) {
    if (data().getConfig().isLatest() == false) {
      return Response.status(Status.FORBIDDEN).entity(getHTML()).build();
    }

    name = StringUtils.trimToNull(name);
    json = StringUtils.trimToNull(json);
    configXml = StringUtils.trimToNull(configXml);
    // JSON allows a null config to just change the name
    if (name == null) {
      return Response.status(Status.BAD_REQUEST).build();
    }
    Object configValue = null;
    if (json != null) {
      configValue = parseJSON(json);
    } else if (configXml != null) {
      Object parsed = parseXML(configXml, data().getConfig().getConfig().getType());
      configValue = parsed;
    }
    updateConfig(name, configValue);
    return Response.ok().build();
  }

  private URI updateConfig(final String name, final Object newConfigValue) {
    final ConfigDocument oldDoc = data().getConfig();
    final ConfigItem<?> newItem = ConfigItem.of(newConfigValue);
    newItem.setName(name);
    newItem.setType(oldDoc.getType());
    ConfigDocument doc = new ConfigDocument(newItem);
    doc.setUniqueId(oldDoc.getUniqueId());
    doc = data().getConfigMaster().update(doc);
    data().setConfig(doc);
    final URI uri = WebConfigResource.uri(data());
    return uri;
  }

  //-------------------------------------------------------------------------
  @DELETE
  @Produces(MediaType.TEXT_HTML)
  public Response deleteHTML() {
    final ConfigDocument doc = data().getConfig();
    if (doc.isLatest() == false) {
      return Response.status(Status.FORBIDDEN).entity(getHTML()).build();
    }
    data().getConfigMaster().remove(doc.getUniqueId());
    final URI uri = WebConfigsResource.uri(data());
    return Response.seeOther(uri).build();
  }

  @DELETE
  @Produces(MediaType.APPLICATION_JSON)
  public Response deleteJSON() {
    final ConfigDocument doc = data().getConfig();
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
  @Override
  protected FlexiBean createRootData() {
    final FlexiBean out = super.createRootData();
    final ConfigDocument doc = data().getConfig();
    out.put("configDoc", doc);
    out.put("config", doc.getConfig().getValue());
    out.put("configDescription", getConfigTypesProvider().getDescription(doc.getConfig().getType()));
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
    final String configId = data.getBestConfigUriId(overrideConfigId);
    return data.getUriInfo().getBaseUriBuilder().path(WebConfigResource.class).build(configId);
  }

}
