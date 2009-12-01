package com.opengamma.financial.security.db;

import java.util.Collection;
import java.util.Set;

import org.hibernate.SessionFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.opengamma.engine.security.Security;
import com.opengamma.engine.security.SecurityKey;
import com.opengamma.engine.security.SecurityMaster;

public class HibernateSecurityMaster implements SecurityMaster {

  private HibernateTemplate _hibernateTemplate = null;
  
  public void setSessionFactory(SessionFactory sessionFactory) {
    _hibernateTemplate = new HibernateTemplate(sessionFactory);
  }
  
  @Override
  public Set<String> getAllSecurityTypes() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Collection<Security> getSecurities(SecurityKey secKey) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Security getSecurity(SecurityKey secKey) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Security getSecurity(String identityKey) {
    // TODO Auto-generated method stub
    return null;
  }
}
 
