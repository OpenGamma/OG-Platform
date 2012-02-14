/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.snapshot;

import java.util.Arrays;
import java.util.List;

import com.opengamma.core.marketdatasnapshot.MarketDataValueType;
import com.opengamma.core.marketdatasnapshot.impl.ManageableMarketDataSnapshot;
import com.opengamma.id.UniqueId;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;

/**
 * Updates a "global value" component of a snapshot
 */
public class SetSnapshotGlobalValueFunction extends AbstractFunctionInvoker implements PublishedFunction {

  /**
   * Default instance.
   */
  public static final SetSnapshotGlobalValueFunction INSTANCE = new SetSnapshotGlobalValueFunction();

  private final MetaFunction _meta;
  
  private static final int SNAPSHOT = 0;
  private static final int VALUE_NAME = 1;
  private static final int IDENTIFIER = 2;
  private static final int OVERRIDE_VALUE = 3;
  private static final int MARKET_VALUE = 4;
  private static final int TYPE = 5;

  private static List<MetaParameter> parameters() {
    return Arrays.asList(
        new MetaParameter("snapshot", JavaTypeInfo.builder(ManageableMarketDataSnapshot.class).get()),
        new MetaParameter("valueName", JavaTypeInfo.builder(String.class).get()),
        new MetaParameter("identifier", JavaTypeInfo.builder(UniqueId.class).get()),
        new MetaParameter("overrideValue", JavaTypeInfo.builder(Double.class).allowNull().get()),
        new MetaParameter("marketValue", JavaTypeInfo.builder(Double.class).allowNull().get()),
        new MetaParameter("type", JavaTypeInfo.builder(MarketDataValueType.class).defaultValue(MarketDataValueType.SECURITY).get()));
  }

  private SetSnapshotGlobalValueFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.MARKET_DATA, "SetSnapshotGlobalValue", getParameters(), this));
  }

  protected SetSnapshotGlobalValueFunction() {
    this(new DefinitionAnnotater(SetSnapshotGlobalValueFunction.class));
  }

  public static ManageableMarketDataSnapshot invoke(final ManageableMarketDataSnapshot snapshot, final String valueName, final UniqueId identifier, final Double overrideValue,
      final Double marketValue, MarketDataValueType type) {
    if (snapshot.getGlobalValues() == null) {
      snapshot.setGlobalValues(UnstructuredMarketDataSnapshotUtil.create());
    }
    UnstructuredMarketDataSnapshotUtil.setValue(snapshot.getGlobalValues(), valueName, identifier, overrideValue, marketValue, type);
    return snapshot;
  }

  // AbstractFunctionInvoker

  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    return invoke((ManageableMarketDataSnapshot) parameters[SNAPSHOT], (String) parameters[VALUE_NAME], (UniqueId) parameters[IDENTIFIER], (Double) parameters[OVERRIDE_VALUE], (Double) parameters[MARKET_VALUE], (MarketDataValueType)parameters[TYPE]);
  }

  // PublishedFunction

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

}
