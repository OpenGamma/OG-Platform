<#escape x as x?html>
{
    "header": {
        "type": "Batches",
        "dataFields": ["id", "id_time", "status"],
        <#if searchResult??>
        "total" : ${"${paging.totalItems}"?replace(',','')},
	      "count": ${"${paging.pagingSize}"?replace(',','')}
	      </#if>
    },
    "data": [
      <#if searchResult??>
        <#list searchResult.documents as item>
            "${item.observationDate}|${item.observationTime}|${item.status}"<#if item_has_next>,</#if>
        </#list>
      </#if>
    ]
}
</#escape>