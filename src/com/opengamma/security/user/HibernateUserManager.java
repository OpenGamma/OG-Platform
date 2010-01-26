/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.security.user;

import java.sql.SQLException;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.security.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import com.opengamma.util.ArgumentChecker;

/**
 * 
 * 
 * @author pietari
 */
@Transactional
public class HibernateUserManager implements UserManager, UserDetailsService {

  private HibernateTemplate _hibernateTemplate = null;

  public void setSessionFactory(SessionFactory sessionFactory) {
    _hibernateTemplate = new HibernateTemplate(sessionFactory);
  }
  
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
    ArgumentChecker.checkNotNull(username, "User name");
    
    return (User) _hibernateTemplate.execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException,
          SQLException {
        return (User) session.createQuery(
            "from User as a where a.username = :username").setParameter("username",
            username).uniqueResult();
      }
    });
  }
  
  @Override
  public UserGroup getUserGroup(final String name) {
    ArgumentChecker.checkNotNull(name, "Group name");
    
    return (UserGroup) _hibernateTemplate.execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException,
          SQLException {
        UserGroup userGroup = (UserGroup) session.createQuery(
            "from UserGroup as a where a.name = :name").setParameter("name",
                name).uniqueResult();
        return userGroup;
      }
    });
  }
  
  @Override
  public Authority getAuthority(final String authority) {
    ArgumentChecker.checkNotNull(authority, "Authority");
    
    return (Authority) _hibernateTemplate.execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException,
          SQLException {
        return (Authority) session.createQuery(
            "from Authority as a where a.authority = :authority").setParameter("authority",
                authority).uniqueResult();
      }
    });
  }
  
  @Override
  public void addUser(User user) {
    ArgumentChecker.checkNotNull(user, "User");
    _hibernateTemplate.save(user);
  }
  
  @Override
  public void deleteUser(User user) {
    ArgumentChecker.checkNotNull(user, "User");
    _hibernateTemplate.delete(user);
  }
  
  @Override
  public void updateUser(User user) {
    ArgumentChecker.checkNotNull(user, "User");
    _hibernateTemplate.update(user);
  }
  
  @Override
  public void addUserGroup(UserGroup group) {
    ArgumentChecker.checkNotNull(group, "User group");
    _hibernateTemplate.save(group);
  }
  
  @Override
  public void deleteUserGroup(UserGroup group) {
    ArgumentChecker.checkNotNull(group, "User group");
    _hibernateTemplate.delete(group);
  }
  
  @Override
  public void updateUserGroup(UserGroup group) {
    ArgumentChecker.checkNotNull(group, "User group");
    _hibernateTemplate.update(group);
  }
  
  @Override
  public void addAuthority(Authority authority) {
    ArgumentChecker.checkNotNull(authority, "Authority");
    _hibernateTemplate.save(authority);
  }
  
  @Override
  public void deleteAuthority(Authority authority) {
    ArgumentChecker.checkNotNull(authority, "Authority");
    _hibernateTemplate.delete(authority);
  }
  
  @Override
  public void updateAuthority(Authority authority) {
    ArgumentChecker.checkNotNull(authority, "Authority");
    _hibernateTemplate.update(authority);
  }
}
