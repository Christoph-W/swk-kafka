See [Licenses](#licenses)

## About this demo project
This project was created for a Meetup of the [Softwerkskammer Leipzig](https://www.softwerkskammer.org/groups/sachsen)
on September 20, 2018 at [TomTom Telematics](https://telematics.tomtom.com/de_de/webfleet/company/) office.

To give an introduction to [Apache Kafka](https://kafka.apache.org) generated vehicle tracking
positions<sup>[*](#data-policy)</sup> are used.

To get an [overview](#show-the-map), the data is shown on a map by starting a web server.

The generated track entries are given in a file. It is used in the [first exercise](#exercise-producer)
to create a producer to populate a topic.

Given this topic the [next exercise](#exercise-consumer) will introduce a consumer.

The [continued exercise](#exercise-stream-processing) will be on the subject stream processing.

## Get in touch with Apache Kafka first time
- download kafka from\
  [apache.org](https://www.apache.org/dyn/closer.cgi?path=/kafka/2.0.0/kafka_2.11-2.0.0.tgz)\
  to [verify](https://www.apache.org/info/verification.html) use [these keys](https://kafka.apache.org/KEYS)
- unpack the archive, e.g.\
  `tar -xzf kafka_2.11-2.0.0.tgz` (Linux)\
  extraction tool for you choice (Windows)
- switch to unpacked kafka:\
  `cd kafka_2.11-2.0.0`
- maybe you need to create directory for (application) logs with:\
  `mkdir logs`
- start zookeeper with:\
  `bin/zookeeper-server-start.sh config/zookeeper.properties` (Linux/MacOS)\
  `bin\windows\zookeeper-server-start.bat config\zookeeper.properties` (Windows)
- start kafka server (single instance):\
  `bin/kafka-server-start.sh config/server.properties` (Linux/MacOS)\
  `bin\windows\kafka-server-start.bat config\server.properties` (Windows)
- create a topic for this single instance:\
  `bin/kafka-topics.sh --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --create --topic my_topic_name` (Linux/MacOS)\
  `bin\windows\kafka-topics.bat --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --create --topic my_topic_name` (Windows)
- list existing topics:\
  `bin/kafka-topics.sh --zookeeper localhost:2181 --list` (Linux/MacOS)\
  `bin\windows\kafka-topics.bat --zookeeper localhost:2181 --list` (Windows)
- send message:\
  `bin/kafka-console-producer.sh --broker-list localhost:9092 --topic my_topic_name` (Linux/MacOS)\
  `bin\windows\kafka-console-producer.bat --broker-list localhost:9092 --topic my_topic_name` (Windows)
- start a consumer:\
  `bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --from-beginning --topic my_topic_name` (Linux/MacOS)\
  `bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9092 --from-beginning --topic my_topic_name` (Windows)

No errors will occur stopping consumer and producer before kafka, and kafka before zookeeper.

Note: Data is stored in default logDir under `/tmp/kafka-logs`. It can be cleared on boot or by a
cron job, depending on the operating system.

## Exercises

### Show the map
[TomTom Maps SDK for Web](https://developer.tomtom.com/maps-sdk-web) is used as map.
*A valid API key must by inserted into [index.html](src/main/resources/static/index.html).* An API
key for testing the map can be requested after registering on [developer.tomtom.com](https://developer.tomtom.com/user/register).
A simple alternative is using the *kafka-console-consumer* introduced above!

1. start web server providing the content:\
  `./gradlew bootRun`
2. navigate to [localhost:8080](http://localhost:8080/index.html)
3. choose account and connect the web socket

#### Show the data as text
Use if API key is out of scope.
1. start web server providing the content:\
  `./gradlew bootRun`
2. navigate to [localhost:8080/text.html](http://localhost:8080/text.html)
3. choose account and connect the web socket

### Exercise Producer
1. read array of 4190 entries from [all_tracks.json](src/main/resources/all_tracks.json)\
   to see entry format look at [a_track.json](src/main/resources/a_track.json)
2. write them to a new topic (for JSON use org.springframework.kafka.support.serializer.JsonSerializer)
- Demo via web-server-map: `./gradlew bootRun -Pargs=--map.source.topic=my_topic`
- Hint 1: check data types, some fields needs to be adjusted (market of Leipzig is located at 51.340674, 12.374684)
- Hint 2: write a MessageListener to verify count of written messages\
  Or use the console consumer to verify written messages.
- Hint 3: consider time to be live, feed entries continuously

### Exercise Consumer
1. read from a topic using JSON deserialization
- Optional: Setup a second consumer in the same group (topic must have 2 or more partitions)
- Optional: How to deal with changes in data structure: add, remove or rename field?\
  Is JSON the best choice? Try [Avro](https://avro.apache.org/), [Thrift](http://thrift.apache.org/)
  or [Protocol Buffer](http://code.google.com/p/protobuf/). Is it worth adding more complexity(?)
  to facilitate structure evolution?

### Exercise Stream Processing
1. produce topic area violation (stream join)
  - given the known track entry topic and a topic of valid areas
  - see [GlobalKTable](https://docs.confluent.io/current/streams/concepts.html#globalktable)

Other idea:
- produce topic speeding offence (stateful processing):
  * calculate average tour speed
  * calculate 5 minute average speed

## Licenses
This tutorial includes Gradle-Wrapper, SockJS-client and TomTom JavaScript SDK.

The tutorial itself is licensed under Apache License Version 2.0.
Check [LICENSE](LICENSE) for details.

## API KEY
Please note that you need to have a valid **api key** for the map which can be obtained at
[TomTom's Developer Portal](http://developer.tomtom.com).
After registration you can request an API key for free. It provides a limited amount of map
transactions for evaluation.

## Footnotes
<a name="data-policy">*</a>: Neither drivers nor vehicles privacy was harmed while generating
                             tracking data.
