/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.batch.db;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import javax.time.Instant;
import javax.time.calendar.LocalDate;
import javax.time.calendar.OffsetTime;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.batch.BatchJob;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.id.Identifier;
import com.opengamma.util.time.DateUtil;

/**
 * This implementation uses Hibernate to write all static data, including LiveData snapshots.
 * <p>
 * Risk itself is written using direct JDBC, however.
 */
public class BatchDbManagerImpl implements BatchDbManager {
  
  private static final Logger s_logger = LoggerFactory
    .getLogger(BatchDbManagerImpl.class);
  
  private HibernateTemplate _hibernateTemplate;

  public void setSessionFactory(SessionFactory sessionFactory) {
    _hibernateTemplate = new HibernateTemplate(sessionFactory);
  }
  
  // --------------------------------------------------------------------------
  
  /*package*/ OpenGammaVersion getOpenGammaVersion(final BatchJob job) {
    OpenGammaVersion version = (OpenGammaVersion) _hibernateTemplate.execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException,
          SQLException {
        Query query = session.getNamedQuery("OpenGammaVersion.one.byVersionAndHash");
        query.setString("version", job.getOpenGammaVersion());
        query.setString("hash", job.getOpenGammaVersionHash());
        return query.uniqueResult();
      }
    });
    if (version == null) {
      version = new OpenGammaVersion();
      version.setVersion(job.getOpenGammaVersion());
      version.setHash(job.getOpenGammaVersionHash());
      _hibernateTemplate.save(version);
    }
    return version;
  }
  
  
  /*package*/ ObservationTime getObservationTime(final BatchJob job) {
    return getObservationTime(job.getObservationTime());
  }
  
  /*package*/ ObservationTime getObservationTime(final String label) {
    ObservationTime observationTime = (ObservationTime) _hibernateTemplate.execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException,
          SQLException {
        Query query = session.getNamedQuery("ObservationTime.one.byLabel");
        query.setString("label", label);
        return query.uniqueResult();
      }
    });
    if (observationTime == null) {
      observationTime = new ObservationTime();
      observationTime.setLabel(label);
      _hibernateTemplate.save(observationTime);
    }
    return observationTime;
  }
 
  
  /*package*/ ObservationDateTime getObservationDateTime(final BatchJob job) {
    return getObservationDateTime(job.getObservationDate(), job.getObservationTime());     
  }
  
  /*package*/ ObservationDateTime getObservationDateTime(final LocalDate observationDate, final String observationTime) {
    ObservationDateTime dateTime = (ObservationDateTime) _hibernateTemplate.execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException,
          SQLException {
        Query query = session.getNamedQuery("ObservationDateTime.one.byDateAndTime");
        query.setDate("date", DateUtil.toSqlDate(observationDate));
        query.setString("time", observationTime);
        return query.uniqueResult();
      }
    });
    if (dateTime == null) {
      dateTime = new ObservationDateTime();
      dateTime.setDate(DateUtil.toSqlDate(observationDate));
      dateTime.setObservationTime(getObservationTime(observationTime));
      _hibernateTemplate.save(dateTime);
    }
    return dateTime;
  }
 
  
  /*package*/ ComputeHost getLocalComputeHost() {
    String tempHostName;
    try {
      tempHostName = InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException e) {
      throw new OpenGammaRuntimeException("Cannot obtain local host name", e);
    }
    final String hostName = tempHostName;
    
    ComputeHost computeHost = (ComputeHost) _hibernateTemplate.execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException,
          SQLException {
        Query query = session.getNamedQuery("ComputeHost.one.byHostName");
        query.setString("hostName", hostName);
        return query.uniqueResult();
      }
    });
    if (computeHost == null) {
      computeHost = new ComputeHost();
      computeHost.setHostName(hostName);
      _hibernateTemplate.save(computeHost);
    }
    return computeHost;
  }
  
  /*package*/ ComputeNode getLocalComputeNode() {
    final ComputeHost host = getLocalComputeHost();
    
    // todo
    ComputeNode node = (ComputeNode) _hibernateTemplate.execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException,
          SQLException {
        Query query = session.getNamedQuery("ComputeNode.one.byHostName");
        query.setString("hostName", host.getHostName());
        return query.uniqueResult();
      }
    });
    if (node == null) {
      node = new ComputeNode();
      node.setComputeHost(host);
      node.setConfigOid(1);
      node.setConfigVersion(1);
      node.setNodeName(host.getHostName());
      _hibernateTemplate.save(node);
    }
    return node;
  }
  
  /*package*/ LiveDataSnapshot getLiveDataSnapshot(final BatchJob job) {

    LiveDataSnapshot liveDataSnapshot = getLiveDataSnapshot(
        job.getSnapshotObservationDate(),
        job.getSnapshotObservationTime());
    
    if (liveDataSnapshot == null) {
      throw new IllegalArgumentException("Snapshot for " 
          + job.getSnapshotObservationDate() 
          + "/" 
          + job.getSnapshotObservationTime() 
          + " cannot be found");
    }
    
    return liveDataSnapshot;
  }
  
  /*package*/ LiveDataSnapshot getLiveDataSnapshot(final LocalDate observationDate, final String observationTime) {
    LiveDataSnapshot liveDataSnapshot = (LiveDataSnapshot) _hibernateTemplate.execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException,
          SQLException {
        Query query = session.getNamedQuery("LiveDataSnapshot.one.byDateAndTime");
        query.setDate("date", DateUtil.toSqlDate(observationDate));
        query.setString("time", observationTime);
        return query.uniqueResult();
      }
    });
    return liveDataSnapshot;
  }
  
  /*package*/ LiveDataField getLiveDataField(final String fieldName) {
    LiveDataField field = (LiveDataField) _hibernateTemplate.execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException,
          SQLException {
        Query query = session.getNamedQuery("LiveDataField.one.byName");
        query.setString("name", fieldName);
        return query.uniqueResult();
      }
    });
    if (field == null) {
      field = new LiveDataField();
      field.setName(fieldName);
      _hibernateTemplate.save(field);
    }
    return field;
  }
  
  /*package*/ LiveDataIdentifier getLiveDataIdentifier(final Identifier identifier) {
    LiveDataIdentifier liveDataIdentifier = (LiveDataIdentifier) _hibernateTemplate.execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException,
          SQLException {
        Query query = session.getNamedQuery("LiveDataIdentifier.one.bySchemeAndValue");
        query.setString("scheme", identifier.getScheme().getName());
        query.setString("value", identifier.getValue());
        return query.uniqueResult();
      }
    });
    if (liveDataIdentifier == null) {
      liveDataIdentifier = new LiveDataIdentifier();
      liveDataIdentifier.setScheme(identifier.getScheme().getName());      
      liveDataIdentifier.setValue(identifier.getValue());
      _hibernateTemplate.save(liveDataIdentifier);
    }
    return liveDataIdentifier;
  }
  
  /*package*/ RiskRun getRiskRunFromDb(final BatchJob job) {
    RiskRun riskRun = null;
    
    if (job.isForceNewRun() == false) {
      riskRun = (RiskRun) _hibernateTemplate.execute(new HibernateCallback() {
        @Override
        public Object doInHibernate(Session session) throws HibernateException,
            SQLException {
          Query query = session.getNamedQuery("RiskRun.one.byViewAndRunTime");
          query.setInteger("viewOid", job.getViewOid());
          query.setInteger("viewVersion", job.getViewVersion());
          query.setDate("runDate", DateUtil.toSqlDate(job.getObservationDate()));
          query.setString("runTime", job.getObservationTime());
          return query.uniqueResult();
        }
      });
    }
    
    return riskRun;
  }
  
  /*package*/ RiskRun createRiskRun(final BatchJob job) {
    Instant now = Instant.nowSystemClock();
    
    LiveDataSnapshot snapshot = getLiveDataSnapshot(job);
    if (!snapshot.isComplete()) {
      throw new IllegalStateException(snapshot + " is not yet complete.");
    }
    
    RiskRun riskRun = new RiskRun();
    riskRun.setOpenGammaVersion(getOpenGammaVersion(job));
    riskRun.setMasterProcessHost(getLocalComputeHost());
    riskRun.setRunReason(job.getRunReason());
    riskRun.setRunTime(getObservationDateTime(job));
    riskRun.setValuationTime(DateUtil.toSqlTimestamp(job.getValuationTime()));
    riskRun.setViewOid(job.getViewOid());
    riskRun.setViewVersion(job.getViewVersion());
    riskRun.setLiveDataSnapshot(snapshot);
    riskRun.setCreateInstant(DateUtil.toSqlTimestamp(now));
    riskRun.setStartInstant(DateUtil.toSqlTimestamp(now));
    riskRun.setComplete(false);
    
    for (ViewCalculationConfiguration calcConf : job.getCalculationConfigurations()) {
      riskRun.addCalculationConfiguration(calcConf);
    }
    
    _hibernateTemplate.save(riskRun);
    return riskRun;
  }
  
  /*package*/ void restartRun(RiskRun riskRun) {
    Instant now = Instant.nowSystemClock();
    
    riskRun.setStartInstant(DateUtil.toSqlTimestamp(now));
    riskRun.setComplete(false);
    
    _hibernateTemplate.update(riskRun);
  }
  
  /*package*/ void endRun(RiskRun riskRun) {
    Instant now = Instant.nowSystemClock();
    
    riskRun.setEndInstant(DateUtil.toSqlTimestamp(now));
    riskRun.setComplete(true);
    
    _hibernateTemplate.update(riskRun);
  }
  
  /*package*/ RiskRun getRiskRunFromHandle(BatchJob job) {
    Object handle = job.getDbHandle();
    if (handle == null) {
      throw new IllegalStateException("Job db handle is null");
    }
    if (!(handle instanceof RiskRun)) {
      throw new IllegalStateException("Job db handle must be of type RiskRun, was " + handle.getClass());
    }
    return (RiskRun) handle;
  }
  
  // --------------------------------------------------------------------------
  

  @Override
  public void addValuesToSnapshot(LocalDate observationDate, String observationTime, Set<LiveDataValue> values) {
    LiveDataSnapshot snapshot = getLiveDataSnapshot(observationDate, observationTime);
    if (snapshot == null) {
      throw new IllegalArgumentException("Snapshot for " + observationTime + "/" + observationTime + " does not exist.");
    }
    if (snapshot.isComplete()) {
      throw new IllegalStateException("Snapshot for " + observationTime + "/" + observationTime + " is already complete.");
    }
    
    Collection<LiveDataSnapshotEntry> changedEntries = new ArrayList<LiveDataSnapshotEntry>();
    for (LiveDataValue value : values) {
      LiveDataSnapshotEntry entry = snapshot.getEntry(value.getIdentifier(), value.getFieldName());
      if (entry != null) {
        if (entry.getValue() != value.getValue()) {
          entry.setValue(value.getValue());
          changedEntries.add(entry);
        }
      } else {
        entry = new LiveDataSnapshotEntry();
        entry.setSnapshot(snapshot);
        entry.setIdentifier(getLiveDataIdentifier(value.getIdentifier()));
        entry.setField(getLiveDataField(value.getFieldName()));
        entry.setValue(value.getValue());
        snapshot.addEntry(entry);
        changedEntries.add(entry);
      }
    }
    _hibernateTemplate.saveOrUpdateAll(changedEntries);
  }

  @Override
  public void createLiveDataSnapshot(LocalDate observationDate, String observationTime) {
    LiveDataSnapshot snapshot = getLiveDataSnapshot(observationDate, observationTime);
    if (snapshot != null) {
      s_logger.info("Snapshot for " + observationTime + "/" + observationTime + " already exists. No need to create.");
      return;
    }
    
    snapshot = new LiveDataSnapshot();
    snapshot.setComplete(false);
    
    ObservationDateTime snapshotTime = getObservationDateTime(observationDate, observationTime);
    snapshot.setSnapshotTime(snapshotTime);
    
    _hibernateTemplate.save(snapshot);
  }

  @Override
  public void endBatch(BatchJob batch) {
    RiskRun run = getRiskRunFromHandle(batch);
    endRun(run);
  }

  @Override
  public void fixLiveDataSnapshotTime(LocalDate observationDate, String observationTime, OffsetTime fix) {
    LiveDataSnapshot snapshot = getLiveDataSnapshot(observationDate, observationTime);
    
    if (snapshot == null) {
      throw new IllegalArgumentException("Snapshot for " 
          + observationDate 
          + "/" 
          + observationTime 
          + " cannot be found");
    }
    
    snapshot.getSnapshotTime().setTime(DateUtil.toSqlTime(fix));
    _hibernateTemplate.save(snapshot);
  }

  @Override
  public void markLiveDataSnapshotComplete(LocalDate observationDate, String observationTime) {
    LiveDataSnapshot snapshot = getLiveDataSnapshot(observationDate, observationTime);
    
    if (snapshot == null) {
      throw new IllegalArgumentException("Snapshot for " 
          + observationDate 
          + "/" 
          + observationTime 
          + " cannot be found");
    }
    
    if (snapshot.isComplete()) {
      throw new IllegalStateException(snapshot + " is already complete.");
    }
    
    snapshot.setComplete(true);
    _hibernateTemplate.save(snapshot);
  }

  @Override
  public void startBatch(BatchJob batch) {
    RiskRun run = getRiskRunFromDb(batch);
    if (run == null) {
      run = createRiskRun(batch);
    } else {
      restartRun(run);
    }
    batch.setDbHandle(run);
  }

  @Override
  public void write(BatchJob batch, ViewComputationResultModel result) {
    
    
    
  }
  
  public static Class<?>[] getHibernateMappingClasses() {
    return new Class[] {
      CalculationConfiguration.class,
      ComputeHost.class,
      ComputeNode.class,
      LiveDataField.class,
      LiveDataIdentifier.class,
      LiveDataSnapshot.class,
      LiveDataSnapshotEntry.class,
      ObservationDateTime.class,
      ObservationTime.class,
      OpenGammaVersion.class,
      RiskComputeFailure.class,
      RiskRun.class,
      RiskValueName.class
    };
  }
  
}
