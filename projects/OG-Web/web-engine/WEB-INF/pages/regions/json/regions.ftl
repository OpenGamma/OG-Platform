<#escape x as x?html>
{
    "header": {
        "type": "Regions",
        <#if searchResult??>
        "pgIdx": ${"${paging.firstItem}"?replace(',','')},
        "pgSze": ${"${paging.pagingSize}"?replace(',','')},
        "pgTtl": ${"${paging.totalItems}"?replace(',','')},
	      </#if>
	      "dataFields": ["id", "name", "validFrom"]
	},
    "data": [<#if searchResult??>
     <#list searchResult.documents as doc>
    	"${doc.uniqueId.objectId}|${doc.region.name}|${doc.versionFromInstant}|${doc.region.getCountry()}"<#if doc_has_next>,</#if>
	   </#list> </#if>]
}
</#escape>