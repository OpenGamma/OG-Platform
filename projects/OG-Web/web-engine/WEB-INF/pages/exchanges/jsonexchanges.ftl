<#escape x as x?html>
{
    "header": {
        "type": "Exchanges",
        <#if searchResult??>
        "total" : ${"${paging.totalItems}"?replace(',','')},
        "page": ${"${paging.pageNumber}"?replace(',','')},
        "pageSize": ${"${paging.pagingSize}"?replace(',','')},
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