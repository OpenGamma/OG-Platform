/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.enginedb.stats;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.threeten.bp.Clock;
import org.threeten.bp.Instant;

import com.opengamma.elsql.ElSqlBundle;
import com.opengamma.engine.calcnode.stats.FunctionCostsDocument;
import com.opengamma.engine.calcnode.stats.FunctionCostsMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.DbConnector;
import com.opengamma.util.db.DbDateUtils;
import com.opengamma.util.db.DbMapSqlParameterSource;

/**
 * Database storage of function costs.
 * <p>
 * This implementation supports history.
 */
public class DbFunctionCostsMaster implements FunctionCostsMaster {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(DbFunctionCostsMaster.class);

  /**
   * External SQL bundle.
   */
  private ElSqlBundle _externalSqlBundle;
  /**
   * The database connector.
   */
  private final DbConnector _dbConnector;
  /**
   * The time-source to use.
   */
  private Clock _timeSource = Clock.systemUTC();

  /**
   * Creates an instance.
   * 
   * @param dbConnector  the database connector, not null
   */
  public DbFunctionCostsMaster(final DbConnector dbConnector) {
    ArgumentChecker.notNull(dbConnector, "dbConnector");
    s_logger.debug("installed DbConnector: {}", dbConnector);
    _dbConnector = dbConnector;
    _externalSqlBundle = ElSqlBundle.of(dbConnector.getDialect().getElSqlConfig(), DbFunctionCostsMaster.class);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the external SQL bundle.
   * 
   * @return the external SQL bundle, not null
   */
  public ElSqlBundle getElSqlBundle() {
    return _externalSqlBundle;
  }

  /**
   * Sets the external SQL bundle.
   * 
   * @param bundle  the external SQL bundle, not null
   */
  public void setElSqlBundle(ElSqlBundle bundle) {
    _externalSqlBundle = bundle;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the database connector.
   * 
   * @return the database connector, not null
   */
  public DbConnector getDbConnector() {
    return _dbConnector;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the time-source that determines the current time.
   * 
   * @return the time-source, not null
   */
  public Clock getClock() {
    return _timeSource;
  }

  /**
   * Sets the time-source.
   * 
   * @param timeSource  the time-source, not null
   */
  public void setClock(final Clock timeSource) {
    ArgumentChecker.notNull(timeSource, "timeSource");
    s_logger.debug("installed Clock: {}", timeSource);
    _timeSource = timeSource;
  }

  //-------------------------------------------------------------------------
  public int getSchemaVersion() {
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource().addValue("version_key", "schema_patch");
    final NamedParameterJdbcOperations namedJdbc = getDbConnector().getJdbcTemplate();
    final String sql = getElSqlBundle().getSql("GetSchemaVersion", args);
    String version = namedJdbc.queryForObject(sql, args, String.class);
    return Integer.parseInt(version);
  }
  
  //-------------------------------------------------------------------------
  @Override
  public FunctionCostsDocument load(final String configuration, final String functionId, Instant versionAsOf) {
    s_logger.debug("load: {} {}", configuration, functionId);
    if (versionAsOf == null) {
      versionAsOf = Instant.now(getClock());
    }
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addValue("configuration", configuration.trim())
      .addValue("function", functionId.trim())
      .addTimestamp("version_instant", versionAsOf)
      .addValue("paging_offset", 0)
      .addValue("paging_fetch", 1);
    final FunctionCostsDocumentExtractor extractor = new FunctionCostsDocumentExtractor();
    final NamedParameterJdbcOperations namedJdbc = getDbConnector().getJdbcTemplate();
    final String sql = getElSqlBundle().getSql("GetCosts", args);
    final List<FunctionCostsDocument> docs = namedJdbc.query(sql, args, extractor);
    return docs.isEmpty() ? null : docs.get(0);
  }

  //-------------------------------------------------------------------------
  @Override
  public FunctionCostsDocument store(final FunctionCostsDocument costs) {
    ArgumentChecker.notNull(costs, "costs");
    ArgumentChecker.notNull(costs.getConfigurationName(), "costs.configurationName");
    ArgumentChecker.notNull(costs.getFunctionId(), "costs.functionId");
    costs.setVersion(Instant.now(getClock()));
    
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addValue("configuration", costs.getConfigurationName().trim())
      .addValue("function", costs.getFunctionId().trim())
      .addTimestamp("version_instant", costs.getVersion())
      .addValue("invocation_cost", costs.getInvocationCost())
      .addValue("data_input_cost", costs.getDataInputCost())
      .addValue("data_output_cost", costs.getDataOutputCost());
    final String sql = getElSqlBundle().getSql("InsertCosts", args);
    getDbConnector().getJdbcTemplate().update(sql, args);
    
    // delete older data
    final DbMapSqlParameterSource deleteArgs = new DbMapSqlParameterSource()
      .addValue("configuration", costs.getConfigurationName().trim())
      .addValue("function", costs.getFunctionId().trim())
      .addValue("offset_zero", 0)
      .addValue("keep_rows", 20);
    final String deleteSql = getElSqlBundle().getSql("DeleteOld", deleteArgs);
    getDbConnector().getJdbcTemplate().update(deleteSql, deleteArgs);
    
    return costs;
  }

  //-------------------------------------------------------------------------
  /**
   * Returns a string summary of this master.
   * 
   * @return the string summary, not null
   */
  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

  //-------------------------------------------------------------------------
  /**
   * Mapper from SQL rows to a FunctionCostsDocument.
   */
  protected final class FunctionCostsDocumentExtractor implements ResultSetExtractor<List<FunctionCostsDocument>> {
    private List<FunctionCostsDocument> _documents = new ArrayList<FunctionCostsDocument>();

    @Override
    public List<FunctionCostsDocument> extractData(final ResultSet rs) throws SQLException, DataAccessException {
      while (rs.next()) {
        FunctionCostsDocument doc = new FunctionCostsDocument();
        doc.setConfigurationName(rs.getString("CONFIGURATION"));
        doc.setFunctionId(rs.getString("FUNCTION_NAME"));
        doc.setVersion(DbDateUtils.fromSqlTimestamp(rs.getTimestamp("VERSION_INSTANT")));
        doc.setInvocationCost(rs.getDouble("INVOCATION_COST"));
        doc.setDataInputCost(rs.getDouble("DATA_INPUT_COST"));
        doc.setDataOutputCost(rs.getDouble("DATA_OUTPUT_COST"));
        _documents.add(doc);
      }
      return _documents;
    }
  }

}
