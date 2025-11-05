# Makefile para facilitar o build dos projetos da Demo de Resiliência
# Use 'make package' para buildar os .jar de ambos os serviços.
# Use 'make clean' para limpar os targets do Maven.

# Define que os alvos não são arquivos.
# Isso força o 'make' a sempre executar os comandos.
.PHONY: all package package-client package-provider clean

# -----------------------------------------------------------------------------
# ALVO PADRÃO: (Roda ao digitar apenas 'make')
# -----------------------------------------------------------------------------
# Define 'package' como o alvo padrão.
all: package

# -----------------------------------------------------------------------------
# ALVOS PRINCIPAIS:
# -----------------------------------------------------------------------------

# 'package' depende de buildar ambos os serviços.
# O Make rodará 'package-client' e 'package-provider' primeiro.
package: package-client package-provider
	@echo "================================================="
	@echo "✅ Ambos os serviços foram empacotados com sucesso."
	@echo "================================================="

# 'clean' limpa ambos os serviços.
clean:
	@echo "🧹 Limpando o projeto 'resilient-client'..."
	@cd client && mvn clean
	@echo "🧹 Limpando o projeto 'unreliable-provider'..."
	@cd api && mvn clean
	@echo "Limpeza completa."

# -----------------------------------------------------------------------------
# ALVOS INTERNOS: (Usados pelos alvos principais)
# -----------------------------------------------------------------------------

# Empacota apenas o 'resilient-client'
package-client:
	@echo "📦 Empacotando 'resilient-client' (pulando testes)..."
	@cd client && mvn clean package -DskipTests

# Empacota apenas o 'unreliable-provider'
package-provider:
	@echo "📦 Empacotando 'unreliable-provider' (pulando testes)..."
	@cd api && mvn clean package -DskipTests
