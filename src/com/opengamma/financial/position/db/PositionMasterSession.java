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
import java.util.UUID;

import javax.time.InstantProvider;

import org.hibernate.Query;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.id.DomainSpecificIdentifier;

/**
 * Session operations for the Hibernate position master
 * 
 * @author Andrew Griffin
 */
public class PositionMasterSession {

  private static final Logger s_logger = LoggerFactory.getLogger(PositionMasterSession.class);
  
  private final Session _session;
  
  public PositionMasterSession (final Session session) {
    _session = session;
  }
  
  protected Session getSession () {
    return _session;
  }
  
  private static Date instantToDate (final InstantProvider instant) {
    return new Date(instant.toInstant ().toEpochMillisLong ());
  }
  
  @SuppressWarnings("unchecked")
  public Collection<DomainSpecificIdentifierAssociationBean> getDomainSpecificIdentifierAssociationBeanByDomainIdentifier (final InstantProvider now, final String domain, final String identifier) {
    final Query query = getSession().getNamedQuery("DomainSpecificIdentifierAssociationBean.many.byDomainIdentifier");
    query.setDate("date", instantToDate (now));
    query.setString("domain", domain);
    query.setString("identifier", identifier);
    return query.list ();
  }
  
  public Collection<DomainSpecificIdentifierAssociationBean> getDomainSpecificIdentifierAssociationBeanByDomainIdentifier (final InstantProvider now, final DomainSpecificIdentifier identifier) {
    return getDomainSpecificIdentifierAssociationBeanByDomainIdentifier (now, identifier.getDomain ().getDomainName (), identifier.getValue ());
  }
  
  @SuppressWarnings("unchecked")
  public Collection<DomainSpecificIdentifierAssociationBean> getDomainSpecificIdentifierAssociationBeanByPosition (final InstantProvider now, final PositionBean position) {
    final Query query = getSession ().getNamedQuery ("DomainSpecificIdentifierAssociationBean.many.byPosition");
    query.setDate ("date", instantToDate (now));
    query.setParameter ("position", position);
    return query.list ();
  }
  
  public void saveDomainSpecificIdentifierAssociationBean (final DomainSpecificIdentifierAssociationBean bean) {
    if (bean.getId () != null) {
      getSession ().update (bean);
    } else {
      bean.setId ((Long)getSession ().save (bean));
    }
    getSession ().flush ();
  }
  
  @SuppressWarnings("unchecked")
  public Collection<PortfolioBean> getPortfolioBeanByName (final InstantProvider now, final String name) {
    final Query query = getSession ().getNamedQuery ("PortfolioBean.many.byName");
    query.setDate ("now", instantToDate (now));
    query.setString ("name", name);
    return query.list ();
  }
  
  public PortfolioBean getPortfolioBeanByIdentifier (final InstantProvider now, final String identifier) {
    final Query query = getSession ().getNamedQuery ("PortfolioBean.one.byIdentifier");
    query.setDate ("now", instantToDate (now));
    query.setString ("identifier", identifier);
    return (PortfolioBean)query.uniqueResult ();
  }
  
  @SuppressWarnings("unchecked")
  public Collection<PortfolioBean> getAllPortfolioBeans (final InstantProvider now) {
    final Query query = getSession ().getNamedQuery ("PortfolioBean.all");
    query.setDate ("now", instantToDate (now));
    return query.list ();
  }
  
  public void savePortfolioBean (final PortfolioBean bean) {
    if (bean.getName () == null) {
      throw new NullPointerException ("portfolio must specify a name");
    }
    if (bean.getRoot () == null) {
      throw new NullPointerException ("portfolio must specify a portfolioNode root");
    }
    if (bean.getRoot ().getId () == null) {
      savePortfolioNodeBean (bean.getRoot ());
    }
    if (bean.getIdentifier () == null) {
      bean.setIdentifier (UUID.randomUUID ().toString ());
    }
    if (bean.getId () != null) {
      getSession ().update (bean);
    } else {
      bean.setId ((Long)getSession ().save (bean));
    }
    getSession ().flush ();
  }
  
  public PortfolioNodeBean getPortfolioNodeBeanByIdentifier (final InstantProvider now, final String identifier) {
    final Query query = getSession ().getNamedQuery ("PortfolioNodeBean.one.byIdentifier");
    query.setDate ("now", instantToDate (now));
    query.setString ("identifier", identifier);
    return (PortfolioNodeBean)query.uniqueResult ();
  }
  
  @SuppressWarnings("unchecked")
  public Collection<PortfolioNodeBean> getPortfolioNodeBeanByImmediateAncestorId (final InstantProvider now, final long ancestorId) {
    final Query query = getSession ().getNamedQuery ("PortfolioNodeBean.many.byImmediateAncestorId");
    query.setDate ("now", instantToDate (now));
    query.setLong ("ancestorId", ancestorId);
    return query.list ();
  }
  
  public Collection<PortfolioNodeBean> getPortfolioNodeBeanByImmediateAncestor (final InstantProvider now, final PortfolioNodeBean ancestor) {
    return getPortfolioNodeBeanByImmediateAncestorId (now, ancestor.getId ());
  }
  
  @SuppressWarnings("unchecked")
  public Collection<PortfolioNodeBean> getPortfolioNodeBeanByAncestorId (final InstantProvider now, final long ancestorId) {
    final Query query = getSession ().getNamedQuery ("PortfolioNodeBean.many.byAncestorId");
    query.setDate ("now", instantToDate (now));
    query.setLong ("ancestorId", ancestorId);
    return query.list ();
  }
  
  @SuppressWarnings("unchecked")
  public Collection<PortfolioNodeBean> getPortfolioNodeBeansByDescendantId (final InstantProvider now, final long descendantId) {
    final Query query = getSession ().getNamedQuery ("PortfolioNodeBean.many.byDescendantId");
    query.setDate ("now", instantToDate (now));
    query.setLong ("descendantId", descendantId);
    return query.list ();
  }
  
  public Collection<PortfolioNodeBean> getPortfolioNodeBeanByAncestor (final InstantProvider now, final PortfolioNodeBean ancestor) {
    return getPortfolioNodeBeanByAncestorId (now, ancestor.getId ());
  }
  
  /**
   * Adjust the date range on the descendant bean to within that of the ancestor. If the descendant bean "starts" before the ancestor,
   * shift the start to that of the ancestor. If it finished after then shift it to that of the ancestor.
   */
  private static boolean dateConstraint (final PortfolioNodeBean descendantBean, final PortfolioNodeBean ancestorBean) {
    boolean changed = false;
    if (ancestorBean.getStartDate () != null) {
      if ((descendantBean.getStartDate () == null) || (descendantBean.getStartDate ().compareTo (ancestorBean.getStartDate ()) < 0)) {
        descendantBean.setStartDate (ancestorBean.getStartDate ());
        if ((descendantBean.getEndDate () != null) && (descendantBean.getStartDate ().compareTo (descendantBean.getEndDate ()) > 0)) {
          descendantBean.setEndDate (descendantBean.getStartDate ());
        }
        changed = true;
      }
    }
    if (ancestorBean.getEndDate () != null) {
      if ((descendantBean.getEndDate () == null) || (descendantBean.getEndDate ().compareTo (ancestorBean.getEndDate ()) > 0)) {
        descendantBean.setEndDate (ancestorBean.getEndDate ());
        if ((descendantBean.getStartDate () != null) && (descendantBean.getEndDate ().compareTo (descendantBean.getStartDate ()) < 0)) {
          descendantBean.setStartDate (descendantBean.getStartDate ());
        }
        changed = true;
      }
    }
    return changed;
  }
  
  /**
   * Saves a PortfolioNodeBean, applying date constraints and updating the NodeHierarchy table.
   */
  @SuppressWarnings("unchecked")
  private static void savePortfolioNodeBeanTransaction (final Session session, final PortfolioNodeBean bean) {
    if (bean.getAncestorId () == null) {
      // either no ancestor, or ancestor not written to database yet
      if (bean.getAncestor () != null) {
        savePortfolioNodeBeanTransaction (session, bean.getAncestor ());
      }
    }
    if (bean.getIdentifier () == null) {
      bean.setIdentifier (UUID.randomUUID ().toString ());
    }
    if (bean.getId () != null) {
      session.update (bean);
    } else {
      bean.setId ((Long)session.save (bean));
    }
    session.flush ();
    // get all nodes previously above this
    Query query = session.getNamedQuery ("PortfolioNode.many.byDescendantId");
    query.setLong ("descendantId", bean.getId ());
    final Collection<Number> oldNodesAbove = query.list();
    // get all nodes below this
    query = session.getNamedQuery ("PortfolioNode.many.byAncestorId");
    query.setLong ("ancestorId", bean.getId ());
    final Collection<Number> nodesBelow = new HashSet<Number> (query.list ());
    // get all nodes now above this
    final Collection<Number> newNodesAbove;
    if (bean.getAncestorId () == null) {
      newNodesAbove = Collections.EMPTY_SET;
    } else {
      query = session.getNamedQuery ("PortfolioNode.many.byDescendantId");
      query.setLong ("descendantId", bean.getAncestorId ());
      newNodesAbove = new HashSet<Number> (query.list ());
      newNodesAbove.add (bean.getAncestorId ());
    }
    // get nodes now not above this (OLDup-NEWup * BELOW) - remove
    Collection<Number> tmp = new HashSet<Number> (oldNodesAbove);
    tmp.removeAll (newNodesAbove);
    if (!tmp.isEmpty ()) {
      //query = getSession ().createSQLQuery ("DELETE FROM pos_nodehierarchy WHERE ancestor_id=:ancestorId AND descendant_id=:descendantId");
      query = session.getNamedQuery ("NodeHierarchy.delete");
      for (Number ancestor_id : tmp) {
        query.setLong ("ancestorId", ancestor_id.longValue ());
        query.setLong ("descendantId", bean.getId ());
        query.executeUpdate ();
        for (Number descendant_id : nodesBelow) {
          query.setLong ("descendantId", descendant_id.longValue ());
          query.executeUpdate ();
        }
      }
    }
    // get nodes now new above this (NEWup-OLDup * BELOW) - add
    newNodesAbove.removeAll (oldNodesAbove);
    if (!newNodesAbove.isEmpty ()) {
      //query = getSession ().createSQLQuery ("INSERT INTO pos_nodehierarchy(ancestor_id,descendant_id) VALUES (:ancestorId,:descendantId)");
      query = session.getNamedQuery ("NodeHierarchy.insert");
      query.setLong ("descendantId", bean.getId ());
      boolean beanChanged = false;
      for (Number ancestor_id : newNodesAbove) {
        query.setLong ("ancestorId", ancestor_id.longValue ());
        query.executeUpdate ();
        final PortfolioNodeBean ancestor = (PortfolioNodeBean)session.get (PortfolioNodeBean.class, ancestor_id.longValue ());
        if (dateConstraint (bean, ancestor)) {
          beanChanged = true;
        }
      }
      if (beanChanged) {
        session.update (bean);
      }
      for (Number ancestor_id : newNodesAbove) {
        query.setLong ("ancestorId", ancestor_id.longValue ());
        for (Number descendant_id : nodesBelow) {
          query.setLong ("descendantId", descendant_id.longValue ());
          query.executeUpdate ();
          final PortfolioNodeBean descendant = (PortfolioNodeBean)session.get (PortfolioNodeBean.class, descendant_id.longValue ());
          if (dateConstraint (descendant, bean)) {
            session.update (descendant);
          }
        }
      }
    }
    session.flush ();
  }
  
  public PositionBean getPositionBeanByIdentifier (final InstantProvider now, final String identifier) {
    final Query query = getSession ().getNamedQuery ("PositionBean.one.byIdentifier");
    query.setDate ("now", instantToDate (now));
    query.setString ("identifier", identifier);
    return (PositionBean)query.uniqueResult ();
  }
  
  @SuppressWarnings("unchecked")
  public Collection<PositionBean> getPositionBeanByImmediatePortfolioNodeId (final InstantProvider now, final long portfolioNodeId) {
    final Query query = getSession ().getNamedQuery ("PositionBean.many.byImmediatePortfolioNodeId");
    query.setDate ("now", instantToDate (now));
    query.setLong ("portfolioNodeId", portfolioNodeId);
    return query.list ();
  }
  
  public Collection<PositionBean> getPositionBeanByImmediatePortfolioNode (final InstantProvider now, final PortfolioNodeBean portfolioNode) {
    return getPositionBeanByImmediatePortfolioNodeId (now, portfolioNode.getId ());
  }
  
  @SuppressWarnings("unchecked")
  public Collection<PositionBean> getPositionBeanByPortfolioNodeId (final InstantProvider now, final long portfolioNodeId) {
    final Query query = getSession ().getNamedQuery ("PositionBean.many.byPortfolioNodeId");
    query.setDate ("now", instantToDate (now));
    query.setLong ("portfolioNodeId", portfolioNodeId);
    return query.list ();
  }
  
  public Collection<PositionBean> getPositionBeanByPortfolioNode (final InstantProvider now, final PortfolioNodeBean portfolioNode) {
    return getPositionBeanByPortfolioNodeId (now, portfolioNode.getId ());
  }
  
  public void savePositionBean (final PositionBean bean) {
    if (bean.getQuantity () == null) {
      throw new NullPointerException ("position must specify a quantity");
    }
    if (bean.getIdentifier () == null) {
      bean.setIdentifier (UUID.randomUUID ().toString ());
    }
    if (bean.getId () != null) {
      getSession ().update (bean);
    } else {
      bean.setId ((Long)getSession ().save (bean));
    }
    getSession ().flush ();
  }
  
  public void addPositionToPortfolioNode (final PositionBean position, final PortfolioNodeBean portfolioNode) {
    if (position.getId () != null) {
      savePositionBean (position);
    }
    if (portfolioNode.getId () != null) {
      savePortfolioNodeBean (portfolioNode);
    }
    final Query query = getSession ().getNamedQuery ("NodeInclusion.insert");
    query.setLong ("positionId", position.getId ());
    query.setLong ("nodeId", portfolioNode.getId ());
    query.executeUpdate ();
    getSession ().flush ();
  }
  
  public void removePositionFromPortfolioNode (final PositionBean position, final PortfolioNodeBean portfolioNode) {
    if (position.getId () == null) throw new NullPointerException ("position bean is not from the database; cannot remove association with no ID");
    if (portfolioNode.getId () == null) throw new NullPointerException ("portfolioNode bean is not from the database; cannot remove association with no ID");
    final Query query = getSession ().getNamedQuery ("NodeInclusion.delete");
    query.setLong ("positionId", position.getId ());
    query.setLong ("nodeId", position.getId ());
    query.executeUpdate ();
    getSession ().flush ();
  }
  
}