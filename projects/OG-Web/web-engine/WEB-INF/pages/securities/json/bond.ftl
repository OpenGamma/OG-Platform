<#escape x as x?html>
<#include "security-header.ftl"> 
        "issuerName":"${security.issuerName}",
        "issuerType":"${security.issuerType}",
        "issuerDomicile":"${security.issuerDomicile}",
        "market":"${security.market}",
        "currency":"${security.currency}",
        "yieldConvention":"${security.yieldConvention.conventionName}",
        "lastTradeDate":"${security.lastTradeDate.expiry}",
        "lastTradeAccuracy":"${security.lastTradeDate.accuracy?replace("_", " ")}",
        "couponType":"${security.couponType}",
        "couponRate":"${security.couponRate}",
        "couponFrequency":"${security.couponFrequency.conventionName}",
        "dayCount":"${security.dayCount.conventionName}",
        "guaranteeType":"${security.guaranteeType}",
        "businessDayConvention":"${security.businessDayConvention}",
        <#if security.announcementDate?has_content>
          "announcementDate": "${security.announcementDate.toLocalDate()}",
        <#else>
          "announcementDate": "-",
        </#if>
        <#if security.interestAccrualDate?has_content>
          "interestAccrualDate": {
              "date": "${security.interestAccrualDate.toLocalDate()}",
              "zone": "${security.interestAccrualDate.zone}"
          },
        <#else>
          "interestAccrualDate": "null",
        </#if>
        <#if security.settlementDate?has_content>
          "settlementDate": {
              "date": "${security.settlementDate.toLocalDate()}",
              "zone": "${security.settlementDate.zone}"
          },
        <#else>
          "settlementDate": "null",
        </#if>
        <#if security.firstCouponDate?has_content>
          "firstCouponDate": {
              "date": "${security.firstCouponDate.toLocalDate()}",
              "zone": "${security.firstCouponDate.zone}"
          },
        <#else>
          "firstCouponDate": "null",
        </#if>
        "issuancePrice":"${security.issuancePrice}",
        "totalAmountIssued":"${security.totalAmountIssued}",
        "minimumAmount":"${security.minimumAmount}",
        "minimumIncrement":"${security.minimumIncrement}",
        "parAmount":"${security.parAmount}",
        "redemptionValue":"${security.redemptionValue}",
<#include "security-footer.ftl"> 
</#escape>