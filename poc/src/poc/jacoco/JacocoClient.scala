package poc.jacoco

import java.net.Socket

import org.jacoco.core.data.{ExecutionDataStore, SessionInfoStore}
import org.jacoco.core.runtime.{IRemoteCommandVisitor, RemoteControlReader, RemoteControlWriter}

import scala.util.Using

class JacocoClient(socket: => Socket) {

  def reset(): Unit = {
    Using.resource(socket) {
      socket => {
        val reader = new RemoteControlReader(socket.getInputStream())
        val remoteControl = new RemoteControlWriter(socket.getOutputStream())
        remoteControl.visitDumpCommand(false, true)
        reader.read()
      }
    }
  }

  def grab(): (SessionInfoStore, ExecutionDataStore) = grab(reset = false)

  def grabAndReset(): (SessionInfoStore, ExecutionDataStore) = grab(reset = true)

  protected def grab(reset: Boolean): (SessionInfoStore, ExecutionDataStore) = {
    grabInfo(remote => remote.visitDumpCommand(true, reset))
  }

  private def grabInfo(doWithRemote: IRemoteCommandVisitor => Unit): (SessionInfoStore, ExecutionDataStore) = {
    Using.resource(socket) {
      socket => {
        val remoteControl = new RemoteControlWriter(socket.getOutputStream())
        val reader = new RemoteControlReader(socket.getInputStream())

        val sessionInfoStore = new SessionInfoStore()
        val execDataStore = new ExecutionDataStore()
        reader.setSessionInfoVisitor(sessionInfoStore)
        reader.setExecutionDataVisitor(execDataStore)

        doWithRemote(remoteControl)

        reader.read()
        (sessionInfoStore, execDataStore)
      }
    }
  }
}
