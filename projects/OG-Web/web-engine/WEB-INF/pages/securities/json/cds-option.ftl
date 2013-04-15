<#escape x as x?html>
<#include "security-header.ftl"> 
  "buy":"${security.buy?string}",
  "protectionBuyer":"${security.protectionBuyer.scheme} - ${security.protectionBuyer.value}",
  "protectionSeller":"${security.protectionSeller.scheme} - ${security.protectionSeller.value}",
  "startDate": "${security.startDate.toLocalDate()}",
  "maturityDate": "${security.maturityDate.toLocalDate()}",
  "currency":"${security.currency}",
  "notional":"${security.notional}",
  "strike":"${security.strike}",
  "knockOut":"${security.knockOut?string}",
  "payer":"${security.payer?string}",
  "exerciseType":"${customRenderer.printExerciseType(security.exerciseType)}",
  "underlyingId":"${security.underlyingId.scheme} - ${security.underlyingId.value}",
  <#if underlyingOrganization??>
    "underlyingName":"${underlyingOrganization.obligor.obligorShortName}",
    "underlyingOid":"${underlyingOrganization.uniqueId.objectId}",
  </#if>
<#include "security-footer.ftl"> 
</#escape>