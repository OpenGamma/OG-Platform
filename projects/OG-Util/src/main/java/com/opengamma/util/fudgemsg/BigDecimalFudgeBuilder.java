/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg;

import java.math.BigDecimal;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

/**
 * Builder for big decimals
 */
@FudgeBuilderFor(BigDecimal.class)
public class BigDecimalFudgeBuilder implements FudgeBuilder<BigDecimal> {

  /** Field name. */
  public static final String BIG_DECIMAL_FIELD_NAME = "bigdecimal";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, BigDecimal object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessage(msg, BIG_DECIMAL_FIELD_NAME, null, object);
    return msg;
  }

  @Override
  public BigDecimal buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    final BigDecimal bd = msg.getValue(BigDecimal.class, BIG_DECIMAL_FIELD_NAME);
    if (bd == null) {
      throw new IllegalArgumentException("Fudge message is not a BigDecimal - field 'bigdecimal' is not present");
    }
    return bd;
  }


}
