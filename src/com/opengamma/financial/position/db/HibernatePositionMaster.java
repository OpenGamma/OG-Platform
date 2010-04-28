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
import com.opengamma.engine.position.PortfolioId;
import com.opengamma.engine.position.PortfolioImpl;
import com.opengamma.engine.position.PortfolioNode;
import com.opengamma.engine.position.PortfolioNodeImpl;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.position.PositionMaster;
import com.opengamma.engine.security.Security;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * A Hibernate database backed implementation of a PositionMaster. 
 */
public class HibernatePositionMaster implements PositionMaster, InitializingBean {

  private static final Logger s_logger = LoggerFactory.getLogger(HibernatePositionMaster.class);
  
  private HibernateTemplate _hibernateTemplate;
  
  public HibernatePositionMaster () {
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
  
  private static class PositionImpl implements Position {
    private final Identifier _identityKey;
    private final BigDecimal _quantity;
    private final IdentifierBundle _securityKey;
    private PositionImpl (final String identifier, final BigDecimal quantity, final IdentifierBundle securityKey) {
      _identityKey = new Identifier (POSITION_IDENTITY_KEY_SCHEME, identifier);
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
    public Identifier getIdentityKey() {
      return _identityKey;
    }
  }
  
  // TODO this is a slow way of constructing the Node graph - there are a number of recursive queries. One of the bulk fetches could be used and the graph built up from the information in each node
  
  private Position positionBeanToPosition (final PositionMasterSession session, final InstantProvider now, final PositionBean position) {
    final Collection<IdentifierAssociationBean> assocBeans = session.getIdentifierAssociationBeanByPosition (now, position);
    final Collection<Identifier> dsids = new ArrayList<Identifier> (assocBeans.size ());
    for (IdentifierAssociationBean assocBean : assocBeans) {
      dsids.add (assocBean.getDomainSpecificIdentifier ());
    }
    return new PositionImpl (position.getIdentifier (), position.getQuantity (), new IdentifierBundle (dsids));
  }

  private void loadPortfolioNodeChildren (final PositionMasterSession session, final InstantProvider now, final PortfolioNodeImpl node, final PortfolioNodeBean bean) {
    node.setIdentityKey (bean.getIdentifier ());
    for (PortfolioNodeBean child : session.getPortfolioNodeBeanByImmediateAncestor (now, bean)) {
      final PortfolioNodeImpl childNode = new PortfolioNodeImpl (child.getName ());
      loadPortfolioNodeChildren (session, now, childNode, child);
      node.addChildNode (childNode);
    }
    for (final PositionBean position : session.getPositionBeanByImmediatePortfolioNode (now, bean)) {
      node.addPosition (positionBeanToPosition (session, now, position));
    }
  }
  
  public PortfolioNode getPortfolioNode (final InstantProvider now, final Identifier identityKey) {
    if (!identityKey.getScheme ().equals (PortfolioNode.PORTFOLIO_NODE_IDENTITY_KEY_SCHEME)) {
      s_logger.debug ("rejecting invalid identity key domain '{}'", identityKey.getScheme ());
      return null;
    }
    return (PortfolioNode)getHibernateTemplate ().execute (new HibernateCallback () {
      @Override
      public Object doInHibernate (final Session session) throws HibernateException, SQLException {
        final PositionMasterSession positionMasterSession = new PositionMasterSession (session);
        s_logger.info ("retrieve {}", identityKey.getValue ());
        final PortfolioNodeBean bean = positionMasterSession.getPortfolioNodeBeanByIdentifier (now, identityKey.getValue ());
        if (bean == null) {
          s_logger.debug ("bean not found for {} at {}", identityKey, now);
          return null;
        }
        final PortfolioNodeImpl node = new PortfolioNodeImpl (bean.getName ());
        loadPortfolioNodeChildren (positionMasterSession, now, node, bean);
        return node;
      }
    });
  }

  @Override
  public PortfolioNode getPortfolioNode(final Identifier identityKey) {
    return getPortfolioNode (TimeSource.system ().instant (), identityKey);
  }
  
  public Position getPosition (final InstantProvider now, final Identifier identityKey) {
    if (!identityKey.getScheme ().equals (Position.POSITION_IDENTITY_KEY_SCHEME)) {
      s_logger.debug ("rejecting invalid identity key domain '{}'", identityKey.getScheme ());
      return null;
    }
    return (Position)getHibernateTemplate ().execute (new HibernateCallback () {
      @Override
      public Object doInHibernate (final Session session) throws HibernateException, SQLException {
        final PositionMasterSession positionMasterSession = new PositionMasterSession (session);
        final PositionBean bean = positionMasterSession.getPositionBeanByIdentifier (now, identityKey.getValue ());
        if (bean == null) {
          s_logger.debug ("bean not found for {} at {}", identityKey, now);
          return null;
        }
        return positionBeanToPosition (positionMasterSession, now, bean);
      }
    });
  }

  @Override
  public Position getPosition(final Identifier identityKey) {
    return getPosition (TimeSource.system ().instant (), identityKey);
  }

  //-------------------------------------------------------------------------
  @Override
  public Portfolio getPortfolio(PortfolioId portfolioId) {
    return getPortfolio(TimeSource.system().instant(), portfolioId);
  }

  public Portfolio getPortfolio(final InstantProvider now, final PortfolioId portfolioId) {
    if (portfolioId.getId().startsWith("h8/") == false) {
      throw new IllegalArgumentException("Invalid portfolio id for Hibernate: " + portfolioId);
    }
    return (Portfolio) getHibernateTemplate().execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(final Session session) throws HibernateException, SQLException {
        final PositionMasterSession positionMasterSession = new PositionMasterSession(session);
        final PortfolioBean dbPortfolio = positionMasterSession.getPortfolioBeanByIdentifier(now, portfolioId.getId().substring(3));
        if (dbPortfolio == null) {
          s_logger.debug("portfolio {} not found at {}", portfolioId, now);
          return null;
        }
        final PortfolioNodeImpl rootNode = new PortfolioNodeImpl();
        final PortfolioImpl portfolio = new PortfolioImpl(portfolioId, dbPortfolio.getName(), rootNode);
        loadPortfolioNodeChildren(positionMasterSession, now, rootNode, dbPortfolio.getRoot());
        return portfolio;
      }
    });
  }

  //-------------------------------------------------------------------------
  @Override
  public Set<PortfolioId> getPortfolioIds() {
    return getPortfolioIds(TimeSource.system().instant());
  }

  @SuppressWarnings("unchecked")
  public Set<PortfolioId> getPortfolioIds(final InstantProvider now) {
    return (Set<PortfolioId>) getHibernateTemplate().execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(final Session session) throws HibernateException, SQLException {
        final PositionMasterSession positionMasterSession = new PositionMasterSession(session);
        final Collection<PortfolioBean> dbPortfolios = positionMasterSession.getAllPortfolioBeans(now);
        final Set<PortfolioId> portfolioIds = new HashSet<PortfolioId>();
        for (PortfolioBean dbPortfolio : dbPortfolios) {
          portfolioIds.add(PortfolioId.of("h8/" + dbPortfolio.getId()));
        }
        return portfolioIds;
      }
    });
  }

}
