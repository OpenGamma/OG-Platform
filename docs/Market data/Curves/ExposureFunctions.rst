==================
Exposure Functions
==================

The curve configuration used to calculate a risk measure is determined by an ExposureFunctions configuration item, which extracts information from the trade to then lookup the relevant curve configuration.

Java example
------------
.. code-block:: java

 List<String> functions = new ArrayList<>();
 functions.add(Currency.USD.getCode());

 Map<ExternalId, String> idsToNames = new HashMap<>();
 idsToNames.put(ExternalId.of(Currency.OBJECT_SCHEME, Currency.USD.getCode()));

 String name = "My Exposure Function";
 
 ExposureFunctions exposures = new ExposureFunctions(name, functions, idsToNames);
 

Fudge XML example
-----------------
.. code-block:: xml

 <?xml version="1.0" encoding="UTF-8"?>
  <bean type="com.opengamma.financial.analytics.curve.exposure.ExposureFunctions">
   <name>My Exposure Function</name>
   <exposureFunctions>
    <item>Currency</item>
   </exposureFunctions>
   <idsToNames>
    <entry key="CurrencyISO~USD">MyUSDCurveConstructionConfiguration</idstoNames>
   </idsToNames>
  </bean>

Exposure Function types
-----------------------

The current available exposure functions are:

Contract Category
~~~~~~~~~~~~~~~~~
* Item type: *Contract Category*
* Example id: *ContractType~MyContractCategory*

The ContractCategoryExposureFunction maps a future trade's contract category to a curve configuration. For non-future trades, this will do nothing.

Currency
~~~~~~~~
* Item type: *Currency*
* Example id: *CurrencyISO~USD*

The CurrencyExposureFunction maps a trade's currency to a curve configuration.

Counterparty
~~~~~~~~~~~~
* Item type: *Counterparty*
* Example id: *Counterparty~MyCounterparty*

The CounterpartyExposureFunction maps a trade's counterparty to a curve configuration.

Region
~~~~~~
* Item type: *Region*
* Example id: *FINANCIAL_REGION~US*

The RegionExposureFunction maps a trade's security region to a curve configuration. The external scheme of the id can be any scheme defined on the security.

Security
~~~~~~~~
* Item type: *Security*
* Example id: *DbSec~1000*

The SecurityExposureFunction maps a trade's security unique id to a curve configuration.

Security type
~~~~~~~~~~~~~
* Item type: *Security Type*
* Example id: *SecurityType~FRA*

The SecurityTypeExposureFunction maps a trade's security type to a curve configuration.

Security / Currency
~~~~~~~~~~~~~~~~~~~
* Item type: *Security / Currency*
* Example id: *SecurityType~FRA_USD*

The SecurityAndCurrencyExposureFunction maps a trade's security type and currency to a curve configuration.

Security / Region
~~~~~~~~~~~~~~~~~
* Item type: *Security / Region*
* Example id: *SecurityType~FRA_FINANCIAL_REGION~US*

The SecurityAndRegionExposureFunction maps a trade's security type and region to a curve configuration. The external scheme of the id can be any scheme defined on the security.

Security / Settlement Exchange
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
* Item type: *Security / Settlement Exchange*
* Example id: *SecurityType~FUTURE_CME*

The SecurityAndSettlementExposureFunction maps a trade's security type and settlement exchange to a curve configuration. This will only work for listed derivatives, for OTC trades this will do nothing.

Security / Trading Exchange
~~~~~~~~~~~~~~~~~~~~~~~~~~~
* Name: *Security / Trading Exchange*
* Example id: *SecurityType~FUTURE_CME*

The SecurityAndSettlementExposureFunction maps a trade's security type and trading exchange to a curve configuration. This will only work for listed derivatives, for OTC trades this will do nothing.

Trade attribute
~~~~~~~~~~~~~~~
* Item type: *TradeAttribute*
* Example id: *TradeAttribute~MyKey=MyValue*

The TradeAttributeExposureFunction maps a trade attribute to a curve configuration.

Underlying
~~~~~~~~~~
* Item type: *Underlying*
* Example id: *DbSec~1000*

The UnderlyingExposureFunction maps a trades's underlying id to a curve configuration.
