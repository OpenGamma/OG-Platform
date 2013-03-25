/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.currency;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Set;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.target.ComputationTargetRequirement;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;

/**
 * Test the {@link CurrencyMatrixLookupFunction}.
 */
@Test(groups = TestGroup.UNIT)
public class CurrencyMatrixLookupFunctionTest {

  private FunctionCompilationContext _functionCompilationContext;
  private CurrencyMatrixLookupFunction _function;

  @BeforeMethod
  public void setupContexts() {
    _functionCompilationContext = new FunctionCompilationContext();
    _function = new CurrencyMatrixLookupFunction("Foo");
    _function.setUniqueId("currencyMatrixLookup");
    _function.init(_functionCompilationContext);
  }

  @Test
  public void testGetResults1() {
    final ComputationTarget ct = new ComputationTarget(CurrencyPair.TYPE, CurrencyPair.parse("USD/EUD"));
    final Set<ValueSpecification> results = _function.getResults(_functionCompilationContext, ct);
    assertEquals(3, results.size());
  }

  @Test
  public void testGetRequirements() {
    ComputationTarget target = new ComputationTarget(CurrencyPair.TYPE, CurrencyPair.parse("GBP/USD"));
    Set<ValueRequirement> requirements = _function.getRequirements(_functionCompilationContext, target, new ValueRequirement(ValueRequirementNames.SPOT_RATE, target.toSpecification()));
    assertEquals(Iterables.getOnlyElement(requirements),
        new ValueRequirement(ValueRequirementNames.SPOT_RATE, new ComputationTargetRequirement(CurrencyMatrixResolver.TYPE, ExternalId.of(CurrencyMatrixResolver.IDENTIFIER_SCHEME, "Foo")),
            ValueProperties.with(AbstractCurrencyMatrixSourcingFunction.SOURCE_CURRENCY_PROPERTY, "USD").with(AbstractCurrencyMatrixSourcingFunction.TARGET_CURRENCY_PROPERTY, "GBP").get()));
    requirements = _function.getRequirements(_functionCompilationContext, target,
        new ValueRequirement(ValueRequirementNames.SPOT_RATE, target.toSpecification(), ValueProperties.with(CurrencyMatrixLookupFunction.CURRENCY_MATRIX_NAME_PROPERTY, "Bar").get()));
    assertEquals(Iterables.getOnlyElement(requirements),
        new ValueRequirement(ValueRequirementNames.SPOT_RATE, new ComputationTargetRequirement(CurrencyMatrixResolver.TYPE, ExternalId.of(CurrencyMatrixResolver.IDENTIFIER_SCHEME, "Bar")),
            ValueProperties.with(AbstractCurrencyMatrixSourcingFunction.SOURCE_CURRENCY_PROPERTY, "USD").with(AbstractCurrencyMatrixSourcingFunction.TARGET_CURRENCY_PROPERTY, "GBP").get()));
    requirements = _function.getRequirements(_functionCompilationContext, target,
        new ValueRequirement(ValueRequirementNames.SPOT_RATE, target.toSpecification(), ValueProperties.with(CurrencyMatrixLookupFunction.CURRENCY_MATRIX_NAME_PROPERTY, "Bar", "Foo").get()));
    assertEquals(
        Iterables.getOnlyElement(requirements),
        new ValueRequirement(ValueRequirementNames.SPOT_RATE, new ComputationTargetRequirement(CurrencyMatrixResolver.TYPE, ExternalIdBundle.of(
            ExternalId.of(CurrencyMatrixResolver.IDENTIFIER_SCHEME, "Foo"), ExternalId.of(CurrencyMatrixResolver.IDENTIFIER_SCHEME, "Bar"))),
            ValueProperties.with(AbstractCurrencyMatrixSourcingFunction.SOURCE_CURRENCY_PROPERTY, "USD").with(AbstractCurrencyMatrixSourcingFunction.TARGET_CURRENCY_PROPERTY, "GBP").get()));
    requirements = _function.getRequirements(_functionCompilationContext, target,
        new ValueRequirement(ValueRequirementNames.SPOT_RATE, target.toSpecification(), ValueProperties.withAny(CurrencyMatrixLookupFunction.CURRENCY_MATRIX_NAME_PROPERTY).get()));
    assertEquals(Iterables.getOnlyElement(requirements),
        new ValueRequirement(ValueRequirementNames.SPOT_RATE, new ComputationTargetRequirement(CurrencyMatrixResolver.TYPE, ExternalId.of(CurrencyMatrixResolver.IDENTIFIER_SCHEME, "Foo")),
            ValueProperties.with(AbstractCurrencyMatrixSourcingFunction.SOURCE_CURRENCY_PROPERTY, "USD").with(AbstractCurrencyMatrixSourcingFunction.TARGET_CURRENCY_PROPERTY, "GBP").get()));
  }

  @Test
  public void testGetResults2() {
    ComputationTarget target = new ComputationTarget(CurrencyPair.TYPE, CurrencyPair.parse("GBP/USD"));
    Set<ValueSpecification> results = _function.getResults(
        _functionCompilationContext,
        target,
        ImmutableMap.of(
            new ValueSpecification(ValueRequirementNames.SPOT_RATE, new ComputationTargetSpecification(CurrencyMatrixResolver.TYPE, UniqueId.of("Matrix", "0")), ValueProperties.with(
                ValuePropertyNames.FUNCTION, "Test").get()),
            new ValueRequirement(ValueRequirementNames.SPOT_RATE, new ComputationTargetRequirement(CurrencyMatrixResolver.TYPE, ExternalId.of(CurrencyMatrixResolver.IDENTIFIER_SCHEME, "Foo")))));
    assertEquals(
        Iterables.getOnlyElement(results),
        new ValueSpecification(ValueRequirementNames.SPOT_RATE, target.toSpecification(), ValueProperties.with(ValuePropertyNames.FUNCTION, "currencyMatrixLookup")
            .with(CurrencyMatrixLookupFunction.CURRENCY_MATRIX_NAME_PROPERTY, "Foo").get()));
  }
}
