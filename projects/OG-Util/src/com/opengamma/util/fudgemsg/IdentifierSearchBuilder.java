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
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierSearch;
import com.opengamma.id.IdentifierSearchType;

/**
 * Fudge builder for {@code IdentifierSearch}.
 */
@FudgeBuilderFor(IdentifierSearch.class)
public final class IdentifierSearchBuilder implements FudgeBuilder<IdentifierSearch> {

  /** Field name. */
  public static final String IDENTIFIERS_KEY = "identifiers";
  /** Field name. */
  public static final String SEARCH_TYPE_KEY = "searchType";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializationContext context, IdentifierSearch object) {
    final MutableFudgeMsg msg = context.newMessage();
    final MutableFudgeMsg ids = context.newMessage();
    for (Identifier identifier : object.getIdentifiers()) {
      context.addToMessage(ids, null, null, identifier);
    }
    context.addToMessage(msg, IDENTIFIERS_KEY, null, ids);
    context.addToMessage(msg, SEARCH_TYPE_KEY, null, object.getSearchType().name());
    return msg;
  }

  @Override
  public IdentifierSearch buildObject(FudgeDeserializationContext context, FudgeMsg msg) {
    final FudgeMsg idMsg = msg.getMessage(IDENTIFIERS_KEY);
    if (idMsg == null) {
      throw new IllegalArgumentException("Fudge message is not a IdentifierSearch - field 'identifiers' is not present");
    }
    final String searchType = msg.getString(SEARCH_TYPE_KEY);
    if (searchType == null) {
      throw new IllegalArgumentException("Fudge message is not a IdentifierSearch - field 'searchType' is not present");
    }
    final Set<Identifier> identifiers = new HashSet<Identifier>();
    for (FudgeField field : idMsg) {
      identifiers.add(context.fieldValueToObject(Identifier.class, field));
    }
    IdentifierSearchType type = IdentifierSearchType.valueOf(msg.getString("searchType"));
    return new IdentifierSearch(identifiers, type);
  }

}
