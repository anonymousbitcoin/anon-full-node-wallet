package org.anonymous.wallets.fullnode.ui;

import org.anonymous.wallets.fullnode.daemon.ANONClientCaller;
import org.anonymous.wallets.fullnode.daemon.ANONClientCaller.NetworkAndBlockchainInfo;
import org.anonymous.wallets.fullnode.daemon.ANONClientCaller.WalletCallException;
import org.anonymous.wallets.fullnode.daemon.ANONInstallationObserver;
import org.anonymous.wallets.fullnode.daemon.ANONInstallationObserver.DAEMON_STATUS;
import org.anonymous.wallets.fullnode.daemon.ANONInstallationObserver.DaemonInfo;
import org.anonymous.wallets.fullnode.daemon.ANONInstallationObserver.InstallationDetectionException;
import org.anonymous.wallets.fullnode.messaging.MessagingPanel;
import org.anonymous.wallets.fullnode.util.*;
import org.anonymous.wallets.fullnode.util.OSUtil.OS_TYPE;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;

/*
 * This Java source file was generated by the Gradle 'init' task.
 */
public class ANONWalletUI extends JFrame {
    private static final String VERSION = "1.1.0";
    private ANONInstallationObserver installationObserver;
    private ANONClientCaller clientCaller;
    private StatusUpdateErrorReporter errorReporter;

    private WalletOperations walletOps;

    private JMenuItem menuItemExit;
    private JMenuItem menuItemAbout;
    private JMenuItem menuItemShowPrivateKey;
    private JMenuItem menuItemImportOnePrivateKey;
    private JMenuItem menuItemOwnIdentity;
    private JMenuItem menuItemExportOwnIdentity;
    private JMenuItem menuItemImportContactIdentity;
    private JMenuItem menuItemRemoveContactIdentity;
    private JMenuItem menuItemMessagingOptions;

    private DashboardPanel dashboard;
    private AddressesPanel addresses;
    private SendCashPanel sendPanel;
    private AddressBookPanel addressBookPanel;
    private MessagingPanel messagingPanel;
    private MasternodePanel masternodePanel;
    private MyMasternodePanel myMasternodePanel;
    private GovernancePanel governancePanel;


    private JMenuItem langEnglish;
    private JMenuItem langDutch;
    private JMenuItem langPortuguese;
    private JMenuItem langItalian;
    private JMenuItem langFrench;
    private JMenuItem langSpanish;
    private JMenuItem langRussian;


    private static final String SUFFIX_TESTNET = " [TESTNET] ";

    private static final String LOCAL_MSG_STARTING = Util.local("LOCAL_MSG_STARTING");
    private static final String LOCAL_MSG_TAB_TRANSACTIONS = Util.local("LOCAL_MSG_TAB_TRANSACTIONS");
    private static final String LOCAL_MSG_TAB_ADDRESSES = Util.local("LOCAL_MSG_TAB_ADDRESSES");
    private static final String LOCAL_MSG_TAB_SEND = Util.local("LOCAL_MSG_TAB_SEND");
    private static final String LOCAL_MSG_TAB_ADDRESS_BOOK = Util.local("LOCAL_MSG_TAB_ADDRESS_BOOK");
    // private static final String LOCAL_MSG_TAB_MSG = Util.local("LOCAL_MSG_TAB_MSG");
    private static final String LOCAL_MSG_TAB_MSTRNDE = Util.local("LOCAL_MSG_TAB_MSTRNDE");
    private static final String LOCAL_MSG_TAB_MYMSTRNDE = Util.local("LOCAL_MSG_TAB_MYMSTRNDE");
    private static final String LOCAL_MENU_MAIN = Util.local("LOCAL_MENU_MAIN");
    private static final String LOCAL_MENU_ABOUT = Util.local("LOCAL_MENU_ABOUT");
    private static final String LOCAL_MENU_QUIT = Util.local("LOCAL_MENU_QUIT");
    private static final String LOCAL_MENU_WALLET = Util.local("LOCAL_MENU_WALLET");
    private static final String LOCAL_MENU_VIEW_PK = Util.local("LOCAL_MENU_VIEW_PK");
    private static final String LOCAL_MENU_IMPORT_PK = Util.local("LOCAL_MENU_IMPORT_PK");
    private static final String LOCAL_MENU_MSG = Util.local("LOCAL_MENU_MSG");
    private static final String LOCAL_MENU_MY_ID = Util.local("LOCAL_MENU_MY_ID");
    private static final String LOCAL_MENU_EXPORT_ID = Util.local("LOCAL_MENU_EXPORT_ID");
    private static final String LOCAL_MENU_IMPORT_CONTACT = Util.local("LOCAL_MENU_IMPORT_CONTACT");
    private static final String LOCAL_MENU_REMOVE_CONTACT = Util.local("LOCAL_MENU_REMOVE_CONTACT");
    private static final String LOCAL_MENU_OPTIONS = Util.local("LOCAL_MENU_OPTIONS");
    private static final String LOCAL_MSG_INITIAL_DISCLAIMER = Util.local("LOCAL_MSG_INITIAL_DISCLAIMER");
    private static final String LOCAL_MSG_TITLE_DISCLAIMER = Util.local("LOCAL_MSG_TITLE_DISCLAIMER");
    private static final String LOCAL_MSG_EXITING = Util.local("LOCAL_MSG_EXITING");
    private static final String LOCAL_MSG_UI_TITLE = Util.local("LOCAL_MSG_UI_TITLE");
    private static final String LOCAL_MSG_DAEMON_ERROR = Util.local("LOCAL_MSG_DAEMON_ERROR");
    private static final String LOCAL_MSG_DAEMON_ERROR_TITLE = Util.local("LOCAL_MSG_DAEMON_ERROR_TITLE");
    private static final String LOCAL_MSG_SET_LANG = Util.local("LOCAL_MSG_SET_LANG");
    private static final String LOCAL_MSG_RESTART = Util.local("LOCAL_MSG_RESTART");
    private static final String LOCAL_MSG_RESTART_DETAIL = Util.local("LOCAL_MSG_RESTART_DETAIL");

    //image resources
    private static final String IMG_TAB_TRANSACTIONS = "images/overview.png";
    private static final String IMG_TAB_ADDRESSES = "images/own-addresses.png";
    private static final String IMG_TAB_SEND = "images/send.png";
    private static final String IMG_TAB_ADDRESS_BOOK = "images/address-book.png";
    private static final String IMG_TAB_MSG = "images/messaging.png";
    private static final String IMG_ANON_ICON = "images/anon-200.png";

    JTabbedPane tabs;

    private void showRestartRequired() {
        JOptionPane.showMessageDialog(
            null,
            LOCAL_MSG_RESTART_DETAIL,
            LOCAL_MSG_RESTART,
            JOptionPane.INFORMATION_MESSAGE);
    }

    public ANONWalletUI(StartupProgressDialog progressDialog, String title)
        throws IOException, InterruptedException, WalletCallException {
        super(title);

        if (progressDialog != null) {
            progressDialog.setProgressText(LOCAL_MSG_STARTING);
        }

        ClassLoader cl = this.getClass().getClassLoader();

        this.setIconImage(new ImageIcon(cl.getResource(IMG_ANON_ICON)).getImage());
        Container contentPane = this.getContentPane();

        errorReporter = new StatusUpdateErrorReporter(this);
        installationObserver = new ANONInstallationObserver(OSUtil.getProgramDirectory());
        clientCaller = new ANONClientCaller(OSUtil.getProgramDirectory());

        if (installationObserver.isOnTestNet()) {
            this.setTitle(this.getTitle() + SUFFIX_TESTNET);
        }

        // Build content
        tabs = new JTabbedPane();
        Font oldTabFont = tabs.getFont();
        Font newTabFont = new Font(oldTabFont.getName(), Font.BOLD, oldTabFont.getSize() * 57 / 50);
        tabs.setFont(newTabFont);
        BackupTracker backupTracker = new BackupTracker(this);

        tabs.addTab(LOCAL_MSG_TAB_TRANSACTIONS.concat(" "),
            new ImageIcon(cl.getResource(IMG_TAB_TRANSACTIONS)),
            dashboard = new DashboardPanel(this, installationObserver, clientCaller,
                errorReporter, backupTracker));
        tabs.addTab(LOCAL_MSG_TAB_ADDRESSES,
            new ImageIcon(cl.getResource(IMG_TAB_ADDRESSES)),
            addresses = new AddressesPanel(this, clientCaller, errorReporter));
        tabs.addTab(LOCAL_MSG_TAB_SEND,
            new ImageIcon(cl.getResource(IMG_TAB_SEND)),
            sendPanel = new SendCashPanel(clientCaller, errorReporter, installationObserver, backupTracker));
        tabs.addTab(LOCAL_MSG_TAB_ADDRESS_BOOK,
            new ImageIcon(cl.getResource(IMG_TAB_ADDRESS_BOOK)),
            addressBookPanel = new AddressBookPanel(sendPanel, tabs));
        // tabs.addTab(LOCAL_MSG_TAB_MSG,
        //     new ImageIcon(cl.getResource(IMG_TAB_MSG)),
        //     messagingPanel = new MessagingPanel(this, sendPanel, tabs, clientCaller, errorReporter));
        tabs.addTab(LOCAL_MSG_TAB_MSTRNDE,
            new ImageIcon(cl.getResource(IMG_TAB_SEND)),
            masternodePanel = new MasternodePanel(this, installationObserver, clientCaller, errorReporter, backupTracker)); 
        tabs.addTab(LOCAL_MSG_TAB_MYMSTRNDE,
            new ImageIcon(cl.getResource(IMG_TAB_SEND)),
            myMasternodePanel = new MyMasternodePanel(this, installationObserver, clientCaller, errorReporter, backupTracker));
        tabs.addTab("Governance",
            new ImageIcon(cl.getResource(IMG_TAB_SEND)),
            governancePanel = new GovernancePanel(this, installationObserver, clientCaller, errorReporter, backupTracker));

        contentPane.add(tabs);

        this.walletOps = new WalletOperations(
            this, tabs, addresses, clientCaller, errorReporter);

        int width = 1000;

        OS_TYPE os = OSUtil.getOSType();

        // Window needs to be larger on Mac/Windows - typically
        if ((os == OS_TYPE.WINDOWS) || (os == OS_TYPE.MAC_OS)) {
            width += 100;
        }

        this.setSize(new Dimension(width, 440));

        // Build menu
        JMenuBar mb = new JMenuBar();
        JMenu file = new JMenu(LOCAL_MENU_MAIN);
        file.setMnemonic(KeyEvent.VK_M);
        int accelaratorKeyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        file.add(menuItemAbout = new JMenuItem(LOCAL_MENU_ABOUT, KeyEvent.VK_T));
        menuItemAbout.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, accelaratorKeyMask));
        file.addSeparator();
        file.add(menuItemExit = new JMenuItem(LOCAL_MENU_QUIT, KeyEvent.VK_Q));
        menuItemExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, accelaratorKeyMask));
        mb.add(file);

        JMenu wallet = new JMenu(LOCAL_MENU_WALLET);
        wallet.setMnemonic(KeyEvent.VK_W);


        wallet.add(menuItemShowPrivateKey = new JMenuItem(LOCAL_MENU_VIEW_PK, KeyEvent.VK_P));
        menuItemShowPrivateKey.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, accelaratorKeyMask));
        wallet.add(menuItemImportOnePrivateKey = new JMenuItem(LOCAL_MENU_IMPORT_PK, KeyEvent.VK_N));
        menuItemImportOnePrivateKey.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, accelaratorKeyMask));


        mb.add(wallet);

        JMenu messaging = new JMenu(LOCAL_MENU_MSG);
        messaging.setMnemonic(KeyEvent.VK_S);
        messaging.add(menuItemOwnIdentity = new JMenuItem(LOCAL_MENU_MY_ID, KeyEvent.VK_D));
        menuItemOwnIdentity.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, accelaratorKeyMask));
        messaging.add(menuItemExportOwnIdentity = new JMenuItem(LOCAL_MENU_EXPORT_ID, KeyEvent.VK_X));
        menuItemExportOwnIdentity.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, accelaratorKeyMask));

        messaging.add(menuItemImportContactIdentity = new JMenuItem(LOCAL_MENU_IMPORT_CONTACT, KeyEvent.VK_Y));
        menuItemImportContactIdentity.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, accelaratorKeyMask));
        messaging.add(menuItemRemoveContactIdentity = new JMenuItem(LOCAL_MENU_REMOVE_CONTACT, KeyEvent.VK_R));
        menuItemRemoveContactIdentity.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, accelaratorKeyMask));

        messaging.add(menuItemMessagingOptions = new JMenuItem(LOCAL_MENU_OPTIONS, KeyEvent.VK_O));
        menuItemMessagingOptions.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, accelaratorKeyMask));

        mb.add(messaging);

        JMenu lang = new JMenu(LOCAL_MSG_SET_LANG);

        lang.add(langEnglish = new JMenuItem("English"));
        lang.add(langDutch = new JMenuItem("Nederlands"));
        lang.add(langPortuguese = new JMenuItem("Português"));
        lang.add(langItalian = new JMenuItem("Italiano"));
        lang.add(langFrench = new JMenuItem("Français"));
        lang.add(langSpanish = new JMenuItem("Español"));
        //lang.add(langRussian = new JMenuItem("русский"));

        mb.add(lang);


        this.setJMenuBar(mb);

        // Add listeners etc.
        menuItemExit.addActionListener(
            e -> ANONWalletUI.this.exitProgram()
        );

        menuItemAbout.addActionListener(
            e -> {
                try {
                    AboutDialog ad = new AboutDialog(ANONWalletUI.this);
                    ad.setVisible(true);
                } catch (UnsupportedEncodingException uee) {
                    Log.error("Unexpected error: ", uee);
                    ANONWalletUI.this.errorReporter.reportError(uee);
                }
            }
        );
        menuItemShowPrivateKey.addActionListener(
            e -> ANONWalletUI.this.walletOps.showPrivateKey()
        );

        menuItemImportOnePrivateKey.addActionListener(
            e -> ANONWalletUI.this.walletOps.importSinglePrivateKey()
        );

        menuItemOwnIdentity.addActionListener(
            e -> ANONWalletUI.this.messagingPanel.openOwnIdentityDialog()
        );

        menuItemExportOwnIdentity.addActionListener(
            e -> ANONWalletUI.this.messagingPanel.exportOwnIdentity()
        );

        menuItemImportContactIdentity.addActionListener(
            e -> ANONWalletUI.this.messagingPanel.importContactIdentity()
        );

        menuItemRemoveContactIdentity.addActionListener(
            e -> ANONWalletUI.this.messagingPanel.removeSelectedContact()
        );

        menuItemMessagingOptions.addActionListener(
            e -> ANONWalletUI.this.messagingPanel.openOptionsDialog()
        );

        langEnglish.addActionListener(
            e -> {
                Util.setLanguage("en");
                showRestartRequired();
            }
        );
        langDutch.addActionListener(
            e -> {
                Util.setLanguage("nl");
                showRestartRequired();
            }
        );

        langPortuguese.addActionListener(
            e -> {
                Util.setLanguage("pt");
                showRestartRequired();
            }
        );

        langItalian.addActionListener(
            e -> {
                Util.setLanguage("it");
                showRestartRequired();
            }
        );
    
	    langFrench.addActionListener(
	        e -> {
	          Util.setLanguage("fr");
	          showRestartRequired();
	        }
	    );
	    
	    langSpanish.addActionListener(
	        e -> {
	          Util.setLanguage("es-ES");
	          showRestartRequired();
	        }
	    );

        // Close operation
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                ANONWalletUI.this.exitProgram();
            }
        });

        // Show initial message
        SwingUtilities.invokeLater(() -> {
            try {
                String userDir = OSUtil.getSettingsDirectory();
                File warningFlagFile = new File(userDir + File.separator + "initialInfoShown_0.75.flag");
                if (warningFlagFile.exists()) {
                    return;
                } else {
                    warningFlagFile.createNewFile();
                }

            } catch (IOException ioe) {
                /* TODO: report exceptions to the user */
                Log.error("Unexpected error: ", ioe);
            }

            JOptionPane.showMessageDialog(
                ANONWalletUI.this.getRootPane().getParent(),
                LOCAL_MSG_INITIAL_DISCLAIMER,
                LOCAL_MSG_TITLE_DISCLAIMER, JOptionPane.INFORMATION_MESSAGE);
        });

        // Finally dispose of the progress dialog
        if (progressDialog != null) {
            progressDialog.doDispose();
        }

        // Notify the messaging TAB that it is being selected - every time
        tabs.addChangeListener(
            e -> {
                JTabbedPane tabs = (JTabbedPane) e.getSource();
                if (tabs.getSelectedIndex() == 4) {
                    ANONWalletUI.this.messagingPanel.tabSelected();
                }
            }
        );

    }

    public void exitProgram() {
        Log.info(LOCAL_MSG_EXITING);

        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        this.dashboard.stopThreadsAndTimers();
        this.addresses.stopThreadsAndTimers();
        this.sendPanel.stopThreadsAndTimers();

        ANONWalletUI.this.setVisible(false);
        ANONWalletUI.this.dispose();

        System.exit(0);
    }

    public static void main(String argv[])
        throws IOException {

        String title = LOCAL_MSG_UI_TITLE;
        title = title.concat(VERSION);
        try {
            OS_TYPE os = OSUtil.getOSType();

            if ((os == OS_TYPE.WINDOWS) || (os == OS_TYPE.MAC_OS)) {
                possiblyCreateANONConfig();
            }

            Log.info("Anonymous Full-Node Desktop Wallet (GUI, made in Java & Swing)");
            Log.info("OS: " + System.getProperty("os.name") + " = " + os);
            Log.info("Current directory: " + new File(".").getCanonicalPath());
            Log.info("Class path: " + System.getProperty("java.class.path"));
            Log.info("Environment PATH: " + System.getenv("PATH"));

            // Look and feel settings - a custom OS-look and feel is set for Windows
            if (os == OS_TYPE.WINDOWS) {
                // Custom Windows L&F and font settings
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");

                // This font looks good but on Windows 7 it misses some chars like the stars...
                //FontUIResource font = new FontUIResource("Lucida Sans Unicode", Font.PLAIN, 11);
                //UIManager.put("Table.font", font);
            } else if (os == OS_TYPE.MAC_OS) {
                // The MacOS L&F is active by default - the property sets the menu bar Mac style
                System.setProperty("apple.laf.useScreenMenuBar", "true");
            } else {
                for (LookAndFeelInfo ui : UIManager.getInstalledLookAndFeels()) {
                    Log.info("Available look and feel: " + ui.getName() + " " + ui.getClassName());
                    if (ui.getName().equals("Nimbus")) {
                        Log.info("Setting look and feel: {0}", ui.getClassName());
                        UIManager.setLookAndFeel(ui.getClassName());
                        break;
                    }
                    ;
                }
            }

            // If anond is currently not running, do a startup of the daemon as a child process
            // It may be started but not ready - then also show dialog
            ANONInstallationObserver initialInstallationObserver =
                new ANONInstallationObserver(OSUtil.getProgramDirectory());
            DaemonInfo zcashdInfo = initialInstallationObserver.getDaemonInfo();
            initialInstallationObserver = null;

            ANONClientCaller initialClientCaller = new ANONClientCaller(OSUtil.getProgramDirectory());
            boolean daemonStartInProgress = false;
            try {
                if (zcashdInfo.status == DAEMON_STATUS.RUNNING) {
                    NetworkAndBlockchainInfo info = initialClientCaller.getNetworkAndBlockchainInfo();
                    // If more than 20 minutes behind in the blockchain - startup in progress
                    if ((System.currentTimeMillis() - info.lastBlockDate.getTime()) > (20 * 60 * 1000)) {
                        Log.info("Current blockchain synchronization date is " +
                            new Date(info.lastBlockDate.getTime()));
                        daemonStartInProgress = true;
                    }
                }
            } catch (WalletCallException wce) {
                if ((wce.getMessage().indexOf("{\"code\":-28") != -1) || // Started but not Ready
                    (wce.getMessage().indexOf("error code: -28") != -1)) {
                    Log.info("anond is currently starting...");
                    daemonStartInProgress = true;
                }
            }

            StartupProgressDialog startupBar = null;
            if ((zcashdInfo.status != DAEMON_STATUS.RUNNING) || (daemonStartInProgress)) {
                Log.info(
                    "anond is not running at the moment or has not started/synchronized 100% - showing splash...");
                startupBar = new StartupProgressDialog(initialClientCaller);
                startupBar.setVisible(true);
                startupBar.waitForStartup();
            }
            initialClientCaller = null;

            // Main GUI is created here
            ANONWalletUI ui = new ANONWalletUI(startupBar, title);
            ui.setVisible(true);

        } catch (InstallationDetectionException ide) {
            Log.error("Installation Error: ", ide);
            JOptionPane.showMessageDialog(
                null,
                "This program was started in directory: " + OSUtil.getProgramDirectory() + "\n" +
                    ide.getMessage() + "\n" +
                    "See the console/logfile output for more detailed error information!",
                "Installation Error",
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        } catch (WalletCallException wce) {
            Log.error("WalletCall Error: ", wce);

            if ((wce.getMessage().indexOf("{\"code\":-28,\"message\"") != -1) ||
                (wce.getMessage().indexOf("error code: -28") != -1)) {
                JOptionPane.showMessageDialog(
                    null,
                    LOCAL_MSG_DAEMON_ERROR,
                    LOCAL_MSG_DAEMON_ERROR_TITLE,
                    JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(
                    null,
                    "There was a problem communicating with theAnonymousdaemon/wallet. \n" +
                        "Please ensure that theAnonymousserver anond is started (e.g. via \n" +
                        "command  \"anond --daemon\"). Error Message: \n" +
                        wce.getMessage() +
                        "See the console/logfile output for more detailed error information!",
                    "Daemon Error",
                    JOptionPane.ERROR_MESSAGE);
            }

            System.exit(2);
        } catch (Exception e) {
            Log.error("Unexpected error: ", e);
            JOptionPane.showMessageDialog(
                null,
                "An unexpected error (Exception) has occurred: \n" + e.getMessage() + "\n" +
                    "See the console/logfile output for more detailed error information!",
                "Unexpected Exception",
                JOptionPane.ERROR_MESSAGE);
            System.exit(3);
        } catch (Error err) {
            // Last resort catch for unexpected problems - just to inform the user
            err.printStackTrace();
            JOptionPane.showMessageDialog(
                null,
                "An unexpected error has occurred: \n" + err.getMessage() + "\n" +
                    "See the console/logfile output for more detailed error information!",
                "Unexpected Error",
                JOptionPane.ERROR_MESSAGE);
            System.exit(4);
        }
    }


    public static void possiblyCreateANONConfig()
        throws IOException {
        String blockchainDir = OSUtil.getBlockchainDirectory();
        File dir = new File(blockchainDir);

        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Log.error("ERROR: Could not create settings directory: " + dir.getCanonicalPath());
                throw new IOException("Could not create settings directory: " + dir.getCanonicalPath());
            }
        }

        File anonConfig = new File(dir, "anon.conf");

        if (!anonConfig.exists()) {

            Log.info("anon.conf (" + anonConfig.getCanonicalPath() +
                ") does not exist. It will be created with default settings.");

            PrintStream configOut = new PrintStream(new FileOutputStream(anonConfig));
            Random r = new Random(System.currentTimeMillis());
            configOut.println("# Generated RPC credentials");
            configOut.println("rpcallowip=127.0.0.1");
            configOut.println("rpcuser=User" + Math.abs(r.nextInt()));
            configOut.println("rpcpassword=Pass" + Math.abs(r.nextInt()) + "" +
                Math.abs(r.nextInt()) + "" +
                Math.abs(r.nextInt()));

            for (String node : getDefaultConfig()) {
                configOut.println(node);
            }

            configOut.close();
        }

    }

    private static List<String> getDefaultConfig() {
        BufferedReader br = null;
        InputStream is = ANONWalletUI.class.getResourceAsStream("/config/config.txt");
        br = new BufferedReader(new InputStreamReader(is));

        List<String> nodes = new ArrayList<>();
        String line;
        try {
            while ((line = br.readLine()) != null) {
                nodes.add(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return nodes;
    }
}
