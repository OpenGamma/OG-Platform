/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.target.ComputationTargetTypeVisitor;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;

/**
 * Fudge message builder for {@link ComputationTarget}.
 * 
 * <pre>
 *   message ComputationTarget extends ComputationTargetType {
 *     optional repeated UniqueId context; // the outer object identifiers
 *     required Object value;              // the target value
 *   }
 * </pre>
 */
@FudgeBuilderFor(ComputationTarget.class)
public class ComputationTargetFudgeBuilder implements FudgeBuilder<ComputationTarget> {

  private static final String VALUE_FIELD = "value";
  private static final String CONTEXT_FIELD = "context";
  
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, ComputationTarget object) {
    MutableFudgeMsg msg = serializer.newMessage();
    ComputationTargetTypeFudgeBuilder.buildMessageImpl(msg, object.getType());
    if (object.getContextIdentifiers() != null) {
      for (UniqueId context : object.getContextIdentifiers()) {
        serializer.addToMessage(msg, CONTEXT_FIELD, null, context);
      }
    }
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

  private static final ComputationTargetTypeVisitor<FudgeMsg, List<FudgeField>> s_getContextIdentifiers = new ComputationTargetTypeVisitor<FudgeMsg, List<FudgeField>>() {

    @Override
    public List<FudgeField> visitMultipleComputationTargetTypes(final Set<ComputationTargetType> types, final FudgeMsg data) {
      return null;
    }

    @Override
    public List<FudgeField> visitNestedComputationTargetTypes(final List<ComputationTargetType> types, final FudgeMsg data) {
      return data.getAllByName(CONTEXT_FIELD);
    }

    @Override
    public List<FudgeField> visitNullComputationTargetType(final FudgeMsg data) {
      return null;
    }

    @Override
    public List<FudgeField> visitClassComputationTargetType(final Class<? extends UniqueIdentifiable> type, final FudgeMsg data) {
      return null;
    }

  };

  @Override
  public ComputationTarget buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    final ComputationTargetType type = ComputationTargetTypeFudgeBuilder.buildObjectImpl(message);
    final Class<? extends UniqueIdentifiable> valueType = type.accept(s_getLeafType, null);
    if (valueType != null) {
      final UniqueIdentifiable value = deserializer.fieldValueToObject(valueType, message.getByName(VALUE_FIELD));
      final List<FudgeField> contextIdentifierFields = type.accept(s_getContextIdentifiers, message);
      if (contextIdentifierFields != null) {
        final List<UniqueId> contextIdentifiers = new ArrayList<UniqueId>(contextIdentifierFields.size());
        for (FudgeField contextIdentifier : contextIdentifierFields) {
          contextIdentifiers.add(deserializer.fieldValueToObject(UniqueId.class, contextIdentifier));
        }
        return new ComputationTarget(type, contextIdentifiers, value);
      } else {
        return new ComputationTarget(type, value);
      }
    } else {
      return ComputationTarget.NULL;
    }
  }

}
