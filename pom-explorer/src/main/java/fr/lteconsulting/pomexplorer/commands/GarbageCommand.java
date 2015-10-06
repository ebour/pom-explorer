package fr.lteconsulting.pomexplorer.commands;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.lteconsulting.pomexplorer.GAV;
import fr.lteconsulting.pomexplorer.GavTools;
import fr.lteconsulting.pomexplorer.ILogger;
import fr.lteconsulting.pomexplorer.Project;
import fr.lteconsulting.pomexplorer.Tools;
import fr.lteconsulting.pomexplorer.WorkingSession;
import fr.lteconsulting.pomexplorer.graph.relation.BuildDependencyRelation;
import fr.lteconsulting.pomexplorer.javac.JavaSourceAnalyzer;

public class GarbageCommand
{
	@Help( "displays the list of dependencies declared but not used in the java code of a project and referenced transitive dependencies not declared in the pom file, arguments : gav_filter" )
	public void dependencies( WorkingSession session, ILogger log, CommandOptions options, FilteredGAVs gavFilter )
	{
		log.html( "<i>Note : although this tool will follow all the transitive dependencies inside your own projects, it will not recursively fetch all your externaly dependencies. For example, if you declare 'undertow-servlet' and depend only on 'undertow-core', you will get warnings that undetow class references have no provider found. This is a sign that you depend on a transitive dependency (from an external library) without declaring it in your maven project.</i><br/>" );

		for( GAV gav : gavFilter.getGavs( session ) )
		{
			Project project = session.projects().forGav( gav );
			if( project == null )
			{
				log.html( Tools.warningMessage( "No project for the gav " + gav + ", ignoring" ) );
				continue;
			}

			// get all dependencies of the gav
			Set<GAV> dependencies = new HashSet<>();
			session.graph().relationsRec(gav).stream().filter(r -> !(r instanceof BuildDependencyRelation))
					.map(r -> r.getTarget()).forEach(dependencies::add);
			Set<GAV> directDependencies = new HashSet<>();
			session.graph().dependencies( gav ).stream().map( dep -> dep.getTarget() ).forEach( directDependencies::add );

			log.html( "Considered project's dependencies:<br/>" );
			dependencies.stream().sorted( Tools.gavAlphabeticalComparator ).forEachOrdered( g -> log.html( g + "<br/>" ) );

			Map<GAV, Set<String>> providers = new HashMap<>();
			Map<String, Set<GAV>> fqnProviders = new HashMap<>();

			// and for each dependency, get the provided classes
			for( GAV provider : dependencies )
			{
				Set<String> providedClasses = new HashSet<>();
				List<String> cc = GavTools.analyseProvidedClasses( session, provider, log );
				if( cc != null )
					providedClasses.addAll( cc );

				log.html( providedClasses.size() + " provided classes, use -v option to display them<br/>" );
				if( options.getFlag( "v" ) )
					providedClasses.stream().forEach( c -> log.html( c + "<br/>" ) );
				providers.put( provider, providedClasses );
				for( String cls : providedClasses )
				{
					Set<GAV> p = fqnProviders.get( cls );
					if( p == null )
					{
						p = new HashSet<>();
						fqnProviders.put( cls, p );
					}
					p.add( provider );
				}
			}

			// own internal classes
			Set<String> ownClasses = new HashSet<>();
			List<String> ownClassesList = GavTools.analyseProvidedClasses( session, gav, log );
			if( ownClassesList != null )
				ownClasses.addAll( ownClassesList );

			// get the referenced fqns in the gav
			log.html( "<br/><b>Analyzing referenced fqns of the project '" + project.getPomFile().getAbsolutePath() + "'</b><br/>" );
			log.html( "Use the -v option to display the list of referenced fqns.<br/>" );
			JavaSourceAnalyzer analyzer = new JavaSourceAnalyzer();
			analyzer.analyzeProject( project, options.getFlag( "v" ), log );
			Set<String> fqnReferences = analyzer.getUsageExtractor().getQualifiedNames();

			Set<GAV> referencedTransitiveDependencies = new HashSet<>();

			Set<GAV> uselessGavs = new HashSet<>();
			providers.keySet().stream().filter( g -> !directDependencies.contains( g ) ).forEach( uselessGavs::add );

			Set<GAV> uselessDirectGavs = new HashSet<>( directDependencies );

			Set<String> noProviders = new HashSet<>();
			for( String referencedFqn : fqnReferences )
			{
				if( referencedFqn.startsWith( "java." ) || referencedFqn.startsWith( "javax." ) || ownClasses.contains( referencedFqn ) )
					continue;

				Set<GAV> referenceProviders = fqnProviders.get( referencedFqn );
				if( referenceProviders == null )
				{
					noProviders.add( referencedFqn );
					continue;
				}

				// if( referenceProviders.size() > 1 )
				// System.out.println( "[MULTIPROVIDER] fqn " + referencedFqn +
				// " is provided by those gavs : " + providers );

				for( GAV providerGav : referenceProviders )
				{
					if( !directDependencies.contains( providerGav ) )
					{
						referencedTransitiveDependencies.add( providerGav );
					}

					uselessGavs.remove( providerGav );
					uselessDirectGavs.remove( providerGav );

					// System.out.println( "[USEDBY] gav " + providerGav +
					// " provides class " + referencedFqn + " and is used by "
					// );
					// Map<String, List<String>> users =
					// app.usageExtractor.getFqnUsage();
					// if( users == null || users.get( referencedFqn ) == null )
					// {
					// System.out.println( "[ERROR] no users found !!" );
					// }
					// else
					// {
					// for( String user : users.get( referencedFqn ) )
					// System.out.println( " used by " + user );
					// }
				}
			}

			log.html( "<br/><br/>" );

			log.html( "Referenced FQNs without a provider (<i>may include false positives, or internally defined fqns</i>):<br/>" );
			log.html( noProviders.size() + " not provided references found<br/>" );
			noProviders.stream().sorted().forEachOrdered( fqn -> log.html( fqn + "<br/>" ) );
			log.html( "<br/>" );

			log.html( "GAV declared in project's hierarchy dependencies but not referenced in the project's sources (<i>may include false positives like imported or module poms</i>):<br/>" );
			log.html( uselessGavs.size() + " declared but not used GAVs<br/>" );
			uselessGavs.stream().sorted( Tools.gavAlphabeticalComparator ).forEachOrdered( g -> log.html( g + " (provides " + providers.get( g ).size() + " classes)<br/>" ) );
			log.html( "<br/>" );

			log.html( "Referenced FQNs from transitive dependencies :<br/>" );
			log.html( referencedTransitiveDependencies.size() + " referenced transitive dependencies GAV<br/>" );
			referencedTransitiveDependencies.stream().sorted().forEachOrdered( g -> log.html( g + "<br/>" ) );
			log.html( "<br/>" );

			log.html( "GAV declared directly in the project's dependencies but not referenced in the project's sources (<i>may include false positives like imported or module poms</i>):<br/>" );
			log.html( uselessDirectGavs.size() + " declared but not used GAVs<br/>" );
			uselessDirectGavs.stream().sorted( Tools.gavAlphabeticalComparator ).forEachOrdered( g -> log.html( g + " (provides " + providers.get( g ).size() + " classes)<br/>" ) );
			log.html( "<br/>" );
		}
	}
}
