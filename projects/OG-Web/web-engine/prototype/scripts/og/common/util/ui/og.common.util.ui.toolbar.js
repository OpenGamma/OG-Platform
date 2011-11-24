/**
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.common.util.ui.toolbar',
    dependencies: [],
    obj: function () {
        return function (obj) {
            var $new_obj = [], html, disabled_cl = 'OG-disabled',
                toolbar_tmpl = '\
                    <div class="OG-icon og-icon-tools-${id} og-js-${id} ${enabled}"><span>${name}</span></div>',
                divider = '<div class="og-divider"></div>',
                buttons = [
                    {name: 'delete', 'level': 'danger'},
                    {name: 'new'}
                ];
            if (!obj) throw new Error('obj is a required input for toolbar');
            if (!obj.location) throw new Error('You need to supply a selector/location for a toolbar to be placed');
            obj.buttons.forEach(function (button) {
                if (og.app.READ_ONLY) button.enabled = disabled_cl;
                if (button.enabled === disabled_cl) button.level = 'off';
            });
            // must convert rendered template into a string
            html = $('<p/>').append($.tmpl(toolbar_tmpl + divider, $.extend(true, buttons, obj.buttons))).html();
            $(obj.location).html(html); // Add the buttons to the page
            if (og.app.READ_ONLY) return; // if READ_ONLY, do not add handlers
            // Implement handlers
            $.each(($.extend(true, $new_obj, {'buttons': buttons}, obj)).buttons, function (i, val) {
                $('.' + obj.location + ' .og-js-' + val.id).unbind('mousedown').bind('mousedown', val.handler);
            });
        };
    }
});