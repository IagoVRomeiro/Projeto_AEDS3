📁 Projeto de Sistema de Gerenciamento com Compressão, Indexação, Casamento de Padrões e Criptografia
🎓 Pontifícia Universidade Católica de Minas Gerais
Instituto de Ciências Exatas e Informática
Disciplina: Algoritmos e Estruturas de Dados III
Período: 1º Semestre de 2025

📌 Objetivo Geral
Desenvolver um sistema completo de gerenciamento de dados baseado em arquivos binários, incorporando funcionalidades de:

CRUD genérico

Indexação (Árvore B+ ou Hashing Estendido)

Compressão de dados (Huffman e LZW)

Casamento de padrões

Criptografia (dois algoritmos distintos)

⚙️ Tecnologias Utilizadas
Linguagem: Java (C-like)

Paradigma: Orientação a Objetos

Entrada/Saída: Arquivos binários e leitura por terminal

Bibliotecas auxiliares para leitura/escrita e manipulação de arquivos

🧱 Estrutura do Sistema
✅ CRUD Genérico (TP1)
Importação de dados por CSV

Registros compostos por:

ID

String de tamanho variável

Data

Lista de Strings

Número (int/float)

Operações:

Criar, Ler (1 ou vários), Atualizar, Deletar (lápide)

Arquivo estruturado com:

Cabeçalho (último ID)

Indicador de tamanho

Lápide

Vetor de bytes

🌳 Indexação (TP2)
Métodos implementados:

Árvore B ou B+ (parametrizável)

Hashing Estendido (buckets dinâmicos)

Indexação baseada no campo ID

Arquivo de índice sincronizado com o de dados

Justificativas documentadas para a escolha do método

📦 Compressão de Dados (TP3)
Algoritmos:

Huffman

LZW (com dicionário inicial definido pelo grupo)

Funcionalidades:

Compressão e descompressão completas da base

Arquivos gerados com o padrão: nomeArquivoAlgoritmoX.db

Comparação automática:

Tempo de execução

Porcentagem de compressão

Descompressão substitui os dados no arquivo principal

🔍 Casamento de Padrões (TP4)
Implementação de algoritmo de busca de padrões (definido e justificado pelo grupo)

Permite buscar um termo/padrão em um campo ou no registro completo

🔐 Criptografia (TP4)
Dois algoritmos implementados (ex.: XOR e César)

Criptografia em campo(s) ou registro completo

Dados são armazenados criptografados, mas exibidos descriptografados

🧪 Execução e Testes
O programa é executado por terminal, com menus interativos permitindo:

Carga de dados

CRUD completo

Compressão/Descompressão com comparação

Indexação e busca

Busca por padrões

Criptografia e descriptografia automática

Cada funcionalidade foi testada individualmente com diferentes volumes de dados e cenários de uso.
