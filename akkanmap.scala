import scala.concurrent.Await
import akka.pattern.ask
import akka.actor.{ ActorRef, ActorSystem, Props, Actor, Inbox }
import scala.concurrent.duration._

case class ScanIP(ip: String, threads: Int)
case class Scan(ip: String, start: Int, range: Int)


class Scanner extends Actor {
    def scan(start: Int, range: Int) {
        for (i <- start to start + range) {
            println(i)
        }
    }

    def receive = {
        case Scan(ip, start, range) => scan(start, range)
    }
}

class ScannerManager extends Actor {

    var children: List[Any] = List() 
    def spawnChildren(threads: Int) {
        for ( i <- 1 to threads ) {
            children ::= context.actorOf(Props[Scanner], name = "child"+i) 
        }
    }

    def askChildren(ip: String, threads: Int) {
        var n: Int = 0
        for ( child <- children ) {
           child ? Scan(ip, 65535/threads * n, 65535/threads)
           n = n + 1
        }
    }

    def receive = { 
        case ScanIP(ip, threads) => {
            spawnChildren(threads)
            askChildren(ip, threads)
        }
    }
}

object akkanmap extends App {
    val system = ActorSystem("akkanmap")
    
    val a = system.actorOf(Props[ScannerManager], "actor")
    a ! ScanIP("192.168.0.11",4)
}
