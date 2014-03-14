/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.holiday;

import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.AbstractPerRequestWebResource;
import com.opengamma.web.exchange.WebExchangeData;
import com.opengamma.web.exchange.WebExchangeUris;
import com.opengamma.web.region.WebRegionData;
import com.opengamma.web.region.WebRegionUris;

/**
 * Abstract base class for RESTful holiday resources.
 */
public abstract class AbstractWebHolidayResource
    extends AbstractPerRequestWebResource<WebHolidayData> {

  /**
   * HTML ftl directory
   */
  protected static final String HTML_DIR = "holidays/html/";
  /**
   * JSON ftl directory
   */
  protected static final String JSON_DIR = "holidays/json/";

  /**
   * Creates the resource.
   * 
   * @param holidayMaster  the holiday master, not null
   */
  protected AbstractWebHolidayResource(final HolidayMaster holidayMaster) {
    super(new WebHolidayData());
    ArgumentChecker.notNull(holidayMaster, "holidayMaster");
    data().setHolidayMaster(holidayMaster);
  }

  /**
   * Creates the resource.
   * 
   * @param parent  the parent resource, not null
   */
  protected AbstractWebHolidayResource(final AbstractWebHolidayResource parent) {
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
    out.put("uris", new WebHolidayUris(data()));
    WebExchangeData exchangeData = new WebExchangeData(data().getUriInfo());
    out.put("exchangeUris", new WebExchangeUris(exchangeData));
    WebRegionData regionData = new WebRegionData(data().getUriInfo());
    out.put("regionUris", new WebRegionUris(regionData));
    return out;
  }

}
