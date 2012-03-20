/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.emptyList;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.time.Instant;
import javax.time.calendar.OffsetDateTime;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.DataNotFoundException;
import com.opengamma.batch.RunCreationMode;
import com.opengamma.batch.SnapshotMode;
import com.opengamma.batch.domain.FunctionUniqueId;
import com.opengamma.batch.domain.HbComputationTargetSpecification;
import com.opengamma.batch.domain.MarketData;
import com.opengamma.batch.domain.MarketDataValue;
import com.opengamma.batch.domain.RiskRun;
import com.opengamma.batch.domain.RiskValueSpecification;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.impl.SimpleSecurity;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.CycleInfo;
import com.opengamma.engine.view.InMemoryViewComputationResultModel;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.calcnode.InvocationResult;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.VersionCorrection;
import com.opengamma.masterdb.DbMasterTestUtils;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.DbTest;

/**
 * Test DbBatchWriter.
 */
public class DbBatchWriterTest extends DbTest {

  private DbBatchMaster _batchMaster;
  private DbBatchWriter _batchWriter;
  private CycleInfo _cycleInfoStub;
  private ComputationTargetSpecification _compTargetSpec;
  private ValueRequirement _requirement;
  private ValueSpecification _specification;

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public DbBatchWriterTest(String databaseType, final String databaseVersion) {
    super(databaseType, databaseVersion);
  }


  @BeforeMethod
  public void setUp() throws Exception {
    super.setUp();

    ConfigurableApplicationContext context = DbMasterTestUtils.getContext(getDatabaseType());
    _batchMaster = (DbBatchMaster) context.getBean(getDatabaseType() + "DbBatchMaster");
    _batchWriter = new DbBatchWriter(_batchMaster.getDbConnector());

    final String calculationConfigName = "config_1";

    _compTargetSpec = new ComputationTargetSpecification(ComputationTargetType.SECURITY, UniqueId.of("Sec", "APPL"));
    final Security security = new SimpleSecurity(_compTargetSpec.getUniqueId(), ExternalIdBundle.of("Sec", "APPL"), "equity", "APPL");

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

  }

  //-------------------------------------------------------------------------  


  @Test(expectedExceptions = IllegalArgumentException.class)
  public void addValuesToNonexistentSnapshot() {
    _batchMaster.addValuesToMarketData(ObjectId.of("nonexistent", "nonexistent"), Collections.<MarketDataValue>emptySet());
  }

  @Test
  public void addValuesToIncompleteSnapshot() {
    MarketData marketData = _batchWriter.createOrGetMarketDataInTransaction(_cycleInfoStub.getMarketDataSnapshotUniqueId());

    _batchMaster.addValuesToMarketData(marketData.getObjectId(), Collections.<MarketDataValue>emptySet());

    List<MarketDataValue> marketDataValues = _batchMaster.getMarketDataValues(marketData.getObjectId(), PagingRequest.ALL).getFirst();
    assertNotNull(marketDataValues);
    assertEquals(0, marketDataValues.size());

    final Set<ComputationTargetSpecification> specs = Sets.newHashSet();
    specs.add(new ComputationTargetSpecification(UniqueId.of("BUID", "EQ12345", null)));
    specs.add(new ComputationTargetSpecification(UniqueId.of("BUID", "EQ12346", "1")));
    specs.add(new ComputationTargetSpecification(UniqueId.of("BUID", "EQ12347", "2")));


    final Map<ComputationTargetSpecification, Long> compTargetSpecIdx = new HashMap<ComputationTargetSpecification, Long>();
    final Map<Long, ComputationTargetSpecification> reversedCompTargetSpecIdx = new HashMap<Long, ComputationTargetSpecification>();    
    

    _batchMaster.getDbConnector().getTransactionTemplate().execute(new TransactionCallback<Void>() {
      @Override
      public Void doInTransaction(TransactionStatus status) {
        // manually populationg db with computation targets
        for (ComputationTargetSpecification spec : specs) {
          HbComputationTargetSpecification hbComputationTargetSpecification = _batchWriter.getOrCreateComputationTargetInTransaction(spec);
          compTargetSpecIdx.put(spec, hbComputationTargetSpecification.getId());
          reversedCompTargetSpecIdx.put(hbComputationTargetSpecification.getId(), spec);
        }
        return null;
      }
    });

    Set<MarketDataValue> values = new HashSet<MarketDataValue>();
    for (ComputationTargetSpecification spec : specs) {
      values.add(new MarketDataValue(spec, 123.45, "value_name"));
    }


    _batchMaster.addValuesToMarketData(marketData.getObjectId(), values);

    marketDataValues = _batchMaster.getMarketDataValues(marketData.getObjectId(), PagingRequest.ALL).getFirst();
    assertEquals(specs.size(), marketDataValues.size());

    Map<Long, MarketDataValue> marketDataValuesMap = newHashMap();
    for (MarketDataValue value : marketDataValues) {
      marketDataValuesMap.put(value.getComputationTargetSpecificationId(), value);
    }

    for (ComputationTargetSpecification spec : specs) {
      Long targetSpecificationId = compTargetSpecIdx.get(spec);
      MarketDataValue marketDataValue = marketDataValuesMap.get(targetSpecificationId);
      assertNotNull(marketDataValue);
      assertEquals(spec, reversedCompTargetSpecIdx.get(marketDataValue.getComputationTargetSpecificationId()));
      assertEquals("value_name", marketDataValue.getName());
      assertEquals(123.45, marketDataValue.getValue(), 0.000001);
    }

    // should not add anything extra
    _batchMaster.addValuesToMarketData(marketData.getObjectId(), values);
    marketDataValues = _batchMaster.getMarketDataValues(marketData.getObjectId(), PagingRequest.ALL).getFirst();
    assertEquals(3, marketDataValues.size());

    // should update 2, add 1
    values = new HashSet<MarketDataValue>();
    values.add(new MarketDataValue(new ComputationTargetSpecification(UniqueId.of("BUID", "EQ12345", null)), 123.46, "value_name"));
    values.add(new MarketDataValue(new ComputationTargetSpecification(UniqueId.of("BUID", "EQ12347", "2")), 123.47, "value_name"));
    values.add(new MarketDataValue(new ComputationTargetSpecification(ExternalId.of("BUID", "EQ12348")), 123.45, "value_name"));

    _batchMaster.addValuesToMarketData(marketData.getObjectId(), values);
    marketDataValues = _batchMaster.getMarketDataValues(marketData.getObjectId(), PagingRequest.ALL).getFirst();
    assertEquals(4, marketDataValues.size());
  }


  /*@Test
  public void fixLiveDataSnapshotTime() {
    _batchMaster.createLiveDataSnapshot(_batch.getBatchId().getBatchSnapshotId());
    _batchMaster.fixLiveDataSnapshotTime(_batch.getBatchId().getBatchSnapshotId(), OffsetTime.now());
  }*/

  /*@Test(expectedExceptions = IllegalArgumentException.class)
  public void tryToFixNonexistentLiveDataSnapshotTime() {
    _batchMaster.fixLiveDataSnapshotTime(_batch.getBatchId().getBatchSnapshotId(), OffsetTime.now());
  }*/

  @Test
  public void createLiveDataSnapshotMultipleTimes() {
    final UniqueId marketDataUid = _cycleInfoStub.getMarketDataSnapshotUniqueId();

    _batchMaster.createMarketData(marketDataUid);
    _batchMaster.createMarketData(marketDataUid);
    _batchMaster.getDbConnector().getTransactionTemplate().execute(new TransactionCallback<Void>() {
      @Override
      public Void doInTransaction(TransactionStatus status) {
        assertNotNull(_batchWriter.createOrGetMarketDataInTransaction(marketDataUid));
        return null;
      }
    });

  }

  @Test
  public void createThenGetRiskRun() {
    final UniqueId marketDataUid = _cycleInfoStub.getMarketDataSnapshotUniqueId();
    
    _batchMaster.createMarketData(marketDataUid);

    RiskRun run = _batchMaster.startRiskRun(_cycleInfoStub, Maps.<String, String>newHashMap(), RunCreationMode.AUTO, SnapshotMode.PREPARED);

    RiskRun run2 = _batchWriter.getRiskRun(run.getObjectId());

    assertNotNull(run2);
    assertNotNull(run2.getCreateInstant());
    assertNotNull(run2.getStartInstant());
    assertNull(run2.getEndInstant());
    assertNotNull(run2.getMarketData());

    // Map<String, String> props = run2.getPropertiesMap();
    //assertEquals(10, props.size());
    //assertEquals("AD_HOC_RUN", props.getId("observationTime"));
    //assertEquals(ZonedDateTime.ofInstant(run2.getCreateInstant(), TimeZone.UTC).toString(), props.getId("valuationTime"));
    //assertEquals("test_view", props.getId("view"));
    //assertEquals(ZonedDateTime.ofInstant(run2.getCreateInstant(), TimeZone.UTC).getZone().toString(), props.getId("timeZone"));
    //assertEquals(ZonedDateTime.ofInstant(run2.getCreateInstant(), TimeZone.UTC).toLocalTime().toString(), props.getId("staticDataTime"));
    //assertEquals(ZonedDateTime.ofInstant(run2.getCreateInstant(), TimeZone.UTC).toLocalTime().toString(), props.getId("configDbTime"));
//    assertEquals("Manual run2 started on "
//        + run2.getCreateInstant().toString()
//        + " by "
//        + System.getProperty("user.name"),
//        props.getId("reason"));
//    assertEquals(run2.getCreateInstant().toString(), props.getId("valuationInstant"));
//    assertEquals(run2.getCreateInstant().toInstant().toString(), props.getId("configDbInstant"));
//    assertEquals(run2.getCreateInstant().toString(), props.getId("staticDataInstant"));
    //assertEquals(run2.getCreateInstant().toInstant(), _riskRun.getOriginalCreationTime());

    // getId
    RiskRun run3 = _batchWriter.getRiskRun(run.getObjectId());
    assertEquals(run2.getId(), run3.getId());
  }


  @Test
  public void startAndEndBatch() {
    final UniqueId marketDataUid = _cycleInfoStub.getMarketDataSnapshotUniqueId();
        
    _batchMaster.createMarketData(marketDataUid);

    RiskRun run = _batchMaster.startRiskRun(_cycleInfoStub, Maps.<String, String>newHashMap(), RunCreationMode.AUTO, SnapshotMode.PREPARED);

    RiskRun run1 = _batchWriter.getRiskRun(run.getObjectId());
    assertNotNull(run1);
    assertNotNull(run1.getStartInstant());
    assertNull(run1.getEndInstant());

    RiskRun run2 = _batchWriter.getRiskRun(run.getObjectId());
    assertEquals(run1.getId(), run2.getId());

    _batchMaster.endRiskRun(run.getObjectId());

    run1 = _batchWriter.getRiskRun(run.getObjectId());

    assertNotNull(run1);
    assertNotNull(run1.getStartInstant());
    assertNotNull(run1.getEndInstant());
  }

  @Test
  public void startBatchTwice() {
    final UniqueId marketDataUid = _cycleInfoStub.getMarketDataSnapshotUniqueId();
            
    _batchMaster.createMarketData(marketDataUid);

    RiskRun run1 = _batchMaster.startRiskRun(_cycleInfoStub, Maps.<String, String>newHashMap(), RunCreationMode.AUTO, SnapshotMode.PREPARED);

    RiskRun run2 = _batchWriter.getRiskRun(run1.getObjectId());
    assertNotNull(run2.getCreateInstant());
    assertEquals(0, run2.getNumRestarts());

    RiskRun run10 = _batchMaster.startRiskRun(_cycleInfoStub, Maps.<String, String>newHashMap(), RunCreationMode.AUTO, SnapshotMode.PREPARED);
    RiskRun run20 = _batchWriter.getRiskRun(run10.getObjectId());
    assertEquals(1, run20.getNumRestarts());

    RiskRun run3 = _batchWriter.getRiskRun(run10.getObjectId());
    assertEquals(run20.getId(), run3.getId());
  }

  @Test
  public void getComputationTargetBySpec() {
    final UniqueId uniqueId = UniqueId.of("foo", "bar");

    _batchMaster.getDbConnector().getTransactionTemplate().execute(new TransactionCallback<Void>() {
      @Override
      public Void doInTransaction(TransactionStatus status) {
        HbComputationTargetSpecification portfolio = _batchWriter.getOrCreateComputationTargetInTransaction(
          new ComputationTargetSpecification(ComputationTargetType.PORTFOLIO_NODE, uniqueId));


        assertNotNull(portfolio);
        assertEquals(ComputationTargetType.PORTFOLIO_NODE, portfolio.getType());
        assertEquals(uniqueId, portfolio.getUniqueId());

        HbComputationTargetSpecification position = _batchWriter.getComputationTargetIntransaction(
          new ComputationTargetSpecification(ComputationTargetType.POSITION, uniqueId));
        assertNull(position);

        HbComputationTargetSpecification security = _batchWriter.getComputationTargetIntransaction(
          new ComputationTargetSpecification(ComputationTargetType.SECURITY, uniqueId));
        assertNull(security);

        HbComputationTargetSpecification primitive = _batchWriter.getComputationTargetIntransaction(
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
    _batchMaster.getDbConnector().getTransactionTemplate().execute(new TransactionCallback<Void>() {
      @Override
      public Void doInTransaction(TransactionStatus status) {
        HbComputationTargetSpecification security = _batchWriter.getOrCreateComputationTargetInTransaction(
          new ComputationTargetSpecification(ComputationTargetType.SECURITY, uniqueId));

        assertEquals(ComputationTargetType.SECURITY, security.getType());

        HbComputationTargetSpecification primitive = _batchWriter.getOrCreateComputationTargetInTransaction(
          new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, uniqueId));

        assertEquals(ComputationTargetType.PRIMITIVE, primitive.getType());
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

    _batchMaster.getDbConnector().getTransactionTemplate().execute(new TransactionCallback<Void>() {
      @Override
      public Void doInTransaction(TransactionStatus status) {
        HbComputationTargetSpecification security = _batchWriter.getOrCreateComputationTargetInTransaction(
          new ComputationTargetSpecification(ComputationTargetType.SECURITY, uniqueId));
        assertEquals(ComputationTargetType.SECURITY, security.getType());        

        com.opengamma.engine.ComputationTarget target = new com.opengamma.engine.ComputationTarget(mockSecurity);

        security = _batchWriter.getOrCreateComputationTargetInTransaction(target.toSpecification());
        assertEquals(ComputationTargetType.SECURITY, security.getType());        
        return null;
      }
    });

  }



  @Test
  public void getValueConstraint() {
    _batchMaster.getDbConnector().getTransactionTemplate().execute(new TransactionCallback<Void>() {
      @Override
      public Void doInTransaction(TransactionStatus status) {
        // create
        RiskValueSpecification valueSpecification1 = _batchWriter.getRiskValueSpecification(ValueProperties.parse("currency=USD"));
        assertNotNull(valueSpecification1);
        assertEquals("{\"properties\":[{\"values\":[\"USD\"],\"name\":\"currency\"}]}", valueSpecification1.getSyntheticForm());

        // getId
        RiskValueSpecification valueSpecification2 = _batchWriter.getRiskValueSpecification(ValueProperties.parse("currency=USD"));
        assertEquals(valueSpecification1, valueSpecification2);
        assertEquals(valueSpecification1.getId(), valueSpecification2.getId());
        return null;
      }
    });
  }


  @Test
  public void getFunctionUniqueId() {
    _batchMaster.getDbConnector().getTransactionTemplate().execute(new TransactionCallback<Void>() {
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
    final ObjectId runId = ObjectId.of("---", "000");

    _batchMaster.getDbConnector().getTransactionTemplate().execute(new TransactionCallback<Void>() {
      @Override
      public Void doInTransaction(TransactionStatus status) {
        assertNull(_batchWriter.getRiskRun(runId));
        _batchMaster.deleteRiskRun(runId);
        return null;
      }
    });
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void getRiskWithoutUniqueId() {
    assertNull(_batchWriter.getRiskRun(null));
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void delete() {

    final UniqueId marketDataUid = _cycleInfoStub.getMarketDataSnapshotUniqueId();                
    _batchMaster.createMarketData(marketDataUid);            
    RiskRun run = _batchMaster.startRiskRun(_cycleInfoStub, Maps.<String, String>newHashMap(), RunCreationMode.AUTO, SnapshotMode.PREPARED);

    assertNotNull(_batchWriter.getRiskRun(run.getObjectId()));
    _batchMaster.deleteRiskRun(run.getObjectId());
    assertNull(_batchWriter.getRiskRun(run.getObjectId()));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void addJobResultsToUnstartedBatch() {
    ViewComputationResultModel result = new InMemoryViewComputationResultModel();
    _batchMaster.addJobResults(null, result);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void addJobResultsWithoutExistingComputeNodeId() {
    final UniqueId marketDataUid = _cycleInfoStub.getMarketDataSnapshotUniqueId();
            
    _batchMaster.createMarketData(marketDataUid);    
    
    RiskRun run = _batchMaster.startRiskRun(_cycleInfoStub, Maps.<String, String>newHashMap(), RunCreationMode.AUTO, SnapshotMode.PREPARED);
    
    InMemoryViewComputationResultModel result = new InMemoryViewComputationResultModel();
    result.addValue("config_1",
      new ComputedValue(
        new ValueSpecification(
          "value",
          new ComputationTargetSpecification(ComputationTargetType.SECURITY, UniqueId.of("Sec", "APPL")), ValueProperties.with(ValuePropertyNames.FUNCTION, "asd").get()),
        1000.0) {{
        this.setInvocationResult(InvocationResult.SUCCESS);
        this.setRequirements(newHashSet(_requirement));
      }});
    _batchMaster.addJobResults(run.getObjectId(), result);
  }

  @Test
  public void addJobResults() {
    final UniqueId marketDataUid = _cycleInfoStub.getMarketDataSnapshotUniqueId();                
    _batchMaster.createMarketData(marketDataUid);            
    RiskRun run = _batchMaster.startRiskRun(_cycleInfoStub, Maps.<String, String>newHashMap(), RunCreationMode.AUTO, SnapshotMode.PREPARED);
    InMemoryViewComputationResultModel result = new InMemoryViewComputationResultModel();
    result.addValue("config_1",
      new ComputedValue(
        _specification,
        1000.0) {{
        this.setInvocationResult(InvocationResult.SUCCESS);
        this.setRequirements(newHashSet(_requirement));
        this.setComputeNodeId("someComputeNode");
      }});
    _batchMaster.addJobResults(run.getObjectId(), result);
  }
}
