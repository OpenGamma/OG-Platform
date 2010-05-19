/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.db;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.time.InstantProvider;
import javax.time.TimeSource;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.opengamma.engine.position.Portfolio;
import com.opengamma.engine.position.PortfolioImpl;
import com.opengamma.engine.position.PortfolioNode;
import com.opengamma.engine.position.PortfolioNodeImpl;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.position.PositionMaster;
import com.opengamma.engine.security.Security;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;

/**
 * A Hibernate database backed implementation of a PositionMaster. 
 */
public class HibernatePositionMaster implements PositionMaster, InitializingBean {

  /**
   * The scheme used for UniqueIdentifier objects.
   */
  public static final String IDENTIFIER_SCHEME_DEFAULT = "HibernatePositionMaster";
  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(HibernatePositionMaster.class);

  /**
   * The Hibernate template.
   */
  private HibernateTemplate _hibernateTemplate;
  /**
   * The scheme in use for UniqueIdentifier.
   */
  private String _identifierScheme = IDENTIFIER_SCHEME_DEFAULT;

  /**
   * Creates an instance.
   */
  public HibernatePositionMaster () {
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

  //-------------------------------------------------------------------------
  /**
   * Sets the Hibernate session factory.
   * @param sessionFactory  the factory, not null
   */
  public void setSessionFactory(final SessionFactory sessionFactory) {
    ArgumentChecker.notNull(sessionFactory, "sessionFactory");
    _hibernateTemplate = new HibernateTemplate(sessionFactory);
  }

  /**
   * Validates that the session factory was provided by Spring.
   */
  @Override
  public void afterPropertiesSet() throws Exception {
    if (getHibernateTemplate() == null) {
      throw new IllegalStateException("sessionFactory not set");
    }
  }

  /**
   * Gets the Hibernate template.
   * @return the template, non-null if correctly initialized
   */
  protected HibernateTemplate getHibernateTemplate() {
    return _hibernateTemplate;
  }

  //-------------------------------------------------------------------------
  /**
   * Hibernate position implementation.
   */
  private static class PositionImpl implements Position, Serializable {
    private final UniqueIdentifier _uid;
    private final BigDecimal _quantity;
    private final IdentifierBundle _securityKey;

    private PositionImpl(
        final UniqueIdentifier uid, final BigDecimal quantity, final IdentifierBundle securityKey) {
      _uid = uid;
      _quantity = quantity;
      _securityKey = securityKey;
    }
    @Override
    public BigDecimal getQuantity() {
      return _quantity;
    }
    @Override
    public Security getSecurity() {
      return null;
    }
    @Override
    public IdentifierBundle getSecurityKey() {
      return _securityKey;
    }
    @Override
    public UniqueIdentifier getUniqueIdentifier() {
      return _uid;
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Gets a portfolio node by unique identifier.
   * @param uid  the unique identifier, not null
   * @return the node, null if not found
   */
  @Override
  public PortfolioNode getPortfolioNode(final UniqueIdentifier uid) {
    return getPortfolioNode(TimeSource.system().instant(), uid);
  }

  /**
   * Gets a portfolio node by unique identifier at an instant.
   * @param instant  the instant to query at, not null
   * @param uid  the unique identifier, not null
   * @return the node, null if not found
   */
  public PortfolioNode getPortfolioNode(final InstantProvider instant, final UniqueIdentifier uid) {
    if (uid.getScheme().equals(getIdentifierScheme()) == false) {
      s_logger.debug("invalid UniqueIdentifier scheme: {}", uid.getScheme());
      throw new IllegalArgumentException("Invalid identifier for HibernatePositionMaster: " + uid);
    }
    return (PortfolioNode) getHibernateTemplate().execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(final Session session) throws HibernateException, SQLException {
        final PositionMasterSession positionMasterSession = new PositionMasterSession(session);
        s_logger.info("retrieve node {}", uid.getValue());
        final PortfolioNodeBean dbNode = positionMasterSession.getPortfolioNodeBeanByIdentifier(instant, uid.getValue());
        if (dbNode == null) {
          s_logger.debug("bean not found for {} at {}", uid, instant);
          return null;
        }
        return loadPortfolioNodeTree(positionMasterSession, instant, dbNode);
      }
    });
  }

  /**
   * Loads a portfolio node.
   * @param session  the session to use, not null
   * @param instant  the instant to query at, not null
   * @param dbNode  the node to populate, not null
   * @return the populated tree, not null
   */
  private PortfolioNodeImpl loadPortfolioNodeTree(
      final PositionMasterSession session, final InstantProvider instant, final PortfolioNodeBean dbNode) {
    // TODO this is a slow way of constructing the Node graph - there are a number of recursive queries
    // One of the bulk fetches could be used and the graph built up from the information in each node
    UniqueIdentifier uid = UniqueIdentifier.of(getIdentifierScheme(), dbNode.getIdentifier());
    String name = dbNode.getName();
    final PortfolioNodeImpl node = new PortfolioNodeImpl(uid, name);
    for (PortfolioNodeBean child : session.getPortfolioNodeBeanByImmediateAncestor(instant, dbNode)) {
      node.addChildNode(loadPortfolioNodeTree(session, instant, child));
    }
    for (final PositionBean position : session.getPositionBeanByImmediatePortfolioNode(instant, dbNode)) {
      node.addPosition(loadPosition(session, instant, position));
    }
    return node;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets a position by unique identifier.
   * @param instant  the instant to query at, not null
   * @param uid  the unique identifier, not null
   * @return the node, null if not found
   */
  @Override
  public Position getPosition(final UniqueIdentifier uid) {
    return getPosition(TimeSource.system().instant(), uid);
  }

  /**
   * Gets a position by unique identifier at an instant.
   * @param instant  the instant to query at, not null
   * @param uid  the unique identifier, not null
   * @return the node, null if not found
   */
  public Position getPosition(final InstantProvider instant, final UniqueIdentifier uid) {
    if (uid.getScheme().equals(getIdentifierScheme()) == false) {
      s_logger.debug("rejecting invalid identity key domain '{}'", uid.getScheme());
      throw new IllegalArgumentException("Invalid identifier for HibernatePositionMaster: " + uid);
    }
    return (Position) getHibernateTemplate().execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(final Session session) throws HibernateException, SQLException {
        final PositionMasterSession positionMasterSession = new PositionMasterSession(session);
        s_logger.info("retrieve position {}", uid.getValue());
        final PositionBean dbPosition = positionMasterSession.getPositionBeanByIdentifier(instant, uid.getValue());
        if (dbPosition == null) {
          s_logger.debug("bean not found for {} at {}", uid, instant);
          return null;
        }
        return loadPosition(positionMasterSession, instant, dbPosition);
      }
    });
  }

  /**
   * Loads full details of a position.
   * @param session  the session to use, not null
   * @param instant  the instant to query at, not null
   * @param dbPosition  the position to populate, not null
   * @return the populated position, not null
   */
  private Position loadPosition(final PositionMasterSession session, final InstantProvider instant, final PositionBean dbPosition) {
    final Collection<IdentifierAssociationBean> assocBeans = session.getIdentifierAssociationBeanByPosition(instant, dbPosition);
    final Collection<Identifier> dsids = new ArrayList<Identifier>(assocBeans.size());
    for (IdentifierAssociationBean assocBean : assocBeans) {
      dsids.add(assocBean.getDomainSpecificIdentifier());
    }
    UniqueIdentifier uid = UniqueIdentifier.of(getIdentifierScheme(), dbPosition.getIdentifier());
    return new PositionImpl(uid, dbPosition.getQuantity(), new IdentifierBundle(dsids));
  }

  //-------------------------------------------------------------------------
  /**
   * Gets a portfolio by unique identifier.
   * @param instant  the instant to query at, not null
   * @param uid  the unique identifier, not null
   * @return the portfolio, null if not found
   */
  @Override
  public Portfolio getPortfolio(UniqueIdentifier uid) {
    return getPortfolio(TimeSource.system().instant(), uid);
  }

  /**
   * Gets a portfolio by unique identifier at an instant.
   * @param instant  the instant to query at, not null
   * @param uid  the unique identifier, not null
   * @return the portfolio, null if not found
   */
  public Portfolio getPortfolio(final InstantProvider instant, final UniqueIdentifier uid) {
    if (uid.getScheme().equals(getIdentifierScheme()) == false) {
      throw new IllegalArgumentException("Invalid identifier for HibernatePositionMaster: " + uid);
    }
    return (Portfolio) getHibernateTemplate().execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(final Session session) throws HibernateException, SQLException {
        final PositionMasterSession positionMasterSession = new PositionMasterSession(session);
        s_logger.info("retrieve portfolio {}", uid.getValue());
        final PortfolioBean dbPortfolio = positionMasterSession.getPortfolioBeanByIdentifier(instant, uid.getValue());
        if (dbPortfolio == null) {
          s_logger.debug("portfolio {} not found at {}", uid, instant);
          return null;
        }
        final PortfolioNodeImpl rootNode = loadPortfolioNodeTree(positionMasterSession, instant, dbPortfolio.getRoot());
        return new PortfolioImpl(uid, dbPortfolio.getName(), rootNode);
      }
    });
  }

  /**
   * Stores a portfolio.
   * @param instant  the instant to store at, not null
   * @param portfolio  the portfolio to store, not null
   */
  public void putPortfolio(final InstantProvider instant, final Portfolio portfolio) {
    getHibernateTemplate().execute(new HibernateCallback() {
      private <T extends DateIdentifiableBean> T identifiedBean(final UniqueIdentifiable identifiable, final T bean) {
        final UniqueIdentifier uniqueIdentifier = identifiable.getUniqueIdentifier();
        if (uniqueIdentifier != null) {
          if (getIdentifierScheme().equals(uniqueIdentifier.getScheme())) {
            bean.setIdentifier(uniqueIdentifier.getValue());
          }
        }
        return bean;
      }
      
      private PortfolioNodeBean portfolioNodeToBean(final PositionMasterSession positionMasterSession,
          final PortfolioNodeBean ancestor, final PortfolioNode portfolioNode) {
        final PortfolioNodeBean portfolioNodeBean = identifiedBean(portfolioNode, new PortfolioNodeBean());
        portfolioNodeBean.setAncestor(ancestor);
        portfolioNodeBean.setName(portfolioNode.getName());
        for (PortfolioNode child : portfolioNode.getChildNodes()) {
          portfolioNodeToBean(positionMasterSession, portfolioNodeBean, child);
        }
        for (Position position : portfolioNode.getPositions()) {
          final PositionBean positionBean = identifiedBean(position, new PositionBean());
          positionBean.setQuantity(position.getQuantity());
          for (Identifier securityIdentifier : position.getSecurityKey()) {
            IdentifierAssociationBean securityAssociation = identifiedBean(position, new IdentifierAssociationBean());
            securityAssociation.setDomainSpecificIdentifier(securityIdentifier);
            securityAssociation.setPosition(positionBean);
            positionMasterSession.saveIdentifierAssociationBean(securityAssociation);
          }
          positionMasterSession.addPositionToPortfolioNode(positionBean, portfolioNodeBean);
        }
        return portfolioNodeBean;
      }
      
      @Override
      public Object doInHibernate(final Session session) throws HibernateException, SQLException {
        final PositionMasterSession positionMasterSession = new PositionMasterSession(session);
        s_logger.info("write portfolio {}", portfolio);
        final PortfolioBean portfolioBean = identifiedBean(portfolio, new PortfolioBean());
        portfolioBean.setName(portfolio.getName());
        portfolioBean.setRoot(portfolioNodeToBean(positionMasterSession, null, portfolio.getRootNode()));
        positionMasterSession.savePortfolioBean(portfolioBean);
        return null;
      }
    });
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the complete set of portfolio unique identifiers.
   * @return the set of unique identifiers, not null
   */
  @Override
  public Set<UniqueIdentifier> getPortfolioIds() {
    return getPortfolioIds(TimeSource.system().instant());
  }

  /**
   * Gets the complete set of portfolio unique identifiers.
   * @param instant  the instant to query at, not null
   * @return the set of unique identifiers, not null
   */
  @SuppressWarnings("unchecked")
  public Set<UniqueIdentifier> getPortfolioIds(final InstantProvider instant) {
    return (Set<UniqueIdentifier>) getHibernateTemplate().execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(final Session session) throws HibernateException, SQLException {
        final PositionMasterSession positionMasterSession = new PositionMasterSession(session);
        final Collection<PortfolioBean> dbPortfolios = positionMasterSession.getAllPortfolioBeans(instant);
        final Set<UniqueIdentifier> portfolioIds = new HashSet<UniqueIdentifier>();
        for (PortfolioBean dbPortfolio : dbPortfolios) {
          portfolioIds.add(UniqueIdentifier.of(getIdentifierScheme(), dbPortfolio.getIdentifier()));
        }
        return portfolioIds;
      }
    });
  }

}
