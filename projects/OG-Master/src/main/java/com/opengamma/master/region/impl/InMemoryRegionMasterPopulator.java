/*
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.region.impl;

import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

import com.google.common.collect.Lists;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.region.ManageableRegion;
import com.opengamma.master.region.RegionDocument;
import com.opengamma.master.region.RegionMaster;
import com.opengamma.util.money.Currency;

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
