
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Main {
    public static int r, c, steps;
    // Stores the maze in a 2d array
    public static char[][] maze;
    // Stores the coordinates of each teleport, in order to build adj list later
    public static LinkedList<Coordinate> teleports;
    // Stores vertices and their adjacent vertices connected by edges
    public static LinkedList<Coordinate> adj[][];
    // Stores distance between index and source
    public static int dist[][];
    // Stores predecessor of index
    public static Coordinate pred[][];

    public static int[][] visited;

    public static Coordinate moves[] = { new Coordinate('0', 0, 1), new Coordinate('0', 0, -1),
            new Coordinate('0', 1, 0),
            new Coordinate('0', -1, 0) };

    static class Coordinate {
        char val = '0';
        int x = -1;
        int y = -1;

        public Coordinate(char val) {
            this.val = val;
        }

        public Coordinate(char val, int x, int y) {
            this.val = val;
            this.x = x;
            this.y = y;
        }

        public double distanceFrom(Coordinate dest) {
            return Math.sqrt((Math.pow((dest.x - this.x), 2) + (Math.pow((dest.y - this.y), 2))));
        }

        @Override
        public boolean equals(Object o) {

            if (o == this) {
                return true;
            }

            if (!(o instanceof Coordinate)) {
                return false;
            }

            Coordinate c = (Coordinate) o;

            return Integer.compare(x, c.x) == 0
                    && Integer.compare(y, c.y) == 0;
        }

        // Debugging purposes
        @Override
        public String toString() {
            return String.format("\"%c\" (%d, %d)", this.val, this.x, this.y);
        }

    }

    public static void main(String[] args) throws FileNotFoundException {
        // Setup Scanner
        File file = new File("in.txt");
        Scanner stdin = new Scanner(file);

        // Setup input variables
        r = stdin.nextInt();
        c = stdin.nextInt();
        stdin.nextLine();
        // System.out.println(r + " " + c);

        maze = new char[r][c];
        dist = new int[r][c];
        pred = new Coordinate[r][c];
        visited = new int[r][c];
        teleports = new LinkedList<Coordinate>();
        createGraph(r, c);

        int nextX;
        int nextY;

        // Read in maze
        while (stdin.hasNextLine()) {
            for (int i = 0; i < r; i++) {
                String line = stdin.nextLine();
                for (int j = 0; j < c; j++) {
                    maze[i][j] = line.charAt(j);
                    dist[i][j] = 0;
                    pred[i][j] = new Coordinate('0');
                    visited[i][j] = 0;

                    if (Character.isUpperCase(maze[i][j])) {
                        teleports.add(new Coordinate(maze[i][j], i, j));
                    }
                    // Create adjacency matrix:
                    // If the current location can move up, down, left, right
                    // Add teleportation edges aswell
                    for (int k = 0; k < moves.length; k++) {
                        nextX = i + moves[k].x;
                        nextY = j + moves[k].y;
                        if (!inbounds(nextX, nextY))
                            continue;
                        else if (maze[nextX][nextY] == '!')
                            continue;
                        else
                            addEdge(i, j, new Coordinate(maze[i][j], nextX, nextY));
                        if (Character.isUpperCase(maze[i][j])) {
                            Iterator<Coordinate> it = teleports.listIterator();
                            while (it.hasNext()) {
                                Coordinate next = it.next();
                                if (next.val == maze[i][j]) {
                                    System.out.println("Add Edge");
                                    addEdge(i, j, next);
                                }
                            }
                        }

                    }
                }
            }
        }

        Coordinate source = find('*'), destination = find('$');

        // Call wrapper function for BFS
        int res = getShortestDistance(source, destination);
        System.out.println(res);

        // Debug print visited
        // for (int i = 0; i < r; i++) {
        // System.out.println();
        // for (int j = 0; j < c; j++) {
        // System.out.print(visited[i][j] + " ");
        // }
        // }

        stdin.close();
    }

    // Wrapper function for BFS that calculates the steps taken.
    public static int getShortestDistance(Coordinate source, Coordinate dest) {

        if (bfs(source, dest) == 0) {
            return -1;
        }
        LinkedList<Coordinate> path = new LinkedList<Coordinate>();

        int distance = 0;
        Coordinate curr = dest;
        path.add(curr);
        while (pred[curr.x][curr.y].val != '0') {
            path.add(pred[curr.x][curr.y]);
            curr = pred[curr.x][curr.y];
            distance++;
        }

        return distance;

    }

    // Runs a BFS from location s to location e.
    public static int bfs(Coordinate source, Coordinate dest) {

        // Set up BFS.
        LinkedList<Coordinate> q = new LinkedList<Coordinate>();
        // System.out.print("Offer ");
        // System.out.println(s.toString());

        // Store '0' for haven't visited...otherwise a distance array.
        visited[source.x][source.y] = 1;
        dist[source.x][source.y] = 0;
        q.offer(source);

        // Run till the queue is done.
        while (q.size() > 0) {

            // Current item
            Coordinate cur = q.remove();
            for (int i = 0; i < adj[cur.x][cur.y].size(); i++) {
                // Adjacent item or next item
                Coordinate adjC = adj[cur.x][cur.y].get(i);
                // System.out.println(adjC.toString());
                if (visited[adjC.x][adjC.y] == 0) {
                    // Modify distance
                    visited[adjC.x][adjC.y] = dist[cur.x][cur.y] + 1;
                    // Set pred to current item
                    pred[adjC.x][adjC.y] = cur;
                    // Add next item to que
                    q.add(adjC);
                    // If the next item is the destination, return it
                    if (adjC.equals(dest))
                        return 1;
                }
            }
        }

        // Never made it!
        return 0;
    }

    // Returns true iff (x, y) is inbounds.
    public static boolean inbounds(int x, int y) {
        return x >= 0 && x < r && y >= 0 && y < c;
    }

    // Returns the first location where character c occurs, or -1 if it doesn't.
    public static Coordinate find(char val) {
        for (int i = 0; i < r; i++)
            for (int j = 0; j < c; j++)
                if (maze[i][j] == val)
                    return new Coordinate(maze[i][j], i, j);
        return null;
    }

    @SuppressWarnings("unchecked")
    public static void createGraph(int r, int c) {
        adj = new LinkedList[r][c];
        for (int i = 0; i < r; i++) {
            for (int j = 0; j < c; j++) {
                adj[i][j] = new LinkedList<Coordinate>();
            }
        }
    }

    public static void addEdge(int r, int c, Coordinate coord) {
        adj[r][c].add(coord);
    }

}