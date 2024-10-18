:: script that builds the java jar (skipping tests) and also runs terraform install
@echo off
call mvn clean package -DskipTests
call cd src/terraform
call terraform apply -auto-approve
call cd ../..


