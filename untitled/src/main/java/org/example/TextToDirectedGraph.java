package org.example;

import java.io.*;
import java.util.*;

public class TextToDirectedGraph {
    private static Map<String, Map<String, Integer>> directedGraph = new HashMap<>();
    public static void main(String[] args) throws IOException {
        String filePath = "text.txt"; // 设置默认文件路径
        if (args.length > 0) {
            filePath = args[0];
        }
        // 读取文本文件并创建有向图
        try {
            directedGraph = createDirectedGraph(filePath);
            // 打印有向图
            printDirectedGraph(directedGraph);
            String dotFormat = showDirectedGraph(directedGraph);
            // 调用 GraphVizHelper 生成图形文件
            GraphVizHelper.createDotGraph(dotFormat, "DirectedGraph");
        } catch (IOException e) {
            System.err.println("无法读取文件: " + e.getMessage());
        }
        while (true) {
            System.out.println("\n请选择一个功能：");
            System.out.println("1. 查询桥接词");
            System.out.println("2. 生成包含桥接词的新文本");
            System.out.println("3. 计算最短路径");
            System.out.println("4. 随机遍历图");
            System.out.println("5. 退出");
            System.out.print("请输入选项 (1-5): ");
            System.out.println("test git");
            System.out.println("test B2");
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String option = reader.readLine().trim();

            switch (option) {
                case "1":
                    System.out.print("请输入word1: ");
                    String word1 = reader.readLine().trim();
                    System.out.print("请输入word2: ");
                    String word2 = reader.readLine().trim();
                    queryBridgeWords(word1, word2);
                    break;
                case "2":
                    System.out.print("请输入新文本: ");
                    String newText = reader.readLine().trim();
                    generateNewText(newText);
                    break;
                case "3":
                    System.out.print("请输入word1: ");
                    word1 = reader.readLine().trim();
                    System.out.print("请输入word2: ");
                    word2 = reader.readLine().trim();
                    calcShortestPath(word1, word2);
                    break;
                case "4":
                    String randomTraversalFile = "random_traversal.txt";
                    randomWalk(randomTraversalFile);
                    break;
                case "5":
                    System.out.println("程序结束");
                    return;
                default:
                    System.out.println("无效选项，请重新选择");
            }
        }
    }

    public static Map<String, Map<String, Integer>> createDirectedGraph(String filePath) throws IOException {
        Map<String, Map<String, Integer>> directedGraph = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            StringBuilder text = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                // 将换行和标点符号替换为空格，并将所有文本拼接到一个StringBuilder中
                text.append(line.replaceAll("[^a-zA-Z ]", " ").toLowerCase()).append(" ");
            }
            // 按空格拆分单词
            String[] words = text.toString().split("\\s+");

            // 构建有向图
            for (int i = 0; i < words.length - 1; i++) {
                String currentWord = words[i];
                String nextWord = words[i + 1];
                if (!currentWord.isEmpty() && !nextWord.isEmpty()) {
                    directedGraph.putIfAbsent(currentWord, new HashMap<>());
                    directedGraph.get(currentWord).put(nextWord, directedGraph.get(currentWord).getOrDefault(nextWord, 0) + 1);
                }
            }
        }
        return directedGraph;
    }

    public static void printDirectedGraph(Map<String, Map<String, Integer>> directedGraph) {
        for (String source : directedGraph.keySet()) {
            System.out.print(source + " -> ");
            Map<String, Integer> neighbors = directedGraph.get(source);
            for (Map.Entry<String, Integer> entry : neighbors.entrySet()) {
                String target = entry.getKey();
                int weight = entry.getValue();
                for (int i = 0; i < weight; i++) {
                    System.out.print(target + " ");
                }
            }
            System.out.println();
        }
    }

    public static String showDirectedGraph(Map<String, Map<String, Integer>> directedGraph) {
        StringBuilder dotFormat = new StringBuilder();
        for (String source : directedGraph.keySet()) {
            Map<String, Integer> neighbors = directedGraph.get(source);
            for (Map.Entry<String, Integer> entry : neighbors.entrySet()) {
                String target = entry.getKey();
                int weight = entry.getValue();
                dotFormat.append(source).append(" -> ").append(target).append(" [label=\"").append(weight).append("\"];\n");
            }
        }
        return dotFormat.toString();
    }

    public static void queryBridgeWords(String word1, String word2) {
        Set<String> bridgeWords = findBridgeWords(word1, word2);
        if (bridgeWords.isEmpty()) {
            System.out.println("No bridge words from " + word1 + " to " + word2 + "!");
        } else {
            System.out.print("The bridge words from " + word1 + " to " + word2 + " are: ");
            int count = 0;
            int size = bridgeWords.size();
            for (String bridgeWord : bridgeWords) {
                System.out.print(bridgeWord);
                if (count < size - 1) {
                    System.out.print(", ");
                }
                count++;
            }
            System.out.println();
        }
    }

    public static Set<String> findBridgeWords(String word1, String word2) {
        Set<String> bridgeWords = new HashSet<>();
        if (!directedGraph.containsKey(word1) || !directedGraph.containsKey(word2)) {
            // 返回空集合
            return bridgeWords;
        }
        Map<String, Integer> neighborsOfWord1 = directedGraph.get(word1);

        for (Map.Entry<String, Integer> entry : neighborsOfWord1.entrySet()) {
            String bridgeWord = entry.getKey();
            if (directedGraph.containsKey(bridgeWord) && directedGraph.get(bridgeWord).containsKey(word2)) {
                bridgeWords.add(bridgeWord);
            }
        }
        return bridgeWords;
    }

    public static void generateNewText(String newText) {
        String[] words = newText.split("\\s+");
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < words.length - 1; i++) {
            String word1 = words[i];
            String word2 = words[i + 1];
            result.append(word1).append(" ");

            if (directedGraph.containsKey(word1) && directedGraph.containsKey(word2)) {
                Set<String> bridgeWords = findBridgeWords(word1, word2);
                if (!bridgeWords.isEmpty()) {
                    // 选择一个随机的桥接词插入到两个单词之间
                    List<String> bridgeWordsList = new ArrayList<>(bridgeWords);
                    String bridgeWord = bridgeWordsList.get(new Random().nextInt(bridgeWordsList.size()));
                    result.append(bridgeWord).append(" ");
                }
            }
        }
        result.append(words[words.length - 1]);
        System.out.println("生成的新文本为: " + result.toString());
    }

    public static void calcShortestPath(String word1, String word2) {
        if (!directedGraph.containsKey(word1) || !directedGraph.containsKey(word2)) {
            System.out.println("图中没有这个单词！");
            return;
        }
        Map<String, Integer> distances = new HashMap<>();
        Map<String, String> previous = new HashMap<>(); // 存储最短路径
        PriorityQueue<String> queue = new PriorityQueue<>(Comparator.comparingInt(distances::get));

        for (String vertex : directedGraph.keySet()) {
            distances.put(vertex, Integer.MAX_VALUE);
            previous.put(vertex, null);
        }
        distances.put(word1, 0);
        queue.add(word1);
        while (!queue.isEmpty()) {
            String current = queue.poll(); // 用于获取距离word1最短的当前节点
            if (current.equals(word2)) {
                break;
            }
            Integer distance = distances.get(current);
            if (distance == null || distance == Integer.MAX_VALUE) {
                continue;
            }
            Map<String, Integer> neighbors = directedGraph.get(current); // 找这个节点的邻居
            for (Map.Entry<String, Integer> neighbor : neighbors.entrySet()) {
                String neighborVertex = neighbor.getKey();
                int alternate = distances.get(current) + neighbor.getValue();
                if (alternate < distances.get(neighborVertex)) {
                    distances.put(neighborVertex, alternate);
                    previous.put(neighborVertex, current);
                    queue.remove(neighborVertex);
                    queue.add(neighborVertex); // 重新存保证优先级
                }
            }
        }
        List<String> path = new ArrayList<>();
        String current = word2;
        while (previous.get(current) != null) {
            path.add(current);
            current = previous.get(current);
        }
        path.add(word1);
        Collections.reverse(path);

        if (path.size() == 1 || !path.get(0).equals(word1) || !path.get(path.size() - 1).equals(word2)) {
            System.out.println("无法找到从 " + word1 + " 到 " + word2 + " 的最短路径！");
        } else {
            System.out.print("最短路径为: ");
            for (String vertex : path) {
                System.out.print(vertex + " ");
            }
            System.out.println();
            System.out.println("路径长度为: " + distances.get(word2));
            highlightPathOnGraph(path);
        }
    }

    private static void highlightPathOnGraph(List<String> path) {
        StringBuilder dotFormat = new StringBuilder();
        for (Map.Entry<String, Map<String, Integer>> entry : directedGraph.entrySet()) {
            String node = entry.getKey();
            for (Map.Entry<String, Integer> edge : entry.getValue().entrySet()) {
                String target = edge.getKey();
                int weight = edge.getValue();
                dotFormat.append(String.format("  \"%s\" -> \"%s\" [label=\"%d\"];\n", node, target, weight));
            }
        }
        for (int i = 0; i < path.size() - 1; i++) {
            String from = path.get(i);
            String to = path.get(i + 1);
            dotFormat.append(String.format("  \"%s\" -> \"%s\" [color=green, penwidth=2.0];\n", from, to));
        }
        String fileName = "DirectedGraph";
        GraphVizHelper.createDotGraph(dotFormat.toString(), fileName);
        System.out.println("图已生成，并在文件 " + fileName + ".png 中高亮显示最短路径");
    }
    public static void randomWalk(String randomTraversalFile) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(randomTraversalFile))) {
            Random random = new Random();
            List<String> vertices = new ArrayList<>(directedGraph.keySet());
            String current = vertices.get(random.nextInt(vertices.size()));

            Set<String> visited = new HashSet<>();
            visited.add(current);
            while (true) {
                writer.write(current + " ");
                visited.add(current);
                Map<String, Integer> neighbors = directedGraph.get(current);

                if (neighbors == null || neighbors.isEmpty()) {
                    break;
                }
                List<String> availableNeighbors = new ArrayList<>();
                for (Map.Entry<String, Integer> neighbor : neighbors.entrySet()) {
                    String neighborVertex = neighbor.getKey();
                    if (!visited.contains(neighborVertex)) {
                        availableNeighbors.add(neighborVertex);
                    }
                }
                if (availableNeighbors.isEmpty()) {
                    break;
                }
                current = availableNeighbors.get(random.nextInt(availableNeighbors.size()));
            }
        } catch (IOException e) {
            System.err.println("写入文件时出错: " + e.getMessage());
        }
    }

}
