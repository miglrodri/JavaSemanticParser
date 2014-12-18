import java.util.ArrayList;
import java.util.Arrays;

public class SemanticParser {

	public static void main(String[] args) {
		System.out.println(semanticSearch("keit country 1990"));
	}

	private static String semanticSearch(String query) {

		System.out.println("semanticSearch(String " + query + ")");

		String SPARQL_QUERY = "";
		String select = " SELECT ?s ";
		String where = " WHERE { ";
		String filter = " FILTER (";
		String order = " ORDER BY ";
		String nothing_filter = "";

		String[] ontology_classes = { "artist", "artists", "album", "albums",
				"track", "tracks" };
		String[] ontology_properties = { "decade", "decades", "genre" };
		ArrayList<String> ontology_genres = new ArrayList<String>(
				Arrays.asList("pop", "rock", "country", "rap"));

		String active_class = "";
		boolean isClass;
		boolean isProperty;
		boolean isDecade;
		boolean firstNothing = true;
		boolean firstFilter = true;
		int index = 0;

		/**
		 * QUERY SPARQL
		 * example:
		 * PEARL	JAM		ALBUM	ROCK		1992
		 * nothing	nothing	class	property	property
		 * 							(genre)		(decade)
		 */
		
		
		/**
		 * For each token
		 */
		for (String token : query.split(" ")) {
			System.out.println(token);
			isClass = false;
			isProperty = false;
			isDecade = false;

			/**
			 * Verify if its a class
			 */
			if (token.equals("artist") || token.equals("artists")) {
				System.out.println(token + " is class");
				active_class = "artist";
				isClass = true;
				where += " ?s rdf:type music:Artist. ";
				where += " ?s music:hasName ?value. ";
				select += " ?value ";
			} else {
				// outra classe
			}

			/**
			 * Verify if its a property
			 */
			if (ontology_genres.contains(token)) {
				System.out.println(token + " is property");
				isProperty = true;
				where += " ?s music:hasMainGenre ?genre" + index + ". ";
				if (!firstFilter) {
					filter += " && ";
				}
				filter += " regex( ?genre" + index + ", \"" + token
						+ "\", \"i\" ) ";
				firstFilter = false;
			}

			/**
			 * Verify if its a value
			 */
			// verify if its a DECADE
			if (isDecade(token)) {
				isDecade = true;
				System.out.println(token + " is decade");
			}
			// verify if its a NOTHING
			else if (isClass == false && isProperty == false) {
				System.out.println(token + " is nothing");
				if (firstNothing) {
					nothing_filter = token;
				} else {
					nothing_filter += " " + token;
				}
				firstNothing = false;
			}

			index++;
		}
		
		if (!nothing_filter.isEmpty()) {
			if (active_class.equals("artist") || active_class.equals("album")
					|| active_class.equals("track")) {
				if (!firstFilter) {
					filter += " && ";
				}
				filter += " regex( ?value, \"" + nothing_filter
						+ "\", \"i\" ) ";
				firstFilter = false;
			} else {
				// tem que procurar em todas as classes

				// procura no artista
				where += " ?s rdf:type music:Artist. ";
				where += " ?s music:hasName ?value. ";
				select += " ?value ";

				if (!firstFilter) {
					filter += " && ";
				}
				filter += " regex( ?value, \"" + nothing_filter
						+ "\", \"i\" ) ";
				firstFilter = false;
			}
		}

		SPARQL_QUERY = select + where + filter + " ) " + " } ";

		return SPARQL_QUERY;

		// searchQuery =
		// "SELECT ?id ?name WHERE { ?id rdf:type music:Artist. ?id music:hasName ?name. FILTER regex( ?name, \""
		// + query + "\", \"i\" )} ORDER BY ?name";
		//
		// System.out
		// .println("private static JavaBean allClassesSearch(String query) { :: \""
		// + query + "\"");
		//
		// /**
		// * Gather information about artists
		// */
		// if (option.equals("all") || option.equals("artists")) {
		// searchQuery =
		// "SELECT ?id ?name WHERE { ?id rdf:type music:Artist. ?id music:hasName ?name. FILTER regex( ?name, \""
		// + query + "\", \"i\" )} ORDER BY ?name";
		//
		// qe = queryDB(searchQuery);
		// results = qe.execSelect();
		// temp = 0;
		//
		// while (results.hasNext()) {
		// QuerySolution binding = results.nextSolution();
		// String temp_artist_id = binding.get("id").toString();
		// String temp_artist_name = binding.get("name").toString();
		//
		// SemanticResult sresult = new SemanticResult();
		// sresult.setResource_name("a" + cleanLiteral(temp_artist_name));
		// sresult.setResource_url("MusicController?artistid="
		// + cleanId(temp_artist_id));
		// temp++; // incremente o contador do nr de resultados
		// // encontrados
		// mybean.addToSemanticArray(sresult);
		// }
		// }
		//
		// /**
		// * Gather information about albums
		// */
		// if (option.equals("all") || option.equals("albums")) {
		// searchQuery =
		// "SELECT ?id ?name WHERE { ?id rdf:type music:Album. ?id music:hasTitle ?name. FILTER regex( ?name, \""
		// + query + "\", \"i\" )} ORDER BY ?name";
		//
		// qe = queryDB(searchQuery);
		// results = qe.execSelect();
		// temp = 0;
		//
		// while (results.hasNext()) {
		// QuerySolution binding = results.nextSolution();
		// String temp_album_id = binding.get("id").toString();
		// String temp_album_name = binding.get("name").toString();
		//
		// SemanticResult sresult = new SemanticResult();
		// sresult.setResource_name("b" + cleanLiteral(temp_album_name));
		// sresult.setResource_url("MusicController?albumid="
		// + cleanId(temp_album_id));
		// temp++; // incremente o contador do nr de resultados
		// // encontrados
		// mybean.addToSemanticArray(sresult);
		// }
		// }
		//
		// /**
		// * Gather information about tracks
		// */
		// if (option.equals("all") || option.equals("tracks")) {
		// searchQuery =
		// "SELECT ?id ?name WHERE { ?id rdf:type music:Track. ?id music:hasTitle ?name. FILTER regex( ?name, \""
		// + query + "\", \"i\" )} ORDER BY ?name";
		//
		// qe = queryDB(searchQuery);
		// results = qe.execSelect();
		// temp = 0;
		//
		// while (results.hasNext()) {
		// QuerySolution binding = results.nextSolution();
		// String temp_track_id = binding.get("id").toString();
		// String temp_track_name = binding.get("name").toString();
		//
		// SemanticResult sresult = new SemanticResult();
		// sresult.setResource_name("c" + cleanLiteral(temp_track_name));
		// sresult.setResource_url("MusicController?trackid="
		// + cleanId(temp_track_id));
		// temp++; // incremente o contador do nr de resultados
		// // encontrados
		// mybean.addToSemanticArray(sresult);
		// }
		// }
		//
		// mybean.setPageType("semantic_search_page");
		// mybean.setOption(query);
		// mybean.setNumberItems(temp);
		//
		// qe.close();
		//
		// return mybean;

	}
	
	private static boolean isDecade(String token) {
		int numero;
		try {
			numero = Integer.parseInt(token);
		} catch (NumberFormatException e) {
			//e.printStackTrace();
			return false;
		}
		if (numero < 1900 || numero > 2020) {
			return false;
		}
		return true;
	}
}
