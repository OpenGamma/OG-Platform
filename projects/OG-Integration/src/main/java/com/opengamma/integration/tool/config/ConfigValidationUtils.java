/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.config;

import java.util.Collections;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.id.ExternalId;
import com.opengamma.master.convention.ManageableConvention;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.master.holiday.HolidaySearchRequest;
import com.opengamma.master.holiday.HolidaySearchResult;

/**
 * Utility methods for confirming existance of configs.
 */
public class ConfigValidationUtils {
  private ConventionSource _conventionSource;
  private HolidayMaster _holidayMaster;

  public ConfigValidationUtils(ConventionSource conventionSource, HolidayMaster holidayMaster) {
    _conventionSource = conventionSource;
    _holidayMaster = holidayMaster;
  }

  public boolean conventionExists(ExternalId externalId) {
    return getConvention(externalId) != null;
  }

  ManageableConvention getConvention(ExternalId externalId) {
    try {
      return (ManageableConvention) _conventionSource.getSingle(externalId);
    } catch (DataNotFoundException dnfe) {
      return null;
    } catch (Exception e) {
      return null;
    }
  }
  
  boolean holidayExists(ExternalId customHolidayId) {
    HolidaySearchRequest searchReq = new HolidaySearchRequest(HolidayType.CUSTOM, Collections.singletonList(customHolidayId));
    HolidaySearchResult search = _holidayMaster.search(searchReq);
    return search.getSingleHoliday() != null;
  }
}