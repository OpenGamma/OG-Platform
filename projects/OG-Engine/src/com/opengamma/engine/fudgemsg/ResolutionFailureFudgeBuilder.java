/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.engine.depgraph.ResolutionFailure;
import com.opengamma.engine.depgraph.ResolutionFailureVisitor;
import com.opengamma.engine.function.ParameterizedFunction;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Fudge message builder for {@code ResolutionFailure}.
 */
@FudgeBuilderFor(ResolutionFailure.class)
public class ResolutionFailureFudgeBuilder implements FudgeBuilder<ResolutionFailure> {

  private static final String ERROR_KEY = "Error";
  private static final String VALUE_REQUIREMENT_KEY = "ValueRequirement";
  private static final String FUNCTION_KEY = "Function";
  private static final String PARAMETERS_KEY = "Parameters";
  private static final String DESIRED_OUTPUT_KEY = "DesiredOutput";
  private static final String SATISFIED_KEY = "Satisfied";
  private static final String UNSATISFIED_KEY = "Unsatisfied";
  private static final String ADDITIONAL_REQUIREMENT_KEY = "AdditionalRequirement";
  private static final String VALUE_SPECIFICATION_KEY = "ValueSpecification";

  /**
   * Visitor that produces Fudge messages from {@link ResolutionFailure} objects.
   */
  public static final class Visitor extends ResolutionFailureVisitor<MutableFudgeMsg> {

    private final FudgeSerializer _serializer;

    public Visitor(final FudgeSerializer serializer) {
      _serializer = serializer;
    }

    private MutableFudgeMsg message(final ResolutionFailure.Status error, final ValueRequirement valueRequirement) {
      final MutableFudgeMsg msg = _serializer.newMessage();
      if (error != null) {
        _serializer.addToMessage(msg, ERROR_KEY, null, error.name());
      }
      _serializer.addToMessage(msg, VALUE_REQUIREMENT_KEY, null, valueRequirement);
      return msg;
    }

    protected MutableFudgeMsg visitCouldNotResolve(final ValueRequirement valueRequirement) {
      return message(ResolutionFailure.Status.COULD_NOT_RESOLVE, valueRequirement);
    }

    protected MutableFudgeMsg visitNoFunctions(final ValueRequirement valueRequirement) {
      return message(ResolutionFailure.Status.NO_FUNCTIONS, valueRequirement);
    }

    protected MutableFudgeMsg visitRecursiveRequirement(final ValueRequirement valueRequirement) {
      return message(ResolutionFailure.Status.RECURSIVE_REQUIREMENT, valueRequirement);
    }

    protected MutableFudgeMsg visitUnsatisfied(final ValueRequirement valueRequirement) {
      return message(ResolutionFailure.Status.UNSATISFIED, valueRequirement);
    }

    protected MutableFudgeMsg visitMarketDataMissing(final ValueRequirement valueRequirement) {
      return message(ResolutionFailure.Status.MARKET_DATA_MISSING, valueRequirement);
    }

    protected MutableFudgeMsg visitFunction(final ValueRequirement valueRequirement, final ParameterizedFunction function, final ValueSpecification desiredOutput,
        final Map<ValueSpecification, ValueRequirement> satisfied, final Set<ResolutionFailure> unsatisfied, final Set<ResolutionFailure> unsatisfiedAdditional) {
      final MutableFudgeMsg msg = message(null, valueRequirement);
      _serializer.addToMessage(msg, FUNCTION_KEY, null, function.getFunction().getFunctionDefinition().getUniqueId());
      _serializer.addToMessage(msg, PARAMETERS_KEY, null, function.getParameters());
      _serializer.addToMessage(msg, DESIRED_OUTPUT_KEY, null, desiredOutput);
      for (Map.Entry<ValueSpecification, ValueRequirement> input : satisfied.entrySet()) {
        final MutableFudgeMsg submessage = _serializer.newMessage();
        _serializer.addToMessage(submessage, VALUE_SPECIFICATION_KEY, null, input.getKey());
        _serializer.addToMessage(submessage, VALUE_REQUIREMENT_KEY, null, input.getValue());
        msg.add(SATISFIED_KEY, null, submessage);
      }
      for (ResolutionFailure failure : unsatisfied) {
        for (MutableFudgeMsg submessage : failure.accept(this)) {
          msg.add(UNSATISFIED_KEY, null, submessage);
        }
      }
      for (ResolutionFailure failure : unsatisfiedAdditional) {
        for (MutableFudgeMsg submessage : failure.accept(this)) {
          msg.add(ADDITIONAL_REQUIREMENT_KEY, null, submessage);
        }
      }
      return msg;
    }

    protected MutableFudgeMsg visitGetAdditionalRequirementsFailed(final ValueRequirement valueRequirement, final ParameterizedFunction function, final ValueSpecification desiredOutput,
        final Map<ValueSpecification, ValueRequirement> requirements) {
      final MutableFudgeMsg msg = message(ResolutionFailure.Status.ADDITIONAL_REQUIREMENT, valueRequirement);
      _serializer.addToMessage(msg, FUNCTION_KEY, null, function.getFunction().getFunctionDefinition().getUniqueId());
      _serializer.addToMessage(msg, PARAMETERS_KEY, null, function.getParameters());
      _serializer.addToMessage(msg, DESIRED_OUTPUT_KEY, null, desiredOutput);
      for (Map.Entry<ValueSpecification, ValueRequirement> input : requirements.entrySet()) {
        final MutableFudgeMsg submessage = _serializer.newMessage();
        _serializer.addToMessage(submessage, VALUE_SPECIFICATION_KEY, null, input.getKey());
        _serializer.addToMessage(submessage, VALUE_REQUIREMENT_KEY, null, input.getValue());
        msg.add(SATISFIED_KEY, null, submessage);
      }
      return msg;
    }

    protected MutableFudgeMsg visitGetResultsFailed(final ValueRequirement valueRequirement, final ParameterizedFunction function, final ValueSpecification desiredOutput) {
      final MutableFudgeMsg msg = message(ResolutionFailure.Status.GET_RESULTS_FAILED, valueRequirement);
      _serializer.addToMessage(msg, FUNCTION_KEY, null, function.getFunction().getFunctionDefinition().getUniqueId());
      _serializer.addToMessage(msg, PARAMETERS_KEY, null, function.getParameters());
      _serializer.addToMessage(msg, DESIRED_OUTPUT_KEY, null, desiredOutput);
      return msg;
    }

    protected MutableFudgeMsg visitGetRequirementsFailed(final ValueRequirement valueRequirement, final ParameterizedFunction function, final ValueSpecification desiredOutput) {
      final MutableFudgeMsg msg = message(ResolutionFailure.Status.GET_REQUIREMENTS_FAILED, valueRequirement);
      _serializer.addToMessage(msg, FUNCTION_KEY, null, function.getFunction().getFunctionDefinition().getUniqueId());
      _serializer.addToMessage(msg, PARAMETERS_KEY, null, function.getParameters());
      _serializer.addToMessage(msg, DESIRED_OUTPUT_KEY, null, desiredOutput);
      return msg;
    }

    protected MutableFudgeMsg visitLateResolutionFailure(final ValueRequirement valueRequirement, final ParameterizedFunction function, final ValueSpecification desiredOutput,
        final Map<ValueSpecification, ValueRequirement> requirements) {
      final MutableFudgeMsg msg = message(ResolutionFailure.Status.LATE_RESOLUTION_FAILURE, valueRequirement);
      _serializer.addToMessage(msg, FUNCTION_KEY, null, function.getFunction().getFunctionDefinition().getUniqueId());
      _serializer.addToMessage(msg, PARAMETERS_KEY, null, function.getParameters());
      _serializer.addToMessage(msg, DESIRED_OUTPUT_KEY, null, desiredOutput);
      for (Map.Entry<ValueSpecification, ValueRequirement> input : requirements.entrySet()) {
        final MutableFudgeMsg submessage = _serializer.newMessage();
        _serializer.addToMessage(submessage, VALUE_SPECIFICATION_KEY, null, input.getKey());
        _serializer.addToMessage(submessage, VALUE_REQUIREMENT_KEY, null, input.getValue());
        msg.add(SATISFIED_KEY, null, submessage);
      }
      return msg;
    }
  }

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final ResolutionFailure failure) {
    final Collection<MutableFudgeMsg> failures = failure.accept(new Visitor(serializer));
    if (failures.size() == 1) {
      return failures.iterator().next();
    } else {
      final MutableFudgeMsg msg = serializer.newMessage();
      for (MutableFudgeMsg submessage : failures) {
        msg.add(null, null, submessage);
      }
      return msg;
    }
  }

  @Override
  public ResolutionFailure buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    throw new UnsupportedOperationException();
  }

}
