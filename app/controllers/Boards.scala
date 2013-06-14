package controllers


import models.{DbUser, Board, DbBoard}
import securesocial.core.SecureSocial


// stupid controller just to test what we have in db ... and display it
object Boards extends CookieLang with SecureSocial {

  def index = UserAwareAction {
    implicit request =>
      Ok(views.html.boards.boardList(DbBoard.findAll, DbUser.fromRequest()))
  }

  def preview(id: Long) = UserAwareAction {
    implicit request =>
      Board.loadById(id).map {
        board => Ok(views.html.boards.previewBoard(board, DbUser.fromRequest()))
      }.getOrElse(NotFound)
  }

}
