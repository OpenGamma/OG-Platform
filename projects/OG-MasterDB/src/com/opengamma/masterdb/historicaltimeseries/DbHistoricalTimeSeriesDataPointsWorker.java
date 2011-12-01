/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.historicaltimeseries;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.time.Duration;
import javax.time.Instant;
import javax.time.calendar.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import com.opengamma.DataNotFoundException;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.change.ChangeType;
import com.opengamma.extsql.ExtSqlBundle;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesGetFilter;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeries;
import com.opengamma.masterdb.AbstractDbMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.DbDateUtils;
import com.opengamma.util.db.DbMapSqlParameterSource;
import com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;


/**
 * A worker that provides the implementation of the data points part of the time-series master.
 * <p>
 * The time-series data points are effectively stored completely separately from the
 * information document about the time-series.
 * <p>
 * The SQL is stored externally in {@code DbHistoricalTimeSeriesMaster.extsql}.
 * Alternate databases or specific SQL requirements can be handled using database
 * specific overrides, such as {@code DbHistoricalTimeSeriesMaster-MySpecialDB.extsql}.
 * <p>
 * This class is mutable but must be treated as immutable after configuration.
 */
public class DbHistoricalTimeSeriesDataPointsWorker extends AbstractDbMaster {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(DbHistoricalTimeSeriesDataPointsWorker.class);

  /**
   * The prefix used for data point unique identifiers.
   */
  protected static final String DATA_POINT_PREFIX = "DP";

  /**
   * The master.
   */
  private DbHistoricalTimeSeriesMaster _master;

  /**
   * Creates an instance.
   * 
   * @param master  the database master, not null
   */
  public DbHistoricalTimeSeriesDataPointsWorker(final DbHistoricalTimeSeriesMaster master) {
    super(master.getDbConnector(), master.getUniqueIdScheme());
    _master = master;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the master.
   * 
   * @return the master, not null
   */
  protected DbHistoricalTimeSeriesMaster getMaster() {
    return _master;
  }

  /**
   * Gets the external SQL bundle.
   * 
   * @return the external SQL bundle, not null
   */
  protected ExtSqlBundle getExtSqlBundle() {
    return getMaster().getExtSqlBundle();
  }

  //-------------------------------------------------------------------------
  
  public ManageableHistoricalTimeSeries getTimeSeries(ObjectIdentifiable objectId, VersionCorrection versionCorrection, HistoricalTimeSeriesGetFilter filter) {
    final long oid = extractOid(objectId); 
    final VersionCorrection vc = versionCorrection.withLatestFixed(now());
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addValue("doc_oid", oid)
      .addTimestamp("version_as_of_instant", vc.getVersionAsOf())
      .addTimestamp("corrected_to_instant", vc.getCorrectedTo())
      .addValue("start_date", DbDateUtils.toSqlDateNullFarPast(filter.getEarliestDate()))
      .addValue("end_date", DbDateUtils.toSqlDateNullFarFuture(filter.getLatestDate()));
    final NamedParameterJdbcOperations namedJdbc = getDbConnector().getJdbcTemplate().getNamedParameterJdbcOperations();
    
    // get metadata
    final String sqlCommon = getExtSqlBundle().getSql("SelectDataPointsCommon", args);
    ManageableHistoricalTimeSeries result = namedJdbc.query(sqlCommon, args, new ManageableHTSExtractor(oid));
    if (result == null) {
      throw new DataNotFoundException("Unable to find time-series: " + objectId);
    }

    // set up limit on number of points to return
    if (filter.getMaxPoints() == null) {
      // return all points (limit all)
      args.addValue("order", "ASC");
    } else if (filter.getMaxPoints() > 0) {
      // return first few points
      args.addValue("paging_fetch", filter.getMaxPoints());
      args.addValue("order", "ASC");
    } else if (filter.getMaxPoints() < 0) {
      // return last few points
      args.addValue("paging_fetch", -filter.getMaxPoints());
      args.addValue("order", "DESC");
    } else {
      // Zero datapoints requested
      result.setTimeSeries(new ArrayLocalDateDoubleTimeSeries());
      return result;
    }

    // get data points
    if (filter.getLatestDate() == null || filter.getEarliestDate() == null || !filter.getLatestDate().isBefore(filter.getEarliestDate())) {
      final String sqlPoints = getExtSqlBundle().getSql("SelectDataPoints", args);
      LocalDateDoubleTimeSeries series = namedJdbc.query(sqlPoints, args, new DataPointsExtractor());
      result.setTimeSeries(series);
    } else {
      //TODO: this is a hack, most of the places that call with this condition want some kind of metadata, which it would be cheaper for us to expose specifically
      result.setTimeSeries(new ArrayLocalDateDoubleTimeSeries());
    }
    return result;
  }
    
  //-------------------------------------------------------------------------
  public UniqueId updateTimeSeriesDataPoints(final ObjectIdentifiable objectId, final LocalDateDoubleTimeSeries series) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(series, "series");
    s_logger.debug("add time-series data points to {}", objectId);
    
    // retry to handle concurrent conflicts
    for (int retry = 0; true; retry++) {
      final UniqueId uniqueId = resolveObjectId(objectId, VersionCorrection.LATEST);
      if (series.isEmpty()) {
        return uniqueId;
      }
      try {
        final Instant now = now();
        UniqueId resultId = getTransactionTemplate().execute(new TransactionCallback<UniqueId>() {
          @Override
          public UniqueId doInTransaction(final TransactionStatus status) {
            insertDataPointsCheckMaxDate(uniqueId, series);
            return insertDataPoints(uniqueId, series, now);
          }
        });
        getMaster().changeManager().entityChanged(ChangeType.UPDATED, uniqueId, resultId, now);
        return resultId;
      } catch (DataIntegrityViolationException ex) {
        if (retry == getMaster().getMaxRetries()) {
          throw ex;
        }
      } catch (DataAccessException ex) {
        throw fixSQLExceptionCause(ex);
      }
    }
  }

  /**
   * Checks the data points can be inserted.
   * 
   * @param uniqueId  the unique identifier, not null
   * @param series  the time-series data points, not empty, not null
   */
  protected void insertDataPointsCheckMaxDate(final UniqueId uniqueId, final LocalDateDoubleTimeSeries series) {
    final Long docOid = extractOid(uniqueId);
    final VersionCorrection vc = getMaster().extractTimeSeriesInstants(uniqueId);
    final DbMapSqlParameterSource queryArgs = new DbMapSqlParameterSource()
      .addValue("doc_oid", docOid)
      .addTimestamp("ver_instant", vc.getVersionAsOf())
      .addTimestamp("corr_instant", vc.getCorrectedTo());
    final String sql = getExtSqlBundle().getSql("SelectMaxPointDate", queryArgs);
    Date result = getDbConnector().getJdbcTemplate().queryForObject(sql, Date.class, queryArgs);
    if (result != null) {
      LocalDate maxDate = DbDateUtils.fromSqlDateAllowNull(result);
      if (series.getTimeAt(0).isAfter(maxDate) == false) {
        throw new IllegalArgumentException("Unable to update data points of time-series " + uniqueId +
            " as the update starts at " + series.getTimeAt(0) +
            " which is before the latest data point in the database at " + maxDate);
      }
    }
  }

  /**
   * Inserts the data points.
   * 
   * @param uniqueId  the unique identifier, not null
   * @param series  the time-series data points, not empty, not null
   * @param now  the current instant, not null
   * @return the unique identifier, not null
   */
  protected UniqueId insertDataPoints(final UniqueId uniqueId, final LocalDateDoubleTimeSeries series, final Instant now) {
    final Long docOid = extractOid(uniqueId);
    final Timestamp nowTS = DbDateUtils.toSqlTimestamp(now);
    final List<DbMapSqlParameterSource> argsList = new ArrayList<DbMapSqlParameterSource>();
    for (Entry<LocalDate, Double> entry : series) {
      LocalDate date = entry.getKey();
      Double value = entry.getValue();
      if (date == null || value == null) {
        throw new IllegalArgumentException("Time-series must not contain a null value");
      }
      final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
        .addValue("doc_oid", docOid)
        .addDate("point_date", date)
        .addValue("ver_instant", nowTS)
        .addValue("corr_instant", nowTS)
        .addValue("point_value", value);
      argsList.add(args);
    }
    final String sqlInsert = getExtSqlBundle().getSql("InsertDataPoint");
    getJdbcTemplate().batchUpdate(sqlInsert, argsList.toArray(new DbMapSqlParameterSource[argsList.size()]));
    return createTimeSeriesUniqueId(docOid, now, now);
  }

  //-------------------------------------------------------------------------
  public UniqueId correctTimeSeriesDataPoints(final ObjectIdentifiable objectId, final LocalDateDoubleTimeSeries series) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(series, "series");
    s_logger.debug("add time-series data points to {}", objectId);
    
    // retry to handle concurrent conflicts
    for (int retry = 0; true; retry++) {
      final UniqueId uniqueId = resolveObjectId(objectId, VersionCorrection.LATEST);
      if (series.isEmpty()) {
        return uniqueId;
      }
      try {
        final Instant now = now();
        UniqueId resultId = getTransactionTemplate().execute(new TransactionCallback<UniqueId>() {
          @Override
          public UniqueId doInTransaction(final TransactionStatus status) {
            return correctDataPoints(uniqueId, series, now);
          }
        });
        getMaster().changeManager().entityChanged(ChangeType.CORRECTED, uniqueId, resultId, now);
        return resultId;
      } catch (DataIntegrityViolationException ex) {
        if (retry == getMaster().getMaxRetries()) {
          throw ex;
        }
      } catch (DataAccessException ex) {
        throw fixSQLExceptionCause(ex);
      }
    }
  }

  /**
   * Corrects the data points.
   * 
   * @param uniqueId  the unique identifier, not null
   * @param series  the time-series data points, not empty, not null
   * @param now  the current instant, not null
   * @return the unique identifier, not null
   */
  protected UniqueId correctDataPoints(UniqueId uniqueId, LocalDateDoubleTimeSeries series, Instant now) {
    final Long docOid = extractOid(uniqueId);
    final Timestamp nowTS = DbDateUtils.toSqlTimestamp(now);
    final List<DbMapSqlParameterSource> argsList = new ArrayList<DbMapSqlParameterSource>();
    for (Entry<LocalDate, Double> entry : series) {
      LocalDate date = entry.getKey();
      Double value = entry.getValue();
      if (date == null || value == null) {
        throw new IllegalArgumentException("Time-series must not contain a null value");
      }
      final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
        .addValue("doc_oid", docOid)
        .addDate("point_date", date)
        .addValue("corr_instant", nowTS)
        .addValue("point_value", value);
      argsList.add(args);
    }
    final String sqlInsert = getExtSqlBundle().getSql("InsertCorrectDataPoint");
    getJdbcTemplate().batchUpdate(sqlInsert, argsList.toArray(new DbMapSqlParameterSource[argsList.size()]));
    return resolveObjectId(uniqueId, VersionCorrection.of(now, now));
  }

  //-------------------------------------------------------------------------
  public UniqueId removeTimeSeriesDataPoints(final ObjectIdentifiable objectId, final LocalDate fromDateInclusive, final LocalDate toDateInclusive) {
    ArgumentChecker.notNull(objectId, "objectId");
    if (fromDateInclusive != null && toDateInclusive != null) {
      ArgumentChecker.inOrderOrEqual(fromDateInclusive, toDateInclusive, "fromDateInclusive", "toDateInclusive");
    }
    s_logger.debug("removing time-series data points from {}", objectId);
    
    // retry to handle concurrent conflicts
    for (int retry = 0; true; retry++) {
      final UniqueId uniqueId = resolveObjectId(objectId, VersionCorrection.LATEST);
      try {
        final Instant now = now();
        UniqueId resultId = getTransactionTemplate().execute(new TransactionCallback<UniqueId>() {
          @Override
          public UniqueId doInTransaction(final TransactionStatus status) {
            return removeDataPoints(uniqueId, fromDateInclusive, toDateInclusive, now);
          }
        });
        getMaster().changeManager().entityChanged(ChangeType.UPDATED, uniqueId, resultId, now);
        return resultId;
      } catch (DataIntegrityViolationException ex) {
        if (retry == getMaster().getMaxRetries()) {
          throw ex;
        }
      } catch (DataAccessException ex) {
        throw fixSQLExceptionCause(ex);
      }
    }
  }

  /**
   * Removes data points.
   * 
   * @param uniqueId  the unique identifier, not null
   * @param fromDateInclusive  the start date to remove from, not null
   * @param toDateInclusive  the end date to remove to, not null
   * @param now  the current instant, not null
   * @return the unique identifier, not null
   */
  protected UniqueId removeDataPoints(UniqueId uniqueId, LocalDate fromDateInclusive, LocalDate toDateInclusive, Instant now) {
    final Long docOid = extractOid(uniqueId);
    // query dates to remove
    final DbMapSqlParameterSource queryArgs = new DbMapSqlParameterSource()
      .addValue("doc_oid", docOid)
      .addValue("start_date", DbDateUtils.toSqlDateNullFarPast(fromDateInclusive))
      .addValue("end_date", DbDateUtils.toSqlDateNullFarFuture(toDateInclusive));
    final String sqlRemove = getExtSqlBundle().getSql("SelectRemoveDataPoints");
    final List<Map<String, Object>> dates = getJdbcTemplate().queryForList(sqlRemove, queryArgs);
    // insert new rows to remove them
    final Timestamp nowTS = DbDateUtils.toSqlTimestamp(now);
    final List<DbMapSqlParameterSource> argsList = new ArrayList<DbMapSqlParameterSource>();
    for (Map<String, Object> date : dates) {
      final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
        .addValue("doc_oid", docOid)
        .addValue("point_date", date.get("POINT_DATE"))
        .addValue("corr_instant", nowTS)
        .addValue("point_value", null, Types.DOUBLE);
      argsList.add(args);
    }
    final String sqlInsert = getExtSqlBundle().getSql("InsertCorrectDataPoint");
    getJdbcTemplate().batchUpdate(sqlInsert, argsList.toArray(new DbMapSqlParameterSource[argsList.size()]));
    return resolveObjectId(uniqueId, VersionCorrection.of(now, now));
  }

  //-------------------------------------------------------------------------
  /**
   * Extracts the object row id from the object identifier.
   * 
   * @param objectId  the object identifier, not null
   * @return the date, null if no point date
   */
  @Override
  protected long extractOid(ObjectIdentifiable objectId) {
    return getMaster().extractOid(objectId);
  }

  /**
   * Creates a unique identifier.
   * 
   * @param oid  the object identifier
   * @param verInstant  the version instant, not null
   * @param corrInstant  the correction instant, not null
   * @return the unique identifier
   */
  protected UniqueId createTimeSeriesUniqueId(long oid, Instant verInstant, Instant corrInstant) {
    String oidStr = DATA_POINT_PREFIX + oid;
    Duration dur = Duration.between(verInstant, corrInstant);
    String verStr = verInstant.toString() + dur.toString();
    return UniqueId.of(getUniqueIdScheme(), oidStr, verStr);
  }

  @Override
  protected long extractRowId(UniqueId uniqueId) {
    int pos = uniqueId.getVersion().indexOf('P');
    if (pos < 0) {
      return super.extractRowId(uniqueId);
    }
    VersionCorrection vc = getMaster().extractTimeSeriesInstants(uniqueId);
    HistoricalTimeSeriesInfoDocument doc = getMaster().get(uniqueId.getObjectId(), vc);  // not very efficient, but works
    return super.extractRowId(doc.getUniqueId());
  }

  //-------------------------------------------------------------------------
  /**
   * Resolves an object identifier to a unique identifier.
   * 
   * @param objectId  the time-series object identifier, not null
   * @param versionCorrection  the version-correction locator to search at, not null
   * @return the time-series, not null
   */
  protected UniqueId resolveObjectId(ObjectIdentifiable objectId, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    checkScheme(objectId);
    final long oid = extractOid(objectId);
    versionCorrection = versionCorrection.withLatestFixed(now());
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addValue("doc_oid", oid)
      .addTimestamp("version_as_of_instant", versionCorrection.getVersionAsOf())
      .addTimestamp("corrected_to_instant", versionCorrection.getCorrectedTo());
    final NamedParameterJdbcOperations namedJdbc = getDbConnector().getJdbcTemplate().getNamedParameterJdbcOperations();
    final UniqueIdExtractor extractor = new UniqueIdExtractor(oid);
    final String sql = getExtSqlBundle().getSql("SelectUniqueIdByVersionCorrection", args);
    final UniqueId uniqueId = namedJdbc.query(sql, args, extractor);
    if (uniqueId == null) {
      throw new DataNotFoundException("Unable to find time-series: " + objectId.getObjectId());
    }
    return uniqueId;
  }

  //-------------------------------------------------------------------------
  /**
   * Mapper from SQL rows to a LocalDateDoubleTimeSeries.
   */
  protected final class DataPointsExtractor implements ResultSetExtractor<LocalDateDoubleTimeSeries> {
    @Override
    public LocalDateDoubleTimeSeries extractData(final ResultSet rs) throws SQLException, DataAccessException {
      final List<LocalDate> dates = new ArrayList<LocalDate>(256);
      final List<Double> values = new ArrayList<Double>(256);
      LocalDate last = null;
      while (rs.next()) {
        LocalDate date = DbDateUtils.fromSqlDateAllowNull(rs.getDate("POINT_DATE"));
        if (date.equals(last) == false) {
          last = date;
          Double value = (Double) rs.getObject("POINT_VALUE");
          if (value != null) {
            dates.add(date);
            values.add(value);
          }
        } else {
          // The data points query should return no more than one value per date
          throw new OpenGammaRuntimeException("Unexpected duplicate data point entry");
        }
      }
      return new ArrayLocalDateDoubleTimeSeries(dates, values);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Mapper from SQL rows to a UniqueId.
   */
  protected final class UniqueIdExtractor implements ResultSetExtractor<UniqueId> {
    private final long _objectId;
    public UniqueIdExtractor(final long objectId) {
      _objectId = objectId;
    }
    @Override
    public UniqueId extractData(final ResultSet rs) throws SQLException, DataAccessException {
      while (rs.next()) {
        Timestamp ver = rs.getTimestamp("max_ver_instant");
        Timestamp corr = rs.getTimestamp("max_corr_instant");
        if (ver == null) {
          ver = rs.getTimestamp("ver_from_instant");
          corr = rs.getTimestamp("corr_from_instant");
        }
        Instant verInstant = DbDateUtils.fromSqlTimestamp(ver);
        Instant corrInstant = (corr != null ? DbDateUtils.fromSqlTimestamp(corr) : verInstant);
        return createTimeSeriesUniqueId(_objectId, verInstant, corrInstant);
      }
      return null;
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Mapper from SQL rows to a ManageableHistoricalTimeSeries.
   */
  protected final class ManageableHTSExtractor implements ResultSetExtractor<ManageableHistoricalTimeSeries> {
    private final long _objectId;
    public ManageableHTSExtractor(final long objectId) {
      _objectId = objectId;
    }
    @Override
    public ManageableHistoricalTimeSeries extractData(final ResultSet rs) throws SQLException, DataAccessException {
      while (rs.next()) {
        Timestamp ver = rs.getTimestamp("max_ver_instant");
        Timestamp corr = rs.getTimestamp("max_corr_instant");
        if (ver == null) {
          ver = rs.getTimestamp("ver_from_instant");
          corr = rs.getTimestamp("corr_from_instant");
        }
        Instant verInstant = DbDateUtils.fromSqlTimestamp(ver);
        Instant corrInstant = (corr != null ? DbDateUtils.fromSqlTimestamp(corr) : verInstant);
        ManageableHistoricalTimeSeries hts = new ManageableHistoricalTimeSeries();
        hts.setUniqueId(createTimeSeriesUniqueId(_objectId, verInstant, corrInstant));
        hts.setVersionInstant(verInstant);
        hts.setCorrectionInstant(corrInstant);
        
//        hts.setEarliestDate(DbDateUtils.fromSqlDateAllowNull(rs.getDate("min_point_date")));
//        hts.setLatestDate(DbDateUtils.fromSqlDateAllowNull(rs.getDate("max_point_date")));        
//        hts.setEarliestValue(rs.getDouble("earliest_point_value"));
//        hts.setLatestValue(rs.getDouble("latest_point_value"));
        
        return hts;
      }
      return null;
    }
  }


}
