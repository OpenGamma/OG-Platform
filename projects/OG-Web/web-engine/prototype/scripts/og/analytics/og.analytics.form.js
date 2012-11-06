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
        var query = null, template = null, emitter = new EventEmitter(), api = {}, initialized = false,
            ag_menu = null, ds_menu = null, ac_menu = null, status = null, selector, $dom = {}, vd_s = '.og-view',
            fcntrls_s = 'input, select, button', ac_s = 'input autocompletechange autocompleteselect',
            ds_template = null, ag_template = null, viewdefs = null, aggregators = null, ac_data = null, ag_data = null,
            ds_data = null, events = {
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
        var auto_combo_handler = function (even, ui) {
            if ((ui && ui.item && ui.item.value || $(this).val()) !== '') {
                $dom.load_btn.removeClass('og-disabled').on('click', function () {status.play();});
            } else $dom.load_btn.addClass('og-disabled').off('click');
        };
        var close_dropmenu = function (menu) {
            if (menu === ds_menu) ag_menu.emitEvent(events.close);
            else ds_menu.emitEvent(events.close);
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
                og.api.rest.viewdefinitions.get(),
                og.api.rest.aggregators.get()
            ).pipe(function (tmpl, ag_tmpl, ds_tmpl, vds, ag_data) {
                if (!tmpl.error) template = tmpl;
                if (!ag_tmpl.error) ag_template = ag_tmpl;
                if (!ds_tmpl.error) ds_template = ds_tmpl;
                if (!vds.error && 'data' in vds) viewdefs = vds.data;
                if (!ag_data.error && 'data' in ag_data) aggregators = ag_data.data;
                if (callback) callback();
            });
        };
        var init = function () {
            if (!selector || !template) return;
            $dom.form = $(selector).html(template);
            if ($dom.form) {
                $dom.ag = $('.og-aggregation', $dom.form);
                $dom.ds = $('.og-datasources', $dom.form);
                $dom.load_btn = $('.og-load', $dom.form);
                $dom.form.on('keydown', fcntrls_s, keydown_handler);
            }
            if (viewdefs) {
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
            if ($dom.ag) $dom.ag_fcntrls = $dom.ag.find(fcntrls_s);
            if ($dom.ds) $dom.ds_fcntrls = $dom.ds.find(fcntrls_s);
            if ($dom.load_btn) $dom.load_btn.on('click', load_query);
            status = new og.analytics.Status(selector + ' .og-status');
        };
        var load_query = function () {
            if ((!ac_menu && !ds_menu) || !~ac_menu.$input.val().indexOf('Db')) return;
            og.analytics.url.main(query = {
                aggregators: ag_menu ? ag_menu.get_query() : [],
                providers: ds_menu.get_query(),
                viewdefinition: ac_menu.$input.val()
            });
        };
        var keydown_handler = function (event) {
            if (event.keyCode !== 9) return;
            var $elem = $(this), shift_key = event.shiftKey,
                active_pos = function (elms, pos) {
                    return $elem.is(elms[pos]());
                };
            if (!shift_key && ac_menu.state === 'focused') ag_menu.emitEvent(events.open);
            if (!shift_key && active_pos($dom.ag_fcntrls,'last')) ds_menu.emitEvent(events.open);
            if (!shift_key && active_pos($dom.ds_fcntrls, 'last')) ds_menu.emitEvent(events.close);
            if (shift_key && $elem.is($dom.load_btn)) ds_menu.emitEvent(events.open);
            if (shift_key && active_pos($dom.ds_fcntrls, 'first')) ag_menu.emitEvent(events.open);
            if (shift_key && active_pos($dom.ag_fcntrls, 'first')) ag_menu.emitEvent(events.close);
        };
        var query_cancelled = function (menu) {
            emitter.emitEvent(events.closeall);
            ac_menu.$input.select();
        };
        var query_selected = function (menu) {
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
                    if (ac_menu) ac_menu.$input.val(url_config.viewdefinition);
                    else ac_data = url_config.viewdefinition;
                }
            }

            query = url_config;
        };
        constructor.prototype.reset_query = function () {
            if (query) query = null;
            [ag_menu, ds_menu].forEach(function (menu) { if (menu) menu.emitEvent(events.resetquery); });
            ac_menu.$input.val('search...');
        };
        return constructor;
    }
});
