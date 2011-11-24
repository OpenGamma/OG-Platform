/**
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.common.util.ui.toolbar',
    dependencies: [],
    obj: function () {
        return function (obj) {
            var $new_obj = [], html,
                toolbar_tmpl = '\
                    <div class="OG-icon og-icon-tools-${id} og-js-${id} ${enabled}" data-tooltip="${tooltip}">\
                      {{if label}} <span>${label}</span> {{/if}}\
                    </div>\
                    {{if divider}} <div class="og-divider"></div> {{/if}}\
                ',
                divider = '';
            if (!obj) throw new Error('obj is a required input for toolbar');
            if (!obj.location) throw new Error('You need to supply a selector/location for a toolbar to be placed');
            $.each(obj.buttons, function (i) {
                if (obj.buttons[i]['enabled'] === 'OG-disabled') obj.buttons[i]['level'] = 'off';
            });
            // must convert rendered template into a string
            html = $('<p/>').append($.tmpl(toolbar_tmpl + divider, obj.buttons)).html();
            $(obj.location).html(html); // Add the buttons to the page
            // Implement handlers
            $.each(($.extend(true, $new_obj, {'buttons': obj.buttons}, obj)).buttons, function (i, val) {
                $('.' + obj.location + ' .og-js-' + val.id).unbind('mousedown').bind('mousedown', val.handler);
            });
            og.common.util.ui.tooltip(obj.location);
        };
    }
});