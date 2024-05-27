package org.example;

import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxCellRenderer;
import org.jgrapht.Graph;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultDirectedGraph;
import java.awt.Color;
import javax.swing.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.io.FileWriter;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;


public class DirectedGraph {
    public Graph<String, DefaultEdge> graph;

    public Map<String, Map<String, List<String>>> bridgeWordsMap;
    public DirectedGraph(String text) {
        this.graph = new DefaultDirectedGraph<>(DefaultEdge.class);

        String[] words = text.split("\\s+");
        for (int i = 0; i < words.length - 1; i++) {
            //换成小写
            String currentWord = words[i].toLowerCase();
            String nextWord = words[i + 1].toLowerCase();
            // 添加顶点和边
            this.graph.addVertex(currentWord);
            this.graph.addVertex(nextWord);
            this.graph.addEdge(currentWord, nextWord);
        }
    }
    public void saveGraphImage(Graph<String, DefaultEdge> graph) {
        // 创建一个 JGraphX 适配器，将 JGraphT 图转换为 JGraphX 图
        JGraphXAdapter<String, DefaultEdge> graphAdapter = new JGraphXAdapter<>(graph);

        // 使用圆形布局来布置图形
        mxCircleLayout layout = new mxCircleLayout(graphAdapter);
        layout.execute(graphAdapter.getDefaultParent());

        // 创建一个 Swing JFrame 来容纳 JGraphX 组件
        JFrame frame = new JFrame();
        mxGraphComponent graphComponent = new mxGraphComponent(graphAdapter);
        frame.getContentPane().add(graphComponent);

        // 设置 JFrame 的大小和可见性
        frame.setSize(800, 600);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 将图像渲染为 BufferedImage
        BufferedImage image = mxCellRenderer.createBufferedImage(graphAdapter, null, 2, Color.WHITE, true, null);

        // 将图像保存到文件
        File outputFile = new File("files/graph_image.png");
        try {
            ImageIO.write(image, "PNG", outputFile);
            System.out.println("Graph image saved to: " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error saving graph image: " + e.getMessage());
        }
    }
    // 添加顶点
    public void addVertex(String vertex) {
        graph.addVertex(vertex);
    }

    // 添加一条从顶点 v 到顶点 w 的边
    public void addEdge(String v, String w) {
        graph.addVertex(v);
        graph.addVertex(w);
        graph.addEdge(v, w);
    }

    // 获取图的顶点数量
    public int getVertexCount() {
        return graph.vertexSet().size();
    }

    // 获取顶点 v 的邻接顶点列表
    private static List<String> getAdjVertices(DirectedGraph graph, String vertex) {
        Set<DefaultEdge> edges = graph.graph.outgoingEdgesOf(vertex);
        List<String> adjVertices = new ArrayList<>();
        for (DefaultEdge edge : edges) {
            adjVertices.add(graph.graph.getEdgeTarget(edge));
        }
        return adjVertices;
    }

    // 打印图的邻接表表示
    public void printGraph() {
        for (String vertex : graph.vertexSet()) {
            System.out.print("顶点 " + vertex + " 的邻接列表: ");
            List<String> adjVertices = getAdjVertices(this,vertex);
            for (String adjVertex : adjVertices) {
                System.out.print(adjVertex + " ");
            }
            System.out.println();
        }
    }

    public static String readFileAsString(String filePath) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(filePath));
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static String processString(String text) {
        // 替换换行符、回车符、标点符号为空格
        String processedText = text.replaceAll("[\\n\\r\\p{Punct}]", " ");

        // 忽略非字母字符
        StringBuilder result = new StringBuilder();
        for (char c : processedText.toCharArray()) {
            if (Character.isLetter(c) || c == ' ') {
                result.append(c);
            }
        }
        return result.toString();
    }

    public static void visualizeGraph(Graph<String, DefaultEdge> graph) {
// 创建一个 JGraphX 适配器，将 JGraphT 图转换为 JGraphX 图
        JGraphXAdapter<String, DefaultEdge> graphAdapter = new JGraphXAdapter<>(graph);

// 创建一个 Swing JFrame 来容纳 JGraphX 组件
        JFrame frame = new JFrame();
        frame.getContentPane().add(new mxGraphComponent(graphAdapter));

// 使用圆形布局来布置图形
        mxCircleLayout layout = new mxCircleLayout(graphAdapter);
        layout.execute(graphAdapter.getDefaultParent());

// 设置 JFrame 的大小和可见性
        frame.setSize(800, 600);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    // 静态方法查找桥接词
    public static List<String> findBridgeWords(DirectedGraph graph, String word1, String word2) {
        word1 = word1.toLowerCase();
        word2 = word2.toLowerCase();
        if (!graph.graph.containsVertex(word1) || !graph.graph.containsVertex(word2)) {
            return null; // 表示word1或word2不在图中
        }

        List<String> bridgeWords = new ArrayList<>();
        List<String> adjVertices1 = graph.getAdjVertices(word1);
        for (String adjVertex1 : adjVertices1) {
            List<String> adjVertices2 = graph.getAdjVertices(adjVertex1);
            if (adjVertices2.contains(word2)) {
                bridgeWords.add(adjVertex1);
            }
        }
        return bridgeWords;
    }


    private List<String> getAdjVertices(String vertex) {
        Set<DefaultEdge> edges = graph.outgoingEdgesOf(vertex);
        List<String> adjVertices = new ArrayList<>();
        for (DefaultEdge edge : edges) {
            adjVertices.add(graph.getEdgeTarget(edge));
        }
        return adjVertices;
    }

    private static boolean DFS(DirectedGraph graph, Stack<String> stack, Set<String> visited, List<String> bridgeWords, String word2, int count) {
//        System.out.println("debug:stack=="+stack.toString());
        if(stack.empty()){
            return false;
        }

        String current = stack.pop();
        if(visited.contains(current) && !current.equals(word2)){
            return bridgeWords.contains(current);
        }
        visited.add(current);

        if(current.equals(word2)){
            //                System.out.println("debug:count == 1");
            return count != 1;
        }
        count++;


        for (String vertex:getAdjVertices(graph, current)) {
            stack.push(vertex);
//            System.out.println("debug:stack=="+stack.toString());
        }

        if(DFS(graph,stack,visited,bridgeWords,word2,count)){
            bridgeWords.add(current);
            return true;
        }

        return false;
    }

    public Map<String, Map<String, List<String>>> getAllBridgeWords(DirectedGraph graph) {
        Map<String, Map<String, List<String>>> bridgeWordsMap = new HashMap<>();

        Set<String> vertices = graph.graph.vertexSet();
        for (String word1 : vertices) {
            Map<String, List<String>> bridgeWordsForWord1 = new HashMap<>();
            for (String word2 : vertices) {
                if (!word1.equals(word2)) {
                    List<String> bridgeWords = findBridgeWords(graph, word1, word2);
                    if (bridgeWords != null) {
                        bridgeWordsForWord1.put(word2, bridgeWords);
                    }
                }
            }
            if (!bridgeWordsForWord1.isEmpty()) {
                bridgeWordsMap.put(word1, bridgeWordsForWord1);
            }
        }

//        System.out.println("debug:bridgeWordsMap=="+bridgeWordsMap.toString());
        this.bridgeWordsMap = bridgeWordsMap;
        return bridgeWordsMap;
    }

    enum queryResult{
        NO_WORD1,
        NO_WORD2,
        SUCCESS,
        NOT_FOUND

    }

    public static queryResult queryBridgeWordsDetail(Map<String, Map<String, List<String>>> bridgeWordsMap, String word1, String word2, List<String> word1toword2List){
        word1 = word1.toLowerCase();
        word2 = word2.toLowerCase();
        Map<String, List<String>> word1bridgeWords = bridgeWordsMap.get(word1);
        if(word1bridgeWords == null){
//            System.out.println("No " + word1  + " in the graph!");
            return DirectedGraph.queryResult.NO_WORD1;
        }

        List<String> foundList = word1bridgeWords.get(word2);
        if(foundList == null){
//            System.out.println("No " + word2  + " in the graph!");
            return DirectedGraph.queryResult.NO_WORD2;
        }

        if(foundList.isEmpty()){
//            System.out.println("No bridge words from " + word1 + " to " + word2 + "!");
            return DirectedGraph.queryResult.NOT_FOUND;
        }

        word1toword2List.clear();
        word1toword2List.addAll(foundList);
//        System.out.println("debug:word1toword2List=="+word1toword2List.toString());
//        System.out.println("The bridge words from " + word1 + " to " + word2 + " are: " + String.join(", ", word1toword2List));
        return DirectedGraph.queryResult.SUCCESS;
    }

    public String generateNewText(String inputText){
        String[] words = inputText.split("\\s+");

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.length - 1; i++) {
            String currentWord = words[i];
            String nextWord = words[i + 1];
//            System.out.println("debug:round "+i);
//            System.out.println("debug:currentWord=="+currentWord);
//            System.out.println("debug:nextWord=="+nextWord);
            sb.append(currentWord).append(" ");
//            System.out.println("debug:newText=="+sb.toString()+"\n");
            List<String> word1toword2List = new ArrayList<>();
            DirectedGraph.queryResult qr = queryBridgeWordsDetail(this.bridgeWordsMap, currentWord, nextWord, word1toword2List);
//            System.out.println("debug:qr = "+qr);
//            System.out.println("debug:"+(qr == queryResult.SUCCESS));
            if(qr == DirectedGraph.queryResult.SUCCESS){

//                int index = (int) (Math.random()* word1toword2List.size());
//                System.out.println("debug:index=="+index+"\n");
                Random random = new Random(System.currentTimeMillis());
                int index = random.nextInt(word1toword2List.size());
                sb.append(word1toword2List.get(index)).append(" ");
            }
        }
        sb.append(words[words.length - 1]);
        return sb.toString();
    }

    public String queryBridgeWords(String word1, String word2) {
        word1 = word1.toLowerCase();
        word2 = word2.toLowerCase();
        if (!graph.containsVertex(word1) || !graph.containsVertex(word2)) {
            return "No " + word1 + " or " + word2 + " in the graph!";
        }

        List<String> bridgeWords = findBridgeWords(this, word1, word2);
        if (bridgeWords == null || bridgeWords.isEmpty()) {
            return "No bridge words from " + word1 + " to " + word2 + "!";
        }

        return "The bridge words from " + word1 + " to " + word2 + " are: " + String.join(", ", bridgeWords) + ".";
    }


    // 计算最短路径并返回字符串形式的路径和路径长度（忽略大小写）
    // 计算所有最短路径并返回字符串形式的路径和路径长度（忽略大小写）
    public String calcShortestPath(String word1, String word2) {
        List<List<String>> shortestPaths = new ArrayList<>();
        word1 = word1.toLowerCase();
        word2 = word2.toLowerCase();

        // 检查是否存在相应的顶点
        if (!isVertexExists(word1) || !isVertexExists(word2)) {
            return "One or both of the words are not in the graph.";
        }

        Queue<List<String>> queue = new LinkedList<>();
        queue.add(new ArrayList<>(Arrays.asList(word1)));

        int shortestPathLength = Integer.MAX_VALUE;

        while (!queue.isEmpty()) {
            List<String> currentPath = queue.poll();
            String currentWord = currentPath.get(currentPath.size() - 1);

            // 如果当前路径的长度大于已知的最短路径长度，停止搜索
            if (currentPath.size() > shortestPathLength) {
                break;
            }

            if (currentWord.equals(word2)) {
                // 如果找到一个新的最短路径，清空之前的最短路径列表
                if (currentPath.size() < shortestPathLength) {
                    shortestPathLength = currentPath.size();
                    shortestPaths.clear();
                }
                shortestPaths.add(currentPath);
            }

            // 继续搜索
            for (String neighbor : getAdjVertices(currentWord)) {
                if (!currentPath.contains(neighbor)) {
                    List<String> newPath = new ArrayList<>(currentPath);
                    newPath.add(neighbor);
                    queue.add(newPath);
                }
            }
        }

        if (shortestPaths.isEmpty()) {
            return "There is no path between the two words.";
        }

        // 输出所有最短路径
        StringBuilder result = new StringBuilder();
        for (List<String> path : shortestPaths) {
            result.append("Shortest path: ").append(String.join(" -> ", path)).append("\n");
        }

        // 计算路径长度
        int pathLength = shortestPaths.get(0).size() - 1;
        result.append("Path length: ").append(pathLength).append(" edges.");
        return result.toString();
    }




    // 检查是否存在顶点（忽略大小写）
    private boolean isVertexExists(String vertex) {
        for (String v : graph.vertexSet()) {
            if (v.equalsIgnoreCase(vertex)) {
                return true;
            }
        }
        return false;
    }
    // 随机游走方法
    public void randomWalk() {
        Set<String> vertices = graph.vertexSet();
        if (vertices.isEmpty()) {
            System.out.println("Graph is empty.");
            return;
        }

        // 从图中随机选择一个起点
        Random random = new Random();
        int randomIndex = random.nextInt(vertices.size());
        String currentVertex = (String) vertices.toArray()[randomIndex];
        System.out.println("Starting at vertex: " + currentVertex);

        Set<DefaultEdge> visitedEdges = new HashSet<>();
        StringBuilder path = new StringBuilder(currentVertex);

        // 标志变量，表示是否停止遍历
        boolean stopTraversal = false;
        Scanner scanner = new Scanner(System.in);

        while (!stopTraversal) {
            // 获取当前节点的所有出边
            Set<DefaultEdge> outgoingEdges = graph.outgoingEdgesOf(currentVertex);
            if (outgoingEdges.isEmpty()) {
                System.out.println("No outgoing edges from current vertex. Stopping walk.");
                break;
            }

            // 从出边中随机选择一条
            int edgeIndex = random.nextInt(outgoingEdges.size());
            DefaultEdge selectedEdge = (DefaultEdge) outgoingEdges.toArray()[edgeIndex];
            String targetVertex = graph.getEdgeTarget(selectedEdge);

            // 检查是否重复边
            if (visitedEdges.contains(selectedEdge)) {
                System.out.println("Encountered a repeated edge. Stopping walk.");
                break;
            }

            // 添加边到访问记录
            visitedEdges.add(selectedEdge);
            currentVertex = targetVertex;
            path.append(" ").append(currentVertex);

            // 打印当前路径
            System.out.println("Current path: " + path.toString());

            // 检查是否用户要停止遍历
            System.out.println("Press 'q' to stop traversal, or any other key to continue.");
            if (scanner.hasNextLine()) {
                String userInput = scanner.nextLine();
                if (userInput.equals("q")) {
                    stopTraversal = true;
                }
            }
        }

        // 将路径写入文件
        writePathToFile(path.toString());
        System.out.println("Random walk completed. Path written to file.");
    }


    // 将路径写入文件的方法
    private void writePathToFile(String path) {
        try (FileWriter writer = new FileWriter("files/random_walk_path.txt")) {
            writer.write(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) throws IOException {
//       1st. write something here to test git...
        Scanner scanner = new Scanner(System.in);

        String content = readFileAsString("files/1.txt");
        content = processString(content);

        // 创建 DirectedGraph 对象
        DirectedGraph directedGraph = new DirectedGraph(content);

        boolean continueNextFunction = true;
        while (continueNextFunction) {
            // 打印图的邻接表表示
            System.out.println("图的邻接表表示:");
            directedGraph.printGraph();

            // 可视化图
            System.out.println("可视化图:");
            directedGraph.saveGraphImage(directedGraph.graph);

            // 获取所有桥接词
            System.out.println("获取所有桥接词:");
            directedGraph.bridgeWordsMap = directedGraph.getAllBridgeWords(directedGraph);

            // 用户输入查询桥接词
            while (true) {
                System.out.println("请输入第一个单词用于查询桥接词:");
                String word1 = scanner.nextLine();
                System.out.println("请输入第二个单词用于查询桥接词:");
                String word2 = scanner.nextLine();
                String queryResult = directedGraph.queryBridgeWords(word1, word2);
                System.out.println(queryResult);

                // 提示用户是否继续当前功能
                System.out.println("是否继续当前功能？(输入'n'表示否，其他任意字符表示是)");
                String continueInput = scanner.nextLine();
                if (continueInput.equalsIgnoreCase("n")) {
                    break;
                }
            }

            // 用户输入用于生成新文本的输入文本
            while (true) {
                System.out.println("请输入用于生成新文本的输入文本:");
                String inputText = scanner.nextLine();
                String newText = directedGraph.generateNewText(inputText);
                System.out.println("新文本: " + newText);

                // 提示用户是否继续当前功能
                System.out.println("是否继续当前功能？(输入'n'表示否，其他任意字符表示是)");
                String continueInput = scanner.nextLine();
                if (continueInput.equalsIgnoreCase("n")) {
                    break;
                }
            }

            // 用户输入用于计算最短路径的起点和终点
            // 用户输入用于计算最短路径的起点和终点
            // 用户输入用于计算最短路径的起点和终点
            while (true) {
                System.out.println("请输入第一个单词用于计算最短路径:");
                String word1 = scanner.nextLine();
                System.out.println("请输入第二个单词用于计算最短路径(留空表示计算与第一个单词的最短路径):");
                String word2 = scanner.nextLine();

                // 如果用户只输入了一个单词，则计算该单词到图中其他任一单词的最短路径
                if (word2.isEmpty()) {
                    for (String vertex : directedGraph.graph.vertexSet()) {
                        if (!vertex.equalsIgnoreCase(word1)) {
                            String shortestPath = directedGraph.calcShortestPath(word1, vertex);
                            System.out.println("Shortest path from \"" + word1 + "\" to \"" + vertex + "\":");
                            System.out.println(shortestPath);
                        }
                    }
                } else {
                    // 计算两个单词之间的最短路径
                    String shortestPath = directedGraph.calcShortestPath(word1, word2);
                    System.out.println("Shortest path from \"" + word1 + "\" to \"" + word2 + "\":");
                    System.out.println(shortestPath);
                }

                // 提示用户是否继续当前功能
                System.out.println("是否继续当前功能？(输入'n'表示否，其他任意字符表示是)");
                String continueInput = scanner.nextLine();
                if (continueInput.equalsIgnoreCase("n")) {
                    break;
                }
            }


            // 随机游走
            while (true) {
                System.out.println("随机游走:");
                directedGraph.randomWalk();

                // 提示用户是否继续当前功能
                System.out.println("是否继续当前功能？(输入'n'表示否，其他任意字符表示是)");
                String continueInput = scanner.nextLine();
                if (continueInput.equalsIgnoreCase("n")) {
                    break;
                }
            }

        }

    }



}