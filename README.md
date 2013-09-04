# Yolo [![Build Status](https://travis-ci.org/ustream/yolo.png?branch=master)](https://travis-ci.org/ustream/yolo)

**You only log once but you can easily parse your data as many ways as you want.**

A general log tailer and parser tool written in Java, inspired by [Parsible](https://github.com/Yipit/parsible) and [Logster](https://github.com/etsy/logster). It is easily extensible with new parsers and processors. The main purpose was to build a simple tool to get metrics values and errors from log files and report to [StatsD](https://github.com/etsy/statsd/) (and [Graphite](http://graphite.wikidot.com/)), but everyone can write custom modules in minutes.

[GitHub Page](http://ustream.github.io/yolo/)

## Main concepts

* **Flexible configuration**: the configuration is in JSON format for easier management. You can define your parsers and processors and bind them as you want.
* **Real time**: the tool tails the log file realtime
* **Whole file reading**: with a cli parameter you can read your logfile from the beginning
* **Handle dynamic filenames**: you can use wildcards in filename (but only the first match will be used)
* **Logrotate friendly**: works easily with logrotate or other log rotating tools
* **Easily debuggable**: debug mode writes verbose logs, and you can use built-in parsers and processors for debugging purposes

## Notice

Because we tail the log file and the application can stop anytime therefore it is not guaranteed that the tool will parse all the lines. In the near future we plan to implement a secure reader which stores the read offset and handles even log rotate events.

## Modules

Each module is stateless, has a description and predefined configuration.

* **Parser**: the parser parses a log line and returns with parameters (a map).
* **Processor**: the processor gets the parser's parameters and does anything with them, like send key/values to StatsD

## Process

* the file tailer reads a new line from the file
* the handler iterates through all the parsers and finds the first which returns with a non-null value
* the output value is passed to the given processor with the "processParams" configuration given in the config
* the processor processes the data
* the handler runs all the parsers (regardless the first match) which runs always. (currently it is only the passthru parser)

## Build

The project uses Gradle and it is embedded with a Gradle wrapper.

```bash
# run tests, if you want
./gradlew test

# create runnable jar file
./gradlew jar

# check if it works
java -jar build/libs/yolo.jar -help
```

### Build Debian package

```bash
./gradlew debian
```

The .deb file will be placed in the build/linux-package directory.

The package contains the runnable jar file, which will be copied to /usr/lib/yolo when installed.

## Sample configuration

An example config file can be found in [example.json](src/main/config/example.json).

## Command line parameters

Simply run the jar with "-help" option.

```bash
$ java -jar build/libs/yolo.jar -help
usage: yolo
 -config <path>   path to config file
 -file <path>     path to logfile, , wildcards are accepted
 -help            print this message
 -listModules     list available modules
 -reopen          reopen file between reading the chunks
 -whole           tail file from the beginning
```

## Example usage

```bash
$ java -jar build/libs/yolo.jar -config /YOURPATH/src/main/config/example.json -file /YOURPATH/foo.log
```

## Available modules

Simply run the jar with "-listModules" option.

```bash
$ java -jar build/libs/yolo.jar -listModules

Available processors
--------------------

* tv.ustream.yolo.module.processor.CompositeProcessor - runs multiple processors
  - params: Map {
      class: String, required
      processors: List, required
    }

* tv.ustream.yolo.module.processor.ConsoleProcessor - writes parameters to console, use it for debug purposes
  - params: Map {
      class: String, required
    }

* tv.ustream.yolo.module.processor.GraphiteProcessor - sends metrics to Graphite
  - params: Map {
      port: Number, default: 2003
      host: String, required
      class: String, required
      flushTimeMs: Number, default: 1000
    }
  - processParams: Map {
      keys: List [
        Map {
          timestamp: String, pattern allowed
          value: String|Number, required, pattern allowed
          multiplier: Number, default: 1
          key: String, required, pattern allowed
        }
      ]
    }

* tv.ustream.yolo.module.processor.StatsDProcessor - sends metrics to StatsD, handles counter, gauge and timing values
  - params: Map {
      port: Number, default: 8125
      host: String, required
      prefix: String, required
      class: String, required
    }
  - processParams: Map {
      keys: List [
        Map {
          value: String|Number, required, pattern allowed
          type: String, required, allowed values: [counter, gauge, timer]
          multiplier: Number, default: 1
          key: String, required, pattern allowed
        }
      ]
    }

Available parsers
-----------------

* tv.ustream.yolo.module.parser.PassThruParser - forwards all lines to processor, runs always
  - params: Map {
      enabled: Boolean, default: true
      class: String, required
      processor: String, required
      processParams: Map
    }

* tv.ustream.yolo.module.parser.RegexpParser - parses line via regular expression and returns with matches
  - params: Map {
      enabled: Boolean, default: true
      regex: String, required
      class: String, required
      processor: String, required
      processParams: Map
    }

```

## Create your own parser

Check [RegexpParser](src/main/java/tv/ustream/yolo/module/parser/RegexpParser.java) for a compact example.

If you are ready, add your parser class to ModuleFactory.availableParsers.

```java
public class TestParser implements IParser
{

    @Override
    public Map<String, String> parse(String line)
    {
        // parse a line here and return with null if no match happened or with a Map if you want to process it
    }

    @Override
    public boolean runAlways()
    {
        // return with true if you want the module to always run for every line regardless of the first match
    }

    @Override
    public List<String> getOutputKeys()
    {
        // return with the keys you will return in parse
    }

    @Override
    public void setUpModule(Map<String, Object> parameters)
    {
        // read the config and set up your parser
    }

    @Override
    public ConfigMap getModuleConfig()
    {
        // build your module's config
    }

    @Override
    public String getModuleDescription()
    {
        // return with a simple description for your module
    }
}
```

## Create your own processor

Check [StatsDProcessor](src/main/java/tv/ustream/yolo/module/processor/StatsDProcessor.java) for a compact example.

If you are ready, add your processor class to ModuleFactory.availableProcessors.

```java
public class TestProcessor implements IProcessor
{

    @Override
    public ConfigMap getProcessParamsConfig()
    {
        // build the processParams config for parsers
    }

    @Override
    public void process(Map<String, String> parserOutput, Map<String, Object> processParams)
    {
        // process the parser's output. The processParams map can contain ConfigPattern objects where you can subtitute your own values
    }

    @Override
    public void setUpModule(Map<String, Object> parameters)
    {
        // read configuration and set up your processor
    }

    @Override
    public ConfigMap getModuleConfig()
    {
        // build your module's config
    }

    @Override
    public String getModuleDescription()
    {
        // return with a simple description for your module
    }

	@Override
	public void stop()
	{
		// here you can stop your processor gracefully
	}

}
```

## Module configuration handling

The configuration is validated with ConfigMap objects which can be built the following way.

```java
ConfigMap config = new ConfigMap();

// add "key1" key with string type, the value will be required
config.addConfigValue("key1", String.class);

// add "key2" key with number type, the value will be optional, and the default value will be 5
config.addConfigValue("key1", Number.class, false, 5);

// add "key3" key with string|number type
ConfigValue<Object> configValue = new ConfigValue<Object>("key3", Object.class);
configValue.setAllowedTypes(Arrays.<Class>asList(String.class, Number.class));
config.addConfigValue(configValue);

// add "key4" key as an enumeration
ConfigValue<String> configValue = new ConfigValue<String>("key4", String.class);
configValue.setAllowedValues(Arrays.asList("value1", "value2"));
config.addConfigValue(configValue);

// add "key5" key which allows a config pattern
ConfigValue<String> configValue = new ConfigValue<String>("key5", String.class);
configValue.allowConfigPattern();
config.addConfigValue(configValue);
```

### Config patterns

Config pattern means a string value with key placeholders, like 'this is #type#'. This can be used in the process parameters configuration, when you want to substitute values from the parser output in the process config.

If you check the [configuration example](src/main/config/example.json), you can see that the statsd process parameters contain expressions like #exceptionName# or #val#, these values will be substituted from the regexp matches.

## Logging

The tool uses [SLF4J](http://www.slf4j.org/) for logging with the SimpleLogger implementation. The minimum log level is INFO and all the logs are written to the standard error output.

To display debug messages use the -Dorg.slf4j.simpleLogger.defaultLogLevel=debug option.

```bash
$ java -Dorg.slf4j.simpleLogger.defaultLogLevel=debug -jar build/libs/yolo.jar -config /YOURPATH/src/main/config/example.json -file /YOURPATH/foo.log
```

## Dependencies

For a detailed dependency list please check the "dependencies" block in [build.gradle](build.gradle).

**Compile dependencies**:

* [Apache Commons CLI](http://commons.apache.org/proper/commons-cli/)
* [Apache Commons IO](http://commons.apache.org/proper/commons-io/)
* [Gson](http://code.google.com/p/google-gson/)
* [java-statsd-client](https://github.com/youdevise/java-statsd-client)
* [SLF4j](http://www.slf4j.org/)

**Testing dependencies**:

* [JUnit](http://junit.org/)
* [Mockito](http://code.google.com/p/mockito/)

## Contributing

Feel free to fork this project and send pull requests if you want to help us improve the tool or add new parsers/processors. But before sending us new modules, please think through if the module serves a general purpose and whether it will be useful for others, not just for you.

## Licence

This project is licensed under the terms of the [MIT License (MIT)](LICENCE.md).

## Authors

* [bandesz](https://github.com/bandesz)
