/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.web.config;

import java.net.URI;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.config.ConfigMaster;

/**
 * RESTful resource for all configuration documents.
 * <p>
 * The configuration documents resource represents the whole of a config master.
 */
@Path("/configs")
public class WebConfigsResource extends AbstractWebConfigResource {

  /**
   * Creates the resource.
   * @param configMaster  the config master, not null
   * @param typeMap  the map of valid types, not null
   */
  public WebConfigsResource(final ConfigMaster configMaster, final Map<String, String> typeMap) {
    super(configMaster, typeMap);
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String get() {
    FlexiBean out = createRootData();
    return getFreemarker().build("configs/configs.ftl", out);
  }

  //-------------------------------------------------------------------------
  @SuppressWarnings("unchecked")
  @Path("{type}")
  public <T> WebConfigTypesResource<T> findType(@PathParam("type") String typeStr) {
    WebConfigData<T> data = (WebConfigData<T>) data();
    Class<T> type = (Class<T>) data.getTypeMap().get(typeStr);
    if (type == null) {
      return null;
    }
    data.setType(type);
    return new WebConfigTypesResource<T>(this);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  protected FlexiBean createRootData() {
    FlexiBean out = super.createRootData();
    return out;
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for configs.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(WebConfigData<?> data) {
    UriBuilder builder = data.getUriInfo().getBaseUriBuilder().path(WebConfigsResource.class);
    return builder.build();
  }

}
