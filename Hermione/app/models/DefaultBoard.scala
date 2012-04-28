package models


object DefaultBoard extends Board(16, 16){
    cells(0)(4) = new Cell(true, false, null)
}
