/*
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.api.rest.configs',
    dependencies: ['og.api.common', 'og.api.rest'],
    obj: function () {
        var common = og.api.common, api = og.api.rest, str = common.str, check = common.check;
        return { // all requests that begin with /configs
            root: 'configs',
            get: function (config) {
                config = config || {};
                var root = this.root, method = [root], data = {}, meta,
                    id = str(config.id), version = str(config.version), version_search = version === '*',
                    fields = ['name', 'type'], field_search = fields.some(function (val) {return val in config;}),
                    all = fields.concat('id', 'version', 'page_size', 'page', 'from', 'to'),
                    ids = config.ids, id_search = ids && $.isArray(ids) && ids.length,
                    meta_request = config.meta, template = str(config.template);
                meta = check({
                    bundle: {method: root + '#get', config: config},
                    dependencies: [{fields: ['version'], require: 'id'}],
                    empties: [
                        {condition: template, label: 'template request', fields: all.concat('meta')},
                        {condition: field_search || id_search, label: 'search request', fields: ['version', 'id']},
                        {condition: meta_request, label: 'meta data request', fields: all}
                    ]
                });
                if (meta_request) method.push('metaData');
                if (!meta_request && !template && (field_search || version_search || id_search || !id))
                    data = common.paginate(config);
                if (field_search) fields
                    .forEach(function (val, idx) {if (val = str(config[val])) data[fields[idx]] = val;});
                if (data.type === '*') delete data.type; // * is superfluous here
                if (id_search) data['configId'] = ids;
                if (template) method.push('templates', template);
                if (id) method = method.concat(version ? [id, 'versions', version_search ? '' : version] : id);
                return api.request(method, {data: data, meta: meta});
            },
            put: function (config) {
                config = config || {};
                var root = this.root, method = [root], data = {}, meta,
                    id = str(config.id), fields = ['name', 'json', 'type', 'xml'],
                    api_fields = ['name', 'configJSON', 'type', 'configXML'];
                meta = check({
                    bundle: {method: root + '#put', config: config},
                    empties: [{
                        condition: !!config.json, label: 'json and xml are mutually exclusive', fields: ['xml']
                    }]
                });
                meta.type = id ? 'PUT' : 'POST';
                fields.forEach(function (val, idx) {if (val = str(config[val])) data[api_fields[idx]] = val;});
                if (id) method.push(id);
                return api.request(method, {data: data, meta: meta});
            },
            del: api.default_del
        };
    }
});