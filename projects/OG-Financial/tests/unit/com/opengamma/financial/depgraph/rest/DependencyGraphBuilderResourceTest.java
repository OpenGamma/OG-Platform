/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.depgraph.rest;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import javax.time.Duration;
import javax.time.Instant;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.DefaultComputationTargetResolver;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.CachingFunctionRepositoryCompiler;
import com.opengamma.engine.function.CompiledFunctionService;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInvoker;
import com.opengamma.engine.function.InMemoryFunctionRepository;
import com.opengamma.engine.function.resolver.DefaultFunctionResolver;
import com.opengamma.engine.marketdata.MarketDataListener;
import com.opengamma.engine.marketdata.MarketDataProvider;
import com.opengamma.engine.marketdata.MarketDataSnapshot;
import com.opengamma.engine.marketdata.availability.DomainMarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.permission.MarketDataPermissionProvider;
import com.opengamma.engine.marketdata.resolver.SingleMarketDataProviderResolver;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.test.MockSecuritySource;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.ExternalScheme;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.livedata.normalization.MarketDataRequirementNames;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * Tests the diagnostic REST exposure of a dependency graph builder.
 */
@Test
public class DependencyGraphBuilderResourceTest {

  private static final Logger s_logger = LoggerFactory.getLogger(DependencyGraphBuilderResourceTest.class);

  private CompiledFunctionService createFunctionCompilationService() {
    final InMemoryFunctionRepository functions = new InMemoryFunctionRepository();
    functions.addFunction(new AbstractFunction.NonCompiled() {

      @Override
      public ComputationTargetType getTargetType() {
        return ComputationTargetType.PRIMITIVE;
      }

      @Override
      public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
        return true;
      }

      @Override
      public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
        throw new OpenGammaRuntimeException("test");
      }

      @Override
      public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
        return Collections.singleton(new ValueSpecification(ValueRequirementNames.FAIR_VALUE, target.toSpecification(), ValueProperties.with(
            ValuePropertyNames.FUNCTION, "Test").get()));
      }

      @Override
      public FunctionInvoker getFunctionInvoker() {
        fail();
        return null;
      }

    });
    final FunctionCompilationContext context = new FunctionCompilationContext();
    final MockSecuritySource securities = new MockSecuritySource();
    context.setSecuritySource(securities);
    return new CompiledFunctionService(functions, new CachingFunctionRepositoryCompiler(), context);
  }

  private DependencyGraphBuilderResourceContextBean createContextBean() {
    final DependencyGraphBuilderResourceContextBean bean = new DependencyGraphBuilderResourceContextBean();
    final CompiledFunctionService cfs = createFunctionCompilationService ();
    cfs.initialize();
    bean.setComputationTargetResolver(new DefaultComputationTargetResolver(cfs.getFunctionCompilationContext().getSecuritySource()));
    bean.setFunctionCompilationContext(cfs.getFunctionCompilationContext());
    bean.setFunctionResolver (new DefaultFunctionResolver(cfs));
    bean.setMarketDataProviderResolver(new SingleMarketDataProviderResolver(new MarketDataProvider() {

      @Override
      public void addListener(MarketDataListener listener) {
        fail();
      }

      @Override
      public void removeListener(MarketDataListener listener) {
        fail();
      }

      @Override
      public void subscribe(UserPrincipal user, ValueRequirement valueRequirement) {
        fail();
      }

      @Override
      public void subscribe(UserPrincipal user, Set<ValueRequirement> valueRequirements) {
        fail();
      }

      @Override
      public void unsubscribe(UserPrincipal user, ValueRequirement valueRequirement) {
        fail();
      }

      @Override
      public void unsubscribe(UserPrincipal user, Set<ValueRequirement> valueRequirements) {
        fail();
      }

      @Override
      public MarketDataAvailabilityProvider getAvailabilityProvider() {
        return new DomainMarketDataAvailabilityProvider(cfs.getFunctionCompilationContext().getSecuritySource(), Arrays.asList(ExternalScheme.of("Foo")), Arrays
            .asList(MarketDataRequirementNames.MARKET_VALUE));
      }

      @Override
      public MarketDataPermissionProvider getPermissionProvider() {
        fail();
        return null;
      }

      @Override
      public boolean isCompatible(MarketDataSpecification marketDataSpec) {
        fail();
        return false;
      }

      @Override
      public MarketDataSnapshot snapshot(MarketDataSpecification marketDataSpec) {
        fail();
        return null;
      }

      @Override
      public Duration getRealTimeDuration(Instant fromInstant, Instant toInstant) {
        fail();
        return null;
      }

    }));
    return bean;
  }

  private DependencyGraphBuilderResource createResource() {
    return new DependencyGraphBuilderResource(createContextBean(), OpenGammaFudgeContext.getInstance());
  }

  public void testSetValuationTime() {
    final DependencyGraphBuilderResource resource = createResource();
    final Instant i1 = resource.getValuationTime();
    final DependencyGraphBuilderResource prime = resource.setValuationTime("2007-12-03T10:15:30+01:00[Europe/Paris]");
    final Instant i2 = prime.getValuationTime();
    assertEquals(i1, resource.getValuationTime()); // original unchanged
    assertFalse(i1.equals(i2));
  }

  public void testSetCalculationConfigurationName() {
    final DependencyGraphBuilderResource resource = createResource();
    final String c1 = resource.getCalculationConfigurationName();
    final DependencyGraphBuilderResource prime = resource.setCalculationConfigurationName("Foo");
    final String c2 = prime.getCalculationConfigurationName();
    assertEquals(c1, resource.getCalculationConfigurationName()); // original unchanged
    assertFalse(c1.equals(c2));
  }

  public void testSetDefaultProperties() {
    final DependencyGraphBuilderResource resource = createResource();
    final ValueProperties p1 = resource.getDefaultProperties();
    final DependencyGraphBuilderResource prime = resource.setDefaultProperties("A=[foo,bar],B=*");
    final ValueProperties p2 = prime.getDefaultProperties();
    assertEquals(p1, resource.getDefaultProperties()); // original unchanged
    assertFalse(p1.equals(p2));
  }

  public void testAddValue() {
    final DependencyGraphBuilderResource resource = createResource();
    final Collection<ValueRequirement> r1 = resource.getRequirements();
    final DependencyGraphBuilderResource prime = resource.addValueRequirement("Foo", "PRIMITIVE", "Test~1");
    final Collection<ValueRequirement> r2 = prime.getRequirements();
    final DependencyGraphBuilderResource prime2 = prime.addValueRequirement("Bar", "PRIMITIVE", "Test~2");
    final Collection<ValueRequirement> r3 = prime2.getRequirements();
    assertEquals(r1, resource.getRequirements()); // original unchanged
    assertEquals(r2, prime.getRequirements()); // unchanged
    assertEquals(r1.size(), 0);
    assertEquals(r2.size(), 1);
    assertEquals(r3.size(), 2);
  }

  public void testBuild_ok() {
    final DependencyGraphBuilderResource resource = createResource();
    final FudgeMsgEnvelope env = resource.addValueRequirement(MarketDataRequirementNames.MARKET_VALUE, "PRIMITIVE", "Foo~1")
        .addValueRequirement(MarketDataRequirementNames.MARKET_VALUE, "PRIMITIVE", "Foo~2").build();
    final FudgeMsg msg = env.getMessage();
    s_logger.debug("testBuild_ok = {}", msg);
    assertTrue(msg.hasField("dependencyGraph"));
    assertFalse(msg.hasField("exception"));
    assertFalse(msg.hasField("failure"));
  }

  public void testBuild_exceptions() {
    final DependencyGraphBuilderResource resource = createResource();
    final FudgeMsgEnvelope env = resource.addValueRequirement(MarketDataRequirementNames.MARKET_VALUE, "PRIMITIVE", "Foo~1")
        .addValueRequirement(ValueRequirementNames.FAIR_VALUE, "PRIMITIVE", "Foo~Bar").build();
    final FudgeMsg msg = env.getMessage();
    s_logger.debug("testBuild_exceptions = {}", msg);
    assertTrue(msg.hasField("dependencyGraph"));
    assertEquals(msg.getAllByName("exception").size(), 2); // one from the exception, and one from not resolving
    assertEquals(msg.getAllByName("failure").size(), 1);
  }

  public void testBuild_failures() {
    final DependencyGraphBuilderResource resource = createResource();
    final FudgeMsgEnvelope env = resource.addValueRequirement(MarketDataRequirementNames.MARKET_VALUE, "PRIMITIVE", "Bar~1")
        .addValueRequirement(ValueRequirementNames.PRESENT_VALUE, "PRIMITIVE", "Bar~2").build();
    final FudgeMsg msg = env.getMessage();
    s_logger.debug("testBuild_failures = {}", msg);
    assertTrue(msg.hasField("dependencyGraph"));
    assertEquals(msg.getAllByName("exception").size(), 2);
    assertEquals(msg.getAllByName("failure").size(), 2);
  }

}
