/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Currency;

import org.junit.Test;

import com.opengamma.id.ObjectIdentifier;
import com.opengamma.id.UniqueIdentifier;

/**
 * Test CurrencyUnit.
 */
public class CurrencyUnitTest {

  //-----------------------------------------------------------------------
  // constants
  //-----------------------------------------------------------------------
  @Test
  public void test_constants() {
      assertEquals(CurrencyUnit.USD, CurrencyUnit.of("USD"));
      assertEquals(CurrencyUnit.EUR, CurrencyUnit.of("EUR"));
      assertEquals(CurrencyUnit.JPY, CurrencyUnit.of("JPY"));
      assertEquals(CurrencyUnit.GBP, CurrencyUnit.of("GBP"));
      assertEquals(CurrencyUnit.CHF, CurrencyUnit.of("CHF"));
      assertEquals(CurrencyUnit.AUD, CurrencyUnit.of("AUD"));
      assertEquals(CurrencyUnit.CAD, CurrencyUnit.of("CAD"));
  }

  //-----------------------------------------------------------------------
  // of(Currency)
  //-----------------------------------------------------------------------
  @Test
  public void test_of_Currency() {
      CurrencyUnit test = CurrencyUnit.of(Currency.getInstance("GBP"));
      assertEquals("GBP", test.getCode());
      assertSame(CurrencyUnit.GBP, test);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_of_Currency_nullCurrency() {
      CurrencyUnit.of((Currency) null);
  }

  //-----------------------------------------------------------------------
  // of(String)
  //-----------------------------------------------------------------------
  @Test
  public void test_of_String() {
      CurrencyUnit test = CurrencyUnit.of("SEK");
      assertEquals("SEK", test.getCode());
      assertSame(CurrencyUnit.of("SEK"), test);
  }

  @Test
  public void test_of_String_unknownCurrencyCreated() {
    CurrencyUnit test = CurrencyUnit.of("AAA");
    assertEquals("AAA", test.getCode());
    assertSame(CurrencyUnit.of("AAA"), test);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_of_String_lowerCase() {
      try {
          CurrencyUnit.of("gbp");
      } catch (IllegalArgumentException ex) {
          assertEquals("Invalid currency code: gbp", ex.getMessage());
          throw ex;
      }
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_of_String_empty() {
      CurrencyUnit.of("");
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_of_String_tooShort() {
      CurrencyUnit.of("AB");
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_of_String_tooLong() {
      CurrencyUnit.of("ABCD");
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_of_String_nullString() {
      CurrencyUnit.of((String) null);
  }

  //-----------------------------------------------------------------------
  // parse(String)
  //-----------------------------------------------------------------------
  @Test
  public void test_parse_String() {
      CurrencyUnit test = CurrencyUnit.parse("GBP");
      assertEquals("GBP", test.getCode());
      assertSame(CurrencyUnit.GBP, test);
  }

  @Test
  public void test_parse_String_unknownCurrencyCreated() {
    CurrencyUnit test = CurrencyUnit.parse("AAA");
    assertEquals("AAA", test.getCode());
    assertSame(CurrencyUnit.of("AAA"), test);
  }

  @Test
  public void test_parse_String_lowerCase() {
    CurrencyUnit test = CurrencyUnit.parse("gbp");
    assertEquals("GBP", test.getCode());
    assertSame(CurrencyUnit.GBP, test);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_parse_String_empty() {
      CurrencyUnit.parse("");
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_parse_String_tooShort() {
      CurrencyUnit.parse("AB");
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_parse_String_tooLong() {
      CurrencyUnit.parse("ABCD");
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_parse_String_nullString() {
      CurrencyUnit.parse((String) null);
  }

  //-----------------------------------------------------------------------
  // Serialisation
  //-----------------------------------------------------------------------
  @Test
  public void test_serialization_GBP() throws Exception {
      CurrencyUnit cu = CurrencyUnit.of("GBP");
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      oos.writeObject(cu);
      oos.close();
      ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
      CurrencyUnit input = (CurrencyUnit) ois.readObject();
      assertSame(input, cu);
  }

  @Test
  public void test_serialization_AAB() throws Exception {
    CurrencyUnit cu = CurrencyUnit.of("AAB");
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(baos);
    oos.writeObject(cu);
    oos.close();
    ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
    CurrencyUnit input = (CurrencyUnit) ois.readObject();
    assertSame(input, cu);
  }

  //-----------------------------------------------------------------------
  // gets
  //-----------------------------------------------------------------------
  @Test
  public void test_gets() {
      CurrencyUnit test = CurrencyUnit.of("GBP");
      assertEquals("GBP", test.getCode());
      assertEquals(ObjectIdentifier.of("CurrencyISO", "GBP"), test.getObjectId());
      assertEquals(UniqueIdentifier.of("CurrencyISO", "GBP"), test.getUniqueId());
      assertEquals(Currency.getInstance("GBP"), test.toCurrency());
  }

  //-----------------------------------------------------------------------
  // compareTo()
  //-----------------------------------------------------------------------
  @Test
  public void test_compareTo() {
      CurrencyUnit a = CurrencyUnit.EUR;
      CurrencyUnit b = CurrencyUnit.GBP;
      CurrencyUnit c = CurrencyUnit.JPY;
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

  @Test(expected = NullPointerException.class)
  public void test_compareTo_null() {
      CurrencyUnit.EUR.compareTo(null);
  }

  //-----------------------------------------------------------------------
  // equals() hashCode()
  //-----------------------------------------------------------------------
  @Test
  public void test_equals_hashCode() {
      CurrencyUnit a = CurrencyUnit.GBP;
      CurrencyUnit b = CurrencyUnit.of("GBP");
      CurrencyUnit c = CurrencyUnit.EUR;
      assertEquals(a.equals(a), true);
      assertEquals(b.equals(b), true);
      assertEquals(c.equals(c), true);
      
      assertEquals(a.equals(b), true);
      assertEquals(b.equals(a), true);
      assertEquals(a.hashCode() == b.hashCode(), true);
      
      assertEquals(a.equals(c), false);
      assertEquals(b.equals(c), false);
  }

  @Test
  public void test_equals_false() {
      CurrencyUnit a = CurrencyUnit.GBP;
      assertEquals(a.equals(null), false);
      assertEquals(a.equals("String"), false);
      assertEquals(a.equals(new Object()), false);
  }

  //-----------------------------------------------------------------------
  // toString()
  //-----------------------------------------------------------------------
  @Test
  public void test_toString() {
      CurrencyUnit test = CurrencyUnit.GBP;
      assertEquals(test.toString(), "GBP");
  }

}
