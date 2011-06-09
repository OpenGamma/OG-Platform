<#escape x as x?html>
{
    "header": {
        "type": "Batches",
        <#if searchResult??>
        "total" : ${"${paging.totalItems}"?replace(',','')},
        "page": ${"${paging.page}"?replace(',','')},
        "pageSize": ${"${paging.pagingSize}"?replace(',','')},
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