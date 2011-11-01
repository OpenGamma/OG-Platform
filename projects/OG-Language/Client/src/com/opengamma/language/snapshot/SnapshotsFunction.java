/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.snapshot;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.id.UniqueId;
import com.opengamma.language.client.CombinedMarketDataSnapshotMaster;
import com.opengamma.language.client.CombiningMaster;
import com.opengamma.language.client.MasterID;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchRequest;

/**
 * Queries the snapshots available from masters.
 */
public class SnapshotsFunction extends AbstractFunctionInvoker implements PublishedFunction {

  /**
   * Default instance.
   */
  public static final SnapshotsFunction INSTANCE = new SnapshotsFunction();

  private final MetaFunction _meta;

  private static List<MetaParameter> parameters() {
    final MetaParameter name = new MetaParameter("name", JavaTypeInfo.builder(String.class).defaultValue("*").get());
    return Arrays.asList(name);
  }

  private SnapshotsFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.MARKET_DATA, "Snapshots", getParameters(), this));
  }

  protected SnapshotsFunction() {
    this(new DefinitionAnnotater(SnapshotsFunction.class));
  }

  public static Map<UniqueId, String> invoke(final SessionContext context, final String name) {
    final MarketDataSnapshotSearchRequest request = new MarketDataSnapshotSearchRequest();
    request.setName(name);
    request.setIncludeData(false);
    final Map<UniqueId, String> result = new LinkedHashMap<UniqueId, String>();
    CombiningMaster.MARKET_DATA_SNAPSHOT.get(context).search(request, new CombinedMarketDataSnapshotMaster.SearchCallback() {

      @Override
      public boolean include(MarketDataSnapshotDocument document) {
        return true;
      }

      @Override
      public void accept(MarketDataSnapshotDocument document, MasterID master, boolean masterUnique, boolean clientUnique) {
        String name = document.getName();
        if (!masterUnique) {
          name = name + " " + document.getUniqueId().toString();
        }
        if (!clientUnique) {
          name = name + " (" + master.getLabel() + ")";
        }
        result.put(document.getUniqueId(), name);
      }

      @Override
      public int compare(MarketDataSnapshotDocument o1, MarketDataSnapshotDocument o2) {
        return o1.getName().compareToIgnoreCase(o2.getName());
      }

    });
    return result;
  }

  // AbstractFunctionInvoker

  @Override
  protected Object invokeImpl(SessionContext sessionContext, Object[] parameters) {
    return invoke(sessionContext, (String) parameters[0]);
  }

  // PublishedFunction

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

}
