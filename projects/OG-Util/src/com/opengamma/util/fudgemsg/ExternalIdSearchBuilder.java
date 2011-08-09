/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg;

import java.util.HashSet;
import java.util.Set;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdSearch;
import com.opengamma.id.ExternalIdSearchType;

/**
 * Fudge builder for {@code ExternalIdSearch}.
 */
@FudgeBuilderFor(ExternalIdSearch.class)
public final class ExternalIdSearchBuilder implements FudgeBuilder<ExternalIdSearch> {

  /** Field name. */
  public static final String IDENTIFIERS_KEY = "identifiers";
  /** Field name. */
  public static final String SEARCH_TYPE_KEY = "searchType";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, ExternalIdSearch object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    final MutableFudgeMsg ids = serializer.newMessage();
    for (ExternalId identifier : object.getExternalIds()) {
      serializer.addToMessage(ids, null, null, identifier);
    }
    serializer.addToMessage(msg, IDENTIFIERS_KEY, null, ids);
    serializer.addToMessage(msg, SEARCH_TYPE_KEY, null, object.getSearchType().name());
    return msg;
  }

  @Override
  public ExternalIdSearch buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    final FudgeMsg idMsg = msg.getMessage(IDENTIFIERS_KEY);
    if (idMsg == null) {
      throw new IllegalArgumentException("Fudge message is not a ExternalIdSearch - field 'identifiers' is not present");
    }
    final String searchType = msg.getString(SEARCH_TYPE_KEY);
    if (searchType == null) {
      throw new IllegalArgumentException("Fudge message is not a ExternalIdSearch - field 'searchType' is not present");
    }
    final Set<ExternalId> identifiers = new HashSet<ExternalId>();
    for (FudgeField field : idMsg) {
      identifiers.add(deserializer.fieldValueToObject(ExternalId.class, field));
    }
    ExternalIdSearchType type = ExternalIdSearchType.valueOf(msg.getString("searchType"));
    return new ExternalIdSearch(identifiers, type);
  }

}
