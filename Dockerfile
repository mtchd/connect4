FROM java:8
WORKDIR /
ADD /target/scala-2.12/Connect4-assembly-0.3.1.jar Connect4-assembly-0.3.1.jar
ADD /default-emojis.json default-emojis.json
CMD java -jar Connect4-assembly-0.3.1.jar
