#!/usr/bin/env bash
set -euo pipefail

# Run integration test using maven
mvn -Dtest=integration.IntegrationIT test

# Run all tests
mvn test
