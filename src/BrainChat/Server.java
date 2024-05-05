package BrainChat;

import java.util.ArrayList; 

import java.io.File; 
import java.io.FileOutputStream; 
import java.io.FileInputStream; 
import java.io.ObjectOutputStream; 
import java.io.ObjectInputStream; 
import java.io.EOFException; 

import java.rmi.server.*; 
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;


public class Server 
{
    public static void main(String[] args) 
    {
        new Server(parseArgs(args));
    }

    public static String parseArgs(String[] args)
    {
        if (args.length < 1) 
        {
            return "localhost";
        }

        return args[0];
    }
    
    
    private final String HOME_DIR_PATH = System.getProperty("user.home") 
        + File.separator + ".BrainChat";
    private final String HISTORY_FILE_PATH = HOME_DIR_PATH + File.separator 
        + "history"; 

    private final Linker.BasicLinker mLinker;

    public Server(String host)
    {
        mLinker = new Linker.BasicLinker();

        createHomeDir();
        createHistoryFile();

        retrieveMessageHistory();

        try 
        {
            if (host.equals("localhost"))
            {
                LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
            }

            Linker linker_stub = (Linker) 
                UnicastRemoteObject.exportObject(mLinker, 0);
            Registry registry = LocateRegistry.getRegistry(host);
            registry.rebind("rmi://server/ConnectService", linker_stub);

            Runtime.getRuntime().addShutdownHook(new Thread(this::saveMessageHistory));
        } 
        catch (Exception e) 
        {
            System.err.println("Erreur: " + e);
        }

        System.out.println ("Server ready...");
    }

    public void retrieveMessageHistory()
    {
        try
        {
            ObjectInputStream stream = new ObjectInputStream(
                    new FileInputStream(HISTORY_FILE_PATH));

                    @SuppressWarnings("unchecked")
            ArrayList<Linker.Message> messages = (ArrayList<Linker.Message>) stream.readObject(); 

            if (messages != null)
            {
                mLinker.setClientMessages(messages);
            }

            stream.close();
        }
        catch (EOFException e) 
        {

        }
        catch (Exception e) 
        {
            System.err.println("Error: cannot retrieve messages in the history file."); 
        }
    }   

    public void saveMessageHistory()
    {
        try
        {
            ObjectOutputStream stream = new ObjectOutputStream(
                    new FileOutputStream(HISTORY_FILE_PATH));

                    stream.writeObject(mLinker.getClientMessages()); 

            stream.close();
        }
        catch (Exception e) 
        {
            System.err.println("Error: cannot save messages in the history file."); 
        }
    }

    private void createHomeDir()
    {
        File homeDir = new File(HOME_DIR_PATH);

        try
        {
            if (! homeDir.exists() && ! homeDir.mkdirs())
            {
                System.err.println("Error: cannot create the BrainChat home directory " +
                        HOME_DIR_PATH + ".");
                System.exit(-1);
            }
        }
        catch (Exception e)
        {
            System.err.println("Error: cannot create the BrainChat home directory " + 
                    HOME_DIR_PATH + "."); 
            System.exit(-1);
        }
    }

    private void createHistoryFile()
    {
        File file = new File(HISTORY_FILE_PATH);

        try
        {
            if (! file.exists() && ! file.createNewFile())
            {
                System.err.println("Error: cannot create the history file " +
                        HISTORY_FILE_PATH + ".");
                System.exit(-1);
            }
        }
        catch (Exception e)
        {
            System.err.println("Error: cannot create the history file " + 
                    HISTORY_FILE_PATH + "."); 
            System.exit(-1);
        }
    }
}
