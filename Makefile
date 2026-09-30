.DEFAULT_GOAL := help

.PHONY: help dev-spring dev-fastapi web

help: ## Show the available development commands
	@printf '%s\n' \
		'Development commands:' \
		'  make dev-spring    Run the Spring Boot API on port 8080' \
		'  make dev-fastapi   Run the FastAPI API on port 8000' \
		'  make web           Run the React frontend (not scaffolded yet)'

dev-spring: ## Run the Spring Boot backend
	$(MAKE) -C services/api-spring run

dev-fastapi: ## Run the FastAPI backend
	$(MAKE) -C services/api-fastapi run

web: ## Run the React frontend once it has been scaffolded
	@printf '%s\n' 'The React frontend has not been scaffolded yet (apps/web).' >&2
	@exit 2
