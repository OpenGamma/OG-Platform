/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.enginedebugger;

import java.util.Map;
import java.util.Set;

import com.opengamma.engine.depgraph.ResolutionFailure;
import com.opengamma.engine.depgraph.ResolutionFailureImpl;
import com.opengamma.engine.depgraph.ResolutionFailureVisitor;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.integration.tool.enginedebugger.node.CouldNotResolveNode;
import com.opengamma.integration.tool.enginedebugger.node.FailedFunctionNode;
import com.opengamma.integration.tool.enginedebugger.node.GetAdditionalRequirementsFailedNode;
import com.opengamma.integration.tool.enginedebugger.node.GetRequirementsFailedNode;
import com.opengamma.integration.tool.enginedebugger.node.GetResultsFailedNode;
import com.opengamma.integration.tool.enginedebugger.node.LateResolutionFailureNode;
import com.opengamma.integration.tool.enginedebugger.node.MarketDataMissingNode;
import com.opengamma.integration.tool.enginedebugger.node.NoFunctionNode;
import com.opengamma.integration.tool.enginedebugger.node.RecursiveRequirementNode;
import com.opengamma.integration.tool.enginedebugger.node.SuccessfulFunctionNode;
import com.opengamma.integration.tool.enginedebugger.node.UnsatisfiedNode;

/**
 * 
 */
public final class ResolutionFailureChildNodeCreatingVisitor extends ResolutionFailureVisitor<Object> {
    
  private ResolutionFailureImpl _parent;

  public ResolutionFailureChildNodeCreatingVisitor(ResolutionFailureImpl parent) {
    _parent = parent;
  }
  
  @Override
  protected Object visitCouldNotResolve(final ValueRequirement valueRequirement) {
    return new CouldNotResolveNode(_parent, valueRequirement);
  }

  @Override
  protected Object visitNoFunctions(final ValueRequirement valueRequirement) {
    return new NoFunctionNode(_parent, valueRequirement);
  }

  @Override
  protected Object visitRecursiveRequirement(final ValueRequirement valueRequirement) {
    return new RecursiveRequirementNode(_parent, valueRequirement);
  }

  @Override
  protected Object visitUnsatisfied(final ValueRequirement valueRequirement) {
    return new UnsatisfiedNode(_parent, valueRequirement);
  }

  @Override
  protected Object visitMarketDataMissing(final ValueRequirement valueRequirement) {
    return new MarketDataMissingNode(_parent, valueRequirement);
  }

  @Override
  protected Object visitSuccessfulFunction(final ValueRequirement valueRequirement, final String function, final ValueSpecification desiredOutput,
      final Map<ValueSpecification, ValueRequirement> satisfied) {
    return new SuccessfulFunctionNode(_parent, valueRequirement, function, desiredOutput, satisfied);
  }

  @Override
  protected Object visitFailedFunction(final ValueRequirement valueRequirement, final String function, final ValueSpecification desiredOutput,
      final Map<ValueSpecification, ValueRequirement> satisfied, final Set<ResolutionFailure> unsatisfied) {
    return new FailedFunctionNode(_parent, valueRequirement, function, desiredOutput, satisfied, unsatisfied);
  }

  @Override
  protected Object visitGetAdditionalRequirementsFailed(final ValueRequirement valueRequirement, final String function, final ValueSpecification desiredOutput,
      final Map<ValueSpecification, ValueRequirement> requirements) {
    return new GetAdditionalRequirementsFailedNode(_parent, valueRequirement, function, desiredOutput, requirements);  
  }

  @Override
  protected Object visitGetResultsFailed(final ValueRequirement valueRequirement, final String function, final ValueSpecification desiredOutput,
      final Map<ValueSpecification, ValueRequirement> requirements) {
    return new GetResultsFailedNode(_parent, valueRequirement, function, desiredOutput, requirements);
  }

  @Override
  protected Object visitGetRequirementsFailed(final ValueRequirement valueRequirement, final String function, final ValueSpecification desiredOutput) {
    return new GetRequirementsFailedNode(_parent, valueRequirement, function, desiredOutput);
  }

  @Override
  protected Object visitLateResolutionFailure(final ValueRequirement valueRequirement, final String function, final ValueSpecification desiredOutput,
      final Map<ValueSpecification, ValueRequirement> requirements) {
    return new LateResolutionFailureNode(_parent, valueRequirement, function, desiredOutput, requirements);
  }

  @Override
  protected Object visitBlacklistSuppressed(final ValueRequirement valueRequirement, final String function, final ValueSpecification desiredOutput,
      final Map<ValueSpecification, ValueRequirement> requirements) {
    return new BlacklistSuppressedNode(_parent, valueRequirement, function, desiredOutput, requirements);
  }

}
