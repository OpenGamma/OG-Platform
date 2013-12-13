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
 * Tests the delegation of the sector enum to a sector object.
 */
@Test(groups = TestGroup.UNIT)
@SuppressWarnings("deprecation")
public class SectorDelegateTest {

  /**
   * Tests the sector conversion.
   */
  @Test
  public void test() {
    assertEquals(com.opengamma.analytics.financial.legalentity.Sector.of(Sector.BASICMATERIALS.name()), Sector.BASICMATERIALS.toSector());
    assertEquals(com.opengamma.analytics.financial.legalentity.Sector.of(Sector.CONSUMERGOODS.name()), Sector.CONSUMERGOODS.toSector());
    assertEquals(com.opengamma.analytics.financial.legalentity.Sector.of(Sector.CONSUMERSERVICES.name()), Sector.CONSUMERSERVICES.toSector());
    assertEquals(com.opengamma.analytics.financial.legalentity.Sector.of(Sector.ENERGY.name()), Sector.ENERGY.toSector());
    assertEquals(com.opengamma.analytics.financial.legalentity.Sector.of(Sector.FINANCIALS.name()), Sector.FINANCIALS.toSector());
    assertEquals(com.opengamma.analytics.financial.legalentity.Sector.of(Sector.GOVERNMENT.name()), Sector.GOVERNMENT.toSector());
    assertEquals(com.opengamma.analytics.financial.legalentity.Sector.of(Sector.HEALTHCARE.name()), Sector.HEALTHCARE.toSector());
    assertEquals(com.opengamma.analytics.financial.legalentity.Sector.of(Sector.INDUSTRIALS.name()), Sector.INDUSTRIALS.toSector());
    assertEquals(com.opengamma.analytics.financial.legalentity.Sector.of(Sector.TECHNOLOGY.name()), Sector.TECHNOLOGY.toSector());
    assertEquals(com.opengamma.analytics.financial.legalentity.Sector.of(Sector.TELECOMMUNICATIONSERVICES.name()), Sector.TELECOMMUNICATIONSERVICES.toSector());
    assertEquals(com.opengamma.analytics.financial.legalentity.Sector.of(Sector.UTILITIES.name()), Sector.UTILITIES.toSector());
    assertEquals(com.opengamma.analytics.financial.legalentity.Sector.of(Sector.MUNICIPAL.name()), Sector.MUNICIPAL.toSector());
    assertEquals(com.opengamma.analytics.financial.legalentity.Sector.of(Sector.GOVERNMENT.name()), Sector.GOVERNMENT.toSector());
    assertEquals(com.opengamma.analytics.financial.legalentity.Sector.of(Sector.NONE.name()), Sector.NONE.toSector());
  }
}
