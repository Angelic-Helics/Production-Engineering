#!/usr/bin/env bash
set -euo pipefail
set -x

JENKINS_CONFIG_ROOT="${JENKINS_CONFIG_ROOT:-$PWD/.jenkins_config}"
mkdir -p "$JENKINS_CONFIG_ROOT"

docker compose --profile mongo --profile prod-eng-service up -d
