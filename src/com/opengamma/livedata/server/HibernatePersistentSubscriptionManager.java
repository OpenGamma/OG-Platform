/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import java.util.List;
import java.util.Set;
import java.util.Timer;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;

/**
 *
 * @author pietari
 */
public class HibernatePersistentSubscriptionManager extends AbstractPersistentSubscriptionManager {
  
  private HibernateTemplate _hibernateTemplate = null;

  @Autowired
  public void setSessionFactory(SessionFactory sessionFactory) {
    _hibernateTemplate = new HibernateTemplate(sessionFactory);
  }
  
  public HibernatePersistentSubscriptionManager(AbstractLiveDataServer server) {
    super(server);
  }
  
  public HibernatePersistentSubscriptionManager(AbstractLiveDataServer server, Timer timer, long savePeriod) {
    super(server, timer, savePeriod);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void readFromStorage() {
    List<PersistentSubscription> subs = (List<PersistentSubscription>) _hibernateTemplate.loadAll(PersistentSubscription.class);
    for (PersistentSubscription sub : subs) {
      addPersistentSubscription(sub);
    }
  }

  @Override
  public void saveToStorage() {
    Set<PersistentSubscription> subscriptions = getPersistentSubscriptions();
    _hibernateTemplate.saveOrUpdateAll(subscriptions);
  }
  
  public static Class<?>[] getHibernateMappingClasses() {
    return new Class[] { PersistentSubscription.class };
  }
  
}
