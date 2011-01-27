/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.config;

import java.util.Map;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.master.config.ConfigMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.AbstractWebResource;
import com.opengamma.web.WebHomeUris;

/**
 * Abstract base class for RESTful config resources.
 */
public abstract class AbstractWebConfigResource extends AbstractWebResource {

  /**
   * The backing bean.
   */
  private final WebConfigData<?> _data;

  /**
   * Creates the resource.
   * @param configMaster  the config master, not null
   * @param typeMap  the map of valid types, not null
   */
  @SuppressWarnings("rawtypes")
  protected AbstractWebConfigResource(final ConfigMaster configMaster, final Map<String, String> typeMap) {
    ArgumentChecker.notNull(configMaster, "configMaster");
    _data = new WebConfigData();
    data().setConfigMaster(configMaster);
    for (String key : typeMap.keySet()) {
      try {
        data().getTypeMap().put(key, getClass().getClassLoader().loadClass(typeMap.get(key)));
      } catch (ClassNotFoundException ex) {
        throw new RuntimeException(ex);
      }
    }
  }

  /**
   * Setter used to inject the URIInfo.
   * This is a roundabout approach, because Spring and JSR-311 injection clash.
   * DO NOT CALL THIS METHOD DIRECTLY.
   * @param uriInfo  the URI info, not null
   */
  @Context
  public void setUriInfo(final UriInfo uriInfo) {
    data().setUriInfo(uriInfo);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  protected FlexiBean createRootData() {
    FlexiBean out = getFreemarker().createRootData();
    out.put("homeUris", new WebHomeUris(data().getUriInfo()));
    out.put("uris", new WebConfigUris(data()));
    return out;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the backing bean.
   * @return the backing bean, not null
   */
  protected WebConfigData<?> data() {
    return _data;
  }

}
