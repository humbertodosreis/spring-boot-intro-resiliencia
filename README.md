# 🚀 Projeto: Demo de Padrões de Resiliência

Este projeto contém uma aplicação de demonstração prática projetada para ilustrar padrões de resiliência em sistemas distribuídos (Timeout, Retry, Backoff e Jitter) para uma audiência de desenvolvedores de nível júnior a pleno.

A demo é totalmente dinâmica: os estados de falha e lentidão são controlados via API, sem a necessidade de reiniciar ou recriar os serviços. A visualização do impacto é feita em tempo real usando Grafana e Prometheus.

---

## 🏛️ Arquitetura da Demonstração

A demo é composta pelos seguintes serviços orquestrados com Docker Compose:

* **`unreliable-provider` (Serviço Instável):**
    * Um serviço Spring Boot que expõe uma API (`/server/unreliable-endpoint`).
    * Possui uma API de controle (`/control`) que permite ao apresentador torná-lo lento ou fazê-lo falhar "N" vezes, dinamicamente.
* **`resilient-client` (Cliente Resiliente):**
    * O serviço Spring Boot principal da demo.
    * Usa **OpenFeign** para chamar o `provider`.
    * Usa **Spring Retry** para implementar as estratégias de Retry, Backoff e Jitter.
    * Expõe endpoints (`/demo/...`) que acionam cada cenário de resiliência.
* **Prometheus:**
    * Coleta métricas de ambos os serviços (`client` e `provider`) via Actuator.
* **Grafana:**
    * Visualiza as métricas do Prometheus.
    * Sobe com um dashboard pré-configurado ("Dashboard de Resiliência") para a demo.
* **Zipkin:**
    * Disponível para análise de tracing distribuído de cada chamada (bônus).

---

## 🛠️ Tecnologias Utilizadas

* **Backend:** Java 21, Spring Boot 3.4, Spring Retry, OpenFeign
* **Infraestrutura:** Docker & Docker Compose
* **Observabilidade:** Prometheus, Grafana (com provisionamento), Zipkin
* **Testes de Carga:** `hey` (ferramenta de linha de comando)

---

## 📋 Pré-requisitos

Para executar este projeto, você precisará ter instalado:

* Java 21 (ou superior)
* Apache Maven
* Docker e Docker Compose
* [hey](https://github.com/rakyll/hey) (ferramenta de teste de carga)
* Um terminal e seu editor de código favorito

---

## ▶️ Como Executar

1.  **Clone este repositório:**
    ```bash
    git clone <url-do-seu-repositorio>
    cd demo-resiliencia
    ```

2.  **Compile os projetos Java:**
    É necessário gerar os arquivos `.jar` que serão copiados para as imagens Docker.

    ```bash
    # Compile o cliente
    cd resilient-client
    mvn clean package
    cd ..

    # Compile o provider
    cd unreliable-provider
    mvn clean package
    cd ..
    ```

3.  **Suba toda a stack com Docker Compose:**
    Este comando irá construir as imagens, criar as redes e iniciar todos os 5 contêineres.

    ```bash
    docker-compose up --build -d
    ```

4.  **Verifique se tudo está no ar:**
    * **Grafana:** `http://localhost:3000` (O dashboard já estará lá!)
    * **Prometheus:** `http://localhost:9090`
    * **Zipkin:** `http://localhost:9411`
    * **API do Client:** `http://localhost:8080/health`
    * **API do Provider:** `http://localhost:8081/control/healthy` (Teste)

---

## 🎤 Roteiro da Demonstração (Runbook)

Este é o guia passo a passo para executar a apresentação. Tenha o Dashboard do Grafana e um terminal lado a lado.

### Setup Inicial

1.  Abra o Grafana no seu navegador: `http://localhost:3000`.
2.  Abra o dashboard "Dashboard de Resiliência".
3.  Defina o *refresh* do Grafana para **5 segundos** (canto superior direito).
4.  Resete o estado do provider para garantir que ele esteja saudável:
    ```bash
    curl -X POST "http://localhost:8081/control/healthy"
    ```

### Cenário 1: Exaustão de Threads (O Problema)

* **Objetivo:** Mostrar que, sem um timeout curto, requisições lentas podem travar toda a aplicação.
* **Gráfico no Grafana:** "Cenário 1: Exaustão de Threads (resilient-client)"

1.  **Preparar (Provider):** Configure o `provider` para ter 5 segundos de lentidão.
    ```bash
    curl -X POST "http://localhost:8081/control/slow?delay=5000"
    ```

2.  **Executar (Client):** Simule 15 usuários simultâneos chamando o endpoint com **timeout longo** (30s).
    ```bash
    hey -c 15 -n 15 "http://localhost:8080/demo/timeout?strategy=long"
    ```

3.  **Observar (Enquanto o `hey` roda):**
    * **Grafana:** O gráfico `tomcat_threads_busy` subirá e **cravará em 10**.
    * **Terminal:** Tente acessar o endpoint de health. Ele ficará travado!
        ```bash
        curl http://localhost:8080/health
        ```
    * **Lição:** Todas as 10 threads do Tomcat estão presas esperando o `provider`. A aplicação está morta.

### Cenário 2: Padrão Timeout (A Solução)

* **Objetivo:** Mostrar como o "Fail Fast" (timeout curto) salva a aplicação.
* **Gráfico no Grafana:** "Cenário 1: Exaustão de Threads (resilient-client)"

1.  **Preparar (Provider):** (Não precisa, ele já está lento).

2.  **Executar (Client):** Rode o *mesmo* teste, mas agora no endpoint com **timeout curto** (2s).
    ```bash
    hey -c 15 -n 15 "http://localhost:8080/demo/timeout?strategy=short"
    ```

3.  **Observar:**
    * **Grafana:** O gráfico `tomcat_threads_busy` dará um pico rápido e **cairá para 0** imediatamente.
    * **Terminal:** Tente acessar o endpoint de health. Ele responde na hora!
        ```bash
        curl http://localhost:8080/health
        ```
    * **Lição:** É melhor falhar rápido (Fail Fast) para 15 usuários do que travar a aplicação para *todos* os usuários.

### Cenário 3: Retry Storm (O Problema)

* **Objetivo:** Mostrar o perigo de retentativas ingênuas (sem backoff), que podem amplificar a falha.
* **Gráfico no Grafana:** "Cenário 2: Carga no Provider (Retries)"

1.  **Preparar (Provider):** Configure o `provider` para falhar nas 2 primeiras tentativas.
    ```bash
    curl -X POST "http://localhost:8081/control/flaky?count=2"
    ```

2.  **Executar (Client):** Simule 10 usuários chamando o endpoint com **retry simples**.
    ```bash
    hey -c 10 -n 10 "http://localhost:8080/demo/retry?strategy=simple"
    ```

3.  **Observar:**
    * **Grafana:** O teste dispara 10 requisições. Como cada uma tenta 3 vezes (1 original + 2 retries), o gráfico do *provider* mostrará um pico de **30 req/s**.
    * **Lição:** Amplificamos a carga no `provider` em 3x, criando uma "Tempestade de Retries" (Retry Storm) e piorando a situação.

### Cenário 4: Retry com Exponential Backoff (A Solução)

* **Objetivo:** Mostrar como o backoff "dá um tempo" para o serviço se recuperar.
* **Gráfico no Grafana:** "Cenário 2: Carga no Provider (Retries)"

1.  **Preparar (Provider):** (Já está configurado para falhar 2x).

2.  **Executar (Client):** Rode o teste no endpoint com **backoff exponencial**.
    ```bash
    hey -c 10 -n 10 "http://localhost:8080/demo/retry?strategy=backoff"
    ```

3.  **Observar:**
    * **Grafana:** O gráfico não será um pico único. Ele mostrará **"degraus"** visuais: um pico de 10 req/s (1ª tentativa), uma pausa, outro pico (2ª tentativa após 1s), outra pausa, e o pico final (3ª tentativa após 2s).
    * **Lição:** A carga foi distribuída ao longo do tempo, dando ao `provider` a chance de se recuperar.

### Cenário 5: Backoff + Jitter (O Refinamento)

* **Objetivo:** Explicar o Jitter para evitar o "Thundering Herd" (manada) de retentativas sincronizadas.
* **Gráfico no Grafana:** "Cenário 2: Carga no Provider (Retries)"

1.  **Preparar (Provider):** (Já está configurado).

2.  **Executar (Client):** Rode o teste no endpoint com **backoff + jitter**.
    ```bash
    hey -c 10 -n 10 "http://localhost:8080/demo/retry?strategy=jitter"
    ```

3.  **Observar:**
    * **Grafana:** O gráfico será similar ao do Backoff, mas os "degraus" não serão picos perfeitos. Eles serão mais "espalhados" e "suavizados".
    * **Lição:** O Jitter adiciona aleatoriedade, evitando que todos os clientes tentem novamente *exatamente* no mesmo segundo. Isso "espalha" a carga de forma ainda mais eficaz.

---

## 🧹 Limpando o Ambiente

Após a demonstração, para parar e remover todos os contêineres, redes e volumes, execute:

```bash
docker-compose down -v
````

## 📂 Estrutura do Projeto

```
.
├── config/
│   ├── grafana/                # Configs de provisionamento do Grafana
│   │   ├── dashboards/
│   │   │   └── resilience-demo.json
│   │   └── provisioning/
│   │       ├── dashboards/
│   │       └── datasources/
│   └── prometheus.yml          # Config de scrape do Prometheus
├── docker-compose.yml          # Orquestrador principal
├── client/                     # Projeto Spring (O Herói)
│   ├── Dockerfile
│   └── pom.xml
└── api/                        # Projeto Spring (O Vilão)
    ├── Dockerfile
    └── pom.xml
```