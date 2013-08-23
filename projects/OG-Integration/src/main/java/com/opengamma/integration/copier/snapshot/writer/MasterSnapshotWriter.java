/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.copier.snapshot.writer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.beans.JodaBeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

import com.opengamma.core.security.SecuritySource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdSearch;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.position.PositionSearchRequest;
import com.opengamma.master.position.PositionSearchResult;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.master.security.SecuritySearchSortOrder;
import com.opengamma.master.security.impl.MasterSecuritySource;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.beancompare.BeanCompare;
import com.opengamma.util.beancompare.BeanDifference;
import com.opengamma.util.tuple.ObjectsPair;

/**
 * A class that writes securities and snapshot positions and trades to the OG masters
 */
public class MasterSnapshotWriter implements SnapshotWriter {

  private static final Logger s_logger = LoggerFactory.getLogger(MasterSnapshotWriter.class);

  private final MarketDataSnapshotMaster _snapshotMaster;

  /**
   * Create a master snapshot writer
   * @param snapshotName             The name of the snapshot to create/write to
   * @param snapshotMaster           The snapshot master to which to write the snapshot
   */

  public MasterSnapshotWriter(String snapshotName, MarketDataSnapshotMaster snapshotMaster) {
    _snapshotMaster = snapshotMaster;


  }


  @Override
  public void close() {
    //To change body of implemented methods use File | Settings | File Templates.
  }
}
