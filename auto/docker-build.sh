#! /bin/bash
set -euo pipefail

sbt assembly

docker build -t mtchd/connect4 .