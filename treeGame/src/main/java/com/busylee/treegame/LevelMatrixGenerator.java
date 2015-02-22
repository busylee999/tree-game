package com.busylee.treegame;

import java.util.Random;

/**
 * Created by busylee on 22.02.15.
 */
public class LevelMatrixGenerator {

    static final Random RANDOM = new Random(System.currentTimeMillis());
    static final int RANDOM_START = 1;
    static final int RANDOM_STOP = 100;

    private static int getRandom(){
        return RANDOM.nextInt(RANDOM_STOP - RANDOM_START) + RANDOM_START;
    }

    private static void fill(int[] arr, int value) {
        for(int i = 0; i < arr.length; ++i)
            arr[i] = value;
    }

    public static int[][] generateLevelMatrix(int rowCount, int columnCount) {
        boolean showLog = false;
        int verticesCount = rowCount * columnCount;

        final int INF = Integer.MAX_VALUE / 2;

        int[][] matrix = new int[verticesCount][verticesCount];
        int[][] resultMatrix = new int[verticesCount][verticesCount];

        int[] treeEdges = new int[verticesCount];
        fill(treeEdges, -1);
        int[] edgesDegrees = new int[verticesCount];
        fill(edgesDegrees, 0);

        for(int i = 0 ; i < verticesCount; ++i) {
            fill(matrix[i], INF);
            fill(resultMatrix[i], 0);
        }

        for(int i = 0 ; i < verticesCount; ++i){
            if(i - 1 >= 0 && i % columnCount != 0)
                matrix[i][i - 1] = getRandom();

            if(i + 1 <verticesCount && i % columnCount != columnCount - 1)
                matrix[i][i + 1] = getRandom();

            if(i + columnCount < verticesCount)
                matrix[i][i + columnCount] = getRandom();

            if(i - columnCount >= 0)
                matrix[i][i - columnCount] = getRandom();
        }

        if(showLog) {
            System.out.print("matrix: \n");

            StringBuilder sb = new StringBuilder();
            for( int i = 0; i < verticesCount; ++i) {
                sb.setLength(0);
                for (int j = 0; j < verticesCount; ++j) {
                    if(matrix[i][j] == INF)
                        sb.append(0);
                    else
                        sb.append(matrix[i][j]);
                    sb.append(" ");
                }
                System.out.print(sb.toString() + "\n");
            }

        }

        boolean[] used = new boolean [verticesCount]; // массив пометок
        int[] dist = new int [verticesCount]; // массив расстояния. dist[v] = вес_ребра(MST, v)
        fill(dist, INF); // устанаавливаем расстояние до всех вершин INF
        int startVertexNumber = 0;//(columnCount + rowCount) / 9;
        dist[startVertexNumber] = 0; // для начальной вершины положим 0
        treeEdges[startVertexNumber] = startVertexNumber;

        for (;;) {
            int v = -1;
            for (int nv = 0; nv < verticesCount; nv++) // перебираем вершины
                if (!used[nv] && dist[nv] < INF && (v == -1 || dist[v] > dist[nv])) { // выбираем самую близкую непомеченную вершину
                    v = nv;
                }
            if (v == -1) break; // ближайшая вершина не найдена
            used[v] = true; // помечаем ее
            for (int to = 0; to < verticesCount; to++) {
                if (edgesDegrees[to] < 3 && edgesDegrees[v] < 3 && !used[to] && matrix[to][v] < INF) { // для всех непомеченных смежных
                    if (dist[to] > matrix[to][v]) {
                        dist[to] = matrix[to][v]; // улучшаем оценку расстояния (релаксация)
                        if(treeEdges[to] == -1) {
                            edgesDegrees[to]++;
                        } else if (treeEdges[to] != to)
                            edgesDegrees[treeEdges[to]]--;
                        edgesDegrees[v]++;
                        treeEdges[to] = v;
                    }
                }
            }
        }

        if(showLog) {
            StringBuilder sb = new StringBuilder();

            System.out.print("result edges degrees: \n");
            for(int i =0; i< verticesCount; ++i) {
                if (edgesDegrees[i] == -1) {
                    edgesDegrees[i] = 0;
                }
                System.out.println(i + " => " + edgesDegrees[i]);
            }

            System.out.print("result edges: \n");
            sb.setLength(0);
            for (int j = 0; j < verticesCount; ++j) {
                sb.append(j);
                sb.append(" => ");
                if(treeEdges[j] == -1) {
                    System.out.print("tree not found restart: \n");
                    generateLevelMatrix(rowCount, columnCount);
                }
                sb.append(treeEdges[j]);
                sb.append("\n");
            }
            System.out.print(sb.toString() + "\n");
        }

        for(int i = 0; i < verticesCount; ++i)
            if(treeEdges[i] != i) {
                resultMatrix[i][treeEdges[i]] = 1;
                resultMatrix[treeEdges[i]][i] = 1;
            }

        if(showLog) {
            StringBuilder sb = new StringBuilder();
            for( int i = 0; i < verticesCount; ++i) {
                sb.setLength(0);
                for (int j = 0; j < verticesCount; ++j) {
                    sb.append(resultMatrix[i][j]);
                }
                System.out.print(sb.toString() + "\n");
            }
        }

        return resultMatrix;
    }

}
