/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.bundle;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;

/**
 * Parses a given bundle xml file.
 */
public class BundleParser {
  
  private static final String FRAGMENT_ELEMENT = "fragment";
  private static final String ID_ATTR = "id";
  private static final String BUNDLE_ELEMENT = "bundle";

  private static final Logger s_logger = LoggerFactory.getLogger(BundleParser.class);
  
  /**
   * The bundle xml file to parse
   */
  private final File _xmlFile;
  /**
   * The base directory for web resource
   */
  private final File _baseDir;
  
  /**
   * Empty bundleManager
   */
  private final BundleManager _bundleManager = new BundleManager();
  /**
   * Elements map
   */
  private final Map<String, Element> _elementsByIdMap = new HashMap<String, Element>(); 
  
  /**
   * Creates a BundleParser with xml file
   * 
   * @param xmlFile   the xml file to parse, not null
   */
  public BundleParser(File xmlFile) {
    this(xmlFile, null);
  }
  
  /**
   * Creates a BundleParser with xml file and base dir
   * 
   * @param xmlFile   the xml file to parse, not null
   * @param baseDir   the base directory for web resource, not null
   */
  public BundleParser(File xmlFile, File baseDir) {
    validate(xmlFile, baseDir);
    _bundleManager.setBaseDir(baseDir);
    _xmlFile = xmlFile;
    _baseDir = baseDir;
  }
 
  private void validate(File xmlFile, File baseDir) {
    ArgumentChecker.notNull(xmlFile, "xmlFile");
    ArgumentChecker.isTrue(xmlFile.exists(), xmlFile + "does not exists");
    
    if (baseDir != null) {
      ArgumentChecker.isTrue(baseDir.exists(), baseDir + " does not exists");
    }
  }

  public BundleManager getBundleManager() {
    DocumentBuilder builder = getDocumentBuilder();
    if (builder != null) {
      try {
        Document document = builder.parse(_xmlFile);
        processXMLDocument(document);
      } catch (SAXException ex) {
        throw new OpenGammaRuntimeException("unable to parse : " + _xmlFile.getAbsolutePath(), ex);
      } catch (IOException ex) {
        throw new OpenGammaRuntimeException("unable to read : " + _xmlFile.getAbsolutePath(), ex);
      }
    }
    return _bundleManager;
  }
  
  private void processXMLDocument(Document document) {
    buildAllElements(document);
    for (Element element : _elementsByIdMap.values()) {
      addToManager(element);
    }
  }

  private void addToManager(Element element) {
    String idAttr = element.getAttribute(ID_ATTR);
    Bundle bundle = new Bundle(idAttr);
    NodeList childNodes = element.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); i++) {
      Node node = childNodes.item(i);
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        Element childElement = (Element) node;
        if (childElement.getNodeName().equals(BUNDLE_ELEMENT)) {
          processRefBundle(bundle, childElement);
        }
        if (childElement.getNodeName().equals(FRAGMENT_ELEMENT)) {
          processFragment(bundle, childElement);
        } 
      }
    }
    _bundleManager.addBundle(bundle);
  }

  private void processFragment(Bundle bundle, Element element) {
    String fragment = element.getTextContent();
    if (isValidFragment(fragment)) {
      bundle.addChildNode(createBundleFragment(fragment));
    }
  }

  private boolean isValidFragment(String fragment) {
    if (!StringUtils.isBlank(fragment)) {
      return true;
    }
    throw new OpenGammaRuntimeException(_xmlFile.getAbsolutePath() + " invalid fragment value");
  }

  private BundleNode createBundleFragment(String fragment) {
    return new Fragment(new File(_baseDir, fragment));
  }
  
  private void processRefBundle(Bundle bundle, Element element) {
    String idRef = element.getAttribute("idref");
    if (isValidIdRef(idRef)) {
      Bundle refBundle = _bundleManager.getBundle(idRef);
      if (refBundle == null) {
        Element refElement = _elementsByIdMap.get(idRef);
        //This can cause infinite loop if we have circular reference
        addToManager(refElement);
        refBundle = _bundleManager.getBundle(idRef);
      }
      bundle.addChildNode(refBundle);
    }
  }

  private boolean isValidIdRef(String idRef) {
    if (!StringUtils.isBlank(idRef) && idRefExists(idRef)) {
      return true;
    }
    throw new OpenGammaRuntimeException(_xmlFile.getAbsolutePath() + " invalid idref ["  + idRef + "]");
  }

  private boolean idRefExists(String idRef) {
    return _elementsByIdMap.get(idRef) != null;
  }

  private void buildAllElements(Document document) {
    Element rootElement = document.getDocumentElement();
    if (isValidRootElement(rootElement)) {
      rootElement.normalize();
      NodeList childNodes = rootElement.getChildNodes();
      for (int i = 0; i < childNodes.getLength(); i++) {
        Node node = childNodes.item(i);
        if (node.getNodeType() == Node.ELEMENT_NODE) {
          Element element = (Element) node;
          if (isValidBundleElement(element)) {
            String idAttr = element.getAttribute(ID_ATTR);
            if (_elementsByIdMap.get(idAttr) == null) {
              _elementsByIdMap.put(idAttr, element);
            } else {
              throw new OpenGammaRuntimeException(_xmlFile.getAbsolutePath() + ": duplicate id attribute in " + node.getNodeName());
            }
          }
        }
      }
    }
  }

  private boolean isValidRootElement(Element rootElement) {
    if (rootElement.getNodeName().equals("uiResourceConfig")) {
      return true;
    }
    throw new OpenGammaRuntimeException(_xmlFile.getAbsolutePath() + ": invalid root element " + rootElement.getNodeName());
  }

  private boolean isValidBundleElement(Element element) {
    return isBundleElement(element) && hasChildren(element) && hasValidId(element);
  }

  private boolean hasValidId(Element element) {
    if (element.hasAttribute(ID_ATTR) && !StringUtils.isBlank(element.getAttribute(ID_ATTR))) {
      return true;
    } 
    throw new OpenGammaRuntimeException(_xmlFile.getAbsolutePath() + ": bundle element needs id attribute");
  }

  private boolean hasChildren(Element element) {
    if (element.hasChildNodes()) {
      return true;
    } 
    throw new OpenGammaRuntimeException(_xmlFile.getAbsolutePath() + ": missing children elements in bundle");
  }

  private boolean isBundleElement(Element element) {
    if (element.getNodeName().equals(BUNDLE_ELEMENT)) {
      return true;
    } 
    throw new OpenGammaRuntimeException(_xmlFile.getAbsolutePath() + ": element not a bundle");
  }
  
  private DocumentBuilder getDocumentBuilder() {
    DocumentBuilder builder = null;
    DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
    try {
      builder = builderFactory.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      s_logger.warn("Unable to create a DOM parser", e);
    }
    return builder;
  }
  
}
