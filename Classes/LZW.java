
import java.io.*;
import java.util.*;

public class LZW {

    public static int versaoCompressao = 0;
    public static long tempoCompressao = 0;
    public static long tempoDescompressao = 0;
    public static double ganho = 0;

    // Realiza a compressão de um arquivo usando o algoritmo LZW
    public static void comprimir(String caminhoEntrada, String caminhoSaida) throws IOException {
        byte[] entrada;
        try (FileInputStream fis = new FileInputStream(caminhoEntrada)) {
            entrada = fis.readAllBytes();
        }

        Map<String, Integer> dicionario = new HashMap<>();
        for (int i = 0; i < 256; i++) {
            dicionario.put("" + (char) i, i);
        }

        List<Integer> saida = new ArrayList<>();
        String w = "";
        int codigo = 256;

        for (byte b : entrada) {
            char c = (char) (b & 0xFF);
            String wc = w + c;
            if (dicionario.containsKey(wc)) {
                w = wc;
            } else {
                saida.add(dicionario.get(w));
                dicionario.put(wc, codigo++);
                w = "" + c;
            }
        }
        if (!w.isEmpty()) {
            saida.add(dicionario.get(w));
        }

        // Grava os códigos como shorts no arquivo binário
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(caminhoSaida))) {
            for (int code : saida) {
                dos.writeShort(code); // 2 bytes por código
            }
        }

    }

    // Realiza a descompressão de um arquivo compactado com LZW
    public static void descomprimir(String caminhoEntrada) throws IOException {
        File arquivoComprimido = new File(caminhoEntrada);
        if (!arquivoComprimido.exists()) {
            System.out.println("Arquivo compactado não encontrado!");
            return;
        }

        List<Integer> codigos = new ArrayList<>();
        try (DataInputStream dis = new DataInputStream(new FileInputStream(arquivoComprimido))) {
            while (dis.available() > 0) {
                codigos.add(dis.readUnsignedShort());
            }
        }

        Map<Integer, byte[]> dicionario = new HashMap<>();
        for (int i = 0; i < 256; i++) {
            dicionario.put(i, new byte[]{(byte) i});
        }

        int codigo = 256;
        byte[] w = dicionario.get(codigos.remove(0));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(w);

        for (int k : codigos) {
            byte[] entrada;
            if (dicionario.containsKey(k)) {
                entrada = dicionario.get(k);
            } else if (k == codigo) {
                byte[] temp = new byte[w.length + 1];
                System.arraycopy(w, 0, temp, 0, w.length);
                temp[w.length] = w[0];
                entrada = temp;
            } else {
                throw new IOException("Código inválido durante descompressão: " + k);
            }

            baos.write(entrada);

            byte[] novaEntrada = new byte[w.length + 1];
            System.arraycopy(w, 0, novaEntrada, 0, w.length);
            novaEntrada[w.length] = entrada[0];
            dicionario.put(codigo++, novaEntrada);

            w = entrada;
        }

        // Sobrescreve o conteúdo do arquivo original
        try (FileOutputStream fos = new FileOutputStream(arquivoComprimido, false)) {
            baos.writeTo(fos);
        }

        // Renomeia de Compressao para Descompressao
        String nomeOriginal = arquivoComprimido.getName();
        String novoNome = nomeOriginal.replace("Compressao", "Descompressao");
        File novoArquivo = new File(arquivoComprimido.getParent(), novoNome);

        novoArquivo.delete(); // caso já exista um arquivo com o nome novo, apaga para evitar erro
        arquivoComprimido.renameTo(novoArquivo);
    }

    // Executa o processo completo de compressão, calcula métricas e exibe os resultados
    public static void ExecutarCompressao(String CAPITULOS) throws IOException {
        String arquivoCompactado = String.format("Compressao/capitulosLZWCompressao%d.db", versaoCompressao);

        long inicio = System.currentTimeMillis();
        comprimir(CAPITULOS, arquivoCompactado);
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

    // Executa o processo de descompressão a partir de um arquivo compactado, medindo o tempo de execução
    public static void ExecutarDescompressao(int versao) {
        String arquivoCompactado = String.format("Compressao/capitulosLZWCompressao%d.db", versao);

        try {
            long inicio = System.currentTimeMillis();
            descomprimir(arquivoCompactado);
            long fim = System.currentTimeMillis();
            tempoDescompressao = (fim - inicio);

            System.out.println("Descompressão concluída em " + tempoDescompressao + " ms");

        } catch (IOException e) {
            System.out.println("Erro na descompressão: " + e.getMessage());

        }
    }

}
