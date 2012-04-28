package model


object DefaultBoard extends Board{
      def this() = {

           this(16,16);
           cells(0)(4) = new Cell(true, false, null);

      }
}
