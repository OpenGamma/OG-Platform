/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.helper;

import static org.testng.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.SimplePortfolio;
import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.core.position.impl.SimplePosition;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.impl.SimpleSecurityLink;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.CompiledFunctionRepository;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionDefinition;
import com.opengamma.engine.function.FunctionInvoker;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.function.InMemoryCompiledFunctionRepository;
import com.opengamma.engine.function.MarketDataAliasingFunction;
import com.opengamma.engine.function.MarketDataSourcingFunction;
import com.opengamma.engine.function.exclusion.AbstractFunctionExclusionGroups;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityFilter;
import com.opengamma.engine.marketdata.availability.OptimisticMarketDataAvailabilityFilter;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class AvailablePortfolioOutputsTest {

  private static final String SECURITY_TYPE_1 = "Bond";
  private static final String SECURITY_TYPE_2 = "Option";
  private static final String CURRENCY_1 = "USD";
  private static final String CURRENCY_2 = "GBP";
  private static final String VALUE_1 = "Foo";
  private static final String VALUE_2 = "Bar";
  private static final String FUNCTION_1_TYPE_1_POSITION = "T1-V1-P";
  private static final String FUNCTION_1_TYPE_1_SECURITY = "T1-V1-S";
  private static final String FUNCTION_2_TYPE_1_POSITION = "T1-V2-P";
  private static final String FUNCTION_TYPE_2_POSITION = "T2-P";
  private static final String FUNCTION_SUM_NODE = "VSUM-N";
  private static final String WILDCARD = "*";

  private Portfolio _testPortfolio;
  private CompiledFunctionRepository _functionRepository;
  private MarketDataAvailabilityFilter _marketDataAvailability;

  private SimplePosition createPosition(final String securityType, final String currency, final String securityId) {
    final SimplePosition position = new SimplePosition();
    position.setUniqueId(UniqueId.of("Position", securityType + "-" + currency + "-" + securityId));
    position.setQuantity(BigDecimal.ONE);
    final SimpleSecurityLink link = new SimpleSecurityLink();
    link.setTarget(new Security() {

      @Override
      public ExternalIdBundle getExternalIdBundle() {
        return ExternalIdBundle.EMPTY;
      }

      @Override
      public String getName() {
        return currency;
      }

      @Override
      public String getSecurityType() {
        return securityType;
      }

      @Override
      public UniqueId getUniqueId() {
        return UniqueId.of("Security", securityType + "-" + currency + "-" + securityId);
      }

      @Override
      public Map<String, String> getAttributes() {
        return new HashMap<String, String>();
      }

      @Override
      public void setAttributes(final Map<String, String> attributes) {
      }

      @Override
      public void addAttribute(final String key, final String value) {
      }
    });
    position.setSecurityLink(link);
    return position;
  }

  private SimplePortfolioNode createChildNode(final String securityType, final String currency) {
    final SimplePortfolioNode node = new SimplePortfolioNode(currency + "-" + securityType);
    node.setUniqueId(UniqueId.of("Node", securityType + "-" + currency));
    node.addPosition(createPosition(securityType, currency, "A"));
    node.addPosition(createPosition(securityType, currency, "B"));
    return node;
  }

  private SimplePortfolioNode createRootNode() {
    final SimplePortfolioNode node = new SimplePortfolioNode("Root");
    node.setUniqueId(UniqueId.of("Node", "0"));
    node.addChildNode(createChildNode(SECURITY_TYPE_1, CURRENCY_1));
    node.addChildNode(createChildNode(SECURITY_TYPE_2, CURRENCY_1));
    node.addChildNode(createChildNode(SECURITY_TYPE_1, CURRENCY_2));
    node.addChildNode(createChildNode(SECURITY_TYPE_2, CURRENCY_2));
    return node;
  }

  private Portfolio createPortfolio() {
    final SimplePortfolio portfolio = new SimplePortfolio("Test");
    portfolio.setRootNode(createRootNode());
    return portfolio;
  }

  private static abstract class MockFunction<T> implements CompiledFunctionDefinition {

    private final String _id;
    private final ComputationTargetType _targetType;

    protected MockFunction(final String id, final ComputationTargetType targetType) {
      _id = id;
      _targetType = targetType;
    }

    @Override
    public Set<ValueRequirement> getAdditionalRequirements(final FunctionCompilationContext context, final ComputationTarget target, final Set<ValueSpecification> inputs,
        final Set<ValueSpecification> outputs) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Instant getEarliestInvocationTime() {
      return null;
    }

    @Override
    public FunctionDefinition getFunctionDefinition() {
      return new FunctionDefinition() {

        @Override
        public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
          throw new UnsupportedOperationException();
        }

        @Override
        public FunctionParameters getDefaultParameters() {
          throw new UnsupportedOperationException();
        }

        @Override
        public String getShortName() {
          throw new UnsupportedOperationException();
        }

        @Override
        public String getUniqueId() {
          return _id;
        }

        @Override
        public void init(final FunctionCompilationContext context) {
          throw new UnsupportedOperationException();
        }
      };
    }

    @Override
    public FunctionInvoker getFunctionInvoker() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Instant getLatestInvocationTime() {
      return null;
    }

    @Override
    public boolean canHandleMissingRequirements() {
      return false;
    }

    protected ValueProperties.Builder properties() {
      return ValueProperties.with(ValuePropertyNames.FUNCTION, _id);
    }

    @Override
    public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
      return getResults(context, target);
    }

    @Override
    public ComputationTargetType getTargetType() {
      return _targetType;
    }

    protected abstract boolean canApplyTo(T target);

    @SuppressWarnings("unchecked")
    @Override
    public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
      return canApplyTo((T) target.getValue());
    }

    protected abstract Set<ValueSpecification> getResults(ComputationTargetSpecification targetSpec, T target);

    @SuppressWarnings("unchecked")
    @Override
    public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
      return getResults(target.toSpecification(), (T) target.getValue());
    }

    protected abstract Set<ValueRequirement> getRequirements(T target, ValueRequirement desiredResult);

    @SuppressWarnings("unchecked")
    @Override
    public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredResult) {
      return getRequirements((T) target.getValue(), desiredResult);
    }

  }

  private static abstract class MockPortfolioNodeFunction extends MockFunction<PortfolioNode> {

    public MockPortfolioNodeFunction(final String id) {
      super(id, ComputationTargetType.PORTFOLIO_NODE);
    }

  }

  private static abstract class MockPositionFunction extends MockFunction<Position> {

    public MockPositionFunction(final String id) {
      super(id, ComputationTargetType.POSITION);
    }

  }

  private static abstract class MockSecurityFunction extends MockFunction<Security> {

    public MockSecurityFunction(final String id) {
      super(id, ComputationTargetType.SECURITY);
    }

  }

  private CompiledFunctionRepository createFunctionRepository() {
    final InMemoryCompiledFunctionRepository repository = new InMemoryCompiledFunctionRepository(new FunctionCompilationContext());
    repository.addFunction(MarketDataSourcingFunction.INSTANCE);
    repository.addFunction(MarketDataAliasingFunction.INSTANCE);
    repository.addFunction(new MockSecurityFunction(FUNCTION_1_TYPE_1_SECURITY) {

      @Override
      protected boolean canApplyTo(final Security node) {
        return SECURITY_TYPE_1.equals(node.getSecurityType());
      }

      @Override
      protected Set<ValueRequirement> getRequirements(final Security node, final ValueRequirement desiredResult) {
        return Collections.emptySet();
      }

      @Override
      protected Set<ValueSpecification> getResults(final ComputationTargetSpecification targetSpec, final Security security) {
        return Collections.singleton(new ValueSpecification(VALUE_1, targetSpec, properties().with(ValuePropertyNames.CURRENCY, security.getName()).get()));
      }

    });
    repository.addFunction(new MockPositionFunction(FUNCTION_1_TYPE_1_POSITION) {

      @Override
      public boolean canApplyTo(final Position position) {
        // TODO: try with just "return true" and rely on the downstream function 
        return SECURITY_TYPE_1.equals(position.getSecurity().getSecurityType());
      }

      @Override
      public Set<ValueSpecification> getResults(final ComputationTargetSpecification targetSpec, final Position position) {
        return Collections.singleton(new ValueSpecification(VALUE_1, targetSpec, properties().with(ValuePropertyNames.CURRENCY, position.getSecurity().getName()).get()));
      }

      @Override
      public Set<ValueRequirement> getRequirements(final Position position, final ValueRequirement desiredValue) {
        return Collections.singleton(new ValueRequirement(VALUE_1, ComputationTargetSpecification.of(position.getSecurity())));
      }

    });
    repository.addFunction(new MockPositionFunction(FUNCTION_2_TYPE_1_POSITION) {

      @Override
      protected boolean canApplyTo(final Position position) {
        return SECURITY_TYPE_1.equals(position.getSecurity().getSecurityType());
      }

      @Override
      protected Set<ValueSpecification> getResults(final ComputationTargetSpecification targetSpec, final Position position) {
        return Collections.singleton(new ValueSpecification(VALUE_2, targetSpec, properties().with(ValuePropertyNames.CURRENCY, position.getSecurity().getName()).get()));
      }

      @Override
      public Set<ValueRequirement> getRequirements(final Position position, final ValueRequirement desiredValue) {
        return Collections.emptySet();
      }

    });
    repository.addFunction(new MockPositionFunction(FUNCTION_TYPE_2_POSITION) {

      @Override
      protected boolean canApplyTo(final Position position) {
        return SECURITY_TYPE_2.equals(position.getSecurity().getSecurityType());
      }

      @Override
      protected Set<ValueSpecification> getResults(final ComputationTargetSpecification targetSpec, final Position position) {
        final Set<ValueSpecification> result = new HashSet<ValueSpecification>();
        result.add(new ValueSpecification(VALUE_1, targetSpec, properties().with(ValuePropertyNames.CURRENCY, position.getSecurity().getName()).get()));
        result.add(new ValueSpecification(VALUE_2, targetSpec, properties().with(ValuePropertyNames.CURRENCY, position.getSecurity().getName()).get()));
        return result;
      }

      @Override
      public Set<ValueRequirement> getRequirements(final Position position, final ValueRequirement desiredValue) {
        return Collections.emptySet();
      }

    });
    repository.addFunction(new MockPortfolioNodeFunction(FUNCTION_SUM_NODE) {

      @Override
      protected boolean canApplyTo(final PortfolioNode node) {
        return true;
      }

      @Override
      protected Set<ValueSpecification> getResults(final ComputationTargetSpecification targetSpec, final PortfolioNode node) {
        final Set<ValueSpecification> result = new HashSet<ValueSpecification>();
        result.add(new ValueSpecification(VALUE_1, targetSpec, properties().withAny(ValuePropertyNames.CURRENCY).get()));
        result.add(new ValueSpecification(VALUE_2, targetSpec, properties().withAny(ValuePropertyNames.CURRENCY).get()));
        return result;
      }

      @Override
      public Set<ValueRequirement> getRequirements(final PortfolioNode node, final ValueRequirement desiredValue) {
        return Collections.emptySet();
      }

    });
    return repository;
  }

  @BeforeClass
  public void init() {
    _testPortfolio = createPortfolio();
    _functionRepository = createFunctionRepository();
    _marketDataAvailability = new OptimisticMarketDataAvailabilityFilter();
  }

  public void testGetSecurityTypes() {
    final AvailableOutputs outputs = new AvailablePortfolioOutputs(_testPortfolio, _functionRepository, null, _marketDataAvailability, WILDCARD);
    final Set<String> securityTypes = outputs.getSecurityTypes();
    assertEquals(securityTypes, new HashSet<String>(Arrays.asList(SECURITY_TYPE_1, SECURITY_TYPE_2)));
  }

  public void testGetTypedPositionOutputs() {
    final AvailableOutputs outputs = new AvailablePortfolioOutputs(_testPortfolio, _functionRepository, null, _marketDataAvailability, WILDCARD);
    Set<AvailableOutput> available = outputs.getPositionOutputs(SECURITY_TYPE_1);
    final AvailableOutputImpl value1Type1 = new AvailableOutputImpl(VALUE_1, WILDCARD);
    value1Type1.setPositionProperties(ValueProperties.with(ValuePropertyNames.FUNCTION, FUNCTION_1_TYPE_1_POSITION).with(ValuePropertyNames.CURRENCY, CURRENCY_1, CURRENCY_2).get(), SECURITY_TYPE_1);
    final AvailableOutputImpl value2Type1 = new AvailableOutputImpl(VALUE_2, WILDCARD);
    value2Type1.setPositionProperties(ValueProperties.with(ValuePropertyNames.FUNCTION, FUNCTION_2_TYPE_1_POSITION).with(ValuePropertyNames.CURRENCY, CURRENCY_1, CURRENCY_2).get(), SECURITY_TYPE_1);
    assertEquals(available, new HashSet<AvailableOutput>(Arrays.asList(value1Type1, value2Type1)));
    available = outputs.getPositionOutputs(SECURITY_TYPE_2);
    final AvailableOutputImpl value1Type2 = new AvailableOutputImpl(VALUE_1, WILDCARD);
    value1Type2.setPositionProperties(ValueProperties.with(ValuePropertyNames.FUNCTION, FUNCTION_TYPE_2_POSITION).with(ValuePropertyNames.CURRENCY, CURRENCY_1, CURRENCY_2).get(), SECURITY_TYPE_2);
    final AvailableOutputImpl value2Type2 = new AvailableOutputImpl(VALUE_2, WILDCARD);
    value2Type2.setPositionProperties(ValueProperties.with(ValuePropertyNames.FUNCTION, FUNCTION_TYPE_2_POSITION).with(ValuePropertyNames.CURRENCY, CURRENCY_1, CURRENCY_2).get(), SECURITY_TYPE_2);
    assertEquals(available, new HashSet<AvailableOutput>(Arrays.asList(value1Type2, value2Type2)));
  }

  public void testGetPortfolioNodeOutputs() {
    final AvailableOutputs outputs = new AvailablePortfolioOutputs(_testPortfolio, _functionRepository, null, _marketDataAvailability, WILDCARD);
    final Set<AvailableOutput> available = outputs.getPortfolioNodeOutputs();
    final AvailableOutputImpl value1 = new AvailableOutputImpl(VALUE_1, WILDCARD);
    value1.setPortfolioNodeProperties(ValueProperties.with(ValuePropertyNames.FUNCTION, FUNCTION_SUM_NODE).withAny(ValuePropertyNames.CURRENCY).get());
    final AvailableOutputImpl value2 = new AvailableOutputImpl(VALUE_2, WILDCARD);
    value2.setPortfolioNodeProperties(ValueProperties.with(ValuePropertyNames.FUNCTION, FUNCTION_SUM_NODE).withAny(ValuePropertyNames.CURRENCY).get());
    assertEquals(available, new HashSet<AvailableOutput>(Arrays.asList(value1, value2)));
  }

  public void testGetPositionOutputs() {
    final AvailableOutputs outputs = new AvailablePortfolioOutputs(_testPortfolio, _functionRepository, null, _marketDataAvailability, WILDCARD);
    final Set<AvailableOutput> available = outputs.getPositionOutputs();
    final AvailableOutputImpl value1 = new AvailableOutputImpl(VALUE_1, WILDCARD);
    value1.setPositionProperties(ValueProperties.with(ValuePropertyNames.FUNCTION, FUNCTION_1_TYPE_1_POSITION).with(ValuePropertyNames.CURRENCY, CURRENCY_1, CURRENCY_2).get(), SECURITY_TYPE_1);
    value1.setPositionProperties(ValueProperties.with(ValuePropertyNames.FUNCTION, FUNCTION_TYPE_2_POSITION).with(ValuePropertyNames.CURRENCY, CURRENCY_1, CURRENCY_2).get(), SECURITY_TYPE_2);
    final AvailableOutputImpl value2 = new AvailableOutputImpl(VALUE_2, WILDCARD);
    value2.setPositionProperties(ValueProperties.with(ValuePropertyNames.FUNCTION, FUNCTION_2_TYPE_1_POSITION).with(ValuePropertyNames.CURRENCY, CURRENCY_1, CURRENCY_2).get(), SECURITY_TYPE_1);
    value2.setPositionProperties(ValueProperties.with(ValuePropertyNames.FUNCTION, FUNCTION_TYPE_2_POSITION).with(ValuePropertyNames.CURRENCY, CURRENCY_1, CURRENCY_2).get(), SECURITY_TYPE_2);
    assertEquals(available, new HashSet<AvailableOutput>(Arrays.asList(value1, value2)));
  }

  public void testGetOutputs() {
    final AvailableOutputs outputs = new AvailablePortfolioOutputs(_testPortfolio, _functionRepository, null, _marketDataAvailability, WILDCARD);
    final Set<AvailableOutput> available = outputs.getOutputs();
    final AvailableOutputImpl value1 = new AvailableOutputImpl(VALUE_1, WILDCARD);
    value1.setPositionProperties(ValueProperties.with(ValuePropertyNames.FUNCTION, FUNCTION_1_TYPE_1_POSITION).with(ValuePropertyNames.CURRENCY, CURRENCY_1, CURRENCY_2).get(), SECURITY_TYPE_1);
    value1.setPositionProperties(ValueProperties.with(ValuePropertyNames.FUNCTION, FUNCTION_TYPE_2_POSITION).with(ValuePropertyNames.CURRENCY, CURRENCY_1, CURRENCY_2).get(), SECURITY_TYPE_2);
    value1.setPortfolioNodeProperties(ValueProperties.with(ValuePropertyNames.FUNCTION, FUNCTION_SUM_NODE).withAny(ValuePropertyNames.CURRENCY).get());
    final AvailableOutputImpl value2 = new AvailableOutputImpl(VALUE_2, WILDCARD);
    value2.setPositionProperties(ValueProperties.with(ValuePropertyNames.FUNCTION, FUNCTION_2_TYPE_1_POSITION).with(ValuePropertyNames.CURRENCY, CURRENCY_1, CURRENCY_2).get(), SECURITY_TYPE_1);
    value2.setPositionProperties(ValueProperties.with(ValuePropertyNames.FUNCTION, FUNCTION_TYPE_2_POSITION).with(ValuePropertyNames.CURRENCY, CURRENCY_1, CURRENCY_2).get(), SECURITY_TYPE_2);
    value2.setPortfolioNodeProperties(ValueProperties.with(ValuePropertyNames.FUNCTION, FUNCTION_SUM_NODE).withAny(ValuePropertyNames.CURRENCY).get());
    assertEquals(available, new HashSet<AvailableOutput>(Arrays.asList(value1, value2)));
  }

  public void testWithNoExclusion() {
    final AvailableOutputs outputs = new AvailablePortfolioOutputs(_testPortfolio, _functionRepository, new AbstractFunctionExclusionGroups() {
      @Override
      protected String getKey(final FunctionDefinition function) {
        return null;
      }
    }, _marketDataAvailability, WILDCARD);
    final Set<AvailableOutput> available = outputs.getOutputs();
    final AvailableOutputImpl value1 = new AvailableOutputImpl(VALUE_1, WILDCARD);
    value1.setPositionProperties(ValueProperties.with(ValuePropertyNames.FUNCTION, FUNCTION_1_TYPE_1_POSITION).with(ValuePropertyNames.CURRENCY, CURRENCY_1, CURRENCY_2).get(), SECURITY_TYPE_1);
    value1.setPositionProperties(ValueProperties.with(ValuePropertyNames.FUNCTION, FUNCTION_TYPE_2_POSITION).with(ValuePropertyNames.CURRENCY, CURRENCY_1, CURRENCY_2).get(), SECURITY_TYPE_2);
    value1.setPortfolioNodeProperties(ValueProperties.with(ValuePropertyNames.FUNCTION, FUNCTION_SUM_NODE).withAny(ValuePropertyNames.CURRENCY).get());
    final AvailableOutputImpl value2 = new AvailableOutputImpl(VALUE_2, WILDCARD);
    value2.setPositionProperties(ValueProperties.with(ValuePropertyNames.FUNCTION, FUNCTION_2_TYPE_1_POSITION).with(ValuePropertyNames.CURRENCY, CURRENCY_1, CURRENCY_2).get(), SECURITY_TYPE_1);
    value2.setPositionProperties(ValueProperties.with(ValuePropertyNames.FUNCTION, FUNCTION_TYPE_2_POSITION).with(ValuePropertyNames.CURRENCY, CURRENCY_1, CURRENCY_2).get(), SECURITY_TYPE_2);
    value2.setPortfolioNodeProperties(ValueProperties.with(ValuePropertyNames.FUNCTION, FUNCTION_SUM_NODE).withAny(ValuePropertyNames.CURRENCY).get());
    assertEquals(available, new HashSet<AvailableOutput>(Arrays.asList(value1, value2)));
  }

  public void testWithExclusion() {
    final AvailableOutputs outputs = new AvailablePortfolioOutputs(_testPortfolio, _functionRepository, new AbstractFunctionExclusionGroups() {
      @Override
      protected String getKey(final FunctionDefinition function) {
        if (FUNCTION_1_TYPE_1_POSITION.equals(function.getUniqueId()) || FUNCTION_1_TYPE_1_SECURITY.equals(function.getUniqueId())) {
          return "group";
        } else {
          return null;
        }
      }
    }, _marketDataAvailability, WILDCARD);
    final Set<AvailableOutput> available = outputs.getOutputs();
    final AvailableOutputImpl value1 = new AvailableOutputImpl(VALUE_1, WILDCARD);
    value1.setPositionProperties(ValueProperties.with(ValuePropertyNames.FUNCTION, FUNCTION_TYPE_2_POSITION).with(ValuePropertyNames.CURRENCY, CURRENCY_1, CURRENCY_2).get(), SECURITY_TYPE_2);
    value1.setPortfolioNodeProperties(ValueProperties.with(ValuePropertyNames.FUNCTION, FUNCTION_SUM_NODE).withAny(ValuePropertyNames.CURRENCY).get());
    final AvailableOutputImpl value2 = new AvailableOutputImpl(VALUE_2, WILDCARD);
    value2.setPositionProperties(ValueProperties.with(ValuePropertyNames.FUNCTION, FUNCTION_2_TYPE_1_POSITION).with(ValuePropertyNames.CURRENCY, CURRENCY_1, CURRENCY_2).get(), SECURITY_TYPE_1);
    value2.setPositionProperties(ValueProperties.with(ValuePropertyNames.FUNCTION, FUNCTION_TYPE_2_POSITION).with(ValuePropertyNames.CURRENCY, CURRENCY_1, CURRENCY_2).get(), SECURITY_TYPE_2);
    value2.setPortfolioNodeProperties(ValueProperties.with(ValuePropertyNames.FUNCTION, FUNCTION_SUM_NODE).withAny(ValuePropertyNames.CURRENCY).get());
    assertEquals(available, new HashSet<AvailableOutput>(Arrays.asList(value1, value2)));
  }

}
