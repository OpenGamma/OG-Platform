/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.calendar;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang.Validate;
import org.threeten.bp.LocalDate;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Populates an {@code ExceptionCalendar} with working and non-working days from
 * an XML data source.
 */
public class XMLCalendarLoader {

  private static final String TAG_WORKING_DAYS = "WorkingDays";
  private static final String TAG_NON_WORKING_DAYS = "NonWorkingDays";
  private static final String TAG_DATE = "Date";

  /**
   * The state of the parser.
   */
  private static enum ParserState {
    WORKING_DAYS, NON_WORKING_DAYS, OTHER;
  }

  /**
   * The source URI.
   */
  private final String _sourceDataURI;

  /**
   * Creates an instance using the URI of the XML file.
   * @param sourceDataUri  the source URI, not null
   */
  public XMLCalendarLoader(final String sourceDataUri) {
    Validate.notNull(sourceDataUri, "URI");
    _sourceDataURI = sourceDataUri;
  }

  // -------------------------------------------------------------------------
  /**
   * Gets the source data URI.
   * @return the URI, not null
   */
  protected String getSourceDataURI() {
    return _sourceDataURI;
  }

  /**
   * Throws a suitable exception.
   * @param th  the error, not null
   * @return the exception to throw, not null
   */
  private OpenGammaRuntimeException wrap(final Throwable th) {
    return new OpenGammaRuntimeException("Unable to populate calendar from XML", th);
  }

  /**
   * Populate the specified working day calendar from the XML file.
   * @param calendar  the calendar to populate, not null
   */
  public void populateCalendar(final ExceptionCalendar calendar) {
    final SAXParserFactory factory = SAXParserFactory.newInstance();
    try {
      final SAXParser parser = factory.newSAXParser();
      parser.parse(getSourceDataURI(), new DefaultHandler() {
        private ParserState _state = ParserState.OTHER;
        private String _innerText;

        @Override
        public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) {
          switch (_state) {
            case OTHER:
              if (qName.equalsIgnoreCase(TAG_WORKING_DAYS)) {
                _state = ParserState.WORKING_DAYS;
              } else if (qName.equalsIgnoreCase(TAG_NON_WORKING_DAYS)) {
                _state = ParserState.NON_WORKING_DAYS;
              }
              break;
          }
        }

        @Override
        public void characters(final char[] ch, final int start, final int length) {
          _innerText = new String(ch, start, length);
        }

        @Override
        public void endElement(final String uri, final String localName, final String qName) {
          switch (_state) {
            case WORKING_DAYS:
              if (qName.equalsIgnoreCase(TAG_DATE)) {
                calendar.addWorkingDay(LocalDate.parse(_innerText));
              } else if (qName.equalsIgnoreCase(TAG_WORKING_DAYS)) {
                _state = ParserState.OTHER;
              }
              break;
            case NON_WORKING_DAYS:
              if (qName.equalsIgnoreCase(TAG_DATE)) {
                calendar.addNonWorkingDay(LocalDate.parse(_innerText));
              } else if (qName.equalsIgnoreCase(TAG_NON_WORKING_DAYS)) {
                _state = ParserState.OTHER;
              }
              break;
          }
        }
      });
    } catch (final ParserConfigurationException ex) {
      throw wrap(ex);
    } catch (final SAXException ex) {
      throw wrap(ex);
    } catch (final IOException ex) {
      throw wrap(ex);
    }
  }

}
