# Documentação Técnica — Credit Recovery API

Documentação técnica da API de geração e consulta de estratégias de recuperação de crédito para clientes PJ.

Este documento concentra informações sobre:

- estrutura da aplicação;
- API REST;
- regras utilizadas pelo motor de estratégia;
- persistência no DynamoDB;
- configuração;
- execução local;
- Docker;
- observabilidade;
- testes automatizados;
- teste de performance.

> Para a visão geral da solução, arquitetura AWS, decisões do MVP, pilares técnicos e evolução para produção, consulte o [README](../README.md).

---

## 1. Tecnologias

### Backend

- Java 21
- Spring Boot
- Spring Web
- Bean Validation
- Spring Boot Actuator
- Maven

### AWS

- AWS SDK for Java 2.x
- Amazon DynamoDB
- Amazon ECS Fargate
- Amazon ECR
- Amazon CloudWatch
- AWS IAM

### Documentação e testes

- OpenAPI
- JUnit
- Spring Boot Test
- MockMvc
- k6

### Infraestrutura e execução

- Docker

---

## 2. Estrutura da aplicação

A aplicação utiliza uma organização em camadas, separando entrada HTTP, orquestração, regras de negócio e persistência.

```text
StrategyController
        ↓
StrategyService
        ↓
CreditStrategyEngine
        ↓
StrategyRepository
        ↓
Amazon DynamoDB
```

### Responsabilidades

| Componente               | Responsabilidade                                           |
| ------------------------ | ---------------------------------------------------------- |
| `StrategyController`     | Recebe as requisições HTTP e retorna as respostas da API   |
| `StrategyService`        | Orquestra geração, persistência e consulta das estratégias |
| `CreditStrategyEngine`   | Concentra as regras utilizadas para geração da estratégia  |
| `StrategyRepository`     | Realiza o acesso ao Amazon DynamoDB                        |
| `GlobalExceptionHandler` | Centraliza o tratamento e padronização dos erros           |
| `AwsDynamoDbConfig`      | Configura o cliente utilizado para acesso ao DynamoDB      |
| `OpenApiConfig`          | Configura a documentação OpenAPI                           |

---

## 3. API REST

### Endpoints

| Método | Endpoint                          | Descrição                           |
| ------ | --------------------------------- | ----------------------------------- |
| `POST` | `/api/v1/strategies`              | Gera e persiste uma estratégia      |
| `GET`  | `/api/v1/strategies/{customerId}` | Consulta a estratégia de um cliente |
| `GET`  | `/actuator/health`                | Health check da aplicação           |
| `GET`  | `/v3/api-docs`                    | Documento OpenAPI em JSON           |

---

## 4. Geração de estratégia

### Endpoint

```http
POST /api/v1/strategies
Content-Type: application/json
```

### Exemplo de requisição

```json
{
  "customerId": "PJ-12345",
  "companyName": "Empresa XPTO LTDA",
  "daysOverdue": 45,
  "outstandingAmount": 15000.0,
  "creditScore": 420,
  "productType": "CREDIT_CARD"
}
```

### Exemplo de resposta

```json
{
  "customerId": "PJ-12345",
  "creditAction": "NEGATIVATION",
  "communicationChannel": "WHATSAPP",
  "cardAction": "NONE",
  "sendToPartnerOffice": false,
  "digitalChannelNotification": true,
  "generatedAt": "2026-08-07T15:00:00Z"
}
```

A estratégia é gerada pelo `CreditStrategyEngine`, persistida no DynamoDB e retornada ao consumidor da API.

---

## 5. Consulta de estratégia

### Endpoint

```http
GET /api/v1/strategies/{customerId}
```

Exemplo:

```http
GET /api/v1/strategies/PJ-12345
```

Quando uma estratégia é encontrada, os dados persistidos para o cliente são retornados.

Quando não existe uma estratégia associada ao `customerId`, a aplicação retorna a resposta de erro correspondente por meio do tratamento global de exceções.

---

## 6. Regras de estratégia

As regras são fictícias e foram criadas apenas para demonstrar o funcionamento do motor de decisão.

| Condição                                               | Canal    | Ação de crédito | Ação de cartão  | Escritório parceiro | Canal digital |
| ------------------------------------------------------ | -------- | --------------- | --------------- | ------------------- | ------------- |
| `daysOverdue == 0` e `creditScore >= 700`              | EMAIL    | POSITIVATION    | NONE            | false               | false         |
| `1 <= daysOverdue <= 10` e `outstandingAmount <= 1000` | SMS      | NONE            | NONE            | false               | false         |
| `daysOverdue <= 10` nos demais casos                   | EMAIL    | NONE            | NONE            | false               | false         |
| `11 <= daysOverdue <= 30`                              | WHATSAPP | NONE            | NONE            | false               | true          |
| `31 <= daysOverdue <= 60`                              | WHATSAPP | NEGATIVATION    | NONE            | false               | true          |
| `daysOverdue > 60`                                     | WHATSAPP | NEGATIVATION    | NONE            | true                | true          |
| `CREDIT_CARD` e `daysOverdue > 60`                     | WHATSAPP | NEGATIVATION    | TEMPORARY_BLOCK | true                | true          |

As regras ficam isoladas no `CreditStrategyEngine`, evitando dependência direta da camada HTTP ou da persistência.

### Exemplos

#### Positivação

```json
{
  "customerId": "PJ-POSITIVE-001",
  "companyName": "Empresa Positiva LTDA",
  "daysOverdue": 0,
  "outstandingAmount": 500.0,
  "creditScore": 700,
  "productType": "LOAN"
}
```

Resultado relevante:

```json
{
  "creditAction": "POSITIVATION"
}
```

#### Comunicação via SMS

```json
{
  "customerId": "PJ-SMS-001",
  "companyName": "Empresa SMS LTDA",
  "daysOverdue": 5,
  "outstandingAmount": 1000.0,
  "creditScore": 420,
  "productType": "LOAN"
}
```

Resultado relevante:

```json
{
  "communicationChannel": "SMS"
}
```

#### Bloqueio temporário de cartão

```json
{
  "customerId": "PJ-CARD-001",
  "companyName": "Empresa Cartão LTDA",
  "daysOverdue": 61,
  "outstandingAmount": 15000.0,
  "creditScore": 420,
  "productType": "CREDIT_CARD"
}
```

Resultado relevante:

```json
{
  "cardAction": "TEMPORARY_BLOCK"
}
```

---

## 7. Persistência com DynamoDB

A aplicação utiliza o Amazon DynamoDB para armazenamento das estratégias geradas.

### Tabela

```text
credit-recovery-strategies
```

### Chave primária

```text
Partition Key: customerId (String)
```

### Campos persistidos

- `customerId`
- `companyName`
- `daysOverdue`
- `outstandingAmount`
- `creditScore`
- `productType`
- `creditAction`
- `communicationChannel`
- `cardAction`
- `sendToPartnerOffice`
- `digitalChannelNotification`
- `generatedAt`

No MVP, o `customerId` funciona como chave única.

Isso significa que a aplicação mantém a estratégia atual associada ao cliente.

### Possível evolução para histórico

Caso seja necessário manter múltiplas estratégias por cliente:

```text
PK: customerId
SK: generatedAt
```

Esse modelo permitiria armazenar e consultar diferentes estratégias geradas ao longo do tempo.

---

## 8. Validação e tratamento de erros

As entradas da API são validadas antes do processamento por meio de Bean Validation.

O tratamento de erros é centralizado no `GlobalExceptionHandler`, evitando respostas de erro diferentes em cada Controller.

Entre os cenários tratados estão:

- requisições inválidas;
- estratégia não encontrada;
- erros durante geração ou persistência.

Essa abordagem mantém o tratamento HTTP separado das regras de negócio.

---

## 9. Configuração

As configurações da aplicação são externalizadas por variáveis de ambiente.

| Variável              | Descrição                     | Valor padrão                 |
| --------------------- | ----------------------------- | ---------------------------- |
| `AWS_REGION`          | Região utilizada pelo AWS SDK | `sa-east-1`                  |
| `DYNAMODB_TABLE_NAME` | Nome da tabela DynamoDB       | `credit-recovery-strategies` |

O projeto não mantém `access key` ou `secret key` diretamente no código-fonte.

### Ambiente local

Para execução local, as credenciais AWS podem ser configuradas utilizando a AWS CLI:

```powershell
aws configure
```

O AWS SDK utiliza sua cadeia padrão de credenciais, que pode considerar mecanismos como:

- perfil configurado pela AWS CLI;
- variáveis de ambiente;
- outros providers suportados pelo SDK.

### Ambiente AWS

No ECS Fargate, a aplicação deve utilizar uma IAM Role associada à task.

```text
ECS Task
   ↓
IAM Task Role
   ↓
DynamoDB
```

Dessa forma, não é necessário armazenar credenciais fixas dentro da aplicação ou da imagem Docker.

---

## 10. Executando localmente

### Pré-requisitos

- Java 21;
- Maven Wrapper disponível no projeto;
- credenciais AWS configuradas;
- tabela DynamoDB disponível.

### Executar a aplicação

No Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

A aplicação será disponibilizada por padrão em:

```text
http://localhost:8080
```

### Verificar health check

```powershell
curl http://localhost:8080/actuator/health
```

Resposta esperada:

```json
{
  "status": "UP"
}
```

---

## 11. Docker

### Build da imagem

```powershell
docker build -t credit-recovery-api:latest .
```

### Executar o container

```powershell
docker run --rm -p 8080:8080 `
  -e AWS_REGION=sa-east-1 `
  -e DYNAMODB_TABLE_NAME=credit-recovery-strategies `
  credit-recovery-api:latest
```

### Health check

```powershell
curl http://localhost:8080/actuator/health
```

---

## 12. Observabilidade

### Logs

A aplicação registra eventos relevantes para acompanhamento e diagnóstico.

Entre eles:

- geração de estratégia;
- consulta encontrada;
- consulta não encontrada;
- erros de processamento;
- erros de persistência;
- duração do processamento por meio de `durationMs`.

Os logs são enviados para:

```text
stdout / stderr
```

Em execução no ECS Fargate, eles podem ser coletados e centralizados pelo Amazon CloudWatch Logs.

### Health check

O Spring Boot Actuator disponibiliza:

```http
GET /actuator/health
```

Esse endpoint pode ser utilizado para verificar a saúde da aplicação e também em mecanismos de health check da infraestrutura.

### Dados sensíveis

Não devem ser registrados diretamente nos logs:

- credenciais AWS;
- requisições completas contendo dados financeiros;
- `outstandingAmount`;
- `creditScore`;
- outras informações sensíveis desnecessárias para diagnóstico.

> A estratégia produtiva completa de logs, métricas, alarmes e tracing está descrita no [README](../README.md).

---

## 13. Testes automatizados

Os testes automatizados cobrem comportamentos importantes da aplicação.

### Principais pontos testados

- regras do `CreditStrategyEngine`;
- respostas HTTP do `StrategyController`;
- validação de requisições inválidas;
- consulta de estratégia existente;
- consulta de estratégia inexistente;
- interação entre `StrategyService` e `StrategyRepository`.

### Executar os testes

No Windows:

```powershell
.\mvnw.cmd test
```

---

## 14. Teste de performance

Foi criado um teste controlado com **k6** para o endpoint:

```http
POST /api/v1/strategies
```

### Configuração

- 3 usuários virtuais;
- duração de 30 segundos;
- threshold: `p95 < 300 ms`;
- taxa máxima de erro: `5%`.

### Resultado

| Métrica               |    Resultado |
| --------------------- | -----------: |
| Total de requisições  |        3.564 |
| Throughput médio      | 118.74 req/s |
| Tempo médio           |     25.15 ms |
| Mediana               |     14.32 ms |
| p90                   |     62.29 ms |
| p95                   |      69.1 ms |
| Maior tempo observado |    528.57 ms |
| Taxa de erro          |        0.00% |
| Checks com sucesso    |         100% |

### Executar o teste

```powershell
k6 run performance\post-strategy.js
```

O resultado representa apenas o cenário controlado executado e não substitui testes de carga e stress mais abrangentes.

> A interpretação desse resultado em relação ao requisito de performance do case está apresentada no [README](../README.md).

---

## 15. OpenAPI

A aplicação disponibiliza sua especificação OpenAPI em:

```http
GET /v3/api-docs
```

Esse endpoint retorna a definição da API em formato JSON e pode ser utilizado por ferramentas compatíveis com OpenAPI.

---

## 16. Referências rápidas

### Aplicação

```text
http://localhost:8080
```

### Gerar estratégia

```text
POST /api/v1/strategies
```

### Consultar estratégia

```text
GET /api/v1/strategies/{customerId}
```

### Health check

```text
GET /actuator/health
```

### OpenAPI

```text
GET /v3/api-docs
```

### Executar aplicação

```powershell
.\mvnw.cmd spring-boot:run
```

### Executar testes

```powershell
.\mvnw.cmd test
```

### Executar teste de performance

```powershell
k6 run performance\post-strategy.js
```
