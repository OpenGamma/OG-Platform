/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.master.db;

import java.util.Map;

import javax.time.TimeSource;

import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import com.opengamma.engine.position.Portfolio;
import com.opengamma.engine.position.PortfolioNode;
import com.opengamma.engine.position.Position;
import com.opengamma.financial.position.master.FullPortfolioGetRequest;
import com.opengamma.financial.position.master.FullPortfolioNodeGetRequest;
import com.opengamma.financial.position.master.FullPositionGetRequest;
import com.opengamma.financial.position.master.PortfolioTreeDocument;
import com.opengamma.financial.position.master.PortfolioTreeSearchHistoricRequest;
import com.opengamma.financial.position.master.PortfolioTreeSearchHistoricResult;
import com.opengamma.financial.position.master.PortfolioTreeSearchRequest;
import com.opengamma.financial.position.master.PortfolioTreeSearchResult;
import com.opengamma.financial.position.master.PositionDocument;
import com.opengamma.financial.position.master.PositionMaster;
import com.opengamma.financial.position.master.PositionSearchHistoricRequest;
import com.opengamma.financial.position.master.PositionSearchHistoricResult;
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
   * @param master  the position master, non-null
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
   * @return the template, non-null if correctly initialized
   */
  protected SimpleJdbcTemplate getTemplate() {
    return _master.getTemplate();
  }

  /**
   * Gets the database helper.
   * @return the helper, non-null if correctly initialized
   */
  protected DbHelper getDbHelper() {
    return _master.getDbHelper();
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
    return getTemplate().queryForLong(getDbHelper().sqlNextSequenceValueSelect("pos_master_seq"));
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
    UniqueIdentifier uid = UniqueIdentifier.of(getIdentifierScheme(), Long.toString(oid), Long.toString(rowId));
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

  protected PortfolioTreeSearchHistoricResult searchPortfolioTreeHistoric(PortfolioTreeSearchHistoricRequest request) {
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

  protected PositionSearchHistoricResult searchPositionHistoric(PositionSearchHistoricRequest request) {
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
