import java.util.Objects;

/**
 * Implementação de um HashMap do zero, com redimensionamento (resize)
 * e otimizações inspiradas no Java 8+.
 */
public class CustomHashMap<K, V> {

    // --- Classes Internas e Constantes ---

    static class Node<K, V> {
        final int hash;
        final K key;
        V value;
        Node<K, V> next;

        Node(int hash, K key, V value, Node<K, V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }

    static final int DEFAULT_CAPACITY = 16;
    static final float DEFAULT_LOAD_FACTOR = 0.75f;

    // --- Atributos da Classe ---

    transient Node<K, V>[] table; // O array de buckets
    transient int size;           // Número de pares chave-valor
    final float loadFactor;
    int threshold;                // Limite para redimensionar (capacity * loadFactor)

    // --- Construtores ---

    public CustomHashMap() {
        this.loadFactor = DEFAULT_LOAD_FACTOR;
    }

    public CustomHashMap(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0) throw new IllegalArgumentException("Capacidade ilegal");
        if (loadFactor <= 0 || Float.isNaN(loadFactor)) throw new IllegalArgumentException("Fator de carga ilegal");

        this.loadFactor = loadFactor;
        this.threshold = tableSizeFor(initialCapacity);
    }

    // --- Funções Auxiliares de Hash e Índice ---

    /**
     * Espalha os bits do hashCode para evitar colisões.
     * Mistura os 16 bits superiores com os 16 bits inferiores.
     */
    static int hash(Object key) {
        if (key == null) return 0;
        int h = key.hashCode();
        return h ^ (h >>> 16);
    }

    /**
     * Encontra a próxima potência de 2 maior ou igual a 'cap'.
     * Garante que o tamanho do array seja sempre uma potência de 2.
     */
    static int tableSizeFor(int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= Integer.MAX_VALUE) ? Integer.MAX_VALUE : n + 1;
    }

    // --- Operações Principais (API) ---

    public V put(K key, V value) {
        return putVal(hash(key), key, value);
    }

    private V putVal(int hash, K key, V value) {
        Node<K, V>[] tab;
        int n, i;

        // 1. Se a tabela estiver vazia, inicializa (resize inicial)
        if ((tab = table) == null || (n = tab.length) == 0) {
            tab = resize();
            n = tab.length;
        }

        // 2. Calcula o índice do bucket: (n - 1) & hash é equivalente a hash % n, mas mais rápido
        i = (n - 1) & hash;

        V oldValue = null;

        // 3. Se o bucket estiver vazio, insere o primeiro nó
        if (tab[i] == null) {
            tab[i] = new Node<>(hash, key, value, null);
        } else {
            // 4. Colisão! Percorre a lista encadeada
            Node<K, V> e = tab[i];
            K k;

            // Se a chave do primeiro nó for igual, guarda para sobrescrever
            if (e.hash == hash && ((k = e.key) == key || (key != null && key.equals(k)))) {
                oldValue = e.value;
                e.value = value;
            } else {
                // Percorre o resto da lista
                while (true) {
                    Node<K, V> next = e.next;
                    if (next == null) {
                        e.next = new Node<>(hash, key, value, null);
                        break;
                    }
                    if (next.hash == hash && ((k = next.key) == key || (key != null && key.equals(k)))) {
                        oldValue = next.value;
                        next.value = value;
                        break;
                    }
                    e = next;
                }
            }
        }

        // 5. Atualiza o tamanho e verifica se precisa redimensionar
        if (++size > threshold) {
            resize();
        }

        return oldValue;
    }

    public V get(Object key) {
        Node<K, V> e = getNode(hash(key), key);
        return e == null ? null : e.value;
    }

    private Node<K, V> getNode(int hash, Object key) {
        Node<K, V>[] tab = table;
        if (tab == null || tab.length == 0) return null;

        int i = (tab.length - 1) & hash;
        Node<K, V> first = tab[i];

        if (first == null) return null;

        // Verifica o primeiro nó
        if (first.hash == hash && ((first.key == key) || (key != null && key.equals(first.key)))) {
            return first;
        }

        // Percorre a lista encadeada
        Node<K, V> e = first.next;
        while (e != null) {
            if (e.hash == hash && ((e.key == key) || (key != null && key.equals(e.key)))) {
                return e;
            }
            e = e.next;
        }
        return null;
    }

    public V remove(Object key) {
        Node<K, V>[] tab = table;
        if (tab == null || tab.length == 0) return null;

        int i = (tab.length - 1) & hash(key);
        Node<K, V> e = tab[i];

        if (e == null) return null;

        Node<K, V> prev = null;
        V oldValue = null;

        // Percorre a lista para encontrar e desvincular o nó
        do {
            if (e.hash == hash(key) && ((e.key == key) || (key != null && key.equals(e.key)))) {
                oldValue = e.value;
                if (prev == null) {
                    tab[i] = e.next; // Remove o primeiro nó da lista
                } else {
                    prev.next = e.next; // Remove nó do meio/fim da lista
                }
                size--;
                return oldValue;
            }
            prev = e;
            e = e.next;
        } while (e != null);

        return null;
    }

    // --- Redimensionamento (Resize / Rehash) ---

    final Node<K, V>[] resize() {
        Node<K, V>[] oldTab = table;
        int oldCap = (oldTab == null) ? 0 : oldTab.length;
        int oldThr = threshold;
        int newCap, newThr = 0;

        // 1. Calcula a nova capacidade e o novo limiar (threshold)
        if (oldCap > 0) {
            if (oldCap >= Integer.MAX_VALUE) {
                threshold = Integer.MAX_VALUE;
                return oldTab;
            } else if ((newCap = oldCap << 1) < Integer.MAX_VALUE && oldCap >= DEFAULT_CAPACITY) {
                newThr = oldThr << 1; // Dobro do threshold antigo
            }
        } else if (oldThr > 0) {
            newCap = oldThr;
        } else {
            newCap = DEFAULT_CAPACITY;
            newThr = (int) (DEFAULT_LOAD_FACTOR * DEFAULT_CAPACITY);
        }

        if (newThr == 0) {
            float ft = (float) newCap * loadFactor;
            newThr = (newCap < Integer.MAX_VALUE && ft < Integer.MAX_VALUE) ? (int) ft : Integer.MAX_VALUE;
        }

        threshold = newThr;

        // 2. Cria o novo array
        @SuppressWarnings("unchecked")
        Node<K, V>[] newTab = (Node<K, V>[]) new Node[newCap];
        table = newTab;

        // 3. Move os nós do array antigo para o novo (Rehash)
        if (oldTab != null) {
            for (int j = 0; j < oldCap; ++j) {
                Node<K, V> e = oldTab[j];
                if (e != null) {
                    oldTab[j] = null; // Ajuda o Garbage Collector

                    if (e.next == null) {
                        // Nó sozinho, apenas recalcula o índice
                        newTab[(e.hash & (newCap - 1))] = e;
                    } else {
                        // Otimização Java 8: Divide a lista em duas sem recalcular o hash.
                        // Se o bit correspondente à nova capacidade for 0, fica no mesmo índice.
                        // Se for 1, vai para o índice + oldCap.
                        Node<K, V> loHead = null, loTail = null; // Lista do índice original
                        Node<K, V> hiHead = null, hiTail = null; // Lista do novo índice

                        do {
                            if ((e.hash & oldCap) == 0) {
                                if (loTail == null) loHead = e;
                                else loTail.next = e;
                                loTail = e;
                            } else {
                                if (hiTail == null) hiHead = e;
                                else hiTail.next = e;
                                hiTail = e;
                            }
                            e = e.next;
                        } while (e != null);

                        if (loTail != null) {
                            loTail.next = null;
                            newTab[j] = loHead;
                        }
                        if (hiTail != null) {
                            hiTail.next = null;
                            newTab[j + oldCap] = hiHead;
                        }
                    }
                }
            }
        }
        return newTab;
    }

    // --- Métodos Utilitários ---

    public int size() { return size; }
    public boolean isEmpty() { return size == 0; }
    public void clear() {
        Node<K, V>[] tab = table;
        if (tab != null && size > 0) {
            size = 0;
            for (int i = 0; i < tab.length; ++i) tab[i] = null;
        }
    }

    // --- Teste / Exemplo de Uso ---
    public static void main(String[] args) {
        CustomHashMap<String, Integer> map = new CustomHashMap<>();

        System.out.println("--- Inserindo dados ---");
        map.put("Maçã", 10);
        map.put("Banana", 20);
        map.put("Uva", 30);

        System.out.println("Tamanho: " + map.size()); // 3

        System.out.println("\n--- Buscando dados ---");
        System.out.println("Banana: " + map.get("Banana")); // 20
        System.out.println("Melancia: " + map.get("Melancia")); // null

        System.out.println("\n--- Atualizando dado ---");
        map.put("Maçã", 15); // Substitui o valor
        System.out.println("Maçã atualizada: " + map.get("Maçã")); // 15

        System.out.println("\n--- Removendo dado ---");
        map.remove("Uva");
        System.out.println("Tamanho após remover: " + map.size()); // 2
        System.out.println("Uva: " + map.get("Uva")); // null

        System.out.println("\n--- Forçando Redimensionamento (Resize) ---");
        // Inserindo muitos elementos para estourar o threshold e forçar o resize
        for (int i = 0; i < 20; i++) {
            map.put("Fruta" + i, i);
        }
        System.out.println("Tamanho final: " + map.size()); // 22
    }
}
