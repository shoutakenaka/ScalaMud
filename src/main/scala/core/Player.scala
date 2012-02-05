package com.kotancode.scalamud.core

import com.kotancode.scalamud._
import akka.actor._
import akka.routing._
import com.kotancode.scalamud.core.Implicits._

import java.net._
import java.io._

case class NewSocket(socket: Socket)
case class TextMessage(message:String)

class Player extends Actor {
	private var inReader: BufferedReader = null
	private var outWriter: PrintWriter = null
	
 	implicit def inputStreamWrapper(in: InputStream) =
  		new BufferedReader(new InputStreamReader(in))

 	implicit def outputStreamWrapper(out: OutputStream) =
  		new PrintWriter(new OutputStreamWriter(out))
	
	def receive = {
		case s:NewSocket => {
			context.actorOf(Props(new Actor {
				def receive = {
					case NewSocket(socket) => {
						setupSocket(socket.getInputStream(), socket.getOutputStream())						
					}
				}})) ! s
		}
		case TextMessage(message) => {
			outWriter.println(message)
			outWriter.flush()
		}		
	}
	
	private def setupSocket(in: BufferedReader, out: PrintWriter) {
	   inReader = in
	   outWriter = out
	
	   // put the player in the void until we know where they really go
	   val theVoid = context.actorFor("/user/server/areas-root/thevoid")
	   println("found the void: "+ theVoid)
	   theVoid ! EnterInventory
	
	   val is: InputStream = classOf[Player].getResourceAsStream("/welcome.txt")
	   val source = scala.io.Source.fromInputStream(is)

	   out.println(source.mkString)
	   out.print("Login: ")
	   out.flush()
	   var playerName = in.readLine();
	   out.println("Welcome to ScalaMUD, " + playerName)
	   self.name = playerName
	   out.flush()
	   println("Player logged in: "+ self)
	   Game.server ! PlayerLoggedIn
	   while (true) {
		   val line = inReader.readLine()
		   outWriter.println(playerName + ": " + line)
		   outWriter.flush()
	   }	
	 }
}