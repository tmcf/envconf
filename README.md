# envconf

An environment, properties, and config file loader.

This project is based substantially on @weavejester's excellent *environ* project.

*envconf* has some minor additions to *environ* concerning multiple config files and java interop which are very specific to my use cases and don't fit the spirit of *environ*.

So I would highly recommend using *environ* unless your needs absolutely match this project.

## Usage

*envconf* loads environment settings in order from:

- configuration files  (edn format)
- environment variables
- java properties
- final configuration files (edn) to allow override of a common environment. This is mostly for dev / testing use.

The configuration file lists are specified in two path environment variables, which contain a list of ; or : separated path names,
to match either unix or windows paths. (Windows is not tested)

- ENVCONF_PATH,  default: ".envconf:.envconf-instance"
- ENVCONF_PATH_FINAL, default: ".envconf-final:.envconf-final-instance"

The use of a main config file and a "-instance" suffixed file is simply to keep specific instance info separate, if needed.

The *final* configuration provides a simple way to override the current environment
and has been useful in dev / testing environments but is not expected to be used in production.

All configuration files are optional, and any missing files are simply skipped.

Configuration files are processed left to right, merging, so right-most files override left. After the ENVCONF_PATH files are loaded,
the java System getenv items are merged, followed by the java System properties. Finally the ENVCONF_PATH_FINAL files are processed.

## Credits

- @weavejester, James Reeves, **environ**
- https://github.com/weavejester/environ


## License

Copyright © 2016 Ted McFadden

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.

This project is distributed under the same license as environ on which is based as of April 2016.
