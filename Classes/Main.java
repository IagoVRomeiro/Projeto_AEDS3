import java.io.IOException;
import java.text.ParseException;


public class Main {

    public static final String CAPITULOS = "Capitulos/capitulos.db";

    public static void main(String[] args) throws ParseException, IOException {

        //Cria as pastas Indices e Compressao
        Funcoes.CriarPastas();

        // Gerar os arquivos capitulos.db
        CriadorCapitulos.gerarCapitulos();

        //Construir a árvore B+ e Hash
        TreeBplus arvore = new TreeBplus();
        arvore.construirArvoreDoArquivo(CAPITULOS);

        HashEstendido hash = new HashEstendido();
        hash.construirHashDoArquivo(CAPITULOS);

        // Iniciar o menu
        CRUD.menu(arvore, hash);

    }
}
