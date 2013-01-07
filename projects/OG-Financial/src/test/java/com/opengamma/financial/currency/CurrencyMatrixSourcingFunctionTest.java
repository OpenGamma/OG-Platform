/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.currency;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Collections;
import java.util.Set;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.EmptyFunctionInputs;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputsImpl;
import com.opengamma.engine.target.ComputationTargetRequirement;
import com.opengamma.engine.target.ComputationTargetSpecificationResolver;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.target.DefaultComputationTargetSpecificationResolver;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.money.Currency;

/**
 * Test the {@link CurrencyMatrixSourcingFunction}.
 */
public class CurrencyMatrixSourcingFunctionTest {

  private final Currency _currencyUSD = Currency.USD;
  private final Currency _currencyGBP = Currency.GBP;
  private final Currency _currencyEUR = Currency.EUR;
  private final double _rateUSD_GBP = 1.6;
  private final double _rateEUR_GBP = 1.1;
  private FunctionExecutionContext _functionExecutionContext;
  private FunctionCompilationContext _functionCompilationContext;
  private CurrencyMatrixSourcingFunction _function;

  @BeforeMethod
  public void setupContexts() {
    _functionExecutionContext = new FunctionExecutionContext();
    _functionCompilationContext = new FunctionCompilationContext();
    final SimpleCurrencyMatrix matrix = new SimpleCurrencyMatrix();
    matrix.setLiveData(_currencyUSD, _currencyGBP, new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.PRIMITIVE, ExternalId.of("LiveData", "USD_GBP")));
    matrix.setFixedConversion(_currencyEUR, _currencyGBP, _rateEUR_GBP);
    matrix.setCrossConversion(_currencyEUR, _currencyUSD, _currencyGBP);
    OpenGammaCompilationContext.setCurrencyMatrixSource(_functionCompilationContext, new CurrencyMatrixSource() {
      @Override
      public CurrencyMatrix getCurrencyMatrix(final String name) {
        assertEquals("Foo", name);
        return matrix;
      }
    });
    _function = new CurrencyMatrixSourcingFunction("Foo");
    _function.setUniqueId("currencyMatrixSourcing");
    _function.init(_functionCompilationContext);
  }

  @Test
  public void testCanApplyTo() {
    assertTrue(_function.canApplyTo(_functionCompilationContext, new ComputationTarget(ComputationTargetType.PRIMITIVE, UniqueId.of(
        CurrencyConversionFunction.RATE_LOOKUP_SCHEME, "USD_GBP"))));
    assertTrue(_function.canApplyTo(_functionCompilationContext, new ComputationTarget(ComputationTargetType.PRIMITIVE, UniqueId.of(
        CurrencyConversionFunction.RATE_LOOKUP_SCHEME, "GBP_USD"))));
    assertTrue(_function.canApplyTo(_functionCompilationContext, new ComputationTarget(ComputationTargetType.PRIMITIVE, UniqueId.of(
        CurrencyConversionFunction.RATE_LOOKUP_SCHEME, "USD_EUR"))));
    assertTrue(_function.canApplyTo(_functionCompilationContext, new ComputationTarget(ComputationTargetType.PRIMITIVE, UniqueId.of(
        CurrencyConversionFunction.RATE_LOOKUP_SCHEME, "EUR_USD"))));
    assertFalse(_function.canApplyTo(_functionCompilationContext, new ComputationTarget(ComputationTargetType.PRIMITIVE, UniqueId.of(
        CurrencyConversionFunction.RATE_LOOKUP_SCHEME, "USD_CHF"))));
    assertFalse(_function.canApplyTo(_functionCompilationContext, new ComputationTarget(ComputationTargetType.PRIMITIVE, UniqueId.of(
        CurrencyConversionFunction.RATE_LOOKUP_SCHEME, "CHF_USD"))));
  }

  @Test
  public void testGetResults() {
    final ComputationTarget ct = new ComputationTarget(ComputationTargetType.PRIMITIVE, UniqueId.of(CurrencyConversionFunction.RATE_LOOKUP_SCHEME, "USD_EUR"));
    final Set<ValueSpecification> results = _function.getResults(_functionCompilationContext, ct);
    assertEquals(1, results.size());
    final ValueSpecification result = results.iterator().next();
    assertEquals(CurrencyConversionFunction.RATE_LOOKUP_VALUE_NAME, result.getValueName());
    assertEquals(ct.toSpecification(), result.getTargetSpecification());
  }

  @Test
  public void testGetRequirementsAndExecute() {
    final ValueProperties properties = ValueProperties.with(ValuePropertyNames.FUNCTION, "liveDataSourcing").get();
    final ComputationTargetSpecificationResolver resolver = new DefaultComputationTargetSpecificationResolver();

    // Require value the "right" way up
    ComputationTarget outTarget = new ComputationTarget(ComputationTargetType.PRIMITIVE, UniqueId.of(CurrencyConversionFunction.RATE_LOOKUP_SCHEME, "USD_GBP"));
    ValueRequirement outRequirement = new ValueRequirement(CurrencyConversionFunction.RATE_LOOKUP_VALUE_NAME, outTarget.toSpecification());
    Set<ValueRequirement> inRequirements = _function.getRequirements(_functionCompilationContext, outTarget, outRequirement);
    assertEquals(1, inRequirements.size());
    final ComputationTargetRequirement inRequirementTarget = new ComputationTargetRequirement(ComputationTargetType.PRIMITIVE, ExternalId.of("LiveData", "USD_GBP"));
    ValueRequirement inRequirement = new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, inRequirementTarget);
    assertTrue(inRequirements.contains(inRequirement));
    Set<ComputedValue> outputs = _function.execute(_functionExecutionContext, new FunctionInputsImpl(resolver.atVersionCorrection(VersionCorrection.LATEST), new ComputedValue(new ValueSpecification(
        inRequirement.getValueName(), ComputationTargetSpecification.of(UniqueId.of("LiveData", "USD_GBP")), properties), _rateUSD_GBP)), outTarget, Collections.singleton(outRequirement));
    assertEquals(1, outputs.size());
    ComputedValue output = outputs.iterator().next();
    assertTrue(output.getValue() instanceof Double);
    assertEquals(_rateUSD_GBP, (Double) output.getValue(), Double.MIN_NORMAL);

    // Require value the "wrong" way up
    outTarget = new ComputationTarget(ComputationTargetType.PRIMITIVE, UniqueId.of(CurrencyConversionFunction.RATE_LOOKUP_SCHEME, "GBP_USD"));
    outRequirement = new ValueRequirement(CurrencyConversionFunction.RATE_LOOKUP_VALUE_NAME, outTarget.toSpecification());
    inRequirements = _function.getRequirements(_functionCompilationContext, outTarget, outRequirement);
    assertEquals(1, inRequirements.size());
    inRequirement = new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, inRequirementTarget);
    assertTrue(inRequirements.contains(inRequirement));
    outputs = _function.execute(_functionExecutionContext,
        new FunctionInputsImpl(resolver.atVersionCorrection(VersionCorrection.LATEST), new ComputedValue(new ValueSpecification(inRequirement.getValueName(),
            ComputationTargetSpecification.of(UniqueId.of("LiveData", "USD_GBP")), properties), _rateUSD_GBP)), outTarget, Collections.singleton(outRequirement));
    assertEquals(1, outputs.size());
    output = outputs.iterator().next();
    assertTrue(output.getValue() instanceof Double);
    assertEquals(1.0 / _rateUSD_GBP, (Double) output.getValue(), Double.MIN_NORMAL);

    // Require no additional live data
    outTarget = new ComputationTarget(ComputationTargetType.PRIMITIVE, UniqueId.of(CurrencyConversionFunction.RATE_LOOKUP_SCHEME, "GBP_EUR"));
    outRequirement = new ValueRequirement(CurrencyConversionFunction.RATE_LOOKUP_VALUE_NAME, outTarget.toSpecification());
    inRequirements = _function.getRequirements(_functionCompilationContext, outTarget, outRequirement);
    assertEquals(0, inRequirements.size());
    outputs = _function.execute(_functionExecutionContext, new EmptyFunctionInputs(), outTarget, Collections.singleton(outRequirement));
    assertEquals(1, outputs.size());
    output = outputs.iterator().next();
    assertTrue(output.getValue() instanceof Double);
    assertEquals(1.0 / _rateEUR_GBP, (Double) output.getValue(), Double.MIN_NORMAL);

    // Require intermediate value
    outTarget = new ComputationTarget(ComputationTargetType.PRIMITIVE, UniqueId.of(CurrencyConversionFunction.RATE_LOOKUP_SCHEME, "EUR_USD"));
    outRequirement = new ValueRequirement(CurrencyConversionFunction.RATE_LOOKUP_VALUE_NAME, outTarget.toSpecification());
    inRequirements = _function.getRequirements(_functionCompilationContext, outTarget, outRequirement);
    assertEquals(1, inRequirements.size());
    inRequirement = new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, inRequirementTarget);
    assertTrue(inRequirements.contains(inRequirement));
    outputs = _function.execute(_functionExecutionContext,
        new FunctionInputsImpl(resolver.atVersionCorrection(VersionCorrection.LATEST), new ComputedValue(new ValueSpecification(inRequirement.getValueName(),
            ComputationTargetSpecification.of(UniqueId.of("LiveData", "USD_GBP")), properties), _rateUSD_GBP)), outTarget, Collections.singleton(outRequirement));
    assertEquals(1, outputs.size());
    output = outputs.iterator().next();
    assertTrue(output.getValue() instanceof Double);
    assertEquals(_rateEUR_GBP / _rateUSD_GBP, (Double) output.getValue(), Double.MIN_NORMAL);
  }

}
