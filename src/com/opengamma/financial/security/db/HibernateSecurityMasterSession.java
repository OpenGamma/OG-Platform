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

import com.opengamma.financial.security.BondSecurity;
import com.opengamma.financial.security.EquityOptionSecurity;
import com.opengamma.financial.security.EquitySecurity;
import com.opengamma.financial.security.OptionType;
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

  private Set<String> getListOfDomains(
      Collection<DomainSpecificIdentifier> identifiers) {
    Set<String> domains = new HashSet<String>();
    for (DomainSpecificIdentifier id : identifiers) {
      domains.add(id.getDomain().getDomainName());
    }
    return domains;
  }

  private Set<String> getListOfValuesForDomain(String domain,
      Collection<DomainSpecificIdentifier> identifiers) {
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
  /* package */ExchangeBean getOrCreateExchangeBean(String name,
      String description) {
    Query query = getSession().getNamedQuery("ExchangeBean.one");
    query.setString("name", name);
    ExchangeBean exchange = (ExchangeBean) query.uniqueResult();
    if (exchange == null) {
      exchange = new ExchangeBean(name, description);
      Long id = (Long) getSession().save(exchange);
      getSession().flush();
      exchange.setId(id);
    } else {
      if (description != null) {
        if (exchange.getDescription () == null) {
          exchange.setDescription (description);
          getSession ().saveOrUpdate (exchange);
          getSession ().flush ();
        }
      }
    }
    return exchange;
  }

  @SuppressWarnings("unchecked")
  /* package */List<ExchangeBean> getExchangeBeans() {
    Query query = getSession().getNamedQuery("ExchangeBean.all");
    return query.list();
  }

  // Currencies
  /* package */CurrencyBean getOrCreateCurrencyBean(String name) {
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
  /* package */List<CurrencyBean> getCurrencyBeans() {
    Query query = getSession().getNamedQuery("CurrencyBean.all");
    return query.list();
  }

  // GICS codes
  /* package */GICSCodeBean getOrCreateGICSCodeBean(final String name,
      final String description) {
    Query query = getSession().getNamedQuery("GICSCodeBean.one");
    query.setString("name", name);
    GICSCodeBean gicsCode = (GICSCodeBean) query.uniqueResult();
    if (gicsCode == null) {
      gicsCode = new GICSCodeBean(name, description);
      Long id = (Long) getSession().save(gicsCode);
      getSession().flush();
      gicsCode.setId(id);
    } else {
      if (description != null) {
        if (gicsCode.getDescription () == null) {
          gicsCode.setDescription (description);
          getSession ().saveOrUpdate (gicsCode);
          getSession ().flush ();
        }
      }
    }
    return gicsCode;
  }

  @SuppressWarnings("unchecked")
  /* package */List<GICSCodeBean> getGICSCodeBeans() {
    Query query = getSession().getNamedQuery("GICSCodeBean.all");
    return query.list();
  }
  
  // Daycount conventions
  /* package */ DayCountBean getOrCreateDayCountBean (final String convention) {
    final Query query = getSession ().getNamedQuery ("DayCountBean.one");
    query.setString ("name", convention);
    DayCountBean bean = (DayCountBean)query.uniqueResult ();
    if (bean == null) {
      bean = new DayCountBean (convention);
      Long id = (Long)getSession ().save (bean);
      getSession ().flush ();
      bean.setId (id);
    }
    return bean;
  }
  
  @SuppressWarnings ("unchecked")
  /* package */ List<DayCountBean> getDayCountBeans () {
    final Query query = getSession ().getNamedQuery ("DayCountBean.all");
    return query.list ();
  }
  
  // Business day conventions
  /* package */ BusinessDayConventionBean getOrCreateBusinessDayConventionBean (final String convention) {
    final Query query = getSession ().getNamedQuery ("BusinessDayConventionBean.one");
    query.setString ("name", convention);
    BusinessDayConventionBean bean = (BusinessDayConventionBean)query.uniqueResult ();
    if (bean == null) {
      bean = new BusinessDayConventionBean (convention);
      Long id = (Long)getSession ().save (bean);
      getSession ().flush ();
      bean.setId (id);
    }
    return bean;
  }

  // Frequencies
  /* package */ FrequencyBean getOrCreateFrequencyBean (final String convention) {
    final Query query = getSession ().getNamedQuery ("FrequencyBean.one");
    query.setString ("name", convention);
    FrequencyBean bean = (FrequencyBean)query.uniqueResult ();
    if (bean == null) {
      bean = new FrequencyBean (convention);
      Long id = (Long)getSession ().save (bean);
      getSession ().flush ();
      bean.setId (id);
    }
    return bean;
  }

  @SuppressWarnings ("unchecked")
  /* package */ List<FrequencyBean> getFrequencyBeans () {
    final Query query = getSession ().getNamedQuery ("FrequencyBean.all");
    return query.list ();
  }

  @SuppressWarnings ("unchecked")
  /* package */ List<BusinessDayConventionBean> getBusinessDayConventionBeans () {
    final Query query = getSession ().getNamedQuery ("BusinessDayConventionBean.all");
    return query.list ();
  }

  // Domain specific ID / Security specific methods
  
  private DomainSpecificIdentifierAssociationBean createDomainSpecificIdentifierAssociationBean (Date now, String domain, String identifier, SecurityBean security) {
    final Transaction transaction = getSession ().beginTransaction ();
    final DomainSpecificIdentifierAssociationBean association = new DomainSpecificIdentifierAssociationBean(security, new DomainSpecificIdentifierBean (domain, identifier));
    Query query = getSession ().getNamedQuery ("DomainSpecificIdentifierAssociationBean.one.previousAssociation");
    query.setString ("domain", domain);
    query.setString ("identifier", identifier);
    query.setDate ("now", now);
    DomainSpecificIdentifierAssociationBean other = (DomainSpecificIdentifierAssociationBean)query.uniqueResult ();
    if (other != null) {
      association.setValidStartDate (other.getValidEndDate ());
    }
    query = getSession ().getNamedQuery ("DomainSpecificIdentifierAssociationBean.one.nextAssociation");
    query.setString ("domain", domain);
    query.setString ("identifier", identifier);
    query.setDate ("now", now);
    other = (DomainSpecificIdentifierAssociationBean)query.uniqueResult ();
    if (other != null) {
      association.setValidEndDate (other.getValidEndDate ());
    }
    Long id = (Long) getSession().save(association);
    association.setId(id);
    transaction.commit ();
    getSession().flush();
    return association;
  }
  
  /* package */DomainSpecificIdentifierAssociationBean getCreateOrUpdateDomainSpecificIdentifierAssociationBean(
      Date now, String domain, String identifier, SecurityBean security) {
    System.err.println ("DSidA: " + now + ", " + domain + ", " + identifier + ", " + security);
    Query query = getSession().getNamedQuery(
        "DomainSpecificIdentifierAssociationBean.one.byDateDomainIdentifier");
    query.setString("domain", domain);
    query.setString("identifier", identifier);
    query.setDate("now", now);
    DomainSpecificIdentifierAssociationBean association = (DomainSpecificIdentifierAssociationBean) query.uniqueResult();
    if (association == null) {
      association = createDomainSpecificIdentifierAssociationBean (now, domain, identifier, security);
    } else {
      if (association.getSecurity().getId().equals(security.getId())) {
        // we're okay, it's already there
      } else {
        // terminate the previous record, and create a new one
        association.setValidEndDate (now);
        getSession ().update (association);
        getSession ().flush ();
        association = createDomainSpecificIdentifierAssociationBean (now, domain, identifier, security);
      }
    }
    System.err.println (association);
    return association;
  }

  // only for testing.
  @SuppressWarnings ("unchecked")
  /* package */List<DomainSpecificIdentifierAssociationBean> getAllAssociations() {
    Query query = getSession().createQuery(
        "from DomainSpecificIdentifierAssociationBean as d");
    return query.list();
  }

  /* package */ void associateOrUpdateDomainSpecificIdentifierWithSecurity(Date now,
      DomainSpecificIdentifier identifier, SecurityBean security) {
    getCreateOrUpdateDomainSpecificIdentifierAssociationBean(now, identifier
        .getDomain().getDomainName(), identifier.getValue(), security
        .getFirstVersion());
  }

  // Generic Securities

  /* package */SecurityBean getSecurityBean(Date now,
      final DomainSpecificIdentifier identifier) {
    Query query = getSession().getNamedQuery(
        "SecurityBean.one.byDateDomainIdentifier");
    query.setString("domain", identifier.getDomain().getDomainName());
    query.setString("identifier", identifier.getValue());
    query.setDate("now", now);
    SecurityBean security = (SecurityBean) query.uniqueResult();
    return security;
  }

  /* package */SecurityBean getSecurityBean(Date now,
      Collection<DomainSpecificIdentifier> identifiers) {
    Set<String> domains = getListOfDomains(identifiers);
    for (String domain : domains) {
      final Set<String> ids = getListOfValuesForDomain(domain, identifiers);
      Query query = getSession().getNamedQuery(
          "SecurityBean.one.byDateDomainIdentifiers");
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

  EquitySecurityBean persistEquitySecurityBean(Date now,
      EquitySecurity equitySecurity) {
    ExchangeBean exchange = getOrCreateExchangeBean(equitySecurity
        .getExchange(), "");
    CurrencyBean currency = getOrCreateCurrencyBean(equitySecurity
        .getCurrency().getISOCode());
    GICSCodeBean gicsCode = getOrCreateGICSCodeBean(equitySecurity
        .getGICSCode().toString(), "");
    EquitySecurityBean equity = createEquitySecurityBean(now, false, now, null,
        null, exchange, equitySecurity.getCompanyName(), currency, gicsCode);
    return equity;
  }

  // /*package*/ EquitySecurityBean createEquitySecurityBean(ExchangeBean
  // exchange, String companyName, CurrencyBean currency) {
  // Date effectiveDateTime = new Date();
  // return createEquitySecurityBean(effectiveDateTime, false,
  // effectiveDateTime, null, null, exchange, companyName, currency);
  // }

  // If firstVersion is null, then the bean is linked to itself.
  /* package */EquitySecurityBean createEquitySecurityBean(
      Date effectiveDateTime, boolean deleted, Date lastModified,
      String modifiedBy, EquitySecurityBean firstVersion,
      ExchangeBean exchange, String companyName, CurrencyBean currency,
      GICSCodeBean gicsCode) {
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
    equity.setGICSCode(gicsCode);
    if (firstVersion == null) { // we need to link it to itself as a parent.
      // TODO: TRANSACTION START - CAN WE GET AWAY WITH NOT USING THE
      // TRANSACTION MANAGER? NO, REWORK THIS
      Transaction transaction = getSession().beginTransaction();
      transaction.begin();
      Long id = (Long) getSession().save(equity);
      equity.setId(id);
      equity.setFirstVersion(equity);
      getSession().update(equity);
      transaction.commit();
      getSession().flush();
      // TODO: TRANSACTION END
    } else {
      Long id = (Long) getSession().save(equity);
      equity.setId(id);
      getSession().flush();
    }
    Hibernate.initialize(equity);
    return equity;
  }

  // Internal query methods for equities
  @SuppressWarnings("unchecked")
  /* package */List<EquitySecurityBean> getEquitySecurityBeans() {
    Query query = getSession().getNamedQuery("EquitySecurityBean.all");
    return query.list();
  }

  @SuppressWarnings("unchecked")
  /* package */List<EquitySecurityBean> getAllVersionsOfEquitySecurityBean(
      EquitySecurityBean firstVersion) {
    Query query = getSession().getNamedQuery(
        "EquitySecurityBean.many.allVersionsByFirstVersion");
    query.setParameter("firstVersion", firstVersion);
    return query.list();
  }

  /* package */EquitySecurityBean getCurrentEquitySecurityBean(Date now,
      ExchangeBean exchange, String companyName, CurrencyBean currency) {
    Query query = getSession().getNamedQuery(
        "EquitySecurityBean.one.byExchangeCompanyNameCurrencyDate");
    query.setDate("now", now);
    query.setParameter("exchange", exchange);
    query.setString("companyName", companyName);
    query.setParameter("currency", currency);
    return (EquitySecurityBean) query.uniqueResult();
  }

  /* package */EquitySecurityBean getCurrentEquitySecurityBean(Date now,
      EquitySecurityBean firstVersion) {
    Query query = getSession().getNamedQuery(
        "EquitySecurityBean.one.byFirstVersionDate");
    query.setParameter("firstVersion", firstVersion);
    query.setDate("now", now);
    return (EquitySecurityBean) query.uniqueResult();
  }

  /* package */EquitySecurityBean getCurrentLiveEquitySecurityBean(Date now,
      ExchangeBean exchange, String companyName, CurrencyBean currency) {
    Query query = getSession().getNamedQuery(
        "EquitySecurityBean.one.liveByExchangeCompanyNameCurrencyDate");
    query.setParameter("exchange", exchange);
    query.setString("companyName", companyName);
    query.setParameter("currency", currency);
    query.setDate("now", now);
    return (EquitySecurityBean) query.uniqueResult();
  }

  /* package */EquitySecurityBean getCurrentLiveEquitySecurityBean(Date now,
      EquitySecurityBean firstVersion) {
    Query query = getSession().getNamedQuery(
        "EquitySecurityBean.one.liveByFirstVersionDate");
    query.setParameter("firstVersion", firstVersion);
    query.setDate("now", now);
    return (EquitySecurityBean) query.uniqueResult();
  }

  // Equity options

  @SuppressWarnings("unchecked")
  /* package */List<EquityOptionSecurityBean> getEquityOptionSecurityBeans() {
    Query query = getSession().getNamedQuery("EquityOptionSecurityBean.all");
    return query.list();
  }
  
  /* package */EquityOptionSecurityBean persistEquityOptionSecurityBean(
      final Date now, final EquityOptionSecurity equityOptionSecurity) {
    final EquityOptionType equityOptionType = EquityOptionType.identify (equityOptionSecurity);
    final Date expiry = new Date(equityOptionSecurity.getExpiry().toInstant()
        .toEpochMillisLong());
    final CurrencyBean currency = getOrCreateCurrencyBean(equityOptionSecurity
        .getCurrency().getISOCode());
    final ExchangeBean exchange = getOrCreateExchangeBean(equityOptionSecurity
        .getExchange(), "");
    return createEquityOptionSecurityBean(now, false, now, null, null,
        equityOptionType, equityOptionSecurity.getOptionType (), equityOptionSecurity.getStrike(), expiry,
        equityOptionSecurity.getUnderlyingIdentityKey(), currency, exchange);
  }

  /* package */EquityOptionSecurityBean createEquityOptionSecurityBean(
      Date effectiveDateTime, boolean deleted, Date lastModified,
      String modifiedBy, EquityOptionSecurityBean firstVersion,
      EquityOptionType equityOptionType, OptionType optionType,
      double strike, Date expiry, String underlyingIdentityKey,
      CurrencyBean currency, ExchangeBean exchange) {
    final EquityOptionSecurityBean equityOption = new EquityOptionSecurityBean();
    // base properties
    equityOption.setEffectiveDateTime(effectiveDateTime);
    equityOption.setDeleted(deleted);
    equityOption.setLastModifiedDateTime(lastModified);
    equityOption.setLastModifiedBy(modifiedBy);
    // first version
    equityOption.setFirstVersion(firstVersion);
    // equity option properties
    equityOption.setEquityOptionType(equityOptionType);
    equityOption.setOptionType(optionType);
    equityOption.setStrike(strike);
    equityOption.setExpiry(expiry);
    equityOption.setUnderlyingIdentityKey(underlyingIdentityKey);
    equityOption.setCurrency(currency);
    equityOption.setExchange(exchange);
    if (firstVersion == null) { // we need to link it to itself as a parent.
      // TODO: TRANSACTION START - CAN WE GET AWAY WITH NOT USING THE
      // TRANSACTION MANAGER? NO, REWORK THIS
      Transaction transaction = getSession().beginTransaction();
      transaction.begin();
      Long id = (Long) getSession().save(equityOption);
      equityOption.setId(id);
      equityOption.setFirstVersion(equityOption);
      getSession().update(equityOption);
      transaction.commit();
      getSession().flush();
      // TODO: TRANSACTION END
    } else {
      Long id = (Long) getSession().save(equityOption);
      equityOption.setId(id);
      getSession().flush();
    }
    Hibernate.initialize(equityOption);
    return equityOption;
  }
  
  // Bonds
  
  /* package */ BondSecurityBean persistBondSecurityBean (final Date now, final BondSecurity bondSecurity) {
    final FrequencyBean frequency = getOrCreateFrequencyBean (bondSecurity.getFrequency ().getConventionName ());
    final CurrencyBean currency = getOrCreateCurrencyBean (bondSecurity.getCurrency ().getISOCode ());
    final DayCountBean dayCountConvention = getOrCreateDayCountBean (bondSecurity.getDayCountConvention ().getConventionName ());
    final BusinessDayConventionBean businessDayConvention = getOrCreateBusinessDayConventionBean (bondSecurity.getBusinessDayConvention ().getConventionName ());
    return createBondSecurityBean (now, false, now, null, null, BondType.identify (bondSecurity), new Date (bondSecurity.getMaturity ().toInstant ().toEpochMillisLong ()), bondSecurity.getCoupon (), frequency, bondSecurity.getCountry (), bondSecurity.getCreditRating (), currency, bondSecurity.getIssuer (), dayCountConvention, businessDayConvention);
  }
  
  /* package */ BondSecurityBean createBondSecurityBean (final Date effectiveDateTime, boolean deleted, Date lastModified, String modifiedBy, BondSecurityBean firstVersion,
      BondType bondType,
      Date maturity,
      double coupon,
      FrequencyBean frequency,
      String country,
      String creditRating,
      CurrencyBean currency,
      String issuer,
      DayCountBean dayCountConvention,
      BusinessDayConventionBean businessDayConvention) {
    final BondSecurityBean bond = new BondSecurityBean();
    // base properties
    bond.setEffectiveDateTime(effectiveDateTime);
    bond.setDeleted(deleted);
    bond.setLastModifiedDateTime(lastModified);
    bond.setLastModifiedBy(modifiedBy);
    // first version
    bond.setFirstVersion(firstVersion);
    // bond properties
    bond.setBondType (bondType);
    bond.setMaturity (maturity);
    bond.setCoupon (coupon);
    bond.setFrequency (frequency);
    bond.setCountry (country);
    bond.setCreditRating (creditRating);
    bond.setCurrency (currency);
    bond.setIssuer (issuer);
    bond.setDayCountConvention (dayCountConvention);
    bond.setBusinessDayConvention (businessDayConvention);
    if (firstVersion == null) { // we need to link it to itself as a parent.
      // TODO: TRANSACTION START - CAN WE GET AWAY WITH NOT USING THE
      // TRANSACTION MANAGER? NO, REWORK THIS
      Transaction transaction = getSession().beginTransaction();
      transaction.begin();
      Long id = (Long) getSession().save(bond);
      bond.setId(id);
      bond.setFirstVersion(bond);
      getSession().update(bond);
      transaction.commit();
      getSession().flush();
      // TODO: TRANSACTION END
    } else {
      Long id = (Long) getSession().save(bond);
      bond.setId(id);
      getSession().flush();
    }
    Hibernate.initialize(bond);
    return bond;
  }
  
}
