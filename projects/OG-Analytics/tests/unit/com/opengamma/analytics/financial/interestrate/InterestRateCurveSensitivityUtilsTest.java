/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import static org.testng.AssertJUnit.assertEquals;
import static com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivityUtils.addSensitivity;
import static com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivityUtils.clean;
import static com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivityUtils.multiplySensitivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class InterestRateCurveSensitivityUtilsTest {

  @Test
  public void testCleanList1() {
    final List<DoublesPair> old = new ArrayList<DoublesPair>();
    old.add(new DoublesPair(0.23, 12.3));

    List<DoublesPair> res = clean(old, 1e-9, 1e-12);
    assertEquals(old.size(), res.size());

    for (int i = 0; i < old.size(); i++) {
      assertEquals(old.get(i).first, res.get(i).first, 0.0);
      assertEquals(old.get(i).second, res.get(i).second, 1e-9);
    }
  }

  @Test
  public void testCleanList2() {
    final List<DoublesPair> old = new ArrayList<DoublesPair>();
    old.add(new DoublesPair(0.23, 12.3));
    old.add(new DoublesPair(0.231, -12.3));
    old.add(new DoublesPair(1.23, 1.0));
    old.add(new DoublesPair(0.23, -12.3));
    old.add(new DoublesPair(1.23, 3.24));
    old.add(new DoublesPair(1.78, -3.24));
    old.add(new DoublesPair(1.23, -1.0));
    old.add(new DoublesPair(1.23, 1.0));

    final List<DoublesPair> expected = new ArrayList<DoublesPair>();
    expected.add(new DoublesPair(0.231, -12.3));
    expected.add(new DoublesPair(1.23, 4.24));
    expected.add(new DoublesPair(1.78, -3.24));

    List<DoublesPair> res = clean(old, 1e-9, 1e-12);
    assertEquals(expected.size(), res.size());

    for (int i = 0; i < expected.size(); i++) {
      assertEquals(expected.get(i).first, res.get(i).first, 0.0);
      assertEquals(expected.get(i).second, res.get(i).second, 1e-9);
    }
  }

  @Test
  public void testCleanMap() {
    String c1 = "c1";
    String c2 = "c2";
    final List<DoublesPair> l1 = new ArrayList<DoublesPair>();
    l1.add(new DoublesPair(0.23, 12.3));
    l1.add(new DoublesPair(0.231, -12.3));
    l1.add(new DoublesPair(1.23, 1.0));
    final Map<String, List<DoublesPair>> m = new HashMap<String, List<DoublesPair>>();
    m.put(c1, l1);
    final List<DoublesPair> l2 = new ArrayList<DoublesPair>();
    l2.add(new DoublesPair(0.23, -12.3));
    l2.add(new DoublesPair(1.23, 3.24));
    l2.add(new DoublesPair(1.78, -3.24));
    l2.add(new DoublesPair(1.23, -1.0));
    l2.add(new DoublesPair(1.23, 1.0));
    m.put(c2, l2);

    final List<DoublesPair> e2 = new ArrayList<DoublesPair>();
    e2.add(new DoublesPair(0.23, -12.3));
    e2.add(new DoublesPair(1.23, 3.24));
    e2.add(new DoublesPair(1.78, -3.24));

    Map<String, List<DoublesPair>> res = clean(m, 1e-9, 1e-12);
    assertSensitivityEquals(l1, res.get(c1), 1e-9);
    assertSensitivityEquals(e2, res.get(c2), 1e-9);
  }

  @Test
  public void testAddSensitivityList() {
    final List<DoublesPair> l1 = new ArrayList<DoublesPair>();
    l1.add(new DoublesPair(0.23, 12.3));
    l1.add(new DoublesPair(0.23, -2.6));
    l1.add(new DoublesPair(0.35, 2.3));
    final List<DoublesPair> l2 = new ArrayList<DoublesPair>();
    l2.add(new DoublesPair(0.35, 12.3));
    l2.add(new DoublesPair(1.23, -2.6));
    List<DoublesPair> res = addSensitivity(l1, l2);
    ArrayList<DoublesPair> expected = new ArrayList<DoublesPair>(); //Note: no cleaning is done with AddSensitivity
    expected.add(new DoublesPair(0.23, 12.3));
    expected.add(new DoublesPair(0.23, -2.6));
    expected.add(new DoublesPair(0.35, 2.3));
    expected.add(new DoublesPair(0.35, 12.3));
    expected.add(new DoublesPair(1.23, -2.6));

    assertEquals(expected.size(), res.size());

    for (int i = 0; i < expected.size(); i++) {
      assertEquals(expected.get(i).first, res.get(i).first, 0.0);
      assertEquals(expected.get(i).second, res.get(i).second, 1e-9);
    }
  }

  @Test
  public void testAddSensitivityMap() {
    final List<DoublesPair> l1 = new ArrayList<DoublesPair>();
    l1.add(new DoublesPair(0.23, 12.3));
    l1.add(new DoublesPair(0.23, -2.6));
    l1.add(new DoublesPair(0.35, 2.3));
    final List<DoublesPair> l2 = new ArrayList<DoublesPair>();
    l2.add(new DoublesPair(0.35, 12.3));
    l2.add(new DoublesPair(1.23, -2.6));
    final List<DoublesPair> l3 = new ArrayList<DoublesPair>();
    l3.add(new DoublesPair(0.35, 12.3));
    l3.add(new DoublesPair(1.23, -2.6));
    String c1 = "C1";
    String c2 = "C2";
    Map<String, List<DoublesPair>> m1 = new HashMap<String, List<DoublesPair>>();
    m1.put(c1, l1);
    m1.put(c2, l2);
    Map<String, List<DoublesPair>> m2 = new HashMap<String, List<DoublesPair>>();
    m2.put(c2, l2);

    Map<String, List<DoublesPair>> res = addSensitivity(m1, m2);
    assertEquals(2, res.size());
    assertSensitivityEquals(l1, res.get(c1), 0.0);
    assertSensitivityEquals(addSensitivity(l2, l3), res.get(c2), 0.0); //add on list is already tested   
  }

  @Test
  public void testMultiplySensitivityList() {
    final List<DoublesPair> l1 = new ArrayList<DoublesPair>();
    l1.add(new DoublesPair(0.23, 2.3));
    l1.add(new DoublesPair(0.23, -2.6));
    l1.add(new DoublesPair(-0.35, 2.3));
    List<DoublesPair> res = multiplySensitivity(l1, 2.0);
    assertEquals(l1.size(), res.size());
    for (int i = 0; i < res.size(); i++) {
      assertEquals(l1.get(i).first, res.get(i).first, 0.0);
      assertEquals(2.0 * l1.get(i).second, res.get(i).second, 1e-9);
    }
  }

  public void testMultiplySensitivityMap() {
    String c1 = "c1";
    String c2 = "c2";
    final List<DoublesPair> l1 = new ArrayList<DoublesPair>();
    l1.add(new DoublesPair(0.23, 2.3));
    final Map<String, List<DoublesPair>> m = new HashMap<String, List<DoublesPair>>();
    m.put(c1, l1);
    final List<DoublesPair> l2 = new ArrayList<DoublesPair>();
    l2.add(new DoublesPair(0.23, -2.6));
    l2.add(new DoublesPair(-0.35, 2.3));
    m.put(c2, l2);
    Map<String, List<DoublesPair>> res = multiplySensitivity(m, 2.0);
    assertSensitivityEquals(multiplySensitivity(l1, 2.0), res.get(c1), 1e-9); // multiplySensitivity on list tested
    assertSensitivityEquals(multiplySensitivity(l2, 2.0), res.get(c2), 1e-9);
  }

  private static void assertSensitivityEquals(List<DoublesPair> expected, List<DoublesPair> accual, double tol) {
    assertEquals(expected.size(), accual.size(), 0);
    for (int i = 0; i < expected.size(); i++) {
      assertEquals(expected.get(i).first, accual.get(i).first, 0.0);
      assertEquals(expected.get(i).second, accual.get(i).second, tol);
    }
  }

}
