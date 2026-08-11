# Credit Recovery API

API REST para geração de estratégias de recuperação de crédito para clientes PJ, desenvolvida como solução para o case de **Engenharia de Software Back-End Cloud AWS**.

A proposta foi construir primeiro um **MVP funcional de ponta a ponta** e, a partir dele, projetar uma evolução da solução para um cenário produtivo, considerando principalmente:

- performance;
- escalabilidade;
- resiliência;
- observabilidade;
- segurança;
- boas práticas de engenharia de software.

> Para detalhes de configuração, endpoints, execução local, Docker e demais informações técnicas, consulte a [Documentação Técnica](./docs/TECHNICAL.md).

---

## 1. Visão geral

A aplicação recebe informações cadastrais, financeiras e relacionadas ao crédito de clientes PJ e utiliza esses dados para gerar uma estratégia de recuperação.

A estratégia pode definir ações como:

- negativação ou positivação;
- comunicação via WhatsApp, e-mail ou SMS;
- envio para escritório parceiro;
- notificação em canais digitais;
- bloqueio temporário de cartão.

As regras utilizadas são **fictícias** e têm como objetivo demonstrar o funcionamento do mecanismo de decisão, já que o case não fornece as regras reais de recuperação de crédito utilizadas pelo banco.

### Fluxo da solução

```text
Usuários
   ↓
API REST
   ↓
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

A solução foi estruturada buscando manter o fluxo simples, as responsabilidades separadas e um caminho claro de evolução.

---

## 2. Implementação

A aplicação foi desenvolvida em **Java 21 com Spring Boot**.

A estrutura principal separa entrada HTTP, orquestração, regras de negócio e persistência.

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

| Componente             | Responsabilidade                                        |
| ---------------------- | ------------------------------------------------------- |
| `StrategyController`   | Receber requisições HTTP e retornar as respostas da API |
| `StrategyService`      | Orquestrar geração, persistência e consulta             |
| `CreditStrategyEngine` | Aplicar as regras de geração da estratégia              |
| `StrategyRepository`   | Realizar o acesso ao Amazon DynamoDB                    |

### Boas práticas de engenharia

Durante o desenvolvimento, algumas decisões foram adotadas para manter a solução organizada e fácil de evoluir:

- **separação de responsabilidades** entre Controller, Service, Engine e Repository;
- **regras de negócio isoladas** do protocolo HTTP e da persistência;
- **validação das entradas** recebidas pela API;
- **tratamento centralizado de erros**;
- **configurações externalizadas** por ambiente;
- **credenciais AWS fora do código e da imagem Docker**;
- **testes automatizados** para comportamentos importantes;
- **logs de eventos relevantes**, evitando dados sensíveis;
- **conteinerização com Docker** para maior consistência entre ambientes.

---

## 3. Motor de estratégia

O `CreditStrategyEngine` concentra as regras responsáveis por transformar os dados recebidos em uma estratégia de recuperação.

### Exemplos de decisões

| Cenário                                         | Estratégia                        |
| ----------------------------------------------- | --------------------------------- |
| Cliente sem atraso e score elevado              | Positivação                       |
| Pequeno atraso e baixo valor em aberto          | Comunicação via SMS               |
| Atraso entre 11 e 30 dias                       | WhatsApp + canal digital          |
| Atraso entre 31 e 60 dias                       | Negativação + comunicação digital |
| Atraso superior a 60 dias                       | Negativação + escritório parceiro |
| Cartão de crédito com atraso superior a 60 dias | Bloqueio temporário do cartão     |

O objetivo não é reproduzir uma política real de crédito, mas demonstrar uma estrutura onde as regras podem ser alteradas ou ampliadas sem impactar diretamente outras camadas da aplicação.

---

## 4. Persistência

Para persistência das estratégias foi utilizado o **Amazon DynamoDB**.

A escolha considera principalmente:

- serviço gerenciado pela AWS;
- capacidade de escalabilidade;
- integração com os demais componentes da arquitetura;
- acesso simples à estratégia pelo identificador do cliente.

### Modelo do MVP

```text
Partition Key: customerId
```

Cada cliente possui sua estratégia atual armazenada.

Essa foi uma simplificação intencional para o MVP.

### Possível evolução

Caso fosse necessário manter o histórico das estratégias:

```text
PK: customerId
SK: generatedAt
```

Isso permitiria armazenar diferentes estratégias para o mesmo cliente ao longo do tempo.

---

## 5. Arquitetura — MVP

O MVP foi estruturado com foco em validar o fluxo completo da solução na AWS sem adicionar complexidade desnecessária.

![Arquitetura implementada — MVP](prints/diagrama-arquitetura-mvp.png)

### Principais componentes

- **Java 21 + Spring Boot** — API;
- **Docker** — conteinerização;
- **Amazon ECR** — armazenamento da imagem;
- **Amazon ECS Fargate** — execução da aplicação;
- **Amazon DynamoDB** — persistência;
- **IAM Task Role** — acesso seguro aos recursos AWS;
- **Amazon CloudWatch** — centralização de logs;
- **Spring Boot Actuator** — health check.

### Fluxo principal

```text
Usuário
   ↓
Spring Boot API
ECS Fargate
   ↓
Motor de estratégia
   ↓
DynamoDB
```

A imagem da aplicação é armazenada no Amazon ECR e executada pelo ECS Fargate.

O acesso ao DynamoDB utiliza uma **IAM Task Role**, evitando credenciais fixas dentro da aplicação ou do container.

Os logs podem ser centralizados no CloudWatch e o Spring Boot Actuator disponibiliza um endpoint para acompanhamento da saúde da aplicação.

### Objetivo do MVP

```text
Receber requisição
        ↓
Processar estratégia
        ↓
Persistir no DynamoDB
        ↓
Retornar resposta
```

O objetivo foi validar primeiro esse fluxo end-to-end.

Os mecanismos adicionais de alta disponibilidade, escalabilidade e observabilidade são tratados na evolução da arquitetura para produção.

---

## 6. Performance

O case define como referência um tempo de resposta de até **300 ms**.

Para obter uma evidência inicial do comportamento da aplicação, foi realizado um teste controlado utilizando **k6** sobre o endpoint principal:

```http
POST /api/v1/strategies
```

### Cenário

```text
3 usuários virtuais
30 segundos
p95 < 300 ms
taxa máxima de erro: 5%
```

### Resultado

| Métrica               |    Resultado |
| --------------------- | -----------: |
| Requisições           |        3.564 |
| Throughput médio      | 118.74 req/s |
| Tempo médio           |     25.15 ms |
| Mediana               |     14.32 ms |
| p90                   |     62.29 ms |
| **p95**               |  **69.1 ms** |
| Maior tempo observado |    528.57 ms |
| Taxa de erro          |    **0.00%** |
| Checks com sucesso    |     **100%** |

![Resultado do teste de performance com k6](prints/k6-performance.png)

### Resultado principal

```text
p95 ≈ 69 ms
      ↓
referência do case: 300 ms
```

No cenário controlado testado, 95% das requisições foram concluídas em aproximadamente **69 ms**.

Foi observado um pico individual de aproximadamente **528 ms**, por isso o resultado não é tratado como garantia de performance em produção.

O teste representa uma **validação inicial do comportamento da API no cenário executado**.

Em produção, essa análise seria complementada por:

- testes de carga com volumes maiores;
- testes de stress;
- diferentes níveis de concorrência;
- monitoramento contínuo do p95;
- alarmes para degradação da latência.

---

## 7. Arquitetura — Produção

Após a validação do fluxo principal, a arquitetura pode evoluir para um cenário com maiores requisitos de disponibilidade e volume de tráfego.

![Arquitetura proposta — produção](prints/diagrama-arquitetura-prod.png)

### Evolução principal

```text
                         Usuários
                            ↓
                Application Load Balancer
                            ↓
              ┌─────────────┴─────────────┐
              ↓                           ↓
       ECS Fargate                 ECS Fargate
        Task — AZ 1                 Task — AZ 2
              └─────────────┬─────────────┘
                            ↓
                        DynamoDB
```

### Componentes adicionados ou ampliados

- **Application Load Balancer** para distribuição das requisições;
- múltiplas tasks no **ECS Fargate**;
- distribuição entre diferentes zonas de disponibilidade;
- **ECS Auto Scaling** de acordo com a demanda;
- **CloudWatch Logs, Metrics e Alarms**;
- **AWS X-Ray** para tracing distribuído;
- **IAM Roles** para controle de acesso.

A aplicação e suas regras permanecem as mesmas.

A principal evolução acontece na infraestrutura ao redor da aplicação, adicionando mecanismos para suportar crescimento de tráfego, falhas individuais e maior necessidade de monitoramento.

---

## 8. Pilares técnicos

### Performance

A validação inicial com k6 apresentou:

```text
p95 ≈ 69 ms
```

no cenário testado.

Em produção, a latência e principalmente o p95 deveriam ser acompanhados continuamente por métricas e alarmes.

---

### Escalabilidade

A aplicação pode ser executada em múltiplas tasks do ECS Fargate.

```text
Aumento de demanda
       ↓
ECS Auto Scaling
       ↓
Mais tasks disponíveis
```

O DynamoDB também fornece uma camada de persistência gerenciada e escalável para acompanhar o crescimento no volume de requisições e dados.

---

### Resiliência

A evolução produtiva evita a dependência de uma única instância da aplicação.

```text
Task 1 saudável ───┐
                   ├── Application Load Balancer
Task 2 saudável ───┘

Task com falha → removida da distribuição de tráfego
```

Com múltiplas tasks distribuídas entre zonas de disponibilidade e health checks realizados pelo Load Balancer, a falha de uma instância individual não precisa comprometer toda a disponibilidade da API.

---

### Observabilidade

A estratégia considera três pilares principais:

```text
Logs        Métricas        Tracing
 │             │               │
 ↓             ↓               ↓
Eventos     Comportamento   Fluxo da
e erros     da aplicação    requisição
```

#### Logs

A aplicação registra eventos como:

- geração de estratégias;
- consultas;
- erros;
- tempo de processamento.

Os logs podem ser centralizados no **Amazon CloudWatch**.

Dados financeiros sensíveis e credenciais não devem ser registrados diretamente.

#### Métricas

Na arquitetura de produção, algumas métricas importantes seriam:

- latência e p95;
- taxa de erros;
- volume de requisições;
- CPU;
- memória;
- quantidade de tasks disponíveis.

O CloudWatch também pode utilizar essas métricas para criação de alarmes.

#### Tracing

O **AWS X-Ray** pode complementar logs e métricas com tracing distribuído.

Isso permite acompanhar o caminho de uma requisição e facilitar a identificação de gargalos ou falhas entre os componentes da solução.

---

## 9. Segurança

O acesso aos serviços AWS foi pensado evitando credenciais fixas na aplicação.

```text
ECS Task
   ↓
IAM Task Role
   ↓
DynamoDB
```

Credenciais AWS não devem permanecer:

- no código-fonte;
- no repositório;
- na imagem Docker.

A IAM Role também permite aplicar o princípio de **menor privilégio**, fornecendo apenas as permissões necessárias para a aplicação.

Nos logs também são evitados dados como:

- credenciais;
- requisições completas com informações sensíveis;
- informações financeiras desnecessárias para diagnóstico.

---

## 10. Testes

Além da validação de performance, foram implementados testes automatizados para comportamentos importantes da aplicação.

### Principais pontos cobertos

- regras do `CreditStrategyEngine`;
- comportamento dos endpoints;
- validação de requisições inválidas;
- consulta de estratégia existente;
- consulta de estratégia inexistente;
- interação entre `StrategyService` e `StrategyRepository`.

A intenção foi validar tanto o fluxo principal quanto regras centrais e alguns cenários de erro.

---

## 11. Trade-offs e evoluções

O MVP foi mantido intencionalmente simples.

A ideia foi implementar o necessário para validar a solução e deixar explícito como ela poderia evoluir conforme novos requisitos surgissem.

| Ponto                  | MVP                                          | Evolução                                 |
| ---------------------- | -------------------------------------------- | ---------------------------------------- |
| Estratégia por cliente | `customerId` como chave única                | Histórico com `customerId + generatedAt` |
| Disponibilidade        | Uma task para validação                      | Múltiplas tasks entre AZs                |
| Entrada da aplicação   | Fluxo simplificado                           | Application Load Balancer                |
| Escalabilidade         | Escopo do MVP                                | ECS Auto Scaling                         |
| Observabilidade        | Logs + tempo de processamento + health check | Métricas + alarmes + tracing             |
| Performance            | Teste controlado com k6                      | Carga e stress em maior escala           |
| Deploy                 | Processo do MVP                              | Pipeline CI/CD                           |
| Infraestrutura         | Recursos necessários para validação          | Infrastructure as Code                   |

### Princípio adotado

```text
Simplicidade
     +
Fluxo funcional
     +
Validação end-to-end
     ↓
Evolução conforme necessidade
```

A intenção foi evitar adicionar serviços ou tecnologias apenas para aumentar a complexidade da solução.

---

## 12. Conclusão

A solução foi construída começando por um MVP funcional capaz de realizar o fluxo principal:

```text
Dados do cliente
       ↓
API REST
       ↓
Motor de estratégia
       ↓
Persistência
       ↓
Estratégia de recuperação
```

A partir desse fluxo, a solução foi evoluída arquiteturalmente para considerar:

- performance;
- alta disponibilidade;
- escalabilidade;
- resiliência;
- observabilidade;
- segurança.

O objetivo foi manter uma implementação simples de compreender e evoluir, utilizando boas práticas de engenharia e relacionando as decisões técnicas às necessidades apresentadas pelo case.
