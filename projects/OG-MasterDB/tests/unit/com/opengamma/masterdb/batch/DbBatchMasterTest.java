/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch;

import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.test.MockSecurity;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.CycleInfo;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.VersionCorrection;
import com.opengamma.masterdb.DbMasterTestUtils;
import com.opengamma.util.test.DbTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import javax.time.Instant;
import java.util.*;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class DbBatchMasterTest extends DbTest {

  private DbBatchMaster _batchMaster;
  private BatchId _batchId;
  private CycleInfo _cycleInfoStub;
  private ComputationTargetSpecification _compTargetSpec;
  private ValueRequirement _requirement;
  private ValueSpecification _specification;

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public DbBatchMasterTest(String databaseType, final String databaseVersion) {
    super(databaseType, databaseVersion);
  }

  @BeforeMethod
  public void setUp() throws Exception {
    super.setUp();

    ConfigurableApplicationContext context = DbMasterTestUtils.getContext(getDatabaseType());
    _batchMaster = (DbBatchMaster) context.getBean(getDatabaseType() + "DbBatchMaster");

    final String calculationConfigName = "config_1";

    _compTargetSpec = new ComputationTargetSpecification(ComputationTargetType.SECURITY, UniqueId.of("Sec", "APPL"));
    final Security security = new MockSecurity(_compTargetSpec.getUniqueId(), "APPL", "equity", ExternalIdBundle.of("Sec", "APPL"));

    _requirement = new ValueRequirement("FAIR_VALUE", security);
    _specification = new ValueSpecification(_requirement, "IDENTITY_FUNCTION");

    _cycleInfoStub = new CycleInfo() {

      @Override
      public Collection<String> getAllCalculationConfigurationNames() {
        return newArrayList(calculationConfigName);
      }

      @Override
      public Collection<com.opengamma.engine.ComputationTarget> getComputationTargetsByConfigName(String calcConfName) {
        if (calcConfName.equals(calculationConfigName)) {
          return newArrayList(
            new com.opengamma.engine.ComputationTarget(ComputationTargetType.PRIMITIVE, new UniqueIdentifiable() {
              @Override
              public UniqueId getUniqueId() {
                return UniqueId.of("Primitive", "Value");
              }
            }),
            new com.opengamma.engine.ComputationTarget(
              _compTargetSpec.getType(),
              security
            )
          );
        } else {
          return emptyList();
        }
      }

      @Override
      public Map<ValueSpecification, Set<ValueRequirement>> getTerminalOutputsByConfigName(String calcConfName) {
        return new HashMap<ValueSpecification, Set<ValueRequirement>>() {{
          put(_specification, new HashSet<ValueRequirement>() {{
            add(_requirement);
          }});
        }};
      }

      @Override
      public UniqueId getMarketDataSnapshotUniqueId() {
        return UniqueId.of("snapshot", "snapshot", "snapshot");
      }

      @Override
      public Instant getValuationTime() {
        return Instant.now();
      }

      @Override
      public VersionCorrection getVersionCorrection() {
        return VersionCorrection.LATEST;
      }

      @Override
      public UniqueId getViewDefinitionUid() {
        return UniqueId.of("viewdef", "viewdef", "viewdef");
      }
    };

    _batchId = new BatchId(
      _cycleInfoStub.getMarketDataSnapshotUniqueId(),
      _cycleInfoStub.getViewDefinitionUid(),
      _cycleInfoStub.getVersionCorrection(),
      _cycleInfoStub.getValuationTime());
  }

  @Test
  public void searchAllBatches() {
    Batch batch = new Batch(_cycleInfoStub);

    _batchMaster.createLiveDataSnapshot(batch.getBatchId().getMarketDataSnapshotUid());
    _batchMaster.startBatch(batch, RunCreationMode.AUTO, SnapshotMode.PREPARED);

    BatchSearchRequest request = new BatchSearchRequest();

    BatchSearchResult result = _batchMaster.search(request);
    assertNotNull(result);

    assertEquals(1, result.getDocuments().size());
    BatchDocument item = result.getDocuments().get(0);
    assertNotNull(item.getUniqueId());
    assertEquals(item.getValuationTime(), batch.getBatchId().getValuationTime());
    assertEquals(BatchStatus.RUNNING, item.getStatus());

    _batchMaster.endBatch(batch.getUniqueId());
    result = _batchMaster.search(request);
    assertEquals(1, result.getDocuments().size());
    item = result.getDocuments().get(0);
    assertNotNull(item.getUniqueId());
    assertEquals(item.getValuationTime(), batch.getBatchId().getValuationTime());
    assertEquals(BatchStatus.COMPLETE, item.getStatus());
  }


  @Test
  public void searchOneBatch() {
    Batch batch = new Batch(_cycleInfoStub);

    _batchMaster.createLiveDataSnapshot(batch.getBatchId().getMarketDataSnapshotUid());
    _batchMaster.startBatch(batch, RunCreationMode.AUTO, SnapshotMode.PREPARED);

    BatchSearchRequest request = new BatchSearchRequest();
    request.setValuationTime(batch.getBatchId().getValuationTime());

    BatchSearchResult result = _batchMaster.search(request);
    assertNotNull(result);

    assertEquals(1, result.getDocuments().size());
    BatchDocument item = result.getDocuments().get(0);
    assertNotNull(item.getUniqueId());
    assertEquals(item.getValuationTime(), batch.getBatchId().getValuationTime());
    assertEquals(BatchStatus.RUNNING, item.getStatus());
  }


/*  @Test(expectedExceptions = DataNotFoundException.class)
  public void getDataNonexistentBatch() {
    Batch batch = new Batch(_batchId, _cycleInfoStub);

    BatchGetRequest request = new BatchGetRequest();

    String viewDefinitionUid = "viewDefinitionUid~1";
    String marketDataSnapshotUid = "marketDataSnapshotUid~1";
    String valuationTime = Instant.now().toString();
    String versionCorrection = VersionCorrection.of(Instant.now(), Instant.now()).toString();


    request.setUniqueId(UniqueId.of("DbBat", Joiner.on(BatchDocument.BATCH_DOCUMENT_UNIQUE_ID_DELIMITER).
      join(newArrayList(viewDefinitionUid, marketDataSnapshotUid, valuationTime, versionCorrection))));

    _batchMaster.get(request);
  }


  @Test
  public void getDataExistingBatch() {
    final Batch batch = new Batch(_batchId, _cycleInfoStub);

    final BatchGetRequest request = new BatchGetRequest();

    _batchMaster.getDbConnector().getTransactionTemplate().execute(new TransactionCallback<Void>() {
      @Override
      public Void doInTransaction(TransactionStatus status) {


        _batchWriter.createLiveDataSnapshot(_batchId.getMarketDataSnapshotUid());
        _batchWriter.startBatch(batch);

        String viewDefinitionUid = _batchId.getViewDefinitionUid().toString();
        String marketDataSnapshotUid = _batchId.getMarketDataSnapshotUid().toString();
        String valuationTime = _batchId.getValuationTime().toString();
        String versionCorrection = _batchId.getVersionCorrection().toString();

        UniqueId batchUniqueIdStr = UniqueId.of("DbBat", Joiner.on(BatchDocument.BATCH_DOCUMENT_UNIQUE_ID_DELIMITER).
          join(newArrayList(viewDefinitionUid, marketDataSnapshotUid, valuationTime, versionCorrection)));

        request.setUniqueId(batchUniqueIdStr);
        return null;
      }
    });

    _batchMaster.getDbConnector().getTransactionTemplate().execute(new TransactionCallback<Void>() {
      @Override
      public Void doInTransaction(TransactionStatus status) {
        BatchDocument result = _batchMaster.get(request);
        assertNotNull(result);
        assertTrue(result.getData().isEmpty());
        return null;
      }
    });
  }

  @Test
  public void getErrorsExistingBatch() {
    final Batch batch = new Batch(_batchId, _cycleInfoStub);
    final BatchGetRequest request = new BatchGetRequest();

    _batchMaster.getDbConnector().getTransactionTemplate().execute(new TransactionCallback<Void>() {
      @Override
      public Void doInTransaction(TransactionStatus status) {

        _batchWriter.createLiveDataSnapshot(_batchId.getMarketDataSnapshotUid());
        _batchWriter.startBatch(batch);

        String viewDefinitionUid = _batchId.getViewDefinitionUid().toString();
        String marketDataSnapshotUid = _batchId.getMarketDataSnapshotUid().toString();
        String valuationTime = _batchId.getValuationTime().toString();
        String versionCorrection = _batchId.getVersionCorrection().toString();

        UniqueId batchUniqueIdStr = UniqueId.of("DbBat", Joiner.on(BatchDocument.BATCH_DOCUMENT_UNIQUE_ID_DELIMITER).
          join(newArrayList(viewDefinitionUid, marketDataSnapshotUid, valuationTime, versionCorrection)));

        request.setUniqueId(batchUniqueIdStr);
        request.setDataPagingRequest(PagingRequest.NONE);
        request.setErrorPagingRequest(PagingRequest.ALL);

        return null;
      }
    });

    _batchMaster.getDbConnector().getTransactionTemplate().execute(new TransactionCallback<Void>() {
      @Override
      public Void doInTransaction(TransactionStatus status) {

        BatchDocument result = _batchMaster.get(request);
        assertNotNull(result);
        assertTrue(result.getErrors().isEmpty());

        return null;
      }
    });
  }*/

}
