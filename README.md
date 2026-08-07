# Credit Recovery API

API REST para gerar e consultar estratégias de recuperação de crédito para clientes PJ.

O projeto recebe dados cadastrais e financeiros simples, aplica um motor de regras fictícias e persiste a estratégia gerada no Amazon DynamoDB.

## O que a aplicação faz

- Recebe dados de um cliente PJ.
- Gera uma estratégia de recuperação de crédito.
- Define ação de crédito, canal de comunicação, ação de cartão e notificações.
- Persiste a estratégia gerada no DynamoDB.
- Permite consultar a estratégia pelo `customerId`.
- Expõe health check para monitoramento.
- Gera logs para acompanhamento em ambiente local ou AWS.

## Tecnologias

- Java 21
- Spring Boot
- Maven
- Spring Web
- Bean Validation
- Spring Boot Actuator
- AWS SDK for Java 2.x
- Amazon DynamoDB
- Docker
- OpenAPI
- JUnit, Spring Boot Test e MockMvc

## Arquitetura implementada

Fluxo principal:

```text
StrategyController
    -> StrategyService
        -> CreditStrategyEngine
        -> StrategyRepository
            -> DynamoDB
```

Responsabilidades:

- `StrategyController`: recebe requisições HTTP e retorna respostas da API.
- `StrategyService`: orquestra a geração, a persistência e a consulta das estratégias.
- `CreditStrategyEngine`: concentra as regras de decisão.
- `StrategyRepository`: acessa o DynamoDB.
- `GlobalExceptionHandler`: padroniza as respostas de erro.
- `AwsDynamoDbConfig`: configura o cliente DynamoDB.
- `OpenApiConfig`: configura a documentação OpenAPI.

### Arquitetura Implementada

Diagrama da arquitetura implementada:

```text
[Adicionar print da arquitetura implementada]
```

## Regras de estratégia

As regras abaixo são fictícias e servem para demonstrar o funcionamento do motor de estratégia.

| Condição                                               | Canal    | Ação de crédito | Ação de cartão  | Escritório parceiro | Canal digital |
| ------------------------------------------------------ | -------- | --------------- | --------------- | ------------------- | ------------- |
| `daysOverdue == 0` e `creditScore >= 700`              | EMAIL    | POSITIVATION    | NONE            | false               | false         |
| `1 <= daysOverdue <= 10` e `outstandingAmount <= 1000` | SMS      | NONE            | NONE            | false               | false         |
| `daysOverdue <= 10` nos demais casos                   | EMAIL    | NONE            | NONE            | false               | false         |
| `11 <= daysOverdue <= 30`                              | WHATSAPP | NONE            | NONE            | false               | true          |
| `31 <= daysOverdue <= 60`                              | WHATSAPP | NEGATIVATION    | NONE            | false               | true          |
| `daysOverdue > 60`                                     | WHATSAPP | NEGATIVATION    | NONE            | true                | true          |
| `CREDIT_CARD` e `daysOverdue > 60`                     | WHATSAPP | NEGATIVATION    | TEMPORARY_BLOCK | true                | true          |

## Endpoints

| Método | Rota                              | Descrição                         |
| ------ | --------------------------------- | --------------------------------- |
| `POST` | `/api/v1/strategies`              | Gera e persiste uma estratégia    |
| `GET`  | `/api/v1/strategies/{customerId}` | Consulta a estratégia por cliente |
| `GET`  | `/actuator/health`                | Health check da aplicação         |
| `GET`  | `/v3/api-docs`                    | Documento OpenAPI em JSON         |

## Exemplo de requisição

```http
POST /api/v1/strategies
Content-Type: application/json
```

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

Exemplo de resposta:

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

## Exemplos adicionais

### Positivação

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

Resultado esperado:

```json
"creditAction": "POSITIVATION"
```

### SMS

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

Resultado esperado:

```json
"communicationChannel": "SMS"
```

### Bloqueio temporário de cartão

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

Resultado esperado:

```json
"cardAction": "TEMPORARY_BLOCK"
```

## Configurações

O projeto utiliza serviços reais da AWS. A integração é feita pela aplicação Spring Boot usando o AWS SDK for Java 2.x.

As principais configurações da aplicação ficam externalizadas por variáveis de ambiente:

| Variável              | Descrição                 | Valor padrão                 |
| --------------------- | ------------------------- | ---------------------------- |
| `AWS_REGION`          | Região AWS usada pelo SDK | `sa-east-1`                  |
| `DYNAMODB_TABLE_NAME` | Nome da tabela DynamoDB   | `credit-recovery-strategies` |

O projeto não possui access key ou secret key no código. Cada ambiente precisa ter sua própria configuração de acesso à AWS.

Em ambiente local, a aplicação espera que as credenciais estejam configuradas na máquina, normalmente por meio da AWS CLI:

```powershell
aws configure
```

Também é possível usar perfis configurados pela AWS CLI, variáveis de ambiente ou outro mecanismo suportado pela cadeia padrão de credenciais da AWS.

Em ambiente AWS, como ECS Fargate, a aplicação deve utilizar IAM Role associada à task. Dessa forma, a aplicação acessa o DynamoDB sem credenciais fixas na imagem Docker ou no código-fonte.

A execução em ECS Fargate depende da configuração manual da infraestrutura no Console AWS, incluindo cluster, task definition, imagem do ECR, variáveis de ambiente, security group, subnets, IAM Role da task e configuração de logs no CloudWatch.

## DynamoDB

Tabela utilizada:

```text
credit-recovery-strategies
```

Chave primária:

```text
Partition key: customerId (String)
```

Campos persistidos:

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

Para o MVP, `customerId` como chave única representa uma estratégia atual por cliente.

Para uma arquitetura com histórico, uma evolução simples seria usar:

```text
PK: customerId
SK: generatedAt
```

Assim, seria possível manter várias estratégias geradas para o mesmo cliente ao longo do tempo.

## Docker

Build da imagem:

```powershell
docker build -t credit-recovery-api:latest .
```

Executar o container:

```powershell
docker run --rm -p 8080:8080 `
  -e AWS_REGION=sa-east-1 `
  -e DYNAMODB_TABLE_NAME=credit-recovery-strategies `
  credit-recovery-api:latest
```

Health check no container:

```powershell
curl http://localhost:8080/actuator/health
```

## Observabilidade

Implementado:

- Logs de geração de estratégia.
- Logs de consulta encontrada.
- Logs de consulta não encontrada.
- Logs de erro na geração.
- Logs de erro na persistência.
- Tempo de processamento da geração em `durationMs`.
- Health check via Spring Boot Actuator.
- Logs em stdout/stderr, compatíveis com coleta pelo CloudWatch Logs no ECS.

Dados que não devem ser registrados:

- requisição completa;
- `outstandingAmount`;
- `creditScore`;
- credenciais;
- informações financeiras sensíveis.

## Performance

O objetivo de performance da API pode ser validado com testes simples nos endpoints principais.

Endpoints sugeridos para medição:

- `GET /actuator/health`
- `POST /api/v1/strategies`
- `GET /api/v1/strategies/{customerId}`

### Print de performance dos endpoints

Espaço para inserir prints dos testes de performance:

```text
[tempo de resposta do health check]

[tempo de resposta do POST /api/v1/strategies]

[tempo de resposta do GET /api/v1/strategies/{customerId}]
```

## Arquitetura em Produção

Para um ambiente produtivo, a arquitetura poderia evoluir sem mudar a ideia central da aplicação.

```text
[ print da arquitetura real/produtiva aqui]
```

## Resiliência e escalabilidade

No projeto:

- A API possui tratamento global de erros.
- O DynamoDB oferece armazenamento gerenciado e escalável.
- O container pode ser executado no ECS Fargate.
- Os logs podem ser coletados pelo CloudWatch.

Em uma arquitetura produtiva:

- Mais de uma task reduziria a indisponibilidade.
- Auto Scaling aumentaria a capacidade conforme a demanda.
- Alarmes ajudariam a detectar falhas.
- Tracing facilitaria o diagnóstico de gargalos.
- Políticas IAM limitariam o acesso da aplicação somente aos recursos necessários.

## Testes

Os testes automatizados cobrem:

- regras do `CreditStrategyEngine`;
- respostas HTTP do `StrategyController`;
- validação de requisição inválida;
- consulta existente e inexistente;
- chamada ao repository pelo `StrategyService`.

Comando:

```powershell
.\mvnw.cmd test
```

## Pontos que podem ser evoluídos

- Adicionar evidência de performance ao repositório ou à documentação.
- Adicionar logs para erros inesperados no tratamento global.
- Criar histórico de estratégias com chave composta no DynamoDB.
- Expor métricas de aplicação em ambiente controlado.
- Proteger endpoints administrativos em ambiente produtivo.
- Criar pipeline de deploy automatizado.
