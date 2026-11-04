public class MazeGenerator {
    public char[][] maze;
    public int maze_width;
    public int maze_height;
    public int currentx;
    public int currenty;

    private static final char SPACE = ',';

    public MazeGenerator(int width, int height) {
        assert width % 2 == 1;
        assert height % 2 == 1;

        this.maze = new char[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                maze[i][j] = '#';
            }
        }

        currentx = 1;
        currenty = 1;
        maze[currenty][currentx] = SPACE;

        maze_width = width;
        maze_height = height;

    }

    // using recursive backtracker. ensure that every point in the maze is reachable from every other point.
    //
    // Reference: https://en.wikipedia.org/wiki/Maze_generation_algorithm
    public char[][] generate_maze() {
        generate_maze(this.currentx, this.currenty);
        return this.maze;
    }

    private void generate_maze(int cx, int cy) {
        // from wikipedia:
        // step 3 - while the current cell has unvisited neighoburs
        while (true) { 
            int unvisited = has_unvisited_neighbors(cx, cy);
            if (unvisited == 0) { break; }
            
            // step 3.1 - choose one of the unvisited neighbours
            Direction d;
            while (true) { 
                int rand_bit = (int)(Math.random() * 4.0);
                if (rand_bit == 0 && ( unvisited       & 1) == 1) { d = Direction.Up; break; }
                if (rand_bit == 1 && ((unvisited >> 1) & 1) == 1) { d = Direction.Right; break; }
                if (rand_bit == 2 && ((unvisited >> 2) & 1) == 1) { d = Direction.Down; break; }
                if (rand_bit == 3 && ((unvisited >> 3) & 1) == 1) { d = Direction.Left; break; }
            }

            // step 3.2 - remove wall between this cell and the next cell
            // step 3.3 - invoke the routine recusively for the next cell
            switch (d) {
                case Direction.Up    -> { 
                    this.maze[cy - 1][cx] = SPACE; 
                    this.maze[cy - 1][cx + 1] = SPACE; 
                    this.maze[cy - 2][cx] = SPACE;
                    this.maze[cy - 2][cx + 1] = SPACE;
                    this.generate_maze(cx, cy - 2); 
                }
                case Direction.Down  -> { 
                    this.maze[cy + 1][cx] = SPACE; 
                    this.maze[cy + 1][cx + 1] = SPACE; 
                    this.maze[cy + 2][cx] = SPACE; 
                    this.maze[cy + 2][cx + 1] = SPACE; 
                    this.generate_maze(cx, cy + 2); 
                }
                case Direction.Left  -> { 
                    this.maze[cy][cx - 1] = SPACE;  
                    this.maze[cy][cx - 2] = SPACE;  
                    this.maze[cy][cx - 3] = SPACE;  
                    this.maze[cy][cx - 4] = SPACE;  
                    this.generate_maze(cx - 4, cy); 
                }
                case Direction.Right -> { 
                    this.maze[cy][cx + 2] = SPACE; 
                    this.maze[cy][cx + 3] = SPACE; 
                    this.maze[cy][cx + 4] = SPACE; 
                    this.maze[cy][cx + 5] = SPACE; 
                    this.generate_maze(cx + 4, cy); 
                }
            }
        }

    }

    private int has_unvisited_neighbors(int cx, int cy) {
        // 0: visited all around
        // & 1: not visited above
        // & 2: not visited right
        // & 4: not visited below
        // & 8: not visited left
        int unvisited = 0;
        // check left wall
        if (cx > 4 && this.maze[cy][cx - 4] == '#') {
            unvisited |= (1 << 3);
        }
        // check right wall
        if (cx < (maze_width - 5) && this.maze[cy][cx + 4] == '#') {
            unvisited |= (1 << 1);
        }

        // check top wall
        if (cy > 2 && this.maze[cy - 2][cx] == '#') {
            unvisited |= 1;
        }
        // check bottom wall
        if (cy < (maze_height - 3) && this.maze[cy + 2][cx] == '#') {
            unvisited |= (1 << 2);
        }

        return unvisited;
    }
}

enum Direction { Up, Down, Left, Right; }