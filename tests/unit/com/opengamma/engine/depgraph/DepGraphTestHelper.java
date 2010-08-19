/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.MapComputationTargetResolver;
import com.opengamma.engine.function.DefaultFunctionResolver;
import com.opengamma.engine.function.InMemoryFunctionRepository;
import com.opengamma.engine.function.MockFunction;
import com.opengamma.engine.livedata.FixedLiveDataAvailabilityProvider;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueIdentifier;

/**
 * 
 */
public class DepGraphTestHelper {
 
  private final ComputationTarget _target;
  private final ValueSpecification _spec1;
  private final ComputedValue _value1;
  private final ValueSpecification _spec2;
  private final ComputedValue _value2;
  private final InMemoryFunctionRepository _functionRepo;
  private final FixedLiveDataAvailabilityProvider _liveDataAvailabilityProvider;
  
  private final DependencyGraphBuilder _builder;
  
  public DepGraphTestHelper() {
    _functionRepo = new InMemoryFunctionRepository();
    UniqueIdentifier targetId = UniqueIdentifier.of("Scheme", "Value");
    _target = new ComputationTarget(targetId);
    ValueRequirement req1 = new ValueRequirement("Req-1", targetId);
    _spec1 = new ValueSpecification(req1, MockFunction.UNIQUE_ID);
    _value1 = new ComputedValue(_spec1, 14.2);
    
    ValueRequirement req2 = new ValueRequirement("Req-2", targetId);
    _spec2 = new ValueSpecification(req2, MockFunction.UNIQUE_ID);
    _value2 = new ComputedValue(_spec2, 15.5);

    _builder = new DependencyGraphBuilder();
    _liveDataAvailabilityProvider = new FixedLiveDataAvailabilityProvider();
    _builder.setLiveDataAvailabilityProvider(_liveDataAvailabilityProvider);
    _builder.setFunctionResolver(new DefaultFunctionResolver(_functionRepo));
    MapComputationTargetResolver targetResolver = new MapComputationTargetResolver();
    targetResolver.addTarget(_target);
    _builder.setTargetResolver(targetResolver);
    _builder.setCalculationConfigurationName("testCalcConf");
  }
  
  public MockFunction addFunctionProducing1and2() {
    MockFunction function = new MockFunction(_target);
    function.addResults(Sets.newHashSet(_value1, _value2));
    _functionRepo.addFunction(function, function);
    return function;
  }
  
  public MockFunction addFunctionRequiring2Producing1() {
    MockFunction function = new MockFunction(_target);
    function.addRequirement(_spec2);
    function.addResult(_value1);
    _functionRepo.addFunction(function, function);
    return function;
  }
  
  public MockFunction addFunctionProducing2() {
    MockFunction function = new MockFunction(_target);
    function.addResult(_value2);
    _functionRepo.addFunction(function, function);
    return function;
  }
  
  public void make2AvailableFromLiveData() {
    _liveDataAvailabilityProvider.addRequirement(_spec2.getRequirementSpecification());
  }

  public DependencyGraphBuilder getBuilder() {
    return _builder;
  }

  public ComputationTarget getTarget() {
    return _target;
  }

  public ValueSpecification getSpec1() {
    return _spec1;
  }

  public ValueSpecification getSpec2() {
    return _spec2;
  }
  
  

}
