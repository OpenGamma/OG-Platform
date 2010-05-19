/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.db;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;

import javax.time.Instant;
import javax.time.InstantProvider;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.Identifier;
import com.opengamma.util.ArgumentChecker;

/**
 * Session operations for the Hibernate position master
 */
public class PositionMasterSession {

  //private static final Logger s_logger = LoggerFactory.getLogger(PositionMasterSession.class);

  /**
   * The Hibernate session.
   */
  private final Session _session;

  /**
   * Creates a session wrapping the Hibernate session.
   * @param session  the session to wrap, not null
   */
  public PositionMasterSession(final Session session) {
    ArgumentChecker.notNull(session, "session");
    _session = session;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the Hibernate session.
   * @return the session, not null
   */
  protected Session getSession() {
    return _session;
  }

  /**
   * Utility method to convert an instant to a date.
   * @param instant  the instant to convert, not null
   * @return the converted date, not null
   */
  private static Date instantToDate(final InstantProvider instant) {
    return new Date(Instant.of(instant).toEpochMillisLong());
  }

  //-------------------------------------------------------------------------
  /**
   * Searches for identifiers.
   * @param instant  the instant to query at, not null
   * @param idScheme  the identifier scheme, not null
   * @param idValue  the identifier value, not null
   * @return the matching identifiers, not null
   */
  @SuppressWarnings("unchecked")
  public Collection<IdentifierAssociationBean> getIdentifierAssociationBeanByIdentifier(
      final InstantProvider instant, final String idScheme, final String idValue) {
    final Query query = getSession().getNamedQuery("IdentifierAssociationBean.many.byIdentifier");
    query.setDate("now", instantToDate(instant));
    query.setString("scheme", idScheme);
    query.setString("identifier", idValue);
    return query.list();
  }

  /**
   * Searches for identifiers.
   * @param instant  the instant to query at, not null
   * @param identifier  the identifier, not null
   * @return the matching identifiers, not null
   */
  public Collection<IdentifierAssociationBean> getIdentifierAssociationBeanByIdentifier(
      final InstantProvider instant, final Identifier identifier) {
    return getIdentifierAssociationBeanByIdentifier(instant, identifier.getScheme().getName(), identifier.getValue());
  }

  /**
   * Searches for identifiers.
   * @param instant  the instant to query at, not null
   * @param position  the position, not null
   * @return the matching identifiers, not null
   */
  @SuppressWarnings("unchecked")
  public Collection<IdentifierAssociationBean> getIdentifierAssociationBeanByPosition(
      final InstantProvider instant, final PositionBean position) {
    final Query query = getSession().getNamedQuery("IdentifierAssociationBean.many.byPosition");
    query.setDate("now", instantToDate(instant));
    query.setParameter("position", position);
    return query.list();
  }

  /**
   * Stores an identifier to the database.
   * @param dbBean  the identifier bean, not null
   */
  public void saveIdentifierAssociationBean(final IdentifierAssociationBean dbBean) {
    ArgumentChecker.notNull(dbBean, "bean");
    ArgumentChecker.notNull(dbBean.getScheme(), "bean.scheme");
    ArgumentChecker.notNull(dbBean.getIdentifier(), "bean.identifier");
    ArgumentChecker.notNull(dbBean.getPosition(), "bean.position");
    if (dbBean.getPosition().getId() == null) {
      savePositionBean(dbBean.getPosition());
    }
    if (dbBean.getId() != null) {
      getSession().update(dbBean);
    } else {
      dbBean.setId((Long) getSession().save(dbBean));
    }
    getSession().flush();
  }

  //-------------------------------------------------------------------------
  /**
   * Finds a portfolio by identifier.
   * @param instant  the instant to query at, not null
   * @param oid  the object identifier to query, not null
   * @return the matching portfolio, null if not found
   */
  public PortfolioBean getPortfolioBeanByIdentifier(final InstantProvider instant, final String oid) {
    final Query query = getSession().getNamedQuery("PortfolioBean.one.byIdentifier");
    query.setDate("now", instantToDate(instant));
    query.setString("identifier", oid);
    return (PortfolioBean) query.uniqueResult();
  }

  /**
   * Searches for portfolios by name.
   * @param instant  the instant to query at, not null
   * @param name  the name to query, not null
   * @return the matching portfolios, not null
   */
  @SuppressWarnings("unchecked")
  public Collection<PortfolioBean> getPortfolioBeanByName(final InstantProvider instant, final String name) {
    final Query query = getSession().getNamedQuery("PortfolioBean.many.byName");
    query.setDate("now", instantToDate(instant));
    query.setString("name", name);
    return query.list();
  }

  /**
   * Gets all the portfolios.
   * @param instant  the instant to query at, not null
   * @return the matching portfolios, not null
   */
  @SuppressWarnings("unchecked")
  public Collection<PortfolioBean> getAllPortfolioBeans(final InstantProvider instant) {
    final Query query = getSession().getNamedQuery("PortfolioBean.all");
    query.setDate("now", instantToDate(instant));
    return query.list();
  }

  //-------------------------------------------------------------------------
  /**
   * Stores a portfolio structure bean.
   * @param session  the session to use, not null
   * @param identifierPrefix  the prefix for the identifier
   * @param bean
   */
  private static void saveOrUpdateIdentifiableBean(
      final Session session, final String identifierPrefix, final DateIdentifiableBean bean) {
    if (bean.getId() != null) {
      if (bean.getIdentifier() == null) {
        bean.setIdentifier(identifierPrefix + bean.getId());
      }
      session.update(bean);
    } else {
      bean.setId((Long) session.save(bean));
      if (bean.getIdentifier() == null) {
        bean.setIdentifier(identifierPrefix + bean.getId());
        session.update(bean);
      }
    }
    session.flush();
  }

  /**
   * Stores a portfolio.
   * @param bean  the portfolo to store, not null
   */
  public void savePortfolioBean(final PortfolioBean bean) {
    ArgumentChecker.notNull(bean, "portfolio");
    ArgumentChecker.notNull(bean.getName(), "portfolio.name");
    ArgumentChecker.notNull(bean.getRoot(), "portfolio.root");
    if (bean.getRoot().getId() == null) {
      savePortfolioNodeBean(bean.getRoot());
    }
    saveOrUpdateIdentifiableBean(getSession(), "portfolio", bean);
  }

  //-------------------------------------------------------------------------
  /**
   * Finds a portfolio node by identifier.
   * @param instant  the instant to query at, not null
   * @param oid  the object identifier to query, not null
   * @return the matching portfolio node, null if not found
   */
  public PortfolioNodeBean getPortfolioNodeBeanByIdentifier(final InstantProvider instant, final String oid) {
    final Query query = getSession().getNamedQuery("PortfolioNodeBean.one.byIdentifier");
    query.setDate("now", instantToDate(instant));
    query.setString("identifier", oid);
    return (PortfolioNodeBean) query.uniqueResult();
  }

  /**
   * Loads child portfolio nodes by ancestor.
   * @param instant  the instant to query at, not null
   * @param ancestorId  the ancestor id in the tree
   * @return the matching portfolio nodes, not null
   */
  @SuppressWarnings("unchecked")
  public Collection<PortfolioNodeBean> getPortfolioNodeBeanByImmediateAncestorId(
      final InstantProvider instant, final long ancestorId) {
    final Query query = getSession().getNamedQuery("PortfolioNodeBean.many.byImmediateAncestorId");
    query.setDate("now", instantToDate(instant));
    query.setLong("ancestorId", ancestorId);
    return query.list();
  }

  /**
   * Loads child portfolio nodes by ancestor.
   * @param instant  the instant to query at, not null
   * @param ancestor  the ancestor in the tree, not null
   * @return the matching portfolio nodes, not null
   */
  public Collection<PortfolioNodeBean> getPortfolioNodeBeanByImmediateAncestor(
      final InstantProvider instant, final PortfolioNodeBean ancestor) {
    return getPortfolioNodeBeanByImmediateAncestorId(instant, ancestor.getId());
  }

  /**
   * Loads all portfolio nodes by ancestor.
   * @param instant  the instant to query at, not null
   * @param ancestorId  the ancestor id in the tree, not null
   * @return the matching portfolio nodes, not null
   */
  @SuppressWarnings("unchecked")
  public Collection<PortfolioNodeBean> getPortfolioNodeBeanByAncestorId(
      final InstantProvider instant, final long ancestorId) {
    final Query query = getSession().getNamedQuery("PortfolioNodeBean.many.byAncestorId");
    query.setDate("now", instantToDate(instant));
    query.setLong("ancestorId", ancestorId);
    return query.list();
  }

  /**
   * Loads all portfolio nodes by ancestor.
   * @param instant  the instant to query at, not null
   * @param ancestor  the ancestor in the tree, not null
   * @return the matching portfolio nodes, not null
   */
  public Collection<PortfolioNodeBean> getPortfolioNodeBeanByAncestor(
      final InstantProvider instant, final PortfolioNodeBean ancestor) {
    return getPortfolioNodeBeanByAncestorId(instant, ancestor.getId());
  }

  /**
   * Loads portfolio nodes using a descendant id.
   * @param instant  the instant to query at, not null
   * @param descendantId  the descendant id in the tree, not null
   * @return the matching portfolio nodes, not null
   */
  @SuppressWarnings("unchecked")
  public Collection<PortfolioNodeBean> getPortfolioNodeBeansByDescendantId(
      final InstantProvider instant, final long descendantId) {
    final Query query = getSession().getNamedQuery("PortfolioNodeBean.many.byDescendantId");
    query.setDate("now", instantToDate(instant));
    query.setLong("descendantId", descendantId);
    return query.list();
  }

  //-------------------------------------------------------------------------
  /**
   * Adjust the date range on the descendant bean to within that of the ancestor.
   * If the descendant bean "starts" before the ancestor, shift the start to that of the ancestor.
   * If it finished after then shift it to that of the ancestor.
   * @param descendantBean  the descendant, not null
   * @param ancestorBean  the ancestor, not null
   * @return true if change made
   */
  private static boolean dateConstraint(final PortfolioNodeBean descendantBean, final PortfolioNodeBean ancestorBean) {
    boolean changed = false;
    if (ancestorBean.getStartDate() != null) {
      if ((descendantBean.getStartDate() == null) || (descendantBean.getStartDate().compareTo(ancestorBean.getStartDate()) < 0)) {
        descendantBean.setStartDate(ancestorBean.getStartDate());
        if ((descendantBean.getEndDate() != null) && (descendantBean.getStartDate().compareTo(descendantBean.getEndDate()) > 0)) {
          descendantBean.setEndDate(descendantBean.getStartDate());
        }
        changed = true;
      }
    }
    if (ancestorBean.getEndDate() != null) {
      if ((descendantBean.getEndDate() == null) || (descendantBean.getEndDate().compareTo(ancestorBean.getEndDate()) > 0)) {
        descendantBean.setEndDate(ancestorBean.getEndDate());
        if ((descendantBean.getStartDate() != null) && (descendantBean.getEndDate().compareTo(descendantBean.getStartDate()) < 0)) {
          descendantBean.setStartDate(descendantBean.getStartDate());
        }
        changed = true;
      }
    }
    return changed;
  }

  /**
   * Saves a PortfolioNodeBean, applying date constraints and updating the NodeHierarchy table.
   * This needs to run as an atomic transaction to keep the database correct.
   * It is static to make it obvious that other things can't be called.
   * @param session  the session to use, not null
   * @param bean  the portfolio node, not null
   */
  @SuppressWarnings("unchecked")
  private static void savePortfolioNodeBeanTransaction(final Session session, final PortfolioNodeBean bean) {
    if (bean.getAncestorId() == null) {
      // either no ancestor, or ancestor not written to database yet
      if (bean.getAncestor() != null) {
        savePortfolioNodeBeanTransaction(session, bean.getAncestor());
      }
    }
    saveOrUpdateIdentifiableBean(session, "node", bean);
    // get all nodes previously above this
    Query query = session.getNamedQuery("PortfolioNode.many.byDescendantId");
    query.setLong("descendantId", bean.getId());
    final Collection<Number> oldNodesAbove = query.list();
    // get all nodes below this
    query = session.getNamedQuery("PortfolioNode.many.byAncestorId");
    query.setLong("ancestorId", bean.getId());
    final Collection<Number> nodesBelow = new HashSet<Number>(query.list());
    // get all nodes now above this
    final Collection<Number> newNodesAbove;
    if (bean.getAncestorId() == null) {
      newNodesAbove = Collections.EMPTY_SET;
    } else {
      query = session.getNamedQuery("PortfolioNode.many.byDescendantId");
      query.setLong("descendantId", bean.getAncestorId());
      newNodesAbove = new HashSet<Number>(query.list());
      newNodesAbove.add(bean.getAncestorId());
    }
    // get nodes now not above this (OLDup-NEWup * BELOW) - remove
    Collection<Number> tmp = new HashSet<Number>(oldNodesAbove);
    tmp.removeAll(newNodesAbove);
    if (!tmp.isEmpty()) {
      query = session.getNamedQuery("NodeHierarchy.delete");
      for (Number ancestor_id : tmp) {
        query.setLong("ancestorId", ancestor_id.longValue());
        query.setLong("descendantId", bean.getId());
        query.executeUpdate();
        for (Number descendant_id : nodesBelow) {
          query.setLong("descendantId", descendant_id.longValue());
          query.executeUpdate();
        }
      }
    }
    // get nodes now new above this (NEWup-OLDup * BELOW) - add
    newNodesAbove.removeAll(oldNodesAbove);
    if (!newNodesAbove.isEmpty()) {
      query = session.getNamedQuery("NodeHierarchy.insert");
      query.setLong("descendantId", bean.getId());
      boolean beanChanged = false;
      for (Number ancestor_id : newNodesAbove) {
        query.setLong("ancestorId", ancestor_id.longValue());
        query.executeUpdate();
        final PortfolioNodeBean ancestor = (PortfolioNodeBean) session.get(PortfolioNodeBean.class, ancestor_id.longValue());
        if (dateConstraint(bean, ancestor)) {
          beanChanged = true;
        }
      }
      if (beanChanged) {
        session.update(bean);
      }
      for (Number ancestor_id : newNodesAbove) {
        query.setLong("ancestorId", ancestor_id.longValue());
        for (Number descendant_id : nodesBelow) {
          query.setLong("descendantId", descendant_id.longValue());
          query.executeUpdate();
          final PortfolioNodeBean descendant = (PortfolioNodeBean) session.get(PortfolioNodeBean.class, descendant_id.longValue());
          if (dateConstraint(descendant, bean)) {
            session.update(descendant);
          }
        }
      }
    }
    session.flush();
  }

  /**
   * Stores a portfolio node.
   * @param bean  the portfolio node, not null
   */
  public void savePortfolioNodeBean(final PortfolioNodeBean bean) {
    final Session session = getSession();
    final Transaction transaction = session.beginTransaction();
    try {
      savePortfolioNodeBeanTransaction(session, bean);
      transaction.commit();
    } catch (Exception e) {
      transaction.rollback();
      throw new OpenGammaRuntimeException("transaction rolled back", e);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Gets a position by object identifier.
   * @param instant  the instant to query at, not null
   * @param oid  the object identifier, not null
   * @return the matching position, null if not found
   */
  public PositionBean getPositionBeanByIdentifier(final InstantProvider instant, final String oid) {
    final Query query = getSession().getNamedQuery("PositionBean.one.byIdentifier");
    query.setDate("now", instantToDate(instant));
    query.setString("identifier", oid);
    return (PositionBean) query.uniqueResult();
  }

  /**
   * Searches for positions by position node.
   * @param instant  the instant to query at, not null
   * @param portfolioNodeId  the node id
   * @return the matching positions, not null
   */
  @SuppressWarnings("unchecked")
  public Collection<PositionBean> getPositionBeanByImmediatePortfolioNodeId(
      final InstantProvider instant, final long portfolioNodeId) {
    final Query query = getSession().getNamedQuery("PositionBean.many.byImmediatePortfolioNodeId");
    query.setDate("now", instantToDate(instant));
    query.setLong("portfolioNodeId", portfolioNodeId);
    return query.list();
  }

  /**
   * Searches for positions by position node.
   * @param instant  the instant to query at, not null
   * @param portfolioNode  the node, not null
   * @return the matching positions, not null
   */
  public Collection<PositionBean> getPositionBeanByImmediatePortfolioNode(
      final InstantProvider instant, final PortfolioNodeBean portfolioNode) {
    return getPositionBeanByImmediatePortfolioNodeId(instant, portfolioNode.getId());
  }

  /**
   * Searches for positions by position node.
   * @param instant  the instant to query at, not null
   * @param portfolioNodeId  the node id
   * @return the matching positions, not null
   */
  @SuppressWarnings("unchecked")
  public Collection<PositionBean> getPositionBeanByPortfolioNodeId(
      final InstantProvider instant, final long portfolioNodeId) {
    final Query query = getSession().getNamedQuery("PositionBean.many.byPortfolioNodeId");
    query.setDate("now", instantToDate(instant));
    query.setLong("portfolioNodeId", portfolioNodeId);
    return query.list();
  }

  /**
   * Searches for positions by position node.
   * @param instant  the instant to query at, not null
   * @param portfolioNode  the node, not null
   * @return the matching positions, not null
   */
  public Collection<PositionBean> getPositionBeanByPortfolioNode(final InstantProvider instant,
      final PortfolioNodeBean portfolioNode) {
    return getPositionBeanByPortfolioNodeId(instant, portfolioNode.getId());
  }

  //-------------------------------------------------------------------------
  /**
   * Stores a position.
   * @param bean  the position, not null
   */
  public void savePositionBean(final PositionBean bean) {
    ArgumentChecker.notNull(bean, "position");
    ArgumentChecker.notNull(bean.getQuantity(), "position.quantity");
    saveOrUpdateIdentifiableBean(getSession(), "position", bean);
  }

  /**
   * Adds a position to a node.
   * @param position  the position, not null
   * @param portfolioNode  the node, not null
   */
  public void addPositionToPortfolioNode(final PositionBean position, final PortfolioNodeBean portfolioNode) {
    if (position.getId() == null) {
      savePositionBean(position);
    }
    if (portfolioNode.getId() == null) {
      savePortfolioNodeBean(portfolioNode);
    }
    final Query query = getSession().getNamedQuery("NodeInclusion.insert");
    query.setLong("positionId", position.getId());
    query.setLong("nodeId", portfolioNode.getId());
    query.executeUpdate();
    getSession().flush();
  }

  /**
   * Removes a position from a node.
   * @param position  the position, not null
   * @param portfolioNode  the node, not null
   */
  public void removePositionFromPortfolioNode(final PositionBean position, final PortfolioNodeBean portfolioNode) {
    ArgumentChecker.notNull(position, "position");
    ArgumentChecker.notNull(position.getId(), "position.id");
    ArgumentChecker.notNull(portfolioNode, "portfolioNode");
    ArgumentChecker.notNull(portfolioNode.getId(), "portfolioNode.id");
    final Query query = getSession().getNamedQuery("NodeInclusion.delete");
    query.setLong("positionId", position.getId());
    query.setLong("nodeId", position.getId());
    query.executeUpdate();
    getSession().flush();
  }

}