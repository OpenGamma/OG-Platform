/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.obligor;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Tests the migration from region enums to objects.
 */
@Test(groups = TestGroup.UNIT)
public class RegionDelegateTest {

  /**
   * Tests the enums are correctly converted into objects.
   */
  @SuppressWarnings("deprecation")
  @Test
  public void test() {
    assertEquals(com.opengamma.analytics.financial.legalentity.Region.of(Region.AFRICA.toString()), Region.AFRICA.toRegion());
    assertEquals(com.opengamma.analytics.financial.legalentity.Region.of(Region.ASIA.toString()), Region.ASIA.toRegion());
    assertEquals(com.opengamma.analytics.financial.legalentity.Region.of(Region.CARIBBEAN.toString()), Region.CARIBBEAN.toRegion());
    assertEquals(com.opengamma.analytics.financial.legalentity.Region.of(Region.EASTERNEUROPE.toString()), Region.EASTERNEUROPE.toRegion());
    assertEquals(com.opengamma.analytics.financial.legalentity.Region.of(Region.EUROPE.toString()), Region.EUROPE.toRegion());
    assertEquals(com.opengamma.analytics.financial.legalentity.Region.of(Region.INDIA.toString()), Region.INDIA.toRegion());
    assertEquals(com.opengamma.analytics.financial.legalentity.Region.of(Region.LATINAMERICA.toString()), Region.LATINAMERICA.toRegion());
    assertEquals(com.opengamma.analytics.financial.legalentity.Region.of(Region.MIDDLEEAST.toString()), Region.MIDDLEEAST.toRegion());
    assertEquals(com.opengamma.analytics.financial.legalentity.Region.of(Region.NORTHAMERICA.toString()), Region.NORTHAMERICA.toRegion());
    assertEquals(com.opengamma.analytics.financial.legalentity.Region.of(Region.OCEANIA.toString()), Region.OCEANIA.toRegion());
    assertEquals(com.opengamma.analytics.financial.legalentity.Region.of(Region.OFFSHORE.toString()), Region.OFFSHORE.toRegion());
    assertEquals(com.opengamma.analytics.financial.legalentity.Region.of(Region.PACIFIC.toString()), Region.PACIFIC.toRegion());
    assertEquals(com.opengamma.analytics.financial.legalentity.Region.of(Region.SUPRA.toString()), Region.SUPRA.toRegion());
    assertEquals(com.opengamma.analytics.financial.legalentity.Region.of(Region.NONE.toString()), Region.NONE.toRegion());
  }
}
