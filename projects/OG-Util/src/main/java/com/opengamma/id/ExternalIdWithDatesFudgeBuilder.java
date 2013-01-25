/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.threeten.bp.LocalDate;

import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;

/**
 * Fudge builder for {@code ExternalId}.
 */
@FudgeBuilderFor(ExternalIdWithDates.class)
public final class ExternalIdWithDatesFudgeBuilder extends AbstractFudgeBuilder implements FudgeBuilder<ExternalIdWithDates> {

  /** Field name. */
  public static final String VALID_FROM_FIELD_NAME = "ValidFrom";
  /** Field name. */
  public static final String VALID_TO_FIELD_NAME = "ValidTo";

  //-------------------------------------------------------------------------
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, ExternalIdWithDates object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static MutableFudgeMsg toFudgeMsg(final FudgeSerializer serializer, final ExternalIdWithDates object) {
    if (object == null) {
      return null;
    }
    final MutableFudgeMsg msg = serializer.newMessage();
    toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static void toFudgeMsg(final FudgeSerializer serializer, final ExternalIdWithDates object, final MutableFudgeMsg msg) {
    ExternalIdFudgeBuilder.toFudgeMsg(serializer, object.getExternalId(), msg);
    addToMessage(msg, VALID_FROM_FIELD_NAME, object.getValidFrom());
    addToMessage(msg, VALID_TO_FIELD_NAME, object.getValidTo());
  }

  //-------------------------------------------------------------------------
  @Override
  public ExternalIdWithDates buildObject(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    return fromFudgeMsg(deserializer, msg);
  }

  public static ExternalIdWithDates fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    if (msg == null) {
      return null;
    }
    ExternalId identifier = ExternalIdFudgeBuilder.fromFudgeMsg(msg);
    LocalDate validFrom = msg.getValue(LocalDate.class, VALID_FROM_FIELD_NAME);
    LocalDate validTo = msg.getValue(LocalDate.class, VALID_TO_FIELD_NAME);
    return ExternalIdWithDates.of(identifier, validFrom, validTo);
  }

}
