/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.historical.dao;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

import javax.sql.DataSource;
import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.sqldate.ArraySQLDateDoubleTimeSeries;
import com.opengamma.util.timeseries.sqldate.MapSQLDateDoubleTimeSeries;
import com.opengamma.util.tuple.Pair;

/**
 * 
 * 
 * @author yomi
 */
public abstract class RowStoreJdbcDao implements TimeSeriesDao {
  private static final Logger s_logger = LoggerFactory.getLogger(RowStoreJdbcDao.class);
  
  private static final int INVALID_KEY = -1;
  private static final String LOAD_TIME_SERIES = "loadTimeSeries";
  private static final String LOAD_TIME_SERIES_WITH_DATES = "loadTimeSeriesWithDates";
  private static final String SELECT_QUOTED_OBJECT_FROM_IDENTIFIERS = "selectQuotedObjectFromIdentifiers";
  private static final String LOAD_ALL_DATA_PROVIDER = "loadAllDataProvider";
  private static final String SELECT_DATA_PROVIDER_ID = "selectDataProviderID";
  private static final String LOAD_TIME_SERIES_DELTA = "loadTimeSeriesDelta";
  private static final String DELETE_DATA_POINT = "deleteDataPoint";
  private static final String INSERT_TIME_SERIES_DELTA_U = "insertTimeSeriesDeltaU";
  private static final String UPDATE_TIME_SERIES = "updateTimeSeries";
  private static final String FIND_DATA_POINT_BY_DATE_AND_ID = "findDataPointByDateAndID";
  private static final String INSERT_TIME_SERIES_KEY = "insertTimeSeriesKey";
  private static final String INSERT_TIME_SERIES = "insertTimeSeries";
  private static final String GET_TIME_SERIES_KEY_ID_BY_IDENTIFIER = "getTimeSeriesKeyIDByIdentifier";
  private static final String GET_TIME_SERIES_KEY_ID_BY_QUOTED_OBJECT = "getTimeSeriesKeyIDByQuotedObject";
  private static final String GET_TIME_SERIES_BY_ID = "getTimeSeriesByID";
  private static final String DELETE_TIME_SERIES_BY_ID = "deleteTimeSeriesByID";
  private static final String FIND_DOMAIN_SPEC_IDENTIFIERS_BY_QUOTED_OBJECT = "findDomainSpecIdentifiersByQuotedObject";
  private static final String FIND_DATA_PROVIDER_BY_ID = "findDataProviderByID";
  private static final String FIND_DATA_SOURCE_BY_ID = "findDataSourceByID";
  private static final String FIND_OBSERVATION_TIME_BY_ID = "findObservationTimeByID";
  private static final String FIND_QUOTED_OBJECT_BY_ID = "findQuotedObjectByID";
  private static final String FIND_DATA_FIELD_BY_ID = "findDataFieldByID";
  private static final String FIND_DOMAIN_BY_ID = "findDomainByID";
  
  private static final String INSERT_TIME_SERIES_DELTA_D = "insertTimeSeriesDeltaD";
  private static final String INSERT_TIME_SERIES_DELTA_I = "insertTimeSeriesDeltaI";
  
  private static final String INSERT_DATA_PROVIDER = "insertDataProvider";
  private static final String INSERT_DATA_FIELD = "insertDataField";
  private static final String INSERT_OBSERVATION_TIME = "insertObservationTime";
  private static final Object INSERT_QUOTED_OBJECT = "insertQuotedObject";
  private static final String INSERT_DATA_SOURCE = "insertDataSource";
  private static final String INSERT_DOMAIN = "insertDomain";
  
  private static final String NAME_COLUMN = "name";
  private static final String IDENTIFIER_COLUMN = "identifier";

  private static final long MILLIS_IN_DAY = 86400000l;

  private static final String SELECT_DATA_SOURCE_ID = "selectDataSourceID";
  private static final String SELECT_DATA_FIELD_ID = "selectDataFieldID";
  private static final String SELECT_OBSERVATION_TIME_ID = "selectObservationTimeID";
  private static final String SELECT_QUOTED_OBJECT_ID = "selectQuotedObjectID";
  private static final Object LOAD_ALL_OBSERVATION_TIMES = "loadAllObservationTimes";
  private static final Object LOAD_ALL_QUOTED_OBJECTS = "loadAllQuotedObjects";
  private static final Object LOAD_ALL_DATA_FIELDS = "loadAllDataFields";
  private static final Object LOAD_ALL_DATA_SOURCES = "loadAllDataSources";
  private static final Object SELECT_DOMAIN_ID = "selectDomainID";
  private static final Object LOAD_ALL_DOMAIN = "loadAllDomain";
  
  private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");

  private DataSourceTransactionManager _transactionManager;
  private SimpleJdbcTemplate _simpleJdbcTemplate;
  private TransactionDefinition _transactionDefinition = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRED);
  
  public RowStoreJdbcDao(DataSourceTransactionManager transactionManager) {
    ArgumentChecker.notNull(transactionManager, "transactionManager");
    _transactionManager = transactionManager;
    DataSource dataSource = _transactionManager.getDataSource();
    _simpleJdbcTemplate = new SimpleJdbcTemplate(dataSource);   
  }
  
  @Override
  public void addTimeSeries(IdentifierBundle domainIdentifiers,
      String dataSource, String dataProvider, String field,
      String observationTime, final LocalDateDoubleTimeSeries timeSeries) {

    ArgumentChecker.notNull(domainIdentifiers, "domainIdentifiers");
    ArgumentChecker.notNull(dataSource, "dataSource");
    ArgumentChecker.notNull(dataProvider, "dataProvider");
    ArgumentChecker.notNull(field, "field");
    ArgumentChecker.notNull(observationTime, "observationTime");
    ArgumentChecker.notNull(timeSeries, "timeSeries");
    
    DoubleTimeSeries<Date> sqlDateDoubleTimeSeries = timeSeries.toSQLDateDoubleTimeSeries();
    s_logger.debug("adding timeseries for {} with dataSource={}, dataProvider={}, dataField={}, observationTime={} startdate={} endate={}", 
        new Object[]{domainIdentifiers, dataSource, dataProvider, field, observationTime, timeSeries.getEarliestTime(), timeSeries.getLatestTime()});
    String quotedObject = findQuotedObject(domainIdentifiers);
    
    if (quotedObject == null) {
      Identifier identifier = domainIdentifiers.getIdentifiers().iterator().next();
      quotedObject = identifier.getScheme().getName() + "_" + identifier.getValue();
      createDomainSpecIdentifiers(domainIdentifiers, quotedObject);
    } else {
      IdentifierBundle loadedIdentifiers = findDomainSpecIdentifiersByQuotedObject(quotedObject);
      Collection<Identifier> missing = new HashSet<Identifier>(domainIdentifiers.getIdentifiers());
      missing.removeAll(loadedIdentifiers.getIdentifiers());
      if (!missing.isEmpty()) {
        createDomainSpecIdentifiers(new IdentifierBundle(missing), quotedObject);
      }
    }
    
    int timeSeriesKeyID = getTimeSeriesKeyIDByQuotedObject(quotedObject, dataSource, dataProvider, field, observationTime);
    if (timeSeriesKeyID == INVALID_KEY) {
      createTimeSeriesKey(quotedObject, dataSource, dataProvider, field, observationTime);
      timeSeriesKeyID = getTimeSeriesKeyIDByQuotedObject(quotedObject, dataSource, dataProvider, field, observationTime);
    }
    
    Map<String, String> sqlQueries = getSqlQueries();
    String insertSQL = sqlQueries.get(INSERT_TIME_SERIES);
    String insertDelta = sqlQueries.get(INSERT_TIME_SERIES_DELTA_I);
    
    Date now = new Date(System.currentTimeMillis());
    s_logger.debug("timeStamp = {}", DATE_FORMAT.format(now));
    
    SqlParameterSource[] batchArgs = new MapSqlParameterSource[sqlDateDoubleTimeSeries.size()];
    int index = 0;
    
    for (Entry<Date, Double> dataPoint : sqlDateDoubleTimeSeries) {
      Date date = dataPoint.getKey();
      Double value = dataPoint.getValue();
      MapSqlParameterSource parameterSource = new MapSqlParameterSource();
      parameterSource.addValue("timeSeriesID", timeSeriesKeyID, Types.INTEGER);
      parameterSource.addValue("date", date, Types.DATE);
      parameterSource.addValue("value", value, Types.DOUBLE);
      parameterSource.addValue("timeStamp", now, Types.TIMESTAMP);
      batchArgs[index++] = parameterSource;
    }
    
    if (!isTriggerSupported()) {
      //start transaction
      TransactionStatus transactionStatus = _transactionManager.getTransaction(_transactionDefinition);
      try {
        _simpleJdbcTemplate.batchUpdate(insertSQL, batchArgs);
        _simpleJdbcTemplate.batchUpdate(insertDelta, batchArgs);
        _transactionManager.commit(transactionStatus);
      } catch (Throwable t) {
        _transactionManager.rollback(transactionStatus);
        Identifier identifer = domainIdentifiers.getIdentifiers().iterator().next();
        s_logger.warn("error trying to insert timeSeries for {}-{}", identifer.getScheme().getName(), identifer.getValue());
        throw new OpenGammaRuntimeException("Unable to add Timeseries", t);
      }
    } else {
      _simpleJdbcTemplate.batchUpdate(insertSQL, batchArgs);
    }
    
  }

  protected abstract boolean isTriggerSupported();

  @Override
  public String findDataFieldByID(int id) {
    s_logger.debug("looking up data field by id={}", id);
    Map<String, String> sqlQueries = getSqlQueries();
    String sql = sqlQueries.get(FIND_DATA_FIELD_BY_ID);
    return findNamedDimensionByID(sql, id);
  }

  @Override
  public int createDataProvider(String dataProvider, String description) {
    Map<String, String> sqlQueries = getSqlQueries();
    String sql = sqlQueries.get(INSERT_DATA_PROVIDER);
    insertNamedDimension(sql, dataProvider, description);
    return getDataProviderID(dataProvider);
  }

  /**
   * @return
   */
  protected abstract Map<String, String> getSqlQueries();

  /**
   * @param dataProvider
   * @param description
   */
  private void insertNamedDimension(String sql, String name, String description) {
    s_logger.debug("running sql={} with values({}, {})", new Object[]{sql, name, description});
    SqlParameterSource parameters = new MapSqlParameterSource()
      .addValue("name", name, Types.VARCHAR)
      .addValue("description", description, Types.VARCHAR);
    _simpleJdbcTemplate.update(sql, parameters);
  }

  @Override
  public String findDataProviderByID(int id) {
    s_logger.debug("looking up data provider by id={}", id);
    Map<String, String> sqlQueries = getSqlQueries();
    String sql = sqlQueries.get(FIND_DATA_PROVIDER_BY_ID);
    return findNamedDimensionByID(sql, id);
  }

  /**
   * @param id
   * @return
   */
  private String findNamedDimensionByID(String sql, int id) {
    s_logger.debug("running sql={}", sql);
    SqlParameterSource parameters = new MapSqlParameterSource().addValue("id", id, Types.INTEGER);
    String result = null;
    try {
      result = _simpleJdbcTemplate.queryForObject(sql.toString(), String.class, parameters);
    } catch (EmptyResultDataAccessException e) {
      s_logger.debug("Empty row return for id={} from sql={}", id, sql);
      result = null;
    }
    return result;
  }

  @Override
  public int createDataSource(String dataSource, String description) {
    Map<String, String> sqlQueries = getSqlQueries();
    String sql = sqlQueries.get(INSERT_DATA_SOURCE);
    insertNamedDimension(sql, dataSource, description);
    return getDataSourceID(dataSource);
  }

  @Override
  public String findDataSourceByID(int id) {
    s_logger.debug("looking up data source by id={}", id);
    Map<String, String> sqlQueries = getSqlQueries();
    String sql = sqlQueries.get(FIND_DATA_SOURCE_BY_ID);
    return findNamedDimensionByID(sql, id);
  }
  
  protected String findQuotedObject(final IdentifierBundle domainIdentifiers) {
    String result = null;
    int size = domainIdentifiers.size();
    if (size < 1) {
      return result;
    }
    Map<String, String> sqlQueries = getSqlQueries();
    String selectQuoted = sqlQueries.get(SELECT_QUOTED_OBJECT_FROM_IDENTIFIERS);
    
    StringBuilder sqlBuffer = new StringBuilder(selectQuoted).append(" AND (");
    int orCounter = 1;
    Object[] parameters = new Object[size*2];
    int paramIndex = 0;
    for (Identifier domainSpecificIdentifier : domainIdentifiers.getIdentifiers()) {
      sqlBuffer.append("(d.name = ? AND dsi.identifier = ?)");
      parameters[paramIndex++] = domainSpecificIdentifier.getScheme().getName();
      parameters[paramIndex++] = domainSpecificIdentifier.getValue();
      if (orCounter++ != size) {
        sqlBuffer.append(" OR ");
      }
    }
    sqlBuffer.append(" ) GROUP BY qo.name");
    String findIdentifiersSql = sqlBuffer.toString(); 
    List<Map<String, Object>> queryForList = _simpleJdbcTemplate.queryForList(findIdentifiersSql, parameters);
    //should return just one quoted object
    if (queryForList.size() > 1) {
      s_logger.warn("{} has more than 1 quoted object associated to them", domainIdentifiers);
      throw new OpenGammaRuntimeException(domainIdentifiers + " has more than 1 quoted object associated to them");
    }
    if (queryForList.size() == 1) {
      Map<String, Object> row = queryForList.get(0);
      result = (String)row.get("name");
    }
    return result;
  }

  @Override
  public void createDomainSpecIdentifiers(final IdentifierBundle domainIdentifiers, final String quotedObject) {
    ArgumentChecker.notNull(domainIdentifiers, "domainIdentifiers");
    //start transaction
    TransactionStatus transactionStatus = _transactionManager.getTransaction(_transactionDefinition);
    
    s_logger.debug("creating/updating domainSpecIdentifiers {}", domainIdentifiers);
    Map<String, String> sqlQueries = getSqlQueries();
    try {
      //find existing identifiers
      Set<Identifier> resolvedIdentifiers = new HashSet<Identifier>(domainIdentifiers.getIdentifiers());
      int size = domainIdentifiers.size();
      if (size > 0) {
        String selectQuoted = sqlQueries.get(SELECT_QUOTED_OBJECT_FROM_IDENTIFIERS);
        StringBuilder sqlBuffer = new StringBuilder(selectQuoted).append(" AND (");
        int orCounter = 1;
        Object[] parameters = new Object[size*2];
        int paramIndex = 0;
        for (Identifier domainSpecificIdentifier : domainIdentifiers.getIdentifiers()) {
          sqlBuffer.append("(d.name = ? AND dsi.identifier = ?)");
          parameters[paramIndex++] = domainSpecificIdentifier.getScheme().getName();
          parameters[paramIndex++] = domainSpecificIdentifier.getValue();
          if (orCounter++ != size) {
            sqlBuffer.append(" OR ");
          }
        }
        sqlBuffer.append(" ) GROUP BY qo.name");
        String findIdentifiersSql = sqlBuffer.toString(); 
        List<Map<String, Object>> queryForList = _simpleJdbcTemplate.queryForList(findIdentifiersSql, parameters);
        //should return just one quoted object
        if (queryForList.size() > 1) {
          s_logger.warn("{} has more than 1 quoted object associated to them", domainIdentifiers);
          throw new OpenGammaRuntimeException(domainIdentifiers + " has more than 1 quoted object associated to them");
        }
        if (queryForList.size() == 1) {
          Map<String, Object> row = queryForList.get(0);
          String loadedQuotedObject = (String)row.get("name");
          if (!loadedQuotedObject.equals(quotedObject)) {
            s_logger.warn("{} has been associated already with {}", loadedQuotedObject, domainIdentifiers);
            throw new OpenGammaRuntimeException(loadedQuotedObject + " has been associated already with " + domainIdentifiers);
          }
          long loadedIdentifierCount = Long.valueOf(String.valueOf(row.get("count")));
          if (loadedIdentifierCount != domainIdentifiers.size()) {
            IdentifierBundle loadeIdentifiers = findDomainSpecIdentifiersByQuotedObject(quotedObject);
            resolvedIdentifiers.removeAll(loadeIdentifiers.getIdentifiers());
          }
        }
        
      }
      if (getQuotedObjectID(quotedObject) == INVALID_KEY) {
        createQuotedObject(quotedObject, quotedObject);
      }
      
      SqlParameterSource[] batchArgs = new MapSqlParameterSource[resolvedIdentifiers.size()];
      int index = 0;
      for (Identifier domainSpecificIdentifier : resolvedIdentifiers) {
        String domainName = domainSpecificIdentifier.getScheme().getName();
        if (getDomainID(domainName) == INVALID_KEY) {
          createDomain(domainName, domainName);
        }
        Map<String, Object> valueMap = new HashMap<String, Object>();
        valueMap.put("quotedObject", quotedObject);
        valueMap.put("domain", domainName);
        valueMap.put("identifier", domainSpecificIdentifier.getValue());
        batchArgs[index++] = new MapSqlParameterSource(valueMap);
      }
      
      String insertSQL = sqlQueries.get("insertDomainSpecIdentifier");
      
      _simpleJdbcTemplate.batchUpdate(insertSQL, batchArgs);
      _transactionManager.commit(transactionStatus);
      
    } catch (Throwable t) {
      _transactionManager.rollback(transactionStatus);
      s_logger.warn("error trying to create domainSpecIdentifiers", t);
      throw new OpenGammaRuntimeException("Unable to create DomainSpecificIdentifiers", t);
    }
    
  }

  @Override
  public int createObservationTime(String observationTime, String description) {
    Map<String, String> sqlQueries = getSqlQueries();
    String sql = sqlQueries.get(INSERT_OBSERVATION_TIME);
    insertNamedDimension(sql, observationTime, description);
    return getObservationTimeID(observationTime);
  }

  @Override
  public int createQuotedObject(String name, String description) {
    Map<String, String> sqlQueries = getSqlQueries();
    String sql = sqlQueries.get(INSERT_QUOTED_OBJECT);
    insertNamedDimension(sql, name, description);
    return getQuotedObjectID(name);
  }

  @Override
  public int createDataField(String field, String description) {
    Map<String, String> sqlQueries = getSqlQueries();
    String sql = sqlQueries.get(INSERT_DATA_FIELD);
    insertNamedDimension(sql, field, description);
    return getDataFieldID(field);
  }

  @Override
  public Set<String> getAllDataProviders() {
    s_logger.debug("loading all dataProviders");
    Map<String, String> sqlQueries = getSqlQueries();
    String sql = sqlQueries.get(LOAD_ALL_DATA_PROVIDER);
    return getAllNamedDimensionNames(sql);
  }

  @Override
  public Set<String> getAllDataSources() {
    s_logger.debug("loading all Datasources");
    Map<String, String> sqlQueries = getSqlQueries();
    String sql = sqlQueries.get(LOAD_ALL_DATA_SOURCES);
    return getAllNamedDimensionNames(sql);
  }

  /**
   * @return
   */
  private Set<String> getAllNamedDimensionNames(final String sql) {
    s_logger.debug("loading all dimension names for sql={}", sql);
    List<String> queryResult = _simpleJdbcTemplate.query(sql, new ParameterizedRowMapper<String>() {
      @Override
      public String mapRow(ResultSet rs, int rowNum) throws SQLException {
        return rs.getString(NAME_COLUMN);
      }
    }, new Object[]{});
    return new TreeSet<String>(queryResult);
  }
  
  @Override
  public String findQuotedObjectByID(int id) {
    s_logger.debug("looking up quotedObject by id={}", id);
    Map<String, String> sqlQueries = getSqlQueries();
    String sql = sqlQueries.get(FIND_QUOTED_OBJECT_BY_ID);
    return findNamedDimensionByID(sql, id);
  }
  
  @Override
  public String findObservationTimeByID(int id) {
    s_logger.debug("looking up observation time by id={}", id);
    Map<String, String> sqlQueries = getSqlQueries();
    String sql = sqlQueries.get(FIND_OBSERVATION_TIME_BY_ID);
    return findNamedDimensionByID(sql, id);
  }

  @Override
  public Set<String> getAllObservationTimes() {
    s_logger.debug("loading all observationTimes");
    Map<String, String> sqlQueries = getSqlQueries();
    String sql = sqlQueries.get(LOAD_ALL_OBSERVATION_TIMES);
    return getAllNamedDimensionNames(sql);
  }

  @Override
  public Set<String> getAllQuotedObjects() {
    s_logger.debug("loading all quotedObjects");
    Map<String, String> sqlQueries = getSqlQueries();
    String sql = sqlQueries.get(LOAD_ALL_QUOTED_OBJECTS);
    return getAllNamedDimensionNames(sql);
  }

  @Override
  public Set<String> getAllTimeSeriesFields() {
    s_logger.debug("loading all dataFields");
    Map<String, String> sqlQueries = getSqlQueries();
    String sql = sqlQueries.get(LOAD_ALL_DATA_FIELDS);
    return getAllNamedDimensionNames(sql);
  }
  
  @Override
  public Set<String> getAllDomains() {
    s_logger.debug("loading all domain");
    Map<String, String> sqlQueries = getSqlQueries();
    String sql = sqlQueries.get(LOAD_ALL_DOMAIN);
    return getAllNamedDimensionNames(sql);
  }

  @Override
  public int getDataProviderID(String name) {
    s_logger.debug("looking up id for dataProvider={}", name);
    String sql = getSqlQueries().get(SELECT_DATA_PROVIDER_ID);
    return getNamedDimensionID(sql, name);
  }

  /**
   * @param name 
   * @return
   */
  private int getNamedDimensionID(final String sql, final String name) {
    s_logger.debug("looking up id from sql={} with name={}", sql, name);
    SqlParameterSource parameters = new MapSqlParameterSource().addValue("name", name);

    int result = INVALID_KEY;
    try {
      result = _simpleJdbcTemplate.queryForInt(sql, parameters);
    } catch(EmptyResultDataAccessException e) {
      s_logger.debug("Empty row return for name = {} from sql = {}", name, sql);
      result = INVALID_KEY;
    }
    s_logger.debug("id = {}", result);
    return result;
  }

  @Override
  public int getDataSourceID(String name) {
    s_logger.debug("looking up id for dataSource={}", name);
    String sql = getSqlQueries().get(SELECT_DATA_SOURCE_ID);
    return getNamedDimensionID(sql, name);
  }

  @Override
  public int getDataFieldID(String name) {
    s_logger.debug("looking up id for dataField={}", name);
    String sql = getSqlQueries().get(SELECT_DATA_FIELD_ID);
    return getNamedDimensionID(sql, name);
  }

  @Override
  public int getObservationTimeID(String name) {
    s_logger.debug("looking up id for observationTime={}", name);
    String sql = getSqlQueries().get(SELECT_OBSERVATION_TIME_ID);
    return getNamedDimensionID(sql, name);
  }

  @Override
  public int getQuotedObjectID(String name) {
    s_logger.debug("looking up id for quotedObject={}", name);
    String sql = getSqlQueries().get(SELECT_QUOTED_OBJECT_ID);
    return getNamedDimensionID(sql, name);
  }
  
  @Override
  public int getDomainID(String name) {
    s_logger.debug("looking up id for domain={}", name);
    String sql = getSqlQueries().get(SELECT_DOMAIN_ID);
    return getNamedDimensionID(sql, name);
  }

  @Override
  public IdentifierBundle findDomainSpecIdentifiersByQuotedObject(String name) {
    ArgumentChecker.notNull(name, "name");
    s_logger.debug("looking up domainSpecIdentifiers using quotedObj={}", name);
        
    Map<String, String> sqlQueries = getSqlQueries();
    String sql = sqlQueries.get(FIND_DOMAIN_SPEC_IDENTIFIERS_BY_QUOTED_OBJECT);
    
    SqlParameterSource parameterSource = new MapSqlParameterSource("quotedObject", name);
    List<Identifier> queryResult = _simpleJdbcTemplate.query(sql, new ParameterizedRowMapper<Identifier>() {
      @Override
      public Identifier mapRow(ResultSet rs, int rowNum)
          throws SQLException {
        String domain = rs.getString(NAME_COLUMN);
        String identifier = rs.getString(IDENTIFIER_COLUMN);
        return new Identifier(domain, identifier);
      }
    }, parameterSource);  
    return new IdentifierBundle(queryResult);
  }
  
  @Override
  public int createDomain(String domain, String description) {
    Map<String, String> sqlQueries = getSqlQueries();
    String sql = sqlQueries.get(INSERT_DOMAIN);
    insertNamedDimension(sql, domain, description);
    return getDomainID(domain);
  }

  @Override
  public String findDomainByID(int id) {
    s_logger.debug("looking up domain by id={}", id);
    Map<String, String> sqlQueries = getSqlQueries();
    String sql = sqlQueries.get(FIND_DOMAIN_BY_ID);
    return findNamedDimensionByID(sql, id);
  }
  
  @Override
  public void createTimeSeriesKey(String quotedObject, String dataSource,
      String dataProvider, String dataField, String observationTime) {
    ArgumentChecker.notNull(quotedObject, "quotedObject");
    ArgumentChecker.notNull(dataSource, "dataSource");
    ArgumentChecker.notNull(dataProvider, "dataProvider");
    ArgumentChecker.notNull(dataField, "dataField");
    ArgumentChecker.notNull(observationTime, "observationTime");
    
    s_logger.debug("creating timeSeriesKey with quotedObj={}, dataSource={}, dataProvider={}, dataField={}, observationTime={}", 
        new Object[]{quotedObject, dataSource, dataProvider, dataField, observationTime});
    
    if (getDataSourceID(dataSource) == INVALID_KEY) {
      createDataSource(dataSource, null);
    }
    if (getDataProviderID(dataProvider) == INVALID_KEY) {
      createDataProvider(dataProvider, null);
    }
    if (getDataFieldID(dataField) == INVALID_KEY) {
      createDataField(dataField, null);
    }
    if (getObservationTimeID(observationTime) == INVALID_KEY) {
      createObservationTime(observationTime, null);
    }
    Map<String, String> sqlQueries = getSqlQueries();
    String sql = sqlQueries.get(INSERT_TIME_SERIES_KEY);
    
    SqlParameterSource parameterSource = new MapSqlParameterSource()
      .addValue("quotedObject", quotedObject)
      .addValue("dataSource", dataSource)
      .addValue("dataProvider", dataProvider)
      .addValue("dataField", dataField)
      .addValue("observationTime", observationTime);
    
    _simpleJdbcTemplate.update(sql, parameterSource);
  }
  
  protected int getTimeSeriesKeyIDByIdentifierBundle(IdentifierBundle identifierBundle, String dataSource,
      String dataProvider, String dataField, String observationTime) {
    ArgumentChecker.notNull(identifierBundle, "identifiers");
    ArgumentChecker.notNull(dataSource, "dataSource");
    ArgumentChecker.notNull(dataProvider, "dataProvider");
    ArgumentChecker.notNull(dataField, "dataField");
    
    int result = INVALID_KEY;
    
    s_logger.debug("Looking up timeSeriesKeyID by identifiers for identifiers={}, dataSource={}, dataProvider={}, dataField={}, observationTime={}", 
        new Object[]{identifierBundle, dataSource, dataProvider, dataField, observationTime});
    
    Map<String, String> sqlQueries = getSqlQueries();
    String sql = null;
    if (observationTime != null) {
      sql = sqlQueries.get("getTimeSeriesKeyIDByIdentifierWithObservationTime");
    } else {
      sql = sqlQueries.get(GET_TIME_SERIES_KEY_ID_BY_IDENTIFIER);
    }
    Set<Identifier> identifiers = identifierBundle.getIdentifiers();
    for (Identifier identifier : identifiers) {
      MapSqlParameterSource parameters = new MapSqlParameterSource().addValue("identifier", identifier.getValue())
      .addValue("domain", identifier.getScheme().getName(), Types.VARCHAR)
      .addValue("dataSource", dataSource, Types.VARCHAR)
      .addValue("dataProvider", dataProvider, Types.VARCHAR)
      .addValue("dataField", dataField, Types.VARCHAR);
      if (observationTime != null) {
        parameters.addValue("observationTime", observationTime, Types.VARCHAR);
      }
      
      try {
        result = _simpleJdbcTemplate.queryForInt(sql, parameters);
      } catch(EmptyResultDataAccessException e) {
        s_logger.debug("Empty timeserieskey  returned for identifier={} dataSource={} dataProvider={} dataField={} observationTime={}", new Object[]{identifier, dataSource, dataProvider, dataField, observationTime});
        result = INVALID_KEY;
      }
      
      if (result != INVALID_KEY) {
        s_logger.debug("timeSeriesKeyID = {}", result);
        return result;
      }
    }
    //no timeseries key found
    return result;
    
  }
  
  protected int getTimeSeriesKeyIDByQuotedObject(String quotedObject, String dataSource,
      String dataProvider, String dataField, String observationTime) {
    ArgumentChecker.notNull(quotedObject, "quotedObject");
    ArgumentChecker.notNull(dataSource, "dataSource");
    ArgumentChecker.notNull(dataProvider, "dataProvider");
    ArgumentChecker.notNull(dataField, "dataField");
    ArgumentChecker.notNull(observationTime, "observationTime");
    
    int result = INVALID_KEY;
    
    s_logger.debug("looking up timeSeriesKeyID by quotedObject with quotedObj={}, dataSource={}, dataProvider={}, dataField={}, observationTime={}", 
        new Object[]{quotedObject, dataSource, dataProvider, dataField, observationTime});
    
    Map<String, String> sqlQueries = getSqlQueries();
    String sql = sqlQueries.get(GET_TIME_SERIES_KEY_ID_BY_QUOTED_OBJECT);
     
    SqlParameterSource parameterSource = new MapSqlParameterSource()
    .addValue("quotedObject", quotedObject, Types.VARCHAR)
    .addValue("dataSource", dataSource, Types.VARCHAR)
    .addValue("dataProvider", dataProvider, Types.VARCHAR)
    .addValue("dataField", dataField, Types.VARCHAR)
    .addValue("observationTime", observationTime, Types.VARCHAR);
    
    try {
      result = _simpleJdbcTemplate.queryForInt(sql, parameterSource);
    } catch(EmptyResultDataAccessException e) {
      s_logger.debug("Empty row returned for timeSeriesKeyID");
      result = INVALID_KEY;
    }
    
    s_logger.debug("timeSeriesKeyID = {}", result);
    return result;
  }
  
  @Override
  public void deleteTimeSeries(IdentifierBundle identifiers,
      String dataSource, String dataProvider, String field,
      String observationTime) {
    ArgumentChecker.notNull(identifiers, "identifiers");
    ArgumentChecker.notNull(dataSource, "dataSource");
    ArgumentChecker.notNull(dataProvider, "dataProvider");
    ArgumentChecker.notNull(field, "field");
    ArgumentChecker.notNull(observationTime, "observationTime");
    
    s_logger.debug("deleting timeseries for identifier={} dataSource={} dataProvider={} dataField={} observationTime={}", 
        new Object[]{identifiers, dataSource, dataProvider, field, observationTime});
    
    int tsID = getTimeSeriesKeyIDByIdentifierBundle(identifiers, dataSource, dataProvider, field, observationTime);
    
    Map<String, String> sqlQueries = getSqlQueries();
    String selectTSSQL = sqlQueries.get(GET_TIME_SERIES_BY_ID);
    MapSqlParameterSource tsIDParameter = new MapSqlParameterSource().addValue("tsID", tsID, Types.INTEGER);
    
    List<Pair<Date, Double>> queryResult = _simpleJdbcTemplate.query(selectTSSQL, new ParameterizedRowMapper<Pair<Date, Double>>() {

      @Override
      public Pair<Date, Double> mapRow(ResultSet rs, int rowNum)
          throws SQLException {
        double tsValue = rs.getDouble("value");
        Date tsDate = rs.getDate("ts_date");
        return Pair.of(tsDate, tsValue);
      }
    }, tsIDParameter);
    
    String deleteSql = sqlQueries.get(DELETE_TIME_SERIES_BY_ID);
    String insertDelta = sqlQueries.get(INSERT_TIME_SERIES_DELTA_D);
    
    Date now = new Date(System.currentTimeMillis());
    s_logger.debug("timeStamp = {}", DATE_FORMAT.format(now));
    
    SqlParameterSource[] batchArgs = new MapSqlParameterSource[queryResult.size()];
    int i = 0;
    for (Pair<Date, Double> pair : queryResult) {
      Date date = pair.getFirst();
      Double value = pair.getSecond();
      MapSqlParameterSource parameterSource = new MapSqlParameterSource();
      parameterSource.addValue("timeSeriesID", tsID, Types.INTEGER);
      parameterSource.addValue("date", date, Types.DATE);
      parameterSource.addValue("value", value, Types.DOUBLE);
      parameterSource.addValue("timeStamp", now, Types.TIMESTAMP);
      batchArgs[i++] = parameterSource;
    }
    
    //start transaction
    TransactionStatus transactionStatus = _transactionManager.getTransaction(_transactionDefinition);
    try {
      _simpleJdbcTemplate.batchUpdate(insertDelta, batchArgs);
      _simpleJdbcTemplate.update(deleteSql, tsIDParameter);
      _transactionManager.commit(transactionStatus);
    } catch (Throwable t) {
      _transactionManager.rollback(transactionStatus);
      s_logger.warn("error trying to delete timeSeries", t);
      throw new OpenGammaRuntimeException("Unable to delete Timeseries", t);
    }
    
  }
  //need to merge with historicalDataProvider  
//  public LocalDateDoubleTimeSeries getTimeSeries(Identifier domainSpecId,
//      String dataSource, String dataProvider, String field,
//      String observationTime) {
//    return getTimeSeries(domainSpecId, dataSource, dataProvider, field, observationTime, null, null);
//  }
//
//  public LocalDateDoubleTimeSeries getTimeSeries(Identifier domainSpecId,
//      String dataSource, String dataProvider, String field,
//      String observationTime, LocalDate start, LocalDate end) {
//    DoubleTimeSeries<Date> timeSeries = loadTimeSeries(domainSpecId, dataSource, dataProvider, field, observationTime, start, end);
//    return timeSeries.toLocalDateDoubleTimeSeries();
//  }
  
  private DoubleTimeSeries<Date> loadTimeSeries(IdentifierBundle identifiers,
      String dataSource, String dataProvider, String field,
      String observationTime, LocalDate start, LocalDate end) {
    ArgumentChecker.notNull(identifiers, "identifiers");
    ArgumentChecker.notNull(dataSource, "dataSource");
    ArgumentChecker.notNull(dataProvider, "dataProvider");
    ArgumentChecker.notNull(field, "field");
    
    s_logger.debug("getting timeseries for identifiers={} dataSource={} dataProvider={} dataField={} observationTime={}", 
        new Object[]{identifiers, dataSource, dataProvider, field, observationTime});
    
    int timeSeriesKey = getTimeSeriesKeyIDByIdentifierBundle(identifiers, dataSource, dataProvider, field, observationTime);
    if (timeSeriesKey == INVALID_KEY) {
      s_logger.debug("empty timeseries returned for identifiers={}, dataSource={} dataProvider={} dataField={} observationTime={}", 
          new Object[]{identifiers, dataSource, dataProvider, field, observationTime});
      return ArraySQLDateDoubleTimeSeries.EMPTY_SERIES;
    }
  
    String sql = null;
    Map<String, String> sqlQueries = getSqlQueries();
    
    if (start != null & end != null) {
        sql = sqlQueries.get(LOAD_TIME_SERIES_WITH_DATES);
    }  else {
      sql = sqlQueries.get(LOAD_TIME_SERIES);
    }
    
    MapSqlParameterSource parameters = new MapSqlParameterSource().addValue("timeSeriesKey", timeSeriesKey, Types.INTEGER);
    if (start != null & end != null) {
      parameters.addValue("startDate", toSQLDate(start), Types.DATE);
      parameters.addValue("endDate", toSQLDate(end), Types.DATE);
    }
    
    final List<Date> dates = new ArrayList<Date>();
    final List<Double> values = new ArrayList<Double>();
    
    NamedParameterJdbcOperations parameterJdbcOperations = _simpleJdbcTemplate.getNamedParameterJdbcOperations();
    parameterJdbcOperations.query(sql, parameters, new RowCallbackHandler() {
      
      @Override
      public void processRow(ResultSet rs) throws SQLException {
        values.add(rs.getDouble("value"));
        dates.add(rs.getDate("ts_date"));
      }
    });
    
    return new ArraySQLDateDoubleTimeSeries(dates, values);
  }

  @Override
  public void updateDataPoint(IdentifierBundle identifiers,
      String dataSource, String dataProvider, String field,
      String observationTime, LocalDate date, Double value) {
    
    ArgumentChecker.notNull(identifiers, "identifiers");
    ArgumentChecker.notNull(dataSource, "dataSource");
    ArgumentChecker.notNull(dataProvider, "dataProvider");
    ArgumentChecker.notNull(field, "field");
    ArgumentChecker.notNull(observationTime, "observationTime");
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(value, "value");
    
    s_logger.debug("updating dataPoint for identifier={} dataSource={} dataProvider={} dataField={} observationTime={} with values(date={}, value={})", 
        new Object[]{identifiers, dataSource, dataProvider, field, observationTime, date, value});
    
    int tsID = getTimeSeriesKeyIDByIdentifierBundle(identifiers, dataSource, dataProvider, field, observationTime);
    
    Map<String, String> sqlQueries = getSqlQueries();
    String selectSQL = sqlQueries.get(FIND_DATA_POINT_BY_DATE_AND_ID);
    
    MapSqlParameterSource parameters = new MapSqlParameterSource()
    .addValue("tsID", tsID, Types.INTEGER)
    .addValue("date", toSQLDate(date), Types.DATE);
    
    Double oldValue = _simpleJdbcTemplate.queryForObject(selectSQL, Double.class, parameters);
    
    String updateSql = sqlQueries.get(UPDATE_TIME_SERIES);
    String insertDelta = sqlQueries.get(INSERT_TIME_SERIES_DELTA_U);
    
    Date now = new Date(System.currentTimeMillis());
    s_logger.debug("timeStamp = {}", DATE_FORMAT.format(now));
    
    parameters.addValue("timeStamp", now, Types.TIMESTAMP);
    parameters.addValue("oldValue", oldValue, Types.DOUBLE);
    parameters.addValue("newValue", value, Types.DOUBLE);
    
    //start transaction
    TransactionStatus transactionStatus = _transactionManager.getTransaction(_transactionDefinition);
    try {
      _simpleJdbcTemplate.update(updateSql, parameters);
      _simpleJdbcTemplate.update(insertDelta, parameters);
      _transactionManager.commit(transactionStatus);
    } catch (Throwable t) {
      _transactionManager.rollback(transactionStatus);
      s_logger.warn("error trying to update dataPoint", t);
      throw new OpenGammaRuntimeException("Unable to update dataPoint", t);
    }
    
  }
  
  @Override
  public void deleteDataPoint(IdentifierBundle identifiers,
      String dataSource, String dataProvider, String field,
      String observationTime, LocalDate date) {
    ArgumentChecker.notNull(identifiers, "identifier");
    ArgumentChecker.notNull(dataSource, "dataSource");
    ArgumentChecker.notNull(dataProvider, "dataProvider");
    ArgumentChecker.notNull(field, "field");
    ArgumentChecker.notNull(observationTime, "observationTime");
    ArgumentChecker.notNull(date, "date");
    
    s_logger.debug("deleting dataPoint for identifier={} dataSource={} dataProvider={} dataField={} observationTime={} date={}", 
        new Object[]{identifiers, dataSource, dataProvider, field, observationTime, date});
    
    int tsID = getTimeSeriesKeyIDByIdentifierBundle(identifiers, dataSource, dataProvider, field, observationTime);
    
    Date sqlDate = toSQLDate(date);
    
    Map<String, String> sqlQueries = getSqlQueries();
    String selectTSSQL = sqlQueries.get(FIND_DATA_POINT_BY_DATE_AND_ID);
    MapSqlParameterSource parameters = new MapSqlParameterSource().addValue("tsID", tsID, Types.INTEGER);
    parameters.addValue("date", sqlDate, Types.DATE);
    
    Double oldValue = _simpleJdbcTemplate.queryForObject(selectTSSQL, Double.class, parameters);
    
    String deleteSql = sqlQueries.get(DELETE_DATA_POINT);
    String insertDelta = sqlQueries.get(INSERT_TIME_SERIES_DELTA_D);
    
    Date now = new Date(System.currentTimeMillis());
    s_logger.debug("timeStamp = {}", DATE_FORMAT.format(now));
    
    MapSqlParameterSource deltaParameters = new MapSqlParameterSource();
    deltaParameters.addValue("timeSeriesID", tsID, Types.INTEGER);
    deltaParameters.addValue("date", sqlDate, Types.DATE);
    deltaParameters.addValue("value", oldValue, Types.DOUBLE);
    deltaParameters.addValue("timeStamp", now, Types.TIMESTAMP);
    
    //start transaction
    TransactionStatus transactionStatus = _transactionManager.getTransaction(_transactionDefinition);
    try {
      _simpleJdbcTemplate.update(insertDelta, deltaParameters);
      _simpleJdbcTemplate.update(deleteSql, parameters);
      _transactionManager.commit(transactionStatus);
    } catch (Throwable t) {
      _transactionManager.rollback(transactionStatus);
      s_logger.warn("error trying to delete dataPoint", t);
      throw new OpenGammaRuntimeException("Unable to delete dataPoint", t);
    }
    
  }
  
  @Override
  public LocalDateDoubleTimeSeries getHistoricalTimeSeries(IdentifierBundle dsids, String dataSource,
      String dataProvider, String field) {
    return getHistoricalTimeSeries(dsids, dataSource, dataProvider, field, null, null);
  }

  @Override
  public LocalDateDoubleTimeSeries getHistoricalTimeSeries(IdentifierBundle dsids, String dataSource,
      String dataProvider, String field, LocalDate start, LocalDate end) {
    //load timeseries without observationTime field
    DoubleTimeSeries<Date> timeSeries = loadTimeSeries(dsids, dataSource, dataProvider, field, null, start, end);
    return timeSeries.toLocalDateDoubleTimeSeries();
  }
  

  @Override
  public LocalDateDoubleTimeSeries getTimeSeriesSnapShot(
      IdentifierBundle identifiers, String dataSource,
      String dataProvider, String field, String observationTime,
      ZonedDateTime timeStamp) {
    
    ArgumentChecker.notNull(identifiers, "identifierss");
    ArgumentChecker.notNull(dataSource, "dataSource");
    ArgumentChecker.notNull(dataProvider, "dataProvider");
    ArgumentChecker.notNull(field, "field");
    ArgumentChecker.notNull(observationTime, "observationTime");
    ArgumentChecker.notNull(timeStamp, "time");
    
    int tsID = getTimeSeriesKeyIDByIdentifierBundle(identifiers, dataSource, dataProvider, field, observationTime);
    
    Map<String, String> sqlQueries = getSqlQueries();
    String selectDeltaSql = sqlQueries.get(LOAD_TIME_SERIES_DELTA);
    
    MapSqlParameterSource parameterSource = new MapSqlParameterSource();
    parameterSource.addValue("time", toSQLDate(timeStamp), Types.TIMESTAMP);
    parameterSource.addValue("tsID", tsID, Types.INTEGER);

    final List<Date> deltaDates = new ArrayList<Date>();
    final List<Double> deltaValues = new ArrayList<Double>();
    final List<String> deltaOperations = new ArrayList<String>();
    
    NamedParameterJdbcOperations jdbcOperations = _simpleJdbcTemplate.getNamedParameterJdbcOperations();
    
    jdbcOperations.query(selectDeltaSql, parameterSource, new RowCallbackHandler() {
      @Override
      public void processRow(ResultSet rs) throws SQLException {
        deltaDates.add(rs.getDate("ts_date"));
        deltaValues.add(rs.getDouble("old_value"));
        deltaOperations.add(rs.getString("operation"));
      }
    });
    
    DoubleTimeSeries<Date> timeSeries = loadTimeSeries(identifiers, dataSource, dataProvider, field, observationTime, null, null);
    
    MapSQLDateDoubleTimeSeries tsMap = new MapSQLDateDoubleTimeSeries(timeSeries);
    
    //reapply deltas
    for (int i = 0; i < deltaDates.size(); i++) {
      Date date = deltaDates.get(i);
      Double oldValue = deltaValues.get(i);
      String operation = deltaOperations.get(i);
      if (operation.toUpperCase().equals("I")) {
        tsMap.removeDataPoint(date);
      }
      if (operation.toUpperCase().equals("D") || operation.toUpperCase().equals("U")) {
        tsMap.putDataPoint(date, oldValue);
      }
    }
    
    return tsMap.toLocalDateDoubleTimeSeries();
  }
  
  private Date toSQLDate(LocalDate localDate) {
    return new Date(localDate.toEpochDays() * MILLIS_IN_DAY);
  }
  
  private Date toSQLDate(ZonedDateTime date) {
    return new Date(date.toInstant().toEpochMillisLong());
  }
}
