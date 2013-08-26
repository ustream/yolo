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
# the first time duplicate the gradle properties and set your custom build config (like Archiva)
cp gradle.properties.dist gradle.properties

# run tests, if you want
./gradlew test

# run build
./gradlew buildJar

# check if it works
java -jar gradlebuild/libs/loggy-1.0.0.jar -help
```
 
## Sample configuration

An example config file can be found in [example.json](src/main/config/example.json).

## Command line parameters

Just run the jar with "-help" option.

```bash
usage: loggy
 -config <path>   path to config file
 -debug           print debugging information
 -file <path>     path to logfile
 -help            print this message
 -listModules     list available modules
 -reopen          reopen file between reading the chunks
 -whole           tail file from the beginning
```

## Example usage

```bash
java -jar gradlebuild/libs/loggy-1.0.0.jar -config /YOURPATH/src/main/config/example.json -file /YOURPATH/foo.log -debug -whole
```

## Available modules

Just run the jar with "-listModules" option.

```bash
Available processors
--------------------

· com.ustream.loggy.module.processor.CompositeProcessor - runs multiple processors
  · class [String], required
  · processors [List], required
  · parser processorParams:

· com.ustream.loggy.module.processor.ConsoleProcessor - writes parameters to console, use it for debug purposes
  · class [String], required

· com.ustream.loggy.module.processor.NoOpProcessor - does nothing, use it if you want to disable a parser temporarily
  · class [String], required

· com.ustream.loggy.module.processor.StatsDProcessor - sends metrics to statsd, it handles counter, gauge and timing values
  · class [String], required
  · prefix [String]
  · host [String], required
  · port [Number], default: 8192
  · parser processorParams:
    · type [String], required, allowed values: [count, gauge, time]
    · key [String], required
    · value [String|Number], required

Available parsers
-----------------

· com.ustream.loggy.module.parser.PassThruParser - forwards all lines to processor, runs always
  · class [String], required
  · processor [String], required
  · processorParams [Map]

· com.ustream.loggy.module.parser.RegexpParser - parses line via regular expression and returns with matches
  · class [String], required
  · processor [String], required
  · processorParams [Map]
  · regex [String], required

```
