/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.integration.tool.portfolio.xml.SchemaVersion;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class SchemaVersionTest {

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullVersionIsNotAllowed() {
    new SchemaVersion(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNonNumericVersionIsNotAllowed() {
    new SchemaVersion("1.a");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testTrailingNonNumericIsNotAllowed() {
    new SchemaVersion("1.4a");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testLeadingNonNumericIsNotAllowed() {
    new SchemaVersion("a1.4");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testMajorVersionOnlyIsNotAllowed() {
    new SchemaVersion("1.");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testMinorVersionOnlyIsNotAllowed() {
    new SchemaVersion(".1");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testDigitsOnlyIsNotAllowed() {
    new SchemaVersion("1");
  }

  @Test
  public void testEquality() {

    SchemaVersion sv1 = new SchemaVersion("3.4");
    SchemaVersion sv2 = new SchemaVersion("3.4");
    assertEquals(sv1, sv2);
  }

  @Test
  public void testMajorOverridesMinor() {

    SchemaVersion sv1 = new SchemaVersion("3.4");
    SchemaVersion sv2 = new SchemaVersion("4.1");
    assertTrue(sv1.compareTo(sv2) < 0);
  }

  @Test
  public void testMinorUsedIfMajorAreSame() {

    SchemaVersion sv1 = new SchemaVersion("3.4");
    SchemaVersion sv2 = new SchemaVersion("3.5");
    assertTrue(sv1.compareTo(sv2) < 0);
  }

  @Test
  public void testIdenticalCompareSame() {

    SchemaVersion sv1 = new SchemaVersion("3.4");
    SchemaVersion sv2 = new SchemaVersion("3.4");
    assertTrue(sv1.compareTo(sv2) == 0);
  }

  @Test
  public void testLeadingZeroesAreIgnoredForOutput() {

    SchemaVersion sv = new SchemaVersion("003.00004");
    assertEquals(sv.toString(), "3.4");
  }

  @Test
  public void testLeadingZeroesAreIgnoredForEquality() {

    SchemaVersion sv1 = new SchemaVersion("003.00004");
    SchemaVersion sv2 = new SchemaVersion("3.4");
    assertEquals(sv1, sv2);
  }
}
