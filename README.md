
# Desafio de Desenvolvimento

O objetivo deste desafio é obter uma ideia das habilidades que o candidato possui, da organização de tempo e também do código.

## Considerações Importantes – Por favor, leia com atenção:

- Comentários sempre são bem-vindos em métodos ou estruturas mais complexas.

- Crie testes unitários, isso é importante e será avaliado!
  
- Pense nas regras de negócio cabíveis no contexto do projeto e trabalhe em cima delas, prevendo possibilidades que talvez não tenham sido descritas, e também documentando adequadamente.

- Faça commits frequentes, assim podemos ver a evolução da sua solução.

- Sobre banco de dados, você pode usar qualquer um que esteja acostumado, inclusive em memória, se preferir. Aqui utilizamos, comumente: PostgreSQL, Microsoft SQL Server, Oracle DB, MySQL e, especialmente para testes, HSQLDB. 

- Durante o período de teste, fique à vontade para enviar dúvidas ao recrutador.

- **Importante: Nos envie, ao final, uma descrição com detalhes de como podemos testar a sua implementação.**

## O que você deve implementar:

Imagine que você quer fazer um sistema de escalação de times. Toda semana você vai montar um time vencedor. 

Não importa se é Esporte tradicional ou eSports.

Exemplos de Esporte tradicional : Futebol, Basquete.

Exemplos de eSports : Counter Strike, Valorant, Free Fire, League of Legends, APEX.

Sua tarefa é construir a melhor solução no tempo combinado, considerando os requisitos que estarão descritos abaixo.

Você pode usar a criatividade pois não existe uma solução definitiva para o desafio.

Abaixo, mais detalhes:

## Estrutura dos Dados

### Tabela de "Integrante" :

- Id
- Nome
- Função

### Tabela de Time:

- Id
- Nome do Clube
- Data

### Tabela de ComposicaoTime:

- Id
- Id_Time  (foreign key tabela Time)
- Id_Integrante  (foreign key tabela Integrante)

## Funcionalidades Principais

### 1) Tratamento de dados

Com esse passo do teste, gostaríamos de medir a sua capacidade de lidar com estruturas de dados usuais. 

Já existe um service criado no projeto (ApiService), com métodos para serem implementados.  
 
Preferencialmente, utilize-o da forma como está, com a assinatura proposta.

**Obs. IMPORTANTE**: Caso decida criar um projeto do zero sem aproveitar o código já fornecido neste repositório, pedimos aqui a restrição de: 

- Não utilizar funções de SQL como 'count' para implementar estes métodos, nem procedures ou qualquer outro tratamento dos dados fora da função em **java**, pois queremos justamente entender o seu domínio sobre a linguagem para lidar com o problema a ser resolvido. Você deve usar os selects apenas para trazer todos os dados, mas processe-os na linguagem.

- Defina as classes e métodos de forma bem clara, de forma fácil para identificar suas funções, e adicione javadoc e comentários explicativos também.

No quadro, alguns detalhes sobre os métodos:

| Método  | Parâmetros | Descrição |
|--|--|--|
| TimeDaData | Data, Lista de todos os Times                              | Vai retornar um Time, com a composição do time daquela data                                 |
| IntegranteMaisUsado | Data inicial e Data final (podem ser null), Lista de todos os Times | Vai retornar o integrante que tiver presente na maior quantidade de times dentro do período |
| IntegrantesDoTimeMaisRecorrente | Data inicial e Data final (podem ser null), Lista de todos os Times | Vai retornar uma lista com os nomes dos integrantes do time mais recorrente dentro do período    |
| FuncaoMaisRecorrente | Data inicial e Data final (podem ser null), Lista de todos os Times | Vai retornar a função mais recorrente nos times dentro do período                                |
| ClubeMaisRecorrente | Data inicial e Data final (podem ser null), Lista de todos os Times |Vai retornar o nome do Clube mais comum dentro do período                      |
| ContagemDeClubesNoPeriodo | Data inicial e Data final (podem ser null), Lista de todos os Times | Vai retornar o número (quantidade) de aparições de cada Clube participante no período                           |
| ContagemPorFuncao | Data inicial e Data final (podem ser null), Lista de todos os Times | Vai retornar o número (quantidade) de Funções dentro do período                             |

## Funcionalidades Extras
### 2) API de Cadastro

#### Cadastro de Integrantes

Fazer um cadastro de integrantes para os times.

#### Cadastro de Times

Fazer um cadastro de times onde não importa muito a quantidade de integrantes. 

Para cadastrar um time para uma determinada semana basta escolher os personagens/integrantes que farão parte dele.


### 3) API para processamento de Dados

Seu sistema vai processar as informações do banco de dados e vai exportá-las através de endpoints.

Você deve usar os selects para trazer todos os dados, mas processe eles na linguagem, através dos métodos implementados no passo 1.

| Endpoint  | Parâmetros |
|--|--|
| TimeDaData | Data | 
| IntegrantesDoTimeMaisRecorrente | Data inicial e Data final (podem ser null) |
| IntegranteMaisUsado | Data inicial e Data final (podem ser null) |
| FuncaoMaisRecorrente | Data inicial e Data final (podem ser null) |
| ClubeMaisRecorrente | Data inicial e Data final (podem ser null) |
| ContagemDeClubesNoPeriodo | Data inicial e Data final (podem ser null) |
| ContagemPorFuncao | Data inicial e Data final (podem ser null) |

Exemplos de Resultados esperados:

TimeDaData
``` 
{
  "data": 2021-01-15,
  "clube": "Falcons",
  "integrantes": [ "Bangalore", "BloodHound", "Crypto" ]
}
```

FuncaoMaisRecorrente
``` 
{
  "Função" : "Meia"
}
```

ContagemDeClubesNoPeriodo
``` 
{
  "Falcons": 5,
  "FURIA": 2,
  "DarkZero Esports": 3
}
```


### 4) Telas

Você pode fazer as telas da maneira mais simples possível e usar qualquer framework que facilite o desenvolvimento.

- Tela de Inserção de Integrantes
  
- Tela de Montagem de Times pode ser feita de diversas maneiras

- Tela de consultas

O mais importante é a tela estar funcional, a beleza não será avaliada.
