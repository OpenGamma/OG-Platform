/*
 * Copyright 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
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
        'og.views.common.state',
        'og.views.config_forms.default'
    ],
    obj: function () {
        var api = og.api, common = og.common, details = common.details, events = common.events,
            history = common.util.history, masthead = common.masthead, routes = common.routes,
            form_inst, form_state, unsaved_txt = 'You have unsaved changes to', toolbar_action = false,
            search, suppress_update = false, ui = common.util.ui,
            module = this, view,
            page_name = module.name.split('.').pop(),
            current_type, config_types = [], // used to populate the dropdown in the new button
            toolbar_buttons = {
                'new': function () {
                    toolbar_action = true;
                    ui.dialog({
                        type: 'input',
                        title: 'Add configuration',
                        width: 400,
                        height: 190,
                        fields: [{type: 'optselect', name: 'Configuration Type', id: 'config_type', options: config_types,
                            value: function () {return current_type; }}],
                        buttons: {
                            'OK': function () {
                                var config_type = ui.dialog({return_field_value: 'config_type'});
                                $(this).dialog('close');
                                routes.go(routes.hash(view.rules.load_new, routes.current().args, {
                                    add: {config_type: config_type}
                                }));
                            },
                            'Cancel': function () {$(this).dialog('close'); }
                        }
                    });
                },
                'delete': function () {
                    toolbar_action = true;
                    ui.dialog({
                        type: 'confirm',
                        title: 'Delete configuration?',
                        width: 400,
                        height: 190,
                        message: 'Are you sure you want to permanently delete this configuration?',
                        buttons: {
                            'Delete': function () {
                                var args = routes.current().args;
                                suppress_update = true;
                                form_inst = form_state = null;
                                $(this).dialog('close');
                                api.rest.configs.del({
                                    handler: function (result) {
                                        if (result.error) {
                                            return view.error(result.message);
                                        }
                                        routes.go(routes.hash(view.rules.load, args, {del: ['id']}));
                                        setTimeout(function () {view.search(args); });
                                    },
                                    id: routes.current().args.id
                                });
                            },
                            'Cancel': function () {$(this).dialog('close'); }
                        }
                    });
                }
            },
            toolbar = function (options) {
                ui.toolbar(options);
                if (config_types.length) {
                    return; // if we already have config_types, return
                }
                $('.OG-tools .og-js-new').addClass('OG-disabled').unbind();
                api.rest.configs.get({
                    meta: true,
                    handler: function (result) {
                        config_types = result.data.groups;
                        ui.toolbar(options);
                    },
                    cache_for: 60 * 60 * 1000 // an hour
                });
            },
            details_page = function (args, new_config_type) {
                var rest_options, is_new = !!new_config_type, rest_handler;
                rest_handler = function (result) {
                    var details_json = result.data, too_large = result.meta.content_length > 0.75 * 1024 * 1024,
                        config_type, render_type, render_options;
                    if (result.error) {
                        view.notify(null);
                        return view.error(result.message);
                    }
                    current_type = details_json.template_data.type.split('.').reverse()[0];
                    config_type = current_type.toLowerCase();
                    if (is_new) {
                        if (!result.data) {
                            return view.error('No template for: ' + new_config_type);
                        }
                        if (!result.data.template_data.configJSON) {
                            result.data.template_data.configJSON = {};
                        }
                        result.data.template_data.name = 'UNTITLED';
                        result.data.template_data.configJSON.name = 'UNTITLED';
                    }
                    if (!is_new) {
                        history.put({name: details_json.template_data.name, item: 'history.' + page_name + '.recent',
                            value: routes.current().hash });
                    }
                    render_type = config_type;
                    if (too_large && !og.views.config_forms[config_type].is_default) {
                        view.error('This configuration is using the default form because it contains too much data (' +
                            result.meta.content_length + ' bytes)');
                        render_type = 'default';
                    }
                    if (!og.views.config_forms[config_type]) {
                        render_type = 'default';
                    }
                    render_options = {
                        is_new: is_new,
                        data: details_json,
                        loading: view.notify.partial('saving...'),
                        save_new_handler: function (result) {
                            var args = routes.current().args;
                            view.notify(null);
                            if (result.error) {
                                return view.error(result.message);
                            }
                            toolbar_action = true;
                            view.search(args);
                            routes.go(routes.hash(view.rules.load_item, routes.current().args, {
                                add: {id: result.meta.id}
                            }));
                        },
                        save_handler: function (result) {
                            var args = routes.current().args;
                            view.notify(null);
                            if (result.error) {
                                return view.error(result.message);
                            }
                            view.notify('saved');
                            setTimeout(function () {
                                view.notify(null);
                                view.search(args);
                                view.details(args);
                            }, 300);
                        },
                        handler: function (form) {
                            var json = details_json.template_data,
                                error_html = '\
                                    <section class="OG-box og-box-glass og-box-error OG-shadow-light">\
                                        This configuration has been deleted\
                                    </section>';
                            if (json.deleted) {
                                $('.OG-layout-admin-details-north').html(error_html);
                                view.layout.inner.sizePane('north', '0');
                                view.layout.inner.open('north');
                            } else {
                                view.layout.inner.close('north');
                                $('.OG-layout-admin-details-north').empty();
                            }
                            if (is_new || json.deleted) {
                                toolbar({
                                    buttons: [
                                        {id: 'new', tooltip: 'New', handler: toolbar_buttons['new']},
                                        {id: 'import', tooltip: 'Import', enabled: 'OG-disabled'},
                                        {id: 'save', tooltip: 'Save', handler: form.submit.partial({as_new: true})},
                                        {id: 'saveas', tooltip: 'Save as', enabled: 'OG-disabled'},
                                        {id: 'delete', tooltip: 'Delete', enabled: 'OG-disabled'}
                                    ],
                                    location: '.OG-tools'
                                });
                            } else {
                                toolbar({
                                    buttons: [
                                        {id: 'new', tooltip: 'New', handler: toolbar_buttons['new']},
                                        {id: 'import', tooltip: 'Import', enabled: 'OG-disabled'},
                                        {id: 'save', tooltip: 'Save', handler: function () {
                                            suppress_update = true;
                                            form.submit();
                                        }},
                                        {id: 'saveas', tooltip: 'Save as', handler: form.submit.partial({as_new: true})},
                                        {id: 'delete', tooltip: 'Delete', handler: toolbar_buttons['delete']}
                                    ],
                                    location: '.OG-tools'
                                });
                            }
                            view.notify(null);
                            setTimeout(view.layout.inner.resizeAll);
                            form_inst = form;
                            form_state = form_inst.compile();
                        },
                        selector: '.OG-layout-admin-details-center .ui-layout-content',
                        type: details_json.template_data.type
                    };
                    $(render_options.selector).css({'overflow': 'auto'});
                    og.views.config_forms[render_type](render_options);
                };
                view.layout.inner.options.south.onclose = null;
                view.layout.inner.close('south');
                rest_options = {
                    dependencies: view.dependencies,
                    update: is_new ? (void 0) : view.update,
                    handler: rest_handler,
                    loading: view.notify.partial({0: 'loading...', 3000: 'still loading...'})
                };
                if (new_config_type) {
                    rest_options.template = new_config_type;
                } else {
                    rest_options.id = args.id;
                }
                api.rest.configs.get(rest_options);
            };
        events.on('hashchange', function () {
            if (!form_inst && !form_state || toolbar_action) {
                toolbar_action = false;
                return void 0;
            }
            var msg = unsaved_txt + ' ' + form_state.data.name + '. \n\n' +
                'OK to discard changes \n' +
                'Cancel to continue editing';
            if (!Object.equals(form_state, form_inst.compile()) && !window.confirm(msg)) {
                return false;
            }
            form_inst = form_state = null;
            return true;
        });
        events.on('unload', function () {
            if (!form_inst && !form_state) {
                return true;
            }
            if (!Object.equals(form_state, form_inst.compile())) {
                return false;
            }
            form_inst = form_state = null;
            return true;
        });
        var build_menu = function (list) {
            var menu_html = '<select class="og-js-type-filter" style="width: 80px">' +
                '<option value="">Type</option>';
            list.forEach(function (entry) {
                menu_html += '<optgroup label="' + entry.group + '">';
                entry.types.forEach(function (type) {
                    menu_html += '<option value="' + type.value + '">' + type.name + '</option>';
                });
                menu_html += '</optgroup>';
            });
            menu_html += "</select>";
            return menu_html;
        };
        return view = $.extend(view = new og.views.common.Core(page_name), {
            default_details: function () {
                // toolbar here relies on dynamic data, so it is instantiated with a callback instead of having
                // options passed in (options is set to null for default_details)
                og.views.common
                    .default_details(page_name, view.name, null, toolbar.partial(view.options.toolbar['default']));
            },
            details: details_page,
            load: function (args) {
                view.layout = og.views.common.layout;
                view.check_state({args: args, conditions: [
                    {new_page: function (args) {
                        view.search(args);
                        masthead.menu.set_tab(page_name);
                    }}
                ]});
                if (!args.id && !args.config_type) {
                    view.default_details();
                }
            },
            load_filter: function (args) {
                view.filter = function () {
                    var filter_name = view.options.slickgrid.columns[0].name;
                    if (!filter_name || filter_name === 'loading') {// wait until type filter is populated
                        return setTimeout(view.filter, 500);
                    }
                    search.filter();
                };
                view.check_state({args: args, conditions: [{new_value: 'id', method: function (args) {
                    view[args.id ? 'load_item' : 'load'](args);
                }}]});
                view.filter();
            },
            load_new: function (args) {
                view.check_state({args: args, conditions: [{new_page: view.load}]});
                view.details(args, args.config_type);
            },
            options: {
                slickgrid: {
                    'selector': '.OG-js-search',
                    'page_type': page_name,
                    'columns': [
                        {id: 'type', toolTip: 'type', name: null, field: 'type', width: 100},
                        {id: 'name', field: 'name', width: 300, cssClass: 'og-link', toolTip: 'name',
                            name: '<input type="text" placeholder="Name" '
                                + 'class="og-js-name-filter" style="width: 280px;">'}
                    ]
                },
                toolbar: {
                    'default': {
                        buttons: [
                            {id: 'new', tooltip: 'New', handler: toolbar_buttons['new']},
                            {id: 'import', tooltip: 'Import', enabled: 'OG-disabled'},
                            {id: 'save', tooltip: 'Save', enabled: 'OG-disabled'},
                            {id: 'saveas', tooltip: 'Save as', enabled: 'OG-disabled'},
                            {id: 'delete', tooltip: 'Delete', enabled: 'OG-disabled'}
                        ],
                        location: '.OG-tools'
                    }
                }
            },
            rules: $.extend(view.rules(['name', 'type']), {
                load_new: { // configs have a rule other views don't have, so we need to extend the default set
                    route: '/' + page_name + '/new/:config_type/name:?/type:?',
                    method: module.name + '.load_new'
                }
            }),
            search: function (args) {
                if (!search) {
                    search = common.search_results.core();
                }
                if (view.options.slickgrid.columns[0].name === 'loading') {
                    return setTimeout(view.search.partial(args), 500);
                }
                if (view.options.slickgrid.columns[0].name === null) {
                    return api.rest.configs.get({
                        meta: true,
                        dependencies: [], // if the page changes, cancel this request
                        handler: function (result) {
                            if (result.error) {
                                return view.error(result.message);
                            }
                            view.options.slickgrid.columns[0].name = build_menu(result.data.groups);
                            view.search(args);
                        },
                        loading: function () {
                            view.options.slickgrid.columns[0].name = 'loading';
                            ui.message({location: '.OG-js-search', message: {0: 'loading...', 3000: 'still loading...'}});
                        },
                        cache_for: 15 * 1000
                    });
                }
                search.load(view.options.slickgrid);
            },
            update: function (delivery) {
                view.search(routes.current().args);
                if (suppress_update) {
                    return suppress_update = false;
                }
                ui.dialog({
                    type: 'confirm',
                    width: 400,
                    height: 190,
                    title: delivery.reset ? 'The connection has been reset!' : 'This item has been updated!',
                    message: 'Would you like to refresh this page (all changes will be lost) or continue working?',
                    buttons: {
                        'Refresh': function () {
                            $(this).dialog('close');
                            view.details(routes.current().args);
                        },
                        'Continue Working': function () {
                            $(this).dialog('close');
                            og.api.rest.configs.get({id: routes.current().args.id, update: view.update,
                                dependencies: view.dependencies });
                        }
                    }
                });
            }
        });
    }
});
