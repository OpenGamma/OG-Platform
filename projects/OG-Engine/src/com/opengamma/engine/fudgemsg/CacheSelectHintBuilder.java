/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.engine.view.cache.CacheSelectHint;

/**
 * Fudge message builder for {@code CacheSelectHint}.
 */
@FudgeBuilderFor(CacheSelectHint.class)
public class CacheSelectHintBuilder implements FudgeBuilder<CacheSelectHint> {

  private static final String VALUES_FIELD_NAME = "cacheValues";
  private static final String PRIVATE_FIELD_NAME = "cacheValuesPrivate";
  
  @Override
  public MutableFudgeFieldContainer buildMessage(FudgeSerializationContext context, CacheSelectHint object) {
    MutableFudgeFieldContainer msg = context.newMessage();
    msg.add(VALUES_FIELD_NAME, object.getValueIdentifiers());
    msg.add(PRIVATE_FIELD_NAME, object.isPrivate());
    return msg;
  }

  @Override
  public CacheSelectHint buildObject(FudgeDeserializationContext context, FudgeFieldContainer msg) {
    final long[] valueIdentifiers = (long[]) msg.getByName(VALUES_FIELD_NAME).getValue();
    final boolean isPrivate = msg.getBoolean(PRIVATE_FIELD_NAME);
    return CacheSelectHint.create(valueIdentifiers, isPrivate);
  }

}
