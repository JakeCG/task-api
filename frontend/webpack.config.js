const path = require('path');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const CopyWebpackPlugin = require('copy-webpack-plugin');

const isDevelopment = process.env.NODE_ENV !== 'production';

module.exports = {
  mode: isDevelopment ? 'development' : 'production',
  entry: {
    main: ['./src/main/assets/js/main.ts', './src/main/assets/scss/main.scss'],
  },
  output: {
    path: path.resolve(__dirname, 'dist/assets'),
    filename: 'js/[name].js',
  },
  module: {
    rules: [
      {
        test: /\.ts$/,
        use: 'ts-loader',
        exclude: /node_modules/,
      },
      {
        test: /\.scss$/,
        use: [MiniCssExtractPlugin.loader, 'css-loader', 'sass-loader'],
      },
    ],
  },
  resolve: {
    extensions: ['.ts', '.js'],
  },
  plugins: [
    new MiniCssExtractPlugin({
      filename: 'css/[name].css',
    }),
    new CopyWebpackPlugin({
      patterns: [
        {
          from: 'node_modules/govuk-frontend/dist/govuk/assets',
          to: 'images',
        },
        {
          from: 'node_modules/govuk-frontend/dist/govuk/all.js',
          to: 'js/govuk.js',
        },
      ],
    }),
  ],
  devtool: isDevelopment ? 'source-map' : false,
};
