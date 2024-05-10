package BrainChat;
import java.io.Serializable;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.rmi.*; 


public interface Linker extends Remote 
{
    String addMessage(String sender, String message) throws RemoteException;

    boolean connect(String name) throws RemoteException;

    void disconnect(String name) throws RemoteException;

    ArrayList<String> getClientNames() throws RemoteException;

    ArrayList<Message> getClientMessages() throws RemoteException;

    void setClientMessages(ArrayList<Message> messages) throws RemoteException;


    class BasicLinker implements Linker
    {
        private final ArrayList<String> mClientNames;
        private ArrayList<Message> mClientMessages;

        public BasicLinker()
        {
            mClientNames = new ArrayList<>();
            mClientMessages = new ArrayList<>();
        }

        @Override
        public String addMessage(String sender, String message) throws RemoteException
        {
            String DATE_FORMAT = "HH:mm:ss";
            String time = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern(DATE_FORMAT));
            mClientMessages.add(new Message(time, sender, message));

            return time; 
        }

        @Override
        public boolean connect(String name) throws RemoteException 
        {
            if (mClientNames.contains(name))
            {
                return false; 
            }

            System.out.println("Adhésion du client: " + name); 
            mClientNames.add(name);
            return true;
        }

        @Override
        public void disconnect(String name) throws RemoteException
        {
            System.out.println("Client exiting: " + name); 
            mClientNames.remove(name);
        }

        @Override
        public ArrayList<String> getClientNames() throws RemoteException
        {
            return mClientNames;
        }

        @Override
        public ArrayList<Message> getClientMessages() throws RemoteException
        {
            return mClientMessages;
        }

        @Override
        public void setClientMessages(ArrayList<Message> messages) throws RemoteException
        {
            mClientMessages = messages;
        }
    }


    class Message implements Serializable
    {
        private static final long serialVersionUID = 667363824879925614L;

        private final String mTime;
        private final String mSender;
        private final String mContent;

        private Message(String time, String sender, String content)
        {
            mTime = time;
            mSender = sender;
            mContent = content;
        }

        public String getTime()
        {
            return mTime;
        }

        public String getSender()
        {
            return mSender;
        }

        public String getContent()
        {
            return mContent;
        }
    }
}
