/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueProperties.Builder;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class PropertyPreservingFunctionTest {

  private MockPropertyPreservingFunction getFunction() {
    final MockPropertyPreservingFunction func = new MockPropertyPreservingFunction(Lists.newArrayList("Prop", "A", "B", "C", "D", "E", "F"), new ArrayList<String>());
    return func;
  }

  @Test
  public void EmptyProperties() {
    final MockPropertyPreservingFunction func = getFunction();
    final ValueProperties props = ValueProperties.none();
    final ValueProperties expected = ValueProperties.none();
    assertEqual(expected, func, props);
  }

  @Test
  public void SingleMatchingProperty() {
    final MockPropertyPreservingFunction func = getFunction();
    assertEqual(ValueProperties.builder().with("A", "V").get(), func, ValueProperties.builder().with("A", "V").get(), ValueProperties.builder().with("A", "V").get());
  }

  @Test
  public void SingleNonMatchingProperty() {
    final MockPropertyPreservingFunction func = getFunction();
    final List<ValueSpecification> specses = getSpecs(ValueProperties.builder().with("A", "V").get(), ValueProperties.builder().with("A", "X").get());
    assertNull(func.getResultProperties(specses));
  }

  @Test
  public void SingleNonMatchingOtherProperty() {
    final MockPropertyPreservingFunction func = getFunction();
    assertEqual(ValueProperties.none(), func, ValueProperties.builder().with("Z", "A").get(), ValueProperties.builder().with("Z", "B").get());
  }

  @Test
  public void SingleMatchingOtherProperty() {
    final MockPropertyPreservingFunction func = getFunction();
    assertEqual(ValueProperties.none(), func, ValueProperties.builder().with("Z", "A").get(), ValueProperties.builder().with("Z", "A").get());
  }

  @Test
  public void OptionalProperty() {
    final MockPropertyPreservingFunction func = getFunction();

    final ValueProperties p = ValueProperties.builder().withOptional("C").withAny("C").with("D", "X").withOptional("D").get();
    assertEqual(p, func, p, p);
  }

  private void assertEqual(final ValueProperties expected, final MockPropertyPreservingFunction func, final ValueProperties... inputs) {
    final List<ValueSpecification> specses = getSpecs(inputs);
    //Check even empty sets
    assertEqualOrdered(expected, func, specses);
    //Try and check a few permutations
    //TODO: check non rotation permutations
    for (int i = 0; i < specses.size(); i++) {
      assertEqualOrdered(expected, func, specses);
      Collections.rotate(specses, 1);
    }
    //Check repeats, since there are 2 code branches
    final List<ValueSpecification> doubled = new ArrayList<ValueSpecification>(inputs.length * 2);
    doubled.addAll(specses);
    doubled.addAll(specses);
    assertEqualOrdered(expected, func, doubled);
  }

  private void assertEqualOrdered(final ValueProperties expected, final MockPropertyPreservingFunction func, final Collection<ValueSpecification> specses) {
    final ValueProperties resultProperties = func.getResultProperties(specses);
    final ValueProperties filteredResult = resultProperties.copy().withoutAny(ValuePropertyNames.FUNCTION).get();
    assertEquals(expected, filteredResult);
  }

  private List<ValueSpecification> getSpecs(final ValueProperties... props) {
    return getSpecs(Lists.newArrayList(props));
  }

  private List<ValueSpecification> getSpecs(final Collection<ValueProperties> props) {
    final List<ValueSpecification> ret = new ArrayList<ValueSpecification>();
    for (final ValueProperties valueProp : props) {
      ret.add(getSpec(valueProp));
    }
    return ret;
  }

  private ValueSpecification getSpec(final ValueProperties props) {
    final Builder realProps = props.copy().with(ValuePropertyNames.FUNCTION, "SomeFunc");
    final ValueSpecification spec = new ValueSpecification("X", ComputationTargetSpecification.of(Currency.USD), realProps.get());
    return spec;
  }

  private class MockPropertyPreservingFunction extends PropertyPreservingFunction {

    private final Collection<String> _preservedProperties;
    private final Collection<String> _optionalPreservedProperties;

    public MockPropertyPreservingFunction(final Collection<String> preservedProperties, final Collection<String> optionalPreservedProperties) {
      super();
      _preservedProperties = preservedProperties;
      _optionalPreservedProperties = optionalPreservedProperties;
      setUniqueId("SOMEID");
    }

    @Override
    public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs,
        final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
      throw new OpenGammaRuntimeException("Shouldn't be called");
    }

    @Override
    public ComputationTargetType getTargetType() {
      throw new OpenGammaRuntimeException("Shouldn't be called");
    }

    @Override
    public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
      throw new OpenGammaRuntimeException("Shouldn't be called");
    }

    @Override
    public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target,
        final ValueRequirement desiredValue) {
      throw new OpenGammaRuntimeException("Shouldn't be called");
    }

    @Override
    public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
      throw new OpenGammaRuntimeException("Shouldn't be called");
    }

    @Override
    protected Collection<String> getPreservedProperties() {
      return _preservedProperties;
    }

    @Override
    protected Collection<String> getOptionalPreservedProperties() {
      return _optionalPreservedProperties;
    }

  }
}
