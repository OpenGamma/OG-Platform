/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.snapshot;

import java.util.Arrays;
import java.util.List;

import com.opengamma.core.marketdatasnapshot.StructuredMarketDataSnapshot;
import com.opengamma.engine.marketdata.snapshot.MarketDataSnapshotter;
import com.opengamma.engine.resource.EngineResourceReference;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.cycle.ViewCycle;
import com.opengamma.financial.marketdatasnapshot.MarketDataSnapshotterImpl;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.error.InvokeInvalidArgumentException;
import com.opengamma.language.procedure.AbstractProcedureInvoker;
import com.opengamma.language.procedure.MetaProcedure;
import com.opengamma.language.procedure.PublishedProcedure;
import com.opengamma.language.view.ViewClientHandle;

/**
 * Creates a snapshot from the latest view cycle. The view client must support cycle access.
 */
public class SnapshotViewResultProcedure extends AbstractProcedureInvoker.SingleResult implements PublishedProcedure {

  /**
   * Default instance.
   */
  public static final SnapshotViewResultProcedure INSTANCE = new SnapshotViewResultProcedure();

  private final MetaProcedure _meta;

  private static final int VIEW_CLIENT = 0;

  private static List<MetaParameter> parameters() {
    final MetaParameter viewClient = new MetaParameter("viewClient", JavaTypeInfo.builder(ViewClientHandle.class).get());
    return Arrays.asList(viewClient);
  }

  private SnapshotViewResultProcedure(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaProcedure(Categories.MARKET_DATA, "SnapshotViewResult", getParameters(), this));
  }

  protected SnapshotViewResultProcedure() {
    this(new DefinitionAnnotater(SnapshotViewResultProcedure.class));
  }

  public static StructuredMarketDataSnapshot invoke(final SessionContext sessionContext, final ViewClientHandle viewClientHandle) {
    try {
      final MarketDataSnapshotter snapshotter = new MarketDataSnapshotterImpl(sessionContext.getGlobalContext().getComputationTargetResolver(), sessionContext.getGlobalContext()
          .getVolatilityCubeDefinitionSource(), sessionContext.getGlobalContext().getHistoricalTimeSeriesSource());
      final ViewClient viewClient = viewClientHandle.get().getViewClient();
      final EngineResourceReference<? extends ViewCycle> viewCycleRef = viewClient.createLatestCycleReference();
      try {
        return snapshotter.createSnapshot(viewClient, viewCycleRef.get());
      } finally {
        viewCycleRef.release();
      }
    } catch (final IllegalStateException e) {
      throw new InvokeInvalidArgumentException(VIEW_CLIENT, "View client is not connected to a process");
    } catch (final UnsupportedOperationException e) {
      throw new InvokeInvalidArgumentException(VIEW_CLIENT, "View client is not configured for cycle access");
    } finally {
      viewClientHandle.unlock();
    }
  }

  // AbstractProcedureInvoker

  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    return invoke(sessionContext, (ViewClientHandle) parameters[VIEW_CLIENT]);
  }

  // PublishedProcedure

  @Override
  public MetaProcedure getMetaProcedure() {
    return _meta;
  }

}
