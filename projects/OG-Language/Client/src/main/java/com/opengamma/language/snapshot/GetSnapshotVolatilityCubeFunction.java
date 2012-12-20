/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.snapshot;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.opengamma.core.marketdatasnapshot.VolatilityCubeKey;
import com.opengamma.core.marketdatasnapshot.VolatilityCubeSnapshot;
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
 * Fetches a "volatility cube" component of a snapshot
 */
public class GetSnapshotVolatilityCubeFunction extends AbstractFunctionInvoker implements PublishedFunction {

  /**
   * Default instance.
   */
  public static final GetSnapshotVolatilityCubeFunction INSTANCE = new GetSnapshotVolatilityCubeFunction();

  private final MetaFunction _meta;

  private static final int SNAPSHOT = 0;
  private static final int NAME = 1;

  private static List<MetaParameter> parameters() {
    return Arrays.asList(
        new MetaParameter("snapshot", JavaTypeInfo.builder(ManageableMarketDataSnapshot.class).get()),
        new MetaParameter("name", JavaTypeInfo.builder(String.class).allowNull().get()));
  }

  private GetSnapshotVolatilityCubeFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.MARKET_DATA, "GetSnapshotVolatilityCube", getParameters(), this));
  }

  protected GetSnapshotVolatilityCubeFunction() {
    this(new DefinitionAnnotater(GetSnapshotVolatilityCubeFunction.class));
  }

  public static VolatilityCubeSnapshot invoke(final ManageableMarketDataSnapshot snapshot, final String name) {
    final VolatilityCubeKey key = StructuredMarketDataSnapshotUtil.toVolatilityCubeKey(name);
    if (key == null) {
      throw new InvokeInvalidArgumentException(NAME, "Invalid cube name");
    }
    final VolatilityCubeSnapshot cubeSnapshot = snapshot.getVolatilityCubes().get(key);
    if (cubeSnapshot == null) {
      throw new InvokeInvalidArgumentException(NAME, "Cube not found in snapshot");
    }
    return cubeSnapshot;
  }

  public static String[] invoke(final ManageableMarketDataSnapshot snapshot) {
    final Set<VolatilityCubeKey> keys = snapshot.getVolatilityCubes().keySet();
    final String[] result = new String[keys.size()];
    int i = 0;
    for (VolatilityCubeKey key : keys) {
      result[i++] = StructuredMarketDataSnapshotUtil.fromVolatilityCubeKey(key);
    }
    Arrays.sort(result);
    return result;
  }

  // AbstractFunctionInvoker

  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    final ManageableMarketDataSnapshot snapshot = (ManageableMarketDataSnapshot) parameters[SNAPSHOT];
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
