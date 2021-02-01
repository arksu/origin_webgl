const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const { CleanWebpackPlugin } = require('clean-webpack-plugin');
const CopyPlugin = require("copy-webpack-plugin");
const TSConfigPathsPlugin = require('tsconfig-paths-webpack-plugin');
const {ProvidePlugin, HotModuleReplacementPlugin} = require("webpack");
const {VueLoaderPlugin} = require('vue-loader')

module.exports = {
    mode: 'development',
    entry: './src/main.ts',
    module: {
        rules: [
            {
                test: /\.tsx?$/,
                loader: 'ts-loader',
                options: {
                    appendTsSuffixTo: [/\.vue$/],
                },
                exclude: /node_modules/,
            },
            {
                test: /\.vue$/,
                loader: 'vue-loader'
            },
            {
                test: /\.s[ac]ss$/,
                // test: /\.scss$/,
                use: [
                    {
                        loader: "vue-style-loader"
                    },
                    {
                        loader: "style-loader" // creates style nodes from JS strings
                    },
                    {
                        loader: "css-loader" // translates CSS into CommonJS
                    },
                    {
                        loader: "sass-loader" // compiles Sass to CSS
                    }
                ]
            },
        ],
    },
    plugins: [
        new CleanWebpackPlugin(),
        new CopyPlugin({
            patterns: [
                {
                    from: "assets/**/*"
                }
            ]
        }),
        new HotModuleReplacementPlugin(),
        new VueLoaderPlugin(),
        new HtmlWebpackPlugin({
            hash: true,
            title: 'Origin',
            template: "./src/index.html",
            filename: 'index.html'
        }),
        new ProvidePlugin({
            process: 'process/browser',
        }),
    ],
    resolve: {
        plugins: [new TSConfigPathsPlugin({})],
        modules: ["node_modules"],
        extensions: [".js", ".ts", ".tsx", ".vue"],
        alias: {
            'vue': '@vue/runtime-dom',
        }
    },
    output: {
        filename: '[name].bundle.js',
        path: path.resolve(path.join(__dirname, ".", "dist")),
    },
    devServer: {
        historyApiFallback: true,
        inline: true,
        hot: true,
        stats: 'minimal',
        compress: true,
        overlay: true,
        progress: true,
        host: '0.0.0.0',
        port: 3070,
        proxy : {
            '/api' : 'http://0.0.0.0:8020',
            '/api/game' : {
                target: 'ws://0.0.0.0:8020',
                ws: true
            }
        }
    },
    target: "web"
};