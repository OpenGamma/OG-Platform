/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.test;

import java.util.Collections;
import java.util.Set;

import javax.time.Instant;

import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.InMemoryFunctionRepository;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.cache.CacheSelectHint;
import com.opengamma.engine.view.calcnode.CalculationJob;
import com.opengamma.engine.view.calcnode.CalculationJobItem;
import com.opengamma.engine.view.calcnode.CalculationJobSpecification;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class CalculationNodeUtils {

  /**
   * Default calculation configuration name
   */
  public static final String CALC_CONF_NAME = "Default";

  public static TestCalculationNode getTestCalcNode(MockFunction mockFunction) {
    TestCalculationNode calcNode = new TestCalculationNode();
    InMemoryFunctionRepository functionRepo = (InMemoryFunctionRepository) calcNode.getFunctionCompilationService().getFunctionRepository();
    functionRepo.addFunction(mockFunction);
    calcNode.getFunctionCompilationService().initialize();
    return calcNode;
  }

  public static MockFunction getMockFunction() {
    ComputationTarget target = new ComputationTarget(ComputationTargetType.CURRENCY, Currency.USD);
    return getMockFunction(target, "Nothing we care about");
  }

  public static MockFunction getMockFunction(ComputationTarget target, Object output) {
    return MockFunction.getMockFunction(MockFunction.UNIQUE_ID, target, output, new ValueRequirement("INPUT", target.toSpecification()));
  }

  public static Set<ValueSpecification> getMockFunctionInputs(final MockFunction function) {
    final Set<ValueRequirement> requirements = function.getRequirements();
    final Set<ValueSpecification> inputs = Sets.newHashSetWithExpectedSize(requirements.size());
    for (ValueRequirement requirement : requirements) {
      inputs.add(new ValueSpecification(requirement.getValueName(), requirement.getTargetReference().getSpecification(), requirement.getConstraints().copy().with(ValuePropertyNames.FUNCTION, "mock")
          .get()));
    }
    return inputs;
  }

  public static CalculationJob getCalculationJob(MockFunction function) {
    final Instant valuationTime = Instant.now();
    final CalculationJobSpecification jobSpec = new CalculationJobSpecification(UniqueId.of("Test", "ViewProcess"), CALC_CONF_NAME, valuationTime, 1L);
    final CalculationJobItem calculationJobItem = new CalculationJobItem(function.getUniqueId(), function.getDefaultParameters(), function.getTarget().toSpecification(),
        getMockFunctionInputs(function),
        function.getResultSpecs());
    final CalculationJob calcJob = new CalculationJob(jobSpec, 0L, VersionCorrection.LATEST, null, Collections.singletonList(calculationJobItem), CacheSelectHint.allShared());
    return calcJob;
  }

}
