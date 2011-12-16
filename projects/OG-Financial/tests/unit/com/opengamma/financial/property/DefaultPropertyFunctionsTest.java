/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.property;

import static org.testng.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.TimeUnit;

import javax.time.Instant;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;
import org.testng.annotations.Test;

import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.MockPositionSource;
import com.opengamma.core.position.impl.SimplePortfolio;
import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.core.position.impl.SimplePosition;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecurityLink;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.core.security.impl.SimpleSecurityLink;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.DefaultComputationTargetResolver;
import com.opengamma.engine.depgraph.DependencyGraphBuilder;
import com.opengamma.engine.depgraph.DependencyGraphBuilderFactory;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.CachingFunctionRepositoryCompiler;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.CompiledFunctionService;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.function.FunctionRepository;
import com.opengamma.engine.function.InMemoryFunctionRepository;
import com.opengamma.engine.function.PortfolioStructure;
import com.opengamma.engine.function.resolver.CompiledFunctionResolver;
import com.opengamma.engine.function.resolver.ComputationTargetResults;
import com.opengamma.engine.function.resolver.DefaultFunctionResolver;
import com.opengamma.engine.function.resolver.FunctionPriority;
import com.opengamma.engine.function.resolver.FunctionResolver;
import com.opengamma.engine.marketdata.availability.DomainMarketDataAvailabilityProvider;
import com.opengamma.engine.test.MockSecuritySource;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.financial.analytics.PositionScalingFunction;
import com.opengamma.financial.analytics.PropertyPreservingFunction;
import com.opengamma.financial.analytics.SummingFunction;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalScheme;
import com.opengamma.id.UniqueId;
import com.opengamma.livedata.normalization.MarketDataRequirementNames;
import com.opengamma.util.money.Currency;

/**
 * Tests the functions used to inject default constraints into the dependency graph.
 */
@Test
public class DefaultPropertyFunctionsTest {

  public class TradeScalingFunction extends PropertyPreservingFunction {

    @Override
    protected Collection<String> getPreservedProperties() {
      return Arrays.asList("Currency", "ForwardCurve", "FundingCurve");
    }

    @Override
    protected Collection<String> getOptionalPreservedProperties() {
      return Collections.emptySet();
    }

    private final String _requirementName;

    public TradeScalingFunction(final String requirementName) {
      Validate.notNull(requirementName, "Requirement name");
      _requirementName = requirementName;
    }

    @Override
    public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
      return target.getType() == ComputationTargetType.TRADE;
    }

    @Override
    public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
      final Trade trade = target.getTrade();
      final Security security = trade.getSecurity();
      final ValueRequirement requirement = new ValueRequirement(_requirementName, ComputationTargetType.SECURITY, security.getUniqueId(), getInputConstraint(desiredValue));
      return Collections.singleton(requirement);
    }

    @Override
    public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
      final ValueSpecification specification = new ValueSpecification(_requirementName, target.toSpecification(), getResultProperties());
      return Collections.singleton(specification);
    }

    @Override
    public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
      final ValueSpecification specification = new ValueSpecification(_requirementName, target.toSpecification(), getResultProperties(inputs.keySet().iterator().next()));
      return Collections.singleton(specification);
    }

    @Override
    public String getShortName() {
      return "TradeScaling for " + _requirementName;
    }

    @Override
    public ComputationTargetType getTargetType() {
      return ComputationTargetType.TRADE;
    }

    @Override
    public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
      throw new UnsupportedOperationException();
    }

  }

  private static class DefaultForwardFundingCurveFunction extends DefaultPropertyFunction {

    private final String _forwardCurveName;
    private final String _fundingCurveName;
    private final String _valueName;

    public DefaultForwardFundingCurveFunction(final String forwardCurveName, final String fundingCurveName, final String valueName) {
      super(ComputationTargetType.SECURITY, true);
      _forwardCurveName = forwardCurveName;
      _fundingCurveName = fundingCurveName;
      _valueName = valueName;
    }

    @Override
    protected void getDefaults(final PropertyDefaults defaults) {
      defaults.addValuePropertyName(_valueName, "ForwardCurve");
      defaults.addValuePropertyName(_valueName, "FundingCurve");
    }

    @Override
    protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
      if ("ForwardCurve".equals(propertyName)) {
        return Collections.singleton(_forwardCurveName);
      } else if ("FundingCurve".equals(propertyName)) {
        return Collections.singleton(_fundingCurveName);
      } else {
        return null;
      }
    }

  }

  private static class MockPVFunction extends AbstractFunction.NonCompiledInvoker {

    @Override
    public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ComputationTargetType getTargetType() {
      return ComputationTargetType.SECURITY;
    }

    @Override
    public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
      return true;
    }

    @Override
    public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
      final Set<String> forwardCurves = desiredValue.getConstraints().getValues("ForwardCurve");
      if (forwardCurves.isEmpty()) {
        return null;
      }
      final Set<String> fundingCurves = desiredValue.getConstraints().getValues("FundingCurve");
      if (fundingCurves.isEmpty()) {
        return null;
      }
      // Two curves have been requested
      assertEquals(forwardCurves.size(), 1);
      assertEquals(fundingCurves.size(), 1);
      return Collections.emptySet();
    }

    @Override
    public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
      return Collections.singleton(new ValueSpecification("Present Value", target.toSpecification(), createValueProperties().withAny("ForwardCurve").withAny("FundingCurve").with("Currency", "USD")
          .get()));
    }

  }

  private FunctionRepository createFunctionRepository() {
    final InMemoryFunctionRepository functions = new InMemoryFunctionRepository();
    // Default property functions
    functions.addFunction(new PortfolioNodeCalcConfigDefaultPropertyFunction.Generic());
    functions.addFunction(new PortfolioNodeCalcConfigDefaultPropertyFunction.Specific());
    functions.addFunction(new PositionCalcConfigDefaultPropertyFunction.Generic());
    functions.addFunction(new PositionCalcConfigDefaultPropertyFunction.Specific());
    functions.addFunction(new PositionDefaultPropertyFunction());
    functions.addFunction(new PrimitiveCalcConfigDefaultPropertyFunction.Generic());
    functions.addFunction(new PrimitiveCalcConfigDefaultPropertyFunction.Specific());
    functions.addFunction(new SecurityCalcConfigDefaultPropertyFunction.Generic());
    functions.addFunction(new SecurityCalcConfigDefaultPropertyFunction.Specific());
    functions.addFunction(new TradeCalcConfigDefaultPropertyFunction.Generic());
    functions.addFunction(new TradeCalcConfigDefaultPropertyFunction.Specific());
    functions.addFunction(new TradeDefaultPropertyFunction());
    // Basic scaling and aggregation
    functions.addFunction(new SummingFunction("Present Value"));
    functions.addFunction(new PositionScalingFunction("Present Value"));
    functions.addFunction(new TradeScalingFunction("Present Value"));
    // Mock PV function
    functions.addFunction(new MockPVFunction());
    // Default curve injection
    functions.addFunction(new DefaultForwardFundingCurveFunction("DefaultForward", "DefaultFunding", "Present Value"));
    return functions;
  }

  private SecuritySource createSecuritySource() {
    final MockSecuritySource securities = new MockSecuritySource();
    final ZonedDateTime zdt = ZonedDateTime.now();
    final SwapLeg leg = new FixedInterestRateLeg(DayCountFactory.INSTANCE.getDayCount("ACT/365"), SimpleFrequency.ANNUAL, ExternalId.of("Test", "Region"),
        BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"), new InterestRateNotional(Currency.USD, 0d), false, 0d);
    final SwapSecurity security = new SwapSecurity(zdt, zdt, zdt, "Counterparty", leg, leg);
    security.addExternalId(ExternalId.of("Security", "Swap"));
    securities.addSecurity(security);
    return securities;
  }

  private SecurityLink createSecurityLink(final SecuritySource securities) {
    final SimpleSecurityLink link = new SimpleSecurityLink(ExternalId.of("Security", "Swap"));
    link.resolve(securities);
    return link;
  }

  private SimpleTrade createTrade(final SecuritySource securities) {
    final SimpleTrade trade = new SimpleTrade();
    trade.setQuantity(BigDecimal.ONE);
    trade.setSecurityLink(createSecurityLink(securities));
    return trade;
  }

  private SimplePosition createPosition(final SecuritySource securities) {
    final SimplePosition position = new SimplePosition();
    position.setQuantity(BigDecimal.ONE);
    position.setSecurityLink(createSecurityLink(securities));
    return position;
  }

  private PositionSource createPositionSource(final SecuritySource securities) {
    final MockPositionSource positions = new MockPositionSource();
    final SimplePortfolio portfolio = new SimplePortfolio("Test");
    final SimplePortfolioNode root = portfolio.getRootNode();
    // Portfolio node with position with a trade with an attribute
    SimplePortfolioNode node = new SimplePortfolioNode("TradeAttr");
    SimpleTrade trade = createTrade(securities);
    trade.addAttribute("Present Value.DEFAULT_ForwardCurve", "FooForward");
    trade.addAttribute("*.DEFAULT_FundingCurve", "FooFunding");
    SimplePosition position = createPosition(securities);
    position.addTrade(trade);
    node.addPosition(position);
    root.addChildNode(node);
    // Portfolio node with position with a trade without an attribute
    node = new SimplePortfolioNode("Trade");
    trade = createTrade(securities);
    position = createPosition(securities);
    position.addTrade(trade);
    node.addPosition(position);
    root.addChildNode(node);
    // Portfolio node with position with an attribute
    node = new SimplePortfolioNode("PositionAttr");
    position = createPosition(securities);
    position.addAttribute("Present Value.DEFAULT_ForwardCurve", "FooForward");
    position.addAttribute("*.DEFAULT_FundingCurve", "FooFunding");
    node.addPosition(position);
    root.addChildNode(node);
    // Portfolio node with position without an attribute
    node = new SimplePortfolioNode("Position");
    position = createPosition(securities);
    node.addPosition(position);
    root.addChildNode(node);
    portfolio.setUniqueId(UniqueId.of("Portfolio", "Test"));
    positions.addPortfolio(portfolio);
    return positions;
  }

  private PortfolioNode getPortfolioNode(final PositionSource positions, final String name) {
    final Portfolio portfolio = positions.getPortfolio(UniqueId.of("Portfolio", "Test"));
    for (PortfolioNode node : portfolio.getRootNode().getChildNodes()) {
      if (name.equals(node.getName())) {
        return node;
      }
    }
    throw new IllegalArgumentException("Couldn't find node " + name);
  }

  private Position getPosition(final PositionSource positions, final String name) {
    final PortfolioNode node = getPortfolioNode(positions, name);
    return node.getPositions().get(0);
  }

  private Trade getTrade(final PositionSource positions, final String name) {
    final Position position = getPosition(positions, name);
    return position.getTrades().iterator().next();
  }

  private FunctionCompilationContext createFunctionCompilationContext() {
    final FunctionCompilationContext context = new FunctionCompilationContext();
    final SecuritySource securities = createSecuritySource();
    context.setPortfolioStructure(new PortfolioStructure(createPositionSource(securities)));
    context.setSecuritySource(securities);
    return context;
  }

  private FunctionPriority createPrioritizer() {
    return new FunctionPriority() {

      @Override
      public int getPriority(final CompiledFunctionDefinition function) {
        if (function instanceof DefaultPropertyFunction) {
          final DefaultPropertyFunction propertyFunction = (DefaultPropertyFunction) function;
          if (propertyFunction.isPermitWithout()) {
            return -1;
          }
          return Integer.MAX_VALUE + propertyFunction.getPriority().getPriorityAdjust() - DefaultPropertyFunction.PriorityClass.MAX_ADJUST;
        }
        return 0;
      }

    };
  }

  private CompiledFunctionResolver createFunctionResolver(final FunctionCompilationContext ctx) {
    final CompiledFunctionService cfs = new CompiledFunctionService(createFunctionRepository(), new CachingFunctionRepositoryCompiler(), ctx);
    cfs.setExecutorService(new AbstractExecutorService() {

      @Override
      public void shutdown() {
      }

      @Override
      public List<Runnable> shutdownNow() {
        return null;
      }

      @Override
      public boolean isShutdown() {
        return false;
      }

      @Override
      public boolean isTerminated() {
        return false;
      }

      @Override
      public boolean awaitTermination(final long timeout, final TimeUnit unit) {
        return false;
      }

      @Override
      public void execute(final Runnable command) {
        command.run();
      }

    });
    cfs.initialize();
    final FunctionResolver resolver = new DefaultFunctionResolver(cfs, createPrioritizer());
    return resolver.compile(Instant.now());
  }

  private DependencyGraphBuilder createBuilder() {
    final DependencyGraphBuilderFactory factory = new DependencyGraphBuilderFactory();
    final DependencyGraphBuilder builder = factory.newInstance();
    final FunctionCompilationContext ctx = createFunctionCompilationContext();
    builder.setCalculationConfigurationName("Default");
    ctx.setViewCalculationConfiguration(new ViewCalculationConfiguration(new ViewDefinition("Name", "User"), "Default"));
    builder.setCompilationContext(ctx);
    final CompiledFunctionResolver cfr = createFunctionResolver(ctx);
    final ComputationTargetResolver targetResolver = new DefaultComputationTargetResolver(ctx.getSecuritySource(), ctx.getPortfolioStructure().getPositionSource());
    builder.setTargetResolver(targetResolver);
    ctx.setComputationTargetResults(new ComputationTargetResults(cfr.getAllResolutionRules(), ctx, targetResolver));
    builder.setFunctionResolver(cfr);
    builder.setMarketDataAvailabilityProvider(new DomainMarketDataAvailabilityProvider(ctx.getSecuritySource(), Arrays.asList(ExternalScheme.of("Foo")), Arrays
        .asList(MarketDataRequirementNames.MARKET_VALUE)));
    return builder;
  }

  private ValueRequirement createValueRequirement(final Object target, final ValueProperties constraints) {
    return new ValueRequirement("Present Value", new ComputationTargetSpecification(target), constraints);
  }

  public void testPortfolioNodeDefault() {
    final DependencyGraphBuilder builder = createBuilder();
    final PositionSource positions = builder.getCompilationContext().getPortfolioStructure().getPositionSource();
    final ValueRequirement req1 = createValueRequirement(getPortfolioNode(positions, "Position"), ValueProperties.none());
    builder.addTarget(req1);
    builder.getDependencyGraph();
    final ValueSpecification res1 = builder.getValueRequirementMapping().get(req1);
    assertEquals(res1.getProperty("ForwardCurve"), "DefaultForward");
    assertEquals(res1.getProperty("FundingCurve"), "DefaultFunding");
    assertEquals(res1.getProperty("Currency"), "USD");
  }

  public void testPortfolioNodeOverride() {
    final DependencyGraphBuilder builder = createBuilder();
    final PositionSource positions = builder.getCompilationContext().getPortfolioStructure().getPositionSource();
    final ValueRequirement req1 = createValueRequirement(getPortfolioNode(positions, "Position"), ValueProperties.with("ForwardCurve", "BarForward").with("FundingCurve", "BarFunding").get());
    final ValueRequirement req2 = createValueRequirement(getPortfolioNode(positions, "PositionAttr"), ValueProperties.with("ForwardCurve", "BarForward").with("FundingCurve", "BarFunding").get());
    builder.addTarget(req1);
    builder.addTarget(req2);
    builder.getDependencyGraph();
    final ValueSpecification res1 = builder.getValueRequirementMapping().get(req1);
    final ValueSpecification res2 = builder.getValueRequirementMapping().get(req2);
    assertEquals(res1.getProperty("ForwardCurve"), "BarForward");
    assertEquals(res1.getProperty("FundingCurve"), "BarFunding");
    assertEquals(res1.getProperty("Currency"), "USD");
    assertEquals(res2.getProperty("ForwardCurve"), "BarForward");
    assertEquals(res2.getProperty("FundingCurve"), "BarFunding");
    assertEquals(res2.getProperty("Currency"), "USD");
  }

  public void testPositionDefault() {
    final DependencyGraphBuilder builder = createBuilder();
    final PositionSource positions = builder.getCompilationContext().getPortfolioStructure().getPositionSource();
    final ValueRequirement req1 = createValueRequirement(getPosition(positions, "Position"), ValueProperties.none());
    builder.addTarget(req1);
    builder.getDependencyGraph();
    final ValueSpecification res1 = builder.getValueRequirementMapping().get(req1);
    assertEquals(res1.getProperty("ForwardCurve"), "DefaultForward");
    assertEquals(res1.getProperty("FundingCurve"), "DefaultFunding");
    assertEquals(res1.getProperty("Currency"), "USD");
  }

  public void testPositionOverride() {
    final DependencyGraphBuilder builder = createBuilder();
    final PositionSource positions = builder.getCompilationContext().getPortfolioStructure().getPositionSource();
    final ValueRequirement req1 = createValueRequirement(getPosition(positions, "Position"), ValueProperties.with("ForwardCurve", "BarForward").with("FundingCurve", "BarFunding").get());
    final ValueRequirement req2 = createValueRequirement(getPosition(positions, "PositionAttr"), ValueProperties.with("ForwardCurve", "BarForward").with("FundingCurve", "BarFunding").get());
    builder.addTarget(req1);
    builder.addTarget(req2);
    builder.getDependencyGraph();
    final ValueSpecification res1 = builder.getValueRequirementMapping().get(req1);
    final ValueSpecification res2 = builder.getValueRequirementMapping().get(req2);
    assertEquals(res1.getProperty("ForwardCurve"), "BarForward");
    assertEquals(res1.getProperty("FundingCurve"), "BarFunding");
    assertEquals(res1.getProperty("Currency"), "USD");
    assertEquals(res2.getProperty("ForwardCurve"), "BarForward");
    assertEquals(res2.getProperty("FundingCurve"), "BarFunding");
    assertEquals(res2.getProperty("Currency"), "USD");
  }

  public void testTradeDefault() {
    final DependencyGraphBuilder builder = createBuilder();
    final PositionSource positions = builder.getCompilationContext().getPortfolioStructure().getPositionSource();
    final ValueRequirement req1 = createValueRequirement(getTrade(positions, "Trade"), ValueProperties.none());
    builder.addTarget(req1);
    builder.getDependencyGraph();
    final ValueSpecification res1 = builder.getValueRequirementMapping().get(req1);
    assertEquals(res1.getProperty("ForwardCurve"), "DefaultForward");
    assertEquals(res1.getProperty("FundingCurve"), "DefaultFunding");
    assertEquals(res1.getProperty("Currency"), "USD");
  }

  public void testTradeOverride() {
    final DependencyGraphBuilder builder = createBuilder();
    final PositionSource positions = builder.getCompilationContext().getPortfolioStructure().getPositionSource();
    final ValueRequirement req1 = createValueRequirement(getTrade(positions, "Trade"), ValueProperties.with("ForwardCurve", "BarForward").with("FundingCurve", "BarFunding").get());
    final ValueRequirement req2 = createValueRequirement(getTrade(positions, "TradeAttr"), ValueProperties.with("ForwardCurve", "BarForward").with("FundingCurve", "BarFunding").get());
    builder.addTarget(req1);
    builder.addTarget(req2);
    builder.getDependencyGraph();
    final ValueSpecification res1 = builder.getValueRequirementMapping().get(req1);
    final ValueSpecification res2 = builder.getValueRequirementMapping().get(req2);
    assertEquals(res1.getProperty("ForwardCurve"), "BarForward");
    assertEquals(res1.getProperty("FundingCurve"), "BarFunding");
    assertEquals(res1.getProperty("Currency"), "USD");
    assertEquals(res2.getProperty("ForwardCurve"), "BarForward");
    assertEquals(res2.getProperty("FundingCurve"), "BarFunding");
    assertEquals(res2.getProperty("Currency"), "USD");
  }

  public void testPortfolioNodeGeneric() {
    final DependencyGraphBuilder builder = createBuilder();
    final ViewCalculationConfiguration config = builder.getCompilationContext().getViewCalculationConfiguration();
    config.setDefaultProperties(ValueProperties.with("PORTFOLIO_NODE.Present Value.DEFAULT_ForwardCurve", "BarForward").with("PORTFOLIO_NODE.*.DEFAULT_FundingCurve", "BarFunding").get());
    final PositionSource positions = builder.getCompilationContext().getPortfolioStructure().getPositionSource();
    final ValueRequirement req1 = createValueRequirement(getPortfolioNode(positions, "Position"), ValueProperties.none());
    final ValueRequirement req2 = createValueRequirement(getPortfolioNode(positions, "PositionAttr"), ValueProperties.none());
    builder.addTarget(req1);
    builder.addTarget(req2);
    builder.getDependencyGraph();
    final ValueSpecification res1 = builder.getValueRequirementMapping().get(req1);
    final ValueSpecification res2 = builder.getValueRequirementMapping().get(req2);
    assertEquals(res1.getProperty("ForwardCurve"), "BarForward");
    assertEquals(res1.getProperty("FundingCurve"), "BarFunding");
    assertEquals(res1.getProperty("Currency"), "USD");
    assertEquals(res2.getProperty("ForwardCurve"), "BarForward");
    assertEquals(res2.getProperty("FundingCurve"), "BarFunding");
    assertEquals(res2.getProperty("Currency"), "USD");
  }

  public void testPortfolioNodeSpecific() {
    final DependencyGraphBuilder builder = createBuilder();
    final ViewCalculationConfiguration config = builder.getCompilationContext().getViewCalculationConfiguration();
    final PositionSource positions = builder.getCompilationContext().getPortfolioStructure().getPositionSource();
    final PortfolioNode node1 = getPortfolioNode(positions, "PositionAttr");
    config.setDefaultProperties(ValueProperties.with("PORTFOLIO_NODE.Present Value.DEFAULT_ForwardCurve." + node1.getUniqueId(), "BarForward")
        .with("PORTFOLIO_NODE.*.DEFAULT_FundingCurve." + node1.getUniqueId(), "BarFunding").get());
    final ValueRequirement req1 = createValueRequirement(node1, ValueProperties.none());
    final ValueRequirement req2 = createValueRequirement(getPortfolioNode(positions, "Position"), ValueProperties.none());
    builder.addTarget(req1);
    builder.addTarget(req2);
    builder.getDependencyGraph();
    final ValueSpecification res1 = builder.getValueRequirementMapping().get(req1);
    final ValueSpecification res2 = builder.getValueRequirementMapping().get(req2);
    assertEquals(res1.getProperty("ForwardCurve"), "BarForward");
    assertEquals(res1.getProperty("FundingCurve"), "BarFunding");
    assertEquals(res1.getProperty("Currency"), "USD");
    assertEquals(res2.getProperty("ForwardCurve"), "DefaultForward");
    assertEquals(res2.getProperty("FundingCurve"), "DefaultFunding");
    assertEquals(res2.getProperty("Currency"), "USD");
  }

  public void testPortfolioNodeSpecificOverride() {
    final DependencyGraphBuilder builder = createBuilder();
    final ViewCalculationConfiguration config = builder.getCompilationContext().getViewCalculationConfiguration();
    final PositionSource positions = builder.getCompilationContext().getPortfolioStructure().getPositionSource();
    final PortfolioNode node1 = getPortfolioNode(positions, "PositionAttr");
    config.setDefaultProperties(ValueProperties.with("PORTFOLIO_NODE.Present Value.DEFAULT_ForwardCurve." + node1.getUniqueId(), "BarForward")
        .with("PORTFOLIO_NODE.Present Value.DEFAULT_FundingCurve." + node1.getUniqueId(), "BarFunding").with("PORTFOLIO_NODE.*.DEFAULT_ForwardCurve", "GenericForward")
        .with("PORTFOLIO_NODE.*.DEFAULT_FundingCurve", "GenericFunding").get());
    final ValueRequirement req1 = createValueRequirement(node1, ValueProperties.none());
    final ValueRequirement req2 = createValueRequirement(getPortfolioNode(positions, "Position"), ValueProperties.none());
    builder.addTarget(req1);
    builder.addTarget(req2);
    builder.getDependencyGraph();
    final ValueSpecification res1 = builder.getValueRequirementMapping().get(req1);
    final ValueSpecification res2 = builder.getValueRequirementMapping().get(req2);
    assertEquals(res1.getProperty("ForwardCurve"), "BarForward");
    assertEquals(res1.getProperty("FundingCurve"), "BarFunding");
    assertEquals(res1.getProperty("Currency"), "USD");
    assertEquals(res2.getProperty("ForwardCurve"), "GenericForward");
    assertEquals(res2.getProperty("FundingCurve"), "GenericFunding");
    assertEquals(res2.getProperty("Currency"), "USD");
  }

  public void testPositionGeneric() {
    final DependencyGraphBuilder builder = createBuilder();
    final ViewCalculationConfiguration config = builder.getCompilationContext().getViewCalculationConfiguration();
    config.setDefaultProperties(ValueProperties.with("POSITION.*.DEFAULT_ForwardCurve", "BarForward").with("POSITION.Present Value.DEFAULT_FundingCurve", "BarFunding").get());
    final PositionSource positions = builder.getCompilationContext().getPortfolioStructure().getPositionSource();
    final ValueRequirement req1 = createValueRequirement(getPosition(positions, "Position"), ValueProperties.none());
    final ValueRequirement req2 = createValueRequirement(getPosition(positions, "PositionAttr"), ValueProperties.none());
    builder.addTarget(req1);
    builder.addTarget(req2);
    builder.getDependencyGraph();
    final ValueSpecification res1 = builder.getValueRequirementMapping().get(req1);
    final ValueSpecification res2 = builder.getValueRequirementMapping().get(req2);
    assertEquals(res1.getProperty("ForwardCurve"), "BarForward");
    assertEquals(res1.getProperty("FundingCurve"), "BarFunding");
    assertEquals(res1.getProperty("Currency"), "USD");
    assertEquals(res2.getProperty("ForwardCurve"), "BarForward");
    assertEquals(res2.getProperty("FundingCurve"), "BarFunding");
    assertEquals(res2.getProperty("Currency"), "USD");
  }

  public void testPositionSpecific() {
    final DependencyGraphBuilder builder = createBuilder();
    final ViewCalculationConfiguration config = builder.getCompilationContext().getViewCalculationConfiguration();
    final PositionSource positions = builder.getCompilationContext().getPortfolioStructure().getPositionSource();
    final Position position1 = getPosition(positions, "PositionAttr");
    config.setDefaultProperties(ValueProperties.with("POSITION.Present Value.DEFAULT_ForwardCurve." + position1.getUniqueId(), "BarForward")
        .with("POSITION.*.DEFAULT_FundingCurve." + position1.getUniqueId(), "BarFunding").get());
    final ValueRequirement req1 = createValueRequirement(position1, ValueProperties.none());
    final ValueRequirement req2 = createValueRequirement(getPosition(positions, "Position"), ValueProperties.none());
    builder.addTarget(req1);
    builder.addTarget(req2);
    builder.getDependencyGraph();
    final ValueSpecification res1 = builder.getValueRequirementMapping().get(req1);
    final ValueSpecification res2 = builder.getValueRequirementMapping().get(req2);
    assertEquals(res1.getProperty("ForwardCurve"), "BarForward");
    assertEquals(res1.getProperty("FundingCurve"), "BarFunding");
    assertEquals(res1.getProperty("Currency"), "USD");
    assertEquals(res2.getProperty("ForwardCurve"), "DefaultForward");
    assertEquals(res2.getProperty("FundingCurve"), "DefaultFunding");
    assertEquals(res2.getProperty("Currency"), "USD");
  }

  public void testPositionSpecificOverride() {
    final DependencyGraphBuilder builder = createBuilder();
    final ViewCalculationConfiguration config = builder.getCompilationContext().getViewCalculationConfiguration();
    final PositionSource positions = builder.getCompilationContext().getPortfolioStructure().getPositionSource();
    final Position position1 = getPosition(positions, "PositionAttr");
    config.setDefaultProperties(ValueProperties.with("POSITION.Present Value.DEFAULT_ForwardCurve." + position1.getUniqueId(), "BarForward")
        .with("POSITION.*.DEFAULT_FundingCurve." + position1.getUniqueId(), "BarFunding").with("POSITION.*.DEFAULT_ForwardCurve", "GenericForward")
        .with("POSITION.Present Value.DEFAULT_FundingCurve", "GenericFunding").get());
    final ValueRequirement req1 = createValueRequirement(position1, ValueProperties.none());
    final ValueRequirement req2 = createValueRequirement(getPosition(positions, "Position"), ValueProperties.none());
    builder.addTarget(req1);
    builder.addTarget(req2);
    builder.getDependencyGraph();
    final ValueSpecification res1 = builder.getValueRequirementMapping().get(req1);
    final ValueSpecification res2 = builder.getValueRequirementMapping().get(req2);
    assertEquals(res1.getProperty("ForwardCurve"), "BarForward");
    assertEquals(res1.getProperty("FundingCurve"), "BarFunding");
    assertEquals(res1.getProperty("Currency"), "USD");
    assertEquals(res2.getProperty("ForwardCurve"), "GenericForward");
    assertEquals(res2.getProperty("FundingCurve"), "GenericFunding");
    assertEquals(res2.getProperty("Currency"), "USD");
  }

  public void testPositionAttribute() {
    final DependencyGraphBuilder builder = createBuilder();
    final PositionSource positions = builder.getCompilationContext().getPortfolioStructure().getPositionSource();
    final ValueRequirement req1 = createValueRequirement(getPosition(positions, "PositionAttr"), ValueProperties.none());
    builder.addTarget(req1);
    builder.getDependencyGraph();
    final ValueSpecification res1 = builder.getValueRequirementMapping().get(req1);
    assertEquals(res1.getProperty("ForwardCurve"), "FooForward");
    assertEquals(res1.getProperty("FundingCurve"), "FooFunding");
    assertEquals(res1.getProperty("Currency"), "USD");
  }

  public void testSecurityGeneric() {
    final DependencyGraphBuilder builder = createBuilder();
    final ViewCalculationConfiguration config = builder.getCompilationContext().getViewCalculationConfiguration();
    config.setDefaultProperties(ValueProperties.with("SECURITY.Present Value.DEFAULT_ForwardCurve", "BarForward").with("SECURITY.*.DEFAULT_FundingCurve", "BarFunding").get());
    final ValueRequirement req1 = createValueRequirement(builder.getCompilationContext().getSecuritySource().getSecurity(ExternalIdBundle.of(ExternalId.of("Security", "Swap"))),
        ValueProperties.none());
    builder.addTarget(req1);
    builder.getDependencyGraph();
    final ValueSpecification res1 = builder.getValueRequirementMapping().get(req1);
    assertEquals(res1.getProperty("ForwardCurve"), "BarForward");
    assertEquals(res1.getProperty("FundingCurve"), "BarFunding");
    assertEquals(res1.getProperty("Currency"), "USD");
  }

  public void testSecuritySpecific() {
    final DependencyGraphBuilder builder = createBuilder();
    final ViewCalculationConfiguration config = builder.getCompilationContext().getViewCalculationConfiguration();
    config.setDefaultProperties(ValueProperties.with("SECURITY.Present Value.DEFAULT_ForwardCurve.Security~Swap", "BarForward")
        .with("SECURITY.*.DEFAULT_FundingCurve.Security~Swap", "BarFunding").get());
    final ValueRequirement req1 = createValueRequirement(builder.getCompilationContext().getSecuritySource().getSecurity(ExternalIdBundle.of(ExternalId.of("Security", "Swap"))),
        ValueProperties.none());
    builder.addTarget(req1);
    builder.getDependencyGraph();
    final ValueSpecification res1 = builder.getValueRequirementMapping().get(req1);
    assertEquals(res1.getProperty("ForwardCurve"), "BarForward");
    assertEquals(res1.getProperty("FundingCurve"), "BarFunding");
    assertEquals(res1.getProperty("Currency"), "USD");
  }

  public void testSecuritySpecificOverride() {
    final DependencyGraphBuilder builder = createBuilder();
    final ViewCalculationConfiguration config = builder.getCompilationContext().getViewCalculationConfiguration();
    config.setDefaultProperties(ValueProperties.with("SECURITY.*.DEFAULT_ForwardCurve", "GenericForward").with("SECURITY.*.DEFAULT_FundingCurve", "GenericFunding")
        .with("SECURITY.Present Value.DEFAULT_ForwardCurve.Security~Swap", "BarForward").with("SECURITY.Present Value.DEFAULT_FundingCurve.Security~Swap", "BarFunding").get());
    final ValueRequirement req1 = createValueRequirement(builder.getCompilationContext().getSecuritySource().getSecurity(ExternalIdBundle.of(ExternalId.of("Security", "Swap"))),
        ValueProperties.none());
    builder.addTarget(req1);
    builder.getDependencyGraph();
    final ValueSpecification res1 = builder.getValueRequirementMapping().get(req1);
    assertEquals(res1.getProperty("ForwardCurve"), "BarForward");
    assertEquals(res1.getProperty("FundingCurve"), "BarFunding");
    assertEquals(res1.getProperty("Currency"), "USD");
  }

  public void testTradeGeneric() {
    final DependencyGraphBuilder builder = createBuilder();
    final ViewCalculationConfiguration config = builder.getCompilationContext().getViewCalculationConfiguration();
    config.setDefaultProperties(ValueProperties.with("TRADE.Present Value.DEFAULT_ForwardCurve", "BarForward").with("TRADE.*.DEFAULT_FundingCurve", "BarFunding").get());
    final PositionSource positions = builder.getCompilationContext().getPortfolioStructure().getPositionSource();
    final ValueRequirement req1 = createValueRequirement(getTrade(positions, "Trade"), ValueProperties.none());
    final ValueRequirement req2 = createValueRequirement(getTrade(positions, "TradeAttr"), ValueProperties.none());
    builder.addTarget(req1);
    builder.addTarget(req2);
    builder.getDependencyGraph();
    final ValueSpecification res1 = builder.getValueRequirementMapping().get(req1);
    final ValueSpecification res2 = builder.getValueRequirementMapping().get(req2);
    assertEquals(res1.getProperty("ForwardCurve"), "BarForward");
    assertEquals(res1.getProperty("FundingCurve"), "BarFunding");
    assertEquals(res1.getProperty("Currency"), "USD");
    assertEquals(res2.getProperty("ForwardCurve"), "BarForward");
    assertEquals(res2.getProperty("FundingCurve"), "BarFunding");
    assertEquals(res2.getProperty("Currency"), "USD");
  }

  public void testTradeSpecific() {
    final DependencyGraphBuilder builder = createBuilder();
    final ViewCalculationConfiguration config = builder.getCompilationContext().getViewCalculationConfiguration();
    final PositionSource positions = builder.getCompilationContext().getPortfolioStructure().getPositionSource();
    final Trade trade1 = getTrade(positions, "TradeAttr");
    config.setDefaultProperties(ValueProperties.with("TRADE.*.DEFAULT_ForwardCurve." + trade1.getUniqueId(), "BarForward")
        .with("TRADE.Present Value.DEFAULT_FundingCurve." + trade1.getUniqueId(), "BarFunding").get());
    final ValueRequirement req1 = createValueRequirement(trade1, ValueProperties.none());
    final ValueRequirement req2 = createValueRequirement(getTrade(positions, "Trade"), ValueProperties.none());
    builder.addTarget(req1);
    builder.addTarget(req2);
    builder.getDependencyGraph();
    final ValueSpecification res1 = builder.getValueRequirementMapping().get(req1);
    final ValueSpecification res2 = builder.getValueRequirementMapping().get(req2);
    assertEquals(res1.getProperty("ForwardCurve"), "BarForward");
    assertEquals(res1.getProperty("FundingCurve"), "BarFunding");
    assertEquals(res1.getProperty("Currency"), "USD");
    assertEquals(res2.getProperty("ForwardCurve"), "DefaultForward");
    assertEquals(res2.getProperty("FundingCurve"), "DefaultFunding");
    assertEquals(res2.getProperty("Currency"), "USD");
  }

  public void testTradeSpecificOverride() {
    final DependencyGraphBuilder builder = createBuilder();
    final ViewCalculationConfiguration config = builder.getCompilationContext().getViewCalculationConfiguration();
    final PositionSource positions = builder.getCompilationContext().getPortfolioStructure().getPositionSource();
    final Trade trade1 = getTrade(positions, "TradeAttr");
    config.setDefaultProperties(ValueProperties.with("TRADE.Present Value.DEFAULT_ForwardCurve", "GenericForward").with("TRADE.*.DEFAULT_FundingCurve", "GenericFunding")
        .with("TRADE.*.DEFAULT_ForwardCurve." + trade1.getUniqueId(), "BarForward").with("TRADE.Present Value.DEFAULT_FundingCurve." + trade1.getUniqueId(), "BarFunding").get());
    final ValueRequirement req1 = createValueRequirement(trade1, ValueProperties.none());
    final ValueRequirement req2 = createValueRequirement(getTrade(positions, "Trade"), ValueProperties.none());
    builder.addTarget(req1);
    builder.addTarget(req2);
    builder.getDependencyGraph();
    final ValueSpecification res1 = builder.getValueRequirementMapping().get(req1);
    final ValueSpecification res2 = builder.getValueRequirementMapping().get(req2);
    assertEquals(res1.getProperty("ForwardCurve"), "BarForward");
    assertEquals(res1.getProperty("FundingCurve"), "BarFunding");
    assertEquals(res1.getProperty("Currency"), "USD");
    assertEquals(res2.getProperty("ForwardCurve"), "GenericForward");
    assertEquals(res2.getProperty("FundingCurve"), "GenericFunding");
    assertEquals(res2.getProperty("Currency"), "USD");
  }

  public void testTradeAttribute() {
    final DependencyGraphBuilder builder = createBuilder();
    final PositionSource positions = builder.getCompilationContext().getPortfolioStructure().getPositionSource();
    final ValueRequirement req1 = createValueRequirement(getTrade(positions, "TradeAttr"), ValueProperties.none());
    builder.addTarget(req1);
    builder.getDependencyGraph();
    final ValueSpecification res1 = builder.getValueRequirementMapping().get(req1);
    assertEquals(res1.getProperty("ForwardCurve"), "FooForward");
    assertEquals(res1.getProperty("FundingCurve"), "FooFunding");
    assertEquals(res1.getProperty("Currency"), "USD");
  }

}
