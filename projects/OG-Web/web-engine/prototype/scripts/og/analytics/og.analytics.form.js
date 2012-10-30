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
        var query = null, template = null, emitter = new EventEmitter(), api = {}, initialized = false, 
            ag_menu = null, ds_menu = null, ac_menu = null, status = null, selector, $dom = {}, vd_s = '.og-view',
            fcntrls_s = 'input, select, button', ac_s = 'input autocompletechange autocompleteselect', 
            ds_template = null, ag_template = null, viewdefs = null, aggregators = null;
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
                resetdsquery:'dropmenu:resetquery'
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
        var close_dropmenu = function (menu) {
            if (menu === ds_menu) ag_menu.emitEvent(events.close);
            else ds_menu.emitEvent(events.close);
        };
        var auto_combo_handler = function (even, ui) {
            if ((ui && ui.item && ui.item.value || $(this).val()) !== '') {
                $dom.load_btn.removeClass('og-disabled').on('click', function () {status.play();});
            } else $dom.load_btn.addClass('og-disabled').off('click');
        };
        var query_selected = function (menu) {
            if (menu === ag_menu) ds_menu.emitEvent(events.open).emitEvent(events.focus);
            else if (menu === ds_menu) $dom.load_btn.focus();
        };
        var query_cancelled = function (menu) {
            emitter.emitEvent(events.closeall);
            ac_menu.$input.select();
        };
        var load_query = function () {
            if (!~ac_menu.$input.val().indexOf('Db')) return;
            og.analytics.url.main(query = {
                viewdefinition: ac_menu.$input.val(),
                providers: ds_menu.get_query(),
                aggregators: ag_menu.get_query()
            });
        };
        var fetch_template = function (callback) {
            $.when(
                og.api.text({module: 'og.analytics.form_tash'}),
                og.api.text({module: 'og.analytics.form_aggregation_tash'}),
                og.api.text({module: 'og.analytics.form_datasources_tash'}),
                og.api.rest.viewdefinitions.get(),
                og.api.rest.aggregators.get()
            ).pipe(function (tmpl, ag_tmpl, ds_tmpl, vds, ag_data) {
                template = tmpl; ag_template = ag_tmpl; ds_template = ds_tmpl;
                viewdefs = vds.data; aggregators = ag_data.data;
                callback();
            });
        };
        var init = function () {
            $dom.form = $(selector).html(template); 
            $dom.ag = $('.og-aggregation', $dom.form);
            $dom.ds = $('.og-datasources', $dom.form);
            $dom.load_btn = $('.og-load', $dom.form);
            $dom.form.on('keydown', fcntrls_s, keydown_handler);
            ac_menu = new og.common.util.ui.AutoCombo(selector+' '+vd_s,'search...', viewdefs);
            ac_menu.$input.on(ac_s, auto_combo_handler).select();
            ag_menu = new og.analytics.AggregatorsMenu({cntr:$dom.ag, tmpl:ag_template, data: aggregators});
            ds_menu = new og.analytics.DatasourcesMenu({cntr:$dom.ds, tmpl:ds_template});
            [ag_menu, ds_menu].forEach(function (menu) { 
                menu.addListener(events.opened, close_dropmenu)
                    .addListener(events.queryselected, query_selected)
                    .addListener(events.querycancelled, query_cancelled)
                    .addListener(events.resetquery, menu.reset_query);
            });
            $dom.ag_fcntrls = $dom.ag.find(fcntrls_s), $dom.ds_fcntrls = $dom.ds.find(fcntrls_s);
            $dom.load_btn.on('click', load_query);
            emitter.addListener(events.closeall, function () {
                close_dropmenu(ag_menu);
                close_dropmenu(ds_menu);
            });
            status = new og.analytics.Status(selector + ' .og-status');
        };
        var constructor = function (conf) {
            var form = this, config = conf || {};
            selector = config.selector || '.OG-layout-analytics-masthead';
            og.views.common.layout.main.allowOverflow('north');
            if (template) init(); else fetch_template(init);
            return form;
        };
        constructor.prototype.reset_query = function () {
            if (query) query = null;
            [ag_menu, ds_menu].forEach(function (menu) { menu.emitEvent(events.resetquery); });
            ac_menu.$input.val('search...');
        };
        constructor.prototype.replay_query = function (url_config) {
            if (!url_config) return;
            
            if (JSON.stringify(url_config) === JSON.stringify(query)) return;

            if (!query || (JSON.stringify(url_config.aggregators) !== JSON.stringify(query.aggregators)))
                var ag_intv = setInterval(function () {
                    if (ag_menu) {
                        clearInterval(ag_intv);
                        ag_menu.replay_query({
                            aggregators: url_config.aggregators.map(function (entry) {
                                return {val:entry, required_field:false};
                            })
                        });
                    }
                });
            
            if (!query || (JSON.stringify(url_config.providers) !== JSON.stringify(query.providers)))
                var ds_intv = setInterval(function (){
                    if (ds_menu) {
                        clearInterval(ds_intv);
                        ds_menu.replay_query({
                            datasources: url_config.providers.map(function (entry) {
                                var obj = {};
                                obj.marketDataType = entry.marketDataType;
                                if (entry.source) obj.source = entry.source;
                                else if (entry.snapshotId) obj.snapshotId = entry.snapshotId;
                                return obj;
                            })
                        });
                    }
                });
            
            if (!query || (url_config.viewdefinition !== query.viewdefinition))
                var ac_intv = setInterval(function (){
                    if (ac_menu) {
                        clearInterval(ac_intv);
                        ac_menu.$input.val(url_config.viewdefinition);
                    }
                });

            query = url_config;
        };
        constructor.prototype.destroy = function () {};
        constructor.prototype.initialized = function () { return initialized; };
        return constructor;
    }
});