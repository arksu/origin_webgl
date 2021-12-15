const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');

module.exports = {
    mode: 'development',
    entry: './src/main.js',
    plugins: [
        new HtmlWebpackPlugin({
            title: 'Origin',
        }),
    ],
    output: {
        filename: '[name].bundle.js',
        path: path.resolve(__dirname, "dist"),
        clean: true,
    },
    devServer: {
        historyApiFallback: true,
        client: {
            progress: true,
        },
        static : ['assets'],
        host: '0.0.0.0',
        port: 3080,
        proxy: {
            '/api': 'http://0.0.0.0:8010',
            '/api/game': {
                target: 'ws://0.0.0.0:8010',
                ws: true
            }
        }
    },
};
