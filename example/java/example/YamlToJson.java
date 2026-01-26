// Run with: mvn compile exec:java -Dexec.mainClass=example.YamlToJson -Dexec.args="input.yaml"

package example;

import com.yaml.YAMLStar;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.nio.file.Files;
import java.nio.file.Paths;

public class YamlToJson {
    public static void main(String[] args) throws Exception {
        String yamlFile = args.length > 0 ? args[0] : "../sample.yaml";
        System.out.println("YAMLStar Example - Loading " + yamlFile + " and outputting JSON\n");

        // Read the YAML file
        String yamlContent = Files.readString(Paths.get(yamlFile));

        System.out.println("Input YAML:");
        System.out.println(yamlContent);
        System.out.println("\n---\n");

        // Parse YAML to Java objects
        Object data = YAMLStar.load(yamlContent);

        // Convert to JSON
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonOutput = gson.toJson(data);

        System.out.println("Output JSON:");
        System.out.println(jsonOutput);
    }
}
