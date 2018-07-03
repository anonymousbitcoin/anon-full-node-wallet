package org.btcprivate.wallets.fullnode.ui;

import org.btcprivate.wallets.fullnode.daemon.BTCPClientCaller;
import org.btcprivate.wallets.fullnode.daemon.BTCPClientCaller.NetworkAndBlockchainInfo;
import org.btcprivate.wallets.fullnode.daemon.BTCPClientCaller.WalletBalance;
import org.btcprivate.wallets.fullnode.daemon.BTCPClientCaller.WalletCallException;
import org.btcprivate.wallets.fullnode.daemon.BTCPInstallationObserver;
import org.btcprivate.wallets.fullnode.daemon.BTCPInstallationObserver.DaemonInfo;
import org.btcprivate.wallets.fullnode.daemon.DataGatheringThread;
import org.btcprivate.wallets.fullnode.util.*;
import org.btcprivate.wallets.fullnode.util.OSUtil.OS_TYPE;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;


/**
 * Dashboard ...
 *
 * @author Ivan Vaklinov <ivan@vaklinov.com>
 */
@SuppressWarnings({"deprecation"})
public class MyMasternodePanel
    extends WalletTabPanel {
  private JFrame parentFrame;
  private BTCPInstallationObserver installationObserver;
  private BTCPClientCaller clientCaller;
  private StatusUpdateErrorReporter errorReporter;
  private BackupTracker backupTracker;

  private DataGatheringThread<NetworkAndBlockchainInfo> netInfoGatheringThread = null;

  private Boolean walletIsEncrypted = null;
  private Integer blockchainPercentage = null;

  private JLabel daemonStatusLabel = null;
  private DataGatheringThread<DaemonInfo> daemonInfoGatheringThread = null;

  private JLabel walletBalanceLabel = null;
  private DataGatheringThread<WalletBalance> walletBalanceGatheringThread = null;
  private int counter = 15;
  private JTable transactionsTable = null;
  private JScrollPane transactionsTablePane = null;
  private String[][] lastMasternodesData = null;
  private DataGatheringThread<String[][]> transactionGatheringThread = null;

  private static final String small_icon_resource = "images/btcp-44.png";

  private static final String LOCAL_MSG_BTCP_WALLET_TITLE = Util.local("LOCAL_MSG_BTCP_WALLET_TITLE");
  private static final String LOCAL_MSG_BTCP_WALLET_TOOLTIP = Util.local("LOCAL_MSG_BTCP_WALLET_TOOLTIP");
  private static final String LOCAL_MSG_DAEMON_SINGLE_CONNECTION = Util.local("LOCAL_MSG_DAEMON_SINGLE_CONNECTION");
  private static final String LOCAL_MSG_DAEMON_CONNECTIONS = Util.local("LOCAL_MSG_DAEMON_CONNECTIONS");
  private static final String LOCAL_MSG_LOOKING_PEERS = Util.local("LOCAL_MSG_LOOKING_PEERS");
  private static final String LOCAL_MSG_T_BALANCE = Util.local("LOCAL_MSG_T_BALANCE");
  private static final String LOCAL_MSG_Z_BALANCE = Util.local("LOCAL_MSG_Z_BALANCE");
  private static final String LOCAL_MSG_TOTAL_BALANCE = Util.local("LOCAL_MSG_TOTAL_BALANCE");
  private static final String LOCAL_MSG_YES = Util.local("LOCAL_MSG_YES");
  private static final String LOCAL_MSG_NO = Util.local("LOCAL_MSG_NO");
  private static final String LOCAL_MSG_IMMATURE = Util.local("LOCAL_MSG_IMMATURE");
  private static final String LOCAL_MSG_IN = Util.local("LOCAL_MSG_IN");
  private static final String LOCAL_MSG_OUT = Util.local("LOCAL_MSG_OUT");
  private static final String LOCAL_MSG_MINED = Util.local("LOCAL_MSG_MINED");
  private static final String LOCAL_MSG_TXN_TYPE = Util.local("LOCAL_MSG_TXN_TYPE");
  private static final String LOCAL_MSG_TXN_DIRECTION = Util.local("LOCAL_MSG_TXN_DIRECTION");
  private static final String LOCAL_MSG_TXN_IS_CONFIRMED = Util.local("LOCAL_MSG_TXN_IS_CONFIRMED");
  private static final String LOCAL_MSG_TXN_AMOUNT = Util.local("LOCAL_MSG_TXN_AMOUNT");
  private static final String LOCAL_MSG_TXN_DATE = Util.local("LOCAL_MSG_TXN_DATE");
  private static final String LOCAL_MSG_TXN_DESTINATION = Util.local("LOCAL_MSG_TXN_DESTINATION");
  private static final String LOCAL_MSG_UNCONFIRMED_TOOLTIP = Util.local("LOCAL_MSG_UNCONFIRMED_TOOLTIP");
  private static final String LOCAL_MSG_UNCONFIRMED_TOOLTIP_B = Util.local("LOCAL_MSG_UNCONFIRMED_TOOLTIP_B");
  private static final String LOCAL_MSG_UNCONFIRMED_TOOLTIP_Z = Util.local("LOCAL_MSG_UNCONFIRMED_TOOLTIP_Z");

  private static final String LOCAL_MSG_MYMSTRNDE_ALIAS = Util.local("LOCAL_MSG_MYMSTRNDE_ALIAS");
  private static final String LOCAL_MSG_MYMSTRNDE_ADDRESS = Util.local("LOCAL_MSG_MYMSTRNDE_ADDRESS");
  private static final String LOCAL_MSG_MYMSTRNDE_PRIVATEKEY = Util.local("LOCAL_MSG_MYMSTRNDE_PRIVATEKEY");
  private static final String LOCAL_MSG_MYMSTRNDE_TXHASH = Util.local("LOCAL_MSG_MYMSTRNDE_TXHASH");
  private static final String LOCAL_MSG_MYMSTRNDE_OUTPUTINDEX = Util.local("LOCAL_MSG_MYMSTRNDE_OUTPUTINDEX");
  private static final String LOCAL_MSG_MYMSTRNDE_STATUS = Util.local("LOCAL_MSG_MYMSTRNDE_STATUS");

  private static final String LOCAL_MSG_SYNC = Util.local("LOCAL_MSG_SYNC");
  private static final String LOCAL_MSG_BLOCK = Util.local("LOCAL_MSG_BLOCK");

  private static final String daemon_txn_receive = "receive";
  private static final String daemon_txn_send = "send";
  private static final String daemon_txn_mined = "generate";
  private static final String daemon_txn_unconfirmed = "immature";


  public MyMasternodePanel(JFrame parentFrame,
                        BTCPInstallationObserver installationObserver,
                        BTCPClientCaller clientCaller,
                        StatusUpdateErrorReporter errorReporter,
                        BackupTracker backupTracker)
      throws IOException, InterruptedException, WalletCallException {
    this.parentFrame = parentFrame;
    this.installationObserver = installationObserver;
    this.clientCaller = clientCaller;
    this.errorReporter = errorReporter;
    this.backupTracker = backupTracker;

    this.timers = new ArrayList<>();
    this.threads = new ArrayList<>();

    // Build content
    JPanel dashboard = this;
    dashboard.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
    dashboard.setLayout(new BorderLayout(0, 0));

    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 3, 3));
    buttonPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

    // JButton startAliasButton = new JButton(LOCAL_MENU_NEW_B_ADDRESS);
    JButton startAliasButton = new JButton("Start Alias");
    buttonPanel.add(startAliasButton);

    // JButton startAllButton = new JButton(LOCAL_MENU_NEW_Z_ADDRESS);
    JButton startAllButton = new JButton("Start All");
    buttonPanel.add(startAllButton);

    // JButton startMissingButton = new JButton(LOCAL_MENU_REFRESH);
    JButton startMissingButton = new JButton("Start Missing");
    buttonPanel.add(startMissingButton);

    // JButton refreshButton = new JButton(LOCAL_MENU_REFRESH);
    JButton refreshButton = new JButton("Update Table");
    buttonPanel.add(refreshButton);

    JLabel updateLabelStart = new JLabel("Updating table in: ");
    buttonPanel.add(updateLabelStart);

    // int countDown = 0;
    JLabel updateTime = new JLabel("20");
    buttonPanel.add(updateTime);

    JLabel updateLabelEnd = new JLabel(" seconds");
    buttonPanel.add(updateLabelEnd);

    dashboard.add(buttonPanel, BorderLayout.SOUTH);

    // make clientcaller function to get names from list-conf

    // String[] pip = MyMasternodePanel.this.clientCaller.getMyAliases();
    // JComboBox myAliases = new JComboBox(pip);


    startAliasButton.addActionListener(e -> {
      try{
        String[] aliases = MyMasternodePanel.this.clientCaller.getMyAliases();
        Log.info(aliases.toString());
        String name = (String) JOptionPane.showInputDialog(MyMasternodePanel.this,
        "Please select from your list of aliases, \nprovided in the configuration file.",
        "Start Masternode By Alias",
        JOptionPane.PLAIN_MESSAGE,
        null,
        aliases,
        aliases[0]);

        if(name == null || "".equals(name)){
          return;
        }
        Log.info("alias0"+aliases[0].toString());
        Log.info("alias1"+aliases[1].toString());
        Log.info("+++++++++++++++++++++++");
        Log.info("namechosen: " + name);
        String response = this.clientCaller.startMasternodeByAlias(name);
        Log.info("response: " + response.toString());

        // myAliases.setEditable(true);

        // Object[] options = new Object[] {};
        // JOptionPane jop = new JOptionPane("Please Select",
        //                                 JOptionPane.QUESTION_MESSAGE,
        //                                 JOptionPane.DEFAULT_OPTION,
        //                                 null,options, null);

        // //add combos to JOptionPane
        // jop.add(myAliases);

        // //create a JDialog and add JOptionPane to it 
        // JDialog diag = new JDialog();
        // diag.getContentPane().add(jop);
        // diag.pack();
        // diag.setVisible(true);

      }catch (Exception ex) {
        Log.error("Error in startAlias:" + ex);
      }
    });

    ActionListener updateTimer = e -> {
      try
      {
        if (counter != -1){
          updateTime.setText("" + counter--);
         
        } else {
          counter = 15;
          startAllButton.setText("Start All");
        }
        
      } catch (Exception ex) {
        Log.error("Error in updateTimer:" + ex);
      }
    };

    Timer xy = new Timer(1000, updateTimer);
    xy.start();
    this.timers.add(xy);

    refreshButton.addActionListener(e -> {
      try{
        // Log.info("clicked!");
        // String response = this.clientCaller.executeMnsyncReset();
        // JLabel updatingLabel = new JLabel(response);
        // buttonPanel.add(updatingLabel);
        // Log.info(response);
        MyMasternodePanel.this.updateMasternodesTable();
        Log.info("Updating masternode status");
        counter = 15;
        Log.info("----------------------------------------------------");
        // String[] test = this.clientCaller.getMyAliases();
        // Log.info(test[0].toString()+ " " + test[1].toString());

      } catch (Exception ex)
      {
        Log.error("Eror in refreshButton: " + ex);
      }
    });

    startAllButton.addActionListener(e -> {
      try{
        String response = this.clientCaller.startAllMasternodes();
        Log.info(response.toString());
        counter = 15;
        startAllButton.setText("Starting...");
        // JPanel responsePanel = new JPanel();
        // responsePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 3, 3));
        // JLabel responseLabel = new JLabel(response.toString());
        // responsePanel.add(responseLabel);
        // dashboard.add(responsePanel, BorderLayout.SOUTH);

      }catch (Exception ex){
        Log.error("Error in startAllButton: " + ex);
      }
    });



    // Upper panel with wallet balance
    // JPanel balanceStatusPanel = new JPanel();
    // // Use border layout to have balances to the left
    // balanceStatusPanel.setLayout(new BorderLayout(3, 3));

    // JPanel tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 9));
    // JLabel logoLabel = new JLabel(new ImageIcon(
    //     this.getClass().getClassLoader().getResource(small_icon_resource)));
    // tempPanel.add(logoLabel);
    // JLabel btcpLabel = new JLabel(LOCAL_MSG_BTCP_WALLET_TITLE);
    // btcpLabel.setFont(new Font("Helvetica", Font.BOLD, 28));
    // tempPanel.add(btcpLabel);
    // tempPanel.setToolTipText(LOCAL_MSG_BTCP_WALLET_TOOLTIP);
    // balanceStatusPanel.add(tempPanel, BorderLayout.WEST);

    // balanceStatusPanel.add(tempPanel, BorderLayout.CENTER);

    // balanceStatusPanel.add(walletBalanceLabel = new JLabel(), BorderLayout.EAST);

    // dashboard.add(balanceStatusPanel, BorderLayout.NORTH);

    // Table of transactions
    lastMasternodesData = getMasternodeListFromRPC();
    dashboard.add(transactionsTablePane = new JScrollPane(
            transactionsTable = this.createMasternodesTable(lastMasternodesData)),BorderLayout.CENTER);

    // Lower panel with installation status
    // JPanel installationStatusPanel = new JPanel();
    // installationStatusPanel.setLayout(new BorderLayout());
    // installationStatusPanel.add(daemonStatusLabel = new JLabel(), BorderLayout.WEST);

    // dashboard.add(installationStatusPanel, BorderLayout.SOUTH);

    // Thread and timer to update the daemon status
    // this.daemonInfoGatheringThread = new DataGatheringThread<>(
    //     () -> {
    //       long start = System.currentTimeMillis();
    //       DaemonInfo daemonInfo = MyMasternodePanel.this.installationObserver.getDaemonInfo();
    //       long end = System.currentTimeMillis();
    //       Log.info("Gathering of dashboard daemon status data done in " + (end - start) + "ms.");

    //       return daemonInfo;
    //     },
    //     this.errorReporter, 2000, true);
    // this.threads.add(this.daemonInfoGatheringThread);

    // TODO USE THIS FOR SOMETHING ELSE LATER
    // ActionListener alDeamonStatus = e -> {
    //   try {
    //     MyMasternodePanel.this.updateStatusLabels();
    //   } catch (Exception ex) {
    //     Log.error("Unexpected error: ", ex);
    //     MyMasternodePanel.this.errorReporter.reportError(ex);
    //   }
    // };
    // Timer t = new Timer(1000, alDeamonStatus);
    // t.start();
    // this.timers.add(t);

    // Thread and timer to update the wallet balance
    // this.walletBalanceGatheringThread = new DataGatheringThread<>(
    //     () -> {
    //       long start = System.currentTimeMillis();
    //       WalletBalance balance = MyMasternodePanel.this.clientCaller.getWalletInfo();
    //       long end = System.currentTimeMillis();

    //       // TODO: move this call to a dedicated one-off gathering thread - this is the wrong place
    //       // it works but a better design is needed.
    //       if (MyMasternodePanel.this.walletIsEncrypted == null) {
    //         MyMasternodePanel.this.walletIsEncrypted = MyMasternodePanel.this.clientCaller.isWalletEncrypted();
    //       }

    //       Log.info("Gathering of dashboard wallet balance data done in " + (end - start) + "ms.");

    //       return balance;
    //     },
    //     this.errorReporter, 8000, true);
    // this.threads.add(this.walletBalanceGatheringThread);

    // ActionListener alWalletBalance = e -> {
    //   try {
    //     MyMasternodePanel.this.updateWalletStatusLabel();
    //   } catch (Exception ex) {
    //     Log.error("Unexpected error: ", ex);
    //     MyMasternodePanel.this.errorReporter.reportError(ex);
    //   }
    // };
    // Timer walletBalanceTimer = new Timer(2000, alWalletBalance);
    // walletBalanceTimer.setInitialDelay(1000);
    // walletBalanceTimer.start();
    // this.timers.add(walletBalanceTimer);

    // Thread and timer to update the transactions table
    this.transactionGatheringThread = new DataGatheringThread<>(
        () -> {
          long start = System.currentTimeMillis();
          String[][] data = MyMasternodePanel.this.getMasternodeListFromRPC();
          long end = System.currentTimeMillis();
          Log.info("Gathering MY MASTERNODES: " + (end - start) + "ms.");

          Log.info(data[1][5].toString());

          return data;
        },
        this.errorReporter, 20000);
    this.threads.add(this.transactionGatheringThread);

    ActionListener alMasternodes = e -> {
      try {
        MyMasternodePanel.this.updateMasternodesTable();
      } catch (Exception ex) {
        Log.error("Unexpected error: ", ex);
        MyMasternodePanel.this.errorReporter.reportError(ex);
      }
    };
    Timer t = new Timer(5000, alMasternodes);
    t.start();
    this.timers.add(t);

    // Thread and timer to update the network and blockchain details
    // this.netInfoGatheringThread = new DataGatheringThread<>(
    //     () -> {
    //       long start = System.currentTimeMillis();
    //       NetworkAndBlockchainInfo data = MyMasternodePanel.this.clientCaller.getNetworkAndBlockchainInfo();
    //       long end = System.currentTimeMillis();
    //       Log.info("Gathering of network and blockchain info data done in " + (end - start) + "ms.");

    //       return data;
    //     },
    //     this.errorReporter, 5000, true);
    // this.threads.add(this.netInfoGatheringThread);

    // ActionListener alNetAndBlockchain = e -> {
    //   try {
    //     MyMasternodePanel.this.updateStatusLabels();
    //   } catch (Exception ex) {
    //     Log.error("Unexpected error: ", ex);
    //     MyMasternodePanel.this.errorReporter.reportError(ex);
    //   }
    // };
    // Timer netAndBlockchainTimer = new Timer(5000, alNetAndBlockchain);
    // netAndBlockchainTimer.setInitialDelay(1000);
    // netAndBlockchainTimer.start();
    // this.timers.add(netAndBlockchainTimer);
  }


  // private void updateWalletStatusLabel()
  //     throws WalletCallException, IOException, InterruptedException {
  //   WalletBalance balance = this.walletBalanceGatheringThread.getLastData();

  //   // It is possible there has been no gathering initially
  //   if (balance == null) {
  //     return;
  //   }

  //   // Format double numbers - else sometimes we get exponential notation 1E-4 ZEN
  //   DecimalFormat df = new DecimalFormat("########0.00######");

  //   String transparentBalance = df.format(balance.transparentBalance);
  //   String privateBalance = df.format(balance.privateBalance);
  //   String totalBalance = df.format(balance.totalBalance);

  //   String transparentUCBalance = df.format(balance.transparentUnconfirmedBalance);
  //   String privateUCBalance = df.format(balance.privateUnconfirmedBalance);
  //   String totalUCBalance = df.format(balance.totalUnconfirmedBalance);

  //   String color1 = transparentBalance.equals(transparentUCBalance) ? "" : "color:#cc3300;";
  //   String color2 = privateBalance.equals(privateUCBalance) ? "" : "color:#cc3300;";
  //   String color3 = totalBalance.equals(totalUCBalance) ? "" : "color:#cc3300;";

  //   String text =
  //       "<html><p text-align: right>" +
  //           "<span style=\"" + color1 + "\">" + LOCAL_MSG_T_BALANCE + ": " +
  //           transparentUCBalance + " BTCP </span><br/> " +
  //           "<span style=\"" + color2 + "\">" + LOCAL_MSG_Z_BALANCE + ": " +
  //           privateUCBalance + " BTCP </span><br/> " +
  //           "<span style=\"" + color3 + "\">" + LOCAL_MSG_TOTAL_BALANCE +
  //           totalUCBalance + " BTCP </span>"
  //           + "</p></html>";

  //   this.walletBalanceLabel.setText(text);

  //   String toolTip = null;
  //   if ((!transparentBalance.equals(transparentUCBalance)) ||
  //       (!privateBalance.equals(privateUCBalance)) ||
  //       (!totalBalance.equals(totalUCBalance))) {
  //     toolTip = "<html>" +
  //         LOCAL_MSG_UNCONFIRMED_TOOLTIP +
  //         "<span style=\"font-size:5px\"><br/></span>" +
  //         LOCAL_MSG_UNCONFIRMED_TOOLTIP_B + ": " + transparentBalance + " BTCP<br/>" +
  //         LOCAL_MSG_UNCONFIRMED_TOOLTIP_Z + ": <span>" + privateBalance + " BTCP</span><br/>" +
  //         "Total: <span style=\"font-weight:bold\">" + totalBalance + " BTCP</span>" +
  //         "</html>";
  //   }

  //   this.walletBalanceLabel.setToolTipText(toolTip);

  //   if (this.parentFrame.isVisible()) {
  //     this.backupTracker.handleWalletBalanceUpdate(balance.totalBalance);
  //   }
  // }


  private void updateMasternodesTable()
      throws WalletCallException, IOException, InterruptedException {
    String[][] newMasternodesData = this.transactionGatheringThread.getLastData();

    // May be null - not even gathered once
    if (newMasternodesData == null) {
      return;
    }

    if (Util.arraysAreDifferent(lastMasternodesData, newMasternodesData)) {
      Log.info("Updating table of transactions");
      this.remove(transactionsTablePane);
      this.add(transactionsTablePane = new JScrollPane(
              transactionsTable = this.createMasternodesTable(newMasternodesData)),
          BorderLayout.CENTER);
    }

    lastMasternodesData = newMasternodesData;

    this.validate();
    this.repaint();
  }


  private JTable createMasternodesTable(String rowData[][])
      throws WalletCallException, IOException, InterruptedException {
    String columnNames[] = {LOCAL_MSG_MYMSTRNDE_ALIAS, LOCAL_MSG_MYMSTRNDE_ADDRESS, LOCAL_MSG_MYMSTRNDE_PRIVATEKEY, LOCAL_MSG_MYMSTRNDE_TXHASH, LOCAL_MSG_MYMSTRNDE_OUTPUTINDEX, LOCAL_MSG_MYMSTRNDE_STATUS};
    
    
    JTable table = new MasternodeTable(
        rowData, columnNames, this.parentFrame, this.clientCaller, this.installationObserver);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
    table.getColumnModel().getColumn(0).setPreferredWidth(300);
    table.getColumnModel().getColumn(1).setPreferredWidth(110);
    table.getColumnModel().getColumn(2).setPreferredWidth(100);
    table.getColumnModel().getColumn(3).setPreferredWidth(300);
    table.getColumnModel().getColumn(4).setPreferredWidth(180);
    table.getColumnModel().getColumn(5).setPreferredWidth(100);

    return table;
  }


  private String[][] getMasternodeListFromRPC()
      throws WalletCallException, IOException, InterruptedException {
    // Get available public+private transactions and unify them.
    // String[][] publicMasternodes = this.clientCaller.getWalletPublicMasternodes();
    // String[][] zReceivedMasternodes = this.clientCaller.getWalletZReceivedMasternodes();

    // String[][] allMasternodes = new String[publicMasternodes.length + zReceivedMasternodes.length][];

    // int i = 0;

    // for (String[] t : publicMasternodes) {
    //   allMasternodes[i++] = t;
    // }

    // for (String[] t : zReceivedMasternodes) {
    //   allMasternodes[i++] = t;
    // }

    // // Sort transactions by date
    // Arrays.sort(allMasternodes, (o1, o2) -> {
    //   Date d1 = new Date(0);
    //   if (!o1[4].equals("N/A")) {
    //     d1 = new Date(Long.valueOf(o1[4]).longValue() * 1000L);
    //   }

    //   Date d2 = new Date(0);
    //   if (!o2[4].equals("N/A")) {
    //     d2 = new Date(Long.valueOf(o2[4]).longValue() * 1000L);
    //   }

    //   if (d1.equals(d2)) {
    //     return 0;
    //   } else {
    //     return d2.compareTo(d1);
    //   }
    // });


    // // Confirmation symbols
    // String confirmed = "\u2690";
    // String notConfirmed = "\u2691";

    // // Windows does not support the flag symbol (Windows 7 by default)
    // // TODO: isolate OS-specific symbol codes in a separate class
    // OS_TYPE os = OSUtil.getOSType();
    // if (os == OS_TYPE.WINDOWS) {
    //   confirmed = " \u25B7";
    //   notConfirmed = " \u25B6";
    // }

    // DecimalFormat df = new DecimalFormat("########0.00######");

    // // Change the direction and date etc. attributes for presentation purposes
    // for (String[] trans : allMasternodes) {
    //   // Direction
    //   if (trans[1].equals(daemon_txn_receive)) {
    //     trans[1] = "\u21E8 " + LOCAL_MSG_IN;
    //   } else if (trans[1].equals(daemon_txn_send)) {
    //     trans[1] = "\u21E6 " + LOCAL_MSG_OUT;
    //   } else if (trans[1].equals(daemon_txn_mined)) {
    //     trans[1] = "\u2692\u2699 " + LOCAL_MSG_MINED;
    //   } else if (trans[1].equals(daemon_txn_unconfirmed)) {
    //     trans[1] = "\u2696 " + LOCAL_MSG_IMMATURE;
    //   }
    //   ;

    //   // Date
    //   if (!trans[4].equals("N/A")) {
    //     trans[4] = new Date(Long.valueOf(trans[4]).longValue() * 1000L).toLocaleString();
    //   }

    //   // Amount
    //   try {
    //     double amount = Double.valueOf(trans[3]);
    //     if (amount < 0d) {
    //       amount = -amount;
    //     }
    //     trans[3] = df.format(amount);
    //   } catch (NumberFormatException nfe) {
    //     Log.error("Error occurred while formatting amount: " + trans[3] +
    //         " - " + nfe.getMessage() + "!");
    //   }

    //   // Confirmed?
    //   try {
    //     boolean isConfirmed = !trans[2].trim().equals("0");

    //     trans[2] = isConfirmed ? (LOCAL_MSG_YES + " " + confirmed) : (LOCAL_MSG_NO + "  " + notConfirmed);
    //   } catch (NumberFormatException nfe) {
    //     Log.error("Error occurred while formatting confirmations: " + trans[2] +
    //         " - " + nfe.getMessage() + "!");
    //   }
    // }
    // String[][] mnListArrays = this.clientCaller.getMasternodeList();
    String[][] myMasternodes = this.clientCaller.getMyMasternodes();

    // return allMasternodes;
    return myMasternodes;
  }
} // End class