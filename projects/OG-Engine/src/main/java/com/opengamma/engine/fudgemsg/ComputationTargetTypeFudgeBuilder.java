/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.mapping.GenericFudgeBuilderFor;
import org.fudgemsg.wire.types.FudgeWireType;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.target.ComputationTargetTypeVisitor;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.ClassUtils;

/**
 * Fudge message builder for {@link ComputationTargetType}.
 * 
 * <pre>
 * message ComputationTargetType {
 *   optional repeated string computationTargetType;     // name of the type
 *   optional repeated message computationTargetType;    // alternative type choices
 *   // The computationTargetType field is repeated for nested types, the outermost first. When a message is present instead
 *   // of a field, the message contains alternative type choices. Each may be a string which is the name of the type or is a
 *   // ComputationTargetType message.
 * }
 * </pre>
 */
@GenericFudgeBuilderFor(ComputationTargetType.class)
public class ComputationTargetTypeFudgeBuilder implements FudgeBuilder<ComputationTargetType> {

  private static class CommonByName {

    private static final Map<String, ComputationTargetType> s_data = new HashMap<String, ComputationTargetType>();

    static {
      try {
        final Class<?> c = ComputationTargetType.class;
        for (final Field field : c.getDeclaredFields()) {
          if (Modifier.isPublic(field.getModifiers()) && Modifier.isStatic(field.getModifiers()) &&
              field.isSynthetic() == false && c.isAssignableFrom(field.getType())) {
            final ComputationTargetType type = (ComputationTargetType) field.get(null);
            s_data.put(type.toString(), type);
          }
        }
      } catch (final IllegalAccessException e) {
        throw new OpenGammaRuntimeException("Can't initialise", e);
      }
    }

    public static ComputationTargetType get(final String name) {
      return s_data.get(name);
    }

  }

  /**
   * Fudge field name.
   */
  private static final String TYPE_FIELD_NAME = "computationTargetType";

  @SuppressWarnings({"unchecked", "rawtypes" })
  public static ComputationTargetType fromString(final String str) {
    final ComputationTargetType common = CommonByName.get(str);
    if (common != null) {
      return common;
    } else {
      return ComputationTargetType.of((Class) ClassUtils.loadClassRuntime(str));
    }
  }

  private static final ComputationTargetTypeVisitor<MutableFudgeMsg, Boolean> s_baseEncoder = new ComputationTargetTypeVisitor<MutableFudgeMsg, Boolean>() {

    @Override
    public Boolean visitMultipleComputationTargetTypes(final Set<ComputationTargetType> types, final MutableFudgeMsg data) {
      // Add a sub-message containing the choices
      final MutableFudgeMsg msg = data.addSubMessage(TYPE_FIELD_NAME, null);
      for (final ComputationTargetType type : types) {
        if (type.accept(s_choiceEncoder, msg)) {
          msg.add(null, null, FudgeWireType.STRING, type.toString());
        }
      }
      return Boolean.FALSE;
    }

    @Override
    public Boolean visitNestedComputationTargetTypes(final List<ComputationTargetType> types, final MutableFudgeMsg data) {
      // Add fields in order
      for (final ComputationTargetType type : types) {
        if (type.accept(s_baseEncoder, data)) {
          data.add(TYPE_FIELD_NAME, null, FudgeWireType.STRING, type.toString());
        }
      }
      return Boolean.FALSE;
    }

    @Override
    public Boolean visitNullComputationTargetType(final MutableFudgeMsg data) {
      return Boolean.TRUE;
    }

    @Override
    public Boolean visitClassComputationTargetType(final Class<? extends UniqueIdentifiable> type, final MutableFudgeMsg data) {
      return Boolean.TRUE;
    }

  };

  private static final ComputationTargetTypeVisitor<MutableFudgeMsg, Boolean> s_choiceEncoder = new ComputationTargetTypeVisitor<MutableFudgeMsg, Boolean>() {

    @Override
    public Boolean visitMultipleComputationTargetTypes(final Set<ComputationTargetType> types, final MutableFudgeMsg data) {
      throw new IllegalArgumentException();
    }

    @Override
    public Boolean visitNestedComputationTargetTypes(final List<ComputationTargetType> types, final MutableFudgeMsg data) {
      // Add a sub-message which encodes the types in the correct order
      final MutableFudgeMsg msg = data.addSubMessage(null, null);
      for (final ComputationTargetType type : types) {
        if (type.accept(s_nestedEncoder, msg)) {
          msg.add(null, null, FudgeWireType.STRING, type.toString());
        }
      }
      return Boolean.FALSE;
    }

    @Override
    public Boolean visitNullComputationTargetType(final MutableFudgeMsg data) {
      return Boolean.TRUE;
    }

    @Override
    public Boolean visitClassComputationTargetType(final Class<? extends UniqueIdentifiable> type, final MutableFudgeMsg data) {
      return Boolean.TRUE;
    }

  };

  private static final ComputationTargetTypeVisitor<MutableFudgeMsg, Boolean> s_nestedEncoder = new ComputationTargetTypeVisitor<MutableFudgeMsg, Boolean>() {

    @Override
    public Boolean visitMultipleComputationTargetTypes(final Set<ComputationTargetType> types, final MutableFudgeMsg data) {
      // Add a sub-message containing the choices
      final MutableFudgeMsg msg = data.addSubMessage(null, null);
      for (final ComputationTargetType type : types) {
        if (type.accept(s_choiceEncoder, msg)) {
          msg.add(null, null, FudgeWireType.STRING, type.toString());
        }
      }
      return Boolean.FALSE;
    }

    @Override
    public Boolean visitNestedComputationTargetTypes(final List<ComputationTargetType> types, final MutableFudgeMsg data) {
      throw new IllegalArgumentException();
    }

    @Override
    public Boolean visitNullComputationTargetType(final MutableFudgeMsg data) {
      return Boolean.TRUE;
    }

    @Override
    public Boolean visitClassComputationTargetType(final Class<? extends UniqueIdentifiable> type, final MutableFudgeMsg data) {
      return Boolean.TRUE;
    }

  };

  public static void buildMessageImpl(final MutableFudgeMsg msg, final ComputationTargetType object) {
    if (object.accept(s_baseEncoder, msg)) {
      msg.add(TYPE_FIELD_NAME, null, FudgeWireType.STRING, object.toString());
    }
  }

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final ComputationTargetType object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    buildMessageImpl(msg, object);
    return msg;
  }

  @SuppressWarnings({"rawtypes", "unchecked" })
  private static ComputationTargetType decodeAlternativeType(final ComputationTargetType current, final FudgeField field) throws Exception {
    if (field.getValue() instanceof String) {
      final String name = (String) field.getValue();
      final ComputationTargetType common = CommonByName.get(name);
      if (common != null) {
        if (current == null) {
          return common;
        } else {
          return current.or(common);
        }
      } else {
        final Class clazz = ClassUtils.loadClass(name);
        if (current == null) {
          return ComputationTargetType.of(clazz);
        } else {
          return current.or(clazz);
        }
      }
    } else if (field.getValue() instanceof FudgeMsg) {
      ComputationTargetType type = null;
      for (final FudgeField field2 : (FudgeMsg) field.getValue()) {
        type = decodeNestedType(type, field2);
      }
      if (type != null) {
        if (current == null) {
          return type;
        } else {
          return current.or(type);
        }
      } else {
        return current;
      }
    } else {
      return current;
    }
  }

  @SuppressWarnings({"rawtypes", "unchecked" })
  private static ComputationTargetType decodeNestedType(final ComputationTargetType outer, final FudgeField field) throws Exception {
    if (field.getValue() instanceof String) {
      final String name = (String) field.getValue();
      final ComputationTargetType common = CommonByName.get(name);
      if (common != null) {
        if (outer == null) {
          return common;
        } else {
          return outer.containing(common);
        }
      } else {
        final Class clazz = ClassUtils.loadClass(name);
        if (outer == null) {
          return ComputationTargetType.of(clazz);
        } else {
          return outer.containing(clazz);
        }
      }
    } else if (field.getValue() instanceof FudgeMsg) {
      ComputationTargetType type = null;
      for (final FudgeField field2 : (FudgeMsg) field.getValue()) {
        type = decodeAlternativeType(type, field2);
      }
      if (type != null) {
        if (outer == null) {
          return type;
        } else {
          return outer.containing(type);
        }
      } else {
        return outer;
      }
    } else {
      return outer;
    }
  }

  public static ComputationTargetType buildObjectImpl(final FudgeMsg msg) {
    try {
      ComputationTargetType result = null;
      for (final FudgeField field : msg) {
        if (TYPE_FIELD_NAME.equals(field.getName())) {
          result = decodeNestedType(result, field);
        }
      }
      return result;
    } catch (final Exception e) {
      throw new OpenGammaRuntimeException("Can't decode message", e);
    }
  }

  @Override
  public ComputationTargetType buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    return buildObjectImpl(message);
  }

}
