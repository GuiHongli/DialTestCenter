const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');

module.exports = {
  entry: './src/main.jsx',
  output: {
    path: path.resolve(__dirname, '../dialingtest-service/src/main/resources/static'),
    filename: 'bundle.js',
    publicPath: '/',
  },
  module: {
    rules: [
      {
        test: /\.(js|jsx)$/,
        use: {
          loader: 'babel-loader',
          options: {
            presets: ['@babel/preset-env', '@babel/preset-react']
          }
        },
        exclude: /node_modules/,
      },
      {
        test: /\.css$/i,
        use: ['style-loader', 'css-loader'],
      },
    ],
  },
  resolve: {
    extensions: ['.jsx', '.js'],
  },
  plugins: [
    new HtmlWebpackPlugin({
      template: './index.html',
      filename: 'index.html',
      inject: true,
    }),
  ],
  devServer: {
    contentBase: path.join(__dirname, 'public'),
    port: 4396,
    hot: true,
    historyApiFallback: true,
    overlay: {
      errors: true,
      warnings: false,
    },
    proxy: {
      '/dialingtest/api': {
        target: 'https://localhost:8087',
        changeOrigin: true,
        secure: false,
      },
    },
  },
};