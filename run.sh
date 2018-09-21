#!/usr/bin/env bash
cd /home/ubuntu/test/d3rvihack/kotlin-source/build/nodes/NetworkMapAndNotary
screen -d -m java -Xmx2G -jar corda.jar

cd /home/ubuntu/test/d3rvihack/kotlin-source/build/nodes/709100D01PQ7HQVZDA69
screen -d -m java -Xmx2G -jar corda.jar && screen -d -m java -Xmx2G -jar corda-webserver.jar

cd /home/ubuntu/test/d3rvihack/kotlin-source/build/nodes/444100D02FKCOHSINA94
screen -d -m java -Xmx2G -jar corda.jar && screen -d -m java -Xmx2G -jar corda-webserver.jar

cd /home/ubuntu/test/d3rvihack/kotlin-source/build/nodes/272200D03NJCEDFEOB38
screen -d -m java -Xmx2G -jar corda.jar && screen -d -m java -Xmx2G -jar corda-webserver.jar

cd /home/ubuntu/test/d3rvihack/kotlin-source/build/nodes/521700C015DIKTCI1V98
screen -d -m java -Xmx2G -jar corda.jar && screen -d -m java -Xmx2G -jar corda-webserver.jar

cd /home/ubuntu/test/d3rvihack/kotlin-source/build/nodes/845900C02P22NDH4QD17
screen -d -m java -Xmx2G -jar corda.jar && screen -d -m java -Xmx2G -jar corda-webserver.jar

cd /home/ubuntu/test/d3rvihack/kotlin-source/build/nodes/501300C03QNF4SQKYO15
screen -d -m java -Xmx2G -jar corda.jar && screen -d -m java -Xmx2G -jar corda-webserver.jar

cd /home/ubuntu/test/d3rvihack/kotlin-source/build/nodes/299500C04WHL0PLF0F52
screen -d -m java -Xmx2G -jar corda.jar && screen -d -m java -Xmx2G -jar corda-webserver.jar

cd /home/ubuntu/test/d3rvihack/kotlin-source/build/nodes/502900C05HITOOMU1P35
screen -d -m java -Xmx2G -jar corda.jar && screen -d -m java -Xmx2G -jar corda-webserver.jar

cd /home/ubuntu/test/d3rvihack/kotlin-source/build/nodes/027300P01DF7BB67DP86
screen -d -m java -Xmx2G -jar corda.jar && screen -d -m java -Xmx2G -jar corda-webserver.jar