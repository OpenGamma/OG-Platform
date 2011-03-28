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
import com.opengamma.core.security.test.MockSecurity;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.InMemoryViewComputationResultModel;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewImpl;
import com.opengamma.financial.batch.AdHocBatchResult;
import com.opengamma.financial.batch.BatchDataSearchRequest;
import com.opengamma.financial.batch.BatchDataSearchResult;
import com.opengamma.financial.batch.BatchErrorSearchRequest;
import com.opengamma.financial.batch.BatchErrorSearchResult;
import com.opengamma.financial.batch.BatchId;
import com.opengamma.financial.batch.BatchSearchRequest;
import com.opengamma.financial.batch.BatchSearchResult;
import com.opengamma.financial.batch.BatchSearchResultItem;
import com.opengamma.financial.batch.BatchStatus;
import com.opengamma.financial.batch.CommandLineBatchJob;
import com.opengamma.financial.batch.CommandLineBatchJobRun;
import com.opengamma.financial.batch.LiveDataValue;
import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.util.db.DbDateUtils;
import com.opengamma.util.test.DBTest;
import com.opengamma.util.test.TransactionalHibernateTest;

/**
 * Test BatchDbManagerImpl.
 */
public class BatchDbManagerImplTest extends TransactionalHibernateTest {

  private BatchDbManagerImpl _dbManager;
  private CommandLineBatchJob _batchJob;
  private CommandLineBatchJobRun _batchJobRun;

  @Factory(dataProvider = "databasesMoreVersions", dataProviderClass = DBTest.class)
  public BatchDbManagerImplTest(String databaseType, final String databaseVersion) {
    super(databaseType, databaseVersion);
  }

  @Override
  public Class<?>[] getHibernateMappingClasses() {
    return BatchDbManagerImpl.getHibernateMappingClasses();
  }

  @BeforeMethod
  public void setUp() throws Exception {
    super.setUp();
    _dbManager = new BatchDbManagerImpl();
    _dbManager.setDbSource(getDbSource());
    
    _batchJob = new CommandLineBatchJob();
    _batchJob.getParameters().initializeDefaults(_batchJob);
    _batchJob.setBatchDbManager(_dbManager);
    _batchJob.getParameters().setViewName("test_view");
    
    _batchJobRun = new CommandLineBatchJobRun(_batchJob);
    ViewImpl view = ViewTestUtils.getMockView();
    _batchJobRun.setView(view);
    
    ConfigDocument<ViewDefinition> doc = new ConfigDocument<ViewDefinition>();
    doc.setUniqueId(UniqueIdentifier.of("Test", "1", "1"));
    doc.setName("Name");
    doc.setVersionFromInstant(Instant.EPOCH);
    doc.setVersionFromInstant(Instant.EPOCH);
    doc.setValue(view.getDefinition());
    _batchJobRun.setViewDefinitionConfig(doc);
    
    _batchJob.addRun(_batchJobRun);
  }

  //-------------------------------------------------------------------------
  @Test
  public void getVersion() {
    // create
    OpenGammaVersion version1 = _dbManager.getOpenGammaVersion(_batchJobRun);
    assertNotNull(version1);
    assertEquals(_batchJob.getOpenGammaVersion(), version1.getVersion());
    
    // get
    OpenGammaVersion version2 = _dbManager.getOpenGammaVersion(_batchJobRun);
    assertEquals(version1, version2);
  }

  @Test
  public void getObservationTime() {
    // create
    ObservationTime time1 = _dbManager.getObservationTime(_batchJobRun);
    assertNotNull(time1);
    assertEquals(_batchJobRun.getObservationTime(), time1.getLabel());
    
    // get
    ObservationTime time2 = _dbManager.getObservationTime(_batchJobRun);
    assertEquals(time1, time2);
  }

  @Test
  public void getObservationDateTime() {
    // create
    ObservationDateTime datetime1 = _dbManager.getObservationDateTime(_batchJobRun);
    assertNotNull(datetime1);
    assertEquals(DbDateUtils.toSqlDate(_batchJobRun.getObservationDate()), datetime1.getDate());
    assertEquals(_batchJobRun.getObservationTime(), datetime1.getObservationTime().getLabel());
    
    // get
    ObservationDateTime datetime2 = _dbManager.getObservationDateTime(_batchJobRun);
    assertEquals(datetime1, datetime2);
  }

  @Test
  public void getLocalComputeHost() throws UnknownHostException {
    // create
    ComputeHost host1 = _dbManager.getLocalComputeHost();
    assertNotNull(host1);
    assertEquals(InetAddress.getLocalHost().getHostName(), host1.getHostName());
    
    // get
    ComputeHost host2 = _dbManager.getLocalComputeHost();
    assertEquals(host1, host2);
  }

  @Test
  public void getLocalComputeNode() throws UnknownHostException {
    // create
    ComputeNode node1 = _dbManager.getLocalComputeNode();
    assertNotNull(node1);
    assertEquals(_dbManager.getLocalComputeHost(), node1.getComputeHost());
    assertEquals(InetAddress.getLocalHost().getHostName(), node1.getNodeName());
    
    // get
    ComputeNode node2 = _dbManager.getLocalComputeNode();
    assertEquals(node1, node2);
  }

  @Test
  public void getNonExistentRiskRunFromDb() {
    RiskRun run = _dbManager.getRiskRunFromDb(_batchJobRun);
    assertNull(run);
  }
  
  @Test(expectedExceptions=IllegalArgumentException.class)
  public void tryToGetNonExistentLiveDataSnapshot() {
    _dbManager.getLiveDataSnapshot(_batchJobRun);
  }

  @Test 
  public void getLiveDataField() {
    // create
    LiveDataField field1 = _dbManager.getLiveDataField("test_field");
    assertNotNull(field1);
    assertEquals("test_field", field1.getName());
    
    // get
    LiveDataField field2 = _dbManager.getLiveDataField("test_field");
    assertEquals(field1, field2);
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void addValuesToNonexistentSnapshot() {
    _dbManager.addValuesToSnapshot(_batchJobRun.getSnapshotId(), Collections.<LiveDataValue>emptySet());
  }

  @Test
  public void addValuesToIncompleteSnapshot() {
    _dbManager.createLiveDataSnapshot(_batchJobRun.getSnapshotId());
    
    _dbManager.addValuesToSnapshot(_batchJobRun.getSnapshotId(), Collections.<LiveDataValue>emptySet());
    
    LiveDataSnapshot snapshot = _dbManager.getLiveDataSnapshot(_batchJobRun);
    assertNotNull(snapshot);
    assertEquals(0, snapshot.getSnapshotEntries().size());
    
    Set<ComputationTargetSpecification> specs = Sets.newHashSet();
    specs.add(new ComputationTargetSpecification(UniqueIdentifier.of("BUID", "EQ12345", null)));
    specs.add(new ComputationTargetSpecification(UniqueIdentifier.of("BUID", "EQ12346", "1")));
    specs.add(new ComputationTargetSpecification(UniqueIdentifier.of("BUID", "EQ12347", "2")));
    
    Set<LiveDataValue> values = new HashSet<LiveDataValue>();
    for (ComputationTargetSpecification spec : specs) {
      values.add(new LiveDataValue(spec, "field_name", 123.45));
    }
    
    _dbManager.addValuesToSnapshot(_batchJobRun.getSnapshotId(), values);
    
    snapshot = _dbManager.getLiveDataSnapshot(_batchJobRun);
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
    _dbManager.addValuesToSnapshot(_batchJobRun.getSnapshotId(), values);
    snapshot = _dbManager.getLiveDataSnapshot(_batchJobRun);
    assertEquals(3, snapshot.getSnapshotEntries().size());
    
    // should update 2, add 1
    values = new HashSet<LiveDataValue>();
    values.add(new LiveDataValue(new ComputationTargetSpecification(UniqueIdentifier.of("BUID", "EQ12345", null)), "field_name", 123.46));
    values.add(new LiveDataValue(new ComputationTargetSpecification(UniqueIdentifier.of("BUID", "EQ12347", "2")), "field_name", 123.47));
    values.add(new LiveDataValue(new ComputationTargetSpecification(Identifier.of("BUID", "EQ12348")), "field_name", 123.45));
    
    _dbManager.addValuesToSnapshot(_batchJobRun.getSnapshotId(), values);
    snapshot = _dbManager.getLiveDataSnapshot(_batchJobRun);
    assertEquals(4, snapshot.getSnapshotEntries().size());
  }

  @Test
  public void fixLiveDataSnapshotTime() {
    _dbManager.createLiveDataSnapshot(_batchJobRun.getSnapshotId());
    _dbManager.fixLiveDataSnapshotTime(_batchJobRun.getSnapshotId(),
        OffsetTime.now());
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void tryToFixNonexistentLiveDataSnapshotTime() {
    _dbManager.fixLiveDataSnapshotTime(_batchJobRun.getSnapshotId(),
        OffsetTime.now());
  }

  @Test
  public void createLiveDataSnapshotMultipleTimes() {
    _dbManager.createLiveDataSnapshot(_batchJobRun.getSnapshotId());
    
    _dbManager.createLiveDataSnapshot(_batchJobRun.getSnapshotId());
    
    assertNotNull(_dbManager.getLiveDataSnapshot(_batchJobRun));
  }

  @Test
  public void createThenGetRiskRun() {
    assertNull(_batchJobRun.getOriginalCreationTime());
    
    _dbManager.createLiveDataSnapshot(_batchJobRun.getSnapshotId());
    RiskRun run = _dbManager.createRiskRun(_batchJobRun);
    
    assertNotNull(run);
    assertNotNull(run.getCreateInstant());
    assertNotNull(run.getStartInstant());
    assertNull(run.getEndInstant());
    assertTrue(run.getCalculationConfigurations().isEmpty());
    assertNotNull(run.getLiveDataSnapshot());
    assertEquals(_dbManager.getLocalComputeHost(), run.getMasterProcessHost());
    assertEquals(_dbManager.getOpenGammaVersion(_batchJobRun), run.getOpenGammaVersion());
    
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
    RiskRun run2 = _dbManager.getRiskRunFromDb(_batchJobRun);
    assertEquals(run, run2);
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void tryToStartBatchWithoutCreatingSnapshot() {
    _dbManager.startBatch(_batchJobRun);
  }

  @Test
  public void startAndEndBatch() {
    _dbManager.createLiveDataSnapshot(_batchJobRun.getSnapshotId());
    _dbManager.startBatch(_batchJobRun);
    
    RiskRun run1 = _dbManager.getRiskRunFromDb(_batchJobRun);
    assertNotNull(run1);
    assertNotNull(run1.getStartInstant());
    assertNull(run1.getEndInstant());
    
    RiskRun run2 = _dbManager.getRiskRunFromHandle(_batchJobRun);
    assertEquals(run1, run2);
    
    _dbManager.endBatch(_batchJobRun);
    
    run1 = _dbManager.getRiskRunFromDb(_batchJobRun);
    run2 = _dbManager.getRiskRunFromHandle(_batchJobRun);
    
    assertNotNull(run1);
    assertNotNull(run1.getStartInstant());
    assertNotNull(run1.getEndInstant());
    
    assertEquals(run1, run2);
  }

  @Test
  public void startBatchTwice() {
    assertNull(_batchJobRun.getOriginalCreationTime());
    
    _dbManager.createLiveDataSnapshot(_batchJobRun.getSnapshotId());
    _dbManager.startBatch(_batchJobRun);
    
    assertEquals(_batchJob.getCreationTime().toInstant(), _batchJobRun.getOriginalCreationTime());
    
    RiskRun run = _dbManager.getRiskRunFromDb(_batchJobRun);
    assertEquals(0, run.getNumRestarts());
    
    _dbManager.startBatch(_batchJobRun);
    assertEquals(1, run.getNumRestarts());
    assertEquals(_batchJob.getCreationTime().toInstant(), _batchJobRun.getOriginalCreationTime());
  }

  @Test
  public void getComputationTargetBySpec() {
    UniqueIdentifier uid = UniqueIdentifier.of("foo", "bar");
    
    ComputationTarget portfolio = _dbManager.getComputationTarget(
        new ComputationTargetSpecification(ComputationTargetType.PORTFOLIO_NODE, uid));
    assertNotNull(portfolio);
    assertEquals(ComputationTargetType.PORTFOLIO_NODE.ordinal(), portfolio.getComputationTargetType());
    assertEquals(uid.getScheme(), portfolio.getIdScheme());
    assertEquals(uid.getValue(), portfolio.getIdValue());
    assertEquals(uid.getVersion(), portfolio.getIdVersion());
    
    ComputationTarget position = _dbManager.getComputationTarget(
        new ComputationTargetSpecification(ComputationTargetType.POSITION, uid));
    assertEquals(ComputationTargetType.POSITION.ordinal(), position.getComputationTargetType());
    
    ComputationTarget security = _dbManager.getComputationTarget(
        new ComputationTargetSpecification(ComputationTargetType.SECURITY, uid));
    assertEquals(ComputationTargetType.SECURITY.ordinal(), security.getComputationTargetType());
    
    ComputationTarget primitive = _dbManager.getComputationTarget(
        new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, uid));
    assertEquals(ComputationTargetType.PRIMITIVE.ordinal(), primitive.getComputationTargetType());
  }

  @Test
  public void getComputationTarget() {
    UniqueIdentifier uid = UniqueIdentifier.of("foo", "bar", "1");
    
    MockSecurity mockSecurity = new MockSecurity("option");
    mockSecurity.setUniqueId(uid);
    mockSecurity.setName("myOption");
    
    ComputationTarget security = _dbManager.getComputationTarget(
        new com.opengamma.engine.ComputationTarget(mockSecurity));
    assertEquals(ComputationTargetType.SECURITY.ordinal(), security.getComputationTargetType());
    assertEquals("myOption", security.getName());
    
    ComputationTarget primitive = _dbManager.getComputationTarget(
        new com.opengamma.engine.ComputationTarget(uid));
    assertEquals(ComputationTargetType.PRIMITIVE.ordinal(), primitive.getComputationTargetType());
    assertNull(primitive.getName());
  }

  @Test
  public void updateComputationTarget() {
    UniqueIdentifier uid = UniqueIdentifier.of("foo", "bar");
    
    MockSecurity mockSecurity = new MockSecurity("option");
    mockSecurity.setUniqueId(uid);
    mockSecurity.setName("myOption");
    
    ComputationTarget security = _dbManager.getComputationTarget(
        new ComputationTargetSpecification(ComputationTargetType.SECURITY, uid));
    assertEquals(ComputationTargetType.SECURITY.ordinal(), security.getComputationTargetType());
    assertNull(security.getName());
    
    security = _dbManager.getComputationTarget(
        new com.opengamma.engine.ComputationTarget(mockSecurity));
    assertEquals(ComputationTargetType.SECURITY.ordinal(), security.getComputationTargetType());
    assertEquals("myOption", security.getName()); 
  }

  @Test
  public void getValueName() {
    // create
    RiskValueName valueName1 = _dbManager.getRiskValueName("test_name");
    assertNotNull(valueName1);
    assertEquals("test_name", valueName1.getName());
    
    // get
    RiskValueName valueName2 = _dbManager.getRiskValueName("test_name");
    assertEquals(valueName1, valueName2);
  }

  @Test
  public void getFunctionUniqueId() {
    // create
    FunctionUniqueId id1 = _dbManager.getFunctionUniqueId("test_id");
    assertNotNull(id1);
    assertEquals("test_id", id1.getUniqueId());
    
    // get
    FunctionUniqueId id2 = _dbManager.getFunctionUniqueId("test_id");
    assertEquals(id1, id2);
  }

  @Test
  public void searchAllBatches() {
    _dbManager.createLiveDataSnapshot(_batchJobRun.getSnapshotId());
    _dbManager.startBatch(_batchJobRun);
    
    BatchSearchRequest request = new BatchSearchRequest();
    
    BatchSearchResult result = _dbManager.search(request);
    assertNotNull(result);
    
    assertEquals(1, result.getItems().size());
    BatchSearchResultItem item = result.getItems().get(0);
    assertEquals(item.getObservationDate(), _batchJobRun.getObservationDate());
    assertEquals(item.getObservationTime(), _batchJobRun.getObservationTime());
    assertEquals(BatchStatus.RUNNING, item.getStatus());
    
    _dbManager.endBatch(_batchJobRun);
    result = _dbManager.search(request);
    assertEquals(1, result.getItems().size());
    item = result.getItems().get(0);
    assertEquals(item.getObservationDate(), _batchJobRun.getObservationDate());
    assertEquals(item.getObservationTime(), _batchJobRun.getObservationTime());
    assertEquals(BatchStatus.COMPLETE, item.getStatus());
  }

  @Test
  public void searchOneBatch() {
    _dbManager.createLiveDataSnapshot(_batchJobRun.getSnapshotId());
    _dbManager.startBatch(_batchJobRun);
    
    BatchSearchRequest request = new BatchSearchRequest();
    request.setObservationDate(_batchJobRun.getObservationDate());
    request.setObservationTime(_batchJobRun.getObservationTime());
    
    BatchSearchResult result = _dbManager.search(request);
    assertNotNull(result);
    
    assertEquals(1, result.getItems().size());
    BatchSearchResultItem item = result.getItems().get(0);
    assertEquals(item.getObservationDate(), _batchJobRun.getObservationDate());
    assertEquals(item.getObservationTime(), _batchJobRun.getObservationTime());
    assertEquals(BatchStatus.RUNNING, item.getStatus());
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void getResultsNonexistentBatch() {
    BatchDataSearchRequest request = new BatchDataSearchRequest();
    request.setObservationDate(LocalDate.of(2000, 5, 5));
    request.setObservationTime(_batchJobRun.getObservationTime());
    
    _dbManager.getResults(request);
  }

  @Test
  public void getResultsExistingBatch() {
    _dbManager.createLiveDataSnapshot(_batchJobRun.getSnapshotId());
    _dbManager.startBatch(_batchJobRun);
    
    BatchDataSearchRequest request = new BatchDataSearchRequest();
    request.setObservationDate(_batchJobRun.getObservationDate());
    request.setObservationTime(_batchJobRun.getObservationTime());
    
    commit();
    startNewTransaction();
    
    BatchDataSearchResult result = _dbManager.getResults(request);
    assertNotNull(result);
    assertTrue(result.getItems().isEmpty());
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void getErrorsNonexistentBatch() {
    BatchErrorSearchRequest request = new BatchErrorSearchRequest();
    request.setObservationDate(LocalDate.of(2000, 5, 5));
    request.setObservationTime(_batchJobRun.getObservationTime());
    
    _dbManager.getErrors(request);
  }

  @Test
  public void getErrorsExistingBatch() {
    _dbManager.createLiveDataSnapshot(_batchJobRun.getSnapshotId());
    _dbManager.startBatch(_batchJobRun);
    
    BatchErrorSearchRequest request = new BatchErrorSearchRequest();
    request.setObservationDate(_batchJobRun.getObservationDate());
    request.setObservationTime(_batchJobRun.getObservationTime());
    
    commit();
    startNewTransaction();
    
    BatchErrorSearchResult result = _dbManager.getErrors(request);
    assertNotNull(result);
    assertTrue(result.getItems().isEmpty());
  }

  @Test(expectedExceptions=IllegalStateException.class)
  public void deleteNonExisting() {
    _dbManager.deleteBatch(_batchJobRun);
  }

  @Test
  public void delete() {
    assertNull(_dbManager.getRiskRunFromDb(_batchJobRun));
    
    _dbManager.createLiveDataSnapshot(_batchJobRun.getSnapshotId());
    _dbManager.startBatch(_batchJobRun);
    
    assertNotNull(_dbManager.getRiskRunFromDb(_batchJobRun));
    
    _dbManager.deleteBatch(_batchJobRun);
    assertNull(_dbManager.getRiskRunFromDb(_batchJobRun));
  }

  @Test
  public void writeAdHocBatchResult() {
    commit(); // don't want this test to be transactional
    
    Instant now = Instant.now();
    
    BatchId batchId = new BatchId(LocalDate.of(2005, 1, 2), "LDN_CLOSE");
    InMemoryViewComputationResultModel result = new InMemoryViewComputationResultModel();
    result.setResultTimestamp(now);
    result.setValuationTime(now);
    result.setViewName("testview");
    
    ComputationTargetSpecification spec = new ComputationTargetSpecification(UniqueIdentifier.of("BUID", "EQ12345", null)); 
    result.addLiveData(new ComputedValue(
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
    
    AdHocBatchResult adHocBatchResult = new AdHocBatchResult(batchId, result);
    _dbManager.write(adHocBatchResult);    
  }

}
