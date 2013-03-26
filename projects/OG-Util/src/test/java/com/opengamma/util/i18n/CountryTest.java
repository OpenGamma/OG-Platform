/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.i18n;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.testng.annotations.Test;

import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;

/**
 * Test Country.
 */
@Test(groups = TestGroup.UNIT)
public class CountryTest {

  //-----------------------------------------------------------------------
  // constants
  //-----------------------------------------------------------------------
  public void test_constants() {
    assertEquals(Country.EU, Country.of("EU"));
    assertEquals(Country.BE, Country.of("BE"));
    assertEquals(Country.CH, Country.of("CH"));
    assertEquals(Country.CZ, Country.of("CZ"));
    assertEquals(Country.DE, Country.of("DE"));
    assertEquals(Country.DK, Country.of("DK"));
    assertEquals(Country.ES, Country.of("ES"));
    assertEquals(Country.FI, Country.of("FI"));
    assertEquals(Country.FR, Country.of("FR"));
    assertEquals(Country.GB, Country.of("GB"));
    assertEquals(Country.GR, Country.of("GR"));
    assertEquals(Country.HU, Country.of("HU"));
    assertEquals(Country.IT, Country.of("IT"));
    assertEquals(Country.NL, Country.of("NL"));
    assertEquals(Country.NO, Country.of("NO"));
    assertEquals(Country.PT, Country.of("PT"));
    assertEquals(Country.SK, Country.of("SK"));
    assertEquals(Country.PL, Country.of("PL"));
    assertEquals(Country.RU, Country.of("RU"));
    assertEquals(Country.SE, Country.of("SE"));

    assertEquals(Country.AR, Country.of("AR"));
    assertEquals(Country.BR, Country.of("BR"));
    assertEquals(Country.CA, Country.of("CA"));
    assertEquals(Country.CL, Country.of("CL"));
    assertEquals(Country.MX, Country.of("MX"));
    assertEquals(Country.US, Country.of("US"));

    assertEquals(Country.AU, Country.of("AU"));
    assertEquals(Country.CN, Country.of("CN"));
    assertEquals(Country.HK, Country.of("HK"));
    assertEquals(Country.IN, Country.of("IN"));
    assertEquals(Country.JP, Country.of("JP"));
    assertEquals(Country.NZ, Country.of("NZ"));
    assertEquals(Country.TH, Country.of("TH"));
  }

  //-----------------------------------------------------------------------
  // of(String)
  //-----------------------------------------------------------------------
  public void test_of_String() {
    Country test = Country.of("SE");
    assertSame(Country.SE, test);
    assertEquals("SE", test.getCode());
    assertSame(Country.of("SE"), test);
  }

  public void test_of_String_unknownCountryCreated() {
    Country test = Country.of("ZY");
    assertEquals("ZY", test.getCode());
    assertSame(Country.of("ZY"), test);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_of_String_lowerCase() {
    try {
      Country.of("gb");
    } catch (IllegalArgumentException ex) {
      assertEquals("Invalid country code: gb", ex.getMessage());
      throw ex;
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_of_String_empty() {
    Country.of("");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_of_String_tooShort() {
    Country.of("A");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_of_String_tooLong() {
    Country.of("ABC");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_of_String_nullString() {
    Country.of((String) null);
  }

  //-----------------------------------------------------------------------
  // parse(String)
  //-----------------------------------------------------------------------
  public void test_parse_String() {
    Country test = Country.parse("GB");
    assertEquals("GB", test.getCode());
    assertSame(Country.GB, test);
  }

  public void test_parse_String_unknownCountryCreated() {
    Country test = Country.parse("ZX");
    assertEquals("ZX", test.getCode());
    assertSame(Country.of("ZX"), test);
  }

  public void test_parse_String_lowerCase() {
    Country test = Country.parse("gb");
    assertEquals("GB", test.getCode());
    assertSame(Country.GB, test);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_parse_String_empty() {
    Country.parse("");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_parse_String_tooShort() {
    Country.parse("A");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_parse_String_tooLong() {
    Country.parse("ABC");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_parse_String_nullString() {
    Country.parse((String) null);
  }

  //-----------------------------------------------------------------------
  // Serialisation
  //-----------------------------------------------------------------------
  public void test_serialization_GB() throws Exception {
    Country cu = Country.of("GB");
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(baos);
    oos.writeObject(cu);
    oos.close();
    ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
    Country input = (Country) ois.readObject();
    assertSame(input, cu);
  }

  public void test_serialization_AA() throws Exception {
    Country cu = Country.of("AA");
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(baos);
    oos.writeObject(cu);
    oos.close();
    ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
    Country input = (Country) ois.readObject();
    assertSame(input, cu);
  }

  //-----------------------------------------------------------------------
  // gets
  //-----------------------------------------------------------------------
  public void test_gets() {
    Country test = Country.of("GB");
    assertEquals("GB", test.getCode());
    assertEquals(ObjectId.of("CountryISO", "GB"), test.getObjectId());
    assertEquals(UniqueId.of("CountryISO", "GB"), test.getUniqueId());
  }

  //-----------------------------------------------------------------------
  // compareTo()
  //-----------------------------------------------------------------------
  public void test_compareTo() {
    Country a = Country.FR;
    Country b = Country.GB;
    Country c = Country.JP;
    assertEquals(a.compareTo(a), 0);
    assertEquals(b.compareTo(b), 0);
    assertEquals(c.compareTo(c), 0);

    assertTrue(a.compareTo(b) < 0);
    assertTrue(b.compareTo(a) > 0);

    assertTrue(a.compareTo(c) < 0);
    assertTrue(c.compareTo(a) > 0);

    assertTrue(b.compareTo(c) < 0);
    assertTrue(c.compareTo(b) > 0);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void test_compareTo_null() {
    Country.AU.compareTo(null);
  }

  //-----------------------------------------------------------------------
  // equals() hashCode()
  //-----------------------------------------------------------------------
  public void test_equals_hashCode() {
    Country a = Country.GB;
    Country b = Country.of("GB");
    Country c = Country.FR;
    assertEquals(a.equals(a), true);
    assertEquals(b.equals(b), true);
    assertEquals(c.equals(c), true);

    assertEquals(a.equals(b), true);
    assertEquals(b.equals(a), true);
    assertEquals(a.hashCode() == b.hashCode(), true);

    assertEquals(a.equals(c), false);
    assertEquals(b.equals(c), false);
  }

  public void test_equals_false() {
    Country a = Country.GB;
    assertEquals(a.equals(null), false);
    assertEquals(a.equals("String"), false);
    assertEquals(a.equals(new Object()), false);
  }

  //-----------------------------------------------------------------------
  // toString()
  //-----------------------------------------------------------------------
  public void test_toString() {
    Country test = Country.GB;
    assertEquals(test.toString(), "GB");
  }

}
