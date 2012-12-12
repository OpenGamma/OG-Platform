<#escape x as x?html>
{
    "header": {
        "type": "Portfolios",
        <#if searchResult??>
        "pgIdx": ${"${paging.firstItem}"?replace(',','')},
        "pgSze": ${"${paging.pagingSize}"?replace(',','')},
        "pgTtl": ${"${paging.totalItems}"?replace(',','')},
        </#if>
        "dataFields": ["id", "node", "name", "validFrom", "nodeCount"]
    },
    "data": [<#if searchResult??>
      <#list searchResult.documents as doc>
         "${doc.portfolio.uniqueId.objectId}|${doc.portfolio.rootNode.uniqueId.objectId}|${doc.portfolio.name}|${doc.versionFromInstant}|${doc.portfolio.rootNode.childNodes?size}"<#if doc_has_next>,</#if>
      </#list> </#if>]
}
</#escape>