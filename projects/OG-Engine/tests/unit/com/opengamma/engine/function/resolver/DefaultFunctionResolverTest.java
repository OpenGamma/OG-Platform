/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.resolver;

import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.ParameterizedFunction;
import com.opengamma.engine.test.PrimitiveTestFunction;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
@Test
public class DefaultFunctionResolverTest {

  private ComputationTarget _target;
  private PrimitiveTestFunction _f1;
  private PrimitiveTestFunction _f2;
  private ParameterizedFunction _parameterizedF1;
  private ParameterizedFunction _parameterizedF2;
  private DefaultCompiledFunctionResolver _resolver;

  @BeforeMethod
  public void setUp() {
    _target = new ComputationTarget(UniqueIdentifier.of("scheme", "test_target"));

    _f1 = new PrimitiveTestFunction("req1");
    _f1.setUniqueId("1");
    _f2 = new PrimitiveTestFunction("req1");
    _f2.setUniqueId("2");

    _parameterizedF1 = new ParameterizedFunction(_f1, _f1.getDefaultParameters());
    _parameterizedF2 = new ParameterizedFunction(_f2, _f2.getDefaultParameters());

    _resolver = new DefaultCompiledFunctionResolver(new FunctionCompilationContext());
  }

  public void globalRuleSelection() {
    _resolver.addRule(new ResolutionRule(_parameterizedF1, ApplyToAllTargets.INSTANCE, 100));
    _resolver.addRule(new ResolutionRule(_parameterizedF2, ApplyToAllTargets.INSTANCE, 200));

    Pair<ParameterizedFunction, ValueSpecification> result = _resolver.resolveFunction(new ValueRequirement("req1", _target.toSpecification()), new DependencyNode(_target)).next();

    assertEquals(_parameterizedF2, result.getFirst());
  }

  public void nonGlobalRuleSelection() {
    _resolver.addRule(new ResolutionRule(_parameterizedF1, ApplyToAllTargets.INSTANCE, 100));
    _resolver.addRule(new ResolutionRule(_parameterizedF2, new ApplyToSubtree(_target.toSpecification()), 200));

    Pair<ParameterizedFunction, ValueSpecification> result = _resolver.resolveFunction(new ValueRequirement("req1", _target.toSpecification()), new DependencyNode(_target)).next();

    assertEquals(_parameterizedF2, result.getFirst());

    ComputationTarget anotherTarget = new ComputationTarget(UniqueIdentifier.of("scheme", "target2"));
    result = _resolver.resolveFunction(new ValueRequirement("req1", anotherTarget.toSpecification()), new DependencyNode(anotherTarget)).next();

    assertEquals(_parameterizedF1, result.getFirst());
  }

}
