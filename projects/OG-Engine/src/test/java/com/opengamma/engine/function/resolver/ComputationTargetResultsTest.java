/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function.resolver;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.position.impl.SimplePosition;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.impl.SimpleSecurity;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.DefaultComputationTargetResolver;
import com.opengamma.engine.InMemorySecuritySource;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.CachingFunctionRepositoryCompiler;
import com.opengamma.engine.function.CompiledFunctionService;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInvoker;
import com.opengamma.engine.function.FunctionRepository;
import com.opengamma.engine.function.InMemoryFunctionRepository;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.test.TestLifecycle;

/**
 * Tests the {@link ComputationTargetResults} class.
 */
@Test(groups = TestGroup.UNIT)
public class ComputationTargetResultsTest {

  private static final Logger s_logger = LoggerFactory.getLogger(ComputationTargetResultsTest.class);

  private final Position POSITION = new SimplePosition(UniqueId.of("PosUID", "0"), BigDecimal.ONE, ExternalId.of("Sec", "0"));
  private final Security SECURITY = new SimpleSecurity(UniqueId.of("SecUID", "0"), ExternalId.of("Sec", "0").toBundle(), "TEST", "Foo");

  private static class MockFunction extends AbstractFunction.NonCompiled {

    private static int _identifier = 0;

    private final ComputationTargetType _type;
    private final String _resultValue;
    private final ValueProperties _resultProperties;
    private final String _requirementValue;
    private final ValueProperties _requirementConstraints;

    public MockFunction(final ComputationTargetType type, final String resultValue, final ValueProperties resultProperties, final String requirementValue,
        final ValueProperties requirementConstraints) {
      setUniqueId(String.valueOf(_identifier++));
      _type = type;
      _resultValue = resultValue;
      _resultProperties = resultProperties;
      _requirementValue = requirementValue;
      _requirementConstraints = requirementConstraints;
    }

    @Override
    public ComputationTargetType getTargetType() {
      return _type;
    }

    @Override
    public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
      return true;
    }

    @Override
    public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
      return Collections.singleton(new ValueSpecification(_resultValue, target.toSpecification(), _resultProperties.copy().with(ValuePropertyNames.FUNCTION, getUniqueId()).get()));
    }

    @Override
    public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
      return Collections.singleton(new ValueRequirement(_requirementValue, target.toSpecification(), _requirementConstraints));
    }

    @Override
    public FunctionInvoker getFunctionInvoker() {
      throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
      return _resultValue + " <- " + _requirementValue + " (" + _type + ")";
    }

  }

  private static class MockFunction2 extends MockFunction {

    public MockFunction2(final ComputationTargetType type, final String resultValue, final ValueProperties resultProperties, final String requirementValue,
        final ValueProperties requirementConstraints) {
      super(type, resultValue, resultProperties, requirementValue, requirementConstraints);
    }

    @Override
    public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
      final ValueSpecification input = inputs.keySet().iterator().next();
      return Collections.singleton(new ValueSpecification(super._resultValue, target.toSpecification(), input.getProperties()));
    }

  }

  private FunctionRepository emptyFunctionRepo() {
    return new InMemoryFunctionRepository();
  }

  private void addFunctions(final InMemoryFunctionRepository repo, final ComputationTargetType type) {
    repo.addFunction(new MockFunction(type, "A3", ValueProperties.all(), "A2", ValueProperties.none()));
    repo.addFunction(new MockFunction(type, "A2", ValueProperties.all(), "A1", ValueProperties.none()));
    repo.addFunction(new MockFunction(type, "A1", ValueProperties.all(), "A0", ValueProperties.none()));
    repo.addFunction(new MockFunction(type, "B3", ValueProperties.all(), "B2", ValueProperties.none()));
    repo.addFunction(new MockFunction(type, "B2", ValueProperties.all(), "B1", ValueProperties.none()));
    repo.addFunction(new MockFunction(type, "B1", ValueProperties.none(), "B0", ValueProperties.none()));
    repo.addFunction(new MockFunction(type, "C3", ValueProperties.all(), "C2", ValueProperties.withAny("F").get()));
    repo.addFunction(new MockFunction(type, "D3", ValueProperties.all(), "D2", ValueProperties.withAny("F").get()));
    repo.addFunction(new MockFunction(type, "D2", ValueProperties.withAny("F").get(), "D1", ValueProperties.with("F", "B").get()));
    repo.addFunction(new MockFunction(type, "E3", ValueProperties.none(), "E2", ValueProperties.none()));
    repo.addFunction(new MockFunction(type, "F3", ValueProperties.with("F", "B").get(), "F2", ValueProperties.none()));
    repo.addFunction(new MockFunction2(type, "G3", ValueProperties.all(), "G2", ValueProperties.none()));
    repo.addFunction(new MockFunction2(type, "G2", ValueProperties.all(), "G1", ValueProperties.none()));
    repo.addFunction(new MockFunction2(type, "G1", ValueProperties.all(), "G0", ValueProperties.none()));
    repo.addFunction(new MockFunction2(type, "H3", ValueProperties.all(), "H2", ValueProperties.none()));
    repo.addFunction(new MockFunction2(type, "H2", ValueProperties.all(), "H1", ValueProperties.none()));
    repo.addFunction(new MockFunction2(type, "H1", ValueProperties.none(), "H0", ValueProperties.none()));
    repo.addFunction(new MockFunction2(type, "I3", ValueProperties.all(), "I2", ValueProperties.withAny("F").get()));
    repo.addFunction(new MockFunction2(type, "J3", ValueProperties.all(), "J2", ValueProperties.withAny("F").get()));
    repo.addFunction(new MockFunction2(type, "J2", ValueProperties.withAny("F").get(), "J1", ValueProperties.with("F", "B").get()));
    repo.addFunction(new MockFunction2(type, "K3", ValueProperties.none(), "L2", ValueProperties.none()));
    repo.addFunction(new MockFunction2(type, "L3", ValueProperties.with("F", "B").get(), "L2", ValueProperties.none()));
  }

  private FunctionRepository basicFunctionRepo() {
    final InMemoryFunctionRepository repo = new InMemoryFunctionRepository();
    addFunctions(repo, ComputationTargetType.PORTFOLIO_NODE);
    addFunctions(repo, ComputationTargetType.POSITION);
    return repo;
  }

  private ComputationTargetResults createComputationTargetResults(final FunctionRepository functionRepo) {
    final InMemorySecuritySource securitySource = new InMemorySecuritySource();
    securitySource.addSecurity(SECURITY);
    final PositionSource positionSource = Mockito.mock(PositionSource.class);
    Mockito.when(positionSource.getPosition(POSITION.getUniqueId())).thenReturn(POSITION);
    Mockito.when(positionSource.getPosition(Mockito.eq(POSITION.getUniqueId().getObjectId()), Mockito.any(VersionCorrection.class))).thenReturn(POSITION);
    final FunctionCompilationContext context = new FunctionCompilationContext();
    context.setRawComputationTargetResolver(new DefaultComputationTargetResolver(securitySource, positionSource));
    context.setComputationTargetResolver(context.getRawComputationTargetResolver().atVersionCorrection(VersionCorrection.of(Instant.now(), Instant.now())));
    final CompiledFunctionService cfs = new CompiledFunctionService(functionRepo, new CachingFunctionRepositoryCompiler(), context);
    TestLifecycle.register(cfs);
    cfs.initialize();
    final FunctionResolver functionResolver = new DefaultFunctionResolver(cfs);
    final CompiledFunctionResolver compiledFunctionResolver = functionResolver.compile(Instant.now());
    final ComputationTargetResults results = new ComputationTargetResults(compiledFunctionResolver.getAllResolutionRules());
    results.setFunctionCompilationContext(context);
    return results;
  }

  public void testMaximalResults_emptyRepo() {
    TestLifecycle.begin();
    try {
      final ComputationTargetResults ctr = createComputationTargetResults(emptyFunctionRepo());
      final ComputationTarget target = new ComputationTarget(ComputationTargetType.POSITION, POSITION);
      final Collection<ValueSpecification> values = ctr.getMaximalResults(target);
      s_logger.debug("testMaximalResults_emptyRepo = {}", values);
      assertTrue(values.isEmpty());
    } finally {
      TestLifecycle.end();
    }
  }

  private Set<String> getResults(final Collection<ValueSpecification> values) {
    final Set<String> results = Sets.newHashSetWithExpectedSize(values.size());
    for (final ValueSpecification value : values) {
      results.add(value.getValueName());
    }
    return results;
  }

  public void testMaximalResults_basicRepo() {
    TestLifecycle.begin();
    try {
      final ComputationTargetResults ctr = createComputationTargetResults(basicFunctionRepo());
      final ComputationTarget target = new ComputationTarget(ComputationTargetType.POSITION, POSITION);
      final Collection<ValueSpecification> values = ctr.getMaximalResults(target);
      s_logger.debug("testMaximalResults_basicRepo = {}", values);
      final Set<String> results = getResults(values);
      assertEquals(results, ImmutableSet.of("A3", "A2", "A1", "B3", "B2", "B1", "C3", "D3", "D2", "E3", "F3", "G3", "G2", "G1", "H3", "H2", "H1", "I3", "J3", "J2", "K3", "L3"));
    } finally {
      TestLifecycle.end();
    }
  }

  public void testPartialResults_emptyRepo() {
    TestLifecycle.begin();
    try {
      final ComputationTargetResults ctr = createComputationTargetResults(emptyFunctionRepo());
      final ComputationTarget target = new ComputationTarget(ComputationTargetType.POSITION, POSITION);
      final Collection<ValueSpecification> values = ctr.getPartialResults(target);
      s_logger.debug("testPartialResults_emptyRepo = {}", values);
      assertTrue(values.isEmpty());
    } finally {
      TestLifecycle.end();
    }
  }

  public void testPartialResults_basicRepo() {
    TestLifecycle.begin();
    try {
      final ComputationTargetResults ctr = createComputationTargetResults(basicFunctionRepo());
      final ComputationTarget target = new ComputationTarget(ComputationTargetType.POSITION, POSITION);
      final Collection<ValueSpecification> values = ctr.getPartialResults(target);
      s_logger.debug("testPartialResults_basicRepo = {}", values);
      final Set<String> results = getResults(values);
      assertEquals(results, ImmutableSet.of("B1", "D2", "E3", "F3", "H3", "H2", "H1", "J3", "J2", "K3", "L3"));
    } finally {
      TestLifecycle.end();
    }
  }

}
