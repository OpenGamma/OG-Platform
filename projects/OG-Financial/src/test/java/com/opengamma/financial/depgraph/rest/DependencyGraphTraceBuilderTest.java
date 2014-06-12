/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.depgraph.rest;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import static org.testng.AssertJUnit.assertNotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import org.testng.annotations.Test;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.DefaultComputationTargetResolver;
import com.opengamma.engine.InMemorySecuritySource;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.CachingFunctionRepositoryCompiler;
import com.opengamma.engine.function.CompiledFunctionService;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInvoker;
import com.opengamma.engine.function.InMemoryFunctionRepository;
import com.opengamma.engine.function.resolver.DefaultFunctionResolver;
import com.opengamma.engine.marketdata.MarketDataListener;
import com.opengamma.engine.marketdata.MarketDataPermissionProvider;
import com.opengamma.engine.marketdata.MarketDataProvider;
import com.opengamma.engine.marketdata.MarketDataSnapshot;
import com.opengamma.engine.marketdata.availability.DefaultMarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.availability.DomainMarketDataAvailabilityFilter;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.resolver.SingleMarketDataProviderResolver;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.target.ComputationTargetRequirement;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.test.TestLifecycle;

/**
 * Tests the diagnostic REST exposure of a dependency graph builder.
 */
@Test(groups = TestGroup.UNIT)
public class DependencyGraphTraceBuilderTest {

  private CompiledFunctionService createFunctionCompilationService() {
    final InMemoryFunctionRepository functions = new InMemoryFunctionRepository();
    functions.addFunction(new AbstractFunction.NonCompiled() {

      @Override
      public ComputationTargetType getTargetType() {
        return ComputationTargetType.PRIMITIVE;
      }

      @Override
      public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
        return true;
      }

      @Override
      public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
        throw new OpenGammaRuntimeException("test");
      }

      @Override
      public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
        return Collections.singleton(new ValueSpecification(ValueRequirementNames.FAIR_VALUE, target.toSpecification(), ValueProperties.with(ValuePropertyNames.FUNCTION, "Test").get()));
      }

      @Override
      public FunctionInvoker getFunctionInvoker() {
        fail();
        return null;
      }

    });
    final FunctionCompilationContext context = new FunctionCompilationContext();
    final InMemorySecuritySource securities = new InMemorySecuritySource();
    context.setSecuritySource(securities);
    context.setRawComputationTargetResolver(new DefaultComputationTargetResolver(securities));
    context.setComputationTargetResolver(context.getRawComputationTargetResolver().atVersionCorrection(VersionCorrection.LATEST));
    return new CompiledFunctionService(functions, new CachingFunctionRepositoryCompiler(), context);
  }

  private DependencyGraphBuilderResourceContextBean createContextBean() {
    final DependencyGraphBuilderResourceContextBean bean = new DependencyGraphBuilderResourceContextBean();
    final CompiledFunctionService cfs = createFunctionCompilationService();
    TestLifecycle.register(cfs);
    cfs.initialize();
    bean.setFunctionCompilationContext(cfs.getFunctionCompilationContext());
    bean.setFunctionResolver(new DefaultFunctionResolver(cfs));
    bean.setMarketDataProviderResolver(new SingleMarketDataProviderResolver(new MarketDataProvider() {

      @Override
      public void addListener(final MarketDataListener listener) {
      }

      @Override
      public void removeListener(final MarketDataListener listener) {
        fail();
      }

      @Override
      public void subscribe(final ValueSpecification valueSpecification) {
        fail();
      }

      @Override
      public void subscribe(final Set<ValueSpecification> valueSpecifications) {
        fail();
      }

      @Override
      public void unsubscribe(final ValueSpecification valueSpecification) {
        fail();
      }

      @Override
      public void unsubscribe(final Set<ValueSpecification> valueSpecifications) {
        fail();
      }

      @Override
      public MarketDataAvailabilityProvider getAvailabilityProvider(final MarketDataSpecification marketDataSpec) {
        return new DomainMarketDataAvailabilityFilter(Arrays.asList(ExternalScheme.of("Foo")), Arrays.asList(MarketDataRequirementNames.MARKET_VALUE))
            .withProvider(new DefaultMarketDataAvailabilityProvider());
      }

      @Override
      public MarketDataPermissionProvider getPermissionProvider() {
        return null;
      }

      @Override
      public boolean isCompatible(final MarketDataSpecification marketDataSpec) {
        fail();
        return false;
      }

      @Override
      public MarketDataSnapshot snapshot(final MarketDataSpecification marketDataSpec) {
        fail();
        return null;
      }

      @Override
      public Duration getRealTimeDuration(final Instant fromInstant, final Instant toInstant) {
        fail();
        return null;
      }

    }));
    return bean;
  }

  private DependencyGraphTraceBuilder createBuilder() {
    return new DependencyGraphTraceBuilder(createContextBean());
  }

  public void testBuild_ok() {
    TestLifecycle.begin();
    try {
      DependencyGraphTraceBuilder builder = createBuilder();
      ComputationTargetRequirement ct1 = new ComputationTargetRequirement(ComputationTargetType.parse("PRIMITIVE"), ExternalId.parse("Foo~1"));
      ValueRequirement req1 = parseValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ct1);
      ComputationTargetRequirement ct2 = new ComputationTargetRequirement(ComputationTargetType.parse("PRIMITIVE"), ExternalId.parse("Foo~2"));
      ValueRequirement req2 = parseValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ct2);
      DependencyGraphTraceBuilderProperties properties = new DependencyGraphTraceBuilderProperties();
      properties = properties.addRequirement(req1).addRequirement(req2);
      DependencyGraphBuildTrace obj = builder.build(properties);
      assertNotNull(obj.getDependencyGraph());
      assertTrue(obj.getExceptionsWithCounts().isEmpty());
      assertTrue(obj.getFailures().isEmpty());
    } finally {
      TestLifecycle.end();
    }
  }

  public void testBuild_exceptions() {
    TestLifecycle.begin();
    try {
      DependencyGraphTraceBuilder builder = createBuilder();
      ComputationTargetRequirement ct1 = new ComputationTargetRequirement(ComputationTargetType.parse("PRIMITIVE"), ExternalId.parse("Foo~1"));
      ValueRequirement req1 = parseValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ct1);
      ComputationTargetSpecification ct2 = new ComputationTargetSpecification(ComputationTargetType.parse("PRIMITIVE"), UniqueId.parse("Foo~Bar"));
      ValueRequirement req2 = parseValueRequirement(ValueRequirementNames.FAIR_VALUE, ct2);
      DependencyGraphTraceBuilderProperties properties = new DependencyGraphTraceBuilderProperties();
      properties = properties.addRequirement(req1).addRequirement(req2);
      DependencyGraphBuildTrace obj = builder.build(properties);
      assertNotNull(obj.getDependencyGraph());
      assertEquals(2, obj.getExceptionsWithCounts().size());
      assertEquals(1, obj.getFailures().size());
    } finally {
      TestLifecycle.end();
    }
  }

  public void testBuild_failures() {
    TestLifecycle.begin();
    try {
      DependencyGraphTraceBuilder builder = createBuilder();
      ComputationTargetRequirement ct1 = new ComputationTargetRequirement(ComputationTargetType.parse("PRIMITIVE"), ExternalId.parse("Bar~1"));
      ValueRequirement req1 = parseValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ct1);
      ComputationTargetSpecification ct2 = new ComputationTargetSpecification(ComputationTargetType.parse("PRIMITIVE"), UniqueId.parse("Bar~2"));
      ValueRequirement req2 = parseValueRequirement(ValueRequirementNames.PRESENT_VALUE, ct2);
      DependencyGraphTraceBuilderProperties properties = new DependencyGraphTraceBuilderProperties();
      properties = properties.addRequirement(req1).addRequirement(req2);
      DependencyGraphBuildTrace obj = builder.build(properties);
      assertNotNull(obj.getDependencyGraph());
      assertEquals(2, obj.getExceptionsWithCounts().size());
      assertEquals(2, obj.getFailures().size());
    } finally {
      TestLifecycle.end();
    }
  }

  private ValueRequirement parseValueRequirement(final String valueName, final ComputationTargetReference target) {
    final String name;
    final ValueProperties constraints;
    final int i = valueName.indexOf('{');
    if ((i > 0) && (valueName.charAt(valueName.length() - 1) == '}')) {
      name = valueName.substring(0, i);
      constraints = ValueProperties.parse(valueName.substring(i));
    } else {
      name = valueName;
      constraints = ValueProperties.none();
    }
    return new ValueRequirement(name, target, constraints);
  }

}
