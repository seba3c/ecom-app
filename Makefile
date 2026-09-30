.DEFAULT_GOAL := help

.PHONY: help dev-spring dev-fastapi seed-spring seed-fastapi web

help: ## Show the available development commands
	@printf '%s\n' \
		'Development commands:' \
		'  make dev-spring    Run the Spring Boot API on port 8080' \
		'  make dev-fastapi   Run the FastAPI API on port 8000' \
		'  make seed-spring   Seed the Spring Boot development database' \
		'  make seed-fastapi  Seed the FastAPI development database' \
		'  make web           Run the React frontend on port 5173'

dev-spring: ## Run the Spring Boot backend
	$(MAKE) -C services/api-spring run

dev-fastapi: ## Run the FastAPI backend
	$(MAKE) -C services/api-fastapi run

seed-spring: ## Load sample catalog into the Spring Boot database
	$(MAKE) -C services/api-spring seed

seed-fastapi: ## Load sample catalog into the FastAPI database
	$(MAKE) -C services/api-fastapi seed

web: ## Run the React frontend
	npm --prefix apps/web run dev
