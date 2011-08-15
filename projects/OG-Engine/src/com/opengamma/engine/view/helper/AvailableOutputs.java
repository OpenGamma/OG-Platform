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

      private boolean canSatisfy(final Set<CompiledFunctionDefinition> visited, final ComputationTarget target, final ValueRequirement requirement) {
        for (CompiledFunctionDefinition function : functions) {
          if (visited.add(function)) {
            try {
              if ((function.getTargetType() == target.getType()) && function.canApplyTo(_context, target)) {
                final Set<ValueSpecification> results = function.getResults(_context, target);
                for (ValueSpecification result : results) {
                  if (requirement.isSatisfiedBy(result)) {
                    if (canSatisfyRequirementsFor(visited, function, target, requirement)) {
                      return true;
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
        return false;
      }

      private boolean canSatisfyRequirementsFor(final Set<CompiledFunctionDefinition> visited, final CompiledFunctionDefinition function,
          final ComputationTarget target, final ValueRequirement result) {
        final Set<ValueRequirement> requirements = function.getRequirements(_context, target, result);
        if (requirements == null) {
          return false;
        }
        if (requirements.isEmpty()) {
          return true;
        }
        for (ValueRequirement requirement : requirements) {
          final ComputationTargetSpecification targetSpec = requirement.getTargetSpecification();
          if (targetSpec.getUniqueId() != null) {
            final Object requirementTarget = targetCache.get(targetSpec.getUniqueId());
            if (requirementTarget != null) {
              final Set<CompiledFunctionDefinition> visitedCopy = new HashSet<CompiledFunctionDefinition>();
              if (visited != null) {
                visitedCopy.addAll(visited);
              }
              if (!canSatisfy(visitedCopy, new ComputationTarget(requirementTarget), requirement)) {
                s_logger.debug("Can't satisfy {} for function {}", requirement, function);
                return false;
              }
            } else {
              s_logger.debug("No target cached for {}, assuming ok", targetSpec);
            }
          }
        }
        return true;
      }

      @Override
      public void preOrderOperation(final PortfolioNode portfolioNode) {
        final ComputationTarget target = new ComputationTarget(ComputationTargetType.PORTFOLIO_NODE, portfolioNode);
        for (CompiledFunctionDefinition function : functions) {
          try {
            if ((function.getTargetType() == ComputationTargetType.PORTFOLIO_NODE) && function.canApplyTo(_context, target)) {
              final Set<ValueSpecification> results = function.getResults(_context, target);
              for (ValueSpecification result : results) {
                if (canSatisfyRequirementsFor(null, function, target, new ValueRequirement(result.getValueName(), result.getTargetSpecification()))) {
                  portfolioNodeOutput(result);
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
                if (canSatisfyRequirementsFor(null, function, target, new ValueRequirement(result.getValueName(), result.getTargetSpecification()))) {
                  positionOutput(result, position.getSecurity().getSecurityType());
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
