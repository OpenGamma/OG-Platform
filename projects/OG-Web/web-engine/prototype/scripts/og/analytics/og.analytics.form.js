/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.form',
    dependencies: [
        'og.common.util.ui.AutoCombo',
        'og.analytics.AggregatorsMenu',
        'og.analytics.DatasourcesMenu',
        'og.views.common.layout'
    ],
    obj: function () {
        var initialized = false,
            query = null,
            FormInst = null,
            Form = function (selector) {
                var emitter = new EventEmitter(), api = {}, ag_dropmenu = og.analytics.AggregatorsMenu,
                    ds_dropmenu = og.analytics.DatasourcesMenu, Status, FormCombo, 
                    ag_menu = null, ds_menu = null, ac_menu = null,
                    events = {
                        focus: 'dropmenu:focus',
                        focused:'dropmenu:focused',
                        open: 'dropmenu:open',
                        opened: 'dropmenu:opened',
                        close: 'dropmenu:close',
                        closed: 'dropmenu:closed',
                        closeall: 'dropmenu:closeall',
                        queryselected: 'dropmenu:queryselected',
                        querycancelled: 'dropmenu:querycancelled'
                    };
                Status = function (selector) {
                    var status = this, interval, init = false;
                    $(selector + ' button').on('click', function () {
                        if (!status.status || status.status === 'paused') return status.play();
                        if (status.status === 'playing') return status.pause();
                    });
                    status.pause = function () {
                        $(selector + ' em').html('paused').removeClass('live').addClass('paused');
                        $(selector + ' button').removeClass('og-icon-play').addClass('og-icon-pause');
                        status.message('');
                        clearInterval(interval);
                        status.status = 'paused';
                    };
                    status.play = function () {
                        if (!init) init = !!$(selector + ' button').removeClass('og-disabled');
                        $(selector + ' em').html('live').removeClass('paused').addClass('live');
                        $(selector + ' button').removeClass('og-icon-pause').addClass('og-icon-play');
                        status.message('starting...');
                        interval = setInterval(function () {
                            status.message('updated ' + (Math.random() + 1).toFixed(2) + ' seconds ago');
                        }, 1000);
                        status.status = 'playing';
                    };
                    status.message = function (message) {$(selector + ' .og-message').html(message);};
                    status.status = null;
                    return status;
                };
                FormCombo = function (config) {
                    var FormCombo = this, vd_s = '.og-view', fcntrls_s = 'input, select, button', 
                        ac_s = 'input autocompletechange autocompleteselect', 
                        $form = $(selector).html(config.template), $ag = $('.og-aggregation', $form),
                        $ds = $('.og-datasources', $form), $ag_fcntrls, $ds_fcntrls, $load_btn = $('.og-load', $form),
                        status;
                        keydown_handler = function (event) {
                            if (event.keyCode !== 9) return;
                            var $elem = $(this), shift_key = event.shiftKey,
                                active_pos = function (elms, pos) {
                                    return $elem.is(elms[pos]());
                                };
                            if (!shift_key && ac_menu.state === 'focused')
                                ag_menu.emitEvent(events.open);
                            if (!shift_key && active_pos($ag_fcntrls,'last'))
                                ds_menu.emitEvent(events.open);
                            if (!shift_key && active_pos($ds_fcntrls, 'last'))
                                ds_menu.emitEvent(events.close);
                            if (shift_key && $elem.is($load_btn)) 
                                ds_menu.emitEvent(events.open);
                            if (shift_key && active_pos($ds_fcntrls, 'first')) 
                                ag_menu.emitEvent(events.open);
                            if (shift_key && active_pos($ag_fcntrls, 'first'))
                                ag_menu.emitEvent(events.close);
                        },
                        close_dropmenu = function (menu) {
                            if (menu === ds_menu) ag_menu.emitEvent(events.close);
                            else ds_menu.emitEvent(events.close);
                        },
                        auto_combo_handler = function (even, ui) {
                            if ((ui && ui.item && ui.item.value || $(this).val()) !== '') {
                                $load_btn.removeClass('og-disabled').on('click', function () {status.play();});
                            } else $load_btn.addClass('og-disabled').off('click');
                        },
                        query_selected = function (menu) {
                            if (menu === ag_menu) ds_menu.emitEvent(events.open).emitEvent(events.focus);
                            else if (menu === ds_menu) $load_btn.focus();
                        },
                        query_cancelled = function (menu) {
                            emitter.emitEvent(events.closeall);
                            ac_menu.$input.select();
                        },
                        load_query = function () {
                            if (!~ac_menu.$input.val().indexOf('Db')) return;
                            og.analytics.url.main(query = {
                                viewdefinition: ac_menu.$input.val(),
                                providers: ds_menu.get_query(),
                                aggregators: ag_menu.get_query()
                            });
                        };
                    config.search.data.sort((function(i){ // sort by name
                        return function (a, b) {return (a[i] === b[i] ? 0 : (a[i] < b[i] ? -1 : 1));};
                    })('name'));
                    $form.on('keydown', fcntrls_s, keydown_handler);
                    ac_menu = new og.common.util.ui.AutoCombo(selector+' '+vd_s,'search...', config.search.data);
                    ac_menu.$input.on(ac_s, auto_combo_handler).select();
                    ag_menu = new ag_dropmenu({
                        $cntr: $ag, tmpl: config.aggregation_markup, data: config.aggregators.data
                    });
                    ds_menu = new ds_dropmenu({
                        $cntr: $ds, tmpl: config.datasources_markup, data: config.datasource.data
                    });
                    [ag_menu, ds_menu].forEach(function (menu) { 
                        menu.addListener(events.opened, close_dropmenu)
                            .addListener(events.queryselected, query_selected)
                            .addListener(events.querycancelled, query_cancelled);
                    });
                    $ag_fcntrls = $ag.find(fcntrls_s), $ds_fcntrls = $ds.find(fcntrls_s);
                    $load_btn.on('click', load_query);
                    emitter.addListener(events.closeall, function () {
                        close_dropmenu(ag_menu);
                        close_dropmenu(ds_menu);
                    });
                    og.views.common.layout.main.allowOverflow('north');
                    status = new Status(selector + ' .og-status');
                };
                $.when(
                    og.api.text({module: 'og.analytics.form_tash'}),
                    og.api.rest.viewdefinitions.get(),
                    og.api.rest.aggregators.get(),
                    {data: ['Live', 'Snapshot', 'Historical']},
                    og.api.text({module: 'og.analytics.form_aggregation_tash'}),
                    og.api.text({module: 'og.analytics.form_datasources_tash'})
                ).pipe(function (template, search, aggregators, datasource, aggregation_markup, datasources_markup) {
                    FormCombo({
                        template: template, 
                        search: search,
                        aggregators: aggregators, 
                        datasource: datasource, 
                        aggregation_markup: aggregation_markup, 
                        datasources_markup: datasources_markup
                    });
                    initialized = true;
                });
                this.set_query = function (q) {
                    query = q;
                    return this;
                };
                this.replay_query = function (url_config) {
                    if (JSON.stringify(url_config) === JSON.stringify(query)) return false;
                    if (!query || (JSON.stringify(url_config.aggregators) !== JSON.stringify(query.aggregators))) {
                        ag_menu.replay_query({
                            aggregators: url_config.aggregators.map(function (entry) {
                                return {val:entry, required_field:false};
                            })
                        });
                    }
                    if (!query || (JSON.stringify(url_config.providers) !== JSON.stringify(query.providers))) {
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
                    if (!query || (url_config.viewdefinition !== query.viewdefinition)) 
                        ac_menu.$input.val(url_config.viewdefinition);
                    return query = url_config;
                };
                this.destroy = function () {

                };
            };
        return function (selector, url_config) {
            if (!initialized && url_config) { 
                return (FormInst = new Form(selector)).set_query(url_config).replay_query(url_config);
            } else if (initialized && url_config) {
                return FormInst.replay_query(url_config);
            } else if (!initialized || (initialized && !url_config)) {
                if (query) query = null;
                return FormInst = new Form(selector);
            }
        }
    }
});