/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.loader;

import static com.opengamma.bbg.BloombergConstants.FIELD_ID_BBG_UNIQUE;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID_CUSIP;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID_ISIN;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID_SEDOL1;
import static com.opengamma.bbg.BloombergConstants.FIELD_INDX_SOURCE;
import static com.opengamma.bbg.BloombergConstants.FIELD_PARSEKYABLE_DES;
import static com.opengamma.bbg.BloombergConstants.FIELD_SECURITY_DES;
import static com.opengamma.bbg.util.BloombergDataUtils.isValidField;

import java.util.Collections;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.bbg.util.BloombergDataUtils;
import com.opengamma.financial.security.index.IborIndex;
import com.opengamma.financial.security.index.Index;
import com.opengamma.financial.security.index.OvernightIndex;
import com.opengamma.financial.security.index.PriceIndex;
import com.opengamma.financial.security.index.SwapIndex;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.time.Tenor;

/**
 * Loads the data for an Index Future from Bloomberg.
 */
public class IndexLoader extends SecurityLoader {

  private static final String BLOOMBERG_INDEX_TYPE = "Index";
  /**
   * Valid Security type values for this index
   */
  public static final Set<String> VALID_SECURITY_TYPES = Collections.unmodifiableSet(Sets.newHashSet(BLOOMBERG_INDEX_TYPE));
  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(IndexLoader.class);
  /**
   * The fields to load from Bloomberg.
   */
  private static final Set<String> BLOOMBERG_INDEX_FIELDS = Collections.unmodifiableSet(Sets.newHashSet(
      FIELD_SECURITY_DES,
      FIELD_PARSEKYABLE_DES,
      FIELD_INDX_SOURCE,
      FIELD_ID_BBG_UNIQUE,
      FIELD_ID_CUSIP,
      FIELD_ID_ISIN,
      FIELD_ID_SEDOL1));
  
  private static final Pattern s_tenorFromDes = Pattern.compile("(.*?)(Overnight.*?|O\\/N.*?|OVERNIGHT.*?|Tomorrow[\\s\\/]Next.*?|T[\\s\\/]N.*?|TOM[\\s\\/]NEXT.*?|\\d+\\s*.*?)");
  private static final Pattern s_overnight = Pattern.compile(".*?(Overnight|O\\/N|OVERNIGHT).*?");
  private static final Pattern s_tomNext = Pattern.compile(".*?(Tomorrow[\\s\\/]Next|T[\\s\\/]N|TOM[\\s\\/]NEXT).*?");
  private static final Pattern s_numberFromTimeUnit = Pattern.compile("(\\d+)\\s*(.*?)");
  private static final String BLOOMBERG_CONVENTION_NAME = "BLOOMBERG_CONVENTION_NAME";
  private static final String BLOOMBERG_INDEX_FAMILY = "BLOOMBERG_INDEX_FAMILY";
  
  private static final String FED_FUNDS_SECURITY_DES = "Federal Funds Effective Rate U";
  private static final Set<String> BLOOMBERG_SECURITY_DES_OVERNIGHT_EXCEPTIONS = Collections.unmodifiableSet(Sets.newHashSet(
      FED_FUNDS_SECURITY_DES
  ));

  /**
   * Creates an instance.
   * @param referenceDataProvider  the provider, not null
   */
  public IndexLoader(ReferenceDataProvider referenceDataProvider) {
    super(s_logger, referenceDataProvider, SecurityType.INDEX);
  }

  //-------------------------------------------------------------------------
  @Override
  protected ManageableSecurity createSecurity(FudgeMsg fieldData) {
    String securityDes = fieldData.getString(FIELD_SECURITY_DES);
    String name = BloombergDataUtils.removeDuplicateWhiteSpace(fieldData.getString(FIELD_SECURITY_DES), " ");
    String bbgUnique = fieldData.getString(FIELD_ID_BBG_UNIQUE);
    String indexSource = fieldData.getString(FIELD_INDX_SOURCE);

    if (!isValidField(bbgUnique)) {
      s_logger.warn("bbgUnique is null, cannot construct index");
      return null;
    }
    if (!isValidField(securityDes)) {
      s_logger.warn("security description is null, cannot construct index");
      return null;
    }
    if (!isValidField(indexSource)) {
      s_logger.warn("index source is null, cannot construct index");
      return null;
    }
 
    Index index;
    Tenor tenor = decodeTenor(securityDes);
    ExternalId conventionId = createConventionId(securityDes);
    ExternalId familyId = ExternalId.of(ExternalScheme.of(BLOOMBERG_INDEX_FAMILY), conventionId.getValue());
    
    if (indexSource.toUpperCase().contains("STAT") || securityDes.toUpperCase().contains("CPI")) {
      // guess it's a price index as source is STATistics agency or description contains CPI. Crude, but hopefully effective.
      index = new PriceIndex(name, conventionId);
      index.setIndexFamilyId(null);
    } else if (securityDes.toUpperCase().contains("ISDAFIX") && tenor != null) {
      // guess it's a swap index
      index = new SwapIndex(name, tenor, conventionId);
      index.setIndexFamilyId(familyId);
    } else if (tenor != null) { 
      // Ibor or overnight
      if (tenor.equals(Tenor.ON)) {
        index = new OvernightIndex(name, conventionId);
        index.setIndexFamilyId(familyId);
      } else {
        index = new IborIndex(name, tenor, conventionId);
        index.setIndexFamilyId(familyId);
      }
      index.setName(name);
    } else {
      s_logger.error("Could not load index {}, source={}, tenor={}, securityDes={}", name, indexSource, tenor, securityDes);
      return null;
    }

    // set identifiers
    parseIdentifiers(fieldData, index);
    return index;
  }
  
  // public visible for tests
  public static ExternalId createConventionId(String securityDes) {
    Matcher matcher = s_tenorFromDes.matcher(securityDes);
    if (matcher.matches()) {
      String descriptionPart = matcher.group(1); // remember, groups are 1 indexed!
      return ExternalId.of(ExternalScheme.of(BLOOMBERG_CONVENTION_NAME), descriptionPart.trim());
    } else {
      return ExternalId.of(ExternalScheme.of(BLOOMBERG_CONVENTION_NAME), securityDes.trim());
    }
  }

  // public visible for tests
  public static Tenor decodeTenor(String securityDes) {
    if (BLOOMBERG_SECURITY_DES_OVERNIGHT_EXCEPTIONS.contains(securityDes)) {
      return Tenor.ON;
    }
    Matcher matcher = s_tenorFromDes.matcher(securityDes);
    if (matcher.matches()) {
      String tenorPart = matcher.group(2); // remember, groups are 1 indexed!
      if (s_overnight.matcher(tenorPart).matches()) {
        return Tenor.ON;
      } else if (s_tomNext.matcher(tenorPart).matches()) {
        return Tenor.TN;
      } else {
        Matcher numberFromTimeMatcher = s_numberFromTimeUnit.matcher(tenorPart);
        if (numberFromTimeMatcher.matches()) {
          String numberStr = numberFromTimeMatcher.group(1).trim();
          int number = Integer.parseInt(numberStr);
          String timeUnit = numberFromTimeMatcher.group(2).trim().toUpperCase();
          if (timeUnit.length() == 0) {
            throw new OpenGammaRuntimeException("Could not decode tenor from description " + securityDes);
          }
          switch (timeUnit.charAt(0)) {
            case 'D':
              // assume days!
              return Tenor.ofDays(number);
            case 'M':
              // assume months!
              return Tenor.ofMonths(number);
            case 'Y':
              // assume years!
              return Tenor.ofYears(number);
          }
        }
      }
    }
    return null;
  }

  @Override
  protected Set<String> getBloombergFields() {
    return BLOOMBERG_INDEX_FIELDS;
  }

}
