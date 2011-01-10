/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.web.config;

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
import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.config.ConfigDocument;

/**
 * RESTful resource for a configuration document.
 * 
 * @param <T>  the config element type
 */
@Path("/configs/{type}/{configId}")
public class WebConfigTypeResource<T> extends AbstractWebConfigTypeResource<T> {

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  public WebConfigTypeResource(final AbstractWebConfigTypeResource<T> parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String get() {
    FlexiBean out = createRootData();
    return getFreemarker().build("configs/configtype.ftl", out);
  }

  @PUT
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public Response put(
      @FormParam("name") String name) {
    name = StringUtils.trimToNull(name);
    if (name == null) {
      FlexiBean out = createRootData();
      if (name == null) {
        out.put("err_nameMissing", true);
      }
      String html = getFreemarker().build("configs/configtype-update.ftl", out);
      return Response.ok(html).build();
    }
    ConfigDocument<T> oldDoc = data().getConfig();
    ConfigDocument<T> doc = new ConfigDocument<T>();
    doc.setUniqueId(oldDoc.getUniqueId());
    doc.setName(name);
    doc.setValue(oldDoc.getValue());
    doc = data().getConfigTypeMaster().update(doc);
    data().setConfig(doc);
    URI uri = WebConfigTypeResource.uri(data());
    return Response.seeOther(uri).build();
  }

  @DELETE
  public Response delete() {
    ConfigDocument<?> doc = data().getConfig();
    data().getConfigTypeMaster().remove(doc.getUniqueId());
    URI uri = WebConfigsResource.uri(data());
    return Response.seeOther(uri).build();
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
    return out;
  }

  //-------------------------------------------------------------------------
  @Path("versions")
  public WebConfigTypeVersionsResource<T> findVersions() {
    return new WebConfigTypeVersionsResource<T>(this);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(final WebConfigData<?> data) {
    return uri(data, null);
  }

  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @param overrideConfigId  the override config id, null uses information from data
   * @return the URI, not null
   */
  public static URI uri(final WebConfigData<?> data, final UniqueIdentifier overrideConfigId) {
    String typeStr = data.getTypeMap().inverse().get(data.getType());
    String configId = data.getBestConfigUriId(overrideConfigId);
    return data.getUriInfo().getBaseUriBuilder().path(WebConfigTypeResource.class).build(typeStr, configId);
  }

}
