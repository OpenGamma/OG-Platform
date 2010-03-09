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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import javax.sql.DataSource;
import javax.time.calendar.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.DomainSpecificIdentifier;
import com.opengamma.timeseries.ArrayDoubleTimeSeries;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.Pair;
import com.opengamma.util.time.DateUtil;

/**
 * 
 * 
 * @author yomi
 */
public abstract class RowStoreJdbcDao implements TimeSeriesDao {
  
  private static final Logger s_logger = LoggerFactory.getLogger(RowStoreJdbcDao.class);
  
  private static final String DESCRIPTION_COLUMN = "description";
  private static final String NAME_COLUMN = "name";
  private static final String ID_COLUMN = "id";
  private static final String IDENTIFIER_COLUMN = "identifier";
  private static final String DOMAIN_SPEC_IDENTIFIER_TABLE = "domain_spec_identifier"; 
  private static final String DATA_SOURCE_TABLE = "data_source";
  private static final String DATA_PROVIDER_TABLE = "data_provider";
  private static final String QUOTED_OBJECT_TABLE = "quoted_object";
  private static final String DATA_FIELD_TABLE = "data_field";
  private static final String OBSERVATION_TIME_TABLE = "observation_time";
  private static final String DOMAIN_TABLE = "domain";
  
  private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
  private static SimpleDateFormat YEAR_FORMAT = new SimpleDateFormat("yyyy");
  private static SimpleDateFormat MONTH_FORMAT = new SimpleDateFormat("MM");
  private static SimpleDateFormat DAY_FORMAT = new SimpleDateFormat("dd");

  private DataSourceTransactionManager _transactionManager;
  private SimpleJdbcTemplate _simpleJdbcTemplate;
  private TransactionDefinition _transactionDefinition = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRED);
  
    
  public RowStoreJdbcDao(DataSourceTransactionManager transactionManager) {
    ArgumentChecker.checkNotNull(transactionManager, "transactionManager");
    _transactionManager = transactionManager;
    DataSource dataSource = _transactionManager.getDataSource();
    _simpleJdbcTemplate = new SimpleJdbcTemplate(dataSource);   
  }

  private Date convertZonedDateTime(ZonedDateTime date) {
    ArgumentChecker.checkNotNull(date, "date");
    return new Date(date.toInstant().toEpochMillis());
  }
  
  private ZonedDateTime convertZonedDateTime(Date date) {
    ArgumentChecker.checkNotNull(date, "date");
    return DateUtil.getUTCDate(Integer.parseInt(YEAR_FORMAT.format(date)), Integer.parseInt(MONTH_FORMAT.format(date)), Integer.parseInt(DAY_FORMAT.format(date)));
  }

  @Override
  public void addTimeSeries(Set<DomainSpecificIdentifier> domainIdentifiers,
      String dataSource, String dataProvider, String field,
      String observationTime, final DoubleTimeSeries timeSeries) {
    ArgumentChecker.checkNotNull(domainIdentifiers, "domainIdentifiers");
    ArgumentChecker.checkNotNull(dataSource, "dataSource");
    ArgumentChecker.checkNotNull(dataProvider, "dataProvider");
    ArgumentChecker.checkNotNull(field, "field");
    ArgumentChecker.checkNotNull(observationTime, "observationTime");
    ArgumentChecker.checkNotNull(timeSeries, "timeSeries");

    s_logger.debug("adding timeseries for {} with dataSource={}, dataProvider={}, dataField={}, observationTime={}", 
        new Object[]{domainIdentifiers, dataSource, dataProvider, field, observationTime});
    String quotedObject = findQuotedObject(domainIdentifiers);
    if (quotedObject == null) {
      DomainSpecificIdentifier identifier = domainIdentifiers.iterator().next();
      quotedObject = identifier.getDomain().getDomainName() + "_" + identifier.getValue();
      createDomainSpecIdentifiers(domainIdentifiers, quotedObject);
    }
    int timeSeriesKeyID = getTimeSeriesKeyIDByQuotedObject(quotedObject, dataSource, dataProvider, field, observationTime);
    if (timeSeriesKeyID == -1) {
      createTimeSeriesKey(quotedObject, dataSource, dataProvider, field, observationTime);
      timeSeriesKeyID = getTimeSeriesKeyIDByQuotedObject(quotedObject, dataSource, dataProvider, field, observationTime);
    }
    
    String insertSQL = "INSERT INTO time_series_data (time_series_id, ts_date, value) VALUES (:timeSeriesID, :date, :value)";
    String insertDelta = "INSERT INTO time_series_data_delta (time_series_id, time_stamp, ts_date, old_value, operation) VALUES (:timeSeriesID, :timeStamp, :date, :value, 'I')";
    
    Date now = new Date(System.currentTimeMillis());
    s_logger.debug("timeStamp = {}", DATE_FORMAT.format(now));
    
    SqlParameterSource[] batchArgs = new MapSqlParameterSource[timeSeries.size()];
    for (int i = 0; i < timeSeries.size(); i++) {
      ZonedDateTime time = timeSeries.getTime(i);
      Double value = timeSeries.getValue(i);
      MapSqlParameterSource parameterSource = new MapSqlParameterSource();
      parameterSource.addValue("timeSeriesID", timeSeriesKeyID, Types.INTEGER);
      parameterSource.addValue("date", convertZonedDateTime(time), Types.DATE);
      parameterSource.addValue("value", value, Types.DOUBLE);
      parameterSource.addValue("timeStamp", now, Types.TIMESTAMP);
      batchArgs[i] = parameterSource;
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
        s_logger.warn("error trying to insert timeSeries", t);
        throw new OpenGammaRuntimeException("Unable to add Timeseries", t);
      }
    } else {
      _simpleJdbcTemplate.batchUpdate(insertSQL, batchArgs);
    }
    
  }

  protected abstract boolean isTriggerSupported();

  @Override
  public String findDataFieldByID(int id) {
    return findNamedDimensionByID(DATA_FIELD_TABLE, id);
  }

  @Override
  public int createDataProvider(String dataProvider, String description) {
    insertNamedDimension(DATA_PROVIDER_TABLE, dataProvider, description);
    return getDataProviderID(dataProvider);
  }

  /**
   * @param dataProvider
   * @param description
   */
  private void insertNamedDimension(String tableName, String name, String description) {
    ArgumentChecker.checkNotNull(tableName, "table");
    ArgumentChecker.checkNotNull(name, "name");
    s_logger.debug("inserting into table={} values({}, {})", new Object[]{tableName, name, description});
    String sql = "INSERT INTO " + tableName + " (" + NAME_COLUMN + ", " + DESCRIPTION_COLUMN + ") VALUES (:name, :description)";
    SqlParameterSource parameters = new MapSqlParameterSource()
      .addValue("name", name)
      .addValue("description", description);

    _simpleJdbcTemplate.update(sql, parameters);
  }

  @Override
  public String findDataProviderByID(int id) {
    return findNamedDimensionByID(DATA_PROVIDER_TABLE, id);
  }

  /**
   * @param id
   * @return
   */
  private String findNamedDimensionByID(String tableName, int id) {
    ArgumentChecker.checkNotNull(tableName, "table");
    s_logger.debug("looking up named dimension from table={} id={}", tableName, id);
    final StringBuffer sql = new StringBuffer("SELECT ").append(NAME_COLUMN).append(" FROM ").append(tableName).append(" WHERE ").append(ID_COLUMN).append(" = :id");
    SqlParameterSource parameters = new MapSqlParameterSource("id", id);
    String result = null;
    try {
      result = _simpleJdbcTemplate.queryForObject(sql.toString(), String.class, parameters);
    } catch (EmptyResultDataAccessException e) {
      s_logger.debug("Empty row return for id = {} from table = {}", id, tableName);
      result = null;
    }
    return result;
  }

  @Override
  public int createDataSource(String dataSource, String description) {
    insertNamedDimension(DATA_SOURCE_TABLE, dataSource, description);
    return getDataSourceID(dataSource);
  }

  @Override
  public String findDataSourceByID(int id) {
    return findNamedDimensionByID(DATA_SOURCE_TABLE, id);
  }
  
  protected String findQuotedObject(final Set<DomainSpecificIdentifier> domainIdentifiers) {
    String result = null;
    int size = domainIdentifiers.size();
    if (size < 1) {
      return result;
    }
    
    StringBuffer sqlBuffer = new StringBuffer();
    sqlBuffer.append("SELECT qo.name, count(qo.name) as count FROM ").append(QUOTED_OBJECT_TABLE).append(" qo, ")
    .append(DOMAIN_SPEC_IDENTIFIER_TABLE).append(" dsi, ").append(DOMAIN_TABLE).append(" d ")
    .append("WHERE d.id = dsi.domain_id AND qo.id = dsi.quoted_obj_id AND (");
    int orCounter = 1;
    Object[] parameters = new Object[size*2];
    int paramIndex = 0;
    for (DomainSpecificIdentifier domainSpecificIdentifier : domainIdentifiers) {
      sqlBuffer.append("(d.name = ? AND dsi.identifier = ?)");
      parameters[paramIndex++] = domainSpecificIdentifier.getDomain().getDomainName();
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
  public void createDomainSpecIdentifiers(final Set<DomainSpecificIdentifier> domainIdentifiers, final String quotedObj) {
    ArgumentChecker.checkNotNull(domainIdentifiers, "domainIdentifiers");
    //start transaction
    TransactionStatus transactionStatus = _transactionManager.getTransaction(_transactionDefinition);
    
    s_logger.debug("creating domainSpecIdentifiers {} for quotedObj={}", domainIdentifiers, quotedObj);
    try {
      //find existing identifiers
      Set<DomainSpecificIdentifier> resolvedIdentifiers = new HashSet<DomainSpecificIdentifier>(domainIdentifiers);
      int size = domainIdentifiers.size();
      if (size > 0) {
        StringBuffer sqlBuffer = new StringBuffer();
        sqlBuffer.append("SELECT qo.name, count(qo.name) as count FROM ").append(QUOTED_OBJECT_TABLE).append(" qo, ")
        .append(DOMAIN_SPEC_IDENTIFIER_TABLE).append(" dsi, ").append(DOMAIN_TABLE).append(" d ")
        .append("WHERE d.id = dsi.domain_id AND qo.id = dsi.quoted_obj_id AND (");
        int orCounter = 1;
        Object[] parameters = new Object[size*2];
        int paramIndex = 0;
        for (DomainSpecificIdentifier domainSpecificIdentifier : domainIdentifiers) {
          sqlBuffer.append("(d.name = ? AND dsi.identifier = ?)");
          parameters[paramIndex++] = domainSpecificIdentifier.getDomain().getDomainName();
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
          if (!loadedQuotedObject.equals(quotedObj)) {
            s_logger.warn("{} has been associated already with {}", loadedQuotedObject, domainIdentifiers);
            throw new OpenGammaRuntimeException(loadedQuotedObject + " has been associated already with " + domainIdentifiers);
          }
          long identifiersCount = Long.valueOf(String.valueOf(row.get("count")));
          if (identifiersCount != domainIdentifiers.size()) {
            Set<DomainSpecificIdentifier> loadeIdentifiers = findDomainSpecIdentifiersByQuotedObject(quotedObj);
            resolvedIdentifiers.removeAll(loadeIdentifiers);
          }
        }
        
      }
      if (getQuotedObjectID(quotedObj) == -1) {
        createQuotedObject(quotedObj, quotedObj);
      }
      
      SqlParameterSource[] batchArgs = new MapSqlParameterSource[resolvedIdentifiers.size()];
      int index = 0;
      for (DomainSpecificIdentifier domainSpecificIdentifier : resolvedIdentifiers) {
        String domainName = domainSpecificIdentifier.getDomain().getDomainName();
        if (getDomainID(domainName) == -1) {
          createDomain(domainName, domainName);
        }
        Map<String, Object> valueMap = new HashMap<String, Object>();
        valueMap.put("quotedObject", quotedObj);
        valueMap.put("domain", domainName);
        valueMap.put("identifier", domainSpecificIdentifier.getValue());
        batchArgs[index++] = new MapSqlParameterSource(valueMap);
      }
      
      String sql = "INSERT into " + DOMAIN_SPEC_IDENTIFIER_TABLE + 
        " (quoted_obj_id, domain_id, identifier) VALUES (" +
        "(SELECT id FROM " + QUOTED_OBJECT_TABLE + " WHERE name = :quotedObject) ," +
        "(SELECT id FROM " + DOMAIN_TABLE + " WHERE name = :domain), " +
        ":identifier)" ;
      
      _simpleJdbcTemplate.batchUpdate(sql, batchArgs);
      _transactionManager.commit(transactionStatus);
      
    } catch (Throwable t) {
      _transactionManager.rollback(transactionStatus);
      s_logger.warn("error trying to create domainSpecIdentifiers", t);
      throw new OpenGammaRuntimeException("Unable to create DomainSpecificIdentifiers", t);
    }
    
  }

  @Override
  public int createObservationTime(String observationTime, String description) {
    insertNamedDimension(OBSERVATION_TIME_TABLE, observationTime, description);
    return getObservationTimeID(observationTime);
  }

  @Override
  public int createQuotedObject(String name, String description) {
    insertNamedDimension(QUOTED_OBJECT_TABLE, name, description);
    return getQuotedObjectID(name);
  }

  @Override
  public int createDataField(String field, String description) {
    insertNamedDimension(DATA_FIELD_TABLE, field, description);
    return getDataFieldID(field);
  }

  @Override
  public Set<String> getAllDataProviders() {
    return getAllNamedDimensionNames(DATA_PROVIDER_TABLE);
  }

  @Override
  public Set<String> getAllDataSources() {
    return getAllNamedDimensionNames(DATA_SOURCE_TABLE);
  }

  /**
   * @return
   */
  private Set<String> getAllNamedDimensionNames(final String tableName) {
    ArgumentChecker.checkNotNull(tableName, "tableName");
    
    s_logger.debug("gettting all names from table = {}", tableName);
    
    final StringBuffer sql = new StringBuffer("SELECT ").append(NAME_COLUMN).append(" fROM ").append(tableName);

    List<Map<String, Object>> queryForList = _simpleJdbcTemplate.queryForList(
        sql.toString(), new Object[] {});
    Set<String> result = new TreeSet<String>();

    for (Map<String, Object> row : queryForList) {
      String name = (String) row.get(NAME_COLUMN);
      result.add(name);
    }

    return result;
  }
  
  @Override
  public String findQuotedObjectByID(int id) {
    return findNamedDimensionByID(QUOTED_OBJECT_TABLE, id);
  }
  
  @Override
  public String findObservationTimeByID(int id) {
    return findNamedDimensionByID(OBSERVATION_TIME_TABLE, id);
  }

  @Override
  public Set<String> getAllObservationTimes() {
    return getAllNamedDimensionNames(OBSERVATION_TIME_TABLE);
  }

  @Override
  public Set<String> getAllQuotedObjects() {
    return getAllNamedDimensionNames(QUOTED_OBJECT_TABLE);
  }

  @Override
  public Set<String> getAllTimeSeriesFields() {
    return getAllNamedDimensionNames(DATA_FIELD_TABLE);
  }

  @Override
  public int getDataProviderID(String name) {
    return getNamedDimensionID(DATA_PROVIDER_TABLE, name);
  }

  /**
   * @param name
   * @return
   */
  private int getNamedDimensionID(final String tableName, final String name) {
    ArgumentChecker.checkNotNull(tableName, "tableName");
    ArgumentChecker.checkNotNull(name, "name");
    
    s_logger.debug("looking up id from table={} with name={}", tableName, name);
    final StringBuffer sql = new StringBuffer("SELECT ").append(ID_COLUMN).append(" FROM ").append(tableName).append(" WHERE ").append(NAME_COLUMN).append(" = :name");
    SqlParameterSource parameters = new MapSqlParameterSource().addValue("name", name);

    int result = -1;
    try {
      result = _simpleJdbcTemplate.queryForInt(sql.toString(), parameters);
    } catch(EmptyResultDataAccessException e) {
      s_logger.debug("Empty row return for name = {} from table = {}", name, tableName);
      result = -1;
    }
    s_logger.debug("id = {}", result);
    return result;
  }

  @Override
  public int getDataSourceID(String name) {
    return getNamedDimensionID(DATA_SOURCE_TABLE, name);
  }

  @Override
  public int getDataFieldID(String name) {
    return getNamedDimensionID(DATA_FIELD_TABLE, name);
  }

  @Override
  public int getObservationTimeID(String name) {
    return getNamedDimensionID(OBSERVATION_TIME_TABLE, name);
  }

  @Override
  public int getQuotedObjectID(String name) {
    return getNamedDimensionID(QUOTED_OBJECT_TABLE, name);
  }

  
  
  @Override
  public Set<DomainSpecificIdentifier> findDomainSpecIdentifiersByQuotedObject(String name) {
    ArgumentChecker.checkNotNull(name, "name");
    
    s_logger.debug("looking up domainSpecIdentifiers using quotedObj={}", name);
    
    String sql = "SELECT d.name, dsi.identifier " +
    		         "FROM domain_spec_identifier dsi, domain d, quoted_object qo " +
    		         "WHERE dsi.domain_id = d.id " +
    		         "AND qo.id = dsi.quoted_obj_id " +
    		         "AND qo.name = :quotedObject ORDER BY d.name";
    
    SqlParameterSource parameterSource = new MapSqlParameterSource("quotedObject", name);
    
    List<Map<String, Object>> queryForList = _simpleJdbcTemplate.queryForList(sql, parameterSource);
    
    Set<DomainSpecificIdentifier> result = new HashSet<DomainSpecificIdentifier>();
    for (Map<String, Object> row : queryForList) {
      DomainSpecificIdentifier identifier = new DomainSpecificIdentifier((String)row.get(NAME_COLUMN), (String)row.get(IDENTIFIER_COLUMN));
      result.add(identifier);
    }
    return result;
  }
  
  @Override
  public int createDomain(String domain, String description) {
    insertNamedDimension(DOMAIN_TABLE, domain, description);
    return getDomainID(domain);
  }

  @Override
  public String findDomainByID(int id) {
    return findNamedDimensionByID(DOMAIN_TABLE, id);
  }

  @Override
  public Set<String> getAllDomains() {
    return getAllNamedDimensionNames(DOMAIN_TABLE);
  }

  @Override
  public int getDomainID(String name) {
    return getNamedDimensionID(DOMAIN_TABLE, name);
  }
  
  @Override
  public void createTimeSeriesKey(String quotedObject, String dataSource,
      String dataProvider, String dataField, String observationTime) {
    ArgumentChecker.checkNotNull(quotedObject, "quotedObject");
    ArgumentChecker.checkNotNull(dataSource, "dataSource");
    ArgumentChecker.checkNotNull(dataProvider, "dataProvider");
    ArgumentChecker.checkNotNull(dataField, "dataField");
    ArgumentChecker.checkNotNull(observationTime, "observationTime");
    
    s_logger.debug("creating timeSeriesKey with quotedObj={}, dataSource={}, dataProvider={}, dataField={}, observationTime={}", 
        new Object[]{quotedObject, dataSource, dataProvider, dataField, observationTime});
    
    if (getDataSourceID(dataSource) == -1) {
      createDataSource(dataSource, null);
    }
    if (getDataProviderID(dataProvider) == -1) {
      createDataProvider(dataProvider, null);
    }
    if (getDataFieldID(dataField) == -1) {
      createDataField(dataField, null);
    }
    if (getObservationTimeID(observationTime) == -1) {
      createObservationTime(observationTime, null);
    }
    String sql = "INSERT into time_series_key " +
    		" (quoted_obj_id, data_source_id, data_provider_id, data_field_id, observation_time_id)" +
    		" VALUES " +
    		"((SELECT id from quoted_object where name = :quotedObject)," +
    		" (SELECT id from data_source where name = :dataSource)," +
    		" (SELECT id from data_provider where name = :dataProvider)," +
    		" (SELECT id from data_field where name = :dataField)," +
    		" (SELECT id from observation_time where name = :observationTime))";
    
    SqlParameterSource parameterSource = new MapSqlParameterSource()
      .addValue("quotedObject", quotedObject)
      .addValue("dataSource", dataSource)
      .addValue("dataProvider", dataProvider)
      .addValue("dataField", dataField)
      .addValue("observationTime", observationTime);
    
    _simpleJdbcTemplate.update(sql, parameterSource);
  }
  
  protected int getTimeSeriesKeyIDByIdentifier(DomainSpecificIdentifier domainSpecId, String dataSource,
      String dataProvider, String dataField, String observationTime) {
    ArgumentChecker.checkNotNull(domainSpecId, "domainSpecId");
    ArgumentChecker.checkNotNull(dataSource, "dataSource");
    ArgumentChecker.checkNotNull(dataProvider, "dataProvider");
    ArgumentChecker.checkNotNull(dataField, "dataField");
    ArgumentChecker.checkNotNull(observationTime, "observationTime");
    
    int result = -1;
    
    s_logger.debug("Looking up timeSeriesKeyID by identifier with domainSpecId={}, dataSource={}, dataProvider={}, dataField={}, observationTime={}", 
        new Object[]{domainSpecId, dataSource, dataProvider, dataField, observationTime});
    
    String sql = "SELECT tsKey.id FROM time_series_key tsKey, quoted_object qo, domain_spec_identifier dsi," +
      " domain d, data_source ds, data_provider dp, data_field df, observation_time ot " +
      " WHERE dsi.domain_id = d.id AND dsi.quoted_obj_id = qo.id " +
      " AND tsKey.quoted_obj_id = qo.id " +
      " AND tsKey.data_source_id = ds.id " +
      " AND tsKey.data_provider_id = dp.id " +
      " AND tsKey.data_field_id = df.id" +
      " AND observation_time_id = ot.id " +
      " AND dsi.identifier = :identifier " +
      " AND d.name = :domain " +
      " AND ds.name = :dataSource " +
      " AND dp.name = :dataProvider " +
      " AND df.name = :dataField " +
      " AND ot.name = :observationTime ";
    
    MapSqlParameterSource parameters = new MapSqlParameterSource().addValue("identifier", domainSpecId.getValue())
    .addValue("domain", domainSpecId.getDomain().getDomainName(), Types.VARCHAR)
    .addValue("dataSource", dataSource, Types.VARCHAR)
    .addValue("dataProvider", dataProvider, Types.VARCHAR)
    .addValue("dataField", dataField, Types.VARCHAR)
    .addValue("observationTime", observationTime, Types.VARCHAR);
    
    result = _simpleJdbcTemplate.queryForInt(sql, parameters);
    s_logger.debug("timeSeriesKeyID = {}", result);
    return result;
  }
  
  protected int getTimeSeriesKeyIDByQuotedObject(String quotedObject, String dataSource,
      String dataProvider, String dataField, String observationTime) {
    ArgumentChecker.checkNotNull(quotedObject, "quotedObject");
    ArgumentChecker.checkNotNull(dataSource, "dataSource");
    ArgumentChecker.checkNotNull(dataProvider, "dataProvider");
    ArgumentChecker.checkNotNull(dataField, "dataField");
    ArgumentChecker.checkNotNull(observationTime, "observationTime");
    
    int result = -1;
    
    s_logger.debug("looking up timeSeriesKeyID by quotedObject with quotedObj={}, dataSource={}, dataProvider={}, dataField={}, observationTime={}", 
        new Object[]{quotedObject, dataSource, dataProvider, dataField, observationTime});
    
    String sql = "SELECT tskey.id FROM " +
    		" time_series_key tskey, " +
    		" quoted_object qo,  " +
    		" data_source ds, " +
    		" data_provider dp, " +
    		" data_field df, " +
    		" observation_time ot" +
    		" WHERE " +
    		" tskey.quoted_obj_id = qo.id " +
    		" AND tskey.data_source_id = ds.id " +
    		" AND tskey.data_provider_id = dp.id " +
    		" AND tskey.data_field_id = df.id " +
    		" AND tskey.observation_time_id = ot.id " +
    		" AND qo.name = :quotedObject " +
    		" AND ds.name = :dataSource " +
    		" AND dp.name = :dataProvider " +
    		" AND df.name = :dataField " +
    		" AND ot.name = :observationTime";
    
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
      result = -1;
    }
    
    s_logger.debug("timeSeriesKeyID = {}", result);
    return result;
  }
  
  @Override
  public void deleteTimeSeries(DomainSpecificIdentifier domainSpecId,
      String dataSource, String dataProvider, String field,
      String observationTime) {
    ArgumentChecker.checkNotNull(domainSpecId, "identifier");
    ArgumentChecker.checkNotNull(dataSource, "dataSource");
    ArgumentChecker.checkNotNull(dataProvider, "dataProvider");
    ArgumentChecker.checkNotNull(field, "field");
    ArgumentChecker.checkNotNull(observationTime, "observationTime");
    
    s_logger.debug("deleting timeseries for identifier={} dataSource={} dataProvider={} dataField={} observationTime={}", 
        new Object[]{domainSpecId, dataSource, dataProvider, field, observationTime});
    
    int tsID = getTimeSeriesKeyIDByIdentifier(domainSpecId, dataSource, dataProvider, field, observationTime);
    
    String selectTSSQL = "SELECT ts_date, value FROM time_series_data where time_series_id = :tsID";
    MapSqlParameterSource tsIDParameter = new MapSqlParameterSource().addValue("tsID", tsID, Types.INTEGER);
    
    List<Pair<Date, Double>> queryResult = _simpleJdbcTemplate.query(selectTSSQL, new ParameterizedRowMapper<Pair<Date, Double>>() {

      @Override
      public Pair<Date, Double> mapRow(ResultSet rs, int rowNum)
          throws SQLException {
        double tsValue = rs.getDouble("value");
        Date tsDate = rs.getDate("ts_date");
        return new Pair<Date, Double>(tsDate, tsValue);
      }
    }, tsIDParameter);
    
    String deleteSql = "DELETE FROM time_series_data WHERE time_series_id = :tsID";
    String insertDelta = "INSERT INTO time_series_data_delta (time_series_id, time_stamp, ts_date, old_value, operation) VALUES (:timeSeriesID, :timeStamp, :date, :value, 'D')";
    
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
  
  @Override
  public DoubleTimeSeries getTimeSeries(DomainSpecificIdentifier domainSpecId,
      String dataSource, String dataProvider, String field,
      String observationTime) {
    return getTimeSeries(domainSpecId, dataSource, dataProvider, field, observationTime, null, null);
  }

  @Override
  public DoubleTimeSeries getTimeSeries(DomainSpecificIdentifier domainSpecId,
      String dataSource, String dataProvider, String field,
      String observationTime, ZonedDateTime start, ZonedDateTime end) {
    ArgumentChecker.checkNotNull(domainSpecId, "identifier");
    ArgumentChecker.checkNotNull(dataSource, "dataSource");
    ArgumentChecker.checkNotNull(dataProvider, "dataProvider");
    ArgumentChecker.checkNotNull(field, "field");
    ArgumentChecker.checkNotNull(observationTime, "observationTime");
    
    s_logger.debug("getting timeseries for identifier={} dataSource={} dataProvider={} dataField={} observationTime={}", 
        new Object[]{domainSpecId, dataSource, dataProvider, field, observationTime});
    
    StringBuilder sql = new StringBuilder("SELECT ts_date, value FROM time_series_data tsd " +
        " WHERE time_series_id = (SELECT tsKey.id FROM time_series_key tsKey, quoted_object qo, domain_spec_identifier dsi," +
        " domain d, data_source ds, data_provider dp, data_field df, observation_time ot " +
        " WHERE dsi.domain_id = d.id AND dsi.quoted_obj_id = qo.id " +
        " AND tsKey.quoted_obj_id = qo.id " +
        " AND tsKey.data_source_id = ds.id " +
        " AND tsKey.data_provider_id = dp.id " +
        " AND tsKey.data_field_id = df.id" +
        " AND observation_time_id = ot.id " +
        " AND dsi.identifier = :identifier " +
        " AND d.name = :domain " +
        " AND ds.name = :dataSource " +
        " AND dp.name = :dataProvider " +
        " AND df.name = :dataField " +
        " AND ot.name = :observationTime)");
    
    if (start != null & end != null) {
        sql.append(" AND ( tsd.ts_date >= :startDate AND ts_date <= :endDate )");
    } 
    
    sql.append(" ORDER BY ts_date");
       
    MapSqlParameterSource parameters = new MapSqlParameterSource().addValue("identifier", domainSpecId.getValue())
      .addValue("domain", domainSpecId.getDomain().getDomainName())
      .addValue("dataSource", dataSource)
      .addValue("dataProvider", dataProvider)
      .addValue("dataField", field)
      .addValue("observationTime", observationTime);
    if (start != null & end != null) {
      parameters.addValue("startDate", convertZonedDateTime(start));
      parameters.addValue("endDate", convertZonedDateTime(end));
    }
    
    List<TimeSeriesData> queryResult = _simpleJdbcTemplate.query(sql.toString(), new ParameterizedRowMapper<TimeSeriesData>() {

      @Override
      public TimeSeriesData mapRow(ResultSet rs, int rowNum)
          throws SQLException {
        double tsValue = rs.getDouble("value");
        Date tsDate = rs.getDate("ts_date");
        return new TimeSeriesData(tsDate, tsValue);
      }
    }, parameters);
    
    List<ZonedDateTime> times = new ArrayList<ZonedDateTime>();
    List<Double> values = new ArrayList<Double>();
    for (TimeSeriesData tsData : queryResult) {
      double tsValue = tsData.getValue();
      values.add(tsValue);
      times.add(DateUtil.getUTCDate(tsData.getYear(), tsData.getMonth(), tsData.getDay()));
    }
    
    return new ArrayDoubleTimeSeries(times, values);
  }

  @Override
  public void updateDataPoint(DomainSpecificIdentifier domainSpecId,
      String dataSource, String dataProvider, String field,
      String observationTime, ZonedDateTime date, Double value) {
    ArgumentChecker.checkNotNull(domainSpecId, "identifier");
    ArgumentChecker.checkNotNull(dataSource, "dataSource");
    ArgumentChecker.checkNotNull(dataProvider, "dataProvider");
    ArgumentChecker.checkNotNull(field, "field");
    ArgumentChecker.checkNotNull(observationTime, "observationTime");
    ArgumentChecker.checkNotNull(date, "date");
    ArgumentChecker.checkNotNull(value, "value");
    
    s_logger.debug("updating dataPoint for identifier={} dataSource={} dataProvider={} dataField={} observationTime={} with values(date={}, value={})", 
        new Object[]{domainSpecId, dataSource, dataProvider, field, observationTime, date, value});
    
    int tsID = getTimeSeriesKeyIDByIdentifier(domainSpecId, dataSource, dataProvider, field, observationTime);
    
    String selectSQL = "SELECT value FROM time_series_data WHERE time_series_id = :tsID AND ts_date = :date";
    
    MapSqlParameterSource parameters = new MapSqlParameterSource()
    .addValue("tsID", tsID, Types.INTEGER)
    .addValue("date", convertZonedDateTime(date), Types.DATE);
    
    Double oldValue = _simpleJdbcTemplate.queryForObject(selectSQL, Double.class, parameters);
    
    
    String updateSql = "UPDATE time_series_data " +
      " SET value = :newValue " +
      " WHERE time_series_id = :tsID " +
      " AND ts_date = :date ";
    
    String insertDelta = "INSERT INTO time_series_data_delta (time_series_id, time_stamp, ts_date, old_value, operation) VALUES (:tsID, :timeStamp, :date, :oldValue, 'U')";
    
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
  public void deleteDataPoint(DomainSpecificIdentifier domainSpecId,
      String dataSource, String dataProvider, String field,
      String observationTime, ZonedDateTime date) {
    ArgumentChecker.checkNotNull(domainSpecId, "identifier");
    ArgumentChecker.checkNotNull(dataSource, "dataSource");
    ArgumentChecker.checkNotNull(dataProvider, "dataProvider");
    ArgumentChecker.checkNotNull(field, "field");
    ArgumentChecker.checkNotNull(observationTime, "observationTime");
    ArgumentChecker.checkNotNull(date, "date");
    
    s_logger.debug("deleting dataPoint for identifier={} dataSource={} dataProvider={} dataField={} observationTime={} date={}", 
        new Object[]{domainSpecId, dataSource, dataProvider, field, observationTime, date});
    
    int tsID = getTimeSeriesKeyIDByIdentifier(domainSpecId, dataSource, dataProvider, field, observationTime);
    
    Date sqlDate = convertZonedDateTime(date);
    
    String selectTSSQL = "SELECT value FROM time_series_data where time_series_id = :tsID AND ts_date = :date";
    MapSqlParameterSource parameters = new MapSqlParameterSource().addValue("tsID", tsID, Types.INTEGER);
    parameters.addValue("date", sqlDate, Types.DATE);
    
    Double oldValue = _simpleJdbcTemplate.queryForObject(selectTSSQL, Double.class, parameters);
    
    String deleteSql = "DELETE FROM time_series_data WHERE time_series_id = :tsID AND ts_date = :date";
    String insertDelta = "INSERT INTO time_series_data_delta (time_series_id, time_stamp, ts_date, old_value, operation) VALUES (:timeSeriesID, :timeStamp, :date, :value, 'D')";
    
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
  public DoubleTimeSeries getTimeSeriesSnapShot(
      DomainSpecificIdentifier domainSpecId, String dataSource,
      String dataProvider, String field, String observationTime,
      ZonedDateTime timeStamp) {
    
    ArgumentChecker.checkNotNull(domainSpecId, "identifier");
    ArgumentChecker.checkNotNull(dataSource, "dataSource");
    ArgumentChecker.checkNotNull(dataProvider, "dataProvider");
    ArgumentChecker.checkNotNull(field, "field");
    ArgumentChecker.checkNotNull(observationTime, "observationTime");
    ArgumentChecker.checkNotNull(timeStamp, "time");
    
    DoubleTimeSeries latestTimeSeries = getTimeSeries(domainSpecId, dataSource, dataProvider, field, observationTime);
    
    int tsID = getTimeSeriesKeyIDByIdentifier(domainSpecId, dataSource, dataProvider, field, observationTime);
    
    String selectDeltaSql = "SELECT ts_date, old_value, operation FROM time_series_data_delta WHERE time_series_id = :tsID AND time_stamp >= :time ORDER BY time_stamp desc";
    
    MapSqlParameterSource parameterSource = new MapSqlParameterSource();
    parameterSource.addValue("time", convertZonedDateTime(timeStamp), Types.TIMESTAMP);
    parameterSource.addValue("tsID", tsID, Types.INTEGER);

    List<Map<String, Object>> deltas = _simpleJdbcTemplate.queryForList(selectDeltaSql, parameterSource);
    
    
    Map<ZonedDateTime, Double> tsMap = new TreeMap<ZonedDateTime, Double>();
    for (int i = 0; i < latestTimeSeries.size(); i++) {
      tsMap.put(latestTimeSeries.getTime(i), latestTimeSeries.getValue(i));
    }
    
    //reapply deltas
    for (Map<String, Object> mapDelta : deltas) {
      Date date = (Date)mapDelta.get("ts_date");
      Double oldValue = (Double)mapDelta.get("old_value");
      String operation = (String)mapDelta.get("operation");
      if (operation.toUpperCase().equals("I")) {
        tsMap.remove(convertZonedDateTime(date));
      }
      if (operation.toUpperCase().equals("D") || operation.toUpperCase().equals("U")) {
        tsMap.put(convertZonedDateTime(date), oldValue);
      }
    }
    
    List<ZonedDateTime> dates = new ArrayList<ZonedDateTime>();
    List<Double> values = new ArrayList<Double>();
    
    for (Entry<ZonedDateTime, Double> entry : tsMap.entrySet()) {
      dates.add(entry.getKey());
      values.add(entry.getValue());
    }
    
    return new ArrayDoubleTimeSeries(dates, values);
  }

  private static class TimeSeriesData {
    private Date _date;
    private double _value;
    
   
    public TimeSeriesData(Date date, double value) {
      ArgumentChecker.checkNotNull(date, "date");
      _date = date;
      _value = value;
    }
    
    public double getValue() {
      return _value;
    }
    
    public int getYear() {
      return Integer.parseInt(YEAR_FORMAT.format(_date));
    }
    
    public int getMonth() {
      return Integer.parseInt(MONTH_FORMAT.format(_date));
    }
    
    public int getDay() {
      return Integer.parseInt(DAY_FORMAT.format(_date));
    }
    
    
    
  }
  
}
