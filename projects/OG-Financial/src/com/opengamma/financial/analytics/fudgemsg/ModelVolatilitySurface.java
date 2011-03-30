/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

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
    protected void buildMessage(final FudgeSerializationContext context, final MutableFudgeFieldContainer message, final VolatilitySurface object) {
      context.objectToFudgeMsgWithClassHeaders(message, SURFACE_FIELD_NAME, null, object.getSurface(), Surface.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public VolatilitySurface buildObject(final FudgeDeserializationContext context, final FudgeFieldContainer message) {
      final Surface<Double, Double, Double> surface = context.fieldValueToObject(Surface.class, message.getByName(SURFACE_FIELD_NAME));
      return new VolatilitySurface(surface);
    }
  }
}
