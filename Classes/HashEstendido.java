import java.io.*;
import java.util.*;

public class HashEstendido {

    private static final int TAM_BUCKET = 4;
    private int profundidadeGlobal; 
    private List<Bucket> diretorio; 
    private final String arquivoIndice = "Indices/capitulosIndiceHash.db"; 
    private boolean emConstrucao = false; 

    // inicializa a hash com profundidade 1 e carrega dados do arquivo
    public HashEstendido() {
        this.profundidadeGlobal = 1;
        this.diretorio = new ArrayList<>();
        diretorio.add(new Bucket(1));
        diretorio.add(new Bucket(1));
        carregar();
    }

    // Calcula o hash aplicando máscara conforme a profundidade global
    private int hash(int id) {
        return id & ((1 << profundidadeGlobal) - 1);
    }

    // Insere um novo registro na hash, dividindo bucket se necessário
    public void inserir(int id, long posicao) {
        int h = hash(id);
        Bucket bucket = diretorio.get(h);

        if (!bucket.estaCheio()) {
            bucket.adicionar(new RegistroIndice(id, posicao));
        } else {
            try {
                dividirBucket(h);
                inserir(id, posicao);
            } catch (IOException e) {
                System.err.println("Erro ao dividir bucket: " + e.getMessage());
            }
        }

        if (!emConstrucao) {
            salvar(); 
        }
    }

    // Busca pela posição de um registro com o id fornecido
    public Long buscar(int id) {
        int h = hash(id);
        Bucket bucket = diretorio.get(h);
        for (RegistroIndice r : bucket.registros) {
            if (r.id == id) {
                return r.posicao;
            }
        }
        return null;
    }

    // Remove um registro do bucket correspondente ao id
    public void remover(int id) {
        int h = hash(id);
        Bucket bucket = diretorio.get(h);
        bucket.registros.removeIf(r -> r.id == id);
        salvar();
    }

    // Constrói o índice
    public void construirHashDoArquivo(String caminhoArquivo) {
        File f = new File(arquivoIndice);
        if (f.exists()) {
            carregar();
            return;
        }

        emConstrucao = true;

        try (RandomAccessFile raf = new RandomAccessFile(caminhoArquivo, "r")) {
            raf.seek(4);
            while (raf.getFilePointer() < raf.length()) {
                long posicaoRegistro = raf.getFilePointer();
                byte validacao = raf.readByte();
                int tamanhoRegistro = raf.readInt();

                if (validacao == 1) {
                    int id = raf.readInt();
                    inserir(id, posicaoRegistro);
                }

                raf.seek(posicaoRegistro + 5 + tamanhoRegistro);
            }
            salvar();
        } catch (IOException e) {
            System.err.println("Erro ao construir índice: " + e.getMessage());
        }

        emConstrucao = false;
    }

    // Divide um bucket que está cheio e redistribui seus registros
    private void dividirBucket(int indice) throws IOException {
        Bucket bucketAntigo = diretorio.get(indice);
        int novaProfundidade = bucketAntigo.profundidadeLocal + 1;

        if (novaProfundidade > profundidadeGlobal) {
            duplicarDiretorio();
        }

        Bucket novoBucket = new Bucket(novaProfundidade);
        List<RegistroIndice> antigos = new ArrayList<>(bucketAntigo.registros);
        bucketAntigo.registros.clear();
        bucketAntigo.profundidadeLocal = novaProfundidade;

        for (int i = 0; i < diretorio.size(); i++) {
            if (diretorio.get(i) == bucketAntigo) {
                if ((i & (1 << (novaProfundidade - 1))) != 0) {
                    diretorio.set(i, novoBucket);
                }
            }
        }

        for (RegistroIndice r : antigos) {
            inserirDireto(r);
        }
    }

    // Insere diretamente um registro no bucket sem reprocessar
    private void inserirDireto(RegistroIndice r) {
        int h = hash(r.id);
        Bucket bucket = diretorio.get(h);
        bucket.adicionar(r);
    }

    // Duplica o tamanho do diretório, aumentando a profundidade global
    private void duplicarDiretorio() {
        int tam = diretorio.size();
        for (int i = 0; i < tam; i++) {
            diretorio.add(diretorio.get(i));
        }
        profundidadeGlobal++;
    }

    // Salva o índice completo (diretório e buckets) em arquivo
    private void salvar() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(arquivoIndice))) {
            oos.writeInt(profundidadeGlobal);
            oos.writeInt(diretorio.size());
            for (Bucket b : diretorio) {
                oos.writeObject(b);
            }
        } catch (IOException e) {
            System.err.println("Erro ao salvar índice: " + e.getMessage());
        }
    }

    // Carrega o índice do arquivo serializado, se existir
    private void carregar() {
        File f = new File(arquivoIndice);
        if (!f.exists()) return;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            profundidadeGlobal = ois.readInt();
            int tam = ois.readInt();
            diretorio = new ArrayList<>();
            for (int i = 0; i < tam; i++) {
                diretorio.add((Bucket) ois.readObject());
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Erro ao carregar índice: " + e.getMessage());
        }
    }

    // Classe auxiliar para representar um par (id, posição) no índice
    static class RegistroIndice implements Serializable {
        int id;
        long posicao;

        public RegistroIndice(int id, long posicao) {
            this.id = id;
            this.posicao = posicao;
        }
    }

    // Classe que representa um bucket com profundidade local e lista de registros
    static class Bucket implements Serializable {
        int profundidadeLocal;
        List<RegistroIndice> registros;

        public Bucket(int profundidade) {
            this.profundidadeLocal = profundidade;
            this.registros = new ArrayList<>();
        }

        // Verifica se o bucket está cheio
        public boolean estaCheio() {
            return registros.size() >= TAM_BUCKET;
        }

        // Adiciona um registro ao bucket
        public void adicionar(RegistroIndice r) {
            registros.add(r);
        }
    }
}
