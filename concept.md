# Game concept
The core of the game is the goal of escaping the maze.

## World
The world consists of a few components.

### Maze
```
########
# #    #
# # # ##
#   #  #
########
```
- maze has walls that you can't pass through
- maze is a lot bigger than what is shown (big enough to the point that you can't brute force it, 1000x1000)
- maze has `compartments` that are visually slightly distinct that you can use to figure out how to escape the maze
- you can't see the whole maze at once; some areas are visually secluded. 

### Compartments
A maze can sometimes have a compartment in the walls:
```
####%###
# #    #
# # # ##
#   #  #
########
```

## End goal
Find the exit point on the edge of the maze to free yourself from the misery using clues found throughout the map.