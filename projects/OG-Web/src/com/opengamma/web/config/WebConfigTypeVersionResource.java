/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.config;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;
import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.config.ConfigDocument;

/**
 * RESTful resource for a version of a config.
 * @param <T>  the config element type
 */
@Path("/configs/{type}/{configId}/versions/{versionId}")
@Produces(MediaType.TEXT_HTML)
public class WebConfigTypeVersionResource<T> extends AbstractWebConfigTypeResource<T> {

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  public WebConfigTypeVersionResource(final AbstractWebConfigTypeResource<T> parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
  @GET
  public String get() {
    FlexiBean out = createRootData();
    return getFreemarker().build("configs/configtypeversion.ftl", out);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  protected FlexiBean createRootData() {
    FlexiBean out = super.createRootData();
    ConfigDocument<T> latestDoc = data().getConfig();
    ConfigDocument<T> versionedConfig = data().getVersioned();
    out.put("latestConfigDoc", latestDoc);
    out.put("latestConfig", latestDoc.getValue());
    out.put("configDoc", versionedConfig);
    out.put("config", versionedConfig.getValue());
    out.put("deleted", !latestDoc.isLatest());
    return out;
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
   * @param overrideVersionId  the override version id, null uses information from data
   * @return the URI, not null
   */
  public static URI uri(final WebConfigData<?> data, final UniqueIdentifier overrideVersionId) {
    String typeStr = data.getTypeMap().inverse().get(data.getType());
    String configId = data.getBestConfigUriId(null);
    String versionId = StringUtils.defaultString(overrideVersionId != null ? overrideVersionId.getVersion() : data.getUriVersionId());
    return data.getUriInfo().getBaseUriBuilder().path(WebConfigTypeVersionResource.class).build(typeStr, configId, versionId);
  }

}
