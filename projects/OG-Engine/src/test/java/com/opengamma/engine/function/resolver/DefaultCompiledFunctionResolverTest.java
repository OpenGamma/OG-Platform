/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.resolver;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.mockito.Mockito;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.SimplePosition;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.DefaultComputationTargetResolver;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInvoker;
import com.opengamma.engine.function.ParameterizedFunction;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.test.PrimitiveTestFunction;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.Triple;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class DefaultCompiledFunctionResolverTest {

  private static ParameterizedFunction function(final CompiledFunctionDefinition cfd, final String uid) {
    ((AbstractFunction) cfd).setUniqueId(uid);
    return new ParameterizedFunction(cfd, cfd.getFunctionDefinition().getDefaultParameters());
  }

  private FunctionCompilationContext createFunctionCompilationContext() {
    final FunctionCompilationContext context = new FunctionCompilationContext();
    context.setRawComputationTargetResolver(new DefaultComputationTargetResolver());
    context.setComputationTargetResolver(context.getRawComputationTargetResolver().atVersionCorrection(VersionCorrection.of(Instant.now(), Instant.now())));
    return context;
  }

  public void testBasicResolution() {
    final ComputationTarget target = new ComputationTarget(ComputationTargetType.PRIMITIVE, UniqueId.of("scheme", "test_target"));
    final ParameterizedFunction parameterizedF1 = function(new PrimitiveTestFunction("req1"), "1");
    final ParameterizedFunction parameterizedF2 = function(new PrimitiveTestFunction("req1"), "2");
    final DefaultCompiledFunctionResolver resolver = new DefaultCompiledFunctionResolver(createFunctionCompilationContext());
    resolver.addRule(new ResolutionRule(parameterizedF1, ApplyToAllTargets.INSTANCE, 100));
    resolver.addRule(new ResolutionRule(parameterizedF2, ApplyToAllTargets.INSTANCE, 200));
    resolver.compileRules();
    Triple<ParameterizedFunction, ValueSpecification, Collection<ValueSpecification>> result = resolver.resolveFunction("req1", target, ValueProperties.none()).next();
    assertEquals(result.getFirst(), parameterizedF2);
  }

  private static class Filter implements ComputationTargetFilter {

    private final ComputationTarget _match;

    public Filter(final ComputationTarget match) {
      _match = match;
    }

    @Override
    public boolean accept(final ComputationTarget target) {
      return _match.getValue().equals(target.getValue());
    }

  }

  public void testFilteredRule() {
    final ComputationTarget target = new ComputationTarget(ComputationTargetType.PRIMITIVE, UniqueId.of("scheme", "test_target"));
    final ParameterizedFunction parameterizedF1 = function(new PrimitiveTestFunction("req1"), "1");
    final ParameterizedFunction parameterizedF2 = function(new PrimitiveTestFunction("req1"), "2");
    final DefaultCompiledFunctionResolver resolver = new DefaultCompiledFunctionResolver(createFunctionCompilationContext());
    resolver.addRule(new ResolutionRule(parameterizedF1, ApplyToAllTargets.INSTANCE, 100));
    resolver.addRule(new ResolutionRule(parameterizedF2, new Filter(target), 200));
    resolver.compileRules();
    Triple<ParameterizedFunction, ValueSpecification, Collection<ValueSpecification>> result = resolver.resolveFunction("req1", target, ValueProperties.none()).next();
    assertEquals(result.getFirst(), parameterizedF2);
    ComputationTarget anotherTarget = new ComputationTarget(ComputationTargetType.PRIMITIVE, UniqueId.of("scheme", "target2"));
    result = resolver.resolveFunction("req1", anotherTarget, ValueProperties.none()).next();
    assertEquals(result.getFirst(), parameterizedF1);
  }

  private static class TestSecurityFunction extends AbstractFunction.NonCompiled {

    @Override
    public ComputationTargetType getTargetType() {
      return ComputationTargetType.SECURITY;
    }

    @Override
    public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
      assertTrue(getTargetType().isCompatible(target.getType()));
      return true;
    }

    @Override
    public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
      return Collections.singleton(new ValueSpecification("Value", target.toSpecification(), createValueProperties().get()));
    }

    @Override
    public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
      fail();
      return null;
    }

    @Override
    public FunctionInvoker getFunctionInvoker() {
      fail();
      return null;
    }

  }

  private static class MockSecurityA implements Security {

    @Override
    public Map<String, String> getAttributes() {
      return null;
    }

    @Override
    public void setAttributes(Map<String, String> attributes) {
    }

    @Override
    public void addAttribute(String key, String value) {
    }

    @Override
    public UniqueId getUniqueId() {
      return UniqueId.of("Sec", "A");
    }

    @Override
    public ExternalIdBundle getExternalIdBundle() {
      return null;
    }

    @Override
    public String getSecurityType() {
      return "A";
    }

    @Override
    public String getName() {
      return "Name";
    }

  }

  public void testSecurityFunction() {
    final ComputationTarget target = new ComputationTarget(ComputationTargetType.SECURITY, new MockSecurityA());
    final ParameterizedFunction pfn = function(new TestSecurityFunction(), "1");
    final DefaultCompiledFunctionResolver resolver = new DefaultCompiledFunctionResolver(createFunctionCompilationContext());
    resolver.addRule(new ResolutionRule(pfn, ApplyToAllTargets.INSTANCE, 0));
    resolver.compileRules();
    Triple<ParameterizedFunction, ValueSpecification, Collection<ValueSpecification>> result = resolver.resolveFunction("Value", target, ValueProperties.none()).next();
    assertEquals(result.getFirst(), pfn);
  }

  private static class MockSecurityB implements Security {

    @Override
    public Map<String, String> getAttributes() {
      return null;
    }

    @Override
    public void setAttributes(Map<String, String> attributes) {
    }

    @Override
    public void addAttribute(String key, String value) {
    }

    @Override
    public UniqueId getUniqueId() {
      return UniqueId.of("Sec", "B");
    }

    @Override
    public ExternalIdBundle getExternalIdBundle() {
      return null;
    }

    @Override
    public String getSecurityType() {
      return "B";
    }

    @Override
    public String getName() {
      return "Name";
    }

  }

  private static class TestSecuritySubClassFunction extends TestSecurityFunction {

    @Override
    public ComputationTargetType getTargetType() {
      return ComputationTargetType.of(MockSecurityA.class);
    }

    @Override
    public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
      assertTrue(getTargetType().isCompatible(target.getType()));
      assertTrue(target.getSecurity() instanceof MockSecurityA);
      return true;
    }

  }

  public void testSecuritySubClassFunction() {
    final ParameterizedFunction pfn1 = function(new TestSecurityFunction(), "1");
    final ParameterizedFunction pfn2 = function(new TestSecuritySubClassFunction(), "2");
    final DefaultCompiledFunctionResolver resolver = new DefaultCompiledFunctionResolver(createFunctionCompilationContext());
    resolver.addRule(new ResolutionRule(pfn1, ApplyToAllTargets.INSTANCE, 0));
    resolver.addRule(new ResolutionRule(pfn2, ApplyToAllTargets.INSTANCE, 1));
    resolver.compileRules();
    Triple<ParameterizedFunction, ValueSpecification, Collection<ValueSpecification>> result = resolver.resolveFunction("Value",
        new ComputationTarget(ComputationTargetType.of(MockSecurityA.class), new MockSecurityA()), ValueProperties.none()).next();
    assertEquals(result.getFirst(), pfn2);
    result = resolver.resolveFunction("Value", new ComputationTarget(ComputationTargetType.of(MockSecurityB.class), new MockSecurityB()), ValueProperties.none()).next();
    assertEquals(result.getFirst(), pfn1);
  }

  private static class TestPositionFunction extends AbstractFunction.NonCompiled {

    @Override
    public ComputationTargetType getTargetType() {
      return ComputationTargetType.POSITION;
    }

    @Override
    public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
      assertTrue(getTargetType().isCompatible(target.getType()));
      return true;
    }

    @Override
    public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
      return Collections.singleton(new ValueSpecification("Value", target.toSpecification(), createValueProperties().get()));
    }

    @Override
    public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
      fail();
      return null;
    }

    @Override
    public FunctionInvoker getFunctionInvoker() {
      fail();
      return null;
    }

  }

  private <T extends UniqueIdentifiable> T mock(final Class<T> clazz) {
    final T object = Mockito.mock(clazz);
    Mockito.when(object.getUniqueId()).thenReturn(UniqueId.of(clazz.getSimpleName(), Integer.toString(object.hashCode())));
    return object;
  }

  public void testPositionFunction() {
    final ComputationTarget target = new ComputationTarget(ComputationTargetType.of(SimplePosition.class), mock(SimplePosition.class));
    final ParameterizedFunction pfn = function(new TestPositionFunction(), "1");
    final DefaultCompiledFunctionResolver resolver = new DefaultCompiledFunctionResolver(createFunctionCompilationContext());
    resolver.addRule(new ResolutionRule(pfn, ApplyToAllTargets.INSTANCE, 0));
    resolver.compileRules();
    Triple<ParameterizedFunction, ValueSpecification, Collection<ValueSpecification>> result = resolver.resolveFunction("Value", target, ValueProperties.none()).next();
    assertEquals(result.getFirst(), pfn);
    result = resolver.resolveFunction(
        "Value",
        new ComputationTarget(new ComputationTargetSpecification(ComputationTargetType.PORTFOLIO_NODE, UniqueId.of("Node", "0")).containing(ComputationTargetType.POSITION, target.getUniqueId()),
            target.getValue()), ValueProperties.none())
        .next();
    assertEquals(result.getFirst(), pfn);
  }

  private static class TestContextPositionFunction extends AbstractFunction.NonCompiled {

    @Override
    public ComputationTargetType getTargetType() {
      return ComputationTargetType.PORTFOLIO_NODE.containing(ComputationTargetType.POSITION);
    }

    @Override
    public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
      assertEquals(target.getType(), ComputationTargetType.PORTFOLIO_NODE.containing(ComputationTargetType.of(SimplePosition.class)));
      assertEquals(target.getContextSpecification(), new ComputationTargetSpecification(ComputationTargetType.PORTFOLIO_NODE, UniqueId.of("Node", "0")));
      return true;
    }

    @Override
    public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
      return Collections.singleton(new ValueSpecification("Value", target.toSpecification(), createValueProperties().get()));
    }

    @Override
    public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
      fail();
      return null;
    }

    @Override
    public FunctionInvoker getFunctionInvoker() {
      fail();
      return null;
    }

  }

  public void testContextPositionFunction() {
    final ComputationTarget target = new ComputationTarget(ComputationTargetType.of(SimplePosition.class), mock(SimplePosition.class));
    final ParameterizedFunction pfn = function(new TestContextPositionFunction(), "1");
    final FunctionCompilationContext context = new FunctionCompilationContext();
    final ComputationTargetResolver.AtVersionCorrection targetResolver = Mockito.mock(ComputationTargetResolver.AtVersionCorrection.class);
    Mockito.when(targetResolver.resolve(target.toSpecification())).thenReturn(target);
    context.setComputationTargetResolver(targetResolver);
    final DefaultCompiledFunctionResolver resolver = new DefaultCompiledFunctionResolver(context);
    resolver.addRule(new ResolutionRule(pfn, ApplyToAllTargets.INSTANCE, 0));
    resolver.compileRules();
    assertFalse(resolver.resolveFunction("Value", target, ValueProperties.none()).hasNext());
    Triple<ParameterizedFunction, ValueSpecification, Collection<ValueSpecification>> result = resolver.resolveFunction(
        "Value", new ComputationTarget(new ComputationTargetSpecification(ComputationTargetType.PORTFOLIO_NODE, UniqueId.of("Node", "0")).containing(ComputationTargetType.of(SimplePosition.class),
            target.getUniqueId()), target.getValue()), ValueProperties.none()).next();
    assertEquals(result.getFirst(), pfn);
  }

  private static class TestTradeFunction extends AbstractFunction.NonCompiled {

    @Override
    public ComputationTargetType getTargetType() {
      return ComputationTargetType.TRADE;
    }

    @Override
    public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
      assertTrue(getTargetType().isCompatible(target.getType()));
      return true;
    }

    @Override
    public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
      return Collections.singleton(new ValueSpecification("Value", target.toSpecification(), createValueProperties().get()));
    }

    @Override
    public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
      fail();
      return null;
    }

    @Override
    public FunctionInvoker getFunctionInvoker() {
      fail();
      return null;
    }

  }

  private static class TestPositionOrTradeFunction extends AbstractFunction.NonCompiled {

    @Override
    public ComputationTargetType getTargetType() {
      return ComputationTargetType.POSITION_OR_TRADE;
    }

    @Override
    public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
      assertTrue(getTargetType().isCompatible(target.getType()));
      return true;
    }

    @Override
    public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
      return Collections.singleton(new ValueSpecification("Value", target.toSpecification(), createValueProperties().get()));
    }

    @Override
    public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
      fail();
      return null;
    }

    @Override
    public FunctionInvoker getFunctionInvoker() {
      fail();
      return null;
    }

  }

  public void testChainedRuleFolding() {
    final ComputationTarget target1 = new ComputationTarget(ComputationTargetType.of(SimplePosition.class), mock(SimplePosition.class));
    final ComputationTarget target2 = new ComputationTarget(ComputationTargetType.of(SimpleTrade.class), mock(SimpleTrade.class));
    final ParameterizedFunction pfn1 = function(new TestPositionFunction(), "1");
    final ParameterizedFunction pfn2 = function(new TestTradeFunction(), "2");
    final ParameterizedFunction pfn3 = function(new TestPositionOrTradeFunction(), "3");
    final FunctionCompilationContext context = new FunctionCompilationContext();
    final ComputationTargetResolver.AtVersionCorrection targetResolver = Mockito.mock(ComputationTargetResolver.AtVersionCorrection.class);
    Mockito.when(targetResolver.resolve(target1.toSpecification())).thenReturn(target1);
    Mockito.when(targetResolver.resolve(target2.toSpecification())).thenReturn(target2);
    context.setComputationTargetResolver(targetResolver);
    final DefaultCompiledFunctionResolver resolver = new DefaultCompiledFunctionResolver(context);
    resolver.addRule(new ResolutionRule(pfn1, ApplyToAllTargets.INSTANCE, 1));
    resolver.addRule(new ResolutionRule(pfn2, ApplyToAllTargets.INSTANCE, 1));
    resolver.addRule(new ResolutionRule(pfn3, ApplyToAllTargets.INSTANCE, 2));
    resolver.compileRules();
    Iterator<Triple<ParameterizedFunction, ValueSpecification, Collection<ValueSpecification>>> itr = resolver.resolveFunction("Value", target1, ValueProperties.none());
    assertEquals(itr.next().getFirst(), pfn3);
    assertEquals(itr.next().getFirst(), pfn1);
    itr = resolver.resolveFunction("Value", target2, ValueProperties.none());
    assertEquals(itr.next().getFirst(), pfn3);
    assertEquals(itr.next().getFirst(), pfn2);
  }

  private interface UserType extends Position, Trade {
    // This is a dumb case, but easy to test because the types and mock functions exist. In practice the equivalent will happen
    // when there are functions for two different interfaces that some securities may implement and a security that implements both
    // is the target
  }

  public void testCompiledRuleFolding() {
    final ComputationTarget target = new ComputationTarget(ComputationTargetType.of(UserType.class), mock(UserType.class));
    final ParameterizedFunction pfn1 = function(new TestPositionFunction(), "1");
    final ParameterizedFunction pfn2 = function(new TestTradeFunction(), "2");
    final FunctionCompilationContext context = new FunctionCompilationContext();
    final ComputationTargetResolver.AtVersionCorrection targetResolver = Mockito.mock(ComputationTargetResolver.AtVersionCorrection.class);
    Mockito.when(targetResolver.resolve(target.toSpecification())).thenReturn(target);
    context.setComputationTargetResolver(targetResolver);
    final DefaultCompiledFunctionResolver resolver = new DefaultCompiledFunctionResolver(context);
    resolver.addRule(new ResolutionRule(pfn1, ApplyToAllTargets.INSTANCE, 1));
    resolver.addRule(new ResolutionRule(pfn2, ApplyToAllTargets.INSTANCE, 2));
    resolver.compileRules();
    Iterator<Triple<ParameterizedFunction, ValueSpecification, Collection<ValueSpecification>>> itr = resolver.resolveFunction("Value", target, ValueProperties.none());
    assertEquals(itr.next().getFirst(), pfn2);
    assertEquals(itr.next().getFirst(), pfn1);
  }

}
