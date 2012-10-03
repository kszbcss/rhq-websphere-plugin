--
-- RHQ WebSphere Plug-in
-- Copyright (C) 2012 Crossroads Bank for Social Security
-- All rights reserved.
--
-- This program is free software; you can redistribute it and/or modify
-- it under the terms of the GNU General Public License, version 2, as
-- published by the Free Software Foundation, and/or the GNU Lesser
-- General Public License, version 2.1, also as published by the Free
-- Software Foundation.
--
-- This program is distributed in the hope that it will be useful,
-- but WITHOUT ANY WARRANTY; without even the implied warranty of
-- MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
-- GNU General Public License and the GNU Lesser General Public License
-- for more details.
--
-- You should have received a copy of the GNU General Public License
-- and the GNU Lesser General Public License along with this program;
-- if not, write to the Free Software Foundation, Inc.,
-- 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
--

create or replace view v_messaging_destination_ref (map_id, resource_id, name, binding_name, bus_name, destination_name) as
    select map.id,
           res.id,
           (select string_value from rhq_config_property where parent_map_id=map.id and name='name'),
           (select string_value from rhq_config_property where parent_map_id=map.id and name='bindingName'),
           (select string_value from rhq_config_property where parent_map_id=map.id and name='busName'),
           (select string_value from rhq_config_property where parent_map_id=map.id and name='destinationName')
        from rhq_config_property as map, rhq_config_property as list, rhq_resource res
        where map.name='messagingDestinationRef' and map.parent_list_id=list.id and list.configuration_id=res.res_configuration_id;

create or replace view v_message_listener (resource_id, activation_spec_jndi_name, destination_jndi_name, bus_name, destination_name, max_concurrency) as
    select r.id,
           (select string_value from rhq_config_property where configuration_id=c.id and name='activationSpecJndiName'),
           (select string_value from rhq_config_property where configuration_id=c.id and name='destinationJndiName'),
           (select string_value from rhq_config_property where configuration_id=c.id and name='busName'),
           (select string_value from rhq_config_property where configuration_id=c.id and name='destinationName'),
           (select string_value from rhq_config_property where configuration_id=c.id and name='maxConcurrency')
        from rhq_resource as r, rhq_config as c
        where resource_type_id=(select id from rhq_resource_type where plugin='WebSphere' and name='Message Driven Bean')
          and r.res_configuration_id=c.id
          and r.inventory_status='COMMITTED';

create or replace view v_sib_resource (resource_id, bus_name, destination_name, type) as
    select resource_id, bus_name, destination_name, 'REF' from v_messaging_destination_ref
    union
    select resource_id, bus_name, destination_name, 'MDB' from v_message_listener;

create or replace view v_server (resource_id, cell, app_target) as
    select r.id,
           (string_to_array(r.resource_key, '/'))[1],
           case when p.string_value is null then (string_to_array(r.resource_key, '/'))[3] else p.string_value end
        from rhq_resource as r, rhq_config_property as p
        where resource_type_id=(select id from rhq_resource_type where plugin='WebSphere' and name='WebSphere Server')
          and p.configuration_id=r.res_configuration_id
          and p.name='clusterName';

-- The following view links an EJB or WAR module to its application. It is useful to process the results from v_messaging_destination_ref:

create or replace view v_application_link (component_resource_id, application_resource_id) as
    select r.id, pp.id
      from rhq_resource as r, rhq_resource as p, rhq_resource pp
     where r.resource_type_id in (select id from rhq_resource_type where plugin='WebSphere' and name in ('Stateless Session Bean', 'Message Driven Bean'))
       and r.parent_resource_id=p.id
       and p.parent_resource_id=pp.id
    union
    select r.id, p.id
      from rhq_resource as r, rhq_resource as p
     where r.resource_type_id=(select id from rhq_resource_type where plugin='WebSphere' and name='Web module')
       and r.parent_resource_id=p.id;

create or replace view v_sib_usage (cell, app_target, application_name, bus_name, destination_name, usage_type) as
    select distinct s.cell, s.app_target, a.name, r.bus_name, r.destination_name, r.type
      from v_sib_resource as r, v_application_link as l, rhq_resource as a, v_server as s
     where r.resource_id=l.component_resource_id
       and l.application_resource_id=a.id
       and a.parent_resource_id=s.resource_id
    order by s.cell, s.app_target, a.name, r.bus_name, r.destination_name, r.type;

-- Function to get the qualified resource name:

create or replace function qualified_resource_name(integer) returns varchar(500) as $$
WITH RECURSIVE
resource(id , parentid , name) AS (select id, parent_resource_id, name from rhq_resource),
temp (id, parentid , parent_list , n) AS (SELECT id,parentid, name, 1 FROM resource
WHERE id = $1
UNION ALL
SELECT temp.id, resource.parentid, cast(resource.name || ' > ' || parent_list as varchar(500)), n + 1 FROM temp, resource
WHERE temp.parentid = resource.id
)
SELECT parent_list
FROM temp order by n desc fetch first row only;
$$ language sql;
