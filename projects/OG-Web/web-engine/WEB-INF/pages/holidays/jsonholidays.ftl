<#escape x as x?html>
{
    "header": {
        "type": "Holidays",
        <#if searchResult??>
        "pgIdx": ${"${paging.firstItem}"?replace(',','')},
        "pgSze": ${"${paging.pagingSize}"?replace(',','')},
        "pgTtl": ${"${paging.totalItems}"?replace(',','')},
	      </#if>
	      "dataFields": ["id","name" <#if searchRequest.type = ''>, "type"</#if> ,"validFrom"]
    },
    "data" : [<#if searchResult??><#list searchResult.documents as doc>
	    "${doc.uniqueId.objectId}|${doc.name}<#if searchRequest.type = ''>|${doc.holiday.type}</#if>|${doc.versionFromInstant}"<#if doc_has_next>,</#if>
	</#list> </#if>]        
}
</#escape>