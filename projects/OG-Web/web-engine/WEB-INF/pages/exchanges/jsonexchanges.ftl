<#escape x as x?html>
{
    "header": {
        "type": "Exchanges",
        <#if searchResult??>
        "pgIdx": ${"${paging.firstItem}"?replace(',','')},
        "pgSze": ${"${paging.pagingSize}"?replace(',','')},
        "pgTtl": ${"${paging.totalItems}"?replace(',','')},
        </#if>
        "dataFields": ["id", "name", "validFrom"]
    },
    "data": [<#if searchResult??>
        <#list searchResult.documents as item>
        "${item.uniqueId.objectId}|${item.exchange.name}|${item.versionFromInstant}"<#if item_has_next>,</#if>
        </#list>
     </#if>]
}
</#escape>