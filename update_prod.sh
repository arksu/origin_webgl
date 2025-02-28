#!/bin/sh


# Check if at least one parameter is provided
if [ $# -eq 0 ]; then
  echo "Usage: $0 [back, front]"
  exit 1
fi

param="$1"

FRONT="frontend"
BACK="backend"

APP_HOME="`pwd -P`"

case "$param" in
  back)
	echo "build backend..."
	cd $APP_HOME/$BACK
	./gradlew clean build -x test

	echo "upload backend..."
	rsync -avz --progress -e "ssh" $APP_HOME/$BACK/build/libs/backend.jar ssh.ark.su:/usr/local/origin/backend/
  	;;
  front)
	echo "build frontend..."

	cd $APP_HOME/$FRONT
	npm run build
	rsync -avz --progress -e "ssh" $APP_HOME/$FRONT/dist/ ssh.ark.su:/usr/local/origin/www/
	rsync -avz --progress -e "ssh" $APP_HOME/$FRONT/assets/ ssh.ark.su:/usr/local/origin/www/assets/
	;;
  *)
	echo "Unknown parameter: $param"
	echo "Valid options are: back, front"
	;;
esac
