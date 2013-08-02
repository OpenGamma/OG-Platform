/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.future;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdBundleFudgeBuilder;
import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;

/**
 * A Fudge builder for {@code BondFutureDeliverable}.
 */
@FudgeBuilderFor(BondFutureDeliverable.class)
public class BondFutureDeliverableFudgeBuilder extends AbstractFudgeBuilder implements FudgeBuilder<BondFutureDeliverable> {

  /** Field name. */
  public static final String IDENTIFIERS_FIELD_NAME = "identifiers";
  /** Field name. */
  public static final String CONVERSION_FACTOR_FIELD_NAME = "conversionFactor";

  //-------------------------------------------------------------------------
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, BondFutureDeliverable object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    BondFutureDeliverableFudgeBuilder.toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static void toFudgeMsg(FudgeSerializer serializer, BondFutureDeliverable object, final MutableFudgeMsg msg) {
    addToMessage(msg, IDENTIFIERS_FIELD_NAME, ExternalIdBundleFudgeBuilder.toFudgeMsg(serializer, object.getIdentifiers()));
    addToMessage(msg, CONVERSION_FACTOR_FIELD_NAME, object.getConversionFactor());
  }

  //-------------------------------------------------------------------------
  @Override
  public BondFutureDeliverable buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    return fromFudgeMsg(deserializer, msg);
  }

  public static BondFutureDeliverable fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg msg) {
    ExternalIdBundle bundle = ExternalIdBundleFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(IDENTIFIERS_FIELD_NAME));
    double conversionFactor = msg.getDouble(CONVERSION_FACTOR_FIELD_NAME);
    return new BondFutureDeliverable(bundle, conversionFactor);
  }

}
