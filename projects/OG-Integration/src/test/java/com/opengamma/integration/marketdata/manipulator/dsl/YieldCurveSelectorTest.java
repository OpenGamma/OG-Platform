/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import static org.mockito.Mockito.mock;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

import org.testng.annotations.Test;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.marketdata.manipulator.MarketDataSelector;
import com.opengamma.engine.marketdata.manipulator.SelectorResolver;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class YieldCurveSelectorTest {

  private final SelectorResolver _resolver = mock(SelectorResolver.class);

  private static final Scenario SCENARIO = new Scenario("scenario");
  private static final String CALC_CONFIG_NAME = "calcConfigName";

  private static ValueSpecification valueSpec(String curveName) {
    ValueProperties properties =
        ValueProperties
            .with(ValuePropertyNames.CURVE, curveName)
            .with(ValuePropertyNames.FUNCTION, "foo")
            .get();
    ComputationTargetSpecification targetSpec =
        new ComputationTargetSpecification(CurrencyPair.TYPE, UniqueId.of(Currency.OBJECT_SCHEME, "GBP"));
    return new ValueSpecification(ValueRequirementNames.YIELD_CURVE, targetSpec, properties);
  }

  private static ValueSpecification valueSpec(Currency currency, String curveName) {
    ValueProperties properties =
        ValueProperties
            .with(ValuePropertyNames.CURVE, curveName)
            .with(ValuePropertyNames.FUNCTION, "foo")
            .get();
    ComputationTargetSpecification targetSpec =
        new ComputationTargetSpecification(CurrencyPair.TYPE, currency.getUniqueId());
    return new ValueSpecification(ValueRequirementNames.YIELD_CURVE, targetSpec, properties);
  }

  /** if no criteria are specified the selector should match any curve */
  @Test
  public void noCriteria() {
    YieldCurveSelector.Builder curve = new YieldCurveSelector.Builder(SCENARIO);
    MarketDataSelector selector = curve.getSelector();
    assertEquals(selector, selector.findMatchingSelector(valueSpec("curveName1"), CALC_CONFIG_NAME, _resolver));
    assertEquals(selector, selector.findMatchingSelector(valueSpec("curveName2"), CALC_CONFIG_NAME, _resolver));
  }

  /** match a single name and fail any other names */
  @Test
  public void singleName() {
    String curveName = "curveName";
    String calcConfigName = "calcConfigName";
    YieldCurveSelector.Builder curve = new YieldCurveSelector.Builder(SCENARIO);
    curve.named(curveName);
    MarketDataSelector selector = curve.getSelector();
    assertEquals(selector, selector.findMatchingSelector(valueSpec(curveName), calcConfigName, _resolver));
    assertNull(selector.findMatchingSelector(valueSpec("otherName"), calcConfigName, _resolver));
  }

  /** match any one of multiple curve names, fail other names */
  @Test
  public void multipleNames() {
    String curveName1 = "curveName1";
    String curveName2 = "curveName2";
    YieldCurveSelector.Builder curve = new YieldCurveSelector.Builder(SCENARIO);
    curve.named(curveName1, curveName2);
    MarketDataSelector selector = curve.getSelector();
    assertEquals(selector, selector.findMatchingSelector(valueSpec(curveName1), CALC_CONFIG_NAME, _resolver));
    assertEquals(selector, selector.findMatchingSelector(valueSpec(curveName2), CALC_CONFIG_NAME, _resolver));
    assertNull(selector.findMatchingSelector(valueSpec("otherName"), CALC_CONFIG_NAME, _resolver));
  }

  /** don't match if the calc config name doesn't match */
  @Test
  public void calcConfigName() {
    YieldCurveSelector.Builder curve = new YieldCurveSelector.Builder(new Scenario("foo").calculationConfigurations(CALC_CONFIG_NAME));
    MarketDataSelector selector = curve.getSelector();
    assertNull(selector.findMatchingSelector(valueSpec("curveName"), "otherCalcConfigName", _resolver));
  }

  /** match if the curve name matches a regular expression */
  @Test
  public void nameMatches() {
    String curve3M = "curve3M";
    String curve6M = "curve6M";
    YieldCurveSelector.Builder curve = new YieldCurveSelector.Builder(SCENARIO);
    curve.nameMatches(".*3M");
    MarketDataSelector selector = curve.getSelector();
    assertEquals(selector, selector.findMatchingSelector(valueSpec(curve3M), CALC_CONFIG_NAME, _resolver));
    assertNull(selector.findMatchingSelector(valueSpec(curve6M), CALC_CONFIG_NAME, _resolver));
  }

  /** match if the curve name matches a glob */
  @Test
  public void nameLike() {
    String curve3M = "curve3M";
    String curve6M = "curve6M";

    YieldCurveSelector.Builder curve1 = new YieldCurveSelector.Builder(SCENARIO);
    curve1.nameLike("*3M");
    MarketDataSelector selector1 = curve1.getSelector();
    assertEquals(selector1, selector1.findMatchingSelector(valueSpec(curve3M), CALC_CONFIG_NAME, _resolver));
    assertNull(selector1.findMatchingSelector(valueSpec(curve6M), CALC_CONFIG_NAME, _resolver));

    YieldCurveSelector.Builder curve2 = new YieldCurveSelector.Builder(SCENARIO);
    curve2.nameLike("curve?M");
    MarketDataSelector selector2 = curve2.getSelector();
    assertEquals(selector2, selector2.findMatchingSelector(valueSpec(curve3M), CALC_CONFIG_NAME, _resolver));
    assertEquals(selector2, selector2.findMatchingSelector(valueSpec(curve6M), CALC_CONFIG_NAME, _resolver));
  }

  /** match if the curve currency is specified */
  @Test
  public void singleCurrency() {
    YieldCurveSelector.Builder curve = new YieldCurveSelector.Builder(SCENARIO);
    curve.currencies("GBP");
    MarketDataSelector selector = curve.getSelector();
    String curveName = "curveName";
    assertEquals(selector, selector.findMatchingSelector(valueSpec(Currency.GBP, curveName), CALC_CONFIG_NAME, _resolver));
    assertNull(selector.findMatchingSelector(valueSpec(Currency.USD, curveName), CALC_CONFIG_NAME, _resolver));
  }

  /** match if the curve currency matches any of the specified currency codes */
  @Test
  public void multipleCurrencies() {
    YieldCurveSelector.Builder curve = new YieldCurveSelector.Builder(SCENARIO);
    curve.currencies("GBP", "USD");
    MarketDataSelector selector = curve.getSelector();
    String curveName = "curveName";
    assertEquals(selector, selector.findMatchingSelector(valueSpec(Currency.GBP, curveName), CALC_CONFIG_NAME, _resolver));
    assertEquals(selector, selector.findMatchingSelector(valueSpec(Currency.USD, curveName), CALC_CONFIG_NAME, _resolver));
    assertNull(selector.findMatchingSelector(valueSpec(Currency.AUD, curveName), CALC_CONFIG_NAME, _resolver));
  }

  /** match if the curve satisfies all criteria, fail if it fails any of them */
  @Test
  public void multipleCriteria() {
    YieldCurveSelector.Builder curve = new YieldCurveSelector.Builder(SCENARIO);
    String curveName1 = "curveName1";
    String curveName2 = "curveName2";
    String curveName3 = "curveName3";
    curve.named(curveName1, curveName2).currencies("USD", "GBP");
    MarketDataSelector selector = curve.getSelector();
    assertEquals(selector, selector.findMatchingSelector(valueSpec(Currency.GBP, curveName1), CALC_CONFIG_NAME, _resolver));
    assertEquals(selector, selector.findMatchingSelector(valueSpec(Currency.USD, curveName2), CALC_CONFIG_NAME, _resolver));
    assertNull(selector.findMatchingSelector(valueSpec(Currency.AUD, curveName1), CALC_CONFIG_NAME, _resolver));
    assertNull(selector.findMatchingSelector(valueSpec(Currency.USD, curveName3), CALC_CONFIG_NAME, _resolver));
  }
}
