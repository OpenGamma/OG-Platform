/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import java.util.List;

import javax.time.Instant;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.id.VersionCorrection;

/**
 * Fudge message builder for {@link ViewCycleExecutionOptions}
 */
@FudgeBuilderFor(ViewCycleExecutionOptions.class)
public class ViewCycleExecutionOptionsFudgeBuilder implements FudgeBuilder<ViewCycleExecutionOptions> {

  private static final String VALUATION_TIME_FIELD = "valuation";
  private static final String MARKET_DATA_SPECIFICATIONS = "marketDataSpecifications";
  private static final String RESOLVER_VERSION_CORRECTION = "resolverVersionCorrection";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, ViewCycleExecutionOptions object) {
    MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessage(msg, VALUATION_TIME_FIELD, null, object.getValuationTime());
    serializer.addToMessageWithClassHeaders(msg, MARKET_DATA_SPECIFICATIONS, null, object.getMarketDataSpecifications());
    serializer.addToMessage(msg, RESOLVER_VERSION_CORRECTION, null, object.getResolverVersionCorrection());
    return msg;
  }

  @SuppressWarnings("unchecked")
  @Override
  public ViewCycleExecutionOptions buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    final ViewCycleExecutionOptions.Builder builder = ViewCycleExecutionOptions.builder();
    FudgeField field;
    field = msg.getByName(VALUATION_TIME_FIELD);
    if (field != null) {
      builder.setValuationTime(deserializer.fieldValueToObject(Instant.class, field));
    }
    field = msg.getByName(MARKET_DATA_SPECIFICATIONS);
    if (field != null) {
      builder.setMarketDataSpecifications(deserializer.fieldValueToObject(List.class, field));
    }
    field = msg.getByName(RESOLVER_VERSION_CORRECTION);
    if (field != null) {
      builder.setResolverVersionCorrection(deserializer.fieldValueToObject(VersionCorrection.class, field));
    }
    return builder.create();
  }

}
