default: jar

SKIPTESTS=-Dmaven.test.skip=true
APPNAME=chealth

.PHONY: clean
clean:
	rm -f *.zip
	mvn clean

.PHONY: jar
jar:
	mvn $(SKIPTESTS) package

.PHONY: zip
zip:
	rm -f $(APPNAME).zip
	zip -r $(APPNAME) pom.xml buildspec.yml src

# This deploys the application to its test environment in AWS Elastic Beanstalk.
.PHONY: deploy-test
deploy-test: zip
	aws --profile ucmerced s3 cp $(APPNAME).zip s3://eas-source-deploy/$(APPNAME)-test.zip

