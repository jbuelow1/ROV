package org.snapshotscience.rov.wet.service.impl;

/* This file is part of WAHU ROV Software.
 *
 * WAHU ROV Software is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * WAHU ROV Software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with WAHU ROV Software.  If not, see <https://www.gnu.org/licenses/>.
 */

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.snapshotscience.rov.common.RovConstants;
import org.snapshotscience.rov.common.command.Command;
import org.snapshotscience.rov.common.response.InvalidCommandResponse;
import org.snapshotscience.rov.wet.service.CommandProcessorService;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * @author Jacob Buelow
 * @author Brian Wachsmuth
 */
@Service
@Slf4j
public class TcpConnectionService implements DisposableBean {

  public static final long IDLE_TIME = 10;  //One second
  public static final int SOCKET_TIMEOUT = 500;
  public static String myIp;

  private CommandProcessorService commandProcessorService;
  private ConnectionServer connectionServer;
  private ApplicationEventPublisher eventPublisher;

  public TcpConnectionService(CommandProcessorService commandProcessorService,
      ApplicationEventPublisher eventPublisher) {
    this.commandProcessorService = commandProcessorService;
    this.eventPublisher = eventPublisher;
    connectionServer = new ConnectionServer();
  }

  @EventListener
  public void startServer(ApplicationReadyEvent event) {
    log.debug("Starting TcpConnectionService server.");
    connectionServer.start();
  }

  @Override
  public void destroy() {
    connectionServer.abort();
  }

  private class ConnectionServer extends Thread {

    private ServerSocket listener;
    private volatile boolean running = true;
    private List<ConnectionHandler> handlers = new ArrayList<>();

    @Override
    public void run() {
      try {
        listener = new ServerSocket(RovConstants.ROV_PORT);
        listener.setSoTimeout(SOCKET_TIMEOUT);
      } catch (IOException e) {
        log.error("Failed to start connection server.", e);
        return;
      }

      while (running) {
        try {
          ConnectionHandler handler = new ConnectionHandler(listener.accept());
          handlers.add(handler);
          handler.start();
          eventPublisher.publishEvent(new ControllerConnectedEvent(this));
        } catch (SocketTimeoutException e) {
          //Do nothing and move on
        } catch (IOException e) {
          log.error("Error starting a new connection.", e);
        }

        cleanupHandlers();

        try {
          sleep(IDLE_TIME);
        } catch (InterruptedException e) {
          break;
        }
      }
    }

    public void abort() {
      running = false;

      Iterator<ConnectionHandler> it = handlers.iterator();
      while (it.hasNext()) {
        ConnectionHandler handler = it.next();
        handler.abort();
        it.remove();
      }
    }

    private void cleanupHandlers() {
      Iterator<ConnectionHandler> it = handlers.iterator();
      while (it.hasNext()) {
        ConnectionHandler handler = it.next();

        if (!handler.isConnected()) {
          it.remove();
        }
      }
    }
  }

  private class ConnectionHandler extends Thread {

    private boolean running = true;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public ConnectionHandler(Socket clientSocket) throws IOException {
      log.debug("Received new connection from client: " + clientSocket.getInetAddress());
      this.socket = clientSocket;
      this.socket.setSoTimeout(SOCKET_TIMEOUT);
      this.out = new ObjectOutputStream(clientSocket.getOutputStream());
      this.in = new ObjectInputStream(clientSocket.getInputStream());
    }

    @Override
    public void run() {
      while (socket.isConnected() && running) {
        try {
          Command command;
          try {
            command = (Command) in.readObject();
            out.writeObject(commandProcessorService.handleCommand(command));
          } catch (ClassCastException e) {
            out.writeObject(new InvalidCommandResponse(e));
          }
        } catch (SocketTimeoutException e) {
          //Do nothing and move on to try reading again.
        } catch (StreamCorruptedException e) {
          //Something went wrong with the network. disconnect.
          eventPublisher.publishEvent(new ControllerDisconnectedEvent(this));
          break;
        } catch (EOFException | SocketException e) {
          //Out channel has closed.  Break out of the loop.
          eventPublisher.publishEvent(new ControllerDisconnectedEvent(this));
          break;
        } catch (ClassNotFoundException | IOException e) {
          log.error("Error handling command stream.", e);
        }

        try {
          sleep(IDLE_TIME);
        } catch (InterruptedException e) {
          break;
        }
      }

      IOUtils.closeQuietly(in);
      IOUtils.closeQuietly(out);
      IOUtils.closeQuietly(socket);

      log.debug("Controller " + socket.getInetAddress() + " disconnected.");
    }

    public void abort() {
      running = false;
    }

    public boolean isConnected() {
      return (socket != null && socket.isConnected());
    }
  }
}
