package ru.mihkopylov.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Comparator;
import java.util.Optional;
import lombok.NonNull;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.IOUtil;

@Mojo(name = "describe", defaultPhase = LifecyclePhase.COMPILE)
public class DescribeMojo extends AbstractMojo {
    //todo support custom template name
    private static final String TEMPLATE_NAME = "template.ftlh";
    @Parameter(defaultValue = "${project.build.outputDirectory}/META-INF/spring-configuration-metadata.json")
    private String jsonFileName;
    @Parameter(defaultValue = "${basedir}/CONFIGURATION.md")
    private String outputFileName;
    @Parameter(defaultValue = "false")
    private boolean failIfNoMetadataFileFound;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Optional<String> jsonStringOptional = readFile();
        if (!jsonStringOptional.isPresent()) {
            getLog().debug( "Terminating" );
            return;
        }
        Metadata metadata = parseMetadata( jsonStringOptional.get() );
        sortMetadataProperties( metadata );
        String markdownString = generateMarkdown( metadata );
        String unEscapedMarkdownString = unEscapeHtmlTags( markdownString );
        writeMarkdownToFile( unEscapedMarkdownString );
    }

    @NonNull
    private String unEscapeHtmlTags( @NonNull String string ) {
        getLog().debug( "Unescaping HTML tags" );
        String result = string.replaceAll( "&lt;", "<" ).replaceAll( "&gt;", ">" ).replaceAll( "&amp;", "&" );
        getLog().debug( "Unescaped HTML tags:\n" + result );
        return result;
    }

    /**
     * Properties should go in a constant order to have generated file unchanged if properties have not changed
     */
    private void sortMetadataProperties( @NonNull Metadata metadata ) {
        getLog().debug( "Sorting properties" );
        metadata.getProperties().sort( Comparator.comparing( Metadata.Property :: getName ) );
    }

    private void writeMarkdownToFile( @NonNull String markdownString ) throws MojoExecutionException {
        File outputFile = new File( outputFileName );
        getLog().debug( "Writing description to file " + outputFile.getAbsolutePath() );
        try (BufferedWriter writer = new BufferedWriter( new FileWriter( outputFile ) )) {
            writer.write( markdownString );
            getLog().debug( "Description is written to file " + outputFile.getAbsolutePath() );
        } catch (IOException e) {
            throw new MojoExecutionException( "Can't write result to file", e );
        }
    }

    @NonNull
    private String generateMarkdown( @NonNull Metadata metadata ) throws MojoExecutionException, MojoFailureException {
        getLog().debug( "Generating description from metadata" );
        try {
            Configuration configuration = new Configuration( Configuration.VERSION_2_3_29 );
            configuration.setDefaultEncoding( "UTF-8" );
            configuration.setClassForTemplateLoading( getClass(), "/templates" );
            configuration.setTemplateExceptionHandler( TemplateExceptionHandler.RETHROW_HANDLER );
            configuration.setLogTemplateExceptions( false );
            configuration.setWrapUncheckedExceptions( true );
            Template template = configuration.getTemplate( TEMPLATE_NAME );
            try (Writer writer = new StringWriter()) {
                template.process( ImmutableMap.of( "metadata", metadata ), writer );
                String result = writer.toString();
                getLog().debug( "Description generated:\n" + result );
                return result;
            }
        } catch (TemplateException e) {
            throw new MojoFailureException( "Bad Template", e );
        } catch (IOException e) {
            throw new MojoExecutionException( "Can't generate result", e );
        }
    }

    @NonNull
    private Metadata parseMetadata( @NonNull String jsonString ) throws MojoExecutionException {
        try {
            getLog().debug( "Parsing string to DTO" );
            Metadata metadata = new ObjectMapper().readValue( jsonString, Metadata.class );
            getLog().debug( "Parsed:\n" + metadata.toString() );
            return metadata;
        } catch (IOException e) {
            throw new MojoExecutionException( "Can't read JSON from file", e );
        }
    }

    @NonNull
    private Optional<String> readFile() throws MojoFailureException, MojoExecutionException {
        try {
            File jsonFile = new File( jsonFileName );
            getLog().debug( "Searching file " + jsonFile.getAbsolutePath() );
            if (!jsonFile.exists()) {
                getLog().debug( "File " + jsonFile.getAbsolutePath() + " not found" );
                if (failIfNoMetadataFileFound) {
                    throw new MojoFailureException(
                            String.format( "File '%s' is not found", jsonFile.getAbsolutePath() ) );
                }
                return Optional.empty();
            }
            getLog().debug( "File " + jsonFile.getAbsolutePath() + " found" );
            try (InputStream is = new FileInputStream( jsonFileName )) {
                getLog().debug( "Reading file " + jsonFile.getAbsolutePath() );
                String result = IOUtil.toString( is );
                getLog().debug( "File content:\n" + result );
                return Optional.of( result );
            }
        } catch (IOException e) {
            throw new MojoExecutionException( String.format( "Can't read file '%s'", jsonFileName ), e );
        }
    }
}
