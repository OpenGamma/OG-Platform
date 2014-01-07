/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch;

import static com.opengamma.util.db.DbDateUtils.toSqlTimestamp;

import java.sql.Timestamp;
import java.sql.Types;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.SqlParameterValue;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.engine.DefaultComputationTargetResolver;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT_DB)
public abstract class AbstractDbBatchMasterWorkerTest extends AbstractDbBatchTest {

  private static final Logger s_logger = LoggerFactory.getLogger(AbstractDbBatchMasterWorkerTest.class);

  protected DbBatchMaster _batchMaster;
  protected UniqueId _marketDataSnapshotUid = UniqueId.of("MrkDta", "market_data_snapshot_uid");
  protected UniqueId _viewDefinitionUid = UniqueId.of("ViewDef", "view definition uid");
  protected VersionCorrection _versionCorrection = VersionCorrection.LATEST;
  protected Instant _valuationTime = Instant.parse("2011-01-01T15:58:34.183Z");

  public AbstractDbBatchMasterWorkerTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doSetUp() {
    _batchMaster = new DbBatchMaster(getDbConnector(), new DefaultComputationTargetResolver());
    
    Timestamp now = toSqlTimestamp(Instant.now());

    final JdbcOperations template = _batchMaster.getDbConnector().getJdbcOperations();
    template.update("INSERT INTO rsk_compute_host (id, host_name) VALUES (?,?)", 1, "compute host");
    template.update("INSERT INTO rsk_compute_node (id, compute_host_id, node_name) VALUES (?,?,?)", 1, 1, "compute node");
    template.update("INSERT INTO rsk_computation_target (id, type, id_scheme, id_value, id_version) VALUES (?,?,?,?,?)", 1, "SECURITY", "DbSec", "APPL", null);
    template.update("INSERT INTO rsk_function_unique_id (id, unique_id) VALUES (?,?)", 1, "FV");
    template.update("INSERT INTO rsk_live_data_snapshot (id, base_uid_scheme, base_uid_value, base_uid_version) VALUES (?,?,?,?)", 1, _marketDataSnapshotUid.getScheme(), _marketDataSnapshotUid.getValue(), _marketDataSnapshotUid.getVersion());
    template.update("INSERT INTO rsk_live_data_snapshot_entry (id, snapshot_id, computation_target_id, name, value) VALUES (?,?,?,?,?)", 1, 1, 1, "FV", 999.99);    
    template.update("INSERT INTO rsk_run (id, version_correction, viewdef_scheme, viewdef_value, viewdef_version, live_data_snapshot_id, create_instant, start_instant, end_instant, valuation_time, num_restarts, complete) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)",
      1, _versionCorrection.toString(), _viewDefinitionUid.getScheme(), _viewDefinitionUid.getValue(), _viewDefinitionUid.getVersion(), 1, now, now,
        new SqlParameterValue(Types.TIMESTAMP, null), toSqlTimestamp(_valuationTime), 0, false);
  }

  @Override
  protected void doTearDown() {
    _batchMaster = null;
  }

}
