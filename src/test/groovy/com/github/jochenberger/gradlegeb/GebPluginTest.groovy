package com.github.jochenberger.gradlegeb
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Assert;
import org.junit.Test;

class GebPluginTest {
	
	@Test
	public void gebPluginAddsJettyTestTaskToProject() {
		Project project = ProjectBuilder.builder().build()
		
		project.apply plugin: 'geb'
		Assert.assertNotNull(project.tasks.jettyTest)
	}
}