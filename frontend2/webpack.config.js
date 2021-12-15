const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const {VueLoaderPlugin} = require('vue-loader')

module.exports = {
    mode: 'development',
    entry: './src/main.js',
    plugins: [
        new VueLoaderPlugin(),
        new HtmlWebpackPlugin({
            title: 'Origin',
            template: "./src/index.html",
            filename: 'index.html'
        }),
    ],
    output: {
        filename: '[name].bundle.js',
        path: path.resolve(__dirname, "dist"),
        clean: true,
    },
    module: {
        rules: [
            {
                test: /\.vue$/,
                use: 'vue-loader'
            },
            {
                test: /\.s[ac]ss$/i,
                use: [
                    // Creates `style` nodes from JS strings
                    "style-loader",
                    // Translates CSS into CommonJS
                    "css-loader",
                    // Compiles Sass to CSS
                    "sass-loader"
                ]
            }
        ]
    },
    devServer: {
        historyApiFallback: true,
        client: {
            progress: true,
        },
        static: ['assets'],
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
