import java.util.ArrayList;
import java.util.Arrays;

public class SemanticParser {

	public static void main(String[] args) {

		// System.out.println(semanticSearch("country artist"));
		// System.out.println(semanticSearch("country artist 1990"));
		// System.out.println(semanticSearch("dre tracks"));
		// System.out.println(semanticSearch("reggae rap"));
		// System.out.println(semanticSearch("pop tracks"));
		// System.out.println(semanticSearch("pop albums"));
		// System.out.println(semanticSearch("1990"));
		// System.out.println(semanticSearch("1990 tracks"));
		// System.out.println(semanticSearch("keit country artist"));
		// System.out.println(semanticSearch("keit artist country"));
		// System.out.println(semanticSearch("artist country keit"));
		// System.out.println(semanticSearch("albums rap res"));
		// System.out.println(semanticSearch("pop rock"));
		// System.out.println(semanticSearch("black"));
		// System.out.println(semanticSearch("black artist"));
		// System.out.println(semanticSearch("album black"));
		// System.out.println(semanticSearch("black track"));
		// System.out.println(semanticSearch("1990"));
		// System.out.println(semanticSearch("1990 2000"));
		// System.out.println(semanticSearch("pop"));
		// System.out.println(semanticSearch("pearl jam tracks"));
		// System.out.println(semanticSearch("country artist 1990"));
		// System.out.println(semanticSearch("\"paris hilton\""));

		String query = "rock \"black track\" asd";

		int firstIndex = 0;
		int lastIndex = 0;
		String exactSearch = "";
		boolean error = false;

		try {
			firstIndex = query.indexOf("\"", 0);
			lastIndex = query.indexOf("\"", firstIndex + 1);
			exactSearch = query.substring(firstIndex + 1, lastIndex);
		} catch (Exception e) {
			// e.printStackTrace();
			error = true;
			exactSearch = "";
		}

		System.out.println("start= " + firstIndex + " | last= " + lastIndex);
		System.out.println("exact= " + exactSearch);

		if (!error) {
			query = query.replaceFirst("\"" + exactSearch + "\"", "");
		}

		System.out.println("query= " + query);
		
	}

	private static String semanticSearch(String query) {

		System.out.println("\n\n\n\n########semanticSearch(String " + query
				+ ")");

		/**
		 * TODO Implementar caso queiramos procurar sintáticamente:
		 * "keit country" -> interpretar tudo dentro das aspas como uma string
		 * TODO
		 */

		String SPARQL_QUERY = "";
		String select = " SELECT ?s ";
		String where = " WHERE { ";
		String filter = " FILTER (";
		String order = " ORDER BY ";
		String nothing_filter = "";

		// String[] ontology_classes = { "artist", "artists", "album", "albums",
		// "track", "tracks" };
		// String[] ontology_properties = { "decade", "decades", "genre" };
		ArrayList<String> ontology_genres = new ArrayList<String>(
				Arrays.asList("pop", "rock", "country", "rap"));

		// TODO Criar arraylist com todos os tipos de géneros automaticamente.
		String QUERY_TO_GET_ALL_TYPE_OF_GENRES = " SELECT DISTINCT   ?value  WHERE {  ?s music:hasMainGenre ?value   } ";

		// TODO Criar arraylist com todos os datatypeProperties que temos.
		String QUERY_TO_GET_ALL_PROPERTIES = "SELECT DISTINCT ?s WHERE {  ?s rdf:type owl:DatatypeProperty   } ";

		String active_class = "";
		boolean isClass;
		boolean isProperty;
		boolean isDecade = false;
		boolean isGenre = false;
		boolean isNothing = false;
		boolean firstNothing = true;
		boolean firstFilter = true;
		int genre_index = 0;

		/**
		 * QUERY SPARQL example: PEARL JAM ALBUM ROCK 1992 nothing nothing class
		 * property property (genre) (decade)
		 */

		/**
		 * For each token
		 */
		for (String token : query.split(" ")) {
			System.out.println(token);
			isClass = false;
			isProperty = false;
			isDecade = false;
			isNothing = false;

			/**
			 * Verify if its a class
			 */
			if (token.equals("artist") || token.equals("artists")) {
				System.out.println(token + " is class music:Artist");
				active_class = "artist";
				isClass = true;
				where += " ?s rdf:type music:Artist. ";
				where += " ?s music:hasName ?value. ";
				select += " ?value ";

			} else if (token.equals("album") || token.equals("albums")) {
				System.out.println(token + " is class music:Album");
				active_class = "album";
				isClass = true;
				where += " ?s rdf:type music:Album. ";
				where += " ?s music:hasTitle ?value. ";
				select += " ?value ";

			} else if (token.equals("track") || token.equals("tracks")) {
				System.out.println(token + " is class music:Track");
				active_class = "track";
				isClass = true;
				where += " ?s rdf:type music:Track. ";
				where += " ?s music:hasTitle ?value. ";
				select += " ?value ";
			}

			/**
			 * Verify if its a property
			 */
			// verify if its a genre
			else if (ontology_genres.contains(token)) {
				System.out.println(token + " is property(genre)");
				isGenre = true;
				isProperty = true;
				where += " ?s music:hasMainGenre ?genre" + genre_index + ". ";
				if (!firstFilter) {
					filter += " && ";
				}
				filter += " regex( ?genre" + genre_index + ", \"" + token
						+ "\", \"i\" ) ";
				firstFilter = false;
				genre_index++;
			}
			// verify if its a DECADE
			else if (isDecadeQuestion(token)) {
				System.out.println(token + " is property(decade)");
				isDecade = true;
				isProperty = true;
				where += " ?s music:hasDecade ?decade. ";
				if (!firstFilter) {
					filter += " && ";
				}
				filter += " regex( ?decade, \"" + token + "\", \"i\" ) ";
				firstFilter = false;
			}

			/**
			 * Verify if its a value
			 */
			// verify if its a NOTHING
			else if (isClass == false && isProperty == false) {
				System.out.println(token + " is nothing(value)");
				if (firstNothing) {
					nothing_filter = token;
				} else {
					nothing_filter += " " + token;
				}
				firstNothing = false;
			}

		}

		// verify if its a genre and a class to know the best way to construct
		// sparql query
		if (isGenre && active_class.equals("artist")) {
			// do nothing for now!

		} else if (isGenre && active_class.equals("album")) {
			where = " WHERE { ";
			for (int i = 0; i < genre_index; i++) {
				where += " ?other music:hasMainGenre ?genre" + i + ". ";
			}
			where += " ?other rdf:type music:Artist. ";
			where += " ?other music:producesAlbum ?s. ";
			where += " ?s music:hasTitle ?value. ";
			// String cenas =
			// "SELECT ?s1 ?value   WHERE {  ?s music:hasMainGenre ?genre0.  ?s rdf:type music:Artist. "
			// + "?s music:producesAlbum ?s1.  ?s1 music:hasTitle ?value.   "
			// + "FILTER ( regex( ?genre0, \"rock\", \"i\" )  )  }";

		} else if (isGenre && active_class.equals("track")) {
			where = " WHERE { ";
			for (int i = 0; i < genre_index; i++) {
				where += " ?other music:hasMainGenre ?genre" + i + ". ";
			}
			where += " ?other rdf:type music:Artist. ";
			where += " ?other music:writesTrack ?s. ";
			where += " ?s music:hasTitle ?value. ";
			// String cenas =
			// "SELECT ?s1 ?value   WHERE {  ?s music:hasMainGenre ?genre0.  ?s rdf:type music:Artist. "
			// + "?s music:producesAlbum ?s1.  ?s1 music:hasTitle ?value.   "
			// + "FILTER ( regex( ?genre0, \"rock\", \"i\" )  )  }";
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

		// nothing string is empty
		else {
			// no class associated and one or various genres associated
			if (active_class.equals("") && isGenre) {
				// procura no artista
				where += " ?s rdf:type music:Artist. ";
				where += " ?s music:hasName ?value. ";
				select += " ?value ";
			} else if (active_class.equals("") && isDecade) {
				// procura no artista
				where += " ?s rdf:type music:Artist. ";
				where += " ?s music:hasName ?value. ";
				select += " ?value ";
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

	private static boolean isDecadeQuestion(String token) {
		int numero;
		try {
			numero = Integer.parseInt(token);
		} catch (NumberFormatException e) {
			// e.printStackTrace();
			return false;
		}
		if (numero < 1900 || numero > 2020) {
			return false;
		}
		return true;
	}
}
