/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.AbstractPortfolioNodeTraversalCallback;
import com.opengamma.core.position.impl.PortfolioNodeTraverser;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.CompiledFunctionRepository;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 * Helper class to identify some of the possible portfolio outputs when constructing a view definition. 
 */
public class AvailableOutputs {

  private static final Logger s_logger = LoggerFactory.getLogger(AvailableOutputs.class);

  private final Set<String> _securityTypes = new HashSet<String>();
  private final Map<String, AvailableOutput> _outputs = new HashMap<String, AvailableOutput>();

  // Hack until the dummy functions get removed
  private static Collection<CompiledFunctionDefinition> removeDummyFunctions(final Collection<CompiledFunctionDefinition> functions) {
    final Collection<CompiledFunctionDefinition> result = new ArrayList<CompiledFunctionDefinition>(functions.size());
    for (CompiledFunctionDefinition function : functions) {
      if (function.getClass().getSimpleName().startsWith("Dummy")) {
        continue;
      }
      result.add(function);
    }
    return result;
  }

  private static final class SingleItem<T> implements Iterator<T> {

    private T _item;

    public SingleItem(final T item) {
      _item = item;
    }

    @Override
    public boolean hasNext() {
      return _item != null;
    }

    @Override
    public T next() {
      final T item = _item;
      _item = null;
      return item;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }

  }

  /**
   * Constructs a new output set.
   * 
   * @param portfolio the portfolio (must be resolved), not {@code null}
   * @param functionRepository the functions, not {@code null}
   */
  public AvailableOutputs(final Portfolio portfolio, final CompiledFunctionRepository functionRepository) {
    ArgumentChecker.notNull(portfolio, "portfolio");
    ArgumentChecker.notNull(functionRepository, "functions");
    final Collection<CompiledFunctionDefinition> functions = removeDummyFunctions(functionRepository.getAllFunctions());
    final Map<UniqueId, Object> targetCache = new HashMap<UniqueId, Object>();
    PortfolioNodeTraverser.depthFirst(new AbstractPortfolioNodeTraversalCallback() {

      @Override
      public void preOrderOperation(final PortfolioNode portfolioNode) {
        targetCache.put(portfolioNode.getUniqueId(), portfolioNode);
      }

      @Override
      public void preOrderOperation(final Position position) {
        targetCache.put(position.getUniqueId(), position);
        targetCache.put(position.getSecurity().getUniqueId(), position.getSecurity());
        for (Trade trade : position.getTrades()) {
          targetCache.put(trade.getUniqueId(), trade);
          targetCache.put(trade.getSecurity().getUniqueId(), trade.getSecurity());
        }
      }

    }).traverse(portfolio.getRootNode());
    PortfolioNodeTraverser.depthFirst(new AbstractPortfolioNodeTraversalCallback() {

      private final FunctionCompilationContext _context = functionRepository.getCompilationContext();

      private Set<ValueSpecification> satisfyRequirement(final Set<CompiledFunctionDefinition> visited, final ComputationTarget target, final ValueRequirement requirement) {
        Set<ValueSpecification> allResults = null;
        for (CompiledFunctionDefinition function : functions) {
          if (visited.add(function)) {
            try {
              if ((function.getTargetType() == target.getType()) && function.canApplyTo(_context, target)) {
                final Set<ValueSpecification> results = function.getResults(_context, target);
                for (ValueSpecification result : results) {
                  if (requirement.isSatisfiedBy(result)) {
                    final Set<ValueSpecification> resolved = resultWithSatisfiedRequirements(visited, function, target, requirement, result.compose(requirement));
                    if (resolved != null) {
                      if (allResults == null) {
                        allResults = new HashSet<ValueSpecification>();
                      }
                      allResults.addAll(resolved);
                    }
                  }
                }
              }
            } catch (Throwable t) {
              s_logger.error("Error applying {} to {}", function, target);
              s_logger.warn("Exception thrown", t);
            }
          }
        }
        return allResults;
      }

      private Set<ValueSpecification> resultWithSatisfiedRequirements(final Set<CompiledFunctionDefinition> visited, final CompiledFunctionDefinition function,
          final ComputationTarget target, final ValueRequirement requiredOutputValue, final ValueSpecification resolvedOutputValue) {
        final Set<ValueRequirement> requirements = function.getRequirements(_context, target, requiredOutputValue);
        if (requirements == null) {
          return null;
        }
        if (requirements.isEmpty()) {
          return Collections.singleton(resolvedOutputValue);
        }
        final Map<Iterator<ValueSpecification>, ValueRequirement> inputs = new HashMap<Iterator<ValueSpecification>, ValueRequirement>();
        for (ValueRequirement requirement : requirements) {
          final ComputationTargetSpecification targetSpec = requirement.getTargetSpecification();
          if (targetSpec.getUniqueId() != null) {
            final Object requirementTarget = targetCache.get(targetSpec.getUniqueId());
            if (requirementTarget != null) {
              final Set<CompiledFunctionDefinition> visitedCopy = new HashSet<CompiledFunctionDefinition>();
              if (visited != null) {
                visitedCopy.addAll(visited);
              }
              final Set<ValueSpecification> satisfied = satisfyRequirement(visitedCopy, new ComputationTarget(requirementTarget), requirement);
              if (satisfied == null) {
                s_logger.debug("Can't satisfy {} for function {}", requirement, function);
                return null;
              }
              inputs.put(satisfied.iterator(), requirement);
            } else {
              s_logger.debug("No target cached for {}, assuming ok", targetSpec);
              inputs.put(new SingleItem<ValueSpecification>(new ValueSpecification(requirement, "")), requirement);
            }
          } else {
            s_logger.debug("No unique ID for {}, assuming ok", targetSpec);
            inputs.put(new SingleItem<ValueSpecification>(new ValueSpecification(requirement, "")), requirement);
          }
        }
        final Set<ValueSpecification> outputs = new HashSet<ValueSpecification>();
        Map<ValueSpecification, ValueRequirement> inputSet = new HashMap<ValueSpecification, ValueRequirement>();
        do {
          for (Map.Entry<Iterator<ValueSpecification>, ValueRequirement> input : inputs.entrySet()) {
            if (!input.getKey().hasNext()) {
              inputSet.clear();
              break;
            }
            inputSet.put(input.getKey().next(), input.getValue());
          }
          if (inputSet.isEmpty()) {
            break;
          } else {
            for (ValueSpecification result : function.getResults(_context, target, inputSet)) {
              if ((resolvedOutputValue == result) || requiredOutputValue.isSatisfiedBy(result)) {
                outputs.add(result);
              }
            }
            inputSet.clear();
          }
        } while (true);
        if (outputs.isEmpty()) {
          s_logger.debug("Provisional result {} not in results after late resolution", resolvedOutputValue);
          return null;
        } else {
          return outputs;
        }
      }

      @Override
      public void preOrderOperation(final PortfolioNode portfolioNode) {
        final ComputationTarget target = new ComputationTarget(ComputationTargetType.PORTFOLIO_NODE, portfolioNode);
        for (CompiledFunctionDefinition function : functions) {
          try {
            if ((function.getTargetType() == ComputationTargetType.PORTFOLIO_NODE) && function.canApplyTo(_context, target)) {
              final Set<ValueSpecification> results = function.getResults(_context, target);
              for (ValueSpecification result : results) {
                final Set<ValueSpecification> resolved = resultWithSatisfiedRequirements(null, function, target, new ValueRequirement(result.getValueName(), result.getTargetSpecification()), result);
                if (resolved != null) {
                  for (ValueSpecification resolvedItem : resolved) {
                    portfolioNodeOutput(resolvedItem);
                  }
                }
              }
            }
          } catch (Throwable t) {
            s_logger.error("Error applying {} to {}", function, target);
            s_logger.warn("Exception thrown", t);
          }
        }
      }

      @Override
      public void preOrderOperation(final Position position) {
        final ComputationTarget target = new ComputationTarget(ComputationTargetType.POSITION, position);
        for (CompiledFunctionDefinition function : functions) {
          try {
            if ((function.getTargetType() == ComputationTargetType.POSITION) && function.canApplyTo(_context, target)) {
              final Set<ValueSpecification> results = function.getResults(_context, target);
              for (ValueSpecification result : results) {
                final Set<ValueSpecification> resolved = resultWithSatisfiedRequirements(null, function, target, new ValueRequirement(result.getValueName(), result.getTargetSpecification()), result);
                if (resolved != null) {
                  for (ValueSpecification resolvedItem : resolved) {
                    positionOutput(resolvedItem, position.getSecurity().getSecurityType());
                  }
                }
              }
            }
          } catch (Throwable t) {
            s_logger.error("Error applying {} to {}", function, target);
            s_logger.warn("Exception thrown", t);
          }
        }
      }

    }).traverse(portfolio.getRootNode());
  }

  private AvailableOutput getOrCreateOutput(final String valueName) {
    AvailableOutput output = _outputs.get(valueName);
    if (output == null) {
      output = new AvailableOutput(valueName);
      _outputs.put(valueName, output);
    }
    return output;
  }

  private void portfolioNodeOutput(final ValueSpecification outputSpec) {
    final AvailableOutput output = getOrCreateOutput(outputSpec.getValueName());
    output.setPortfolioNodeProperties(outputSpec.getProperties());
  }

  private void positionOutput(final ValueSpecification outputSpec, final String securityType) {
    final AvailableOutput output = getOrCreateOutput(outputSpec.getValueName());
    _securityTypes.add(securityType);
    output.setPositionProperties(outputSpec.getProperties(), securityType);
  }

  /**
   * Returns the set of security types defined within the portfolio.
   * 
   * @return the set of security types
   */
  public Set<String> getSecurityTypes() {
    return Collections.unmodifiableSet(_securityTypes);
  }

  /**
   * Returns a set of outputs that can be asked of positions on the given security type.
   * 
   * @param securityType security type
   * @return set of outputs, not {@code null}
   */
  public Set<AvailableOutput> getPositionOutputs(final String securityType) {
    final Set<AvailableOutput> result = new HashSet<AvailableOutput>();
    for (AvailableOutput output : _outputs.values()) {
      if (output.isAvailableOn(securityType)) {
        result.add(AvailableOutput.ofPosition(output, securityType));
      }
    }
    return result;
  }

  /**
   * Returns a set of outputs that can be asked of portfolio nodes.
   * 
   * @return set of outputs, not {@code null}
   */
  public Set<AvailableOutput> getPortfolioNodeOutputs() {
    final Set<AvailableOutput> result = new HashSet<AvailableOutput>();
    for (AvailableOutput output : _outputs.values()) {
      if (output.isAvailableOnPortfolioNode()) {
        result.add(AvailableOutput.ofPortfolioNode(output));
      }
    }
    return result;
  }

  /**
   * Returns a set of outputs that can be asked of position nodes.
   * 
   * @return set of outputs, not {@code null}
   */
  public Set<AvailableOutput> getPositionOutputs() {
    final Set<AvailableOutput> result = new HashSet<AvailableOutput>();
    for (AvailableOutput output : _outputs.values()) {
      if (output.isAvailableOnPosition()) {
        result.add(AvailableOutput.ofPosition(output));
      }
    }
    return result;
  }

  /**
   * Returns a set of outputs that can be asked of portfolio or position nodes.
   * 
   * @return set of outputs, not {@code null}
   */
  public Set<AvailableOutput> getOutputs() {
    return new HashSet<AvailableOutput>(_outputs.values());
  }

}
