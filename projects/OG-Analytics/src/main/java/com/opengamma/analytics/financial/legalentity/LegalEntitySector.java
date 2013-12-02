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
  private final boolean _useSectorName;
  private final boolean _useClassificationName;
  private final Set<String> _classifications;

  protected LegalEntitySector(final boolean useSectorName, final boolean useClassificationName, final Set<String> classifications) {
    _useSectorName = useSectorName;
    _useClassificationName = useClassificationName;
    _classifications = classifications;
  }

  public static Builder builder() {
    return new Builder();
  }

  @Override
  public Object getMetaData(final LegalEntity legalEntity) {
    ArgumentChecker.notNull(legalEntity, "obligor");
    if (!(_useSectorName || _useClassificationName)) {
      return legalEntity.getSector();
    }
    final Sector sector = legalEntity.getSector();
    final Set<Object> selections = new HashSet<>();
    if (_useSectorName) {
      selections.add(sector.getName());
    }
    int classificationCount = 0;
    if (_useClassificationName) {
      final Map<String, Object> classifications = sector.getClassifications().toMap();
      if (classifications.isEmpty()) {
        throw new IllegalStateException("Sector " + legalEntity.getSector() + " does not contain any classifications");
      }
      for (final Map.Entry<String, Object> entry : classifications.entrySet()) {
        if (_classifications.contains(entry.getKey())) {
          selections.add(entry.getValue());
          classificationCount++;
        }
      }
    }
    if (classificationCount != _classifications.size()) {
      throw new IllegalStateException("Classifications " + sector.getClassifications() + " do not contain matches for " + _classifications);
    }
    return selections;
  }

  public static class Builder {
    private boolean _name;
    private boolean _classificationName;
    private final Set<String> _classificationsToUse;

    protected Builder() {
      _classificationsToUse = new HashSet<>();
    }

    public Builder useName() {
      _name = true;
      return this;
    }

    public Builder useClassificationName() {
      _classificationName = true;
      return this;
    }

    public Builder useClassificationForType(final String classificationToUse) {
      _classificationName = true;
      _classificationsToUse.add(classificationToUse);
      return this;
    }

    public LegalEntitySector create() {
      return new LegalEntitySector(_name, _classificationName, _classificationsToUse);
    }
  }
}
