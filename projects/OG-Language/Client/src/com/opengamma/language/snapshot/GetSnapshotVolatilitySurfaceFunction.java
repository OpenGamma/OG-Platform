/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.snapshot;

import java.util.Arrays;
import java.util.List;

import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceKey;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.ManageableMarketDataSnapshot;
import com.opengamma.id.UniqueId;
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

  private static List<MetaParameter> parameters() {
    return Arrays.asList(
        new MetaParameter("snapshot", JavaTypeInfo.builder(ManageableMarketDataSnapshot.class).get()),
        new MetaParameter("name", JavaTypeInfo.builder(String.class).get()));
  }

  private GetSnapshotVolatilitySurfaceFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.MARKET_DATA, "GetSnapshotVolatilitySurface", getParameters(), this));
  }

  protected GetSnapshotVolatilitySurfaceFunction() {
    this(new DefinitionAnnotater(GetSnapshotVolatilitySurfaceFunction.class));
  }

  public static VolatilitySurfaceSnapshot invoke(final ManageableMarketDataSnapshot snapshot, final String name) {
    // TODO: this is bad; need to do some escaping
    final String[] surfaceNames = name.split("_");
    if (surfaceNames.length != 3) {
      throw new InvokeInvalidArgumentException(1, "Invalid surface name");
    }
    final VolatilitySurfaceKey key = new VolatilitySurfaceKey(UniqueId.parse(surfaceNames[0]), surfaceNames[1], surfaceNames[2]);
    if (snapshot.getVolatilitySurfaces() == null) {
      throw new InvokeInvalidArgumentException(0, "No surfaces in snapshot");
    }
    final VolatilitySurfaceSnapshot surfaceSnapshot = snapshot.getVolatilitySurfaces().get(key);
    if (surfaceSnapshot == null) {
      throw new InvokeInvalidArgumentException(1, "Surface not found in snapshot");
    }
    return surfaceSnapshot;
  }

  // AbstractFunctionInvoker

  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    return invoke((ManageableMarketDataSnapshot) parameters[0], (String) parameters[1]);
  }

  // PublishedFunction

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

}
