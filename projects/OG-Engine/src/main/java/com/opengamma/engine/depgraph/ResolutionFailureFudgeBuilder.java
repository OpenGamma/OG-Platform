/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.mapping.GenericFudgeBuilderFor;

import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Fudge message builder for {@code ResolutionFailure}.
 */
@GenericFudgeBuilderFor(ResolutionFailure.class)
public class ResolutionFailureFudgeBuilder implements FudgeBuilder<ResolutionFailure> {

  private static final String ERROR_KEY = "Error";
  private static final String VALUE_REQUIREMENT_KEY = "ValueRequirement";
  private static final String FUNCTION_KEY = "Function";
  private static final String DESIRED_OUTPUT_KEY = "DesiredOutput";
  private static final String SATISFIED_KEY = "Satisfied";
  private static final String UNSATISFIED_KEY = "Unsatisfied";
  private static final String ADDITIONAL_REQUIREMENT_KEY = "AdditionalRequirement";
  private static final String VALUE_SPECIFICATION_KEY = "ValueSpecification";

  /**
   * Visitor that produces Fudge messages from {@link ResolutionFailure} objects.
   */
  private static final class VisitorImpl extends ResolutionFailureVisitor<MutableFudgeMsg> {

    private final FudgeSerializer _serializer;

    public VisitorImpl(final FudgeSerializer serializer) {
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

    @Override
    protected MutableFudgeMsg visitCouldNotResolve(final ValueRequirement valueRequirement) {
      return message(ResolutionFailure.Status.COULD_NOT_RESOLVE, valueRequirement);
    }

    @Override
    protected MutableFudgeMsg visitNoFunctions(final ValueRequirement valueRequirement) {
      return message(ResolutionFailure.Status.NO_FUNCTIONS, valueRequirement);
    }

    @Override
    protected MutableFudgeMsg visitRecursiveRequirement(final ValueRequirement valueRequirement) {
      return message(ResolutionFailure.Status.RECURSIVE_REQUIREMENT, valueRequirement);
    }

    @Override
    protected MutableFudgeMsg visitUnsatisfied(final ValueRequirement valueRequirement) {
      return message(ResolutionFailure.Status.UNSATISFIED, valueRequirement);
    }

    @Override
    protected MutableFudgeMsg visitMarketDataMissing(final ValueRequirement valueRequirement) {
      return message(ResolutionFailure.Status.MARKET_DATA_MISSING, valueRequirement);
    }

    @Override
    protected MutableFudgeMsg visitFunction(final ValueRequirement valueRequirement, final String function, final ValueSpecification desiredOutput,
        final Map<ValueSpecification, ValueRequirement> satisfied, final Set<ResolutionFailure> unsatisfied, final Set<ResolutionFailure> unsatisfiedAdditional) {
      final MutableFudgeMsg msg = message(null, valueRequirement);
      _serializer.addToMessage(msg, FUNCTION_KEY, null, function);
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

    @Override
    protected MutableFudgeMsg visitGetAdditionalRequirementsFailed(final ValueRequirement valueRequirement, final String function, final ValueSpecification desiredOutput,
        final Map<ValueSpecification, ValueRequirement> requirements) {
      final MutableFudgeMsg msg = message(ResolutionFailure.Status.GET_ADDITIONAL_REQUIREMENTS_FAILED, valueRequirement);
      _serializer.addToMessage(msg, FUNCTION_KEY, null, function);
      _serializer.addToMessage(msg, DESIRED_OUTPUT_KEY, null, desiredOutput);
      for (Map.Entry<ValueSpecification, ValueRequirement> input : requirements.entrySet()) {
        final MutableFudgeMsg submessage = _serializer.newMessage();
        _serializer.addToMessage(submessage, VALUE_SPECIFICATION_KEY, null, input.getKey());
        _serializer.addToMessage(submessage, VALUE_REQUIREMENT_KEY, null, input.getValue());
        msg.add(SATISFIED_KEY, null, submessage);
      }
      return msg;
    }

    @Override
    protected MutableFudgeMsg visitGetResultsFailed(final ValueRequirement valueRequirement, final String function, final ValueSpecification desiredOutput,
        final Map<ValueSpecification, ValueRequirement> requirements) {
      final MutableFudgeMsg msg = message(ResolutionFailure.Status.GET_RESULTS_FAILED, valueRequirement);
      _serializer.addToMessage(msg, FUNCTION_KEY, null, function);
      _serializer.addToMessage(msg, DESIRED_OUTPUT_KEY, null, desiredOutput);
      for (Map.Entry<ValueSpecification, ValueRequirement> input : requirements.entrySet()) {
        final MutableFudgeMsg submessage = _serializer.newMessage();
        _serializer.addToMessage(submessage, VALUE_SPECIFICATION_KEY, null, input.getKey());
        _serializer.addToMessage(submessage, VALUE_REQUIREMENT_KEY, null, input.getValue());
        msg.add(SATISFIED_KEY, null, submessage);
      }
      return msg;
    }

    @Override
    protected MutableFudgeMsg visitGetRequirementsFailed(final ValueRequirement valueRequirement, final String function, final ValueSpecification desiredOutput) {
      final MutableFudgeMsg msg = message(ResolutionFailure.Status.GET_REQUIREMENTS_FAILED, valueRequirement);
      _serializer.addToMessage(msg, FUNCTION_KEY, null, function);
      _serializer.addToMessage(msg, DESIRED_OUTPUT_KEY, null, desiredOutput);
      return msg;
    }

    @Override
    protected MutableFudgeMsg visitLateResolutionFailure(final ValueRequirement valueRequirement, final String function, final ValueSpecification desiredOutput,
        final Map<ValueSpecification, ValueRequirement> requirements) {
      final MutableFudgeMsg msg = message(ResolutionFailure.Status.LATE_RESOLUTION_FAILURE, valueRequirement);
      _serializer.addToMessage(msg, FUNCTION_KEY, null, function);
      _serializer.addToMessage(msg, DESIRED_OUTPUT_KEY, null, desiredOutput);
      for (Map.Entry<ValueSpecification, ValueRequirement> input : requirements.entrySet()) {
        final MutableFudgeMsg submessage = _serializer.newMessage();
        _serializer.addToMessage(submessage, VALUE_SPECIFICATION_KEY, null, input.getKey());
        _serializer.addToMessage(submessage, VALUE_REQUIREMENT_KEY, null, input.getValue());
        msg.add(SATISFIED_KEY, null, submessage);
      }
      return msg;
    }

    @Override
    protected MutableFudgeMsg visitBlacklistSuppressed(final ValueRequirement valueRequirement, final String function, final ValueSpecification desiredOutput,
        final Map<ValueSpecification, ValueRequirement> requirements) {
      final MutableFudgeMsg msg = message(ResolutionFailure.Status.SUPPRESSED, valueRequirement);
      _serializer.addToMessage(msg, FUNCTION_KEY, null, function);
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

  /**
   * Visitor that produces Fudge messages from {@link ResolutionFailure} objects.
   */
  public static final class Visitor extends ResolutionFailureVisitor<MutableFudgeMsg> {

    private final FudgeContext _context;

    public Visitor(final FudgeContext context) {
      _context = context;
    }

    private VisitorImpl getUnderlying() {
      return new VisitorImpl(new FudgeSerializer(_context));
    }

    @Override
    protected MutableFudgeMsg visitCouldNotResolve(final ValueRequirement valueRequirement) {
      return getUnderlying().visitCouldNotResolve(valueRequirement);
    }

    @Override
    protected MutableFudgeMsg visitNoFunctions(final ValueRequirement valueRequirement) {
      return getUnderlying().visitNoFunctions(valueRequirement);
    }

    @Override
    protected MutableFudgeMsg visitRecursiveRequirement(final ValueRequirement valueRequirement) {
      return getUnderlying().visitRecursiveRequirement(valueRequirement);
    }

    @Override
    protected MutableFudgeMsg visitUnsatisfied(final ValueRequirement valueRequirement) {
      return getUnderlying().visitUnsatisfied(valueRequirement);
    }

    @Override
    protected MutableFudgeMsg visitMarketDataMissing(final ValueRequirement valueRequirement) {
      return getUnderlying().visitMarketDataMissing(valueRequirement);
    }

    @Override
    protected MutableFudgeMsg visitFunction(final ValueRequirement valueRequirement, final String function, final ValueSpecification desiredOutput,
        final Map<ValueSpecification, ValueRequirement> satisfied, final Set<ResolutionFailure> unsatisfied, final Set<ResolutionFailure> unsatisfiedAdditional) {
      return getUnderlying().visitFunction(valueRequirement, function, desiredOutput, satisfied, unsatisfied, unsatisfiedAdditional);
    }

    @Override
    protected MutableFudgeMsg visitGetAdditionalRequirementsFailed(final ValueRequirement valueRequirement, final String function, final ValueSpecification desiredOutput,
        final Map<ValueSpecification, ValueRequirement> requirements) {
      return getUnderlying().visitGetAdditionalRequirementsFailed(valueRequirement, function, desiredOutput, requirements);
    }

    @Override
    protected MutableFudgeMsg visitGetResultsFailed(final ValueRequirement valueRequirement, final String function, final ValueSpecification desiredOutput,
        final Map<ValueSpecification, ValueRequirement> requirements) {
      return getUnderlying().visitGetResultsFailed(valueRequirement, function, desiredOutput, requirements);
    }

    @Override
    protected MutableFudgeMsg visitGetRequirementsFailed(final ValueRequirement valueRequirement, final String function, final ValueSpecification desiredOutput) {
      return getUnderlying().visitGetRequirementsFailed(valueRequirement, function, desiredOutput);
    }

    @Override
    protected MutableFudgeMsg visitLateResolutionFailure(final ValueRequirement valueRequirement, final String function, final ValueSpecification desiredOutput,
        final Map<ValueSpecification, ValueRequirement> requirements) {
      return getUnderlying().visitLateResolutionFailure(valueRequirement, function, desiredOutput, requirements);
    }

    @Override
    protected MutableFudgeMsg visitBlacklistSuppressed(final ValueRequirement valueRequirement, final String function, final ValueSpecification desiredOutput,
        final Map<ValueSpecification, ValueRequirement> requirements) {
      return getUnderlying().visitBlacklistSuppressed(valueRequirement, function, desiredOutput, requirements);
    }

  }

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final ResolutionFailure failure) {
    final Collection<MutableFudgeMsg> failures = failure.accept(new VisitorImpl(serializer));
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

  private ResolutionFailure functionApplication(final FudgeDeserializer deserializer, final FudgeMsg message, final ValueRequirement valueRequirement) {
    final String functionId = message.getString(FUNCTION_KEY);
    final ValueSpecification desiredOutput = deserializer.fieldValueToObject(ValueSpecification.class, message.getByName(DESIRED_OUTPUT_KEY));
    ResolutionFailure failure = ResolutionFailureImpl.functionApplication(valueRequirement, functionId, desiredOutput);
    List<FudgeField> fields = message.getAllByName(SATISFIED_KEY);
    if ((fields != null) && !fields.isEmpty()) {
      final Map<ValueSpecification, ValueRequirement> requirements = new HashMap<ValueSpecification, ValueRequirement>();
      for (FudgeField requirement : fields) {
        final FudgeMsg requirementMsg = (FudgeMsg) requirement.getValue();
        requirements.put(deserializer.fieldValueToObject(ValueSpecification.class, requirementMsg.getByName(VALUE_SPECIFICATION_KEY)),
            deserializer.fieldValueToObject(ValueRequirement.class, requirementMsg.getByName(VALUE_REQUIREMENT_KEY)));
      }
      failure = failure.requirements(requirements);
    }
    fields = message.getAllByName(UNSATISFIED_KEY);
    if ((fields != null) && !fields.isEmpty()) {
      for (FudgeField requirement : fields) {
        final ResolutionFailure requirementFailure = deserializer.fieldValueToObject(ResolutionFailure.class, requirement);
        failure = failure.requirement(requirementFailure.getValueRequirement(), requirementFailure);
      }
    }
    fields = message.getAllByName(ADDITIONAL_REQUIREMENT_KEY);
    if ((fields != null) && !fields.isEmpty()) {
      for (FudgeField requirement : fields) {
        final ResolutionFailure requirementFailure = deserializer.fieldValueToObject(ResolutionFailure.class, requirement);
        failure = failure.additionalRequirement(requirementFailure.getValueRequirement(), requirementFailure);
      }
    }
    return failure;
  }

  @Override
  public ResolutionFailure buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    FudgeField valueRequirementField = message.getByName(VALUE_REQUIREMENT_KEY);
    if (valueRequirementField == null) {
      ResolutionFailure failure = null;
      for (FudgeField field : message) {
        if ((field.getName() == null) && (field.getOrdinal() == null)) {
          final ResolutionFailure newFailure = buildObject(deserializer, (FudgeMsg) field.getValue());
          if (failure == null) {
            failure = newFailure;
          } else {
            failure.merge(newFailure);
          }
        }
      }
      return failure;
    } else {
      final String error = message.getString(ERROR_KEY);
      final ValueRequirement valueRequirement = deserializer.fieldValueToObject(ValueRequirement.class, valueRequirementField);
      if (error != null) {
        switch (ResolutionFailure.Status.valueOf(error)) {
          case COULD_NOT_RESOLVE:
            return ResolutionFailureImpl.couldNotResolve(valueRequirement);
          case GET_ADDITIONAL_REQUIREMENTS_FAILED:
            return functionApplication(deserializer, message, valueRequirement).getAdditionalRequirementsFailed();
          case GET_REQUIREMENTS_FAILED:
            return functionApplication(deserializer, message, valueRequirement).getRequirementsFailed();
          case GET_RESULTS_FAILED:
            return functionApplication(deserializer, message, valueRequirement).getResultsFailed();
          case LATE_RESOLUTION_FAILURE:
            return functionApplication(deserializer, message, valueRequirement).lateResolutionFailure();
          case MARKET_DATA_MISSING:
            return ResolutionFailureImpl.marketDataMissing(valueRequirement);
          case NO_FUNCTIONS:
            return ResolutionFailureImpl.noFunctions(valueRequirement);
          case RECURSIVE_REQUIREMENT:
            return ResolutionFailureImpl.recursiveRequirement(valueRequirement);
          case SUPPRESSED:
            return functionApplication(deserializer, message, valueRequirement).suppressed();
          case UNSATISFIED:
            return ResolutionFailureImpl.unsatisfied(valueRequirement);
        }
        throw new IllegalStateException(error);
      } else {
        return functionApplication(deserializer, message, valueRequirement);
      }
    }
  }

}
