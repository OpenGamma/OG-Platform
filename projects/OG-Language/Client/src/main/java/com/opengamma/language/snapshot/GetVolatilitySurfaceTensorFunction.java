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
import com.opengamma.language.ValueUtils;
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
 * Fetches the data from a volatility surface as a 2D matrix tensor.
 */
public class GetVolatilitySurfaceTensorFunction extends AbstractFunctionInvoker implements PublishedFunction {

  /**
   * Default instance.
   */
  public static final GetVolatilitySurfaceTensorFunction INSTANCE = new GetVolatilitySurfaceTensorFunction();

  private final MetaFunction _meta;

  private static List<MetaParameter> parameters() {
    return Arrays.asList(
        new MetaParameter("snapshot", JavaTypeInfo.builder(ManageableVolatilitySurfaceSnapshot.class).get()),
        new MetaParameter("marketValue", JavaTypeInfo.builder(Boolean.class).defaultValue(Boolean.TRUE).get()),
        new MetaParameter("overrideValue", JavaTypeInfo.builder(Boolean.class).defaultValue(Boolean.FALSE).get()));
  }

  private GetVolatilitySurfaceTensorFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.MARKET_DATA, "GetVolatilitySurfaceTensor", getParameters(), this));
  }

  protected GetVolatilitySurfaceTensorFunction() {
    this(new DefinitionAnnotater(GetVolatilitySurfaceTensorFunction.class));
  }

  @SuppressWarnings("unchecked")
  public static Value[][] invoke(final ManageableVolatilitySurfaceSnapshot snapshot, final Boolean marketValue, final Boolean overrideValue) {
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
    final Value[][] values = new Value[keyY.size()][keyX.size()];
    for (int i = 0; i < keyY.size(); i++) {
      final Object y = keyY.get(i);
      for (int j = 0; j < keyX.size(); j++) {
        final ValueSnapshot value = snapshot.getValues().get(Pair.of(keyX.get(j), y));
        if (value == null) {
          values[i][j] = new Value();
        } else if (Boolean.TRUE.equals(overrideValue) && (value.getOverrideValue() != null)) {
          values[i][j] = ValueUtils.of(value.getOverrideValue());
        } else if (Boolean.TRUE.equals(marketValue) && (value.getMarketValue() != null)) {
          values[i][j] = ValueUtils.of(value.getMarketValue());
        } else {
          values[i][j] = new Value();
        }
      }
    }
    return values;
  }

  // AbstractFunctionInvoker

  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    return invoke((ManageableVolatilitySurfaceSnapshot) parameters[0], (Boolean) parameters[1], (Boolean) parameters[2]);
  }

  // PublishedFunction

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

}
