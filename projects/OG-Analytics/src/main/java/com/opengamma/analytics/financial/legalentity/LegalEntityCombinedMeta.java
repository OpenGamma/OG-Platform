/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.legalentity;

import java.util.HashSet;
import java.util.Set;

import com.opengamma.util.ArgumentChecker;

/**
 * Combines a number of {@link LegalEntityMeta}s.
 */
public class LegalEntityCombinedMeta implements LegalEntityMeta<LegalEntity> {
  /** The metas to use */
  private final Set<LegalEntityMeta<LegalEntity>> _metasToUse;

  /**
   * @param metasToUse The metas, not null
   */
  protected LegalEntityCombinedMeta(final Set<LegalEntityMeta<LegalEntity>> metasToUse) {
    ArgumentChecker.notNull(metasToUse, "metas");
    _metasToUse = metasToUse;
  }

  /**
   * Builder for this class.
   * @return The builder.
   */
  public static Builder builder() {
    return new Builder();
  }

  @Override
  public Object getMetaData(final LegalEntity legalEntity) {
    ArgumentChecker.notNull(legalEntity, "legal entity");
    final Set<Object> selections = new HashSet<>();
    for (final LegalEntityMeta<LegalEntity> meta : _metasToUse) {
      selections.add(meta.getMetaData(legalEntity));
    }
    return selections;
  }

  /**
   * A builder for this class.
   */
  public static class Builder {
    /** The set of metas */
    private final Set<LegalEntityMeta<LegalEntity>> _metas;

    /**
     * Protected constructor
     */
    protected Builder() {
      _metas = new HashSet<>();
    }

    /**
     * Adds a meta to the set.
     * @param meta The meta, not null
     * @return The builder
     */
    public Builder useMeta(final LegalEntityMeta<LegalEntity> meta) {
      ArgumentChecker.notNull(meta, "meta");
      _metas.add(meta);
      return this;
    }

    /**
     * Creates a combined meta object.
     * @return The combined meta
     */
    public LegalEntityCombinedMeta create() {
      return new LegalEntityCombinedMeta(_metas);
    }
  }
}
