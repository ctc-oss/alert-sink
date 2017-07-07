#!/usr/bin/env bash

sbt docker:publishLocal \
 && docker-compose rm -f \
 && docker-compose up
