select * from TSS_DOMAIN_SPEC_IDENTIFIER dsi, TSS_DOMAIN d where dsi.IDENTIFIER = 'OGM US Equity' and dsi.DOMAIN_ID = d.ID and d.NAME = 'bbgTicker';

select dsi.quoted_obj_id, count(dsi.quoted_obj_id) as "COUNT" 
from TSS_DOMAIN_SPEC_IDENTIFIER dsi, TSS_DOMAIN d 
where dsi.DOMAIN_ID = d.ID
AND ((dsi.identifier = 'AAPL US Equity' AND d.name = 'bbgTicker')OR (dsi.identifier = '123456789' AND d.name = 'cusip') OR (dsi.identifier = 'XI45678-89' AND d.name = 'bbgUnique'))
GROUP BY dsi.quoted_obj_id;

select * from TSS_DOMAIN;

select * from TSS_DOMAIN_SPEC_IDENTIFIER;


select dsi.quoted_obj_id, dsi.identifier, d.name from TSS_DOMAIN_SPEC_IDENTIFIER dsi, TSS_DOMAIN d where dsi.DOMAIN_ID = d.ID 
AND ((dsi.identifier = 'AAPL US Equity' AND d.name = 'bbgTicker') OR (dsi.identifier = '123456789' AND d.name = 'cusip')); 

select dsi.quoted_obj_id, dsi.identifier, d.name from TSS_DOMAIN_SPEC_IDENTIFIER dsi, TSS_DOMAIN d where dsi.DOMAIN_ID = d.ID 
AND ((dsi.identifier = 'AAPL US Equity' AND d.name = 'bbgTicker') OR (dsi.identifier = '123456789' AND d.name = 'cusip'));
	
SELECT tablename AS name FROM SYS.SYSTABLES WHERE tabletype = 'T';

SELECT constraintname AS name, tablename AS table_name FROM SYS.SYSCONSTRAINTS, SYS.SYSTABLES 
   WHERE SYS.SYSTABLES.tableid = SYS.SYSCONSTRAINTS.tableid AND type = 'F';
   
select d.name, dsi.identifier, qo.name, dsi.quoted_obj_id from TSS_DOMAIN_SPEC_IDENTIFIER dsi, TSS_DOMAIN d, TSS_QUOTED_OBJECT qo
	where dsi.DOMAIN_ID = d.ID and qo.id = dsi.QUOTED_OBJ_ID
	order by dsi.QUOTED_OBJ_ID;
	
SELECT qo.name, count(qo.name) as count
FROM tss_quoted_object qo, tss_domain_spec_identifier dsi, tss_domain d 
WHERE d.id = dsi.domain_id AND qo.id = dsi.quoted_obj_id AND ((d.name = 'bbgTicker' AND dsi.identifier = 'AAPL US Equity') ) 
GROUP BY qo.name;

SELECT * from tss_quoted_object;

select * from tss_data_source;

select * from tss_data_provider;

select * from tss_data_field;

select * from tss_observation_time;

select * from TSS_TIME_SERIES_KEY;

INSERT into tss_time_series_key  
(qouted_obj_id, data_soure_id, data_provider_id, data_field_id, observation_time_id) 
values (
(SELECT id from quoted_object where name = 'Dell'), 
(SELECT id from data_source where name = 'BBG'), 
(SELECT id from data_provider where name = 'CMPL'), 
(SELECT id from data_field where name = 'CLOSE'), 
(SELECT id from observation_time where name = 'LCLOSE'));

SELECT tskey.id FROM  
tss_time_series_key tskey,  
tss_quoted_object qo,   
tss_data_source ds,  
tss_data_provider dp,  
tss_data_field df,  
tss_observation_time ot 
WHERE  
tskey.qouted_obj_id = qo.id  
AND tskey.data_soure_id = ds.id  
AND tskey.data_provider_id = dp.id  
AND tskey.data_field_id = df.id  
AND tskey.observation_time_id = ot.id  
AND qo.name = 'OpenGamma'  
AND ds.name = 'BBG'  
AND dp.name = 'CMPL'  
AND df.name = 'CLOSE'  
AND ot.name = 'LCLOSE';

select * from TSS_TIME_SERIES_DATA;