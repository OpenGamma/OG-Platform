/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.currency;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Collections;
import java.util.Set;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.DefaultComputationTargetResolver;
import com.opengamma.engine.function.EmptyFunctionInputs;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputsImpl;
import com.opengamma.engine.target.ComputationTargetRequirement;
import com.opengamma.engine.target.ComputationTargetSpecificationResolver;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class CurrencyMatrixSpotSourcingFunctionTest {

  private final Currency _currencyUSD = Currency.USD;
  private final Currency _currencyGBP = Currency.GBP;
  private final Currency _currencyEUR = Currency.EUR;
  private final double _rateUSD_GBP = 1.6;
  private final double _rateEUR_GBP = 1.1;
  private FunctionExecutionContext _functionExecutionContext;
  private FunctionCompilationContext _functionCompilationContext;
  private CurrencyMatrixSpotSourcingFunction _function;
  private ComputationTarget _matrixTarget;

  @BeforeMethod
  public void setupContexts() {
    _functionExecutionContext = new FunctionExecutionContext();
    _functionCompilationContext = new FunctionCompilationContext();
    SimpleCurrencyMatrix matrix = new SimpleCurrencyMatrix();
    matrix.setUniqueId(UniqueId.of("Matrix", "Test"));
    matrix.setLiveData(_currencyUSD, _currencyGBP, new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.PRIMITIVE, ExternalId.of("LiveData", "USD_GBP")));
    matrix.setFixedConversion(_currencyEUR, _currencyGBP, _rateEUR_GBP);
    matrix.setCrossConversion(_currencyEUR, _currencyUSD, _currencyGBP);
    _matrixTarget = new ComputationTarget(CurrencyMatrixResolver.TYPE, matrix);
    _function = new CurrencyMatrixSpotSourcingFunction();
    _function.setUniqueId("currencyMatrixSourcing");
    _function.init(_functionCompilationContext);
  }

  @Test
  public void testGetResults() {
    final Set<ValueSpecification> results = _function.getResults(_functionCompilationContext, _matrixTarget);
    assertEquals(6, results.size());
  }

  @Test
  public void testGetRequirementsAndExecute() {
    final ValueProperties properties = ValueProperties.with(ValuePropertyNames.FUNCTION, "liveDataSourcing").get();
    final ComputationTargetSpecificationResolver resolver = new DefaultComputationTargetResolver().getSpecificationResolver();

    // Require value the "right" way up
    // [PLAT-2290] Execute should be taking a ValueSpecification, getRequirements should be taking a ValueRequirement
    ValueRequirement outRequirement = new ValueRequirement(ValueRequirementNames.SPOT_RATE, _matrixTarget.toSpecification(), ValueProperties
        .with(AbstractCurrencyMatrixSourcingFunction.SOURCE_CURRENCY_PROPERTY, "USD").with(AbstractCurrencyMatrixSourcingFunction.TARGET_CURRENCY_PROPERTY, "GBP")
        .with(ValuePropertyNames.FUNCTION, "CMSF").get());
    Set<ValueRequirement> inRequirements = _function.getRequirements(_functionCompilationContext, _matrixTarget, outRequirement);
    assertEquals(1, inRequirements.size());
    final ComputationTargetRequirement inRequirementTarget = new ComputationTargetRequirement(ComputationTargetType.PRIMITIVE, ExternalId.of("LiveData", "USD_GBP"));
    for (ValueRequirement requirement : inRequirements) {
      assertEquals(requirement.getValueName(), MarketDataRequirementNames.MARKET_VALUE);
      assertEquals(requirement.getTargetReference(), inRequirementTarget);
      assertTrue(requirement.getConstraints().isSatisfiedBy(ValueProperties.none()));
    }
    Set<ComputedValue> outputs = _function.execute(_functionExecutionContext, new FunctionInputsImpl(resolver.atVersionCorrection(VersionCorrection.LATEST), new ComputedValue(new ValueSpecification(
        MarketDataRequirementNames.MARKET_VALUE, ComputationTargetSpecification.of(UniqueId.of("ExternalId-LiveData", "USD_GBP")), properties), _rateUSD_GBP)), _matrixTarget,
        Collections.singleton(outRequirement));
    assertEquals(1, outputs.size());
    ComputedValue output = outputs.iterator().next();
    assertTrue(output.getValue() instanceof Double);
    assertEquals(_rateUSD_GBP, (Double) output.getValue(), Double.MIN_NORMAL);

    // Require value the "wrong" way up
    // [PLAT-2290] Execute should be taking a ValueSpecification, getRequirements should be taking a ValueRequirement
    outRequirement = new ValueRequirement(ValueRequirementNames.SPOT_RATE, _matrixTarget.toSpecification(), ValueProperties
        .with(AbstractCurrencyMatrixSourcingFunction.SOURCE_CURRENCY_PROPERTY, "GBP").with(AbstractCurrencyMatrixSourcingFunction.TARGET_CURRENCY_PROPERTY, "USD")
        .with(ValuePropertyNames.FUNCTION, "CMSF").get());
    inRequirements = _function.getRequirements(_functionCompilationContext, _matrixTarget, outRequirement);
    assertEquals(1, inRequirements.size());
    for (ValueRequirement requirement : inRequirements) {
      assertEquals(requirement.getValueName(), MarketDataRequirementNames.MARKET_VALUE);
      assertEquals(requirement.getTargetReference(), inRequirementTarget);
      assertTrue(requirement.getConstraints().isSatisfiedBy(ValueProperties.none()));
    }
    outputs = _function.execute(_functionExecutionContext,
        new FunctionInputsImpl(resolver.atVersionCorrection(VersionCorrection.LATEST), new ComputedValue(new ValueSpecification(MarketDataRequirementNames.MARKET_VALUE,
            ComputationTargetSpecification.of(UniqueId.of("ExternalId-LiveData", "USD_GBP")), properties), _rateUSD_GBP)), _matrixTarget, Collections.singleton(outRequirement));
    assertEquals(1, outputs.size());
    output = outputs.iterator().next();
    assertTrue(output.getValue() instanceof Double);
    assertEquals(1.0 / _rateUSD_GBP, (Double) output.getValue(), Double.MIN_NORMAL);

    // Require no additional live data
    // [PLAT-2290] Execute should be taking a ValueSpecification, getRequirements should be taking a ValueRequirement
    outRequirement = new ValueRequirement(ValueRequirementNames.SPOT_RATE, _matrixTarget.toSpecification(), ValueProperties
        .with(AbstractCurrencyMatrixSourcingFunction.SOURCE_CURRENCY_PROPERTY, "GBP").with(AbstractCurrencyMatrixSourcingFunction.TARGET_CURRENCY_PROPERTY, "EUR")
        .with(ValuePropertyNames.FUNCTION, "CMSF").get());
    inRequirements = _function.getRequirements(_functionCompilationContext, _matrixTarget, outRequirement);
    assertEquals(0, inRequirements.size());
    outputs = _function.execute(_functionExecutionContext, new EmptyFunctionInputs(), _matrixTarget, Collections.singleton(outRequirement));
    assertEquals(1, outputs.size());
    output = outputs.iterator().next();
    assertTrue(output.getValue() instanceof Double);
    assertEquals(1.0 / _rateEUR_GBP, (Double) output.getValue(), Double.MIN_NORMAL);

    // Require intermediate value
    // [PLAT-2290] Execute should be taking a ValueSpecification, getRequirements should be taking a ValueRequirement
    outRequirement = new ValueRequirement(ValueRequirementNames.SPOT_RATE, _matrixTarget.toSpecification(), ValueProperties
        .with(AbstractCurrencyMatrixSourcingFunction.SOURCE_CURRENCY_PROPERTY, "EUR").with(AbstractCurrencyMatrixSourcingFunction.TARGET_CURRENCY_PROPERTY, "USD")
        .with(ValuePropertyNames.FUNCTION, "CMSF").get());
    inRequirements = _function.getRequirements(_functionCompilationContext, _matrixTarget, outRequirement);
    assertEquals(1, inRequirements.size());
    for (ValueRequirement requirement : inRequirements) {
      assertEquals(requirement.getValueName(), MarketDataRequirementNames.MARKET_VALUE);
      assertEquals(requirement.getTargetReference(), inRequirementTarget);
      assertTrue(requirement.getConstraints().isSatisfiedBy(ValueProperties.none()));
    }
    outputs = _function.execute(_functionExecutionContext,
        new FunctionInputsImpl(resolver.atVersionCorrection(VersionCorrection.LATEST), new ComputedValue(new ValueSpecification(MarketDataRequirementNames.MARKET_VALUE,
            ComputationTargetSpecification.of(UniqueId.of("ExternalId-LiveData", "USD_GBP")), properties), _rateUSD_GBP)), _matrixTarget, Collections.singleton(outRequirement));
    assertEquals(1, outputs.size());
    output = outputs.iterator().next();
    assertTrue(output.getValue() instanceof Double);
    assertEquals(_rateEUR_GBP / _rateUSD_GBP, (Double) output.getValue(), Double.MIN_NORMAL);
  }

}
