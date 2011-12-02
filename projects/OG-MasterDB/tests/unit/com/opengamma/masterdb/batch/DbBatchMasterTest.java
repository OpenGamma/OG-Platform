/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.time.Instant;
import javax.time.calendar.LocalDate;
import javax.time.calendar.OffsetTime;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.google.common.collect.Sets;
import com.opengamma.DataNotFoundException;
import com.opengamma.core.position.impl.MockPositionSource;
import com.opengamma.core.position.impl.SimplePortfolio;
import com.opengamma.core.security.impl.SimpleSecurity;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.test.ViewProcessorTestEnvironment;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.InMemoryViewComputationResultModel;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.financial.batch.AdHocBatchResult;
import com.opengamma.financial.batch.BatchDocument;
import com.opengamma.financial.batch.BatchGetRequest;
import com.opengamma.financial.batch.BatchId;
import com.opengamma.financial.batch.BatchSearchRequest;
import com.opengamma.financial.batch.BatchSearchResult;
import com.opengamma.financial.batch.BatchStatus;
import com.opengamma.financial.batch.CommandLineBatchJob;
import com.opengamma.financial.batch.CommandLineBatchJobRun;
import com.opengamma.financial.batch.LiveDataValue;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.util.db.DbDateUtils;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TransactionalHibernateTest;

/**
 * Test DbBatchMaster.
 */
public class DbBatchMasterTest extends TransactionalHibernateTest {

  private DbBatchMaster _batchMaster;
  private CommandLineBatchJob _batchJob;
  private CommandLineBatchJobRun _batchJobRun;

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public DbBatchMasterTest(String databaseType, final String databaseVersion) {
    super(databaseType, databaseVersion);
  }

  @Override
  public Class<?>[] getHibernateMappingClasses() {
    return DbBatchMaster.getHibernateMappingClasses();
  }

  @BeforeMethod
  public void setUp() throws Exception {
    super.setUp();
    _batchMaster = new DbBatchMaster(getDbConnector());

    _batchJob = new CommandLineBatchJob();
    _batchJob.getParameters().initializeDefaults(_batchJob);
    _batchJob.setBatchMaster(_batchMaster);
    _batchJob.getParameters().setViewName("test_view");

    _batchJobRun = new CommandLineBatchJobRun(_batchJob);
    ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();

    UniqueId portfolioId = UniqueId.of("foo", "bar", "1");

    MockPositionSource positionSource = new MockPositionSource();
    positionSource.addPortfolio(new SimplePortfolio(portfolioId, "test_portfolio"));
    env.setPositionSource(positionSource);

    ViewDefinition viewDefinition = new ViewDefinition("mock_view", portfolioId.toLatest(), "ViewTestUser");
    env.setViewDefinition(viewDefinition);

    env.init();

    CompiledViewDefinition compiledViewDefinition = env.compileViewDefinition(Instant.now(), VersionCorrection.LATEST);
    _batchJobRun.setCompiledViewDefinition(compiledViewDefinition);

    _batchJobRun.setViewProcessor(env.getViewProcessor());

    ConfigDocument<ViewDefinition> doc = new ConfigDocument<ViewDefinition>(ViewDefinition.class);
    doc.setUniqueId(UniqueId.of("Test", "1", "1"));
    doc.setName("Name");
    doc.setVersionFromInstant(Instant.EPOCH);
    doc.setVersionFromInstant(Instant.EPOCH);
    doc.setValue(env.getViewDefinition());
    _batchJobRun.setViewDefinitionConfig(doc);

    _batchJob.addRun(_batchJobRun);
  }

  //-------------------------------------------------------------------------
  @Test
  public void getVersion() {
    // create
    OpenGammaVersion version1 = _batchMaster.getOpenGammaVersion(_batchJobRun);
    assertNotNull(version1);
    assertEquals(_batchJob.getOpenGammaVersion(), version1.getVersion());

    // get
    OpenGammaVersion version2 = _batchMaster.getOpenGammaVersion(_batchJobRun);
    assertEquals(version1, version2);
  }

  @Test
  public void getObservationTime() {
    // create
    ObservationTime time1 = _batchMaster.getObservationTime(_batchJobRun);
    assertNotNull(time1);
    assertEquals(_batchJobRun.getObservationTime(), time1.getLabel());

    // get
    ObservationTime time2 = _batchMaster.getObservationTime(_batchJobRun);
    assertEquals(time1, time2);
  }

  @Test
  public void getObservationDateTime() {
    // create
    ObservationDateTime datetime1 = _batchMaster.getObservationDateTime(_batchJobRun);
    assertNotNull(datetime1);
    assertEquals(DbDateUtils.toSqlDate(_batchJobRun.getObservationDate()), datetime1.getDate());
    assertEquals(_batchJobRun.getObservationTime(), datetime1.getObservationTime().getLabel());

    // get
    ObservationDateTime datetime2 = _batchMaster.getObservationDateTime(_batchJobRun);
    assertEquals(datetime1, datetime2);
  }

  @Test
  public void getLocalComputeHost() throws UnknownHostException {
    // create
    ComputeHost host1 = _batchMaster.getLocalComputeHost();
    assertNotNull(host1);
    assertEquals(InetAddress.getLocalHost().getHostName(), host1.getHostName());

    // get
    ComputeHost host2 = _batchMaster.getLocalComputeHost();
    assertEquals(host1, host2);
  }

  @Test
  public void getLocalComputeNode() throws UnknownHostException {
    // create
    ComputeNode node1 = _batchMaster.getLocalComputeNode();
    assertNotNull(node1);
    assertEquals(_batchMaster.getLocalComputeHost(), node1.getComputeHost());
    assertEquals(InetAddress.getLocalHost().getHostName(), node1.getNodeName());

    // get
    ComputeNode node2 = _batchMaster.getLocalComputeNode();
    assertEquals(node1, node2);
  }

  @Test
  public void getNonExistentRiskRunFromDb() {
    RiskRun run = _batchMaster.getRiskRunFromDb(_batchJobRun);
    assertNull(run);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void tryToGetNonExistentLiveDataSnapshot() {
    _batchMaster.getLiveDataSnapshot(_batchJobRun);
  }

  @Test
  public void getLiveDataField() {
    // create
    LiveDataField field1 = _batchMaster.getLiveDataField("test_field");
    assertNotNull(field1);
    assertEquals("test_field", field1.getName());

    // get
    LiveDataField field2 = _batchMaster.getLiveDataField("test_field");
    assertEquals(field1, field2);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void addValuesToNonexistentSnapshot() {
    _batchMaster.addValuesToSnapshot(_batchJobRun.getSnapshotId(), Collections.<LiveDataValue>emptySet());
  }

  @Test
  public void addValuesToIncompleteSnapshot() {
    _batchMaster.createLiveDataSnapshot(_batchJobRun.getSnapshotId());

    _batchMaster.addValuesToSnapshot(_batchJobRun.getSnapshotId(), Collections.<LiveDataValue>emptySet());

    LiveDataSnapshot snapshot = _batchMaster.getLiveDataSnapshot(_batchJobRun);
    assertNotNull(snapshot);
    assertEquals(0, snapshot.getSnapshotEntries().size());

    Set<ComputationTargetSpecification> specs = Sets.newHashSet();
    specs.add(new ComputationTargetSpecification(UniqueId.of("BUID", "EQ12345", null)));
    specs.add(new ComputationTargetSpecification(UniqueId.of("BUID", "EQ12346", "1")));
    specs.add(new ComputationTargetSpecification(UniqueId.of("BUID", "EQ12347", "2")));

    Set<LiveDataValue> values = new HashSet<LiveDataValue>();
    for (ComputationTargetSpecification spec : specs) {
      values.add(new LiveDataValue(spec, "field_name", 123.45));
    }

    _batchMaster.addValuesToSnapshot(_batchJobRun.getSnapshotId(), values);

    snapshot = _batchMaster.getLiveDataSnapshot(_batchJobRun);
    assertEquals(specs.size(), snapshot.getSnapshotEntries().size());
    for (ComputationTargetSpecification spec : specs) {
      LiveDataSnapshotEntry entry = snapshot.getEntry(spec, "field_name");
      assertNotNull(entry);
      assertEquals(snapshot, entry.getSnapshot());
      assertEquals(spec, entry.getComputationTarget().toComputationTargetSpec());
      assertEquals("field_name", entry.getField().getName());
      assertEquals(123.45, entry.getValue(), 0.000001);
    }

    // should not add anything extra
    _batchMaster.addValuesToSnapshot(_batchJobRun.getSnapshotId(), values);
    snapshot = _batchMaster.getLiveDataSnapshot(_batchJobRun);
    assertEquals(3, snapshot.getSnapshotEntries().size());

    // should update 2, add 1
    values = new HashSet<LiveDataValue>();
    values.add(new LiveDataValue(new ComputationTargetSpecification(UniqueId.of("BUID", "EQ12345", null)), "field_name", 123.46));
    values.add(new LiveDataValue(new ComputationTargetSpecification(UniqueId.of("BUID", "EQ12347", "2")), "field_name", 123.47));
    values.add(new LiveDataValue(new ComputationTargetSpecification(ExternalId.of("BUID", "EQ12348")), "field_name", 123.45));

    _batchMaster.addValuesToSnapshot(_batchJobRun.getSnapshotId(), values);
    snapshot = _batchMaster.getLiveDataSnapshot(_batchJobRun);
    assertEquals(4, snapshot.getSnapshotEntries().size());
  }

  @Test
  public void fixLiveDataSnapshotTime() {
    _batchMaster.createLiveDataSnapshot(_batchJobRun.getSnapshotId());
    _batchMaster.fixLiveDataSnapshotTime(_batchJobRun.getSnapshotId(),
        OffsetTime.now());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void tryToFixNonexistentLiveDataSnapshotTime() {
    _batchMaster.fixLiveDataSnapshotTime(_batchJobRun.getSnapshotId(),
        OffsetTime.now());
  }

  @Test
  public void createLiveDataSnapshotMultipleTimes() {
    _batchMaster.createLiveDataSnapshot(_batchJobRun.getSnapshotId());

    _batchMaster.createLiveDataSnapshot(_batchJobRun.getSnapshotId());

    assertNotNull(_batchMaster.getLiveDataSnapshot(_batchJobRun));
  }

  @Test
  public void createThenGetRiskRun() {
    assertNull(_batchJobRun.getOriginalCreationTime());

    _batchMaster.createLiveDataSnapshot(_batchJobRun.getSnapshotId());
    RiskRun run = _batchMaster.createRiskRun(_batchJobRun);

    assertNotNull(run);
    assertNotNull(run.getCreateInstant());
    assertNotNull(run.getStartInstant());
    assertNull(run.getEndInstant());
    assertTrue(run.getCalculationConfigurations().isEmpty());
    assertNotNull(run.getLiveDataSnapshot());
    assertEquals(_batchMaster.getLocalComputeHost(), run.getMasterProcessHost());
    assertEquals(_batchMaster.getOpenGammaVersion(_batchJobRun), run.getOpenGammaVersion());

    Map<String, String> props = run.getPropertiesMap();
    assertEquals(10, props.size());
    assertEquals("AD_HOC_RUN", props.get("observationTime"));
    assertEquals(_batchJob.getCreationTime().toLocalTime().toString(), props.get("valuationTime"));
    assertEquals("test_view", props.get("view"));
    assertEquals(_batchJob.getCreationTime().getZone().toString(), props.get("timeZone"));
    assertEquals(_batchJob.getCreationTime().toLocalTime().toString(), props.get("staticDataTime"));
    assertEquals(_batchJob.getCreationTime().toLocalTime().toString(), props.get("configDbTime"));
    assertEquals("Manual run started on "
        + _batchJob.getCreationTime().toString()
        + " by "
        + System.getProperty("user.name"),
        props.get("reason"));
    assertEquals(_batchJob.getCreationTime().toInstant().toString(), props.get("valuationInstant"));
    assertEquals(_batchJob.getCreationTime().toInstant().toString(), props.get("configDbInstant"));
    assertEquals(_batchJob.getCreationTime().toInstant().toString(), props.get("staticDataInstant"));
    assertEquals(_batchJob.getCreationTime().toInstant(), _batchJobRun.getOriginalCreationTime());

    // get
    RiskRun run2 = _batchMaster.getRiskRunFromDb(_batchJobRun);
    assertEquals(run, run2);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void tryToStartBatchWithoutCreatingSnapshot() {
    _batchMaster.startBatch(_batchJobRun);
  }

  @Test
  public void startAndEndBatch() {
    _batchMaster.createLiveDataSnapshot(_batchJobRun.getSnapshotId());
    _batchMaster.startBatch(_batchJobRun);

    RiskRun run1 = _batchMaster.getRiskRunFromDb(_batchJobRun);
    assertNotNull(run1);
    assertNotNull(run1.getStartInstant());
    assertNull(run1.getEndInstant());

    RiskRun run2 = _batchMaster.getRiskRunFromHandle(_batchJobRun);
    assertEquals(run1, run2);

    _batchMaster.endBatch(_batchJobRun);

    run1 = _batchMaster.getRiskRunFromDb(_batchJobRun);
    run2 = _batchMaster.getRiskRunFromHandle(_batchJobRun);

    assertNotNull(run1);
    assertNotNull(run1.getStartInstant());
    assertNotNull(run1.getEndInstant());

    assertEquals(run1, run2);
  }

  @Test
  public void startBatchTwice() {
    assertNull(_batchJobRun.getOriginalCreationTime());

    _batchMaster.createLiveDataSnapshot(_batchJobRun.getSnapshotId());
    _batchMaster.startBatch(_batchJobRun);

    assertEquals(_batchJob.getCreationTime().toInstant(), _batchJobRun.getOriginalCreationTime());

    RiskRun run = _batchMaster.getRiskRunFromDb(_batchJobRun);
    assertEquals(0, run.getNumRestarts());

    _batchMaster.startBatch(_batchJobRun);
    assertEquals(1, run.getNumRestarts());
    assertEquals(_batchJob.getCreationTime().toInstant(), _batchJobRun.getOriginalCreationTime());
  }

  @Test
  public void getComputationTargetBySpec() {
    UniqueId uniqueId = UniqueId.of("foo", "bar");

    ComputationTarget portfolio = _batchMaster.getComputationTarget(
        new ComputationTargetSpecification(ComputationTargetType.PORTFOLIO_NODE, uniqueId));
    assertNotNull(portfolio);
    assertEquals(ComputationTargetType.PORTFOLIO_NODE.ordinal(), portfolio.getComputationTargetType());
    assertEquals(uniqueId.getScheme(), portfolio.getIdScheme());
    assertEquals(uniqueId.getValue(), portfolio.getIdValue());
    assertEquals(uniqueId.getVersion(), portfolio.getIdVersion());

    ComputationTarget position = _batchMaster.getComputationTarget(
        new ComputationTargetSpecification(ComputationTargetType.POSITION, uniqueId));
    assertEquals(ComputationTargetType.POSITION.ordinal(), position.getComputationTargetType());

    ComputationTarget security = _batchMaster.getComputationTarget(
        new ComputationTargetSpecification(ComputationTargetType.SECURITY, uniqueId));
    assertEquals(ComputationTargetType.SECURITY.ordinal(), security.getComputationTargetType());

    ComputationTarget primitive = _batchMaster.getComputationTarget(
        new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, uniqueId));
    assertEquals(ComputationTargetType.PRIMITIVE.ordinal(), primitive.getComputationTargetType());
  }

  @Test
  public void getComputationTarget() {
    UniqueId uniqueId = UniqueId.of("foo", "bar", "1");

    SimpleSecurity mockSecurity = new SimpleSecurity("option");
    mockSecurity.setUniqueId(uniqueId);
    mockSecurity.setName("myOption");

    ComputationTarget security = _batchMaster.getComputationTarget(
        new com.opengamma.engine.ComputationTarget(mockSecurity));
    assertEquals(ComputationTargetType.SECURITY.ordinal(), security.getComputationTargetType());
    assertEquals("myOption", security.getName());

    ComputationTarget primitive = _batchMaster.getComputationTarget(
        new com.opengamma.engine.ComputationTarget(uniqueId));
    assertEquals(ComputationTargetType.PRIMITIVE.ordinal(), primitive.getComputationTargetType());
    assertNull(primitive.getName());
  }

  @Test
  public void updateComputationTarget() {
    UniqueId uniqueId = UniqueId.of("foo", "bar");

    SimpleSecurity mockSecurity = new SimpleSecurity("option");
    mockSecurity.setUniqueId(uniqueId);
    mockSecurity.setName("myOption");

    ComputationTarget security = _batchMaster.getComputationTarget(
        new ComputationTargetSpecification(ComputationTargetType.SECURITY, uniqueId));
    assertEquals(ComputationTargetType.SECURITY.ordinal(), security.getComputationTargetType());
    assertNull(security.getName());

    security = _batchMaster.getComputationTarget(
        new com.opengamma.engine.ComputationTarget(mockSecurity));
    assertEquals(ComputationTargetType.SECURITY.ordinal(), security.getComputationTargetType());
    assertEquals("myOption", security.getName());
  }

  @Test
  public void getValueName() {
    // create
    RiskValueName valueName1 = _batchMaster.getRiskValueName("test_name");
    assertNotNull(valueName1);
    assertEquals("test_name", valueName1.getName());

    // get
    RiskValueName valueName2 = _batchMaster.getRiskValueName("test_name");
    assertEquals(valueName1, valueName2);
  }

  @Test
  public void getValueConstraint() {
    // create
    RiskValueRequirement valueRequirement1 = _batchMaster.getRiskValueRequirement(ValueProperties.parse("currency=USD"));
    assertNotNull(valueRequirement1);
    assertEquals("{\"properties\":[{\"values\":[\"USD\"],\"name\":\"currency\"}]}", valueRequirement1.getSyntheticForm());

    // get
    RiskValueRequirement valueRequirement2 = _batchMaster.getRiskValueRequirement(ValueProperties.parse("currency=USD"));
    assertEquals(valueRequirement1, valueRequirement2);
    assertEquals(valueRequirement1.getId(), valueRequirement2.getId());
  }

  @Test
  public void getFunctionUniqueId() {
    // create
    FunctionUniqueId id1 = _batchMaster.getFunctionUniqueId("test_id");
    assertNotNull(id1);
    assertEquals("test_id", id1.getUniqueId());

    // get
    FunctionUniqueId id2 = _batchMaster.getFunctionUniqueId("test_id");
    assertEquals(id1, id2);
  }

  @Test
  public void searchAllBatches() {
    _batchMaster.createLiveDataSnapshot(_batchJobRun.getSnapshotId());
    _batchMaster.startBatch(_batchJobRun);

    BatchSearchRequest request = new BatchSearchRequest();

    BatchSearchResult result = _batchMaster.search(request);
    assertNotNull(result);

    assertEquals(1, result.getDocuments().size());
    BatchDocument item = result.getDocuments().get(0);
    assertNotNull(item.getUniqueId());
    assertEquals(item.getObservationDate(), _batchJobRun.getObservationDate());
    assertEquals(item.getObservationTime(), _batchJobRun.getObservationTime());
    assertEquals(BatchStatus.RUNNING, item.getStatus());

    _batchMaster.endBatch(_batchJobRun);
    result = _batchMaster.search(request);
    assertEquals(1, result.getDocuments().size());
    item = result.getDocuments().get(0);
    assertNotNull(item.getUniqueId());
    assertEquals(item.getObservationDate(), _batchJobRun.getObservationDate());
    assertEquals(item.getObservationTime(), _batchJobRun.getObservationTime());
    assertEquals(BatchStatus.COMPLETE, item.getStatus());
  }

  @Test
  public void searchOneBatch() {
    _batchMaster.createLiveDataSnapshot(_batchJobRun.getSnapshotId());
    _batchMaster.startBatch(_batchJobRun);

    BatchSearchRequest request = new BatchSearchRequest();
    request.setObservationDate(_batchJobRun.getObservationDate());
    request.setObservationTime(_batchJobRun.getObservationTime());

    BatchSearchResult result = _batchMaster.search(request);
    assertNotNull(result);

    assertEquals(1, result.getDocuments().size());
    BatchDocument item = result.getDocuments().get(0);
    assertNotNull(item.getUniqueId());
    assertEquals(item.getObservationDate(), _batchJobRun.getObservationDate());
    assertEquals(item.getObservationTime(), _batchJobRun.getObservationTime());
    assertEquals(BatchStatus.RUNNING, item.getStatus());
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void get_dataNonexistentBatch() {
    BatchGetRequest request = new BatchGetRequest();
    request.setUniqueId(UniqueId.of("DbBat", "2000-05-05-" + _batchJobRun.getObservationTime()));

    _batchMaster.get(request);
  }

  @Test
  public void get_dataExistingBatch() {
    _batchMaster.createLiveDataSnapshot(_batchJobRun.getSnapshotId());
    _batchMaster.startBatch(_batchJobRun);

    BatchGetRequest request = new BatchGetRequest();
    request.setUniqueId(UniqueId.of("DbBat", _batchJobRun.getObservationDate() + "-" + _batchJobRun.getObservationTime()));

    commit();
    startNewTransaction();

    BatchDocument result = _batchMaster.get(request);
    assertNotNull(result);
    assertTrue(result.getData().isEmpty());
  }

  @Test
  public void get_errorsExistingBatch() {
    _batchMaster.createLiveDataSnapshot(_batchJobRun.getSnapshotId());
    _batchMaster.startBatch(_batchJobRun);

    BatchGetRequest request = new BatchGetRequest();
    request.setUniqueId(UniqueId.of("DbBat", _batchJobRun.getObservationDate() + "-" + _batchJobRun.getObservationTime()));
    request.setDataPagingRequest(PagingRequest.NONE);
    request.setErrorPagingRequest(PagingRequest.ALL);

    commit();
    startNewTransaction();

    BatchDocument result = _batchMaster.get(request);
    assertNotNull(result);
    assertTrue(result.getErrors().isEmpty());
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void deleteNonExisting() {
    assertNull(_batchMaster.getRiskRunFromDb(_batchJobRun));

    _batchMaster.delete(UniqueId.of("DbBat", _batchJobRun.getObservationDate() + "-" + _batchJobRun.getObservationTime()));
  }

  @Test
  public void delete() {
    assertNull(_batchMaster.getRiskRunFromDb(_batchJobRun));

    _batchMaster.createLiveDataSnapshot(_batchJobRun.getSnapshotId());
    _batchMaster.startBatch(_batchJobRun);

    assertNotNull(_batchMaster.getRiskRunFromDb(_batchJobRun));

    _batchMaster.delete(UniqueId.of("DbBat", _batchJobRun.getObservationDate() + "-" + _batchJobRun.getObservationTime()));
    assertNull(_batchMaster.getRiskRunFromDb(_batchJobRun));
  }

  @Test
  public void writeAdHocBatchResult() {
    commit(); // don't want this test to be transactional

    Instant now = Instant.now();

    BatchId batchId = new BatchId(LocalDate.of(2005, 1, 2), "LDN_CLOSE");
    InMemoryViewComputationResultModel result = new InMemoryViewComputationResultModel();
    result.setCalculationTime(now);
    result.setValuationTime(now);
    result.setViewProcessId(UniqueId.of("Test", "ViewProcess"));

    ComputationTargetSpecification spec = new ComputationTargetSpecification(UniqueId.of("BUID", "EQ12345", null));
    result.addMarketData(new ComputedValue(
        new ValueSpecification(
            "MarketValue",
            spec,
            ValueProperties.with(ValuePropertyNames.FUNCTION, "marketdatafunction").get()),
        1.12));
    result.addValue("MyCalcConf",
        new ComputedValue(
            new ValueSpecification(
                "PresentValue",
                spec,
                ValueProperties.with(ValuePropertyNames.FUNCTION, "pvfunction").get()),
            1.12));


    result.addRequirement(
        new ValueRequirement(
            "PresentValue",
            spec,
            ValueProperties.with(ValuePropertyNames.FUNCTION, "pvfunction").get()),
        new ValueSpecification(
            "PresentValue",
            spec,
            ValueProperties.with(ValuePropertyNames.FUNCTION, "pvfunction").get()));

    result.addRequirement(
        new ValueRequirement(
            "MarketValue",
            spec,
            ValueProperties.with(ValuePropertyNames.FUNCTION, "marketdatafunction").get()),
        new ValueSpecification(
            "MarketValue",
            spec,
            ValueProperties.with(ValuePropertyNames.FUNCTION, "marketdatafunction").get()));

    AdHocBatchResult adHocBatchResult = new AdHocBatchResult(batchId, result);
    _batchMaster.write(adHocBatchResult);
  }

}
