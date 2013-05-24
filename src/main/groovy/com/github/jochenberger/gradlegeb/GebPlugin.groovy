package com.github.jochenberger.gradlegeb

import groovy.io.FileType

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.jetty.JettyRun
import org.gradle.api.tasks.testing.Test

class GebPlugin implements Plugin<Project>{

  @Override
  public void apply(Project p) {

    p.apply plugin: 'jetty'
    p.apply plugin: 'groovy'
    p.extensions.create('geb', GebPluginExtension)
    p.afterEvaluate{
      p.dependencies.add("testCompile", "org.gebish:geb-spock:$p.geb.gebVersion")
      p.dependencies.add("testCompile", "org.codehaus.groovy:groovy-all:$p.geb.groovyVersion")

      p.dependencies.add("testCompile", "org.spockframework:spock-core:$p.geb.spockVersion")
      p.geb.drivers.each {
        p.dependencies.add("testCompile", "org.seleniumhq.selenium:selenium-$it-driver:$p.geb.seleniumVersion")
      }
      p.dependencies.add("testCompile", "org.seleniumhq.selenium:selenium-support:$p.geb.seleniumVersion")
    }

    p.task([type: JettyRun, description: "Starts a Jetty server", group: "Check"], "jettyTest")
    p.tasks.test.doFirst {
      File spockConfig = File.createTempFile("spockconfig", ".groovy", temporaryDir)
      spockConfig.text = GebPlugin.class.getResourceAsStream("SpockConfig.groovy").text
      systemProperty "spock.configuration", spockConfig.absolutePath
    }

    Closure removeEmptyResults = {
      logger.debug "Analyzing results in $testResultsDir"
      testResultsDir.eachFileMatch FileType.FILES, ~/.*\.xml/, {
        logger.debug "Analyzing $it"
        int numberOfTests = new XmlSlurper().parse(it).@tests.text() as int
        if (numberOfTests == 0){
          logger.debug "$it is empty"
          it.delete()
        }else{
          logger.debug "$it has $numberOfTests test results"
        }
      }
    }
    p.afterEvaluate{
      p.geb.drivers.each { String d ->

        Task t = p.task([type: Test, description: "Run Selenium tests with $d", group: "Test"], "${d}Test").doFirst {
          File spockConfig = File.createTempFile("spockconfig", ".groovy", temporaryDir)
          spockConfig.text = GebPlugin.class.getResourceAsStream("SpockConfigUI.groovy").text
          systemProperty "spock.configuration", spockConfig.absolutePath
          systemProperty "geb.env", d
          systemProperty "geb.build.baseUrl", "http://localhost:${p.tasks.jettyTest.httpPort}/${p.tasks.jettyTest.contextPath}/"
          testResultsDir p.file("${p.buildDir}/${d}test-results")
          testReportDir p.file("${p.reporting.baseDir}/${d}tests")
        }

        t.doFirst {
          p.tasks.jettyTest.daemon = true;
          p.tasks.jettyTest.execute();
        }

        t.doLast removeEmptyResults
        //t.inputs.files p.tasks.test.inputs.files
        //t.outputs.files p.tasks.test.outputs.files

        p.tasks.test.dependsOn(t)
      }
    }
    p.tasks.test.doLast removeEmptyResults
    // TODO shut-down jetty after tests

    //		p.tasks.test.doLast {
    //			p.tasks.jettyStop.execute()
    //		}
  }
}
