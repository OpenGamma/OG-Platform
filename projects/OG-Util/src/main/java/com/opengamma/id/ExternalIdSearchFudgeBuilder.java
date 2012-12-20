/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
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
 * Fudge builder for {@code ExternalIdSearch}.
 */
@FudgeBuilderFor(ExternalIdSearch.class)
public final class ExternalIdSearchFudgeBuilder extends AbstractFudgeBuilder implements FudgeBuilder<ExternalIdSearch> {

  /** Field name. */
  public static final String IDENTIFIERS_FIELD_NAME = "identifiers";
  /** Field name. */
  public static final String SEARCH_TYPE_FIELD_NAME = "searchType";

  //-------------------------------------------------------------------------
  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final ExternalIdSearch object) {
    return toFudgeMsg(serializer, object);
  }

  public static MutableFudgeMsg toFudgeMsg(final FudgeSerializer serializer, final ExternalIdSearch object) {
    if (object == null) {
      return null;
    }
    final MutableFudgeMsg msg = serializer.newMessage();
    toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static void toFudgeMsg(final FudgeSerializer serializer, final ExternalIdSearch object, final MutableFudgeMsg msg) {
    final MutableFudgeMsg ids = serializer.newMessage();
    for (ExternalId externalId : object.getExternalIds()) {
      addToMessage(ids, null, ExternalIdFudgeBuilder.toFudgeMsg(serializer, externalId));
    }
    addToMessage(msg, IDENTIFIERS_FIELD_NAME, ids);
    addToMessage(msg, SEARCH_TYPE_FIELD_NAME, object.getSearchType().name());
  }

  //-------------------------------------------------------------------------
  @Override
  public ExternalIdSearch buildObject(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    return fromFudgeMsg(deserializer, msg);
  }

  public static ExternalIdSearch fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    if (msg == null) {
      return null;
    }
    final FudgeMsg idMsg = msg.getMessage(IDENTIFIERS_FIELD_NAME);
    final String searchType = msg.getString(SEARCH_TYPE_FIELD_NAME);
    Set<ExternalId> ids = new HashSet<ExternalId>();
    for (FudgeField field : idMsg) {
      ids.add(ExternalIdFudgeBuilder.fromFudgeMsg((FudgeMsg) field.getValue()));
    }
    ExternalIdSearchType type = ExternalIdSearchType.valueOf(searchType);
    return new ExternalIdSearch(ids, type);
  }

}
