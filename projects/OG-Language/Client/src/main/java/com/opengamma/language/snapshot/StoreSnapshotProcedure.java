/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.snapshot;

import java.util.Arrays;
import java.util.List;

import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotSource;
import com.opengamma.core.marketdatasnapshot.impl.ManageableMarketDataSnapshot;
import com.opengamma.financial.user.rest.RemoteClient;
import com.opengamma.id.UniqueId;
import com.opengamma.language.client.ContextRemoteClient;
import com.opengamma.language.client.MasterID;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.error.InvokeInvalidArgumentException;
import com.opengamma.language.procedure.AbstractProcedureInvoker;
import com.opengamma.language.procedure.MetaProcedure;
import com.opengamma.language.procedure.PublishedProcedure;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;

/**
 * Writes a snapshot to the {@link MarketDataSnapshotSource}.
 */
public class StoreSnapshotProcedure extends AbstractProcedureInvoker.SingleResult implements PublishedProcedure {

  /**
   * Default instance.
   */
  public static final StoreSnapshotProcedure INSTANCE = new StoreSnapshotProcedure();

  private final MetaProcedure _meta;

  private static final int SNAPSHOT = 0;
  private static final int IDENTIFIER = 1;
  private static final int MASTER = 2;

  private static List<MetaParameter> parameters() {
    final MetaParameter snapshot = new MetaParameter("snapshot", JavaTypeInfo.builder(ManageableMarketDataSnapshot.class).get());
    final MetaParameter identifier = new MetaParameter("identifier", JavaTypeInfo.builder(UniqueId.class).allowNull().get());
    final MetaParameter target = new MetaParameter("master", JavaTypeInfo.builder(MasterID.class).defaultValue(MasterID.SESSION).get());
    return Arrays.asList(snapshot, identifier, target);
  }

  private StoreSnapshotProcedure(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaProcedure(Categories.MARKET_DATA, "StoreSnapshot", getParameters(), this));
  }

  protected StoreSnapshotProcedure() {
    this(new DefinitionAnnotater(StoreSnapshotProcedure.class));
  }

  protected static UniqueId invoke(final MarketDataSnapshotMaster master, final UniqueId identifier, final ManageableMarketDataSnapshot snapshot) {
    if (snapshot.getName() == null) {
      snapshot.setName("Unnamed");
    }
    MarketDataSnapshotDocument document = new MarketDataSnapshotDocument(identifier, snapshot);
    if (identifier == null) {
      document = master.add(document);
    } else {
      document = master.update(document);
    }
    return document.getUniqueId();
  }

  public static UniqueId invoke(final SessionContext sessionContext, final ManageableMarketDataSnapshot snapshot, final UniqueId identifier, final MasterID master) {
    final RemoteClient client = ContextRemoteClient.get(sessionContext, master);
    final MarketDataSnapshotMaster mdsMaster;
    try {
      mdsMaster = client.getMarketDataSnapshotMaster();
    } catch (UnsupportedOperationException e) {
      throw new InvokeInvalidArgumentException(MASTER, e);
    }
    return invoke(mdsMaster, identifier, snapshot);
  }

  // AbstractProcedureInvoker

  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    return invoke(sessionContext, (ManageableMarketDataSnapshot) parameters[SNAPSHOT], (UniqueId) parameters[IDENTIFIER], (MasterID) parameters[MASTER]);
  }

  // PublishedProcedure

  @Override
  public MetaProcedure getMetaProcedure() {
    return _meta;
  }

}
