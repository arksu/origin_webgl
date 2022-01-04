#!/bin/sh

FRONT="frontend"
BACK="backend"

APP_HOME="`pwd -P`"

echo "build backend..."
cd $APP_HOME/$BACK
gradle clean build

echo "upload backend..."
rsync -avz --progress -e "ssh" $APP_HOME/$BACK/build/distributions/backend.zip ssh.ark.su:/usr/local/origin/

echo "build frontend..."

cd $APP_HOME/$FRONT
npm run build
rsync -avz --progress -e "ssh" $APP_HOME/$FRONT/dist/ ssh.ark.su:/usr/local/www/origin.ark.su/
