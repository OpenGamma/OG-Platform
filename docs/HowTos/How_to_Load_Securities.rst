================================
How to Load Securities using XML
================================

It is possible to add securities via xml through the configuration screens. To illustrate the process, take the creation of a corportate bond security,

Open web a browser and connect to the OG server URL
Navigate to "/jax/securities/", In the Add security by XML section select **DbSec** from the dropdown.

Add the sample XML below

.. code :: xml

	<?xml version="1.0" encoding="UTF-8"?>
	<bean type="com.opengamma.financial.security.bond.CorporateBondSecurity">
	   <externalIdBundle>
	      <externalIds>
	         <item>BLOOMBERG_BUID~COEI6386938</item>
	         <item>BLOOMBERG_TCM~ABBEY 5.125 2021-04-14 Corp</item>
	         <item>BLOOMBERG_TICKER~ABBEY 5.125 04/14/21 EMTN Corp</item>
	         <item>CUSIP~EI6386938</item>
	         <item>ISIN~XS0616897616</item>
	         <item>SEDOL1~B636SR9</item>
	      </externalIds>
	   </externalIdBundle>
	   <name>ABBEY 5 1/8 04/14/21</name>
	   <securityType>BOND</securityType>
	   <attributes>
	      <entry key="IndustrySector">Financial</entry>
	      <entry key="Bullet">Y</entry>
	      <entry key="LegalEntityId">CUSIP_ENTITY_STUB~EI6386</entry>
	      <entry key="Perpetual">N</entry>
	      <entry key="RatingComposite">AAA</entry>
	      <entry key="RatingSP">AAA</entry>
	      <entry key="EOM">Y</entry>
	      <entry key="RatingMoody">Aaa</entry>
	      <entry key="RatingFitch">AAA</entry>
	      <entry key="Callable">N</entry>
	   </attributes>
	   <requiredPermissions />
	   <issuerName>ABBEY NATL TREASURY SERV</issuerName>
	   <issuerType>Banks</issuerType>
	   <issuerDomicile>GB</issuerDomicile>
	   <market>EURO MTN</market>
	   <currency>GBP</currency>
	   <yieldConvention>US street</yieldConvention>
	   <guaranteeType>MULTIPLE GUARANTORS</guaranteeType>
	   <lastTradeDate>
	      <expiry>2021-04-14T00:00Z</expiry>
	      <accuracy>DAY_MONTH_YEAR</accuracy>
	   </lastTradeDate>
	   <couponType>FIXED</couponType>
	   <couponRate>5.125</couponRate>
	   <couponFrequency>Annual</couponFrequency>
	   <dayCount>Actual/Actual ICMA</dayCount>
	   <announcementDate>2011-04-07T17:00Z</announcementDate>
	   <interestAccrualDate>2011-04-14T17:00Z</interestAccrualDate>
	   <settlementDate>2013-06-07T17:00Z</settlementDate>
	   <firstCouponDate>2012-04-14T17:00Z</firstCouponDate>
	   <issuancePrice>99.725</issuancePrice>
	   <totalAmountIssued>1.25E9</totalAmountIssued>
	   <minimumAmount>100000.0</minimumAmount>
	   <minimumIncrement>1000.0</minimumIncrement>
	   <parAmount>1000.0</parAmount>
	   <redemptionValue>100.0</redemptionValue>
	</bean>


Attributes, such as industry classification, yield conventions and call features are added as key-value pairs, for example:

.. code :: xml

      <attributes>
        <entry key="IndustrySector">Financial</entry>
        <entry key="RatingSP">AAA</entry>
      </attributes>

The XML maps directly onto the data model, in this case the Joda­Bean BondSecurity. The properties definitions below match the XML bean nodes:

.. code :: java

      /*Theissuername.*/
      @PropertyDefinition(validate="notNull")
      privateString_issuerName;
      /*Theissuertype.*/
      @PropertyDefinition(validate="notNull")
      privateString_issuerType;
      /*Theissuerdomicile.*/
      @PropertyDefinition(validate="notNull")
      privateString_issuerDomicile;
      /*Themarket.*/
      @PropertyDefinition(validate="notNull")
      privateString_market;
      /*Thecurrency.*/
      @PropertyDefinition(validate="notNull")
      privateCurrency_currency;
      /* Theyieldconvention.*/
      ...
      ...
      /*Theparamount.*/
      @PropertyDefinition
      privatedouble_parAmount;
      /*Theredemptionvalue.*/
      @PropertyDefinition
      privatedouble_redemptionValue;


And of type:

.. code :: java

    /*Thesecuritytype.*/
    publicstaticfinalStringSECURITY_TYPE="BOND";


Further security definitions can be found by inspecting the subclasses of ``com.opengamma.master.security.ManageableSecurity``.

Currently the ``com/opengamma/integration/tool/portfolio/SecurityLoaderTool.java'' tool exists to add securities in bulk via an external identifier, for example a Bloomberg Ticker. A similar tool could be written to load XML inputs or the REST endpoint could be accessed programmatically to load the securities.

The main functionality of a tool would need the following

.. code :: java

	BeansecurityBean=JodaBeanSerialization.deserializer().xmlReader().read(securityXml);
	ManageableSecuritymanageableSecurity=(ManageableSecurity)securityBean;
	SecurityMastersecurityMaster=toolContext.getSecurityMaster();
	securityMaster.add(newSecurityDocument(manageableSecurity));
	

This method would form the main functionality of the tool. Here we have the steps:

	1. Deserialze the xml into bean representation
	2. Cast the security bean into a ManageableSecurity
	3. Obtain the SecurityMaster from the tool context
	4. Add the security to the SecurityMaster
