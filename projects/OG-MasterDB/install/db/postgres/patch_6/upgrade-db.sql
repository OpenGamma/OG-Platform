

-- In order to add the unique constraint we need to get rid of any existing duplicates
--  we could probably just delete them (since you can't reliably query them anyway) but I'd rather keep the data around
begin;

-- TODO this is not right IGN-101

update cfg_config as toDelete 
set
 name = toDelete.name || '_duplicateEntry_' || toDelete.oid,
 ver_to_instant =  CASE ver_to_instant 
				when cast ( '9999-12-31 23:59:59' AS  timestamp without time zone ) then  current_timestamp
				else ver_to_instant  
				end

from
(

select * from 
(
select name,config_type, max(oid) as saveOid, count(*) as c from cfg_config  
group by  name, config_type
) as setCounts
where c>1

) as toSave

where toDelete.name=toSave.name and toDelete.config_type = toSave.config_type and toDelete.oid <> toSave.saveOid

;


alter table cfg_config add constraint name_type_unique unique (name, config_type, ver_to_instant); -- TODO this is not right IGN-101

commit;