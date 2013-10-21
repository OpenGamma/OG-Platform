/*
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.financial.mock;

import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.region.RegionSource;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.convention.ConventionBundleMaster;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.DefaultConventionBundleSource;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.master.holiday.impl.InMemoryHolidayMaster;
import com.opengamma.master.holiday.impl.InMemoryHolidayMasterPopulator;
import com.opengamma.master.holiday.impl.MasterHolidaySource;
import com.opengamma.master.region.RegionMaster;
import com.opengamma.master.region.impl.InMemoryRegionMaster;
import com.opengamma.master.region.impl.InMemoryRegionMasterPopulator;
import com.opengamma.master.region.impl.MasterRegionSource;

public class MockSources {

  private static String HOLIDAY_RESOURCE = "mock.holidays";
  private static String REGION_RESOURCE = "mock.regions";

  private FunctionExecutionContext _context = new FunctionExecutionContext();

  public static FunctionExecutionContext isdaMocks() {
    String scheme = ExternalSchemes.ISDA_HOLIDAY.getName();
    MockSources mock = new MockSources();
    mock.setRegionSource(REGION_RESOURCE, scheme);
    mock.setHolidaySource(HOLIDAY_RESOURCE, scheme);
    mock.setConventionBundleSource();
    return mock.mock();
  }

  public FunctionExecutionContext mock() {
    return _context;
  }

  public void setHolidaySource(final String resourceLocation, final String regionScheme) {
    HolidayMaster holidayMaster = new InMemoryHolidayMaster();
    InMemoryHolidayMasterPopulator.populate(holidayMaster, InMemoryHolidayMasterPopulator.load(resourceLocation, regionScheme));
    HolidaySource holidaySource = new MasterHolidaySource(holidayMaster);
    OpenGammaExecutionContext.setHolidaySource(_context, holidaySource);
    // could add master also
  }

  public void setRegionSource(final String resourceLocation, final String regionScheme) {
    RegionMaster regionMaster = new InMemoryRegionMaster();
    InMemoryRegionMasterPopulator.populate(regionMaster, InMemoryRegionMasterPopulator.load(resourceLocation, regionScheme));
    RegionSource regionSource = new MasterRegionSource(regionMaster);
    OpenGammaExecutionContext.setRegionSource(_context, regionSource);
    // could add master also
  }

  public void setConventionBundleSource() {
    ConventionBundleMaster conventionMaster = new InMemoryConventionBundleMaster();
    ConventionBundleSource conventionSource = new DefaultConventionBundleSource(conventionMaster);
    OpenGammaExecutionContext.setConventionBundleSource(_context, conventionSource);
    // could add master also
  }
}
