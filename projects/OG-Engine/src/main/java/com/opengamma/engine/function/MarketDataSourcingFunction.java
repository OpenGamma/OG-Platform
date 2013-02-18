/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import java.util.Collections;
import java.util.Set;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.target.ComputationTargetReferenceVisitor;
import com.opengamma.engine.target.ComputationTargetRequirement;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * Special case of function implementation that is never executed by the graph executor but is used to source market data. It will not be considered directly during graph construction; the singleton
 * instance is associated with DependencyNode objects to act as a marker on the node.
 */
public final class MarketDataSourcingFunction extends AbstractFunction.NonCompiledInvoker implements ComputationTargetReferenceVisitor<FunctionParameters> {

  /**
   * Function input parameters; defining which externally sourced market data is to be introduced by this function. These inputs allow a full external identifier bundle to be referenced; the value
   * specification that the market data must be added to the cache under will be identified from the dependency graph structure.
   */
  public static final class Inputs implements FunctionParameters {

    private static final long serialVersionUID = 1L;

    private final ExternalIdBundle _identifiers;

    private Inputs(final ExternalIdBundle identifiers) {
      ArgumentChecker.notNull(identifiers, "identifiers");
      _identifiers = identifiers;
    }

    private ExternalIdBundle getIdentifiers() {
      return _identifiers;
    }

    public FudgeMsg toFudgeMsg(final FudgeSerializer serializer) {
      final MutableFudgeMsg msg = serializer.newMessage();
      serializer.addToMessage(msg, "identifiers", null, getIdentifiers());
      return msg;
    }

    public static Inputs fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg) {
      return new Inputs(deserializer.fieldValueToObject(ExternalIdBundle.class, msg.getByName("identifiers")));
    }

    @Override
    public int hashCode() {
      return getIdentifiers().hashCode();
    }

    @Override
    public boolean equals(final Object o) {
      if (o == this) {
        return true;
      }
      if (!(o instanceof Inputs)) {
        return false;
      }
      return getIdentifiers().equals(((Inputs) o).getIdentifiers());
    }

  }

  /**
   * Singleton instance.
   */
  public static final MarketDataSourcingFunction INSTANCE = new MarketDataSourcingFunction();

  /**
   * Function unique ID
   */
  public static final String UNIQUE_ID = "MarketDataSourcingFunction";

  private MarketDataSourcingFunction() {
    setUniqueId(UNIQUE_ID);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    return null;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return false;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    return null;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.emptySet();
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PRIMITIVE;
  }

  @Override
  public FunctionParameters visitComputationTargetRequirement(final ComputationTargetRequirement reference) {
    return new Inputs(reference.getIdentifiers());
  }

  @Override
  public FunctionParameters visitComputationTargetSpecification(final ComputationTargetSpecification specification) {
    return getDefaultParameters();
  }

  public FunctionParameters getParameters(final ValueRequirement valueRequirement) {
    return valueRequirement.getTargetReference().accept(this);
  }

  public ValueRequirement getMarketDataRequirement(final FunctionParameters parameters, final ValueSpecification desiredValue) {
    final ComputationTargetReference target;
    if (parameters instanceof Inputs) {
      target = new ComputationTargetRequirement(desiredValue.getTargetSpecification().getType(), ((Inputs) parameters).getIdentifiers());
    } else {
      target = desiredValue.getTargetSpecification();
    }
    return new ValueRequirement(desiredValue.getValueName(), target);
  }

}
