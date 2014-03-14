/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.region;

import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.master.region.RegionMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.AbstractPerRequestWebResource;

/**
 * Abstract base class for RESTful region resources.
 */
public abstract class AbstractWebRegionResource
    extends AbstractPerRequestWebResource<WebRegionData> {

  /**
   * HTML ftl directory
   */
  protected static final String HTML_DIR = "regions/html/";
  /**
   * JSON ftl directory
   */
  protected static final String JSON_DIR = "regions/json/";

  /**
   * Creates the resource.
   * 
   * @param regionMaster  the region master, not null
   */
  protected AbstractWebRegionResource(final RegionMaster regionMaster) {
    super(new WebRegionData());
    ArgumentChecker.notNull(regionMaster, "regionMaster");
    data().setRegionMaster(regionMaster);
  }

  /**
   * Creates the resource.
   * 
   * @param parent  the parent resource, not null
   */
  protected AbstractWebRegionResource(final AbstractWebRegionResource parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * 
   * @return the output root data, not null
   */
  @Override
  protected FlexiBean createRootData() {
    FlexiBean out = super.createRootData();
    out.put("uris", new WebRegionUris(data()));
    return out;
  }

}
