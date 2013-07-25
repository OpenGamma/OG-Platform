/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.money;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertSame;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.Test;

import com.google.common.collect.Sets;
import com.opengamma.util.test.TestGroup;

/**
 * Test MultipleCurrencyAmount.
 */
@Test(groups = TestGroup.UNIT)
public class MultipleCurrencyAmountTest {

  private static final Currency CCY1 = Currency.AUD;
  private static final Currency CCY2 = Currency.CAD;
  private static final Currency CCY3 = Currency.CHF;
  private static final double A1 = 101;
  private static final double A2 = 103;
  private static final double A3 = 107;
  private static final CurrencyAmount CA1 = CurrencyAmount.of(CCY1, A1);
  private static final CurrencyAmount CA2 = CurrencyAmount.of(CCY2, A2);
  private static final CurrencyAmount CA3 = CurrencyAmount.of(CCY3, A3);
  private static final Currency[] CCY_ARRAY;
  private static final double[] A_ARRAY;
  private static final List<Currency> CCY_LIST;
  private static final List<Double> A_LIST;
  private static final Map<Currency, Double> CCY_A_MAP;
  private static final CurrencyAmount[] CA_ARRAY;
  private static final List<CurrencyAmount> CA_LIST;
  private static final Set<CurrencyAmount> CA_SET;
  private static final MultipleCurrencyAmount MULTIPLE;

  static {
    CCY_ARRAY = new Currency[] {CCY1, CCY2, CCY3 };
    A_ARRAY = new double[] {A1, A2, A3 };
    CCY_LIST = Arrays.asList(CCY_ARRAY);
    A_LIST = Arrays.asList(A1, A2, A3);
    CCY_A_MAP = new HashMap<Currency, Double>();
    CCY_A_MAP.put(CCY1, A1);
    CCY_A_MAP.put(CCY2, A2);
    CCY_A_MAP.put(CCY3, A3);
    CA_ARRAY = new CurrencyAmount[] {CA1, CA2, CA3 };
    CA_LIST = Arrays.asList(CA_ARRAY);
    CA_SET = Sets.newHashSet(CA_ARRAY);
    MULTIPLE = MultipleCurrencyAmount.of(CA_ARRAY);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrency() {
    MultipleCurrencyAmount.of(null, A1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrencyArray() {
    MultipleCurrencyAmount.of(null, A_ARRAY);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInCurrencyArray() {
    MultipleCurrencyAmount.of(new Currency[] {CCY1, null, CCY2 }, A_ARRAY);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAmountArray() {
    MultipleCurrencyAmount.of(CCY_ARRAY, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLengthAmountArray() {
    MultipleCurrencyAmount.of(CCY_ARRAY, new double[] {A1, A2 });
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrencyList() {
    MultipleCurrencyAmount.of(null, A_LIST);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInCurrencyList() {
    MultipleCurrencyAmount.of(Arrays.asList(CCY1, null, CCY2), A_LIST);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAmountList() {
    MultipleCurrencyAmount.of(CCY_LIST, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInAmountList() {
    MultipleCurrencyAmount.of(CCY_LIST, Arrays.asList(null, A2, A3));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLengthAmountList() {
    MultipleCurrencyAmount.of(CCY_LIST, Arrays.asList(A1, A2));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullMap() {
    MultipleCurrencyAmount.of((Map<Currency, Double>) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrencyInMap() {
    final Map<Currency, Double> map = new HashMap<Currency, Double>();
    map.put(CCY1, A1);
    map.put(null, A2);
    map.put(CCY3, A3);
    MultipleCurrencyAmount.of(map);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAmountInMap() {
    final Map<Currency, Double> map = new HashMap<Currency, Double>();
    map.put(CCY1, A1);
    map.put(CCY2, null);
    map.put(CCY3, A3);
    MultipleCurrencyAmount.of(map);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrencyAmountArray() {
    MultipleCurrencyAmount.of((CurrencyAmount[]) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInCurrencyAmountArray() {
    MultipleCurrencyAmount.of(new CurrencyAmount[] {null, CA2, CA3 });
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrencyAmountList() {
    MultipleCurrencyAmount.of((List<CurrencyAmount>) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInCurrencyAmountList() {
    MultipleCurrencyAmount.of(Arrays.asList(null, CA2, CA3));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrencyAmountSet() {
    MultipleCurrencyAmount.of((Set<CurrencyAmount>) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInCurrencyAmountSet() {
    MultipleCurrencyAmount.of(Sets.newHashSet(null, CA2, CA3));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetAmountNullCurrency() {
    MULTIPLE.getAmount(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetAmountWrongCurrency() {
    MULTIPLE.getAmount(Currency.DEM);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetCurrencyAmountNullCurrency() {
    MULTIPLE.getCurrencyAmount(null);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testPlusNullCurrencyAmount() {
    MULTIPLE.plus((CurrencyAmount) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testPlusNullCurrency() {
    MULTIPLE.plus(null, A1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testPlusNullMultipleCurrencyAmount() {
    MULTIPLE.plus((MultipleCurrencyAmount) null);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWithoutNull() {
    MULTIPLE.without(null);
  }

  //-------------------------------------------------------------------------
  @Test
  public void testObject() {
    assertEquals(MULTIPLE.size(), CA_LIST.size());
    final CurrencyAmount[] ca = MULTIPLE.getCurrencyAmounts();
    assertEquals(ca.length, CA_SET.size());
    for (final CurrencyAmount element : ca) {
      assertTrue(CA_SET.contains(element));
    }
    MultipleCurrencyAmount other = MultipleCurrencyAmount.of(CA_ARRAY);
    assertEquals(MULTIPLE, other);
    assertEquals(MULTIPLE.hashCode(), other.hashCode());
    CurrencyAmount[] array = new CurrencyAmount[] {CurrencyAmount.of(CCY1, A1), CurrencyAmount.of(CCY1, A2),
        CurrencyAmount.of(CCY1, A3) };
    other = MultipleCurrencyAmount.of(array);
    assertFalse(MULTIPLE.equals(other));
    array = new CurrencyAmount[] {CurrencyAmount.of(CCY1, A1), CurrencyAmount.of(CCY2, A1), CurrencyAmount.of(CCY3, A1) };
    other = MultipleCurrencyAmount.of(array);
    assertFalse(MULTIPLE.equals(other));
    assertTrue(MULTIPLE.equals(MULTIPLE));
  }

  @Test
  public void testEqualsRubbish() {
    assertFalse(MULTIPLE.equals(""));
    assertFalse(MULTIPLE.equals(null));
  }

  @Test
  public void testToString() {
    assertTrue(MULTIPLE.toString().contains(CA1.toString()));
    assertTrue(MULTIPLE.toString().contains(CA2.toString()));
    assertTrue(MULTIPLE.toString().contains(CA3.toString()));
  }

  @Test
  public void testConstructors() {
    assertEquals(MULTIPLE, MultipleCurrencyAmount.of(CCY_LIST, A_LIST));
    assertEquals(MULTIPLE, MultipleCurrencyAmount.of(CCY_A_MAP));
    assertEquals(MULTIPLE, MultipleCurrencyAmount.of(CA_ARRAY));
    assertEquals(MULTIPLE, MultipleCurrencyAmount.of(CA_LIST));
    assertEquals(MULTIPLE, MultipleCurrencyAmount.of(CA_SET));
  }

  @Test
  public void testStaticConstruction() {
    assertEquals(MultipleCurrencyAmount.of(CCY1, A1), MultipleCurrencyAmount.of(CCY1, A1));
    assertEquals(MULTIPLE, MultipleCurrencyAmount.of(CCY_LIST, A_LIST));
    assertEquals(MULTIPLE, MultipleCurrencyAmount.of(CCY_A_MAP));
    assertEquals(MULTIPLE, MultipleCurrencyAmount.of(CA_ARRAY));
    assertEquals(MULTIPLE, MultipleCurrencyAmount.of(CA_LIST));
    assertEquals(MULTIPLE, MultipleCurrencyAmount.of(CA_SET));
  }

  @Test
  public void testConstructionRepeatedCurrencies() {
    final CurrencyAmount ca4 = CurrencyAmount.of(CCY1, A1 * 2);
    final CurrencyAmount ca5 = CurrencyAmount.of(CCY2, A2 * 3);
    final Currency[] ccyArray = new Currency[] {CCY1, CCY2, CCY3, ca4.getCurrency(), ca5.getCurrency() };
    final double[] aArray = new double[] {A1, A2, A3, ca4.getAmount(), ca5.getAmount() };
    final List<Currency> ccyList = Arrays.asList(ccyArray);
    final List<Double> aList = Arrays.asList(A1, A2, A3, A1 * 2, A2 * 3);
    final CurrencyAmount[] caArray = new CurrencyAmount[] {CA1, CA2, CA3, ca4, ca5 };
    final List<CurrencyAmount> caList = Arrays.asList(caArray);
    final HashSet<CurrencyAmount> caSet = Sets.newHashSet(caArray);
    final Set<CurrencyAmount> expected = Sets.newHashSet(CurrencyAmount.of(CCY1, A1 * 3),
        CurrencyAmount.of(CCY2, A2 * 4), CA3);
    assertSameData(expected, MultipleCurrencyAmount.of(ccyArray, aArray));
    assertSameData(expected, MultipleCurrencyAmount.of(ccyList, aList));
    assertSameData(expected, MultipleCurrencyAmount.of(caArray));
    assertSameData(expected, MultipleCurrencyAmount.of(caList));
    assertSameData(expected, MultipleCurrencyAmount.of(caSet));
  }

  @Test
  public void testGetAmount() {
    for (int i = 0; i < CCY_ARRAY.length; i++) {
      assertEquals(MULTIPLE.getAmount(CCY_ARRAY[i]), A_ARRAY[i]);
      assertEquals(MULTIPLE.getCurrencyAmount(CCY_ARRAY[i]), CA_ARRAY[i]);
    }
  }

  @Test
  public void testGetCurrencyAmountWrongCurrency() {
    assertEquals(null, MULTIPLE.getCurrencyAmount(Currency.DEM));
  }

  //-------------------------------------------------------------------------
  @Test
  public void testPlus1() {
    final double a = 117;
    CurrencyAmount ca = CurrencyAmount.of(Currency.CZK, a);
    Set<CurrencyAmount> expected = new HashSet<CurrencyAmount>(CA_SET);
    expected.add(ca);
    MultipleCurrencyAmount mca = MultipleCurrencyAmount.of(CA_ARRAY);
    MultipleCurrencyAmount test = mca.plus(ca);
    assertSameData(expected, test);
  }

  @Test
  public void testPlus2() {
    final double a = 117;
    CurrencyAmount ca = CurrencyAmount.of(Currency.AUD, a);
    Set<CurrencyAmount> expected = Sets.newHashSet(CA1.plus(ca), CA2, CA3);
    MultipleCurrencyAmount mca = MultipleCurrencyAmount.of(CA_LIST);
    MultipleCurrencyAmount test = mca.plus(ca);
    assertSameData(expected, test);
  }

  @Test
  public void testPlus_CurrencyAmount() {
    CurrencyAmount ca = CurrencyAmount.of(Currency.AUD, 117);
    CurrencyAmount cb = CurrencyAmount.of(Currency.USD, 12);
    CurrencyAmount cc = CurrencyAmount.of(Currency.AUD, 3);
    CurrencyAmount cd = CurrencyAmount.of(Currency.NZD, 3);
    MultipleCurrencyAmount mc1 = MultipleCurrencyAmount.of(ca, cb);
    MultipleCurrencyAmount mc2 = MultipleCurrencyAmount.of(cc, cd);
    Set<CurrencyAmount> expected = Sets.newHashSet(cb, cd, CurrencyAmount.of(Currency.AUD, 120));
    MultipleCurrencyAmount test = mc1.plus(mc2);
    assertSameData(expected, test);
  }

  //-------------------------------------------------------------------------
  @Test
  public void testMultipliedBy() {
    final double factor = 2.5;
    MultipleCurrencyAmount mca1 = MultipleCurrencyAmount.of(CA1);
    MultipleCurrencyAmount multiplied1 = mca1.multipliedBy(factor);
    MultipleCurrencyAmount expected1 = MultipleCurrencyAmount.of(CCY1, A1*factor);
    assertEquals(expected1, multiplied1, "MultipleCurrencyAmount: multipliedBy");
    MultipleCurrencyAmount mca2 = MultipleCurrencyAmount.of(CA2, CA3);
    MultipleCurrencyAmount multiplied2 = mca2.multipliedBy(factor);
    MultipleCurrencyAmount expected2 = MultipleCurrencyAmount.of(CCY2, A2*factor).plus(CCY3, A3*factor);
    assertEquals(expected2, multiplied2, "MultipleCurrencyAmount: multipliedBy");
  }

  //-------------------------------------------------------------------------
  @Test
  public void testWithout() {
    final Set<CurrencyAmount> expected = Sets.newHashSet(CA1, CA3);
    final MultipleCurrencyAmount mca = MultipleCurrencyAmount.of(CCY_LIST, A_LIST);
    final MultipleCurrencyAmount test = mca.without(CCY2);
    assertSameData(expected, test);
  }

  @Test
  public void testWithoutToEmpty() {
    final Set<CurrencyAmount> expected = Sets.newHashSet();
    final MultipleCurrencyAmount mca = MultipleCurrencyAmount.of(CA1);
    final MultipleCurrencyAmount test = mca.without(CCY1);
    assertSameData(expected, test);
  }

  @Test
  public void testWithoutEmpty() {
    final Set<CurrencyAmount> expected = Sets.newHashSet();
    final MultipleCurrencyAmount mca = MultipleCurrencyAmount.of();
    final MultipleCurrencyAmount test = mca.without(CCY1);
    assertSameData(expected, test);
  }

  @Test
  public void testWithoutKeyNotPresent() {
    assertSame(MULTIPLE, MULTIPLE.without(Currency.DEM));
  }

  //-------------------------------------------------------------------------
  @Test
  public void testWith() {
    final double a = A2 * 10;
    final CurrencyAmount ca = CurrencyAmount.of(CCY2, a);
    final Set<CurrencyAmount> expected = Sets.newHashSet(CA1, ca, CA3);
    final MultipleCurrencyAmount mca = MultipleCurrencyAmount.of(CCY_LIST, A_LIST);
    final MultipleCurrencyAmount test = mca.with(CCY2, a);
    assertSameData(expected, test);
  }

  @Test
  public void testWithKeyNotPresent() {
    final Set<CurrencyAmount> expected = Sets.newHashSet(CA1, CA2, CA3, CurrencyAmount.of(Currency.DEM, A1));
    MultipleCurrencyAmount test = MULTIPLE.with(Currency.DEM, A1);
    assertSameData(expected, test);
  }

  //-------------------------------------------------------------------------
  private void assertSameData(final Set<CurrencyAmount> expected, final MultipleCurrencyAmount actual) {
    final CurrencyAmount[] amounts = actual.getCurrencyAmounts();
    assertEquals(amounts.length, expected.size());
    for (final CurrencyAmount amount : amounts) {
      assertTrue(expected.contains(amount), "Expected: " + expected + " but found: " + amount);
    }
  }
}
