/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import java.util.List;
import java.util.Set;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.target.ComputationTargetTypeVisitor;
import com.opengamma.id.UniqueIdentifiable;

/**
 * Fudge message builder for {@link ComputationTarget}.
 * 
 * <pre>
 *   message ComputationTarget extends ComputationTargetSpecification {
 *     required Object value;              // the target value
 *   }
 * </pre>
 */
@FudgeBuilderFor(ComputationTarget.class)
public class ComputationTargetFudgeBuilder implements FudgeBuilder<ComputationTarget> {

  private static final String VALUE_FIELD = "value";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, ComputationTarget object) {
    MutableFudgeMsg msg = serializer.newMessage();
    ComputationTargetReferenceFudgeBuilder.buildMessageImpl(serializer, msg, object.toSpecification());
    serializer.addToMessageWithClassHeaders(msg, VALUE_FIELD, null, object.getValue());
    return msg;
  }

  private static final ComputationTargetTypeVisitor<Void, Class<? extends UniqueIdentifiable>> s_getLeafType = new ComputationTargetTypeVisitor<Void, Class<? extends UniqueIdentifiable>>() {

    @Override
    public Class<? extends UniqueIdentifiable> visitMultipleComputationTargetTypes(final Set<ComputationTargetType> types, final Void data) {
      throw new IllegalArgumentException();
    }

    @Override
    public Class<? extends UniqueIdentifiable> visitNestedComputationTargetTypes(final List<ComputationTargetType> types, final Void data) {
      return types.get(types.size() - 1).accept(this, data);
    }

    @Override
    public Class<? extends UniqueIdentifiable> visitNullComputationTargetType(final Void data) {
      return null;
    }

    @Override
    public Class<? extends UniqueIdentifiable> visitClassComputationTargetType(final Class<? extends UniqueIdentifiable> type, final Void data) {
      return type;
    }

  };

  @Override
  public ComputationTarget buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    final ComputationTargetSpecification specification = ComputationTargetReferenceFudgeBuilder.buildObjectImpl(deserializer, message).getSpecification();
    final Class<? extends UniqueIdentifiable> valueType = specification.getType().accept(s_getLeafType, null);
    if (valueType != null) {
      final UniqueIdentifiable value = deserializer.fieldValueToObject(valueType, message.getByName(VALUE_FIELD));
      return new ComputationTarget(specification, value);
    } else {
      return ComputationTarget.NULL;
    }
  }

}
