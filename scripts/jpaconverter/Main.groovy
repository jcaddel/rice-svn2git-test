/**
 * Copyright 2005-2013 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl2.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import japa.parser.JavaParser
import japa.parser.ast.CompilationUnit

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.kuali.rice.devtools.jpa.eclipselink.conv.ojb.OjbUtil
import org.kuali.rice.devtools.jpa.eclipselink.conv.parser.visitor.EntityVisitor

import groovy.grape.Grape;
import groovy.util.ConfigSlurper

//Grape.grab(group:"org.kuali.rice",module:"rice-development-tools",version:"2.4.0-M3-SNAPSHOT")

//@GrabConfig(systemClassLoader=true)
@Grapes([
	@Grab(group="org.kuali.rice",module="rice-development-tools",version="2.4.0-M3-SNAPSHOT")
	])
@GrabExclude('commons-transaction:commons-transaction')
@GrabExclude('commons-beanutils:commons-beanutils')


/*
@Grapes([
    @Grab(
      group='commons-io',
      module='commons-io',
      version='2.1'
    ),
    @Grab(
      group='com.google.code.javaparser',
      module='javaparser',
      version='1.0.9'
    ),
    @Grab(
      group='org.kuali.db.ojb',
      module='db-ojb',
      version='1.0.4-patch8'
    ),
    @Grab(
        group='commons-lang',
        module='commons-lang',
        version='2.6'
    ),
    @Grab(
        group='log4j',
        module='log4j',
        version='2.1.17'
    )
    ]
  )
*/
def cli = new CliBuilder()
cli.h( longOpt: 'help', required: false, 'show usage information' )
cli.c( longOpt: 'config', required: true, argName:"config file", args:1, 'Location of groovy configuration file' )
cli.b( longOpt: 'base', required: true, argName:"base directory", args:1, 'Absolute path to the base directory for the conversion.' )
cli.n( longOpt: 'dryrun', required: false, 'If set, the script will dump the resulting java files to the console instead of updating the existing files.' )
cli.e( longOpt: 'errorsonly', required: false, 'If set, the script will only report any errors or warnings it will encounter and *not* update any files.' )
cli._( longOpt: 'replace', required: false, 'Replace all existing JPA annotations on classes referenced by OJB files.')

def opt = cli.parse(args)
if (!opt) {
    return
}
if (opt.h) {
    cli.usage();
    return
}
println ""
File baseDirectory = new File(opt.b)
println "Using Base Directory $baseDirectory.canonicalPath"

if ( !baseDirectory.exists() || !baseDirectory.isDirectory() ) {
    println "ERROR: Specified base directory does not exist or is not a directory - aborting."
    return
}

File configFile = new File(opt.c)
println "Loading configuration file: $configFile.canonicalPath"
if ( !configFile.exists() ) {
    println "ERROR: Configuration file does not exist - aborting."
    return
}
c = new ConfigSlurper().parse( configFile.text )

if ( opt.n ) {
    c.project.dryrun = true
}

if ( c.project.dryrun ) {
    println "**************************************************"
    println "Project in Dry Run Mode - no files will be updated"
    println "**************************************************"
}

if ( opt.e ) {
    c.project.errorsonly = true
    c.project.dryrun = true
}

if ( c.project.errorsonly ) {
    println "******************************************************"
    println "Project in Errors Only Mode - no files will be updated"
    println "******************************************************"
}

if ( opt.replace ) {
    c.project.replaceExistingAnnotations = true
}

if ( c.project.replaceExistingAnnotations ) {
    println "**************************************************"
    println "Configured to replace all existing JPA annotations"
    println "**************************************************"
}

//println c
//BasicConfigurator.configure()

/*
 * NOTE: When running from within Eclipse/IDE, you need to add groovy to the --classpath of the script being run so that
 * Groovy can install its RootLoader classloader - needed for the URL additions below.
 * 
 * In Eclipse (and Groovy 2.1.5), this will be: ${groovy_home}/lib/groovy-all-2.1.5.jar
 */
def rootLoader = this.class.classLoader.getRootLoader()
if ( !rootLoader ) {
    println "ERROR!  RootLoader is null - unable to add additional classes to the classpath.  This happens when running Groovy from within Eclipse.  Process will fail unless it is able to access the RootLoader."
    return
}
println "RootLoader: $rootLoader"

// add additional items to the classpath
for ( classpathDir in c.project.classpathDirectories ) {
    // NOTE: That trailing slash is absolutely required.  Otherwise it just adds the directory to the path as if it's a file and does not scan inside
    def classpathUrl = new URL("file://"+new File( baseDirectory, classpathDir ).canonicalPath + "/")
    //println "Adding Classpath URL: $classpathUrl"
    rootLoader.addURL( classpathUrl )
}

for ( jarPath in c.project.classpathJarDirectories ) {
    File jarDir = new File( baseDirectory, jarPath )
    println "Scanning $jarDir.canonicalPath for JAR files."
    def jars   = jarDir.listFiles().findAll { it.name.endsWith('.jar') } 
    jars.each { 
        println "Adding Classpath URL: ${it.toURI().toURL()}"
        rootLoader.addURL(it.toURI().toURL()) 
    }
}

println "\n**************************************************"
println "\n\nRootLoader Path URLs:\n${rootLoader.getURLs().join( '\n' )}"
println "**************************************************"

fullSourcePaths = c.project.sourceDirectories.collect { new File( baseDirectory, it ).canonicalPath }
fullOjbPaths = c.ojb.repositoryFiles.collect { new File( baseDirectory, it ).canonicalPath }

println "\n**************************************************"
println "\n\nSource Directories: \n${fullSourcePaths.join( '\n' )}"
println "**************************************************"

fullSourcePaths.each {
    if ( !new File( it ).exists() ) {
        println "ERROR: $it does not exist.  Aborting."
        return
    }
}

println "\n**************************************************"
println "\n\nOJB Files: \n${fullOjbPaths.join( '\n' )}"
println "**************************************************"

fullOjbPaths.each {
    if ( !new File( it ).exists() ) {
        println "ERROR: $it does not exist.  Aborting."
        return
    }
}


def drs = OjbUtil.getDescriptorRepositories( fullOjbPaths );
def ojbMappedClasses = OjbUtil.mappedClasses(drs);

//println "\n\nOJB Mapped Classes: \n${ojbMappedClasses.join( '\n' )}"
println "\n\n"

def mappedJavaFiles = convertClassesToJavaFiles(ojbMappedClasses)

println "\n**************************************************"
println "\n\nJava Files: \n${mappedJavaFiles.values().join( '\n' )}"
println "**************************************************"

def void processJavaFile( File ojbMappedFile, String subclassName ) {
    println "Processing File: $ojbMappedFile"
    if ( ojbMappedFile == null ) return;
    
    def unit = JavaParser.parse(ojbMappedFile)
    entityVisitor.visit(unit, subclassName)

    if ( c.project.dryRun ) {
        if ( !c.project.errorsonly ) {
            println unit.toString()
        }
    } else {
        ojbMappedFile.delete()
        ojbMappedFile << unit.toString()
    }
}

def File convertClassToFile( String className ) {
    println "Looking for source file for $className"
    for ( String dir in fullSourcePaths ) {
        def fileNameWithinSource = className.replace('.', '/') + ".java"
        def javaFile = new File( dir + "/" + fileNameWithinSource)
        //println "Looking for: $javaFile.canonicalPath"
        if ( javaFile.exists() ) {
            println "Found: $javaFile.canonicalPath"
            return javaFile
        }
    }
    println "No source file found.  Ignoring."
    return null
}

def Map convertClassesToJavaFiles(Collection mappedClasses) {
    def javaFiles = [:]
    for ( className in mappedClasses ) {
        def javaFile = convertClassToFile(className)
        if ( javaFile != null ) {
            javaFiles[className] = javaFile
        }
    }
    return javaFiles
}

println "\n\n************************************************************"
println "*** Starting Conversion"
println "************************************************************\n\n"

entityVisitor = new EntityVisitor(drs, c.ojb.converterMappings, c.project.replaceExistingAnnotations, c.project.upperCaseDbArtifactNames)

//if ( c.project.errorsonly ) {
//    Logger.getLogger( "org.kuali.rice.devtools" ).setLevel(Level.WARN)
//}

for (String className : mappedJavaFiles.keySet()) {
    File ojbMappedFile = mappedJavaFiles[className]
    processJavaFile(ojbMappedFile, className)
    
    Collection<String> superClasses = OjbUtil.getSuperClasses(className, "org.kuali.rice");
    for (String superClass : superClasses) {
        processJavaFile(convertClassToFile(superClass), className)
    }

}