/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.analytics.financial.legalentity.LegalEntityShortName;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldPeriodicCurve;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.SimplyCompoundedForwardSensitivity;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Test {@link IssuerProviderIssuerAnnuallyCompoundeding}. 
 */
@Test(groups = TestGroup.UNIT)
public class IssuerProviderIssuerAnnuallyCompoundedingTest {

  /* Building curves */
  private static final CombinedInterpolatorExtrapolator INTERPOLATOR = new CombinedInterpolatorExtrapolator(
      Interpolator1DFactory.LINEAR_INSTANCE, Interpolator1DFactory.FLAT_EXTRAPOLATOR_INSTANCE,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR_INSTANCE);
  private static final YieldPeriodicCurve YIELD_CURVE_ANN;
  private static final YieldPeriodicCurve YIELD_CURVE_QUA;
  private static final YieldPeriodicCurve YIELD_CURVE_DIS;
  private static final IssuerProviderDiscount ISSUER_PROVIDER = new IssuerProviderDiscount();
  private static final String CURVE_NAME_ANN = "testIssuerCurve1";
  private static final Double[] TIME_ANN = new Double[] {0.1, 0.5, 1.0, 2.0, 5.0, 10.0 };
  private static final Double[] RATE_ANN = new Double[] {0.01, 0.01, 0.015, 0.02, 0.02, 0.025 };
  private static final String ISSUER_NAME_ANN = "testIssuer1";
  static {
    InterpolatedDoublesCurve interpolatedCurve = InterpolatedDoublesCurve.fromSorted(TIME_ANN, RATE_ANN,
        INTERPOLATOR, CURVE_NAME_ANN);
    YIELD_CURVE_ANN = YieldPeriodicCurve.from(1, interpolatedCurve);
    LegalEntityFilter<LegalEntity> filter = new LegalEntityShortName();
    ISSUER_PROVIDER.setCurve(Pairs.of((Object) ISSUER_NAME_ANN, filter), YIELD_CURVE_ANN);
  }
  private static final String CURVE_NAME_QUA = "testIssuerCurve2";
  private static final Double[] TIME_QUA = new Double[] {0.1, 0.5, 1.0, 2.0, 5.0, 7.0, 10.0, 15.0 };
  private static final Double[] RATE_QUA = new Double[] {0.01, 0.015, 0.015, 0.02, 0.02, 0.025, 0.0125, 0.03 };
  private static final String ISSUER_NAME_QUA = "testIssuer2";
  static {
    InterpolatedDoublesCurve interpolatedCurve = InterpolatedDoublesCurve.fromSorted(TIME_QUA, RATE_QUA,
        INTERPOLATOR, CURVE_NAME_QUA);
    YIELD_CURVE_QUA = YieldPeriodicCurve.from(4, interpolatedCurve);
    LegalEntityFilter<LegalEntity> filter = new LegalEntityShortName();
    ISSUER_PROVIDER.setCurve(Pairs.of((Object) ISSUER_NAME_QUA, filter), YIELD_CURVE_QUA);
  }
  private static final String CURVE_NAME_DIS = "testDiscountCurve";
  private static final Double[] TIME_DIS = new Double[] {0.25, 0.5, 1.0, 3.0, 5.0, 10.0 };
  private static final Double[] RATE_DIS = new Double[] {0.02, 0.02, 0.025, 0.03, 0.03, 0.035 };
  private static final Currency USD = Currency.USD;
  static {
    InterpolatedDoublesCurve interpolatedCurve = InterpolatedDoublesCurve.fromSorted(TIME_DIS, RATE_DIS,
        INTERPOLATOR, CURVE_NAME_DIS);
    YIELD_CURVE_DIS = YieldPeriodicCurve.from(2, interpolatedCurve);
    ISSUER_PROVIDER.setCurve(USD, YIELD_CURVE_DIS);
  }

  /* Variables used for testing */
  private static final List<ForwardSensitivity> SENSITIVITY_LIST = new ArrayList<>();
  static {
    ForwardSensitivity fwdSense = new SimplyCompoundedForwardSensitivity(0.75, 1.0, 0.25, 124.0);
    SENSITIVITY_LIST.add(fwdSense);
  }
  private static final LegalEntity ISSUER_ANN = new LegalEntity(null, ISSUER_NAME_ANN, null, null, null);
  private static final LegalEntity ISSUER_QUA = new LegalEntity(null, ISSUER_NAME_QUA, null, null, null);
  private static final double[] KEYS = new double[] {-1.0, 0.0, 1.35, 12.5, 30.0 };
  private static final double NUM_KEYS = KEYS.length;

  /**
   * Test overridden methods
   */
  @Test
  public void accesserTest() {
    double tol = 1.0e-12;
    IssuerProviderIssuerAnnuallyCompoundeding wrapper = new IssuerProviderIssuerAnnuallyCompoundeding(ISSUER_PROVIDER);
    assertTrue(wrapper.getIssuerProvider().equals(ISSUER_PROVIDER));
    assertTrue(wrapper.getMulticurveProvider().equals(ISSUER_PROVIDER.getMulticurveProvider()));

    for (int i = 0; i < NUM_KEYS; ++i) {
      double disExpected1 = Math.pow((1.0 + YIELD_CURVE_ANN.getCurve().getYValue(KEYS[i])), -KEYS[i]);
      double disComputed1 = wrapper.getDiscountFactor(ISSUER_ANN, KEYS[i]);
      assertEquals("accesserTest", disExpected1, disComputed1, tol);
      double disExpected2 = Math.pow((1.0 + 0.25 * YIELD_CURVE_QUA.getCurve().getYValue(KEYS[i])), -4.0 * KEYS[i]);
      double disComputed2 = wrapper.getDiscountFactor(ISSUER_QUA, KEYS[i]);
      assertEquals("accesserTest", disExpected2, disComputed2, tol);
      double disExpectedDis = Math.pow((1.0 + 0.5 * YIELD_CURVE_DIS.getCurve().getYValue(KEYS[i])), -2.0 * KEYS[i]);
      double disComputedDis = wrapper.getMulticurveProvider().getDiscountFactor(USD, KEYS[i]);
      assertEquals("accesserTest", disExpectedDis, disComputedDis, tol);
    }

    assertEquals("accesserTest", CURVE_NAME_ANN, wrapper.getName(ISSUER_ANN));
    assertEquals("accesserTest", CURVE_NAME_QUA, wrapper.getName(ISSUER_QUA));
    LegalEntityFilter<LegalEntity> filter = new LegalEntityShortName();
    assertEquals("accesserTest", CURVE_NAME_ANN, wrapper.getName(Pairs.of((Object) ISSUER_NAME_ANN, filter)));
    assertEquals("accesserTest", CURVE_NAME_QUA, wrapper.getName(Pairs.of((Object) ISSUER_NAME_QUA, filter)));
    assertEquals("accesserTest", ISSUER_PROVIDER.getAllNames(), wrapper.getAllNames());

    assertArrayRelative("accesserTest", ISSUER_PROVIDER.parameterForwardSensitivity(CURVE_NAME_ANN, SENSITIVITY_LIST),
        wrapper.parameterForwardSensitivity(CURVE_NAME_ANN, SENSITIVITY_LIST), tol);
    assertArrayRelative("accesserTest", ISSUER_PROVIDER.parameterForwardSensitivity(CURVE_NAME_QUA, SENSITIVITY_LIST),
        wrapper.parameterForwardSensitivity(CURVE_NAME_QUA, SENSITIVITY_LIST), tol);

    assertEquals("accesserTest", TIME_ANN.length, wrapper.getNumberOfParameters(CURVE_NAME_ANN).intValue());
    assertEquals("accesserTest", TIME_QUA.length, wrapper.getNumberOfParameters(CURVE_NAME_QUA).intValue());
    assertEquals("accesserTest", TIME_DIS.length, wrapper.getNumberOfParameters(CURVE_NAME_DIS).intValue());
    List<String> list = wrapper.getUnderlyingCurvesNames(CURVE_NAME_DIS);
    assertEquals("accesserTest", new ArrayList<>(), list); // YieldPeriodicCurve always returns empty list
    Set<Pair<Object, LegalEntityFilter<LegalEntity>>> issuers = wrapper.getIssuers();
    Iterator<Pair<Object, LegalEntityFilter<LegalEntity>>> it = issuers.iterator();
    Pair<Object, LegalEntityFilter<LegalEntity>> first = it.next();
    assertEquals("accesserTest", ISSUER_NAME_ANN, first.getKey());
    assertEquals(new LegalEntityShortName(), first.getValue());
    Pair<Object, LegalEntityFilter<LegalEntity>> second = it.next();
    assertEquals("accesserTest", ISSUER_NAME_QUA, second.getKey());
    assertEquals("accesserTest", new LegalEntityShortName(), second.getValue());
    assertFalse("accesserTest", it.hasNext());
    Set<String> set = wrapper.getAllCurveNames();
    assertTrue("accesserTest", set.contains(CURVE_NAME_ANN));
    assertTrue("accesserTest", set.contains(CURVE_NAME_QUA));
    assertTrue("accesserTest", set.contains(CURVE_NAME_DIS));
  }

  /**
   * Test issuer provider with spread(s). 
   * Note that because the interpolation/extrapolation holds linearity, interpolation and shift commute.    
   */
  @Test
  public void spreadTest() {
    double tol = 1.0e-12;
    double spread = 0.03;
    double zeroSpread = 0.0;

    IssuerProviderIssuerAnnuallyCompoundeding noSpreadProvider = new IssuerProviderIssuerAnnuallyCompoundeding(
        ISSUER_PROVIDER);
    IssuerProviderIssuerAnnuallyCompoundeding zeroSpreadProvider = new IssuerProviderIssuerAnnuallyCompoundeding(
        ISSUER_PROVIDER, ISSUER_ANN, zeroSpread);
    IssuerProviderIssuerAnnuallyCompoundeding withSpreadProvider = new IssuerProviderIssuerAnnuallyCompoundeding(
        ISSUER_PROVIDER, ISSUER_ANN, spread);
    IssuerProviderIssuerAnnuallyCompoundeding zeroSpreadProviderRe = new IssuerProviderIssuerAnnuallyCompoundeding(
        noSpreadProvider, ISSUER_ANN, zeroSpread);
    IssuerProviderIssuerAnnuallyCompoundeding withSpreadProviderRe = new IssuerProviderIssuerAnnuallyCompoundeding(
        noSpreadProvider, ISSUER_ANN, spread);

    assertTrue(noSpreadProvider.equals(zeroSpreadProvider));
    assertTrue(noSpreadProvider.equals(zeroSpreadProviderRe));
    assertArrayRelative("spreadTest", noSpreadProvider.parameterForwardSensitivity(CURVE_NAME_QUA, SENSITIVITY_LIST),
        withSpreadProvider.parameterForwardSensitivity(CURVE_NAME_QUA, SENSITIVITY_LIST), tol);
    assertArrayRelative("spreadTest", noSpreadProvider.parameterForwardSensitivity(CURVE_NAME_ANN, SENSITIVITY_LIST),
        withSpreadProvider.parameterForwardSensitivity(CURVE_NAME_ANN, SENSITIVITY_LIST), tol);
    assertArrayRelative("spreadTest", withSpreadProvider.parameterForwardSensitivity(CURVE_NAME_QUA, SENSITIVITY_LIST),
        withSpreadProviderRe.parameterForwardSensitivity(CURVE_NAME_QUA, SENSITIVITY_LIST), tol);
    assertArrayRelative("spreadTest", withSpreadProvider.parameterForwardSensitivity(CURVE_NAME_ANN, SENSITIVITY_LIST),
        withSpreadProviderRe.parameterForwardSensitivity(CURVE_NAME_ANN, SENSITIVITY_LIST), tol);
    for (int i = 0; i < NUM_KEYS; ++i) {
      double disExpected1 = Math.pow((1.0 + YIELD_CURVE_ANN.getCurve().getYValue(KEYS[i]) + spread), -KEYS[i]);
      double disComputed1 = withSpreadProvider.getDiscountFactor(ISSUER_ANN, KEYS[i]);
      double disComputed1Re = withSpreadProviderRe.getDiscountFactor(ISSUER_ANN, KEYS[i]);
      assertEquals("spreadTest", disExpected1, disComputed1, tol);
      assertEquals("spreadTest", disExpected1, disComputed1Re, tol);
      double disExpected2 = Math.pow((1.0 + 0.25 * YIELD_CURVE_QUA.getCurve().getYValue(KEYS[i])), -4.0 * KEYS[i]);
      double disComputed2 = withSpreadProvider.getDiscountFactor(ISSUER_QUA, KEYS[i]);
      double disComputed2Re = withSpreadProviderRe.getDiscountFactor(ISSUER_QUA, KEYS[i]);
      assertEquals("spreadTest", disExpected2, disComputed2, tol);
      assertEquals("spreadTest", disExpected2, disComputed2Re, tol);
      double disExpectedDis = Math.pow((1.0 + 0.5 * YIELD_CURVE_DIS.getCurve().getYValue(KEYS[i])), -2.0 * KEYS[i]);
      double disComputedDis = withSpreadProvider.getMulticurveProvider().getDiscountFactor(USD, KEYS[i]);
      double disComputedDisRe = withSpreadProviderRe.getMulticurveProvider().getDiscountFactor(USD, KEYS[i]);
      assertEquals("spreadTest", disExpectedDis, disComputedDis, tol);
      assertEquals("spreadTest", disExpectedDis, disComputedDisRe, tol);
    }

    /*
     * hashCode and equals
     */
    assertTrue("spreadTest", noSpreadProvider.hashCode() == zeroSpreadProvider.hashCode());
    assertFalse("spreadTest", withSpreadProvider.hashCode() == withSpreadProviderRe.hashCode());
    assertFalse(withSpreadProvider.equals(withSpreadProviderRe)); // underlying hashMap is not equal

    assertFalse("spreadTest", zeroSpreadProvider.hashCode() == withSpreadProvider.hashCode());
    assertFalse("spreadTest", zeroSpreadProvider.equals(withSpreadProvider));

    IssuerProviderIssuerAnnuallyCompoundeding withSpreadProviderOther = new IssuerProviderIssuerAnnuallyCompoundeding(
        ISSUER_PROVIDER, ISSUER_QUA, spread);
    assertFalse("spreadTest", withSpreadProviderOther.hashCode() == withSpreadProvider.hashCode());
    assertFalse("spreadTest", withSpreadProviderOther.equals(withSpreadProvider));
  }

  /**
   * parameterSensitivity is not supported. 
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void sensitivityFailTest() {
    IssuerProviderIssuerAnnuallyCompoundeding wrapper = new IssuerProviderIssuerAnnuallyCompoundeding(ISSUER_PROVIDER);
    List<DoublesPair> pointSensitivity = new ArrayList<>();
    pointSensitivity.add(DoublesPair.of(0.1, 0.1));
    wrapper.parameterSensitivity(CURVE_NAME_ANN, pointSensitivity);
  }

  /**
   * copy is not supported, while underlying IssuerProviderDiscount supports copy
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void copyFailTest() {
    IssuerProviderIssuerAnnuallyCompoundeding wrapper = new IssuerProviderIssuerAnnuallyCompoundeding(ISSUER_PROVIDER);
    wrapper.copy();
  }

  private static void assertArrayRelative(String message, double[] expected, double[] obtained, double relativeTol) {
    int nData = expected.length;
    assertEquals(message, nData, obtained.length);
    for (int i = 0; i < nData; ++i) {
      assertRelative(message, expected[i], obtained[i], relativeTol);
    }
  }

  private static void assertRelative(String message, double expected, double obtained, double relativeTol) {
    double ref = Math.max(Math.abs(expected), 1.0);
    assertEquals(message, expected, obtained, ref * relativeTol);
  }
}
