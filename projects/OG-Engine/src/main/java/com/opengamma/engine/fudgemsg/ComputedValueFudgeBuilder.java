/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import org.apache.commons.lang.Validate;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.fudgemsg.WriteReplaceHelper;

/**
 * Fudge message builder for {@code ComputedValue}.
 */
@FudgeBuilderFor(ComputedValue.class)
public class ComputedValueFudgeBuilder implements FudgeBuilder<ComputedValue> {

  /**
   * Fudge field name.
   */
  private static final String SPECIFICATION_KEY = "specification";
  /**
   * Fudge field name.
   */
  private static final String VALUE_KEY = "value";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final ComputedValue object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    appendToMsg(serializer, object, msg);
    return msg;
  }

  /*package*/static void appendToMsg(final FudgeSerializer serializer, final ComputedValue object, final MutableFudgeMsg msg) {
    final ValueSpecification specification = object.getSpecification();
    if (specification != null) {
      serializer.addToMessage(msg, SPECIFICATION_KEY, null, specification);
    }
    final Object value = object.getValue();
    if (value != null) {
      serializer.addToMessageWithClassHeaders(msg, VALUE_KEY, null, WriteReplaceHelper.writeReplace(value));
    }
  }

  @Override
  public ComputedValue buildObject(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    final ValueSpecification valueSpec = getValueSpecification(deserializer, msg);
    final Object valueObject = getValueObject(deserializer, msg);
    return new ComputedValue(valueSpec, valueObject);
  }

  /*package*/static ValueSpecification getValueSpecification(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    final FudgeField fudgeField = msg.getByName(SPECIFICATION_KEY);
    Validate.notNull(fudgeField, "Fudge message is not a ComputedValue - field 'specification' is not present");
    final ValueSpecification valueSpec = deserializer.fieldValueToObject(ValueSpecification.class, fudgeField);
    return valueSpec;
  }

  /*package*/static Object getValueObject(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    FudgeField fudgeField;
    fudgeField = msg.getByName(VALUE_KEY);
    if (fudgeField == null) {
      return null;
    }
    //Validate.notNull(fudgeField, "Fudge message is not a ComputedValue - field 'value' is not present");
    final Object valueObject = deserializer.fieldValueToObject(fudgeField);
    return valueObject;
  }

}
