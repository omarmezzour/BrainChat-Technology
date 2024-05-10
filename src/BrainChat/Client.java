package BrainChat;
import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;


public interface Client extends Remote
{
    void writeMessage(String time, String sender, String message) throws RemoteException;

    void notifyDisconnected(String name) throws RemoteException;

    void notifyConnected(String name) throws RemoteException;


    class BasicClient implements Client, Serializable
    {
        private static final long serialVersionUID = 4885573965833413193L;

        private boolean mIsConnected;
        private String mName;
        private Registry mRegistry;
        private Linker mLinker;
        private Application mApp; 

        public BasicClient(String host)
        {
            mIsConnected = false;
            getRemotedObjects(host);
        }

        public void bindWithGUI(Application app)
        {
            mApp = app;
        }

        public boolean connect(String name)
        {
            mApp.addToChat("[Server]: Initiating your connection...",
                    Application.ATTR_SERVER); 

            try 
            {
                if (! mLinker.connect(name))
                {
                    mApp.addToChat("[Server]: Error, this pseudo is not available.", 
                            Application.ATTR_ERROR);
                    return false;
                }
            } 
            catch (Exception e)  
            {
                mApp.addToChat("[Server]: Error with the server, try again or " + 
                        "relaunch the app.", Application.ATTR_ERROR);
                return false;
            } 

            try
            {
                Client this_stub = (Client) 
                    UnicastRemoteObject.exportObject(this, 0);
                mRegistry.rebind("rmi://client/" + name, this_stub); 

                mName = name;
                mIsConnected = true;
            }
            catch (Exception e)
            {
                mApp.addToChat("[Server]: Error with the server, try again or " +
                        "relaunch the app.", Application.ATTR_ERROR);
                return false;
            }

            spreadConnection();
            retrieveMessages();

            mApp.addToChat("[Server]: You are connected as \"" + mName + "\".",
                    Application.ATTR_SERVER); 

            return true;
        }

        public void disconnect()
        {
            mApp.addToChat("[Server]: Initiating your disconnection...",
                    Application.ATTR_SERVER); 

            try
            {
                mRegistry.unbind("rmi://client/" + mName);
                UnicastRemoteObject.unexportObject(this, true);
                mLinker.disconnect(mName);
                mIsConnected = false;
            }
            catch (Exception e)
            {
                mApp.addToChat("[Server]: Error, cannot completely disconnect you. " + 
                        "Your username may be unavailable until the server restarts.",
                        Application.ATTR_ERROR); 
            }

            spreadDisconnection();
            mApp.clearUsersList();  

            mApp.addToChat("[Server]: Disconnection finished.",
                    Application.ATTR_SERVER); 
        }

        public void sendMessage(String message)
        {
            try 
            {
                String time = mLinker.addMessage(mName, message);
                mLinker.getClientNames().forEach(
                        s -> 
                        {
                            try 
                            {
                                Client client = (Client) mRegistry.lookup("rmi://client/" + s);
                                client.writeMessage(time, mName, message);
                            } 
                            catch (Exception e)  
                            {
                                mApp.addToChat("[Server]: Error, cannot distribute this message " +
                                        "to \"" + s + "\".", Application.ATTR_ERROR); 
                            }
                        }
                ) ;
            } 
            catch (Exception e)  
            {
                mApp.addToChat("[Server]: Error, cannot distribute this message.",
                        Application.ATTR_ERROR); 
            }
        }

        private void getRemotedObjects(String host)
        {
            try 
            {
                mRegistry = LocateRegistry.getRegistry(host); 
                mLinker = (Linker) mRegistry.lookup("rmi://server/ConnectService");
            } 
            catch (Exception e)  
            {
                mApp.addToChat("[Server]: Error with the server, please " + 
                        "relaunch the app.", Application.ATTR_ERROR);
                System.exit(-1);
            }
        }

        private void spreadConnection()
        {
            try 
            {
                mLinker.getClientNames().forEach(
                        s -> 
                        {
                            try 
                            {
                                Client client = (Client) mRegistry.lookup("rmi://client/" + s);
                                client.notifyConnected(mName);
                                if (! s.equals(mName))
                                {
                                    mApp.addToUsersList(s);
                                }
                            } 
                            catch (Exception e)  
                            {
                                mApp.addToChat("[Server]: Error, cannot notify your connection " +
                                        "to \"" + s + "\".", Application.ATTR_ERROR); 
                            }
                        }
                ) ;
            } 
            catch (Exception e)  
            {
                mApp.addToChat("[Server]: Error, cannot notify your connection.",
                        Application.ATTR_ERROR); 
            }
        }


        private void spreadDisconnection()
        {
            try 
            {
                mLinker.getClientNames().forEach(
                        s -> 
                        {
                            try 
                            {
                                Client client = (Client) mRegistry.lookup("rmi://client/" + s);
                                client.notifyDisconnected(mName);
                            } 
                            catch (Exception e)  
                            {
                                mApp.addToChat("[Server]: Error, cannot notify your disconnection " +
                                        "to \"" + s + "\".", Application.ATTR_ERROR); 
                            }
                        }
                ) ;

                notifyDisconnected(mName);
            } 
            catch (Exception e)  
            {
                mApp.addToChat("[Server]: Error, cannot notify your disconnection.",
                        Application.ATTR_ERROR); 
            }
        }

        private void retrieveMessages()
        {
            mApp.addToChat("[Server]: Recovering message history...",
                    Application.ATTR_SERVER); 

            try 
            {
                mLinker.getClientMessages().forEach(
                        m -> 
                        {
                            try
                            {
                                writeMessage(m.getTime(), m.getSender(), m.getContent());
                            }
                            catch (Exception e)
                            {
                                mApp.addToChat("[Server]: Error, cannot retrieve a message " +
                                       "from the history.",
                                        Application.ATTR_ERROR); 
                            }
                        }
                );
            } 
            catch (Exception e)  
            {
                mApp.addToChat("[Server]: Error, cannot retrieve message history.",
                        Application.ATTR_ERROR); 
            }
        }

        @Override
        public void writeMessage(String time, String sender, String message) throws RemoteException
        {
            mApp.addToChat("(" + time + ") ", Application.ATTR_BOLD);
            mApp.addToChat(sender + ": ", Application.ATTR_BOLD);
            mApp.addToChat(message, Application.ATTR_PLAIN);
        }

        @Override
        public void notifyDisconnected(String name) throws RemoteException
        {
            mApp.removeFromUserList(name);

            if (! name.equals(mName))
            {
                mApp.addToChat(name + " is disconnected.", Application.ATTR_SERVER);
            }
        }

        @Override
        public void notifyConnected(String name) throws RemoteException
        {
            if (! name.equals(mName))
            {
                mApp.addToChat(name + " is connected.", Application.ATTR_SERVER);
            }

            mApp.addToUsersList(name);
        }

        public boolean isConnected()
        {
            return mIsConnected;        
        }
    }
}