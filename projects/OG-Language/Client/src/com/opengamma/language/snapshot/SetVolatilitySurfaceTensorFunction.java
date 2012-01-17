/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.snapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.opengamma.core.marketdatasnapshot.ValueSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.ManageableVolatilitySurfaceSnapshot;
import com.opengamma.language.Value;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.error.InvokeInvalidArgumentException;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;
import com.opengamma.util.tuple.Pair;

/**
 * Modifies a volatility surface to take values from the updated 2D matrix tensor.
 */
public class SetVolatilitySurfaceTensorFunction extends AbstractFunctionInvoker implements PublishedFunction {

  /**
   * Default instance.
   */
  public static final SetVolatilitySurfaceTensorFunction INSTANCE = new SetVolatilitySurfaceTensorFunction();

  private final MetaFunction _meta;

  private static List<MetaParameter> parameters() {
    return Arrays.asList(
        new MetaParameter("snapshot", JavaTypeInfo.builder(ManageableVolatilitySurfaceSnapshot.class).get()),
        new MetaParameter("overrideValue", JavaTypeInfo.builder(Value.class).arrayOf().arrayOf().allowNull().get()),
        new MetaParameter("marketValue", JavaTypeInfo.builder(Value.class).arrayOf().arrayOf().allowNull().get()));
  }

  private SetVolatilitySurfaceTensorFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.MARKET_DATA, "SetVolatilitySurfaceTensor", getParameters(), this));
  }

  protected SetVolatilitySurfaceTensorFunction() {
    this(new DefinitionAnnotater(SetVolatilitySurfaceTensorFunction.class));
  }

  @SuppressWarnings("unchecked")
  public static ManageableVolatilitySurfaceSnapshot invoke(final ManageableVolatilitySurfaceSnapshot snapshot, final Value[][] overrideValue, final Value[][] marketValue) {
    final Set<Comparable<Object>> keyXSet = new HashSet<Comparable<Object>>();
    final Set<Comparable<Object>> keyYSet = new HashSet<Comparable<Object>>();
    for (Pair<Object, Object> key : snapshot.getValues().keySet()) {
      if (key.getFirst() instanceof Comparable) {
        keyXSet.add((Comparable<Object>) key.getFirst());
      } else {
        throw new InvokeInvalidArgumentException(0, "surface X key '" + key.getFirst() + "' is not comparable");
      }
      if (key.getSecond() instanceof Comparable) {
        keyYSet.add((Comparable<Object>) key.getSecond());
      } else {
        throw new InvokeInvalidArgumentException(0, "surface Y key '" + key.getSecond() + "' is not comparable");
      }
    }
    final List<Comparable<Object>> keyX = new ArrayList<Comparable<Object>>(keyXSet);
    final List<Comparable<Object>> keyY = new ArrayList<Comparable<Object>>(keyYSet);
    Collections.sort(keyX);
    Collections.sort(keyY);
    if ((overrideValue != null) && (overrideValue.length < keyY.size())) {
      throw new InvokeInvalidArgumentException(1, "Not enough rows in matrix");
    }
    if ((marketValue != null) && (marketValue.length < keyY.size())) {
      throw new InvokeInvalidArgumentException(2, "Not enough rows in matrix");
    }
    for (int i = 0; i < keyY.size(); i++) {
      final Object y = keyY.get(i);
      if ((overrideValue != null) && (overrideValue[i].length < keyX.size())) {
        throw new InvokeInvalidArgumentException(1, "Not enough columns in matrix");
      }
      if ((marketValue != null) && (marketValue[i].length < keyX.size())) {
        throw new InvokeInvalidArgumentException(2, "Not enough columns in matrix");
      }
      for (int j = 0; j < keyX.size(); j++) {
        final Pair<Object, Object> key = Pair.of((Object) keyX.get(j), y);
        final ValueSnapshot value = snapshot.getValues().get(key);
        if (marketValue != null) {
          Double override;
          if (overrideValue != null) {
            override = overrideValue[i][j].getDoubleValue();
          } else {
            if (value != null) {
              override = value.getOverrideValue();
            } else {
              override = null;
            }
          }
          snapshot.getValues().put(key, new ValueSnapshot(marketValue[i][j].getDoubleValue(), override));
        } else if (overrideValue != null) {
          if (value != null) {
            value.setOverrideValue(overrideValue[i][j].getDoubleValue());
          } else {
            snapshot.getValues().put(key, new ValueSnapshot(null, overrideValue[i][j].getDoubleValue()));
          }
        }
      }
    }
    return snapshot;
  }

  // AbstractFunctionInvoker

  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    return invoke((ManageableVolatilitySurfaceSnapshot) parameters[0], (Value[][]) parameters[1], (Value[][]) parameters[2]);
  }

  // PublishedFunction

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

}
