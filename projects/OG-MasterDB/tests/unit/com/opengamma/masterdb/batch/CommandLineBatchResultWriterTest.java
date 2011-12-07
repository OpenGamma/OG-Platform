/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.time.Instant;

import org.springframework.orm.hibernate3.HibernateTemplate;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.google.common.collect.Sets;
import com.opengamma.core.position.impl.SimplePosition;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.function.EmptyFunctionParameters;
import com.opengamma.engine.test.CalculationNodeUtils;
import com.opengamma.engine.test.MockFunction;
import com.opengamma.engine.test.TestCalculationNode;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ResultModelDefinition;
import com.opengamma.engine.view.ResultOutputMode;
import com.opengamma.engine.view.cache.ViewComputationCache;
import com.opengamma.engine.view.calcnode.CalculationJob;
import com.opengamma.engine.view.calcnode.CalculationJobItem;
import com.opengamma.engine.view.calcnode.CalculationJobResult;
import com.opengamma.engine.view.calcnode.CalculationJobResultItem;
import com.opengamma.engine.view.calcnode.MissingInputException;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.util.db.DbDateUtils;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.HibernateTest;

/**
 * Test command line.
 */
public class CommandLineBatchResultWriterTest extends HibernateTest {

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
  private com.opengamma.masterdb.batch.ComputationTarget _dbComputationTarget;
  private Set<com.opengamma.masterdb.batch.ComputationTarget> _dbComputationTargets;
  private Set<RiskValueName> _valueNames;
  private Set<RiskValueRequirement> _valueRequirements;
  private Set<RiskValueSpecification> _valueSpecifications;

  private MockFunction _mockFunction;
  private TestCalculationNode _calcNode;
  private CalculationJob _calcJob;

  private HibernateTemplate _hibernateTemplate;

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public CommandLineBatchResultWriterTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
  }

  @BeforeMethod
  public void setUp() throws Exception {
    super.setUp();
    Instant now = Instant.now();

    _hibernateTemplate = new HibernateTemplate(getSessionFactory());
    _hibernateTemplate.setAllowCreate(false);

    getSessionFactory().getCurrentSession().beginTransaction();

    _openGammaVersion = new OpenGammaVersion();
    _openGammaVersion.setVersion("1.0");
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
    _computeNode.setNodeName("test-node");
    _hibernateTemplate.save(_computeNode);

    _liveDataSnapshot = new LiveDataSnapshot();
    _liveDataSnapshot.setSnapshotTime(_observationDateTime);
    _hibernateTemplate.save(_liveDataSnapshot);

    _riskRun = new RiskRun();
    _riskRun.setOpenGammaVersion(_openGammaVersion);
    _riskRun.setMasterProcessHost(_computeHost);
    _riskRun.setRunTime(_observationDateTime);
    _riskRun.setLiveDataSnapshot(_liveDataSnapshot);
    _riskRun.setCreateInstant(DbDateUtils.toSqlTimestamp(now));
    _riskRun.setStartInstant(DbDateUtils.toSqlTimestamp(now));
    _riskRun.setNumRestarts(0);
    _riskRun.setComplete(false);
    _hibernateTemplate.save(_riskRun);

    _calculationConfiguration = new CalculationConfiguration();
    _calculationConfiguration.setName(CalculationNodeUtils.CALC_CONF_NAME);
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

    _valueRequirements = new HashSet<RiskValueRequirement>();

    _valueRequirements.add(new RiskValueRequirement(ValueProperties.parse("currency=USD")));
    _valueRequirements.add(new RiskValueRequirement(ValueProperties.parse("currency=EUR")));
    _valueRequirements.add(new RiskValueRequirement(ValueProperties.parse("currency=GBP")));
    _hibernateTemplate.saveOrUpdateAll(_valueRequirements);

    _valueSpecifications = new HashSet<RiskValueSpecification>();
    _valueSpecifications.add(new RiskValueSpecification(ValueProperties.parse("currency=USD")));
    _valueSpecifications.add(new RiskValueSpecification(ValueProperties.parse("currency=EUR")));
    _valueSpecifications.add(new RiskValueSpecification(ValueProperties.parse("currency=GBP")));
    _hibernateTemplate.saveOrUpdateAll(_valueSpecifications);

    _mockFunctionComputationTarget = new com.opengamma.engine.ComputationTarget(ComputationTargetType.POSITION,
        new SimplePosition(
            UniqueId.of("Mock", "AAPL Stock UID"),
            new BigDecimal(500),
            ExternalId.of("Mock", "AAPL Stock ID")));
    _mockFunctionOutput = new Double(4000.50);
    _mockFunction = CalculationNodeUtils.getMockFunction(_mockFunctionComputationTarget, _mockFunctionOutput);

    _dbComputationTargets = new HashSet<com.opengamma.masterdb.batch.ComputationTarget>();
    _dbComputationTarget = new com.opengamma.masterdb.batch.ComputationTarget();
    _dbComputationTarget.setComputationTargetType(_mockFunction.getTarget().getType());
    _dbComputationTarget.setIdScheme(_mockFunction.getTarget().getUniqueId().getScheme());
    _dbComputationTarget.setIdValue(_mockFunction.getTarget().getUniqueId().getValue());
    _dbComputationTargets.add(_dbComputationTarget);
    _hibernateTemplate.saveOrUpdateAll(_dbComputationTargets);

    _calcNode = CalculationNodeUtils.getTestCalcNode(_mockFunction);
    _calcJob = CalculationNodeUtils.getCalculationJob(_mockFunction);

    getSessionFactory().getCurrentSession().getTransaction().commit();
  }

  //-------------------------------------------------------------------------
  private CommandLineBatchResultWriter getSuccessResultWriter() {
    CalculationJobResultItem item = new CalculationJobResultItem(_calcJob.getJobItems().get(0));
    CalculationJobResult result = new CalculationJobResult(
        _calcJob.getSpecification(),
        200,
        Collections.singletonList(item),
        "localhost");

    return getResultWriter(result);
  }

  private CommandLineBatchResultWriter getResultWriter(CalculationJobResult result) {

    Map<String, ViewComputationCache> cachesByCalculationConfiguration = new HashMap<String, ViewComputationCache>();
    cachesByCalculationConfiguration.put(CalculationNodeUtils.CALC_CONF_NAME, getCache());

    ResultModelDefinition resultModelDefinition = new ResultModelDefinition();
    resultModelDefinition.setAggregatePositionOutputMode(ResultOutputMode.ALL);
    resultModelDefinition.setPositionOutputMode(ResultOutputMode.ALL);
    resultModelDefinition.setSecurityOutputMode(ResultOutputMode.NONE);
    resultModelDefinition.setPrimitiveOutputMode(ResultOutputMode.NONE);
    CommandLineBatchResultWriter resultWriter = new CommandLineBatchResultWriter(
        getDbConnector(),
        resultModelDefinition,
        cachesByCalculationConfiguration,
        _dbComputationTargets,
        _riskRun,
        _valueNames,
        _valueRequirements,
        _valueSpecifications);
    resultWriter.initialize();

    return resultWriter;
  }

  private DependencyGraph getPositionDepGraph() {
    DependencyNode node = new DependencyNode(_mockFunction.getTarget());
    node.setFunction(_mockFunction);
    node.addOutputValues(_mockFunction.getResultSpecs());

    DependencyGraph graph = new DependencyGraph(CalculationNodeUtils.CALC_CONF_NAME);
    graph.addDependencyNode(node);
    return graph;
  }

  private DependencyGraph getPrimitiveDepGraph() {
    com.opengamma.engine.ComputationTarget primitiveTarget =
      new com.opengamma.engine.ComputationTarget(ComputationTargetType.PRIMITIVE, new String("foo"));

    MockFunction function = new MockFunction(primitiveTarget);

    DependencyNode node = new DependencyNode(primitiveTarget);
    node.setFunction(function);
    node.addOutputValues(_mockFunction.getResultSpecs());

    DependencyGraph graph = new DependencyGraph(CalculationNodeUtils.CALC_CONF_NAME);
    graph.addDependencyNode(node);
    return graph;
  }

  private void setIsRestart(CommandLineBatchResultWriter resultWriter) {
    _riskRun.setNumRestarts(1);
    resultWriter.setRestart(true);
  }


  // --------------------------------------------------------------------------

  @Test
  public void getComputeFailureFromDb() {
    CommandLineBatchResultWriter resultWriter1 = getSuccessResultWriter();

    StringBuffer longString = new StringBuffer();
    for (int i = 0; i < 2000; i++) {
      longString.append('f');
    }

    CalculationJobResultItem item = new CalculationJobResultItem(
        _calcJob.getJobItems().get(0),
        new RuntimeException(longString.toString()));

    resultWriter1.openSession();
    assertEquals(0, resultWriter1.getNumRiskComputeFailureRows());
    assertNotNull(resultWriter1.getComputeFailureFromDb(item));
    assertEquals(1, resultWriter1.getNumRiskComputeFailureRows());
    assertNotNull(resultWriter1.getComputeFailureFromDb(item));
    assertEquals(1, resultWriter1.getNumRiskComputeFailureRows());
    resultWriter1.closeSession();

    // writer with no in-memory cache
    CommandLineBatchResultWriter resultWriter2 = getSuccessResultWriter();

    resultWriter2.openSession();
    assertNotNull(resultWriter2.getComputeFailureFromDb(item));
    assertEquals(1, resultWriter2.getNumRiskComputeFailureRows());
    resultWriter2.closeSession();
  }

  @Test
  public void getComputeFailureFromDbNullMessage() {
    CommandLineBatchResultWriter resultWriter1 = getSuccessResultWriter();
    resultWriter1.initialize();

    CalculationJobResultItem item = new CalculationJobResultItem(
        _calcJob.getJobItems().get(0),
        new RuntimeException((String) null));

    resultWriter1.openSession();
    assertEquals(0, resultWriter1.getNumRiskComputeFailureRows());
    assertNotNull(resultWriter1.getComputeFailureFromDb(item));
    assertEquals(1, resultWriter1.getNumRiskComputeFailureRows());
    assertNotNull(resultWriter1.getComputeFailureFromDb(item));
    assertEquals(1, resultWriter1.getNumRiskComputeFailureRows());
    resultWriter1.closeSession();

    // writer with no in-memory cache
    CommandLineBatchResultWriter resultWriter2 = getSuccessResultWriter();
    resultWriter2.initialize();

    resultWriter2.openSession();
    assertNotNull(resultWriter2.getComputeFailureFromDb(item));
    assertEquals(1, resultWriter2.getNumRiskComputeFailureRows());
    resultWriter2.closeSession();
  }

  @Test
  public void notARestart() {
    // should execute

    CommandLineBatchResultWriter resultWriter = getSuccessResultWriter();
    DependencyGraph originalGraph = getPositionDepGraph();
    DependencyGraph graphToExecute = resultWriter.getGraphToExecute(originalGraph);
    assertEquals(originalGraph.getSize(), graphToExecute.getSize());
  }

  @Test
  public void restartButNoStatusEntryInDb() {
    // should re-execute

    CommandLineBatchResultWriter resultWriter = getSuccessResultWriter();
    setIsRestart(resultWriter);

    DependencyGraph originalGraph = getPositionDepGraph();
    DependencyGraph graphToExecute = resultWriter.getGraphToExecute(originalGraph);
    assertEquals(originalGraph.getSize(), graphToExecute.getSize());
  }

  @Test
  public void restartSuccessButOutputsNotInCache() {
    // should re-execute, but not write results into DB.

    CommandLineBatchResultWriter resultWriter = getSuccessResultWriter();
    setIsRestart(resultWriter);

    resultWriter.openSession();
    resultWriter.upsertStatusEntries(
        _calcJob.getSpecification(),
        StatusEntry.Status.SUCCESS,
        Sets.newHashSet(_dbComputationTarget.toComputationTargetSpec()));
    resultWriter.closeSession();

    DependencyGraph originalGraph = getPositionDepGraph();
    DependencyGraph graphToExecute = resultWriter.getGraphToExecute(originalGraph);
    assertEquals(originalGraph.getSize(), graphToExecute.getSize());
  }

  @Test
  public void restartSuccessOutputInCache() {
    // should not re-execute

    putOutputToCache();

    CommandLineBatchResultWriter resultWriter = getSuccessResultWriter();
    setIsRestart(resultWriter);

    resultWriter.openSession();
    resultWriter.upsertStatusEntries(
        _calcJob.getSpecification(),
        StatusEntry.Status.SUCCESS,
        Sets.newHashSet(_dbComputationTarget.toComputationTargetSpec()));
    resultWriter.closeSession();

    DependencyGraph originalGraph = getPositionDepGraph();
    DependencyGraph graphToExecute = resultWriter.getGraphToExecute(originalGraph);
    assertEquals(0, graphToExecute.getSize());
  }

  @Test
  public void restartFailed() {
    // should re-execute

    CommandLineBatchResultWriter resultWriter = getSuccessResultWriter();
    setIsRestart(resultWriter);

    resultWriter.openSession();
    resultWriter.upsertStatusEntries(
        _calcJob.getSpecification(),
        StatusEntry.Status.FAILURE,
        Sets.newHashSet(_dbComputationTarget.toComputationTargetSpec()));
    resultWriter.closeSession();

    DependencyGraph originalGraph = getPositionDepGraph();
    DependencyGraph graphToExecute = resultWriter.getGraphToExecute(originalGraph);
    assertEquals(originalGraph.getSize(), graphToExecute.getSize());
  }

  @Test
  public void restartRunning() {
    // should re-execute (assumption being that the previous batch attempt
    // was hard-killed while it was running and is no longer really running)

    CommandLineBatchResultWriter resultWriter = getSuccessResultWriter();
    setIsRestart(resultWriter);

    resultWriter.openSession();
    resultWriter.upsertStatusEntries(
        _calcJob.getSpecification(),
        StatusEntry.Status.RUNNING,
        Sets.newHashSet(_dbComputationTarget.toComputationTargetSpec()));
    resultWriter.closeSession();

    DependencyGraph originalGraph = getPositionDepGraph();
    DependencyGraph graphToExecute = resultWriter.getGraphToExecute(originalGraph);
    assertEquals(originalGraph.getSize(), graphToExecute.getSize());
  }

  @Test
  public void restartNotRunning() {
    // should re-execute

    CommandLineBatchResultWriter resultWriter = getSuccessResultWriter();
    setIsRestart(resultWriter);

    resultWriter.openSession();
    resultWriter.upsertStatusEntries(
        _calcJob.getSpecification(),
        StatusEntry.Status.NOT_RUNNING,
        Sets.newHashSet(_dbComputationTarget.toComputationTargetSpec()));
    resultWriter.closeSession();

    DependencyGraph originalGraph = getPositionDepGraph();
    DependencyGraph graphToExecute = resultWriter.getGraphToExecute(originalGraph);
    assertEquals(originalGraph.getSize(), graphToExecute.getSize());
  }

  @Test
  public void restartPrimitiveSuccess() {
    // should re-execute

    CommandLineBatchResultWriter resultWriter = getSuccessResultWriter();
    setIsRestart(resultWriter);

    DependencyGraph originalGraph = getPrimitiveDepGraph();
    DependencyGraph graphToExecute = resultWriter.getGraphToExecute(originalGraph);
    assertEquals(originalGraph.getSize(), graphToExecute.getSize());
  }

  @Test
  public void restartPrimitiveSuccessOutputsInCache() {
    // should not re-execute

    CommandLineBatchResultWriter resultWriter = getSuccessResultWriter();
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

    CommandLineBatchResultWriter resultWriter = getSuccessResultWriter();
    resultWriter.write(result, null);

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

    CommandLineBatchResultWriter resultWriter = getSuccessResultWriter();
    resultWriter.write(result, null);

    assertEquals(1, resultWriter.getNumRiskRows());
    RiskValue value = getValueFromDb(resultWriter);
    assertEquals(_mockFunction.getResult().getValue(), value.getValue());

    assertEquals(0, resultWriter.getNumRiskFailureRows());
    assertEquals(0, resultWriter.getNumRiskFailureReasonRows());
    assertEquals(0, resultWriter.getNumRiskComputeFailureRows());
  }

  @Test
  public void nonScalarFunctionWasSuccessful() {
    CalculationJobResultItem item = new CalculationJobResultItem(_calcJob.getJobItems().get(0));

    ComputedValue outputWithANonDoubleValue = new ComputedValue(
        _mockFunction.getResultSpec(),
        new DoubleMatrix1D(new double[] { 4.0, 5.0, 6.0 }));
    putValue(outputWithANonDoubleValue);

    CalculationJobResult result = new CalculationJobResult(
        _calcJob.getSpecification(),
        200,
        Collections.singletonList(item),
        "localhost");

    CommandLineBatchResultWriter resultWriter = getSuccessResultWriter();
    resultWriter.write(result, null);

    assertEquals(3, resultWriter.getNumRiskRows());

    RiskValue value = resultWriter.getValue(
        CalculationNodeUtils.CALC_CONF_NAME,
        _mockFunction.getResultSpec().getValueName() + "[0]",
        _mockFunction.getResultSpec().getProperties(),
        _mockFunction.getResultSpec().getProperties(),
        _mockFunction.getTarget().toSpecification());
    assertEquals(4.0, value.getValue(), 0.0000001);

    value = resultWriter.getValue(
        CalculationNodeUtils.CALC_CONF_NAME,
        _mockFunction.getResultSpec().getValueName() + "[1]",
        _mockFunction.getResultSpec().getProperties(),
        _mockFunction.getResultSpec().getProperties(),
        _mockFunction.getTarget().toSpecification());
    assertEquals(5.0, value.getValue(), 0.0000001);

    value = resultWriter.getValue(
        CalculationNodeUtils.CALC_CONF_NAME,
        _mockFunction.getResultSpec().getValueName() + "[2]",
        _mockFunction.getResultSpec().getProperties(),
        _mockFunction.getResultSpec().getProperties(),
        _mockFunction.getTarget().toSpecification());
    assertEquals(6.0, value.getValue(), 0.0000001);

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

    CommandLineBatchResultWriter resultWriter = getSuccessResultWriter();
    resultWriter.write(result, null);

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

    CommandLineBatchResultWriter resultWriter = getSuccessResultWriter();
    resultWriter.write(result, null);

    assertEquals(0, resultWriter.getNumRiskRows());
    assertEquals(1, resultWriter.getNumRiskFailureRows());
    assertEquals(1, resultWriter.getNumRiskFailureReasonRows());
    assertEquals(1, resultWriter.getNumRiskComputeFailureRows());
  }

  @Test
  public void functionExecutionThrewExceptionNullMessage() {
    CalculationJobResultItem item = new CalculationJobResultItem(
        _calcJob.getJobItems().get(0),
        new RuntimeException((String) null));

    CalculationJobResult result = new CalculationJobResult(
        _calcJob.getSpecification(),
        200,
        Collections.singletonList(item),
        "localhost");

    CommandLineBatchResultWriter resultWriter = getSuccessResultWriter();
    resultWriter.write(result, null);

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
            _mockFunction.getUniqueId()));

    ComputeFailureKey inputFailureKey = new ComputeFailureKey(
        item.getItem().getFunctionUniqueIdentifier(),
        item.getExceptionClass(),
        item.getExceptionMsg(),
        item.getStackTrace());

    CommandLineBatchResultWriter resultWriter = getSuccessResultWriter();

    resultWriter.openSession();
    ComputeFailure inputFailure = resultWriter.saveComputeFailure(inputFailureKey);
    resultWriter.closeSession();

    assertEquals(0, resultWriter.getNumRiskRows());
    assertEquals(0, resultWriter.getNumRiskFailureRows());
    assertEquals(0, resultWriter.getNumRiskFailureReasonRows());
    assertEquals(1, resultWriter.getNumRiskComputeFailureRows());

    CommandLineBatchResultWriter.BatchResultWriterFailure cachedInputFailure = new CommandLineBatchResultWriter.BatchResultWriterFailure();
    cachedInputFailure.addComputeFailureId(inputFailure.getId());
    ComputedValue cachedInputFailureValue = new ComputedValue(item.getItem().getInputs().iterator().next(), cachedInputFailure);
    putValue(cachedInputFailureValue);

    CalculationJobResult result = new CalculationJobResult(
        _calcJob.getSpecification(),
        200,
        Collections.singletonList(item),
        "localhost");

    resultWriter.write(result, null);

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
            _mockFunction.getUniqueId()));

    CalculationJobResult result = new CalculationJobResult(
        _calcJob.getSpecification(),
        200,
        Collections.singletonList(item),
        "localhost");

    CommandLineBatchResultWriter resultWriter = getSuccessResultWriter();
    resultWriter.write(result, null);

    assertEquals(0, resultWriter.getNumRiskRows());
    assertEquals(1, resultWriter.getNumRiskFailureRows());
    assertEquals(1, resultWriter.getNumRiskFailureReasonRows());
    assertEquals(1, resultWriter.getNumRiskComputeFailureRows());
  }

  @Test
  public void successfulAndFailedResultOnSameTarget() {
    CalculationJobResultItem successItem = new CalculationJobResultItem(
        new CalculationJobItem("function1",
            new EmptyFunctionParameters(),
            _mockFunction.getTarget().toSpecification(),
            Collections.<ValueSpecification>emptySet(),
            Collections.singleton(new ValueRequirement("OUTPUT1", _mockFunction.getTarget().toSpecification()))));

    CalculationJobResultItem failedItem = new CalculationJobResultItem(
        new CalculationJobItem("function1",
            new EmptyFunctionParameters(),
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

    CommandLineBatchResultWriter resultWriter = getSuccessResultWriter();
    resultWriter.write(result, null);

    // note - success row not written
    assertEquals(0, resultWriter.getNumRiskRows());
    assertEquals(2, resultWriter.getNumRiskFailureRows());
    assertEquals(1, resultWriter.getNumRiskFailureReasonRows());
    assertEquals(1, resultWriter.getNumRiskComputeFailureRows());
  }



  // --------------------------------------------------------------------------



  @Override
  public Class<?>[] getHibernateMappingClasses() {
    return DbBatchMaster.getHibernateMappingClasses();
  }

  private RiskValue getValueFromDb(CommandLineBatchResultWriter resultWriter) {
    return resultWriter.getValue(
        CalculationNodeUtils.CALC_CONF_NAME,
        _mockFunction.getResultSpec().getValueName(),
        _mockFunction.getResultSpec().getProperties(),
        _mockFunction.getResultSpec().getProperties(),
        _mockFunction.getTarget().toSpecification());
  }

  private void putOutputToCache() {
    ComputedValue output = _mockFunction.getResult();
    putValue(output);
  }

  private void putValue(ComputedValue value) {
    ViewComputationCache cache = getCache();
    cache.putSharedValue(value);
  }

  private ViewComputationCache getCache() {
    ViewComputationCache cache = _calcNode.getCache(_calcJob.getSpecification());
    return cache;
  }

}
