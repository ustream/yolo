# Loggy

A general log tailer and parser tool written in Java, inspired by [Parsible](https://github.com/Yipit/parsible) and [Logster](https://github.com/etsy/logster). It is easily extensible with new parsers and processors. The main purpose was to build a simple tool to get metrics values and errors from log files and report to StatsD (or Graphite), but everyone can write custom modules in minutes.

## Main concepts

* **Parser**: the parser parses a log line and returns with parameters (a map). There is a builtin configurable regexp parser.
* **Processor**: the processor gets the parser's parameters and do anything with them, like send key/values to StatsD
* **Flexible configuration**: the configuration is in JSON format for easier management. You can define your parsers and processors and bind them as you want.
* **Real time**: the tool tails the log file nearly realtime (1 second batch reading mode)
* **Whole file reading**: with a cli parameter you can read your logfile from the beginning
* **Logrotate friendly**: works easily with logrotate or other log rotating tools

## Build

The project uses Gradle and it is embedded with a Gradle wrapper.

```bash
# run tests, if you want
./gradlew test

# create jar file
./gradlew jar

# check if it works
java -jar build/libs/loggy-[version].jar -help
```
 
## Sample configuration

An example config file can be found in [example.json](src/main/config/example.json).

## Command line parameters

Just run the jar with "-help" option.

```bash
$ java -jar build/libs/loggy-[version].jar -help
usage: loggy
 -config <path>   path to config file
 -file <path>     path to logfile
 -help            print this message
 -listModules     list available modules
 -reopen          reopen file between reading the chunks
 -whole           tail file from the beginning
```

## Example usage

```bash
$ java -jar build/libs/loggy-[version].jar -config /YOURPATH/src/main/config/example.json -file /YOURPATH/foo.log
```

## Available modules

Just run the jar with "-listModules" option.

```bash
$ java -jar build/libs/loggy-[version].jar -listModules

Available processors
--------------------

* tv.ustream.loggy.module.processor.CompositeProcessor - runs multiple processors
  - class [String], required
  - processors [List], required

* tv.ustream.loggy.module.processor.ConsoleProcessor - writes parameters to console, use it for debug purposes
  - class [String], required

* tv.ustream.loggy.module.processor.NoOpProcessor - does nothing, use it if you want to disable a parser temporarily
  - class [String], required

* tv.ustream.loggy.module.processor.StatsDProcessor - sends metrics to statsd, it handles counter, gauge and timing values
  - class [String], required
  - prefix [String]
  - host [String], required
  - port [Number], default: 8192
  - processParams:
    - type [String], required, allowed values: [count, gauge, time]
    - key [String], required
    - value [String|Number], required

Available parsers
-----------------

* tv.ustream.loggy.module.parser.PassThruParser - forwards all lines to processor, runs always
  - class [String], required
  - processor [String], required
  - processParams [Map]

* tv.ustream.loggy.module.parser.RegexpParser - parses line via regular expression and returns with matches
  - class [String], required
  - processor [String], required
  - processParams [Map]
  - regex [String], required

```

## Debugging

To display debug messages use the -Dorg.slf4j.simpleLogger.defaultLogLevel=debug option.

```bash
$ java -Dorg.slf4j.simpleLogger.defaultLogLevel=debug -jar build/libs/loggy-[version].jar -config /YOURPATH/src/main/config/example.json -file /YOURPATH/foo.log
```

## Licence

This project is licensed under the terms of the [MIT License (MIT)](LICENCE.md).


## Authors

* [bandesz](https://github.com/bandesz)
