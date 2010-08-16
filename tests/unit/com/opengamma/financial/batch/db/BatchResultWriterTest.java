/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.batch.db;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.time.Instant;

import org.junit.Before;
import org.junit.Test;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.function.MockFunction;
import com.opengamma.engine.position.PositionImpl;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ResultModelDefinition;
import com.opengamma.engine.view.ResultOutputMode;
import com.opengamma.engine.view.cache.ViewComputationCache;
import com.opengamma.engine.view.calc.TestDependencyGraphExecutor;
import com.opengamma.engine.view.calcnode.AbstractCalculationNodeTest;
import com.opengamma.engine.view.calcnode.CalculationJob;
import com.opengamma.engine.view.calcnode.CalculationJobItem;
import com.opengamma.engine.view.calcnode.CalculationJobResult;
import com.opengamma.engine.view.calcnode.CalculationJobResultItem;
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
    
    _mockFunctionComputationTarget = new com.opengamma.engine.ComputationTarget(ComputationTargetType.POSITION, 
        new PositionImpl(
            UniqueIdentifier.of("Mock", "AAPL Stock UID"), 
            new BigDecimal(500), 
            new Identifier("Mock", "AAPL Stock ID")));
    _mockFunctionOutput = new Double(4000.50);   
    _mockFunction = AbstractCalculationNodeTest.getMockFunction(_mockFunctionComputationTarget, _mockFunctionOutput);
    
    _dbComputationTargets = new HashSet<com.opengamma.financial.batch.db.ComputationTarget>();
    _dbComputationTarget = new com.opengamma.financial.batch.db.ComputationTarget();
    _dbComputationTarget.setComputationTargetType(_mockFunction.getTarget().getType());
    _dbComputationTarget.setIdScheme(_mockFunction.getTarget().getUniqueIdentifier().getScheme());
    _dbComputationTarget.setIdValue(_mockFunction.getTarget().getUniqueIdentifier().getValue());
    _dbComputationTargets.add(_dbComputationTarget);
    _hibernateTemplate.saveOrUpdateAll(_dbComputationTargets);

    _calcNode = AbstractCalculationNodeTest.getTestCalcNode(_mockFunction);
    _calcJob = AbstractCalculationNodeTest.getCalculationJob(_mockFunction);

    getSessionFactory().getCurrentSession().getTransaction().commit();
  }
  
  
  // --------------------------------------------------------------------------
  
  
  private BatchResultWriter getSuccessResultWriter() {
    CalculationJobResultItem item = new CalculationJobResultItem(_calcJob.getJobItems().get(0));
    CalculationJobResult result = new CalculationJobResult(
        _calcJob.getSpecification(),
        200,
        Collections.singletonList(item),
        "localhost");
    
    return getResultWriter(result);
  }
  
  private BatchResultWriter getResultWriter(CalculationJobResult result) {
    
    Map<String, ViewComputationCache> cachesByCalculationConfiguration = new HashMap<String, ViewComputationCache>();
    cachesByCalculationConfiguration.put(AbstractCalculationNodeTest.CALC_CONF_NAME, getCache());
    
    ResultModelDefinition resultModelDefinition = new ResultModelDefinition();
    resultModelDefinition.setAggregatePositionOutputMode(ResultOutputMode.ALL);
    resultModelDefinition.setPositionOutputMode(ResultOutputMode.ALL);
    resultModelDefinition.setSecurityOutputMode(ResultOutputMode.NONE);
    resultModelDefinition.setPrimitiveOutputMode(ResultOutputMode.NONE);
    BatchResultWriter resultWriter = new BatchResultWriter(
        new TestDependencyGraphExecutor<CalculationJobResult>(result),
        resultModelDefinition,
        cachesByCalculationConfiguration);
    
    resultWriter.setJdbcUrl(getDbTool().getJdbcUrl());
    resultWriter.setUsername(getDbTool().getUser());
    resultWriter.setPassword(getDbTool().getPassword());
    
    resultWriter.setTransactionManager(getTransactionManager());
    resultWriter.setSessionFactory(getSessionFactory());
    
    resultWriter.setRiskRun(_riskRun);
    resultWriter.setRiskValueNames(_valueNames);
    
    resultWriter.setComputationTargets(_dbComputationTargets);
    resultWriter.initialize(getDbTool());
    
    return resultWriter;
  }

  private DependencyGraph getPositionDepGraph() {
    DependencyNode node = new DependencyNode(
        _mockFunction,
        _mockFunction.getTarget(),
        Collections.<DependencyNode>emptySet(),
        Collections.<ValueSpecification>emptySet(),
        _mockFunction.getResultSpecs());
    DependencyGraph graph = new DependencyGraph(AbstractCalculationNodeTest.CALC_CONF_NAME);
    graph.addDependencyNode(node);
    return graph;
  }
  
  private DependencyGraph getPrimitiveDepGraph() {
    com.opengamma.engine.ComputationTarget primitiveTarget = 
      new com.opengamma.engine.ComputationTarget(ComputationTargetType.PRIMITIVE, new String("foo"));
    
    DependencyNode node = new DependencyNode(
        new MockFunction(primitiveTarget),
        primitiveTarget,
        Collections.<DependencyNode>emptySet(),
        Collections.<ValueSpecification>emptySet(),
        _mockFunction.getResultSpecs());
    DependencyGraph graph = new DependencyGraph(AbstractCalculationNodeTest.CALC_CONF_NAME);
    graph.addDependencyNode(node);
    return graph;
  }
  
  private void setIsRestart(BatchResultWriter resultWriter) {
    _riskRun.setNumRestarts(1);
    resultWriter.setIsRestart(true);
  }
  

  // --------------------------------------------------------------------------
  
  
  @Test
  public void notARestart() {
    // should execute

    BatchResultWriter resultWriter = getSuccessResultWriter();
    DependencyGraph originalGraph = getPositionDepGraph();
    DependencyGraph graphToExecute = resultWriter.getGraphToExecute(originalGraph);
    assertEquals(originalGraph.getSize(), graphToExecute.getSize());
  }
  
  @Test
  public void restartButNoStatusEntryInDb() {
    // should re-execute
    
    BatchResultWriter resultWriter = getSuccessResultWriter();
    setIsRestart(resultWriter);

    DependencyGraph originalGraph = getPositionDepGraph();
    DependencyGraph graphToExecute = resultWriter.getGraphToExecute(originalGraph);
    assertEquals(originalGraph.getSize(), graphToExecute.getSize());
  }
  
  @Test
  public void restartSuccessButOutputsNotInCache() {
    // should re-execute, but not write results into DB.
    
    BatchResultWriter resultWriter = getSuccessResultWriter();
    setIsRestart(resultWriter);
    
    resultWriter.openSession();
    resultWriter.upsertStatusEntries(
        _calcJob.getSpecification(), 
        StatusEntry.Status.SUCCESS, 
        Sets.newHashSet(_dbComputationTarget.toSpec()));
    resultWriter.closeSession();
    
    DependencyGraph originalGraph = getPositionDepGraph();
    DependencyGraph graphToExecute = resultWriter.getGraphToExecute(originalGraph);
    assertEquals(originalGraph.getSize(), graphToExecute.getSize());
  }
  
  @Test
  public void restartSuccessOutputInCache() {
    // should not re-execute
    
    putOutputToCache();
    
    BatchResultWriter resultWriter = getSuccessResultWriter();
    setIsRestart(resultWriter);
    
    resultWriter.openSession();
    resultWriter.upsertStatusEntries(
        _calcJob.getSpecification(), 
        StatusEntry.Status.SUCCESS, 
        Sets.newHashSet(_dbComputationTarget.toSpec()));
    resultWriter.closeSession();
    
    DependencyGraph originalGraph = getPositionDepGraph();
    DependencyGraph graphToExecute = resultWriter.getGraphToExecute(originalGraph);
    assertEquals(0, graphToExecute.getSize());
  }

  @Test
  public void restartFailed() {
    // should re-execute
    
    BatchResultWriter resultWriter = getSuccessResultWriter();
    setIsRestart(resultWriter);
    
    resultWriter.openSession();
    resultWriter.upsertStatusEntries(
        _calcJob.getSpecification(), 
        StatusEntry.Status.FAILURE, 
        Sets.newHashSet(_dbComputationTarget.toSpec()));
    resultWriter.closeSession();
    
    DependencyGraph originalGraph = getPositionDepGraph();
    DependencyGraph graphToExecute = resultWriter.getGraphToExecute(originalGraph);
    assertEquals(originalGraph.getSize(), graphToExecute.getSize());
  }
  
  @Test
  public void restartRunning() {
    // should re-execute (assumption being that the previous batch attempt
    // was hard-killed while it was running and is no longer really running)
    
    BatchResultWriter resultWriter = getSuccessResultWriter();
    setIsRestart(resultWriter);
    
    resultWriter.openSession();
    resultWriter.upsertStatusEntries(
        _calcJob.getSpecification(), 
        StatusEntry.Status.RUNNING, 
        Sets.newHashSet(_dbComputationTarget.toSpec()));
    resultWriter.closeSession();
    
    DependencyGraph originalGraph = getPositionDepGraph();
    DependencyGraph graphToExecute = resultWriter.getGraphToExecute(originalGraph);
    assertEquals(originalGraph.getSize(), graphToExecute.getSize());
  }
  
  @Test
  public void restartNotRunning() {
    // should re-execute
    
    BatchResultWriter resultWriter = getSuccessResultWriter();
    setIsRestart(resultWriter);
    
    resultWriter.openSession();
    resultWriter.upsertStatusEntries(
        _calcJob.getSpecification(), 
        StatusEntry.Status.NOT_RUNNING, 
        Sets.newHashSet(_dbComputationTarget.toSpec()));
    resultWriter.closeSession();
    
    DependencyGraph originalGraph = getPositionDepGraph();
    DependencyGraph graphToExecute = resultWriter.getGraphToExecute(originalGraph);
    assertEquals(originalGraph.getSize(), graphToExecute.getSize());
  }
  
  @Test
  public void restartPrimitiveSuccess() {
    // should re-execute
    
    BatchResultWriter resultWriter = getSuccessResultWriter();
    setIsRestart(resultWriter);
    
    DependencyGraph originalGraph = getPrimitiveDepGraph();
    DependencyGraph graphToExecute = resultWriter.getGraphToExecute(originalGraph);
    assertEquals(originalGraph.getSize(), graphToExecute.getSize());
  }

  @Test
  public void restartPrimitiveSuccessOutputsInCache() {
    // should not re-execute
    
    BatchResultWriter resultWriter = getSuccessResultWriter();
    setIsRestart(resultWriter);
    
    DependencyGraph originalGraph = getPrimitiveDepGraph();
    DependencyNode primitiveNode = originalGraph.getDependencyNodes().iterator().next();
    putValue(new ComputedValue(primitiveNode.getOutputValues().iterator().next(), 500.50));
    
    DependencyGraph graphToExecute = resultWriter.getGraphToExecute(originalGraph);
    assertEquals(0, graphToExecute.getSize());
  }
  
  
  // --------------------------------------------------------------------------
  
  
  @Test
  public void emptyResult() {
    CalculationJobResult result = new CalculationJobResult(
        _calcJob.getSpecification(),
        0,
        Collections.<CalculationJobResultItem>emptyList(),
        "localhost");
    
    BatchResultWriter resultWriter = getSuccessResultWriter();
    resultWriter.jobExecuted(result, null);
    
    assertEquals(0, resultWriter.getNumRiskRows());
    assertEquals(0, resultWriter.getNumRiskFailureRows());
    assertEquals(0, resultWriter.getNumRiskFailureReasonRows());
    assertEquals(0, resultWriter.getNumRiskComputeFailureRows());
  }
  
  @Test
  public void functionWasSuccessful() {
    CalculationJobResultItem item = new CalculationJobResultItem(_calcJob.getJobItems().get(0));
    putOutputToCache();
    
    CalculationJobResult result = new CalculationJobResult(
        _calcJob.getSpecification(),
        200,
        Collections.singletonList(item),
        "localhost");
    
    BatchResultWriter resultWriter = getSuccessResultWriter();
    resultWriter.jobExecuted(result, null);
    
    assertEquals(1, resultWriter.getNumRiskRows());
    RiskValue value = getValueFromDb(resultWriter);
    assertEquals(_mockFunction.getResult().getValue(), value.getValue());
    
    assertEquals(0, resultWriter.getNumRiskFailureRows());
    assertEquals(0, resultWriter.getNumRiskFailureReasonRows());
    assertEquals(0, resultWriter.getNumRiskComputeFailureRows());
  }
  
  @Test
  public void functionWasSuccessfulButProducesUnsupportedOutputType() {
    CalculationJobResultItem item = new CalculationJobResultItem(_calcJob.getJobItems().get(0));
    
    ComputedValue outputWithANonDoubleValue = new ComputedValue(
        _mockFunction.getResultSpec(), 
        "unsupported value type: String");
    putValue(outputWithANonDoubleValue);
    
    CalculationJobResult result = new CalculationJobResult(
        _calcJob.getSpecification(),
        200,
        Collections.singletonList(item),
        "localhost");
    
    BatchResultWriter resultWriter = getSuccessResultWriter();
    resultWriter.jobExecuted(result, null);
    
    assertEquals(0, resultWriter.getNumRiskRows());
    assertEquals(1, resultWriter.getNumRiskFailureRows());
    assertEquals(0, resultWriter.getNumRiskFailureReasonRows());
    assertEquals(0, resultWriter.getNumRiskComputeFailureRows());
  }
  
  @Test
  public void functionExecutionThrewException() {
    CalculationJobResultItem item = new CalculationJobResultItem(
        _calcJob.getJobItems().get(0),
        new RuntimeException("function execution failed"));
    
    CalculationJobResult result = new CalculationJobResult(
        _calcJob.getSpecification(),
        200,
        Collections.singletonList(item),
        "localhost");
    
    BatchResultWriter resultWriter = getSuccessResultWriter();
    resultWriter.jobExecuted(result, null);
    
    assertEquals(0, resultWriter.getNumRiskRows());
    assertEquals(1, resultWriter.getNumRiskFailureRows());
    assertEquals(1, resultWriter.getNumRiskFailureReasonRows());
    assertEquals(1, resultWriter.getNumRiskComputeFailureRows());
  }
  
  @Test
  public void missingFunctionInputs() {
    CalculationJobResultItem item = new CalculationJobResultItem(
        _calcJob.getJobItems().get(0),
        new MissingInputException(
            _calcJob.getJobItems().get(0).getInputs(), 
            _mockFunction.getUniqueIdentifier()));
    
    ComputeFailureKey inputFailureKey = new ComputeFailureKey(
        item.getItem().getFunctionUniqueIdentifier(),
        item.getExceptionClass(),
        item.getExceptionMsg(),
        item.getStackTrace());
    
    BatchResultWriter resultWriter = getSuccessResultWriter();
    
    resultWriter.openSession();
    ComputeFailure inputFailure = resultWriter.saveComputeFailure(inputFailureKey);
    resultWriter.closeSession();
    
    assertEquals(0, resultWriter.getNumRiskRows());
    assertEquals(0, resultWriter.getNumRiskFailureRows());
    assertEquals(0, resultWriter.getNumRiskFailureReasonRows());
    assertEquals(1, resultWriter.getNumRiskComputeFailureRows());
    
    BatchResultWriter.BatchResultWriterFailure cachedInputFailure = new BatchResultWriter.BatchResultWriterFailure();
    cachedInputFailure.addComputeFailureId(inputFailure.getId());
    ComputedValue cachedInputFailureValue = new ComputedValue(item.getItem().getInputs().iterator().next(), cachedInputFailure);
    putValue(cachedInputFailureValue);
    
    CalculationJobResult result = new CalculationJobResult(
        _calcJob.getSpecification(),
        200,
        Collections.singletonList(item),
        "localhost");
    
    resultWriter.jobExecuted(result, null);
    
    assertEquals(0, resultWriter.getNumRiskRows());
    assertEquals(1, resultWriter.getNumRiskFailureRows());
    assertEquals(1, resultWriter.getNumRiskFailureReasonRows());
    assertEquals(1, resultWriter.getNumRiskComputeFailureRows());
  }
  
  @Test
  public void missingFunctionInputsButNoInputFailureInformationInCache() {
    CalculationJobResultItem item = new CalculationJobResultItem(
        _calcJob.getJobItems().get(0),
        new MissingInputException(
            _calcJob.getJobItems().get(0).getInputs(), 
            _mockFunction.getUniqueIdentifier()));
    
    CalculationJobResult result = new CalculationJobResult(
        _calcJob.getSpecification(),
        200,
        Collections.singletonList(item),
        "localhost");
    
    BatchResultWriter resultWriter = getSuccessResultWriter();
    resultWriter.jobExecuted(result, null);
    
    assertEquals(0, resultWriter.getNumRiskRows());
    assertEquals(1, resultWriter.getNumRiskFailureRows());
    assertEquals(0, resultWriter.getNumRiskFailureReasonRows());
    assertEquals(0, resultWriter.getNumRiskComputeFailureRows());
  }
  
  @Test
  public void successfulAndFailedResultOnSameTarget() {
    CalculationJobResultItem successItem = new CalculationJobResultItem(
        new CalculationJobItem("function1", 
            _mockFunction.getTarget().toSpecification(),
            Collections.<ValueSpecification>emptySet(),
            Collections.singleton(new ValueRequirement("OUTPUT1", _mockFunction.getTarget().toSpecification()))));
    
    CalculationJobResultItem failedItem = new CalculationJobResultItem(
        new CalculationJobItem("function1", 
            _mockFunction.getTarget().toSpecification(),
            Collections.<ValueSpecification>emptySet(),
            Collections.singleton(new ValueRequirement("OUTPUT2", _mockFunction.getTarget().toSpecification()))),
            new RuntimeException("function execution failed"));
    
    ArrayList<CalculationJobResultItem> items = new ArrayList<CalculationJobResultItem>();
    items.add(successItem);
    items.add(failedItem);
    
    CalculationJobResult result = new CalculationJobResult(
        _calcJob.getSpecification(),
        200,
        items,
        "localhost");
    
    BatchResultWriter resultWriter = getSuccessResultWriter();
    resultWriter.jobExecuted(result, null);
    
    // note - success row not written
    assertEquals(0, resultWriter.getNumRiskRows());
    assertEquals(2, resultWriter.getNumRiskFailureRows());
    assertEquals(1, resultWriter.getNumRiskFailureReasonRows());
    assertEquals(1, resultWriter.getNumRiskComputeFailureRows());
  }
  
  
  
  // --------------------------------------------------------------------------
  
  
  
  @Override
  public Class<?>[] getHibernateMappingClasses() {
    return BatchDbManagerImpl.getHibernateMappingClasses();
  }
  
  private RiskValue getValueFromDb(BatchResultWriter resultWriter) {
    return resultWriter.getValue(
        AbstractCalculationNodeTest.CALC_CONF_NAME, 
        _mockFunction.getResultSpec().getRequirementSpecification().getValueName(), 
        _mockFunction.getTarget().toSpecification());
  }
  
  private void putOutputToCache() {
    ComputedValue output = _mockFunction.getResult();
    putValue(output);
  }
  
  private void putValue(ComputedValue value) {
    ViewComputationCache cache = getCache();
    cache.putValue(value);
  }

  private ViewComputationCache getCache() {
    ViewComputationCache cache = _calcNode.getCache(_calcJob.getSpecification());
    return cache;
  }
  
}
