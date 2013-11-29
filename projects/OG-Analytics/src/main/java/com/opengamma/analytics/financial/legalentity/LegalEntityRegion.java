/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.legalentity;

import java.util.HashSet;
import java.util.Set;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;

/**
 * Gets the region of an {@link LegalEntity}.
 */
public class LegalEntityRegion implements LegalEntityMeta<LegalEntity> {
  private final String _name;
  private final String _country;
  private final String _currency;

  protected LegalEntityRegion(final String name, final String country, final String currency) {
    _name = name;
    _country = country;
    _currency = currency;
  }

  public static Builder builder() {
    return new Builder();
  }

  @Override
  public Object getMetaData(final LegalEntity legalEntity) {
    ArgumentChecker.notNull(legalEntity, "obligor");
    if (_name == null && _country == null && _currency == null) {
      return legalEntity.getRegion();
    }
    final Set<Object> selections = new HashSet<>();
    if (_name != null) {
      selections.add(legalEntity.getRegion().getName());
    }
    if (_country != null) {
      final Set<Country> countries = legalEntity.getRegion().getCountries();
      selections.addAll(countries);
    }
    if (_currency != null) {
      final Set<Currency> currencies = legalEntity.getRegion().getCurrencies();
      selections.addAll(currencies);
    }
    return selections;
  }

  public static class Builder {
    private String _name;
    private String _country;
    private String _currency;

    protected Builder() {
    }

    public Builder withName(final String name) {
      _name = name;
      return this;
    }

    public Builder withCountry(final Country country) {
      _country = country.getCode();
      return this;
    }

    public Builder withCountry(final String country) {
      _country = country;
      return this;
    }

    public Builder withCurrency(final Currency currency) {
      _currency = currency.getCode();
      return this;
    }

    public Builder withCurrency(final String currency) {
      _currency = currency;
      return this;
    }

    public LegalEntityRegion create() {
      return new LegalEntityRegion(_name, _country, _currency);
    }
  }
}
