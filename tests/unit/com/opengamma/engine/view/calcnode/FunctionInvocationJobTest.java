/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collections;

import org.junit.Test;

import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.InMemoryFunctionRepository;
import com.opengamma.engine.function.MockFunction;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.cache.MapViewComputationCache;

/**
 * 
 */
public class FunctionInvocationJobTest {
  
  /**
   * Just test a basic no-op function invocation. Because there are no inputs
   * or outputs, this is really just here to make sure that the class itself
   * can work in the absence of doing any real work.
   */
  @Test
  public void mockFunctionInvocationNoInputsNoOutputs() {
    ComputationTarget target = new ComputationTarget(ComputationTargetType.PRIMITIVE, "USD");
    MockFunction fn = new MockFunction(target);
    InMemoryFunctionRepository functionRepo = new InMemoryFunctionRepository();
    functionRepo.addFunction(fn, fn);
    
    long iterationTimestamp = System.currentTimeMillis();
    CalculationJobSpecification jobSpec = new CalculationJobSpecification("view", "config", iterationTimestamp, 1L);
    MapViewComputationCache cache = new MapViewComputationCache();
    FunctionExecutionContext execContext = new FunctionExecutionContext();
    ViewProcessorQuery viewProcessorQuery = new ViewProcessorQuery(null, null);
    
    FunctionInvocationJob job = new FunctionInvocationJob(jobSpec, fn.getUniqueIdentifier(), Collections.<ValueSpecification>emptySet(), cache, functionRepo, execContext, viewProcessorQuery, target, Collections.<ValueRequirement>emptySet());
    
    job.run();
    
    assertEquals(0, cache.size());
  }
  
  @Test
  public void mockFunctionInvocationNoInputsOneOutput() {
    ComputationTarget target = new ComputationTarget(ComputationTargetType.PRIMITIVE, "USD");
    ValueRequirement resultValueReq = new ValueRequirement("FOO", target.toSpecification());
    ValueSpecification resultValueSpec = new ValueSpecification(resultValueReq);
    ComputedValue resultValue = new ComputedValue(resultValueSpec, "Bar");
    MockFunction fn = new MockFunction(
        target,
        Collections.<ValueRequirement>emptySet(),
        Sets.newHashSet(resultValue));
    
    InMemoryFunctionRepository functionRepo = new InMemoryFunctionRepository();
    functionRepo.addFunction(fn, fn);
    
    long iterationTimestamp = System.currentTimeMillis();
    CalculationJobSpecification jobSpec = new CalculationJobSpecification("view", "config", iterationTimestamp, 1L);
    MapViewComputationCache cache = new MapViewComputationCache();
    FunctionExecutionContext execContext = new FunctionExecutionContext();
    ViewProcessorQuery viewProcessorQuery = new ViewProcessorQuery(null, null);
    
    FunctionInvocationJob job = new FunctionInvocationJob(
        jobSpec,
        fn.getUniqueIdentifier(),
        Collections.<ValueSpecification>emptySet(),
        cache,
        functionRepo,
        execContext,
        viewProcessorQuery,
        target,
        Sets.newHashSet(resultValueReq));
    
    job.run();
    
    Object valueFromCache = cache.getValue(resultValueSpec);
    assertNotNull(resultValue);
    assertEquals(resultValue.getValue(), valueFromCache);
  }

  /**
   * Exactly the same as {@link #mockFunctionInvocationNoInputsOneOutput()} except
   * that we say we don't want the requirement, so we shouldn't have a value in
   * the cache.
   */
  @Test
  public void mockFunctionInvocationNoInputsOneOutputEliminated() {
    ComputationTarget target = new ComputationTarget(ComputationTargetType.PRIMITIVE, "USD");
    ValueRequirement resultValueReq = new ValueRequirement("FOO", target.toSpecification());
    ValueSpecification resultValueSpec = new ValueSpecification(resultValueReq);
    ComputedValue resultValue = new ComputedValue(resultValueSpec, "Bar");
    MockFunction fn = new MockFunction(
        target,
        Collections.<ValueRequirement>emptySet(),
        Sets.newHashSet(resultValue));
    
    InMemoryFunctionRepository functionRepo = new InMemoryFunctionRepository();
    functionRepo.addFunction(fn, fn);
    
    long iterationTimestamp = System.currentTimeMillis();
    CalculationJobSpecification jobSpec = new CalculationJobSpecification("view", "config", iterationTimestamp, 1L);
    MapViewComputationCache cache = new MapViewComputationCache();
    FunctionExecutionContext execContext = new FunctionExecutionContext();
    ViewProcessorQuery viewProcessorQuery = new ViewProcessorQuery(null, null);
    
    FunctionInvocationJob job = new FunctionInvocationJob(jobSpec, fn.getUniqueIdentifier(), Collections.<ValueSpecification>emptySet(), cache, functionRepo, execContext, viewProcessorQuery, target, Collections.<ValueRequirement>emptySet());
    
    job.run();
    
    assertEquals(0, cache.size());
  }

}
