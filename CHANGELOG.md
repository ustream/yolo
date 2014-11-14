# Changelog

2.0.2
-----
 - Use ospackage-plugin for building debian package
 - configure uploadDebianArchive task for uploading the debian package to a maven repository

2.0.1
-----
 - Keep Java 1.7 compatibility (just build changes)

2.0.0
-----
 - Java 8 compatibility (fix some tests)
 - Test nashorn as JavaScript engine (in the script file simply create a function called parse, check examples)
 - use Gradle 2.2

1.3.1
-----
 - Better exception logging

1.3.0
-----
 - Add filter support to JSON parser
 - Allow optional list type in config
 - Bugfix: handle null value properly when flattening JSON

1.2.0
-----
 - Gzip support

1.1.0
-----
 - Adding changelog :)
 - When using dynamic filename (with wildcards) all matching files will be used. Newly created or deleted matching files are also handled
 - Handle escaped wildcards in file parameter (\\* or \?)
 - Use dynamic versions in dependencies
