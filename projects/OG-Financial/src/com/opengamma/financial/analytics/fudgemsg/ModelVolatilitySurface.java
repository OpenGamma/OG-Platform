/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.math.surface.Surface;

/**
 * Holds Fudge builders for the volatility surface model.
 */
/* package */final class ModelVolatilitySurface {

  /**
   * Restricted constructor.
   */
  private ModelVolatilitySurface() {
  }

  //-------------------------------------------------------------------------
  /**
   * Fudge builder for {@code VolatilitySurface}.
   */
  @FudgeBuilderFor(VolatilitySurface.class)
  public static final class VolatilitySurfaceBuilder extends AbstractFudgeBuilder<VolatilitySurface> {
    private static final String SURFACE_FIELD_NAME = "sigma";

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final VolatilitySurface object) {
      serializer.addToMessageWithClassHeaders(message, SURFACE_FIELD_NAME, null, object.getSurface(), Surface.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public VolatilitySurface buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final Surface<Double, Double, Double> surface = deserializer.fieldValueToObject(Surface.class, message.getByName(SURFACE_FIELD_NAME));
      return new VolatilitySurface(surface);
    }
  }
}
