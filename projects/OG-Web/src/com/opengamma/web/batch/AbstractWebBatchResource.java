/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.batch;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.financial.batch.BatchDbManager;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractWebResource;
import com.opengamma.web.WebHomeUris;

/**
 * 
 */
public class AbstractWebBatchResource extends AbstractWebResource {

  /**
   * The backing bean.
   */
  private final WebBatchData _data;

  /**
   * Creates the resource.
   * @param batchDbManager  the batch db manager, not null
   */
  protected AbstractWebBatchResource(final BatchDbManager batchDbManager) {
    ArgumentChecker.notNull(batchDbManager, "batchDbManager");
    _data = new WebBatchData();
    data().setBatchDbManager(batchDbManager);
  }

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  protected AbstractWebBatchResource(final AbstractWebBatchResource parent) {
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
   * @return the output root data, not null
   */
  protected FlexiBean createRootData() {
    FlexiBean out = getFreemarker().createRootData();
    out.put("homeUris", new WebHomeUris(data().getUriInfo()));
    out.put("uris", new WebBatchUris(data()));
    return out;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the backing bean.
   * @return the backing bean, not null
   */
  protected WebBatchData data() {
    return _data;
  }

}
