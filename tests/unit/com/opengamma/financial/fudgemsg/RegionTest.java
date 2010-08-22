/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.engine.world.Region;
import com.opengamma.financial.InMemoryRegionRepository;
import com.opengamma.financial.RegionRepository;
import com.opengamma.financial.RegionSearchRequest;

public class RegionTest extends FinancialTestBase {

  private Region getRef() {
    return getRegionRepository().searchRegions(new RegionSearchRequest(RegionRepository.POLITICAL_HIERARCHY_NAME, InMemoryRegionRepository.ISO_COUNTRY_2, "UK")).getBestResult();
  }

  @Test
  public void testCycle() {
    final Region ref = getRef();
    assertEquals(ref, cycleObject(Region.class, ref));
  }

}
