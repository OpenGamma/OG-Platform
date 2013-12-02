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
 * Gets the region or sub-fields of the region of an {@link LegalEntity}.
 */
//TODO bean for the top level class
public class LegalEntityRegion implements LegalEntityMeta<LegalEntity> {
  private final boolean _useName;
  private final boolean _useCountry;
  private final Set<Country> _countries;
  private final boolean _useCurrency;
  private final Set<Currency> _currencies;

  protected LegalEntityRegion(final boolean useName, final boolean useCountries, final Set<Country> countries,
      final boolean useCurrencies, final Set<Currency> currencies) {
    _useName = useName;
    _useCountry = useCountries;
    _countries = countries;
    _useCurrency = useCurrencies;
    _currencies = currencies;
  }

  public static Builder builder() {
    return new Builder();
  }

  @Override
  public Object getMetaData(final LegalEntity legalEntity) {
    ArgumentChecker.notNull(legalEntity, "legal entity");
    final Region region = legalEntity.getRegion();
    if (region == null) {
      throw new IllegalStateException("Region for this legal entity " + legalEntity + " was null");
    }
    if (!(_useName || _useCountry || _useCurrency)) {
      return region;
    }
    final Set<Object> selections = new HashSet<>();
    if (_useName) {
      selections.add(region.getName());
    }
    if (_useCountry) {
      final Set<Country> countries = region.getCountries();
      if (_countries.isEmpty()) {
        selections.addAll(countries);
      } else if (countries.containsAll(_countries)) {
        selections.addAll(_countries);
      } else {
        throw new IllegalStateException();
      }
    }
    if (_useCurrency) {
      final Set<Currency> currencies = region.getCurrencies();
      if (_currencies.isEmpty()) {
        selections.addAll(currencies);
      } else if (currencies.containsAll(_currencies)) {
        selections.addAll(_currencies);
      } else {
        throw new IllegalStateException();
      }
    }
    return selections;
  }

  public static class Builder {
    private boolean _name;
    private boolean _country;
    private final Set<Country> _countriesToUse;
    private boolean _currency;
    private final Set<Currency> _currenciesToUse;

    protected Builder() {
      _countriesToUse = new HashSet<>();
      _currenciesToUse = new HashSet<>();
    }

    public Builder useName() {
      _name = true;
      return this;
    }

    public Builder useCountries() {
      _country = true;
      return this;
    }

    public Builder useCountry(final String country) {
      _country = true;
      _countriesToUse.add(Country.of(country));
      return this;
    }

    public Builder useCurrencies() {
      _currency = true;
      return this;
    }

    public Builder useCurrency(final String currency) {
      _currency = true;
      _currenciesToUse.add(Currency.of(currency));
      return this;
    }

    public LegalEntityRegion create() {
      return new LegalEntityRegion(_name, _country, _countriesToUse, _currency, _currenciesToUse);
    }
  }
}
