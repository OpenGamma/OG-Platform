/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.mapping.GenericFudgeBuilderFor;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.target.ComputationTargetReferenceVisitor;
import com.opengamma.engine.target.ComputationTargetRequirement;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.target.ComputationTargetTypeVisitor;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;

/**
 * Fudge builder for {@link ComputationTargetReference}.
 *
 * <pre>
 *
 * message ComputationTargetReference extends ComputationTargetType {
 * }
 *
 * message ComputationTargetSpecification extends ComputationTargetReference {
 *   optional repeated UniqueId computationTargetIdentifier;         // the target specification identifier
 * }
 *
 * message ComputationTargetRequirement extends ComputationTargetReference {
 *   optional repeated ExternalIdBundle computationTargetIdentifier; // the target requirement identifiers
 * }
 *
 * </pre>
 *
 * When references are nested to give object context, the outermost identifier is listed first followed by inner identifiers. The type is the type that should be assigned to the resultant inner most
 * reference.
 */
@GenericFudgeBuilderFor(ComputationTargetReference.class)
public class ComputationTargetReferenceFudgeBuilder implements FudgeBuilder<ComputationTargetReference> {

  /**
   * Fudge field name.
   */
  private static final String IDENTIFIER_FIELD_NAME = "computationTargetIdentifier";

  private static final ComputationTargetReferenceVisitor<Object> s_encodeIdentifier = new ComputationTargetReferenceVisitor<Object>() {

    @Override
    public Object visitComputationTargetRequirement(final ComputationTargetRequirement requirement) {
      return requirement.getIdentifiers();
    }

    @Override
    public Object visitComputationTargetSpecification(final ComputationTargetSpecification specification) {
      return specification.getUniqueId();
    }

  };

  private static void encodeIdentifiers(final FudgeSerializer serializer, final MutableFudgeMsg msg, final ComputationTargetReference object) {
    if (object.getParent() != null) {
      encodeIdentifiers(serializer, msg, object.getParent());
    }
    serializer.addToMessage(msg, IDENTIFIER_FIELD_NAME, null, object.accept(s_encodeIdentifier));
  }

  public static void buildMessageImpl(final FudgeSerializer serializer, final MutableFudgeMsg msg, final ComputationTargetReference object) {
    ComputationTargetTypeFudgeBuilder.buildMessageImpl(msg, object.getType());
    encodeIdentifiers(serializer, msg, object);
  }

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final ComputationTargetReference object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    buildMessageImpl(serializer, msg, object);
    return msg;
  }

  private static ComputationTargetTypeVisitor<Void, List<ComputationTargetType>> s_getNestedType = new ComputationTargetTypeVisitor<Void, List<ComputationTargetType>>() {

    @Override
    public List<ComputationTargetType> visitMultipleComputationTargetTypes(final Set<ComputationTargetType> types, final Void reserved) {
      return Collections.emptyList();
    }

    @Override
    public List<ComputationTargetType> visitNestedComputationTargetTypes(final List<ComputationTargetType> types, final Void reserved) {
      return types;
    }

    @Override
    public List<ComputationTargetType> visitNullComputationTargetType(final Void reserved) {
      return null;
    }

    @Override
    public List<ComputationTargetType> visitClassComputationTargetType(final Class<? extends UniqueIdentifiable> type, final Void reserved) {
      return Collections.emptyList();
    }

  };

  public static ComputationTargetReference buildObjectImpl(final FudgeDeserializer deserializer, final FudgeMsg message) {
    ComputationTargetType type = ComputationTargetTypeFudgeBuilder.buildObjectImpl(message);
    final List<ComputationTargetType> types = type.accept(s_getNestedType, null);
    if (types == null) {
      if (message.getByName(IDENTIFIER_FIELD_NAME) == null) {
        return ComputationTargetSpecification.NULL;
      } else {
        return new ComputationTargetRequirement(type, ExternalIdBundle.EMPTY);
      }
    } else if (types.isEmpty()) {
      FudgeField field = message.getByName(IDENTIFIER_FIELD_NAME);
      if (field == null) {
        field = message.getByName("computationTargetId");
      }
      if (field.getValue() instanceof FudgeMsg) {
        return new ComputationTargetRequirement(type, deserializer.fieldValueToObject(ExternalIdBundle.class, field));
      } else {
        return new ComputationTargetSpecification(type, message.getFieldValue(UniqueId.class, field));
      }
    } else {
      ComputationTargetReference result = null;
      final Iterator<ComputationTargetType> itrType = types.iterator();
      for (final FudgeField field : message.getAllByName(IDENTIFIER_FIELD_NAME)) {
        type = itrType.next();
        if (field.getValue() instanceof FudgeMsg) {
          final ExternalIdBundle identifiers = deserializer.fieldValueToObject(ExternalIdBundle.class, field);
          if (result == null) {
            result = new ComputationTargetRequirement(type, identifiers);
          } else {
            result = result.containing(type, identifiers);
          }
        } else {
          final UniqueId identifier = message.getFieldValue(UniqueId.class, field);
          if (result == null) {
            result = new ComputationTargetSpecification(type, identifier);
          } else {
            result = result.containing(type, identifier);
          }
        }
      }
      return result;
    }
  }

  @Override
  public ComputationTargetReference buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    return buildObjectImpl(deserializer, message);
  }

}
