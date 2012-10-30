/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import javax.time.Instant;

import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.MapComputationTargetResolver;
import com.opengamma.engine.function.CachingFunctionRepositoryCompiler;
import com.opengamma.engine.function.CompiledFunctionService;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.InMemoryFunctionRepository;
import com.opengamma.engine.function.resolver.DefaultFunctionResolver;
import com.opengamma.engine.function.resolver.FunctionPriority;
import com.opengamma.engine.marketdata.availability.FixedMarketDataAvailabilityProvider;
import com.opengamma.engine.test.MockFunction;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueId;

/**
 * 
 */
public class DepGraphTestHelper {

  private static final String REQUIREMENT_1 = "Req-1";
  private static final String REQUIREMENT_2 = "Req-2";
  private static final String TEST_PROPERTY = "TEST";
  private static final String FUNCTION_PRODUCING_1_AND_2 = "functionProducing1and2";
  private static final String FUNCTION_REQUIRING_2_PRODUCING_1 = "functionRequiring2Producing1";
  private static final String FUNCTION_PRODUCING_2 = "functionProducing2";
  private static final String FUNCTION_PRODUCING_2_BETA = "functionProducing2Beta";

  private final ComputationTarget _target;
  private final ValueRequirement _req1;
  private final ValueSpecification _spec1;
  private final ComputedValue _value1;
  private final ValueRequirement _req1Foo;
  private final ValueSpecification _spec1Foo;
  private final ComputedValue _value1Foo;
  private final ValueRequirement _req1Bar;
  private final ValueSpecification _spec1Bar;
  private final ComputedValue _value1Bar;
  private final ValueRequirement _req1Any;
  private final ValueRequirement _req2;
  private final ValueSpecification _spec2;
  private final ComputedValue _value2;
  private final ValueRequirement _req2Beta;
  private final ValueSpecification _spec2Beta;
  private final ComputedValue _value2Beta;
  private final ValueRequirement _req2Foo;
  private final ValueSpecification _spec2Foo;
  private final ComputedValue _value2Foo;
  private final ValueRequirement _req2Bar;
  private final ValueSpecification _spec2Bar;
  private final ComputedValue _value2Bar;
  private final ValueRequirement _req2Any;
  private final InMemoryFunctionRepository _functionRepo;
  private final FixedMarketDataAvailabilityProvider _liveDataAvailabilityProvider;

  private DependencyGraphBuilder _builder;
  private int _mockId;

  public DepGraphTestHelper() {
    _functionRepo = new InMemoryFunctionRepository();
    UniqueId targetId = UniqueId.of("Scheme", "Value");
    _target = new ComputationTarget(targetId);
    _req1 = new ValueRequirement(REQUIREMENT_1, targetId);
    _spec1 = new ValueSpecification(_req1, MockFunction.UNIQUE_ID);
    _value1 = new ComputedValue(_spec1, 14.2);
    _req1Foo = new ValueRequirement(REQUIREMENT_1, targetId, ValueProperties.with(TEST_PROPERTY, "Foo").get());
    _spec1Foo = new ValueSpecification(_req1Foo, MockFunction.UNIQUE_ID);
    _value1Foo = new ComputedValue(_spec1Foo, 14.3);
    _req1Bar = new ValueRequirement(REQUIREMENT_1, targetId, ValueProperties.with(TEST_PROPERTY, "Bar").get());
    _spec1Bar = new ValueSpecification (_req1Bar, MockFunction.UNIQUE_ID);
    _value1Bar = new ComputedValue (_spec1Bar, 9.0);
    _req1Any = new ValueRequirement(REQUIREMENT_1, targetId, ValueProperties.withAny(TEST_PROPERTY).get());
    _req2 = new ValueRequirement(REQUIREMENT_2, targetId);
    _spec2 = new ValueSpecification(_req2, MockFunction.UNIQUE_ID);
    _value2 = new ComputedValue(_spec2, 15.5);
    _req2Beta = new ValueRequirement(REQUIREMENT_2, targetId, ValueProperties.with(ValuePropertyNames.FUNCTION, FUNCTION_PRODUCING_2_BETA).get());
    _spec2Beta = new ValueSpecification(_req2, FUNCTION_PRODUCING_2_BETA);
    _value2Beta = new ComputedValue(_spec2Beta, 31.0);
    _req2Foo = new ValueRequirement(REQUIREMENT_2, targetId, ValueProperties.with(TEST_PROPERTY, "Foo").get());
    _spec2Foo = new ValueSpecification(_req2Foo, MockFunction.UNIQUE_ID);
    _value2Foo = new ComputedValue(_spec2Foo, 15.6);
    _req2Bar = new ValueRequirement(REQUIREMENT_2, targetId, ValueProperties.with(TEST_PROPERTY, "Bar").get());
    _spec2Bar = new ValueSpecification(_req2Bar, MockFunction.UNIQUE_ID);
    _value2Bar = new ComputedValue(_spec2Bar, 7.8);
    _req2Any = new ValueRequirement(REQUIREMENT_2, targetId, ValueProperties.withAny(TEST_PROPERTY).get());
    _liveDataAvailabilityProvider = new FixedMarketDataAvailabilityProvider();
  }

  public InMemoryFunctionRepository getFunctionRepository() {
    return _functionRepo;
  }

  public MockFunction addFunctionProducing1and2() {
    MockFunction function = new MockFunction(FUNCTION_PRODUCING_1_AND_2, _target);
    function.addResults(Sets.newHashSet(_value1, _value2));
    _functionRepo.addFunction(function);
    return function;
  }

  public MockFunction addFunctionRequiring2Producing1() {
    MockFunction function = new MockFunction(FUNCTION_REQUIRING_2_PRODUCING_1, _target);
    function.addRequirement(_req2);
    function.addResult(_value1);
    _functionRepo.addFunction(function);
    return function;
  }

  public MockFunction addFunctionProducing2() {
    MockFunction function = new MockFunction(FUNCTION_PRODUCING_2, _target);
    function.addResult(_value2);
    _functionRepo.addFunction(function);
    return function;
  }

  public MockFunction addFunctionProducing2Beta() {
    MockFunction function = new MockFunction(FUNCTION_PRODUCING_2_BETA, _target);
    function.addResult(_value2Beta);
    _functionRepo.addFunction(function);
    return function;
  }

  public MockFunction addFunctionProducing(final ComputedValue result) {
    MockFunction function = new MockFunction(Integer.toString(_mockId++), _target);
    function.addResult(result);
    _functionRepo.addFunction(function);
    return function;
  }

  public MockFunction addFunctionRequiringProducing(final ValueRequirement requirement, final ComputedValue result) {
    MockFunction function = new MockFunction(Integer.toString(_mockId++), _target);
    function.addRequirement(requirement);
    function.addResult(result);
    _functionRepo.addFunction(function);
    return function;
  }

  public void make2AvailableFromLiveData() {
    _liveDataAvailabilityProvider.addAvailableRequirement(_req2);
  }

  public void make2MissingFromLiveData() {
    _liveDataAvailabilityProvider.addMissingRequirement(_req2);
  }

  public DependencyGraphBuilder getBuilder(final FunctionPriority prioritizer) {
    if (_builder == null) {
      _builder = new DependencyGraphBuilder();
      _builder.setMarketDataAvailabilityProvider(_liveDataAvailabilityProvider);
      final FunctionCompilationContext context = new FunctionCompilationContext();
      final MapComputationTargetResolver targetResolver = new MapComputationTargetResolver();
      context.setComputationTargetResolver(targetResolver);
      _builder.setCompilationContext(context);
      final CompiledFunctionService compilationService = new CompiledFunctionService(_functionRepo, new CachingFunctionRepositoryCompiler(), context);
      compilationService.initialize();
      final DefaultFunctionResolver resolver;
      if (prioritizer != null) {
        resolver = new DefaultFunctionResolver(compilationService, prioritizer);
      } else {
        resolver = new DefaultFunctionResolver(compilationService);
      }
      _builder.setFunctionResolver(resolver.compile(Instant.now()));
      targetResolver.addTarget(_target);
      _builder.setCalculationConfigurationName("testCalcConf");
    }
    return _builder;
  }

  public ComputationTarget getTarget() {
    return _target;
  }

  public ValueRequirement getRequirement1() {
    return _req1;
  }

  public ValueSpecification getSpec1() {
    return _spec1;
  }

  public ValueRequirement getRequirement1Foo() {
    return _req1Foo;
  }

  public ValueSpecification getSpec1Foo() {
    return _spec1Foo;
  }

  public ComputedValue getValue1Foo() {
    return _value1Foo;
  }

  public ValueRequirement getRequirement1Bar() {
    return _req1Bar;
  }
  
  public ValueSpecification getSpecification1Bar () {
    return _spec1Bar;
  }
  
  public ComputedValue getValue1Bar () {
    return _value1Bar;
  }

  public ValueRequirement getRequirement1Any() {
    return _req1Any;
  }

  public ValueRequirement getRequirement2() {
    return _req2;
  }

  public ValueSpecification getSpec2() {
    return _spec2;
  }

  public ValueRequirement getRequirement2Beta() {
    return _req2Beta;
  }

  public ValueSpecification getSpec2Beta() {
    return _spec2Beta;
  }

  public ValueRequirement getRequirement2Foo() {
    return _req2Foo;
  }

  public ValueSpecification getSpec2Foo() {
    return _spec2Foo;
  }

  public ComputedValue getValue2Foo() {
    return _value2Foo;
  }

  public ValueRequirement getRequirement2Bar() {
    return _req2Bar;
  }

  public ValueSpecification getSpec2Bar() {
    return _spec2Bar;
  }

  public ComputedValue getValue2Bar() {
    return _value2Bar;
  }

  public ValueRequirement getRequirement2Any() {
    return _req2Any;
  }

}
