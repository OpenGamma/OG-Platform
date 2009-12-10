package com.opengamma.financial.security.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.hibernate.SessionFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.security.Security;
import com.opengamma.financial.Currency;
import com.opengamma.financial.security.EquitySecurity;
import com.opengamma.id.DomainSpecificIdentifier;

//import com.opengamma.engine.security.Security;
//import com.opengamma.engine.security.SecurityKey;
//import com.opengamma.engine.security.SecurityMaster;

public class HibernateSecurityMaster {//implements SecurityMaster {

  private HibernateTemplate _hibernateTemplate = null;
  
  public void setSessionFactory(SessionFactory sessionFactory) {
    _hibernateTemplate = new HibernateTemplate(sessionFactory);
  }
  
  public Object getSingleResult(List<?> list) {
    if (list.size() == 1) {
      return list.get(0);
    } else if (list.size() == 0) {
      return null;
    } else {
      throw new OpenGammaRuntimeException("Expecting single row results, got "+list);
    }
  }
  
  public ExchangeBean getOrCreateExchangeBean(String name, String description) {
    List<?> results = _hibernateTemplate.find("from ExchangeBean as e where e.name=?", name);
    ExchangeBean exchange = (ExchangeBean) getSingleResult(results);
    if (exchange == null) {
      exchange = new ExchangeBean(name, description);
      _hibernateTemplate.save(exchange);
    }
    return exchange;
  }
  
  @SuppressWarnings("unchecked")
  public List<ExchangeBean> getExchangeBeans() {
    return _hibernateTemplate.find("from ExchangeBean");
  }
  
  public List<String> getExchanges() {
    List<ExchangeBean> exchangeBeans = getExchangeBeans();
    List<String> exchanges = new ArrayList<String>();
    if (exchangeBeans != null) {
      for (ExchangeBean exchangeBean : exchangeBeans) {
        exchanges.add(exchangeBean.getName());
      }
    }
    return exchanges;
  }
  
  public CurrencyBean getOrCreateCurrencyBean(String name) {
    List<?> results = _hibernateTemplate.find("from CurrencyBean as c where c.name=?", name);
    CurrencyBean currency = (CurrencyBean) getSingleResult(results);
    if (currency == null) {
      currency = new CurrencyBean(name);
      _hibernateTemplate.save(currency);
    }
    return currency;
  }
  
  @SuppressWarnings("unchecked")
  public List<CurrencyBean> getCurrencyBeans() {
    return _hibernateTemplate.find("from CurrencyBean");
  }
  
  public Currency currencyBeanToCurrency(CurrencyBean currencyBean) {
    return Currency.getInstance(currencyBean.getName());
  }
  
  
  public List<Currency> getCurrencies() {
    List<CurrencyBean> currencyBeans = getCurrencyBeans();
    List<Currency> currencies = new ArrayList<Currency>();
    if (currencyBeans != null) {
      for (CurrencyBean currencyBean : currencyBeans) {
        currencies.add(currencyBeanToCurrency(currencyBean));
      }
    }
    return currencies;
  }
  
  // the parameter beans here must be already persisted.
  public EquitySecurityBean getOrCreateEquitySecurityBean(ExchangeBean exchange, String companyName, CurrencyBean currency) {
    List<?> results = _hibernateTemplate.find("from EquitySecurityBean as e where e.exchange=? and e.companyName=? and e.currency=?", new Object[] { exchange, companyName, currency });
    EquitySecurityBean equity = (EquitySecurityBean) getSingleResult(results);
    if (equity == null) {
      equity = new EquitySecurityBean(exchange, companyName, currency);
      _hibernateTemplate.save(equity);
    }
    return equity;
  }
  
  public EquitySecurityBean persistEquitySecurityBean(EquitySecurity equitySecurity) {
    ExchangeBean exchange = getOrCreateExchangeBean(equitySecurity.getExchange(), null);
    CurrencyBean currency = getOrCreateCurrencyBean(equitySecurity.getCurrency().getISOCode());
    EquitySecurityBean equity = getOrCreateEquitySecurityBean(exchange, equitySecurity.getCompanyName(), currency);
    return equity;
  }
  
  public DomainSpecificIdentifierAssociationBean getOrCreateDomainSpecificIdentifierAssociationBean(String domain, String identifier, SecurityBean security) {
    List<?> results = _hibernateTemplate.find("from DomainSpecificIdentifierAssociationBean as a where "+
                                                   "a.domainSpecificIdentifier.domain = ? and "+
                                                   "a.domainSpecificIdentifier.identifier = ? and "+
                                                   "security = ?", new Object[] {domain, identifier, security});
    DomainSpecificIdentifierAssociationBean association = (DomainSpecificIdentifierAssociationBean) getSingleResult(results);
    if (association == null) {
      association = new DomainSpecificIdentifierAssociationBean(security, new DomainSpecificIdentifierBean(domain, identifier));
      _hibernateTemplate.save(association);
    }
    return association;
  }
  
  public void associateDomainSpecificIdentifierWithSecurity(DomainSpecificIdentifier identifier, SecurityBean security) {
    getOrCreateDomainSpecificIdentifierAssociationBean(identifier.getDomain().getDomainName(), identifier.getValue(), security);
  }
  
  public void persistEquitySecurity(EquitySecurity equitySecurity) {
    EquitySecurityBean equity = persistEquitySecurityBean(equitySecurity);
    Collection<DomainSpecificIdentifier> identifiers = equitySecurity.getIdentifiers();
    for (DomainSpecificIdentifier identifier : identifiers) {
      associateDomainSpecificIdentifierWithSecurity(identifier, equity);
    }
  }
  
  public DomainSpecificIdentifier domainSpecificIdentifierBeanToDomainSpecificIdentifier(DomainSpecificIdentifierBean domainSpecificIdentifierBean) {
    return new DomainSpecificIdentifier(domainSpecificIdentifierBean.getDomain(), domainSpecificIdentifierBean.getIdentifier());
  }

  @SuppressWarnings("unchecked")
  public Security getSecurity(final DomainSpecificIdentifier identifier, boolean populateWithOtherIdentifiers) {
    List<?> results = _hibernateTemplate.find("from DomainSpecificIdentifierAssociationBean as a where "+
                                                   "a.domainSpecificIdentifier.domain = ? and "+
                                                   "a.domainSpecificIdentifier.identifier = ?", 
                                                   new Object[] {identifier.getDomain().getDomainName(), 
                                                                 identifier.getValue()});

    DomainSpecificIdentifierAssociationBean association = (DomainSpecificIdentifierAssociationBean) getSingleResult(results);
    if (association != null) {
      SecurityBean security = association.getSecurity();
      final List<DomainSpecificIdentifier> identifiers = new ArrayList<DomainSpecificIdentifier>();
      if (populateWithOtherIdentifiers) {
        List<DomainSpecificIdentifierAssociationBean> otherIdentifiers = _hibernateTemplate.find("from DomainSpecificIdentifierAssociationBean as a where a.security = ?", security);
        for (DomainSpecificIdentifierAssociationBean associationBean : otherIdentifiers) {
          identifiers.add(domainSpecificIdentifierBeanToDomainSpecificIdentifier(associationBean.getDomainSpecificIdentifier()));
        }
      } else {
        identifiers.add(identifier);
      }
      security.accept(new SecurityBeanVisitor<Security>() {
        @Override
        public Security visitEquitySecurityBean(EquitySecurityBean security) {
          EquitySecurity result = new EquitySecurity();
          result.setCompanyName(security.getCompanyName());
          result.setCurrency(currencyBeanToCurrency(security.getCurrency()));
          result.setExchange(security.getExchange().getName());
          result.setTicker(identifier.getValue());
          result.setIdentifiers(identifiers);
          return result;
        }
      });
    }
    return null;
  }

  
//  @Override
//  public Set<String> getAllSecurityTypes() {
//    // TODO Auto-generated method stub
//    return null;
//  }
//
//  @Override
//  public Collection<Security> getSecurities(SecurityKey secKey) {
//    // TODO Auto-generated method stub
//    return null;
//  }
//
//  @Override
//  public Security getSecurity(SecurityKey secKey) {
//    // TODO Auto-generated method stub
//    return null;
//  }
//
//  @Override
//  public Security getSecurity(String identityKey) {
//    // TODO Auto-generated method stub
//    return null;
//  }
}
 
