/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.Form',
    dependencies: [
        'og.common.util.ui.AutoCombo',
        'og.analytics.AggregatorsMenu',
        'og.analytics.DatasourcesMenu',
        'og.analytics.Status',
        'og.views.common.layout'
    ],
    obj: function () {

        // Private
        var query = null, template = null, emitter = new EventEmitter(), initialized = false,
            ag_menu = null, ds_menu = null, ac_menu = null, status = null, selector, $dom = {}, vd_s = '.og-view',
            fcntrls_s = 'input, select, a, button', ac_s = 'input autocompletechange autocompleteselect',
            ds_template = null, ag_template = null, viewdefs = null, viewdefs_store = [], aggregators = null,
            ac_data = null, ag_data = null, ds_data = null,
            events = {
                focus: 'dropmenu:focus',
                focused:'dropmenu:focused',
                open: 'dropmenu:open',
                opened: 'dropmenu:opened',
                close: 'dropmenu:close',
                closed: 'dropmenu:closed',
                closeall: 'dropmenu:closeall',
                queryselected: 'dropmenu:queryselected',
                querycancelled: 'dropmenu:querycancelled',
                resetquery:'dropmenu:resetquery'
            };
        var auto_combo_handler = function (event, ui) {
            if (!$dom || !('load_btn' in $dom) || !$dom.load_btn) return;
            /*
                TODO AG: reactivate the load button when a query is replayed
                if ((ui && ui.item && ui.item.value || $(this).val()) !== '') $dom.load_btn.removeClass('og-disabled');
                else $dom.load_btn.addClass('og-disabled');
            */
        };
        var close_dropmenu = function (menu) {
            if (!menu || !ds_menu || !ag_menu) return;
            if (menu === ds_menu) ag_menu.emitEvent(events.close);
            else ds_menu.emitEvent(events.close);
        };
        var start_status = function (event) {
            if (status) status.play();
        };
        var constructor = function (conf) {
            if (!conf) return og.dev.warn('og.analytics.Form: Missing param [conf] to constructor');
            if (!('selector' in conf) || !conf.selector)
                return og.dev.warn('og.analytics.Form: Missing param key [conf.selector] to constructor');
            if (typeof conf.selector !== 'string')
                return og.dev.warn(
                    'og.analytics.Form: Invalid type param key [conf.selector] to constructor; expected "string"'
                );
            var form = this; selector = conf.selector;
            og.views.common.layout.main.allowOverflow('north');
            if (template) init(); else fetch_template(init);
            return form;
        };
        var fetch_template = function (callback) {
            $.when(
                og.api.text({module: 'og.analytics.form_tash'}),
                og.api.text({module: 'og.analytics.form_aggregation_tash'}),
                og.api.text({module: 'og.analytics.form_datasources_tash'}),
                og.api.rest.aggregators.get()
            ).pipe(function (tmpl, ag_tmpl, ds_tmpl, vds, ag_data) {
                if (!tmpl.error) template = tmpl;
                if (!ag_tmpl.error) ag_template = ag_tmpl;
                if (!ds_tmpl.error) ds_template = ds_tmpl;
                if (!vds.error && 'data' in vds && vds.data.length) viewdefs = (viewdefs_store = vds.data).pluck('name');
                if (!ag_data.error && 'data' in ag_data) aggregators = ag_data.data;
                if (callback) callback();
            });
        };
        var get_view_index = function (val, key) {
            var pick;
            if (viewdefs_store && viewdefs_store.length){
                viewdefs_store.forEach(function (entry) {
                    if (entry[key] === val) pick = key === 'id' ? entry.name: entry.id;
                });
            }
            return pick;
        };
        var init = function () {
            if (!selector || !template) return;
            $dom.form = $(selector);
            if ($dom.form) {
                $dom.form.html(template).on('keydown', fcntrls_s, keydown_handler);
                $dom.ag = $('.og-aggregation', $dom.form);
                $dom.ds = $('.og-datasources', $dom.form);
                $dom.load_btn = $('.og-load', $dom.form);
            }
            if (viewdefs) {
                if (ac_data) ac_data = get_view_index(ac_data, 'id');
                ac_menu = new og.common.util.ui.AutoCombo(selector+' '+vd_s, 'search...', viewdefs, ac_data);
                ac_menu.$input.on(ac_s, auto_combo_handler).select();
            }
            if ($dom.ag && ag_template) {
                ag_menu = new og.analytics.AggregatorsMenu({
                    cntr:$dom.ag, tmpl:ag_template, data: aggregators, opts:ag_data
                });
            }
            if ($dom.ds && ds_template) {
                ds_menu = new og.analytics.DatasourcesMenu({cntr:$dom.ds, tmpl:ds_template, opts:ds_data});
            }
            if (ag_menu && ds_menu) {
                [ag_menu, ds_menu].forEach(function (menu) {
                    menu.addListener(events.opened, close_dropmenu)
                        .addListener(events.queryselected, query_selected)
                        .addListener(events.querycancelled, query_cancelled)
                        .addListener(events.resetquery, menu.reset_query);
                });
                emitter.addListener(events.closeall, function () {
                    close_dropmenu(ag_menu);
                    close_dropmenu(ds_menu);
                });
            }
            if ($dom.load_btn) $dom.load_btn.on('click', load_query).on('click', start_status);
            //status = new og.analytics.Status(selector + ' .og-status');
        };
        var load_query = function () {
            if (!ac_menu || !ds_menu) return;
            var id = get_view_index(ac_menu.$input.val(), 'name'), providers = ds_menu.get_query();
            if (!id) return;
            if (!providers) return;
            og.analytics.url.main(query = {
                aggregators: ag_menu ? ag_menu.get_query() : [],
                providers: providers,
                viewdefinition: id
            });
        };
        var keydown_handler = function (event) {
            if (event.keyCode === 13 && $('.OG-analytics-form .ui-autocomplete-input').is(':focus')) load_query();
            if (event.keyCode !== 9) return;
            var $elem = $(this), shift_key = event.shiftKey;
            if (!$elem || !ac_menu || !ag_menu || !ds_menu || !$dom.ag || !$dom.ds) return;
            $dom.ag_fcntrls = $dom.ag.find(fcntrls_s);
            $dom.ds_fcntrls = $dom.ds.find(fcntrls_s);
            if (!shift_key) {
                if (ac_menu.state === 'focused') ag_menu.emitEvent(events.open);
                if ($elem.is($dom.ag_fcntrls.eq(-1))) ds_menu.emitEvent(events.open);
                if ($elem.is($dom.ds_fcntrls.eq(-1))) ds_menu.emitEvent(events.close);
            } else if (shift_key) {
                if ($elem.is($dom.load_btn)) ds_menu.emitEvent(events.open);
                if ($elem.is($dom.ds_fcntrls.eq(0))) ag_menu.emitEvent(events.open);
                if ($elem.is($dom.ag_fcntrls.eq(0))) ag_menu.emitEvent(events.close);
            }
        };
        var query_cancelled = function (menu) {
            emitter.emitEvent(events.closeall);
            if (ac_menu) ac_menu.$input.select();
        };
        var query_selected = function (menu) {
            if (!ac_menu || !ds_menu) return;
            if (menu === ag_menu) ds_menu.emitEvent(events.open).emitEvent(events.focus);
            else if (menu === ds_menu) $dom.load_btn.focus();
        };

        // Public
        constructor.prototype.destroy = function () {};
        constructor.prototype.initialized = function () { return initialized; };
        constructor.prototype.replay_query = function (url_config) {
            if (!url_config) return;

            if (JSON.stringify(url_config) === JSON.stringify(query)) return;

            var ag_val, ds_val;
            if ('aggregators' in url_config && $.isArray(url_config.aggregators) && url_config.aggregators.length) {
                if (!query || (JSON.stringify(url_config.aggregators) !== JSON.stringify(query.aggregators))) {
                    ag_val = {
                        aggregators: url_config.aggregators.map(function (entry) {
                            return {val:entry, required_field:false};
                        })
                    };
                    if (ag_menu) ag_menu.replay_query(ag_val); else ag_data = ag_val;
                }
            }

            if ('providers' in url_config && $.isArray(url_config.providers) && url_config.providers.length) {
                if (!query || (JSON.stringify(url_config.providers) !== JSON.stringify(query.providers))) {
                    ds_val = {
                        datasources: url_config.providers.map(function (entry) {
                            var obj = {};
                            if (entry.marketDataType) obj.marketDataType = entry.marketDataType;
                            if (entry.source) obj.source = entry.source;
                            else if (entry.snapshotId) obj.snapshotId = entry.snapshotId;
                            else if (entry.resolverKey) {
                                obj.resolverKey = entry.resolverKey;
                                if (entry.date) obj.date = entry.date;
                            }
                            return obj;
                        })
                    };
                    if (ds_menu) ds_menu.replay_query(ds_val); else ds_data = ds_val;
                }
            }

            if ('viewdefinition' in url_config && url_config.viewdefinition &&
                typeof url_config.viewdefinition === 'string') {
                if (!query || (url_config.viewdefinition !== query.viewdefinition)) {
                    if (ac_menu) ac_menu.$input.val(get_view_index(url_config.viewdefinition, 'id'));
                    else ac_data = url_config.viewdefinition;
                }
            }

            query = url_config;
        };
        constructor.prototype.reset_query = function () {
            if (query) query = null;
            [ag_menu, ds_menu].forEach(function (menu) { if (menu) menu.emitEvent(events.resetquery); });
            if (ac_menu) ac_menu.$input.val('search...');
        };
        return constructor;
    }
});
