/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.analytics.financial.legalentity.LegalEntityShortName;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldPeriodicCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Pairs;

/**
 * Test {@link IssuerProviderIssuerAnnuallyCompoundeding}. 
 */
@Test(groups = TestGroup.UNIT)
public class IssuerProviderIssuerAnnuallyCompoundedingTest {

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

  /**
   * Without spread.
   */
  @Test
  public void withoutSpreadTest() {
    double tol = 1.0e-12;
    IssuerProviderIssuerAnnuallyCompoundeding wrapper = new IssuerProviderIssuerAnnuallyCompoundeding(ISSUER_PROVIDER);
    assertTrue(wrapper.getIssuerProvider().equals(ISSUER_PROVIDER));
    assertTrue(wrapper.getMulticurveProvider().equals(ISSUER_PROVIDER.getMulticurveProvider()));

    LegalEntity issuer1 = new LegalEntity(null, ISSUER_NAME_ANN, null, null, null);
    LegalEntity issuer2 = new LegalEntity(null, ISSUER_NAME_QUA, null, null, null);
    double[] keys = new double[] {-1.0, 0.0, 1.35, 12.5, 30.0 };
    int nKeys = keys.length;
    for (int i = 0; i < nKeys; ++i) {
      double disExpected1 = Math.pow((1.0 + YIELD_CURVE_ANN.getCurve().getYValue(keys[i])), -keys[i]);
      double disComputed1 = wrapper.getDiscountFactor(issuer1, keys[i]);
      assertEquals("withoutSpreadTest", disExpected1, disComputed1, tol);
      double disExpected2 = Math.pow((1.0 + 0.25 * YIELD_CURVE_QUA.getCurve().getYValue(keys[i])), -4.0 * keys[i]);
      double disComputed2 = wrapper.getDiscountFactor(issuer2, keys[i]);
      assertEquals("withoutSpreadTest", disExpected2, disComputed2, tol);
      double disExpectedDis = Math.pow((1.0 + 0.5 * YIELD_CURVE_DIS.getCurve().getYValue(keys[i])), -2.0 * keys[i]);
      double disComputedDis = wrapper.getMulticurveProvider().getDiscountFactor(USD, keys[i]);
      assertEquals("withoutSpreadTest", disExpectedDis, disComputedDis, tol);
    }

    assertEquals("withoutSpreadTest", CURVE_NAME_ANN, wrapper.getName(issuer1));
    assertEquals("withoutSpreadTest", CURVE_NAME_QUA, wrapper.getName(issuer2));
    LegalEntityFilter<LegalEntity> filter = new LegalEntityShortName();
    assertEquals("withoutSpreadTest", CURVE_NAME_ANN, wrapper.getName(Pairs.of((Object) ISSUER_NAME_ANN, filter)));
    assertEquals("withoutSpreadTest", CURVE_NAME_QUA, wrapper.getName(Pairs.of((Object) ISSUER_NAME_QUA, filter)));
    assertEquals("withoutSpreadTest", ISSUER_PROVIDER.getAllNames(), wrapper.getAllNames());

    assertEquals("withoutSpreadTest", TIME_ANN.length, wrapper.getNumberOfParameters(CURVE_NAME_ANN).intValue());
    assertEquals("withoutSpreadTest", TIME_QUA.length, wrapper.getNumberOfParameters(CURVE_NAME_QUA).intValue());
    assertEquals("withoutSpreadTest", TIME_DIS.length, wrapper.getNumberOfParameters(CURVE_NAME_DIS).intValue());
    //    List<String> list = ISSUER_PROVIDER.getUnderlyingCurvesNames(CURVE_NAME_DIS);
    //    assertEquals("withoutSpreadTest", ISSUER_PROVIDER.getUnderlyingCurvesNames(CURVE_NAME_DIS), wrapper.getAllNames());
  }

  /**
   * Test issuer provider with spread(s). 
   * Note that because the interpolation/extrapolation holds linearity, interpolation and shift commute.    
   */
  @Test
  public void spreadTest() {

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
}
