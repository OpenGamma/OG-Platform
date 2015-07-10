/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.regression;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDateTime;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.opengamma.analytics.financial.interestrate.NelsonSiegelBondCurveModel;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.NodalDoublesCurve;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceData;
import com.opengamma.financial.analytics.DoubleLabelledMatrix1D;
import com.opengamma.financial.analytics.StringLabelledMatrix1D;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.Pairs;

@Test(groups = TestGroup.UNIT)
public class EqualityCheckerTest {

  private static final double DELTA = 0.0001;

  private static void checkEqual(Object value1, Object value2) {
    assertTrue(EqualityChecker.equals(value1, value2, DELTA));
  }

  private static void checkNotEqual(Object value1, Object value2) {
    assertFalse(EqualityChecker.equals(value1, value2, DELTA));
  }

  @Test
  public void doubleValue() {
    checkEqual(2d, 2d);
    checkEqual(2d, 2.000001);
    checkNotEqual(2d, 2.1);
  }

  @Test
  public void bean() {
    String name = "name";
    // same data
    NodalDoublesCurve curve1 = new NodalDoublesCurve(new double[]{1, 2, 3}, new double[]{2, 3, 4}, true, name);
    checkEqual(curve1, curve1);

    // same data, different name, name should be ignored
    NodalDoublesCurve curve2 = new NodalDoublesCurve(new double[]{1, 2, 3}, new double[]{2, 3, 4}, true, "name2");
    checkEqual(curve1, curve2);

    // different curve data
    NodalDoublesCurve curve3 = new NodalDoublesCurve(new double[]{1.1, 2, 3}, new double[]{2, 3, 4}, true, name);
    checkNotEqual(curve1, curve3);

    // different curve data but within delta, should be equal
    NodalDoublesCurve curve4 = new NodalDoublesCurve(new double[]{1.000001, 2, 3}, new double[]{2, 3, 4}, true, name);
    checkEqual(curve1, curve4);
  }

  @Test
  public void yieldCurve() {
    NodalDoublesCurve nodalDoublesCurve1 = new NodalDoublesCurve(new double[]{1, 2, 3}, new double[]{2, 3, 4}, true);
    checkEqual(new YieldCurve("name", nodalDoublesCurve1), new YieldCurve("name", nodalDoublesCurve1));
    // name is ignored because it's auto-generated and differs between runs
    checkEqual(new YieldCurve("name1", nodalDoublesCurve1), new YieldCurve("name2", nodalDoublesCurve1));
    NodalDoublesCurve nodalDoublesCurve2 = new NodalDoublesCurve(new double[]{1.1, 2, 3}, new double[]{2, 3, 4}, true);
    checkNotEqual(new YieldCurve("name", nodalDoublesCurve1), new YieldCurve("name", nodalDoublesCurve2));
  }

  @Test
  public void objectArray() {
    checkEqual(new Object[]{1, 1.1, "foo"}, new Object[]{1, 1.1, "foo"});
    checkEqual(new Object[]{1, 1.1, "foo"}, new Object[]{1, 1.100001, "foo"});
    checkNotEqual(new Object[]{1, 1.1, "foo"}, new Object[]{1, 1.1, "bar"});
    checkEqual(new Number[]{1.1d, 1.2d}, new Number[]{1.1d, 1.2d});
    checkEqual(new Number[]{1.1d, 1.20000001d}, new Number[]{1.1d, 1.2d});
    checkNotEqual(new Number[]{1.1d, 1.2d}, new Number[]{1.1d, 1.3d});
  }

  @Test
  public void doubleArray() {
    checkEqual(new Double[]{1d, 2d, 3d}, new Double[]{1d, 2d, 3d});
    checkEqual(new Double[]{1d, 2d, 3d}, new Double[]{1.000001, 2.000001, 3.000001});
    checkNotEqual(new Double[]{1d, 2d, 3d}, new Double[]{1d, 2d, 4d});
    checkNotEqual(new Double[]{1d, 2d, 3d}, new Double[]{1d, 2d});
  }

  @Test
  public void primitiveDoubleArray() {
    checkEqual(new double[]{1, 2, 3}, new double[]{1, 2, 3});
    checkEqual(new double[]{1, 2, 3}, new double[]{1.000001, 2.000001, 3.000001});
    checkNotEqual(new double[]{1, 2, 3}, new double[]{1, 2, 4});
    checkNotEqual(new double[]{1, 2, 3}, new double[]{1, 2});
  }

  @Test
  public void invokedSerializedForm() {
    checkEqual(new NelsonSiegelBondCurveModel().getParameterizedFunction(),
               new NelsonSiegelBondCurveModel().getParameterizedFunction());
    checkEqual(new ForwardCurve(2, 3).getForwardCurve(), new ForwardCurve(2, 3).getForwardCurve());
    checkEqual(new ForwardCurve(2, 3).getForwardCurve(), new ForwardCurve(2.000001, 3.000001).getForwardCurve());
    checkNotEqual(new ForwardCurve(2, 3).getForwardCurve(), new ForwardCurve(2.1, 3.1).getForwardCurve());
  }

  @Test
  public void map() {
    checkEqual(ImmutableMap.of("a", 1d, "b", 2d), ImmutableMap.of("a", 1d, "b", 2d));
    checkEqual(ImmutableMap.of("a", 1d, "b", 2d), ImmutableMap.of("a", 1.000001, "b", 2d));
    checkNotEqual(ImmutableMap.of("a", 1d, "b", 2d), ImmutableMap.of("a", 1.1, "b", 2d));
    checkNotEqual(ImmutableMap.of("a", 1d, "b", 2d), ImmutableMap.of("a", 1d));
  }

  @Test
  public void multipleCurrencyAmount() {
    checkEqual(MultipleCurrencyAmount.of(new Currency[]{Currency.AUD, Currency.USD}, new double[]{5, 10}),
               MultipleCurrencyAmount.of(new Currency[]{Currency.AUD, Currency.USD}, new double[]{5, 10}));
    checkEqual(MultipleCurrencyAmount.of(new Currency[]{Currency.AUD, Currency.USD}, new double[]{5, 10}),
               MultipleCurrencyAmount.of(new Currency[]{Currency.AUD, Currency.USD}, new double[]{5.00001, 10}));
    checkEqual(MultipleCurrencyAmount.of(new Currency[]{Currency.AUD, Currency.USD}, new double[]{5, 10}),
               MultipleCurrencyAmount.of(new Currency[]{Currency.USD, Currency.AUD}, new double[]{10, 5}));
    checkNotEqual(MultipleCurrencyAmount.of(new Currency[]{Currency.AUD, Currency.USD}, new double[]{5, 10}),
                  MultipleCurrencyAmount.of(new Currency[]{Currency.AUD, Currency.USD}, new double[]{5.1, 10}));
    checkNotEqual(MultipleCurrencyAmount.of(new Currency[]{Currency.AUD, Currency.USD}, new double[]{5, 10}),
                  MultipleCurrencyAmount.of(Currency.AUD, 5));

  }

  @Test
  public void list() {
    checkEqual(Lists.newArrayList(1d, 2d, 3d), Lists.newArrayList(1d, 2d, 3d));
    checkEqual(Lists.newArrayList(1d, 2d, 3d), Lists.newArrayList(1.000001, 2d, 3d));
    checkNotEqual(Lists.newArrayList(1d, 2d, 3d), Lists.newArrayList(1.1, 2d, 3d));
    checkNotEqual(Lists.newArrayList(1d, 2d, 3d), Lists.newArrayList(1d, 2d));
    checkNotEqual(Lists.newArrayList(1), Lists.newArrayList("1"));
  }

  @Test
  public void differentTypes() {
    checkNotEqual("foo", 1d);
  }

  @Test
  public void nulls() {
    checkEqual(null, null);
    checkNotEqual("foo", null);
    checkNotEqual(null, "foo");
  }

  @Test
  public void arbitraryTypes() {
    checkEqual("foo", "foo");
    checkNotEqual("foo", "bar");
    checkEqual(1, 1);
    checkNotEqual(2, 1);
    checkEqual(LocalDateTime.of(2011, 3, 8, 2, 18), LocalDateTime.of(2011, 3, 8, 2, 18));
    checkNotEqual(LocalDateTime.of(2011, 3, 8, 2, 18), LocalDateTime.of(2011, 3, 8, 2, 19));
    checkEqual(Sets.newHashSet(1, 2, 3), Sets.newHashSet(3, 2, 1));
    checkNotEqual(Sets.newHashSet(1, 2, 3), Sets.newHashSet(3, 2));
  }

  @Test
  public void labelledMatrix1D() {
    checkEqual(new DoubleLabelledMatrix1D(new Double[]{1d, 2d, 3d},
                                          new Object[]{"1", "2", "3"},
                                          "labels",
                                          new double[]{10, 20, 30},
                                          "values"),
               new DoubleLabelledMatrix1D(new Double[]{1d, 2d, 3d},
                                          new Object[]{"1", "2", "3"},
                                          "labels",
                                          new double[]{10, 20, 30},
                                          "values"));
    checkEqual(new DoubleLabelledMatrix1D(new Double[]{1.00001d, 2d, 3d},
                                          new Object[]{"1", "2", "3"},
                                          "labels",
                                          new double[]{10.00001, 20, 30},
                                          "values"),
               new DoubleLabelledMatrix1D(new Double[]{1d, 2d, 3d},
                                          new Object[]{"1", "2", "3"},
                                          "labels",
                                          new double[]{10, 20, 30},
                                          "values"));
    checkNotEqual(new DoubleLabelledMatrix1D(new Double[]{1.1d, 2d, 3d},
                                             new Object[]{"1", "2", "3"},
                                             "labels",
                                             new double[]{10.1, 20, 30},
                                             "values"),
                  new DoubleLabelledMatrix1D(new Double[]{1d, 2d, 3d},
                                             new Object[]{"1", "2", "3"},
                                             "labels",
                                             new double[]{10, 20, 30},
                                             "values"));
    checkEqual(new StringLabelledMatrix1D(new String[]{"1", "2", "3"},
                                          new Object[]{"a", "b", "c"},
                                          "labels",
                                          new double[]{10, 20, 30},
                                          "values"),
               new StringLabelledMatrix1D(new String[]{"1", "2", "3"},
                                          new Object[]{"a", "b", "c"},
                                          "labels",
                                          new double[]{10, 20, 30},
                                          "values"));
    checkNotEqual(new StringLabelledMatrix1D(new String[]{"1", "2", "3"},
                                             new Object[]{"a", "b", "c"},
                                             "labels",
                                             new double[]{10, 20, 30},
                                             "values"),
                  new StringLabelledMatrix1D(new String[]{"1", "2", "3"},
                                             new Object[]{"a", "b", "c"},
                                             "foo",
                                             new double[]{10, 20, 30},
                                             "values"));
  }

  @Test
  public void volatilitySurfaceData() {
    VolatilitySurfaceData<String, String> surface1 =
        new VolatilitySurfaceData<>("defName",
                                    "specName",
                                    UniqueId.of("tst", "123"),
                                    new String[]{"1", "2"},
                                    "xLabel",
                                    new String[]{"a", "b"},
                                    "yLabel",
                                    ImmutableMap.of(Pairs.of("1", "a"), 1d,
                                                    Pairs.of("1", "b"), 2d,
                                                    Pairs.of("2", "a"), 3d,
                                                    Pairs.of("2", "b"), 4d));
    VolatilitySurfaceData<String, String> surface2 =
        new VolatilitySurfaceData<>("defName",
                                    "specName",
                                    UniqueId.of("tst", "123"),
                                    new String[]{"1", "2"},
                                    "xLabel",
                                    new String[]{"a", "b"},
                                    "yLabel",
                                    ImmutableMap.of(Pairs.of("1", "a"), 1d,
                                                    Pairs.of("1", "b"), 2d,
                                                    Pairs.of("2", "a"), 3d,
                                                    Pairs.of("2", "b"), 4d));
    checkEqual(surface1, surface2);

    VolatilitySurfaceData<String, String> surface3 =
        new VolatilitySurfaceData<>("defName",
                                    "specName",
                                    UniqueId.of("tst", "123"),
                                    new String[]{"1", "2"},
                                    "xLabel",
                                    new String[]{"a", "b"},
                                    "yLabel",
                                    ImmutableMap.of(Pairs.of("1", "a"), 1.00001,
                                                    Pairs.of("1", "b"), 2.00001,
                                                    Pairs.of("2", "a"), 3.00001,
                                                    Pairs.of("2", "b"), 4.00001));
    checkEqual(surface1, surface3);

    VolatilitySurfaceData<String, String> surface4 =
        new VolatilitySurfaceData<>("defName",
                                    "specName",
                                    UniqueId.of("tst", "123"),
                                    new String[]{"1", "2"},
                                    "xLabel",
                                    new String[]{"a", "b"},
                                    "yLabel",
                                    ImmutableMap.of(Pairs.of("1", "a"), 1.1,
                                                    Pairs.of("1", "b"), 2.1,
                                                    Pairs.of("2", "a"), 3.1,
                                                    Pairs.of("2", "b"), 4.1));
    checkNotEqual(surface1, surface4);

    VolatilitySurfaceData<String, String> surface5 =
        new VolatilitySurfaceData<>("defName",
                                    "specName",
                                    UniqueId.of("tst", "123"),
                                    new String[]{"1", "2"},
                                    "X",
                                    new String[]{"a", "b"},
                                    "Y",
                                    ImmutableMap.of(Pairs.of("1", "a"), 1d,
                                                    Pairs.of("1", "b"), 2d,
                                                    Pairs.of("2", "a"), 3d,
                                                    Pairs.of("2", "b"), 4d));
    checkNotEqual(surface1, surface5);
  }
}
