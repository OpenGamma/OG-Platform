/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.Collections;

import org.testng.annotations.Test;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.ParameterizedFunction;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.test.MockFunction;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the Fudge builder for {@link ResolutionFailure}
 */
@Test(groups = TestGroup.UNIT)
public class ResolutionFailureFudgeBuilderTest extends AbstractFudgeBuilderTestCase {

  private int _count;

  private ValueRequirement valueRequirement() {
    return new ValueRequirement("Foo" + (_count++), ComputationTargetSpecification.NULL);
  }

  private ParameterizedFunction parameterizedFunction() {
    final ComputationTarget target = new ComputationTarget(ComputationTargetType.NULL, null);
    final MockFunction function = new MockFunction(target);
    return new ParameterizedFunction(function, function.getDefaultParameters());
  }

  private ValueSpecification valueSpecification(final ValueRequirement requirement) {
    return new ValueSpecification(requirement.getValueName(), requirement.getTargetReference().getSpecification(), ValueProperties.with(ValuePropertyNames.FUNCTION, "Mock").get());
  }

  public void testRecursiveRequirement() {
    final ValueRequirement requirement = valueRequirement();
    assertEncodeDecodeCycle(ResolutionFailure.class, ResolutionFailureImpl.recursiveRequirement(requirement));
  }

  public void testFunctionApplication() {
    final ValueRequirement req1 = valueRequirement();
    final ValueRequirement req2 = valueRequirement();
    final ValueRequirement req3 = valueRequirement();
    final ParameterizedFunction function = parameterizedFunction();
    final ValueSpecification spec1 = valueSpecification(req1);
    final ValueSpecification spec2 = valueSpecification(req2);
    assertEncodeDecodeCycle(ResolutionFailure.class, ResolutionFailureImpl.functionApplication(req1, function, spec1).getRequirementsFailed());
    assertEncodeDecodeCycle(ResolutionFailure.class, ResolutionFailureImpl.functionApplication(req1, function, spec1).requirement(req2, ResolutionFailureImpl.recursiveRequirement(req2)));
    assertEncodeDecodeCycle(ResolutionFailure.class, ResolutionFailureImpl.functionApplication(req1, function, spec1).requirements(Collections.singletonMap(spec2, req2)));
    assertEncodeDecodeCycle(ResolutionFailure.class,
        ResolutionFailureImpl.functionApplication(req1, function, spec1).requirements(Collections.singletonMap(spec2, req2)).requirement(req3, ResolutionFailureImpl.unsatisfied(req3)));
    assertEncodeDecodeCycle(ResolutionFailure.class, ResolutionFailureImpl.functionApplication(req1, function, spec1).requirements(Collections.singletonMap(spec2, req2))
        .getAdditionalRequirementsFailed());
    assertEncodeDecodeCycle(ResolutionFailure.class, ResolutionFailureImpl.functionApplication(req1, function, spec1).requirements(Collections.singletonMap(spec2, req2)).getResultsFailed());
    assertEncodeDecodeCycle(ResolutionFailure.class, ResolutionFailureImpl.functionApplication(req1, function, spec1).requirements(Collections.singletonMap(spec2, req2)).suppressed());
    assertEncodeDecodeCycle(ResolutionFailure.class, ResolutionFailureImpl.functionApplication(req1, function, spec1).requirements(Collections.singletonMap(spec2, req2)).lateResolutionFailure());
    assertEncodeDecodeCycle(ResolutionFailure.class,
        ResolutionFailureImpl.functionApplication(req1, function, spec1).requirements(Collections.singletonMap(spec2, req2)).additionalRequirement(req3, ResolutionFailureImpl.unsatisfied(req3)));
    assertEncodeDecodeCycle(ResolutionFailure.class, ResolutionFailureImpl.functionApplication(req1, function, spec1).requirements(Collections.singletonMap(spec2, req2))
            .additionalRequirement(req3, ResolutionFailureImpl.recursiveRequirement(req3)));
  }

  public void testNoFunctions() {
    final ValueRequirement requirement = valueRequirement();
    assertEncodeDecodeCycle(ResolutionFailure.class, ResolutionFailureImpl.noFunctions(requirement));
  }

  public void testCouldNotResolve() {
    final ValueRequirement requirement = valueRequirement();
    assertEncodeDecodeCycle(ResolutionFailure.class, ResolutionFailureImpl.couldNotResolve(requirement));
  }

  public void testUnsatisfied() {
    final ValueRequirement requirement = valueRequirement();
    assertEncodeDecodeCycle(ResolutionFailure.class, ResolutionFailureImpl.unsatisfied(requirement));
  }

  public void testMarketDataMissing() {
    final ValueRequirement requirement = valueRequirement();
    assertEncodeDecodeCycle(ResolutionFailure.class, ResolutionFailureImpl.marketDataMissing(requirement));
  }

}
