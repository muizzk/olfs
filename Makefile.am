
AUTOMAKE_OPTIONS = foreign 

SUBDIRS = . hyrax_tests

TOMCAT = $(prefix)/apache-tomcat-7.0.57

all-local:
	ant server -DHYRAX_VERSION="$(HYRAX_VERSION)" -DOLFS_VERSION="$(PACKAGE_VERSION)"

# What should this target do? jhrg 1/2/18
install-exec-local:
	if test -n "$(TOMCAT)"; \
	then cp build/dist/opendap.war $(TOMCAT)/webapps; \
	else echo "Cannot install opendap.war; TOMCAT not set"; fi