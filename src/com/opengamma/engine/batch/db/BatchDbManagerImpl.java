/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.batch.db;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.Set;

import javax.time.Instant;
import javax.time.calendar.LocalDate;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.batch.BatchJob;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.util.time.DateUtil;

/**
 * This implementation uses Hibernate to write all static data, including snapshots.
 * <p>
 * Risk itself is written using direct JDBC, however.
 */
public class BatchDbManagerImpl implements BatchDbManager {
  
  private HibernateTemplate _hibernateTemplate;

  public void setSessionFactory(SessionFactory sessionFactory) {
    _hibernateTemplate = new HibernateTemplate(sessionFactory);
  }
  
  // --------------------------------------------------------------------------
  
  private OpenGammaVersion getOpenGammaVersion(final BatchJob job) {
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
  
  
  private ObservationTime getObservationTime(final BatchJob job) {
    ObservationTime observationTime = (ObservationTime) _hibernateTemplate.execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException,
          SQLException {
        Query query = session.getNamedQuery("ObservationTime.one.byLabel");
        query.setString("label", job.getObservationTime());
        return query.uniqueResult();
      }
    });
    if (observationTime == null) {
      observationTime = new ObservationTime();
      observationTime.setLabel(job.getObservationTime());
      _hibernateTemplate.save(observationTime);
    }
    return observationTime;
  }
 
  
  private ObservationDateTime getObservationDateTime(final BatchJob job) {
    ObservationDateTime dateTime = (ObservationDateTime) _hibernateTemplate.execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException,
          SQLException {
        Query query = session.getNamedQuery("ObservationDateTime.one.byDateAndTime");
        query.setDate("date", DateUtil.toSqlDate(job.getObservationDate()));
        query.setString("time", job.getObservationTime());
        return query.uniqueResult();
      }
    });
    if (dateTime == null) {
      dateTime = new ObservationDateTime();
      dateTime.setDate(DateUtil.toSqlDate(job.getObservationDate()));
      dateTime.setObservationTime(getObservationTime(job));
    }
    return dateTime; 
  }
 
  
  private ComputeHost getLocalComputeHost() {
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
 
  
  private LiveDataSnapshot getLiveDataSnapshot(final BatchJob job) {
    LiveDataSnapshot liveDataSnapshot = (LiveDataSnapshot) _hibernateTemplate.execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException,
          SQLException {
        Query query = session.getNamedQuery("LiveDataSnapshot.one.byDateAndTime");
        query.setDate("date", DateUtil.toSqlDate(job.getSnapshotObservationDate()));
        query.setString("time", job.getSnapshotObservationTime());
        return query.uniqueResult();
      }
    });
    
    if (liveDataSnapshot == null) {
      throw new OpenGammaRuntimeException("Snapshot for date " 
          + job.getSnapshotObservationDate() 
          + " and time " 
          + job.getSnapshotObservationTime() 
          + " cannot be found");
    }
    
    if (!liveDataSnapshot.isComplete()) {
      throw new OpenGammaRuntimeException(liveDataSnapshot + " is not yet complete.");
    }
    
    return liveDataSnapshot;
  }
 
  
  private RiskRun getRiskRunFromDb(final BatchJob job) {
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
  
  private RiskRun createRiskRun(final BatchJob job) {
    Instant now = Instant.nowSystemClock();

    RiskRun riskRun = new RiskRun();
    riskRun.setOpenGammaVersion(getOpenGammaVersion(job));
    riskRun.setMasterProcessHost(getLocalComputeHost());
    riskRun.setRunReason(job.getRunReason());
    riskRun.setRunTime(getObservationDateTime(job));
    riskRun.setValuationTime(DateUtil.toSqlTimestamp(job.getValuationTime()));
    riskRun.setViewOid(job.getViewOid());
    riskRun.setViewVersion(job.getViewVersion());
    riskRun.setLiveDataSnapshot(getLiveDataSnapshot(job));
    riskRun.setCreateInstant(DateUtil.toSqlTimestamp(now));
    
    for (ViewCalculationConfiguration calcConf : job.getView().getDefinition().getAllCalculationConfigurations()) {
      riskRun.addCalculationConfiguration(calcConf);
    }
    
    return riskRun;
  }
  
  private void startRun(RiskRun riskRun) {
    Instant now = Instant.nowSystemClock();
    
    riskRun.setStartInstant(DateUtil.toSqlTimestamp(now));
    riskRun.setComplete(false);
    
    _hibernateTemplate.saveOrUpdate(riskRun);
  }
  
  private void endRun(RiskRun riskRun) {
    Instant now = Instant.nowSystemClock();
    
    riskRun.setEndInstant(DateUtil.toSqlTimestamp(now));
    riskRun.setComplete(true);
    
    _hibernateTemplate.update(riskRun);
  }
  
  private RiskRun getRiskRunFromHandle(BatchJob job) {
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
        
    
  }

  @Override
  public void createLiveDataSnapshot(LocalDate observationDate, String observationTime) {
    
    
  }

  @Override
  public void endBatch(BatchJob batch) {
    RiskRun run = getRiskRunFromHandle(batch);
    endRun(run);
  }

  @Override
  public void fixLiveDataSnapshotTime(LocalDate observationDate, String observationTime, Instant fix) {
    
    
  }

  @Override
  public void markLiveDataSnapshotComplete(LocalDate observationDate, String observationTime) {
    
    
  }

  @Override
  public void startBatch(BatchJob batch) {
    RiskRun run = getRiskRunFromDb(batch);
    if (run == null) {
      run = createRiskRun(batch);
    }
    startRun(run);
    batch.setDbHandle(run);
  }

  @Override
  public void write(BatchJob batch, ViewComputationResultModel result) {
    
    
    
  }
  
}
