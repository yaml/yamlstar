import org.yamlstar.YAMLStar;
import java.util.Map;
import java.util.List;

public class Example {
    public static void main(String[] args) {
        // Test 1: Load a simple mapping
        System.out.println("Test 1: Simple mapping");
        Map<String, Object> config = (Map<String, Object>) YAMLStar.load("key: value");
        System.out.println("  key = " + config.get("key"));
        System.out.println();

        // Test 2: Load a sequence
        System.out.println("Test 2: Simple sequence");
        List<Object> items = (List<Object>) YAMLStar.load("- a\n- b\n- c");
        System.out.println("  items[0] = " + items.get(0));
        System.out.println("  items[1] = " + items.get(1));
        System.out.println("  items[2] = " + items.get(2));
        System.out.println();

        // Test 3: Load nested structure
        System.out.println("Test 3: Nested structure");
        String yaml = "database:\n  host: localhost\n  port: 5432";
        Map<String, Object> data = (Map<String, Object>) YAMLStar.load(yaml);
        Map<String, Object> db = (Map<String, Object>) data.get("database");
        System.out.println("  host = " + db.get("host"));
        System.out.println("  port = " + db.get("port"));
        System.out.println();

        // Test 4: Load multiple documents
        System.out.println("Test 4: Multiple documents");
        List<Object> docs = YAMLStar.loadAll("---\ndoc1\n---\ndoc2\n---\ndoc3");
        System.out.println("  Number of documents: " + docs.size());
        for (int i = 0; i < docs.size(); i++) {
            System.out.println("  doc[" + i + "] = " + docs.get(i));
        }
        System.out.println();

        // Test 5: Version
        System.out.println("Test 5: Version");
        System.out.println("  YAMLStar version: " + YAMLStar.version());
        System.out.println();

        System.out.println("All tests passed!");
    }
}
