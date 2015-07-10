/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.holiday;

import java.net.URI;

import com.opengamma.id.UniqueId;

/**
 * URIs for web-based holidays.
 */
public class WebHolidayUris {

  /**
   * The data.
   */
  private final WebHolidayData _data;

  /**
   * Creates an instance.
   * @param data  the web data, not null
   */
  public WebHolidayUris(WebHolidayData data) {
    _data = data;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the base URI.
   * @return the URI
   */
  public URI base() {
    return holidays();
  }

  /**
   * Gets the URI.
   * @return the URI
   */
  public URI holidays() {
    return WebHolidaysResource.uri(_data);
  }

  /**
   * Gets the URI.
   * @return the URI
   */
  public URI holiday() {
    return WebHolidayResource.uri(_data);
  }

  /**
   * Gets the URI.
   * @param holidayId  the holiday, not null
   * @return the URI
   */
  public URI holiday(final UniqueId holidayId) {
    return WebHolidayResource.uri(_data, holidayId);
  }

}
