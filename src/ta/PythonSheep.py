from ta import PythonSheepWrapper

from was import Move

import math

class PythonSheep(PythonSheepWrapper):
    def initialize(self, sheep):
        self.sheep = sheep
        self.board = sheep.getGameBoard()

    def move(self):
        pastures = self.board.getPasturePositions()
        me = self.sheep.getLocation()
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
