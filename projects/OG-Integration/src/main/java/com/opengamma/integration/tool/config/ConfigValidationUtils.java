/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.config;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.region.Region;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.analytics.conversion.CalendarUtils;
import com.opengamma.financial.convention.FinancialConvention;
import com.opengamma.financial.convention.HolidaySourceCalendarAdapter;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.security.index.IborIndex;
import com.opengamma.financial.security.index.Index;
import com.opengamma.financial.security.index.OvernightIndex;
import com.opengamma.financial.security.index.PriceIndex;
import com.opengamma.financial.security.index.SwapIndex;
import com.opengamma.id.ExternalId;
import com.opengamma.master.convention.ManageableConvention;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.master.holiday.HolidaySearchRequest;
import com.opengamma.master.holiday.HolidaySearchResult;
import com.opengamma.master.holiday.impl.MasterHolidaySource;
import com.opengamma.util.money.Currency;

/**
 * Utility methods for confirming existance of configs.
 */
public class ConfigValidationUtils {
  private ConventionSource _conventionSource;
  private HolidayMaster _holidayMaster;
  private SecuritySource _securitySource;
  private RegionSource _regionSource;

  public ConfigValidationUtils(SecuritySource securitySource, ConventionSource conventionSource, HolidayMaster holidayMaster, RegionSource regionSource) {
    _securitySource = securitySource;
    _conventionSource = conventionSource;
    _holidayMaster = holidayMaster;
    _regionSource = regionSource;
  }

  public boolean conventionExists(ExternalId externalId) {
    return getConvention(externalId) != null;
  }
  
  public boolean conventionExists(ExternalId externalId, Class<? extends FinancialConvention> clazz) {
    ManageableConvention convention = getConvention(externalId);
    return  convention != null && convention.getClass().isAssignableFrom(clazz);
  }
  
  public boolean conventionExists(ExternalId externalId, Set<Class<? extends FinancialConvention>> clazzes) {
    ManageableConvention convention = getConvention(externalId);
    if (convention == null) {
      return false;
    }
    for (Class<? extends FinancialConvention> conventionClazz : clazzes) {
      if (convention.getClass().isAssignableFrom(conventionClazz)) {
        return true;
      }
    }
    return false;
  }
  
  public boolean indexExists(ExternalId externalId) {
    try {
      Security security = _securitySource.getSingle(externalId.toBundle());
      if (security instanceof Index) { // implicit null check
        return true;
      }
    } catch (Exception e) {
    }
    return false;
  }
  
  public Index getIndex(ExternalId externalId) {
    try {
      Security security = _securitySource.getSingle(externalId.toBundle());
      if (security instanceof Index) { // implicit null check
        return (Index) security;
      }
    } catch (Exception e) {
    }
    return null;    
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
  
  public boolean holidayExists(final ExternalId regionId) {
    String separator = getMultipleRegionSeparator(regionId);
    if (separator != null) {
      String[] regions = regionId.getValue().split(separator);
      final Set<Region> resultRegions = new HashSet<>();
      for (final String region : regions) {
        if (regionId.isScheme(ExternalSchemes.FINANCIAL)) {
          resultRegions.add(_regionSource.getHighestLevelRegion(ExternalSchemes.financialRegionId(region)));
        } else if (regionId.isScheme(ExternalSchemes.ISDA_HOLIDAY)) {
          resultRegions.add(_regionSource.getHighestLevelRegion(ExternalSchemes.isdaHoliday(region)));
        }
      }
      for (Region region : resultRegions) {
        HolidaySearchRequest request;
        if (region.getCurrency() != null) {
          request = new HolidaySearchRequest(region.getCurrency());
        } else {
          request = new HolidaySearchRequest(HolidayType.BANK, region.getExternalIdBundle());
        }
        HolidaySearchResult searchResult = _holidayMaster.search(request);
        if (searchResult.getDocuments().size() == 0) {
          return false;
        }
      }
      return true;
    }
    final Region region = _regionSource.getHighestLevelRegion(regionId); // we've checked that they are the same.
    HolidaySearchRequest request;
    if (region.getCurrency() != null) {
      request = new HolidaySearchRequest(region.getCurrency());
    } else {
      request = new HolidaySearchRequest(HolidayType.BANK, region.getExternalIdBundle());
    }
    HolidaySearchResult searchResult = _holidayMaster.search(request);
    return searchResult.getDocuments().size() != 0;
  }
  
  /**
   * Returns the escaped separator character for parsing multiple regions
   * 
   * @param regionId the region id to parse.
   * @return the escaped separator charactor.
   */
  private static String getMultipleRegionSeparator(ExternalId regionId) {
    if (!(regionId.isScheme(ExternalSchemes.FINANCIAL) || regionId.isScheme(ExternalSchemes.ISDA_HOLIDAY))) {
      return null;
    }
    
    String regions = regionId.getValue();
    if (regions.contains("+")) {
      return "\\+";
    } else if (regions.contains(",")) {
      return ",";
    } else {
      return null;
    }
  }

  public void checkIndex(Index index, ValidationNode parent) {
    ConventionValidator conventionValidator = new ConventionValidator(this);
    ExternalId conventionId = null;
    if (index instanceof OvernightIndex) {
      OvernightIndex overnightIndex = (OvernightIndex) index;
      conventionId = overnightIndex.getConventionId();
    } else if (index instanceof IborIndex) {
      IborIndex iborIndex = (IborIndex) index;
      conventionId = iborIndex.getConventionId();      
    } else if (index instanceof SwapIndex) {
      SwapIndex swapIndex = (SwapIndex) index;
      conventionId = swapIndex.getConventionId();
    } else if (index instanceof PriceIndex) {
      PriceIndex priceIndex = (PriceIndex) index;
      conventionId = priceIndex.getConventionId();
    }
    if (conventionId != null) {
      ManageableConvention convention = getConvention(conventionId);
      if (convention != null) {
        conventionValidator.followConvention(convention, parent);
        return;
      }
    }
    ValidationNode validationNode = new ValidationNode();
    validationNode.setType(index.getClass());
    validationNode.setName(index.getName());
    validationNode.setError(true);
    validationNode.getErrors().add("Could not find convention " + conventionId + " for index " + index.getExternalIdBundle());
  }
}