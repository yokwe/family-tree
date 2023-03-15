#
# Makefile
#

all:
	echo "all"

build-yokwe:
	cd ../yokwe-root; mvn clean ant:ant install

build:
	mvn clean ant:ant install

run-main:
	ant run-main
