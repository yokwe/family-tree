#
# Makefile
#

all:
	echo "all"

build-yokwe:
	cd ../yokwe-root; mvn ant:ant install

build: build-yokwe
	mvn ant:ant install

claen:
	mvn clean

clear-log:
	echo -n >tmp/family-tree.log

run-main: clear-log
	ant run-main
