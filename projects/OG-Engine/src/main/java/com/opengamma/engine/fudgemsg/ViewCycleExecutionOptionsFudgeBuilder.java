/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import java.util.ArrayList;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.threeten.bp.Instant;

import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.id.VersionCorrection;

/**
 * Fudge message builder for {@link ViewCycleExecutionOptions}
 */
@FudgeBuilderFor(ViewCycleExecutionOptions.class)
public class ViewCycleExecutionOptionsFudgeBuilder implements FudgeBuilder<ViewCycleExecutionOptions> {

  private static final String VALUATION_TIME_FIELD = "valuation";
  private static final String RESOLVER_VERSION_CORRECTION = "resolverVersionCorrection";
  private static final String MARKET_DATA_SPECIFICATION = "marketDataSpecification";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final ViewCycleExecutionOptions object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessage(msg, VALUATION_TIME_FIELD, null, object.getValuationTime());
    for (final MarketDataSpecification spec : object.getMarketDataSpecifications()) {
      serializer.addToMessageWithClassHeaders(msg, MARKET_DATA_SPECIFICATION, null, spec);
    }
    serializer.addToMessage(msg, RESOLVER_VERSION_CORRECTION, null, object.getResolverVersionCorrection());
    return msg;
  }

  @Override
  public ViewCycleExecutionOptions buildObject(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    final ViewCycleExecutionOptions.Builder builder = ViewCycleExecutionOptions.builder();
    FudgeField field;
    field = msg.getByName(VALUATION_TIME_FIELD);
    if (field != null) {
      builder.setValuationTime(deserializer.fieldValueToObject(Instant.class, field));
    }
    final ArrayList<MarketDataSpecification> specs = new ArrayList<>();
    for (final FudgeField marketDataSpecificationField : msg.getAllByName(MARKET_DATA_SPECIFICATION)) {
      specs.add(deserializer.fieldValueToObject(MarketDataSpecification.class, marketDataSpecificationField));
    }
    builder.setMarketDataSpecifications(specs);
    field = msg.getByName(RESOLVER_VERSION_CORRECTION);
    if (field != null) {
      builder.setResolverVersionCorrection(deserializer.fieldValueToObject(VersionCorrection.class, field));
    }
    return builder.create();
  }

}
