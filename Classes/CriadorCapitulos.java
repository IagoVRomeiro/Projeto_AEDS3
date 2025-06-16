import java.io.*;
import java.text.ParseException;

public class CriadorCapitulos {

    private static final String CSV_PATH = "Capitulos/capitulos.csv";
    private static final String BIN_PATH = "Capitulos/capitulos.db";

    public static void gerarCapitulos() throws IOException, ParseException {
        File arquivoBinario = new File(BIN_PATH);

        // Se já existe, sai logo
        if (arquivoBinario.exists()) {
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(CSV_PATH));
             RandomAccessFile raf = new RandomAccessFile(BIN_PATH, "rw")) {

            raf.writeInt(0); // reserva 4 bytes para último ID inserido

            String linha;
            while ((linha = br.readLine()) != null) {
                String[] campos = Funcoes.separarPorVirgula(linha);

                short numeroCapitulo = Short.parseShort(campos[0]);
                int id = numeroCapitulo;
                short volume = Short.parseShort(campos[1]);
                String nome = campos[2];
                String[] titulos = {campos[3], campos[4]};
                short paginas = Short.parseShort(campos[5]);
                String data = Funcoes.formatarData(campos[6]);
                String episodio = campos[7];

                Capitulo capitulo = new Capitulo(id, numeroCapitulo, volume, nome, titulos, paginas, data, episodio);
                byte[] dataBytes = capitulo.toByteArray();

                // Escreve capítulo no arquivo binário, no final
                Funcoes.escreverCapitulo(dataBytes, raf.length());
                Funcoes.IncrementaUltimoIdInserido();
            }
        }

        System.out.println("Importação concluída com sucesso!");
    }
}
