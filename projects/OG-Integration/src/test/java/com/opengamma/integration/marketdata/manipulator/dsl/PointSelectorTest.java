/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.marketdata.manipulator.SelectorResolver;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.target.resolver.PrimitiveResolver;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.option.AmericanExerciseType;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Expiry;

@Test(groups = TestGroup.UNIT)
public class PointSelectorTest {

  private final SelectorResolver _noOpResolver = mock(SelectorResolver.class);
  private final ValueSpecification _valueSpec = valueSpec("scheme", "id");

  private PointSelector.Builder builder() {
    return new PointSelector.Builder(new Scenario("scenarioName"));
  }

  private ValueSpecification valueSpec(String scheme, String value) {
    return valueSpec(ExternalId.of(scheme, value));
  }

  private ValueSpecification valueSpec(ExternalId id) {
    ValueProperties properties =
        ValueProperties
            .with("Id", id.toString())
            .with(ValuePropertyNames.FUNCTION, "foo")
            .get();
    return new ValueSpecification(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetSpecification.NULL, properties);
  }

  @Test
  public void valueName() {
    PointSelector selector = builder().getSelector(); // will match any ExternalId
    assertNotNull(selector.findMatchingSelector(_valueSpec, "default", _noOpResolver));
    ValueSpecification valueSpec = new ValueSpecification(ValueRequirementNames.YIELD_CURVE,
        ComputationTargetSpecification.NULL,
        ValueProperties.with(ValuePropertyNames.FUNCTION, "foo").get());
    assertNull(selector.findMatchingSelector(valueSpec, "default", _noOpResolver));
  }

  @Test
  public void ids() {
    PointSelector selector = builder().ids("scheme~value1", "scheme~value2").getSelector();
    assertNotNull(selector.findMatchingSelector(valueSpec("scheme", "value1"), "calcConfig", _noOpResolver));
    assertNotNull(selector.findMatchingSelector(valueSpec("scheme", "value2"), "calcConfig", _noOpResolver));
    assertNull(selector.findMatchingSelector(valueSpec("scheme", "value3"), "calcConfig", _noOpResolver));
  }

  @Test
  public void idMatchesOnExternalIdBundle() {
    UniqueId uid = PrimitiveResolver.resolveExternalId(ExternalIdBundle.of(ExternalId.of("scheme-A", "value-1"), ExternalId.of("scheme-B", "value-2")));

    ValueSpecification valueSpec = new ValueSpecification(MarketDataRequirementNames.MARKET_VALUE,
        new ComputationTargetSpecification(ComputationTargetType.ANYTHING, uid), ValueProperties.with(ValuePropertyNames.FUNCTION, "foo").get());
    assertNotNull(builder().idMatches("scheme-A", "value-\\d").getSelector().findMatchingSelector(valueSpec,
        "default",
        _noOpResolver));
    assertNotNull(builder().idMatches("scheme-B", "value-\\d").getSelector().findMatchingSelector(valueSpec,
        "default",
        _noOpResolver));
    assertNotNull(builder().idMatches("scheme-A", "[a-z\\-]+\\d").getSelector().findMatchingSelector(valueSpec,
                                                                                                  "default",
                                                                                                  _noOpResolver));
    assertNotNull(builder().idMatches("scheme-B", "[a-z\\-]+\\d").getSelector().findMatchingSelector(valueSpec,
                                                                                                  "default",
                                                                                                  _noOpResolver));
    assertNull(builder().idMatches("Scheme-A", "value-\\d").getSelector().findMatchingSelector(valueSpec,
        "default",
        _noOpResolver));

    assertNull(builder().idMatches("scheme-C", "value-\\d").getSelector().findMatchingSelector(valueSpec,
                                                                                               "default",
                                                                                               _noOpResolver));

    assertNull(builder().idMatches("scheme-A", "nn-\\d").getSelector().findMatchingSelector(valueSpec,
                                                                                               "default",
                                                                                               _noOpResolver));
  }

  @Test
  public void idMatchesExactlyOnExternalIdBundle() {
    UniqueId uid = PrimitiveResolver.resolveExternalId(ExternalIdBundle.of(ExternalId.of("scheme-A", "value-1"), ExternalId.of("scheme-B", "value-2")));

    ValueSpecification valueSpec = new ValueSpecification(MarketDataRequirementNames.MARKET_VALUE,
                                                          new ComputationTargetSpecification(ComputationTargetType.ANYTHING, uid), ValueProperties.with(ValuePropertyNames.FUNCTION, "foo").get());
    assertNotNull(builder().id("scheme-A", "value-1").getSelector().findMatchingSelector(valueSpec,
                                                                                                  "default",
                                                                                                  _noOpResolver));
    assertNotNull(builder().id("scheme-B", "value-2").getSelector().findMatchingSelector(valueSpec,
                                                                                                  "default",
                                                                                                  _noOpResolver));
    assertNull(builder().id("Scheme-A", "value-1").getSelector().findMatchingSelector(valueSpec,
                                                                                               "default",
                                                                                               _noOpResolver));

    assertNull(builder().id("scheme-C", "value-0").getSelector().findMatchingSelector(valueSpec,
                                                                                               "default",
                                                                                               _noOpResolver));

    assertNull(builder().id("scheme-A", "nn-2").getSelector().findMatchingSelector(valueSpec,
                                                                                            "default",
                                                                                            _noOpResolver));
  }

  @Test
  public void calcConfigNames() {
    PointSelector selector = new PointSelector(Sets.newHashSet("default", "cc1"), null, null, null, null, null, null);
    assertNotNull(selector.findMatchingSelector(_valueSpec, "default", _noOpResolver));
    assertNotNull(selector.findMatchingSelector(_valueSpec, "cc1", _noOpResolver));
    assertNull(selector.findMatchingSelector(_valueSpec, "cc2", _noOpResolver));
  }

  @Test
  public void idMatches() {
    PointSelector selector = builder().idMatches("scheme", "value\\d").getSelector();
    assertNotNull(selector.findMatchingSelector(valueSpec("scheme", "value1"), "default", _noOpResolver));
    assertNotNull(selector.findMatchingSelector(valueSpec("scheme", "value2"), "default", _noOpResolver));
    assertNull(selector.findMatchingSelector(valueSpec("scheme", "value"), "default", _noOpResolver));
  }

  @Test
  public void idLike() {
    PointSelector selector1 = builder().idLike("scheme", "value?").getSelector();
    assertNotNull(selector1.findMatchingSelector(valueSpec("scheme", "value1"), "default", _noOpResolver));
    assertNotNull(selector1.findMatchingSelector(valueSpec("scheme", "value2"), "default", _noOpResolver));
    assertNull(selector1.findMatchingSelector(valueSpec("scheme", "value"), "default", _noOpResolver));

    PointSelector selector2 = builder().idLike("scheme", "val*").getSelector();
    assertNotNull(selector2.findMatchingSelector(valueSpec("scheme", "value1"), "default", _noOpResolver));
    assertNotNull(selector2.findMatchingSelector(valueSpec("scheme", "value2"), "default", _noOpResolver));
    assertNull(selector2.findMatchingSelector(valueSpec("scheme", "xvalue"), "default", _noOpResolver));

    PointSelector selector3 = builder().idLike("scheme", "val%").getSelector();
    assertNotNull(selector3.findMatchingSelector(valueSpec("scheme", "value1"), "default", _noOpResolver));
    assertNotNull(selector3.findMatchingSelector(valueSpec("scheme", "value2"), "default", _noOpResolver));
    assertNull(selector3.findMatchingSelector(valueSpec("scheme", "xvalue"), "default", _noOpResolver));
  }

  @Test
  public void securityTypes() {
    ExternalId equityId = ExternalId.of("sec", "eq");
    ExternalId optionId = ExternalId.of("sec", "eqo");
    ExternalId forwardId = ExternalId.of("sec", "fx");
    EquitySecurity equity = new EquitySecurity("exch", "excd", "ACME", Currency.USD);
    ExternalId region = ExternalId.of("regionScheme", "regionValue");
    ZonedDateTime now = ZonedDateTime.now();
    FXForwardSecurity fxForward = new FXForwardSecurity(Currency.AUD, 123, Currency.CAD, 321, now, region);
    FXOptionSecurity fxOption = new FXOptionSecurity(Currency.AUD, Currency.CAD, 123, 321, new Expiry(now),
        now, true, new AmericanExerciseType());
    SelectorResolver resolver = mock(SelectorResolver.class);
    when(resolver.resolveSecurity(equityId)).thenReturn(equity);
    when(resolver.resolveSecurity(forwardId)).thenReturn(fxForward);
    when(resolver.resolveSecurity(optionId)).thenReturn(fxOption);

    PointSelector lCaseSelector = builder().securityTypes("equity", "fx_forward").getSelector();
    assertNotNull(lCaseSelector.findMatchingSelector(valueSpec(equityId), "default", resolver));
    assertNotNull(lCaseSelector.findMatchingSelector(valueSpec(forwardId), "default", resolver));
    assertNull(lCaseSelector.findMatchingSelector(valueSpec(optionId), "default", resolver));

    PointSelector mixedCaseSelector = builder().securityTypes("Equity", "FX_Forward").getSelector();
    assertNotNull(mixedCaseSelector.findMatchingSelector(valueSpec(equityId), "default", resolver));
    assertNotNull(mixedCaseSelector.findMatchingSelector(valueSpec(forwardId), "default", resolver));
    assertNull(mixedCaseSelector.findMatchingSelector(valueSpec(optionId), "default", resolver));
  }
}
