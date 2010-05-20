/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.db;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import javax.sql.DataSource;
import javax.time.Instant;
import javax.time.InstantProvider;
import javax.time.TimeSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import com.opengamma.engine.position.Portfolio;
import com.opengamma.engine.position.PortfolioImpl;
import com.opengamma.engine.position.PortfolioNode;
import com.opengamma.engine.position.PortfolioNodeImpl;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.position.PositionImpl;
import com.opengamma.engine.position.PositionMaster;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.LongObjectPair;

/**
 * A database backed position master.
 * <p>
 * The position master provides a uniform structural view over a set of positions
 * holding them in a tree structure portfolio.
 * This class provides database storage for the entire tree.
 */
public class DbPositionMaster implements PositionMaster, InitializingBean {

  /**
   * The scheme used for UniqueIdentifier objects.
   */
  public static final String IDENTIFIER_SCHEME_DEFAULT = "HibernatePositionMaster";
  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(DbPositionMaster.class);

  /**
   * The time-source to use.
   */
  private TimeSource _timeSource = TimeSource.system();
  /**
   * The scheme in use for UniqueIdentifier.
   */
  private String _identifierScheme = IDENTIFIER_SCHEME_DEFAULT;
  /**
   * The template for database operations.
   */
  private SimpleJdbcTemplate _jdbcTemplate;

  /**
   * Creates an instance.
   */
  public DbPositionMaster () {
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the time-source that determines the current time.
   * @return the time-source, not null
   */
  public TimeSource getTimeSource() {
    return _timeSource;
  }

  /**
   * Sets the scheme in use for UniqueIdentifier.
   * @param scheme  the scheme, not null
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
  public String getIdentifierScheme() {
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
  private void checkIdentifierScheme(final UniqueIdentifier uid) {
    if (uid.getScheme().equals(getIdentifierScheme()) == false) {
      s_logger.debug("invalid UniqueIdentifier scheme: {}", uid.getScheme());
      throw new IllegalArgumentException("Invalid identifier for HibernatePositionMaster: " + uid);
    }
  }

  /**
   * Creates a unique identifier.
   * @param portfolioOid  the portfolio object identifier
   * @param version  the version
   */
  private UniqueIdentifier createPortfolioUniqueIdentifier(long portfolioOid, long version) {
    return UniqueIdentifier.of(getIdentifierScheme(), Long.toString(portfolioOid), Long.toString(version));
  }

  /**
   * Creates a unique identifier.
   * @param portfolioOid  the portfolio object identifier
   * @param oid  the object identifier
   * @param version  the version
   */
  private UniqueIdentifier createUniqueIdentifier(long portfolioOid, long oid, long version) {
    String value = new StringBuilder().append(portfolioOid).append('-').append(oid).toString();
    return UniqueIdentifier.of(getIdentifierScheme(), value, Long.toString(version));
  }

  /**
   * Extracts a portfolio object identifier from a unique identifier.
   * @param uid  the unique identifier, not null
   * @return the portfolio object identifier, not null
   */
  private Long extractPortfolioOid(final UniqueIdentifier uid) {
    int pos = uid.getValue().indexOf('-');
    if (pos < 0) {
      return new Long(uid.getValue());
    }
    return new Long(uid.getValue().substring(0, pos));
  }

  /**
   * Extracts the non-portfolio object identifier from a unique identifier.
   * @param uid  the unique identifier, not null
   * @return the non-portfolio object identifier, not null
   */
  private Long extractOtherOid(final UniqueIdentifier uid) {
    int pos = uid.getValue().indexOf('-');
    if (pos < 0) {
      throw new IllegalArgumentException("Unique identifier is invalid: " + uid);
    }
    return new Long(uid.getValue().substring(pos + 1));
  }

  //-------------------------------------------------------------------------
  /**
   * Initializes the position master with a data-source.
   * @param dataSource  the data-source, not null
   */
  public void setSessionFactory(final DataSource dataSource) {
    ArgumentChecker.notNull(dataSource, "dataSource");
    _jdbcTemplate = new SimpleJdbcTemplate(dataSource);
  }

  /**
   * Validates that the data-source was provided by Spring.
   */
  @Override
  public void afterPropertiesSet() throws Exception {
    if (_jdbcTemplate == null) {
      throw new IllegalStateException("sessionFactory not set");
    }
  }

  /**
   * Gets the database template.
   * @return the template, non-null if correctly initialized
   */
  protected SimpleJdbcTemplate getTemplate() {
    return _jdbcTemplate;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets a portfolio by unique identifier.
   * @param uid  the unique identifier, not null
   * @return the portfolio, null if not found
   */
  @Override
  public Portfolio getPortfolio(UniqueIdentifier uid) {
    checkIdentifierScheme(uid);
    if (uid.isVersioned()) {
      return getPortfolio(extractPortfolioOid(uid), new Long(uid.getVersion()));
    }
    return getPortfolio(uid, Instant.now(_timeSource));
  }

  /**
   * Gets a portfolio by unique identifier at an instant.
   * Any version in the unique identifier is ignored.
   * @param uid  the unique identifier, not null
   * @param instantProvider  the instant to query at, not null
   * @return the portfolio, null if not found
   */
  public Portfolio getPortfolio(final UniqueIdentifier uid, final InstantProvider instantProvider) {
    checkIdentifierScheme(uid);
    Long portfolioOid = extractPortfolioOid(uid);
    Long version = getVersionByPortfolioOidInstant(portfolioOid, instantProvider);
    return getPortfolio(portfolioOid, version);
  }

  //-------------------------------------------------------------------------
  /**
   * Queries to find the applicable version at the given instant.
   * @param instant  the instant to query at, not null
   * @return the version number applicable at the instant, null if none
   */
  private Long getVersionByPortfolioOidInstant(Long portfolioOid, InstantProvider instantProvider) {
    Date instant = new Date(Instant.of(instantProvider).toEpochMillisLong());
    MapSqlParameterSource map = new MapSqlParameterSource()
      .addValue("portfolio_oid", portfolioOid)
      .addValue("instant", instant);
    return getTemplate().queryForLong(sqlVersionByPortfolioOidInstant(), map);
  }

  /**
   * Gets the SQL for getting the version by instant.
   * @return the SQL, not null
   */
  protected String sqlVersionByPortfolioOidInstant() {
    return "SELECT version " +
    		    "FROM pos_portfolio " +
    		    "WHERE pos_portfolio.oid = :portfolio_oid " +
      		    "AND start_instant >= :instant AND end_instant < :instant";
  }

  //-------------------------------------------------------------------------
  /**
   * Gets a portfolio by object identifier and version.
   * @param portfolioOid  the object identifier
   * @param version  the version
   * @return the portfolio, null if not found
   */
  private Portfolio getPortfolio(final Long portfolioOid, final Long version) {
    if (version == null) {
      return null;
    }
    MapSqlParameterSource args = new MapSqlParameterSource()
      .addValue("portfolio_oid", portfolioOid)
      .addValue("version", version);
    PortfolioNodeExtractor extractor = new PortfolioNodeExtractor(portfolioOid, version);
    PortfolioNodeImpl rootNode = (PortfolioNodeImpl) getTemplate().getNamedParameterJdbcOperations().query(sqlPortfolioByOidVersion(), args, extractor);
    UniqueIdentifier uid = createPortfolioUniqueIdentifier(portfolioOid, version);
    return new PortfolioImpl(uid, rootNode.getName(), rootNode);  // TODO: portfolio name
  }

  /**
   * Gets the SQL for getting the version by instant.
   * @return the SQL, not null
   */
  protected String sqlPortfolioByOidVersion() {
    return "SELECT pos_node.oid AS node_oid, left_id, right_id, pos_node.name AS node_name, " +
    		      "pos_position.oid AS position_oid, quantity, counterparty, trader, " +
    		      "pos_identifier.scheme AS position_id_scheme, pos_identifier.value AS position_id_value " +
            "FROM pos_nodetree LEFT JOIN pos_node ON pos_nodetree.node_oid = pos_node.oid " +
                              "LEFT JOIN pos_position ON pos_node.oid = pos_position.node_oid " +
                              "LEFT JOIN pos_identifier ON pos_position.oid = pos_identifier.position_oid " +
            "WHERE pos_node.portfolio_oid = :portfolio_oid " +
              "AND pos_nodetree.version >= :version AND pos_nodetree.version < :version " +
              "AND pos_node.version >= :version AND pos_node.version < :version " +
              "AND pos_position.version >= :version AND pos_position.version < :version " +
              "AND pos_identifier.version >= :version AND pos_identifier.version < :version " +
            "ORDER BY left_id";
  }

  //-------------------------------------------------------------------------
  /**
   * Gets a portfolio node by unique identifier.
   * @param uid  the unique identifier, not null
   * @return the node, null if not found
   */
  @Override
  public PortfolioNode getPortfolioNode(final UniqueIdentifier uid) {
    checkIdentifierScheme(uid);
    if (uid.isVersioned()) {
      return getPortfolioNode(extractPortfolioOid(uid), extractOtherOid(uid), new Long(uid.getVersion()));
    }
    return getPortfolioNode(uid, Instant.now(_timeSource));
  }

  /**
   * Gets a portfolio node by unique identifier at an instant.
   * @param uid  the unique identifier, not null
   * @param instantProvider  the instant to query at, not null
   * @return the node, null if not found
   */
  public PortfolioNode getPortfolioNode(final UniqueIdentifier uid, final InstantProvider instantProvider) {
    checkIdentifierScheme(uid);
    Long portfolioOid = extractPortfolioOid(uid);
    Long version = getVersionByPortfolioOidInstant(portfolioOid, instantProvider);
    return getPortfolioNode(portfolioOid, extractOtherOid(uid), version);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets a portfolio node by object identifier and version.
   * @param portfolioOid  the object identifier, not null
   * @param portfolioNodeOid  the object identifier, not null
   * @param version  the version, not null
   * @return the portfolio, null if not found
   */
  private PortfolioNode getPortfolioNode(final Long portfolioOid, final Long portfolioNodeOid, final Long version) {
    if (version == null) {
      return null;
    }
    MapSqlParameterSource args = new MapSqlParameterSource()
      .addValue("portfolio_oid", portfolioOid)
      .addValue("node_oid", portfolioNodeOid)
      .addValue("version", version);
    PortfolioNodeExtractor extractor = new PortfolioNodeExtractor(portfolioOid, version);
    return (PortfolioNode) getTemplate().getNamedParameterJdbcOperations().query(sqlPortfolioNodeByOidVersion(), args, extractor);
  }

  /**
   * Gets the SQL for getting the version by instant.
   * @return the SQL, not null
   */
  protected String sqlPortfolioNodeByOidVersion() {
    return "SELECT pos_node.oid AS node_oid, left_id, right_id, pos_node.name AS node_name, " +
              "pos_position.oid AS position_oid, quantity, counterparty, trader, " +
              "pos_identifier.scheme AS position_id_scheme, pos_identifier.value AS position_id_value " +
            "FROM pos_nodetree AS base, " +
              "pos_nodetree LEFT JOIN pos_node ON pos_nodetree.node_oid = pos_node.oid " +
                            "LEFT JOIN pos_position ON pos_node.oid = pos_position.node_oid " +
                            "LEFT JOIN pos_identifier ON pos_position.oid = pos_identifier.position_oid " +
            "WHERE pos_node.portfolio_oid = :portfolio_oid " +
              "AND base.node_oid = :node_oid " +
              "AND pos_nodetree.left_id BETWEEN base.left_id AND base.right_id " +
              "AND pos_nodetree.version >= :version AND pos_nodetree.version < :version " +
              "AND pos_node.version >= :version AND pos_node.version < :version " +
              "AND pos_position.version >= :version AND pos_position.version < :version " +
              "AND pos_identifier.version >= :version AND pos_identifier.version < :version " +
            "ORDER BY left_id";
  }

  //-------------------------------------------------------------------------
  /**
   * Mapper from SQL rows to a PortfolioNode.
   */
  private class PortfolioNodeExtractor implements ResultSetExtractor {
    private final long _portfolioOid;
    private final long _version;
    private final Stack<LongObjectPair<PortfolioNodeImpl>> _nodes = new Stack<LongObjectPair<PortfolioNodeImpl>>();
    private PositionImpl _position = null;
    private long _lastNodeOid = -1;
    private long _lastPositionOid = -1;

    private PortfolioNodeExtractor(long portfolioOid, long version) {
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
          String idScheme = rs.getString("POSITION_ID_SCHEME");
          String idValue = rs.getString("POSITION_ID_VALUE");
          if (idScheme != null && idValue != null) {
            Identifier id = Identifier.of(idScheme, idValue);
            _position.setSecurityKey(_position.getSecurityKey().withIdentifier(id));
          }
        }
      }
      return _nodes.get(0).getSecond();
    }

    private void buildTreeNodes(ResultSet rs, long nodeOid) throws SQLException {
      _lastNodeOid = nodeOid;
      long leftId = rs.getLong("LEFT_ID");
      long rightId = rs.getLong("RIGHT_ID");
      UniqueIdentifier uid = createUniqueIdentifier(_portfolioOid, nodeOid, _version);
      String name = rs.getString("NAME");
      PortfolioNodeImpl node = new PortfolioNodeImpl(uid, name);
      // find and add to parent unless this is the root
      if (_nodes.size() > 0) {
        PortfolioNodeImpl parent = _nodes.peek().getSecond();
        while (leftId > _nodes.peek().getFirstLong()) {
          _nodes.pop();
        }
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
   * Stores a portfolio.
   * @param instantProvider  the instant to store at, not null
   * @param portfolio  the portfolio to store, not null
   */
  public void putPortfolio(final Portfolio portfolio, final InstantProvider instantProvider) {
    // TODO
  }

//    getTemplate().execute(new HibernateCallback() {
//      private <T extends DateIdentifiableBean> T identifiedBean(final UniqueIdentifiable identifiable, final T bean) {
//        final UniqueIdentifier uniqueIdentifier = identifiable.getUniqueIdentifier();
//        if (uniqueIdentifier != null) {
//          if (getIdentifierScheme().equals(uniqueIdentifier.getScheme())) {
//            bean.setIdentifier(uniqueIdentifier.getValue());
//          }
//        }
//        return bean;
//      }
//      
//      private PortfolioNodeBean portfolioNodeToBean(final PositionMasterSession positionMasterSession,
//          final PortfolioNodeBean ancestor, final PortfolioNode portfolioNode) {
//        final PortfolioNodeBean portfolioNodeBean = identifiedBean(portfolioNode, new PortfolioNodeBean());
//        portfolioNodeBean.setAncestor(ancestor);
//        portfolioNodeBean.setName(portfolioNode.getName());
//        for (PortfolioNode child : portfolioNode.getChildNodes()) {
//          portfolioNodeToBean(positionMasterSession, portfolioNodeBean, child);
//        }
//        for (Position position : portfolioNode.getPositions()) {
//          final PositionBean positionBean = identifiedBean(position, new PositionBean());
//          positionBean.setQuantity(position.getQuantity());
//          for (Identifier securityIdentifier : position.getSecurityKey()) {
//            IdentifierAssociationBean securityAssociation = identifiedBean(position, new IdentifierAssociationBean());
//            securityAssociation.setDomainSpecificIdentifier(securityIdentifier);
//            securityAssociation.setPosition(positionBean);
//            positionMasterSession.saveIdentifierAssociationBean(securityAssociation);
//          }
//          positionMasterSession.addPositionToPortfolioNode(positionBean, portfolioNodeBean);
//        }
//        return portfolioNodeBean;
//      }
//      
//      @Override
//      public Object doInHibernate(final Session session) throws HibernateException, SQLException {
//        final PositionMasterSession positionMasterSession = new PositionMasterSession(session);
//        s_logger.info("write portfolio {}", portfolio);
//        final PortfolioBean portfolioBean = identifiedBean(portfolio, new PortfolioBean());
//        portfolioBean.setName(portfolio.getName());
//        portfolioBean.setRoot(portfolioNodeToBean(positionMasterSession, null, portfolio.getRootNode()));
//        positionMasterSession.savePortfolioBean(portfolioBean);
//        return null;
//      }
//    });
//  }
//
  //-------------------------------------------------------------------------
  /**
   * Gets a position by unique identifier.
   * @param uid  the unique identifier, not null
   * @return the position, null if not found
   */
  @Override
  public Position getPosition(final UniqueIdentifier uid) {
    checkIdentifierScheme(uid);
    if (uid.isVersioned()) {
      return getPosition(extractPortfolioOid(uid), extractOtherOid(uid), new Long(uid.getVersion()));
    }
    return getPosition(uid, Instant.now(_timeSource));
  }

  /**
   * Gets a position by unique identifier at an instant.
   * @param uid  the unique identifier, not null
   * @param instantProvider  the instant to query at, not null
   * @return the position, null if not found
   */
  public Position getPosition(final UniqueIdentifier uid, final InstantProvider instantProvider) {
    checkIdentifierScheme(uid);
    Long portfolioOid = extractPortfolioOid(uid);
    Long version = getVersionByPortfolioOidInstant(portfolioOid, instantProvider);
    return getPosition(portfolioOid, extractOtherOid(uid), version);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets a position by object identifier and version.
   * @param portfolioOid  the object identifier, not null
   * @param positionOid  the object identifier, not null
   * @param version  the version, not null
   * @return the position, null if not found
   */
  private Position getPosition(final Long portfolioOid, final Long positionOid, final Long version) {
    if (version == null) {
      return null;
    }
    MapSqlParameterSource args = new MapSqlParameterSource()
      .addValue("position_oid", positionOid)
      .addValue("version", version);
    PositionExtractor extractor = new PositionExtractor(portfolioOid, version);
    return (Position) getTemplate().getNamedParameterJdbcOperations().query(sqlPositionByOidVersion(), args, extractor);
  }

  /**
   * Gets the SQL for getting the version by instant.
   * @return the SQL, not null
   */
  protected String sqlPositionByOidVersion() {
    return "SELECT pos_position.oid AS position_oid, quantity, counterparty, trader, " +
              "pos_identifier.scheme AS position_id_scheme, pos_identifier.value AS position_id_value " +
            "FROM pos_position LEFT JOIN pos_identifier ON pos_position.oid = pos_identifier.position_oid " +
            "WHERE pos_position.version >= :version AND pos_position.version < :version " +
              "AND pos_identifier.version >= :version AND pos_identifier.version < :version";
  }

  //-------------------------------------------------------------------------
  /**
   * Mapper from SQL rows to a PortfolioNode.
   */
  private class PositionExtractor implements ResultSetExtractor {
    private final long _portfolioOid;
    private final long _version;
    private PositionImpl _position = null;

    private PositionExtractor(long portfolioOid, long version) {
      _portfolioOid = portfolioOid;
      _version = version;
    }

    @Override
    public Object extractData(ResultSet rs) throws SQLException, DataAccessException {
      while (rs.next()) {
        if (_position == null) {
          buildPosition(rs);
        }
        String idScheme = rs.getString("POSITION_ID_SCHEME");
        String idValue = rs.getString("POSITION_ID_VALUE");
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
    return getPortfolioIds(Instant.now(_timeSource));
  }

  /**
   * Gets the complete set of portfolio unique identifiers.
   * @param instantProvider  the instant to query at, not null
   * @return the set of unique identifiers, not null
   */
  public Set<UniqueIdentifier> getPortfolioIds(final InstantProvider instantProvider) {
    Date instant = new Date(Instant.of(instantProvider).toEpochMillisLong());
    MapSqlParameterSource args = new MapSqlParameterSource()
      .addValue("instant", instant);
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
            "WHERE start_instant >= :instant AND end_instant < :instant";
  }

  //-------------------------------------------------------------------------
  /**
   * Maps SQL results to UniqueIdentifier.
   */
  class UniqueIdentifierMapper implements ParameterizedRowMapper<UniqueIdentifier> {
    @Override
    public UniqueIdentifier mapRow(ResultSet rs, int rowNum) throws SQLException {
      long portfolioOid = rs.getLong("OID");
      long version = rs.getLong("VERSION");
      return createPortfolioUniqueIdentifier(portfolioOid, version);
    }
  }

}
