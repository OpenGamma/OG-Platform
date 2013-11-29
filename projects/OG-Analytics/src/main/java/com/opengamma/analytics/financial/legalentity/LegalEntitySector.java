/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.legalentity;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.opengamma.util.ArgumentChecker;

/**
 * Gets the sector of an {@link LegalEntity}.
 */
public class LegalEntitySector implements LegalEntityMeta<LegalEntity> {
  private final String _sectorName;
  private final String _classificationName;

  protected LegalEntitySector(final String sectorName, final String classificationName) {
    _sectorName = sectorName;
    _classificationName = classificationName;
  }

  public static Builder builder() {
    return new Builder();
  }

  @Override
  public Object getMetaData(final LegalEntity legalEntity) {
    ArgumentChecker.notNull(legalEntity, "obligor");
    if (_sectorName == null && _classificationName == null) {
      return legalEntity.getSector();
    }
    final Sector sector = legalEntity.getSector();
    final Set<Object> selections = new HashSet<>();
    if (_sectorName != null) {
      selections.add(sector.getName());
    }
    if (_classificationName != null) {
      final Map<String, Object> classifications = sector.getClassifications().toMap();
      if (classifications.isEmpty()) {
        throw new IllegalStateException("Sector " + legalEntity.getSector() + " does not contain any classifications");
      }
      final Object classification = classifications.get(_classificationName);
      if (classification != null) {
        selections.add(classification);
      }
    }
    return selections;
  }

  public static class Builder {
    private String _name;
    private String _classificationName;

    protected Builder() {
    }

    public Builder withName(final String name) {
      _name = name;
      return this;
    }

    public Builder withClassificationName(final String classificationName) {
      _classificationName = classificationName;
      return this;
    }

    public LegalEntitySector create() {
      return new LegalEntitySector(_name, _classificationName);
    }
  }
}
