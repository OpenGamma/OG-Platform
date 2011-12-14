/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch.document;

import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.masterdb.DbMasterTestUtils;
import com.opengamma.util.test.DbTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import javax.time.Instant;
import javax.time.calendar.OffsetDateTime;

import java.sql.Timestamp;

import static com.opengamma.util.db.DbDateUtils.toSqlTimestamp;

public abstract class AbstractDbBatchDocumentMasterTest extends DbTest {


  protected DbBatchDocumentMaster _batchMaster;
  
  protected UniqueId _marketDataSnapshotUid = UniqueId.of("MrkDta", "market_data_snapshot_uid");
  protected UniqueId _viewDefinitionUid = UniqueId.of("ViewDef", "view definition uid");
  protected VersionCorrection _versionCorrection = VersionCorrection.LATEST;
  protected Instant _valuationTime = OffsetDateTime.parse("2011-01-01T15:58:34.183Z").toInstant();

  private static final Logger s_logger = LoggerFactory.getLogger(AbstractDbBatchDocumentMasterTest.class);

  public AbstractDbBatchDocumentMasterTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
  }

  @BeforeMethod
  public void setUp() throws Exception {
    super.setUp();

    ConfigurableApplicationContext context = DbMasterTestUtils.getContext(getDatabaseType());
    _batchMaster = (DbBatchDocumentMaster) context.getBean(getDatabaseType() + "DbBatchDocumentMaster");
    
    Timestamp now = toSqlTimestamp(Instant.now());

    final SimpleJdbcTemplate template = _batchMaster.getDbConnector().getJdbcTemplate();
    template.update("INSERT INTO rsk_compute_host (id, host_name) VALUES (?,?)", 1, "compute host");
    template.update("INSERT INTO rsk_compute_node (id, compute_host_id, node_name) VALUES (?,?,?)", 1, 1, "compute node");
    //template.update("INSERT INTO rsk_computation_target_type (id, name) VALUES (?,?)", 1, 1, "SECURITY");
    template.update("INSERT INTO rsk_computation_target (id, type_id, id_scheme, id_value, id_version, name) VALUES (?,?,?,?,?,?)", 1, 2, "DbSec", "APPL", null, "Apple");
    template.update("INSERT INTO rsk_function_unique_id (id, unique_id) VALUES (?,?)", 1, "FV");
    template.update("INSERT INTO rsk_live_data_field (id, name) VALUES (?,?)", 1, "field");
    template.update("INSERT INTO rsk_live_data_snapshot (id, market_data_snapshot_uid) VALUES (?,?)", 1, _marketDataSnapshotUid.toString());
    template.update("INSERT INTO rsk_live_data_snapshot_entry (id, snapshot_id, computation_target_id, field_id, value) VALUES (?,?,?,?,?)", 1, 1, 1, 1, 999.99);
    template.update("INSERT INTO rsk_view_definition (id, uid) VALUES (?,?)", 1, _viewDefinitionUid.toString());
    template.update("INSERT INTO rsk_version_correction (id, as_of, corrected_to) VALUES (?,?,?)", 1, _versionCorrection.getVersionAsOf(), _versionCorrection.getCorrectedTo());
    template.update("INSERT INTO rsk_run (id, version_correction_id, view_definition_id, live_data_snapshot_id, create_instant, start_instant, end_instant, valuation_time, num_restarts, complete) VALUES (?,?,?,?,?,?,?,?,?,?)",
      1, 1, 1, 1, now, now, null, toSqlTimestamp(_valuationTime), 0, false);


  }

  @AfterMethod
  public void tearDown() throws Exception {
    _batchMaster = null;
    super.tearDown();
  }


}
