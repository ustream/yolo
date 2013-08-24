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

An example config file can be found in [example.json](src/main/config/example.json)

## Command line parameters

Just run the jar with "-help" option.

```bash
usage: loggy
 -config <path>   path to config file
 -debug           print debugging information
 -file <path>     path to logfile
 -help            print this message
 -reopen          reopen file between reading the chunks
 -whole           tail file from the beginning
```

## Example usage

```bash
java -jar gradlebuild/libs/loggy-1.0.0.jar -config /YOURPATH/src/main/config/test.json -file /YOURPATH/foo.log -debug -whole
```
