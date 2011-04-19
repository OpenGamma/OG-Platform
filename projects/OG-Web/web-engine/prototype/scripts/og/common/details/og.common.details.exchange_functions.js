/**
 * @copyright 2009 - 2010 by OpenGamma Inc
 * @license See distribution for license
 */

$.register_module({
    name: 'og.common.details.exchange_functions',
    dependencies: [],
    obj: function () {
        var exchange_functions,
            render_info = function (selector, json) {
                var html = '', lbl = '', data = '', i;
                for (i in json.regionKey) lbl += '<div>' + i + '</div>';
                for (i in json.identifiers) lbl +=  '<div>' + i + '</div>';
                for (i in json.regionKey) data += '<div><strong class="OG-editable" og-edit="name">' +
                    json.regionKey[i] + '</strong></div>';
                for (i in json.identifiers) data += '<div><strong class="OG-editable" og-edit="name">' +
                    json.identifiers[i] + '</strong></div>';
                html += '<div class="og-lbl">' + lbl + '</div>';
                html += '<div>' + data + '</div>';
                $(selector).html(html);
            };
        return exchange_functions = {render_info: render_info};

    }
});