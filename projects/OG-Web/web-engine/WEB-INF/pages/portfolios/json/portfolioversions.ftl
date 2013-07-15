<#escape x as x?html>
{
    "header": {
        "type": "PortfolioHistory",
        <#if versionsResult??>
        "pgIdx": ${"${paging.firstItem}"?replace(',','')},
        "pgSze": ${"${paging.pagingSize}"?replace(',','')},
        "pgTtl": ${"${paging.totalItems}"?replace(',','')},
        </#if>
        "object_id": "${portfolio.uniqueId.objectId}",
        "dataFields": ["version", "name", "validFrom", "validTo", "correctionFrom", "correctionTo"]
    },
    "data": [<#if versionsResult??>
        <#list versionsResult.documents as item>
        "${item.uniqueId.version}|${item.portfolio.name}|${item.versionFromInstant}|${item.versionToInstant}|${item.correctionFromInstant}|${item.correctionToInstant}"<#if item_has_next>,</#if>
        </#list>
     </#if>]
}
</#escape>
