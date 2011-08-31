/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.security.option.ExerciseType;
import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;

/**
 * A Fudge builder for {@code ExerciseType}.
 */
@FudgeBuilderFor(ExerciseType.class)
public class ExerciseTypeBuilder extends AbstractFudgeBuilder implements FudgeBuilder<ExerciseType> {

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, ExerciseType object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    ExerciseTypeBuilder.toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static MutableFudgeMsg toFudgeMsg(final FudgeSerializer serializer, final ExerciseType object) {
    if (object == null) {
      return null;
    }
    final MutableFudgeMsg msg = serializer.newMessage();
    toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static void toFudgeMsg(final FudgeSerializer serializer, final ExerciseType object, final MutableFudgeMsg msg) {
    FudgeSerializer.addClassHeader(msg, object.getClass(), ExerciseType.class);
  }

  @Override
  public ExerciseType buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    return fromFudgeMsg(deserializer, msg);
  }

  public static ExerciseType fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg msg) {
    final String className = msg.getString(0);
    try {
      return (ExerciseType) Class.forName(className).newInstance();
    } catch (Exception th) {
      throw new OpenGammaRuntimeException("Unable to create ExerciseType: " + className, th);
    }
  }

}
