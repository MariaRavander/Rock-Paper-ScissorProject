package server.net;

import common.MessageException;
import common.MessageSplitter;
import java.io.IOException;
import java.util.StringJoiner;

import common.MsgType;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;
import server.controller.Controller;

public class PlayerHandler {
    private final Server server;
    private final SocketChannel playerChannel;
    private final Controller contr;
    
    private String username = "anonymous";
    public int totalScore = 0;
    public int roundScore = 0;
    public boolean madeChoice = false;
    public boolean playing = false;
    private final MessageSplitter msgSplitter = new MessageSplitter();
    private final ByteBuffer msgFromClient = ByteBuffer.allocateDirect(2018);
    
    PlayerHandler(Server server, SocketChannel playerChannel, Controller contr) {
        this.server = server;
        this.playerChannel = playerChannel;
        this.contr = contr;
    }
    
    public void handlePlayerRequest() {
        while (msgSplitter.hasNext()) {
            Message msg = new Message(msgSplitter.nextMsg());
            switch(msg.msgType) {
                case USER:
                    username = msg.msgBody;
                    server.broadcast(msg.msgType + "##" + username);
                    break;

                case JOIN:
                    if(playing == false){
                        if(contr.gameRunning()) {
                            try {
                                sendMsg(createMessage(MsgType.WAITJOIN));
                            } catch(IOException e) {
                                System.out.println(e);}
                        } 
                        else if(contr.prepareGame()) {
                            playing = true;
                            server.broadcast(MsgType.NEWGAME.toString());
                        } else 
                            try {
                                playing = true;
                                sendMsg(createMessage(MsgType.WAITCONNECT));
                            } catch(IOException e) {
                                System.out.println(e);}
                    }else
                        try {
                            sendMsg(createMessage(MsgType.ALREADYJOINED));
                        } catch (IOException ex) {
                            Logger.getLogger(PlayerHandler.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    break;
                    
                case PLAY:
                    if(playing == false) {
                        try {
                            sendMsg(createMessage(MsgType.PLAYERBLOCKED));
                        } catch(IOException e) {
                            System.out.println(e);}
                    }
                    else if(madeChoice == false) {
                        server.broadcast(MsgType.PLAYERCHOICE + "##" + username);
                        if(contr.sendChoice(msg.msgBody.toLowerCase(), this)) {
                            server.broadcast(MsgType.CHOICES + "##" + contr.getChoices());
                            server.broadcast(MsgType.RESULT + "##" + contr.getResult());
                            contr.endGame();
                        } else
                            try {
                                sendMsg(createMessage(MsgType.WAITROUND));
                            } catch(IOException e) {
                                System.out.println(e);}
                    } else {
                        try {
                            sendMsg(createMessage(MsgType.PLAYBLOCKED));
                        } catch(IOException e) {
                            System.out.println(e);}
                    }
                    break;

                case DISCONNECT:
                    System.out.println(msg);
                    try {
                        disconnectClient();
                    } catch(IOException ioe) {
                        System.out.println(ioe);
                    }
                    server.broadcast(msg.msgType + "##" + username);
                    break; 

                default:
                    System.out.println("Command:" + msg.receivedString + "is not known.");
            }
        }
    }

    void sendMsg(ByteBuffer msg) throws IOException {
        playerChannel.write(msg);
        if (msg.hasRemaining()) {
            throw new MessageException("Could not send message");
        }
    }
    
    ByteBuffer createMessage(MsgType type) {
        StringJoiner joiner = new StringJoiner("##");
        joiner.add(MsgType.BROADCAST.toString());
        joiner.add(type.toString());
        String messageWithLengthHeader = MessageSplitter.prependLengthHeader(joiner.toString());

        return ByteBuffer.wrap(messageWithLengthHeader.getBytes());
    }

    
    void disconnectClient() throws IOException {
        playerChannel.close();
    }
    
    void recvMsg() throws IOException {
        msgFromClient.clear();
        int numOfReadBytes;
        numOfReadBytes = playerChannel.read(msgFromClient);
        if (numOfReadBytes == -1) {
            throw new IOException("Client has closed connection.");
        }
        String recvdString = extractMessageFromBuffer();
        msgSplitter.appendRecvdString(recvdString);
        handlePlayerRequest();
    }
    
    private String extractMessageFromBuffer() {
        msgFromClient.flip();
        byte[] bytes = new byte[msgFromClient.remaining()];
        msgFromClient.get(bytes);
        return new String(bytes);
    }    
    
    private static class Message {
        private String receivedString;
        private MsgType msgType;
        private String msgBody;
        
        private Message (String receivedString) {
            parse(receivedString);
            this.receivedString = receivedString;
        }
        
        private void parse (String strToParse) {
            try {
                String[] msgTokens = strToParse.split("##");
                msgType = MsgType.valueOf(msgTokens[0].toUpperCase());
                if (hasBody(msgTokens)) {
                    msgBody = msgTokens[1];
                }
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
        
        private boolean hasBody(String[] msgTokens) {
            return msgTokens.length > 1;
        }
    }
    
    public String getUsername() {
        return username;
    }    
}
