/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Collections.emptyList;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
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
import com.opengamma.core.security.impl.SimpleSecurity;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.MapComputationTargetResolver;
import com.opengamma.engine.calcnode.InvocationResult;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValueResult;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.AggregatedExecutionLog;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewResultEntry;
import com.opengamma.engine.view.cycle.ViewCycleMetadata;
import com.opengamma.engine.view.impl.InMemoryViewComputationResultModel;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.money.Currency;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT_DB)
public class DbBatchWriterTest extends AbstractDbBatchTest {

  private DbBatchMaster _batchMaster;
  private DbBatchWriter _batchWriter;
  private ViewCycleMetadata _cycleMetadataStub;
  private ComputationTargetSpecification _compTargetSpec;
  private ValueRequirement _requirement;
  private ValueSpecification _specification;

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public DbBatchWriterTest(final String databaseType, final String databaseVersion) {
    super(databaseType, databaseVersion);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doSetUp() {
    MapComputationTargetResolver computationTargetResolver = new MapComputationTargetResolver();
    _batchMaster = new DbBatchMaster(getDbConnector(), computationTargetResolver);
    _batchWriter = new DbBatchWriter(_batchMaster.getDbConnector(), computationTargetResolver);

    final String calculationConfigName = "config_1";

    EquitySecurity aapl = new EquitySecurity("EXCH", "EXCH_CODE", "APPLE", Currency.USD);
    aapl.setUniqueId(UniqueId.of("Sec", "APPL"));
    ComputationTarget target = new ComputationTarget(ComputationTargetType.SECURITY, aapl);
    computationTargetResolver.addTarget(target);

    _compTargetSpec = target.getLeafSpecification();
    _requirement = new ValueRequirement("FAIR_VALUE", _compTargetSpec);
    _specification = new ValueSpecification("FAIR_VALUE", _compTargetSpec, ValueProperties.with(ValuePropertyNames.FUNCTION, "IDENTITY_FUNCTION").get());

    final Instant _valuationTime = Instant.parse("2011-12-14T14:20:17.143Z");

    _cycleMetadataStub = new ViewCycleMetadata() {

      @Override
      public UniqueId getViewCycleId() {
        return UniqueId.of("viewcycle", "viewcycle", "viewcycle");
      }

      @Override
      public Collection<String> getAllCalculationConfigurationNames() {
        return newArrayList(calculationConfigName);
      }

      @Override
      public Collection<com.opengamma.engine.ComputationTargetSpecification> getComputationTargets(final String calcConfName) {
        if (calcConfName.equals(calculationConfigName)) {
          return Arrays.asList(ComputationTargetSpecification.of(UniqueId.of("Primitive", "Value")), _compTargetSpec);
        } else {
          return emptyList();
        }
      }

      @Override
      public Map<ValueSpecification, Set<ValueRequirement>> getTerminalOutputs(final String calcConfName) {
        return ImmutableMap.<ValueSpecification, Set<ValueRequirement>>of(_specification, ImmutableSet.of(_requirement));
      }

      @Override
      public UniqueId getMarketDataSnapshotId() {
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
      public UniqueId getViewDefinitionId() {
        return UniqueId.of("viewdef", "viewdef", "viewdef");
      }

      @Override
      public String getName() {
        return "cycle_name";
      }

    };

  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void addValuesToNonexistentSnapshot() {
    _batchMaster.addValuesToMarketData(ObjectId.of("nonexistent", "nonexistent"), ImmutableSet.of(new MarketDataValue()));
  }

  @Test
  public void addValuesToIncompleteSnapshot() {
    final MarketData marketData = _batchWriter.createOrGetMarketDataInTransaction(_cycleMetadataStub.getMarketDataSnapshotId());

    _batchMaster.addValuesToMarketData(marketData.getObjectId(), Collections.<MarketDataValue>emptySet());

    List<MarketDataValue> marketDataValues = _batchMaster.getMarketDataValues(marketData.getObjectId(), PagingRequest.ALL).getFirst();
    assertNotNull(marketDataValues);
    assertEquals(0, marketDataValues.size());

    final Set<ComputationTargetSpecification> specs = Sets.newHashSet();
    specs.add(ComputationTargetSpecification.of(UniqueId.of("BUID", "EQ12345", null)));
    specs.add(ComputationTargetSpecification.of(UniqueId.of("BUID", "EQ12346", "1")));
    specs.add(ComputationTargetSpecification.of(UniqueId.of("BUID", "EQ12347", "2")));


    final Map<ComputationTargetSpecification, Long> compTargetSpecIdx = new HashMap<ComputationTargetSpecification, Long>();
    final Map<Long, ComputationTargetSpecification> reversedCompTargetSpecIdx = new HashMap<Long, ComputationTargetSpecification>();


    _batchMaster.getDbConnector().getTransactionTemplate().execute(new TransactionCallback<Void>() {
      @Override
      public Void doInTransaction(final TransactionStatus status) {
        // manually populationg db with computation targets
        for (final ComputationTargetSpecification spec : specs) {
          final HbComputationTargetSpecification hbComputationTargetSpecification = _batchWriter.getOrCreateComputationTargetInTransaction(spec);
          compTargetSpecIdx.put(spec, hbComputationTargetSpecification.getId());
          reversedCompTargetSpecIdx.put(hbComputationTargetSpecification.getId(), spec);
        }
        return null;
      }
    });

    Set<MarketDataValue> values = new HashSet<MarketDataValue>();
    for (final ComputationTargetSpecification spec : specs) {
      values.add(new MarketDataValue(spec, 123.45, "value_name"));
    }


    _batchMaster.addValuesToMarketData(marketData.getObjectId(), values);

    marketDataValues = _batchMaster.getMarketDataValues(marketData.getObjectId(), PagingRequest.ALL).getFirst();
    assertEquals(specs.size(), marketDataValues.size());

    final Map<Long, MarketDataValue> marketDataValuesMap = newHashMap();
    for (final MarketDataValue value : marketDataValues) {
      marketDataValuesMap.put(value.getComputationTargetSpecificationId(), value);
    }

    for (final ComputationTargetSpecification spec : specs) {
      final Long targetSpecificationId = compTargetSpecIdx.get(spec);
      final MarketDataValue marketDataValue = marketDataValuesMap.get(targetSpecificationId);
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
    values.add(new MarketDataValue(ComputationTargetSpecification.of(UniqueId.of("BUID", "EQ12345", null)), 123.46, "value_name"));
    values.add(new MarketDataValue(ComputationTargetSpecification.of(UniqueId.of("BUID", "EQ12347", "2")), 123.47, "value_name"));
    values.add(new MarketDataValue(ComputationTargetSpecification.of(UniqueId.of("BUID", "EQ12348")), 123.45, "value_name"));

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
    final UniqueId marketDataUid = _cycleMetadataStub.getMarketDataSnapshotId();

    _batchMaster.createMarketData(marketDataUid);
    _batchMaster.createMarketData(marketDataUid);
    _batchMaster.getDbConnector().getTransactionTemplate().execute(new TransactionCallback<Void>() {
      @Override
      public Void doInTransaction(final TransactionStatus status) {
        assertNotNull(_batchWriter.createOrGetMarketDataInTransaction(marketDataUid));
        return null;
      }
    });

  }

  @Test
  public void createThenGetRiskRun() {
    final UniqueId marketDataUid = _cycleMetadataStub.getMarketDataSnapshotId();

    _batchMaster.createMarketData(marketDataUid);

    final RiskRun run = _batchMaster.startRiskRun(_cycleMetadataStub, Maps.<String, String>newHashMap(), RunCreationMode.AUTO, SnapshotMode.PREPARED);

    final RiskRun run2 = _batchWriter.getRiskRun(run.getObjectId());

    assertNotNull(run2);
    assertNotNull(run2.getCreateInstant());
    assertNotNull(run2.getStartInstant());
    assertNull(run2.getEndInstant());
    assertNotNull(run2.getMarketData());
    assertNotNull(run2.getName());

    // Map<String, String> props = run2.getPropertiesMap();
    //assertEquals(10, props.size());
    //assertEquals("AD_HOC_RUN", props.getId("observationTime"));
    //assertEquals(ZonedDateTime.ofInstant(run2.getCreateInstant(), ZoneOffset.UTC).toString(), props.getId("valuationTime"));
    //assertEquals("test_view", props.getId("view"));
    //assertEquals(ZonedDateTime.ofInstant(run2.getCreateInstant(), ZoneOffset.UTC).getZone().toString(), props.getId("timeZone"));
    //assertEquals(ZonedDateTime.ofInstant(run2.getCreateInstant(), ZoneOffset.UTC).toLocalTime().toString(), props.getId("staticDataTime"));
    //assertEquals(ZonedDateTime.ofInstant(run2.getCreateInstant(), ZoneOffset.UTC).toLocalTime().toString(), props.getId("configDbTime"));
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
    final RiskRun run3 = _batchWriter.getRiskRun(run.getObjectId());
    assertEquals(run2.getId(), run3.getId());
  }


  @Test
  public void startAndEndBatch() {
    final UniqueId marketDataUid = _cycleMetadataStub.getMarketDataSnapshotId();

    _batchMaster.createMarketData(marketDataUid);

    final RiskRun run = _batchMaster.startRiskRun(_cycleMetadataStub, Maps.<String, String>newHashMap(), RunCreationMode.AUTO, SnapshotMode.PREPARED);

    RiskRun run1 = _batchWriter.getRiskRun(run.getObjectId());
    assertNotNull(run1);
    assertNotNull(run1.getStartInstant());
    assertNull(run1.getEndInstant());

    final RiskRun run2 = _batchWriter.getRiskRun(run.getObjectId());
    assertEquals(run1.getId(), run2.getId());

    _batchMaster.endRiskRun(run.getObjectId());

    run1 = _batchWriter.getRiskRun(run.getObjectId());

    assertNotNull(run1);
    assertNotNull(run1.getStartInstant());
    assertNotNull(run1.getEndInstant());
  }

  @Test
  public void startBatchTwice() {
    final UniqueId marketDataUid = _cycleMetadataStub.getMarketDataSnapshotId();

    _batchMaster.createMarketData(marketDataUid);

    final RiskRun run1 = _batchMaster.startRiskRun(_cycleMetadataStub, Maps.<String, String>newHashMap(), RunCreationMode.AUTO, SnapshotMode.PREPARED);

    final RiskRun run2 = _batchWriter.getRiskRun(run1.getObjectId());
    assertNotNull(run2.getCreateInstant());
    assertEquals(0, run2.getNumRestarts());

    final RiskRun run10 = _batchMaster.startRiskRun(_cycleMetadataStub, Maps.<String, String>newHashMap(), RunCreationMode.AUTO, SnapshotMode.PREPARED);
    final RiskRun run20 = _batchWriter.getRiskRun(run10.getObjectId());
    assertEquals(1, run20.getNumRestarts());

    final RiskRun run3 = _batchWriter.getRiskRun(run10.getObjectId());
    assertEquals(run20.getId(), run3.getId());
  }

  @Test
  public void getComputationTargetBySpec() {
    final UniqueId uniqueId = UniqueId.of("foo", "bar");

    _batchMaster.getDbConnector().getTransactionTemplate().execute(new TransactionCallback<Void>() {
      @Override
      public Void doInTransaction(final TransactionStatus status) {
        final HbComputationTargetSpecification portfolio = _batchWriter.getOrCreateComputationTargetInTransaction(
          new ComputationTargetSpecification(ComputationTargetType.PORTFOLIO_NODE, uniqueId));


        assertNotNull(portfolio);
        assertEquals(ComputationTargetType.PORTFOLIO_NODE, portfolio.getType());
        assertEquals(uniqueId, portfolio.getUniqueId());

        final HbComputationTargetSpecification position = _batchWriter.getComputationTargetIntransaction(
          new ComputationTargetSpecification(ComputationTargetType.POSITION, uniqueId));
        assertNull(position);

        final HbComputationTargetSpecification security = _batchWriter.getComputationTargetIntransaction(
          new ComputationTargetSpecification(ComputationTargetType.SECURITY, uniqueId));
        assertNull(security);

        final HbComputationTargetSpecification primitive = _batchWriter.getComputationTargetIntransaction(
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
      public Void doInTransaction(final TransactionStatus status) {
        final HbComputationTargetSpecification security = _batchWriter.getOrCreateComputationTargetInTransaction(
          new ComputationTargetSpecification(ComputationTargetType.SECURITY, uniqueId));

        assertEquals(ComputationTargetType.SECURITY, security.getType());

        final HbComputationTargetSpecification primitive = _batchWriter.getOrCreateComputationTargetInTransaction(
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
      public Void doInTransaction(final TransactionStatus status) {
        HbComputationTargetSpecification security = _batchWriter.getOrCreateComputationTargetInTransaction(
          new ComputationTargetSpecification(ComputationTargetType.SECURITY, uniqueId));
        assertEquals(ComputationTargetType.SECURITY, security.getType());

        final com.opengamma.engine.ComputationTarget target = new com.opengamma.engine.ComputationTarget(ComputationTargetType.SECURITY, mockSecurity);

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
      public Void doInTransaction(final TransactionStatus status) {
        // create
        final RiskValueSpecification valueSpecification1 = _batchWriter.getRiskValueSpecification(ValueProperties.parse("currency=USD"));
        assertNotNull(valueSpecification1);
        assertEquals("{\"properties\":[{\"values\":[\"USD\"],\"name\":\"currency\"}]}", valueSpecification1.getSyntheticForm());

        // getId
        final RiskValueSpecification valueSpecification2 = _batchWriter.getRiskValueSpecification(ValueProperties.parse("currency=USD"));
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
      public Void doInTransaction(final TransactionStatus status) {
        // create
        final FunctionUniqueId id1 = _batchWriter.getFunctionUniqueIdInTransaction("test_id");
        assertNotNull(id1);
        assertEquals("test_id", id1.getUniqueId());

        // getId
        final FunctionUniqueId id2 = _batchWriter.getFunctionUniqueIdInTransaction("test_id");
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
      public Void doInTransaction(final TransactionStatus status) {
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

    final UniqueId marketDataUid = _cycleMetadataStub.getMarketDataSnapshotId();
    _batchMaster.createMarketData(marketDataUid);
    final RiskRun run = _batchMaster.startRiskRun(_cycleMetadataStub, Maps.<String, String>newHashMap(), RunCreationMode.AUTO, SnapshotMode.PREPARED);

    assertNotNull(_batchWriter.getRiskRun(run.getObjectId()));
    _batchMaster.deleteRiskRun(run.getObjectId());
    assertNull(_batchWriter.getRiskRun(run.getObjectId()));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void addJobResultsToUnstartedBatch() {
    final ViewComputationResultModel result = new InMemoryViewComputationResultModel();
    _batchMaster.addJobResults(null, result);
  }

  @Test
  public void addJobResultsWithoutExistingComputeNodeId() {
    final UniqueId marketDataUid = _cycleMetadataStub.getMarketDataSnapshotId();

    _batchMaster.createMarketData(marketDataUid);

    final RiskRun run = _batchMaster.startRiskRun(_cycleMetadataStub, Maps.<String, String>newHashMap(), RunCreationMode.AUTO, SnapshotMode.PREPARED);

    final InMemoryViewComputationResultModel result = new InMemoryViewComputationResultModel();
    final ComputationTargetSpecification computationTargetSpec = new ComputationTargetSpecification(ComputationTargetType.SECURITY, UniqueId.of("Sec", "APPL"));
    final ValueProperties properties = ValueProperties.with(ValuePropertyNames.FUNCTION, "asd").get();
    final ValueSpecification valueSpec = new ValueSpecification("value", computationTargetSpec, properties);
    final ComputedValueResult cvr = new ComputedValueResult(valueSpec, 1000.0, AggregatedExecutionLog.EMPTY, null, null, InvocationResult.SUCCESS);
    //cvr.setRequirements(newHashSet(_requirement));
    result.addValue("config_1", cvr);
    
    // Result will be skipped but should not cause any exception to be thrown 
    _batchMaster.addJobResults(run.getObjectId(), result);
  }

  @Test
  public void addJobResults() {
    final UniqueId marketDataUid = _cycleMetadataStub.getMarketDataSnapshotId();
    _batchMaster.createMarketData(marketDataUid);
    final RiskRun run = _batchMaster.startRiskRun(_cycleMetadataStub, Maps.<String, String>newHashMap(), RunCreationMode.AUTO, SnapshotMode.PREPARED);
    final InMemoryViewComputationResultModel result = new InMemoryViewComputationResultModel();
    final ComputedValueResult cvr = new ComputedValueResult(_specification, 1000.0, AggregatedExecutionLog.EMPTY, "someComputeNode", null, InvocationResult.SUCCESS);
    //cvr.setRequirements(newHashSet(_requirement));
    result.addValue("config_1", cvr);
    _batchMaster.addJobResults(run.getObjectId(), result);
  }
  
  @Test
  public void truncateSmallValueToZero() {
    final UniqueId marketDataUid = _cycleMetadataStub.getMarketDataSnapshotId();
    _batchMaster.createMarketData(marketDataUid);
    final RiskRun run = _batchMaster.startRiskRun(_cycleMetadataStub, Maps.<String, String>newHashMap(), RunCreationMode.AUTO, SnapshotMode.PREPARED);
    final InMemoryViewComputationResultModel result = new InMemoryViewComputationResultModel();
    final ComputedValueResult cvr = new ComputedValueResult(_specification, 1e-323, AggregatedExecutionLog.EMPTY, "someComputeNode", null, InvocationResult.SUCCESS);
    //cvr.setRequirements(newHashSet(_requirement));
    result.addValue("config_1", cvr);
    _batchMaster.addJobResults(run.getObjectId(), result);
    
    List<ViewResultEntry> resultEntries = _batchMaster.getBatchValues(run.getObjectId(), PagingRequest.ALL).getFirst();
    ViewResultEntry resultEntry = Iterables.getOnlyElement(resultEntries);
    assertEquals(0d, resultEntry.getComputedValue().getValue());
  }
  
}
