import java.io.*;
import java.util.*;

public class Criptografia {

    // Caminho do arquivo original de capítulos
    public static String CAPITULOS = "Capitulos/capitulos.db";

    // Chave usada para criptografia XOR
    private static final byte CHAVE_XOR = 0x5A;

    // Criptografa os dados com XOR (opera byte a byte com a chave)
    public static byte[] criptografarXOR(byte[] dados) {
        byte[] criptografado = new byte[dados.length];
        for (int i = 0; i < dados.length; i++) {
            criptografado[i] = (byte)(dados[i] ^ CHAVE_XOR);
        }
        return criptografado;
    }

    // Criptografa os dados com o algoritmo de César (desloca +3 cada byte)
    public static byte[] criptografarCaesar(byte[] dados) {
        byte[] criptografado = new byte[dados.length];
        for (int i = 0; i < dados.length; i++) {
            criptografado[i] = (byte)(dados[i] + 3);
        }
        return criptografado;
    }

    // Descriptografa dados usando XOR (mesmo processo que criptografar)
    public static byte[] descriptografarXOR(byte[] dados) {
        return criptografarXOR(dados);
    }

    // Descriptografa dados com o algoritmo de César (desloca -3 cada byte)
    public static byte[] descriptografarCaesar(byte[] dados) {
        byte[] descriptografado = new byte[dados.length];
        for (int i = 0; i < dados.length; i++) {
            descriptografado[i] = (byte)(dados[i] - 3);
        }
        return descriptografado;
    }

    // Lê o arquivo capitulos.db, criptografa e salva como novo arquivo com nome baseado no método e versão
    public static void criptografar(int metodo, int versao) throws IOException {
        String nomeMetodo = (metodo == 1) ? "XOR" : "Caesar";
        String arquivoOrigem = CAPITULOS;
        String arquivoDestino = String.format("Criptografia/criptografia%s%d.db", nomeMetodo, versao);

        // Leitura dos dados do arquivo original
        byte[] dadosOriginais;
        try (FileInputStream fis = new FileInputStream(arquivoOrigem)) {
            dadosOriginais = fis.readAllBytes();
        }

        // Aplica o método de criptografia escolhido
        byte[] dadosCriptografados;
        if (metodo == 1) {
            dadosCriptografados = criptografarXOR(dadosOriginais);
        } else if (metodo == 2) {
            dadosCriptografados = criptografarCaesar(dadosOriginais);
        } else {
            System.out.println("Método inválido.");
            return;
        }

        // Escreve os dados criptografados no novo arquivo
        try (FileOutputStream fos = new FileOutputStream(arquivoDestino)) {
            fos.write(dadosCriptografados);
        }

        System.out.println("Arquivo criptografado salvo em: " + arquivoDestino);
    }

    // Lê um arquivo criptografado, descriptografa e retorna os dados
    public static byte[] descriptografarArquivo(int metodo, String arquivoCriptografado) throws IOException {
        byte[] dadosCriptografados;
        try (FileInputStream fis = new FileInputStream(arquivoCriptografado)) {
            dadosCriptografados = fis.readAllBytes();
        }

        // Aplica o método de descriptografia conforme escolhido
        if (metodo == 1) {
            return descriptografarXOR(dadosCriptografados);
        } else if (metodo == 2) {
            return descriptografarCaesar(dadosCriptografados);
        } else {
            System.out.println("Método inválido.");
            return null;
        }
    }

    // Pré-processa o padrão de busca para gerar a tabela de prefixo (KMP)
    private static int[] kmpPreprocess(byte[] pattern) {
        int[] lps = new int[pattern.length];
        int j = 0;
        for (int i = 1; i < pattern.length;) {
            if (pattern[i] == pattern[j]) {
                lps[i++] = ++j;
            } else if (j > 0) {
                j = lps[j - 1];
            } else {
                lps[i++] = 0;
            }
        }
        return lps;
    }

    // Algoritmo de KMP que busca todas as ocorrências do padrão no texto
    public static List<Integer> buscarPadrao(byte[] texto, byte[] padrao) {
        List<Integer> ocorrencias = new ArrayList<>();
        int[] lps = kmpPreprocess(padrao);
        int i = 0, j = 0;

        while (i < texto.length) {
            if (texto[i] == padrao[j]) {
                i++; j++;
                if (j == padrao.length) {
                    ocorrencias.add(i - j);  // Encontrou uma ocorrência
                    j = lps[j - 1];          // Continua buscando
                }
            } else {
                if (j != 0) {
                    j = lps[j - 1];
                } else {
                    i++;
                }
            }
        }

        return ocorrencias;
    }

    // Descriptografa e realiza a busca de padrão em um arquivo criptografado específico
    public static List<Integer> buscarPadraoNoArquivo(int metodo, int versao, byte[] padrao) throws IOException {
        String nomeMetodo = (metodo == 1) ? "XOR" : "Caesar";
        String arquivoCriptografado = String.format("Criptografia/criptografia%s%d.db", nomeMetodo, versao);

        // Lê e descriptografa o conteúdo do arquivo
        byte[] dadosDescriptografados = descriptografarArquivo(metodo, arquivoCriptografado);
        if (dadosDescriptografados == null) {
            System.out.println("Erro na descriptografia para busca de padrão.");
            return Collections.emptyList();
        }

        // Realiza a busca usando KMP
        return buscarPadrao(dadosDescriptografados, padrao);
    }
}
