/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.world.region.Region;
import com.opengamma.financial.world.region.RegionUtils;

@Ignore
public class RegionTest extends FinancialTestBase {

  private final Logger s_logger = LoggerFactory.getLogger(getClass());
  
  private Region getRef() {
    return getRegionSource().getHighestLevelRegion(RegionUtils.countryRegionId("GB"));
  }

  @Test
  public void testCycle() {
    final Region ref = getRef();
    final Region cycledRef = cycleObject(Region.class, ref);
    s_logger.info("pre = " + ref);
    s_logger.info("post=" + cycledRef);
    assertEquals(ref, cycledRef);
  }

}
