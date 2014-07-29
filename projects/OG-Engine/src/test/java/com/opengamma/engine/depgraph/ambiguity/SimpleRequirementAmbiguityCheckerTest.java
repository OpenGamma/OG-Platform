/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph.ambiguity;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.position.impl.MockPositionSource;
import com.opengamma.core.position.impl.SimplePortfolio;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.DefaultComputationTargetResolver;
import com.opengamma.engine.InMemorySecuritySource;
import com.opengamma.engine.function.CachingFunctionRepositoryCompiler;
import com.opengamma.engine.function.CompiledFunctionService;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionRepositoryCompiler;
import com.opengamma.engine.function.InMemoryFunctionRepository;
import com.opengamma.engine.function.resolver.DefaultFunctionResolver;
import com.opengamma.engine.function.resolver.FunctionResolver;
import com.opengamma.engine.marketdata.availability.DefaultMarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.availability.OptimisticMarketDataAvailabilityFilter;
import com.opengamma.engine.test.MockFunction;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.test.TestLifecycle;

/**
 * Tests the {@link SimpleRequirementAmbiguityChecker} class.
 */
@Test(groups = TestGroup.UNIT)
public class SimpleRequirementAmbiguityCheckerTest {

  private MockFunction mockFunctionX() {
    final MockFunction function = new MockFunction("X", ComputationTarget.NULL);
    function.addRequirement(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetSpecification.NULL));
    function.addResult(new ValueSpecification("X", ComputationTargetSpecification.NULL, ValueProperties.with(ValuePropertyNames.FUNCTION, "X").get()), 42d);
    return function;
  }

  private MockFunction mockFunctionA1() {
    final MockFunction function = new MockFunction("A1", ComputationTarget.NULL);
    function.addRequirement(new ValueRequirement("X", ComputationTargetSpecification.NULL));
    function.addResult(new ValueSpecification("A", ComputationTargetSpecification.NULL, ValueProperties.with(ValuePropertyNames.FUNCTION, "A1").get()), 42d);
    return function;
  }

  private MockFunction mockFunctionA2() {
    final MockFunction function = new MockFunction("A2", ComputationTarget.NULL);
    function.addRequirement(new ValueRequirement("X", ComputationTargetSpecification.NULL));
    function.addResult(new ValueSpecification("A", ComputationTargetSpecification.NULL, ValueProperties.with(ValuePropertyNames.FUNCTION, "A2").get()), 42d);
    return function;
  }

  private MockFunction mockFunctionY() {
    final MockFunction function = new MockFunction("Y", ComputationTarget.NULL);
    function.addRequirement(new ValueRequirement("A", ComputationTargetSpecification.NULL));
    function.addResult(new ValueSpecification("Y", ComputationTargetSpecification.NULL, ValueProperties.with(ValuePropertyNames.FUNCTION, "Y").get()), 42d);
    return function;
  }

  private MockFunction mockFunctionZ1() {
    final MockFunction function = new MockFunction("Z1", ComputationTarget.NULL) {
      @Override
      public Set<ValueRequirement> getAdditionalRequirements(final FunctionCompilationContext context, final ComputationTarget target, final Set<ValueSpecification> inputs,
          final Set<ValueSpecification> outputs) {
        return Collections.singleton(new ValueRequirement("X", ComputationTargetSpecification.NULL));
      }
    };
    function.addRequirement(new ValueRequirement("A", ComputationTargetSpecification.NULL));
    function.addResult(new ValueSpecification("Z", ComputationTargetSpecification.NULL, ValueProperties.with(ValuePropertyNames.FUNCTION, "Z1").get()), 42d);
    return function;
  }

  private MockFunction mockFunctionZ2() {
    final MockFunction function = new MockFunction("Z2", ComputationTarget.NULL) {
      @Override
      public Set<ValueRequirement> getAdditionalRequirements(final FunctionCompilationContext context, final ComputationTarget target, final Set<ValueSpecification> inputs,
          final Set<ValueSpecification> outputs) {
        return Collections.singleton(new ValueRequirement("A", ComputationTargetSpecification.NULL));
      }
    };
    function.addRequirement(new ValueRequirement("X", ComputationTargetSpecification.NULL));
    function.addResult(new ValueSpecification("Z", ComputationTargetSpecification.NULL, ValueProperties.with(ValuePropertyNames.FUNCTION, "Z2").get()), 42d);
    return function;
  }

  private Portfolio portfolio() {
    final SimplePortfolio portfolio = new SimplePortfolio("Foo");
    portfolio.setUniqueId(UniqueId.of("Portfolio", "Test", "0"));
    return portfolio;
  }

  private SecuritySource securities() {
    final InMemorySecuritySource securities = new InMemorySecuritySource();
    return securities;
  }

  private PositionSource positions() {
    final MockPositionSource positions = new MockPositionSource();
    positions.addPortfolio(portfolio());
    return positions;
  }

  private ComputationTargetResolver targetResolver() {
    return new DefaultComputationTargetResolver(securities(), positions());
  }

  private FunctionResolver functionResolver(final FunctionCompilationContext compilationContext) {
    final InMemoryFunctionRepository functionRepository = new InMemoryFunctionRepository();
    functionRepository.addFunction(mockFunctionX());
    functionRepository.addFunction(mockFunctionA1());
    functionRepository.addFunction(mockFunctionA2());
    functionRepository.addFunction(mockFunctionY());
    functionRepository.addFunction(mockFunctionZ1());
    functionRepository.addFunction(mockFunctionZ2());
    final FunctionRepositoryCompiler compiler = new CachingFunctionRepositoryCompiler();
    final CompiledFunctionService cfs = new CompiledFunctionService(functionRepository, compiler, compilationContext);
    TestLifecycle.register(cfs);
    cfs.initialize();
    return new DefaultFunctionResolver(cfs);
  }

  private AmbiguityCheckerContext context() {
    final MarketDataAvailabilityProvider mdap = new OptimisticMarketDataAvailabilityFilter().withProvider(new DefaultMarketDataAvailabilityProvider());
    final FunctionCompilationContext compilationContext = new FunctionCompilationContext();
    compilationContext.setRawComputationTargetResolver(targetResolver());
    final FunctionResolver resolver = functionResolver(compilationContext);
    return new AmbiguityCheckerContext(mdap, compilationContext, resolver, null);
  }

  public void testPortfolioResolution() {
    TestLifecycle.begin();
    try {
      ViewDefinition viewDefinition = new ViewDefinition("Test 1", UniqueId.of("Portfolio", "Test"), UserPrincipal.getTestUser());
      ViewCalculationConfiguration calcConfig = new ViewCalculationConfiguration(viewDefinition, "Default");
      SimpleRequirementAmbiguityChecker checker = new SimpleRequirementAmbiguityChecker(context(), Instant.now(), VersionCorrection.LATEST, calcConfig);
      // Resolved the portfolio
      assertEquals(checker.getCompilationContext().getPortfolio().getUniqueId(), portfolio().getUniqueId());
      viewDefinition = new ViewDefinition("Test 2", UniqueId.of("Portfolio", "Missing", "1"), UserPrincipal.getTestUser());
      calcConfig = new ViewCalculationConfiguration(viewDefinition, "Default");
      checker = new SimpleRequirementAmbiguityChecker(context(), Instant.now(), VersionCorrection.LATEST, calcConfig);
      // Portfolio id not found
      assertEquals(checker.getCompilationContext().getPortfolio(), null);
      viewDefinition = new ViewDefinition("Test 3", UserPrincipal.getTestUser());
      calcConfig = new ViewCalculationConfiguration(viewDefinition, "Default");
      checker = new SimpleRequirementAmbiguityChecker(context(), Instant.now(), VersionCorrection.LATEST, calcConfig);
      // No portfolio requested
      assertEquals(checker.getCompilationContext().getPortfolio(), null);
    } finally {
      TestLifecycle.end();
    }
  }

  public void testDirectMarketDataRequest() {
    TestLifecycle.begin();
    try {
      final SimpleRequirementAmbiguityChecker checker = new SimpleRequirementAmbiguityChecker(context(), Instant.now(), VersionCorrection.LATEST);
      final ValueRequirement requirement = new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetSpecification.NULL);
      final FullRequirementResolution resolved = checker.resolve(requirement);
      assertNotNull(resolved);
      assertEquals(resolved.getRequirement(), requirement);
      assertTrue(resolved.isResolved());
      assertFalse(resolved.isAmbiguous());
      assertEquals(resolved.getResolutions().size(), 1);
      Collection<RequirementResolution> requirementResolutions = resolved.getResolutions().iterator().next();
      assertEquals(requirementResolutions.size(), 1);
      final RequirementResolution requirementResolution = requirementResolutions.iterator().next();
      assertEquals(requirementResolution.getSpecification(),
          new ValueSpecification(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetSpecification.NULL, ValueProperties
              .with(ValuePropertyNames.FUNCTION, "MarketDataSourcingFunction").get()));
    } finally {
      TestLifecycle.end();
    }
  }

  public void testAliasedMarketDataRequest() {
    TestLifecycle.begin();
    try {
      final SimpleRequirementAmbiguityChecker checker = new SimpleRequirementAmbiguityChecker(context(), Instant.now(), VersionCorrection.LATEST);
      final ValueRequirement requirement = new ValueRequirement("Market_FooBar", ComputationTargetSpecification.NULL, ValueProperties.with(ValuePropertyNames.FUNCTION, "Foobar")
          .with("X", "Y").get());
      final FullRequirementResolution resolved = checker.resolve(requirement);
      assertNotNull(resolved);
      assertEquals(resolved.getRequirement(), requirement);
      assertTrue(resolved.isResolved());
      assertFalse(resolved.isAmbiguous());
      assertEquals(resolved.getResolutions().size(), 1);
      Collection<RequirementResolution> requirementResolutions = resolved.getResolutions().iterator().next();
      assertEquals(requirementResolutions.size(), 1);
      final RequirementResolution requirementResolution = requirementResolutions.iterator().next();
      assertEquals(requirementResolution.getSpecification(),
          new ValueSpecification("Market_FooBar", ComputationTargetSpecification.NULL, ValueProperties.with(ValuePropertyNames.FUNCTION, "Foobar").with("X", "Y").get()));
    } finally {
      TestLifecycle.end();
    }
  }

  public void testUnresolved() {
    TestLifecycle.begin();
    try {
      final SimpleRequirementAmbiguityChecker checker = new SimpleRequirementAmbiguityChecker(context(), Instant.now(), VersionCorrection.LATEST);
      final ValueRequirement requirement = new ValueRequirement("Fail", ComputationTargetSpecification.NULL);
      final FullRequirementResolution resolved = checker.resolve(requirement);
      assertNotNull(resolved);
      assertFalse(resolved.isResolved());
      assertFalse(resolved.isAmbiguous());
      assertFalse(resolved.isDeeplyAmbiguous());
    } finally {
      TestLifecycle.end();
    }
  }

  public void testUnambigousGraph() {
    TestLifecycle.begin();
    try {
      final SimpleRequirementAmbiguityChecker checker = new SimpleRequirementAmbiguityChecker(context(), Instant.now(), VersionCorrection.LATEST);
      final ValueRequirement requirement = new ValueRequirement("X", ComputationTargetSpecification.NULL);
      final FullRequirementResolution resolved = checker.resolve(requirement);
      assertNotNull(resolved);
      assertTrue(resolved.isResolved());
      assertFalse(resolved.isAmbiguous());
      assertFalse(resolved.isDeeplyAmbiguous());
      assertEquals(resolved.getResolutions().size(), 1);
      Collection<RequirementResolution> requirementResolutions = resolved.getResolutions().iterator().next();
      assertEquals(requirementResolutions.size(), 1);
      final RequirementResolution requirementResolution = requirementResolutions.iterator().next();
      assertEquals(requirementResolution.getSpecification().getFunctionUniqueId(), "X");
    } finally {
      TestLifecycle.end();
    }
  }

  public void testAmbigiousFunctionSelection() {
    TestLifecycle.begin();
    try {
      final SimpleRequirementAmbiguityChecker checker = new SimpleRequirementAmbiguityChecker(context(), Instant.now(), VersionCorrection.LATEST);
      final ValueRequirement requirement = new ValueRequirement("A", ComputationTargetSpecification.NULL);
      final FullRequirementResolution resolved = checker.resolve(requirement);
      assertNotNull(resolved);
      assertTrue(resolved.isResolved());
      assertTrue(resolved.isAmbiguous());
      assertTrue(resolved.isDeeplyAmbiguous());
      assertEquals(resolved.getResolutions().size(), 1);
      Collection<RequirementResolution> requirementResolutions = resolved.getResolutions().iterator().next();
      assertEquals(requirementResolutions.size(), 2);
    } finally {
      TestLifecycle.end();
    }
  }

  public void testAmbigousInputSelection() {
    TestLifecycle.begin();
    try {
      final SimpleRequirementAmbiguityChecker checker = new SimpleRequirementAmbiguityChecker(context(), Instant.now(), VersionCorrection.LATEST);
      final ValueRequirement requirement = new ValueRequirement("Y", ComputationTargetSpecification.NULL);
      final FullRequirementResolution resolved = checker.resolve(requirement);
      assertNotNull(resolved);
      assertTrue(resolved.isResolved());
      assertFalse(resolved.isAmbiguous());
      assertTrue(resolved.isDeeplyAmbiguous());
      assertEquals(resolved.getResolutions().size(), 1);
      Collection<RequirementResolution> requirementResolutions = resolved.getResolutions().iterator().next();
      assertEquals(requirementResolutions.size(), 1);
    } finally {
      TestLifecycle.end();
    }
  }

  public void testAmbiguousAdditionalRequirements() {
    TestLifecycle.begin();
    try {
      final SimpleRequirementAmbiguityChecker checker = new SimpleRequirementAmbiguityChecker(context(), Instant.now(), VersionCorrection.LATEST);
      final ValueRequirement requirement = new ValueRequirement("Z", ComputationTargetSpecification.NULL);
      final FullRequirementResolution resolved = checker.resolve(requirement);
      assertNotNull(resolved);
      assertTrue(resolved.isResolved());
      assertTrue(resolved.isAmbiguous());
      assertTrue(resolved.isDeeplyAmbiguous());
      assertEquals(resolved.getResolutions().size(), 1);
      Collection<RequirementResolution> requirementResolutions = resolved.getResolutions().iterator().next();
      assertEquals(requirementResolutions.size(), 2);
      for (RequirementResolution requirementResolution : requirementResolutions) {
        final Collection<FullRequirementResolution> inputs = requirementResolution.getInputs();
        assertEquals(inputs.size(), 2);
        boolean ambiguous = false;
        for (FullRequirementResolution input : inputs) {
          ambiguous |= input.isAmbiguous();
        }
        assertTrue(ambiguous);
      }
    } finally {
      TestLifecycle.end();
    }
  }

  // TODO: Test function exclusion groups

}
