
alter table tss_identifier 
  add constraint rsk_chk_uq_identifier 
    unique (identification_scheme_id, identifier_value, valid_from, valid_to);
    