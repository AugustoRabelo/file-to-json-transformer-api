# file-to-json-transformer-api

Este projeto consiste em uma aplicação Java 21 com o objetivo de importar dados não normalizados de um arquivo, processá-los e armazená-los em um banco de dados relacional. Além disso, ele fornece um serviço de exportação dos dados já normalizados, com dois endpoints REST para consultas de pedidos.
Funcionalidades principais:

    Importação de arquivos com dados não estruturados.

    Normalização e armazenamento dos dados em um banco de dados H2 (em memória).

    Exportação de dados através de endpoints REST para:

        Buscar pedidos por ID.

        Buscar pedidos em um intervalo de datas.

    Interface de configuração e controle para fácil manipulação de dados.

Arquitetura:

A aplicação segue uma arquitetura baseada em services. Temos dois serviços principais:

    FileImportService: Responsável pela importação e processamento dos dados.

    DataExportService: Responsável pela exportação de dados com endpoints REST.

Tecnologias Utilizadas:

    Java 21: Utilizado como linguagem base para a aplicação.

    Spring Boot 3.x: Framework para desenvolvimento do backend e APIs REST.

    Gradle: Gerenciador de dependências e build system.

    H2 Database: Banco de dados em memória utilizado para armazenamento de dados processados.

    Docker: Containerização da aplicação para facilitar a execução e distribuição.

Padrões e Arquitetura:

    A aplicação segue os princípios de camadas, com a separação entre controladores, serviços e repositórios.

    O padrão de Injeção de Dependências (Dependency Injection) do Spring Boot é utilizado para gerenciar os componentes da aplicação.

    O design pattern utilizado para gerenciar o processo de importação e exportação de dados foi o padrão Service Layer, com a lógica de negócio encapsulada nos serviços.

    A escolha do banco de dados H2 em memória foi feita devido à sua simplicidade e agilidade para o desenvolvimento e testes, dispensando configurações adicionais para o armazenamento de dados.

Endpoints

A aplicação oferece os seguintes endpoints para interação:

    POST /import: Envia um arquivo para ser processado e importar os dados.

    GET /orders/{id}: Consulta um pedido pelo ID.

    GET /orders: Consulta pedidos por um intervalo de datas (startDate e endDate).

Como Rodar o Projeto
1. Pré-requisitos

Certifique-se de ter o seguinte instalado:

    Java 21 ou versão superior.

    Gradle (ou use o wrapper gradlew).

    Docker (para containerizar a aplicação).

Caso não tenha o Docker instalado, você pode seguir as instruções aqui para instalar.
2. Configuração Local
   Rodando com Docker:

Se você já tem o Docker configurado em sua máquina, basta rodar os seguintes comandos para construir a imagem e rodar o container.

    Construir a imagem Docker:

docker build -t file-import-export-service .

    Rodar a aplicação no Docker:

docker run -p 8080:8080 file-import-export-service

Isso iniciará a aplicação na porta 8080. O banco de dados H2 estará em memória e os dados serão perdidos quando a aplicação for desligada.