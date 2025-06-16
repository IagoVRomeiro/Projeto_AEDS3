
import java.io.*;
import java.util.*;

public class Huffman {

    public static int versaoCompressao = 0;
    public static long tempoCompressao = 0;
    public static long tempoDescompressao = 0;
    public static double ganho = 0;

    // Classe auxiliar que representa um nó da árvore de Huffman
    static class NoHuffman implements Comparable<NoHuffman> {

        byte valor;
        int frequencia;
        NoHuffman esquerda, direita;

        NoHuffman(byte valor, int frequencia) {
            this.valor = valor;
            this.frequencia = frequencia;
        }

        NoHuffman(NoHuffman esquerda, NoHuffman direita) {
            this.valor = 0;
            this.frequencia = esquerda.frequencia + direita.frequencia;
            this.esquerda = esquerda;
            this.direita = direita;
        }

        @Override
        public int compareTo(NoHuffman outro) {
            return Integer.compare(this.frequencia, outro.frequencia);
        }

        boolean ehFolha() {
            return esquerda == null && direita == null;
        }
    }

    // Gera um mapa de códigos Huffman para cada byte, a partir da árvore
    private static Map<Byte, String> gerarCodigos(NoHuffman raiz) {
        Map<Byte, String> codigos = new HashMap<>();
        gerarCodigos(raiz, "", codigos);
        return codigos;
    }

    // Função recursiva que preenche o mapa de códigos com base na árvore
    private static void gerarCodigos(NoHuffman no, String codigo, Map<Byte, String> mapa) {
        if (no.ehFolha()) {
            mapa.put(no.valor, codigo);
            return;
        }
        gerarCodigos(no.esquerda, codigo + "0", mapa);
        gerarCodigos(no.direita, codigo + "1", mapa);
    }

    // Comprime um arquivo usando o algoritmo de Huffman
    public static void comprimir(String caminhoEntrada, String caminhoSaida) throws IOException {
        byte[] dados;
        try (FileInputStream leitor = new FileInputStream(caminhoEntrada)) {
            dados = leitor.readAllBytes();
        }

        Map<Byte, Integer> frequencias = new HashMap<>();
        for (byte b : dados) {
            frequencias.put(b, frequencias.getOrDefault(b, 0) + 1);
        }

        PriorityQueue<NoHuffman> fila = new PriorityQueue<>();
        for (Map.Entry<Byte, Integer> entrada : frequencias.entrySet()) {
            fila.add(new NoHuffman(entrada.getKey(), entrada.getValue()));
        }

        while (fila.size() > 1) {
            NoHuffman esquerda = fila.poll();
            NoHuffman direita = fila.poll();
            fila.add(new NoHuffman(esquerda, direita));
        }

        NoHuffman raiz = fila.poll();
        Map<Byte, String> codigos = gerarCodigos(raiz);

        StringBuilder bits = new StringBuilder();
        for (byte b : dados) {
            bits.append(codigos.get(b));
        }

        BitSet conjuntoBits = new BitSet(bits.length());
        for (int i = 0; i < bits.length(); i++) {
            if (bits.charAt(i) == '1') {
                conjuntoBits.set(i);
            }
        }

        try (ObjectOutputStream saida = new ObjectOutputStream(new FileOutputStream(caminhoSaida))) {
            saida.writeObject(codigos);
            saida.writeInt(bits.length());
            saida.write(conjuntoBits.toByteArray());
        }

    }

    // Descomprime um arquivo previamente comprimido com Huffman
    public static void descomprimir(String caminhoEntrada)
            throws IOException, ClassNotFoundException {

        File arquivoComprimido = new File(caminhoEntrada);
        if (!arquivoComprimido.exists()) {
            System.out.println("Arquivo compactado não encontrado!");
            return;
        }

        Map<Byte, String> codigos;
        int tamanhoBits;
        BitSet conjuntoBits;

        try (ObjectInputStream entrada = new ObjectInputStream(new FileInputStream(arquivoComprimido))) {
            codigos = (Map<Byte, String>) entrada.readObject();
            tamanhoBits = entrada.readInt();
            conjuntoBits = BitSet.valueOf(entrada.readAllBytes());
        }

        Map<String, Byte> inverso = new HashMap<>();
        codigos.forEach((b, codigo) -> inverso.put(codigo, b));

        StringBuilder codigoAtual = new StringBuilder();
        ByteArrayOutputStream saidaBytes = new ByteArrayOutputStream();

        for (int i = 0; i < tamanhoBits; i++) {
            codigoAtual.append(conjuntoBits.get(i) ? '1' : '0');
            Byte b = inverso.get(codigoAtual.toString());
            if (b != null) {
                saidaBytes.write(b);
                codigoAtual.setLength(0);
            }
        }

        // Sobrescreve o arquivo original com os dados descomprimidos
        try (FileOutputStream fos = new FileOutputStream(arquivoComprimido, false)) {
            fos.write(saidaBytes.toByteArray());
        }

        // Renomeia arquivo de Compressao para Descompressao
        String nomeOriginal = arquivoComprimido.getName();
        String novoNome = nomeOriginal.replace("Compressao", "Descompressao");
        File novoArquivo = new File(arquivoComprimido.getParent(), novoNome);

        novoArquivo.delete(); // remove arquivo de destino se já existir
        arquivoComprimido.renameTo(novoArquivo);
    }

    // Executa o processo completo de compressão e exibe tempo e estatísticas
    public static void ExecutarCompressao(String CAPITULOS) throws IOException {
        String arquivoCompactado = String.format("Compressao/capitulosHuffmanCompressao%d.db", versaoCompressao);

        long inicio = System.currentTimeMillis();
        Huffman.comprimir(CAPITULOS, arquivoCompactado);
        long fim = System.currentTimeMillis();
        tempoCompressao = (fim - inicio);

        File original = new File(CAPITULOS);
        File comprimido = new File(arquivoCompactado);
        long tamanhoOriginal = original.length();
        long tamanhoCompactado = comprimido.length();
        ganho = 100.0 * (tamanhoOriginal - tamanhoCompactado) / tamanhoOriginal;

        System.out.println("Compactação concluída em " + tempoCompressao + " ms");
        System.out.println("Tamanho original: " + tamanhoOriginal + " bytes");
        System.out.println("Tamanho compactado: " + tamanhoCompactado + " bytes");
        System.out.printf("Ganho de compressão: %.2f%%\n", ganho);
    }

    // Descompressão completa de um arquivo compactado
    public static void ExecutarDescompressao(int versao) {
        String arquivoCompactado = String.format("Compressao/capitulosHuffmanCompressao%d.db", versao);

        try {
            long inicio = System.currentTimeMillis();
            descomprimir(arquivoCompactado);
            long fim = System.currentTimeMillis();
            tempoDescompressao = (fim - inicio);

            System.out.println("Descompressão concluída em " + tempoDescompressao + " ms");

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Erro na descompressão: " + e.getMessage());

        }
    }

}
