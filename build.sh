#!/bin/bash
git clone https://github.com/JetBrains/kotlin.git ./testData/kt-compiler-project
git clone https://github.com/ItsLastDay/KotlinFuzzer.git ./testData/fuzzer-project
hg clone https://bitbucket.org/vorpal-research/kotoed ./testData/kotoed-project