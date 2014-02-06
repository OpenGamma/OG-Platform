<#escape x as x?html>
<#include "security-header.ftl"> 
        "id":"${securityEntryData.id}",
        "currency":"${securityEntryData.currency}",
        "maturityDate":"${securityEntryData.maturityDate}",
        "factorSetId":"${securityEntryData.factorSetId}",
        <#if factorExposuresList??>
          "factors":[
            <#list factorExposuresList as factorExposure>
              {
                "factorType":"${factorExposure.factorType}",
                "factorName":"${factorExposure.factorName}",
                "node":"${factorExposure.node}",
                "priceTsId":"${factorExposure.priceTsId}",
                "lastPrice":"${factorExposure.lastPrice}",
                "exposureTsId":"${factorExposure.exposureTsId}",
                "lastExposure":"${factorExposure.lastExposure}",
                "convexityTsId":"${factorExposure.convexityTsId}",
                "lastConvexity":"${factorExposure.lastConvexity}"
              }
              <#if factorExposure_has_next>,</#if>
            </#list>
          ],
        </#if>
<#include "security-footer.ftl"> 
</#escape>
