#!/bin/zsh

java -jar team3-heuristic.jar <<cat customParams simpleInput

(cat customParams simpleInput) | java -jar team3-heuristic.jar