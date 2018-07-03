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

    // JButton updateTableButton = new JButton(LOCAL_MENU_REFRESH);
    JButton updateTableButton = new JButton("Update Table");
    buttonPanel.add(updateTableButton);

    JLabel updateLabelStart = new JLabel("Table is updated every 5 seconds");
    buttonPanel.add(updateLabelStart);

    dashboard.add(buttonPanel, BorderLayout.SOUTH);

    // JPanel installationStatusPanel = new JPanel();
    // installationStatusPanel.setLayout(new BorderLayout());


    // dashboard.add(installationStatusPanel, BorderLayout.SOUTH);

    // this.daemonStatusLabel.setText("stuff yeah");

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
        String response = this.clientCaller.startMasternodeByAlias(name);
        Log.info("response: " + response.toString());
        startAliasButton.setText("Starting " + name);


      }catch (Exception ex) {
        Log.error("Error in startAlias:" + ex);
      }
      counter = 5;
    });

    ActionListener updateTimer = e -> {
      try
      {
        if (counter == 0){
          startAllButton.setText("Start All");
          startAliasButton.setText("Start Alias");
          startMissingButton.setText("Start Missing");
          updateTableButton.setText("Update Table");
          // counter = 5;
        }
        counter--;
      } catch (Exception ex) {
        Log.error("Error in updateTimer:" + ex);
      }
    };

    Timer xy = new Timer(1000, updateTimer);
    xy.start();
    this.timers.add(xy);

    updateTableButton.addActionListener(e -> {
      try{
        MyMasternodePanel.this.updateMasternodesTable();
        Log.info("Updating masternode status");

        Log.info("----------------------------------------------------");
        updateTableButton.setText("Updating!");

      } catch (Exception ex)
      {
        Log.error("Eror in updateTableButton: " + ex);
      }
      counter = 5;
    });

    startAllButton.addActionListener(e -> {
      try{
        String response = this.clientCaller.startAllMasternodes();
        Log.info(response.toString());
        startAllButton.setText("Starting...");

      }catch (Exception ex){
        Log.error("Error in startAllButton: " + ex);
      }
      counter = 5;
    });

    startMissingButton.addActionListener(e -> {
      try
      {
        String response = this.clientCaller.startMissingMasternodes();
        Log.info(response.toString());
        startMissingButton.setText("Starting...");
      } catch (Exception ex){
        startMissingButton.setText("Not synced!");
        Log.error("Error in startMissingButton: " + ex);
      }
      counter = 5;
    });

    lastMasternodesData = getMasternodeListFromRPC();
    dashboard.add(daemonStatusLabel = new JLabel(), BorderLayout.NORTH);
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

    ActionListener alDeamonStatus = e -> {
      try {
        MyMasternodePanel.this.updateStatusLabels();
      } catch (Exception ex) {
        Log.error("Unexpected error: ", ex);
        MyMasternodePanel.this.errorReporter.reportError(ex);
      }
    };
    Timer syncTimer = new Timer(1000, alDeamonStatus);
    syncTimer.start();
    this.timers.add(syncTimer);



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

  private void updateStatusLabels()
      throws IOException, InterruptedException {
    // NetworkAndBlockchainInfo info = this.netInfoGatheringThread.getLastData();

    // // It is possible there has been no gathering initially
    // if (info == null) {
    //   return;
    // }

    // DaemonInfo daemonInfo = this.daemonInfoGatheringThread.getLastData();

    // // It is possible there has been no gathering initially
    // if (daemonInfo == null) {
    //   return;
    // }

    // // TODO: Get the start date right after ZClassic release - from first block!!!
    // final Date startDate = new Date("06 Nov 2016 02:00:00 GMT");
    // final Date nowDate = new Date(System.currentTimeMillis());

    // long fullTime = nowDate.getTime() - startDate.getTime();
    // long remainingTime = nowDate.getTime() - info.lastBlockDate.getTime();

    // String percentage = "100";
    // if (remainingTime > 20 * 60 * 1000) // TODO is this wrong? After 20 min we report 100% anyway
    // {
    //   double dPercentage = 100d - (((double) remainingTime / (double) fullTime) * 100d);
    //   if (dPercentage < 0) {
    //     dPercentage = 0;
    //   } else if (dPercentage > 100d) {
    //     dPercentage = 100d;
    //   }

    //   //TODO #.00 until 100%
    //   DecimalFormat df = new DecimalFormat("##0.##");
    //   percentage = df.format(dPercentage);

    //   // Also set a member that may be queried
    //   this.blockchainPercentage = new Integer((int) dPercentage);
    // } else {
    //   this.blockchainPercentage = 100;
    // }

    // // Just in case early on the call returns some junk date
    // if (info.lastBlockDate.before(startDate)) {
    //   // TODO: write log that we fix minimum date! - this condition should not occur
    //   info.lastBlockDate = startDate;
    // }

    // //String connections = " \u26D7";
    // String tickSymbol = " \u2705";
    // OS_TYPE os = OSUtil.getOSType();
    // // Handling special symbols on Mac OS/Windows
    // // TODO: isolate OS-specific symbol stuff in separate code
    // if ((os == OS_TYPE.MAC_OS) || (os == OS_TYPE.WINDOWS)) {
    //   //connections = " \u21D4";
    //   tickSymbol = " \u2606";
    // }

    // String tick = "<span style=\"font-weight:bold;color:green\">" + tickSymbol + "</span>";

    // String netColor = "black"; //"#808080";
    // if (info.numConnections > 2) {
    //   netColor = "green";
    // } else if (info.numConnections > 0) {
    //   netColor = "black";
    // }

    // String syncPercentageColor;
    // if (percentage.toString() == "100") {
    //   syncPercentageColor = "green";
    // } else {
    //   syncPercentageColor = "black";
    // }


    // DateFormat formatter = DateFormat.getDateTimeInstance();
    // String lastBlockDate = formatter.format(info.lastBlockDate);
    // StringBuilder stringBuilder = new StringBuilder();
    // stringBuilder.append("<html>");
    // stringBuilder.append("<span style=\"font-weight:bold;color:");
    // stringBuilder.append(netColor);
    // stringBuilder.append("\"> ");
    // if (info.numConnections == 1) {
    //   stringBuilder.append("1 " + LOCAL_MSG_DAEMON_SINGLE_CONNECTION + "</span>");
    // } else if (info.numConnections > 1) {
    //   stringBuilder.append(info.numConnections);
    //   stringBuilder.append(" " + LOCAL_MSG_DAEMON_CONNECTIONS + "</span>");
    // } else {
    //   stringBuilder.append(LOCAL_MSG_LOOKING_PEERS + "</span>");
    // }
    // stringBuilder.append("<br/><span style=\"font-weight:bold\">" + LOCAL_MSG_SYNC + " &nbsp;-&nbsp;</span><span style=\"font-weight:bold;color:");
    // stringBuilder.append(syncPercentageColor);
    // stringBuilder.append("\">");
    // stringBuilder.append(percentage);
    // stringBuilder.append("%</span><br/>");
    // stringBuilder.append("<span style=\"font-weight:bold\">" + LOCAL_MSG_BLOCK + "&nbsp;-&nbsp;");
    // stringBuilder.append(info.lastBlockHeight.trim());
    // stringBuilder.append("</span>");
    // stringBuilder.append(", " + LOCAL_MSG_MINED + " ");
    // stringBuilder.append(lastBlockDate);
    // stringBuilder.append("</span>");
    // String text =
    //     stringBuilder.toString();
    String text = "";
    try {
      text = this.clientCaller.getMasternodeSyncStatus(true);
    } catch (Exception e) {
      //TODO: handle exception
    }

    this.daemonStatusLabel.setText(text);
  }


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


  private String[][] getMasternodeListFromRPC() throws WalletCallException, IOException, InterruptedException {

    String[][] myMasternodes = this.clientCaller.getMyMasternodes();
    return myMasternodes;
  }
}
