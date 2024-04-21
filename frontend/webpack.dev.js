const { merge } = require('webpack-merge');
const common = require('./webpack.common.js');

module.exports = merge(common, {
    mode: 'development',
    devtool: 'inline-source-map',
    devServer: {
        historyApiFallback: true,
        client: {
            progress: true,
        },
        static: ['assets'],
        host: '0.0.0.0',
        port: 3070,
        proxy: {
            '/api': 'http://0.0.0.0:8110',
            '/api/game': {
                target: 'ws://0.0.0.0:8110',
                ws: true
            }
        }
    },
});