/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
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

import com.opengamma.financial.analytics.volatility.cube.CubeInstrumentProvider;
import com.opengamma.financial.analytics.volatility.cube.SwaptionVolatilityCubeSpecification;
import com.opengamma.id.UniqueIdentifiable;

/**
 * 
 */
@FudgeBuilderFor(SwaptionVolatilityCubeSpecification.class)
public class SwaptionVolatilityCubeSpecificationBuilder implements FudgeBuilder<SwaptionVolatilityCubeSpecification> {
  private static final String NAME_FIELD = "name";
  private static final String TARGET_FIELD = "target";
  private static final String CUBE_QUOTE_TYPE_FIELD = "cubeQuoteType";
  private static final String QUOTE_UNITS_FIELD = "quoteUnits";
  private static final String INSTRUMENT_PROVIDER_FIELD = "cubeInstrumentProvider";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final SwaptionVolatilityCubeSpecification object) {
    final MutableFudgeMsg message = serializer.newMessage();
    message.add(NAME_FIELD, object.getName());
    message.add(TARGET_FIELD, FudgeSerializer.addClassHeader(serializer.objectToFudgeMsg(object.getTarget()), object.getTarget().getClass()));
    message.add(CUBE_QUOTE_TYPE_FIELD, object.getCubeQuoteType());
    message.add(QUOTE_UNITS_FIELD, object.getQuoteUnits());
    serializer.addToMessageWithClassHeaders(message, INSTRUMENT_PROVIDER_FIELD, null, object.getCubeInstrumentProvider());
    return message;
  }

  @Override
  public SwaptionVolatilityCubeSpecification buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final String name = message.getString(NAME_FIELD);
    final UniqueIdentifiable target = deserializer.fieldValueToObject(UniqueIdentifiable.class, message.getByName(TARGET_FIELD));
    final String cubeQuoteType = message.getString(CUBE_QUOTE_TYPE_FIELD);
    final String quoteUnits = message.getString(QUOTE_UNITS_FIELD);
    final CubeInstrumentProvider<?, ?, ?> cubeInstrumentProvider = (CubeInstrumentProvider<?, ?, ?>) deserializer.fieldValueToObject(message.getByName(INSTRUMENT_PROVIDER_FIELD));
    return new SwaptionVolatilityCubeSpecification(name, target, cubeQuoteType, quoteUnits, cubeInstrumentProvider);
  }

}
