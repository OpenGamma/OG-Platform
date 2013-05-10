/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.snapshot;

import com.opengamma.core.marketdatasnapshot.CurveKey;
import com.opengamma.core.marketdatasnapshot.CurveSnapshot;
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

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Retrieves a curve component of a snapshot
 */
public class GetSnapshotCurveFunction extends AbstractFunctionInvoker implements PublishedFunction {

  /**
   * Default instance.
   */
  public static final GetSnapshotCurveFunction INSTANCE = new GetSnapshotCurveFunction();

  private final MetaFunction _meta;

  private static final int SNAPSHOT = 0;
  private static final int NAME = 1;

  private static List<MetaParameter> parameters() {
    return Arrays.asList(
        new MetaParameter("snapshot", JavaTypeInfo.builder(ManageableMarketDataSnapshot.class).get()),
        new MetaParameter("name", JavaTypeInfo.builder(String.class).allowNull().get()));
  }

  private GetSnapshotCurveFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.MARKET_DATA, "GetSnapshotCurve", getParameters(), this));
  }

  protected GetSnapshotCurveFunction() {
    this(new DefinitionAnnotater(GetSnapshotCurveFunction.class));
  }

  public static CurveSnapshot invoke(final ManageableMarketDataSnapshot snapshot, final String name) {
    final CurveKey key = new CurveKey(name);
    if (key == null) {
      throw new InvokeInvalidArgumentException(NAME, "Invalid curve name");
    }
    final CurveSnapshot curveSnapshot = snapshot.getCurves().get(key);
    if (curveSnapshot == null) {
      throw new InvokeInvalidArgumentException(NAME, "Curve not found");
    }
    return curveSnapshot;
  }

  public static String[] invoke(final ManageableMarketDataSnapshot snapshot) {
    final Set<CurveKey> keys = snapshot.getCurves().keySet();
    final String[] result = new String[keys.size()];
    int i = 0;
    for (CurveKey key : keys) {
      result[i++] = key.getName();
    }
    Arrays.sort(result);
    return result;
  }

  // AbstractFunctionInvoker

  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    final ManageableMarketDataSnapshot snapshot = (ManageableMarketDataSnapshot) parameters[SNAPSHOT];
    if (snapshot.getCurves() == null) {
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
