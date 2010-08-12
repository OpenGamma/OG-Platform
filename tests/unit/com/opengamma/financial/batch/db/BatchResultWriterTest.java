/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.batch.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.time.Instant;

import org.junit.Before;
import org.junit.Test;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.MockFunction;
import com.opengamma.engine.position.PositionImpl;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.cache.ViewComputationCache;
import com.opengamma.engine.view.calcnode.AbstractCalculationNodeTest;
import com.opengamma.engine.view.calcnode.CalculationJob;
import com.opengamma.engine.view.calcnode.CalculationJobItem;
import com.opengamma.engine.view.calcnode.CalculationJobResult;
import com.opengamma.engine.view.calcnode.CalculationJobResultItem;
import com.opengamma.engine.view.calcnode.InvocationResult;
import com.opengamma.engine.view.calcnode.MissingInputException;
import com.opengamma.engine.view.calcnode.TestCalculationNode;
import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.test.HibernateTest;
import com.opengamma.util.time.DateUtil;

/**
 * 
 */
public class BatchResultWriterTest extends HibernateTest {
  
  private RiskRun _riskRun;
  private ObservationDateTime _observationDateTime;
  private ObservationTime _observationTime;
  private CalculationConfiguration _calculationConfiguration;
  private OpenGammaVersion _openGammaVersion;
  private LiveDataSnapshot _liveDataSnapshot;
  private ComputeHost _computeHost;
  private ComputeNode _computeNode;
  private com.opengamma.engine.ComputationTarget _mockFunctionComputationTarget;
  private Object _mockFunctionOutput;
  private com.opengamma.financial.batch.db.ComputationTarget _dbComputationTarget;
  private Set<com.opengamma.financial.batch.db.ComputationTarget> _dbComputationTargets;
  private Set<RiskValueName> _valueNames;
  
  private BatchResultWriter _resultWriter;

  private MockFunction _mockFunction;
  private TestCalculationNode _calcNode;
  private CalculationJob _calcJob;
  
  private HibernateTemplate _hibernateTemplate;
  
  public BatchResultWriterTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
  }
  
  @Before
  public void setUp() throws Exception {
    super.setUp();
    
    Instant now = Instant.nowSystemClock();
    
    _hibernateTemplate = new HibernateTemplate(getSessionFactory());
    _hibernateTemplate.setAllowCreate(false);
    
    getSessionFactory().getCurrentSession().beginTransaction();
    
    _openGammaVersion = new OpenGammaVersion();
    _openGammaVersion.setVersion("1.0");
    _openGammaVersion.setHash("1a2c3d4f");
    _hibernateTemplate.save(_openGammaVersion);
    
    _observationTime = new ObservationTime();
    _observationTime.setLabel("LDN_CLOSE");
    _hibernateTemplate.save(_observationTime);
    
    _observationDateTime = new ObservationDateTime();
    _observationDateTime.setDate(new Date(now.toEpochMillisLong()));
    _observationDateTime.setObservationTime(_observationTime);
    _hibernateTemplate.save(_observationDateTime);
    
    _computeHost = new ComputeHost();
    _computeHost.setHostName("test-host");
    _hibernateTemplate.save(_computeHost);
    
    _computeNode = new ComputeNode();
    _computeNode.setComputeHost(_computeHost);
    _computeNode.setConfigOid("1");
    _computeNode.setConfigVersion(1);
    _computeNode.setNodeName("test-node");
    _hibernateTemplate.save(_computeNode);
    
    _liveDataSnapshot = new LiveDataSnapshot();
    _liveDataSnapshot.setComplete(true);
    _liveDataSnapshot.setSnapshotTime(_observationDateTime);
    _hibernateTemplate.save(_liveDataSnapshot);
    
    _riskRun = new RiskRun();
    _riskRun.setOpenGammaVersion(_openGammaVersion);
    _riskRun.setMasterProcessHost(_computeHost);
    _riskRun.setRunReason("BatchResultWriterTest");
    _riskRun.setRunTime(_observationDateTime);
    _riskRun.setValuationTime(DateUtil.toSqlTimestamp(now));
    _riskRun.setViewOid("view-oid");
    _riskRun.setViewVersion(1);
    _riskRun.setLiveDataSnapshot(_liveDataSnapshot);
    _riskRun.setCreateInstant(DateUtil.toSqlTimestamp(now));
    _riskRun.setStartInstant(DateUtil.toSqlTimestamp(now));
    _riskRun.setNumRestarts(0);
    _riskRun.setComplete(false);
    _hibernateTemplate.save(_riskRun);
    
    _calculationConfiguration = new CalculationConfiguration();
    _calculationConfiguration.setName(AbstractCalculationNodeTest.CALC_CONF_NAME);
    _calculationConfiguration.setRiskRun(_riskRun);
    _hibernateTemplate.save(_calculationConfiguration);
    _riskRun.addCalculationConfiguration(_calculationConfiguration);
    
    _valueNames = new HashSet<RiskValueName>();
    RiskValueName valueName = new RiskValueName();
    valueName.setName("OUTPUT");
    _valueNames.add(valueName);
    valueName = new RiskValueName();
    valueName.setName("OUTPUT1");
    _valueNames.add(valueName);
    valueName = new RiskValueName();
    valueName.setName("OUTPUT2");
    _valueNames.add(valueName);
    _hibernateTemplate.saveOrUpdateAll(_valueNames);
    
    _resultWriter = new BatchResultWriter();
    
    _resultWriter.setJdbcUrl(getDbTool().getJdbcUrl());
    _resultWriter.setUsername(getDbTool().getUser());
    _resultWriter.setPassword(getDbTool().getPassword());
    
    _resultWriter.setTransactionManager(getTransactionManager());
    _resultWriter.setSessionFactory(getSessionFactory());
    
    _resultWriter.setRiskRun(_riskRun);
    _resultWriter.setComputeNode(_computeNode);
    
    _resultWriter.setRiskValueNames(_valueNames);
    
    _mockFunctionComputationTarget = new com.opengamma.engine.ComputationTarget(ComputationTargetType.POSITION, 
        new PositionImpl(
            UniqueIdentifier.of("Mock", "AAPL Stock UID"), 
            new BigDecimal(500), 
            new Identifier("Mock", "AAPL Stock ID")));
    _mockFunctionOutput = new Double(4000.50);   
    
    _mockFunction = AbstractCalculationNodeTest.getMockFunction(_mockFunctionComputationTarget, _mockFunctionOutput);
    _calcNode = AbstractCalculationNodeTest.getTestCalcNode(_mockFunction);
    _calcJob = AbstractCalculationNodeTest.getCalculationJob(_mockFunction, _resultWriter);
    
    _dbComputationTargets = new HashSet<com.opengamma.financial.batch.db.ComputationTarget>();
    _dbComputationTarget = new com.opengamma.financial.batch.db.ComputationTarget();
    _dbComputationTarget.setComputationTargetType(_mockFunction.getTarget().getType());
    _dbComputationTarget.setIdScheme(_mockFunction.getTarget().getUniqueIdentifier().getScheme());
    _dbComputationTarget.setIdValue(_mockFunction.getTarget().getUniqueIdentifier().getValue());
    _dbComputationTargets.add(_dbComputationTarget);
    _hibernateTemplate.saveOrUpdateAll(_dbComputationTargets);
    
    _resultWriter.setComputationTargets(_dbComputationTargets);
    _resultWriter.initialize(_calcNode, getDbTool());
    
    getSessionFactory().getCurrentSession().getTransaction().commit();
  }
  
  @Test
  public void getItemsToExecuteNotARestart() {
    List<CalculationJobItem> itemsToExecute = _resultWriter.getItemsToExecute(_calcNode, _calcJob);
    assertEquals(_calcJob.getJobItems(), itemsToExecute);
  }
  
  @Test
  public void getItemsToExecuteRestart() {
    _riskRun.setNumRestarts(1);
    _resultWriter.setIsRestart(true);
    
    // nothing in db
    List<CalculationJobItem> itemsToExecute = _resultWriter.getItemsToExecute(_calcNode, _calcJob);
    assertEquals(_calcJob.getJobItems(), itemsToExecute);
    
    // already successfully executed, but outputs NOT in cache.
    // should re-execute, but not write results into DB.
    _resultWriter.openSession();
    try {
      _resultWriter.upsertStatusEntries(
          _calcJob.getSpecification(), 
          StatusEntry.Status.SUCCESS, 
          Sets.newHashSet(_dbComputationTarget.toSpec()));
    } finally {
      _resultWriter.closeSession();
    }
    
    itemsToExecute = _resultWriter.getItemsToExecute(_calcNode, _calcJob);
    assertEquals(1, itemsToExecute.size());
    CalculationJobItem expected = _calcJob.getJobItems().get(0);
    CalculationJobItem actual = itemsToExecute.get(0);
    assertEquals(expected.getComputationTargetSpecification(), actual.getComputationTargetSpecification());
    assertEquals(expected.getDesiredValues(), actual.getDesiredValues());
    assertEquals(expected.getFunctionUniqueIdentifier(), actual.getFunctionUniqueIdentifier());
    assertEquals(expected.getInputs(), actual.getInputs());
    assertEquals(expected.getOutputs(), actual.getOutputs());
    assertEquals(false, expected.isOutputsDisabled());
    assertEquals(true, actual.isOutputsDisabled());
    
    // already successfully executed, AND outputs in cache
    // should not re-execute
    putOutputToCache();
    
    _resultWriter.upsertStatusEntries(
        _calcJob.getSpecification(), 
        StatusEntry.Status.SUCCESS, 
        Sets.newHashSet(_dbComputationTarget.toSpec()));
    
    itemsToExecute = _resultWriter.getItemsToExecute(_calcNode, _calcJob);
    assertTrue(itemsToExecute.isEmpty());
    
    // failed
    // should re-execute
    _resultWriter.upsertStatusEntries(
        _calcJob.getSpecification(), 
        StatusEntry.Status.FAILURE, 
        Sets.newHashSet(_dbComputationTarget.toSpec()));
    
    itemsToExecute = _resultWriter.getItemsToExecute(_calcNode, _calcJob);
    assertEquals(_calcJob.getJobItems(), itemsToExecute);
    
    // running
    // should re-execute (assumption being that the previous batch attempt
    // was hard-killed while it was running and is no longer really running)
    _resultWriter.upsertStatusEntries(
        _calcJob.getSpecification(), 
        StatusEntry.Status.RUNNING, 
        Sets.newHashSet(_dbComputationTarget.toSpec()));
    
    itemsToExecute = _resultWriter.getItemsToExecute(_calcNode, _calcJob);
    assertEquals(_calcJob.getJobItems(), itemsToExecute);
    
    // not running
    // should re-execute
    _resultWriter.upsertStatusEntries(
        _calcJob.getSpecification(), 
        StatusEntry.Status.NOT_RUNNING, 
        Sets.newHashSet(_dbComputationTarget.toSpec()));
    
    itemsToExecute = _resultWriter.getItemsToExecute(_calcNode, _calcJob);
    assertEquals(_calcJob.getJobItems(), itemsToExecute);
    
    // a PRIMITIVE (non-DB-writable) target, outputs NOT in cache
    // should re-execute
    ComputationTargetSpecification primitiveTarget = new ComputationTargetSpecification(
        ComputationTargetType.PRIMITIVE,
        UniqueIdentifier.of("foo", "bar"));
    ValueRequirement primitiveOutputReq = new ValueRequirement("FairValue", primitiveTarget);
    ValueSpecification primitiveOutputSpec = new ValueSpecification(primitiveOutputReq);
    CalculationJobItem primitiveItem = new CalculationJobItem(
        "function1",
        primitiveTarget,
        Collections.<ValueSpecification>emptySet(),
        Collections.singleton(primitiveOutputReq),
        false); // <---- the key part
            
    CalculationJob primitiveJob = new CalculationJob(
        _calcJob.getSpecification(),
        Collections.singletonList(primitiveItem),
        _calcJob.getResultWriter());
    itemsToExecute = _resultWriter.getItemsToExecute(_calcNode, primitiveJob);
    assertEquals(primitiveJob.getJobItems(), itemsToExecute);
    
    // a PRIMITIVE (non-DB-writable) target, outputs in cache
    // should not re-execute
    putValue(new ComputedValue(primitiveOutputSpec, 500.50));
    itemsToExecute = _resultWriter.getItemsToExecute(_calcNode, primitiveJob);
    assertTrue(itemsToExecute.isEmpty());
  }

  @Test
  public void emptyResult() {
    CalculationJobResult result = new CalculationJobResult(
        _calcJob.getSpecification(),
        0,
        Collections.<CalculationJobResultItem>emptyList());
    _resultWriter.write(_calcNode, result);
    assertEquals(0, _resultWriter.getNumRiskRows());
    assertEquals(0, _resultWriter.getNumRiskFailureRows());
    assertEquals(0, _resultWriter.getNumRiskFailureReasonRows());
    assertEquals(0, _resultWriter.getNumRiskComputeFailureRows());
  }
  
  @Test
  public void functionWasSuccessful() {
    CalculationJobResultItem item = new CalculationJobResultItem(
        _calcJob.getJobItems().get(0),
        InvocationResult.SUCCESS);
    item.setResults(Collections.singleton(_mockFunction.getResult()));
    
    CalculationJobResult result = new CalculationJobResult(
        _calcJob.getSpecification(),
        200,
        Collections.singletonList(item));
    
    _resultWriter.write(_calcNode, result);
    
    assertEquals(1, _resultWriter.getNumRiskRows());
    RiskValue value = getValueFromDb();
    assertEquals(_mockFunction.getResult().getValue(), value.getValue());
    
    assertEquals(0, _resultWriter.getNumRiskFailureRows());
    assertEquals(0, _resultWriter.getNumRiskFailureReasonRows());
    assertEquals(0, _resultWriter.getNumRiskComputeFailureRows());
  }
  
  @Test
  public void functionWasSuccessfulButProducesUnsupportedOutputType() {
    CalculationJobResultItem item = new CalculationJobResultItem(
        _calcJob.getJobItems().get(0),
        InvocationResult.SUCCESS);
    
    ComputedValue outputWithANonDoubleValue = new ComputedValue(
        _mockFunction.getResultSpec(), 
        "unsupported value type: String");
    item.setResults(Collections.singleton(outputWithANonDoubleValue));
    
    CalculationJobResult result = new CalculationJobResult(
        _calcJob.getSpecification(),
        200,
        Collections.singletonList(item));
    
    _resultWriter.write(_calcNode, result);
    
    assertEquals(0, _resultWriter.getNumRiskRows());
    assertEquals(1, _resultWriter.getNumRiskFailureRows());
    assertEquals(0, _resultWriter.getNumRiskFailureReasonRows());
    assertEquals(0, _resultWriter.getNumRiskComputeFailureRows());
  }
  
  @Test
  public void functionExecutionThrewException() {
    CalculationJobResultItem item = new CalculationJobResultItem(
        _calcJob.getJobItems().get(0),
        InvocationResult.ERROR);
    item.setException(new RuntimeException("function execution failed"));
    
    CalculationJobResult result = new CalculationJobResult(
        _calcJob.getSpecification(),
        200,
        Collections.singletonList(item));
    
    _resultWriter.write(_calcNode, result);
    
    assertEquals(0, _resultWriter.getNumRiskRows());
    assertEquals(1, _resultWriter.getNumRiskFailureRows());
    assertEquals(1, _resultWriter.getNumRiskFailureReasonRows());
    assertEquals(1, _resultWriter.getNumRiskComputeFailureRows());
  }
  
  @Test
  public void missingFunctionInputs() {
    CalculationJobResultItem item = new CalculationJobResultItem(
        _calcJob.getJobItems().get(0),
        InvocationResult.ERROR);
    
    item.setException(new MissingInputException(
        item.getItem().getInputs(), 
        _mockFunction.getUniqueIdentifier()));
    
    ComputeFailureKey inputFailureKey = new ComputeFailureKey(
        "inputFunction",
        "exceptionClass",
        "inputFailed",
        new StackTraceElement[0]);
    
    ComputeFailure inputFailure;
    _resultWriter.openSession();
    try {
      inputFailure = _resultWriter.saveComputeFailure(inputFailureKey);
    } finally {
      _resultWriter.closeSession();
    }
    
    assertEquals(0, _resultWriter.getNumRiskRows());
    assertEquals(0, _resultWriter.getNumRiskFailureRows());
    assertEquals(0, _resultWriter.getNumRiskFailureReasonRows());
    assertEquals(1, _resultWriter.getNumRiskComputeFailureRows());
    
    BatchResultWriter.BatchResultWriterFailure inputFailureInCache = new BatchResultWriter.BatchResultWriterFailure();
    inputFailureInCache.getComputeFailureIds().add(inputFailure.getId());
    
    putValue(new ComputedValue(item.getItem().getInputs().iterator().next(), inputFailureInCache));
    
    CalculationJobResult result = new CalculationJobResult(
        _calcJob.getSpecification(),
        200,
        Collections.singletonList(item));
    
    _resultWriter.write(_calcNode, result);
    
    assertEquals(0, _resultWriter.getNumRiskRows());
    assertEquals(1, _resultWriter.getNumRiskFailureRows());
    assertEquals(1, _resultWriter.getNumRiskFailureReasonRows());
    assertEquals(1, _resultWriter.getNumRiskComputeFailureRows());
  }
  
  @Test
  public void missingFunctionInputsButNoInputFailureInformationInCache() {
    CalculationJobResultItem item = new CalculationJobResultItem(
        _calcJob.getJobItems().get(0),
        InvocationResult.ERROR);
    
    item.setException(new MissingInputException(
        item.getItem().getInputs(), 
        _mockFunction.getUniqueIdentifier()));
    
    CalculationJobResult result = new CalculationJobResult(
        _calcJob.getSpecification(),
        200,
        Collections.singletonList(item));
    
    _resultWriter.write(_calcNode, result);
    
    assertEquals(0, _resultWriter.getNumRiskRows());
    assertEquals(1, _resultWriter.getNumRiskFailureRows());
    assertEquals(0, _resultWriter.getNumRiskFailureReasonRows());
    assertEquals(0, _resultWriter.getNumRiskComputeFailureRows());
  }
  
  @Test
  public void successfulAndFailedResultOnSameTarget() {
    CalculationJobResultItem successItem = new CalculationJobResultItem(
        new CalculationJobItem("function1", 
            _mockFunction.getTarget().toSpecification(),
            Collections.<ValueSpecification>emptySet(),
            Collections.singleton(new ValueRequirement("OUTPUT1", _mockFunction.getTarget().toSpecification())),
            true),
        InvocationResult.SUCCESS);
    successItem.setResults(Collections.<ComputedValue>emptySet());
    
    CalculationJobResultItem failedItem = new CalculationJobResultItem(
        new CalculationJobItem("function1", 
            _mockFunction.getTarget().toSpecification(),
            Collections.<ValueSpecification>emptySet(),
            Collections.singleton(new ValueRequirement("OUTPUT2", _mockFunction.getTarget().toSpecification())),
            true),
        InvocationResult.ERROR);
    failedItem.setException(new RuntimeException("function execution failed"));
    
    ArrayList<CalculationJobResultItem> items = new ArrayList<CalculationJobResultItem>();
    items.add(successItem);
    items.add(failedItem);
    
    CalculationJobResult result = new CalculationJobResult(
        _calcJob.getSpecification(),
        200,
        items);
    
    _resultWriter.write(_calcNode, result);
    
    // note - success row not written
    assertEquals(0, _resultWriter.getNumRiskRows());
    assertEquals(2, _resultWriter.getNumRiskFailureRows());
    assertEquals(1, _resultWriter.getNumRiskFailureReasonRows());
    assertEquals(1, _resultWriter.getNumRiskComputeFailureRows());
  }
  
  @Override
  public Class<?>[] getHibernateMappingClasses() {
    return BatchDbManagerImpl.getHibernateMappingClasses();
  }
  
  private RiskValue getValueFromDb() {
    return _resultWriter.getValue(
        AbstractCalculationNodeTest.CALC_CONF_NAME, 
        _mockFunction.getResultSpec().getRequirementSpecification().getValueName(), 
        _mockFunction.getTarget().toSpecification());
  }
  
  private void putOutputToCache() {
    ComputedValue output = _mockFunction.getResult();
    putValue(output);
  }
  
  private void putValue(ComputedValue value) {
    ViewComputationCache cache = _calcNode.getCache(_calcJob.getSpecification());
    cache.putValue(value);
  }
  
}
