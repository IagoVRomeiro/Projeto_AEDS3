
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Funcoes {

    public static String IndiceHash = "Indices/capitulosIndiceHash.db";
    public static String IndiceArvore = "Indices/capitulosIndiceArvore.db";
    public static String CAPITULOS = "Capitulos/capitulos.db";
    public static Scanner sc = new Scanner(System.in);

    // Garante int válido
    public static int lerIntValido() {
        while (true) {
            try {
                return sc.nextInt();
            } catch (InputMismatchException e) {
                System.out.print("Entrada inválida. Digite um número válido: ");
                sc.nextLine();
            }
        }
    }

    // Garante Data válida
    public static String lerDataValida(String mensagem) {
        Pattern padraoData = Pattern.compile(
                "^(0[1-9]|[12][0-9]|3[01])/(0[1-9]|1[0-2])/\\d{4}$"
        );

        while (true) {
            System.out.print(mensagem);
            String entrada = sc.nextLine();
            Matcher matcher = padraoData.matcher(entrada);

            if (matcher.matches()) {
                return entrada;
            } else {
                System.out.println("Data inválida. Use o formato dd/MM/yyyy com valores válidos.");
            }
        }
    }

    // Garante short válido
    public static short lerShortValido(String mensagem) {
        while (true) {
            try {
                System.out.print(mensagem);
                return sc.nextShort();
            } catch (InputMismatchException e) {
                System.out.println("Valor inválido. Por favor, insira um número válido do tipo short.");
                sc.nextLine();
            }
        }
    }

    // Separa o texto CSV
    public static String[] separarPorVirgula(String texto) {
        String[] campos = texto.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

        for (int i = 0; i < campos.length; i++) {
            campos[i] = campos[i].replaceAll("^\"|\"$", "");
        }

        return campos;
    }

    // Converte a data
    public static String formatarData(String data) throws ParseException {
        SimpleDateFormat formatoEntrada = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);
        SimpleDateFormat formatoSaida = new SimpleDateFormat("dd/MM/yyyy");
        Date date = formatoEntrada.parse(data);
        return formatoSaida.format(date);
    }

    // Pergunta ao usuário qual ID ele deseja
    public static int qualID() {
        int ultimoID = 0;

        try (RandomAccessFile raf = new RandomAccessFile(CAPITULOS, "r")) {
            raf.seek(0);
            ultimoID = raf.readInt();
        } catch (IOException e) {
            System.out.println("Erro ao ler o último ID: " + e.getMessage());
            return 0;
        }

        int id;
        do {
            System.out.println("Qual o ID?");
            id = lerIntValido();

            if (id < 1 || id > ultimoID) {
                System.out.println("ID inválido! Informe um valor entre 1 e " + ultimoID + ".");
            }
        } while (id < 1 || id > ultimoID);

        return id;
    }

    // Incrementa o último ID inserido no início do arquivo
    public static void IncrementaUltimoIdInserido() throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(CAPITULOS, "rw")) {
            int ultimoID = raf.readInt();
            raf.seek(0);
            raf.writeInt(ultimoID + 1);
        }
    }

    // Escreve os dados do capítulo no arquivo de forma binária
    public static void escreverCapitulo(byte[] dataBytes, long lugar) throws IOException {

        try (RandomAccessFile raf = new RandomAccessFile(CAPITULOS, "rw")) {
            raf.seek(lugar);

            raf.writeByte(1);
            raf.writeInt(dataBytes.length);
            raf.write(dataBytes);
        }
    }

    // Cria as pastas "Compressao" e "Indices"
    public static void CriarPastas() {

        // Criar Pasta Compressao
        String caminho = "Compressao";
        File pasta = new File(caminho);
        if (!pasta.exists()) {
            pasta.mkdirs();
        }

        // Criar Pasta Indices
        caminho = "Indices";
        pasta = new File(caminho);
        if (!pasta.exists()) {
            pasta.mkdirs();
        }
    }

    // Pergunta a quantidade de IDs e os coleta
    public static int[] PerguntaVetorID() {
        System.out.print("\nDigite a quantidade de capítulos que deseja pesquisar: ");
        int qtd = lerIntValido();
        int[] ids = new int[qtd];

        for (int i = 0; i < qtd; i++) {
            ids[i] = qualID();
        }

        return ids;
    }

    // Coleta dados para criar um novo capítulo
    public static Capitulo InstanciaCapitulo() throws IOException {
        try (RandomAccessFile RAF = new RandomAccessFile(CAPITULOS, "rw")) {

            RAF.seek(0);
            int UltimoId = RAF.readInt();
            int id = UltimoId + 1;

            short numCapitulo = lerShortValido("(short) Capitulo: ");
            short volume = lerShortValido("(short) Volume: ");

            sc.nextLine();

            System.out.print("(String) Nome: ");
            String nome = sc.nextLine();

            System.out.print("(String) Titulo Original: ");
            String tituloOriginal = sc.nextLine();

            System.out.print("(String) Titulo Ingles: ");
            String tituloIngles = sc.nextLine();

            short paginas = lerShortValido("(short) Paginas: ");
            sc.nextLine();

            String data = lerDataValida("(xx/xx/xxxx) Data: ");

            System.out.print("(String) Episodio: ");
            String episodio = sc.nextLine();

            String[] titulos = {tituloOriginal, tituloIngles};

            return new Capitulo(id, numCapitulo, volume, nome, titulos, paginas, data, episodio);
        }
    }

    // Compara os resultados da compressão entre Huffman e LZW
    public static void CompararCompressao(int versao, double ganhoHuffman, double ganhoLZW, long tempoHuffman, long tempoLZW) throws IOException {

        File huffmanFile = new File(String.format("Compressao/capitulosHuffmanCompressao%d.db", versao));
        File lzwFile = new File(String.format("Compressao/capitulosLZWCompressao%d.db", versao));

        // Verifica se os arquivos de compressão existem
        if (!huffmanFile.exists() || !lzwFile.exists()) {
            return;
        }

        long tamanhoHuffman = huffmanFile.length();
        long tamanhoLZW = lzwFile.length();

        System.out.println("\n--- Comparação de Compressão ---");
        System.out.printf("Huffman: %.2f%% de redução (%d bytes)\n", ganhoHuffman, tamanhoHuffman);
        System.out.printf("LZW:     %.2f%% de redução (%d bytes)\n", ganhoLZW, tamanhoLZW);

        if (ganhoHuffman > ganhoLZW) {
            System.out.print("Huffman teve melhor compressão");
        } else if (ganhoHuffman < ganhoLZW) {
            System.out.print("LZW teve melhor compressão");
        } else {
            System.out.println("Ambos tiveram compressão equivalente.");
            return;
        }

        if (tempoHuffman > tempoLZW) {
            System.out.println(" e LZW teve melhor tempo de Descompressão.");
        } else if (tempoHuffman < tempoLZW) {
            System.out.println(" e Huffman teve melhor tempo de Descompressão.");
        } else {
            System.out.println(" e ambos tiveram tempo de descompressão equivalente.");
        }
    }

    // Compara tempos de descompressão entre Huffman e LZW.
    public static void CompararDescompressao(long tempoHuffman, long tempoLZW) throws IOException {

        System.out.println();
        if (tempoHuffman > tempoLZW) {
            System.out.println("LZW teve melhor tempo de Descompressão.");
        } else if (tempoHuffman < tempoLZW) {
            System.out.println("Huffman teve melhor tempo de Descompressão.");
        } else {
            System.out.println("Ambos tiveram compressão equivalente.");
        }
    }


    // Verifica se existe versão pra ser descompactada
    public static boolean verificarArquivosCompactadosExistem(int versao) {
        String arquivoHuffman = String.format("Compressao/capitulosHuffmanCompressao%d.db", versao);
        String arquivoLZW = String.format("Compressao/capitulosLZWCompressao%d.db", versao);

        File fHuffman = new File(arquivoHuffman);
        File fLZW = new File(arquivoLZW);

        boolean huffmanExiste = fHuffman.exists();
        boolean lzwExiste = fLZW.exists();

        if (!huffmanExiste) {
            System.out.println("Arquivo compactado Huffman não encontrado!");
        }

        if (!lzwExiste) {
            System.out.println("Arquivo compactado LZW não encontrado!");
        }

        return huffmanExiste && lzwExiste;
    }
}
