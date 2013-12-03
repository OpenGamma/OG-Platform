/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.MapComputationTargetResolver;
import com.opengamma.engine.function.CachingFunctionRepositoryCompiler;
import com.opengamma.engine.function.CompiledFunctionService;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.InMemoryFunctionRepository;
import com.opengamma.engine.function.resolver.DefaultFunctionResolver;
import com.opengamma.engine.function.resolver.FunctionPriority;
import com.opengamma.engine.marketdata.availability.FixedMarketDataAvailabilityProvider;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.test.MockFunction;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.test.TestLifecycle;

/**
 *
 */
@Test(groups = TestGroup.UNIT)
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

  private int _mockId;

  public DepGraphTestHelper() {
    _functionRepo = new InMemoryFunctionRepository();
    final UniqueId targetId = UniqueId.of("Scheme", "Value");
    _target = new ComputationTarget(ComputationTargetType.PRIMITIVE, targetId);
    final ComputationTargetSpecification targetSpec = _target.toSpecification();
    _req1 = new ValueRequirement(REQUIREMENT_1, targetSpec);
    _spec1 = new ValueSpecification(REQUIREMENT_1, targetSpec, _req1.getConstraints().copy().with(ValuePropertyNames.FUNCTION, MockFunction.UNIQUE_ID).get());
    _value1 = new ComputedValue(_spec1, 14.2);
    _req1Foo = new ValueRequirement(REQUIREMENT_1, targetSpec, ValueProperties.with(TEST_PROPERTY, "Foo").get());
    _spec1Foo = new ValueSpecification(REQUIREMENT_1, targetSpec, _req1Foo.getConstraints().copy().with(ValuePropertyNames.FUNCTION, MockFunction.UNIQUE_ID).get());
    _value1Foo = new ComputedValue(_spec1Foo, 14.3);
    _req1Bar = new ValueRequirement(REQUIREMENT_1, targetSpec, ValueProperties.with(TEST_PROPERTY, "Bar").get());
    _spec1Bar = new ValueSpecification(REQUIREMENT_1, targetSpec, _req1Bar.getConstraints().copy().with(ValuePropertyNames.FUNCTION, MockFunction.UNIQUE_ID).get());
    _value1Bar = new ComputedValue(_spec1Bar, 9.0);
    _req1Any = new ValueRequirement(REQUIREMENT_1, targetSpec, ValueProperties.withAny(TEST_PROPERTY).get());
    _req2 = new ValueRequirement(REQUIREMENT_2, targetSpec);
    _spec2 = new ValueSpecification(REQUIREMENT_2, targetSpec, _req2.getConstraints().copy().with(ValuePropertyNames.FUNCTION, MockFunction.UNIQUE_ID).get());
    _value2 = new ComputedValue(_spec2, 15.5);
    _req2Beta = new ValueRequirement(REQUIREMENT_2, targetSpec, ValueProperties.with(ValuePropertyNames.FUNCTION, FUNCTION_PRODUCING_2_BETA).get());
    _spec2Beta = new ValueSpecification(REQUIREMENT_2, targetSpec, _req2Beta.getConstraints().copy().with(ValuePropertyNames.FUNCTION, FUNCTION_PRODUCING_2_BETA).get());
    _value2Beta = new ComputedValue(_spec2Beta, 31.0);
    _req2Foo = new ValueRequirement(REQUIREMENT_2, targetSpec, ValueProperties.with(TEST_PROPERTY, "Foo").get());
    _spec2Foo = new ValueSpecification(REQUIREMENT_2, targetSpec, _req2Foo.getConstraints().copy().with(ValuePropertyNames.FUNCTION, MockFunction.UNIQUE_ID).get());
    _value2Foo = new ComputedValue(_spec2Foo, 15.6);
    _req2Bar = new ValueRequirement(REQUIREMENT_2, targetSpec, ValueProperties.with(TEST_PROPERTY, "Bar").get());
    _spec2Bar = new ValueSpecification(REQUIREMENT_2, targetSpec, _req2Bar.getConstraints().copy().with(ValuePropertyNames.FUNCTION, MockFunction.UNIQUE_ID).get());
    _value2Bar = new ComputedValue(_spec2Bar, 7.8);
    _req2Any = new ValueRequirement(REQUIREMENT_2, targetSpec, ValueProperties.withAny(TEST_PROPERTY).get());
    _liveDataAvailabilityProvider = new FixedMarketDataAvailabilityProvider();
  }

  public InMemoryFunctionRepository getFunctionRepository() {
    return _functionRepo;
  }

  public MockFunction addFunctionProducing1and2() {
    final MockFunction function = new MockFunction(FUNCTION_PRODUCING_1_AND_2, _target);
    function.addResults(Sets.newHashSet(_value1, _value2));
    _functionRepo.addFunction(function);
    return function;
  }

  public MockFunction addFunctionRequiring2Producing1() {
    final MockFunction function = new MockFunction(FUNCTION_REQUIRING_2_PRODUCING_1, _target);
    function.addRequirement(_req2);
    function.addResult(_value1);
    _functionRepo.addFunction(function);
    return function;
  }

  public MockFunction addFunctionProducing2() {
    final MockFunction function = new MockFunction(FUNCTION_PRODUCING_2, _target);
    function.addResult(_value2);
    _functionRepo.addFunction(function);
    return function;
  }

  public MockFunction addFunctionProducing2Beta() {
    final MockFunction function = new MockFunction(FUNCTION_PRODUCING_2_BETA, _target);
    function.addResult(_value2Beta);
    _functionRepo.addFunction(function);
    return function;
  }

  public MockFunction addFunctionProducing(final ComputedValue result) {
    final MockFunction function = new MockFunction(Integer.toString(_mockId++), _target);
    function.addResult(result);
    _functionRepo.addFunction(function);
    return function;
  }

  public MockFunction addFunctionRequiringProducing(final ValueRequirement requirement, final ComputedValue result) {
    final MockFunction function = new MockFunction(Integer.toString(_mockId++), _target);
    function.addRequirement(requirement);
    function.addResult(result);
    _functionRepo.addFunction(function);
    return function;
  }

  public void make2AvailableFromLiveData() {
    _liveDataAvailabilityProvider.addAvailableData(new ValueSpecification(_req2.getValueName(), _req2.getTargetReference().getSpecification(), ValueProperties.with(
        ValuePropertyNames.FUNCTION, "LiveData").get()));
  }

  public DependencyGraphBuilder createBuilder(final FunctionPriority prioritizer) {
    final Instant now = Instant.now();
    final DependencyGraphBuilder builder = new DependencyGraphBuilder();
    builder.setMarketDataAvailabilityProvider(_liveDataAvailabilityProvider);
    final FunctionCompilationContext context = new FunctionCompilationContext();
    final ComputationTargetResolver targetResolver = new MapComputationTargetResolver();
    context.setRawComputationTargetResolver(targetResolver);
    context.setComputationTargetResolver(targetResolver.atVersionCorrection(VersionCorrection.of(now, now)));
    builder.setCompilationContext(context);
    final CompiledFunctionService compilationService = new CompiledFunctionService(_functionRepo, new CachingFunctionRepositoryCompiler(), context);
    TestLifecycle.register(compilationService);
    compilationService.initialize();
    final DefaultFunctionResolver resolver;
    if (prioritizer != null) {
      resolver = new DefaultFunctionResolver(compilationService, prioritizer);
    } else {
      resolver = new DefaultFunctionResolver(compilationService);
    }
    builder.setFunctionResolver(resolver.compile(now));
    builder.setCalculationConfigurationName("testCalcConf");
    return builder;
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

  public ValueSpecification getSpecification1Bar() {
    return _spec1Bar;
  }

  public ComputedValue getValue1Bar() {
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
