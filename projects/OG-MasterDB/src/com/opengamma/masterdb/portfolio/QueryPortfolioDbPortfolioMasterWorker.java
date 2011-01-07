/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.portfolio;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.time.Instant;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import com.google.common.base.Objects;
import com.opengamma.DataNotFoundException;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.AbstractDocumentsResult;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioHistoryRequest;
import com.opengamma.master.portfolio.PortfolioHistoryResult;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;
import com.opengamma.util.db.DbDateUtils;
import com.opengamma.util.db.DbMapSqlParameterSource;
import com.opengamma.util.db.Paging;
import com.opengamma.util.db.PagingRequest;
import com.opengamma.util.tuple.LongObjectPair;

/**
 * Portfolio master worker to get the portfolio.
 */
public class QueryPortfolioDbPortfolioMasterWorker extends DbPortfolioMasterWorker {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(QueryPortfolioDbPortfolioMasterWorker.class);
  /**
   * SQL select.
   */
  protected static final String SELECT =
      "SELECT " +
        "f.id AS portfolio_id, " +
        "f.oid AS portfolio_oid, " +
        "f.ver_from_instant AS ver_from_instant, " +
        "f.ver_to_instant AS ver_to_instant, " +
        "f.corr_from_instant AS corr_from_instant, " +
        "f.corr_to_instant AS corr_to_instant, " +
        "f.name AS portfolio_name, " +
        "n.id AS node_id, " +
        "n.oid AS node_oid, " +
        "n.tree_left AS tree_left, " +
        "n.tree_right AS tree_right, " +
        "n.name AS node_name, " +
        "p.key_scheme AS pos_key_scheme, " +
        "p.key_value AS pos_key_value ";
  /**
   * SQL from.
   */
  protected static final String FROM =
      "FROM prt_portfolio f " +
        "LEFT JOIN prt_node n ON (n.portfolio_id = f.id) " +
        "LEFT JOIN prt_position p ON (p.node_id = n.id) ";
  /**
   * SQL order by.
   */
  protected static final String ORDER_BY =
      "ORDER BY f.oid, n.tree_left, p.key_scheme, p.key_value ";

  /**
   * Creates an instance.
   */
  public QueryPortfolioDbPortfolioMasterWorker() {
    super();
  }

  //-------------------------------------------------------------------------
  @Override
  protected PortfolioDocument get(final UniqueIdentifier uid) {
    if (uid.isVersioned()) {
      return getPortfolioById(uid);
    } else {
      return getPortfolioByLatest(uid);
    }
  }

  /**
   * Gets a portfolio by searching for the latest version of an object identifier.
   * @param uid  the unique identifier
   * @return the portfolio document, null if not found
   */
  protected PortfolioDocument getPortfolioByLatest(final UniqueIdentifier uid) {
    s_logger.debug("getPortfolioByLatest: {}", uid);
    final Instant now = Instant.now(getTimeSource());
    return getPortfolioByOidInstants(uid, now, now);
  }

  /**
   * Gets a portfolio by object identifier at instants.
   * @param oid  the portfolio oid, not null
   * @param versionAsOf  the version instant, not null
   * @param correctedTo  the corrected to instant, not null
   * @return the portfolio document, not null
   */
  protected PortfolioDocument getPortfolioByOidInstants(final UniqueIdentifier oid, final Instant versionAsOf, final Instant correctedTo) {
    s_logger.debug("getPortfolioByOidInstants {}", oid);
    final long portfolioOid = extractOid(oid);
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addValue("portfolio_oid", portfolioOid)
      .addTimestamp("version_as_of", versionAsOf)
      .addTimestamp("corrected_to", correctedTo);
    final PortfolioDocumentExtractor extractor = new PortfolioDocumentExtractor(true);
    final NamedParameterJdbcOperations namedJdbc = getJdbcTemplate().getNamedParameterJdbcOperations();
    final List<PortfolioDocument> docs = namedJdbc.query(sqlSelectPortfolioByOidInstants(), args, extractor);
    if (docs.isEmpty()) {
      throw new DataNotFoundException("Portfolio not found: " + oid);
    }
    return docs.get(0);
  }

  /**
   * Gets the SQL for getting a portfolio by object identifier and instants.
   * @return the SQL, not null
   */
  protected String sqlSelectPortfolioByOidInstants() {
    return SELECT + FROM +
      "WHERE f.oid = :portfolio_oid " +
        "AND f.ver_from_instant <= :version_as_of AND f.ver_to_instant > :version_as_of " +
        "AND f.corr_from_instant <= :corrected_to AND f.corr_to_instant > :corrected_to " +
      ORDER_BY;
  }

  /**
   * Gets a portfolio by identifier.
   * @param uid  the unique identifier
   * @return the portfolio document, null if not found
   */
  protected PortfolioDocument getPortfolioById(final UniqueIdentifier uid) {
    s_logger.debug("getPortfolioById {}", uid);
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addValue("portfolio_id", extractRowId(uid));
    final PortfolioDocumentExtractor extractor = new PortfolioDocumentExtractor(true);
    final NamedParameterJdbcOperations namedJdbc = getJdbcTemplate().getNamedParameterJdbcOperations();
    final List<PortfolioDocument> docs = namedJdbc.query(sqlSelectPortfolioById(), args, extractor);
    if (docs.isEmpty()) {
      throw new DataNotFoundException("Portfolio not found: " + uid);
    }
    return docs.get(0);
  }

  /**
   * Gets the SQL for getting a portfolio by unique row identifier.
   * @return the SQL, not null
   */
  protected String sqlSelectPortfolioById() {
    return SELECT + FROM +
      "WHERE f.id = :portfolio_id " +
      ORDER_BY;
  }

  //-------------------------------------------------------------------------
  @Override
  protected PortfolioSearchResult search(PortfolioSearchRequest request) {
    s_logger.debug("searchPortfolios: {}", request);
    final PortfolioSearchResult result = new PortfolioSearchResult();
    if ((request.getPortfolioIds() != null && request.getPortfolioIds().size() == 0) ||
        (request.getNodeIds() != null && request.getNodeIds().size() == 0)) {
      return result;
    }
    final Instant now = Instant.now(getTimeSource());
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addTimestamp("version_as_of_instant", Objects.firstNonNull(request.getVersionAsOfInstant(), now))
      .addTimestamp("corrected_to_instant", Objects.firstNonNull(request.getCorrectedToInstant(), now))
      .addValue("name", getDbHelper().sqlWildcardAdjustValue(request.getName()))
      .addValue("depth", request.getDepth());
    searchWithPaging(request.getPagingRequest(), sqlSearch(request), args, new PortfolioDocumentExtractor(true), result);
    return result;
  }

  /**
   * Gets the SQL to search for portfolios.
   * @param request  the request, not null
   * @return the SQL search and count, not null
   */
  protected String[] sqlSearch(final PortfolioSearchRequest request) {
    String where = "WHERE (ver_from_instant <= :version_as_of_instant AND ver_to_instant > :version_as_of_instant) " +
                "AND (corr_from_instant <= :corrected_to_instant AND corr_to_instant > :corrected_to_instant) ";
    if (request.getName() != null) {
      where += getDbHelper().sqlWildcardQuery("AND UPPER(name) ", "UPPER(:name)", request.getName());
    }
    if (request.getPortfolioIds() != null) {
      StringBuilder buf = new StringBuilder(request.getPortfolioIds().size() * 10);
      for (UniqueIdentifier uid : request.getPortfolioIds()) {
        getMaster().checkScheme(uid);
        buf.append(extractOid(uid)).append(", ");
      }
      buf.setLength(buf.length() - 2);
      where += "AND oid IN (" + buf + ") ";
    }
    if (request.getNodeIds() != null) {
      StringBuilder buf = new StringBuilder(request.getNodeIds().size() * 10);
      for (UniqueIdentifier uid : request.getNodeIds()) {
        getMaster().checkScheme(uid);
        buf.append(extractOid(uid)).append(", ");
      }
      buf.setLength(buf.length() - 2);
      where += "AND oid IN (SELECT DISTINCT portfolio_oid FROM prt_node WHERE oid IN (" + buf + ")) ";
    }
    
    String selectFromWhereInner = "SELECT id FROM prt_portfolio " + where;
    String inner = getDbHelper().sqlApplyPaging(selectFromWhereInner, "ORDER BY oid ", request.getPagingRequest());
    String search = SELECT + FROM + "WHERE f.id IN (" + inner + ") ";
    if (request.getDepth() >= 0) {
      search += "AND n.depth <= :depth ";
    }
    search += ORDER_BY;
    String count = "SELECT COUNT(*) FROM prt_portfolio " + where;
    return new String[] {search, count};
  }

  //-------------------------------------------------------------------------
  @Override
  protected PortfolioHistoryResult history(final PortfolioHistoryRequest request) {
    s_logger.debug("searchPortfolioHistoric: {}", request);
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addValue("portfolio_oid", extractOid(request.getObjectId()))
      .addTimestampNullIgnored("versions_from_instant", request.getVersionsFromInstant())
      .addTimestampNullIgnored("versions_to_instant", request.getVersionsToInstant())
      .addTimestampNullIgnored("corrections_from_instant", request.getCorrectionsFromInstant())
      .addTimestampNullIgnored("corrections_to_instant", request.getCorrectionsToInstant())
      .addValue("depth", request.getDepth());
    final PortfolioHistoryResult result = new PortfolioHistoryResult();
    searchWithPaging(request.getPagingRequest(), sqlHistory(request), args, new PortfolioDocumentExtractor(true), result);
    return result;
  }

  /**
   * Gets the SQL for searching the history of a portfolio.
   * @param request  the request, not null
   * @return the SQL search and count, not null
   */
  protected String[] sqlHistory(final PortfolioHistoryRequest request) {
    String where = "WHERE oid = :portfolio_oid ";
    if (request.getVersionsFromInstant() != null && request.getVersionsFromInstant().equals(request.getVersionsToInstant())) {
      where += "AND (ver_from_instant <= :versions_from_instant AND ver_to_instant > :versions_from_instant) ";
    } else {
      if (request.getVersionsFromInstant() != null) {
        where += "AND ((ver_from_instant <= :versions_from_instant AND ver_to_instant > :versions_from_instant) " +
                            "OR ver_from_instant >= :versions_from_instant) ";
      }
      if (request.getVersionsToInstant() != null) {
        where += "AND ((ver_from_instant <= :versions_to_instant AND ver_to_instant > :versions_to_instant) " +
                            "OR ver_to_instant < :versions_to_instant) ";
      }
    }
    if (request.getCorrectionsFromInstant() != null && request.getCorrectionsFromInstant().equals(request.getCorrectionsToInstant())) {
      where += "AND (corr_from_instant <= :corrections_from_instant AND corr_to_instant > :corrections_from_instant) ";
    } else {
      if (request.getCorrectionsFromInstant() != null) {
        where += "AND ((corr_from_instant <= :corrections_from_instant AND corr_to_instant > :corrections_from_instant) " +
                            "OR corr_from_instant >= :corrections_from_instant) ";
      }
      if (request.getCorrectionsToInstant() != null) {
        where += "AND ((corr_from_instant <= :corrections_to_instant AND ver_to_instant > :corrections_to_instant) " +
                            "OR corr_to_instant < :corrections_to_instant) ";
      }
    }
    String selectFromWhereInner = "SELECT id FROM prt_portfolio " + where;
    String inner = getDbHelper().sqlApplyPaging(selectFromWhereInner, "ORDER BY ver_from_instant DESC, corr_from_instant DESC ", request.getPagingRequest());
    String search = SELECT + FROM + "WHERE f.id IN (" + inner + ") ";
    if (request.getDepth() >= 0) {
      search += "AND n.depth <= :depth ";
    }
    search += "ORDER BY f.ver_from_instant DESC, f.corr_from_instant DESC, n.tree_left, p.key_scheme, p.key_value";
    String count = "SELECT COUNT(*) FROM prt_portfolio " + where;
    return new String[] {search, count};
  }

  /**
   * Searches for documents with paging.
   * 
   * @param pagingRequest  the paging request, not null
   * @param sql  the array of SQL, query and count, not null
   * @param args  the query arguments, not null
   * @param extractor  the extractor of results, not null
   * @param result  the object to populate, not null
   */
  protected void searchWithPaging(
      final PagingRequest pagingRequest, final String[] sql, final DbMapSqlParameterSource args,
      final ResultSetExtractor<List<PortfolioDocument>> extractor, final AbstractDocumentsResult<PortfolioDocument> result) {
    final NamedParameterJdbcOperations namedJdbc = getJdbcTemplate().getNamedParameterJdbcOperations();
    if (pagingRequest.equals(PagingRequest.ALL)) {
      result.getDocuments().addAll(namedJdbc.query(sql[0], args, extractor));
      result.setPaging(Paging.of(result.getDocuments(), pagingRequest));
    } else {
      final int count = namedJdbc.queryForInt(sql[1], args);
      result.setPaging(new Paging(pagingRequest, count));
      if (count > 0) {
        result.getDocuments().addAll(namedJdbc.query(sql[0], args, extractor));
      }
    }
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
          UniqueIdentifier id = UniqueIdentifier.of(posIdScheme, posIdValue);
          _node.addPosition(id);
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
      _portfolio.setUniqueId(createUniqueIdentifier(portfolioOid, portfolioId));
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
      _node.setUniqueId(createUniqueIdentifier(nodeOid, nodeId));
      _node.setPortfolioId(_portfolio.getUniqueId());
      if (_nodes.size() == 0) {
        if (_complete == false) {
          final Long parentNodeId = (Long) rs.getObject("PARENT_NODE_ID");
          final Long parentNodeOid = (Long) rs.getObject("PARENT_NODE_OID");
          if (parentNodeId != null && parentNodeOid != null) {
            _node.setParentNodeId(createUniqueIdentifier(parentNodeOid, parentNodeId));
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

  //-------------------------------------------------------------------------
  @Override
  protected ManageablePortfolioNode getNode(final UniqueIdentifier uid) {
    if (uid.isVersioned()) {
      return getNodeById(uid);
    } else {
      return getNodeByInstants(uid, null, null);
    }
  }

  /**
   * Gets a node by searching for the latest version of an object identifier.
   * @param uid  the unique identifier
   * @param versionAsOf  the instant to fetch, not null
   * @param correctedTo  the instant to fetch, not null
   * @return the node, null if not found
   */
  protected ManageablePortfolioNode getNodeByInstants(final UniqueIdentifier uid, final Instant versionAsOf, final Instant correctedTo) {
    s_logger.debug("getNodeByLatest: {}", uid);
    final Instant now = Instant.now(getTimeSource());
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addValue("node_oid", extractOid(uid))
      .addTimestamp("version_as_of_instant", Objects.firstNonNull(versionAsOf, now))
      .addTimestamp("corrected_to_instant", Objects.firstNonNull(correctedTo, now));
    final PortfolioDocumentExtractor extractor = new PortfolioDocumentExtractor(false);
    final NamedParameterJdbcOperations namedJdbc = getJdbcTemplate().getNamedParameterJdbcOperations();
    final List<PortfolioDocument> docs = namedJdbc.query(sqlSelectNodeByInstants(), args, extractor);
    if (docs.isEmpty()) {
      throw new DataNotFoundException("Node not found: " + uid);
    }
    return docs.get(0).getPortfolio().getRootNode();  // SQL loads desired node in place of the root node
  }

  /**
   * Gets the SQL for getting a node by object id and instants.
   * @return the SQL, not null
   */
  protected String sqlSelectNodeByInstants() {
    return SELECT +
      ", n.parent_node_id AS parent_node_id " +
      ", n.parent_node_oid AS parent_node_oid " +
      FROM +
      ", (SELECT portfolio_id, tree_left, tree_right FROM prt_node WHERE oid = :node_oid) base " +
      "WHERE f.id = base.portfolio_id " +
        "AND (ver_from_instant <= :version_as_of_instant AND ver_to_instant > :version_as_of_instant) " +
        "AND (corr_from_instant <= :corrected_to_instant AND corr_to_instant > :corrected_to_instant) " +
        "AND n.tree_left BETWEEN base.tree_left AND base.tree_right ";
  }

  /**
   * Gets a node by identifier.
   * @param uid  the unique identifier
   * @return the node, null if not found
   */
  protected ManageablePortfolioNode getNodeById(final UniqueIdentifier uid) {
    s_logger.debug("getNodeById {}", uid);
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addValue("node_id", extractRowId(uid));
    final PortfolioDocumentExtractor extractor = new PortfolioDocumentExtractor(false);
    final NamedParameterJdbcOperations namedJdbc = getJdbcTemplate().getNamedParameterJdbcOperations();
    final List<PortfolioDocument> docs = namedJdbc.query(sqlSelectNodeById(), args, extractor);
    if (docs.isEmpty()) {
      throw new DataNotFoundException("Node not found: " + uid);
    }
    return docs.get(0).getPortfolio().getRootNode();  // SQL loads desired node in place of the root node
  }

  /**
   * Gets the SQL for getting a node by unique row identifier.
   * @return the SQL, not null
   */
  protected String sqlSelectNodeById() {
    return SELECT +
      ", n.parent_node_id AS parent_node_id " +
      ", n.parent_node_oid AS parent_node_oid " +
      FROM +
      ", (SELECT portfolio_id, tree_left, tree_right FROM prt_node WHERE id = :node_id) base " +
      "WHERE f.id = base.portfolio_id " +
        "AND n.tree_left BETWEEN base.tree_left AND base.tree_right ";
  }

}
