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

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.volatility.local.LocalVolatilitySurfaceMoneyness;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.sabr.SmileSurfaceDataBundle;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceMoneyness;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceMoneynessFcnBackedByGrid;
import com.opengamma.analytics.financial.model.volatility.surface.PureImpliedVolatilitySurface;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurfaceInterpolator;
import com.opengamma.analytics.math.surface.Surface;

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

    @Override
    public VolatilitySurface buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final Surface<Double, Double, Double> surface = deserializer.fieldValueToObject(Surface.class, message.getByName(SURFACE_FIELD_NAME));
      return new VolatilitySurface(surface);
    }
  }

  @FudgeBuilderFor(BlackVolatilitySurfaceMoneyness.class)
  public static final class BlackVolatilitySurfaceMoneynessBuilder extends AbstractFudgeBuilder<BlackVolatilitySurfaceMoneyness> {
    private static final String SURFACE_FIELD_NAME = "volatility";
    private static final String FORWARD_CURVE_FIELD_NAME = "forwardCurve";

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final BlackVolatilitySurfaceMoneyness object) {
      serializer.addToMessage(message, SURFACE_FIELD_NAME, null, object.getSurface());
      serializer.addToMessage(message, FORWARD_CURVE_FIELD_NAME, null, object.getForwardCurve());
    }

    @SuppressWarnings("unchecked")
    @Override
    public BlackVolatilitySurfaceMoneyness buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final Object object = deserializer.fieldValueToObject(message.getByName(SURFACE_FIELD_NAME));
      if (object instanceof Surface) {
        final ForwardCurve forwardCurve = deserializer.fieldValueToObject(ForwardCurve.class, message.getByName(FORWARD_CURVE_FIELD_NAME));
        return new BlackVolatilitySurfaceMoneyness((Surface<Double, Double, Double>) object, forwardCurve);
      } else if (object instanceof BlackVolatilitySurfaceMoneyness) {
        return new BlackVolatilitySurfaceMoneyness((BlackVolatilitySurfaceMoneyness) object);
      }
      throw new OpenGammaRuntimeException("Could not deserialize object " + object);
    }
  }

  @FudgeBuilderFor(BlackVolatilitySurfaceMoneynessFcnBackedByGrid.class)
  public static final class BlackVolatilitySurfaceMoneynessWithGridBuilder extends AbstractFudgeBuilder<BlackVolatilitySurfaceMoneynessFcnBackedByGrid> {
    private static final String SURFACE_FIELD_NAME = "volatility";
    private static final String FORWARD_CURVE_FIELD_NAME = "forwardCurve";
    private static final String GRID_FIELD_NAME = "gridData";
    private static final String INTERPOLATOR_FIELD_NAME = "interpolator";

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final BlackVolatilitySurfaceMoneynessFcnBackedByGrid object) {
      serializer.addToMessage(message, SURFACE_FIELD_NAME, null, object.getSurface());
      serializer.addToMessage(message, FORWARD_CURVE_FIELD_NAME, null, object.getForwardCurve());
      serializer.addToMessage(message, GRID_FIELD_NAME, null, object.getGridData());
      serializer.addToMessage(message, INTERPOLATOR_FIELD_NAME, null, object.getInterpolator());

    }

    @SuppressWarnings("unchecked")
    @Override
    public BlackVolatilitySurfaceMoneynessFcnBackedByGrid buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final Object object = deserializer.fieldValueToObject(message.getByName(SURFACE_FIELD_NAME));
      if (object instanceof Surface) {
        final ForwardCurve forwardCurve = deserializer.fieldValueToObject(ForwardCurve.class, message.getByName(FORWARD_CURVE_FIELD_NAME));
        final SmileSurfaceDataBundle grid = deserializer.fieldValueToObject(SmileSurfaceDataBundle.class, message.getByName(GRID_FIELD_NAME));
        final VolatilitySurfaceInterpolator interpolator = deserializer.fieldValueToObject(VolatilitySurfaceInterpolator.class, message.getByName(INTERPOLATOR_FIELD_NAME));
        return new BlackVolatilitySurfaceMoneynessFcnBackedByGrid((Surface<Double, Double, Double>) object, forwardCurve, grid, interpolator);
      } else if (object instanceof BlackVolatilitySurfaceMoneynessFcnBackedByGrid) {
        return new BlackVolatilitySurfaceMoneynessFcnBackedByGrid((BlackVolatilitySurfaceMoneynessFcnBackedByGrid) object);
      }
      throw new OpenGammaRuntimeException("Could not deserialize object " + object);
    }

  }

  @FudgeBuilderFor(LocalVolatilitySurfaceMoneyness.class)
  public static final class LocalVolatilitySurfaceMoneynessBuilder extends AbstractFudgeBuilder<LocalVolatilitySurfaceMoneyness> {
    private static final String SURFACE_FIELD_NAME = "volatility";
    private static final String FORWARD_CURVE_FIELD_NAME = "forwardCurve";

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final LocalVolatilitySurfaceMoneyness object) {
      serializer.addToMessage(message, SURFACE_FIELD_NAME, null, object.getSurface());
      serializer.addToMessage(message, FORWARD_CURVE_FIELD_NAME, null, object.getForwardCurve());
    }

    @SuppressWarnings("unchecked")
    @Override
    public LocalVolatilitySurfaceMoneyness buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final Object object = deserializer.fieldValueToObject(message.getByName(SURFACE_FIELD_NAME));
      if (object instanceof Surface) {
        final ForwardCurve forwardCurve = deserializer.fieldValueToObject(ForwardCurve.class, message.getByName(FORWARD_CURVE_FIELD_NAME));
        return new LocalVolatilitySurfaceMoneyness((Surface<Double, Double, Double>) object, forwardCurve);
      } else if (object instanceof LocalVolatilitySurfaceMoneyness) {
        return new LocalVolatilitySurfaceMoneyness((LocalVolatilitySurfaceMoneyness) object);
      }
      throw new OpenGammaRuntimeException("Could not deserialize object " + object);
    }

  }

  /**
   * Fudge builder for {@code PureImpliedVolatilitySurface}
   */
  @FudgeBuilderFor(PureImpliedVolatilitySurface.class)
  public static final class PureImpliedVolatilitySurfaceBuilder extends AbstractFudgeBuilder<PureImpliedVolatilitySurface> {
    private static final String SURFACE_FIELD_NAME = "surface";

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final PureImpliedVolatilitySurface object) {
      serializer.addToMessageWithClassHeaders(message, SURFACE_FIELD_NAME, null, object.getSurface(), Surface.class);
    }

    @Override
    public PureImpliedVolatilitySurface buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final Surface<Double, Double, Double> surface = deserializer.fieldValueToObject(Surface.class, message.getByName(SURFACE_FIELD_NAME));
      return new PureImpliedVolatilitySurface(surface);
    }
  }
}
