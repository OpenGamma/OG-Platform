/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.portfolio;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

import javax.time.Instant;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.opengamma.DataNotFoundException;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.AbstractHistoryRequest;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioHistoryRequest;
import com.opengamma.master.portfolio.PortfolioHistoryResult;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;
import com.opengamma.master.portfolio.PortfolioSearchSortOrder;
import com.opengamma.masterdb.AbstractDocumentDbMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.DbConnector;
import com.opengamma.util.db.DbDateUtils;
import com.opengamma.util.db.DbMapSqlParameterSource;
import com.opengamma.util.paging.Paging;
import com.opengamma.util.tuple.LongObjectPair;

/**
 * A portfolio master implementation using a database for persistence.
 * <p>
 * This is a full implementation of the portfolio master using an SQL database.
 * Full details of the API are in {@link PortfolioMaster}.
 * <p>
 * This class uses SQL via JDBC. The SQL may be changed by subclassing the relevant methods.
 * <p>
 * This class is mutable but must be treated as immutable after configuration.
 */
public class DbPortfolioMaster extends AbstractDocumentDbMaster<PortfolioDocument> implements PortfolioMaster {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(DbPortfolioMaster.class);

  /**
   * The default scheme for unique identifiers.
   */
  public static final String IDENTIFIER_SCHEME_DEFAULT = "DbPrt";
  /**
   * SQL select.
   */
  protected static final String SELECT =
      "SELECT " +
        "main.id AS portfolio_id, " +
        "main.oid AS portfolio_oid, " +
        "main.ver_from_instant AS ver_from_instant, " +
        "main.ver_to_instant AS ver_to_instant, " +
        "main.corr_from_instant AS corr_from_instant, " +
        "main.corr_to_instant AS corr_to_instant, " +
        "main.name AS portfolio_name, " +
        "n.id AS node_id, " +
        "n.oid AS node_oid, " +
        "n.tree_left AS tree_left, " +
        "n.tree_right AS tree_right, " +
        "n.name AS node_name, " +
        "p.key_scheme AS pos_key_scheme, " +
        "p.key_value AS pos_key_value, " +
        "prtAttr.key AS prt_attr_key, " +
        "prtAttr.value AS prt_attr_value ";
  /**
   * SQL from.
   */
  protected static final String FROM =
      "FROM prt_portfolio main " +
        "LEFT JOIN prt_node n ON (n.portfolio_id = main.id) " +
        "LEFT JOIN prt_position p ON (p.node_id = n.id) " +
        "LEFT JOIN prt_portfolio_attribute prtAttr ON (prtAttr.portfolio_id = main.id) ";
  /**
   * SQL order by.
   */
  protected static final EnumMap<PortfolioSearchSortOrder, String> ORDER_BY_MAP = new EnumMap<PortfolioSearchSortOrder, String>(PortfolioSearchSortOrder.class);
  static {
    ORDER_BY_MAP.put(PortfolioSearchSortOrder.OBJECT_ID_ASC, "oid ASC");
    ORDER_BY_MAP.put(PortfolioSearchSortOrder.OBJECT_ID_DESC, "oid DESC");
    ORDER_BY_MAP.put(PortfolioSearchSortOrder.VERSION_FROM_INSTANT_ASC, "ver_from_instant ASC");
    ORDER_BY_MAP.put(PortfolioSearchSortOrder.VERSION_FROM_INSTANT_DESC, "ver_from_instant DESC");
    ORDER_BY_MAP.put(PortfolioSearchSortOrder.NAME_ASC, "name ASC");
    ORDER_BY_MAP.put(PortfolioSearchSortOrder.NAME_DESC, "name DESC");
  }

  /**
   * Creates an instance.
   * 
   * @param dbConnector  the database connector, not null
   */
  public DbPortfolioMaster(final DbConnector dbConnector) {
    super(dbConnector, IDENTIFIER_SCHEME_DEFAULT);
  }

  //-------------------------------------------------------------------------
  @Override
  public PortfolioSearchResult search(final PortfolioSearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getPagingRequest(), "request.pagingRequest");
    ArgumentChecker.notNull(request.getVersionCorrection(), "request.versionCorrection");
    s_logger.debug("search {}", request);
    
    final PortfolioSearchResult result = new PortfolioSearchResult();
    if ((request.getPortfolioObjectIds() != null && request.getPortfolioObjectIds().size() == 0) ||
        (request.getNodeObjectIds() != null && request.getNodeObjectIds().size() == 0)) {
      result.setPaging(Paging.of(request.getPagingRequest(), 0));
      return result;
    }
    final VersionCorrection vc = request.getVersionCorrection().withLatestFixed(now());
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addTimestamp("version_as_of_instant", vc.getVersionAsOf())
      .addTimestamp("corrected_to_instant", vc.getCorrectedTo())
      .addValue("name", getDialect().sqlWildcardAdjustValue(request.getName()))
      .addValue("depth", request.getDepth());
    searchWithPaging(request.getPagingRequest(), sqlSearch(request), args, new PortfolioDocumentExtractor(true), result);
    return result;
  }

  /**
   * Gets the SQL to search for portfolios.
   * 
   * @param request  the request, not null
   * @return the SQL search and count, not null
   */
  protected String[] sqlSearch(final PortfolioSearchRequest request) {
    String where = "WHERE ver_from_instant <= :version_as_of_instant AND ver_to_instant > :version_as_of_instant " +
                "AND corr_from_instant <= :corrected_to_instant AND corr_to_instant > :corrected_to_instant ";
    if (request.getName() != null) {
      where += getDialect().sqlWildcardQuery("AND UPPER(name) ", "UPPER(:name)", request.getName());
    }
    if (request.getPortfolioObjectIds() != null) {
      StringBuilder buf = new StringBuilder(request.getPortfolioObjectIds().size() * 10);
      for (ObjectId objectId : request.getPortfolioObjectIds()) {
        checkScheme(objectId);
        buf.append(extractOid(objectId)).append(", ");
      }
      buf.setLength(buf.length() - 2);
      where += "AND oid IN (" + buf + ") ";
    }
    if (request.getNodeObjectIds() != null) {
      StringBuilder buf = new StringBuilder(request.getNodeObjectIds().size() * 10);
      for (ObjectId objectId : request.getNodeObjectIds()) {
        checkScheme(objectId);
        buf.append(extractOid(objectId)).append(", ");
      }
      buf.setLength(buf.length() - 2);
      where += "AND oid IN (SELECT DISTINCT portfolio_oid FROM prt_node WHERE oid IN (" + buf + ")) ";
    }
    
    String selectFromWhereInner = "SELECT id FROM prt_portfolio " + where;
    String orderBy = ORDER_BY_MAP.get(request.getSortOrder());
    String inner = getDialect().sqlApplyPaging(selectFromWhereInner, "ORDER BY " + orderBy + " ", request.getPagingRequest());
    String search = SELECT + FROM + "WHERE main.id IN (" + inner + ") ";
    if (request.getDepth() >= 0) {
      search += "AND n.depth <= :depth ";
    }
    search += "ORDER BY main." + orderBy + sqlAdditionalOrderBy(false);
    String count = "SELECT COUNT(*) FROM prt_portfolio " + where;
    return new String[] {search, count};
  }

  //-------------------------------------------------------------------------
  @Override
  public PortfolioDocument get(final UniqueId uniqueId) {
    return doGet(uniqueId, new PortfolioDocumentExtractor(true), "Portfolio");
  }

  //-------------------------------------------------------------------------
  @Override
  public PortfolioDocument get(final ObjectIdentifiable objectId, final VersionCorrection versionCorrection) {
    return doGetByOidInstants(objectId, versionCorrection, new PortfolioDocumentExtractor(true), "Portfolio");
  }

  //-------------------------------------------------------------------------
  @Override
  public PortfolioHistoryResult history(final PortfolioHistoryRequest request) {
    return doHistory(request, new PortfolioHistoryResult(), new PortfolioDocumentExtractor(true));
  }

  @Override
  protected DbMapSqlParameterSource argsHistory(AbstractHistoryRequest request) {
    DbMapSqlParameterSource args = super.argsHistory(request);
    args.addValue("depth", ((PortfolioHistoryRequest) request).getDepth());
    return args;
  }

  @Override
  protected String[] sqlHistory(AbstractHistoryRequest request) {
    String where = sqlHistoryWhere(request);
    String selectFromWhereInner = "SELECT id FROM " + mainTableName() + " " + where;
    String inner = getDialect().sqlApplyPaging(selectFromWhereInner, "ORDER BY ver_from_instant DESC, corr_from_instant DESC ", request.getPagingRequest());
    String search = SELECT + FROM + "WHERE main.id IN (" + inner + ") ";
    if (((PortfolioHistoryRequest) request).getDepth() >= 0) {
      search += "AND n.depth <= :depth ";
    }
    search += "ORDER BY main.ver_from_instant DESC, main.corr_from_instant DESC" + sqlAdditionalOrderBy(false);
    String count = "SELECT COUNT(*) FROM " + mainTableName() + " " + where;
    return new String[] {search, count};
  }

  //-------------------------------------------------------------------------
  /**
   * Inserts a new document.
   * 
   * @param document  the document, not null
   * @return the new document, not null
   */
  @Override
  protected PortfolioDocument insert(final PortfolioDocument document) {
    ArgumentChecker.notNull(document.getPortfolio(), "document.portfolio");
    ArgumentChecker.notNull(document.getPortfolio().getRootNode(), "document.portfolio.rootNode");
    
    final Long portfolioId = nextId("prt_master_seq");
    final Long portfolioOid = (document.getUniqueId() != null ? extractOid(document.getUniqueId()) : portfolioId);
    final UniqueId portfolioUid = createUniqueId(portfolioOid, portfolioId);
    
    // the arguments for inserting into the portfolio table
    final DbMapSqlParameterSource portfolioArgs = new DbMapSqlParameterSource()
      .addValue("portfolio_id", portfolioId)
      .addValue("portfolio_oid", portfolioOid)
      .addTimestamp("ver_from_instant", document.getVersionFromInstant())
      .addTimestampNullFuture("ver_to_instant", document.getVersionToInstant())
      .addTimestamp("corr_from_instant", document.getCorrectionFromInstant())
      .addTimestampNullFuture("corr_to_instant", document.getCorrectionToInstant())
      .addValue("name", StringUtils.defaultString(document.getPortfolio().getName()));
    
    // the arguments for inserting into the node table
    final List<DbMapSqlParameterSource> nodeList = new ArrayList<DbMapSqlParameterSource>(256);
    final List<DbMapSqlParameterSource> posList = new ArrayList<DbMapSqlParameterSource>(256);
    insertBuildArgs(portfolioUid, null, document.getPortfolio().getRootNode(), document.getUniqueId() != null,
        portfolioId, portfolioOid, null, null,
        new AtomicInteger(1), 0, nodeList, posList);
    
    // the arguments for inserting into the portifolio_attribute table
    final List<DbMapSqlParameterSource> prtAttrList = Lists.newArrayList();
    for (Entry<String, String> entry : document.getPortfolio().getAttributes().entrySet()) {
      final long prtAttrId = nextId("prt_portfolio_attr_seq");
      final DbMapSqlParameterSource posAttrArgs = new DbMapSqlParameterSource()
          .addValue("attr_id", prtAttrId)
          .addValue("portfolio_id", portfolioId)
          .addValue("portfolio_oid", portfolioOid)
          .addValue("key", entry.getKey())
          .addValue("value", entry.getValue());
      prtAttrList.add(posAttrArgs);
    }
    
    getJdbcTemplate().update(sqlInsertPortfolio(), portfolioArgs);
    getJdbcTemplate().batchUpdate(sqlInsertNode(), nodeList.toArray(new DbMapSqlParameterSource[nodeList.size()]));
    getJdbcTemplate().batchUpdate(sqlInsertPosition(), posList.toArray(new DbMapSqlParameterSource[posList.size()]));
    getJdbcTemplate().batchUpdate(sqlInsertPortfolioAttributes(), prtAttrList.toArray(new DbMapSqlParameterSource[prtAttrList.size()]));
    // set the uniqueId
    document.getPortfolio().setUniqueId(portfolioUid);
    document.setUniqueId(portfolioUid);
    return document;
  }

  /**
   * Recursively create the arguments to insert into the tree existing nodes.
   * 
   * @param portfolioUid  the portfolio unique identifier, not null
   * @param parentNodeUid  the parent node unique identifier, not null
   * @param node  the root node, not null
   * @param update  true if updating portfolio, false if adding new portfolio
   * @param portfolioId  the portfolio id, not null
   * @param portfolioOid  the portfolio oid, not null
   * @param parentNodeId  the parent node id, null if root node
   * @param parentNodeOid  the parent node oid, null if root node
   * @param counter  the counter to create the node id, use {@code getAndIncrement}, not null
   * @param depth  the depth of the node in the portfolio
   * @param argsList  the list of arguments to build, not null
   * @param posList  the list of arguments to for inserting positions, not null
   */
  protected void insertBuildArgs(
      final UniqueId portfolioUid, final UniqueId parentNodeUid,
      final ManageablePortfolioNode node, final boolean update,
      final Long portfolioId, final Long portfolioOid, final Long parentNodeId, final Long parentNodeOid,
      final AtomicInteger counter, final int depth, final List<DbMapSqlParameterSource> argsList, final List<DbMapSqlParameterSource> posList) {
    // need to insert parent before children for referential integrity
    final Long nodeId = nextId("prt_master_seq");
    final Long nodeOid = (update && node.getUniqueId() != null ? extractOid(node.getUniqueId()) : nodeId);
    UniqueId nodeUid = createUniqueId(nodeOid, nodeId);
    node.setUniqueId(nodeUid);
    node.setParentNodeId(parentNodeUid);
    node.setPortfolioId(portfolioUid);
    final DbMapSqlParameterSource treeArgs = new DbMapSqlParameterSource()
      .addValue("node_id", nodeId)
      .addValue("node_oid", nodeOid)
      .addValue("portfolio_id", portfolioId)
      .addValue("portfolio_oid", portfolioOid)
      .addValue("parent_node_id", parentNodeId)
      .addValue("parent_node_oid", parentNodeOid)
      .addValue("depth", depth)
      .addValue("name", StringUtils.defaultString(node.getName()));
    argsList.add(treeArgs);
    
    // store position links
    Set<ObjectId> positionIds = new LinkedHashSet<ObjectId>(node.getPositionIds());
    node.getPositionIds().clear();
    node.getPositionIds().addAll(positionIds);
    for (ObjectId positionId : positionIds) {
      final DbMapSqlParameterSource posArgs = new DbMapSqlParameterSource()
        .addValue("node_id", nodeId)
        .addValue("key_scheme", positionId.getScheme())
        .addValue("key_value", positionId.getValue());
      posList.add(posArgs);
    }
    
    // store the left/right before/after the child loop and back fill into stored args row
    treeArgs.addValue("tree_left", counter.getAndIncrement());
    for (ManageablePortfolioNode childNode : node.getChildNodes()) {
      insertBuildArgs(portfolioUid, nodeUid, childNode, update, portfolioId, portfolioOid, nodeId, nodeOid, counter, depth + 1, argsList, posList);
    }
    treeArgs.addValue("tree_right", counter.getAndIncrement());
  }

  /**
   * Gets the SQL for inserting a portfolio.
   * 
   * @return the SQL, not null
   */
  protected String sqlInsertPortfolio() {
    return "INSERT INTO prt_portfolio " +
              "(id, oid, ver_from_instant, ver_to_instant, corr_from_instant, corr_to_instant, name) " +
            "VALUES " +
              "(:portfolio_id, :portfolio_oid, :ver_from_instant, :ver_to_instant, :corr_from_instant, :corr_to_instant, :name)";
  }

  /**
   * Gets the SQL for inserting a node.
   * 
   * @return the SQL, not null
   */
  protected String sqlInsertNode() {
    return "INSERT INTO prt_node " +
              "(id, oid, portfolio_id, portfolio_oid, parent_node_id, parent_node_oid, depth, tree_left, tree_right, name) " +
            "VALUES " +
              "(:node_id, :node_oid, :portfolio_id, :portfolio_oid, :parent_node_id, :parent_node_oid, :depth, :tree_left, :tree_right, :name) ";
  }

  /**
   * Gets the SQL for inserting a position.
   * 
   * @return the SQL, not null
   */
  protected String sqlInsertPosition() {
    return "INSERT INTO prt_position (node_id, key_scheme, key_value) " +
            "VALUES " +
            "(:node_id, :key_scheme, :key_value)";
  }
  
  /**
   * Gets the SQL for inserting position attributes.
   * 
   * @return the SQL, not null.
   */
  protected String sqlInsertPortfolioAttributes() {
    return "INSERT INTO prt_portfolio_attribute (id, portfolio_id, portfolio_oid, key, value) " +
           "VALUES (:attr_id, :portfolio_id, :portfolio_oid, :key, :value)";
  }

  //-------------------------------------------------------------------------
  @Override
  public ManageablePortfolioNode getNode(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    checkScheme(uniqueId);
    
    if (uniqueId.isVersioned()) {
      return getNodeById(uniqueId);
    } else {
      return getNodeByInstants(uniqueId, null, null);
    }
  }

  /**
   * Gets a node by searching for the latest version of an object identifier.
   * 
   * @param uniqueId  the unique identifier, not null
   * @param versionAsOf  the instant to fetch, not null
   * @param correctedTo  the instant to fetch, not null
   * @return the node, null if not found
   */
  protected ManageablePortfolioNode getNodeByInstants(final UniqueId uniqueId, final Instant versionAsOf, final Instant correctedTo) {
    s_logger.debug("getNodeByLatest {}", uniqueId);
    final Instant now = now();
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addValue("node_oid", extractOid(uniqueId))
      .addTimestamp("version_as_of_instant", Objects.firstNonNull(versionAsOf, now))
      .addTimestamp("corrected_to_instant", Objects.firstNonNull(correctedTo, now));
    final PortfolioDocumentExtractor extractor = new PortfolioDocumentExtractor(false);
    final NamedParameterJdbcOperations namedJdbc = getJdbcTemplate().getNamedParameterJdbcOperations();
    final List<PortfolioDocument> docs = namedJdbc.query(sqlSelectNodeByInstants(), args, extractor);
    if (docs.isEmpty()) {
      throw new DataNotFoundException("Node not found: " + uniqueId);
    }
    return docs.get(0).getPortfolio().getRootNode();  // SQL loads desired node in place of the root node
  }

  /**
   * Gets the SQL for getting a node by object id and instants.
   * 
   * @return the SQL, not null
   */
  protected String sqlSelectNodeByInstants() {
    return SELECT +
      ", n.parent_node_id AS parent_node_id " +
      ", n.parent_node_oid AS parent_node_oid " +
      FROM +
      ", (SELECT portfolio_id, tree_left, tree_right FROM prt_node WHERE oid = :node_oid) base " +
      "WHERE base.portfolio_id = main.id " +
        "AND (ver_from_instant <= :version_as_of_instant AND ver_to_instant > :version_as_of_instant) " +
        "AND (corr_from_instant <= :corrected_to_instant AND corr_to_instant > :corrected_to_instant) " +
        "AND n.tree_left BETWEEN base.tree_left AND base.tree_right " +
      sqlAdditionalWhere() +
      sqlAdditionalOrderBy(true);
  }

  /**
   * Gets a node by identifier.
   * 
   * @param uniqueId  the unique identifier, not null
   * @return the node, null if not found
   */
  protected ManageablePortfolioNode getNodeById(final UniqueId uniqueId) {
    s_logger.debug("getNodeById {}", uniqueId);
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addValue("node_id", extractRowId(uniqueId));
    final PortfolioDocumentExtractor extractor = new PortfolioDocumentExtractor(false);
    final NamedParameterJdbcOperations namedJdbc = getJdbcTemplate().getNamedParameterJdbcOperations();
    String sql = sqlSelectNodeById();
    final List<PortfolioDocument> docs = namedJdbc.query(sql, args, extractor);
    if (docs.isEmpty()) {
      throw new DataNotFoundException("Node not found: " + uniqueId);
    }
    return docs.get(0).getPortfolio().getRootNode();  // SQL loads desired node in place of the root node
  }

  /**
   * Gets the SQL for getting a node by unique row identifier.
   * 
   * @return the SQL, not null
   */
  protected String sqlSelectNodeById() {
    return SELECT +
      ", n.parent_node_id AS parent_node_id " +
      ", n.parent_node_oid AS parent_node_oid " +
      FROM +
      ", (SELECT portfolio_id, tree_left, tree_right FROM prt_node WHERE id = :node_id) base " +
      "WHERE base.portfolio_id = main.id " +
        "AND n.tree_left BETWEEN base.tree_left AND base.tree_right " +
      sqlAdditionalWhere() +
      sqlAdditionalOrderBy(true);
  }

  //-------------------------------------------------------------------------
  @Override
  protected String sqlSelectFrom() {
    return SELECT + FROM;
  }

  @Override
  protected String sqlAdditionalOrderBy(final boolean orderByPrefix) {
    //PLAT-1723 need to order sufficiently for stack based tree traversing to work
    return (orderByPrefix ? "ORDER BY " : ", ") + "main.id, n.tree_left, p.key_scheme, p.key_value ";
  }

  @Override
  protected String mainTableName() {
    return "prt_portfolio";
  }

  //-------------------------------------------------------------------------
  /**
   * Mapper from SQL rows to a PortfolioDocument.
   */
  protected final class PortfolioDocumentExtractor implements ResultSetExtractor<List<PortfolioDocument>> {
    private final boolean _complete;
    private long _lastPortfolioId = -1;
    private long _lastNodeId = -1;
    private ManageablePortfolio _portfolio;
    private ManageablePortfolioNode _node;
    private List<PortfolioDocument> _documents = new ArrayList<PortfolioDocument>();
    private final Stack<LongObjectPair<ManageablePortfolioNode>> _nodes = new Stack<LongObjectPair<ManageablePortfolioNode>>();

    public PortfolioDocumentExtractor(boolean complete) {
      _complete = complete;
    }

    @Override
    public List<PortfolioDocument> extractData(ResultSet rs) throws SQLException, DataAccessException {
      while (rs.next()) {
        final long portfolioId = rs.getLong("PORTFOLIO_ID");
        final long nodeId = rs.getLong("NODE_ID");
        if (_lastPortfolioId != portfolioId) {
          _lastPortfolioId = portfolioId;
          buildPortfolio(rs, portfolioId);
        }
        if (_lastNodeId != nodeId) {
          _lastNodeId = nodeId;
          buildNode(rs, nodeId);
        }
        final String posIdScheme = rs.getString("POS_KEY_SCHEME");
        final String posIdValue = rs.getString("POS_KEY_VALUE");
        if (posIdScheme != null && posIdValue != null) {
          UniqueId id = UniqueId.of(posIdScheme, posIdValue);
          if (!_node.getPositionIds().contains(id.getObjectId())) {
            _node.addPosition(id);
          }
        }
        
        final String prtAttrKey = rs.getString("PRT_ATTR_KEY");
        final String prtAttrValue = rs.getString("PRT_ATTR_VALUE");
        if (prtAttrKey != null && prtAttrValue != null) {
          _portfolio.addAttribute(prtAttrKey, prtAttrValue);
        }
      }
      return _documents;
    }

    private void buildPortfolio(final ResultSet rs, final long portfolioId) throws SQLException {
      final long portfolioOid = rs.getLong("PORTFOLIO_OID");
      final Timestamp versionFrom = rs.getTimestamp("VER_FROM_INSTANT");
      final Timestamp versionTo = rs.getTimestamp("VER_TO_INSTANT");
      final Timestamp correctionFrom = rs.getTimestamp("CORR_FROM_INSTANT");
      final Timestamp correctionTo = rs.getTimestamp("CORR_TO_INSTANT");
      final String name = StringUtils.defaultString(rs.getString("PORTFOLIO_NAME"));
      _portfolio = new ManageablePortfolio(name);
      _portfolio.setUniqueId(createUniqueId(portfolioOid, portfolioId));
      final PortfolioDocument doc = new PortfolioDocument(_portfolio);
      doc.setVersionFromInstant(DbDateUtils.fromSqlTimestamp(versionFrom));
      doc.setVersionToInstant(DbDateUtils.fromSqlTimestampNullFarFuture(versionTo));
      doc.setCorrectionFromInstant(DbDateUtils.fromSqlTimestamp(correctionFrom));
      doc.setCorrectionToInstant(DbDateUtils.fromSqlTimestampNullFarFuture(correctionTo));
      _documents.add(doc);
      _nodes.clear();
    }

    private void buildNode(final ResultSet rs, final long nodeId) throws SQLException {
      final long nodeOid = rs.getLong("NODE_OID");
      final long treeLeft = rs.getLong("TREE_LEFT");
      final long treeRight = rs.getLong("TREE_RIGHT");
      final String name = StringUtils.defaultString(rs.getString("NODE_NAME"));
      _node = new ManageablePortfolioNode(name);
      _node.setUniqueId(createUniqueId(nodeOid, nodeId));
      _node.setPortfolioId(_portfolio.getUniqueId());
      if (_nodes.size() == 0) {
        if (_complete == false) {
          final Long parentNodeId = (Long) rs.getObject("PARENT_NODE_ID");
          final Long parentNodeOid = (Long) rs.getObject("PARENT_NODE_OID");
          if (parentNodeId != null && parentNodeOid != null) {
            _node.setParentNodeId(createUniqueId(parentNodeOid, parentNodeId));
          }
        }
        _portfolio.setRootNode(_node);
      } else {
        while (treeLeft > _nodes.peek().first) {
          _nodes.pop();
        }
        final ManageablePortfolioNode parentNode = _nodes.peek().second;
        _node.setParentNodeId(parentNode.getUniqueId());
        parentNode.addChildNode(_node);
      }
      // add to stack
      _nodes.push(LongObjectPair.of(treeRight, _node));
    }
  }

}
