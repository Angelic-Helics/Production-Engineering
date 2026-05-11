#!/bin/bash
set -x

docker compose --profile monitoring --profile mongo --profile prod-eng-service down
