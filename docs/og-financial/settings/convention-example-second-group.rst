Conventions: Examples - Second group
==================================
The currencies referred in this documentation are AUD, CAD, CHF and ZAR

Forex
-----

**Spot**

The names in the examples are **XXXYYYFXSpot** where **XXX** is the first currency (in the standard order) and **YYY** the second currency. 

* AUDUSDFXSpot
* AUDJPYFXSpot

* EURCHFFXSpot

* USDCADFXSpot
* USDCHFFXSpot
* USDZARFXSpot

* CHFJPYFXSpot


**Forward**

The names in the examples are **XXXYYYFXSwap** where **XXX** is the first currency (in the standard order) and **YYY** the second currency. 

* AUDUSDFXSwap
* AUDJPYFXSwap

* EURCHFFXSwap

* USDCADFXSwap
* USDCHFFXSwap
* USDZARFXSwap

* CHFJPYFXSwap

Overnight Index
-------------

AUD
CAD
CHF
ZAR

Ibor Index
--------
* AUDBBSW3M
* AUDBBSW6M

* CADCDOR3M

* CHFLIBOR1M 
* CHFLIBOR3M 
* CHFLIBOR6M

Swap Legs
--------

**Ibor Compounded Leg**

The names in the examples are **XXXYYYZZCmpVV**. The **XXX** is the currency of the leg. The **YYY** is the index name (like *CDOR*). The **ZZ** is the index tenor (like *1M* or *3M*). The **Cmp** is a literal indicating composition. The **VV** is the payment tenor (like *6M* or *1Y*). The last part indicates the compounding type (in case of spread).

* CADCDOR3MCmp6M_Flat
