#!/usr/bin/env bash
cd /home/ubuntu/test/d3rvihack/kotlin-source/build/nodes/NetworkMapAndNotary
screen -d -m java -Xmx2G -jar corda.jar

cd /home/ubuntu/test/d3rvihack/kotlin-source/build/nodes/DEALER-D01
screen -d -m java -Xmx2G -jar corda.jar && screen -d -m java -Xmx2G -jar corda-webserver.jar

cd /home/ubuntu/test/d3rvihack/kotlin-source/build/nodes/DEALER-D02
screen -d -m java -Xmx2G -jar corda.jar && screen -d -m java -Xmx2G -jar corda-webserver.jar

cd /home/ubuntu/test/d3rvihack/kotlin-source/build/nodes/DEALER-D03
screen -d -m java -Xmx2G -jar corda.jar && screen -d -m java -Xmx2G -jar corda-webserver.jar

cd /home/ubuntu/test/d3rvihack/kotlin-source/build/nodes/CLIENT-C01
screen -d -m java -Xmx2G -jar corda.jar && screen -d -m java -Xmx2G -jar corda-webserver.jar

cd /home/ubuntu/test/d3rvihack/kotlin-source/build/nodes/CLIENT-C02
screen -d -m java -Xmx2G -jar corda.jar && screen -d -m java -Xmx2G -jar corda-webserver.jar

cd /home/ubuntu/test/d3rvihack/kotlin-source/build/nodes/CLIENT-C03
screen -d -m java -Xmx2G -jar corda.jar && screen -d -m java -Xmx2G -jar corda-webserver.jar

cd /home/ubuntu/test/d3rvihack/kotlin-source/build/nodes/CLIENT-C04
screen -d -m java -Xmx2G -jar corda.jar && screen -d -m java -Xmx2G -jar corda-webserver.jar

cd /home/ubuntu/test/d3rvihack/kotlin-source/build/nodes/CLIENT-C05
screen -d -m java -Xmx2G -jar corda.jar && screen -d -m java -Xmx2G -jar corda-webserver.jar

cd /home/ubuntu/test/d3rvihack/kotlin-source/build/nodes/CCP-P01
screen -d -m java -Xmx2G -jar corda.jar && screen -d -m java -Xmx2G -jar corda-webserver.jar