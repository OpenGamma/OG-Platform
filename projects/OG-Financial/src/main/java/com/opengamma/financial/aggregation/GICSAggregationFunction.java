/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.legalentity.LegalEntity;
import com.opengamma.core.legalentity.LegalEntitySource;
import com.opengamma.core.obligor.definition.Obligor;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.SimplePositionComparator;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.cds.AbstractCreditDefaultSwapSecurity;
import com.opengamma.financial.security.cds.CreditDefaultSwapIndexDefinitionSecurity;
import com.opengamma.financial.security.cds.CreditDefaultSwapIndexSecurity;
import com.opengamma.financial.security.cds.CreditDefaultSwapSecurity;
import com.opengamma.financial.security.cds.LegacyVanillaCDSSecurity;
import com.opengamma.financial.security.cds.StandardVanillaCDSSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.equity.GICSCode;
import com.opengamma.financial.security.option.CreditDefaultSwapOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;

/**
 * Abstract aggregation function for bucketing equities and equity options by GICS code of the underlying
 */
public class GICSAggregationFunction implements AggregationFunction<String> {

  private static final Logger s_logger = LoggerFactory.getLogger(GICSAggregationFunction.class);

  private static final String UNKNOWN = "Unknown";
  private boolean _useAttributes;
  private final Comparator<Position> _comparator = new SimplePositionComparator();
  private final CdsObligorSectorExtractor _obligorSectorExtractor;
  private final CdsOptionObligorSectorExtractor _cdsOptionSectorExtractor;
  private final CdsIndexFamilyExtractor _cdsIndexFamilyExtractor;

  /**
   * Enumerated type representing how specific the GICS code should be interpreted.
   */
  public enum Level {

    /**
     * Sector
     */
    SECTOR("Sector"),

    /**
     * Industry Group
     */
    INDUSTRY_GROUP("Industry Group"),

    /**
     * Industry
     */
    INDUSTRY("Industry"),

    /**
     * Sub-industry
     */
    SUB_INDUSTRY("Sub-industry");

    private final String _displayName;

    private Level(String displayName) {
      _displayName = displayName;
    }

    public String getDisplayName() {
      return _displayName;
    }

    public int getNumber() {
      return ordinal() + 1;
    }

  }

  private Level _level;
  private SecuritySource _secSource;
  private boolean _includeEmptyCategories;

  public GICSAggregationFunction(SecuritySource secSource, LegalEntitySource legalEntitySource, String level) {
    this(secSource, legalEntitySource, Enum.valueOf(Level.class, level));
  }

  public GICSAggregationFunction(SecuritySource secSource, LegalEntitySource legalEntitySource, Level level) {
    this(secSource, legalEntitySource, level, false);
  }

  public GICSAggregationFunction(SecuritySource secSource,
                                 LegalEntitySource legalEntitySource,
                                 Level level,
                                 boolean useAttributes) {
    this(secSource, legalEntitySource, level, useAttributes, true);
  }

  public GICSAggregationFunction(SecuritySource secSource, String level) {
    this(secSource, Enum.valueOf(Level.class, level));
  }

  public GICSAggregationFunction(SecuritySource secSource, Level level) {
    this(secSource, level, false);
  }

  public GICSAggregationFunction(SecuritySource secSource,
                                 Level level,
                                 boolean useAttributes) {
    this(secSource, level, useAttributes, true);
  }

  public GICSAggregationFunction(SecuritySource secSource, Level level,
                                 boolean useAttributes,
                                 boolean includeEmptyCategories) {
    this(secSource, null, level, useAttributes, includeEmptyCategories);
  }

  public GICSAggregationFunction(SecuritySource secSource,
                                 LegalEntitySource legalEntitySource, Level level,
                                 boolean useAttributes,
                                 boolean includeEmptyCategories) {
    _secSource = secSource;
    _level = level;
    _useAttributes = useAttributes;
    _includeEmptyCategories = includeEmptyCategories;
    if (legalEntitySource == null) {
      if (_level == Level.SECTOR) {
        s_logger.warn("No organization source supplied - will be unable to show sectors for CDS reference entities");
      }
      _obligorSectorExtractor = null;
      _cdsOptionSectorExtractor = null;
      _cdsIndexFamilyExtractor = null;
    } else {
      _obligorSectorExtractor = new CdsObligorSectorExtractor(legalEntitySource);
      _cdsOptionSectorExtractor = new CdsOptionObligorSectorExtractor(secSource, legalEntitySource);
      _cdsIndexFamilyExtractor = new CdsIndexFamilyExtractor(secSource);
    }
  }

  private FinancialSecurityVisitor<String> _equitySecurityVisitor = new FinancialSecurityVisitorAdapter<String>() {
    @Override
    public String visitEquitySecurity(EquitySecurity security) {
      if (security.getGicsCode() != null) {
        switch (_level) {
          case SECTOR:
            return security.getGicsCode().getSectorDescription();
          case INDUSTRY_GROUP:
            return security.getGicsCode().getIndustryGroupDescription();
          case INDUSTRY:
            return security.getGicsCode().getIndustryDescription();
          case SUB_INDUSTRY:
            return security.getGicsCode().getSubIndustryDescription();
        }
      }
      return UNKNOWN;
    }
  };

  private FinancialSecurityVisitor<String> _equityOptionSecurityVisitor = new FinancialSecurityVisitorAdapter<String>() {
    @Override
    public String visitEquityOptionSecurity(EquityOptionSecurity security) {
      EquitySecurity underlying = (EquitySecurity) _secSource.getSingle(ExternalIdBundle.of(security.getUnderlyingId()));
      if (underlying != null) {
        if (underlying.getGicsCode() != null) {
          switch (_level) {
            case SECTOR:
              return underlying.getGicsCode().getSectorDescription();
            case INDUSTRY_GROUP:
              return underlying.getGicsCode().getIndustryGroupDescription();
            case INDUSTRY:
              return underlying.getGicsCode().getIndustryDescription();
            case SUB_INDUSTRY:
              return underlying.getGicsCode().getSubIndustryDescription();
          }
        }
      }
      return UNKNOWN;
    }
  };

  private FinancialSecurityVisitor<String> _equityIndexOptionSecurityVisitor = new FinancialSecurityVisitorAdapter<String>() {
    @Override
    public String visitEquityIndexOptionSecurity(EquityIndexOptionSecurity security) {
      if (_level == Level.SECTOR) {
        return security.getUnderlyingId().getValue();
      } else {
        return UNKNOWN;
      }
    }
  };

  private FinancialSecurityVisitor<String> _standardVanillaCdsSecurityVisitor = new FinancialSecurityVisitorAdapter<String>() {
    @Override
    public String visitStandardVanillaCDSSecurity(StandardVanillaCDSSecurity cds) {
      return sectorExtractionIsValid() ? _obligorSectorExtractor.extractOrElse(cds, UNKNOWN) : UNKNOWN;
    }
  };

  private FinancialSecurityVisitor<String> _legacyVanillaCdsSecurityVisitor = new FinancialSecurityVisitorAdapter<String>() {
    @Override
    public String visitLegacyVanillaCDSSecurity(LegacyVanillaCDSSecurity cds) {
      return sectorExtractionIsValid() ? _obligorSectorExtractor.extractOrElse(cds, UNKNOWN) : UNKNOWN;
    }
  };

  private FinancialSecurityVisitor<String> _cdsOptionSecurityVisitor = new FinancialSecurityVisitorAdapter<String>() {
    @Override
    public String visitCreditDefaultSwapOptionSecurity(CreditDefaultSwapOptionSecurity cdsOption) {
      return sectorExtractionIsValid() ? _cdsOptionSectorExtractor.extractOrElse(cdsOption, UNKNOWN) : UNKNOWN;
    }
  };

  private FinancialSecurityVisitor<String> _cdsIndexSecurityVisitor = new FinancialSecurityVisitorAdapter<String>() {
    @Override
    public String visitCreditDefaultSwapIndexSecurity(CreditDefaultSwapIndexSecurity cdsIndex) {
      return sectorExtractionIsValid() ? _cdsIndexFamilyExtractor.extractOrElse(cdsIndex, UNKNOWN) : UNKNOWN;
    }
  };


  private boolean sectorExtractionIsValid() {
    return (_level == Level.SECTOR && _obligorSectorExtractor != null);
  }

  @Override
  public String classifyPosition(Position position) {
    if (_useAttributes) {
      Map<String, String> attributes = position.getAttributes();
      if (attributes.containsKey(getName())) {
        return attributes.get(getName());
      } else {
        return UNKNOWN;
      }
    } else {
      FinancialSecurityVisitor<String> visitorAdapter = FinancialSecurityVisitorAdapter.<String>builder()
          .equitySecurityVisitor(_equitySecurityVisitor)
          .equityOptionVisitor(_equityOptionSecurityVisitor)
          .equityIndexOptionVisitor(_equityIndexOptionSecurityVisitor)
          .standardVanillaCDSSecurityVisitor(_standardVanillaCdsSecurityVisitor)
          .legacyVanillaCDSSecurityVisitor(_legacyVanillaCdsSecurityVisitor)
          .creditDefaultSwapOptionSecurityVisitor(_cdsOptionSecurityVisitor)
          .creditDefaultSwapIndexSecurityVisitor(_cdsIndexSecurityVisitor)
          .create();
      FinancialSecurity security = (FinancialSecurity) position.getSecurityLink().resolve(_secSource);
      try {
        String classification = security.accept(visitorAdapter);
        return classification == null ? UNKNOWN : classification;
      } catch (UnsupportedOperationException uoe) {
        return UNKNOWN;
      }
    }
  }

  @Override
  public String getName() {
    return "GICS - level " + _level.getNumber() + " (" + _level.getDisplayName() + ")";
  }

  @Override
  public Collection<String> getRequiredEntries() {
    if (_includeEmptyCategories) {
      Collection<String> baseList = new ArrayList<>();
      switch (_level) {
        case SECTOR:
          baseList.addAll(GICSCode.getAllSectorDescriptions());
          break;
        case INDUSTRY_GROUP:
          baseList.addAll(GICSCode.getAllIndustryGroupDescriptions());
          break;
        case INDUSTRY:
          baseList.addAll(GICSCode.getAllIndustryDescriptions());
          break;
        case SUB_INDUSTRY:
          baseList.addAll(GICSCode.getAllSubIndustryDescriptions());
          break;
      }
      baseList.add(UNKNOWN);
      return baseList;
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  public int compare(String o1, String o2) {
    if (o1.equals(UNKNOWN)) {
      if (o2.equals(UNKNOWN)) {
        return 0;
      }
      return 1;
    } else if (o2.equals(UNKNOWN)) {
      return -1;
    }
    return o1.compareTo(o2);
  }

  @Override
  public Comparator<Position> getPositionComparator() {
    return _comparator;
  }

  /**
   * Inner class to extract the sector from an obligor on a CDS.
   */
  private static class CdsObligorSectorExtractor {

    private final CdsRedCodeExtractor<LegalEntity> _obligorExtractor;

    public CdsObligorSectorExtractor(LegalEntitySource legalEntitySource) {
      _obligorExtractor = new CdsRedCodeExtractor<>(new CdsObligorExtractor(legalEntitySource));
    }

    public String extractOrElse(CreditDefaultSwapSecurity cds, String alternative) {

      LegalEntity legalEntity = _obligorExtractor.extract(cds);
      return legalEntity != null && legalEntity.getAttributes().get("sector") != null ? legalEntity.getAttributes().get("sector") : alternative;
    }
  }

  /**
   * Inner class to extract the sector from an obligor on a CDS option.
   */
  private static class CdsOptionObligorSectorExtractor {

    private final CdsOptionValueExtractor<LegalEntity> _obligorExtractor;

    public CdsOptionObligorSectorExtractor(final SecuritySource securitySource, final LegalEntitySource legalEntitySource) {
      _obligorExtractor = new CdsOptionValueExtractor<LegalEntity>() {
        @Override
        public LegalEntity extract(CreditDefaultSwapOptionSecurity cdsOption) {
          ExternalId underlyingId = cdsOption.getUnderlyingId();
          Security underlying = securitySource.getSingle(underlyingId.toBundle());
          if (underlying instanceof AbstractCreditDefaultSwapSecurity) {
            String redCode = ((CreditDefaultSwapSecurity) underlying).getReferenceEntity().getValue();
            return legalEntitySource.getSingle(ExternalId.of(ExternalSchemes.MARKIT_RED_CODE, redCode));
          }
          return null;
        }
      };
    }

    public String extractOrElse(CreditDefaultSwapOptionSecurity cdsOption, String alternative) {

      LegalEntity legalEntity = _obligorExtractor.extract(cdsOption);
      return legalEntity != null && legalEntity.getAttributes().get("sector") != null ? legalEntity.getAttributes().get("sector") : alternative;
    }
  }

  /**
   * Inner class to extract the family from an CDS index.
   */
  private static class CdsIndexFamilyExtractor {

    private final SecuritySource _securitySource;

    public CdsIndexFamilyExtractor(final SecuritySource securitySource) {
      _securitySource = securitySource;
    }

    public String extractOrElse(CreditDefaultSwapIndexSecurity cdsIndex, String alternative) {
      final CreditDefaultSwapIndexDefinitionSecurity underlyingDefinition = (CreditDefaultSwapIndexDefinitionSecurity) _securitySource.getSingle(ExternalIdBundle.of(cdsIndex.getReferenceEntity()));
      String family = underlyingDefinition.getFamily();
      return family != null ? family : alternative;
    }
  }
}
