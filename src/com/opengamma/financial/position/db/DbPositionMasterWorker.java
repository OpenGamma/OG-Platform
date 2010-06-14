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
import org.springframework.dao.DataIntegrityViolationException;
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
import com.opengamma.financial.position.ManagedPortfolioNode;
import com.opengamma.financial.position.ManagedPosition;
import com.opengamma.financial.position.PortfolioNodeSummary;
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
    String value = new StringBuilder().append(DbPositionMaster.TYPE_PORTFOLIO).append(portfolioOid).toString();
    return UniqueIdentifier.of(getIdentifierScheme(), value, Long.toString(version));
  }

  /**
   * Creates a unique identifier.
   * @param portfolioOid  the portfolio object identifier
   * @param oid  the object identifier
   * @param version  the version
   * @return the unique identifier, not null
   */
  protected UniqueIdentifier createNodeUniqueIdentifier(final long portfolioOid, final long oid, final long version) {
    String value = new StringBuilder().append(DbPositionMaster.TYPE_NODE).append(portfolioOid).append('-').append(oid).toString();
    return UniqueIdentifier.of(getIdentifierScheme(), value, Long.toString(version));
  }

  /**
   * Creates a unique identifier.
   * @param portfolioOid  the portfolio object identifier
   * @param oid  the object identifier
   * @param version  the version
   * @return the unique identifier, not null
   */
  protected UniqueIdentifier createPositionUniqueIdentifier(final long portfolioOid, final long oid, final long version) {
    String value = new StringBuilder().append(DbPositionMaster.TYPE_POSITION).append(portfolioOid).append('-').append(oid).toString();
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
              "LEFT JOIN pos_nodetree ON (pos_nodetree.node_oid = pos_node.oid AND :version >= pos_nodetree.start_version AND :version < pos_nodetree.end_version) " +
              "LEFT JOIN pos_position ON (pos_position.node_oid = pos_node.oid AND :version >= pos_position.start_version AND :version < pos_position.end_version) " +
              "LEFT JOIN pos_securitykey ON (pos_securitykey.position_oid = pos_position.oid AND pos_securitykey.position_version = pos_position.start_version) " +
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
              "LEFT JOIN pos_position ON (pos_position.node_oid = pos_node.oid AND :version >= pos_position.start_version AND :version < pos_position.end_version) " +
              "LEFT JOIN pos_securitykey ON (pos_securitykey.position_oid = pos_position.oid AND pos_securitykey.position_version = pos_position.start_version) " +
            "WHERE tree.node_oid = pos_node.oid " +  // join
              "AND base.node_oid = :node_oid " +  // filter by desired node
              "AND tree.left_id BETWEEN base.left_id AND base.right_id " +  // filter children within tree
              "AND tree.portfolio_oid = :portfolio_oid " +  // constrain tree to be within portfolio (as left_id is duplicated in each portfolio)
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
      UniqueIdentifier uid = createNodeUniqueIdentifier(_portfolioOid, nodeOid, _version);
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
      UniqueIdentifier uid = createPositionUniqueIdentifier(_portfolioOid, positionOid, _version);
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
              "LEFT JOIN pos_securitykey ON (pos_securitykey.position_oid = pos_position.oid AND pos_securitykey.position_version = pos_position.start_version) " +
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
      UniqueIdentifier uid = createPositionUniqueIdentifier(_portfolioOid, positionOid, _version);
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
    final Instant instant = Objects.firstNonNull(request.getInstant(), now);
    final MapSqlParameterSource args = new MapSqlParameterSource()
      .addValue("name", getDbHelper().sqlWildcardAdjustValue(request.getName()))
      .addValue("instant", DateUtil.toSqlTimestamp(instant));
    final PortfolioSummaryMapper mapper = new PortfolioSummaryMapper();
    final String[] selectAndCount = sqlPortfolioSummaries(request);
    final List<PortfolioSummary> result = getTemplate().query(selectAndCount[0], mapper, args);
    final int totalItems = getTemplate().queryForInt(selectAndCount[1], args);
    final Paging paging = new Paging(request.getPagingRequest(), totalItems);
    return new SearchPortfoliosResult(paging, result);
  }

  /**
   * Gets the SQL for searching for portfolios.
   * @param request  the request, not null
   * @return the SQL, not null
   */
  protected String[] sqlPortfolioSummaries(final SearchPortfoliosRequest request) {
    String selectTotalPositions =
      "SELECT COUNT(*) " +
      "FROM pos_position " +
      "WHERE pos_position.portfolio_oid = pos_portfolio.oid " +
        "AND pos_portfolio.version >= pos_position.start_version AND pos_portfolio.version < pos_position.end_version";
    String select =
      "SELECT oid, version, status, name, start_instant, end_instant, (" + selectTotalPositions + ") AS total_positions ";
    String fromWhere =
      "FROM pos_portfolio " +
      "WHERE :instant >= start_instant AND :instant < end_instant ";
    fromWhere += getDbHelper().sqlWildcardQuery("AND name ", ":name", request.getName());
    fromWhere += (request.isIncludeDeleted() ? "" : "AND status = 'A' ");
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
      final long portfolioOid = rs.getLong("OID");
      final long version = rs.getLong("VERSION");
      final UniqueIdentifier uid = createPortfolioUniqueIdentifier(portfolioOid, version);
      final PortfolioSummary summary = new PortfolioSummary(uid);
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
   * Gets a managed position by object identifier and version.
   * @param portfolioOid  the object identifier
   * @param nodeOid  the object identifier
   * @param version  the version
   * @return the position, null if not found
   */
  protected ManagedPortfolioNode selectManagedPortfolioNode(final long portfolioOid, final long nodeOid, final long version) {
    final MapSqlParameterSource args = new MapSqlParameterSource()
      .addValue("portfolio_oid", portfolioOid)
      .addValue("node_oid", nodeOid)
      .addValue("version", version);
    final ManagedPortfolioNodeExtractor extractor = new ManagedPortfolioNodeExtractor(portfolioOid, nodeOid, version);
    final NamedParameterJdbcOperations namedJdbc = getTemplate().getNamedParameterJdbcOperations();
    final ManagedPortfolioNode node = (ManagedPortfolioNode) namedJdbc.query(sqlManagedPortfolioNodeByOidVersion(), args, extractor);
    if (node != null) {
      final PositionSummaryMapper mapper = new PositionSummaryMapper(portfolioOid, version);
      List<PositionSummary> positions = getTemplate().query(sqlManagedPortfolioNodePositionsByOidVersion(), mapper, args);
      node.getPositions().addAll(positions);
    }
    return node;
  }

  /**
   * Gets the SQL for getting a position by oid and version.
   * @return the SQL, not null
   */
  protected String sqlManagedPortfolioNodeByOidVersion() {
    String selectTotalPositions =
          "SELECT COUNT(*) " +
          "FROM pos_position " +
          "WHERE pos_position.node_oid IN (" +
              "SELECT tree.node_oid " +
              "FROM pos_nodetree AS base, pos_nodetree AS tree " +
              "WHERE base.node_oid = pos_node.oid " +  // filter by desired node
                "AND tree.left_id BETWEEN base.left_id AND base.right_id " +  // filter children within tree
                "AND tree.portfolio_oid = :portfolio_oid " +  // constrain tree to be within portfolio (as left_id is duplicated in each portfolio)
                "AND :version >= base.start_version AND :version < base.end_version " +
                "AND :version >= tree.start_version AND :version < tree.end_version " +
            ")" +
            "AND :version >= pos_position.start_version AND :version < pos_position.end_version ";
    return "SELECT pos_node.oid AS node_oid, parent_node_oid, pos_node.name AS node_name, (" + selectTotalPositions + ") AS total_positions " +
            "FROM pos_nodetree, pos_node " +
            "WHERE pos_nodetree.node_oid = pos_node.oid " +  // join
              "AND :version >= pos_node.start_version AND :version < pos_node.end_version " +
              "AND :version >= pos_nodetree.start_version AND :version < pos_nodetree.end_version " +
              "AND (pos_nodetree.node_oid = :node_oid OR pos_nodetree.parent_node_oid = :node_oid) ";  // select node and children
  }

  /**
   * Gets the SQL for getting a position by oid and version.
   * @return the SQL, not null
   */
  protected String sqlManagedPortfolioNodePositionsByOidVersion() {
    return "SELECT oid AS position_oid, quantity " +
            "FROM pos_position " +
            "WHERE node_oid = :node_oid " +
              "AND :version >= start_version AND :version < end_version";
  }

  //-------------------------------------------------------------------------
  /**
   * Mapper from SQL rows to a ManagedPortfolioNode.
   */
  protected final class ManagedPortfolioNodeExtractor implements ResultSetExtractor {
    private final long _portfolioOid;
    private final long _nodeOid;
    private final long _version;

    protected ManagedPortfolioNodeExtractor(long portfolioOid, long nodeOid, long version) {
      _portfolioOid = portfolioOid;
      _nodeOid = nodeOid;
      _version = version;
    }

    @Override
    public Object extractData(ResultSet rs) throws SQLException, DataAccessException {
      ManagedPortfolioNode node = null;
      List<PortfolioNodeSummary> children = new ArrayList<PortfolioNodeSummary>();
      while (rs.next()) {
        final Long parentOid = (Long) rs.getObject("PARENT_NODE_OID");
        final long nodeOid = rs.getLong("NODE_OID");
        final String name = rs.getString("NODE_NAME");
        if (nodeOid == _nodeOid) {
          node = new ManagedPortfolioNode();
          node.setPortfolioUid(createPortfolioUniqueIdentifier(_portfolioOid, _version));
          if (parentOid != null) {
            node.setParentNodeUid(createNodeUniqueIdentifier(_portfolioOid, parentOid, _version));
          }
          node.setUniqueIdentifier(createNodeUniqueIdentifier(_portfolioOid, nodeOid, _version));
          node.setName(name);
        } else if (parentOid != null && parentOid == _nodeOid) {
          PortfolioNodeSummary child = new PortfolioNodeSummary();
          child.setUniqueIdentifier(createNodeUniqueIdentifier(_portfolioOid, nodeOid, _version));
          child.setName(name);
          child.setTotalPositions(rs.getInt("TOTAL_POSITIONS"));
          children.add(child);
        } else {
          throw new DataIntegrityViolationException("SQL statement returned invalid data");
        }
      }
      if (node == null) {
        return null;
      }
      node.getChildNodes().addAll(children);
      return node;
    }
  }

  /**
   * Maps SQL results to PositionSummary.
   */
  protected final class PositionSummaryMapper implements ParameterizedRowMapper<PositionSummary> {
    private final long _portfolioOid;
    private final long _version;

    protected PositionSummaryMapper(long portfolioOid, long version) {
      _portfolioOid = portfolioOid;
      _version = version;
    }

    @Override
    public PositionSummary mapRow(ResultSet rs, int rowNum) throws SQLException {
      final long positionOid = rs.getLong("POSITION_OID");
      final PositionSummary summary = new PositionSummary();
      summary.setUniqueIdentifier(createPositionUniqueIdentifier(_portfolioOid, positionOid, _version));
      summary.setQuantity(rs.getBigDecimal("QUANTITY"));
      return summary;
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Gets a managed position by object identifier and version.
   * @param portfolioOid  the object identifier
   * @param positionOid  the object identifier
   * @param version  the version
   * @return the position, null if not found
   */
  protected ManagedPosition selectManagedPosition(final long portfolioOid, final long positionOid, final long version) {
    final MapSqlParameterSource args = new MapSqlParameterSource()
      .addValue("position_oid", positionOid)
      .addValue("version", version);
    final ManagedPositionExtractor extractor = new ManagedPositionExtractor(portfolioOid, version);
    final NamedParameterJdbcOperations namedJdbc = getTemplate().getNamedParameterJdbcOperations();
    return (ManagedPosition) namedJdbc.query(sqlManagedPositionByOidVersion(), args, extractor);
  }

  /**
   * Gets the SQL for getting a position by oid and version.
   * @return the SQL, not null
   */
  protected String sqlManagedPositionByOidVersion() {
    return "SELECT pos_position.oid AS position_oid, node_oid, quantity, " +
              "pos_securitykey.id_scheme AS seckey_scheme, pos_securitykey.id_value AS seckey_value " +
            "FROM pos_node, pos_position " +
              "LEFT JOIN pos_securitykey ON (pos_securitykey.position_oid = pos_position.oid AND pos_securitykey.position_version = pos_position.start_version) " +
            "WHERE pos_position.oid = :position_oid " +
              "AND :version >= pos_position.start_version AND :version < pos_position.end_version ";
  }

  //-------------------------------------------------------------------------
  /**
   * Mapper from SQL rows to a ManagedPosition.
   */
  protected final class ManagedPositionExtractor implements ResultSetExtractor {
    private final long _portfolioOid;
    private final long _version;
    private ManagedPosition _object;

    protected ManagedPositionExtractor(long portfolioOid, long version) {
      _portfolioOid = portfolioOid;
      _version = version;
    }

    @Override
    public Object extractData(ResultSet rs) throws SQLException, DataAccessException {
      while (rs.next()) {
        if (_object == null) {
          buildObject(rs);
        }
        final String idScheme = rs.getString("SECKEY_SCHEME");
        final String idValue = rs.getString("SECKEY_VALUE");
        if (idScheme != null && idValue != null) {
          final Identifier id = Identifier.of(idScheme, idValue);
          _object.setSecurityKey(_object.getSecurityKey().withIdentifier(id));
        }
      }
      return _object;
    }

    private void buildObject(ResultSet rs) throws SQLException {
      final long positionOid = rs.getLong("POSITION_OID");
      final long parentOid = rs.getLong("NODE_OID");
      final BigDecimal quantity = rs.getBigDecimal("QUANTITY");
      _object = new ManagedPosition();
      _object.setPortfolioUid(createPortfolioUniqueIdentifier(_portfolioOid, _version));
      _object.setParentNodeUid(createNodeUniqueIdentifier(_portfolioOid, parentOid, _version));
      _object.setUniqueIdentifier(createPositionUniqueIdentifier(_portfolioOid, positionOid, _version));
      _object.setQuantity(quantity);
      _object.setSecurityKey(IdentifierBundle.EMPTY);
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
    final MapSqlParameterSource args = new MapSqlParameterSource()
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
    insertNodesBuildArgs(rootNode, null, portfolioOid, new long[] {nodeOid, 1}, version, nodeList, treeList);
    getTemplate().batchUpdate(sqlInsertNode(), (MapSqlParameterSource[]) nodeList.toArray(new MapSqlParameterSource[nodeList.size()]));
    getTemplate().batchUpdate(sqlInsertTree(), (MapSqlParameterSource[]) treeList.toArray(new MapSqlParameterSource[treeList.size()]));
    return createNodeUniqueIdentifier(portfolioOid, nodeOid, version);
  }

  /**
   * Recursively create the arguments to insert the nodes.
   * @param node  the root node, not null
   * @param portfolioOid  the portfolio oid, not null
   * @param parentNodeOid  the parent node oid, null if root node
   * @param counter  the counter to create node oid, not null
   * @param version  the version, not null
   * @param nodeList  the list of node arguments to build, not null
   * @param treeList  the list of tree arguments to build, not null
   */
  protected void insertNodesBuildArgs(
      final PortfolioNode node, final Long parentNodeOid, final long portfolioOid, long[] counter, final Long version,
      List<MapSqlParameterSource> nodeList, List<MapSqlParameterSource> treeList) {
    // depth first, storing the left/right before/after the loop
    final long nodeOid = counter[0]++;
    final long left = counter[1]++;
    for (PortfolioNode childNode : node.getChildNodes()) {
      insertNodesBuildArgs(childNode, nodeOid, portfolioOid, counter, version, nodeList, treeList);
    }
    final long right = counter[1]++;
    // the arguments for inserting into the node table
    final MapSqlParameterSource nodeArgs = new MapSqlParameterSource()
      .addValue("portfolio_oid", portfolioOid)
      .addValue("node_oid", nodeOid)
      .addValue("start_version", version)
      .addValue("end_version", END_VERSION)
      .addValue("name", node.getName());
    nodeList.add(nodeArgs);
    // the arguments for inserting into the tree table
    final MapSqlParameterSource treeArgs = new MapSqlParameterSource()
      .addValue("portfolio_oid", portfolioOid)
      .addValue("parent_node_oid", parentNodeOid)
      .addValue("node_oid", nodeOid)
      .addValue("start_version", version)
      .addValue("end_version", END_VERSION)
      .addValue("left_id", left)
      .addValue("right_id", right);
    treeList.add(treeArgs);
    // set the uid
    final UniqueIdentifier uid = createNodeUniqueIdentifier(portfolioOid, nodeOid, version);
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
    insertTreeBuildArgs(rootNode, null, portfolioOid, new long[] {1}, version, treeList);
    getTemplate().batchUpdate(sqlInsertTree(), (MapSqlParameterSource[]) treeList.toArray(new MapSqlParameterSource[treeList.size()]));
    return createNodeUniqueIdentifier(portfolioOid, nodeOid, version);
  }

  /**
   * Recursively create the arguments to insert into the tree existing nodes.
   * @param node  the root node, not null
   * @param portfolioOid  the portfolio oid, not null
   * @param parentNodeOid  the parent node oid, null if root node
   * @param counter  the counter to create node oid, not null
   * @param version  the version, not null
   * @param treeList  the list of tree arguments to build, not null
   */
  protected void insertTreeBuildArgs(
      final PortfolioNode node, final Long parentNodeOid, final Long portfolioOid, long[] counter, final Long version, List<MapSqlParameterSource> treeList) {
    // depth first, storing the left/right before/after the loop
    final long nodeOid = getParent().extractOtherOid(node.getUniqueIdentifier());
    final long left = counter[0]++;
    for (PortfolioNode childNode : node.getChildNodes()) {
      insertTreeBuildArgs(childNode, nodeOid, portfolioOid, counter, version, treeList);
    }
    final long right = counter[0]++;
    final MapSqlParameterSource treeArgs = new MapSqlParameterSource()
      .addValue("portfolio_oid", portfolioOid)
      .addValue("parent_node_oid", parentNodeOid)
      .addValue("node_oid", nodeOid)
      .addValue("start_version", version)
      .addValue("end_version", END_VERSION)
      .addValue("left_id", left)
      .addValue("right_id", right);
    treeList.add(treeArgs);
    // set the uid
    final UniqueIdentifier uid = createNodeUniqueIdentifier(portfolioOid, nodeOid, version);
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
    final MapSqlParameterSource args = new MapSqlParameterSource()
      .addValue("portfolio_oid", portfolioOid)
      .addValue("node_oid", nodeOid)
      .addValue("start_version", version)
      .addValue("end_version", END_VERSION)
      .addValue("name", node.getName());
    getTemplate().update(sqlInsertNode(), args);
    final UniqueIdentifier uid = createNodeUniqueIdentifier(portfolioOid, nodeOid, version);
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
              "(portfolio_oid, parent_node_oid, node_oid, start_version, end_version, left_id, right_id) " +
            "VALUES " +
              "(:portfolio_oid, :parent_node_oid, :node_oid, :start_version, :end_version, :left_id, :right_id)";
  }

  //-------------------------------------------------------------------------
  /**
   * Inserts a row into the tree/node tables.
   * @param rootNode  the root node, not null
   * @param portfolioOid  the portfolio object identifier, not null
   * @param version  the version, not null
   */
  protected void insertTreePositions(
      final PortfolioNode rootNode, final long portfolioOid, final long version) {
    final long positionOid = selectNextPositionOid();
    final List<MapSqlParameterSource> positionList = new ArrayList<MapSqlParameterSource>();
    final List<MapSqlParameterSource> secKeyList = new ArrayList<MapSqlParameterSource>();
    insertTreePositionsBuildArgs(rootNode, portfolioOid, new long[] {positionOid}, version, positionList, secKeyList);
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
      final PortfolioNode node, final long portfolioOid, final long[] counter, final Long version,
      final List<MapSqlParameterSource> positionList, final List<MapSqlParameterSource> secKeyList) {
    // depth first
    for (PortfolioNode childNode : node.getChildNodes()) {
      insertTreePositionsBuildArgs(childNode, portfolioOid, counter, version, positionList, secKeyList);
    }
    final Long nodeOid = getParent().extractOtherOid(node.getUniqueIdentifier());
    for (Position position : node.getPositions()) {
      final long positionOid = counter[0]++;
      // the arguments for inserting into the position table
      final MapSqlParameterSource positionArgs = new MapSqlParameterSource()
        .addValue("portfolio_oid", portfolioOid)
        .addValue("node_oid", nodeOid)
        .addValue("position_oid", positionOid)
        .addValue("start_version", version)
        .addValue("end_version", END_VERSION)
        .addValue("quantity", position.getQuantity());
      positionList.add(positionArgs);
      // the arguments for inserting into the seckey table
      for (Identifier id : position.getSecurityKey()) {
        final MapSqlParameterSource treeArgs = new MapSqlParameterSource()
          .addValue("position_oid", positionOid)
          .addValue("position_version", version)
          .addValue("id_scheme", id.getScheme().getName())
          .addValue("id_value", id.getValue());
        secKeyList.add(treeArgs);
      }
      // set the uid
      final UniqueIdentifier uid = createPositionUniqueIdentifier(portfolioOid, positionOid, version);
      setUniqueIdentifier(position, uid);
    }
  }

  /**
   * Inserts a row into the tree/node tables.
   * @param position  the position data, not null
   * @param portfolioOid  the portfolio object identifier, not null
   * @param parentNodeOid  the parent node object identifier, not null
   * @param version  the version, not null
   * @return the unique identifier of the inserted position, not null
   */
  protected UniqueIdentifier insertPosition(
      final Position position, final long portfolioOid, final long parentNodeOid, final long version) {
    final UniqueIdentifier parentUid = createNodeUniqueIdentifier(portfolioOid, parentNodeOid, version);
    final PortfolioNodeImpl tempRoot = new PortfolioNodeImpl(parentUid, "Temp");
    tempRoot.addPosition(position);
    insertTreePositions(tempRoot, portfolioOid, version);
    return position.getUniqueIdentifier();
  }

  /**
   * Gets the SQL for inserting a position.
   * @return the SQL, not null
   */
  protected String sqlInsertPosition() {
    return "INSERT INTO pos_position " +
              "(portfolio_oid, node_oid, oid, start_version, end_version, quantity) " +
            "VALUES " +
              "(:portfolio_oid, :node_oid, :position_oid, :start_version, :end_version, :quantity)";
  }

  /**
   * Gets the SQL for inserting a security key.
   * @return the SQL, not null
   */
  protected String sqlInsertSecurityKey() {
    return "INSERT INTO pos_securitykey " +
              "(position_oid, position_version, id_scheme, id_value) " +
            "VALUES " +
              "(:position_oid, :position_version, :id_scheme, :id_value)";
  }

  //-------------------------------------------------------------------------
  /**
   * Updates the portfolio table to end-date a portfolio.
   * @param portfolioOid  the portfolio to end
   * @param instant  the instant to use, not null
   */
  protected void updatePortfolioSetEndInstant(final long portfolioOid, final Instant instant) {
    final MapSqlParameterSource args = new MapSqlParameterSource()
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
    final MapSqlParameterSource args = new MapSqlParameterSource()
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
            "WHERE portfolio_oid = :portfolio_oid " +
              "AND end_version = :search_version";
  }

  //-------------------------------------------------------------------------
  /**
   * Updates the node table to end-version a node for removal.
   * This updates the node and position tables but not the tree table.
   * @param portfolioOid  the portfolio to end
   * @param nodeOid  the node to end
   * @param endVersion  the version number to end the rows with
   */
  protected void updateNodesAndPositionsForRemovalSetEndVersion(final long portfolioOid, final long nodeOid, final long endVersion) {
    final MapSqlParameterSource args = new MapSqlParameterSource()
      .addValue("portfolio_oid", portfolioOid)
      .addValue("node_oid", nodeOid)
      .addValue("search_version", END_VERSION)
      .addValue("end_version", endVersion);
    getTemplate().update(sqlUpdatePositionsForRemovalSetEndVersion(), args);
    getTemplate().update(sqlUpdateNodesForRemovalSetEndVersion(), args);
  }

  /**
   * Gets the SQL for end-versioning nodes from a parent tree node.
   * @return the SQL, not null
   */
  protected String sqlUpdateNodesForRemovalSetEndVersion() {
    return "UPDATE pos_node " +
            "SET end_version = :end_version " +
            "WHERE oid IN (" +
                "SELECT tree.node_oid " +
                "FROM pos_nodetree AS base, pos_nodetree AS tree " +
                "WHERE base.node_oid = :node_oid " +  // filter by desired node
                  "AND tree.left_id BETWEEN base.left_id AND base.right_id " +  // filter children within tree
                  "AND tree.portfolio_oid = :portfolio_oid " +  // filter by this portfolio (because tree left_id is not constrained)
                  "AND base.end_version = :search_version " +
                  "AND tree.end_version = :search_version " +
              ") " +
              "AND end_version = :search_version ";
  }

  /**
   * Gets the SQL for end-versioning positions from a parent tree node.
   * @return the SQL, not null
   */
  protected String sqlUpdatePositionsForRemovalSetEndVersion() {
    return "UPDATE pos_position " +
            "SET end_version = :end_version " +
            "WHERE node_oid IN (" +
                "SELECT tree.node_oid " +
                "FROM pos_nodetree AS base, pos_nodetree AS tree " +
                "WHERE base.node_oid = :node_oid " +  // filter by desired node
                  "AND tree.left_id BETWEEN base.left_id AND base.right_id " +  // filter children within tree
                  "AND tree.portfolio_oid = :portfolio_oid " +  // filter by this portfolio (because tree left_id is not constrained)
                  "AND base.end_version = :search_version " +
                  "AND tree.end_version = :search_version " +
              ") " +
              "AND end_version = :search_version ";
  }

  //-------------------------------------------------------------------------
  /**
   * Updates the node table to end-version a node.
   * @param portfolioOid  the portfolio to end
   * @param nodeOid  the node to end
   * @param endVersion  the version number to end the rows with
   */
  protected void updateNodeSetEndVersion(final long portfolioOid, final long nodeOid, final long endVersion) {
    final MapSqlParameterSource args = new MapSqlParameterSource()
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
   * Updates the position table to end-version a position and associated seckeys.
   * @param portfolioOid  the portfolio to end
   * @param positionOid  the position to end
   * @param endVersion  the version number to end the rows with
   */
  protected void updatePositionSetEndVersion(final long portfolioOid, final long positionOid, final long endVersion) {
    final MapSqlParameterSource args = new MapSqlParameterSource()
      .addValue("position_oid", positionOid)
      .addValue("search_version", END_VERSION)
      .addValue("end_version", endVersion);
    getTemplate().update(sqlUpdatePositionSetEndVersion(), args);
  }

  /**
   * Gets the SQL for end-versioning a position.
   * @return the SQL, not null
   */
  protected String sqlUpdatePositionSetEndVersion() {
    return "UPDATE pos_position " +
            "SET end_version = :end_version " +
            "WHERE oid = :position_oid " +
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
