/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.snapshot;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.opengamma.core.marketdatasnapshot.YieldCurveKey;
import com.opengamma.core.marketdatasnapshot.YieldCurveSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.ManageableMarketDataSnapshot;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.error.InvokeInvalidArgumentException;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;

/**
 * Retrieves a "yield curve" component of a snapshot
 */
public class GetSnapshotYieldCurveFunction extends AbstractFunctionInvoker implements PublishedFunction {

  /**
   * Default instance.
   */
  public static final GetSnapshotYieldCurveFunction INSTANCE = new GetSnapshotYieldCurveFunction();

  private final MetaFunction _meta;

  private static final int SNAPSHOT = 0;
  private static final int NAME = 1;

  private static List<MetaParameter> parameters() {
    return Arrays.asList(
        new MetaParameter("snapshot", JavaTypeInfo.builder(ManageableMarketDataSnapshot.class).get()),
        new MetaParameter("name", JavaTypeInfo.builder(String.class).allowNull().get()));
  }

  private GetSnapshotYieldCurveFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.MARKET_DATA, "GetSnapshotYieldCurve", getParameters(), this));
  }

  protected GetSnapshotYieldCurveFunction() {
    this(new DefinitionAnnotater(GetSnapshotYieldCurveFunction.class));
  }

  public static YieldCurveSnapshot invoke(final ManageableMarketDataSnapshot snapshot, final String name) {
    final YieldCurveKey key = StructuredMarketDataSnapshotUtil.toYieldCurveKey(name);
    if (key == null) {
      throw new InvokeInvalidArgumentException(NAME, "Invalid curve name");
    }
    final YieldCurveSnapshot curveSnapshot = snapshot.getYieldCurves().get(key);
    if (curveSnapshot == null) {
      throw new InvokeInvalidArgumentException(NAME, "Curve not found");
    }
    return curveSnapshot;
  }

  public static String[] invoke(final ManageableMarketDataSnapshot snapshot) {
    final Set<YieldCurveKey> keys = snapshot.getYieldCurves().keySet();
    final String[] result = new String[keys.size()];
    int i = 0;
    for (YieldCurveKey key : keys) {
      result[i++] = StructuredMarketDataSnapshotUtil.fromYieldCurveKey(key);
    }
    Arrays.sort(result);
    return result;
  }

  // AbstractFunctionInvoker

  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    final ManageableMarketDataSnapshot snapshot = (ManageableMarketDataSnapshot) parameters[SNAPSHOT];
    if (snapshot.getYieldCurves() == null) {
      throw new InvokeInvalidArgumentException(SNAPSHOT, "Snapshot does not contain any curves");
    }
    if (parameters[NAME] == null) {
      return invoke(snapshot);
    } else {
      return invoke(snapshot, (String) parameters[NAME]);
    }
  }

  // PublishedFunction

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

}
