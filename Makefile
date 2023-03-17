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

run-main:
	ant run-main
	
run-main2:
	ant run-main2
	