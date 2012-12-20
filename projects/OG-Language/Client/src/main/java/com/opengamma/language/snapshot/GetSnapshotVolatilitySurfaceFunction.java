/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.snapshot;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceKey;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceSnapshot;
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
 * Retrieves a "volatility surface" component of a snapshot
 */
public class GetSnapshotVolatilitySurfaceFunction extends AbstractFunctionInvoker implements PublishedFunction {

  /**
   * Default instance.
   */
  public static final GetSnapshotVolatilitySurfaceFunction INSTANCE = new GetSnapshotVolatilitySurfaceFunction();

  private final MetaFunction _meta;

  private static final int SNAPSHOT = 0;
  private static final int NAME = 1;

  private static List<MetaParameter> parameters() {
    return Arrays.asList(
        new MetaParameter("snapshot", JavaTypeInfo.builder(ManageableMarketDataSnapshot.class).get()),
        new MetaParameter("name", JavaTypeInfo.builder(String.class).allowNull().get()));
  }

  private GetSnapshotVolatilitySurfaceFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.MARKET_DATA, "GetSnapshotVolatilitySurface", getParameters(), this));
  }

  protected GetSnapshotVolatilitySurfaceFunction() {
    this(new DefinitionAnnotater(GetSnapshotVolatilitySurfaceFunction.class));
  }

  public static VolatilitySurfaceSnapshot invoke(final ManageableMarketDataSnapshot snapshot, final String name) {
    final VolatilitySurfaceKey key = StructuredMarketDataSnapshotUtil.toVolatilitySurfaceKey(name);
    if (key == null) {
      throw new InvokeInvalidArgumentException(NAME, "Invalid surface name");
    }
    final VolatilitySurfaceSnapshot surfaceSnapshot = snapshot.getVolatilitySurfaces().get(key);
    if (surfaceSnapshot == null) {
      throw new InvokeInvalidArgumentException(NAME, "Surface not found in snapshot");
    }
    return surfaceSnapshot;
  }

  public static String[] invoke(final ManageableMarketDataSnapshot snapshot) {
    final Set<VolatilitySurfaceKey> keys = snapshot.getVolatilitySurfaces().keySet();
    final String[] result = new String[keys.size()];
    int i = 0;
    for (VolatilitySurfaceKey key : keys) {
      result[i++] = StructuredMarketDataSnapshotUtil.fromVolatilitySurfaceKey(key);
    }
    Arrays.sort(result);
    return result;
  }

  // AbstractFunctionInvoker

  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    final ManageableMarketDataSnapshot snapshot = (ManageableMarketDataSnapshot) parameters[SNAPSHOT];
    if (snapshot.getVolatilitySurfaces() == null) {
      throw new InvokeInvalidArgumentException(SNAPSHOT, "No surfaces in snapshot");
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
