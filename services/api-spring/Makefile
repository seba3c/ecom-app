.PHONY: setup run run-h2 run-postgres test build code-format

# Resolve dependencies and compile application classes
setup:
	./gradlew classes

# Run with the default MySQL profile
run:
	./gradlew bootRun

# Run with the in-memory H2 profile
run-h2:
	SPRING_PROFILES_ACTIVE=h2 ./gradlew bootRun

# Run with the PostgreSQL profile
run-postgres:
	SPRING_PROFILES_ACTIVE=postgres ./gradlew bootRun

# Run the complete test suite
test:
	./gradlew test

# Build and verify the application
build:
	./gradlew build

# Apply formatting and remove unused imports
code-format:
	./gradlew spotlessApply
