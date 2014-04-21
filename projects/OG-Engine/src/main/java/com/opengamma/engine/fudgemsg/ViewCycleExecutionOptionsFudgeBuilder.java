/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import java.util.ArrayList;
import java.util.Map;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.threeten.bp.Instant;

import com.google.common.collect.Maps;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.marketdata.manipulator.DistinctMarketDataSelector;
import com.opengamma.engine.marketdata.manipulator.MarketDataSelector;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.id.VersionCorrection;

/**
 * Fudge message builder for {@link ViewCycleExecutionOptions}
 */
@FudgeBuilderFor(ViewCycleExecutionOptions.class)
public class ViewCycleExecutionOptionsFudgeBuilder implements FudgeBuilder<ViewCycleExecutionOptions> {

  private static final String NAME = "name";
  private static final String VALUATION_TIME_FIELD = "valuation";
  private static final String RESOLVER_VERSION_CORRECTION = "resolverVersionCorrection";
  private static final String MARKET_DATA_SPECIFICATION = "marketDataSpecification";
  private static final String MARKET_DATA_SELECTOR = "marketDataSelector";
  private static final String FUNCTION_PARAMETERS = "functionParameters";
  private static final String SELECTOR = "selector";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final ViewCycleExecutionOptions object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessage(msg, NAME, null, object.getName());
    serializer.addToMessage(msg, VALUATION_TIME_FIELD, null, object.getValuationTime());
    for (final MarketDataSpecification spec : object.getMarketDataSpecifications()) {
      serializer.addToMessageWithClassHeaders(msg, MARKET_DATA_SPECIFICATION, null, spec);
    }
    serializer.addToMessageWithClassHeaders(msg, MARKET_DATA_SELECTOR, null, object.getMarketDataSelector());
    serializer.addToMessage(msg, RESOLVER_VERSION_CORRECTION, null, object.getResolverVersionCorrection());
    if (!object.getFunctionParameters().isEmpty()) {
      MutableFudgeMsg parametersMsg = serializer.newMessage();
      for (Map.Entry<DistinctMarketDataSelector, FunctionParameters> entry : object.getFunctionParameters().entrySet()) {
        MutableFudgeMsg entryMsg = serializer.newMessage();
        serializer.addToMessageWithClassHeaders(entryMsg, SELECTOR, null, entry.getKey());
        serializer.addToMessageWithClassHeaders(entryMsg, FUNCTION_PARAMETERS, null, entry.getValue());
        serializer.addToMessage(parametersMsg, FUNCTION_PARAMETERS, null, entryMsg);
      }
      serializer.addToMessage(msg, FUNCTION_PARAMETERS, null, parametersMsg);
    }
    return msg;
  }

  @Override
  public ViewCycleExecutionOptions buildObject(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    final ViewCycleExecutionOptions.Builder builder = ViewCycleExecutionOptions.builder();
    FudgeField field;
    field = msg.getByName(NAME);
    if (field != null) {
      builder.setName(deserializer.fieldValueToObject(String.class, field));
    }
    field = msg.getByName(VALUATION_TIME_FIELD);
    if (field != null) {
      builder.setValuationTime(deserializer.fieldValueToObject(Instant.class, field));
    }
    final ArrayList<MarketDataSpecification> specs = new ArrayList<>();
    for (final FudgeField marketDataSpecificationField : msg.getAllByName(MARKET_DATA_SPECIFICATION)) {
      specs.add(deserializer.fieldValueToObject(MarketDataSpecification.class, marketDataSpecificationField));
    }
    builder.setMarketDataSpecifications(specs);
    field = msg.getByName(MARKET_DATA_SELECTOR);
    if (field != null) {
      builder.setMarketDataSelector(deserializer.fieldValueToObject(MarketDataSelector.class, field));
    }
    field = msg.getByName(RESOLVER_VERSION_CORRECTION);
    if (field != null) {
      builder.setResolverVersionCorrection(deserializer.fieldValueToObject(VersionCorrection.class, field));
    }
    field = msg.getByName(FUNCTION_PARAMETERS);
    if (field != null) {
      Map<DistinctMarketDataSelector, FunctionParameters> paramMap = Maps.newHashMap();
      FudgeMsg paramsMsg = (FudgeMsg) field.getValue();
      for (FudgeField paramsField : paramsMsg) {
        FudgeMsg paramMsg = (FudgeMsg) paramsField.getValue();
        DistinctMarketDataSelector selector = deserializer.fieldValueToObject(DistinctMarketDataSelector.class,
                                                                              paramMsg.getByName(SELECTOR));
        FunctionParameters params = deserializer.fieldValueToObject(FunctionParameters.class,
                                                                    paramMsg.getByName(FUNCTION_PARAMETERS));
        paramMap.put(selector, params);
      }
      builder.setFunctionParameters(paramMap);
    }
    return builder.create();
  }

}
