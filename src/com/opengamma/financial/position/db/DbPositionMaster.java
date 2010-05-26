/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.db;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import javax.sql.DataSource;
import javax.time.Instant;
import javax.time.InstantProvider;
import javax.time.TimeSource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.JdbcUpdateAffectedIncorrectNumberOfRowsException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.util.Assert;

import com.opengamma.DataNotFoundException;
import com.opengamma.engine.position.Portfolio;
import com.opengamma.engine.position.PortfolioImpl;
import com.opengamma.engine.position.PortfolioNode;
import com.opengamma.engine.position.PortfolioNodeImpl;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.position.PositionImpl;
import com.opengamma.engine.position.PositionMaster;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.MutableUniqueIdentifiable;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.tuple.LongObjectPair;

/**
 * A database backed position master.
 * <p>
 * The position master provides a uniform structural view over a set of positions
 * holding them in a tree structure portfolio.
 * This class provides database storage for the entire tree.
 */
public class DbPositionMaster implements PositionMaster {

  /**
   * The scheme used for UniqueIdentifier objects.
   */
  public static final String IDENTIFIER_SCHEME_DEFAULT = "Db.PosMaster";
  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(DbPositionMaster.class);

  /**
   * The template for database operations.
   */
  private SimpleJdbcTemplate _jdbcTemplate;
  /**
   * The time-source to use.
   */
  private TimeSource _timeSource = TimeSource.system();
  /**
   * The scheme in use for UniqueIdentifier.
   */
  private String _identifierScheme = IDENTIFIER_SCHEME_DEFAULT;

  /**
   * Creates an instance.
   * @param transactionManager  the transaction manager, not null
   */
  public DbPositionMaster(DataSourceTransactionManager transactionManager) {
    ArgumentChecker.notNull(transactionManager, "transactionManager");
    DataSource dataSource = transactionManager.getDataSource();
    _jdbcTemplate = new SimpleJdbcTemplate(dataSource);   
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the database template.
   * @return the template, non-null if correctly initialized
   */
  protected SimpleJdbcTemplate getTemplate() {
    return _jdbcTemplate;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the time-source that determines the current time.
   * @return the time-source, not null
   */
  protected TimeSource getTimeSource() {
    return _timeSource;
  }

  /**
   * Sets the time-source.
   * @param timeSource  the time-source, not null
   */
  public void setTimeSource(final TimeSource timeSource) {
    ArgumentChecker.notNull(timeSource, "timeSource");
    _timeSource = timeSource;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the scheme in use for UniqueIdentifier.
   * @return the scheme, not null
   */
  protected String getIdentifierScheme() {
    return _identifierScheme;
  }

  /**
   * Sets the scheme in use for UniqueIdentifier.
   * @param scheme  the scheme, not null
   */
  public void setIdentifierScheme(final String scheme) {
    ArgumentChecker.notNull(scheme, "scheme");
    _identifierScheme = scheme;
  }

  /**
   * Checks whether the unique identifier has the right scheme.
   * @param uid  the unique identifier, not null
   */
  protected void checkIdentifierScheme(final UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "UniqueIdentifier");
    if (uid.getScheme().equals(getIdentifierScheme()) == false) {
      s_logger.debug("invalid UniqueIdentifier scheme: {}", uid.getScheme());
      throw new IllegalArgumentException("Invalid identifier for DbPositionMaster: " + uid);
    }
  }

  /**
   * Creates a unique identifier.
   * @param portfolioOid  the portfolio object identifier
   * @param version  the version
   * @return the unique identifier, not null
   */
  protected UniqueIdentifier createPortfolioUniqueIdentifier(final long portfolioOid, final long version) {
    return UniqueIdentifier.of(getIdentifierScheme(), Long.toString(portfolioOid), Long.toString(version));
  }

  /**
   * Creates a unique identifier.
   * @param portfolioOid  the portfolio object identifier
   * @param oid  the object identifier
   * @param version  the version
   * @return the unique identifier, not null
   */
  protected UniqueIdentifier createUniqueIdentifier(final long portfolioOid, final long oid, final long version) {
    String value = new StringBuilder().append(portfolioOid).append('-').append(oid).toString();
    return UniqueIdentifier.of(getIdentifierScheme(), value, Long.toString(version));
  }

  /**
   * Sets the unique identifier on an object.
   * @param obj  the object to set on, null ignored
   * @param uid  the unique identifier to set, not null
   */
  protected void setUniqueIdentifier(final Object obj, final UniqueIdentifier uid) {
    if (obj instanceof MutableUniqueIdentifiable) {
      ((MutableUniqueIdentifiable) obj).setUniqueIdentifier(uid);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Extracts a portfolio object identifier from a unique identifier.
   * @param uid  the unique identifier, not null
   * @return the portfolio object identifier
   */
  protected long extractPortfolioOid(final UniqueIdentifier uid) {
    int pos = uid.getValue().indexOf('-');
    if (pos < 0) {
      return Long.parseLong(uid.getValue());
    }
    return Long.parseLong(uid.getValue().substring(0, pos));
  }

  /**
   * Extracts the non-portfolio object identifier from a unique identifier.
   * @param uid  the unique identifier, not null
   * @return the non-portfolio object identifier
   */
  protected long extractOtherOid(final UniqueIdentifier uid) {
    int pos = uid.getValue().indexOf('-');
    if (pos < 0) {
      throw new IllegalArgumentException("Unique identifier is invalid: " + uid);
    }
    return Long.parseLong(uid.getValue().substring(pos + 1));
  }

  /**
   * Extracts the version from a unique identifier.
   * @param uid  the unique identifier, not null
   * @return the version
   */
  protected long extractVersion(final UniqueIdentifier uid) {
    return Long.parseLong(uid.getVersion());
  }

  //-------------------------------------------------------------------------
  /**
   * Gets a portfolio by unique identifier.
   * @param uid  the unique identifier, not null
   * @return the portfolio, null if not found
   * @throws IllegalArgumentException if the identifier is not from this position master
   */
  @Override
  public Portfolio getPortfolio(final UniqueIdentifier uid) {
    checkIdentifierScheme(uid);
    if (uid.isVersioned()) {
      return selectPortfolio(extractPortfolioOid(uid), extractVersion(uid));
    }
    return getPortfolio(uid, Instant.now(getTimeSource()));
  }

  /**
   * Gets a portfolio by unique identifier at an instant.
   * Any version in the unique identifier is ignored.
   * @param uid  the unique identifier, not null
   * @param instantProvider  the instant to query at, not null
   * @return the portfolio, null if not found
   * @throws IllegalArgumentException if the identifier is not from this position master
   */
  public Portfolio getPortfolio(final UniqueIdentifier uid, final InstantProvider instantProvider) {
    try {
      checkIdentifierScheme(uid);
      Instant instant = Instant.of(instantProvider);
      long portfolioOid = extractPortfolioOid(uid);
      long version = selectVersionByPortfolioOidInstant(portfolioOid, instant);
      return selectPortfolio(portfolioOid, version);
    } catch (DataNotFoundException ex) {
      return null;
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Queries to find the applicable version at the given instant.
   * @param portfolioOid  the portfolio object identifier
   * @param instant  the instant to query at, not null
   * @return the version number applicable at the instant
   * @throws DataNotFoundException if the portfolio is not found
   */
  protected long selectVersionByPortfolioOidInstant(final long portfolioOid, final Instant instant) {
    MapSqlParameterSource map = new MapSqlParameterSource()
      .addValue("portfolio_oid", portfolioOid)
      .addValue("instant", DateUtil.toSqlTimestamp(instant));
    try {
      return getTemplate().queryForLong(sqlVersionByPortfolioOidInstant(), map);
    } catch (EmptyResultDataAccessException ex) {
      throw new DataNotFoundException("Portfolio not found: " + portfolioOid + " at " + instant, ex);
    }
  }

  /**
   * Gets the SQL for getting the version by instant.
   * @return the SQL, not null
   */
  protected String sqlVersionByPortfolioOidInstant() {
    return "SELECT version " +
            "FROM pos_portfolio " +
            "WHERE pos_portfolio.oid = :portfolio_oid " +
              "AND :instant >= start_instant  AND :instant < end_instant";
  }

  //-------------------------------------------------------------------------
  /**
   * Gets a portfolio by object identifier and version.
   * @param portfolioOid  the object identifier
   * @param version  the version
   * @return the portfolio, null if not found
   */
  protected Portfolio selectPortfolio(final long portfolioOid, final long version) {
    MapSqlParameterSource args = new MapSqlParameterSource()
      .addValue("portfolio_oid", portfolioOid)
      .addValue("version", version);
    NamedParameterJdbcOperations namedJdbc = getTemplate().getNamedParameterJdbcOperations();
    SqlRowSet rowSet = namedJdbc.queryForRowSet(sqlBasicPortfolioByOidVersion(), args);
    if (rowSet.next() == false || rowSet.getTimestamp("START_INSTANT").equals(rowSet.getTimestamp("END_INSTANT"))) {
      return null;
    }
    final String portfolioName = rowSet.getString("NAME");
    PortfolioNodeImpl rootNode = selectPortfolioRootNode(portfolioOid, version);
    if (rootNode == null) {
      throw new IllegalStateException("Portfolio does not have root node: " + portfolioOid);
    }
    UniqueIdentifier uid = createPortfolioUniqueIdentifier(portfolioOid, version);
    return new PortfolioImpl(uid, portfolioName, rootNode);
  }

  /**
   * Gets the SQL for getting the version by instant.
   * @return the SQL, not null
   */
  protected String sqlBasicPortfolioByOidVersion() {
    return "SELECT name, start_instant, end_instant " +
            "FROM pos_portfolio " +
            "WHERE pos_portfolio.oid = :portfolio_oid " +
              "AND pos_portfolio.version = :version ";
  }

  //-------------------------------------------------------------------------
  /**
   * Gets a portfolio root node by object identifier and version.
   * @param portfolioOid  the object identifier
   * @param version  the version
   * @return the portfolio, null if not found
   */
  protected PortfolioNodeImpl selectPortfolioRootNode(final long portfolioOid, final long version) {
    MapSqlParameterSource args = new MapSqlParameterSource()
      .addValue("portfolio_oid", portfolioOid)
      .addValue("version", version);
    PortfolioNodeExtractor extractor = new PortfolioNodeExtractor(portfolioOid, version);
    NamedParameterJdbcOperations namedJdbc = getTemplate().getNamedParameterJdbcOperations();
    return (PortfolioNodeImpl) namedJdbc.query(sqlPortfolioRootNodeByOidVersion(), args, extractor);
  }

  /**
   * Gets the SQL for getting the root node and all below it.
   * @return the SQL, not null
   */
  protected String sqlPortfolioRootNodeByOidVersion() {
    return "SELECT pos_node.oid AS node_oid, left_id, right_id, pos_node.name AS node_name, " +
              "pos_position.oid AS position_oid, quantity, " +
              "pos_securitykey.id_scheme AS seckey_scheme, pos_securitykey.id_value AS seckey_value " +
            "FROM pos_node " +
              "LEFT JOIN pos_nodetree ON (pos_node.oid = pos_nodetree.node_oid AND :version >= pos_nodetree.start_version AND :version < pos_nodetree.end_version) " +
              "LEFT JOIN pos_position ON (pos_node.oid = pos_position.node_oid AND :version >= pos_position.start_version AND :version < pos_position.end_version) " +
              "LEFT JOIN pos_securitykey ON (pos_position.oid = pos_securitykey.position_oid AND :version >= pos_securitykey.start_version AND :version < pos_securitykey.end_version) " +
            "WHERE pos_node.portfolio_oid = :portfolio_oid " +
              "AND :version >= pos_node.start_version AND :version < pos_node.end_version " +
            "ORDER BY left_id";
  }

  //-------------------------------------------------------------------------
  /**
   * Gets a portfolio node by unique identifier.
   * @param uid  the unique identifier, not null
   * @return the node, null if not found
   * @throws IllegalArgumentException if the identifier is not from this position master
   */
  @Override
  public PortfolioNode getPortfolioNode(final UniqueIdentifier uid) {
    checkIdentifierScheme(uid);
    if (uid.isVersioned()) {
      return selectPortfolioNodeTree(extractPortfolioOid(uid), extractOtherOid(uid), extractVersion(uid));
    }
    return getPortfolioNode(uid, Instant.now(getTimeSource()));
  }

  /**
   * Gets a portfolio node by unique identifier at an instant.
   * @param uid  the unique identifier, not null
   * @param instantProvider  the instant to query at, not null
   * @return the node, null if not found
   * @throws IllegalArgumentException if the identifier is not from this position master
   */
  public PortfolioNode getPortfolioNode(final UniqueIdentifier uid, final InstantProvider instantProvider) {
    try {
      checkIdentifierScheme(uid);
      Instant instant = Instant.of(instantProvider);
      long portfolioOid = extractPortfolioOid(uid);
      long version = selectVersionByPortfolioOidInstant(portfolioOid, instant);
      return selectPortfolioNodeTree(portfolioOid, extractOtherOid(uid), version);
    } catch (DataNotFoundException ex) {
      return null;
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Gets a portfolio node by object identifier and version.
   * @param portfolioOid  the object identifier
   * @param portfolioNodeOid  the object identifier
   * @param version  the version
   * @return the portfolio, null if not found
   */
  protected PortfolioNode selectPortfolioNodeTree(final long portfolioOid, final long portfolioNodeOid, final long version) {
    MapSqlParameterSource args = new MapSqlParameterSource()
      .addValue("portfolio_oid", portfolioOid)
      .addValue("node_oid", portfolioNodeOid)
      .addValue("version", version);
    PortfolioNodeExtractor extractor = new PortfolioNodeExtractor(portfolioOid, version);
    NamedParameterJdbcOperations namedJdbc = getTemplate().getNamedParameterJdbcOperations();
    return (PortfolioNode) namedJdbc.query(sqlPortfolioNodeByOidVersion(), args, extractor);
  }

  /**
   * Gets the SQL for getting a tree of node by base node.
   * @return the SQL, not null
   */
  protected String sqlPortfolioNodeByOidVersion() {
    return "SELECT pos_node.oid AS node_oid, tree.left_id AS left_id, tree.right_id AS right_id, pos_node.name AS node_name " +
              ", pos_position.oid AS position_oid, quantity, " +
              "pos_securitykey.id_scheme AS seckey_scheme, pos_securitykey.id_value AS seckey_value " +
            "FROM pos_nodetree AS base, pos_nodetree AS tree, pos_node " +
              "LEFT JOIN pos_position ON (pos_node.oid = pos_position.node_oid AND :version >= pos_position.start_version AND :version < pos_position.end_version) " +
              "LEFT JOIN pos_securitykey ON (pos_position.oid = pos_securitykey.position_oid AND :version >= pos_securitykey.start_version AND :version < pos_securitykey.end_version) " +
            "WHERE pos_node.portfolio_oid = :portfolio_oid " +
              "AND base.node_oid = :node_oid " +
              "AND tree.left_id BETWEEN base.left_id AND base.right_id " +
              "AND tree.node_oid = pos_node.oid " +
              "AND :version >= base.start_version AND :version < base.end_version " +
              "AND :version >= tree.start_version AND :version < tree.end_version " +
              "AND :version >= pos_node.start_version AND :version < pos_node.end_version " +
            "ORDER BY left_id";
  }

  //-------------------------------------------------------------------------
  /**
   * Mapper from SQL rows to a PortfolioNode.
   */
  protected final class PortfolioNodeExtractor implements ResultSetExtractor {
    private final long _portfolioOid;
    private final long _version;
    private final Stack<LongObjectPair<PortfolioNodeImpl>> _nodes = new Stack<LongObjectPair<PortfolioNodeImpl>>();
    private PositionImpl _position;
    private long _lastNodeOid = -1;
    private long _lastPositionOid = -1;

    protected PortfolioNodeExtractor(long portfolioOid, long version) {
      _portfolioOid = portfolioOid;
      _version = version;
    }

    @Override
    public Object extractData(ResultSet rs) throws SQLException, DataAccessException {
      while (rs.next()) {
        // build tree nodes
        long nodeOid = rs.getLong("NODE_OID");
        if (nodeOid != _lastNodeOid) {
          buildTreeNodes(rs, nodeOid);
        }
        // build position
        if (rs.getObject("POSITION_OID") != null) {
          long positionOid = rs.getLong("POSITION_OID");
          if (positionOid != _lastPositionOid) {
            buildPosition(rs, positionOid);
          }
          String idScheme = rs.getString("SECKEY_SCHEME");
          String idValue = rs.getString("SECKEY_VALUE");
          if (idScheme != null && idValue != null) {
            Identifier id = Identifier.of(idScheme, idValue);
            _position.setSecurityKey(_position.getSecurityKey().withIdentifier(id));
          }
        }
      }
      return _nodes.size() == 0 ? null : _nodes.get(0).getSecond();
    }

    private void buildTreeNodes(ResultSet rs, long nodeOid) throws SQLException {
      _lastNodeOid = nodeOid;
      long leftId = rs.getLong("LEFT_ID");
      long rightId = rs.getLong("RIGHT_ID");
      UniqueIdentifier uid = createUniqueIdentifier(_portfolioOid, nodeOid, _version);
      String name = StringUtils.defaultString(rs.getString("NODE_NAME"));
      PortfolioNodeImpl node = new PortfolioNodeImpl(uid, name);
      // find and add to parent unless this is the root
      if (_nodes.size() > 0) {
        while (leftId > _nodes.peek().getFirstLong()) {
          _nodes.pop();
        }
        PortfolioNodeImpl parent = _nodes.peek().getSecond();
        parent.addChildNode(node);
      }
      // add to stack
      _nodes.push(new LongObjectPair<PortfolioNodeImpl>(rightId, node));
    }

    private void buildPosition(ResultSet rs, long positionOid) throws SQLException {
      _lastPositionOid = positionOid;
      BigDecimal quantity = rs.getBigDecimal("QUANTITY");
      // Trade not supported yet
      UniqueIdentifier uid = createUniqueIdentifier(_portfolioOid, positionOid, _version);
      _position = new PositionImpl(uid, quantity, IdentifierBundle.EMPTY);
      PortfolioNodeImpl parent = _nodes.peek().getSecond();
      parent.addPosition(_position);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Gets a position by unique identifier.
   * @param uid  the unique identifier, not null
   * @return the position, null if not found
   * @throws IllegalArgumentException if the identifier is not from this position master
   */
  @Override
  public Position getPosition(final UniqueIdentifier uid) {
    checkIdentifierScheme(uid);
    if (uid.isVersioned()) {
      return selectPosition(extractPortfolioOid(uid), extractOtherOid(uid), extractVersion(uid));
    }
    return getPosition(uid, Instant.now(getTimeSource()));
  }

  /**
   * Gets a position by unique identifier at an instant.
   * @param uid  the unique identifier, not null
   * @param instantProvider  the instant to query at, not null
   * @return the position, null if not found
   * @throws IllegalArgumentException if the identifier is not from this position master
   */
  public Position getPosition(final UniqueIdentifier uid, final InstantProvider instantProvider) {
    try {
      checkIdentifierScheme(uid);
      Instant instant = Instant.of(instantProvider);
      long portfolioOid = extractPortfolioOid(uid);
      long version = selectVersionByPortfolioOidInstant(portfolioOid, instant);
      return selectPosition(portfolioOid, extractOtherOid(uid), version);
    } catch (DataNotFoundException ex) {
      return null;
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Gets a position by object identifier and version.
   * @param portfolioOid  the object identifier
   * @param positionOid  the object identifier
   * @param version  the version
   * @return the position, null if not found
   */
  protected Position selectPosition(final long portfolioOid, final long positionOid, final long version) {
    MapSqlParameterSource args = new MapSqlParameterSource()
      .addValue("position_oid", positionOid)
      .addValue("version", version);
    PositionExtractor extractor = new PositionExtractor(portfolioOid, version);
    NamedParameterJdbcOperations namedJdbc = getTemplate().getNamedParameterJdbcOperations();
    return (Position) namedJdbc.query(sqlPositionByOidVersion(), args, extractor);
  }

  /**
   * Gets the SQL for getting a position by oid and version.
   * @return the SQL, not null
   */
  protected String sqlPositionByOidVersion() {
    return "SELECT pos_position.oid AS position_oid, quantity, " +
              "pos_securitykey.id_scheme AS seckey_scheme, pos_securitykey.id_value AS seckey_value " +
            "FROM pos_position " +
              "LEFT JOIN pos_securitykey ON (pos_position.oid = pos_securitykey.position_oid AND :version >= pos_securitykey.start_version AND :version < pos_securitykey.end_version) " +
            "WHERE pos_position.oid = :position_oid " +
              "AND :version >= pos_position.start_version AND :version < pos_position.end_version ";
  }

  //-------------------------------------------------------------------------
  /**
   * Mapper from SQL rows to a PortfolioNode.
   */
  protected final class PositionExtractor implements ResultSetExtractor {
    private final long _portfolioOid;
    private final long _version;
    private PositionImpl _position;

    protected PositionExtractor(long portfolioOid, long version) {
      _portfolioOid = portfolioOid;
      _version = version;
    }

    @Override
    public Object extractData(ResultSet rs) throws SQLException, DataAccessException {
      while (rs.next()) {
        if (_position == null) {
          buildPosition(rs);
        }
        String idScheme = rs.getString("SECKEY_SCHEME");
        String idValue = rs.getString("SECKEY_VALUE");
        if (idScheme != null && idValue != null) {
          Identifier id = Identifier.of(idScheme, idValue);
          _position.setSecurityKey(_position.getSecurityKey().withIdentifier(id));
        }
      }
      return _position;
    }

    private void buildPosition(ResultSet rs) throws SQLException {
      long positionOid = rs.getLong("POSITION_OID");
      BigDecimal quantity = rs.getBigDecimal("QUANTITY");
      // Trade not supported yet
      UniqueIdentifier uid = createUniqueIdentifier(_portfolioOid, positionOid, _version);
      _position = new PositionImpl(uid, quantity, IdentifierBundle.EMPTY);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the complete set of portfolio unique identifiers.
   * @return the set of unique identifiers, not null
   */
  @Override
  public Set<UniqueIdentifier> getPortfolioIds() {
    return selectPortfolioIds(Instant.now(getTimeSource()));
  }

  /**
   * Gets the complete set of portfolio unique identifiers.
   * @param instantProvider  the instant to query at, not null
   * @return the set of unique identifiers, not null
   */
  public Set<UniqueIdentifier> getPortfolioIds(final InstantProvider instantProvider) {
    Instant instant = Instant.of(instantProvider);
    return selectPortfolioIds(instant);
  }

  //-------------------------------------------------------------------------
  /**
   * Selects the portfolio ids from the database.
   * @param instant  the instant to query at, not null
   * @return the set of unique identifiers, not null
   */
  protected Set<UniqueIdentifier> selectPortfolioIds(final Instant instant) {
    MapSqlParameterSource args = new MapSqlParameterSource()
      .addValue("instant", DateUtil.toSqlTimestamp(instant));
    UniqueIdentifierMapper mapper = new UniqueIdentifierMapper();
    List<UniqueIdentifier> result = getTemplate().query(sqlPortfolioIdsByInstant(), mapper, args);
    return new TreeSet<UniqueIdentifier>(result);
  }

  /**
   * Gets the SQL for getting the version by instant.
   * @return the SQL, not null
   */
  protected String sqlPortfolioIdsByInstant() {
    return "SELECT oid, version " +
            "FROM pos_portfolio " +
            "WHERE :instant >= start_instant AND :instant < end_instant";
  }

  //-------------------------------------------------------------------------
  /**
   * Maps SQL results to UniqueIdentifier.
   */
  protected class UniqueIdentifierMapper implements ParameterizedRowMapper<UniqueIdentifier> {
    @Override
    public UniqueIdentifier mapRow(ResultSet rs, int rowNum) throws SQLException {
      long portfolioOid = rs.getLong("OID");
      long version = rs.getLong("VERSION");
      return createPortfolioUniqueIdentifier(portfolioOid, version);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Stores a portfolio.
   * The portfolio may originate from another position master, but it will have
   * its unique identifiers changed by calling this method.
   * @param portfolio  the portfolio to store, not null
   * @return the updated unique identifier of the portfolio, not null
   */
  public UniqueIdentifier putPortfolio(final Portfolio portfolio) {
    ArgumentChecker.notNull(portfolio, "portfolio");
    Instant instant = Instant.now(getTimeSource());
    UniqueIdentifier uid = portfolio.getUniqueIdentifier();
    if (uid != null && uid.getScheme().equals(getIdentifierScheme())) {
      return updatePortfolio(portfolio, instant);
    } else {
      return createPortfolio(portfolio, instant);
    }
  }

  /**
   * Updates a portfolio without updating the rest of the tree or positions.
   * This ignores the version in the object passed in.
   * @param portfolio  the portfolio to remove, not null
   * @return the unique identifier of the portfolio version that indicates removal, null if no row removed
   * @throws IllegalArgumentException if the portfolio is not from this position master
   * @throws DataNotFoundException if the portfolio is not found
   */
  public UniqueIdentifier updatePortfolioOnly(final Portfolio portfolio) {
    Assert.notNull(portfolio, "portfolio");
    Instant instant = Instant.now(getTimeSource());
    UniqueIdentifier oldUid = portfolio.getUniqueIdentifier();
    checkIdentifierScheme(oldUid);
    long portfolioOid = extractPortfolioOid(oldUid);
    long oldVersion = extractVersion(oldUid);
    long latestVersion = selectVersionByPortfolioOidInstant(portfolioOid, instant);  // find latest version
    if (oldVersion != latestVersion) {
      throw new DataIntegrityViolationException("Unable to update Portfolio as version is not the latest version");
    }
    updatePortfolioSetEndInstant(portfolioOid, latestVersion, instant);  // end-date old version
    UniqueIdentifier uid = insertPortfolio(portfolio, portfolioOid, latestVersion + 1, instant);  // insert new version
    setUniqueIdentifier(portfolio, uid);
    return uid;
  }

  /**
   * Removes a portfolio by end-dating the row.
   * This ignores the version in the object passed in.
   * @param portfolioUid  the portfolio unique identifier to remove, not null
   * @return the unique identifier of the portfolio version that indicates removal, null if no row removed
   * @throws IllegalArgumentException if the portfolio is not from this position master
   * @throws DataNotFoundException if the portfolio is not found
   */
  public UniqueIdentifier removePortfolio(final UniqueIdentifier portfolioUid) {
    ArgumentChecker.notNull(portfolioUid, "portfolio unique identifier");
    Instant instant = Instant.now(getTimeSource());
    checkIdentifierScheme(portfolioUid);
    long portfolioOid = extractPortfolioOid(portfolioUid);
    long oldVersion = extractVersion(portfolioUid);
    long latestVersion = selectVersionByPortfolioOidInstant(portfolioOid, instant);  // find latest version
    if (oldVersion != latestVersion) {
      throw new DataIntegrityViolationException("Unable to update Portfolio as version is not the latest version");
    }
    return updatePortfolioSetEndInstant(portfolioOid, latestVersion, instant);  // end-date it
  }

  //-------------------------------------------------------------------------
  /**
   * Adds a position to the specified node.
   * @param node  the node to add to, not null
   * @param positions  the positions to add, not null
   * @return the updated unique identifier of the node, not null
   * @throws IllegalArgumentException if the node is not from this position master
   */
  public UniqueIdentifier addPositions(final PortfolioNode node, final List<Position> positions) {
    // TODO
    // check node version is non-null, ours and latest for that node
    // check position uids are null or not ours
    // add latest row to portfolio
    // insert positions
    // update node uid and position uids (in memory)
    return node.getUniqueIdentifier();
  }

  /**
   * Adds a position to the specified node.
   * @param position  the position to update, not null
   * @return the updated unique identifier of the position, not null
   * @throws IllegalArgumentException if the node is not from this position master
   */
  public UniqueIdentifier updatePosition(final Position position) {
    // TODO
    // check position uid is non-null, ours and latest for that node
    // add latest row to portfolio
    // update position
    // update position uid (in memory)
    return position.getUniqueIdentifier();
  }

  /**
   * Adds a position to the specified node.
   * @param node  the node to add to, not null
   * @param positions  the positions to add, not null
   * @return the updated unique identifier of the node, not null
   */
  public UniqueIdentifier removePositions(final PortfolioNode node, final List<Position> positions) {
    // TODO
    // check node version is non-null, ours and latest for that node
    // check position uids are non-null, ours and latest for that node
    // add latest row to portfolio
    // remove positions
    // update node uid and position uids (in memory)
    return node.getUniqueIdentifier();
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the portfolio in the database.
   * @param portfolio  the portfolio to create on the database, not null
   * @param instant  the instant to store at, not null
   * @return the unique identifier created, not null
   */
  protected UniqueIdentifier createPortfolio(final Portfolio portfolio, final Instant instant) {
    long portfolioOid = selectNextPortfolioOid();
    UniqueIdentifier uid = insertPortfolio(portfolio, portfolioOid, 1, instant);
    insertTreeNodes(portfolio.getRootNode(), portfolioOid, 1);
    insertTreePositions(portfolio.getRootNode(), portfolioOid, 1);
    return uid;
  }

  /**
   * Updates the portfolio in the database.
   * @param portfolio  the portfolio to update on the database, not null
   * @param instant  the instant to store at, not null
   * @return the unique identifier of the updated portflio, not null
   */
  protected UniqueIdentifier updatePortfolio(final Portfolio portfolio, final Instant instant) {
    // TODO
    return portfolio.getUniqueIdentifier();
  }

  //-------------------------------------------------------------------------
  /**
   * Selects the next portfolio object identifier.
   * @return the next object identifier, not null
   */
  protected long selectNextPortfolioOid() {
    return getTemplate().queryForLong(sqlMaxOid("pos_portfolio")) + 1;
  }

  /**
   * Selects the next portfolio object identifier.
   * @return the next object identifier, not null
   */
  protected long selectNextNodeOid() {
    return getTemplate().queryForLong(sqlMaxOid("pos_node")) + 1;
  }

  /**
   * Selects the next position object identifier.
   * @return the next object identifier, not null
   */
  protected long selectNextPositionOid() {
    return getTemplate().queryForLong(sqlMaxOid("pos_position")) + 1;
  }

  /**
   * Selects the next security key object identifier.
   * @return the next object identifier, not null
   */
  protected long selectNextSecurityKeyOid() {
    return getTemplate().queryForLong(sqlMaxOid("pos_securitykey")) + 1;
  }

  /**
   * Gets the SQL for getting the version by instant.
   * @param table  the table name, not null
   * @return the SQL, not null
   */
  protected String sqlMaxOid(final String table) {
    return "SELECT MAX(oid) " +
            "FROM " + table;
  }

  //-------------------------------------------------------------------------
  /**
   * Inserts a row into the portfolio table.
   * @param portfolio  the portfolio, not null
   * @param portfolioOid  the portfolio oid, not null
   * @param version  the version, not null
   * @param instant  the instant to store at, not null
   * @return the version, not null
   */
  protected UniqueIdentifier insertPortfolio(
      final Portfolio portfolio, final long portfolioOid, final long version, final Instant instant) {
    MapSqlParameterSource args = new MapSqlParameterSource()
      .addValue("portfolio_oid", portfolioOid)
      .addValue("version", version)
      .addValue("start_instant", DateUtil.toSqlTimestamp(instant))
      .addValue("end_instant", DateUtil.MAX_SQL_TIMESTAMP)
      .addValue("name", portfolio.getName());
    getTemplate().update(sqlInsertPortfolio(), args);
    UniqueIdentifier uid = createPortfolioUniqueIdentifier(portfolioOid, version);
    setUniqueIdentifier(portfolio, uid);
    return uid;
  }

  /**
   * Gets the SQL for getting the version by instant.
   * @return the SQL, not null
   */
  protected String sqlInsertPortfolio() {
    return "INSERT INTO pos_portfolio " +
              "(oid, version, start_instant, end_instant, name)" +
            "VALUES " +
              "(:portfolio_oid, :version, :start_instant, :end_instant, :name)";
  }

  //-------------------------------------------------------------------------
  /**
   * Inserts a row into the portfolio table.
   * @param rootNode  the root node, not null
   * @param portfolioOid  the portfolio oid, not null
   * @param version  the version, not null
   * @return the root node object identifier, not null
   */
  protected UniqueIdentifier insertTreeNodes(
      final PortfolioNode rootNode, final long portfolioOid, final long version) {
    final long nodeOid = selectNextNodeOid();
    final List<MapSqlParameterSource> nodeList = new ArrayList<MapSqlParameterSource>();
    final List<MapSqlParameterSource> treeList = new ArrayList<MapSqlParameterSource>();
    insertTreeNodesBuildArgs(rootNode, portfolioOid, new long[] {nodeOid, 1}, version, nodeList, treeList);
    getTemplate().batchUpdate(sqlInsertNode(), (MapSqlParameterSource[]) nodeList.toArray(new MapSqlParameterSource[nodeList.size()]));
    getTemplate().batchUpdate(sqlInsertTree(), (MapSqlParameterSource[]) treeList.toArray(new MapSqlParameterSource[treeList.size()]));
    return createUniqueIdentifier(portfolioOid, nodeOid, version);
  }

  /**
   * Recursively create the arguments to insert the nodes.
   * @param node  the root node, not null
   * @param portfolioOid  the portfolio oid, not null
   * @param counter  the counter to create node oid, not null
   * @param version  the version, not null
   * @param nodeList  the list of node arguments to build, not null
   * @param treeList  the list of tree arguments to build, not null
   */
  protected void insertTreeNodesBuildArgs(
      final PortfolioNode node, final Long portfolioOid, long[] counter, final Long version,
      List<MapSqlParameterSource> nodeList, List<MapSqlParameterSource> treeList) {
    // depth first, storing the left/right before/after the loop
    final long left = counter[1]++;
    for (PortfolioNode childNode : node.getChildNodes()) {
      insertTreeNodesBuildArgs(childNode, portfolioOid, counter, version, nodeList, treeList);
    }
    final long right = counter[1]++;
    final long nodeOid = counter[0]++;
    // the arguments for inserting into the node table
    MapSqlParameterSource nodeArgs = new MapSqlParameterSource()
      .addValue("portfolio_oid", portfolioOid)
      .addValue("node_oid", nodeOid)
      .addValue("start_version", version)
      .addValue("end_version", Long.MAX_VALUE)
      .addValue("name", node.getName());
    nodeList.add(nodeArgs);
    // the arguments for inserting into the tree table
    MapSqlParameterSource treeArgs = new MapSqlParameterSource()
      .addValue("node_oid", nodeOid)
      .addValue("start_version", version)
      .addValue("end_version", Long.MAX_VALUE)
      .addValue("left_id", left)
      .addValue("right_id", right);
    treeList.add(treeArgs);
    // set the uid
    UniqueIdentifier uid = createUniqueIdentifier(portfolioOid, nodeOid, version);
    setUniqueIdentifier(node, uid);
  }

  /**
   * Gets the SQL for inserting a node.
   * @return the SQL, not null
   */
  protected String sqlInsertNode() {
    return "INSERT INTO pos_node " +
              "(portfolio_oid, oid, start_version, end_version, name)" +
            "VALUES " +
              "(:portfolio_oid, :node_oid, :start_version, :end_version, :name)";
  }

  /**
   * Gets the SQL for inserting a node in the tree.
   * @return the SQL, not null
   */
  protected String sqlInsertTree() {
    return "INSERT INTO pos_nodetree " +
              "(node_oid, start_version, end_version, left_id, right_id)" +
            "VALUES " +
              "(:node_oid, :start_version, :end_version, :left_id, :right_id)";
  }

  //-------------------------------------------------------------------------
  /**
   * Inserts a row into the portfolio table.
   * @param rootNode  the root node, not null
   * @param portfolioOid  the portfolio object identifier, not null
   * @param version  the version, not null
   */
  protected void insertTreePositions(
      final PortfolioNode rootNode, final long portfolioOid, final long version) {
    final long positionOid = selectNextPositionOid();
    final long secKeyOid = selectNextSecurityKeyOid();
    final List<MapSqlParameterSource> positionList = new ArrayList<MapSqlParameterSource>();
    final List<MapSqlParameterSource> secKeyList = new ArrayList<MapSqlParameterSource>();
    insertTreePositionsBuildArgs(rootNode, portfolioOid, new long[] {positionOid, secKeyOid}, version, positionList, secKeyList);
    getTemplate().batchUpdate(sqlInsertPosition(), (MapSqlParameterSource[]) positionList.toArray(new MapSqlParameterSource[positionList.size()]));
    getTemplate().batchUpdate(sqlInsertSecurityKey(), (MapSqlParameterSource[]) secKeyList.toArray(new MapSqlParameterSource[secKeyList.size()]));
  }

  /**
   * Recursively create the arguments to insert the nodes.
   * @param node  the root node, not null
   * @param portfolioOid  the portfolio object identifier, not null
   * @param counter  the counter to create object identifiers, not null
   * @param version  the version, not null
   * @param positionList  the list of position arguments to build, not null
   * @param secKeyList  the list of security-key arguments to build, not null
   */
  protected void insertTreePositionsBuildArgs(
      final PortfolioNode node, final Long portfolioOid, long[] counter, final Long version,
      List<MapSqlParameterSource> positionList, List<MapSqlParameterSource> secKeyList) {
    // depth first
    for (PortfolioNode childNode : node.getChildNodes()) {
      insertTreePositionsBuildArgs(childNode, portfolioOid, counter, version, positionList, secKeyList);
    }
    final Long nodeOid = extractOtherOid(node.getUniqueIdentifier());
    for (Position position : node.getPositions()) {
      final long positionOid = counter[0]++;
      // the arguments for inserting into the position table
      MapSqlParameterSource positionArgs = new MapSqlParameterSource()
        .addValue("node_oid", nodeOid)
        .addValue("position_oid", positionOid)
        .addValue("start_version", version)
        .addValue("end_version", Long.MAX_VALUE)
        .addValue("quantity", position.getQuantity());
      positionList.add(positionArgs);
      // the arguments for inserting into the seckey table
      for (Identifier id : position.getSecurityKey()) {
        final long secKeyOid = counter[1]++;
        MapSqlParameterSource treeArgs = new MapSqlParameterSource()
          .addValue("position_oid", positionOid)
          .addValue("seckey_oid", secKeyOid)
          .addValue("start_version", version)
          .addValue("end_version", Long.MAX_VALUE)
          .addValue("id_scheme", id.getScheme().getName())
          .addValue("id_value", id.getValue());
        secKeyList.add(treeArgs);
      }
      // set the uid
      UniqueIdentifier uid = createUniqueIdentifier(portfolioOid, positionOid, version);
      setUniqueIdentifier(position, uid);
    }
  }

  /**
   * Gets the SQL for inserting a position.
   * @return the SQL, not null
   */
  protected String sqlInsertPosition() {
    return "INSERT INTO pos_position " +
              "(node_oid, oid, start_version, end_version, quantity)" +
            "VALUES " +
              "(:node_oid, :position_oid, :start_version, :end_version, :quantity)";
  }

  /**
   * Gets the SQL for inserting a security key.
   * @return the SQL, not null
   */
  protected String sqlInsertSecurityKey() {
    return "INSERT INTO pos_securitykey " +
              "(position_oid, oid, start_version, end_version, id_scheme, id_value)" +
            "VALUES " +
              "(:position_oid, :seckey_oid, :start_version, :end_version, :id_scheme, :id_value)";
  }

  //-------------------------------------------------------------------------
  /**
   * Updates the portfolio table to end-date a portfolio.
   * @param portfolioOid  the portfolio to end
   * @param version  the version number to update the end-date of
   * @param instant  the instant to use, not null
   * @return the updated unique identifier, not null
   */
  protected UniqueIdentifier updatePortfolioSetEndInstant(final long portfolioOid, final long version, final Instant instant) {
    MapSqlParameterSource args = new MapSqlParameterSource()
      .addValue("portfolio_oid", portfolioOid)
      .addValue("version", version)
      .addValue("end_instant", DateUtil.toSqlTimestamp(instant));
    int rows = getTemplate().update(sqlUpdatePortfolioSetEndInstant(), args);
    if (rows != 1) {
      throw new JdbcUpdateAffectedIncorrectNumberOfRowsException(sqlUpdatePortfolioSetEndInstant(), 1, rows);
    }
    return createPortfolioUniqueIdentifier(portfolioOid, version);
  }

  /**
   * Gets the SQL for end-dating a portfolio.
   * @return the SQL, not null
   */
  protected String sqlUpdatePortfolioSetEndInstant() {
    return "UPDATE pos_portfolio " +
            "SET end_instant = :end_instant " +
            "WHERE oid = :portfolio_oid " +
              "AND version = :version ";
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
