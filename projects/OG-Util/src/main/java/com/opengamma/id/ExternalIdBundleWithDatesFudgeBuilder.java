/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import java.util.HashSet;
import java.util.Set;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;

/**
 * Fudge builder for {@code ExternalIdBundleWithDates}.
 */
@FudgeBuilderFor(ExternalIdBundleWithDates.class)
public final class ExternalIdBundleWithDatesFudgeBuilder extends AbstractFudgeBuilder implements FudgeBuilder<ExternalIdBundleWithDates> {

  /** Field name. */
  public static final String ID_FIELD_NAME = "ID";

  //-------------------------------------------------------------------------
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, ExternalIdBundleWithDates object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static MutableFudgeMsg toFudgeMsg(final FudgeSerializer serializer, final ExternalIdBundleWithDates object) {
    if (object == null) {
      return null;
    }
    final MutableFudgeMsg msg = serializer.newMessage();
    toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static void toFudgeMsg(final FudgeSerializer serializer, final ExternalIdBundleWithDates object, final MutableFudgeMsg msg) {
    for (ExternalIdWithDates externalId : object) {
      addToMessage(msg, ID_FIELD_NAME, ExternalIdWithDatesFudgeBuilder.toFudgeMsg(serializer, externalId));
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public ExternalIdBundleWithDates buildObject(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    return fromFudgeMsg(deserializer, msg);
  }

  public static ExternalIdBundleWithDates fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    if (msg == null) {
      return null;
    }
    Set<ExternalIdWithDates> ids = new HashSet<ExternalIdWithDates>();
    for (FudgeField field : msg.getAllByName(ID_FIELD_NAME)) {
      ids.add(ExternalIdWithDatesFudgeBuilder.fromFudgeMsg(deserializer, (FudgeMsg) field.getValue()));
    }
    return ExternalIdBundleWithDates.of(ids);
  }

}
