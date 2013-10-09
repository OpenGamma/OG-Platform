/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.money;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Set;

import org.testng.annotations.Test;

import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;

/**
 * Test Currency.
 */
@Test(groups = TestGroup.UNIT)
public class CurrencyTest {

  //-----------------------------------------------------------------------
  // constants
  //-----------------------------------------------------------------------
  public void test_constants() {
    assertEquals(Currency.USD, Currency.of("USD"));
    assertEquals(Currency.EUR, Currency.of("EUR"));
    assertEquals(Currency.JPY, Currency.of("JPY"));
    assertEquals(Currency.GBP, Currency.of("GBP"));
    assertEquals(Currency.CHF, Currency.of("CHF"));
    assertEquals(Currency.AUD, Currency.of("AUD"));
    assertEquals(Currency.CAD, Currency.of("CAD"));
  }

  //-----------------------------------------------------------------------
  // getAvailableCurrencies()
  //-----------------------------------------------------------------------
  public void test_getAvailable() {
    Set<Currency> available = Currency.getAvailableCurrencies();
    assertTrue(available.contains(Currency.USD));
    assertTrue(available.contains(Currency.EUR));
    assertTrue(available.contains(Currency.JPY));
    assertTrue(available.contains(Currency.GBP));
    assertTrue(available.contains(Currency.CHF));
    assertTrue(available.contains(Currency.AUD));
    assertTrue(available.contains(Currency.CAD));
  }

  //-----------------------------------------------------------------------
  // of(Currency)
  //-----------------------------------------------------------------------
  public void test_of_Currency() {
    Currency test = Currency.of(java.util.Currency.getInstance("GBP"));
    assertEquals("GBP", test.getCode());
    assertSame(Currency.GBP, test);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_of_Currency_nullCurrency() {
    Currency.of((java.util.Currency) null);
  }

  //-----------------------------------------------------------------------
  // of(String)
  //-----------------------------------------------------------------------
  public void test_of_String() {
    Currency test = Currency.of("SEK");
    assertEquals("SEK", test.getCode());
    assertSame(Currency.of("SEK"), test);
  }

  public void test_of_String_unknownCurrencyCreated() {
    Currency test = Currency.of("AAA");
    assertEquals("AAA", test.getCode());
    assertSame(Currency.of("AAA"), test);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_of_String_lowerCase() {
    try {
      Currency.of("gbp");
    } catch (IllegalArgumentException ex) {
      assertEquals("Invalid currency code: gbp", ex.getMessage());
      throw ex;
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_of_String_empty() {
    Currency.of("");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_of_String_tooShort() {
    Currency.of("AB");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_of_String_tooLong() {
    Currency.of("ABCD");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_of_String_nullString() {
    Currency.of((String) null);
  }

  //-----------------------------------------------------------------------
  // parse(String)
  //-----------------------------------------------------------------------
  public void test_parse_String() {
    Currency test = Currency.parse("GBP");
    assertEquals("GBP", test.getCode());
    assertSame(Currency.GBP, test);
  }

  public void test_parse_String_unknownCurrencyCreated() {
    Currency test = Currency.parse("AAA");
    assertEquals("AAA", test.getCode());
    assertSame(Currency.of("AAA"), test);
  }

  public void test_parse_String_lowerCase() {
    Currency test = Currency.parse("gbp");
    assertEquals("GBP", test.getCode());
    assertSame(Currency.GBP, test);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_parse_String_empty() {
    Currency.parse("");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_parse_String_tooShort() {
    Currency.parse("AB");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_parse_String_tooLong() {
    Currency.parse("ABCD");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_parse_String_nullString() {
    Currency.parse((String) null);
  }

  //-----------------------------------------------------------------------
  // Serialisation
  //-----------------------------------------------------------------------
  public void test_serialization_GBP() throws Exception {
    Currency cu = Currency.of("GBP");
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(baos);
    oos.writeObject(cu);
    oos.close();
    ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
    Currency input = (Currency) ois.readObject();
    assertSame(input, cu);
  }

  public void test_serialization_AAB() throws Exception {
    Currency cu = Currency.of("AAB");
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(baos);
    oos.writeObject(cu);
    oos.close();
    ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
    Currency input = (Currency) ois.readObject();
    assertSame(input, cu);
  }

  //-----------------------------------------------------------------------
  // gets
  //-----------------------------------------------------------------------
  public void test_gets() {
    Currency test = Currency.of("GBP");
    assertEquals("GBP", test.getCode());
    assertEquals(ObjectId.of("CurrencyISO", "GBP"), test.getObjectId());
    assertEquals(UniqueId.of("CurrencyISO", "GBP"), test.getUniqueId());
    assertEquals(java.util.Currency.getInstance("GBP"), test.toCurrency());
  }

  //-----------------------------------------------------------------------
  // compareTo()
  //-----------------------------------------------------------------------
  public void test_compareTo() {
    Currency a = Currency.EUR;
    Currency b = Currency.GBP;
    Currency c = Currency.JPY;
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
    Currency.EUR.compareTo(null);
  }

  //-----------------------------------------------------------------------
  // equals() hashCode()
  //-----------------------------------------------------------------------
  public void test_equals_hashCode() {
    Currency a = Currency.GBP;
    Currency b = Currency.of("GBP");
    Currency c = Currency.EUR;
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
    Currency a = Currency.GBP;
    assertEquals(a.equals(null), false);
    assertEquals(a.equals("String"), false);
    assertEquals(a.equals(new Object()), false);
  }

  //-----------------------------------------------------------------------
  // toString()
  //-----------------------------------------------------------------------
  public void test_toString() {
    Currency test = Currency.GBP;
    assertEquals(test.toString(), "GBP");
  }

}
