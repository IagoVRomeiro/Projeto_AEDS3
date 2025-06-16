
import java.io.IOException;

public class CRUD {

    public static final String CAPITULOS = "Capitulos/capitulos.db";

    public static void menu(TreeBplus arvore, HashEstendido hash) throws IOException {

        while (true) {
            System.out.println("\n--- Menu CRUD Capitulo ---");
            System.out.println("1. Criar Capitulo");
            System.out.println("2. Ler Um Capitulo");
            System.out.println("3. Ler Multiplos Capitulos");
            System.out.println("4. Atualizar Capitulo");
            System.out.println("5. Deletar Capitulo");
            System.out.println("6. Compactar base de dados");
            System.out.println("7. Descompactar base de dados");
            System.out.println("8. Sair");
            System.out.print("Escolha uma opcao: ");

            int opcao = Funcoes.lerIntValido();

            switch (opcao) {
                case 1 -> {
                    // Cria um novo capítulo e insere nas estruturas.
                    FuncoesCRUD.Criar(Funcoes.InstanciaCapitulo(), arvore, hash);
                }
                case 2 -> {
                    // Lê e exibe um capítulo a partir do ID informado.
                    FuncoesCRUD.Ler(Funcoes.qualID(), arvore, hash);
                }
                case 3 ->
                    // Lê e exibe vários capítulos a partir de um vetor de IDs.
                    FuncoesCRUD.LerMais(Funcoes.PerguntaVetorID(), arvore, hash);
                case 4 -> {
                    // Atualiza os dados de um capítulo com base no ID.
                    FuncoesCRUD.Atualizar(Funcoes.qualID(), arvore, hash);
                }
                case 5 -> {
                    // Remove um capítulo da base de dados com base no ID.
                    FuncoesCRUD.Deletar(Funcoes.qualID(), arvore, hash);
                }
                case 6 ->
                    // Executa compressão dos dados com Huffman e LZW.
                    FuncoesCRUD.Compressao();
                case 7 ->
                    // Executa descompressão dos dados com Huffman e LZW.
                    FuncoesCRUD.Descompressao();
                case 8 -> {
                    // Encerra o programa.
                    System.out.println("Saindo...");
                    return;
                }
                default ->
                    // Trata opção inválida digitada pelo usuário.
                    System.out.println("Opção inválida. Tente novamente.");
            }

        }

    }
}
