# Build Finder

[Build Finder](https://github.com/project-ncl/build-finder/) iterates over any files or directories in the input, recursively scanning any supported (possibly compressed) archive types, locating the associated Koji build for each file matching any given Koji archive type. It attempts to find at least one Koji build containing the file checksum (duplicate builds result in a warning) and records files that don't have any corresponding Koji build to a file. For files with a corresponding Koji build, if the Koji build does not have a corresponding Koji task, it reports the build as an *import*. For builds with a corresponding Koji task, it writes information about the build to a file. Additionally, it writes various reports about the builds.

## Build Status

| Name      | Description                | Badge                      |
| --------- | -------------------------- | -------------------------- |
| Maven     | Latest Release             | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.redhat.red.build/koji-build-finder/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.redhat.red.build/koji-build-finder)
| AppVeyor  | Build Status (Windows)     | [![Build Status (AppVeyor)](https://ci.appveyor.com/api/projects/status/g1bjwaun03fao5qm?svg=true)](https://ci.appveyor.com/project/dwalluck/build-finder) |
| Travis CI | Build Status (Linux/OS X)  | [![Build Status (Travis CI)](https://api.travis-ci.com/project-ncl/build-finder.svg)](https://travis-ci.com/project-ncl/build-finder) |
| Codecov   | Code Coverage              | [![Code Coverage](https://codecov.io/gh/project-ncl/build-finder/graph/badge.svg)](https://codecov.io/gh/project-ncl/build-finder) |
| Snyk      | Known Vulnerabilities      | [![Known Vulnerabilities](https://snyk.io/test/github/project-ncl/build-finder/badge.svg)](https://snyk.io/test/github/project-ncl/build-finder) |

## Development

To format the code, run `net.revelc.code.formatter:formatter-maven-plugin:format`.

[Apache Maven](https://maven.apache.org/) is used for the building. The command `mvn clean install` will compile the code and run all of the unit tests.

## Operation

The support for various compressed archive types relies on [Apache Commons VFS](https://commons.apache.org/proper/commons-vfs/) and the compressor and archive formats that Commons VFS can open *automatically*. If an exception occurs while trying to open a file, then the file is considered to be a normal file and recursive processing of the file is aborted.

The default supported Koji archive types are `jar`, `xml`, `pom`, `so`, `dll`, and `dylib`. Build Finder uses [Koji Java Interface](https://github.com/release-engineering/kojiji) for Koji support and asks for all known extensions for the given Koji archive type name. Note that if you specify no Koji archive types, Build Finder will ask the Koji server for all known Koji archive types. The default set of types is meant to give a reasonable default, particularly for Java-based distributions.

Build Finder operates in three stages:

1. Checksums are calculated offline for all files in the distribution, including files inside archives. Checksum information is stored in JSON format.

2. An online Koji archive lookup in performed for each checksum in stage one and the respective archive, if found, is mapped to the corresponding Koji build. The build is either an *import* and has no corresponding Koji task information or is *built from source* and includes corresponding Koji task information. Build information is stored in JSON format.

3. Reports are generated from the archive and build information gathered in the first two stages. The format of the reports is HTML and/or text.

## Usage

To see the available options, execute the command `java -jar target/build-finder-<version>.jar --help`, where `<version>` is the Build Finder version. The options are as follows:

    Usage: build-finder [OPTIONS] FILE...
    Finds builds in Koji and PNC.
          FILE...                One or more files.
      -a, --archive-type=STRING  Add a Koji archive type to check.
                                   Default: [jar, xml, pom, so, dll, dylib]
      -b, --build-system=BUILD_SYSTEM
                                 Add a build system (none, koji, pnc).
                                   Default: [pnc, koji]
      -c, --config=FILE          Specify configuration file to use.
                                   Default: ${user.home}.
                                   build-finder/config.json
          --cache-lifespan=LONG  Specify cache lifespan.
                                   Default: 3600000
          --cache-max-idle=LONG  Specify cache maximum idle time.
                                   Default: 3600000
      -d, --debug                Enable debug logging.
          --disable-cache        Disable local cache.
          --disable-recursion    Disable recursion.
      -e, --archive-extension=STRING
                                 Add a Koji archive type extension to check.
                                   Default: [dll, dylib, ear, jar, jdocbook,
                                   jdocbook-style, kar, plugin, pom, rar, sar, so,
                                   war, xml]
      -h, --help                 Show this help message and exit.
      -k, --checksum-only        Only checksum files and do not find builds.
          --koji-hub-url=URL     Set Koji hub URL.
          --koji-multicall-size=INT
                                 Set Koji multicall size.
                                   Default: 8
          --koji-num-threads=INT Set Koji num threads.
                                   Default: 12
          --koji-web-url=URL     Set Koji web URL.
          --krb-ccache=FILE      Set location of Kerberos credential cache.
          --krb-keytab=FILE      Set location of Kerberos keytab.
          --krb-password[=STRING]
                                 Set Kerberos password.
          --krb-principal=STRING Set Kerberos client principal.
          --krb-service=STRING   Set Kerberos client service.
      -o, --output-directory=FILE
                                 Set output directory.
                                   Default: .
          --pnc-partition-size=INT
                                 Set Pnc partition size.
                                   Default: 18
          --pnc-url=URL          Set Pnc URL.
      -q, --quiet                Disable all logging.
      -t, --checksum-type=CHECKSUM
                                 Add a checksum type (md5, sha1, sha256).
                                   Default: [md5, sha1, sha256]
          --use-builds-file      Use builds file.
          --use-checksums-file   Use checksums file.
      -V, --version              Print version information and exit.
      -x, --exclude=PATTERN      Add a pattern to exclude from build lookup.
                                   Default: [^(?!.*/pom\.xml$).*/.*\.xml$]

### Running via Docker containers

There is a `Dockerfile` and a `Makefile` supplied in the code repository. If you are unfamiliar with Java-based projects, you can easily create a container image and run Build Finder in a Fedora Linux container by executing the following commands in a shell:
1. Build the container image:

```
$ make build
```

2. Invoke shell in the container so you can try the tool out:

```
$ make shell
# java -jar target/build-finder-<version>.jar
```

where `<version>` should be replaced with the current version of the software.

## Getting Started

On the first run, Build Finder will write a starter configuration file. You may optionally edit this file by hand, but you do not need to create it ahead of time as Build Finder will create a default configuration file if none exists.


### Configuration file format

The configuration file is in JSON format. The default configuration file, `config.json`, is as follows.

    {
      "archive-extensions" : [ "dll", "dylib", "ear", "jar", "jdocbook", "jdocbook-style", "kar", "plugin", "pom", "rar", "sar", "so", "war", "xml" ],
      "archive-types" : [ "jar", "xml", "pom", "so", "dll", "dylib" ],
      "build-systems" : [ "pnc", "koji" ],
      "cache-lifespan" : 3600000,
      "cache-max-idle" : 3600000,
      "checksum-only" : false,
      "checksum-type" : [ "sha1", "sha256", "md5" ],
      "disable-cache" : false,
      "disable-recursion" : false,
      "excludes" : [ "^(?!.*/pom\\.xml$).*/.*\\.xml$" ],
      "koji-multicall-size" : 8,
      "koji-num-threads" : 12,
      "output-directory" : ".",
      "pnc-partition-size" : 18,
      "use-builds-file" : false,
      "use-checksums-file" : false
    }

The `archive-extensions` option specifies the Koji archive type extensions to include in the archive search. If this option is given, it will override the `archive-types` option and only files matching the extensions will have their checksums taken.

The `archive-types` option specifies the Koji archive types to include in the archive search.

The `build-system` option specifies the build systems to use for search.

The `cache-lifespan` option specifies the cache entry lifespan in milliseconds.

The `cache-max-idle` option specifies the cache entry maximum idle time in milliseconds.

The `checksum-only` option specifies whether or not to skip the Koji build lookup stage and only checksum the files in the input. This stage is performed offline, whereas the build lookup stage is online.

The `checksum-type` option specifies the checksum type to use for lookups. Note that at this time Koji can only support a single checksum type in its database, `md5`, even though the Koji API currently provides additional support for `sha256` and `sha512` checksum types.

The `disable-cache` option disables the local infinispan cache for checksums and builds.

The `disable-recursion` option disables recursion when examining archives.

The `excludes` option is list of regular expression patterns. Any paths that match any of these patterns will be excluded during the build-lookup stage search.

The `koji-multicall-size` option sets the Koji multicall size.

The `koji-num-threads` option sets the number of Koji threads.

The `koji-hub-url` and `koji-web-url` options must be set to valid URLs for your particular network.

The `pnc-partition-size` option sets the Pnc partition size.

The `pnc-url` option must be set to a valid URL for your particular network if you want Pnc support.

The `output-directory` option specifies the directory to use for output.

The `use-checksums-file` and `use-builds-file` options specify whether to load any existing `checksums.json` or `builds.json` file, respectively. These files are always written, but not loaded by default.

All of the options found in the configuration file can also be specified and overridden via command-line options.

### Command-line options

The `koji-*-url` options are the only required command-line options (if not specified in the configuration file) and these options specify the URLs for the Koji server. If running Build Finder for the first time, you should pass these options so that they are written to the configuration file.

The `krb-*` options are used for logging in via Kerberos as opposed to via SSL as it does not require the additional setup of SSL certificates. Note that the [Apache Kerby](https://directory.apache.org/kerby/) library is used to supply Kerberos functionality. As such, interaction with the other Kerberos implementations, such as the canonical MIT Kerberos implementation, may not work with the `krb-ccache` or `krb-keytab` options. The `krb-principal` and `krb-password` options are expected to always work, but care should be taken to protect your password. Note that when using the `krb-*` options, the `krb-service` option is necessary in order for Kerberos login to work.

### Execution

After optionally completing setup of the configuration file, `config.json`, you can run the software with a command as follows.

    java -jar build-finder-<version>.jar /path/to/distribution.zip

where `<version>` is the current version of the software and `/path/to/distribution.zip` is the path to the file that you wish to examine. In this execution, Build Finder will read through the file `distribution.zip`, trying to match each file entry against a build in the Koji database provided that the file name matches one of the specified Koji archive types and does not match the exclusion pattern.

On the first completed run, the software will create a `checksum-<checksum-type>.json` file to cache the file checksums and a `builds.json` file to cache the Koji build information. Currently, if you wish to perform a clean run, you must *manually* remove the JSON files. Otherwise, the cache will be loaded and the information will not be recalculated. Alternatively, you may specify a different output directory for each run using the `--output-directory` option.

## Output File Formats

This section describes the JSON files used for caching the distribution information between runs in more detail.

### Checksums

The `checksum-md5.json` file contains a map where the key is the MD5 checksum of the file and the value is a list of all files with the checksum. Note that it is possible to have more than one file with the given checksum. For completeness, the `checksum-md5.json` file contains every single file found in the input, including any files found by recursively scanning compressed files or inside archive files.

### Builds

The `builds.json` file contains a map where the key is the Koji build ID. The special ID of 0 is used for files with no associated build, as Koji builds start at ID 1. The map values contain additional maps. A partial list of what is contained is in the value maps is: Koji Build Info, Koji Task Info, Koji Task Request, Koji Archive, a list of all remote archives associated with the build and a list of local files from the distribution associated with this build.

## Reports

After a completed run, several output files are produced in the current directory. These files are overwritten on additional runs, so if the output files need to be saved between multiple runs, then specify unique directories for each run.

### Builds Report

This is an HTML-based report located in the file `output.html`. It contains all Koji builds found as well as any problems associated with the builds.

#### Problems flagged

The report currently reports total builds, including number of builds that are *imports*. Additionally, it reports:

* Matching files with no Koji build associated. These are potentially files that need to be rebuilt from source, for example, a dynamic library downloaded from upstream during the build process.

* Builds that are *imports* and not built from source. These represent files which, as they are builds with a known community import in the Koji database, almost certainly need to be built from source and/or removed from the distribution if not required at runtime. These often appear inside shaded jars and the like.

### Statistics Report

This is an HTML-based report which displays various statistics about the distribution, including the number and percentage of builds and artifacts built from source. Note that the total number of artifacts includes *not-found* artifacts. If you wish to exclude these not-found artifacts, use the `--excludes` option with the appropriate pattern(s).

### Products Report

This is an HTML-based report which displays the list of builds partitioned by product (Koji build target). Note that the report tries to find a minimal set of products which cover the set of builds. Therefore, there will only be one product shown per build, even if the build appears in multiple products.

### Koji builds (NVR) Report

This is a text-based report located in the file `nvr.txt`. The format of the file is one `name-version-release` per line, as is typical with Koji native builds and RPMS.

### Maven artifacts (GAV) Report

This is a text-based report located in the file `gav.txt`. The format of the file is one `groupId:artifactId:version` per line, as is typical with Maven builds.
