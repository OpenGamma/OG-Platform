<#escape x as x?html>
{
    "header": {
        "type": "Batches",
        <#if searchResult??>
        "pgIdx": ${"${paging.firstItem}"?replace(',','')},
        "pgSze": ${"${paging.pagingSize}"?replace(',','')},
        "pgTtl": ${"${paging.totalItems}"?replace(',','')},
	      </#if>
	      "dataFields": ["id", "date", "time", "status"]
    },
    "data": [
      <#if searchResult??>
        <#list searchResult.documents as item>
            "${item.uniqueId.objectId}|${item.observationDate}|${item.observationTime}|${item.status}"<#if item_has_next>,</#if>
        </#list>
      </#if>
    ]
}
</#escape>