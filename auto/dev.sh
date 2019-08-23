#! /bin/bash
set -euo pipefail

. $(dirname $0)/../config/dev.env

docker run --rm -it -e TOKEN connect4
