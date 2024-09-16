#!/bin/sh

FRONT="frontend"
BACK="backend"

APP_HOME="`pwd -P`"

echo "build backend..."
cd $APP_HOME/$BACK
./gradlew clean build -x test

echo "upload backend..."
rsync -avz --progress -e "ssh" $APP_HOME/$BACK/build/libs/backend.jar ssh.ark.su:/usr/local/origin/backend/

echo "build frontend..."

cd $APP_HOME/$FRONT
npm run build
rsync -avz --progress -e "ssh" $APP_HOME/$FRONT/dist/ ssh.ark.su:/usr/local/origin/www/
rsync -avz --progress -e "ssh" $APP_HOME/$FRONT/assets/ ssh.ark.su:/usr/local/origin/www/assets/
