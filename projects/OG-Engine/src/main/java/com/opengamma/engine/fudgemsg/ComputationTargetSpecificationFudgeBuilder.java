/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.id.UniqueId;

/**
 * Fudge builder for {@link ComputationTargetSpecification}.
 * 
 * <pre>
 * message ComputationTargetSpecification {
 *   required ComputationTargetType computationTargetType; // the target type
 *   required UniqueId computationTargetIdendifier;        // the target identifier
 * }
 * </pre>
 */
@FudgeBuilderFor(ComputationTargetSpecification.class)
public class ComputationTargetSpecificationFudgeBuilder implements FudgeBuilder<ComputationTargetSpecification> {
  /**
   * Fudge field name.
   */
  private static final String TYPE_FIELD_NAME = "computationTargetType";
  /**
   * Fudge field name.
   */
  private static final String IDENTIFIER_FIELD_NAME = "computationTargetIdentifier";

  protected static void buildMessageImpl(final MutableFudgeMsg msg, final ComputationTargetSpecification object) {
    msg.add(TYPE_FIELD_NAME, object.getType().name());
    UniqueId uid = object.getUniqueId();
    if (uid != null) {
      msg.add(IDENTIFIER_FIELD_NAME, uid);
    }
  }

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final ComputationTargetSpecification object) {
    MutableFudgeMsg msg = serializer.newMessage();
    buildMessageImpl(msg, object);
    return msg;
  }

  protected static ComputationTargetSpecification buildObjectImpl(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final ComputationTargetType type = ComputationTargetType.valueOf(message.getString(TYPE_FIELD_NAME));
    FudgeField uniqueIdField = message.getByName(IDENTIFIER_FIELD_NAME);
    UniqueId uid = uniqueIdField != null ? deserializer.fieldValueToObject(UniqueId.class, uniqueIdField) : null;
    return new ComputationTargetSpecification(type, uid);
  }

  @Override
  public ComputationTargetSpecification buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    return buildObjectImpl(deserializer, message);
  }

}
