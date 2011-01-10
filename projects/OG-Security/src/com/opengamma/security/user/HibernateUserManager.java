/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.security.user;

import java.sql.SQLException;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.security.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.DbSource;

/**
 * A manager providing access to the security subsystem.
 */
@Transactional
public class HibernateUserManager implements UserManager, UserDetailsService {

  /**
   * The Hibernate template.
   */
  private HibernateTemplate _hibernateTemplate;

  /**
   * Creates an instance providing access to the database.
   * @param dbSource  the database access source, not null
   */
  public HibernateUserManager(DbSource dbSource) {
    _hibernateTemplate = dbSource.getHibernateTemplate();
  }

  /**
   * Creates an instance providing access to the database.
   * @param sessionFactory  the session factory, not null
   * @deprecated use the DbSource constructor
   */
  @Deprecated
  public HibernateUserManager(SessionFactory sessionFactory) {
    // this constructor allows certain tests to exist
    _hibernateTemplate = new HibernateTemplate(sessionFactory);
  }

  //-------------------------------------------------------------------------
  @Override
  public UserDetails loadUserByUsername(String username)
    throws UsernameNotFoundException {
    User user = getUser(username);
    if (user == null) {
      throw new UsernameNotFoundException(username);
    }
    return user;
  }

  @Override
  public User getUser(final String username) {
    ArgumentChecker.notNull(username, "User name");
    
    return (User) _hibernateTemplate.execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException,
          SQLException {
        Query query = session.getNamedQuery("User.one.byUsername");
        query.setString("username", username);
        return query.uniqueResult();
      }
    });
  }

  @Override
  public UserGroup getUserGroup(final String name) {
    ArgumentChecker.notNull(name, "Group name");
    
    return (UserGroup) _hibernateTemplate.execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException,
          SQLException {
        Query query = session.getNamedQuery("UserGroup.one.byName");
        query.setString("name", name);
        return query.uniqueResult();
      }
    });
  }

  @Override
  public Authority getAuthority(final String regex) {
    ArgumentChecker.notNull(regex, "Authority");
    
    return (Authority) _hibernateTemplate.execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException,
          SQLException {
        Query query = session.getNamedQuery("Authority.one.byAuthorityRegex");
        query.setString("regex", regex);
        return query.uniqueResult();
      }
    });
  }

  @Override
  public void addUser(User user) {
    ArgumentChecker.notNull(user, "User");
    _hibernateTemplate.save(user);
  }

  @Override
  public void deleteUser(User user) {
    ArgumentChecker.notNull(user, "User");
    _hibernateTemplate.delete(user);
  }

  @Override
  public void updateUser(User user) {
    ArgumentChecker.notNull(user, "User");
    _hibernateTemplate.update(user);
  }

  @Override
  public void addUserGroup(UserGroup group) {
    ArgumentChecker.notNull(group, "User group");
    _hibernateTemplate.save(group);
  }

  @Override
  public void deleteUserGroup(UserGroup group) {
    ArgumentChecker.notNull(group, "User group");
    _hibernateTemplate.delete(group);
  }

  @Override
  public void updateUserGroup(UserGroup group) {
    ArgumentChecker.notNull(group, "User group");
    _hibernateTemplate.update(group);
  }

  @Override
  public void addAuthority(Authority authority) {
    ArgumentChecker.notNull(authority, "Authority");
    _hibernateTemplate.save(authority);
  }

  @Override
  public void deleteAuthority(Authority authority) {
    ArgumentChecker.notNull(authority, "Authority");
    _hibernateTemplate.delete(authority);
  }

  @Override
  public void updateAuthority(Authority authority) {
    ArgumentChecker.notNull(authority, "Authority");
    _hibernateTemplate.update(authority);
  }

}
