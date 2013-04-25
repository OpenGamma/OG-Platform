/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.financial.analytics.curve.CurveConfigurationSpecification;
import com.opengamma.id.UniqueId;

/**
 * 
 */
@FudgeBuilderFor(CurveConfigurationSpecification.class)
public class CurveConfigurationSpecificationBuilder implements FudgeBuilder<CurveConfigurationSpecification> {
  private static final String TARGET_ID_FIELD = "targetId";
  private static final String TARGET_TYPE_FIELD = "targetType";
  private static final String PRIORITY_FIELD = "priority";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final CurveConfigurationSpecification object) {
    final MutableFudgeMsg message = serializer.newMessage();
    FudgeSerializer.addClassHeader(message, CurveConfigurationSpecification.class);
    serializer.addToMessage(message, TARGET_ID_FIELD, null, object.getTargetSpec().getUniqueId());
    serializer.addToMessage(message, TARGET_TYPE_FIELD, null, object.getTargetSpec().getType());
    message.add(PRIORITY_FIELD, object.getPriority());
    return message;
  }

  @Override
  public CurveConfigurationSpecification buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final UniqueId id = deserializer.fieldValueToObject(UniqueId.class, message.getByName(TARGET_ID_FIELD));
    final ComputationTargetType type = deserializer.fieldValueToObject(ComputationTargetType.class, message.getByName(TARGET_TYPE_FIELD));
    final ComputationTargetSpecification targetSpec = new ComputationTargetSpecification(type, id);
    final int priority = message.getInt(PRIORITY_FIELD);
    return new CurveConfigurationSpecification(targetSpec, priority);
  }

}
