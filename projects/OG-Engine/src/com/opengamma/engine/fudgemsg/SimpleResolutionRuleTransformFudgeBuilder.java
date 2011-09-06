/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import java.util.Map;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.types.IndicatorType;

import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.function.resolver.ComputationTargetFilter;
import com.opengamma.engine.function.resolver.SimpleResolutionRuleTransform;
import com.opengamma.engine.function.resolver.SimpleResolutionRuleTransform.Action;
import com.opengamma.engine.function.resolver.SimpleResolutionRuleTransform.Adjust;
import com.opengamma.engine.function.resolver.SimpleResolutionRuleTransform.DontUse;
import com.opengamma.engine.function.resolver.SimpleResolutionRuleTransform.MultipleAdjust;

/**
 * Fudge message builder for {@code SimpleResolutionRuleTransform}.
 */
@FudgeBuilderFor(SimpleResolutionRuleTransform.class)
public class SimpleResolutionRuleTransformFudgeBuilder implements FudgeBuilder<SimpleResolutionRuleTransform> {

  private static final String PARAMETERS_FIELD = "parameters";
  private static final String PRIORITY_FIELD = "priorityAdjustment";
  private static final String FILTER_FIELD = "targetFilter";

  private static FudgeMsg adjustToFudgeMsg(final FudgeSerializer serializer, final Adjust adjust) {
    final MutableFudgeMsg message = serializer.newMessage();
    if (adjust.getParameters() != null) {
      serializer.addToMessageWithClassHeaders(message, PARAMETERS_FIELD, null, adjust.getParameters(), FunctionParameters.class);
    }
    if (adjust.getComputationTargetFilter() != null) {
      serializer.addToMessageWithClassHeaders(message, FILTER_FIELD, null, adjust.getComputationTargetFilter(), ComputationTargetFilter.class);
    }
    if (adjust.getPriorityAdjustment() != null) {
      message.add(PRIORITY_FIELD, adjust.getPriorityAdjustment());
    }
    return message;
  }

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final SimpleResolutionRuleTransform object) {
    final MutableFudgeMsg message = serializer.newMessage();
    message.add(0, SimpleResolutionRuleTransform.class.getName());
    for (Map.Entry<String, Action> functionTransformation : object.getFunctionTransformations().entrySet()) {
      final String functionName = functionTransformation.getKey();
      final Action action = functionTransformation.getValue();
      if (action instanceof DontUse) {
        message.add(functionName, IndicatorType.INSTANCE);
      } else if (action instanceof Adjust) {
        message.add(functionName, adjustToFudgeMsg(serializer, (Adjust) action));
      } else if (action instanceof MultipleAdjust) {
        for (Adjust adjust : ((MultipleAdjust) action).getAdjusts()) {
          message.add(functionName, adjustToFudgeMsg(serializer, adjust));
        }
      } else {
        throw new IllegalArgumentException("Unexpected value " + action);
      }
    }
    return message;
  }

  @Override
  public SimpleResolutionRuleTransform buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final SimpleResolutionRuleTransform transform = new SimpleResolutionRuleTransform();
    for (FudgeField field : message) {
      if (field.getName() != null) {
        if (field.getValue() == IndicatorType.INSTANCE) {
          transform.suppressRule(field.getName());
        } else if (field.getValue() instanceof FudgeMsg) {
          final FudgeMsg action = (FudgeMsg) field.getValue();
          final FudgeField parameters = action.getByName(PARAMETERS_FIELD);
          final FudgeField filter = action.getByName(FILTER_FIELD);
          final FudgeField priority = action.getByName(PRIORITY_FIELD);
          transform.adjustRule(field.getName(), (parameters != null) ? deserializer.fieldValueToObject(FunctionParameters.class, parameters) : null, (filter != null) ? deserializer.fieldValueToObject(
              ComputationTargetFilter.class, filter) : null, (priority != null) ? message.getFieldValue(Integer.class, priority) : null);
        }
      }
    }
    return transform;
  }
}
