include ../common/init.mk
include $M/lein.mk
include $(COMMON)/common.mk
include $(COMMON)/binding.mk

VERSION := 0.1.3
GROUP_PATH := com/yaml
ARTIFACT := yamlstar
BUNDLE_DIR := target/bundle/$(GROUP_PATH)/$(ARTIFACT)/$(VERSION)

MAKES-CLEAN := \
  target/ \
  pom.xml \
  pom.xml.asc \
  .lein-failures \


test install:: $(LEIN)
	lein $@

# Build all artifacts needed for Maven Central
jar:: $(LEIN)
	lein jar

pom:: $(LEIN)
	lein pom

sources-jar:: $(LEIN)
	cd .. && jar cf java/target/$(ARTIFACT)-$(VERSION)-sources.jar \
	  -C core/src . \
	  -C java/src .

javadoc-jar::
	mkdir -p target/javadoc
	echo "Clojure source - see sources JAR" > target/javadoc/README.txt
	jar cf target/$(ARTIFACT)-$(VERSION)-javadoc.jar -C target/javadoc .

# Create the bundle for Central Portal upload
bundle:: jar pom sources-jar javadoc-jar
	rm -rf target/bundle target/bundle.zip
	mkdir -p $(BUNDLE_DIR)
	# Copy artifacts
	cp target/$(ARTIFACT)-$(VERSION).jar $(BUNDLE_DIR)/
	cp pom.xml $(BUNDLE_DIR)/$(ARTIFACT)-$(VERSION).pom
	cp target/$(ARTIFACT)-$(VERSION)-sources.jar $(BUNDLE_DIR)/
	cp target/$(ARTIFACT)-$(VERSION)-javadoc.jar $(BUNDLE_DIR)/
	# Sign all artifacts (jar, pom, sources, javadoc)
	@for f in $(BUNDLE_DIR)/*.jar $(BUNDLE_DIR)/*.pom; do \
	  gpg --batch --yes -ab -u $(GPG_KEY_ID) "$$f"; \
	done
	# Generate checksums for all artifacts and signatures
	@for f in $(BUNDLE_DIR)/*.jar $(BUNDLE_DIR)/*.pom $(BUNDLE_DIR)/*.asc; do \
	  md5sum "$$f" | cut -d' ' -f1 > "$$f.md5"; \
	  sha1sum "$$f" | cut -d' ' -f1 > "$$f.sha1"; \
	done
	# Create bundle ZIP
	cd target/bundle && zip -r ../bundle.zip .
	@echo "Bundle created: target/bundle.zip"

deploy:: bundle
	@echo "Uploading bundle to Maven Central Portal..."
	curl --fail --request POST \
	  --header "Authorization: Bearer $(CENTRAL_TOKEN)" \
	  --form bundle=@target/bundle.zip \
	  "https://central.sonatype.com/api/v1/publisher/upload?publishingType=AUTOMATIC"

release-deps:: $(LEIN)
