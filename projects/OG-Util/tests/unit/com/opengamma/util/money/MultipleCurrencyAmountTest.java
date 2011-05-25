/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.money;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.Test;

import com.google.common.collect.Sets;

/**
 * 
 */
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
    CCY_ARRAY = new Currency[] {CCY1, CCY2, CCY3};
    A_ARRAY = new double[] {A1, A2, A3};
    CCY_LIST = Arrays.asList(CCY_ARRAY);
    A_LIST = Arrays.asList(A1, A2, A3);
    CCY_A_MAP = new HashMap<Currency, Double>();
    CCY_A_MAP.put(CCY1, A1);
    CCY_A_MAP.put(CCY2, A2);
    CCY_A_MAP.put(CCY3, A3);
    CA_ARRAY = new CurrencyAmount[] {CA1, CA2, CA3};
    CA_LIST = Arrays.asList(CA_ARRAY);
    CA_SET = Sets.newHashSet(CA_ARRAY);
    MULTIPLE = MultipleCurrencyAmount.of(CA_ARRAY);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrency() {
    new MultipleCurrencyAmount(null, A1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrencyArray() {
    new MultipleCurrencyAmount(null, A_ARRAY);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInCurrencyArray() {
    new MultipleCurrencyAmount(new Currency[] {CCY1, null, CCY2}, A_ARRAY);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAmountArray() {
    new MultipleCurrencyAmount(CCY_ARRAY, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLengthAmountArray() {
    new MultipleCurrencyAmount(CCY_ARRAY, new double[] {A1, A2});
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrencyList() {
    new MultipleCurrencyAmount(null, A_LIST);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInCurrencyList() {
    new MultipleCurrencyAmount(Arrays.asList(CCY1, null, CCY2), A_LIST);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAmountList() {
    new MultipleCurrencyAmount(CCY_LIST, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInAmountList() {
    new MultipleCurrencyAmount(CCY_LIST, Arrays.asList(null, A2, A3));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLengthAmountList() {
    new MultipleCurrencyAmount(CCY_LIST, Arrays.asList(A1, A2));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullMap() {
    new MultipleCurrencyAmount((Map<Currency, Double>) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrencyInMap() {
    final Map<Currency, Double> map = new HashMap<Currency, Double>();
    map.put(CCY1, A1);
    map.put(null, A2);
    map.put(CCY3, A3);
    new MultipleCurrencyAmount(map);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAmountInMap() {
    final Map<Currency, Double> map = new HashMap<Currency, Double>();
    map.put(CCY1, A1);
    map.put(CCY2, null);
    map.put(CCY3, A3);
    new MultipleCurrencyAmount(map);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrencyAmountArray() {
    new MultipleCurrencyAmount((CurrencyAmount[]) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInCurrencyAmountArray() {
    new MultipleCurrencyAmount(new CurrencyAmount[] {null, CA2, CA3});
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrencyAmountList() {
    new MultipleCurrencyAmount((List<CurrencyAmount>) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInCurrencyAmountList() {
    new MultipleCurrencyAmount(Arrays.asList(null, CA2, CA3));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrencyAmountSet() {
    new MultipleCurrencyAmount((Set<CurrencyAmount>) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInCurrencyAmountSet() {
    new MultipleCurrencyAmount(Sets.newHashSet(null, CA2, CA3));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetAmountNullCurrency() {
    MULTIPLE.getAmountFor(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetAmountWrongCurrency() {
    MULTIPLE.getAmountFor(Currency.DEM);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetCurrencyAmountNullCurrency() {
    MULTIPLE.getCurrencyAmountFor(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetCurrencyAmountWrongCurrency() {
    MULTIPLE.getCurrencyAmountFor(Currency.DEM);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAddNullCurrencyAmount() {
    MULTIPLE.add((CurrencyAmount) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAddNullCurrency() {
    MULTIPLE.add(null, A1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAddNullMultipleCurrencyAmount() {
    MULTIPLE.add((MultipleCurrencyAmount) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testRemoveKeyNotPresent() {
    MULTIPLE.remove(Currency.DEM);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testReplaceKeyNotPresent() {
    MULTIPLE.replace(Currency.DEM, A1);
  }

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
        CurrencyAmount.of(CCY1, A3)};
    other = MultipleCurrencyAmount.of(array);
    assertFalse(MULTIPLE.equals(other));
    array = new CurrencyAmount[] {CurrencyAmount.of(CCY1, A1), CurrencyAmount.of(CCY2, A1), CurrencyAmount.of(CCY3, A1)};
    other = MultipleCurrencyAmount.of(array);
    assertFalse(MULTIPLE.equals(other));
  }

  @Test
  public void testConstructors() {
    assertEquals(MULTIPLE, new MultipleCurrencyAmount(CCY_LIST, A_LIST));
    assertEquals(MULTIPLE, new MultipleCurrencyAmount(CCY_A_MAP));
    assertEquals(MULTIPLE, new MultipleCurrencyAmount(CA_ARRAY));
    assertEquals(MULTIPLE, new MultipleCurrencyAmount(CA_LIST));
    assertEquals(MULTIPLE, new MultipleCurrencyAmount(CA_SET));
  }

  @Test
  public void testStaticConstruction() {
    assertEquals(new MultipleCurrencyAmount(CCY1, A1), MultipleCurrencyAmount.of(CCY1, A1));
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
    final Currency[] ccyArray = new Currency[] {CCY1, CCY2, CCY3, ca4.getCurrency(), ca5.getCurrency()};
    final double[] aArray = new double[] {A1, A2, A3, ca4.getAmount(), ca5.getAmount()};
    final List<Currency> ccyList = Arrays.asList(ccyArray);
    final List<Double> aList = Arrays.asList(A1, A2, A3, A1 * 2, A2 * 3);
    final CurrencyAmount[] caArray = new CurrencyAmount[] {CA1, CA2, CA3, ca4, ca5};
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
      assertEquals(MULTIPLE.getAmountFor(CCY_ARRAY[i]), A_ARRAY[i]);
      assertEquals(MULTIPLE.getCurrencyAmountFor(CCY_ARRAY[i]), CA_ARRAY[i]);
    }
  }

  @Test
  public void testAdd() {
    Set<CurrencyAmount> expected = new HashSet<CurrencyAmount>(CA_SET);
    MultipleCurrencyAmount mca = MultipleCurrencyAmount.of(CA_ARRAY);
    final double a = 117;
    CurrencyAmount ca = CurrencyAmount.of(Currency.CZK, a);
    mca.add(ca);
    expected.add(ca);
    assertSameData(expected, mca);
    ca = CurrencyAmount.of(Currency.AUD, a);
    expected = Sets.newHashSet(CA1.plus(ca), CA2, CA3);
    mca = MultipleCurrencyAmount.of(CA_LIST);
    mca.add(ca);
    assertSameData(expected, mca);
  }

  @Test
  public void testRemove() {
    final Set<CurrencyAmount> expected = Sets.newHashSet(CA1, CA3);
    final MultipleCurrencyAmount mca = MultipleCurrencyAmount.of(CCY_LIST, A_LIST);
    mca.remove(CCY2);
    assertSameData(expected, mca);
  }

  @Test
  public void testReplace() {
    final double a = A2 * 10;
    final CurrencyAmount ca = CurrencyAmount.of(CCY2, a);
    final Set<CurrencyAmount> expected = Sets.newHashSet(CA1, ca, CA3);
    final MultipleCurrencyAmount mca = MultipleCurrencyAmount.of(CCY_LIST, A_LIST);
    mca.replace(CCY2, a);
    assertSameData(expected, mca);
  }

  private void assertSameData(final Set<CurrencyAmount> expected, final MultipleCurrencyAmount actual) {
    final CurrencyAmount[] amounts = actual.getCurrencyAmounts();
    assertEquals(amounts.length, expected.size());
    for (final CurrencyAmount amount : amounts) {
      assertTrue(expected.contains(amount));
    }
  }
}
