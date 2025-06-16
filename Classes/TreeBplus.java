import java.io.*;
import java.util.*;

public class TreeBplus {

    public No raiz;
    private final int ordem = 5;

    // Construtor: inicializa a árvore com um nó raiz do tipo folha
    public TreeBplus() {
        raiz = new No(true);
    }

    // Retorna a raiz da árvore B+
    public No getRaiz() {
        return raiz;
    }

    // Insere um par ID/endereço na folha correta, dividindo a folha se necessário
    public void inserir(int id, long endereco) {
        No folha = encontrarFolha(raiz, id);
        folha.inserirIdEnderecoOrdenado(id, endereco);

        if (folha.verificaOverflow(ordem)) {
            dividirFolha(folha);
        }
    }

    // Imprime os IDs de todas as folhas da árvore da esquerda para a direita
    public void imprimirFolhas() {
        No atual = encontrarFolhaMaisEsquerda();
        while (atual != null) {
            System.out.println(atual.getIds());
            atual = atual.getPonteiroLado();
        }
    }

    // Lê registros válidos de um arquivo binário e os insere na árvore B+
    public void construirArvoreDoArquivo(String caminhoArquivo) {
        String caminhoIndice = "Indices/capitulosIndiceArvore.db";

        try (RandomAccessFile raf = new RandomAccessFile(caminhoArquivo, "r")) {
            raf.seek(4); // Pula os 4 bytes do último ID inserido

            while (raf.getFilePointer() < raf.length()) {
                long posicaoRegistro = raf.getFilePointer(); // endereço do registro

                byte validacao = raf.readByte();
                int tamanhoRegistro = raf.readInt();

                if (validacao == 1) {
                    byte[] byteArray = new byte[tamanhoRegistro];
                    raf.readFully(byteArray);

                    Capitulo cap = new Capitulo();
                    cap.fromByteArray(byteArray);

                    int id = cap.getId(); 
                    inserir(id, posicaoRegistro);
                } else {
                    raf.skipBytes(tamanhoRegistro);
                }
            }

            salvarFolhasNoArquivo(caminhoIndice);

        } catch (IOException e) {
            System.err.println("Erro ao ler o arquivo de dados: " + e.getMessage());

        }
    }

    // Salva os IDs e endereços das folhas da árvore no arquivo de índice
    public void salvarFolhasNoArquivo(String caminhoIndice) throws IOException {
        File arquivo = new File(caminhoIndice);
        if (arquivo.exists()) {
            return; 
        }

        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(arquivo))) {
            No atual = encontrarFolhaMaisEsquerda();
            while (atual != null) {
                ArrayList<Integer> ids = atual.getIds();
                ArrayList<Long> enderecos = atual.getEnderecos();
                for (int i = 0; i < ids.size(); i++) {
                    dos.writeInt(ids.get(i));
                    dos.writeLong(enderecos.get(i));
                }
                atual = atual.getPonteiroLado();
            }
        }
    }

    // Retorna a folha mais à esquerda da árvore, usada para percorrer folhas
    private No encontrarFolhaMaisEsquerda() {
        No atual = raiz;
        while (!atual.ehFolha()) {
            atual = atual.getFilhos().get(0);
        }
        return atual;
    }

    // Navega pela árvore até encontrar a folha correta para um ID
    private No encontrarFolha(No noAtual, int id) {
        while (!noAtual.ehFolha()) {
            noAtual = noAtual.proximoNo(id);
        }
        return noAtual;
    }

    // Divide uma folha que sofreu overflow e promove o menor ID da nova folha
    private void dividirFolha(No folha) {
        No novoNo = folha.separaRetornaFolha(ordem);
        int idPromovido = novoNo.getIds().get(0);

        if (folha == raiz) {
            No novaRaiz = new No(false);
            novaRaiz.getIds().add(idPromovido);
            novaRaiz.getFilhos().add(folha);
            novaRaiz.getFilhos().add(novoNo);
            raiz = novaRaiz;
        } else {
            promover(folha, idPromovido, novoNo);
        }
    }

    // Promove um ID para o nó pai e ajusta os ponteiros dos filhos
    private void promover(No filhoAntigo, int idPromovido, No novoFilho) {
        No pai = encontrarPai(raiz, filhoAntigo);
        if (pai == null) {
            return;
        }

        int pos = pai.inserirId(idPromovido);
        pai.getFilhos().add(pos + 1, novoFilho);

        if (pai.verificaOverflow(ordem)) {
            dividirInterno(pai);
        }
    }

    // Divide um nó interno com overflow e promove o ID do meio
    private void dividirInterno(No no) {
        int meio = (ordem - 1) / 2;
        int idPromovido = no.getIds().get(meio);

        No novoNo = new No(false);

        // Copiar IDs e filhos após o meio para o novo nó
        for (int i = meio + 1; i < no.getIds().size(); i++) {
            novoNo.getIds().add(no.getIds().get(i));
        }
        for (int i = meio + 1; i < no.getFilhos().size(); i++) {
            novoNo.getFilhos().add(no.getFilhos().get(i));
        }

        // Remover do nó original
        while (no.getIds().size() > meio) {
            no.getIds().remove(no.getIds().size() - 1);
        }
        while (no.getFilhos().size() > meio + 1) {
            no.getFilhos().remove(no.getFilhos().size() - 1);
        }

        if (no == raiz) {
            No novaRaiz = new No(false);
            novaRaiz.getIds().add(idPromovido);
            novaRaiz.getFilhos().add(no);
            novaRaiz.getFilhos().add(novoNo);
            raiz = novaRaiz;
        } else {
            promover(no, idPromovido, novoNo);
        }
    }

    // Busca o nó pai de um dado nó filho na árvore
    private No encontrarPai(No noAtual, No filhoProcurado) {
        if (noAtual.ehFolha() || noAtual.getFilhos().isEmpty()) {
            return null;
        }

        for (No filho : noAtual.getFilhos()) {
            if (filho == filhoProcurado) {
                return noAtual;
            }
            No pai = encontrarPai(filho, filhoProcurado);
            if (pai != null) {
                return pai;
            }
        }
        return null;
    }

    // Retorna o endereço associado a um ID
    public Long buscar(int id) {
        No atual = encontrarFolha(raiz, id); // usa o caminho correto pelos nós internos
        ArrayList<Integer> ids = atual.getIds();
        ArrayList<Long> enderecos = atual.getEnderecos();
        for (int i = 0; i < ids.size(); i++) {
            if (ids.get(i) == id) {
                return enderecos.get(i);
            }
        }
        return null;
    }

    // Remove um ID e seu endereço da folha
    public void remover(int id) {
        No folha = encontrarFolha(raiz, id);
        ArrayList<Integer> ids = folha.getIds();
        ArrayList<Long> enderecos = folha.getEnderecos();

        for (int i = 0; i < ids.size(); i++) {
            if (ids.get(i) == id) {
                ids.remove(i);
                enderecos.remove(i);
                break;
            }
        }
    }

    // Carrega os pares ID/endereço de um arquivo e recria as folhas da árvore
    public void carregarFolhasDoArquivo(String caminhoIndice) throws IOException {
        boolean carregouAlgumaCoisa;
        try (DataInputStream dis = new DataInputStream(new FileInputStream(caminhoIndice))) {
            No anterior = null;
            carregouAlgumaCoisa = false;
            while (dis.available() > 0) {
                No folha = new No(true);

                // Lê uma sequência de pares id/endereço
                while (dis.available() >= 12 && folha.getIds().size() < ordem - 1) {
                    int id = dis.readInt();
                    long endereco = dis.readLong();

                    folha.getIds().add(id);
                    folha.getEnderecos().add(endereco);

                    System.out.println("Carregado: ID = " + id + ", Endereço = " + endereco);
                    carregouAlgumaCoisa = true;
                }

                if (folha.getIds().isEmpty()) {
                    System.out.println("Folha criada sem IDs, pulando.");
                    continue;
                }

                if (anterior == null) {
                    raiz = folha;
                } else {
                    anterior.setPonteiroLado(folha);
                }

                anterior = folha;
            }
        }

        if (!carregouAlgumaCoisa) {
            System.out.println("Nenhum dado foi carregado do arquivo de índice.");
        } else {
            System.out.println("Folhas carregadas com sucesso.");
        }
    }

    public static class No {

        private final ArrayList<Integer> ids;
        private final ArrayList<Long> enderecos;
        private final ArrayList<No> filhos;
        private No ponteiroLado;
        private final boolean folha;

        // Inicializa um nó, que pode ser folha ou interno
        public No(boolean folha) {
            this.folha = folha;
            ids = new ArrayList<>();
            enderecos = new ArrayList<>();
            filhos = new ArrayList<>();
            ponteiroLado = null;
        }

        // Verifica se o nó é folha
        public boolean ehFolha() {
            return folha;
        }

        // Verifica se o número de IDs excede o limite permitido
        public boolean verificaOverflow(int ordem) {
            return ids.size() >= ordem;
        }

        // Retorna a lista de IDs armazenados no nó
        public ArrayList<Integer> getIds() {
            return ids;
        }

        // Retorna a lista de endereços associados aos IDs (usado em folhas)
        public ArrayList<Long> getEnderecos() {
            return enderecos;
        }

        // Retorna a lista de nós filhos (usado em nós internos)
        public ArrayList<No> getFilhos() {
            return filhos;
        }

        // Retorna o ponteiro para o próximo nó folha (usado para varredura sequencial)
        public No getPonteiroLado() {
            return ponteiroLado;
        }

        // Define o ponteiro para o próximo nó folha na sequência
        public void setPonteiroLado(No ponteiro) {
            this.ponteiroLado = ponteiro;
        }

        // Insere um par ID/endereço na ordem correta dentro do nó folha
        public void inserirIdEnderecoOrdenado(int id, long endereco) {
            for (int i = 0; i < ids.size(); i++) {
                if (id < ids.get(i)) {
                    ids.add(i, id);
                    enderecos.add(i, endereco);
                    return;
                }
            }
            ids.add(id);
            enderecos.add(endereco);
        }

        // Retorna o próximo nó filho com base no ID
        public No proximoNo(int id) {
            int i = 0;
            while (i < ids.size() && id >= ids.get(i)) {
                i++;
            }
            return filhos.get(i);
        }

        // Insere um ID em ordem crescente no nó interno e retorna a posição
        public int inserirId(int id) {
            for (int i = 0; i < ids.size(); i++) {
                if (id < ids.get(i)) {
                    ids.add(i, id);
                    return i;
                }
            }
            ids.add(id);
            return ids.size() - 1;
        }

        // Divide um nó folha em dois e retorna o novo nó folha criado
        public No separaRetornaFolha(int ordem) {
            No novoNo = new No(true);
            int meio = (ordem + 1) / 2;

            for (int i = meio; i < ids.size(); i++) {
                novoNo.ids.add(ids.get(i));
                novoNo.enderecos.add(enderecos.get(i));
            }

            for (int i = ids.size() - 1; i >= meio; i--) {
                ids.remove(i);
                enderecos.remove(i);
            }

            novoNo.ponteiroLado = this.ponteiroLado;
            this.ponteiroLado = novoNo;

            return novoNo;
        }

        // Divide um nó interno em dois e retorna o novo nó criado
        public No separaRetorna(int ordem) {
            No novoNo = new No(false);
            int meio = (ordem - 1) / 2;

            // Copiar IDs após o meio
            for (int i = meio + 1; i < ids.size(); i++) {
                novoNo.ids.add(ids.get(i));
            }

            // Copiar filhos após o meio
            for (int i = meio + 1; i < filhos.size(); i++) {
                novoNo.filhos.add(filhos.get(i));
            }

            // Remover IDs e filhos do nó original
            while (ids.size() > meio) {
                ids.remove(ids.size() - 1);
            }
            while (filhos.size() > meio + 1) {
                filhos.remove(filhos.size() - 1);
            }

            return novoNo;
        }

    }

}
