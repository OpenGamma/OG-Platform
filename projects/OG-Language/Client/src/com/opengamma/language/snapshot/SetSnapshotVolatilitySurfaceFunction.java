/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.snapshot;

import java.util.Arrays;
import java.util.HashMap;
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
 * Updates a "volatility surface" component of a snapshot
 */
public class SetSnapshotVolatilitySurfaceFunction extends AbstractFunctionInvoker implements PublishedFunction {

  /**
   * Default instance.
   */
  public static final SetSnapshotVolatilitySurfaceFunction INSTANCE = new SetSnapshotVolatilitySurfaceFunction();

  private final MetaFunction _meta;

  private static List<MetaParameter> parameters() {
    return Arrays.asList(
        new MetaParameter("snapshot", JavaTypeInfo.builder(ManageableMarketDataSnapshot.class).get()),
        new MetaParameter("name", JavaTypeInfo.builder(String.class).get()),
        new MetaParameter("surface", JavaTypeInfo.builder(VolatilitySurfaceSnapshot.class).allowNull().get()));
  }

  private SetSnapshotVolatilitySurfaceFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.MARKET_DATA, "SetSnapshotVolatilitySurface", getParameters(), this));
  }

  protected SetSnapshotVolatilitySurfaceFunction() {
    this(new DefinitionAnnotater(SetSnapshotVolatilitySurfaceFunction.class));
  }

  public static ManageableMarketDataSnapshot invoke(final ManageableMarketDataSnapshot snapshot, final String name, final VolatilitySurfaceSnapshot surface) {
    // TODO: this is bad; need to do some escaping
    final String[] surfaceNames = name.split("_");
    if (surfaceNames.length != 3) {
      throw new InvokeInvalidArgumentException(1, "Invalid surface name");
    }
    final VolatilitySurfaceKey key = new VolatilitySurfaceKey(UniqueId.parse(surfaceNames[0]), surfaceNames[1], surfaceNames[2]);
    if (snapshot.getVolatilitySurfaces() == null) {
      snapshot.setVolatilitySurfaces(new HashMap<VolatilitySurfaceKey, VolatilitySurfaceSnapshot>());
    }
    if (surface != null) {
      snapshot.getVolatilitySurfaces().put(key, surface);
    } else {
      snapshot.getVolatilitySurfaces().remove(key);
    }
    return snapshot;
  }

  // AbstractFunctionInvoker

  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    return invoke((ManageableMarketDataSnapshot) parameters[0], (String) parameters[1], (VolatilitySurfaceSnapshot) parameters[2]);
  }

  // PublishedFunction

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

}
