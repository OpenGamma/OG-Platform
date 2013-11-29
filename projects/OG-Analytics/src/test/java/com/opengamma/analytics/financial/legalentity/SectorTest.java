/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.legalentity;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import java.util.HashMap;
import java.util.Map;

import org.joda.beans.impl.flexi.FlexiBean;
import org.testng.annotations.Test;

import com.opengamma.analytics.financial.legalentity.GICSCode;
import com.opengamma.analytics.financial.legalentity.ICBCode;
import com.opengamma.analytics.financial.legalentity.Sector;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the sector object.
 */
@Test(groups = TestGroup.UNIT)
public class SectorTest {

  /**
   * Tests failure on null name
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullName1() {
    Sector.of(null);
  }

  /**
   * Test failure on null name
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullName2() {
    Sector.of(null, new FlexiBean());
  }

  /**
   * Tests failure on null classifications
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullClassifications() {
    Sector.of("SECTOR", null);
  }

  /**
   * Tests the object
   */
  @Test
  public void testObject() {
    Sector sector = Sector.of("NAME");
    assertEquals("NAME", sector.getName());
    final FlexiBean classifications = new FlexiBean();
    assertEquals(classifications, sector.getClassifications());
    Sector other = Sector.of("NAME");
    assertEquals(sector, other);
    assertEquals(sector.hashCode(), other.hashCode());
    other = Sector.of("OTHER");
    assertFalse(sector.equals(other));
    final Map<String, Object> map = new HashMap<>();
    map.put("GICS", GICSCode.of(10203040));
    map.put("ICB", ICBCode.of("3456"));
    classifications.putAll(map);
    sector = Sector.of("NAME", classifications);
    other = Sector.of("NAME", classifications);
    assertEquals(sector, other);
    assertEquals(sector.hashCode(), other.hashCode());
    other = Sector.of("OTHER", classifications);
    assertFalse(sector.equals(other));
    final FlexiBean moreClassifications = new FlexiBean();
    moreClassifications.putAll(map);
    moreClassifications.append("A", "B");
    other = Sector.of("NAME", moreClassifications);
    assertFalse(sector.equals(other));
  }
}
