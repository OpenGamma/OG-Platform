/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch;

import com.google.common.collect.Sets;
import com.opengamma.DataNotFoundException;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.impl.SimpleSecurity;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.test.MockSecurity;
import com.opengamma.engine.value.*;
import com.opengamma.engine.view.CycleInfo;
import com.opengamma.engine.view.InMemoryViewComputationResultModel;
import com.opengamma.engine.view.ViewResultModel;
import com.opengamma.engine.view.calcnode.InvocationResult;
import com.opengamma.id.*;
import com.opengamma.id.VersionCorrection;
import com.opengamma.masterdb.DbMasterTestUtils;
import com.opengamma.masterdb.batch.document.DbBatchDocumentMaster;
import com.opengamma.util.test.DbTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import javax.time.Instant;
import javax.time.calendar.LocalTime;
import javax.time.calendar.OffsetDateTime;
import java.util.*;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Collections.emptyList;
import static org.testng.AssertJUnit.*;

/**
 * Test DbBatchWriter.
 */
public class DbBatchWriterTest extends DbTest {

  private DbBatchWriter _batchWriter;
  private BatchId _batchId;
  private CycleInfo _cycleInfoStub;
  private ComputationTargetSpecification _compTargetSpec;
  private ValueRequirement _requirement;
  private ValueSpecification _specification;

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public DbBatchWriterTest(String databaseType, final String databaseVersion) {
    super(databaseType, databaseVersion);
  }


  public Class<?>[] getHibernateMappingClasses() {
    return DbBatchWriter.getHibernateMappingClasses();
  }

  @BeforeMethod
  public void setUp() throws Exception {
    super.setUp();

    ConfigurableApplicationContext context = DbMasterTestUtils.getContext(getDatabaseType());
    _batchWriter = (DbBatchWriter) context.getBean(getDatabaseType() + "DbBatchWriter");

    final String calculationConfigName = "config_1";

    _compTargetSpec = new ComputationTargetSpecification(ComputationTargetType.SECURITY, UniqueId.of("Sec", "APPL"));
    final Security security = new MockSecurity(_compTargetSpec.getUniqueId(), "APPL", "equity", ExternalIdBundle.of("Sec", "APPL"));

    _requirement = new ValueRequirement("FAIR_VALUE", security);
    _specification = new ValueSpecification(_requirement, "IDENTITY_FUNCTION");
    
    final Instant _valuationTime = OffsetDateTime.parse("2011-12-14T14:20:17.143Z").toInstant();

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
        return _valuationTime;
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

  //-------------------------------------------------------------------------


  @Test
  public void getNonExistentRiskRunFromDb() {
    Batch batch = new Batch(_cycleInfoStub);
    RiskRun run = _batchWriter.findRiskRunInDb(batch);
    assertNull(run);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void tryToGetNonExistentLiveDataSnapshot() {
    _batchWriter.getDbConnector().getTransactionTemplate().execute(new TransactionCallback<Void>() {
      @Override
      public Void doInTransaction(TransactionStatus status) {
        _batchWriter.getLiveDataSnapshotInTransaction(UniqueId.of("none", "none", "none"));
        return null;
      }
    });
  }

  @Test
  public void getLiveDataField() {
    _batchWriter.getDbConnector().getTransactionTemplate().execute(new TransactionCallback<Void>() {
      @Override
      public Void doInTransaction(TransactionStatus status) {
        // create
        LiveDataField field1 = _batchWriter.getLiveDataField("test_field");
        assertNotNull(field1);
        assertEquals("test_field", field1.getName());

        // getId
        LiveDataField field2 = _batchWriter.getLiveDataField("test_field");
        assertEquals(field1, field2);
        return null;
      }
    });
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void addValuesToNonexistentSnapshot() {
    Batch batch = new Batch(_cycleInfoStub);
    _batchWriter.addValuesToSnapshot(batch.getBatchId().getMarketDataSnapshotUid(), Collections.<LiveDataValue>emptySet());
  }

  @Test
  public void addValuesToIncompleteSnapshot() {
    Batch batch = new Batch(_cycleInfoStub);
    _batchWriter.createLiveDataSnapshot(batch.getBatchId().getMarketDataSnapshotUid());

    _batchWriter.addValuesToSnapshot(batch.getBatchId().getMarketDataSnapshotUid(), Collections.<LiveDataValue>emptySet());

    Set<LiveDataValue> liveDataValues = _batchWriter.getSnapshotValues(batch.getBatchId().getMarketDataSnapshotUid());
    assertNotNull(liveDataValues);
    assertEquals(0, liveDataValues.size());

    final Set<ComputationTargetSpecification> specs = Sets.newHashSet();
    specs.add(new ComputationTargetSpecification(UniqueId.of("BUID", "EQ12345", null)));
    specs.add(new ComputationTargetSpecification(UniqueId.of("BUID", "EQ12346", "1")));
    specs.add(new ComputationTargetSpecification(UniqueId.of("BUID", "EQ12347", "2")));

    Set<LiveDataValue> values = new HashSet<LiveDataValue>();
    for (ComputationTargetSpecification spec : specs) {
      values.add(new LiveDataValue(spec, "field_name", 123.45, "value_name"));
    }

    _batchWriter.getDbConnector().getTransactionTemplate().execute(new TransactionCallback<Void>() {
      @Override
      public Void doInTransaction(TransactionStatus status) {
        // manually populationg db with computation targets
        for (ComputationTargetSpecification spec : specs) {
          _batchWriter.getOrCreateComputationTargetInTransaction(spec, "target_name");
        }
        return null;
      }
    });
    _batchWriter.addValuesToSnapshot(batch.getBatchId().getMarketDataSnapshotUid(), values);

    liveDataValues = _batchWriter.getSnapshotValues(batch.getBatchId().getMarketDataSnapshotUid());
    assertEquals(specs.size(), liveDataValues.size());

    Map<ComputationTargetSpecification, LiveDataValue> liveDataValuesMap = newHashMap();
    for (LiveDataValue liveDataValue : liveDataValues) {
      liveDataValuesMap.put(liveDataValue.getComputationTargetSpecification(), liveDataValue);
    }

    for (ComputationTargetSpecification spec : specs) {
      LiveDataValue liveDataValue = liveDataValuesMap.get(spec);
      assertNotNull(liveDataValue);
      assertEquals(spec, liveDataValue.getComputationTargetSpecification());
      assertEquals("field_name", liveDataValue.getFieldName());
      //TODO the human readable name set on LiveDataValue is lost !!!!
      assertEquals("target_name", liveDataValue.getValueName());
      assertEquals(123.45, liveDataValue.getValue(), 0.000001);
    }

    // should not add anything extra
    _batchWriter.addValuesToSnapshot(batch.getBatchId().getMarketDataSnapshotUid(), values);
    liveDataValues = _batchWriter.getSnapshotValues(batch.getBatchId().getMarketDataSnapshotUid());
    assertEquals(3, liveDataValues.size());

    // should update 2, add 1
    values = new HashSet<LiveDataValue>();
    values.add(new LiveDataValue(new ComputationTargetSpecification(UniqueId.of("BUID", "EQ12345", null)), "field_name", 123.46, "value_name"));
    values.add(new LiveDataValue(new ComputationTargetSpecification(UniqueId.of("BUID", "EQ12347", "2")), "field_name", 123.47, "value_name"));
    values.add(new LiveDataValue(new ComputationTargetSpecification(ExternalId.of("BUID", "EQ12348")), "field_name", 123.45, "value_name"));

    _batchWriter.addValuesToSnapshot(batch.getBatchId().getMarketDataSnapshotUid(), values);
    liveDataValues = _batchWriter.getSnapshotValues(batch.getBatchId().getMarketDataSnapshotUid());
    assertEquals(4, liveDataValues.size());
  }


  /*@Test
  public void fixLiveDataSnapshotTime() {
    _batchWriter.createLiveDataSnapshot(_batch.getBatchId().getMarketDataSnapshotUid());
    _batchWriter.fixLiveDataSnapshotTime(_batch.getBatchId().getMarketDataSnapshotUid(), OffsetTime.now());
  }*/

  /*@Test(expectedExceptions = IllegalArgumentException.class)
  public void tryToFixNonexistentLiveDataSnapshotTime() {
    _batchWriter.fixLiveDataSnapshotTime(_batch.getBatchId().getMarketDataSnapshotUid(), OffsetTime.now());
  }*/

  @Test
  public void createLiveDataSnapshotMultipleTimes() {
    final Batch batch = new Batch(_cycleInfoStub);
    _batchWriter.createLiveDataSnapshot(batch.getBatchId().getMarketDataSnapshotUid());
    _batchWriter.createLiveDataSnapshot(batch.getBatchId().getMarketDataSnapshotUid());
    _batchWriter.getDbConnector().getTransactionTemplate().execute(new TransactionCallback<Void>() {
      @Override
      public Void doInTransaction(TransactionStatus status) {
        assertNotNull(_batchWriter.getLiveDataSnapshotInTransaction(batch.getBatchId().getMarketDataSnapshotUid()));
        return null;
      }
    });

  }

  @Test
  public void createThenGetRiskRun() {
    //assertNull(_riskRun.getOriginalCreationTime());
    Batch batch = new Batch(_cycleInfoStub);
    _batchWriter.createLiveDataSnapshot(batch.getBatchId().getMarketDataSnapshotUid());

    _batchWriter.startBatch(batch, RunCreationMode.AUTO, SnapshotMode.PREPARED);
    RiskRun run = _batchWriter.findRiskRunInDb(batch);

    assertNotNull(run);
    assertNotNull(run.getCreateInstant());
    assertNotNull(run.getStartInstant());
    assertNull(run.getEndInstant());
    assertNotNull(run.getLiveDataSnapshot());

    // Map<String, String> props = run.getPropertiesMap();
    //assertEquals(10, props.size());
    //assertEquals("AD_HOC_RUN", props.getId("observationTime"));
    //assertEquals(ZonedDateTime.ofInstant(run.getCreateInstant(), TimeZone.UTC).toString(), props.getId("valuationTime"));
    //assertEquals("test_view", props.getId("view"));
    //assertEquals(ZonedDateTime.ofInstant(run.getCreateInstant(), TimeZone.UTC).getZone().toString(), props.getId("timeZone"));
    //assertEquals(ZonedDateTime.ofInstant(run.getCreateInstant(), TimeZone.UTC).toLocalTime().toString(), props.getId("staticDataTime"));
    //assertEquals(ZonedDateTime.ofInstant(run.getCreateInstant(), TimeZone.UTC).toLocalTime().toString(), props.getId("configDbTime"));
//    assertEquals("Manual run started on "
//        + run.getCreateInstant().toString()
//        + " by "
//        + System.getProperty("user.name"),
//        props.getId("reason"));
//    assertEquals(run.getCreateInstant().toString(), props.getId("valuationInstant"));
//    assertEquals(run.getCreateInstant().toInstant().toString(), props.getId("configDbInstant"));
//    assertEquals(run.getCreateInstant().toString(), props.getId("staticDataInstant"));
    //assertEquals(run.getCreateInstant().toInstant(), _riskRun.getOriginalCreationTime());

    // getId
    RiskRun run2 = _batchWriter.findRiskRunInDb(batch);
    assertEquals(run.getId(), run2.getId());
  }


  @Test
  public void startAndEndBatch() {
    Batch batch = new Batch(_cycleInfoStub);
    _batchWriter.createLiveDataSnapshot(batch.getBatchId().getMarketDataSnapshotUid());
    _batchWriter.startBatch(batch, RunCreationMode.AUTO, SnapshotMode.PREPARED);

    RiskRun run1 = _batchWriter.findRiskRunInDb(batch);
    assertNotNull(run1);
    assertNotNull(run1.getStartInstant());
    assertNull(run1.getEndInstant());

    RiskRun run2 = _batchWriter.findRiskRunInDb(batch);
    assertEquals(run1.getId(), run2.getId());

    _batchWriter.endBatch(batch.getUniqueId());

    run1 = _batchWriter.findRiskRunInDb(batch);

    assertNotNull(run1);
    assertNotNull(run1.getStartInstant());
    assertNotNull(run1.getEndInstant());
  }

  @Test
  public void startBatchTwice() {
    Batch batch = new Batch(_cycleInfoStub);

    _batchWriter.createLiveDataSnapshot(batch.getBatchId().getMarketDataSnapshotUid());
    _batchWriter.startBatch(batch, RunCreationMode.AUTO, SnapshotMode.PREPARED);

    RiskRun riskRun2 = _batchWriter.findRiskRunInDb(batch);
    assertNotNull(riskRun2.getCreateInstant());
    assertEquals(0, riskRun2.getNumRestarts());

    _batchWriter.startBatch(batch, RunCreationMode.AUTO, SnapshotMode.PREPARED);
    riskRun2 = _batchWriter.findRiskRunInDb(batch);
    assertEquals(1, riskRun2.getNumRestarts());

    RiskRun riskRun3 = _batchWriter.findRiskRunInDb(batch);
    assertEquals(riskRun2.getId(), riskRun3.getId());
  }

  @Test
  public void getComputationTargetBySpec() {
    final UniqueId uniqueId = UniqueId.of("foo", "bar");

    _batchWriter.getDbConnector().getTransactionTemplate().execute(new TransactionCallback<Void>() {
      @Override
      public Void doInTransaction(TransactionStatus status) {
        ComputationTarget portfolio = _batchWriter.getOrCreateComputationTargetInTransaction(
          new ComputationTargetSpecification(ComputationTargetType.PORTFOLIO_NODE, uniqueId),
          "a portfolio");


        assertNotNull(portfolio);
        assertEquals(ComputationTargetType.PORTFOLIO_NODE.ordinal(), portfolio.getComputationTargetType());
        assertEquals(uniqueId.getScheme(), portfolio.getIdScheme());
        assertEquals(uniqueId.getValue(), portfolio.getIdValue());
        assertEquals(uniqueId.getVersion(), portfolio.getIdVersion());

        ComputationTarget position = _batchWriter.getComputationTargetIntransaction(
          new ComputationTargetSpecification(ComputationTargetType.POSITION, uniqueId));
        assertNull(position);

        ComputationTarget security = _batchWriter.getComputationTargetIntransaction(
          new ComputationTargetSpecification(ComputationTargetType.SECURITY, uniqueId));
        assertNull(security);

        ComputationTarget primitive = _batchWriter.getComputationTargetIntransaction(
          new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, uniqueId));
        assertNull(primitive);


        return null;
      }
    });


  }

  @Test
  public void getComputationTarget() {
    final UniqueId uniqueId = UniqueId.of("foo", "bar", "1");

    final SimpleSecurity mockSecurity = new SimpleSecurity("option");
    mockSecurity.setUniqueId(uniqueId);
    mockSecurity.setName("myOption");

    //Batch batch = new Batch(_batchId, _cycleInfo);
    _batchWriter.getDbConnector().getTransactionTemplate().execute(new TransactionCallback<Void>() {
      @Override
      public Void doInTransaction(TransactionStatus status) {
        ComputationTarget security = _batchWriter.getOrCreateComputationTargetInTransaction(
          new ComputationTargetSpecification(ComputationTargetType.SECURITY, uniqueId),
          mockSecurity.getName());

        assertEquals(ComputationTargetType.SECURITY.ordinal(), security.getComputationTargetType());
        assertEquals("myOption", security.getName());

        ComputationTarget primitive = _batchWriter.getOrCreateComputationTargetInTransaction(
          new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, uniqueId), null);

        assertEquals(ComputationTargetType.PRIMITIVE.ordinal(), primitive.getComputationTargetType());
        assertNull(primitive.getName());
        return null;
      }
    });


  }

  @Test
  public void updateComputationTarget() {
    final UniqueId uniqueId = UniqueId.of("foo", "bar");

    final SimpleSecurity mockSecurity = new SimpleSecurity("option");
    mockSecurity.setUniqueId(uniqueId);
    mockSecurity.setName("myOption");

    _batchWriter.getDbConnector().getTransactionTemplate().execute(new TransactionCallback<Void>() {
      @Override
      public Void doInTransaction(TransactionStatus status) {
        ComputationTarget security = _batchWriter.getOrCreateComputationTargetInTransaction(
          new ComputationTargetSpecification(ComputationTargetType.SECURITY, uniqueId), mockSecurity.getName());
        assertEquals(ComputationTargetType.SECURITY.ordinal(), security.getComputationTargetType());
        assertEquals(security.getName(), mockSecurity.getName());

        com.opengamma.engine.ComputationTarget target = new com.opengamma.engine.ComputationTarget(mockSecurity);

        security = _batchWriter.getOrCreateComputationTargetInTransaction(target.toSpecification(), target.getName());
        assertEquals(ComputationTargetType.SECURITY.ordinal(), security.getComputationTargetType());
        assertEquals("myOption", security.getName());
        return null;
      }
    });

  }


  @Test
  public void getValueName() {
    _batchWriter.getDbConnector().getTransactionTemplate().execute(new TransactionCallback<Void>() {
      @Override
      public Void doInTransaction(TransactionStatus status) {
        // create
        RiskValueName valueName1 = _batchWriter.getRiskValueName("test_name");
        assertNotNull(valueName1);
        assertEquals("test_name", valueName1.getName());

        // getId
        RiskValueName valueName2 = _batchWriter.getRiskValueName("test_name");
        assertEquals(valueName1, valueName2);
        return null;
      }
    });
  }

  @Test
  public void getValueConstraint() {
    _batchWriter.getDbConnector().getTransactionTemplate().execute(new TransactionCallback<Void>() {
      @Override
      public Void doInTransaction(TransactionStatus status) {
        // create
        RiskValueRequirement valueRequirement1 = _batchWriter.getRiskValueRequirement(ValueProperties.parse("currency=USD"));
        assertNotNull(valueRequirement1);
        assertEquals("{\"properties\":[{\"values\":[\"USD\"],\"name\":\"currency\"}]}", valueRequirement1.getSyntheticForm());

        // getId
        RiskValueRequirement valueRequirement2 = _batchWriter.getRiskValueRequirement(ValueProperties.parse("currency=USD"));
        assertEquals(valueRequirement1, valueRequirement2);
        assertEquals(valueRequirement1.getId(), valueRequirement2.getId());
        return null;
      }
    });
  }


  @Test
  public void getFunctionUniqueId() {
    _batchWriter.getDbConnector().getTransactionTemplate().execute(new TransactionCallback<Void>() {
      @Override
      public Void doInTransaction(TransactionStatus status) {
        // create
        FunctionUniqueId id1 = _batchWriter.getFunctionUniqueIdInTransaction("test_id");
        assertNotNull(id1);
        assertEquals("test_id", id1.getUniqueId());

        // getId
        FunctionUniqueId id2 = _batchWriter.getFunctionUniqueIdInTransaction("test_id");
        assertEquals(id1, id2);
        return null;
      }
    });
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void deleteNonExisting() {
    Batch batch = new Batch(_cycleInfoStub);
    batch.setUniqueId(UniqueId.of("---", "000"));
    assertNull(_batchWriter.getRiskRun(batch.getUniqueId()));
    _batchWriter.deleteBatch(batch.getUniqueId());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void getRiskWithoutUniqueId() {
    Batch batch = new Batch(_cycleInfoStub);
    assertNull(_batchWriter.getRiskRun(batch.getUniqueId()));
  }

  @Test
  public void delete() {
    Batch batch = new Batch(_cycleInfoStub);
    assertNull(_batchWriter.findRiskRunInDb(batch.getBatchId()));

    _batchWriter.createLiveDataSnapshot(batch.getBatchId().getMarketDataSnapshotUid());
    _batchWriter.startBatch(batch, RunCreationMode.AUTO, SnapshotMode.PREPARED);

    assertNotNull(_batchWriter.findRiskRunInDb(batch.getBatchId()));    
    _batchWriter.deleteBatch(batch.getUniqueId());
    assertNull(_batchWriter.findRiskRunInDb(batch.getBatchId()));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void addJobResultsToUnstartedBatch() {
    Batch batch = new Batch(_cycleInfoStub);
    ViewResultModel result = new InMemoryViewComputationResultModel();
    _batchWriter.addJobResults(batch.getUniqueId(), result);
  }

/*  @Test(expectedExceptions = IllegalArgumentException.class)
  public void addJobResultsWithoutSnapshot() {
    Batch batch = new Batch(_cycleInfoStub);
    _batchWriter.startBatch(batch, RunCreationMode.AUTO, SnapshotMode.PREPARED);
    ViewResultModel result = new InMemoryViewComputationResultModel();
    _batchWriter.addJobResults(batch.getUniqueId(), result);
  }*/

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void addJobResultsWithoutExistingComputeNodeId() {
    Batch batch = new Batch(_cycleInfoStub);
    _batchWriter.createLiveDataSnapshot(_batchId.getMarketDataSnapshotUid());
    _batchWriter.startBatch(batch, RunCreationMode.AUTO, SnapshotMode.PREPARED);
    InMemoryViewComputationResultModel result = new InMemoryViewComputationResultModel();
    result.addValue("config_1",
      new ComputedValue(
        new ValueSpecification(
          "value",
          new ComputationTargetSpecification(ComputationTargetType.SECURITY, UniqueId.of("Sec", "APPL")), ValueProperties.with(ValuePropertyNames.FUNCTION, "asd").get()),
        1000.0) {{
        this.setInvocationResult(InvocationResult.SUCCESS);
        this.setRequirement(_requirement);
      }});
    _batchWriter.addJobResults(batch.getUniqueId(), result);
  }

  @Test
  public void addJobResults() {
    Batch batch = new Batch(_cycleInfoStub);
    _batchWriter.createLiveDataSnapshot(_batchId.getMarketDataSnapshotUid());
    _batchWriter.startBatch(batch, RunCreationMode.AUTO, SnapshotMode.PREPARED);
    InMemoryViewComputationResultModel result = new InMemoryViewComputationResultModel();
    result.addValue("config_1",
      new ComputedValue(
        _specification,
        1000.0) {{
        this.setInvocationResult(InvocationResult.SUCCESS);
        this.setRequirement(_requirement);
        this.setComputeNodeId("someComputeNode");
      }});
    _batchWriter.addJobResults(batch.getUniqueId(), result);
  }
}
