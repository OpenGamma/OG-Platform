/*
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.region.impl;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.threeten.bp.LocalDate;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.region.ManageableRegion;
import com.opengamma.master.region.RegionDocument;
import com.opengamma.master.region.RegionMaster;
import com.opengamma.util.money.Currency;

import au.com.bytecode.opencsv.CSVReader;

/**
 *  Populate a holiday master with holidays - can load from a csv in the classpath.
 */
public class InMemoryRegionMasterPopulator {

  public static void populate(final RegionMaster master, Collection<ManageableRegion> regions) {
    for (ManageableRegion region : regions) {
      RegionDocument doc = new RegionDocument();
      doc.setRegion(region);
      master.add(doc);
    }
  }

  public static List<ManageableRegion> load(final String resourceLocation, final String regionScheme) {
    final ResourceBundle properties = ResourceBundle.getBundle(resourceLocation);
    List<ManageableRegion> regions = Lists.newArrayListWithExpectedSize(properties.keySet().size());
    for (String regionName : properties.keySet()) {
      Currency ccy = Currency.of(properties.getString(regionName));
      ManageableRegion region = new ManageableRegion();
      region.setName(regionName);
      region.setFullName(regionName);
      region.setExternalIdBundle(ExternalIdBundle.of(regionScheme, regionName));
      region.setCurrency(ccy);
      regions.add(region);
    }
    return regions;
  }

}
