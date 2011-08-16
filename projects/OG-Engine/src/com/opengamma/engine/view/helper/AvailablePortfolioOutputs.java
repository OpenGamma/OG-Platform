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
 * Implementation of {@link AvailableOutputs} that scans a function repository to give possible outputs available on a portfolio.
 * <p>
 * The named values aren't guaranteed to be computable for all nodes, positions or security types indicated as the full set of
 * functions may require underlying market data or other information that is not available or computable. Due to the black-box
 * nature of the function definitions additional values not mentioned may be available for a portfolio.
 * <p>
 * For more accurate details, refer to the documentation for the functions installed in the repository.
 */
public class AvailablePortfolioOutputs extends AvailableOutputsImpl {

  private static final Logger s_logger = LoggerFactory.getLogger(AvailablePortfolioOutputs.class);

  private final String _anyValue;

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
   * @param anyValue value to use when composing a wild-card with a finite set of property values, or {@code null} to not compose
   */
  public AvailablePortfolioOutputs(final Portfolio portfolio, final CompiledFunctionRepository functionRepository, final String anyValue) {
    ArgumentChecker.notNull(portfolio, "portfolio");
    ArgumentChecker.notNull(functionRepository, "functions");
    _anyValue = anyValue;
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
            final Set<ValueSpecification> results = function.getResults(_context, target, inputSet);
            if (results != null) {
              for (ValueSpecification result : results) {
                if ((resolvedOutputValue == result) || requiredOutputValue.isSatisfiedBy(result)) {
                  outputs.add(result);
                }
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
                    portfolioNodeOutput(resolvedItem.getValueName(), resolvedItem.getProperties());
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
                    positionOutput(resolvedItem.getValueName(), position.getSecurity().getSecurityType(), resolvedItem.getProperties());
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

  @Override
  protected AvailableOutputImpl createOutput(final String valueName) {
    return new AvailableOutputImpl(valueName, _anyValue);
  }

}
