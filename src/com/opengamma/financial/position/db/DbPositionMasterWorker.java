/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.db;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import javax.time.Instant;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import com.google.common.base.Objects;
import com.opengamma.DataNotFoundException;
import com.opengamma.engine.position.Portfolio;
import com.opengamma.engine.position.PortfolioImpl;
import com.opengamma.engine.position.PortfolioNode;
import com.opengamma.engine.position.PortfolioNodeImpl;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.position.PositionImpl;
import com.opengamma.financial.position.PortfolioSummary;
import com.opengamma.financial.position.PositionSummary;
import com.opengamma.financial.position.SearchPortfoliosRequest;
import com.opengamma.financial.position.SearchPortfoliosResult;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.MutableUniqueIdentifiable;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.db.DbHelper;
import com.opengamma.util.db.Paging;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.tuple.LongObjectPair;

/**
 * Low level SQL focused part of the database backed position master.
 */
public class DbPositionMasterWorker {

  /**
   * The maximum instant used for end-versioning.
   */
  protected static final Timestamp END_INSTANT = DateUtil.MAX_SQL_TIMESTAMP;
  /**
   * The maximum version used for end-versioning.
   */
  protected static final long END_VERSION = Long.MAX_VALUE;
  /**
   * Status flag for active portfolios.
   */
  protected static final String STATUS_ACTIVE = "A";
  /**
   * Status flag for deleted portfolios.
   */
  protected static final String STATUS_DELETED = "D";

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(DbPositionMasterWorker.class);

  /**
   * The main master.
   */
  private final DbPositionMaster _parent;

  /**
   * Creates an instance.
   * @param parent  the parent manager, not null
   */
  public DbPositionMasterWorker(DbPositionMaster parent) {
    Validate.notNull(parent, "transactionManager");
    _parent = parent;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the parent master.
   * @return the parent, not null
   */
  protected DbPositionMaster getParent() {
    return _parent;
  }

  /**
   * Gets the database template.
   * @return the template, non-null if correctly initialized
   */
  protected SimpleJdbcTemplate getTemplate() {
    return _parent.getTemplate();
  }

  /**
   * Gets the database helper.
   * @return the helper, non-null if correctly initialized
   */
  protected DbHelper getDbHelper() {
    return _parent.getDbHelper();
  }

  /**
   * Gets the scheme in use for UniqueIdentifier.
   * @return the scheme, not null
   */
  protected String getIdentifierScheme() {
    return _parent.getIdentifierScheme();
  }

  //-------------------------------------------------------------------------
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
   * Gets a portfolio by object identifier and version.
   * @param portfolioOid  the object identifier
   * @param version  the version
   * @param ignoreDeleted  whether to ignore deleted entries
   * @param loadTree  whether to load the tree
   * @param loadPositions  whether to load the positions
   * @return the portfolio, null if not found
   */
  protected PortfolioImpl selectPortfolioByOidVersion(final long portfolioOid, final long version,
      final boolean ignoreDeleted, final boolean loadTree, final boolean loadPositions) {
    MapSqlParameterSource args = new MapSqlParameterSource()
      .addValue("portfolio_oid", portfolioOid)
      .addValue("version", version);
    Map<String, Object> portfolioResult;
    try {
      portfolioResult = getTemplate().queryForMap(sqlPortfolioBasicsByOidVersion(ignoreDeleted), args);
    } catch (EmptyResultDataAccessException ex) {
      s_logger.info("Portfolio not found: " + portfolioOid + " version " + version);
      return null;
    }
    final String name = (String) portfolioResult.get("NAME");
    UniqueIdentifier uid = createPortfolioUniqueIdentifier(portfolioOid, version);
    PortfolioNodeImpl root = new PortfolioNodeImpl();
    if (loadTree) {
      // TODO: handle loadPositions
      root = selectPortfolioRootNode(portfolioOid, version);
    }
    return new PortfolioImpl(uid, name, root);
  }

  /**
   * Gets the SQL for getting the version by instant.
   * @param ignoreDeleted  whether to ignore deleted entries
   * @return the SQL, not null
   */
  protected String sqlPortfolioBasicsByOidVersion(final boolean ignoreDeleted) {
    return "SELECT name " +
            "FROM pos_portfolio " +
            "WHERE pos_portfolio.oid = :portfolio_oid " +
              "AND pos_portfolio.version = :version " +
              (ignoreDeleted ? "AND status = 'A'" : "");
  }

  //-------------------------------------------------------------------------
  /**
   * Queries to find the applicable version at the given instant.
   * @param portfolioOid  the portfolio object identifier
   * @param instant  the instant to query at, not null
   * @return the version number applicable at the instant, null if not found
   */
  protected PortfolioImpl selectPortfolioByOidInstant(final long portfolioOid, final Instant instant) {
    MapSqlParameterSource args = new MapSqlParameterSource()
      .addValue("portfolio_oid", portfolioOid)
      .addValue("instant", DateUtil.toSqlTimestamp(instant));
    Map<String, Object> portfolioResult;
    try {
      portfolioResult = getTemplate().queryForMap(sqlPortfolioBasicsByOidInstant(), args);
    } catch (EmptyResultDataAccessException ex) {
      s_logger.info("Portfolio not found: " + portfolioOid + " at " + instant);
      return null;
    }
    long version = (Long) portfolioResult.get("VERSION");
    String name = StringUtils.defaultString((String) portfolioResult.get("NAME"));
    PortfolioNodeImpl root = selectPortfolioRootNode(portfolioOid, version);
    UniqueIdentifier uid = createPortfolioUniqueIdentifier(portfolioOid, version);
    return new PortfolioImpl(uid, name, root);
  }

  /**
   * Gets the SQL for getting the version by instant.
   * @return the SQL, not null
   */
  protected String sqlPortfolioBasicsByOidInstant() {
    return "SELECT version, name " +
            "FROM pos_portfolio " +
            "WHERE pos_portfolio.oid = :portfolio_oid " +
              "AND :instant >= start_instant  AND :instant < end_instant " +
              "AND status = 'A'";
  }

  //-------------------------------------------------------------------------
  /**
   * Queries to find the applicable version at the given instant.
   * @param portfolioOid  the portfolio object identifier
   * @param instant  the instant to query at, not null
   * @param ignoreDeleted  whether to ignore deleted rows
   * @return the version number applicable at the instant
   * @throws DataNotFoundException if the portfolio is not found
   */
  protected long selectVersionByPortfolioOidInstant(final long portfolioOid, final Instant instant, boolean ignoreDeleted) {
    MapSqlParameterSource map = new MapSqlParameterSource()
      .addValue("portfolio_oid", portfolioOid)
      .addValue("instant", DateUtil.toSqlTimestamp(instant));
    try {
      return getTemplate().queryForLong(sqlVersionByPortfolioOidInstant(ignoreDeleted), map);
    } catch (EmptyResultDataAccessException ex) {
      s_logger.info("Portfolio not found: " + portfolioOid + " at " + instant);
      throw new DataNotFoundException("Portfolio not found: " + portfolioOid + " at " + instant, ex);
    }
  }

  /**
   * Gets the SQL for getting the version by instant.
   * @param ignoreDeleted  whether to ignore deleted rows
   * @return the SQL, not null
   */
  protected String sqlVersionByPortfolioOidInstant(boolean ignoreDeleted) {
    return "SELECT version " +
            "FROM pos_portfolio " +
            "WHERE pos_portfolio.oid = :portfolio_oid " +
              "AND :instant >= start_instant  AND :instant < end_instant " +
              (ignoreDeleted ? "AND status = 'A'" : "");
  }

  //-------------------------------------------------------------------------
  /**
   * Gets a portfolio root node by object identifier and version.
   * @param portfolioOid  the object identifier
   * @param version  the version
   * @return the portfolio root node, not null
   * @throws DataNotFoundException if the root node is missing
   */
  protected PortfolioNodeImpl selectPortfolioRootNode(final long portfolioOid, final long version) {
    MapSqlParameterSource args = new MapSqlParameterSource()
      .addValue("portfolio_oid", portfolioOid)
      .addValue("version", version);
    PortfolioNodeExtractor extractor = new PortfolioNodeExtractor(portfolioOid, version);
    NamedParameterJdbcOperations namedJdbc = getTemplate().getNamedParameterJdbcOperations();
    PortfolioNodeImpl rootNode = (PortfolioNodeImpl) namedJdbc.query(sqlPortfolioRootNodeByOidVersion(), args, extractor);
    if (rootNode == null) {
      s_logger.error("Portfolio does not have a root node: " + portfolioOid);
      throw new DataNotFoundException("Portfolio does not have root node: " + portfolioOid);
    }
    return rootNode;
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
            "ORDER BY left_id, node_oid, position_oid";
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
    return "SELECT pos_node.oid AS node_oid, tree.left_id AS left_id, tree.right_id AS right_id, pos_node.name AS node_name, " +
              "pos_position.oid AS position_oid, quantity, " +
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
            "ORDER BY left_id, node_oid, position_oid";
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
            "WHERE :instant >= start_instant AND :instant < end_instant " +
              "AND status = 'A'";
  }

  //-------------------------------------------------------------------------
  /**
   * Maps SQL results to UniqueIdentifier.
   */
  protected final class UniqueIdentifierMapper implements ParameterizedRowMapper<UniqueIdentifier> {
    @Override
    public UniqueIdentifier mapRow(ResultSet rs, int rowNum) throws SQLException {
      long portfolioOid = rs.getLong("OID");
      long version = rs.getLong("VERSION");
      return createPortfolioUniqueIdentifier(portfolioOid, version);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Gets a portfolio by object identifier and version.
   * @param request  the request, not null
   * @param now  the current instant, not null
   * @return the response, null if not found
   */
  protected SearchPortfoliosResult selectPortfolioSummaries(final SearchPortfoliosRequest request, final Instant now) {
    Instant instant = Objects.firstNonNull(request.getInstant(), now);
    MapSqlParameterSource args = new MapSqlParameterSource()
      .addValue("name", getDbHelper().sqlWildcardAdjustValue(request.getName()))
      .addValue("instant", DateUtil.toSqlTimestamp(instant));
    PortfolioSummaryMapper mapper = new PortfolioSummaryMapper();
    String[] selectAndCount = sqlPortfolioSummaries(request);
    List<PortfolioSummary> result = getTemplate().query(selectAndCount[0], mapper, args);
    int totalItems = getTemplate().queryForInt(selectAndCount[1], args);
    Paging paging = new Paging(request.getPagingRequest(), totalItems);
    return new SearchPortfoliosResult(paging, result);
  }

  /**
   * Gets the SQL for searching for portfolios.
   * @param request  the request, not null
   * @return the SQL, not null
   */
  protected String[] sqlPortfolioSummaries(final SearchPortfoliosRequest request) {
    String selectSub =
      "SELECT COUNT(*) " +
      "FROM pos_position, pos_node " +
      "WHERE pos_position.node_oid = pos_node.oid " +
      "AND pos_portfolio.oid = pos_node.portfolio_oid " +
      "AND pos_portfolio.version >= pos_node.start_version AND pos_portfolio.version < pos_node.end_version " +
      "AND pos_portfolio.version >= pos_position.start_version AND pos_portfolio.version < pos_position.end_version";
    String fromWhere =
      "FROM pos_portfolio " +
      "WHERE :instant >= start_instant AND :instant < end_instant ";
    fromWhere += getDbHelper().sqlWildcardQuery("AND name ", ":name", request.getName());
    fromWhere += (request.isIncludeDeleted() ? "" : "AND status = 'A' ");
    String select =
      "SELECT oid, version, status, name, start_instant, end_instant, (" + selectSub + ") AS total_positions ";
    String selectFromWhere = select + fromWhere;
    String orderBy = "ORDER BY name, oid ";
    select = getDbHelper().sqlApplyPaging(selectFromWhere, orderBy, request.getPagingRequest());
    String count = "SELECT COUNT(*) " + fromWhere;
    return new String[] {select, count};
  }

  /**
   * Maps SQL results to PortfolioSummary.
   */
  protected final class PortfolioSummaryMapper implements ParameterizedRowMapper<PortfolioSummary> {
    @Override
    public PortfolioSummary mapRow(ResultSet rs, int rowNum) throws SQLException {
      long portfolioOid = rs.getLong("OID");
      long version = rs.getLong("VERSION");
      UniqueIdentifier uid = createPortfolioUniqueIdentifier(portfolioOid, version);
      PortfolioSummary summary = new PortfolioSummary(uid);
      summary.setName(rs.getString("NAME"));
      summary.setStartInstant(DateUtil.fromSqlTimestamp(rs.getTimestamp("START_INSTANT")));
      summary.setEndInstant(DateUtil.fromSqlTimestamp(rs.getTimestamp("END_INSTANT")));
      summary.setTotalPositions(rs.getInt("TOTAL_POSITIONS"));
      summary.setActive(rs.getString("STATUS").equals(STATUS_ACTIVE));
      return summary;
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Gets a position summary by object identifier and version.
   * @param portfolioOid  the object identifier
   * @param positionOid  the object identifier
   * @param version  the version
   * @return the position, null if not found
   */
  protected PositionSummary selectPositionSummary(final long portfolioOid, final long positionOid, final long version) {
    MapSqlParameterSource args = new MapSqlParameterSource()
      .addValue("position_oid", positionOid)
      .addValue("version", version);
    PositionSummaryExtractor extractor = new PositionSummaryExtractor(portfolioOid, version);
    NamedParameterJdbcOperations namedJdbc = getTemplate().getNamedParameterJdbcOperations();
    return (PositionSummary) namedJdbc.query(sqlPositionSummaryByOidVersion(), args, extractor);
  }

  /**
   * Gets the SQL for getting a position summary by oid and version.
   * @return the SQL, not null
   */
  protected String sqlPositionSummaryByOidVersion() {
    return "SELECT pos_position.oid AS position_oid, node_oid, quantity, " +
              "pos_securitykey.id_scheme AS seckey_scheme, pos_securitykey.id_value AS seckey_value " +
            "FROM pos_node, pos_position " +
              "LEFT JOIN pos_securitykey ON (pos_position.oid = pos_securitykey.position_oid AND :version >= pos_securitykey.start_version AND :version < pos_securitykey.end_version) " +
            "WHERE pos_position.oid = :position_oid " +
              "AND :version >= pos_position.start_version AND :version < pos_position.end_version ";
  }

  //-------------------------------------------------------------------------
  /**
   * Mapper from SQL rows to a PositionSummary.
   */
  protected final class PositionSummaryExtractor implements ResultSetExtractor {
    private final long _portfolioOid;
    private final long _version;
    private PositionSummary _summary;

    protected PositionSummaryExtractor(long portfolioOid, long version) {
      _portfolioOid = portfolioOid;
      _version = version;
    }

    @Override
    public Object extractData(ResultSet rs) throws SQLException, DataAccessException {
      while (rs.next()) {
        if (_summary == null) {
          buildSummary(rs);
        }
        final String idScheme = rs.getString("SECKEY_SCHEME");
        final String idValue = rs.getString("SECKEY_VALUE");
        if (idScheme != null && idValue != null) {
          final Identifier id = Identifier.of(idScheme, idValue);
          _summary.setSecurityKey(_summary.getSecurityKey().withIdentifier(id));
        }
      }
      return _summary;
    }

    private void buildSummary(ResultSet rs) throws SQLException {
      final long positionOid = rs.getLong("POSITION_OID");
      final long parentOid = rs.getLong("NODE_OID");
      final BigDecimal quantity = rs.getBigDecimal("QUANTITY");
      final UniqueIdentifier postionUid = createUniqueIdentifier(_portfolioOid, positionOid, _version);
      final UniqueIdentifier parentUid = createUniqueIdentifier(_portfolioOid, parentOid, _version);
      _summary = new PositionSummary(postionUid);
      _summary.setParentNode(parentUid);
      _summary.setQuantity(quantity);
      _summary.setSecurityKey(IdentifierBundle.EMPTY);
    }
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
   * @param active true if the portfolio is active, false for deleted
   * @return the version, not null
   */
  protected UniqueIdentifier insertPortfolio(
      final Portfolio portfolio, final long portfolioOid, final long version, final Instant instant, boolean active) {
    MapSqlParameterSource args = new MapSqlParameterSource()
      .addValue("portfolio_oid", portfolioOid)
      .addValue("version", version)
      .addValue("status", active ? STATUS_ACTIVE : STATUS_DELETED)
      .addValue("start_instant", DateUtil.toSqlTimestamp(instant))
      .addValue("end_instant", END_INSTANT)
      .addValue("name", portfolio.getName());
    getTemplate().update(sqlInsertPortfolio(), args);
    return createPortfolioUniqueIdentifier(portfolioOid, version);
  }

  /**
   * Gets the SQL for getting the version by instant.
   * @return the SQL, not null
   */
  protected String sqlInsertPortfolio() {
    return "INSERT INTO pos_portfolio " +
              "(oid, version, status, start_instant, end_instant, name) " +
            "VALUES " +
              "(:portfolio_oid, :version, :status, :start_instant, :end_instant, :name)";
  }

  //-------------------------------------------------------------------------
  /**
   * Inserts a tree of nodes.
   * @param rootNode  the root node, not null
   * @param portfolioOid  the portfolio oid, not null
   * @param version  the version, not null
   * @return the root node object identifier, not null
   */
  protected UniqueIdentifier insertNodes(
      final PortfolioNode rootNode, final long portfolioOid, final long version) {
    final long nodeOid = selectNextNodeOid();
    final List<MapSqlParameterSource> nodeList = new ArrayList<MapSqlParameterSource>();
    final List<MapSqlParameterSource> treeList = new ArrayList<MapSqlParameterSource>();
    insertNodesBuildArgs(rootNode, portfolioOid, new long[] {nodeOid, 1}, version, nodeList, treeList);
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
  protected void insertNodesBuildArgs(
      final PortfolioNode node, final Long portfolioOid, long[] counter, final Long version,
      List<MapSqlParameterSource> nodeList, List<MapSqlParameterSource> treeList) {
    // depth first, storing the left/right before/after the loop
    final long left = counter[1]++;
    for (PortfolioNode childNode : node.getChildNodes()) {
      insertNodesBuildArgs(childNode, portfolioOid, counter, version, nodeList, treeList);
    }
    final long right = counter[1]++;
    final long nodeOid = counter[0]++;
    // the arguments for inserting into the node table
    MapSqlParameterSource nodeArgs = new MapSqlParameterSource()
      .addValue("portfolio_oid", portfolioOid)
      .addValue("node_oid", nodeOid)
      .addValue("start_version", version)
      .addValue("end_version", END_VERSION)
      .addValue("name", node.getName());
    nodeList.add(nodeArgs);
    // the arguments for inserting into the tree table
    MapSqlParameterSource treeArgs = new MapSqlParameterSource()
      .addValue("node_oid", nodeOid)
      .addValue("start_version", version)
      .addValue("end_version", END_VERSION)
      .addValue("left_id", left)
      .addValue("right_id", right);
    treeList.add(treeArgs);
    // set the uid
    UniqueIdentifier uid = createUniqueIdentifier(portfolioOid, nodeOid, version);
    setUniqueIdentifier(node, uid);
  }

  /**
   * Inserts a tree of nodes.
   * @param rootNode  the root node, not null
   * @param portfolioOid  the portfolio oid, not null
   * @param version  the version, not null
   * @return the root node object identifier, not null
   */
  protected UniqueIdentifier insertTree(
      final PortfolioNode rootNode, final long portfolioOid, final long version) {
    final long nodeOid = selectNextNodeOid();
    final List<MapSqlParameterSource> treeList = new ArrayList<MapSqlParameterSource>();
    insertTreeBuildArgs(rootNode, portfolioOid, new long[] {1}, version, treeList);
    getTemplate().batchUpdate(sqlInsertTree(), (MapSqlParameterSource[]) treeList.toArray(new MapSqlParameterSource[treeList.size()]));
    return createUniqueIdentifier(portfolioOid, nodeOid, version);
  }

  /**
   * Recursively create the arguments to insert into the tree existing nodes.
   * @param node  the root node, not null
   * @param portfolioOid  the portfolio oid, not null
   * @param counter  the counter to create node oid, not null
   * @param version  the version, not null
   * @param treeList  the list of tree arguments to build, not null
   */
  protected void insertTreeBuildArgs(
      final PortfolioNode node, final Long portfolioOid, long[] counter, final Long version, List<MapSqlParameterSource> treeList) {
    // depth first, storing the left/right before/after the loop
    final long left = counter[0]++;
    for (PortfolioNode childNode : node.getChildNodes()) {
      insertTreeBuildArgs(childNode, portfolioOid, counter, version, treeList);
    }
    final long right = counter[0]++;
    final long nodeOid = getParent().extractOtherOid(node.getUniqueIdentifier());
    MapSqlParameterSource treeArgs = new MapSqlParameterSource()
      .addValue("node_oid", nodeOid)
      .addValue("start_version", version)
      .addValue("end_version", END_VERSION)
      .addValue("left_id", left)
      .addValue("right_id", right);
    treeList.add(treeArgs);
    // set the uid
    UniqueIdentifier uid = createUniqueIdentifier(portfolioOid, nodeOid, version);
    setUniqueIdentifier(node, uid);
  }

  /**
   * Inserts a row into the node table.
   * @param node  the portfolio node, not null
   * @param portfolioOid  the portfolio oid, not null
   * @param nodeOid  the node oid, not null
   * @param version  the version, not null
   * @param instant  the instant to store at, not null
   * @return the version, not null
   */
  protected UniqueIdentifier insertNode(
      final PortfolioNode node, final long portfolioOid, final long nodeOid, final long version, final Instant instant) {
    MapSqlParameterSource args = new MapSqlParameterSource()
      .addValue("portfolio_oid", portfolioOid)
      .addValue("node_oid", nodeOid)
      .addValue("start_version", version)
      .addValue("end_version", END_VERSION)
      .addValue("name", node.getName());
    getTemplate().update(sqlInsertNode(), args);
    UniqueIdentifier uid = createUniqueIdentifier(portfolioOid, nodeOid, version);
    setUniqueIdentifier(node, uid);
    return uid;
  }

  /**
   * Gets the SQL for inserting a node.
   * @return the SQL, not null
   */
  protected String sqlInsertNode() {
    return "INSERT INTO pos_node " +
              "(portfolio_oid, oid, start_version, end_version, name) " +
            "VALUES " +
              "(:portfolio_oid, :node_oid, :start_version, :end_version, :name)";
  }

  /**
   * Gets the SQL for inserting a node in the tree.
   * @return the SQL, not null
   */
  protected String sqlInsertTree() {
    return "INSERT INTO pos_nodetree " +
              "(node_oid, start_version, end_version, left_id, right_id) " +
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
    final Long nodeOid = getParent().extractOtherOid(node.getUniqueIdentifier());
    for (Position position : node.getPositions()) {
      final long positionOid = counter[0]++;
      // the arguments for inserting into the position table
      MapSqlParameterSource positionArgs = new MapSqlParameterSource()
        .addValue("node_oid", nodeOid)
        .addValue("position_oid", positionOid)
        .addValue("start_version", version)
        .addValue("end_version", END_VERSION)
        .addValue("quantity", position.getQuantity());
      positionList.add(positionArgs);
      // the arguments for inserting into the seckey table
      for (Identifier id : position.getSecurityKey()) {
        final long secKeyOid = counter[1]++;
        MapSqlParameterSource treeArgs = new MapSqlParameterSource()
          .addValue("position_oid", positionOid)
          .addValue("seckey_oid", secKeyOid)
          .addValue("start_version", version)
          .addValue("end_version", END_VERSION)
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
              "(node_oid, oid, start_version, end_version, quantity) " +
            "VALUES " +
              "(:node_oid, :position_oid, :start_version, :end_version, :quantity)";
  }

  /**
   * Gets the SQL for inserting a security key.
   * @return the SQL, not null
   */
  protected String sqlInsertSecurityKey() {
    return "INSERT INTO pos_securitykey " +
              "(position_oid, oid, start_version, end_version, id_scheme, id_value) " +
            "VALUES " +
              "(:position_oid, :seckey_oid, :start_version, :end_version, :id_scheme, :id_value)";
  }

  //-------------------------------------------------------------------------
  /**
   * Updates the portfolio table to end-date a portfolio.
   * @param portfolioOid  the portfolio to end
   * @param instant  the instant to use, not null
   */
  protected void updatePortfolioSetEndInstant(final long portfolioOid, final Instant instant) {
    MapSqlParameterSource args = new MapSqlParameterSource()
      .addValue("portfolio_oid", portfolioOid)
      .addValue("search_instant", END_INSTANT)
      .addValue("end_instant", DateUtil.toSqlTimestamp(instant));
    getTemplate().update(sqlUpdatePortfolioSetEndInstant(), args);
  }

  /**
   * Gets the SQL for end-dating a portfolio.
   * @return the SQL, not null
   */
  protected String sqlUpdatePortfolioSetEndInstant() {
    return "UPDATE pos_portfolio " +
            "SET end_instant = :end_instant " +
            "WHERE oid = :portfolio_oid " +
              "AND end_instant = :search_instant ";
  }

  //-------------------------------------------------------------------------
  /**
   * Updates the tree table to end-version each node.
   * @param portfolioOid  the portfolio to end
   * @param endVersion  the version number to end the rows with
   */
  protected void updateTreeSetEndVersion(final long portfolioOid, final long endVersion) {
    MapSqlParameterSource args = new MapSqlParameterSource()
      .addValue("portfolio_oid", portfolioOid)
      .addValue("search_version", END_VERSION)
      .addValue("end_version", endVersion);
    getTemplate().update(sqlUpdateTreeSetEndVersion(), args);
  }

  /**
   * Gets the SQL for end-versioning a tree node.
   * @return the SQL, not null
   */
  protected String sqlUpdateTreeSetEndVersion() {
    return "UPDATE pos_nodetree " +
            "SET end_version = :end_version " +
            "WHERE node_oid IN (" +
                "SELECT oid FROM pos_node WHERE portfolio_oid = :portfolio_oid" +
              ") " +
              "AND end_version = :search_version";
  }

  //-------------------------------------------------------------------------
  /**
   * Updates the node table to end-version a node.
   * @param portfolioOid  the portfolio to end
   * @param nodeOid  the node to end
   * @param endVersion  the version number to end the rows with
   */
  protected void updateNodeSetEndVersion(final long portfolioOid, final long nodeOid, final long endVersion) {
    MapSqlParameterSource args = new MapSqlParameterSource()
      .addValue("node_oid", nodeOid)
      .addValue("search_version", END_VERSION)
      .addValue("end_version", endVersion);
    getTemplate().update(sqlUpdateNodeSetEndVersion(), args);
  }

  /**
   * Gets the SQL for end-versioning a node.
   * @return the SQL, not null
   */
  protected String sqlUpdateNodeSetEndVersion() {
    return "UPDATE pos_node " +
            "SET end_version = :end_version " +
            "WHERE oid = :node_oid " +
              "AND end_version = :search_version";
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
