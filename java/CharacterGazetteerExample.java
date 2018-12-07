import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import sk.sav.ui.ikt.nlp.gazetteer.character.CharacterGazetteer;
import sk.sav.ui.ikt.nlp.gazetteer.character.CharacterGazetteer.Representation;

public class CharacterGazetteerExample {

	public static void main(String args[]) throws IOException {

		CharacterGazetteer gazetteer = new CharacterGazetteer(
				Representation.CHILDSIBLING, true);

		HashMap<Integer, String> classMap = new HashMap<Integer, String>();

		classMap.put(1, "PER");
		gazetteer.insert("Evgeni Nabokov", 1);
		gazetteer.insert("Anders Nilsson", 1);

		classMap.put(2, "ORG");
		gazetteer.insert("New York Islanders", 2);
		gazetteer.insert("Islanders", 2);
		gazetteer.insert("Bridgeport Sound Tigers", 2);
		gazetteer.insert("Sound Tigers", 2);

		classMap.put(3, "LOC");
		gazetteer.insert("New York", 3);
		gazetteer.insert("Bridgeport", 3);

		classMap.put(4, "MISC");
		gazetteer.insert("American Hockey League", 4);

		String text = "The New York Islanders have placed goaltender Evgeni Nabokov on injured reserve and called up Anders Nilsson from the Bridgeport Sound Tigers of the American Hockey League.";

		// perform search within a stream
		// List<int[]> matches = gazetteer.find(new ByteArrayInputStream(text
		// .getBytes()));

		// perform search within a string
		List<int[]> matches = gazetteer.find(text);

		for (int[] match : matches) {
			String ids = "[";
			for (int i = 2; i < match.length; i++) {
				ids += classMap.get(match[i]);
			}
			ids += "]";
			System.out.println(String.format("[%02d,%02d] %s -> %s", match[0],
					match[1], text.substring(match[0], match[1]), ids));
		}
	}
}
