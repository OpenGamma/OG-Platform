/*
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.views.common.layout',
    dependencies: [],
    obj: function () {
        return {
            admin: function () {return {
                main: $('.OG-layout-admin-container').layout({
                    defaults: {enableCursorHotkey: false},
                    north:    {spacing_open: 0, size: 43, paneClass: 'OG-layout-admin-masthead'},
                    south:    {spacing_open: 0, size: 36, paneClass: 'OG-layout-admin-footer'},
                    east:     {spacing_closed: 0, initClosed: true}, // Not used
                    west:     {size: '33%', paneClass: 'OG-layout-admin-search'},
                    center:   {paneClass: 'ui-layout-details'}
                }),
                inner: $('.ui-layout-details').layout({
                    defaults: {enableCursorHotkey: false},
                    north: { // used for deleted view message
                        paneSelector: '.OG-layout-admin-details-north', paneClass: 'OG-layout-admin-details-north',
                        size: 50, initClosed: true, spacing_closed: 0, spacing_open: 0
                    },
                    south: { // versions / sync etc
                        paneSelector: '.OG-layout-admin-details-south', paneClass: 'OG-layout-admin-details-south',
                        size: '50%', initClosed: true, spacing_closed: 0,
                        onopen_start: function () {$('.OG-layout-admin-details-south').empty();}
                    },
                    center: {
                        paneSelector: '.OG-layout-admin-details-center', paneClass: 'OG-layout-admin-details-center',
                        contentSelector: '.ui-layout-content'
                    }
                })
            };},
            analytics: function () {return {
                main: $('.OG-layout-admin-container').layout({
                    defaults: {enableCursorHotkey: false},
                    north: {spacing_open: 0, paneClass: 'OG-layout-admin-masthead', size: 43},
                    south: {spacing_open: 0, paneClass: 'OG-layout-admin-footer', size: 36},
                    center: {paneClass: 'OG-layout-analytics', contentSelector: '.ui-layout-content'}
                })
            };},
            analytics2: function () {return {
                main: $('.OG-layout-analytics-container').layout({
                    defaults: {enableCursorHotkey: false},
                    north: {spacing_open: 0, paneClass: 'OG-layout-analytics-masthead', size: 43},
                    south: {spacing_open: 0, paneClass: 'OG-layout-analytics-footer', size: 20},
                    east: {spacing_closed: 0, initClosed: false, paneClass: 'OG-layout-analytics-dock', size: 350},
                    center: {paneClass: 'OG-layout-analytics2'}
                }),
                inner: $('.OG-layout-analytics2').layout({
                    defaults: {enableCursorHotkey: false},
                    south: { // dep graph
                        paneSelector: '.OG-layout-analytics-south', paneClass: 'OG-layout-analytics-south',
                        size: '25%', initClosed: false, spacing_closed: 0
                    },
                    center: { // main grid
                        paneSelector: '.OG-layout-analytics-center', paneClass: 'OG-layout-analytics-center',
                        contentSelector: '.ui-layout-content'
                    }
                })
            };}
        };
    }
});