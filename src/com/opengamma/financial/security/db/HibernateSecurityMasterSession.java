package com.opengamma.financial.security.db;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.security.EquitySecurity;
import com.opengamma.id.DomainSpecificIdentifier;

public class HibernateSecurityMasterSession {
  private Session _session;

  protected Session getSession() {
    return _session;
  }

  public HibernateSecurityMasterSession(Session session) {
    _session = session;
  }
  
  // UTILITY METHODS
  
  private Set<String> getListOfDomains(Collection<DomainSpecificIdentifier> identifiers) {
    Set<String> domains = new HashSet<String>();
    for (DomainSpecificIdentifier id : identifiers) {
      domains.add(id.getDomain().getDomainName());
    }
    return domains;
  }
  
  private Set<String> getListOfValuesForDomain(String domain, Collection<DomainSpecificIdentifier> identifiers) {
    Set<String> values = new HashSet<String>();
    for (DomainSpecificIdentifier id : identifiers) {
      if (id.getDomain().getDomainName().equals(domain)) {
        values.add(id.getValue());
      }
    }
    return values;
  }
  
  // SESSION LEVEL METHODS
  
  // Exchanges
  /*package*/ ExchangeBean getOrCreateExchangeBean(String name, String description) {
    Query query = getSession().getNamedQuery("ExchangeBean.one");
    query.setString("name", name);
    query.setString("description", description);
    ExchangeBean exchange = (ExchangeBean) query.uniqueResult();
    if (exchange == null) {
      exchange = new ExchangeBean(name, description);
      Long id = (Long)getSession().save(exchange);
      getSession().flush();
      exchange.setId(id);
    }
    return exchange;
  }

  
  @SuppressWarnings("unchecked")
  /*package*/ List<ExchangeBean> getExchangeBeans() {
    Query query = getSession().getNamedQuery("ExchangeBean.all");
    return query.list();
  }
  
  // Currencies
  /*package*/ CurrencyBean getOrCreateCurrencyBean(String name) {
    Query query = getSession().getNamedQuery("CurrencyBean.one");
    query.setString("name", name);
    CurrencyBean currency = (CurrencyBean) query.uniqueResult();
    if (currency == null) {
      currency = new CurrencyBean(name);
      Long id = (Long) getSession().save(currency);
      getSession().flush();
      currency.setId(id);
    }
    return currency;
  }
  
  @SuppressWarnings("unchecked")
  /*package*/ List<CurrencyBean> getCurrencyBeans() {
    Query query = getSession().getNamedQuery("CurrencyBean.all");
    return query.list();
  }
  

  
  // Domain specific ID / Security specific methods
  /*package*/ DomainSpecificIdentifierAssociationBean getOrCreateDomainSpecificIdentifierAssociationBean(Date now, String domain, String identifier, SecurityBean security) {
    Query query = getSession().getNamedQuery("DomainSpecificIdentifierAssociationBean.one.byDateDomainIdentifierSecurity");
    query.setString("domain", domain);
    query.setString("identifier", identifier);
    query.setParameter("security", security);
    query.setDate ("now", now);
    DomainSpecificIdentifierAssociationBean association = (DomainSpecificIdentifierAssociationBean) query.uniqueResult();
    if (association == null) {
      association = new DomainSpecificIdentifierAssociationBean(security, new DomainSpecificIdentifierBean(domain, identifier));
      Long id = (Long) getSession().save(association);
      association.setId(id);
      getSession().flush();
    }
    return association;
  }
  
  /*package*/ DomainSpecificIdentifierAssociationBean getCreateOrUpdateDomainSpecificIdentifierAssociationBean(Date now, String domain, String identifier, SecurityBean security) {
    Query query = getSession().getNamedQuery("DomainSpecificIdentifierAssociationBean.one.byDateDomainIdentifier");
    query.setString("domain", domain);
    query.setString("identifier", identifier);
    query.setDate ("now", now);
    DomainSpecificIdentifierAssociationBean association = (DomainSpecificIdentifierAssociationBean) query.uniqueResult();
    if (association == null) {
      association = new DomainSpecificIdentifierAssociationBean(security, new DomainSpecificIdentifierBean(domain, identifier));
      Long id = (Long) getSession().save(association);
      association.setId(id);
      getSession().flush();
    } else {
      if (association.getSecurity().getId().equals(security.getId())) {
        // we're okay, it's already there
      } else {
        association.setSecurity(security);
        getSession().saveOrUpdate(association);
        getSession().flush();
      }
    }
    return association;
  }  
  
  // only for testing.
  /*package*/List<DomainSpecificIdentifierAssociationBean> getAllAssociations() {
    Query query = getSession().createQuery("from DomainSpecificIdentifierAssociationBean as d");
    return query.list();
  }
  
  void associateOrUpdateDomainSpecificIdentifierWithSecurity(Date now, DomainSpecificIdentifier identifier, SecurityBean security) {
    getCreateOrUpdateDomainSpecificIdentifierAssociationBean(now, identifier.getDomain().getDomainName(), identifier.getValue(), security.getFirstVersion());
  }
  
  // Generic Securities
  
  /*package*/ SecurityBean getSecurityBean(Date now, final DomainSpecificIdentifier identifier) {
    Query query = getSession().getNamedQuery("SecurityBean.one.byDateDomainIdentifier");
    query.setString("domain", identifier.getDomain().getDomainName());
    query.setString("identifier", identifier.getValue());
    query.setDate("now", now);
    SecurityBean security = (SecurityBean) query.uniqueResult();
    return security;
  }
  
  /*package*/ SecurityBean getSecurityBean(Date now, Collection<DomainSpecificIdentifier> identifiers) {
    Set<String> domains = getListOfDomains(identifiers);
    for (String domain : domains) {
      final Set<String> ids = getListOfValuesForDomain(domain, identifiers);
      Query query = getSession().getNamedQuery("SecurityBean.one.byDateDomainIdentifiers");
      query.setString("domain", domain);
      query.setParameterList("identifiers", ids);
      query.setDate("now", now);
      SecurityBean security = (SecurityBean) query.uniqueResult();
      if (security != null) {
        return security;
      }
    }
    return null; // none of the dsid's matched.
  }
  
  // Equities
  
  EquitySecurityBean persistEquitySecurityBean(Date now, EquitySecurity equitySecurity) {
    ExchangeBean exchange = getOrCreateExchangeBean(equitySecurity.getExchange(), "");
    CurrencyBean currency = getOrCreateCurrencyBean(equitySecurity.getCurrency().getISOCode());
    EquitySecurityBean equity = createEquitySecurityBean(now, false, now, null, null, exchange, equitySecurity.getCompanyName(), currency);
    return equity;
  }
  
//  /*package*/ EquitySecurityBean createEquitySecurityBean(ExchangeBean exchange, String companyName, CurrencyBean currency) {
//    Date effectiveDateTime = new Date();
//    return createEquitySecurityBean(effectiveDateTime, false, effectiveDateTime, null, null, exchange, companyName, currency);
//  }
  

  
  // If firstVersion is null, then the bean is linked to itself.
  /*package*/ EquitySecurityBean createEquitySecurityBean(Date effectiveDateTime, boolean deleted, Date lastModified, String modifiedBy, EquitySecurityBean firstVersion,
                                                          ExchangeBean exchange, String companyName, CurrencyBean currency) {
    final EquitySecurityBean equity = new EquitySecurityBean();
    // base properties
    equity.setEffectiveDateTime(effectiveDateTime);
    equity.setDeleted(deleted);
    equity.setLastModifiedDateTime(lastModified);
    equity.setLastModifiedBy(modifiedBy);
    // first version
    equity.setFirstVersion(firstVersion);
    // equity properties
    equity.setExchange(exchange);
    equity.setCompanyName(companyName);
    equity.setCurrency(currency);
    if (firstVersion == null) { // we need to link it to itself as a parent.
      // TODO: TRANSACTION START - CAN WE GET AWAY WITH NOT USING THE TRANSACTION MANAGER? NO, REWORK THIS
      Transaction transaction = getSession().beginTransaction();
      transaction.begin();
      Long id = (Long)getSession().save(equity);
      equity.setId(id);
      equity.setFirstVersion(equity);
      getSession().update(equity);
      transaction.commit();
      getSession().flush();
      // TODO: TRANSACTION END          
    } else {
      Long id = (Long)getSession().save(equity);
      equity.setId(id);
      getSession().flush();
    }
    Hibernate.initialize(equity);
    return equity;
  }

  //Internal query methods for equities
  @SuppressWarnings("unchecked")
  /*package*/ List<EquitySecurityBean> getEquitySecurityBeans() {
    Query query = getSession().getNamedQuery("EquitySecurityBean.all");
    return query.list();
  }  
  
  @SuppressWarnings("unchecked")
  /*package*/ List<EquitySecurityBean> getAllVersionsOfEquitySecurityBean(EquitySecurityBean firstVersion) {
    Query query = getSession().getNamedQuery("EquitySecurityBean.many.allVersionsByFirstVersion");
    query.setParameter("firstVersion", firstVersion);
    return query.list();
  }
  
  /*package*/ EquitySecurityBean getCurrentEquitySecurityBean(Date now, ExchangeBean exchange, String companyName, CurrencyBean currency) {
    Query query = getSession().getNamedQuery("EquitySecurityBean.one.byExchangeCompanyNameCurrencyDate");
    query.setDate("now", now);
    query.setParameter("exchange", exchange);
    query.setString("companyName", companyName);
    query.setParameter("currency", currency);
    return (EquitySecurityBean) query.uniqueResult();
  }
  
  /*package*/ EquitySecurityBean getCurrentEquitySecurityBean(Date now, EquitySecurityBean firstVersion) {
    Query query = getSession().getNamedQuery("EquitySecurityBean.one.byFirstVersionDate");
    query.setParameter("firstVersion", firstVersion);
    query.setDate("now", now);
    return (EquitySecurityBean) query.uniqueResult();
  }
  
  /*package*/ EquitySecurityBean getCurrentLiveEquitySecurityBean(Date now, ExchangeBean exchange, String companyName, CurrencyBean currency) {
    Query query = getSession().getNamedQuery("EquitySecurityBean.one.liveByExchangeCompanyNameCurrencyDate");
    query.setParameter("exchange", exchange);
    query.setString("companyName", companyName);
    query.setParameter("currency", currency);
    query.setDate("now", now);
    return (EquitySecurityBean) query.uniqueResult();
  }
  
  /*package*/ EquitySecurityBean getCurrentLiveEquitySecurityBean(Date now, EquitySecurityBean firstVersion) {
    Query query = getSession().getNamedQuery("EquitySecurityBean.one.liveByFirstVersionDate");
    query.setParameter("firstVersion", firstVersion);
    query.setDate("now", now);
    return (EquitySecurityBean) query.uniqueResult();
  }
}
 
