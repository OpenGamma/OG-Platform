package com.opengamma.master.cache;

import org.threeten.bp.Instant;

import com.opengamma.master.AbstractDocument;

import net.sf.ehcache.Element;
import net.sf.ehcache.search.attribute.AttributeExtractor;
import net.sf.ehcache.search.attribute.AttributeExtractorException;

/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

public class InstantExtractor implements AttributeExtractor {

  public static final Instant MIN_INSTANT = Instant.EPOCH;
  public static final Instant MAX_INSTANT = Instant.parse("9999-12-31T00:00Z");

  @Override
  public Object attributeFor(Element element, String attributeName) throws AttributeExtractorException {
    if (element != null && AbstractDocument.class.isAssignableFrom(element.getObjectValue().getClass())) {
      switch (attributeName) {
        case "VersionFromInstant":
          return ((AbstractDocument) element.getObjectValue()).getVersionFromInstant() != null
                    ? ((AbstractDocument) element.getObjectValue()).getVersionFromInstant().toString()
                    : MIN_INSTANT.toString();
        case "VersionToInstant":
          return ((AbstractDocument) element.getObjectValue()).getVersionToInstant() != null
                    ? ((AbstractDocument) element.getObjectValue()).getVersionToInstant().toString()
                    : MAX_INSTANT.toString();
        case "CorrectionFromInstant":
          return ((AbstractDocument) element.getObjectValue()).getCorrectionFromInstant() != null
                    ? ((AbstractDocument) element.getObjectValue()).getCorrectionFromInstant().toString()
                    : MIN_INSTANT.toString();
        case "CorrectionToInstant":
          return ((AbstractDocument) element.getObjectValue()).getCorrectionToInstant() != null
                    ? ((AbstractDocument) element.getObjectValue()).getCorrectionToInstant().toString()
                    : MAX_INSTANT.toString();
        default:
          throw new AttributeExtractorException("Unknown attribute name in InstantExtractor");
      }
    } else {
      throw new AttributeExtractorException("Null or non-document element passed to InstantExtractor");
    }
  }

}
