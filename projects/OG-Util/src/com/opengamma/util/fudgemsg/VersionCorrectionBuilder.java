/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg;

import javax.time.Instant;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.id.VersionCorrection;

/**
 * Fudge builder for {@code VersionCorrection}.
 */
@FudgeBuilderFor(VersionCorrection.class)
public final class VersionCorrectionBuilder implements FudgeBuilder<VersionCorrection> {

  /** Field name. */
  public static final String VERSION_AS_OF_FIELD_NAME = "versionAsOf";
  /** Field name. */
  public static final String CORRECTED_TO_FIELD_NAME = "correctedTo";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, VersionCorrection object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    if (object.getVersionAsOf() != null) {
      msg.add(VERSION_AS_OF_FIELD_NAME, object.getVersionAsOf());
    }
    if (object.getCorrectedTo() != null) {
      msg.add(CORRECTED_TO_FIELD_NAME, object.getCorrectedTo());
    }
    return msg;
  }

  @Override
  public VersionCorrection buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    final Instant versionAsOf = msg.getValue(Instant.class, VERSION_AS_OF_FIELD_NAME);
    final Instant correctedTo = msg.getValue(Instant.class, CORRECTED_TO_FIELD_NAME);
    return VersionCorrection.of(versionAsOf, correctedTo);
  }

}
