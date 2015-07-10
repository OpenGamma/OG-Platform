/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.marketdatasnapshot.impl;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.opengamma.DataNotFoundException;
import com.opengamma.core.change.ChangeEvent;
import com.opengamma.core.change.ChangeListener;
import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotChangeListener;
import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotSource;
import com.opengamma.core.marketdatasnapshot.NamedSnapshot;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.AbstractMasterSource;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchRequest;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicSPI;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * A {@code MarketDataSnapshotSource} implemented using an underlying {@code MarketDataSnapshotMaster}.
 * <p>
 * The {@link MarketDataSnapshotSource} interface provides snapshots to the engine via a narrow API.
 * This class provides the source on top of a standard {@link MarketDataSnapshotMaster}.
 */
@PublicSPI
public class MasterSnapshotSource
    extends AbstractMasterSource<NamedSnapshot, MarketDataSnapshotDocument, MarketDataSnapshotMaster>
    implements MarketDataSnapshotSource {

  /**
   * The listeners.
   */
  private final ConcurrentMap<Pair<UniqueId, MarketDataSnapshotChangeListener>, ChangeListener> _registeredListeners =
      new ConcurrentHashMap<>();

  /**
   * Creates an instance with an underlying master which does not override versions.
   *
   * @param master  the master, not null
   */
  public MasterSnapshotSource(final MarketDataSnapshotMaster master) {
    super(master);
  }

  //-------------------------------------------------------------------------
  @Override
  public void addChangeListener(final UniqueId uniqueId, final MarketDataSnapshotChangeListener listener) {
    ChangeListener changeListener = new ChangeListener() {
      @Override
      public void entityChanged(ChangeEvent event) {
        ObjectId changedId = event.getObjectId();
        if (changedId != null && changedId.getScheme().equals(uniqueId.getScheme()) &&
            changedId.getValue().equals(uniqueId.getValue())) {
          //TODO This is over cautious in the case of corrections to non latest versions
          listener.objectChanged(uniqueId.getObjectId());
        }
      }
    };
    _registeredListeners.put(Pairs.of(uniqueId, listener), changeListener);
    getMaster().changeManager().addChangeListener(changeListener);
  }

  @Override
  public void removeChangeListener(UniqueId uid, MarketDataSnapshotChangeListener listener) {
    ChangeListener changeListener = _registeredListeners.remove(Pairs.of(uid, listener));
    getMaster().changeManager().removeChangeListener(changeListener);
  }


  @Override
  public <S extends NamedSnapshot> S getSingle(Class<S> type,
                                               String snapshotName,
                                               VersionCorrection versionCorrection) {

    // Try to find an exact match using the name and type first. If this doesn't work
    // (perhaps as the type searched for is a superclass of the type held), we search
    // again by name only and check the type after.
    // TODO - review usage patterns, do we normally hit or miss using type. If names are generally unique searching by type is potentially redundant
    TypedSnapshotSearcher<S> searcher =
        new TypedSnapshotSearcher<>(getMaster(), type, snapshotName, versionCorrection);
    return searcher.search();
  }

  /**
   * Helper class which assists in the search for snapshots. It first searches using a
   * name and expected type explicitly. If that does not succeed, it then searches by
   * name and checks the type afterwards. This is primarily to support the case where
   * a search is made using a supertype (e.g. interface) but the database holds the
   * implementation type.
   *
   * @param  <S> the type of snapshot being searched for
   */
  private static final class TypedSnapshotSearcher<S extends NamedSnapshot> {

    /**
     * Logger for the class.
     */
    private static final Logger s_logger = LoggerFactory.getLogger(TypedSnapshotSearcher.class);

    /**
     * The master to search for data in.
     */
    private final MarketDataSnapshotMaster _master;

    /**
     * The type of snapshot being searched for.
     */
    private final Class<S> _type;

    /**
     * The name of the snapshot being searched for.
     */
    private final String _snapshotName;

    /**
     * The version correction of the snapshot being searched for.
     */
    private final VersionCorrection _versionCorrection;

    /**
     * Create a searcher configured with the details of what to search for.
     *
     * @param  master the master to search in, not null
     * @param  type the type of snapshot being searched for
     * @param  snapshotName the name of the snapshot being searched for
     * @param  versionCorrection the version correction of the snapshot being searched for
     */
    public TypedSnapshotSearcher(MarketDataSnapshotMaster master,
                                 Class<S> type,
                                 String snapshotName,
                                 VersionCorrection versionCorrection) {
      _master = ArgumentChecker.notNull(master, "master");
      _type = ArgumentChecker.notNull(type, "type");
      _snapshotName = ArgumentChecker.notNull(snapshotName, "snapshotName");
      _versionCorrection = ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    }

    /**
     * Attempt to find a snapshot with the name and type specified. If no match
     * found initially, search with the name for a snapshot which is type
     * compatible with the type requested.
     *
     * @return a matching snapshot, not null
     * @throws DataNotFoundException if no matching snapshot can be found
     */
    public S search() {
      S result = findWithMatchingType();
      return result != null ? result : findWithGeneralType();
    }

    private S findWithMatchingType() {
      MarketDataSnapshotSearchRequest request = createBaseSearchRequest();
      request.setType(_type);
      return selectResult(_master.search(request).getNamedSnapshots(), true);
    }

    private S findWithGeneralType() {
      MarketDataSnapshotSearchRequest request = createBaseSearchRequest();
      S result = selectResult(_master.search(request).getNamedSnapshots(), false);

      if (result != null) {
        return result;
      } else {
        throw new DataNotFoundException("No snapshot found with type: [" + _type.getName() + "], id: [" +
                                            _snapshotName + "] and versionCorrection: [" + _versionCorrection + "]");
      }
    }

    private MarketDataSnapshotSearchRequest createBaseSearchRequest() {
      MarketDataSnapshotSearchRequest request = new MarketDataSnapshotSearchRequest();
      request.setName(_snapshotName);
      request.setVersionCorrection(_versionCorrection);
      return request;
    }

    private S selectResult(List<NamedSnapshot> results, boolean warnOnTypeMismatch) {
      List<S> filtered = filterForCorrectType(results, warnOnTypeMismatch);
      if (filtered.size() < results.size()) {
        s_logger.info("Filtered out {} snapshot(s) where type is not: {}", results.size() - filtered.size(), _type);
      }

      return selectFirst(filtered);
    }

    private List<S> filterForCorrectType(List<NamedSnapshot> results, boolean warnOnTypeMismatch) {

      ImmutableList.Builder<S> builder = ImmutableList.builder();

      for (NamedSnapshot snapshot : results) {

        if (_type.isAssignableFrom(snapshot.getClass())) {
          builder.add(_type.cast(snapshot));
        } else if (warnOnTypeMismatch) {
          s_logger.warn("Found matching snapshot with expected type: {} and name: {} - but actual type was: {}",
                        _type.getName(), _snapshotName, snapshot.getClass().getName());
        }
      }

      return builder.build();
    }

    private S selectFirst(List<S> filtered) {
      if (filtered.isEmpty()) {
        return null;
      }

      if (filtered.size() > 1) {
        s_logger.warn("Found multiple matching snapshot results for type: {} and name: {} - returning first match found",
                      _type.getName(), _snapshotName);
      }

      return filtered.get(0);
    }
  }
}
