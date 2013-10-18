/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.engine.value.SurfaceAndCubePropertyNames;
import com.opengamma.financial.analytics.volatility.surface.SurfaceAndCubeQuoteType;
import com.opengamma.financial.analytics.volatility.surface.SurfaceInstrumentProvider;
import com.opengamma.financial.analytics.volatility.surface.VolatilitySurfaceSpecification;
import com.opengamma.financial.security.option.AmericanExerciseType;
import com.opengamma.financial.security.option.EuropeanExerciseType;
import com.opengamma.financial.security.option.ExerciseType;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.money.Currency;

/**
 * Builder for converting VolatilitySurfaceSpecification instances to/from Fudge messages.
 */
@FudgeBuilderFor(VolatilitySurfaceSpecification.class)
public class VolatilitySurfaceSpecificationFudgeBuilder implements FudgeBuilder<VolatilitySurfaceSpecification> {
  /** Field for whether or not to use underlying securities to get expiry */
  private static final String USE_UNDERLYING_SECURITY_FOR_EXPIRY = "useUnderlyingSecurityForExpiry";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final VolatilitySurfaceSpecification object) {
    final MutableFudgeMsg message = serializer.newMessage();
    message.add(null, 0, object.getClass().getName());
    // the following forces it not to use a secondary type if one is available.
    message.add("target", FudgeSerializer.addClassHeader(serializer.objectToFudgeMsg(object.getTarget()), object.getTarget().getClass()));
    // for compatibility with old code, remove.
    if (object.getTarget() instanceof Currency) {
      message.add("currency", object.getTarget());
    } else {
      // just for now...
      message.add("currency", Currency.USD);
    }
    message.add("name", object.getName());
    if (object.getSurfaceQuoteType() != null) {
      message.add("quote", object.getSurfaceQuoteType());
    }
    if (object.getQuoteUnits() != null) {
      message.add("quoteUnits", object.getQuoteUnits());
    }
    if (object.getExerciseType() != null) {
      message.add("exerciseType", object.getExerciseType().getName());
    }
    message.add(USE_UNDERLYING_SECURITY_FOR_EXPIRY, object.isUseUnderlyingSecurityForExpiry());
    serializer.addToMessageWithClassHeaders(message, "surfaceInstrumentProvider", null, object.getSurfaceInstrumentProvider());
    return message;
  }

  @Override
  public VolatilitySurfaceSpecification buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    UniqueIdentifiable target;
    if (!message.hasField("target")) {
      target = deserializer.fieldValueToObject(Currency.class, message.getByName("currency"));
    } else {
      target = deserializer.fieldValueToObject(UniqueIdentifiable.class, message.getByName("target"));
    }
    final String quoteType;
    if (message.hasField("quote") && message.getString("quote") != null) {
      quoteType = message.getString("quote");
    } else {
      quoteType = SurfaceAndCubeQuoteType.CALL_STRIKE;
    }
    final ExerciseType exerciseType;
    final ExerciseType american = new AmericanExerciseType();
    final ExerciseType european = new EuropeanExerciseType();
    if (message.hasField("exerciseType") && message.getString("exerciseType") != null) {
      final String exerciseTypeName = message.getString("exerciseType");
      exerciseType = exerciseTypeName.equalsIgnoreCase(american.getName()) ? american : european;
    } else {
      exerciseType = european;
    }
    final String quoteUnits;
    if (message.hasField("quoteUnits") && message.getString("quoteUnits") != null) {
      quoteUnits = message.getString("quoteUnits");
    } else {
      quoteUnits = SurfaceAndCubePropertyNames.VOLATILITY_QUOTE;
    }
    final String name = message.getString("name");
    final FudgeField field = message.getByName("surfaceInstrumentProvider");
    final SurfaceInstrumentProvider<?, ?> surfaceInstrumentProvider = (SurfaceInstrumentProvider<?, ?>) deserializer.fieldValueToObject(field);
    if (message.hasField(USE_UNDERLYING_SECURITY_FOR_EXPIRY)) {
      final boolean useUnderlyingSecurityForExpiry = message.getBoolean(USE_UNDERLYING_SECURITY_FOR_EXPIRY);
      return new VolatilitySurfaceSpecification(name, target, quoteType, quoteUnits, exerciseType, surfaceInstrumentProvider, useUnderlyingSecurityForExpiry);
    }
    return new VolatilitySurfaceSpecification(name, target, quoteType, quoteUnits, exerciseType, surfaceInstrumentProvider);
  }

}
