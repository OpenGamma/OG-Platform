/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import java.util.ArrayList;
import java.util.List;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.mapping.GenericFudgeBuilderFor;
import org.fudgemsg.wire.types.FudgeWireType;

import com.opengamma.engine.value.ValueProperties;

/**
 * Fudge message builder for {@link ValueProperties}. Messages can take the form:
 * <ul>
 * <li>The empty property set is an empty message.
 * <li>A simple property set has a field named {@code with} that contains a sub-message. This in turn contains a field per property - the name of the field is the name of the property. This is either
 * a:
 * <ul>
 * <li>Simple string value
 * <li>An indicator indicating a wild-card property value
 * <li>A sub-message containing un-named fields with each possible value of the property.
 * </ul>
 * If the property is optional, the sub-message form is used with a field named {@code optional}. If the field is an optional wild-card the sub-message form is used which contains only the
 * {@code optional} field.
 * <li>The infinite property set ({@link ValueProperties#all}) has a field named {@code without} that contains an empty sub-message.
 * <li>The near-infinite property set has a field named {@code without} that contains a field for each of absent entries, the string value of each field is the property name.
 * </ul>
 */
@GenericFudgeBuilderFor(ValueProperties.class)
public class ValuePropertiesFudgeBuilder implements FudgeBuilder<ValueProperties> {

  // TODO: The message format described above is not particularly efficient or convenient, but kept for compatibility

  /**
   * Field name for the sub-message containing property values.
   */
  public static final String WITH_FIELD = "with";

  /**
   * Field name for the sub-message representing the infinite or near-infinite property set.
   */
  public static final String WITHOUT_FIELD = "without";

  /**
   * Field name for the 'optional' flag.
   */
  public static final String OPTIONAL_FIELD = "optional";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final ValueProperties object) {
    final MutableFudgeMsg message = serializer.newMessage();
    object.toFudgeMsg(message);
    return message;
  }

  @Override
  public ValueProperties buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    if (message.isEmpty()) {
      return ValueProperties.none();
    }
    FudgeMsg subMsg = message.getMessage(WITHOUT_FIELD);
    if (subMsg != null) {
      if (subMsg.isEmpty()) {
        // Infinite
        return ValueProperties.all();
      } else {
        // Near-infinite
        final ValueProperties.Builder builder = ValueProperties.all().copy();
        for (FudgeField field : subMsg) {
          if (field.getType().getTypeId() == FudgeWireType.STRING_TYPE_ID) {
            builder.withoutAny((String) field.getValue());
          }
        }
        return builder.get();
      }
    }
    subMsg = message.getMessage(WITH_FIELD);
    if (subMsg == null) {
      return ValueProperties.none();
    }
    final ValueProperties.Builder builder = ValueProperties.builder();
    for (FudgeField field : subMsg) {
      final String propertyName = field.getName();
      switch (field.getType().getTypeId()) {
        case FudgeWireType.INDICATOR_TYPE_ID:
          builder.withAny(propertyName);
          break;
        case FudgeWireType.STRING_TYPE_ID:
          builder.with(propertyName, (String) field.getValue());
          break;
        case FudgeWireType.SUB_MESSAGE_TYPE_ID: {
          final FudgeMsg subMsg2 = (FudgeMsg) field.getValue();
          final List<String> values = new ArrayList<String>(subMsg2.getNumFields());
          for (FudgeField field2 : subMsg2) {
            switch (field2.getType().getTypeId()) {
              case FudgeWireType.INDICATOR_TYPE_ID:
                builder.withOptional(propertyName);
                break;
              case FudgeWireType.STRING_TYPE_ID:
                values.add((String) field2.getValue());
                break;
            }
          }
          if (!values.isEmpty()) {
            builder.with(propertyName, values);
          }
          break;
        }
      }
    }
    return builder.get();
  }

}
