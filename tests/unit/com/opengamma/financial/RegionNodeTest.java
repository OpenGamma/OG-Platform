/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.world.region.DefaultRegionSource;
import com.opengamma.financial.world.region.InMemoryRegionMaster;
import com.opengamma.financial.world.region.Region;
import com.opengamma.financial.world.region.RegionFileReader;
import com.opengamma.financial.world.region.RegionMaster;
import com.opengamma.financial.world.region.RegionSource;
import com.opengamma.id.Identifier;

public class RegionNodeTest {

  private static final Logger s_logger = LoggerFactory.getLogger(RegionNodeTest.class);

  private RegionMaster _regionRepository1;
  private RegionSource _regionSource1;
  private RegionMaster _regionRepository2;
  private RegionSource _regionSource2;

  @Before
  public void setup() {
    // Use two repositories so that objects are not referentially equal
    _regionRepository1 = new InMemoryRegionMaster();
    RegionFileReader.populateMaster(_regionRepository1, new File(RegionFileReader.REGIONS_FILE_PATH));
    _regionSource1 = new DefaultRegionSource(_regionRepository1);
    _regionRepository2 = new InMemoryRegionMaster();
    RegionFileReader.populateMaster(_regionRepository2, new File(RegionFileReader.REGIONS_FILE_PATH));
    _regionSource2 = new DefaultRegionSource(_regionRepository2);
  }

  @Test
  public void testHashCode () {
    final Region ukRegion = _regionSource1.getHighestLevelRegion(Identifier.of(InMemoryRegionMaster.ISO_COUNTRY_2, "GB"));
    int hc = ukRegion.hashCode();
    s_logger.debug("Hashcode = {}", hc);
  }

  @Test
  public void testEquals() {
    final Region ukRegion1 = _regionSource1.getHighestLevelRegion(Identifier.of(InMemoryRegionMaster.ISO_COUNTRY_2, "GB"));
    final Region usRegion = _regionSource1.getHighestLevelRegion(Identifier.of(InMemoryRegionMaster.ISO_COUNTRY_2, "US"));
    final Region ukRegion2 = _regionSource2.getHighestLevelRegion(Identifier.of(InMemoryRegionMaster.ISO_COUNTRY_2, "GB"));
    assertTrue(ukRegion1.equals(ukRegion1));
    assertFalse(ukRegion1.equals(usRegion));
    assertTrue(ukRegion1.equals(ukRegion2));
    assertFalse(usRegion.equals(ukRegion1));
    assertTrue(usRegion.equals(usRegion));
    assertFalse(usRegion.equals(ukRegion2));
    assertTrue(ukRegion2.equals(ukRegion1));
    assertFalse(ukRegion2.equals(usRegion));
    assertTrue(ukRegion2.equals(ukRegion2));
  }

}
