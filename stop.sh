#!/bin/bash
set -x

docker compose --profile mongo --profile prod-eng-service down
