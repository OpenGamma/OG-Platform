/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function.dsl;

import com.opengamma.core.position.impl.SimplePosition;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.function.dsl.functions.BaseNonCompiledInvoker;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.*;
import com.opengamma.id.UniqueId;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.test.TestGroup;

import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.opengamma.engine.function.dsl.Function.*;
import static com.opengamma.engine.function.dsl.TargetSpecificationReference.originalTarget;
import static com.opengamma.engine.function.dsl.properties.RecordingValueProperties.copyFrom;
import static com.opengamma.engine.function.dsl.properties.RecordingValueProperties.desiredValue;
import static com.opengamma.engine.value.ValueRequirementNames.DV01;
import static com.opengamma.engine.value.ValueRequirementNames.PV01;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT, enabled = false, description = "FAILING")
public class BaseNonCompiledInvokerTest {

  public void getResultsTest_1() {
    FunctionCompilationContext fcctx = mock(FunctionCompilationContext.class);
    DV01_test_fun dv01 = new DV01_test_fun();

    assertTrue(dv01.canApplyTo(fcctx, new ComputationTarget(ComputationTargetType.POSITION, new SimplePosition())));

    ComputationTargetSpecification cts = new ComputationTargetSpecification(ComputationTargetType.POSITION, UniqueId.of("a", "b"));
    ComputationTarget ct = mock(ComputationTarget.class);
    when(ct.toSpecification()).thenReturn(cts);
    Set<ValueSpecification> specs = dv01.getResults(null, ct);
    assertEquals(specs.size(), 1);

    ValueSpecification spec = specs.iterator().next();
    assertEquals(spec.getProperties(), ValueProperties.all());
    assertEquals(spec.getTargetSpecification(), cts);
    assertEquals(spec.getValueName(), "DV01");
  }

  public void getRequirements() {
    FunctionCompilationContext fcctx = mock(FunctionCompilationContext.class);
    DV01_test_fun dv01 = new DV01_test_fun();

    assertTrue(dv01.canApplyTo(fcctx, new ComputationTarget(ComputationTargetType.POSITION, new SimplePosition())));

    ComputationTargetSpecification cts = new ComputationTargetSpecification(ComputationTargetType.POSITION, UniqueId.of("a", "b"));
    ComputationTarget ct = mock(ComputationTarget.class);
    when(ct.toSpecification()).thenReturn(cts);

    ValueProperties valueProperties = ValueProperties.builder().with("A", "1").with("B", "1").with(ValuePropertyNames.FUNCTION, "PV01_Function").get();
    ValueRequirement desiredValue = new ValueRequirement("PV01", ct.getType(), ct.getUniqueId(), valueProperties);
    Set<ValueRequirement> requirements = dv01.getRequirements(null, ct, desiredValue);
    assertEquals(requirements.size(), 1);

    ValueRequirement requirement = requirements.iterator().next();
    assertEquals(requirement.getConstraints(), requirement.getConstraints());
    assertEquals(requirement.getTargetReference().getSpecification(), cts);
    assertEquals(requirement.getValueName(), "PV01");
  }

  public void getResultsTest_2() {
    FunctionCompilationContext fcctx = mock(FunctionCompilationContext.class);
    DV01_test_fun dv01 = new DV01_test_fun();

    assertTrue(dv01.canApplyTo(fcctx, new ComputationTarget(ComputationTargetType.POSITION, new SimplePosition())));

    ComputationTargetSpecification cts = new ComputationTargetSpecification(ComputationTargetType.POSITION, UniqueId.of("a", "b"));
    ComputationTarget ct = mock(ComputationTarget.class);
    when(ct.toSpecification()).thenReturn(cts);
    ValueProperties valueProperties = ValueProperties.builder().with("A", "1").with("B", "1").with(ValuePropertyNames.FUNCTION, "PV01_Function").get();
    ValueRequirement desiredValue = new ValueRequirement("PV01", ct.getType(), ct.getUniqueId(), valueProperties);
    ValueSpecification specifiedValue = new ValueSpecification(desiredValue.getValueName(), desiredValue.getTargetReference().getSpecification(), desiredValue.getConstraints().copy().with("X", "3").get());
    Map<ValueSpecification, ValueRequirement> inputSpecificationsMap = new HashMap<ValueSpecification, ValueRequirement>();
    inputSpecificationsMap.put(specifiedValue, desiredValue);
    Set<ValueSpecification> specs = dv01.getResults(null, ct, inputSpecificationsMap);
    assertEquals(specs.size(), 1);

    ValueSpecification spec = specs.iterator().next();
    assertEquals(spec.getProperties().getValues(ValuePropertyNames.FUNCTION), Collections.singleton(dv01.getUniqueId()));
    assertEquals(spec.getTargetSpecification(), cts);
    assertEquals(spec.getValueName(), "DV01");
  }

  class DV01_test_fun extends BaseNonCompiledInvoker {
    @Override
    protected FunctionSignature functionSignature() {

      return function("DV01Function", ComputationTargetType.POSITION)

          .outputs(

              output(DV01)
                  .targetSpec(originalTarget())  //takes  ComputationTargetSpecification or TargetSpecificationReference
                  .properties(copyFrom(PV01)
                      .withReplacement(ValuePropertyNames.FUNCTION, getUniqueId())
                      .withAny(ValuePropertyNames.SHIFT))
          )
          .inputs(
              input(PV01)
                  .targetSpec(originalTarget())
                  .properties(desiredValue()
                      .withoutAny(ValuePropertyNames.SHIFT)
                  )
          );
    }

    @Override
    public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
      return null;
    }

    @Override
    public String getUniqueId() {
      return "DV01_Test_Function";
    }
  }

}
