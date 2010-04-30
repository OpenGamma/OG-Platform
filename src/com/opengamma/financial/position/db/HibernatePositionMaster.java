/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.db;

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
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;

/**
 * A Hibernate database backed implementation of a PositionMaster. 
 */
public class HibernatePositionMaster implements PositionMaster, InitializingBean {
  
  public static final String IDENTIFIER_SCHEME_DEFAULT = "HibernatePositionMaster";

  private static final Logger s_logger = LoggerFactory.getLogger(HibernatePositionMaster.class);
  
  private HibernateTemplate _hibernateTemplate;
  private String _identifierScheme = IDENTIFIER_SCHEME_DEFAULT;
  
  public HibernatePositionMaster () {
  }
  
  public String getIdentifierScheme () {
    return _identifierScheme;
  }
  
  public void setIdentifierScheme (final String identifierScheme) {
    _identifierScheme = identifierScheme;
  }
  
  public void setSessionFactory (final SessionFactory sessionFactory) {
    ArgumentChecker.notNull(sessionFactory, "sessionFactory");
    _hibernateTemplate = new HibernateTemplate (sessionFactory);
  }
  
  protected HibernateTemplate getHibernateTemplate () {
    return _hibernateTemplate;
  }
  
  @Override
  public void afterPropertiesSet () throws Exception {
    if (getHibernateTemplate () == null) {
      throw new IllegalStateException ("sessionFactory not set");
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Hibernate position implementation.
   */
  private static class PositionImpl implements Position {
    private final UniqueIdentifier _identityKey;
    private final BigDecimal _quantity;
    private final IdentifierBundle _securityKey;
    private PositionImpl(final UniqueIdentifier identifier, final BigDecimal quantity, final IdentifierBundle securityKey) {
      _identityKey = identifier;
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
      return _identityKey;
    }
  }
  
  //-------------------------------------------------------------------------
  @Override
  public PortfolioNode getPortfolioNode(final UniqueIdentifier identifier) {
    return getPortfolioNode(TimeSource.system().instant(), identifier);
  }

  public PortfolioNode getPortfolioNode(final InstantProvider now, final UniqueIdentifier identifier) {
    if (identifier.getScheme().equals(getIdentifierScheme()) == false) {
      s_logger.debug("rejecting invalid identity key domain '{}'", identifier.getScheme());
      throw new IllegalArgumentException("Invalid identifier for HibernatePositionMaster: " + identifier);
    }
    return (PortfolioNode) getHibernateTemplate().execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(final Session session) throws HibernateException, SQLException {
        final PositionMasterSession positionMasterSession = new PositionMasterSession(session);
        s_logger.info("retrieve node {}", identifier.getValue());
        final PortfolioNodeBean dbNode = positionMasterSession.getPortfolioNodeBeanByIdentifier(now, identifier.getValue());
        if (dbNode == null) {
          s_logger.debug("bean not found for {} at {}", identifier, now);
          return null;
        }
        return loadPortfolioNode(positionMasterSession, now, dbNode);
      }
    });
  }

  private PortfolioNodeImpl loadPortfolioNode(final PositionMasterSession session, final InstantProvider now, final PortfolioNodeBean dbNode) {
    // TODO this is a slow way of constructing the Node graph - there are a number of recursive queries. One of the bulk fetches could be used and the graph built up from the information in each node
    UniqueIdentifier uid = UniqueIdentifier.of(getIdentifierScheme(), dbNode.getIdentifier());
    String name = dbNode.getName();
    final PortfolioNodeImpl node = new PortfolioNodeImpl(uid, name);
    for (PortfolioNodeBean child : session.getPortfolioNodeBeanByImmediateAncestor(now, dbNode)) {
      node.addChildNode(loadPortfolioNode(session, now, child));
    }
    for (final PositionBean position : session.getPositionBeanByImmediatePortfolioNode(now, dbNode)) {
      node.addPosition(loadPosition(session, now, position));
    }
    return node;
  }

  //-------------------------------------------------------------------------
  @Override
  public Position getPosition(final UniqueIdentifier identifier) {
    return getPosition(TimeSource.system().instant(), identifier);
  }

  public Position getPosition(final InstantProvider now, final UniqueIdentifier identifier) {
    if (identifier.getScheme().equals(getIdentifierScheme()) == false) {
      s_logger.debug("rejecting invalid identity key domain '{}'", identifier.getScheme());
      throw new IllegalArgumentException("Invalid identifier for HibernatePositionMaster: " + identifier);
    }
    return (Position) getHibernateTemplate().execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(final Session session) throws HibernateException, SQLException {
        final PositionMasterSession positionMasterSession = new PositionMasterSession(session);
        s_logger.info("retrieve position {}", identifier.getValue());
        final PositionBean dbPosition = positionMasterSession.getPositionBeanByIdentifier(now, identifier.getValue());
        if (dbPosition == null) {
          s_logger.debug("bean not found for {} at {}", identifier, now);
          return null;
        }
        return loadPosition(positionMasterSession, now, dbPosition);
      }
    });
  }

  private Position loadPosition (final PositionMasterSession session, final InstantProvider now, final PositionBean dbPosition) {
    final Collection<IdentifierAssociationBean> assocBeans = session.getIdentifierAssociationBeanByPosition(now, dbPosition);
    final Collection<Identifier> dsids = new ArrayList<Identifier>(assocBeans.size());
    for (IdentifierAssociationBean assocBean : assocBeans) {
      dsids.add(assocBean.getDomainSpecificIdentifier());
    }
    UniqueIdentifier uid = UniqueIdentifier.of(getIdentifierScheme(), dbPosition.getIdentifier());
    return new PositionImpl(uid, dbPosition.getQuantity(), new IdentifierBundle(dsids));
  }

  //-------------------------------------------------------------------------
  @Override
  public Portfolio getPortfolio(UniqueIdentifier identifier) {
    return getPortfolio(TimeSource.system().instant(), identifier);
  }

  public Portfolio getPortfolio(final InstantProvider now, final UniqueIdentifier identifier) {
    if (identifier.getScheme().equals(getIdentifierScheme()) == false) {
      throw new IllegalArgumentException("Invalid identifier for HibernatePositionMaster: " + identifier);
    }
    return (Portfolio) getHibernateTemplate().execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(final Session session) throws HibernateException, SQLException {
        final PositionMasterSession positionMasterSession = new PositionMasterSession(session);
        s_logger.info("retrieve portfolio {}", identifier.getValue());
        final PortfolioBean dbPortfolio = positionMasterSession.getPortfolioBeanByIdentifier(now, identifier.getValue());
        if (dbPortfolio == null) {
          s_logger.debug("portfolio {} not found at {}", identifier, now);
          return null;
        }
        final PortfolioNodeImpl rootNode = loadPortfolioNode(positionMasterSession, now, dbPortfolio.getRoot());
        return new PortfolioImpl(identifier, dbPortfolio.getName(), rootNode);
      }
    });
  }

  //-------------------------------------------------------------------------
  @Override
  public Set<UniqueIdentifier> getPortfolioIds() {
    return getPortfolioIds(TimeSource.system().instant());
  }

  @SuppressWarnings("unchecked")
  public Set<UniqueIdentifier> getPortfolioIds(final InstantProvider now) {
    return (Set<UniqueIdentifier>) getHibernateTemplate().execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(final Session session) throws HibernateException, SQLException {
        final PositionMasterSession positionMasterSession = new PositionMasterSession(session);
        final Collection<PortfolioBean> dbPortfolios = positionMasterSession.getAllPortfolioBeans(now);
        final Set<UniqueIdentifier> portfolioIds = new HashSet<UniqueIdentifier>();
        for (PortfolioBean dbPortfolio : dbPortfolios) {
          portfolioIds.add(UniqueIdentifier.of(getIdentifierScheme(), dbPortfolio.getIdentifier()));
        }
        return portfolioIds;
      }
    });
  }

}
