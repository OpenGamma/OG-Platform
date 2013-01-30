/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.snapshot;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Clock;
import org.threeten.bp.Instant;

import com.opengamma.core.marketdatasnapshot.impl.ManageableMarketDataSnapshot;
import com.opengamma.id.UniqueId;
import com.opengamma.language.client.CombiningMaster;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.error.InvokeInvalidArgumentException;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotHistoryRequest;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotHistoryResult;
import com.opengamma.util.paging.PagingRequest;

/**
 * Queries the versions available of a given snapshot.
 */
public class SnapshotVersionsFunction extends AbstractFunctionInvoker implements PublishedFunction {

  private static final Logger s_logger = LoggerFactory.getLogger(SnapshotVersionsFunction.class);

  /**
   * Default instance.
   */
  public static final SnapshotVersionsFunction INSTANCE = new SnapshotVersionsFunction();

  private final MetaFunction _meta;

  private static List<MetaParameter> parameters() {
    final MetaParameter snapshot = new MetaParameter("snapshot", JavaTypeInfo.builder(UniqueId.class).get());
    final MetaParameter correction = new MetaParameter("correction", JavaTypeInfo.builder(Instant.class).allowNull().get());
    return Arrays.asList(snapshot, correction);
  }

  private SnapshotVersionsFunction(final DefinitionAnnotater info) {
    super(parameters());
    _meta = info.annotate(new MetaFunction(Categories.MARKET_DATA, "SnapshotVersions", getParameters(), this));
  }

  protected SnapshotVersionsFunction() {
    this(new DefinitionAnnotater(SnapshotVersionsFunction.class));
  }

  private static final Comparator<MarketDataSnapshotDocument> s_sortSnapshotsByVersion = new Comparator<MarketDataSnapshotDocument>() {
    @Override
    public int compare(final MarketDataSnapshotDocument o1, final MarketDataSnapshotDocument o2) {
      return o1.getVersionFromInstant().compareTo(o2.getVersionFromInstant());
    }
  };

  // TODO: Returning a matrix like this is bad; what is the fundamental data structure represented? Return that and
  // use a type converter to reduce it to a matrix.

  public static Object[][] invoke(final SessionContext context, final UniqueId snapshot, final Instant correction) {
    final MarketDataSnapshotHistoryRequest request = new MarketDataSnapshotHistoryRequest(snapshot.getObjectId(), null,
        (correction != null) ? correction : Clock.systemDefaultZone().instant());
    // TODO: this is bad as there is no way of navigating the pages, but we don't want to hang if a lot of data comes back
    request.setPagingRequest(PagingRequest.ofPage(1, 100));
    request.setIncludeData(false);
    final MarketDataSnapshotHistoryResult result;
    try {
      result = CombiningMaster.MARKET_DATA_SNAPSHOT.get(context).history(request);
    } catch (IllegalArgumentException e) {
      s_logger.warn("Identifier invalid", e);
      throw new InvokeInvalidArgumentException(1, "Identifier " + snapshot + " is not valid");
    }
    final List<MarketDataSnapshotDocument> snapshots = result.getDocuments();
    if (snapshots.isEmpty()) {
      throw new InvokeInvalidArgumentException(1, "Snapshot " + snapshot + " not found");
    }
    Collections.sort(snapshots, s_sortSnapshotsByVersion);
    final Object[][] values = new Object[snapshots.size()][4];
    int i = 0;
    for (MarketDataSnapshotDocument document : snapshots) {
      final ManageableMarketDataSnapshot documentSnapshot = document.getSnapshot();
      values[i][0] = document.getUniqueId();
      values[i][1] = document.getVersionFromInstant();
      values[i][2] = documentSnapshot.getName();
      values[i][3] = (documentSnapshot.getBasisViewName() != null) ? documentSnapshot.getBasisViewName() : "";
      i++;
    }
    return values;
  }

  // AbstractFunctionInvoker

  @Override
  public Object invokeImpl(final SessionContext context, final Object[] parameters) {
    return invoke(context, (UniqueId) parameters[0], (Instant) parameters[1]);
  }

  // PublishedFunction

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

}
