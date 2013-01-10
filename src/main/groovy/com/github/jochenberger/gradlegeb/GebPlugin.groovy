package com.github.jochenberger.gradlegeb

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.jetty.JettyRun
import org.gradle.api.tasks.testing.Test

import com.github.jochenberger.gradlegeb.GebPlugin;

class GebPlugin implements Plugin<Project>{

	def gebVersion = "0.9.0-RC-1"
	def seleniumVersion = "2.28.0"
	def spockVersion = "0.7-groovy-2.0"
	def groovyVersion = "2.0.5"

	def drivers = ["firefox", "chrome"]


	@Override
	public void apply(Project p) {
		p.apply plugin: 'jetty'
		p.apply plugin: 'groovy'

		p.dependencies.add("testCompile", "org.gebish:geb-spock:$gebVersion")
		p.dependencies.add("groovy", "org.codehaus.groovy:groovy-all:$groovyVersion")

		p.dependencies.add("testCompile", "org.spockframework:spock-core:$spockVersion")
		drivers.each {
			p.dependencies.add("testCompile", "org.seleniumhq.selenium:selenium-$it-driver:$seleniumVersion")
		}
		p.dependencies.add("testCompile", "org.seleniumhq.selenium:selenium-support:$seleniumVersion")


		p.task([type: JettyRun, description: "Starts a Jetty server", group: "Check"], "jettyTest")
		p.tasks.test.doFirst {
			//TODO check if gradle supports this out of the box
			File spockConfig = File.createTempFile("spockconfig", "groovy")
			spockConfig.text = GebPlugin.class.getResourceAsStream("SpockConfig.groovy").text
			systemProperty "spock.configuration", spockConfig.absolutePath
		}

		drivers.each { String d ->

			Task t = p.task([type: Test, description: "Run Selenium tests with $d", group: "Test"], "${d}Test").doFirst {
				//TODO check if gradle supports this out of the box
				File spockConfig = File.createTempFile("spockconfig", "groovy")
				spockConfig.text = GebPlugin.class.getResourceAsStream("SpockConfigUI.groovy").text
				systemProperty "spock.configuration", spockConfig.absolutePath
				systemProperty "geb.env", d
				systemProperty "geb.build.baseUrl", "http://localhost:${p.tasks.jettyTest.httpPort}/${p.tasks.jettyTest.contextPath}/"
			}

			t.doFirst {
				p.tasks.jettyTest.daemon = true;
				p.tasks.jettyTest.execute();
			}

			//t.inputs.files p.tasks.test.inputs.files
			//t.outputs.files p.tasks.test.outputs.files

			if (d == 'firefox'){
				p.tasks.test.dependsOn(t)
			}
		}
		// TODO shut-down jetty after tests

		//		p.tasks.test.doLast {
		//			p.tasks.jettyStop.execute()
		//		}
	}
}
