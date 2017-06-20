package edu.duke.cs.tpie;

import org.jerkar.api.depmanagement.JkDependencies;
import org.jerkar.api.depmanagement.JkModuleId;
import org.jerkar.api.depmanagement.JkVersion;
import org.jerkar.api.file.JkFileTreeSet;
import org.jerkar.api.java.JkJavaCompiler;
import org.jerkar.tool.builtins.eclipse.JkBuildPluginEclipse;
import org.jerkar.tool.builtins.javabuild.JkJavaBuild;
import org.jerkar.tool.builtins.javabuild.JkJavaPacker;


public class Build extends JkJavaBuild {
	
	public Build() {
		
		// don't run unit tests in the build system
		// it takes forever
		tests.skip = true;
		
		pack.javadoc = true;
		
		// tell the eclipse plugin to stop using classpath vars
		JkBuildPluginEclipse eclipse = new JkBuildPluginEclipse();
		eclipse.useAbsolutePathsInClasspath = true;
		plugins.configure(eclipse);
	}

	@Override
	public JkModuleId moduleId() {
		return JkModuleId.of("edu.duke.cs", "tpie");
	}
	
	@Override
	public JkVersion version() {
		return JkVersion.name("1.1");
	}

	@Override
	public String javaSourceVersion() {
		return JkJavaCompiler.V8;
	}
	
	@Override
	public JkDependencies dependencies() {
		return JkDependencies.builder()
			
			// test dependencies
			.on("org.hamcrest:hamcrest-all:1.3").scope(TEST)
			.on("junit:junit:4.12").scope(TEST)
			
			// runtime dependencies
			.on("commons-io:commons-io:2.5")
			.on("org.apache.commons:commons-collections4:4.1")
			
			.build();
	}
	
	@Override
	public JkFileTreeSet editedSources() {
		return JkFileTreeSet.of(file("src/java"));
	}
	
	@Override
	public JkFileTreeSet editedResources() {
		return JkFileTreeSet.of(file("resources"));
	}
	
	@Override
	public JkFileTreeSet unitTestEditedSources() {
		return JkFileTreeSet.of(file("test"));
	}
	
	@Override
	protected JkJavaPacker createPacker() {
		// TODO: find out how to exclude tpie-java.so from the sources jar
		// TODO: find out how to get the javadoc jar to have the verison name
		// which means these things need to be done manually before publishing a release
		return JkJavaPacker.builder(this)
			.includeVersion(true)
			.doJar(true)
			.doSources(true)
			.extraFilesInJar(JkFileTreeSet.of(
				baseDir().include("LICENSE.md"),
				baseDir().include("README.md")
			))
			.build();
	}
}
