/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.master.db;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javax.time.TimeSource;

import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.financial.position.master.FullPortfolioGetRequest;
import com.opengamma.financial.position.master.FullPortfolioNodeGetRequest;
import com.opengamma.financial.position.master.FullPositionGetRequest;
import com.opengamma.financial.position.master.FullTradeGetRequest;
import com.opengamma.financial.position.master.PortfolioTreeDocument;
import com.opengamma.financial.position.master.PortfolioTreeHistoryRequest;
import com.opengamma.financial.position.master.PortfolioTreeHistoryResult;
import com.opengamma.financial.position.master.PortfolioTreeSearchRequest;
import com.opengamma.financial.position.master.PortfolioTreeSearchResult;
import com.opengamma.financial.position.master.PositionDocument;
import com.opengamma.financial.position.master.PositionMaster;
import com.opengamma.financial.position.master.PositionHistoryRequest;
import com.opengamma.financial.position.master.PositionHistoryResult;
import com.opengamma.financial.position.master.PositionSearchRequest;
import com.opengamma.financial.position.master.PositionSearchResult;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.db.DbHelper;

/**
 * Base worker class for the position master.
 * <p>
 * This is designed to allow each position master method to be implemented by a
 * different class and easily replaced by an alternative.
 * <p>
 * The API of this class follows {@link PositionMaster}.
 * Each of the methods should be implemented as per the documentation on the master.
 * The parameters to the methods will be pre-checked for nulls before the worker is called,
 * including any internal required values in request or document objects.
 * <p>
 * This base implementation throws {@code UnsupportedOperationException} from each method.
 * As a result, subclasses only need to implement those methods they want to.
 */
public class DbPositionMasterWorker {

  /**
   * The main master.
   */
  private DbPositionMaster _master;

  /**
   * Creates an instance.
   */
  protected DbPositionMasterWorker() {
  }

  /**
   * Initializes the instance.
   * @param master  the position master, not null
   */
  protected void init(final DbPositionMaster master) {
    _master = master;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the parent master.
   * @return the parent, not null
   */
  protected DbPositionMaster getMaster() {
    return _master;
  }

  /**
   * Gets the database template.
   * @return the database template, non-null if correctly initialized
   */
  protected SimpleJdbcTemplate getJdbcTemplate() {
    return _master.getDbSource().getJdbcTemplate();
  }

  /**
   * Gets the transaction template.
   * @return the transaction template, non-null if correctly initialized
   */
  protected TransactionTemplate getTransactionTemplate() {
    return _master.getDbSource().getTransactionTemplate();
  }

  /**
   * Gets the database helper.
   * @return the helper, non-null if correctly initialized
   */
  protected DbHelper getDbHelper() {
    return _master.getDbSource().getDialect();
  }

  /**
   * Gets the time-source that determines the current time.
   * @return the time-source, not null
   */
  protected TimeSource getTimeSource() {
    return _master.getTimeSource();
  }

  /**
   * Gets the scheme in use for UniqueIdentifier.
   * @return the scheme, not null
   */
  protected String getIdentifierScheme() {
    return _master.getIdentifierScheme();
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the next database id.
   * @return the next database id
   */
  protected long nextId() {
    return getJdbcTemplate().queryForLong(getDbHelper().sqlNextSequenceValueSelect("pos_master_seq"));
  }

  /**
   * Extracts the row id.
   * @param id  the identifier to extract from, not null
   * @return the extracted row id
   */
  protected long extractRowId(final UniqueIdentifier id) {
    try {
      return Long.parseLong(id.getValue()) + Long.parseLong(id.getVersion());
    } catch (NumberFormatException ex) {
      throw new IllegalArgumentException("UniqueIdentifier is not from this position master: " + id, ex);
    }
  }

  /**
   * Extracts the oid.
   * @param id  the identifier to extract from, not null
   * @return the extracted oid
   */
  protected long extractOid(final UniqueIdentifier id) {
    try {
      return Long.parseLong(id.getValue());
    } catch (NumberFormatException ex) {
      throw new IllegalArgumentException("UniqueIdentifier is not from this position master: " + id, ex);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Creates an object identifier.
   * @param oid  the portfolio object identifier
   * @param deduplicate  the deduplication map, may be null
   * @return the unique identifier, not null
   */
  protected UniqueIdentifier createObjectIdentifier(final long oid, Map<UniqueIdentifier, UniqueIdentifier> deduplicate) {
    UniqueIdentifier uid = UniqueIdentifier.of(getIdentifierScheme(), Long.toString(oid));
    if (deduplicate == null) {
      return uid;
    }
    UniqueIdentifier stored = deduplicate.get(uid);
    if (stored != null) {
      return stored;
    }
    deduplicate.put(uid, uid);
    return uid;
  }

  /**
   * Creates a unique identifier.
   * @param oid  the portfolio object identifier
   * @param rowId  the node unique row identifier, null if object identifier
   * @param deduplicate  the deduplication map, may be null
   * @return the unique identifier, not null
   */
  protected UniqueIdentifier createUniqueIdentifier(final long oid, final long rowId, Map<UniqueIdentifier, UniqueIdentifier> deduplicate) {
    UniqueIdentifier uid = UniqueIdentifier.of(getIdentifierScheme(), Long.toString(oid), Long.toString(rowId - oid));
    if (deduplicate == null) {
      return uid;
    }
    UniqueIdentifier stored = deduplicate.get(uid);
    if (stored != null) {
      return stored;
    }
    deduplicate.put(uid, uid);
    return uid;
  }

  //-------------------------------------------------------------------------
  /**
   * Extracts a BigDecimal handling DB annoyances.
   * @param rs  the result set, not null
   * @param columnName  the column name, not null
   * @return the extracted value, may be null
   * @throws SQLException 
   */
  protected BigDecimal extractBigDecimal(final ResultSet rs, final String columnName) throws SQLException {
    BigDecimal value = rs.getBigDecimal(columnName);
    if (value == null) {
      return null;
    }
    BigDecimal stripped = value.stripTrailingZeros();  // Derby, and maybe others, add trailing zeroes
    if (stripped.scale() < 0) {
      return stripped.setScale(0);
    }
    return stripped;
  }

  //-------------------------------------------------------------------------
  protected PortfolioTreeSearchResult searchPortfolioTrees(PortfolioTreeSearchRequest request) {
    throw new UnsupportedOperationException();
  }

  protected PortfolioTreeDocument getPortfolioTree(UniqueIdentifier uid) {
    throw new UnsupportedOperationException();
  }

  protected PortfolioTreeDocument addPortfolioTree(PortfolioTreeDocument document) {
    throw new UnsupportedOperationException();
  }

  protected PortfolioTreeDocument updatePortfolioTree(PortfolioTreeDocument document) {
    throw new UnsupportedOperationException();
  }

  protected void removePortfolioTree(UniqueIdentifier uid) {
    throw new UnsupportedOperationException();
  }

  protected PortfolioTreeHistoryResult historyPortfolioTree(PortfolioTreeHistoryRequest request) {
    throw new UnsupportedOperationException();
  }

  protected PortfolioTreeDocument correctPortfolioTree(PortfolioTreeDocument document) {
    throw new UnsupportedOperationException();
  }

  //-------------------------------------------------------------------------
  protected PositionSearchResult searchPositions(PositionSearchRequest request) {
    throw new UnsupportedOperationException();
  }

  protected PositionDocument getPosition(final UniqueIdentifier uid) {
    throw new UnsupportedOperationException();
  }

  protected PositionDocument addPosition(PositionDocument document) {
    throw new UnsupportedOperationException();
  }

  protected PositionDocument updatePosition(PositionDocument document) {
    throw new UnsupportedOperationException();
  }

  protected void removePosition(UniqueIdentifier uid) {
    throw new UnsupportedOperationException();
  }

  protected PositionHistoryResult historyPosition(PositionHistoryRequest request) {
    throw new UnsupportedOperationException();
  }

  protected PositionDocument correctPosition(PositionDocument document) {
    throw new UnsupportedOperationException();
  }

  //-------------------------------------------------------------------------
  protected Portfolio getFullPortfolio(FullPortfolioGetRequest request) {
    throw new UnsupportedOperationException();
  }

  protected PortfolioNode getFullPortfolioNode(FullPortfolioNodeGetRequest request) {
    throw new UnsupportedOperationException();
  }

  protected Position getFullPosition(FullPositionGetRequest request) {
    throw new UnsupportedOperationException();
  }
  
  protected Trade getFullTrade(FullTradeGetRequest request) {
    throw new UnsupportedOperationException();
  }

  //-------------------------------------------------------------------------
  /**
   * Returns a string summary of this position master.
   * @return the string summary, not null
   */
  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + getIdentifierScheme() + "]";
  }

}
