/*
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.form.TemporalMenu',
    dependencies: [],
    obj: function () {
        var module = this, Block = og.common.util.ui.Block;
        var TemporalMenu = function (config) {
            if (!config) {
                return og.dev.warn('og.analytics.TemporalMenu: Missing param [config] to constructor.');
            }
            if (!('form' in config) || !config.form) {
                return og.dev.warn('og.analytics.TemporalMenu: Missing param key [config.form] to constructor.');
            }
            // Private
            var block = this, menu, form = config.form, temporal = config.temporal || [];
            var menu_handler = function (event) {
                var $elem = $(event.srcElement || event.target), $custom;
                if ($elem.is('.custom') || $elem.is('.custom input') || $elem.is('.custom-date-time')) {
                    if ($elem.is('.custom-date-time')) {
                        $custom = $elem.siblings('.custom').find('input');
                        if (!($custom.attr('checked'))) {
                            $custom.attr('checked', 'checked');
                        }
                    }
                    return $elem.parents('fieldset').find('.custom-date-time').focus(0);
                }
                if ($elem.is('.latest') || $elem.is('.latest input')) {
                    $custom = $elem.parents('fieldset').find('.custom-date-time');
                    if ($custom.attr('value') !== '') {
                        return $custom.attr('value', '');
                    }
                }
            };
            form.Block.call(block, {
                module: 'og.analytics.form_temporal_tash',
                extras: temporal,
                processor: function (data) {
                    data.temporal = {
                        valuation: data['valuation-time'],
                        version: data['portfolio-version'],
                        correction: data['portfolio-correction']
                    };
                    delete data['portfolio-correction'];
                    delete data['portfolio-version'];
                    delete data['valuation-time'];
                }
            });
            form.on('form:load', function (event) {
                var menu = new og.common.util.ui.DropMenu({cntr: $('.og-temporal')});
                menu.$dom.menu = $('.og-menu', '.og-temporal').on('click', '.custom, .latest', menu_handler);
                menu.$dom.date_input = $('.custom-date-time', menu.$dom.menu).on('click', menu_handler)
                    .datetimepicker({
                        dateFormat: 'yy-mm-dd',
                        timeFormat: 'hh:mmZ',
                        separator: 'T',
                        onSelect: function () { menu.fire('dropmenu:open'); },
                        onClose: function () { menu.fire('dropmenu:open'); }
                    });
                og.common.events.on('temporal:dropmenu:open', function () {menu.fire('dropmenu:open', this); });
                og.common.events.on('temporal:dropmenu:close', function () {menu.fire('dropmenu:close', this); });
                og.common.events.on('temporal:dropmenu:focus', function () {menu.fire('dropmenu:focus', this); });
            });
        };
        TemporalMenu.prototype = new Block();
        return TemporalMenu;
    }
});