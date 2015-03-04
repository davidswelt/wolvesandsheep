from pyplayers import PythonWolfInterface

from was import Move

import math

## Example Wolf in Python

class PythonWolf(PythonWolfInterface):
    def initialize(self, wolf):
        self.wolf = wolf
        self.board = self.wolf.getGameBoard()

    def move(self):
        pastures = self.board.getPasturePositions()
        me = self.wolf.getLocation()
        target = None
        for p in pastures:
            d = math.sqrt((p.x - me.x)**2 + (p.y - me.y)**2)
            if target == None:
                target = p
                distance = d
            elif d < distance:
                target = p
                distance = d

        return Move(target.x - me.x, target.y - me.y)
