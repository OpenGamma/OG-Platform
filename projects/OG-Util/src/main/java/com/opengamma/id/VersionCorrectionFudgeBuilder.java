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
import org.threeten.bp.Instant;

import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;

/**
 * Fudge builder for {@code VersionCorrection}.
 */
@FudgeBuilderFor(VersionCorrection.class)
public final class VersionCorrectionFudgeBuilder extends AbstractFudgeBuilder implements FudgeBuilder<VersionCorrection> {

  /** Field name. */
  public static final String VERSION_AS_OF_FIELD_NAME = "versionAsOf";
  /** Field name. */
  public static final String CORRECTED_TO_FIELD_NAME = "correctedTo";

  //-------------------------------------------------------------------------
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, VersionCorrection object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static MutableFudgeMsg toFudgeMsg(final FudgeSerializer serializer, final VersionCorrection object) {
    if (object == null) {
      return null;
    }
    final MutableFudgeMsg msg = serializer.newMessage();
    toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static void toFudgeMsg(final FudgeSerializer serializer, final VersionCorrection object, final MutableFudgeMsg msg) {
    addToMessage(msg, VERSION_AS_OF_FIELD_NAME, object.getVersionAsOf());
    addToMessage(msg, CORRECTED_TO_FIELD_NAME, object.getCorrectedTo());
  }

  //-------------------------------------------------------------------------
  @Override
  public VersionCorrection buildObject(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    return fromFudgeMsg(deserializer, msg);
  }

  public static VersionCorrection fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    if (msg == null) {
      return null;
    }
    final Instant versionAsOf = msg.getValue(Instant.class, VERSION_AS_OF_FIELD_NAME);
    final Instant correctedTo = msg.getValue(Instant.class, CORRECTED_TO_FIELD_NAME);
    return VersionCorrection.of(versionAsOf, correctedTo);
  }

}
