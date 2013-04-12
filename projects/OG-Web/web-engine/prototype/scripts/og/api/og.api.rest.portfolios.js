/*
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.api.rest.portfolios',
    dependencies: ['og.api.common', 'og.api.rest'],
    obj: function () {
        var common = og.api.common, api = og.api.rest, str = common.str, check = common.check;
        return { // all requests that begin with /portfolios
            root: 'portfolios',
            get: function (config) {
                config = config || {};
                var root = this.root, method = [root], data = {}, meta,
                    id = str(config.id), node = str(config.node), version = str(config.version),
                    name = str(config.name), name_search =  config.name, version_search = version === '*',
                    ids = config.ids, id_search = ids && $.isArray(ids) && ids.length,
                    nodes = config.nodes, node_search = nodes && $.isArray(nodes) && nodes.length,
                    search = !id || id_search || node_search || name_search || version_search;
                meta = check({
                    bundle: {method: root + '#get', config: config},
                    dependencies: [{fields: ['node', 'version'], require: 'id'}],
                    required: [{condition: version_search, one_of: ['id', 'node']}],
                    empties: [
                        {
                            condition: name_search || id_search || node_search,
                            label: 'search request cannot have id, node, or version',
                            fields: ['id', 'node', 'version']
                        },
                        {label: 'meta data unavailable for /' + root, fields: ['meta']}
                    ]
                });
                if (search) data = common.paginate(config);
                if (name_search) data.name = name;
                if (id_search) data['portfolioId'] = ids;
                if (node_search) data['nodeId'] = nodes;
                version = version ? [id, 'versions', version_search ? false : version].filter(Boolean) : id;
                if (id) method = method.concat(version);
                if (node) method.push('nodes', node);
                return api.request(method, {data: data, meta: meta});
            },
            put: function (config) {
                config = config || {};
                var root = this.root, method = [root], data = {}, meta,
                    name = str(config.name), id = str(config.id), version = str(config.version),
                    node = str(config.node), new_node = config['new'], position = str(config.position);
                meta = check({
                    bundle: {method: root + '#put', config: config},
                    required: [{one_of: ['name', 'id']}, {one_of: ['name', 'position']}],
                    dependencies: [
                        {fields: ['node', 'version', 'position'], require: 'id'},
                        {fields: ['position'], require: 'node'}
                    ],
                    empties: [
                        {condition: !!name, label: 'name exists', fields: ['position']},
                        {condition: new_node, label: 'node is not set to an ID', fields: ['position']}
                    ]
                });
                meta.type = !id || new_node || position ? 'POST' : 'PUT';
                if (name) data.name = name;
                if (id) method = method.concat(version ? [id, 'versions', version] : id);
                if (new_node || node) method = method.concat(new_node && !node ? 'nodes' : ['nodes', node]);
                if (position) method.push('positions'), data.uid = position;
                return api.request(method, {data: data, meta: meta});
            },
            del: function (config) {
                config = config || {};
                var root = this.root, method = [root], meta,
                    id = str(config.id), version = str(config.version),
                    node = str(config.node), position = str(config.position);
                meta = check({
                    bundle: {method: root + '#del', config: config},
                    required: [{all_of: ['id']}],
                    dependencies: [
                        {fields: ['node', 'version', 'position'], require: 'id'},
                        {fields: ['position'], require: 'node'}
                    ]
                });
                meta.type = 'DELETE';
                method = method.concat(version ? [id, 'versions', version] : id);
                if (node) method.push('nodes', node);
                if (position) method.push('positions', position);
                return api.request(method, {data: {}, meta: meta});
            }
        };
    }
});