/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.batch.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.time.calendar.OffsetTime;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.batch.BatchJob;
import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.test.HibernateTest;
import com.opengamma.util.time.DateUtil;

/**
 * 
 */
public class BatchDbManagerImplTest extends HibernateTest {
    
    private PlatformTransactionManager _transactionManager;
    private TransactionStatus _transaction;
    private BatchDbManagerImpl _dbManager;
    private BatchJob _batchJob;
    
    public BatchDbManagerImplTest(String databaseType, final String databaseVersion) {
      super(databaseType, databaseVersion);
    }
    
    @Override
    public Class<?>[] getHibernateMappingClasses() {
      return BatchDbManagerImpl.getHibernateMappingClasses();
    }

    @Before
    public void setUp() throws Exception {
      super.setUp();
      
      _transactionManager = new HibernateTransactionManager(getSessionFactory());
      _transaction = _transactionManager.getTransaction(new DefaultTransactionDefinition());
      
      _dbManager = new BatchDbManagerImpl();
      _dbManager.setSessionFactory(getSessionFactory());
      
      _batchJob = new BatchJob();
      _batchJob.setBatchDbManager(_dbManager);
      _batchJob.setViewName("test_view");
      _batchJob.init();
   }
    
    @After
    public void tearDown() throws Exception {
      if (_transaction != null && !_transaction.isRollbackOnly()) {
        _transactionManager.commit(_transaction);
      }
    }
    
    @Test
    public void getVersion() {
      // create
      OpenGammaVersion version1 = _dbManager.getOpenGammaVersion(_batchJob);
      assertNotNull(version1);
      assertEquals(_batchJob.getOpenGammaVersion(), version1.getVersion());
      assertEquals(_batchJob.getOpenGammaVersionHash(), version1.getHash());
      
      // get
      OpenGammaVersion version2 = _dbManager.getOpenGammaVersion(_batchJob);
      assertEquals(version1, version2);
    }
    
    @Test
    public void getObservationTime() {
      // create
      ObservationTime time1 = _dbManager.getObservationTime(_batchJob);
      assertNotNull(time1);
      assertEquals(_batchJob.getObservationTime(), time1.getLabel());
      
      // get
      ObservationTime time2 = _dbManager.getObservationTime(_batchJob);
      assertEquals(time1, time2);
    }
    
    @Test
    public void getObservationDateTime() {
      // create
      ObservationDateTime datetime1 = _dbManager.getObservationDateTime(_batchJob);
      assertNotNull(datetime1);
      assertEquals(DateUtil.toSqlDate(_batchJob.getObservationDate()), datetime1.getDate());
      assertEquals(_batchJob.getObservationTime(), datetime1.getObservationTime().getLabel());
      
      // get
      ObservationDateTime datetime2 = _dbManager.getObservationDateTime(_batchJob);
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
      assertEquals(1, node1.getConfigOid());
      assertEquals(1, node1.getConfigVersion());
      assertEquals(InetAddress.getLocalHost().getHostName(), node1.getNodeName());
      
      // get
      ComputeNode node2 = _dbManager.getLocalComputeNode();
      assertEquals(node1, node2);
    }
    
    @Test
    public void getNonExistentRiskRunFromDb() {
      RiskRun run = _dbManager.getRiskRunFromDb(_batchJob);
      assertNull(run);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void tryToGetNonExistentLiveDataSnapshot() {
      _dbManager.getLiveDataSnapshot(_batchJob);
    }
    
    @Test
    public void markSnapshotComplete() {
      _dbManager.createLiveDataSnapshot(
          _batchJob.getSnapshotObservationDate(), 
          _batchJob.getSnapshotObservationTime());
      
      LiveDataSnapshot snapshot = _dbManager.getLiveDataSnapshot(_batchJob);
      assertNotNull(snapshot);
      assertEquals(DateUtil.toSqlDate(_batchJob.getSnapshotObservationDate()), 
          snapshot.getSnapshotTime().getDate());
      assertEquals(_batchJob.getSnapshotObservationTime(), 
          snapshot.getSnapshotTime().getObservationTime().getLabel());
      assertFalse(snapshot.isComplete());
      assertTrue(snapshot.getSnapshotEntries().isEmpty());

      _dbManager.markLiveDataSnapshotComplete(
          _batchJob.getSnapshotObservationDate(), 
          _batchJob.getSnapshotObservationTime());

      assertTrue(snapshot.isComplete());
    }
    
    @Test(expected=IllegalArgumentException.class) 
    public void tryToMarkNonExistentSnapshotComplete() {
      _dbManager.markLiveDataSnapshotComplete(
          _batchJob.getSnapshotObservationDate(), 
          _batchJob.getSnapshotObservationTime());
    }
    
    @Test(expected=IllegalStateException.class) 
    public void tryToMarkSnapshotCompleteTwice() {
      _dbManager.createLiveDataSnapshot(
          _batchJob.getSnapshotObservationDate(), 
          _batchJob.getSnapshotObservationTime());

      _dbManager.markLiveDataSnapshotComplete(
          _batchJob.getSnapshotObservationDate(), 
          _batchJob.getSnapshotObservationTime());
      
      _dbManager.markLiveDataSnapshotComplete(
          _batchJob.getSnapshotObservationDate(), 
          _batchJob.getSnapshotObservationTime());
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
    
    @Test 
    public void getLiveDataIdentifier() {
      Identifier identifier = new Identifier("BUID", "EQ12345");
      
      // create
      LiveDataIdentifier id1 = _dbManager.getLiveDataIdentifier(identifier);
      assertNotNull(id1);
      assertEquals(identifier.getScheme().getName(), id1.getScheme());
      assertEquals(identifier.getValue(), id1.getValue());
      
      // get
      LiveDataIdentifier id2 = _dbManager.getLiveDataIdentifier(identifier);
      assertEquals(id1, id2);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void addValuesToNonexistentSnapshot() {
      _dbManager.addValuesToSnapshot(_batchJob.getSnapshotObservationDate(), 
          _batchJob.getSnapshotObservationTime(), Collections.<LiveDataValue>emptySet());
    }
    
    @Test(expected=IllegalStateException.class)
    public void addValuesToCompleteSnapshot() {
      _dbManager.createLiveDataSnapshot(
          _batchJob.getSnapshotObservationDate(), 
          _batchJob.getSnapshotObservationTime());
            
      _dbManager.markLiveDataSnapshotComplete(
          _batchJob.getSnapshotObservationDate(), 
          _batchJob.getSnapshotObservationTime());
      
      _dbManager.addValuesToSnapshot(_batchJob.getSnapshotObservationDate(), 
          _batchJob.getSnapshotObservationTime(), Collections.<LiveDataValue>emptySet());
    }
    
    @Test
    public void addValuesToIncompleteSnapshot() {
      _dbManager.createLiveDataSnapshot(
          _batchJob.getSnapshotObservationDate(), 
          _batchJob.getSnapshotObservationTime());
      
      _dbManager.addValuesToSnapshot(_batchJob.getSnapshotObservationDate(), 
          _batchJob.getSnapshotObservationTime(), Collections.<LiveDataValue>emptySet());
      
      LiveDataSnapshot snapshot = _dbManager.getLiveDataSnapshot(_batchJob);
      assertNotNull(snapshot);
      assertEquals(0, snapshot.getSnapshotEntries().size());
      
      Set<Identifier> identifiers = Sets.newHashSet();
      identifiers.add(new Identifier("BUID", "EQ12345"));
      identifiers.add(new Identifier("BUID", "EQ12346"));
      identifiers.add(new Identifier("BUID", "EQ12347"));
      
      Set<LiveDataValue> values = new HashSet<LiveDataValue>();
      for (Identifier id : identifiers) {
        values.add(new LiveDataValue(id, "field_name", 123.45));
      }
      
      _dbManager.addValuesToSnapshot(_batchJob.getSnapshotObservationDate(), 
          _batchJob.getSnapshotObservationTime(), values);
      
      snapshot = _dbManager.getLiveDataSnapshot(_batchJob);
      assertEquals(identifiers.size(), snapshot.getSnapshotEntries().size());
      for (Identifier id : identifiers) {
        LiveDataSnapshotEntry entry = snapshot.getEntry(id, "field_name");
        assertNotNull(entry);
        assertEquals(snapshot, entry.getSnapshot());
        assertEquals(id, entry.getIdentifier().toOpenGammaIdentifier());
        assertEquals("field_name", entry.getField().getName());
        assertEquals(123.45, entry.getValue(), 0.000001);
      }
      
      // should not add anything extra
      _dbManager.addValuesToSnapshot(_batchJob.getSnapshotObservationDate(), 
          _batchJob.getSnapshotObservationTime(), values);
      snapshot = _dbManager.getLiveDataSnapshot(_batchJob);
      assertEquals(3, snapshot.getSnapshotEntries().size());
      
      // should update 1, add 1
      values = new HashSet<LiveDataValue>();
      values.add(new LiveDataValue(new Identifier("BUID", "EQ12347"), "field_name", 123.46));
      values.add(new LiveDataValue(new Identifier("BUID", "EQ12348"), "field_name", 123.45));
      
      _dbManager.addValuesToSnapshot(_batchJob.getSnapshotObservationDate(), 
          _batchJob.getSnapshotObservationTime(), values);
      snapshot = _dbManager.getLiveDataSnapshot(_batchJob);
      assertEquals(4, snapshot.getSnapshotEntries().size());
    }
    
    @Test
    public void fixLiveDataSnapshotTime() {
      _dbManager.createLiveDataSnapshot(
          _batchJob.getSnapshotObservationDate(), 
          _batchJob.getSnapshotObservationTime());
      
      _dbManager.fixLiveDataSnapshotTime(
          _batchJob.getSnapshotObservationDate(), 
          _batchJob.getSnapshotObservationTime(),
          OffsetTime.nowSystemClock());
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void tryToFixNonexistentLiveDataSnapshotTime() {
      _dbManager.fixLiveDataSnapshotTime(
          _batchJob.getSnapshotObservationDate(), 
          _batchJob.getSnapshotObservationTime(),
          OffsetTime.nowSystemClock());
    }
    
    @Test
    public void fixCompleteLiveDataSnapshotTime() {
      _dbManager.createLiveDataSnapshot(
          _batchJob.getSnapshotObservationDate(), 
          _batchJob.getSnapshotObservationTime());
      
      _dbManager.markLiveDataSnapshotComplete(
          _batchJob.getSnapshotObservationDate(), 
          _batchJob.getSnapshotObservationTime());
      
      // this is OK
      _dbManager.fixLiveDataSnapshotTime(
          _batchJob.getSnapshotObservationDate(), 
          _batchJob.getSnapshotObservationTime(),
          OffsetTime.nowSystemClock());
    }
    
    @Test
    public void createLiveDataSnapshotMultipleTimes() {
      _dbManager.createLiveDataSnapshot(
          _batchJob.getSnapshotObservationDate(), 
          _batchJob.getSnapshotObservationTime());
      
      _dbManager.createLiveDataSnapshot(
          _batchJob.getSnapshotObservationDate(), 
          _batchJob.getSnapshotObservationTime());
      
      assertNotNull(_dbManager.getLiveDataSnapshot(_batchJob));
    }
    
    @Test
    public void createThenGetRiskRun() {
      _dbManager.createLiveDataSnapshot(
          _batchJob.getSnapshotObservationDate(), 
          _batchJob.getSnapshotObservationTime());
      _dbManager.markLiveDataSnapshotComplete(
          _batchJob.getSnapshotObservationDate(), 
          _batchJob.getSnapshotObservationTime());
      RiskRun run = _dbManager.createRiskRun(_batchJob);
      
      assertNotNull(run);
      assertNotNull(run.getCreateInstant());
      assertNotNull(run.getStartInstant());
      assertNull(run.getEndInstant());
      assertTrue(run.getCalculationConfigurations().isEmpty());
      assertNotNull(run.getLiveDataSnapshot());
      assertEquals(_dbManager.getLocalComputeHost(), run.getMasterProcessHost());
      assertEquals(_dbManager.getOpenGammaVersion(_batchJob), run.getOpenGammaVersion());
      assertEquals(_batchJob.getRunReason(), run.getRunReason());
      assertNotNull(run.getValuationTime());
      assertEquals(_batchJob.getViewOid(), run.getViewOid());
      assertEquals(_batchJob.getViewVersion(), run.getViewVersion());
      
      // get
      RiskRun run2 = _dbManager.getRiskRunFromDb(_batchJob);
      assertEquals(run, run2);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void tryToStartBatchWithoutCreatingSnapshot() {
      _dbManager.startBatch(_batchJob);
    }
    
    @Test(expected=IllegalStateException.class)
    public void tryToStartBatchWithoutMarkingSnapshotComplete() {
      _dbManager.createLiveDataSnapshot(
          _batchJob.getSnapshotObservationDate(), 
          _batchJob.getSnapshotObservationTime());
      _dbManager.startBatch(_batchJob);
    }
    
    @Test
    public void startAndEndBatch() {
      _dbManager.createLiveDataSnapshot(
          _batchJob.getSnapshotObservationDate(), 
          _batchJob.getSnapshotObservationTime());
      _dbManager.markLiveDataSnapshotComplete(
          _batchJob.getSnapshotObservationDate(), 
          _batchJob.getSnapshotObservationTime());
      _dbManager.startBatch(_batchJob);
      
      RiskRun run1 = _dbManager.getRiskRunFromDb(_batchJob);
      assertNotNull(run1);
      assertNotNull(run1.getStartInstant());
      assertNull(run1.getEndInstant());
      
      RiskRun run2 = _dbManager.getRiskRunFromHandle(_batchJob);
      assertEquals(run1, run2);
      
      _dbManager.endBatch(_batchJob);
      
      run1 = _dbManager.getRiskRunFromDb(_batchJob);
      run2 = _dbManager.getRiskRunFromHandle(_batchJob);
      
      assertNotNull(run1);
      assertNotNull(run1.getStartInstant());
      assertNotNull(run1.getEndInstant());
      
      assertEquals(run1, run2);
    }
    
    @Test
    public void startBatchTwice() {
      _dbManager.createLiveDataSnapshot(
          _batchJob.getSnapshotObservationDate(), 
          _batchJob.getSnapshotObservationTime());
      _dbManager.markLiveDataSnapshotComplete(
          _batchJob.getSnapshotObservationDate(), 
          _batchJob.getSnapshotObservationTime());
      _dbManager.startBatch(_batchJob);
      _dbManager.startBatch(_batchJob);
    }
    
    @Test
    public void getComputationTarget() {
      UniqueIdentifier uid = UniqueIdentifier.of("foo", "bar");
      
      ComputationTarget portfolio = _dbManager.getComputationTarget(
          new ComputationTargetSpecification(ComputationTargetType.MULTIPLE_POSITIONS, uid));
      assertNotNull(portfolio);
      assertEquals(ComputationTargetType.MULTIPLE_POSITIONS.ordinal(), portfolio.getComputationTargetType());
      assertEquals(uid.getScheme(), portfolio.getIdScheme());
      assertEquals(uid.getValue(), portfolio.getIdValue());
      
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
    public void getValueName() {
      // create
      RiskValueName valueName1 = _dbManager.getRiskValueName("test_name");
      assertNotNull(valueName1);
      assertEquals("test_name", valueName1.getName());
      
      // get
      RiskValueName valueName2 = _dbManager.getRiskValueName("test_name");
      assertEquals(valueName1, valueName2);
    }
    
}
