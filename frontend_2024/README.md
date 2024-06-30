# WebGL client
for Origin MMO game

# How it works

- [TypeScript](https://github.com/microsoft/TypeScript) as programming language
- [Vue3.x](https://github.com/vuejs/vue-next) as framework for UI part
- [pinia](https://github.com/vuejs/pinia) as store engine for vue3
- [PixiJS](https://github.com/pixijs/pixi.js) as WebglGL graphics render

# Getting started
First, install node_modules
```shell
npm install
```

Start local development version:
```shell
npm run dev
```
It would be started on localhost port 3070

on M1 Apple platform, if you are using yarn, you must run
```shell
npm rebuild node-sass
```
after each change package.json

### api requests

на каждом запросе смотрим есть ли у нас токен в сторе, если есть добавляем его в запрос

часть запросов может приходить с 400 (создание персонажа, не корректное имя) в этом случае не надо разлогинивать, такие ошибки игнорируем но надо их отобразить