gradle-geb
==========

Gradle plugin for UI tests with [geb](http://www.gebish.org/) and [spock](http://code.google.com/p/spock/)

Adds tasks for geb-based UI tests to a gradle project.  
Currently, the plugin should work with gradle 1.6.

## What it should do
Eventually, the plugin should be able to analyze the tests in the project. For the non-UI tests, everything should stay like it is, they should be run by the default `gradle test` task.  
For the UI tests, a custom test task should be added for every supported browser, i.e. `firefoxTest`, `ieTest` etc., probably with a dependency from the `test` task to one of the UI tasks, maybe depending on the OS.  
When `gradle test` is run, the non-UI tests should be run as usual, for the UI tests, an embedded jetty should be started before the respective task's execution and terminated afterward.


## Current issues
* Lacks documentation
* Lacks tests
* Lacks license headers
* Does not specify test inputs/outputs, so tests are always out-of-date
* Spock configs are used to run only the UI tests in the UI test tasks and the non-UI tests in the regular test task. The test reports for the different tasks are generated with empty reports for the excluded tests (see also http://forums.gradle.org/gradle/topics/how_can_i_specify_test_classes_filtered_by_whether_they_extend_a_certain_base_class),
* Jetty is not explicitly shut down after running the tests, I'm not sure if this is an issue. If it is, we could define a stop port and key for the `jettyTest` task and use those to shut down the container.

## Licensing
The code is licensed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).


## Contibuting
Any sort of help is absolutely welcome. If you have found a problem or even written a patch, feel free to create an issue or pull request.
