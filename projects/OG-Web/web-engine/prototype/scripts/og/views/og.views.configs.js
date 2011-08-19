/*
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.views.configs',
    dependencies: [
        'og.api.rest',
        'og.api.text',
        'og.common.masthead.menu',
        'og.common.routes',
        'og.common.search_results.core',
        'og.common.util.history',
        'og.common.util.ui.dialog',
        'og.common.util.ui.message',
        'og.common.util.ui.toolbar',
        'og.views.common.layout',
        'og.views.common.versions',
        'og.views.common.state',
        'og.views.common.default_details',
        'og.views.configs.viewdefinition',
        'og.views.configs.yieldcurvedefinition',
        'og.views.configs.curvespecificationbuilderconfiguration',
        'og.views.configs.default'
    ],
    obj: function () {
        var api = og.api,
            common = og.common,
            details = common.details,
            history = common.util.history,
            masthead = common.masthead,
            routes = common.routes,
            search,
            ui = common.util.ui,
            module = this,
            page_name = module.name.split('.').pop(),
            check_state = og.views.common.state.check.partial('/' + page_name),
            configs,
            config_types = [], // used to populate the dropdown in the new button
            toolbar_buttons = {
                'new': function () {ui.dialog({
                    type: 'input',
                    title: 'Add configuration',
                    fields: [
                        {type: 'select', name: 'Configuration Type', id: 'config_type', options: config_types}
                    ],
                    buttons: {
                        'OK': function () {
                            var config_type = ui.dialog({return_field_value: 'config_type'}),
                                args = $.extend({}, routes.last().args, {config_type: config_type});
                            $(this).dialog('close');
                            routes.go(routes.hash(module.rules.load_new, args));
                        }
                    }
                })},
                'delete': function () {ui.dialog({
                    type: 'confirm',
                    title: 'Delete configuration?',
                    message: 'Are you sure you want to permanently delete this configuration?',
                    buttons: {
                        'Delete': function () {
                            var args = routes.current().args;
                            $(this).dialog('close');
                            api.rest.configs.del({
                                handler: function (result) {
                                    if (result.error) return ui.dialog({type: 'error', message: result.message});
                                    delete args.id;
                                    configs.search(args);
                                    routes.go(routes.hash(module.rules.load, args));
                                }, id: routes.current().args.id
                            });
                        }
                    }
                })}
            },
            options = {
                slickgrid: {
                    'selector': '.OG-js-search', 'page_type': 'configs',
                    'columns': [
                        {
                            id: 'type', toolTip: 'type', name: null, field: 'type', width: 100
                        },
                        {id: 'name', field: 'name', width: 300, cssClass: 'og-link', toolTip: 'name',
                            name: '<input type="text" placeholder="Name" '
                                + 'class="og-js-name-filter" style="width: 280px;">'
                        }
                    ]
                },
                toolbar: {
                    'default': {
                        buttons: [
                            {name: 'delete', enabled: 'OG-disabled'},
                            {name: 'new', handler: toolbar_buttons['new']}
                        ],
                        location: '.OG-toolbar'
                    },
                    active: {
                        buttons: [
                            {name: 'delete', handler: toolbar_buttons['delete']},
                            {name: 'new', handler: toolbar_buttons['new']}
                        ],
                        location: '.OG-toolbar'
                    }
                }
            },
            form_generators = {
                'default': og.views.configs['default'],
                viewdefinition: og.views.configs.viewdefinition,
                yieldcurvedefinition: og.views.configs.yieldcurvedefinition,
                curvespecificationbuilderconfiguration: og.views.configs.curvespecificationbuilderconfiguration
            },
            toolbar = function (type) {
                ui.toolbar(options.toolbar[type]);
                if (!config_types.length) // disable new button until we have config_types
                    $('.OG-toolbar .og-js-new').addClass('OG-disabled').unbind();
                api.rest.configs.get({
                    meta: true,
                    handler: function (result) {
                        config_types = result.data.types.sort().map(function (val) {return {name: val, value: val};});
                        $('.OG-toolbar .og-js-new').removeClass('OG-disabled').click(toolbar_buttons['new']);
                    },
                    cache_for: 15 * 1000
                });
            },
            // toolbar here relies on dynamic data, so it is instantiated with a callback instead of having
            // options passed in (options is set to null for default_details)
            default_details = og.views.common.default_details
                .partial(page_name, 'Configurations', null, toolbar.partial('default')),
            details_page = function (args, new_config_type) {
                var rest_options, is_new = !!new_config_type, rest_handler = function (result) {
                    if (result.error) return alert(result.message);
                    if (is_new) {
                        if (!result.data) result.data = {template_data: {type: new_config_type, configJSON: {}}};
                        if (!result.data.template_data.configJSON) result.data.template_data.configJSON = {};
                        result.data.template_data.name = 'UNTITLED';
                        result.data.template_data.configJSON.name = 'UNTITLED';
                    }
                    var details_json = result.data,
                        config_type = details_json.template_data.type.toLowerCase(),
                        template = module.name + '.' + config_type, text_handler;
                    if (!is_new) history.put({
                        name: details_json.template_data.name,
                        item: 'history.configs.recent',
                        value: routes.current().hash
                    });
                    (form_generators[config_type] || form_generators['default'])({
                        is_new: is_new,
                        data: details_json,
                        loading: function () {
                            ui.message({location: '.ui-layout-inner-center', message: 'saving...'});
                        },
                        save_new_handler: function (result) {
                            var args = $.extend({}, routes.last().args, {id: result.meta.id});
                            ui.message({location: '.OG-js-details-panel', destroy: true});
                            if (result.error) return ui.dialog({type: 'error', message: result.message});
                            configs.search(args);
                            routes.go(routes.hash(module.rules.load_configs, args));
                        },
                        save_handler: function (result) {
                            ui.message({location: '.ui-layout-inner-center', destroy: true});
                            if (result.error) return ui.dialog({type: 'error', message: result.message});
                            ui.message({location: '.ui-layout-inner-center', message: 'saved'});
                            setTimeout(function () {
                                routes.handler();
                            }, 300);
                        },
                        handler: function () {
                            var json = details_json.template_data,
                                error_html = '\
                                    <section class="OG-box og-box-glass og-box-error OG-shadow-light">\
                                        This configuration has been deleted\
                                    </section>\
                                ',
                                layout = og.views.common.layout;
                            toolbar('active');
                            if (json && json.deleted) {
                                $('.ui-layout-inner-north').html(error_html);
                                layout.inner.sizePane('north', '0');
                                layout.inner.open('north');
                                $('.OG-toolbar .og-js-delete').addClass('OG-disabled').unbind();
                            } else {
                                if (is_new) $('.OG-toolbar .og-js-delete').addClass('OG-disabled').unbind();
                                layout.inner.close('north');
                                $('.ui-layout-inner-north').empty();
                            }
                            ui.message({location: '.ui-layout-inner-center', destroy: true});
                            layout.inner.resizeAll();
                        },
                        selector: '.ui-layout-inner-center .ui-layout-content'
                    });
                    if (!(config_type in form_generators))
                        og.dev.warn('using default config template for config type: ' + config_type);
                };
                rest_options = {
                    handler: rest_handler,
                    loading: function () {
                        if (!og.views.common.layout.inner.state.south.isClosed) {og.views.common.versions()}
                        ui.message({
                            location: '.ui-layout-inner-center', message: {0: 'loading...', 3000: 'still loading...'}
                        });
                    }
                };
                if (new_config_type) rest_options.template = new_config_type; else rest_options.id = args.id;
                api.rest.configs.get(rest_options);
            };
        module.rules = {
            load: {route: '/' + page_name + '/name:?/type:?', method: module.name + '.load'},
            load_configs: {route: '/' + page_name + '/:id/name:?/type:?', method: module.name + '.load_' + page_name},
            load_filter: {route: '/' + page_name + '/filter:/:id?/name:?/type:?', method: module.name + '.load_filter'},
            load_new: {route: '/' + page_name + '/new/:config_type/name:?/type:?', method: module.name + '.load_new'}
        };
        return configs = {
            load: function (args) {
                check_state({args: args, conditions: [
                    {new_page: function (args) {
                        configs.search(args);
                        masthead.menu.set_tab(page_name);
                    }}
                ]});
                if (!args.id && !args.config_type) default_details();
            },
            load_filter: function (args) {
                var search_filter = function () {
                    var filter_name = options.slickgrid.columns[0].name;
                    if (!filter_name || filter_name === 'loading') // wait until type filter is populated
                        return setTimeout(search_filter, 500);
                    search.filter($.extend(args, {filter: true}));
                };
                check_state({args: args, conditions: [{new_page: function () {
                    if (args.id) return configs.load_configs(args);
                    configs.load(args);
                }}]});
                search_filter();
            },
            load_configs: function (args) {
                check_state({args: args, conditions: [{new_page: configs.load}]});
                configs.details(args);
            },
            load_new: function (args) {
                check_state({args: args, conditions: [{new_page: configs.load}]});
                details_page(args, args.config_type);
            },
            search: function (args) {
                if (!search) search = common.search_results.core();
                if (options.slickgrid.columns[0].name === 'loading')
                    return setTimeout(configs.search.partial(args), 500);
                if (options.slickgrid.columns[0].name === null) return api.rest.configs.get({
                    meta: true,
                    handler: function (result) {
                        options.slickgrid.columns[0].name = [
                            '<select class="og-js-type-filter" style="width: 80px">',
                            result.data.types.sort().reduce(function (acc, type) {
                                return acc + '<option value="' + type + '">' + type + '</option>';
                            }, '<option value="">Type</option>'),
                            '</select>'
                        ].join('');
                        configs.search(args);
                    },
                    loading: function () {options.slickgrid.columns[0].name = 'loading';},
                    cache_for: 15 * 1000
                });
                search.load($.extend(options.slickgrid, {url: args}));
            },
            details: details_page,
            init: function () {for (var rule in module.rules) routes.add(module.rules[rule]);},
            rules: module.rules
        };
    }
});
