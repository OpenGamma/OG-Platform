/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.calendar;

import java.io.IOException;

import javax.time.calendar.LocalDate;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Populates an ExceptionCalendar with working and non-working days from an XML data source.
 * 
 * @author Andrew Griffin
 */
public class XMLCalendarLoader {
  
  private static final String TAG_WORKING_DAYS = "WorkingDays";
  private static final String TAG_NON_WORKING_DAYS = "NonWorkingDays";
  private static final String TAG_DATE = "Date";

  private static enum ParserState {
    WORKING_DAYS,
    NON_WORKING_DAYS,
    OTHER;
  };
  
  private final String _sourceDataURI;
  
  public XMLCalendarLoader (final String sourceDataUri) {
    _sourceDataURI = sourceDataUri;
  }
  
  protected String getSourceDataURI () {
    return _sourceDataURI;
  }
  
  private OpenGammaRuntimeException wrap (final Throwable t) {
    return new OpenGammaRuntimeException ("couldn't populate calendar from XML", t);
  }
  
  public void populateCalendar (final ExceptionCalendar calendar) {
    final SAXParserFactory factory = SAXParserFactory.newInstance ();
    try {
      final SAXParser parser = factory.newSAXParser ();
      parser.parse (getSourceDataURI (), new DefaultHandler () {
        
        private ParserState _state = ParserState.OTHER;
        
        private String _innerText;
        
        private ParserState getState () {
          return _state;
        }
        
        private void setState (final ParserState state) {
          _state = state;
        }
        
        private void setInnerText (String innerText) {
          _innerText = innerText;
        }
        
        private String getInnerText () {
          return _innerText;
        }
        
        @Override
        public void startElement (String uri, String localName, String qName, Attributes attributes) {
          switch (getState ()) {
          case OTHER :
            if (qName.equalsIgnoreCase (TAG_WORKING_DAYS)) {
              setState (ParserState.WORKING_DAYS);
            } else if (qName.equalsIgnoreCase (TAG_NON_WORKING_DAYS)) {
              setState (ParserState.NON_WORKING_DAYS);
            }
            break;
          }
        }
        
        @Override
        public void characters (char[] ch, int start, int length) {
          setInnerText (new String (ch, start, length));
        }
        
        @Override
        public void endElement (String uri, String localName, String qName) {
          switch (getState ()) {
          case WORKING_DAYS :
            if (qName.equalsIgnoreCase (TAG_DATE)) {
              calendar.addWorkingDay (LocalDate.parse (getInnerText ()));
            } else if (qName.equalsIgnoreCase (TAG_WORKING_DAYS)) {
              setState (ParserState.OTHER);
            }
            break;
          case NON_WORKING_DAYS :
            if (qName.equalsIgnoreCase (TAG_DATE)) {
              calendar.addNonWorkingDay (LocalDate.parse (getInnerText ()));
            } else if (qName.equalsIgnoreCase (TAG_NON_WORKING_DAYS)) {
              setState (ParserState.OTHER);
            }
            break;
          }
        }
        
      });
    } catch (ParserConfigurationException e) {
      throw wrap (e);
    } catch (SAXException e) {
      throw wrap (e);
    } catch (IOException e) {
      throw wrap (e);
    }
  }
  
}