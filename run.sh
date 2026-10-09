#!/usr/bin/env bash
set -e
cd "$(dirname "$0")"
mkdir -p out
javac --add-modules jdk.httpserver -encoding UTF-8 -d out src/com/smartcanteen/Main.java
java --add-modules jdk.httpserver -cp out com.smartcanteen.Main
