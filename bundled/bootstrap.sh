#!/usr/bin/env bash

#Enforce variables must be set
set -u

#Check for dependencies on downloader programs
which -s curl
CURL_INSTALLED=$?
which -s wget
WGET_INSTALLED=$?

#Enforce strict error checking for the rest of the script
set -e

URL='https://s3.amazonaws.com/MinecraftDownload/launcher/minecraft_server.jar?v=1311305402832'

cd bundled

if [ $WGET_INSTALLED -eq 0 ]; then
    wget "$URL" -O minecraft_server.jar
elif [ $CURL_INSTALLED -eq 0 ]; then
    curl -o minecraft_server.jar "$URL"
else
    echo "No downloader found. Please download the following file in your browser and move it to the 'clojurecraft/bundled' folder: $URL"
    exit 1
fi
