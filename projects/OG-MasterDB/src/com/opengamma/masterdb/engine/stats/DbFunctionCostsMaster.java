/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb.engine.stats;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.time.Instant;
import javax.time.TimeSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import com.opengamma.engine.view.calcnode.stats.FunctionCostsDocument;
import com.opengamma.engine.view.calcnode.stats.FunctionCostsMaster;
import com.opengamma.extsql.ExtSqlBundle;
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
  private ExtSqlBundle _externalSqlBundle;
  /**
   * The database connector.
   */
  private final DbConnector _dbConnector;
  /**
   * The time-source to use.
   */
  private TimeSource _timeSource = TimeSource.system();

  /**
   * Creates an instance.
   * 
   * @param dbConnector  the database connector, not null
   */
  public DbFunctionCostsMaster(final DbConnector dbConnector) {
    ArgumentChecker.notNull(dbConnector, "dbConnector");
    s_logger.debug("installed DbConnector: {}", dbConnector);
    _dbConnector = dbConnector;
    _externalSqlBundle = ExtSqlBundle.of(dbConnector.getDialect().getExtSqlConfig(), DbFunctionCostsMaster.class);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the external SQL bundle.
   * 
   * @return the external SQL bundle, not null
   */
  public ExtSqlBundle getExtSqlBundle() {
    return _externalSqlBundle;
  }

  /**
   * Sets the external SQL bundle.
   * 
   * @param bundle  the external SQL bundle, not null
   */
  public void setExtSqlBundle(ExtSqlBundle bundle) {
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
  public TimeSource getTimeSource() {
    return _timeSource;
  }

  /**
   * Sets the time-source.
   * 
   * @param timeSource  the time-source, not null
   */
  public void setTimeSource(final TimeSource timeSource) {
    ArgumentChecker.notNull(timeSource, "timeSource");
    s_logger.debug("installed TimeSource: {}", timeSource);
    _timeSource = timeSource;
  }

  //-------------------------------------------------------------------------
  @Override
  public FunctionCostsDocument load(final String configuration, final String functionId, Instant versionAsOf) {
    s_logger.debug("load: {} {}", configuration, functionId);
    if (versionAsOf == null) {
      versionAsOf = Instant.now(getTimeSource());
    }
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addValue("configuration", configuration)
      .addValue("function", functionId)
      .addTimestamp("version_instant", versionAsOf)
      .addValue("paging_offset", 0)
      .addValue("paging_fetch", 1);
    final FunctionCostsDocumentExtractor extractor = new FunctionCostsDocumentExtractor();
    final NamedParameterJdbcOperations namedJdbc = getDbConnector().getJdbcTemplate().getNamedParameterJdbcOperations();
    final String sql = getExtSqlBundle().getSql("GetCosts", args);
    final List<FunctionCostsDocument> docs = namedJdbc.query(sql, args, extractor);
    return docs.isEmpty() ? null : docs.get(0);
  }

  //-------------------------------------------------------------------------
  @Override
  public FunctionCostsDocument store(final FunctionCostsDocument costs) {
    ArgumentChecker.notNull(costs, "costs");
    ArgumentChecker.notNull(costs.getConfigurationName(), "costs.configurationName");
    ArgumentChecker.notNull(costs.getFunctionId(), "costs.functionId");
    costs.setVersion(Instant.now(getTimeSource()));
    
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addValue("configuration", costs.getConfigurationName())
      .addValue("function", costs.getFunctionId())
      .addTimestamp("version_instant", costs.getVersion())
      .addValue("invocation_cost", costs.getInvocationCost())
      .addValue("data_input_cost", costs.getDataInputCost())
      .addValue("data_output_cost", costs.getDataOutputCost());
    final String sql = getExtSqlBundle().getSql("InsertCosts", args);
    getDbConnector().getJdbcTemplate().update(sql, args);
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
        doc.setFunctionId(rs.getString("FUNCTION"));
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
