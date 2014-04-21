/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.core.convention.ConventionType;

/**
 * Fudge builder for {@code ConventionType}.
 */
@FudgeBuilderFor(ConventionType.class)
public final class ConventionTypeFudgeBuilder implements FudgeBuilder<ConventionType> {

  /** Field name. */
  public static final String CONVENTION_TYPE_FIELD_NAME = "conventionType";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, ConventionType object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    FudgeSerializer.addClassHeader(msg, ConventionType.class);
    serializer.addToMessage(msg, CONVENTION_TYPE_FIELD_NAME, null, object.getName());
    return msg;
  }

  @Override
  public ConventionType buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    final String conventionTypeStr = msg.getString(CONVENTION_TYPE_FIELD_NAME);
    if (conventionTypeStr == null) {
      throw new IllegalArgumentException("Fudge message is not a ConventionType - field 'conventionType' is not present");
    }
    return ConventionType.of(conventionTypeStr);
  }

}
