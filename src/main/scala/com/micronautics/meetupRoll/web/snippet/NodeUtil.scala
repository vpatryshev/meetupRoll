package com.micronautics.meetupRoll.web.snippet

/**
 * Created with IntelliJ IDEA.
 * User: Julia
 * Date: 9/6/13
 */
object NodeUtil {

  def alertSuccess(message: String) =
    <div class="alert alert-success"><strong>Info!</strong>{" " + message}</div>

  def alertError(message: String) =
    <div class="alert alert-error"><strong>Error!</strong>{" " + message}</div>

}
