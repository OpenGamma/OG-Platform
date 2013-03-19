/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.Assert;
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
import com.opengamma.util.test.TestGroup;

/**
 * Tests the support classes for {@link ResolutionFailure}
 */
@Test(groups = TestGroup.UNIT)
public class ResolutionFailureTest {

  private int _count;

  private static final class DebugResolutionFailureVisitor extends ResolutionFailureVisitor<String> {

    @Override
    protected String visitRecursiveRequirement(final ValueRequirement valueRequirement) {
      return "recursiveRequirement=" + valueRequirement;
    }

    @Override
    protected String visitFailedFunction(final ValueRequirement valueRequirement, final String function, final ValueSpecification desiredOutput,
        final Map<ValueSpecification, ValueRequirement> satisfied, final Set<ResolutionFailure> unsatisfied) {
      final StringBuilder sb = new StringBuilder();
      sb.append("failedFunction=").append(valueRequirement).append(",").append(function).append(",").append(desiredOutput).append(",")
          .append(satisfied);
      for (ResolutionFailure requirement : unsatisfied) {
        sb.append(",").append(requirement.accept(this));
      }
      return sb.toString();
    }

    @Override
    protected String visitGetRequirementsFailed(final ValueRequirement valueRequirement, final String function, final ValueSpecification desiredOutput) {
      return "getRequirementsFailed=" + valueRequirement + "," + function + "," + desiredOutput;
    }

    @Override
    protected String visitSuccessfulFunction(final ValueRequirement valueRequirement, final String function, final ValueSpecification desiredOutput,
        final Map<ValueSpecification, ValueRequirement> satisfied) {
      return "successfulFunction=" + valueRequirement + "," + function + "," + desiredOutput + "," + satisfied;
    }

    @Override
    protected String visitGetAdditionalRequirementsFailed(final ValueRequirement valueRequirement, final String function, final ValueSpecification desiredOutput,
        final Map<ValueSpecification, ValueRequirement> requirements) {
      return "getAdditionalRequirementsFailed=" + valueRequirement + "," + function + "," + desiredOutput + "," + requirements;
    }

    @Override
    protected String visitNoFunctions(final ValueRequirement valueRequirement) {
      return "noFunctions=" + valueRequirement;
    }

    @Override
    protected String visitCouldNotResolve(final ValueRequirement valueRequirement) {
      return "couldNotResolve=" + valueRequirement;
    }

    @Override
    protected String visitUnsatisfied(final ValueRequirement valueRequirement) {
      return "unsatisfied=" + valueRequirement;
    }

    @Override
    protected String visitMarketDataMissing(final ValueRequirement valueRequirement) {
      return "marketDataMissing=" + valueRequirement;
    }

    @Override
    protected String visitBlacklistSuppressed(final ValueRequirement valueRequirement, final String function, final ValueSpecification desiredOutput,
        final Map<ValueSpecification, ValueRequirement> requirements) {
      return "blacklistSuppressed=" + valueRequirement + "," + function + "," + desiredOutput + "," + requirements;
    }

    @Override
    protected String visitGetResultsFailed(final ValueRequirement valueRequirement, final String function, final ValueSpecification desiredOutput,
        final Map<ValueSpecification, ValueRequirement> requirements) {
      return "getResultsFailed=" + valueRequirement + "," + function + "," + desiredOutput + "," + requirements;
    }

    @Override
    protected String visitLateResolutionFailure(final ValueRequirement valueRequirement, final String function, final ValueSpecification desiredOutput,
        final Map<ValueSpecification, ValueRequirement> requirements) {
      return "lateResolutionFailure=" + valueRequirement + "," + function + "," + desiredOutput + "," + requirements;
    }

  }

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

  private void assertEquals(final ResolutionFailure failure, final List<String> expected) {
    final ResolutionFailureGatherer<String> gatherer = new ResolutionFailureGatherer<String>(new DebugResolutionFailureVisitor());
    failure.accept(gatherer);
    Assert.assertEquals(gatherer.getResults(), expected);
  }

  private void assertEquals(final ResolutionFailure failure, final String... expected) {
    assertEquals(failure, Arrays.asList(expected));
  }

  public void testRecursiveRequirement() {
    final ValueRequirement requirement = valueRequirement();
    assertEquals(ResolutionFailureImpl.recursiveRequirement(requirement), "recursiveRequirement=" + requirement);
  }

  public void testFunctionApplication() {
    final ValueRequirement req1 = valueRequirement();
    final ValueRequirement req2 = valueRequirement();
    final ValueRequirement req3 = valueRequirement();
    final ParameterizedFunction function = parameterizedFunction();
    final ValueSpecification spec1 = valueSpecification(req1);
    final ValueSpecification spec2 = valueSpecification(req2);
    assertEquals(ResolutionFailureImpl.functionApplication(req1, function, spec1).getRequirementsFailed(), "getRequirementsFailed=" + req1 + ",mock," + spec1);
    assertEquals(ResolutionFailureImpl.functionApplication(req1, function, spec1).requirement(req2, null), "failedFunction=" + req1 + ",mock," + spec1 + ",{},[unsatisfied=" + req2 + "]");
    assertEquals(ResolutionFailureImpl.functionApplication(req1, function, spec1).requirement(req2, ResolutionFailureImpl.recursiveRequirement(req2)), "failedFunction=" + req1 + ",mock," + spec1 +
        ",{},[recursiveRequirement=" + req2 + "]");
    assertEquals(ResolutionFailureImpl.functionApplication(req1, function, spec1).requirements(Collections.singletonMap(spec2, req2)), "successfulFunction=" + req1 + ",mock," + spec1 + ",{" + spec2 +
        "=" + req2 + "}");
    assertEquals(ResolutionFailureImpl.functionApplication(req1, function, spec1).requirements(Collections.singletonMap(spec2, req2)).requirement(req3, null), "failedFunction=" + req1 + ",mock," +
        spec1 + ",{" + spec2 + "=" + req2 + "},[unsatisfied=" + req3 + "]");
    assertEquals(ResolutionFailureImpl.functionApplication(req1, function, spec1).requirements(Collections.singletonMap(spec2, req2)).getAdditionalRequirementsFailed(),
        "getAdditionalRequirementsFailed=" + req1 + ",mock," + spec1 + ",{" + spec2 + "=" + req2 + "}");
    assertEquals(ResolutionFailureImpl.functionApplication(req1, function, spec1).requirements(Collections.singletonMap(spec2, req2)).getResultsFailed(), "getResultsFailed=" + req1 + ",mock," +
        spec1 + ",{" + spec2 + "=" + req2 + "}");
    assertEquals(ResolutionFailureImpl.functionApplication(req1, function, spec1).requirements(Collections.singletonMap(spec2, req2)).suppressed(), "blacklistSuppressed=" + req1 + ",mock," + spec1 +
        ",{" + spec2 + "=" + req2 + "}");
    assertEquals(ResolutionFailureImpl.functionApplication(req1, function, spec1).requirements(Collections.singletonMap(spec2, req2)).lateResolutionFailure(), "lateResolutionFailure=" + req1 +
        ",mock," + spec1 + ",{" + spec2 + "=" + req2 + "}");
    assertEquals(ResolutionFailureImpl.functionApplication(req1, function, spec1).requirements(Collections.singletonMap(spec2, req2)).additionalRequirement(req3, null), "failedFunction=" + req1 +
        ",mock," + spec1 + ",{" + spec2 + "=" + req2 + "},[unsatisfied=" + req3 + "]");
    assertEquals(
        ResolutionFailureImpl.functionApplication(req1, function, spec1).requirements(Collections.singletonMap(spec2, req2))
            .additionalRequirement(req3, ResolutionFailureImpl.recursiveRequirement(req3)), "failedFunction=" + req1 + ",mock," + spec1 + ",{" + spec2 + "=" + req2 + "},[recursiveRequirement=" +
            req3 + "]");
  }

  public void testNoFunctions() {
    final ValueRequirement requirement = valueRequirement();
    assertEquals(ResolutionFailureImpl.noFunctions(requirement), "noFunctions=" + requirement);
  }

  public void testCouldNotResolve() {
    final ValueRequirement requirement = valueRequirement();
    assertEquals(ResolutionFailureImpl.couldNotResolve(requirement), "couldNotResolve=" + requirement);
  }

  public void testUnsatisfied() {
    final ValueRequirement requirement = valueRequirement();
    assertEquals(ResolutionFailureImpl.unsatisfied(requirement), "unsatisfied=" + requirement);
  }

  public void testMarketDataMissing() {
    final ValueRequirement requirement = valueRequirement();
    assertEquals(ResolutionFailureImpl.marketDataMissing(requirement), "marketDataMissing=" + requirement);
  }

}
