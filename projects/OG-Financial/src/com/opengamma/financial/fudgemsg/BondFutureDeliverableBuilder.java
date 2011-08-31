/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.financial.security.future.BondFutureDeliverable;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;
import com.opengamma.util.fudgemsg.ExternalIdBundleBuilder;

/**
 * A Fudge builder for {@code BondFutureDeliverable}.
 */
@FudgeBuilderFor(BondFutureDeliverable.class)
public class BondFutureDeliverableBuilder extends AbstractFudgeBuilder implements FudgeBuilder<BondFutureDeliverable> {

  /** Field name. */
  public static final String IDENTIFIERS_KEY = "identifiers";
  /** Field name. */
  public static final String CONVERSION_FACTOR_KEY = "conversionFactor";

  //-------------------------------------------------------------------------
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, BondFutureDeliverable object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    BondFutureDeliverableBuilder.toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static void toFudgeMsg(FudgeSerializer serializer, BondFutureDeliverable object, final MutableFudgeMsg msg) {
    addToMessage(msg, IDENTIFIERS_KEY, ExternalIdBundleBuilder.toFudgeMsg(serializer, object.getIdentifiers()));
    addToMessage(msg, CONVERSION_FACTOR_KEY, object.getConversionFactor());
  }

  //-------------------------------------------------------------------------
  @Override
  public BondFutureDeliverable buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    return fromFudgeMsg(deserializer, msg);
  }

  public static BondFutureDeliverable fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg msg) {
    ExternalIdBundle bundleI = ExternalIdBundleBuilder.fromFudgeMsg(deserializer, msg.getMessage(IDENTIFIERS_KEY));
    double conversionFactor = msg.getDouble(CONVERSION_FACTOR_KEY);
    return new BondFutureDeliverable(bundleI, conversionFactor);
  }

}
