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
        'og.views.config_forms.viewdefinition',
        'og.views.config_forms.yieldcurvedefinition',
        'og.views.config_forms.curvespecificationbuilderconfiguration',
        'og.views.config_forms.volatilitycubedefinition',
        'og.views.config_forms.default'
    ],
    obj: function () {
        var api = og.api,
            common = og.common,
            details = common.details,
            history = common.util.history,
            masthead = common.masthead,
            routes = common.routes,
            search, layout,
            ui = common.util.ui,
            module = this,
            page_name = module.name.split('.').pop(),
            check_state = og.views.common.state.check.partial('/' + page_name),
            view,
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
                                    view.search(args);
                                    routes.go(routes.hash(module.rules.load, args));
                                }, id: routes.current().args.id
                            });
                        }
                    }
                })}
            },
            options = {
                slickgrid: {
                    'selector': '.OG-js-search', 'page_type': page_name,
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
                            {id: 'new', tooltip: 'New', handler: toolbar_buttons['new']},
                            {id: 'save', tooltip: 'Save', enabled: 'OG-disabled'},
                            {id: 'saveas', tooltip: 'Save as', enabled: 'OG-disabled'},
                            {id: 'delete', tooltip: 'Delete', enabled: 'OG-disabled'}
                        ],
                        location: '.OG-tools'
                    }
                }
            },
            toolbar = function (options) {
                ui.toolbar(options);
                if (config_types.length) return; // if we already have config_types, return
                $('.OG-tools .og-js-new').addClass('OG-disabled').unbind();
                api.rest.configs.get({
                    meta: true,
                    handler: function (result) {
                        config_types = result.data.types.sort().map(function (val) {return {name: val, value: val};});
                        ui.toolbar(options);
                    },
                    cache_for: 60 * 60 * 1000 // an hour
                });
            },
            // toolbar here relies on dynamic data, so it is instantiated with a callback instead of having
            // options passed in (options is set to null for default_details)
            default_details = og.views.common.default_details
                .partial(page_name, 'Configurations', null, toolbar.partial(options.toolbar['default'])),
            details_page = function (args, new_config_type) {
                var rest_options, is_new = !!new_config_type, rest_handler;
                rest_handler = function (result) {
                    var details_json = result.data, too_large = result.meta.content_length > 0.75 * 1024 * 1024,
                        config_type = details_json.template_data.type.toLowerCase().split('.').reverse()[0],
                        render_type, render_options;
                    if (result.error) return ui.dialog({type: 'error', message: result.message});
                    if (is_new) {
                        if (!result.data)
                            return ui.dialog({type: 'error', message: 'No template for: ' + new_config_type});
                        if (!result.data.template_data.configJSON) result.data.template_data.configJSON = {};
                        result.data.template_data.name = 'UNTITLED';
                        result.data.template_data.configJSON.name = 'UNTITLED';
                    }
                    if (!is_new) history.put({
                        name: details_json.template_data.name,
                        item: 'history.' + page_name + '.recent',
                        value: routes.current().hash
                    });
                    if (!og.views.config_forms[config_type]) {
                        ui.message({location: '.ui-layout-inner-center', destroy: true});
                        return ui.dialog({type: 'error', message: 'No renderer for: ' + config_type});
                    }
                    if (too_large && !og.views.config_forms[config_type].is_default) ui.dialog({
                        type: 'error',
                        message: 'This configuration is using the default form because it contains too much data (' +
                            result.meta.content_length + ' bytes)'
                    });
                    render_type = too_large ? 'default' : config_type;
                    render_options = {
                        is_new: is_new,
                        data: details_json,
                        loading: function () {
                            ui.message({location: '.ui-layout-inner-center', message: 'saving...'});
                        },
                        save_new_handler: function (result) {
                            var args = $.extend({}, routes.current().args, {id: result.meta.id});
                            ui.message({location: '.OG-js-details-panel', destroy: true});
                            if (result.error) {
                                ui.message({location: '.ui-layout-inner-center', destroy: true});
                                return ui.dialog({type: 'error', message: result.message});
                            }
                            view.search(args);
                            routes.go(routes.hash(module.rules.load_item, args));
                        },
                        save_handler: function (result) {
                            var args = routes.current().args;
                            if (result.error) {
                                ui.message({location: '.ui-layout-inner-center', destroy: true});
                                return ui.dialog({type: 'error', message: result.message});
                            }
                            ui.message({location: '.ui-layout-inner-center', message: 'saved'});
                            setTimeout(function () {view.search(args), details_page(args);}, 300);
                        },
                        handler: function (form) {
                            var json = details_json.template_data,
                                error_html = '\
                                    <section class="OG-box og-box-glass og-box-error OG-shadow-light">\
                                        This configuration has been deleted\
                                    </section>';
                            if (json.deleted) {
                                $('.ui-layout-inner-north').html(error_html);
                                layout.inner.sizePane('north', '0');
                                layout.inner.open('north');
                            } else {
                                layout.inner.close('north');
                                $('.ui-layout-inner-north').empty();
                            }
                            if (is_new || json.deleted) toolbar({
                                buttons: [
                                    {id: 'new', tooltip: 'New', handler: toolbar_buttons['new']},
                                    {id: 'save', tooltip: 'Save', handler: form.submit.partial({as_new: true})},
                                    {id: 'saveas', tooltip: 'Save as', enabled: 'OG-disabled'},
                                    {id: 'delete', tooltip: 'Delete', enabled: 'OG-disabled'}
                                ],
                                location: '.OG-tools'
                            }); else toolbar({
                                buttons: [
                                    {id: 'new', tooltip: 'New', handler: toolbar_buttons['new']},
                                    {id: 'save', tooltip: 'Save', handler: form.submit},
                                    {id: 'saveas', tooltip: 'Save as', handler: form.submit.partial({as_new: true})},
                                    {id: 'delete', tooltip: 'Delete', handler: toolbar_buttons['delete']}
                                ],
                                location: '.OG-tools'
                            });
                            ui.message({location: '.ui-layout-inner-center', destroy: true});
                            setTimeout(layout.inner.resizeAll);
                        },
                        selector: '.ui-layout-inner-center .ui-layout-content',
                        type: details_json.template_data.type
                    };
                    if (render_type !== config_type)
                        render_options.type_map = og.views.config_forms[config_type].type_map;
                    og.views.config_forms[render_type](render_options);
                };
                layout.inner.options.south.onclose = null;
                layout.inner.close('south');
                rest_options = {
                    dependencies: ['id'],
                    handler: rest_handler,
                    loading: function () {
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
            load_item: {route: '/' + page_name + '/:id/name:?/type:?', method: module.name + '.load_item'},
            load_filter: {route: '/' + page_name + '/filter:/:id?/name:?/type:?', method: module.name + '.load_filter'},
            load_new: {route: '/' + page_name + '/new/:config_type/name:?/type:?', method: module.name + '.load_new'}
        };
        return view = {
            load: function (args) {
                layout = og.views.common.layout;
                check_state({args: args, conditions: [
                    {new_page: function (args) {view.search(args), masthead.menu.set_tab(page_name);}}
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
                check_state({args: args, conditions: [{new_value: 'id', method: function (args) {
                    view[args.id ? 'load_item' : 'load'](args);
                }}]});
                search_filter();
            },
            load_item: function (args) {
                check_state({args: args, conditions: [{new_page: view.load}]});
                view.details(args);
            },
            load_new: function (args) {
                check_state({args: args, conditions: [{new_page: view.load}]});
                view.details(args, args.config_type);
            },
            search: function (args) {
                if (!search) search = common.search_results.core();
                if (options.slickgrid.columns[0].name === 'loading')
                    return setTimeout(view.search.partial(args), 500);
                if (options.slickgrid.columns[0].name === null) return api.rest.configs.get({
                    meta: true,
                    handler: function (result) {
                        if (result.error) return ui.dialog({type: 'error', message: result.message});
                        options.slickgrid.columns[0].name = [
                            '<select class="og-js-type-filter" style="width: 80px">',
                            result.data.types.sort().reduce(function (acc, type) {
                                return acc + '<option value="' + type + '">' + type + '</option>';
                            }, '<option value="">Type</option>'),
                            '</select>'
                        ].join('');
                        view.search(args);
                    },
                    loading: function () {
                        options.slickgrid.columns[0].name = 'loading';
                        ui.message({location: '.OG-js-search', message: {0: 'loading...', 3000: 'still loading...'}});
                    },
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
