.PHONY: setup run run-h2 run-postgres seed test build code-format

# Resolve dependencies and compile application classes
setup:
	./gradlew classes

# Run with the default MySQL profile
run:
	SPRING_PROFILES_ACTIVE=mysql ./gradlew bootRun

# Run with the in-memory H2 profile
run-h2:
	SPRING_PROFILES_ACTIVE=h2 ./gradlew bootRun

# Run with the PostgreSQL profile
run-postgres:
	SPRING_PROFILES_ACTIVE=postgres ./gradlew bootRun

# Load the development sample catalog into the default MySQL database
seed:
	SPRING_PROFILES_ACTIVE=mysql ./gradlew bootRun --args='--app.seed-catalog=true --spring.main.web-application-type=none'

# Run the complete test suite
test:
	./gradlew test

# Build and verify the application
build:
	./gradlew build

# Apply formatting and remove unused imports
code-format:
	./gradlew spotlessApply
