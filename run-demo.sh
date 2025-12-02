#!/usr/bin/env bash
set -euo pipefail

# Convenience script to run demo: starts docker and launches the java app
echo "Starting Docker MySQL..."
docker-compose up -d --build

echo "Waiting for the MySQL container to be ready..."
sleep 4

echo "Launching the Java Swing demo..."
mvn -Dexec.mainClass=Main org.codehaus.mojo:exec-maven-plugin:3.1.0:java
