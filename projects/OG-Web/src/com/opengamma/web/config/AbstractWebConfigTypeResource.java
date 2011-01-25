/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.config;

import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.web.AbstractWebResource;
import com.opengamma.web.WebHomeUris;

/**
 * Abstract base class for RESTful config resources.
 * 
 * @param <T>  the config element type
 */
public abstract class AbstractWebConfigTypeResource<T> extends AbstractWebResource {

  /**
   * The backing bean.
   */
  private final WebConfigData<T> _data;

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  @SuppressWarnings("unchecked")
  protected AbstractWebConfigTypeResource(final AbstractWebConfigResource parent) {
    super(parent);
    _data = (WebConfigData<T>) parent.data();
  }

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  protected AbstractWebConfigTypeResource(final AbstractWebConfigTypeResource<T> parent) {
    super(parent);
    _data = parent._data;
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
  protected WebConfigData<T> data() {
    return _data;
  }

}
