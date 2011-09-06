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
 * Fudge builder for {@code ExternalIdBundle}.
 */
@FudgeBuilderFor(ExternalIdBundle.class)
public final class ExternalIdBundleFudgeBuilder extends AbstractFudgeBuilder implements FudgeBuilder<ExternalIdBundle> {

  /** Field name. */
  public static final String ID_FIELD_NAME = "ID";

  //-------------------------------------------------------------------------
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, ExternalIdBundle object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static MutableFudgeMsg toFudgeMsg(final FudgeSerializer serializer, final ExternalIdBundle object) {
    if (object == null) {
      return null;
    }
    final MutableFudgeMsg msg = serializer.newMessage();
    toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static void toFudgeMsg(final FudgeSerializer serializer, final ExternalIdBundle object, final MutableFudgeMsg msg) {
    for (ExternalId externalId : object) {
      addToMessage(msg, ID_FIELD_NAME, ExternalIdFudgeBuilder.toFudgeMsg(serializer, externalId));
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public ExternalIdBundle buildObject(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    return fromFudgeMsg(deserializer, msg);
  }

  public static ExternalIdBundle fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    if (msg == null) {
      return null;
    }
    Set<ExternalId> ids = new HashSet<ExternalId>();
    for (FudgeField field : msg.getAllByName(ID_FIELD_NAME)) {
      ids.add(ExternalIdFudgeBuilder.fromFudgeMsg((FudgeMsg) field.getValue()));
    }
    return ExternalIdBundle.of(ids);
  }

}
