# Zephyr Platform

## Descrição

O **Zephyr Platform** é um projeto focado em comparar duas abordagens para consumir e persistir dados de um arquivo `.csv`: utilizando um serviço consumidor tradicional e usando o **Kafka Connect**.

Este repositório serve como um "pai" para os módulos principais do projeto, organizando os repositórios individuais:
- **[Zephyr Producer](https://github.com/marcuslira2/zephyr-producer)** - Responsável por ler arquivos CSV e produzir mensagens para o Kafka.
- **[Zephyr Consumer](https://github.com/marcuslira2/zephyr-consumer)** - Responsável por consumir as mensagens do Kafka e persistir no banco de dados.
- **[Zephyr Infra](https://github.com/marcuslira2/zephyr-infra)** - Contém a infraestrutura Docker para subir o ambiente com Kafka, PostgreSQL e Kafka Connect.

---

## 📌 Infraestrutura e Serviços

O `docker-compose.yml` localizado no repositório **Zephyr Infra** define e configura um ambiente baseado em contêineres com **Kafka**, **PostgreSQL** e **Kafka Connect**, facilitando a inicialização e a orquestração dos serviços necessários para o processamento de mensagens e persistência de dados.

### 1. PostgreSQL (`postgres`)
- Banco de dados relacional para armazenamento de dados.
- Persistência garantida através do volume `pgdata`.
- Exposto na porta **5432**.

### 2. Zookeeper (`zookeeper`)
- Serviço de coordenação necessário para o funcionamento do Kafka.
- Exposto na porta **2181**.

### 3. Kafka (`kafka`)
- Broker de mensagens usado para publicar e consumir eventos.
- Configurado para escutar nas portas **9092** (externa) e **9093** (interna para comunicação entre contêineres).
- Permite a criação automática de tópicos.

### 4. Kafka Connect (`kafka-connect`)
- Framework para integrar Kafka com bancos de dados e outros sistemas.
- Conectado ao broker Kafka (`kafka:9093`).
- Expõe sua API REST na porta **8083**.
- Configurado para armazenar informações de configuração, offsets e status em tópicos internos.
- Plugins e conectores podem ser adicionados no diretório `./connect-plugins`.

---

## ⚙️ Configuração do Conector Sink (`sink-config.json`)

### PostgreSQL Sink Connector
Este projeto utiliza o **Kafka Connect** com o **JDBC Sink Connector** para armazenar dados do Kafka em um banco **PostgreSQL**.

### Configuração do Conector:
- **Tópico de origem**: `topic.csv.kafka`
- **Banco de destino**: PostgreSQL (`kafkateste`)
- **Tabela**: `product` (criada automaticamente se não existir)
- **Modo de inserção**: `insert` (apenas insere novos registros)
- **Conversão de dados**: JSON

### 🚀 Como Registrar o Conector
1. Certifique-se de que o **Kafka Connect** está rodando.
2. Envie a configuração via API REST do Kafka Connect:
   ```bash
   curl -X POST -H "Content-Type: application/json" --data @sink-config.json http://localhost:8083/connectors
   ```
3. Monitore o status do conector:
   ```bash
   curl -X GET http://localhost:8083/connectors/postgres-sink-connector/status
   ```

Esse conector facilita a ingestão contínua de dados do Kafka para o PostgreSQL, garantindo escalabilidade e compatibilidade com JSON. 🚀

---

## ☕ Serviços Java

Os serviços Java foram desenvolvidos com **Java 17**, utilizando **Maven** para automação de build.

- **Zephyr Producer**:
  - Lê um arquivo CSV da pasta configurada.
  - Envia os dados para um tópico correspondente ao tipo de processamento desejado:
    - **CONSUMER**: Envia um lote de mensagens comprimidas para que o serviço consumidor receba, descomprima, leia e persista os dados.
    - **KAFKA**: Ajusta a mensagem conforme a configuração do **Kafka Connect** e do `sink-config.json`, permitindo persistência direta no banco.

---

## 🛠 Como Iniciar o Projeto

### 📌 Pré-requisitos
- **Java 17**
- **Docker**
- **Maven**
- **Postman** (ou outra ferramenta para enviar requisições HTTP)

### 🚀 Passos para Configuração
1. Clone os repositórios individuais e configure cada um conforme as instruções em seus respectivos `README.md`.
2. Configure o ambiente do Java corretamente.
3. Ajuste o arquivo `sink-config.json` conforme necessário.
4. Configure os arquivos `application.properties` ou `application.yml` dos serviços Java.
5. Importe a collection de requisições que está na pasta `config` do Postman.
6. Suba os dois serviços Java (produtor e consumidor).
7. Envie as requisições via Postman para processar os arquivos CSV.

---

## 🔍 Comandos Úteis para Análise

### 📊 Verificar se um tópico tem mensagens acumuladas:
```bash
docker exec -it kafka kafka-run-class kafka.tools.GetOffsetShell --broker-list localhost:9092 --topic {NOME_DO_TOPICO} --time -1
```

### 📜 Listar tópicos disponíveis:
```bash
docker exec -it kafka kafka-topics --list --bootstrap-server localhost:9092
```

### 📥 Ler uma mensagem do tópico:
```bash
docker exec -it kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic {NOME_DO_TOPICO} --from-beginning --max-messages 1 --property print.value=true
```

---

## 📄 Licença

Este projeto está sob a licença **MIT**. Sinta-se livre para usar, modificar e contribuir!

---

## 📫 Contato
Caso tenha alguma dúvida ou sugestão, entre em contato via [GitHub Issues](https://github.com/marcuslira2/zephyr-platform/issues).

