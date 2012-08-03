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

import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;

/**
 * Fudge message builder for {@link ViewCycleExecutionOptions}
 */
@FudgeBuilderFor(ViewCycleExecutionOptions.class)
public class ViewCycleExecutionOptionsFudgeBuilder implements FudgeBuilder<ViewCycleExecutionOptions> {

  private static final String VALUATION_TIME_FIELD = "valuation";
  private static final String MARKET_DATA_SPECIFICATIONS = "marketDataSpecifications";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, ViewCycleExecutionOptions object) {
    MutableFudgeMsg msg = serializer.newMessage();
    msg.add(VALUATION_TIME_FIELD, object.getValuationTime());
    serializer.addToMessageWithClassHeaders(msg, MARKET_DATA_SPECIFICATIONS, null, object.getMarketDataSpecifications());
    return msg;
  }

  @SuppressWarnings("unchecked")
  @Override
  public ViewCycleExecutionOptions buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    FudgeField valuationTimeField = msg.getByName(VALUATION_TIME_FIELD);
    Instant valuationTimeProvider;
    if (valuationTimeField != null) {
      valuationTimeProvider = deserializer.fieldValueToObject(Instant.class, valuationTimeField);
    } else {
      valuationTimeProvider = null;
    }
    FudgeField marketDataSpecificationField = msg.getByName(MARKET_DATA_SPECIFICATIONS);
    List<MarketDataSpecification> specs;
    if (marketDataSpecificationField != null) {
      specs = deserializer.fieldValueToObject(List.class, marketDataSpecificationField);
    } else {
      specs = null;
    }
    return new ViewCycleExecutionOptions(valuationTimeProvider, specs);
  }

}
