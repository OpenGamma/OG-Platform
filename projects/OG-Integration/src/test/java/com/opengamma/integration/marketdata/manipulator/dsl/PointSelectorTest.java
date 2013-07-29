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
import com.opengamma.core.marketdatasnapshot.YieldCurveKey;
import com.opengamma.engine.marketdata.manipulator.SelectorResolver;
import com.opengamma.engine.marketdata.manipulator.StructureIdentifier;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.option.AmericanExerciseType;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;

public class PointSelectorTest {

  private final SelectorResolver _noOpResolver = mock(SelectorResolver.class);
  private final StructureIdentifier<ExternalId> _structureId = structureId("scheme", "id");

  private PointSelector.Builder builder() {
    return new PointSelector.Builder(new Scenario("scenarioName"));
  }

  private StructureIdentifier<ExternalId> structureId(String scheme, String value) {
    return StructureIdentifier.of(ExternalId.of(scheme, value));
  }

  @Test
  public void structureType() {
    PointSelector selector = builder().getSelector(); // will match any ExternalId
    assertNotNull(selector.findMatchingSelector(_structureId, "default", _noOpResolver));
    StructureIdentifier<YieldCurveKey> structureId = StructureIdentifier.of(new YieldCurveKey(Currency.GBP, "a curve"));
    assertNull(selector.findMatchingSelector(structureId, "default", _noOpResolver));
  }

  @Test
  public void ids() {
    PointSelector selector = builder().ids("scheme~value1", "scheme~value2").getSelector();
    assertNotNull(selector.findMatchingSelector(structureId("scheme", "value1"), "calcConfig", _noOpResolver));
    assertNotNull(selector.findMatchingSelector(structureId("scheme", "value2"), "calcConfig", _noOpResolver));
    assertNull(selector.findMatchingSelector(structureId("scheme", "value3"), "calcConfig", _noOpResolver));
  }

  @Test
  public void calcConfigNames() {
    PointSelector selector = new PointSelector(Sets.newHashSet("default", "cc1"), null, null, null, null, null, null);
    assertNotNull(selector.findMatchingSelector(_structureId, "default", _noOpResolver));
    assertNotNull(selector.findMatchingSelector(_structureId, "cc1", _noOpResolver));
    assertNull(selector.findMatchingSelector(_structureId, "cc2", _noOpResolver));
  }

  @Test
  public void idMatches() {
    PointSelector selector = builder().idMatches("scheme", "value\\d").getSelector();
    assertNotNull(selector.findMatchingSelector(structureId("scheme", "value1"), "default", _noOpResolver));
    assertNotNull(selector.findMatchingSelector(structureId("scheme", "value2"), "default", _noOpResolver));
    assertNull(selector.findMatchingSelector(structureId("scheme", "value"), "default", _noOpResolver));
  }

  @Test
  public void idLike() {
    PointSelector selector1 = builder().idLike("scheme", "value?").getSelector();
    assertNotNull(selector1.findMatchingSelector(structureId("scheme", "value1"), "default", _noOpResolver));
    assertNotNull(selector1.findMatchingSelector(structureId("scheme", "value2"), "default", _noOpResolver));
    assertNull(selector1.findMatchingSelector(structureId("scheme", "value"), "default", _noOpResolver));

    PointSelector selector2 = builder().idLike("scheme", "val*").getSelector();
    assertNotNull(selector2.findMatchingSelector(structureId("scheme", "value1"), "default", _noOpResolver));
    assertNotNull(selector2.findMatchingSelector(structureId("scheme", "value2"), "default", _noOpResolver));
    assertNull(selector2.findMatchingSelector(structureId("scheme", "xvalue"), "default", _noOpResolver));

    PointSelector selector3 = builder().idLike("scheme", "val%").getSelector();
    assertNotNull(selector3.findMatchingSelector(structureId("scheme", "value1"), "default", _noOpResolver));
    assertNotNull(selector3.findMatchingSelector(structureId("scheme", "value2"), "default", _noOpResolver));
    assertNull(selector3.findMatchingSelector(structureId("scheme", "xvalue"), "default", _noOpResolver));
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
    assertNotNull(lCaseSelector.findMatchingSelector(StructureIdentifier.of(equityId), "default", resolver));
    assertNotNull(lCaseSelector.findMatchingSelector(StructureIdentifier.of(forwardId), "default", resolver));
    assertNull(lCaseSelector.findMatchingSelector(StructureIdentifier.of(optionId), "default", resolver));

    PointSelector mixedCaseSelector = builder().securityTypes("Equity", "FX_Forward").getSelector();
    assertNotNull(mixedCaseSelector.findMatchingSelector(StructureIdentifier.of(equityId), "default", resolver));
    assertNotNull(mixedCaseSelector.findMatchingSelector(StructureIdentifier.of(forwardId), "default", resolver));
    assertNull(mixedCaseSelector.findMatchingSelector(StructureIdentifier.of(optionId), "default", resolver));
  }
}
