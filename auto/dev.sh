#! /bin/bash
set -euo pipefail

source config/dev.env

docker run --rm -it -e TOKEN connect4
