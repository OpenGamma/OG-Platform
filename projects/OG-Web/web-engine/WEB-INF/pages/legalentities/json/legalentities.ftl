<#escape x as x?html>
{
	"header": {
    	"type": "LegalEntities",
    	<#if searchResult??>
        "pgIdx": ${"${paging.firstItem}"?replace(',','')},
        "pgSze": ${"${paging.pagingSize}"?replace(',','')},
        "pgTtl": ${"${paging.totalItems}"?replace(',','')},
	    </#if>
	    "dataFields": ["id", "name"]
    },
	"data": [<#if searchResult??>
	 <#list searchResult.legalEntities as legalentity>
	   "${legalentity.uniqueId.objectId}|${legalentity.name}"<#if legalentity_has_next>,</#if>
	 </#list> </#if>]
}
</#escape>