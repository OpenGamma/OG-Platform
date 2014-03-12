/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.convention;

import java.util.Map.Entry;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.master.convention.ManageableConvention;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.AbstractPerRequestWebResource;
import com.opengamma.web.WebHomeUris;

/**
 * Abstract base class for RESTful convention resources.
 */
public abstract class AbstractWebConventionResource
    extends AbstractPerRequestWebResource {

  /**
   * HTML ftl directory
   */
  protected static final String HTML_DIR = "conventions/html/";
  /**
   * JSON ftl directory
   */
  protected static final String JSON_DIR = "conventions/json/";

  /**
   * The Convention types provider
   */
  private final ConventionTypesProvider _conventionTypesProvider = ConventionTypesProvider.getInstance();

  /**
   * The backing bean.
   */
  private final WebConventionData _data;

  /**
   * Creates the resource.
   * 
   * @param conventionMaster  the convention master, not null
   */
  protected AbstractWebConventionResource(final ConventionMaster conventionMaster) {
    ArgumentChecker.notNull(conventionMaster, "conventionMaster");
    _data = new WebConventionData();
    data().setConventionMaster(conventionMaster);
    initializeMetaData();
  }

  //init meta-data
  private void initializeMetaData() {
    for (Entry<String, Class<? extends ManageableConvention>> entry : _conventionTypesProvider.getTypeMap().entrySet()) {
      data().getTypeMap().put(entry.getKey(), entry.getValue());
    }
  }

  /**
   * Creates the resource.
   * 
   * @param parent  the parent resource, not null
   */
  protected AbstractWebConventionResource(final AbstractWebConventionResource parent) {
    super(parent);
    _data = parent._data;
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
   * 
   * @return the output root data, not null
   */
  protected FlexiBean createRootData() {
    FlexiBean out = getFreemarker().createRootData();
    out.put("homeUris", new WebHomeUris(data().getUriInfo()));
    out.put("uris", new WebConventionUris(data()));
    return out;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the backing bean.
   * @return the backing bean, not null
   */
  protected WebConventionData data() {
    return _data;
  }

  /**
   * Gets the convention types provider.
   * 
   * @return the convention types provider
   */
  public ConventionTypesProvider getConventionTypesProvider() {
    return _conventionTypesProvider;
  }

}
