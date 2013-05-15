/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.snapshot;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.threeten.bp.Period;

import com.opengamma.core.marketdatasnapshot.ValueSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.ManageableVolatilitySurfaceSnapshot;
import com.opengamma.financial.analytics.volatility.surface.BloombergFXOptionVolatilitySurfaceInstrumentProvider.FXVolQuoteType;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.error.InvokeInvalidArgumentException;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;

/**
 * Updates a point on a "volatility surface"
 */
public class SetVolatilitySurfacePointFunction extends AbstractFunctionInvoker implements PublishedFunction {

  /**
   * Default instance.
   */
  public static final SetVolatilitySurfacePointFunction INSTANCE = new SetVolatilitySurfacePointFunction();

  /**
   * Coordinate types that the function recognises.
   */
  public static enum ObjectType {
    /**
     * Values of type Double.
     */
    DOUBLE,
    /**
     * Values of type Tenor.
     */
    TENOR,
    /**
     * Values of type Pair<Integer, FXVolQuoteType>.
     */
    INTEGER_FXVOLQUOTETYPE_PAIR;
  }

  private final MetaFunction _meta;

  private static final int SNAPSHOT = 0;
  private static final int X = 1;
  private static final int Y = 2;
  private static final int OVERRIDE_VALUE = 3;
  private static final int MARKET_VALUE = 4;
  private static final int X_CLASS = 5;
  private static final int Y_CLASS = 6;

  private static List<MetaParameter> parameters() {
    return Arrays.asList(
        new MetaParameter("snapshot", JavaTypeInfo.builder(ManageableVolatilitySurfaceSnapshot.class).get()),
        new MetaParameter("x", JavaTypeInfo.builder(String.class).get()),
        new MetaParameter("y", JavaTypeInfo.builder(String.class).get()),
        new MetaParameter("overrideValue", JavaTypeInfo.builder(Double.class).allowNull().get()),
        new MetaParameter("marketValue", JavaTypeInfo.builder(Double.class).allowNull().get()),
        new MetaParameter("xc", JavaTypeInfo.builder(ObjectType.class).allowNull().get()),
        new MetaParameter("yc", JavaTypeInfo.builder(ObjectType.class).allowNull().get()));
  }

  private SetVolatilitySurfacePointFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.MARKET_DATA, "SetVolatilitySurfacePoint", getParameters(), this));
  }

  protected SetVolatilitySurfacePointFunction() {
    this(new DefinitionAnnotater(SetVolatilitySurfacePointFunction.class));
  }

  private static Object convert(final ObjectType type, final String str, final int index) {
    try {
      switch (type) {
        case DOUBLE:
          return new Double(Double.parseDouble(str));
        case TENOR:
          return new Tenor(Period.parse(str));
        case INTEGER_FXVOLQUOTETYPE_PAIR: {
          final String[] parts = str.split(", ");
          return Pair.of(Integer.parseInt(parts[0]), FXVolQuoteType.valueOf(parts[1]));
        }
        default:
          throw new IllegalStateException();
      }
    } catch (RuntimeException e) {
      throw new InvokeInvalidArgumentException(index, e.getMessage());
    }
  }

  public static ManageableVolatilitySurfaceSnapshot invoke(final ManageableVolatilitySurfaceSnapshot snapshot, final String x, final String y,
      final Double overrideValue, final Double marketValue, final ObjectType xType, final ObjectType yType) {
    final Pair<Object, Object> key = Pair.of(convert(xType, x, X_CLASS), convert(yType, y, Y_CLASS));
    if (snapshot.getValues() == null) {
      snapshot.setValues(new HashMap<Pair<Object, Object>, ValueSnapshot>());
    }
    if (marketValue != null) {
      snapshot.getValues().put(key, new ValueSnapshot(marketValue, overrideValue));
    } else {
      final ValueSnapshot value = snapshot.getValues().get(key);
      if (value != null) {
        value.setOverrideValue(overrideValue);
      } else {
        snapshot.getValues().put(key, new ValueSnapshot(overrideValue, overrideValue));
      }
    }
    return snapshot;
  }

  public static ManageableVolatilitySurfaceSnapshot invoke(final ManageableVolatilitySurfaceSnapshot snapshot, final String x, final String y, final Double overrideValue, final Double marketValue) {
    for (Map.Entry<Pair<Object, Object>, ValueSnapshot> surfacePoint : snapshot.getValues().entrySet()) {
      final Object xObject = surfacePoint.getKey().getFirst();
      if (x.equals(StructuredMarketDataSnapshotUtil.toString(xObject))) {
        final Object yObject = surfacePoint.getKey().getSecond();
        if (y.equals(StructuredMarketDataSnapshotUtil.toString(yObject))) {
          if (marketValue != null) {
            snapshot.getValues().put(surfacePoint.getKey(), new ValueSnapshot(marketValue, overrideValue));
          } else {
            surfacePoint.getValue().setOverrideValue(overrideValue);
          }
          break;
        }
      }
    }
    return snapshot;
  }

  // AbstractFunctionInvoker

  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    if ((parameters[X_CLASS] != null) || (parameters[Y_CLASS] != null)) {
      if (parameters[X_CLASS] == null) {
        throw new InvokeInvalidArgumentException(X_CLASS, "Must specify xc as well as yc");
      }
      if (parameters[Y_CLASS] == null) {
        throw new InvokeInvalidArgumentException(Y_CLASS, "Must specify yc as well as xc");
      }
      return invoke((ManageableVolatilitySurfaceSnapshot) parameters[SNAPSHOT], (String) parameters[X], (String) parameters[Y],
          (Double) parameters[OVERRIDE_VALUE], (Double) parameters[MARKET_VALUE], (ObjectType) parameters[X_CLASS], (ObjectType) parameters[Y_CLASS]);
    } else {
      return invoke((ManageableVolatilitySurfaceSnapshot) parameters[SNAPSHOT], (String) parameters[X], (String) parameters[Y], (Double) parameters[OVERRIDE_VALUE], (Double) parameters[MARKET_VALUE]);
    }
  }

  // PublishedFunction

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

}
