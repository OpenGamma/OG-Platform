/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.namedsnapshot;

import java.util.Map.Entry;

import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.core.marketdatasnapshot.NamedSnapshot;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.AbstractPerRequestWebResource;

/**
 * Abstract base class for RESTful named snapshot resources.
 */
public abstract class AbstractWebNamedSnapshotResource
    extends AbstractPerRequestWebResource<WebNamedSnapshotData> {

  /**
   * HTML ftl directory
   */
  protected static final String HTML_DIR = "namedsnapshots/html/";
  /**
   * JSON ftl directory
   */
  protected static final String JSON_DIR = "namedsnapshots/json/";

  /**
   * The named snapshots types provider
   */
  private final NamedSnapshotsTypesProvider _namedSnapshotsTypesProvider = NamedSnapshotsTypesProvider.getInstance();

  /**
   * Creates the resource.
   * 
   * @param snapshotMaster  the snapshot master, not null
   */
  protected AbstractWebNamedSnapshotResource(final MarketDataSnapshotMaster snapshotMaster) {
    super(new WebNamedSnapshotData());
    ArgumentChecker.notNull(snapshotMaster, "snapshotMaster");
    data().setSnapshotMaster(snapshotMaster);
    initializeMetaData();
  }

  //init meta-data
  private void initializeMetaData() {
    for (Entry<String, Class<? extends NamedSnapshot>> entry : _namedSnapshotsTypesProvider.getTypeMap().entrySet()) {
      data().getTypeMap().put(entry.getKey(), entry.getValue());
    }
  }

  /**
   * Creates the resource.
   * 
   * @param parent  the parent resource, not null
   */
  protected AbstractWebNamedSnapshotResource(final AbstractWebNamedSnapshotResource parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * 
   * @return the output root data, not null
   */
  @Override
  protected FlexiBean createRootData() {
    FlexiBean out = super.createRootData();
    out.put("uris", new WebNamedSnapshotUris(data()));
    return out;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the snapshot types provider.
   * 
   * @return the snapshot types provider
   */
  public NamedSnapshotsTypesProvider getSnapshotTypesProvider() {
    return _namedSnapshotsTypesProvider;
  }

}
