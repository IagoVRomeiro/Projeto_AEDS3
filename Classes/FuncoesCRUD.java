
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Scanner;

public class FuncoesCRUD {

    public static String IndiceHash = "Indices/capitulosIndiceHash.db";
    public static String IndiceArvore = "Indices/capitulosIndiceArvore.db";
    public static String CAPITULOS = "Capitulos/capitulos.db";
    public static Scanner sc = new Scanner(System.in);

    // Cria um novo capítulo, grava no arquivo, atualiza os índices B+ e Hash.
    public static void Criar(Capitulo capitulo, TreeBplus arvore, HashEstendido hash) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(CAPITULOS, "rw")) {
            byte[] bytes = capitulo.toByteArray();
            long endereco = raf.length(); // Endereço atual onde será escrito

            Funcoes.escreverCapitulo(bytes, endereco);
            Funcoes.IncrementaUltimoIdInserido();

            // Atualiza a árvore B+ com o novo ID e endereço
            arvore.inserir(capitulo.getId(), endereco);
            // Atualiza o arquivo de índice
            arvore.salvarFolhasNoArquivo(IndiceArvore);

            hash.inserir(capitulo.getId(), endereco);

            hash.construirHashDoArquivo(CAPITULOS);

            System.out.println("Criado com sucesso");
        }
    }

    // Lê e exibe um capítulo com base no ID, utilizando os índices B+ e Hash.
    public static void Ler(int ID, TreeBplus arvore, HashEstendido hash) throws IOException {
        // Buscar nos dois índices
        Long enderecoArvore = arvore.buscar(ID);
        Long enderecoHash = hash.buscar(ID);

        // Evita imprimir null diretamente
        System.out.println("[Arvore B+] Endereco encontrado: "
                + (enderecoArvore != null ? enderecoArvore : "nao encontrado"));
        System.out.println("[Hash Estendido] Endereco encontrado: "
                + (enderecoHash != null ? enderecoHash : "nao encontrado"));

        // Prioridade: Hash, se não existir, usa da Árvore
        Long endereco = (enderecoHash != null) ? enderecoHash : enderecoArvore;

        try (RandomAccessFile raf = new RandomAccessFile(CAPITULOS, "rw")) {
            raf.seek(endereco);
            byte valido = raf.readByte();
            int tamanhoVetor = raf.readInt();

            if (valido == 1) {
                byte[] byteArray = new byte[tamanhoVetor];
                raf.readFully(byteArray);

                Capitulo capitulo = new Capitulo();
                capitulo.fromByteArray(byteArray);

                System.out.println(capitulo.toString());
            }
        }
    }

    // Lê e exibe múltiplos capítulos com base em um vetor de IDs, usando os índices B+ e Hash.
    public static void LerMais(int[] ids, TreeBplus arvore, HashEstendido hash) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(CAPITULOS, "r")) {
            for (int id : ids) {
                Long enderecoArvore = arvore.buscar(id);
                Long enderecoHash = hash.buscar(id);

                System.out.println("\n[ID " + id + "]");
                System.out.println("  [Árvore B+] Endereço: " + enderecoArvore);
                System.out.println("  [Hash Estendido] Endereço: " + enderecoHash);

                Long endereco = (enderecoHash != null) ? enderecoHash : enderecoArvore;

                if (endereco != null) {
                    raf.seek(endereco);
                    byte valido = raf.readByte();
                    int tamanhoVetor = raf.readInt();

                    if (valido == 1) {
                        byte[] byteArray = new byte[tamanhoVetor];
                        raf.readFully(byteArray);

                        Capitulo capitulo = new Capitulo();
                        capitulo.fromByteArray(byteArray);

                        System.out.println("  Conteúdo:");
                        System.out.println("  " + capitulo.toString());
                    } else {
                        System.out.println("  Registro marcado como removido.");
                    }
                } else {
                    System.out.println("  Não encontrado em nenhum índice.");
                }
            }
        }
    }

    // Atualiza um capítulo existente: tenta sobrescrever se couber, senão insere no fim e atualiza os índices.
    public static void Atualizar(int ID, TreeBplus arvore, HashEstendido hash) throws IOException {
        try (RandomAccessFile RAF = new RandomAccessFile(CAPITULOS, "rw")) {
            Long posicao = arvore.buscar(ID);

            if (posicao == null) {
                System.out.println("ID não encontrado na árvore.");

            }

            RAF.seek(posicao);
            byte valido = RAF.readByte();
            int tamanhoVetor = RAF.readInt();

            if (valido != 1) {
                System.out.println("Registro marcado como removido.");

            }

            byte[] byteArray = new byte[tamanhoVetor];
            RAF.readFully(byteArray);

            Capitulo capitulo = new Capitulo();
            capitulo.fromByteArray(byteArray);

            if (capitulo.getId() != ID) {
                System.out.println("ID no arquivo não confere.");

            }

            Capitulo novoCapitulo = Funcoes.InstanciaCapitulo();
            novoCapitulo.setId(ID);
            byte[] novoByteArray = novoCapitulo.toByteArray();

            if (novoByteArray.length <= tamanhoVetor) {
                System.out.println("Atualização coube no espaço reservado.");
                RAF.seek(posicao + 5); // 1 byte lápide + 4 bytes tamanho
                RAF.write(novoByteArray);
                // Preenche o restante com zeros se necessário
                RAF.write(new byte[tamanhoVetor - novoByteArray.length]);
            } else {
                System.out.println("Atualização não coube. Inserido no fim do arquivo.");

                // Marca como removido o antigo
                RAF.seek(posicao);
                RAF.writeByte(0);

                // Escreve no fim do arquivo usando o método auxiliar
                long novaPosicao = RAF.length();
                Funcoes.escreverCapitulo(novoByteArray, novaPosicao);

                // Atualiza os índices
                arvore.remover(ID);
                arvore.inserir(ID, novaPosicao);

                hash.remover(ID);
                hash.inserir(ID, novaPosicao);
            }

            // Salva os arquivos de índice
            arvore.salvarFolhasNoArquivo(IndiceArvore);

        }
    }

    // Realiza a exclusão lógica de um capítulo com base no ID e atualiza os índices B+ e Hash.
    public static void Deletar(int ID, TreeBplus arvore, HashEstendido hash) throws IOException {
        try (RandomAccessFile RAF = new RandomAccessFile(CAPITULOS, "rw")) {
            RAF.seek(0);
            int UltimoId = RAF.readInt();

            while (RAF.getFilePointer() < RAF.length()) {
                long ponteiro = RAF.getFilePointer();
                byte valido = RAF.readByte();
                int tamanhoVetor = RAF.readInt();

                if (valido == 1) {
                    byte[] byteArray = new byte[tamanhoVetor];
                    RAF.readFully(byteArray);
                    Capitulo capitulo = new Capitulo();
                    capitulo.fromByteArray(byteArray);

                    if (capitulo.getId() == ID) {
                        // Exclusão lógica no arquivo
                        RAF.seek(ponteiro);
                        RAF.writeByte(0);

                        if (ID == UltimoId) {
                            RAF.seek(0);
                            RAF.writeInt(UltimoId - 1);
                        }

                        // --- Atualiza árvore B+ ---
                        arvore.remover(ID); // Certifique-se de que está implementado
                        arvore.salvarFolhasNoArquivo(IndiceArvore);

                        // --- Atualiza índice hash ---
                        hash.remover(ID);
                        System.out.println("Excluído com sucesso");

                    }
                } else {
                    RAF.skipBytes(tamanhoVetor);
                }
            }
        }

    }

    // Compressão dos dados usando Huffman e LZW.
    public static void Compressao() throws IOException {
        System.out.print("Qual versão? ");
        int versao = Funcoes.lerIntValido();

        Huffman.versaoCompressao = versao;
        LZW.versaoCompressao = versao;

        System.out.println("\n-----------Huffman-----------");
        Huffman.ExecutarCompressao(CAPITULOS);

        System.out.println("\n-----------LZW-----------");
        LZW.ExecutarCompressao(CAPITULOS);

        Funcoes.CompararCompressao(versao, Huffman.ganho, LZW.ganho, Huffman.tempoCompressao, LZW.tempoCompressao);
    }

    // Descompressão dos dados usando Huffman e LZW.
    public static void Descompressao() throws IOException {
        System.out.print("Digite a versao X da compressao para descompactar: ");
        int versao = Funcoes.lerIntValido();

        if (Funcoes.verificarArquivosCompactadosExistem(versao)) {
            System.out.println("-----------Huffman-----------");
            Huffman.ExecutarDescompressao(versao);

            System.out.println("\n-----------LZW-----------");
            LZW.ExecutarDescompressao(versao);

            Funcoes.CompararDescompressao(Huffman.tempoDescompressao, LZW.tempoDescompressao);
        }

    }
}
